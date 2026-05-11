package ui;

import model.filesystem.*;
import model.exceptions.*;
import java.util.*;

/**
 * Processes Unix-like shell commands against a {@link FileSystem}.
 *
 * <p>Supported commands: {@code touch}, {@code cat}, {@code echo},
 * {@code rm [-r]}, {@code mkdir}, {@code ls}, {@code cd}, {@code pwd},
 * {@code tree}, {@code cp}, {@code mv}, {@code chmod}, {@code find},
 * {@code grep}, {@code stat}, {@code df}, {@code logs}, {@code help},
 * {@code exit}/{@code quit}.</p>
 *
 * @author Carolain Quesada
 * @version 1.0
 */
public class CommandProcessor {

    private final FileSystem fileSystem;
    private boolean running;

    /**
     * Constructs a processor for the given file system.
     *
     * @param fileSystem file system to operate on; must not be {@code null}
     */
    public CommandProcessor(FileSystem fileSystem) {
        this.fileSystem = fileSystem;
        this.running    = true;
    }

    /**
     * Parses and executes one command line.
     * Null/blank input is a no-op. Exceptions are caught and displayed.
     *
     * @param commandLine command line string from the user
     * @return {@code true} while the processor should continue;
     *         {@code false} after exit/quit
     */
    public boolean processCommand(String commandLine) {
        if (commandLine == null || commandLine.trim().isEmpty()) return true;
        CommandParser parser  = new CommandParser(commandLine);
        String        command = parser.getCommand().toLowerCase();
        try {
            switch (command) {
                case "touch"  -> handleTouch(parser);
                case "cat"    -> handleCat(parser);
                case "echo"   -> handleEcho(parser);
                case "rm"     -> handleRm(parser);
                case "mkdir"  -> handleMkdir(parser);
                case "ls"     -> handleLs(parser);
                case "cd"     -> handleCd(parser);
                case "pwd"    -> handlePwd(parser);
                case "tree"   -> handleTree(parser);
                case "cp"     -> handleCp(parser);
                case "mv"     -> handleMv(parser);
                case "chmod"  -> handleChmod(parser);
                case "find"   -> handleFind(parser);
                case "grep"   -> handleGrep(parser);
                case "stat"   -> handleStat(parser);
                case "df"     -> handleDf(parser);
                case "logs"   -> handleLogs(parser);
                case "help"   -> UIFormatter.printMainMenu(fileSystem.getCurrentWorkingDirectory());
                case "exit", "quit" -> { running = false; UIFormatter.success("¡Hasta luego!"); }
                case ""       -> { /* empty line */ }
                default       -> UIFormatter.error("Comando desconocido: " + command + ". Escriba 'help'.");
            }
        } catch (FileSystemException e) {
            UIFormatter.error(e.getMessage()); fileSystem.getLogger().logError(command.toUpperCase(), e);
        } catch (Exception e) {
            UIFormatter.error("Error inesperado: " + e.getMessage()); fileSystem.getLogger().logError(command.toUpperCase(), e);
        }
        return running;
    }

    // ---- File operation handlers ----

    /** touch &lt;path&gt; — creates an empty file. */
    private void handleTouch(CommandParser p) throws FileSystemException {
        if (!p.requiresMinArgs(1)) { UIFormatter.error("Uso: touch <ruta>"); return; }
        fileSystem.createFile(p.getArg(0), "", "user");
        UIFormatter.success("Archivo creado: " + p.getArg(0));
    }

    /** cat &lt;path&gt; — prints file content. */
    private void handleCat(CommandParser p) throws FileSystemException {
        if (!p.requiresArgs(1)) { UIFormatter.error("Uso: cat <ruta>"); return; }
        System.out.println(fileSystem.readFile(p.getArg(0)));
    }

    /** echo &lt;text&gt; [&gt;|&gt;&gt;] &lt;path&gt; — writes or appends text to a file. */
    private void handleEcho(CommandParser p) throws FileSystemException {
        if (p.getArgCount() < 3) { UIFormatter.error("Uso: echo <texto> [>|>>] <ruta>"); return; }
        StringBuilder text = new StringBuilder();
        int redirectIndex = -1; String operator = "";
        for (int i = 0; i < p.getArgCount(); i++) {
            String arg = p.getArg(i);
            if (arg.equals(">") || arg.equals(">>")) { redirectIndex = i; operator = arg; break; }
            if (i > 0) text.append(" ");
            text.append(arg);
        }
        if (redirectIndex == -1 || redirectIndex + 1 >= p.getArgCount()) {
            UIFormatter.error("Uso: echo <texto> [>|>>] <ruta>"); return;
        }
        String path = p.getArg(redirectIndex + 1); boolean append = ">>".equals(operator);
        try {
            fileSystem.writeFile(path, text.toString(), append);
            UIFormatter.success("Escrito en: " + path);
        } catch (FileNotFoundException e) {
            if (!append) { fileSystem.createFile(path, text.toString(), "user"); UIFormatter.success("Archivo creado: " + path); }
            else throw e;
        }
    }

    /** rm [-r] &lt;path&gt; — deletes a file or directory. */
    private void handleRm(CommandParser p) throws FileSystemException {
        if (!p.requiresMinArgs(1)) { UIFormatter.error("Uso: rm [-r] <ruta>"); return; }
        String path = p.getArg(0);
        if (p.hasOption("-r")) fileSystem.deleteRecursive(path); else fileSystem.deleteEntry(path);
        UIFormatter.success("Eliminado: " + path);
    }

    // ---- Directory operation handlers ----

