package model.filesystem;

/**
 * Represents the superblock of the Unix-style file system.
 *
 * <p>The superblock is the single authoritative record of file system metadata.
 * It owns two bitmaps — one for inodes and one for data blocks — and exposes
 * allocation/deallocation operations that keep those bitmaps consistent.  It
 * also tracks auxiliary accounting information such as the creation time,
 * last-modification time, and mount count.</p>
 *
 * <p>When any allocation or deallocation changes the superblock state, the
 * object marks itself as <em>dirty</em>.  Callers can inspect this flag with
 * {@link #isDirty()} and clear it with {@link #markClean()} after flushing
 * the superblock to persistent storage.</p>
 *
 * @author Carolain Quesada
 * @version 1.0
 * @see FileSystemConfig
 * @see InodeBitmap
 * @see BlockBitmap
 */
public class SuperBlock {

    /** Configuration parameters that define the size and layout of the file system. */
    private final FileSystemConfig config;

    /** Bitmap tracking which inode slots are in use. */
    private final InodeBitmap inodeBitmap;

    /** Bitmap tracking which data blocks are in use. */
    private final BlockBitmap blockBitmap;

    /** The epoch-millisecond timestamp when this superblock was created. */
    private long creationTime;

    /** The epoch-millisecond timestamp of the most recent state change. */
    private long lastModificationTime;

    /**
     * Number of times the file system has been mounted.
     * Starts at {@code 1} after construction.
     */
    private int mountCount;

    /**
     * {@code true} when there are unsaved changes that have not yet been
     * flushed to persistent storage.
     */
    private boolean isDirty;

    /**
     * Constructs a {@code SuperBlock} from a {@link FileSystemConfig}.
     *
     * <p>Both bitmaps are initialised, the creation and modification
     * timestamps are set to the current time, the mount count is set to
     * {@code 1}, and the dirty flag is cleared.</p>
     *
     * @param config the file system configuration; must not be {@code null}
     */
    public SuperBlock(FileSystemConfig config) {
        this.config = config;
        this.inodeBitmap = new InodeBitmap(config.getTotalInodes());
        this.blockBitmap = new BlockBitmap(config.getTotalBlocks());
        this.creationTime = System.currentTimeMillis();
        this.lastModificationTime = this.creationTime;
        this.mountCount = 1;
        this.isDirty = false;
    }

    /**
     * Returns the configuration that was used to create this superblock.
     *
     * @return the {@link FileSystemConfig}; never {@code null}
     */
    public FileSystemConfig getConfig() {
        return config;
    }

    /**
     * Returns the data block size.
     *
     * @return the block size in bytes
     */
    public int getBlockSize() {
        return config.getBlockSize();
    }

    /**
     * Returns the total number of inode slots in the file system.
     *
     * @return the total inode count
     */
    public int getTotalInodes() {
        return config.getTotalInodes();
    }

    /**
     * Returns the total number of data blocks in the file system.
     *
     * @return the total block count
     */
    public int getTotalBlocks() {
        return config.getTotalBlocks();
    }

    // -----------------------------------------------------------------------
    // Inode allocation
    // -----------------------------------------------------------------------

    /**
     * Allocates the next free inode slot and returns its ID.
     *
     * <p>On success the superblock is marked dirty.</p>
     *
     * @return the allocated inode ID, or {@code -1} if no free inodes remain
     */
    public int allocateInode() {
        int inodeId = inodeBitmap.allocate();
        if (inodeId != -1) {
            markDirty();
        }
        return inodeId;
    }

    /**
     * Releases a previously allocated inode slot.
     *
     * <p>Errors (invalid ID or slot not allocated) are caught and printed to
     * standard error rather than propagated, so that a single bad deallocation
     * does not abort the whole operation.</p>
     *
     * @param inodeId the inode ID to free
     */
    public void freeInode(int inodeId) {
        try {
            inodeBitmap.deallocate(inodeId);
            markDirty();
        } catch (IllegalArgumentException e) {
            System.err.println("Error freeing inode: " + e.getMessage());
        }
    }

    /**
     * Checks whether a specific inode slot is currently allocated.
     *
     * @param inodeId the inode identifier to query
     * @return {@code true} if the slot is in use
     */
    public boolean isInodeAllocated(int inodeId) {
        return inodeBitmap.isAllocated(inodeId);
    }

    /**
     * Returns the number of free inode slots.
     *
     * @return the free inode count
     */
    public int getFreeInodeCount() {
        return inodeBitmap.getFreeCount();
    }

    /**
     * Returns the number of occupied inode slots.
     *
     * @return the used inode count
     */
    public int getUsedInodeCount() {
        return inodeBitmap.getUsedCount();
    }

    // -----------------------------------------------------------------------
    // Block allocation
    // -----------------------------------------------------------------------

    /**
     * Allocates a contiguous run of data blocks.
     *
     * <p>On success the superblock is marked dirty.</p>
     *
     * @param numberOfBlocks the number of consecutive blocks to allocate
     * @return the starting block ID, or {@code -1} if not enough contiguous
     *         free blocks are available
     */
    public int allocateBlocks(int numberOfBlocks) {
        int startBlock = blockBitmap.allocate(numberOfBlocks);
        if (startBlock != -1) {
            markDirty();
        }
        return startBlock;
    }

    /**
     * Releases a contiguous run of data blocks.
     *
     * <p>Errors are caught and printed to standard error rather than
     * propagated.</p>
     *
     * @param startBlock     the first block in the run to free
     * @param numberOfBlocks the number of consecutive blocks to free
     */
    public void freeBlocks(int startBlock, int numberOfBlocks) {
        try {
            blockBitmap.deallocate(startBlock, numberOfBlocks);
            markDirty();
        } catch (IllegalArgumentException e) {
            System.err.println("Error freeing blocks: " + e.getMessage());
        }
    }

