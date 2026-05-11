package model;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Implements a simple block-based file system simulator.
 *
 * <p>The simulator manages a fixed-size pool of data blocks (see
 * {@link #TOTAL_BLOQUES} and {@link #TAMANIO_BLOQUE}), a boolean bitmap that
 * tracks which blocks are in use, a flat inode table ({@link #MAX_INODOS}),
 * and a string array that holds the actual block data.  There is no directory
 * hierarchy; all files live in a single namespace.</p>
 *
 * <h2>Life-cycle</h2>
 * <ol>
 *   <li>Create an instance with {@link #SistemaArchivos()}.</li>
 *   <li>Call {@link #inicializar()} to allocate internal structures and load
 *       previously persisted files from disk.</li>
 *   <li>Use {@link #crearArchivo}, {@link #leerArchivo}, and
 *       {@link #eliminarArchivo} to manipulate files.</li>
 * </ol>
 *
 * <h2>Persistence</h2>
 * <p>Files are automatically saved to and loaded from
 * {@code archivos_guardados.txt} (a Java {@link Properties} file) in the
 * working directory.  The file is deleted when there are no more stored
 * files.</p>
 *
 * @author Carolain Quesada
 * @version 1.0
 * @see Inodo
 * @see SuperBloque
 */
public class SistemaArchivos {

    /**
     * Total number of data blocks available in the simulated file system.
     * Every file's content is distributed across one or more of these blocks.
     */
    public static final int TOTAL_BLOQUES = 100;

    /**
     * Maximum number of characters stored in each data block.
     * Content that exceeds this size spans multiple blocks.
     */
    public static final int TAMANIO_BLOQUE = 64;

    /**
     * Maximum number of inode entries supported by the inode table.
     * Each file consumes exactly one inode entry.
     */
    public static final int MAX_INODOS = 100;

    /**
     * Path to the flat-file used for persistence between sessions.
     * The file uses the {@link Properties} format: one {@code name=content}
     * entry per stored file.
     */
    private static final Path ARCHIVO_PERSISTENCIA = Paths.get("archivos_guardados.txt");

    /**
     * The super block that tracks global allocation metadata (total blocks,
     * free blocks, block size).
     */
    private SuperBloque superBloque;

    /**
     * Boolean bitmap where {@code bitmap[i] == true} means block {@code i}
     * is currently occupied.
     */
    private boolean[] bitmap;

    /**
     * Flat inode table.  A {@code null} slot represents a free inode entry.
     */
    private Inodo[] tablaInodos;

    /**
     * Array of data block contents.  {@code bloquesDatos[i]} holds the
     * characters stored in block {@code i}, or {@code null} when the block
     * is free.
     */
    private String[] bloquesDatos;

    /**
     * Whether {@link #inicializar()} has been called at least once.
     * Operations that require an initialized file system check this flag
     * before proceeding.
     */
    private boolean inicializado;

    /**
     * Constructs a new {@code SistemaArchivos} in a non-initialized state.
     *
     * <p>Most operations will return an error message until
     * {@link #inicializar()} is called.</p>
     */
    public SistemaArchivos() {
        inicializado = false;
    }

    /**
     * Initializes the simulated file system and loads any previously persisted
     * files from disk.
     *
     * <p>This method resets all internal data structures (super block, bitmap,
     * inode table, data blocks) and then attempts to reload files saved in
     * {@code archivos_guardados.txt}.  Calling this method a second time on an
     * already-initialized instance will discard all current in-memory data and
     * start fresh.</p>
     */
    public void inicializar() {
        inicializar(true);
    }

    /**
     * Internal initialization method.
     *
     * @param cargarGuardados {@code true} to reload persisted files from disk;
     *                        {@code false} to start with a clean file system
     *                        (used by the fragmentation demo)
     */
    private void inicializar(boolean cargarGuardados) {
        superBloque = new SuperBloque(TOTAL_BLOQUES, TAMANIO_BLOQUE);
        bitmap = new boolean[TOTAL_BLOQUES];
        tablaInodos = new Inodo[MAX_INODOS];
        bloquesDatos = new String[TOTAL_BLOQUES];
        inicializado = true;
        if (cargarGuardados) {
            cargarDesdeTxt();
        }
    }

    /**
     * Indicates whether the file system has already been initialized.
     *
     * @return {@code true} if {@link #inicializar()} has been called;
     *         {@code false} otherwise
     */
    public boolean estaInicializado() {
        return inicializado;
    }

    /**
     * Creates a new file and distributes its content across available data
     * blocks.  The result is also persisted to disk.
     *
     * <p>The file name is normalized (trimmed) and validated before proceeding.
     * Duplicate names and full inode tables are also checked.</p>
     *
     * @param nombreArchivo the name for the new file; must not be blank and
     *                      must match {@code [A-Za-z0-9._-]+}
     * @param contenido     the text content to store; {@code null} is treated
     *                      as an empty string
     * @return a human-readable status message:
     *         <ul>
     *           <li>on success — the assigned block indexes</li>
     *           <li>on failure — a message describing the reason</li>
     *         </ul>
     */
    public String crearArchivo(String nombreArchivo, String contenido) {
        return crearArchivo(nombreArchivo, contenido, true);
    }

    /**
     * Internal file creation helper.
     *
     * @param nombreArchivo  the file name
     * @param contenido      the file content
     * @param guardarCambios {@code true} to persist changes to disk
     * @return a status message
     */
    private String crearArchivo(String nombreArchivo, String contenido, boolean guardarCambios) {
        if (!inicializado) {
            return "Error: primero debe inicializar el sistema de archivos con la opcion 1.";
        }

        String nombreValidado = normalizarNombre(nombreArchivo);
        String errorNombre = validarNombreArchivo(nombreValidado);
        if (errorNombre != null) {
            return errorNombre;
        }
        if (buscarInodo(nombreValidado) != null) {
            return "Error: ya existe un archivo con ese nombre.";
        }
        int posicionInodo = buscarPosicionInodoLibre();
        if (posicionInodo == -1) {
            return "Error: la tabla de inodos esta llena.";
        }

        String texto = contenido == null ? "" : contenido;
        int bloquesNecesarios = calcularBloquesNecesarios(texto);
        if (bloquesNecesarios > superBloque.getBloquesLibres()) {
            int caracteresDisponibles = superBloque.getBloquesLibres() * TAMANIO_BLOQUE;
            return "Error: no hay espacio suficiente. El archivo necesita "
                    + bloquesNecesarios + " bloques, pero solo hay "
                    + superBloque.getBloquesLibres() + " bloques libres ("
                    + caracteresDisponibles + " caracteres disponibles).";
        }

        int[] bloquesAsignados = asignarBloques(texto, bloquesNecesarios);
        tablaInodos[posicionInodo] = new Inodo(nombreValidado, texto.length(), bloquesAsignados);
        String advertencia = guardarCambios ? guardarEnTxt() : "";
        return "Archivo creado correctamente. Bloques asignados: "
                + Arrays.toString(bloquesAsignados)
                + advertencia;
    }

    /**
     * Reads the full content of an existing file.
     *
     * <p>The content is reconstructed by concatenating each assigned data
     * block.  If padding was added to the last block during creation, it is
     * trimmed to match the stored file size.</p>
     *
     * @param nombreArchivo the name of the file to read
     * @return the file content, or a human-readable error message if the file
     *         does not exist or the system has not been initialized
     */
    public String leerArchivo(String nombreArchivo) {
        if (!inicializado) {
            return "Error: primero debe inicializar el sistema de archivos con la opcion 1.";
        }

        String nombreValidado = normalizarNombre(nombreArchivo);
        String errorNombre = validarNombreArchivo(nombreValidado);
        if (errorNombre != null) {
            return errorNombre;
        }

        Inodo inodo = buscarInodo(nombreValidado);
        if (inodo == null) {
            return "Error: archivo no encontrado.";
        }

        StringBuilder contenido = new StringBuilder();
        for (int bloque : inodo.getBloquesAsignados()) {
            contenido.append(bloquesDatos[bloque]);
        }

        if (contenido.length() > inodo.getTamanioArchivo()) {
            return contenido.substring(0, inodo.getTamanioArchivo());
        }
        return contenido.toString();
    }

    /**
     * Deletes an existing file and releases all of its assigned data blocks.
     * The change is also persisted to disk.
     *
     * @param nombreArchivo the name of the file to delete
     * @return a human-readable status message describing the result
     */
    public String eliminarArchivo(String nombreArchivo) {
        return eliminarArchivo(nombreArchivo, true);
    }

    /**
     * Internal file deletion helper.
     *
     * @param nombreArchivo  the file name
     * @param guardarCambios {@code true} to persist the change to disk
     * @return a status message
     */
    private String eliminarArchivo(String nombreArchivo, boolean guardarCambios) {
        if (!inicializado) {
            return "Error: primero debe inicializar el sistema de archivos con la opcion 1.";
        }

        String nombreValidado = normalizarNombre(nombreArchivo);
        String errorNombre = validarNombreArchivo(nombreValidado);
        if (errorNombre != null) {
            return errorNombre;
        }

        for (int i = 0; i < tablaInodos.length; i++) {
            Inodo inodo = tablaInodos[i];
            if (inodo != null && inodo.getNombreArchivo().equals(nombreValidado)) {
                for (int bloque : inodo.getBloquesAsignados()) {
                    bitmap[bloque] = false;
                    bloquesDatos[bloque] = null;
                    superBloque.liberarBloque();
                }
                tablaInodos[i] = null;
                String advertencia = guardarCambios ? guardarEnTxt() : "";
                return "Archivo eliminado correctamente." + advertencia;
            }
        }
        return "Error: archivo no encontrado.";
    }

    /**
     * Builds a detailed report of the current file system state.
     *
     * <p>The report includes total blocks, free blocks, occupied blocks,
     * the current bitmap, a list of stored files with their sizes and block
     * assignments, and a fragmentation analysis.</p>
     *
     * @return a formatted multi-line status report
     */
    public String mostrarEstado() {
        if (!inicializado) {
            return "Error: primero debe inicializar el sistema de archivos con la opcion 1.";
        }

        StringBuilder estado = new StringBuilder();
        estado.append("=== Estado del Sistema de Archivos ===\n");
        estado.append("Total de bloques: ").append(superBloque.getTotalBloques()).append("\n");
        estado.append("Tamano de bloque: ").append(superBloque.getTamanioBloque()).append(" caracteres\n");
        estado.append("Bloques libres: ").append(superBloque.getBloquesLibres()).append("\n");
        estado.append("Bloques ocupados: ").append(superBloque.getBloquesOcupados()).append("\n");
        estado.append("Bitmap: ").append(mostrarBitmap()).append("\n");
        estado.append("Archivos almacenados:\n");

        boolean hayArchivos = false;
        for (Inodo inodo : tablaInodos) {
            if (inodo != null) {
                hayArchivos = true;
                estado.append("  - ")
                        .append(inodo.getNombreArchivo())
                        .append(" (")
                        .append(inodo.getTamanioArchivo())
                        .append(" caracteres) -> bloques ")
                        .append(Arrays.toString(inodo.getBloquesAsignados()))
                        .append("\n");
            }
        }

        if (!hayArchivos) {
            estado.append("  No hay archivos almacenados.\n");
        }

        estado.append("Fragmentacion: ").append(describirFragmentacion());
        return estado.toString();
    }

    /**
     * Returns a compact string representation of the block bitmap.
     *
     * <p>Each character is either {@code '1'} (block occupied) or {@code '0'}
     * (block free).  A space separator is inserted after every tenth character
     * to aid readability.</p>
     *
     * @return the formatted bitmap string, e.g.
     *         {@code "1100000000 0000000000 ..."}
     */
    public String mostrarBitmap() {
        if (!inicializado) {
            return "Error: primero debe inicializar el sistema de archivos con la opcion 1.";
        }

        StringBuilder salida = new StringBuilder();
        for (int i = 0; i < bitmap.length; i++) {
            salida.append(bitmap[i] ? "1" : "0");
            if ((i + 1) % 10 == 0 && i < bitmap.length - 1) {
                salida.append(" ");
            }
        }
        return salida.toString();
    }

    /**
     * Runs a predefined sequence of operations that demonstrates external
     * fragmentation.
     *
     * The sequence is:
     * <ol>
     *   <li>Re-initialize the file system without loading saved files.</li>
     *   <li>Create files A, B, and C.</li>
     *   <li>Delete file B, leaving a gap in the bitmap.</li>
     *   <li>Create file D, which is large enough to fill the gap and may
     *       therefore be stored in non-contiguous blocks.</li>
     * </ol>
     *
     * @return a formatted explanation of the simulation followed by the
     *         resulting file system state report
     */
    public String simularFragmentacion() {
        inicializar(false);
        crearArchivo("A.txt", repetir("A", 130), false);
        crearArchivo("B.txt", repetir("B", 70), false);
        crearArchivo("C.txt", repetir("C", 150), false);
        eliminarArchivo("B.txt", false);
        crearArchivo("D.txt", repetir("D", 180), false);

        return "Simulacion realizada: se crearon A, B y C; se elimino B; luego se creo D.\n"
                + "El archivo D reutiliza huecos libres y puede quedar en bloques no contiguos.\n\n"
                + mostrarEstado();
    }

    /**
     * Lists the contents of a directory path.
     *
     * <p>In this simplified model there is no real directory structure, so
     * this method delegates directly to {@link #mostrarEstado()}.</p>
     *
     * @param ruta the directory path (ignored by this simplified model)
     * @return the current file system state report
     */
    public String listarDirectorio(String ruta) {
        return mostrarEstado();
    }

    /**
     * Attempts to create a directory.
     *
     * <p>This simplified model does not support directories; this method
     * always returns {@code false}.</p>
     *
     * @param rutaPadre the parent directory path (ignored)
     * @param nombre    the directory name (ignored)
     * @return {@code false} because directories are not supported
     */
    public boolean crearDirectorio(String rutaPadre, String nombre) {
        return false;
    }

    /**
     * Creates a file using a directory-oriented service signature.
     *
     * <p>The {@code rutaPadre} argument is ignored by this simplified model;
     * files are stored directly in the flat namespace.</p>
     *
     * @param rutaPadre the parent path (ignored)
     * @param nombre    the file name
     * @param contenido the file content
     * @return {@code true} if the file was created successfully;
     *         {@code false} otherwise
     */
    public boolean crearArchivo(String rutaPadre, String nombre, String contenido) {
        return crearArchivo(nombre, contenido).startsWith("Archivo creado");
    }

    /**
     * Writes content to an existing file, either overwriting or appending.
     *
     * <p>Internally this method deletes the old file and recreates it with
     * the updated content.</p>
     *
     * @param ruta      the file path; only the last component after the final
     *                  {@code '/'} is used as the file name
     * @param contenido the content to write
     * @param append    {@code true} to append to the existing content;
     *                  {@code false} to overwrite it
     * @return {@code true} if the write succeeded; {@code false} if the file
     *         does not exist or the system is not initialized
     */
    public boolean escribirArchivo(String ruta, String contenido, boolean append) {
        if (!inicializado) {
            return false;
        }
        String nombre = extraerNombre(ruta);
        Inodo inodo = buscarInodo(nombre);
        if (inodo == null) {
            return false;
        }

        String nuevoContenido = append ? leerArchivo(nombre) + contenido : contenido;
        eliminarArchivo(nombre);
        return crearArchivo(nombre, nuevoContenido).startsWith("Archivo creado");
    }

    /**
     * Deletes a file entry identified by its path.
     *
     * @param ruta the file path; only the last component after the final
     *             {@code '/'} is used as the file name
     * @return {@code true} if the file was deleted; {@code false} otherwise
     */
    public boolean eliminarEntrada(String ruta) {
        return eliminarArchivo(extraerNombre(ruta)).startsWith("Archivo eliminado");
    }

    /**
     * Searches all stored files by name using a case-insensitive substring
     * match.
     *
     * @param consulta the search text; {@code null} or blank returns an empty
     *                 list
     * @return an unmodifiable list of matching file paths in the form
     *         {@code "/fileName"}; never {@code null}
     */
    public List<String> buscarPorNombre(String consulta) {
        List<String> resultados = new ArrayList<>();
        if (!inicializado) {
            return resultados;
        }
        if (consulta == null || consulta.isBlank()) {
            return resultados;
        }
        String texto = consulta.toLowerCase();
        for (Inodo inodo : tablaInodos) {
            if (inodo != null && inodo.getNombreArchivo().toLowerCase().contains(texto)) {
                resultados.add("/" + inodo.getNombreArchivo());
            }
        }
        return resultados;
    }

    /**
     * Searches all stored files by content using a case-insensitive substring
     * match.
     *
     * @param consulta the search text; {@code null} or blank returns an empty
     *                 list
     * @return a list of file paths whose content contains the search text;
     *         never {@code null}
     */
    public List<String> buscarEnContenido(String consulta) {
        List<String> resultados = new ArrayList<>();
        if (!inicializado) {
            return resultados;
        }
        if (consulta == null || consulta.isBlank()) {
            return resultados;
        }
        String texto = consulta.toLowerCase();
        for (Inodo inodo : tablaInodos) {
            if (inodo != null && leerArchivo(inodo.getNombreArchivo()).toLowerCase().contains(texto)) {
                resultados.add("/" + inodo.getNombreArchivo());
            }
        }
        return resultados;
    }

    /**
     * Returns a simple tree-like representation of all stored files.
     *
     * <p>The output always starts with {@code "/"} (representing the root)
     * followed by an indented entry for each stored file.</p>
     *
     * @return a formatted tree string, or an error message if the system has
     *         not been initialized
     */
    public String imprimirArbol() {
        if (!inicializado) {
            return "Error: primero debe inicializar el sistema de archivos con la opcion 1.";
        }

        StringBuilder arbol = new StringBuilder("/\n");
        for (Inodo inodo : tablaInodos) {
            if (inodo != null) {
                arbol.append("  ").append(inodo.getNombreArchivo()).append("\n");
            }
        }
        return arbol.toString();
    }

    /**
     * Returns file system statistics.
     *
     * <p>This method is a convenience alias for {@link #mostrarEstado()}.</p>
     *
     * @return a formatted statistics report
     */
    public String obtenerEstadisticas() {
        return mostrarEstado();
    }

    // -----------------------------------------------------------------------
    // Private helpers
    // -----------------------------------------------------------------------

    /**
     * Calculates the number of data blocks required to store the given content.
     *
     * <p>An empty string still requires one block.</p>
     *
     * @param contenido the text to be stored
     * @return the number of blocks needed; always {@code >= 1}
     */
    private int calcularBloquesNecesarios(String contenido) {
        if (contenido.isEmpty()) {
            return 1;
        }
        return (int) Math.ceil((double) contenido.length() / TAMANIO_BLOQUE);
    }

    /**
     * Finds free blocks in the bitmap, writes content slices to them, and
     * returns the array of assigned block indexes.
     *
     * @param contenido         the content to distribute across blocks
     * @param bloquesNecesarios the exact number of blocks to allocate
     * @return the allocated block indexes in assignment order
     */
    private int[] asignarBloques(String contenido, int bloquesNecesarios) {
        int[] bloquesAsignados = new int[bloquesNecesarios];
        int indiceAsignado = 0;

        for (int i = 0; i < bitmap.length && indiceAsignado < bloquesNecesarios; i++) {
            if (!bitmap[i]) {
                int inicio = indiceAsignado * TAMANIO_BLOQUE;
                int fin = Math.min(inicio + TAMANIO_BLOQUE, contenido.length());
                bloquesDatos[i] = inicio < contenido.length() ? contenido.substring(inicio, fin) : "";
                bitmap[i] = true;
                superBloque.reservarBloque();
                bloquesAsignados[indiceAsignado] = i;
                indiceAsignado++;
            }
        }

        return bloquesAsignados;
    }

    /**
     * Searches the inode table for an entry matching the given file name.
     *
     * @param nombreArchivo the file name to look up
     * @return the matching {@link Inodo}, or {@code null} when not found
     */
    private Inodo buscarInodo(String nombreArchivo) {
        for (Inodo inodo : tablaInodos) {
            if (inodo != null && inodo.getNombreArchivo().equals(nombreArchivo)) {
                return inodo;
            }
        }
        return null;
    }

    /**
     * Normalizes a file name by trimming whitespace.  Returns an empty string
     * if the input is {@code null}.
     *
     * @param nombreArchivo the raw file name
     * @return the trimmed name, never {@code null}
     */
    private String normalizarNombre(String nombreArchivo) {
        return nombreArchivo == null ? "" : nombreArchivo.trim();
    }

    /**
     * Validates a file name against the allowed character set and reserved
     * names.
     *
     * <p>Allowed characters: letters (a-z, A-Z), digits (0-9), dot ({@code .}),
     * hyphen ({@code -}), and underscore ({@code _}).  The names {@code .} and
     * {@code ..} are reserved.</p>
     *
     * @param nombreArchivo the normalized file name to validate
     * @return {@code null} when the name is valid; otherwise a human-readable
     *         error message
     */
    private String validarNombreArchivo(String nombreArchivo) {
        if (nombreArchivo == null || nombreArchivo.isBlank()) {
            return "Error: el nombre del archivo no puede estar vacio.";
        }
        if (!nombreArchivo.matches("[A-Za-z0-9._-]+")) {
            return "Error: nombre de archivo invalido. Use solo letras, numeros, punto, guion o guion bajo; sin espacios ni barras.";
        }
        if (nombreArchivo.equals(".") || nombreArchivo.equals("..")) {
            return "Error: nombre de archivo reservado.";
        }
        return null;
    }

    /**
     * Finds the index of the first free slot in the inode table.
     *
     * @return the free slot index, or {@code -1} if the table is full
     */
    private int buscarPosicionInodoLibre() {
        for (int i = 0; i < tablaInodos.length; i++) {
            if (tablaInodos[i] == null) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Builds a human-readable fragmentation report.
     *
     * <p>A file is considered fragmented when its assigned blocks are not
     * consecutive.  Returns a neutral message when no fragmentation is found.</p>
     *
     * @return a description of fragmented files, or
     *         {@code "no se detectan archivos fragmentados"} when none exist
     */
    private String describirFragmentacion() {
        StringBuilder resultado = new StringBuilder();
        boolean encontroFragmentacion = false;

        for (Inodo inodo : tablaInodos) {
            if (inodo == null) {
                continue;
            }
            int[] bloques = inodo.getBloquesAsignados();
            if (bloques.length > 1 && !sonContiguos(bloques)) {
                if (!encontroFragmentacion) {
                    resultado.append("detectada en ");
                } else {
                    resultado.append(", ");
                }
                resultado.append(inodo.getNombreArchivo())
                        .append(" ")
                        .append(Arrays.toString(bloques));
                encontroFragmentacion = true;
            }
        }

        return encontroFragmentacion ? resultado.toString() : "no se detectan archivos fragmentados";
    }

    /**
     * Checks whether the elements of an integer array form a contiguous
     * sequence (each element equals the previous one plus one).
     *
     * @param bloques the block index array to test; must have length {@code >= 1}
     * @return {@code true} if all elements are contiguous; {@code false} otherwise
     */
    private boolean sonContiguos(int[] bloques) {
        for (int i = 1; i < bloques.length; i++) {
            if (bloques[i] != bloques[i - 1] + 1) {
                return false;
            }
        }
        return true;
    }

    /**
     * Builds a string by repeating the given text the specified number of
     * times.
     *
     * @param texto the text to repeat
     * @param veces the number of repetitions
     * @return the repeated string
     */
    private String repetir(String texto, int veces) {
        StringBuilder salida = new StringBuilder();
        for (int i = 0; i < veces; i++) {
            salida.append(texto);
        }
        return salida.toString();
    }

    /**
     * Extracts the base file name from a path string.
     *
     * <p>Both {@code '/'} and {@code '\\'} are treated as path separators.
     * If the path contains no separator, the whole string is returned.</p>
     *
     * @param ruta the path string; may be {@code null} or blank
     * @return the last path component, or an empty string when the input is
     *         {@code null} or blank
     */
    private String extraerNombre(String ruta) {
        if (ruta == null || ruta.isBlank()) {
            return "";
        }
        String normalizada = ruta.replace('\\', '/').trim();
        int indice = normalizada.lastIndexOf('/');
        return indice >= 0 ? normalizada.substring(indice + 1) : normalizada;
    }

    /**
     * Loads previously saved files from the persistence file into the in-memory
     * file system.
     *
     * <p>Each entry in the {@link Properties} file is treated as a file name
     * mapping to its content.  Errors are reported to standard output as
     * warnings and do not abort initialization.</p>
     */
    private void cargarDesdeTxt() {
        if (!Files.exists(ARCHIVO_PERSISTENCIA)) {
            return;
        }

        Properties archivos = new Properties();
        try (Reader reader = Files.newBufferedReader(ARCHIVO_PERSISTENCIA, StandardCharsets.UTF_8)) {
            archivos.load(reader);
            for (String nombre : archivos.stringPropertyNames()) {
                crearArchivo(nombre, archivos.getProperty(nombre, ""), false);
            }
        } catch (IOException ex) {
            System.out.println("Advertencia: no se pudo cargar " + ARCHIVO_PERSISTENCIA + ": " + ex.getMessage());
        }
    }

    /**
     * Saves the current in-memory file system state to the persistence file.
     *
     * <p>All active inodes (non-null entries in the inode table) are written
     * as {@code name=content} entries.  If there are no files, the persistence
     * file is deleted.  Any I/O error is returned as a warning string rather
     * than throwing an exception.</p>
     *
     * @return an empty string on success, or a warning message when the write
     *         fails
     */
    private String guardarEnTxt() {
        Properties archivos = new Properties();
        for (Inodo inodo : tablaInodos) {
            if (inodo != null) {
                archivos.setProperty(inodo.getNombreArchivo(), reconstruirContenido(inodo));
            }
        }

        try {
            if (archivos.isEmpty()) {
                Files.deleteIfExists(ARCHIVO_PERSISTENCIA);
                return "";
            }
            try (Writer writer = Files.newBufferedWriter(ARCHIVO_PERSISTENCIA, StandardCharsets.UTF_8)) {
                archivos.store(writer, "Archivos guardados del simulador");
            }
            return "";
        } catch (IOException ex) {
            return " Advertencia: no se pudo actualizar " + ARCHIVO_PERSISTENCIA + ".";
        }
    }

    /**
     * Reconstructs the full content of a file by concatenating its data
     * blocks and trimming any trailing padding.
     *
     * @param inodo the inode whose content is to be reconstructed
     * @return the reconstructed file content; never {@code null}
     */
    private String reconstruirContenido(Inodo inodo) {
        StringBuilder contenido = new StringBuilder();
        for (int bloque : inodo.getBloquesAsignados()) {
            contenido.append(bloquesDatos[bloque]);
        }
        if (contenido.length() > inodo.getTamanioArchivo()) {
            return contenido.substring(0, inodo.getTamanioArchivo());
        }
        return contenido.toString();
    }
}
