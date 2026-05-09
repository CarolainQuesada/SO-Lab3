package model;

public class Archivo {
    private final String nombre;
    private final String contenido;

    public Archivo(String nombre, String contenido) {
        this.nombre = nombre;
        this.contenido = contenido;
    }

    public String getNombre() {
        return nombre;
    }

    public String getContenido() {
        return contenido;
    }

    public int getTamanio() {
        return contenido == null ? 0 : contenido.length();
    }
}
