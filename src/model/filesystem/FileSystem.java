package model.filesystem;

import model.exceptions.*;
import model.log.FileSystemLogger;
import java.util.*;

/**
 * Represents a complete Unix-style in-memory file system implementation.
 * <p>
 * This class is the main coordinator for the richer file system model. It
 * manages path validation, inode resolution, directory hierarchy operations,
 * file creation and updates, permission changes, search utilities, logging,
 * and storage accounting through the {@link SuperBlock}.
 * </p>
 * <p>
 * The design follows familiar Unix concepts: a root directory, absolute paths,
 * inode metadata, owner/group permissions, recursive directory traversal, and
 * command-like operations such as {@code mkdir}, {@code cp}, {@code mv},
 * {@code chmod}, and {@code rm}. Data is kept in memory for educational
 * purposes, while operational events are written to the file system logger.
 * </p>
 *
 * @author Carolain Quesada
 */
public class FileSystem {
    private final SuperBlock superBlock;
    private final Inode rootInode;
    private final FileSystemLogger logger;
    private String currentWorkingDirectory;

    /**
     * Creates a new file system with default configuration.
     * <p>
     * The default configuration defines block size, total blocks, total inodes,
     * and root ownership values through {@link FileSystemConfig}.
     * </p>
     */
    public FileSystem() {
        this(new FileSystemConfig());
    }

    /**
     * Creates a new file system with the specified configuration.
     * <p>
     * The constructor creates the super block, root inode, operation logger,
     * working directory, and default top-level directories. The provided
     * configuration controls the capacity limits and root ownership metadata.
     * </p>
     *
     * @param config the file system configuration; must not be {@code null}
     */
    public FileSystem(FileSystemConfig config) {
        this.superBlock = new SuperBlock(config);
        this.rootInode = new Inode(0, "", true, null,
            config.getRootOwner(), config.getRootGroup());
        this.logger = new FileSystemLogger("filesystem.log", true);
        this.currentWorkingDirectory = "/";
        
        logger.logInfo("INICIO", "Sistema de archivos inicializado: " + config);
        initializeDefaultDirectories();
    }

    /**
     * Creates the default top-level directories used by the simulator.
     * <p>
     * The method intentionally logs and suppresses initialization failures so
     * that a partially initialized file system can still be inspected from the
     * console.
     * </p>
     */
    private void initializeDefaultDirectories() {
        try {
            makeDirectory("/home", "root", "root");
            makeDirectory("/tmp", "root", "root");
            makeDirectory("/var", "root", "root");
            logger.logInfo("INIT", "Directorios predeterminados creados");
        } catch (FileSystemException e) {
            logger.logError("INIT", e);
        }
    }

    // File operations
    /**
     * Creates a file at the specified path with content.
     * <p>
     * The method validates the absolute path, resolves the parent directory,
     * prevents duplicate file names, allocates block and inode resources, and
     * finally attaches the new file inode to its parent directory.
     * </p>
     *
     * @param path the absolute file path, for example {@code /home/user/file.txt}
     * @param content the file content; {@code null} is converted to an empty
     *        string
     * @param owner the logical owner assigned to the file
     * @throws FileSystemException if the path is invalid, the parent directory
     *         does not exist, the entry already exists, or there is not enough
     *         simulated space
     */
    public void createFile(String path, String content, String owner) throws FileSystemException {
        validatePath(path);
        
        if (content == null) {
            content = "";
        }

        String normalizedPath = normalizePath(path);
        String parentPath = getParentPath(normalizedPath);
        String fileName = getFileName(normalizedPath);

        Inode parentDir = resolveInode(parentPath);
        if (parentDir == null || !parentDir.isDirectory()) {
            throw new FileNotFoundException("Parent directory", parentPath);
        }

        if (parentDir.hasChild(fileName)) {
            throw new DuplicateEntryException(fileName, parentPath);
        }

        // Check space needed
        int blocksNeeded = superBlock.calculateBlocksNeeded(content.length());
        if (superBlock.allocateBlocks(blocksNeeded) == -1) {
            throw new InsufficientSpaceException("blocks", blocksNeeded, 
                superBlock.getFreeBlockCount());
        }

        int inodeId = superBlock.allocateInode();
        if (inodeId == -1) {
            superBlock.freeBlocks(superBlock.allocateBlocks(blocksNeeded), blocksNeeded);
            throw new InsufficientSpaceException("inodes", 1, superBlock.getFreeInodeCount());
        }

        // Create the inode
        Inode fileInode = new Inode(inodeId, fileName, false, parentDir, owner, "users");
        fileInode.setContent(content);
        parentDir.addChild(fileInode);

        logger.logFileOperation("CREATE", normalizedPath, true);
    }

