package model;

import java.util.Arrays;

/**
 * Represents an inode entry in the simple simulated file system.
 *
 * <p>An inode (index node) stores the metadata associated with a single file:
 * its name, its size in characters, and the indexes of the data blocks that
 * hold its content.  Block indexes are stored as a defensive copy so that
 * external code cannot modify the internal state of this object.</p>
 *
 * <p>This class is immutable after construction.</p>
 *
 * @author Carolain Quesada
 * @version 1.0
 * @see SistemaArchivos
 */
public class Inodo {

    /** The name of the file associated with this inode. */
    private final String nombreArchivo;

    /** The size of the file content in characters. */
    private final int tamanioArchivo;

    /**
     * The indexes of the data blocks that store the file content.
     * A defensive copy is kept internally.
     */
    private final int[] bloquesAsignados;

    /**
     * Constructs an {@code Inodo} with the given file metadata.
     *
     * <p>A defensive copy of {@code bloquesAsignados} is created so that
     * later changes to the caller's array do not affect this object.</p>
     *
     * @param nombreArchivo    the name of the file associated with this inode;
     *                         must not be {@code null}
     * @param tamanioArchivo   the file size in characters; must be {@code >= 0}
     * @param bloquesAsignados the data block indexes assigned to the file;
     *                         must not be {@code null}
     */
    public Inodo(String nombreArchivo, int tamanioArchivo, int[] bloquesAsignados) {
        this.nombreArchivo = nombreArchivo;
        this.tamanioArchivo = tamanioArchivo;
        this.bloquesAsignados = Arrays.copyOf(bloquesAsignados, bloquesAsignados.length);
    }

    /**
     * Returns the name of the file associated with this inode.
     *
     * @return the file name; never {@code null}
     */
    public String getNombreArchivo() {
        return nombreArchivo;
    }

    /**
     * Returns the size of the stored file content.
     *
     * @return the file size in characters; always {@code >= 0}
     */
    public int getTamanioArchivo() {
        return tamanioArchivo;
    }

    /**
     * Returns a defensive copy of the data block indexes assigned to this file.
     *
     * <p>Callers may freely modify the returned array without affecting the
     * internal state of this inode.</p>
     *
     * @return a new array containing the assigned block indexes; never {@code null}
     */
    public int[] getBloquesAsignados() {
        return Arrays.copyOf(bloquesAsignados, bloquesAsignados.length);
    }
}