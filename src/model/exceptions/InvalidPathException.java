package model.exceptions;

/**
 * Thrown when a path string is malformed or otherwise invalid.
 *
 * <p>Common causes include a path that does not start with {@code '/'}, a
 * {@code null} or blank path, or path components that contain illegal
 * characters.</p>
 *
 * @author Carolain Quesada
 * @version 1.0
 * @see FileSystemException
 */
public class InvalidPathException extends FileSystemException {

    /**
     * Constructs an {@code InvalidPathException} with the invalid path and a
     * reason.
     *
     * @param path   the path string that failed validation; may be {@code null}
     * @param reason a short explanation of why the path is invalid (e.g.
     *               {@code "Path must start with /"})
     */
    public InvalidPathException(String path, String reason) {
        super("Ruta inválida: " + path + " (" + reason + ")");
    }

    /**
     * Constructs an {@code InvalidPathException} with a custom error message.
     *
     * @param message a human-readable description of the validation failure
     */
    public InvalidPathException(String message) {
        super(message);
    }
}