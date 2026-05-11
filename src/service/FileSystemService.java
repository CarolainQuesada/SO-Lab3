package service;

import model.SistemaArchivos;
import util.InputUtil;
import java.util.List;

/**
 * Menu-driven service layer for the simple in-memory file system.
 *
 * <p>Bridges the console user and {@link SistemaArchivos}. Reads input via
 * {@link InputUtil}, delegates to the model, and prints results.</p>
 *
 * @author Carolain Quesada
 * @version 1.0
 */
public class FileSystemService {

    private final SistemaArchivos sistema = new SistemaArchivos();

    /**
     * Creates a service instance with a fresh simplified file system model.
     */
    public FileSystemService() {
    }

    /**
     * Starts the interactive service loop until the user selects option 0.
     */
    public void run() {
        boolean ejecutando = true;
        System.out.println("=== Sistema de Archivos en Memoria ===");
        while (ejecutando) {
            mostrarMenu();
            String opcion = InputUtil.readLine("Seleccione una opción: ");
            switch (opcion) {
                case "1"  -> listarDirectorio();
                case "2"  -> crearDirectorio();
                case "3"  -> crearArchivo();
                case "4"  -> leerArchivo();
                case "5"  -> escribirArchivo();
                case "6"  -> eliminarEntrada();
                case "7"  -> buscarPorNombre();
                case "8"  -> buscarEnContenido();
                case "9"  -> mostrarArbol();
                case "10" -> mostrarEstadisticas();
                case "0"  -> { ejecutando = false; System.out.println("Saliendo..."); }
                default   -> System.out.println("Opción no válida. Intente de nuevo.");
            }
        }
    }

    private void mostrarMenu() {
        System.out.println("\nMenú:");
        System.out.println(" 1. Listar directorio");   System.out.println(" 2. Crear directorio");
        System.out.println(" 3. Crear archivo");        System.out.println(" 4. Leer archivo");
        System.out.println(" 5. Escribir en archivo");  System.out.println(" 6. Eliminar archivo/directorio");
        System.out.println(" 7. Buscar por nombre");   System.out.println(" 8. Buscar en contenido");
        System.out.println(" 9. Mostrar árbol completo");System.out.println("10. Mostrar estadísticas");
        System.out.println(" 0. Salir");
    }

    /** Handles option 1: list directory. */
    private void listarDirectorio() {
        System.out.println(sistema.listarDirectorio(InputUtil.readLine("Ruta del directorio: ")));
    }

    /** Handles option 2: create directory (not supported by simplified model). */
    private void crearDirectorio() {
        String padre = InputUtil.readLine("Directorio padre: ");
        String nombre = InputUtil.readLine("Nombre del nuevo directorio: ");
        System.out.println(sistema.crearDirectorio(padre, nombre)
            ? "Directorio creado: " + padre + "/" + nombre
            : "No se pudo crear el directorio.");
    }

    /** Handles option 3: create file. */
    private void crearArchivo() {
        String padre    = InputUtil.readLine("Directorio donde crear el archivo: ");
        String nombre   = InputUtil.readLine("Nombre del archivo: ");
        String contenido = InputUtil.readLine("Contenido del archivo: ");
        System.out.println(sistema.crearArchivo(padre, nombre, contenido)
            ? "Archivo creado: " + padre + "/" + nombre
            : "No se pudo crear el archivo.");
    }

    /** Handles option 4: read file. */
    private void leerArchivo() {
        System.out.println("\n" + sistema.leerArchivo(InputUtil.readLine("Ruta del archivo: ")));
    }

    /** Handles option 5: write to file. */
    private void escribirArchivo() {
        String ruta      = InputUtil.readLine("Ruta del archivo: ");
        String contenido = InputUtil.readLine("Contenido a escribir: ");
        boolean append   = !"overwrite".equalsIgnoreCase(InputUtil.readLine("Modo (append/overwrite) [append]: "));
        System.out.println(sistema.escribirArchivo(ruta, contenido, append)
            ? "Contenido escrito en: " + ruta : "No se pudo escribir en el archivo.");
    }

    /** Handles option 6: delete entry. */
    private void eliminarEntrada() {
        String ruta = InputUtil.readLine("Ruta a eliminar: ");
        System.out.println(sistema.eliminarEntrada(ruta)
            ? "Entrada eliminada: " + ruta : "No se pudo eliminar.");
    }

    /** Handles option 7: search by name. */
    private void buscarPorNombre() {
        String consulta = InputUtil.readLine("Nombre de búsqueda: ");
        List<String> r  = sistema.buscarPorNombre(consulta);
        if (r.isEmpty()) { System.out.println("No se encontraron coincidencias."); return; }
        System.out.println("Resultados:"); r.forEach(x -> System.out.println("  " + x));
    }

    /** Handles option 8: search by content. */
    private void buscarEnContenido() {
        String consulta = InputUtil.readLine("Texto de búsqueda: ");
        List<String> r  = sistema.buscarEnContenido(consulta);
        if (r.isEmpty()) { System.out.println("No se encontraron coincidencias."); return; }
        System.out.println("Resultados:"); r.forEach(x -> System.out.println("  " + x));
    }

    /** Handles option 9: show file tree. */
    private void mostrarArbol() { System.out.println(sistema.imprimirArbol()); }

    /** Handles option 10: show statistics. */
    private void mostrarEstadisticas() { System.out.println(sistema.obtenerEstadisticas()); }
}
