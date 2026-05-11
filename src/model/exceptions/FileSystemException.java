package model.exceptions;

/**
 * Base exception for all file system operations.
 *
 * <p>All checked exceptions thrown by the file system layer extend this class,
 * so callers can handle the entire family with a single {@code catch} clause
 * when fine-grained handling is not required.</p>
 *
 * @author Carolain Quesada
 * @version 1.0
 */
public class FileSystemException extends Exception {

    /**
     * Constructs a {@code FileSystemException} with a detailed error message.
     *
     * @param message a human-readable description of the error
     */
    public FileSystemException(String message) {
        super(message);
    }

    /**
     * Constructs a {@code FileSystemException} with a message and an
     * underlying cause.
     *
     * @param message a human-readable description of the error
     * @param cause   the exception that caused this exception to be thrown;
     *                may be {@code null}
     */
    public FileSystemException(String message, Throwable cause) {
        super(message, cause);
    }
}