    /**
     * Reads the content of a file.
     * <p>
     * Directories cannot be read as files. Successful reads are recorded in the
     * operation log.
     * </p>
     *
     * @param path the absolute file path
     * @return the file content
     * @throws FileSystemException if the path is invalid, the file is missing,
     *         or the target is a directory
     */
    public String readFile(String path) throws FileSystemException {
        validatePath(path);
        String normalizedPath = normalizePath(path);
        
        Inode inode = resolveInode(normalizedPath);
        if (inode == null) {
            throw new FileNotFoundException(normalizedPath);
        }

        if (inode.isDirectory()) {
            throw new FileSystemException("Cannot read: " + normalizedPath + " is a directory");
        }

        logger.logFileOperation("READ", normalizedPath, true);
        return inode.getContent();
    }

    /**
     * Writes content to a file, either by overwriting or appending.
     * <p>
     * The method validates the target, calculates the storage needed for the
     * resulting content, updates the inode content, and logs the operation.
     * </p>
     *
     * @param path the absolute file path
     * @param content the content to write; should not be {@code null}
     * @param append {@code true} to append to existing content; {@code false}
     *        to replace it
     * @throws FileSystemException if the path is invalid, the file is missing,
     *         the target is a directory, or simulated storage is insufficient
     */
    public void writeFile(String path, String content, boolean append) throws FileSystemException {
        validatePath(path);
        String normalizedPath = normalizePath(path);

        Inode inode = resolveInode(normalizedPath);
        if (inode == null) {
            throw new FileNotFoundException(normalizedPath);
        }

        if (inode.isDirectory()) {
            throw new FileSystemException("Cannot write: " + normalizedPath + " is a directory");
        }

        String newContent = append ? inode.getContent() + content : content;
        int blocksNeeded = superBlock.calculateBlocksNeeded(newContent.length());

        if (superBlock.allocateBlocks(blocksNeeded) == -1) {
            throw new InsufficientSpaceException("blocks", blocksNeeded, 
                superBlock.getFreeBlockCount());
        }

        inode.setContent(newContent);
        logger.logFileOperation("WRITE", normalizedPath, true);
    }

    /**
     * Deletes a file or empty directory.
     * <p>
     * Root deletion is rejected. Directory deletion is allowed only when the
     * directory has no children; callers that need recursive deletion should use
     * {@link #deleteRecursive(String)} instead.
     * </p>
     *
     * @param path the absolute path to delete
     * @throws FileSystemException if the path is invalid, the target does not
     *         exist, the target is root, or the directory is not empty
     */
    public void deleteEntry(String path) throws FileSystemException {
        validatePath(path);
        String normalizedPath = normalizePath(path);

        if (normalizedPath.equals("/")) {
            throw new FileSystemException("Cannot delete root directory");
        }

        String parentPath = getParentPath(normalizedPath);
        String entryName = getFileName(normalizedPath);

        Inode parentDir = resolveInode(parentPath);
        if (parentDir == null) {
            throw new FileNotFoundException("Parent directory", parentPath);
        }

        Inode entryInode = parentDir.findChild(entryName);
        if (entryInode == null) {
            throw new FileNotFoundException(normalizedPath);
        }

        if (entryInode.isDirectory() && !entryInode.getChildren().isEmpty()) {
            throw new DirectoryNotEmptyException(normalizedPath);
        }

        // Free resources
        superBlock.freeInode(entryInode.getId());
        int blocksUsed = superBlock.calculateBlocksNeeded(entryInode.getSize());
        if (blocksUsed > 0) {
            superBlock.freeBlocks(0, blocksUsed);
        }

        parentDir.removeChild(entryName);
        logger.logFileOperation("DELETE", normalizedPath, true);
    }

