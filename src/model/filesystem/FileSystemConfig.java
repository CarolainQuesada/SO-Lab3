package model.filesystem;

/**
 * Holds the configuration parameters that define the shape of a file system
 * instance.
 *
 * <p>A {@code FileSystemConfig} is typically created once and passed to the
 * {@link FileSystem} and {@link SuperBlock} constructors.  All limits and
 * default values are exposed as public constants so that callers can refer to
 * them without constructing an instance.</p>
 *
 * <p>Example — creating a small test file system:</p>
 * <pre>{@code
 * FileSystemConfig cfg = new FileSystemConfig(512, 64, 256, "admin", "staff");
 * FileSystem fs = new FileSystem(cfg);
 * }</pre>
 *
 * @author Carolain Quesada
 * @version 1.0
 * @see FileSystem
 * @see SuperBlock
 */
public class FileSystemConfig {

    /** Default block size in bytes (4 KB). */
    public static final int DEFAULT_BLOCK_SIZE = 4096;

    /** Default maximum number of inodes in the file system. */
    public static final int DEFAULT_TOTAL_INODES = 1000;

    /** Default maximum number of data blocks in the file system. */
    public static final int DEFAULT_TOTAL_BLOCKS = 10000;

    /** Default owner name assigned to the root directory. */
    public static final String DEFAULT_OWNER = "root";

    /** Default group name assigned to the root directory. */
    public static final String DEFAULT_GROUP = "root";

    /** The size of each data block in bytes. */
    private int blockSize;

    /** The total number of inodes available in the file system. */
    private int totalInodes;

    /** The total number of data blocks available in the file system. */
    private int totalBlocks;

    /** The owner name assigned to the root directory. */
    private String rootOwner;

    /** The group name assigned to the root directory. */
    private String rootGroup;

    /**
     * Constructs a {@code FileSystemConfig} populated with all default values.
     *
     * <ul>
     *   <li>Block size: {@value #DEFAULT_BLOCK_SIZE} bytes</li>
     *   <li>Total inodes: {@value #DEFAULT_TOTAL_INODES}</li>
     *   <li>Total blocks: {@value #DEFAULT_TOTAL_BLOCKS}</li>
     *   <li>Root owner/group: {@value #DEFAULT_OWNER}</li>
     * </ul>
     */
    public FileSystemConfig() {
        this(DEFAULT_BLOCK_SIZE, DEFAULT_TOTAL_INODES, DEFAULT_TOTAL_BLOCKS,
             DEFAULT_OWNER, DEFAULT_GROUP);
    }

    /**
     * Constructs a {@code FileSystemConfig} with fully specified parameters.
     *
     * @param blockSize   the size of each data block in bytes; must be {@code > 0}
     * @param totalInodes the total number of inodes; must be {@code > 0}
     * @param totalBlocks the total number of data blocks; must be {@code > 0}
     * @param rootOwner   the owner name for the root directory; must not be
     *                    {@code null}
     * @param rootGroup   the group name for the root directory; must not be
     *                    {@code null}
     */
    public FileSystemConfig(int blockSize, int totalInodes, int totalBlocks,
                           String rootOwner, String rootGroup) {
        this.blockSize = blockSize;
        this.totalInodes = totalInodes;
        this.totalBlocks = totalBlocks;
        this.rootOwner = rootOwner;
        this.rootGroup = rootGroup;
    }

    /**
     * Returns the data block size.
     *
     * @return the block size in bytes
     */
    public int getBlockSize() {
        return blockSize;
    }

    /**
     * Sets the data block size.
     *
     * @param blockSize the block size in bytes; must be {@code > 0}
     */
    public void setBlockSize(int blockSize) {
        this.blockSize = blockSize;
    }

    /**
     * Returns the total number of inodes.
     *
     * @return the inode count
     */
    public int getTotalInodes() {
        return totalInodes;
    }

    /**
     * Sets the total number of inodes.
     *
     * @param totalInodes the inode count; must be {@code > 0}
     */
    public void setTotalInodes(int totalInodes) {
        this.totalInodes = totalInodes;
    }

    /**
     * Returns the total number of data blocks.
     *
     * @return the block count
     */
    public int getTotalBlocks() {
        return totalBlocks;
    }

    /**
     * Sets the total number of data blocks.
     *
     * @param totalBlocks the block count; must be {@code > 0}
     */
    public void setTotalBlocks(int totalBlocks) {
        this.totalBlocks = totalBlocks;
    }

    /**
     * Returns the owner name assigned to the root directory.
     *
     * @return the root owner name
     */
    public String getRootOwner() {
        return rootOwner;
    }

    /**
     * Sets the owner name assigned to the root directory.
     *
     * @param rootOwner the root owner name; must not be {@code null}
     */
    public void setRootOwner(String rootOwner) {
        this.rootOwner = rootOwner;
    }

    /**
     * Returns the group name assigned to the root directory.
     *
     * @return the root group name
     */
    public String getRootGroup() {
        return rootGroup;
    }

    /**
     * Sets the group name assigned to the root directory.
     *
     * @param rootGroup the root group name; must not be {@code null}
     */
    public void setRootGroup(String rootGroup) {
        this.rootGroup = rootGroup;
    }

    /**
     * Calculates the number of data blocks required to store a file of the
     * given size.
     *
     * <p>Uses ceiling division:
     * {@code ceil(fileSize / blockSize)}.</p>
     *
     * @param fileSize the file size in bytes; must be {@code >= 0}
     * @return the required number of blocks; {@code 0} when {@code fileSize}
     *         is {@code 0}
     */
    public int calculateBlocksNeeded(int fileSize) {
        return (fileSize + blockSize - 1) / blockSize;
    }

    /**
     * Calculates the total storage capacity of the file system.
     *
     * @return the total capacity in bytes ({@code totalBlocks * blockSize})
     */
    public long getTotalCapacity() {
        return (long) totalBlocks * blockSize;
    }

    /**
     * Returns a concise string summarising this configuration.
     *
     * @return a formatted configuration summary
     */
    @Override
    public String toString() {
        return String.format(
            "FileSystemConfig{blockSize=%d, totalInodes=%d, totalBlocks=%d, capacity=%d KB}",
            blockSize, totalInodes, totalBlocks, getTotalCapacity() / 1024
        );
    }
}