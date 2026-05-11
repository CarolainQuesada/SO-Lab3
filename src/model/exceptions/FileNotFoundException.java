package model.exceptions;

/**
 * Thrown when a file or directory cannot be found in the file system.
 *
 * @author Carolain Quesada
 */
public class FileNotFoundException extends FileSystemException {
    /**
     * Constructs a FileNotFoundException for the specified path.
     *
     * @param path the path that was not found
     */
    public FileNotFoundException(String path) {
        super("Archivo o directorio no encontrado: " + path);
    }

    /**
     * Constructs a FileNotFoundException with a custom message.
     *
     * @param message the error message
     * @param path    the path that was not found
     */
    public FileNotFoundException(String message, String path) {
        super(message + " (" + path + ")");
    }
}