    /** mkdir &lt;path&gt; — creates a directory. */
    private void handleMkdir(CommandParser p) throws FileSystemException {
        if (!p.requiresMinArgs(1)) { UIFormatter.error("Uso: mkdir <ruta>"); return; }
        fileSystem.makeDirectory(p.getArg(0), "user", "users");
        UIFormatter.success("Directorio creado: " + p.getArg(0));
    }

    /** ls [path] — lists directory contents. */
    private void handleLs(CommandParser p) throws FileSystemException {
        String path = p.getArgCount() > 0 ? p.getArg(0) : fileSystem.getCurrentWorkingDirectory();
        UIFormatter.printTableHeader("Tipo", "Nombre");
        for (String entry : fileSystem.listDirectory(path)) if (!entry.startsWith("d")) System.out.println(entry);
    }

    /** cd &lt;path&gt; — changes the working directory. */
    private void handleCd(CommandParser p) throws FileSystemException {
        if (!p.requiresArgs(1)) { UIFormatter.error("Uso: cd <ruta>"); return; }
        fileSystem.changeDirectory(p.getArg(0));
        UIFormatter.success("Directorio cambiado a: " + p.getArg(0));
    }

    /** pwd — prints the working directory. */
    private void handlePwd(CommandParser p) { System.out.println(fileSystem.getCurrentWorkingDirectory()); }

    /** tree [path] — shows the directory tree. */
    private void handleTree(CommandParser p) { System.out.println(fileSystem.getTreeStructure()); }

    // ---- File manipulation handlers ----

    /** cp &lt;src&gt; &lt;dst&gt; — copies a file. */
    private void handleCp(CommandParser p) throws FileSystemException {
        if (!p.requiresArgs(2)) { UIFormatter.error("Uso: cp <origen> <destino>"); return; }
        fileSystem.copyFile(p.getArg(0), p.getArg(1));
        UIFormatter.success("Archivo copiado: " + p.getArg(0) + " -> " + p.getArg(1));
    }

    /** mv &lt;src&gt; &lt;dst&gt; — moves or renames a file or directory. */
    private void handleMv(CommandParser p) throws FileSystemException {
        if (!p.requiresArgs(2)) { UIFormatter.error("Uso: mv <origen> <destino>"); return; }
        fileSystem.moveEntry(p.getArg(0), p.getArg(1));
        UIFormatter.success("Movido: " + p.getArg(0) + " -> " + p.getArg(1));
    }

    /** chmod &lt;perms&gt; &lt;path&gt; — changes file permissions. */
    private void handleChmod(CommandParser p) throws FileSystemException {
        if (!p.requiresArgs(2)) { UIFormatter.error("Uso: chmod <permisos> <ruta>"); return; }
        fileSystem.changePermissions(p.getArg(1), p.getArg(0));
        UIFormatter.success("Permisos cambiados: " + p.getArg(1));
    }

    // ---- Search handlers ----

    /** find &lt;pattern&gt; — searches by file name. */
    private void handleFind(CommandParser p) throws FileSystemException {
        if (!p.requiresMinArgs(1)) { UIFormatter.error("Uso: find <patrón>"); return; }
        List<String> results = fileSystem.searchByName(p.getArg(0));
        if (results.isEmpty()) { UIFormatter.warning("No se encontraron coincidencias"); return; }
        System.out.println("Encontradas " + results.size() + " coincidencia(s):");
        results.forEach(r -> System.out.println("  " + r));
    }

    /** grep &lt;pattern&gt; — searches by file content. */
    private void handleGrep(CommandParser p) throws FileSystemException {
        if (!p.requiresMinArgs(1)) { UIFormatter.error("Uso: grep <patrón>"); return; }
        List<String> results = fileSystem.searchByContent(p.getArg(0));
        if (results.isEmpty()) { UIFormatter.warning("No se encontraron coincidencias"); return; }
        System.out.println("Encontrados " + results.size() + " archivo(s):");
        results.forEach(r -> System.out.println("  " + r));
    }

    // ---- Info handlers ----

    /** stat &lt;path&gt; — prints detailed inode information. */
    private void handleStat(CommandParser p) throws FileSystemException {
        if (!p.requiresArgs(1)) { UIFormatter.error("Uso: stat <ruta>"); return; }
        Inode inode = fileSystem.resolveInode(p.getArg(0));
        if (inode == null) throw new FileNotFoundException(p.getArg(0));
        System.out.println("  Inodo: "       + inode.getId());
        System.out.println("  Nombre: "      + inode.getName());
        System.out.println("  Tipo: "        + (inode.isDirectory() ? "Directorio" : "Archivo"));
        System.out.println("  Tamaño: "      + inode.getSize() + " bytes");
        System.out.println("  Permisos: "    + inode.getPermissions());
        System.out.println("  Propietario: " + inode.getPermissions().getOwnerName());
        System.out.println("  Creado: "      + inode.getCreatedAt());
        System.out.println("  Modificado: "  + inode.getModifiedAt());
    }

    /** df — shows disk usage statistics. */
    private void handleDf(CommandParser p) { System.out.println(fileSystem.getStatistics()); }

    /** logs [n] — shows last n log entries (default 10). */
    private void handleLogs(CommandParser p) {
        int lines = p.getArgCount() > 0 ? Integer.parseInt(p.getArg(0)) : 10;
        System.out.println(fileSystem.getLogger().getLastLogs(lines));
    }

    /**
     * Returns whether the processor is still running.
     *
     * @return {@code true} until exit/quit is received
     */
    public boolean isRunning() { return running; }
}