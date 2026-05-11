package model.log;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Append-only logger for file system operations.
 *
 * <p>Each entry follows the format:</p>
 * <pre>[yyyy-MM-dd HH:mm:ss] LEVEL - OPERATION: details</pre>
 *
 * <p>I/O errors are printed to stderr and swallowed so logging never
 * aborts a file system operation.</p>
 *
 * @author Carolain Quesada
 * @version 1.0
 */
public class FileSystemLogger {

    private static final DateTimeFormatter DATE_FORMAT =
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /** Path of the file where log entries are appended. */
    private final String logFilePath;
    /** Whether log entries should also be echoed to the console. */
    private final boolean enableConsoleOutput;

    /**
     * Constructs a logger that writes to the given file.
     *
     * @param logFilePath         path to the log file
     * @param enableConsoleOutput {@code true} to also echo entries to stdout
     */
    public FileSystemLogger(String logFilePath, boolean enableConsoleOutput) {
        this.logFilePath          = logFilePath;
        this.enableConsoleOutput  = enableConsoleOutput;
    }

    /**
     * Logs an INFO-level message.
     * @param operation operation label
     * @param details   additional context
     */
    public void logInfo(String operation, String details) { log("INFO", operation, details); }

    /**
     * Logs a WARN-level message.
     * @param operation operation label
     * @param details   additional context
     */
    public void logWarning(String operation, String details) { log("WARN", operation, details); }

    /**
     * Logs an ERROR-level message.
     * @param operation operation label
     * @param details   additional context
     */
    public void logError(String operation, String details) { log("ERROR", operation, details); }

    /**
     * Logs an exception as an ERROR entry.
     * @param operation operation during which the exception occurred
     * @param exception the exception to log
     */
    public void logError(String operation, Exception exception) {
        log("ERROR", operation, exception.getClass().getSimpleName() + ": " + exception.getMessage());
    }

    /**
     * Logs the result of a file operation at FILE_OP level.
     * @param operationType e.g. {@code "CREATE"}, {@code "DELETE"}
     * @param path          file path involved
     * @param success       {@code true} if the operation succeeded
     */
    public void logFileOperation(String operationType, String path, boolean success) {
        log("FILE_OP", operationType + " " + (success ? "SUCCESS" : "FAILED"), path);
    }

    /**
     * Logs an access-control event at ACCESS level.
     * @param operation    operation attempted
     * @param path         file or directory path
     * @param accessDenied {@code true} if access was denied
     */
    public void logAccessEvent(String operation, String path, boolean accessDenied) {
        log("ACCESS", operation + " " + (accessDenied ? "DENIED" : "ALLOWED"), path);
    }

    /**
     * Formats and records a single log entry.
     *
     * @param level severity or category label, such as {@code INFO} or
     *        {@code ERROR}
     * @param operation logical operation associated with the entry
     * @param details additional context to include after the operation
     */
    private void log(String level, String operation, String details) {
        String entry = String.format("[%s] %s - %s: %s",
            LocalDateTime.now().format(DATE_FORMAT), level, operation, details);
        if (enableConsoleOutput) System.out.println(entry);
        writeToFile(entry);
    }

    /**
     * Appends a formatted entry to the configured log file.
     * <p>
     * Logging failures are reported to standard error and intentionally not
     * propagated, preserving the main file system operation.
     * </p>
     *
     * @param entry the fully formatted log entry
     */
    private void writeToFile(String entry) {
        try (FileWriter fw = new FileWriter(logFilePath, true);
             BufferedWriter bw = new BufferedWriter(fw)) {
            bw.write(entry); bw.newLine();
        } catch (IOException e) {
            System.err.println("Failed to write log entry: " + e.getMessage());
        }
    }

    /** Truncates the log file, removing all existing entries. */
    public void clearLogs() {
        try (FileWriter fw = new FileWriter(logFilePath)) { /* truncates */ }
        catch (IOException e) { System.err.println("Failed to clear logs: " + e.getMessage()); }
    }

    /**
     * Returns the last {@code lineCount} lines from the log file.
     *
     * @param lineCount number of lines to retrieve; must be {@code > 0}
     * @return log lines as a single string, or an error message on failure
     */
    public String getLastLogs(int lineCount) {
        StringBuilder result = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new FileReader(logFilePath))) {
            java.util.List<String> lines = new java.util.ArrayList<>();
            String line;
            while ((line = br.readLine()) != null) lines.add(line);
            for (int i = Math.max(0, lines.size() - lineCount); i < lines.size(); i++)
                result.append(lines.get(i)).append("\n");
        } catch (IOException e) { result.append("Error reading logs: ").append(e.getMessage()); }
        return result.toString();
    }
}
