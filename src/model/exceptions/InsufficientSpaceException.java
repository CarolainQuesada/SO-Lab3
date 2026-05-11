package model.exceptions;

/**
 * Thrown when the file system does not have enough free blocks or inodes to
 * satisfy an allocation request.
 *
 * @author Carolain Quesada
 * @version 1.0
 * @see FileSystemException
 */
public class InsufficientSpaceException extends FileSystemException {

    /**
     * Constructs an {@code InsufficientSpaceException} with details about the
     * exhausted resource.
     *
     * @param resourceType a label identifying the type of resource that is
     *                     exhausted (e.g. {@code "blocks"} or {@code "inodes"})
     * @param required     the number of resource units that were requested
     * @param available    the number of resource units that were available
     */
    public InsufficientSpaceException(String resourceType, int required, int available) {
        super(String.format("Espacio insuficiente en %s: requerido=%d, disponible=%d",
              resourceType, required, available));
    }
}