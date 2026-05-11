package model.filesystem;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Represents an inode in the in-memory Unix-style file system.
 * <p>
 * An inode stores the metadata required to describe either a file or a
 * directory: identifier, name, parent reference, permissions, size, timestamps,
 * and block pointers. Directory inodes maintain a list of child inodes, while
 * file inodes maintain text content.
 * </p>
 * <p>
 * This class also updates access and modification timestamps when content or
 * directory relationships change, which helps the simulator behave more like a
 * real file system.
 * </p>
 *
 * @author Carolain Quesada
 */
public class Inode {
    private static final DateTimeFormatter DATE_FORMAT = 
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final int id;
    private final String name;
    private final boolean isDirectory;
    private Inode parent;
    private FilePermissions permissions;
    private String content;
    private final List<Inode> children;
    private List<Integer> blockPointers;  // References to data blocks
    private int size;  // File size in bytes
    private LocalDateTime createdAt;
    private LocalDateTime modifiedAt;
    private LocalDateTime accessedAt;

    /**
     * Creates an inode for a file or directory.
     * <p>
     * Directory inodes are initialized with an empty child list and no content.
     * File inodes are initialized with empty content and no children.
     * </p>
     *
     * @param id           the inode ID
     * @param name         the name of the file/directory
     * @param isDirectory  whether this is a directory
     * @param parent       the parent inode
     * @param owner        the owner of the file
     * @param group        the group of the file
     */
    public Inode(int id, String name, boolean isDirectory, Inode parent, 
                 String owner, String group) {
        this.id = id;
        this.name = name;
        this.isDirectory = isDirectory;
        this.parent = parent;
        this.permissions = new FilePermissions(owner, group);
        this.content = isDirectory ? null : "";
        this.children = isDirectory ? new ArrayList<>() : null;
        this.blockPointers = new ArrayList<>();
        this.size = 0;
        this.createdAt = LocalDateTime.now();
        this.modifiedAt = LocalDateTime.now();
        this.accessedAt = LocalDateTime.now();
    }

    /**
     * Gets the inode identifier.
     *
     * @return the inode identifier
     */
    public int getId() {
        return id;
    }

    /**
     * Gets the inode name.
     *
     * @return the file or directory name
     */
    public String getName() {
        return name;
    }

    /**
     * Indicates whether this inode represents a directory.
     *
     * @return true for directories; false for files
     */
    public boolean isDirectory() {
        return isDirectory;
    }

    /**
     * Gets the parent inode.
     *
     * @return the parent inode, or null for the root inode
     */
    public Inode getParent() {
        return parent;
    }

    /**
     * Sets the parent inode.
     *
     * @param parent the new parent inode
     */
    public void setParent(Inode parent) {
        this.parent = parent;
    }

    /**
     * Gets this inode's permissions.
     *
     * @return the permissions object
     */
    public FilePermissions getPermissions() {
        return permissions;
    }

    /**
     * Gets file content and updates the access timestamp.
     *
     * @return the file content, or null when this inode is a directory
     */
    public String getContent() {
        if (isDirectory) {
            return null;
        }
        updateAccessedTime();
        return content;
    }

    /**
     * Updates file content and size.
     *
     * @param content the new file content
     */
    public void setContent(String content) {
        if (!isDirectory && content != null) {
            this.content = content;
            this.size = content.length();
            updateModifiedTime();
        }
    }

    /**
     * Gets child inodes for a directory.
     *
     * @return the child inode list, or null for files
     */
    public List<Inode> getChildren() {
        return children;
    }

    /**
     * Gets the file size.
     *
     * @return the size in bytes
     */
    public int getSize() {
        return size;
    }

    /**
     * Sets the file size.
     *
     * @param size the new size in bytes
     */
    public void setSize(int size) {
        this.size = size;
    }

    /**
     * Gets the creation timestamp.
     *
     * @return the creation timestamp
     */
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    /**
     * Gets the last modification timestamp.
     *
     * @return the last modification timestamp
     */
    public LocalDateTime getModifiedAt() {
        return modifiedAt;
    }

    /**
     * Gets the last access timestamp.
     *
     * @return the last access timestamp
     */
    public LocalDateTime getAccessedAt() {
        return accessedAt;
    }