    /**
     * Checks whether a specific data block is currently allocated.
     *
     * @param blockId the block identifier to query
     * @return {@code true} if the block is in use
     */
    public boolean isBlockAllocated(int blockId) {
        return blockBitmap.isAllocated(blockId);
    }

    /**
     * Returns the number of free data blocks.
     *
     * @return the free block count
     */
    public int getFreeBlockCount() {
        return blockBitmap.getFreeCount();
    }

    /**
     * Returns the number of occupied data blocks.
     *
     * @return the used block count
     */
    public int getUsedBlockCount() {
        return blockBitmap.getUsedCount();
    }

    /**
     * Delegates block-count calculation to the file system configuration.
     *
     * @param fileSize the file size in bytes
     * @return the number of blocks required to store a file of that size
     */
    public int calculateBlocksNeeded(int fileSize) {
        return config.calculateBlocksNeeded(fileSize);
    }

    // -----------------------------------------------------------------------
    // Timestamps and mount count
    // -----------------------------------------------------------------------

    /**
     * Returns the epoch-millisecond timestamp when this superblock was created.
     *
     * @return the creation timestamp in milliseconds
     */
    public long getCreationTime() {
        return creationTime;
    }

    /**
     * Returns the epoch-millisecond timestamp of the most recent state change.
     *
     * @return the last-modification timestamp in milliseconds
     */
    public long getLastModificationTime() {
        return lastModificationTime;
    }

    /**
     * Sets the last-modification timestamp to the current time and marks the
     * superblock as dirty.
     */
    public void updateLastModificationTime() {
        this.lastModificationTime = System.currentTimeMillis();
        markDirty();
    }

    /**
     * Returns the number of times this file system has been mounted.
     *
     * @return the mount count; always {@code >= 1} after construction
     */
    public int getMountCount() {
        return mountCount;
    }

    /**
     * Increments the mount count by one and marks the superblock as dirty.
     */
    public void incrementMountCount() {
        this.mountCount++;
        markDirty();
    }

    // -----------------------------------------------------------------------
    // Dirty flag
    // -----------------------------------------------------------------------

    /**
     * Returns whether the superblock has unsaved in-memory changes.
     *
     * @return {@code true} if the superblock is dirty
     */
    public boolean isDirty() {
        return isDirty;
    }

    /**
     * Marks the superblock as having unsaved changes.
     *
     * <p>This method is called automatically by all allocation and
     * deallocation methods.</p>
     */
    public void markDirty() {
        this.isDirty = true;
    }

    /**
     * Marks the superblock as clean, indicating that all changes have been
     * flushed to persistent storage.
     */
    public void markClean() {
        this.isDirty = false;
    }

    // -----------------------------------------------------------------------
    // Statistics
    // -----------------------------------------------------------------------

    /**
     * Returns the total storage capacity of the file system in bytes.
     *
     * @return total capacity in bytes ({@code totalBlocks * blockSize})
     */
    public long getTotalCapacity() {
        return config.getTotalCapacity();
    }

    /**
     * Returns the number of bytes currently consumed by allocated blocks.
     *
     * @return used space in bytes
     */
    public long getUsedSpace() {
        return (long) getUsedBlockCount() * getBlockSize();
    }

    /**
     * Returns the number of bytes currently available in free blocks.
     *
     * @return free space in bytes
     */
    public long getFreeSpace() {
        return (long) getFreeBlockCount() * getBlockSize();
    }

    /**
     * Returns the percentage of total capacity that is currently in use.
     *
     * @return usage percentage in the range {@code [0.0, 100.0]}; returns
     *         {@code 0.0} when the total capacity is zero
     */
    public double getUsagePercentage() {
        if (getTotalCapacity() == 0) return 0;
        return (getUsedSpace() * 100.0) / getTotalCapacity();
    }

    /**
     * Returns a formatted multi-line statistics report.
     *
     * <p>The report includes total capacity, used and free space (with
     * percentages), inode and block usage, mount count, and dirty status.</p>
     *
     * @return a human-readable statistics string
     */
    public String getStatistics() {
        return String.format(
            "=== Estadísticas del Sistema de Archivos ===\n" +
            "Capacidad Total: %d KB\n" +
            "Espacio Usado: %d KB (%.2f%%)\n" +
            "Espacio Libre: %d KB (%.2f%%)\n" +
            "Inodos: %d usados / %d total\n" +
            "Bloques: %d usados / %d total (tamaño: %d KB cada uno)\n" +
            "Conteo de Montajes: %d\n" +
            "Estado: %s",
            getTotalCapacity() / 1024,
            getUsedSpace() / 1024,
            getUsagePercentage(),
            getFreeSpace() / 1024,
            100 - getUsagePercentage(),
            getUsedInodeCount(),
            getTotalInodes(),
            getUsedBlockCount(),
            getTotalBlocks(),
            getBlockSize() / 1024,
            getMountCount(),
            isDirty ? "SUCIO" : "LIMPIO"
        );
    }

    /**
     * Returns a compact one-line summary of the superblock state.
     *
     * @return a string in the form
     *         {@code SuperBlock{capacity=N KB, usage=X.XX%, inodes=A/B, blocks=C/D}}
     */
    @Override
    public String toString() {
        return String.format("SuperBlock{capacity=%d KB, usage=%.2f%%, inodes=%d/%d, blocks=%d/%d}",
            getTotalCapacity() / 1024, getUsagePercentage(),
            getUsedInodeCount(), getTotalInodes(),
            getUsedBlockCount(), getTotalBlocks());
    }
}