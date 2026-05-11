package model;

/**
 * Stores global allocation metadata for the simple file system simulator.
 *
 * <p>The super block is the central accounting record of the file system.  It
 * keeps track of the total number of data blocks, how many are currently free,
 * and the configured block size.  Methods {@link #reservarBloque()} and
 * {@link #liberarBloque()} update the free-block counter while guarding
 * against underflow and overflow.</p>
 *
 * <p>The total block count and block size are fixed at construction time and
 * cannot be changed afterwards.</p>
 *
 * @author Carolain Quesada
 * @version 1.0
 * @see SistemaArchivos
 */
public class SuperBloque {

    /**
     * The total number of data blocks that exist in the file system.
     * This value is set at construction and never changes.
     */
    private final int totalBloques;

    /**
     * The number of data blocks that are currently available (not allocated).
     * Starts equal to {@link #totalBloques} and changes as files are created
     * or deleted.
     */
    private int bloquesLibres;

    /**
     * The maximum number of characters that fit in a single data block.
     * This value is set at construction and never changes.
     */
    private final int tamanioBloque;

    /**
     * Constructs a {@code SuperBloque} with all blocks initially free.
     *
     * @param totalBloques  the total number of data blocks available in the
     *                      file system; must be {@code > 0}
     * @param tamanioBloque the capacity of each block in characters;
     *                      must be {@code > 0}
     */
    public SuperBloque(int totalBloques, int tamanioBloque) {
        this.totalBloques = totalBloques;
        this.bloquesLibres = totalBloques;
        this.tamanioBloque = tamanioBloque;
    }

    /**
     * Returns the total number of data blocks that exist in the file system.
     *
     * @return the total block count; always {@code > 0}
     */
    public int getTotalBloques() {
        return totalBloques;
    }

    /**
     * Returns the number of data blocks that are currently free.
     *
     * @return the free block count; in the range {@code [0, totalBloques]}
     */
    public int getBloquesLibres() {
        return bloquesLibres;
    }

    /**
     * Returns the number of data blocks that are currently occupied.
     *
     * <p>This is a convenience method equivalent to
     * {@code getTotalBloques() - getBloquesLibres()}.</p>
     *
     * @return the used block count; in the range {@code [0, totalBloques]}
     */
    public int getBloquesOcupados() {
        return totalBloques - bloquesLibres;
    }

    /**
     * Returns the capacity of each data block.
     *
     * @return the block size in characters; always {@code > 0}
     */
    public int getTamanioBloque() {
        return tamanioBloque;
    }

    /**
     * Marks one free block as reserved (allocated).
     *
     * <p>Decrements the free-block counter by one.  If there are no free
     * blocks this method is a no-op; the counter is never allowed to go
     * below {@code 0}.</p>
     */
    public void reservarBloque() {
        if (bloquesLibres > 0) {
            bloquesLibres--;
        }
    }

    /**
     * Marks one occupied block as free (released).
     *
     * <p>Increments the free-block counter by one.  If all blocks are already
     * free this method is a no-op; the counter is never allowed to exceed
     * {@link #totalBloques}.</p>
     */
    public void liberarBloque() {
        if (bloquesLibres < totalBloques) {
            bloquesLibres++;
        }
    }
}