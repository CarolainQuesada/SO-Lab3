package ui;

/**
 * Utility class for consistent ANSI-coloured console output.
 * <p>
 * The class centralizes success, error, warning, informational, table, and
 * prompt formatting for the shell interface. All methods are static and the
 * class cannot be instantiated.
 * </p>
 *
 * @author Carolain Quesada
 * @version 1.0
 */
public final class UIFormatter {

    private static final String RESET  = "\u001B[0m";
    private static final String BOLD   = "\u001B[1m";
    private static final String GREEN  = "\u001B[32m";
    private static final String RED    = "\u001B[31m";
    private static final String YELLOW = "\u001B[33m";
    private static final String BLUE   = "\u001B[34m";
    private static final String CYAN   = "\u001B[36m";

    /**
     * Prevents instantiation of this static utility class.
     */
    private UIFormatter() {
    }

    /**
     * Prints a green success message prefixed with a check mark.
     *
     * @param message the message to display
     */
    public static void success(String message) {
        System.out.println(GREEN + "\u2713 " + message + RESET);
    }

    /**
     * Prints a red error message prefixed with a cross mark.
     *
     * @param message the message to display
     */
    public static void error(String message) {
        System.out.println(RED + "\u2717 " + message + RESET);
    }

    /**
     * Prints a yellow warning message prefixed with a warning symbol.
     *
     * @param message the message to display
     */
    public static void warning(String message) {
        System.out.println(YELLOW + "\u26A0 " + message + RESET);
    }

    /**
     * Prints a blue informational message prefixed with an information symbol.
     *
     * @param message the message to display
     */
    public static void info(String message) {
        System.out.println(BLUE + "\u2139 " + message + RESET);
    }

    /**
     * Prints a cyan bold header with a border sized to the title.
     *
     * @param title header text
     */
    public static void header(String title) {
        String sep = "\u2550".repeat(title.length() + 4);
        System.out.println(CYAN + BOLD + sep + "\n  " + title + "\n" + sep + RESET);
    }

    /**
     * Prints a 60-character separator line.
     */
    public static void separator() {
        System.out.println("\u2500".repeat(60));
    }

    /**
     * Prints the complete command reference for the shell.
     *
     * @param currentPath current working directory shown in the header
     */
    public static void printMainMenu(String currentPath) {
        header("Shell del Sistema de Archivos");
        System.out.println(BOLD + "Directorio Actual: " + RESET + currentPath + "\n");
        System.out.println(BOLD + "Operaciones de Archivos:" + RESET);
        System.out.println("  touch <ruta>             - Crear un archivo");
        System.out.println("  cat <ruta>               - Mostrar contenido del archivo");
        System.out.println("  echo <texto> > <ruta>    - Escribir en archivo");
        System.out.println("  echo <texto> >> <ruta>   - Agregar a archivo");
        System.out.println("  rm <ruta>                - Eliminar archivo o directorio");
        System.out.println("  rm -r <ruta>             - Eliminar directorio recursivamente\n");
        System.out.println(BOLD + "Operaciones de Directorios:" + RESET);
        System.out.println("  mkdir <ruta>             - Crear directorio");
        System.out.println("  ls [ruta]                - Listar contenido del directorio");
        System.out.println("  cd <ruta>                - Cambiar directorio");
        System.out.println("  pwd                      - Mostrar directorio de trabajo");
        System.out.println("  tree [ruta]              - Mostrar \u00E1rbol de directorios\n");
        System.out.println(BOLD + "Manipulaci\u00F3n de Archivos:" + RESET);
        System.out.println("  cp <origen> <destino>    - Copiar archivo");
        System.out.println("  mv <origen> <destino>    - Mover/renombrar archivo");
        System.out.println("  chmod <perms> <ruta>     - Cambiar permisos\n");
        System.out.println(BOLD + "B\u00FAsqueda e Informaci\u00F3n:" + RESET);
        System.out.println("  find <patr\u00F3n>            - Buscar por nombre de archivo");
        System.out.println("  grep <patr\u00F3n>            - Buscar por contenido");
        System.out.println("  stat <ruta>              - Mostrar informaci\u00F3n del archivo");
        System.out.println("  df                       - Mostrar estad\u00EDsticas del sistema");
        System.out.println("  logs [l\u00EDneas]            - Mostrar registros recientes\n");
        System.out.println(BOLD + "Otros:" + RESET);
        System.out.println("  help                     - Mostrar este men\u00FA");
        System.out.println("  exit                     - Salir del programa\n");
    }

    /**
     * Prints a bold table header row with a separator line.
     * Each column occupies 20 characters.
     *
     * @param columns column labels
     */
    public static void printTableHeader(String... columns) {
        StringBuilder sb = new StringBuilder();
        for (String col : columns) {
            sb.append(String.format("%-20s ", col));
        }
        System.out.println(BOLD + sb + RESET);
        System.out.println("\u2500".repeat(20 * columns.length));
    }

    /**
     * Prints a table data row aligned with {@link #printTableHeader}.
     *
     * @param values cell values
     */
    public static void printTableRow(String... values) {
        StringBuilder sb = new StringBuilder();
        for (String val : values) {
            sb.append(String.format("%-20s ", val));
        }
        System.out.println(sb);
    }

    /**
     * Prints file information as a plain line.
     *
     * @param info formatted file information string
     */
    public static void printFileInfo(String info) {
        System.out.println(info);
    }

    /**
     * Prints the shell prompt ({@code username@sistemaarchivos:path$ }) without
     * a newline.
     *
     * @param currentPath current working directory
     * @param username username displayed in the prompt
     * @return {@code null}
     */
    public static String printPrompt(String currentPath, String username) {
        System.out.print(BOLD + BLUE + username + "@sistemaarchivos" + RESET + ":");
        System.out.print(BOLD + CYAN + currentPath + RESET + "$ ");
        return null;
    }
}
