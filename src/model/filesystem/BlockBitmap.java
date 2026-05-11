package model.filesystem;

/**
 * Tracks allocated and free data blocks in the file system using a boolean
 * bitmap.
 *
 * <p>Each position {@code i} in the internal array corresponds to block
 * {@code i}: {@code true} means the block is in use and {@code false} means
 * it is free.  Allocation always searches for a run of consecutive free
 * blocks because the current implementation writes file content contiguously
 * when possible.</p>
 *
 * <p>This class is <em>not</em> thread-safe.</p>
 *
 * @author Carolain Quesada
 * @version 1.0
 * @see SuperBlock
 */
public class BlockBitmap {

    /**
     * The bitmap array.  {@code bitmap[i] == true} means block {@code i} is
     * allocated.
     */
    private final boolean[] bitmap;

    /** The total number of blocks managed by this bitmap. */
    private final int totalBlocks;

    /** The number of currently allocated blocks. */
    private int usedCount;

    /**
     * Constructs a {@code BlockBitmap} for the specified number of blocks.
     * All blocks are initially free.
     *
     * @param totalBlocks the total number of blocks to manage; must be
     *                    {@code > 0}
     */
    public BlockBitmap(int totalBlocks) {
        this.totalBlocks = totalBlocks;
        this.bitmap = new boolean[totalBlocks];
        this.usedCount = 0;
    }

    /**
     * Allocates a contiguous run of {@code numberOfBlocks} free blocks.
     *
     * <p>The bitmap is scanned from block 0 onwards for the first run long
     * enough to satisfy the request.  When such a run is found all blocks in
     * the run are marked as used and the starting block ID is returned.</p>
     *
     * @param numberOfBlocks the number of consecutive blocks to allocate;
     *                       must be {@code > 0} and {@code <= totalBlocks}
     * @return the starting block ID (inclusive) of the allocated run, or
     *         {@code -1} if no sufficiently long contiguous free run exists
     */
    public int allocate(int numberOfBlocks) {
        if (numberOfBlocks <= 0 || numberOfBlocks > totalBlocks) {
            return -1;
        }

        int consecutiveCount = 0;
        int startBlock = -1;

        for (int i = 0; i < totalBlocks; i++) {
            if (!bitmap[i]) {
                if (consecutiveCount == 0) {
                    startBlock = i;
                }
                consecutiveCount++;

                if (consecutiveCount == numberOfBlocks) {
                    for (int j = startBlock; j < startBlock + numberOfBlocks; j++) {
                        bitmap[j] = true;
                    }
                    usedCount += numberOfBlocks;
                    return startBlock;
                }
            } else {
                consecutiveCount = 0;
            }
        }

        return -1;
    }

    /**
     * Releases a contiguous run of allocated blocks.
     *
     * @param startBlock     the first block in the run to free; must be
     *                       {@code >= 0}
     * @param numberOfBlocks the number of consecutive blocks to free
     * @throws IllegalArgumentException if the block range is outside the
     *         valid range {@code [0, totalBlocks)}, or if any block in the
     *         range was not previously allocated
     */
    public void deallocate(int startBlock, int numberOfBlocks) {
        if (startBlock < 0 || startBlock + numberOfBlocks > totalBlocks) {
            throw new IllegalArgumentException(
                String.format("Invalid block range: start=%d, count=%d", startBlock, numberOfBlocks)
            );
        }

        for (int i = startBlock; i < startBlock + numberOfBlocks; i++) {
            if (!bitmap[i]) {
                throw new IllegalArgumentException("Block not allocated: " + i);
            }
            bitmap[i] = false;
        }

        usedCount -= numberOfBlocks;
    }

    /**
     * Checks whether a specific block is currently allocated.
     *
     * @param blockId the block identifier to query
     * @return {@code true} if the block is allocated; {@code false} if it is
     *         free or the ID is out of range
     */
    public boolean isAllocated(int blockId) {
        if (blockId < 0 || blockId >= totalBlocks) {
            return false;
        }
        return bitmap[blockId];
    }

    /**
     * Returns the number of currently free blocks.
     *
     * @return the free block count; in the range {@code [0, totalBlocks]}
     */
    public int getFreeCount() {
        return totalBlocks - usedCount;
    }

    /**
     * Returns the number of currently allocated blocks.
     *
     * @return the used block count; in the range {@code [0, totalBlocks]}
     */
    public int getUsedCount() {
        return usedCount;
    }

    /**
     * Returns the total number of blocks managed by this bitmap.
     *
     * @return the total block count
     */
    public int getTotalCount() {
        return totalBlocks;
    }
}