package service;

import model.SistemaArchivos;
import util.InputUtil;

import java.util.List;

public class FileSystemService {
    private final SistemaArchivos sistema = new SistemaArchivos();

    public void run() {
        boolean ejecutando = true;
        System.out.println("=== Sistema de Archivos en Memoria ===");
        while (ejecutando) {
            mostrarMenu();
            String opcion = InputUtil.readLine("Seleccione una opción: ");
            switch (opcion) {
                case "1" -> listarDirectorio();
                case "2" -> crearDirectorio();
                case "3" -> crearArchivo();
                case "4" -> leerArchivo();
                case "5" -> eliminarEntrada();
                case "6" -> buscarPorNombre();
                case "7" -> mostrarArbol();
                case "0" -> {
                    ejecutando = false;
                    System.out.println("Saliendo...");
                }
                default -> System.out.println("Opción no válida. Intente de nuevo.");
            }
        }
    }

    private void mostrarMenu() {
        System.out.println("\nMenú:");
        System.out.println(" 1. Listar directorio");
        System.out.println(" 2. Crear directorio");
        System.out.println(" 3. Crear archivo");
        System.out.println(" 4. Leer archivo");
        System.out.println(" 5. Eliminar archivo/directorio");
        System.out.println(" 6. Buscar por nombre");
        System.out.println(" 7. Mostrar árbol completo");
        System.out.println(" 0. Salir");
    }

    private void listarDirectorio() {
        String ruta = InputUtil.readLine("Ruta del directorio (/ o /subdir): ");
        System.out.println(sistema.listarDirectorio(ruta));
    }

    private void crearDirectorio() {
        String padre = InputUtil.readLine("Directorio padre (/ o /ruta): ");
        String nombre = InputUtil.readLine("Nombre del nuevo directorio: ");
        if (sistema.crearDirectorio(padre, nombre)) {
            System.out.println("Directorio creado: " + padre + "/" + nombre);
        } else {
            System.out.println("No se pudo crear el directorio. Verifique la ruta o el nombre.");
        }
    }

    private void crearArchivo() {
        String padre = InputUtil.readLine("Directorio donde crear el archivo (/ o /ruta): ");
        String nombre = InputUtil.readLine("Nombre del archivo: ");
        String contenido = InputUtil.readLine("Contenido del archivo: ");
        if (sistema.crearArchivo(padre, nombre, contenido)) {
            System.out.println("Archivo creado: " + padre + "/" + nombre);
        } else {
            System.out.println("No se pudo crear el archivo. Verifique la ruta o el nombre.");
        }
    }

    private void leerArchivo() {
        String ruta = InputUtil.readLine("Ruta del archivo (/nombre.txt o /dir/nombre.txt): ");
        System.out.println("\n" + sistema.leerArchivo(ruta));
    }

    private void eliminarEntrada() {
        String ruta = InputUtil.readLine("Ruta del archivo o directorio a eliminar: ");
        if (sistema.eliminarEntrada(ruta)) {
            System.out.println("Entrada eliminada: " + ruta);
        } else {
            System.out.println("No se pudo eliminar. Verifique la ruta.");
        }
    }

    private void buscarPorNombre() {
        String consulta = InputUtil.readLine("Nombre o texto de búsqueda: ");
        List<String> resultados = sistema.buscarPorNombre(consulta);
        if (resultados.isEmpty()) {
            System.out.println("No se encontraron coincidencias para: " + consulta);
            return;
        }
        System.out.println("Resultados de búsqueda:");
        for (String resultado : resultados) {
            System.out.println("  " + resultado);
        }
    }

    private void mostrarArbol() {
        System.out.println(sistema.imprimirArbol());
    }
}
