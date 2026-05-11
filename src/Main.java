import java.util.Scanner;
import model.SistemaArchivos;

/**
 * Console entry point for the simple file system simulator.
 *
 * <p>Presents an interactive text menu that lets the user initialize the file
 * system, create files, read files, delete files, inspect the block bitmap,
 * display system status, and run a fragmentation demo.  The loop continues
 * until the user selects the exit option.</p>
 *
 * <p>Example session:</p>
 * <pre>{@code
 * === Simulador de Sistema de Archivos ===
 * 1. Inicializar sistema de archivos.
 * 2. Crear archivo.
 * ...
 * Seleccione una opcion: 1
 * Sistema de archivos inicializado con 100 bloques de 64 caracteres.
 * }</pre>
 *
 * @author Carolain Quesada
 * @version 1.0
 * @see SistemaArchivos
 */
public class Main {

    /**
     * Shared {@link Scanner} used for all console input in this class.
     * Declared as a class-level constant to avoid creating multiple scanner
     * instances, which can cause input-stream conflicts.
     */
    private static final Scanner SCANNER = new Scanner(System.in);

    /**
     * Prevents instantiation of this application entry-point class.
     */
    private Main() {
    }

    /**
     * Application entry point.  Builds a {@link SistemaArchivos} instance and
     * enters the interactive menu loop.  The loop terminates when the user
     * chooses option {@code 7} (exit).
     *
     * @param args command-line arguments; not used by this program
     */
    public static void main(String[] args) {
        SistemaArchivos sistema = new SistemaArchivos();
        boolean continuar = true;

        System.out.println("=== Simulador de Sistema de Archivos ===");

        while (continuar) {
            mostrarMenu();
            String opcion = leerOpcionMenu();

            switch (opcion) {
                case "1" -> inicializarSistema(sistema);
                case "2" -> crearArchivo(sistema);
                case "3" -> leerArchivo(sistema);
                case "4" -> eliminarArchivo(sistema);
                case "5" -> System.out.println(sistema.mostrarEstado());
                case "6" -> System.out.println("Bitmap: " + sistema.mostrarBitmap());
                case "7" -> {
                    continuar = false;
                    System.out.println("Saliendo del sistema...");
                }
                case "8" -> System.out.println(sistema.simularFragmentacion());
                default -> System.out.println("Opcion no valida. Intente de nuevo.");
            }
        }

        SCANNER.close();
    }

    /**
     * Prints the main menu options to standard output.
     */
    private static void mostrarMenu() {
        System.out.println();
        System.out.println("1. Inicializar sistema de archivos.");
        System.out.println("2. Crear archivo.");
        System.out.println("3. Leer archivo.");
        System.out.println("4. Eliminar archivo.");
        System.out.println("5. Mostrar estado del sistema.");
        System.out.println("6. Mostrar bitmap.");
        System.out.println("7. Salir.");
        System.out.println("8. Simular fragmentacion.");
    }

    /**
     * Initializes the file system, asking for confirmation before resetting an
     * already initialized instance.
     *
     * @param sistema the file system instance to initialize
     */
    private static void inicializarSistema(SistemaArchivos sistema) {
        if (sistema.estaInicializado()
                && !confirmar("El sistema ya esta inicializado. Desea reiniciarlo? (S/N): ")) {
            System.out.println("Inicializacion cancelada.");
            return;
        }

        sistema.inicializar();
        System.out.println("Sistema de archivos inicializado con 100 bloques de 64 caracteres.");
    }

    /**
     * Interactively prompts the user for a file name and multi-line content,
     * then delegates creation to {@link SistemaArchivos#crearArchivo(String, String)}.
     *
     * @param sistema the file system instance to operate on
     */
    private static void crearArchivo(SistemaArchivos sistema) {
        String nombre = leerLinea("Nombre del archivo: ");
        String contenido = leerContenido();
        System.out.println(sistema.crearArchivo(nombre, contenido));
    }

    /**
     * Prompts the user for a file name and prints its content to standard output.
     *
     * @param sistema the file system instance to read from
     */
    private static void leerArchivo(SistemaArchivos sistema) {
        String nombre = leerLinea("Nombre del archivo a leer: ");
        System.out.println("Contenido:");
        System.out.println(sistema.leerArchivo(nombre));
    }

    /**
     * Prompts the user for a file name and delegates deletion to
     * {@link SistemaArchivos#eliminarArchivo(String)}.
     *
     * @param sistema the file system instance to operate on
     */
    private static void eliminarArchivo(SistemaArchivos sistema) {
        String nombre = leerLinea("Nombre del archivo a eliminar: ");
        if (!confirmar("Esta seguro que desea eliminar " + nombre + "? (S/N): ")) {
            System.out.println("Eliminacion cancelada.");
            return;
        }
        System.out.println(sistema.eliminarArchivo(nombre));
    }

    /**
     * Displays a prompt, reads one line from the console, and returns it
     * with leading and trailing whitespace removed.
     *
     * @param mensaje the prompt text to display before reading
     * @return the trimmed line entered by the user
     */
    private static String leerLinea(String mensaje) {
        System.out.print(mensaje);
        return SCANNER.nextLine().trim();
    }

    /**
     * Reads and validates a main-menu option.
     *
     * <p>Only numeric options displayed by the menu are accepted. Invalid
     * input, including letters, blank text, and numbers outside the available
     * range, causes the prompt to be shown again.</p>
     *
     * @return a valid menu option in the range {@code 1} through {@code 8}
     */
    private static String leerOpcionMenu() {
        while (true) {
            String opcion = leerLinea("Seleccione una opcion: ");
            if (opcion.matches("[1-8]")) {
                return opcion;
            }
            System.out.println("Opcion no valida. Ingrese solo numeros del 1 al 8.");
        }
    }

    /**
     * Reads a yes/no confirmation from the console.
     *
     * <p>Only {@code S} and {@code N} are accepted. The comparison is
     * case-insensitive, so values such as {@code s}, {@code S}, {@code n}, and
     * {@code N} are valid.</p>
     *
     * @param mensaje prompt shown to the user
     * @return {@code true} when the user confirms with {@code S};
     *         {@code false} when the user answers {@code N}
     */
    private static boolean confirmar(String mensaje) {
        while (true) {
            String respuesta = leerLinea(mensaje);
            if (respuesta.equalsIgnoreCase("S")) {
                return true;
            }
            if (respuesta.equalsIgnoreCase("N")) {
                return false;
            }
            System.out.println("Respuesta no valida. Ingrese S para si o N para no.");
        }
    }

    /**
     * Reads a multi-line block of content from standard input.
     *
     * <p>The user signals end-of-input by typing {@code FIN} on its own line.
     * The marker is case-insensitive and surrounding spaces are ignored. Lines
     * are joined with the platform line separator.</p>
     *
     * @return the accumulated content, without the terminating {@code FIN} line
     */
    private static String leerContenido() {
        System.out.println("Contenido del archivo. Escriba FIN en una linea separada para terminar:");
        StringBuilder contenido = new StringBuilder();

        while (true) {
            String linea = SCANNER.nextLine();
            if (linea.trim().equalsIgnoreCase("FIN")) {
                break;
            }
            if (contenido.length() > 0) {
                contenido.append(System.lineSeparator());
            }
            contenido.append(linea);
        }

        return contenido.toString();
    }
}