    /**
     * Gets a defensive copy of block pointers.
     *
     * @return the block pointer list
     */
    public List<Integer> getBlockPointers() {
        return new ArrayList<>(blockPointers);
    }

    /**
     * Adds a block pointer to this inode.
     *
     * @param blockId the data block identifier
     */
    public void addBlockPointer(int blockId) {
        blockPointers.add(blockId);
    }

    /**
     * Removes all block pointers from this inode.
     */
    public void clearBlockPointers() {
        blockPointers.clear();
    }

    // Directory operations
    /**
     * Adds a child inode to this directory.
     * <p>
     * The operation is ignored when this inode is not a directory, the child is
     * {@code null}, or the child is already present.
     * </p>
     *
     * @param child the child inode
     */
    public void addChild(Inode child) {
        if (isDirectory && child != null && !children.contains(child)) {
            children.add(child);
            child.setParent(this);
            updateModifiedTime();
        }
    }

    /**
     * Removes a child inode from this directory.
     *
     * @param name the name of the child to remove
     * @return true if removed, false otherwise
     */
    public boolean removeChild(String name) {
        if (!isDirectory) {
            return false;
        }
        boolean removed = children.removeIf(c -> c.getName().equals(name));
        if (removed) {
            updateModifiedTime();
        }
        return removed;
    }

    /**
     * Searches for a child inode by name.
     *
     * @param name the name to search for
     * @return the child inode, or null if not found
     */
    public Inode findChild(String name) {
        if (!isDirectory) {
            return null;
        }
        return children.stream()
            .filter(c -> c.getName().equals(name))
            .findFirst()
            .orElse(null);
    }

    /**
     * Checks if a child with the given name exists.
     *
     * @param name the name to check
     * @return true if exists, false otherwise
     */
    public boolean hasChild(String name) {
        return findChild(name) != null;
    }

    // Path operations
    /**
     * Gets the full path of this inode.
     *
     * @return the full path (e.g., "/home/user/file.txt")
     */
    public String getFullPath() {
        if (parent == null) {
            return "/";
        }
        if (parent.getParent() == null) {
            return "/" + name;
        }
        return parent.getFullPath() + "/" + name;
    }

    /**
     * Gets the depth of this inode in the tree.
     *
     * @return 0 for root, 1 for direct children of root, etc.
     */
    public int getDepth() {
        int depth = 0;
        Inode current = this.parent;
        while (current != null && current.getParent() != null) {
            depth++;
            current = current.getParent();
        }
        return depth;
    }

    // Time operations
    /**
     * Updates the modification timestamp to the current system time.
     */
    private void updateModifiedTime() {
        this.modifiedAt = LocalDateTime.now();
    }

    /**
     * Updates the access timestamp to the current system time.
     */
    private void updateAccessedTime() {
        this.accessedAt = LocalDateTime.now();
    }

    /**
     * Gets the inode information as a formatted string.
     * <p>
     * The format is intended for console tables and includes permissions,
     * owner, type, size, and name.
     * </p>
     *
     * @return formatted inode information
     */
    public String getInfo() {
        String type = isDirectory ? "DIR" : "FILE";
        String perms = permissions.toString();
        String sizeStr = isDirectory ? "-" : String.valueOf(size);
        
        return String.format("%-10s %-10s %-6s %10s %s",
            perms, permissions.getOwnerName(), type, sizeStr, name);
    }

    /**
     * Returns a compact diagnostic representation of this inode.
     *
     * @return a string containing the inode ID, name, type, size, and full path
     */
    @Override
    public String toString() {
        return String.format("Inode{id=%d, name='%s', isDir=%s, size=%d, path='%s'}",
            id, name, isDirectory, size, getFullPath());
    }

    /**
     * Creates a deep copy of this inode.
     *
     * @param newParent the parent inode for the copy
     * @return the copied inode
     */
    public Inode deepCopy(Inode newParent) {
        Inode copy = new Inode(this.id, this.name, this.isDirectory, newParent,
            this.permissions.getOwnerName(), this.permissions.getGroupName());
        copy.content = this.content;
        copy.size = this.size;
        copy.blockPointers = new ArrayList<>(this.blockPointers);
        
        if (this.isDirectory) {
            for (Inode child : this.children) {
                copy.addChild(child.deepCopy(copy));
            }
        }
        
        return copy;
    }
}