    /**
     * Recursively deletes a file or directory.
     * <p>
     * Directory children are copied before traversal so entries can be removed
     * safely while iterating.
     * </p>
     *
     * @param path the absolute path to delete
     * @throws FileSystemException if the path is invalid or any delete step
     *         fails
     */
    public void deleteRecursive(String path) throws FileSystemException {
        validatePath(path);
        String normalizedPath = normalizePath(path);

        Inode inode = resolveInode(normalizedPath);
        if (inode == null) {
            throw new FileNotFoundException(normalizedPath);
        }

        if (inode.isDirectory()) {
            List<Inode> childrenCopy = new ArrayList<>(inode.getChildren());
            for (Inode child : childrenCopy) {
                deleteRecursive(normalizedPath + "/" + child.getName());
            }
        }

        deleteEntry(normalizedPath);
    }

    // Directory operations
    /**
     * Creates a directory at the specified path.
     * <p>
     * The parent directory must already exist, and the new directory name must
     * not already be present under that parent.
     * </p>
     *
     * @param path the absolute directory path
     * @param owner the directory owner
     * @param group the directory group
     * @throws FileSystemException if the path is invalid, the parent directory
     *         is missing, an entry already exists, or no inode is available
     */
    public void makeDirectory(String path, String owner, String group) throws FileSystemException {
        validatePath(path);
        String normalizedPath = normalizePath(path);

        if (resolveInode(normalizedPath) != null) {
            throw new DuplicateEntryException(getFileName(normalizedPath), 
                getParentPath(normalizedPath));
        }

        String parentPath = getParentPath(normalizedPath);
        String dirName = getFileName(normalizedPath);

        Inode parentDir = resolveInode(parentPath);
        if (parentDir == null || !parentDir.isDirectory()) {
            throw new FileNotFoundException("Parent directory", parentPath);
        }

        int inodeId = superBlock.allocateInode();
        if (inodeId == -1) {
            throw new InsufficientSpaceException("inodes", 1, superBlock.getFreeInodeCount());
        }

        Inode dirInode = new Inode(inodeId, dirName, true, parentDir, owner, group);
        parentDir.addChild(dirInode);

        logger.logFileOperation("MKDIR", normalizedPath, true);
    }

    /**
     * Lists the contents of a directory.
     * <p>
     * The returned list includes the conventional {@code .} and {@code ..}
     * entries followed by each child with a one-character type marker:
     * {@code d} for directories and {@code -} for files.
     * </p>
     *
     * @param path the absolute directory path
     * @return a list of formatted directory entries
     * @throws FileSystemException if the path is invalid or does not identify
     *         an existing directory
     */
    public List<String> listDirectory(String path) throws FileSystemException {
        validatePath(path);
        String normalizedPath = normalizePath(path);

        Inode dir = resolveInode(normalizedPath);
        if (dir == null || !dir.isDirectory()) {
            throw new FileNotFoundException("Directory", normalizedPath);
        }

        List<String> entries = new ArrayList<>();
        entries.add(".");
        entries.add("..");

        for (Inode child : dir.getChildren()) {
            String type = child.isDirectory() ? "d" : "-";
            entries.add(String.format("%s %s", type, child.getName()));
        }

        return entries;
    }

