package model.exceptions;

/**
 * Thrown when an operation attempts to create a file or directory whose name
 * already exists within the target parent directory.
 *
 * @author Carolain Quesada
 * @version 1.0
 * @see FileSystemException
 */
public class DuplicateEntryException extends FileSystemException {

    /**
     * Constructs a {@code DuplicateEntryException} identifying the conflicting
     * entry and its parent directory.
     *
     * @param entryName  the name of the entry that already exists
     * @param parentPath the absolute path of the directory that contains the
     *                   duplicate entry
     */
    public DuplicateEntryException(String entryName, String parentPath) {
        super(String.format("La entrada ya existe: %s en %s", entryName, parentPath));
    }
}