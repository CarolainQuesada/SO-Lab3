package model.exceptions;

/**
 * Thrown when the current user or process does not have the required
 * permissions to perform a file system operation.
 *
 * <p>Permission checks are based on the Unix-style read/write/execute bits
 * stored in each inode's {@code FilePermissions} object.</p>
 *
 * @author Carolain Quesada
 * @version 1.0
 * @see FileSystemException
 */
public class PermissionDeniedException extends FileSystemException {

    /**
     * Constructs a {@code PermissionDeniedException} identifying the denied
     * operation and the affected path.
     *
     * @param operation the name of the operation that was denied (e.g.
     *                  {@code "read"} or {@code "write"})
     * @param path      the absolute path of the file or directory on which the
     *                  operation was attempted
     */
    public PermissionDeniedException(String operation, String path) {
        super(String.format("Permiso denegado para la operación %s en: %s", operation, path));
    }

    /**
     * Constructs a {@code PermissionDeniedException} with a custom error
     * message.
     *
     * @param message a human-readable description of the permission failure
     */
    public PermissionDeniedException(String message) {
        super(message);
    }
}