    /**
     * Changes the current working directory.
     *
     * @param path the absolute directory path
     * @throws FileSystemException if the path is invalid or does not identify
     *         an existing directory
     */
    public void changeDirectory(String path) throws FileSystemException {
        validatePath(path);
        String normalizedPath = normalizePath(path);

        Inode dir = resolveInode(normalizedPath);
        if (dir == null || !dir.isDirectory()) {
            throw new FileNotFoundException("Directory", normalizedPath);
        }

        this.currentWorkingDirectory = normalizedPath;
        logger.logInfo("CD", "Cambiado a: " + normalizedPath);
    }

    // Copy and move operations
    /**
     * Copies a file to a new location.
     * <p>
     * Only regular files are supported by this method. Directories are copied
     * through the recursive helper used by {@link #moveEntry(String, String)}.
     * </p>
     *
     * @param sourcePath the absolute source file path
     * @param destinationPath the absolute destination file path
     * @throws FileSystemException if either path is invalid, the source is not
     *         a file, the destination cannot be created, or storage is
     *         insufficient
     */
    public void copyFile(String sourcePath, String destinationPath) throws FileSystemException {
        validatePath(sourcePath);
        validatePath(destinationPath);

        String normalizedSource = normalizePath(sourcePath);
        String normalizedDest = normalizePath(destinationPath);

        Inode sourceInode = resolveInode(normalizedSource);
        if (sourceInode == null || sourceInode.isDirectory()) {
            throw new FileNotFoundException(normalizedSource);
        }

        String content = sourceInode.getContent();
        createFile(normalizedDest, content, sourceInode.getPermissions().getOwnerName());

        logger.logFileOperation("COPY", normalizedSource + " -> " + normalizedDest, true);
    }

    /**
     * Moves or renames a file or directory.
     * <p>
     * The operation is implemented as copy followed by recursive deletion of
     * the original entry.
     * </p>
     *
     * @param sourcePath the absolute source path
     * @param destinationPath the absolute destination path
     * @throws FileSystemException if validation, copy, or deletion fails
     */
    public void moveEntry(String sourcePath, String destinationPath) throws FileSystemException {
        validatePath(sourcePath);
        validatePath(destinationPath);

        String normalizedSource = normalizePath(sourcePath);
        String normalizedDest = normalizePath(destinationPath);

        Inode sourceInode = resolveInode(normalizedSource);
        if (sourceInode == null) {
            throw new FileNotFoundException(normalizedSource);
        }

        if (sourceInode.isDirectory()) {
            copyDirectory(normalizedSource, normalizedDest);
        } else {
            copyFile(normalizedSource, normalizedDest);
        }

        deleteRecursive(normalizedSource);
        logger.logFileOperation("MOVE", normalizedSource + " -> " + normalizedDest, true);
    }

    /**
     * Copies a directory and all of its descendants to a new location.
     *
     * @param sourcePath the absolute source directory path
     * @param destinationPath the absolute destination directory path
     * @throws FileSystemException if the destination cannot be created or any
     *         child copy fails
     */
    private void copyDirectory(String sourcePath, String destinationPath) throws FileSystemException {
        makeDirectory(destinationPath, "root", "root");

        Inode sourceDir = resolveInode(sourcePath);
        for (Inode child : sourceDir.getChildren()) {
            String childSourcePath = sourcePath + "/" + child.getName();
            String childDestPath = destinationPath + "/" + child.getName();

            if (child.isDirectory()) {
                copyDirectory(childSourcePath, childDestPath);
            } else {
                copyFile(childSourcePath, childDestPath);
            }
        }
    }

    // Search operations
    /**
     * Searches files and directories by name pattern.
     *
     * @param pattern the search pattern; matched as a case-insensitive
     *        substring
     * @return a list of matching absolute paths
     */
    public List<String> searchByName(String pattern) {
        List<String> results = new ArrayList<>();
        searchRecursive(rootInode, pattern.toLowerCase(), results, true);
        return results;
    }

