/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
package sistema.archivos;

import java.util.*;

/**
 * Simulación simple de un sistema de archivos en memoria.
 */
public class SistemaArchivos {

    static class File {
        String name;
        String content;

        File(String name, String content) {
            this.name = name;
            this.content = content;
        }

        @Override
        public String toString() {
            return "File: " + name;
        }
    }

    static class Directory {
        String name;
        List<File> files = new ArrayList<>();
        List<Directory> subdirs = new ArrayList<>();

        Directory(String name) {
            this.name = name;
        }

        void addFile(File file) {
            files.add(file);
        }

        void addSubdir(Directory dir) {
            subdirs.add(dir);
        }

        boolean removeFile(String name) {
            return files.removeIf(f -> f.name.equals(name));
        }

        boolean removeSubdir(String name) {
            return subdirs.removeIf(d -> d.name.equals(name));
        }

        File findFile(String name) {
            for (File f : files) {
                if (f.name.equals(name)) {
                    return f;
                }
            }
            return null;
        }

        Directory findSubdir(String name) {
            for (Directory d : subdirs) {
                if (d.name.equals(name)) {
                    return d;
                }
            }
            return null;
        }

        void listContents() {
            System.out.println("\nContents of /" + getPath() + ":");
            for (Directory d : subdirs) {
                System.out.println("  [D] " + d.name);
            }
            for (File f : files) {
                System.out.println("  [F] " + f.name);
            }
        }

        String getPath() {
            return name.equals("root") ? "" : name;
        }

        @Override
        public String toString() {
            return "Directory: " + name;
        }
    }

    public static void main(String[] args) {
        Directory root = buildSampleFileSystem();
        Scanner scanner = new Scanner(System.in);

        System.out.println("=== Sistema de Archivos en Memoria ===");
        boolean running = true;
        while (running) {
            printMenu();
            System.out.print("Seleccione una opción: ");
            String option = scanner.nextLine().trim();
            switch (option) {
                case "1" -> listDirectory(root, scanner);
                case "2" -> createDirectory(root, scanner);
                case "3" -> createFile(root, scanner);
                case "4" -> readFile(root, scanner);
                case "5" -> deleteEntry(root, scanner);
                case "6" -> searchByName(root, scanner);
                case "7" -> printTree(root, "");
                case "0" -> {
                    running = false;
                    System.out.println("Saliendo...");
                }
                default -> System.out.println("Opción no válida. Intente de nuevo.");
            }
        }

        scanner.close();
    }

    private static Directory buildSampleFileSystem() {
        Directory root = new Directory("root");
        root.addFile(new File("archivo1.txt", "Contenido del archivo 1"));
        root.addFile(new File("archivo2.txt", "Contenido del archivo 2"));

        Directory subdir = new Directory("subdir");
        subdir.addFile(new File("archivo3.txt", "Contenido del archivo 3"));
        root.addSubdir(subdir);
        return root;
    }

