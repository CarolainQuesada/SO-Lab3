package model.filesystem;

/**
 * Tracks allocated and free inode slots in the file system using a boolean
 * bitmap.
 *
 * <p>Each position {@code i} in the internal array corresponds to inode ID
 * {@code i}: {@code true} means the slot is occupied and {@code false} means
 * it is free.  Inode 0 is permanently reserved for the root directory and is
 * marked as used at construction time.</p>
 *
 * <p>This class is <em>not</em> thread-safe.</p>
 *
 * @author Carolain Quesada
 * @version 1.0
 * @see SuperBlock
 */
public class InodeBitmap {

    /**
     * The bitmap array.  {@code bitmap[i] == true} means inode {@code i} is
     * allocated.
     */
    private final boolean[] bitmap;

    /** The total number of inode slots managed by this bitmap. */
    private final int totalInodes;

    /** The number of currently allocated inode slots (includes inode 0). */
    private int usedCount;

    /**
     * Constructs an {@code InodeBitmap} for the specified number of inodes.
     * Inode 0 is pre-allocated to represent the root directory.
     *
     * @param totalInodes the total number of inode slots to manage; must be
     *                    {@code >= 1}
     */
    public InodeBitmap(int totalInodes) {
        this.totalInodes = totalInodes;
        this.bitmap = new boolean[totalInodes];
        this.usedCount = 0;
        // Reserve inode 0 for the root directory
        this.bitmap[0] = true;
        this.usedCount = 1;
    }

    /**
     * Allocates the next available inode slot and returns its ID.
     *
     * <p>The bitmap is scanned from index 0 onwards; the first free slot is
     * marked as used and its index is returned.</p>
     *
     * @return the allocated inode ID, or {@code -1} if no free inodes remain
     */
    public int allocate() {
        for (int i = 0; i < totalInodes; i++) {
            if (!bitmap[i]) {
                bitmap[i] = true;
                usedCount++;
                return i;
            }
        }
        return -1;
    }

    /**
     * Releases a previously allocated inode slot.
     *
     * @param inodeId the inode ID to free; must be in the range
     *                {@code [0, totalInodes)}
     * @throws IllegalArgumentException if {@code inodeId} is out of range or
     *         if the slot was not previously allocated
     */
    public void deallocate(int inodeId) {
        if (inodeId < 0 || inodeId >= totalInodes) {
            throw new IllegalArgumentException("Invalid inode ID: " + inodeId);
        }
        if (!bitmap[inodeId]) {
            throw new IllegalArgumentException("Inode not allocated: " + inodeId);
        }
        bitmap[inodeId] = false;
        usedCount--;
    }

    /**
     * Checks whether a specific inode slot is currently allocated.
     *
     * @param inodeId the inode ID to query
     * @return {@code true} if the slot is allocated; {@code false} if it is
     *         free or the ID is out of range
     */
    public boolean isAllocated(int inodeId) {
        if (inodeId < 0 || inodeId >= totalInodes) {
            return false;
        }
        return bitmap[inodeId];
    }

    /**
     * Returns the number of currently free inode slots.
     *
     * @return the free inode count
     */
    public int getFreeCount() {
        return totalInodes - usedCount;
    }

    /**
     * Returns the number of currently allocated inode slots.
     *
     * @return the used inode count (includes the pre-allocated root inode)
     */
    public int getUsedCount() {
        return usedCount;
    }

    /**
     * Returns the total number of inode slots managed by this bitmap.
     *
     * @return the total inode count
     */
    public int getTotalCount() {
        return totalInodes;
    }
}