    /**
     * Searches regular files by content.
     *
     * @param pattern the search pattern; matched as a case-insensitive
     *        substring
     * @return a list of absolute file paths whose content contains the pattern
     */
    public List<String> searchByContent(String pattern) {
        List<String> results = new ArrayList<>();
        searchRecursive(rootInode, pattern.toLowerCase(), results, false);
        return results;
    }

    /**
     * Recursively walks the inode tree and appends matching paths to a result
     * list.
     *
     * @param current the inode currently being inspected
     * @param pattern the lower-case search pattern
     * @param results the mutable result list to populate
     * @param searchByName {@code true} to match inode names; {@code false} to
     *        match file content only
     */
    private void searchRecursive(Inode current, String pattern, List<String> results, 
                                 boolean searchByName) {
        if (!current.isDirectory()) {
            if (searchByName) {
                if (current.getName().toLowerCase().contains(pattern)) {
                    results.add(current.getFullPath());
                }
            } else {
                if (current.getContent() != null && 
                    current.getContent().toLowerCase().contains(pattern)) {
                    results.add(current.getFullPath());
                }
            }
        } else {
            if (searchByName && !current.getName().isBlank() && 
                current.getName().toLowerCase().contains(pattern)) {
                results.add(current.getFullPath() + "/");
            }

            for (Inode child : current.getChildren()) {
                searchRecursive(child, pattern, results, searchByName);
            }
        }
    }

    // Permission operations
    /**
     * Changes file permissions.
     * <p>
     * Permissions are expressed with the traditional three-digit octal style:
     * owner, group, and others. For example, {@code 755} grants full owner
     * permissions and read/execute permissions to group and others.
     * </p>
     *
     * @param path the absolute file or directory path
     * @param permissions the permissions in three-digit octal form, for example
     *        {@code 755} or {@code 644}
     * @throws FileSystemException if the path is invalid, permissions are
     *         malformed, or the entry does not exist
     */
    public void changePermissions(String path, String permissions) throws FileSystemException {
        validatePath(path);
        String normalizedPath = normalizePath(path);

        if (permissions.length() != 3) {
            throw new FileSystemException("Invalid permission format: " + permissions);
        }

        Inode inode = resolveInode(normalizedPath);
        if (inode == null) {
            throw new FileNotFoundException(normalizedPath);
        }

        try {
            int owner = Character.getNumericValue(permissions.charAt(0));
            int group = Character.getNumericValue(permissions.charAt(1));
            int others = Character.getNumericValue(permissions.charAt(2));

            inode.getPermissions().setOwnerPermissions(owner);
            inode.getPermissions().setGroupPermissions(group);
            inode.getPermissions().setOthersPermissions(others);

            logger.logInfo("CHMOD", normalizedPath + " -> " + permissions);
        } catch (NumberFormatException e) {
            throw new FileSystemException("Valores de permisos inválidos: " + permissions);
        }
    }

    // Path resolution
    /**
     * Resolves a path to its inode.
     * <p>
     * Resolution starts at the root inode and walks each path segment in order.
     * Invalid paths or missing segments return {@code null} instead of throwing,
     * which makes this method convenient for existence checks.
     * </p>
     *
     * @param path the absolute file or directory path
     * @return the matching inode, or {@code null} if not found
     */
    public Inode resolveInode(String path) {
        try {
            validatePath(path);
        } catch (InvalidPathException e) {
            return null;
        }

        String normalizedPath = normalizePath(path);

        if (normalizedPath.equals("/")) {
            return rootInode;
        }

        String[] segments = normalizedPath.split("/");
        Inode current = rootInode;

        for (String segment : segments) {
            if (segment.isEmpty()) {
                continue;
            }

            current = current.findChild(segment);
            if (current == null) {
                return null;
            }
        }

        return current;
    }

