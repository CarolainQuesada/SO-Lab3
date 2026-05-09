package model;

import java.util.ArrayList;
import java.util.List;

public class Inodo {
    private final int id;
    private final String nombre;
    private final boolean esDirectorio;
    private String contenido;
    private final List<Inodo> hijos;
    private Inodo padre;

    public Inodo(int id, String nombre, boolean esDirectorio, Inodo padre) {
        this.id = id;
        this.nombre = nombre;
        this.esDirectorio = esDirectorio;
        this.padre = padre;
        this.contenido = esDirectorio ? null : "";
        this.hijos = esDirectorio ? new ArrayList<>() : null;
    }

    public int getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public boolean esDirectorio() {
        return esDirectorio;
    }

    public String getContenido() {
        return contenido;
    }

    public void setContenido(String contenido) {
        if (!esDirectorio) {
            this.contenido = contenido;
        }
    }

    public List<Inodo> getHijos() {
        return hijos;
    }

    public Inodo getPadre() {
        return padre;
    }

    public void setPadre(Inodo padre) {
        this.padre = padre;
    }

    public void agregarHijo(Inodo hijo) {
        if (esDirectorio && hijo != null) {
            hijos.add(hijo);
            hijo.setPadre(this);
        }
    }

    public boolean eliminarHijo(String nombre) {
        if (!esDirectorio) {
            return false;
        }
        return hijos.removeIf(h -> h.getNombre().equals(nombre));
    }

    public Inodo buscarHijo(String nombre) {
        if (!esDirectorio) {
            return null;
        }
        for (Inodo hijo : hijos) {
            if (hijo.getNombre().equals(nombre)) {
                return hijo;
            }
        }
        return null;
    }

    public String obtenerRuta() {
        if (padre == null) {
            return "/";
        }
        if (padre.getPadre() == null) {
            return "/" + nombre;
        }
        return padre.obtenerRuta() + "/" + nombre;
    }
}
