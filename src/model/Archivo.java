package model;

/**
 * Immutable value object that pairs a file name with its text content.
 *
 * <p>This class is used as a lightweight data transfer object inside the
 * simulated file system.  Once constructed, neither the name nor the content
 * can be changed.</p>
 *
 * @author Carolain Quesada
 * @version 1.0
 */
public class Archivo {

    /** The name that identifies this file within the file system. */
    private final String nombre;

    /** The full text content stored in this file. */
    private final String contenido;

    /**
     * Constructs an {@code Archivo} with the specified name and content.
     *
     * @param nombre    the file name; must not be {@code null}
     * @param contenido the file content; may be {@code null}, which is
     *                  treated as an empty string by {@link #getTamanio()}
     */
    public Archivo(String nombre, String contenido) {
        this.nombre = nombre;
        this.contenido = contenido;
    }

    /**
     * Returns the name of this file.
     *
     * @return the file name; never {@code null} when the object was constructed
     *         with a non-null argument
     */
    public String getNombre() {
        return nombre;
    }

    /**
     * Returns the text content of this file.
     *
     * @return the file content, or {@code null} if {@code null} was passed to
     *         the constructor
     */
    public String getContenido() {
        return contenido;
    }

    /**
     * Returns the number of characters in the file content.
     *
     * <p>If the content is {@code null} this method returns {@code 0} instead
     * of throwing a {@link NullPointerException}.</p>
     *
     * @return the content length, or {@code 0} when content is {@code null}
     */
    public int getTamanio() {
        return contenido == null ? 0 : contenido.length();
    }
}