    /**
     * Gets the complete tree structure of the file system.
     *
     * @return a formatted tree representation rooted at {@code /}
     */
    public String getTreeStructure() {
        StringBuilder sb = new StringBuilder();
        printTree(rootInode, "", sb);
        return sb.toString();
    }

    /**
     * Appends a formatted subtree to a string builder.
     *
     * @param inode the inode to render
     * @param prefix the connector prefix used for nested entries
     * @param sb the destination builder
     */
    private void printTree(Inode inode, String prefix, StringBuilder sb) {
        if (!inode.getName().isEmpty()) {
            sb.append(prefix).append(inode.isDirectory() ? "📁 " : "📄 ")
              .append(inode.getName()).append("\n");
            prefix = prefix.replace("├── ", "│   ").replace("└── ", "    ");
        }

        if (inode.isDirectory()) {
            List<Inode> children = inode.getChildren();
            for (int i = 0; i < children.size(); i++) {
                String connector = (i == children.size() - 1) ? "└── " : "├── ";
                printTree(children.get(i), prefix + connector, sb);
            }
        }
    }

    // Utility methods
    /**
     * Validates the minimum requirements for a path accepted by this file
     * system.
     *
     * @param path the path to validate
     * @throws InvalidPathException if the path is {@code null}, blank, or does
     *         not start with {@code /}
     */
    private void validatePath(String path) throws InvalidPathException {
        if (path == null || path.trim().isEmpty()) {
            throw new InvalidPathException(path, "Path cannot be empty");
        }
        if (!path.startsWith("/")) {
            throw new InvalidPathException(path, "Path must start with /");
        }
    }

    /**
     * Normalizes repeated and trailing path separators.
     *
     * @param path the absolute path to normalize
     * @return the normalized path, preserving {@code /} for the root directory
     */
    private String normalizePath(String path) {
        if (path.equals("/")) {
            return "/";
        }
        path = path.replaceAll("/+", "/");
        while (path.endsWith("/") && path.length() > 1) {
            path = path.substring(0, path.length() - 1);
        }
        return path;
    }

    /**
     * Extracts the parent directory path from an absolute path.
     *
     * @param path the normalized absolute path
     * @return the parent path, or {@code /} for root-level entries
     */
    private String getParentPath(String path) {
        if (path.equals("/")) {
            return "/";
        }
        int lastSlash = path.lastIndexOf('/');
        return lastSlash == 0 ? "/" : path.substring(0, lastSlash);
    }

    /**
     * Extracts the final path segment from an absolute path.
     *
     * @param path the normalized absolute path
     * @return the file or directory name portion of the path
     */
    private String getFileName(String path) {
        int lastSlash = path.lastIndexOf('/');
        return path.substring(lastSlash + 1);
    }

    // Statistics and info
    /**
     * Gets the formatted file system statistics.
     *
     * @return a statistics report from the super block
     */
    public String getStatistics() {
        return superBlock.getStatistics();
    }

    /**
     * Gets the current working directory.
     *
     * @return the current working directory path
     */
    public String getCurrentWorkingDirectory() {
        return currentWorkingDirectory;
    }

    /**
     * Gets the super block used by this file system.
     *
     * @return the super block instance
     */
    public SuperBlock getSuperBlock() {
        return superBlock;
    }

    /**
     * Gets the logger used for file system events.
     *
     * @return the file system logger
     */
    public FileSystemLogger getLogger() {
        return logger;
    }

    /**
     * Gets the root inode.
     *
     * @return the root inode
     */
    public Inode getRootInode() {
        return rootInode;
    }

    /**
     * Returns a compact diagnostic representation of this file system.
     *
     * @return a string containing the current working directory and superblock
     *         summary
     */
    @Override
    public String toString() {
        return String.format("FileSystem{cwd='%s', %s}", currentWorkingDirectory, superBlock);
    }
}