    private static void printMenu() {
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

    private static void listDirectory(Directory root, Scanner scanner) {
        System.out.print("Ruta del directorio (por ejemplo / o /subdir): ");
        String path = scanner.nextLine().trim();
        Directory dir = findDirectoryByPath(root, path);
        if (dir == null) {
            System.out.println("Directorio no encontrado: " + path);
            return;
        }
        dir.listContents();
    }

    private static void createDirectory(Directory root, Scanner scanner) {
        System.out.print("Directorio padre (/ o /ruta): ");
        String parentPath = scanner.nextLine().trim();
        Directory parent = findDirectoryByPath(root, parentPath);
        if (parent == null) {
            System.out.println("Directorio padre no encontrado.");
            return;
        }
        System.out.print("Nombre del nuevo directorio: ");
        String name = scanner.nextLine().trim();
        if (name.isEmpty()) {
            System.out.println("El nombre no puede estar vacío.");
            return;
        }
        if (parent.findSubdir(name) != null) {
            System.out.println("Ya existe un subdirectorio con ese nombre.");
            return;
        }
        parent.addSubdir(new Directory(name));
        System.out.println("Directorio creado: " + normalizePath(parentPath) + "/" + name);
    }

    private static void createFile(Directory root, Scanner scanner) {
        System.out.print("Directorio donde crear el archivo (/ o /ruta): ");
        String parentPath = scanner.nextLine().trim();
        Directory parent = findDirectoryByPath(root, parentPath);
        if (parent == null) {
            System.out.println("Directorio no encontrado.");
            return;
        }
        System.out.print("Nombre del archivo: ");
        String name = scanner.nextLine().trim();
        if (name.isEmpty()) {
            System.out.println("El nombre no puede estar vacío.");
            return;
        }
        if (parent.findFile(name) != null) {
            System.out.println("Ya existe un archivo con ese nombre en el directorio.");
            return;
        }
        System.out.print("Contenido del archivo: ");
        String content = scanner.nextLine();
        parent.addFile(new File(name, content));
        System.out.println("Archivo creado: " + normalizePath(parentPath) + "/" + name);
    }

    private static void readFile(Directory root, Scanner scanner) {
        System.out.print("Ruta del archivo (/nombre.txt o /dir/nombre.txt): ");
        String filePath = scanner.nextLine().trim();
        File file = findFileByPath(root, filePath);
        if (file == null) {
            System.out.println("Archivo no encontrado: " + filePath);
            return;
        }
        System.out.println("\nContenido de " + filePath + ":");
        System.out.println(file.content);
    }

    private static void deleteEntry(Directory root, Scanner scanner) {
        System.out.print("Ruta del archivo o directorio a eliminar: ");
        String path = scanner.nextLine().trim();
        if (path.equals("/") || path.isEmpty()) {
            System.out.println("No se puede eliminar el directorio raíz.");
            return;
        }
        String normalized = normalizePath(path);
        String parentPath = getParentPath(normalized);
        String name = getLastPathSegment(normalized);
        Directory parent = findDirectoryByPath(root, parentPath);
        if (parent == null) {
            System.out.println("Ruta inválida.");
            return;
        }
        if (parent.removeFile(name)) {
            System.out.println("Archivo eliminado: " + normalized);
            return;
        }
        Directory dir = parent.findSubdir(name);
        if (dir != null) {
            System.out.println("Directorio eliminado: " + normalized);
            parent.removeSubdir(name);
            return;
        }
        System.out.println("No se encontró archivo ni directorio: " + normalized);
    }

    private static void searchByName(Directory root, Scanner scanner) {
        System.out.print("Nombre o texto de búsqueda: ");
        String query = scanner.nextLine().trim().toLowerCase();
        if (query.isEmpty()) {
            System.out.println("La búsqueda no puede estar vacía.");
            return;
        }
        List<String> results = new ArrayList<>();
        searchRecursive(root, "/", query, results);
        if (results.isEmpty()) {
            System.out.println("No se encontraron coincidencias para: " + query);
            return;
        }
        System.out.println("Resultados de búsqueda:");
        for (String result : results) {
            System.out.println("  " + result);
        }
    }

    private static void searchRecursive(Directory dir, String currentPath, String query, List<String> results) {
        for (File file : dir.files) {
            if (file.name.toLowerCase().contains(query) || file.content.toLowerCase().contains(query)) {
                results.add(currentPath + file.name);
            }
        }
        for (Directory subdir : dir.subdirs) {
            String nextPath = currentPath + subdir.name + "/";
            if (subdir.name.toLowerCase().contains(query)) {
                results.add(nextPath);
            }
            searchRecursive(subdir, nextPath, query, results);
        }
    }

    private static void printTree(Directory dir, String indent) {
        String label = indent.isEmpty() ? "/" : indent + dir.name + "/";
        System.out.println(label);
        for (File file : dir.files) {
            System.out.println(indent + "  " + file.name);
        }
        for (Directory subdir : dir.subdirs) {
            printTree(subdir, indent + "  ");
        }
    }

    private static Directory findDirectoryByPath(Directory root, String path) {
        String normalized = normalizePath(path);
        if (normalized.isEmpty() || normalized.equals("/")) {
            return root;
        }
        String[] parts = normalized.substring(1).split("/");
        Directory current = root;
        for (String part : parts) {
            if (part.isEmpty()) {
                continue;
            }
            current = current.findSubdir(part);
            if (current == null) {
                return null;
            }
        }
        return current;
    }

    private static File findFileByPath(Directory root, String path) {
        String normalized = normalizePath(path);
        int lastSlash = normalized.lastIndexOf('/');
        if (lastSlash < 0) {
            return root.findFile(normalized);
        }
        String parentPath = normalized.substring(0, lastSlash + 1);
        String fileName = normalized.substring(lastSlash + 1);
        Directory parent = findDirectoryByPath(root, parentPath);
        if (parent == null) {
            return null;
        }
        return parent.findFile(fileName);
    }

    private static String normalizePath(String path) {
        if (path == null || path.isEmpty()) {
            return "/";
        }
        String normalized = path.replace('\\', '/').trim();
        if (!normalized.startsWith("/")) {
            normalized = "/" + normalized;
        }
        while (normalized.endsWith("/") && normalized.length() > 1) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }
        return normalized;
    }

    private static String getParentPath(String path) {
        if (path == null || path.isEmpty() || path.equals("/")) {
            return "/";
        }
        int lastSlash = path.lastIndexOf('/');
        if (lastSlash <= 0) {
            return "/";
        }
        return path.substring(0, lastSlash);
    }

    private static String getLastPathSegment(String path) {
        if (path == null || path.isEmpty() || path.equals("/")) {
            return "";
        }
        int lastSlash = path.lastIndexOf('/');
        return path.substring(lastSlash + 1);
    }
}
