package model;

import java.util.ArrayList;
import java.util.List;

public class SistemaArchivos {
    private final SuperBloque superBloque;
    private final Inodo raiz;
    private int siguienteId;

    public SistemaArchivos() {
        superBloque = new SuperBloque(100, 1000);
        raiz = new Inodo(0, "", true, null);
        siguienteId = 1;
        inicializarEjemplo();
    }

    private void inicializarEjemplo() {
        crearArchivo("/", "archivo1.txt", "Contenido del archivo 1");
        crearArchivo("/", "archivo2.txt", "Contenido del archivo 2");
        crearDirectorio("/", "subdir");
        crearArchivo("/subdir", "archivo3.txt", "Contenido del archivo 3");
    }

    public String listarDirectorio(String ruta) {
        Inodo dir = resolverInodoPorRuta(ruta);
        if (dir == null || !dir.esDirectorio()) {
            return "Directorio no encontrado: " + ruta;
        }
        StringBuilder texto = new StringBuilder("Contenido de " + normalizarRuta(ruta) + ":\n");
        for (Inodo hijo : dir.getHijos()) {
            texto.append(hijo.esDirectorio() ? "  [D] " : "  [F] ").append(hijo.getNombre()).append("\n");
        }
        return texto.toString();
    }

    public boolean crearDirectorio(String rutaPadre, String nombre) {
        if (nombre == null || nombre.isBlank()) {
            return false;
        }
        Inodo padre = resolverInodoPorRuta(rutaPadre);
        if (padre == null || !padre.esDirectorio()) {
            return false;
        }
        if (padre.buscarHijo(nombre) != null) {
            return false;
        }
        Inodo nuevo = new Inodo(siguienteId++, nombre, true, padre);
        padre.agregarHijo(nuevo);
        superBloque.reservarInodo();
        return true;
    }

    public boolean crearArchivo(String rutaPadre, String nombre, String contenido) {
        if (nombre == null || nombre.isBlank()) {
            return false;
        }
        Inodo padre = resolverInodoPorRuta(rutaPadre);
        if (padre == null || !padre.esDirectorio()) {
            return false;
        }
        if (padre.buscarHijo(nombre) != null) {
            return false;
        }
        Inodo archivo = new Inodo(siguienteId++, nombre, false, padre);
        archivo.setContenido(contenido == null ? "" : contenido);
        padre.agregarHijo(archivo);
        superBloque.reservarInodo();
        superBloque.reservarBloques(1);
        return true;
    }

    public String leerArchivo(String ruta) {
        Inodo archivo = resolverInodoPorRuta(ruta);
        if (archivo == null || archivo.esDirectorio()) {
            return "Archivo no encontrado: " + ruta;
        }
        return archivo.getContenido();
    }

    public boolean eliminarEntrada(String ruta) {
        if (ruta == null || normalizarRuta(ruta).equals("/")) {
            return false;
        }
        String normal = normalizarRuta(ruta);
        String padreRuta = obtenerRutaPadre(normal);
        String nombre = extraerNombre(normal);
        Inodo padre = resolverInodoPorRuta(padreRuta);
        if (padre == null || !padre.esDirectorio()) {
            return false;
        }
        Inodo hijo = padre.buscarHijo(nombre);
        if (hijo == null) {
            return false;
        }
        if (padre.eliminarHijo(nombre)) {
            superBloque.liberarInodo();
            if (!hijo.esDirectorio()) {
                superBloque.liberarBloques(1);
            }
            return true;
        }
        return false;
    }

    public List<String> buscarPorNombre(String consulta) {
        List<String> resultados = new ArrayList<>();
        if (consulta == null || consulta.isBlank()) {
            return resultados;
        }
        buscarRecursivo(raiz, consulta.toLowerCase(), resultados);
        return resultados;
    }

    public String imprimirArbol() {
        StringBuilder resultado = new StringBuilder();
        imprimirRecursivo(raiz, "", resultado);
        return resultado.toString();
    }

    private void buscarRecursivo(Inodo actual, String consulta, List<String> resultados) {
        if (!actual.esDirectorio() && actual.getNombre().toLowerCase().contains(consulta)) {
            resultados.add(actual.obtenerRuta());
        }
        if (actual.esDirectorio()) {
            if (!actual.getNombre().isBlank() && actual.getNombre().toLowerCase().contains(consulta)) {
                resultados.add(actual.obtenerRuta() + "/");
            }
            for (Inodo hijo : actual.getHijos()) {
                buscarRecursivo(hijo, consulta, resultados);
            }
        }
    }

    private void imprimirRecursivo(Inodo actual, String indent, StringBuilder resultado) {
        String nombre = actual.getNombre().isBlank() ? "/" : actual.getNombre();
        resultado.append(indent).append(nombre).append(actual.esDirectorio() ? "/" : "").append("\n");
        if (actual.esDirectorio()) {
            for (Inodo hijo : actual.getHijos()) {
                imprimirRecursivo(hijo, indent + "  ", resultado);
            }
        }
    }

    private Inodo resolverInodoPorRuta(String ruta) {
        String normal = normalizarRuta(ruta);
        if (normal.equals("/")) {
            return raiz;
        }
        String[] segmentos = normal.substring(1).split("/");
        Inodo actual = raiz;
        for (String segmento : segmentos) {
            if (segmento.isBlank()) {
                continue;
            }
            actual = actual.buscarHijo(segmento);
            if (actual == null) {
                return null;
            }
        }
        return actual;
    }

    private String normalizarRuta(String ruta) {
        if (ruta == null || ruta.isBlank()) {
            return "/";
        }
        String normal = ruta.replace('\\', '/').trim();
        if (!normal.startsWith("/")) {
            normal = "/" + normal;
        }
        while (normal.endsWith("/") && normal.length() > 1) {
            normal = normal.substring(0, normal.length() - 1);
        }
        return normal;
    }

    private String obtenerRutaPadre(String ruta) {
        int indice = ruta.lastIndexOf('/');
        if (indice <= 0) {
            return "/";
        }
        return ruta.substring(0, indice);
    }

    private String extraerNombre(String ruta) {
        int indice = ruta.lastIndexOf('/');
        return ruta.substring(indice + 1);
    }
}
