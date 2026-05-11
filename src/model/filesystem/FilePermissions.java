package model.filesystem;

/**
 * Represents Unix-style file permissions (rwxrwxrwx format).
 * Supports owner, group, and others permissions.
 *
 * @author Carolain Quesada
 */
public class FilePermissions {
    /** Bit mask for read permission. */
    public static final int READ = 4;
    /** Bit mask for write permission. */
    public static final int WRITE = 2;
    /** Bit mask for execute permission. */
    public static final int EXECUTE = 1;

    /** Permission bits applied to the owner. */
    private int ownerPermissions;  // rwx for owner (0-7)
    /** Permission bits applied to the group. */
    private int groupPermissions;  // rwx for group (0-7)
    /** Permission bits applied to all other users. */
    private int othersPermissions; // rwx for others (0-7)
    /** Logical owner name shown by file metadata commands. */
    private String ownerName;
    /** Logical group name shown by file metadata commands. */
    private String groupName;

    /**
     * Creates file permissions with default owner:group 644 (rw-r--r--).
     *
     * @param ownerName the owner of the file
     * @param groupName the group of the file
     */
    public FilePermissions(String ownerName, String groupName) {
        this(ownerName, groupName, READ + WRITE, READ, READ);
    }

    /**
     * Creates file permissions with specified values.
     *
     * @param ownerName         the owner of the file
     * @param groupName         the group of the file
     * @param ownerPermissions  permissions for owner (0-7)
     * @param groupPermissions  permissions for group (0-7)
     * @param othersPermissions permissions for others (0-7)
     */
    public FilePermissions(String ownerName, String groupName, 
                          int ownerPermissions, int groupPermissions, int othersPermissions) {
        this.ownerName = ownerName;
        this.groupName = groupName;
        this.ownerPermissions = ownerPermissions & 7;
        this.groupPermissions = groupPermissions & 7;
        this.othersPermissions = othersPermissions & 7;
    }

    /**
     * Checks whether the owner can read the entry.
     *
     * @return true if read permission is enabled for the owner
     */
    public boolean canOwnerRead() {
        return (ownerPermissions & READ) != 0;
    }

    /**
     * Checks whether the owner can write to the entry.
     *
     * @return true if write permission is enabled for the owner
     */
    public boolean canOwnerWrite() {
        return (ownerPermissions & WRITE) != 0;
    }

    /**
     * Checks whether the owner can execute the entry.
     *
     * @return true if execute permission is enabled for the owner
     */
    public boolean canOwnerExecute() {
        return (ownerPermissions & EXECUTE) != 0;
    }

    /**
     * Checks whether the group can read the entry.
     *
     * @return true if read permission is enabled for the group
     */
    public boolean canGroupRead() {
        return (groupPermissions & READ) != 0;
    }

    /**
     * Checks whether the group can write to the entry.
     *
     * @return true if write permission is enabled for the group
     */
    public boolean canGroupWrite() {
        return (groupPermissions & WRITE) != 0;
    }

    /**
     * Checks whether the group can execute the entry.
     *
     * @return true if execute permission is enabled for the group
     */
    public boolean canGroupExecute() {
        return (groupPermissions & EXECUTE) != 0;
    }

    /**
     * Checks whether other users can read the entry.
     *
     * @return true if read permission is enabled for other users
     */
    public boolean canOthersRead() {
        return (othersPermissions & READ) != 0;
    }

    /**
     * Checks whether other users can write to the entry.
     *
     * @return true if write permission is enabled for other users
     */
    public boolean canOthersWrite() {
        return (othersPermissions & WRITE) != 0;
    }

    /**
     * Checks whether other users can execute the entry.
     *
     * @return true if execute permission is enabled for other users
     */
    public boolean canOthersExecute() {
        return (othersPermissions & EXECUTE) != 0;
    }

    /**
     * Changes permissions for the owner.
     *
     * @param permissions permissions value in the range {@code 0-7}; values
     *        outside the range are masked to the lowest three bits
     */
    public void setOwnerPermissions(int permissions) {
        this.ownerPermissions = permissions & 7;
    }

    /**
     * Changes permissions for the group.
     *
     * @param permissions permissions value in the range {@code 0-7}; values
     *        outside the range are masked to the lowest three bits
     */
    public void setGroupPermissions(int permissions) {
        this.groupPermissions = permissions & 7;
    }

    /**
     * Changes permissions for other users.
     *
     * @param permissions permissions value in the range {@code 0-7}; values
     *        outside the range are masked to the lowest three bits
     */
    public void setOthersPermissions(int permissions) {
        this.othersPermissions = permissions & 7;
    }

    /**
     * Gets the owner name.
     *
     * @return the owner name
     */
    public String getOwnerName() {
        return ownerName;
    }

    /**
     * Gets the group name.
     *
     * @return the group name
     */
    public String getGroupName() {
        return groupName;
    }

    /**
     * Returns permissions as a symbolic Unix-style string.
     *
     * @return permissions in {@code rwxrwxrwx} form, for example
     *         {@code rw-r--r--}
     */
    @Override
    public String toString() {
        return permissionBits(ownerPermissions) + 
               permissionBits(groupPermissions) + 
               permissionBits(othersPermissions);
    }

    /**
     * Returns octal representation (e.g., "644").
     *
     * @return the octal permission representation
     */
    public String toOctal() {
        return String.format("%d%d%d", ownerPermissions, groupPermissions, othersPermissions);
    }

    /**
     * Converts a three-bit numeric permission value to symbolic form.
     *
     * @param perms the permission bits to convert
     * @return a three-character string such as {@code rwx}, {@code r--}, or
     *         {@code ---}
     */
    private String permissionBits(int perms) {
        return (((perms & READ) != 0) ? "r" : "-") +
               (((perms & WRITE) != 0) ? "w" : "-") +
               (((perms & EXECUTE) != 0) ? "x" : "-");
    }
}
