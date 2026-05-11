package model.exceptions;

/**
 * Thrown when an operation attempts to delete a directory that still contains
 * children.
 *
 * <p>Use {@code rm -r} (recursive deletion) to remove a directory together
 * with all of its contents.</p>
 *
 * @author Carolain Quesada
 * @version 1.0
 * @see FileSystemException
 */
public class DirectoryNotEmptyException extends FileSystemException {

    /**
     * Constructs a {@code DirectoryNotEmptyException} for the specified
     * directory path.
     *
     * @param directoryPath the absolute path of the non-empty directory that
     *                      could not be deleted
     */
    public DirectoryNotEmptyException(String directoryPath) {
        super("No se puede eliminar el directorio: no está vacío: " + directoryPath);
    }
}