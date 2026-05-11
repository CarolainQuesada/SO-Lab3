# Simulador de Sistema de Archivos

> Laboratorio Programado — Sistemas Operativos  
> Universidad Nacional, Sede Regional Brunca – Campus Coto  
> Carrera: Ingeniería en Sistemas de Información  
> **Autora:** Carolain Quesada Soto

---

## Descripción

Implementación de un sistema de archivos simulado basado en las estructuras internas de sistemas reales como **ext2/ext3**. El programa modela un disco virtual con 100 bloques de 64 caracteres cada uno, gestionando superbloque, bitmap, inodos y bloques de datos mediante una interfaz de consola interactiva.

---

## Requisitos del Sistema

| Componente | Versión mínima |
|------------|----------------|
| Java Development Kit (JDK) | 17 o superior |
| NetBeans IDE *(opcional)* | 17 o superior |
| Apache Ant *(opcional, para consola)* | 1.10 o superior |

Verificar instalación:

```bash
java -version
javac -version
```

---

## Estructura del Proyecto

```
Sistema Archivos/
├── src/
│   └── sistema/
│       └── archivos/
│           ├── Main.java               # Punto de entrada principal
│           ├── SistemaArchivos.java    # Lógica central del sistema de archivos
│           ├── SuperBloque.java        # Estructura del superbloque
│           └── Inodo.java              # Estructura de inodos
├── SO-Lab3/
│   └── src/
│       ├── Main.java                   # Punto de entrada (versión extendida)
│       ├── model/
│       │   ├── SistemaArchivos.java
│       │   ├── SuperBloque.java
│       │   ├── Inodo.java
│       │   ├── Archivo.java
│       │   ├── filesystem/
│       │   │   ├── FileSystem.java
│       │   │   ├── SuperBlock.java
│       │   │   ├── Inode.java
│       │   │   ├── BlockBitmap.java
│       │   │   ├── InodeBitmap.java
│       │   │   ├── FileSystemConfig.java
│       │   │   └── FilePermissions.java
│       │   ├── exceptions/             # Excepciones personalizadas
│       │   └── log/
│       │       └── FileSystemLogger.java
│       ├── service/
│       │   └── FileSystemService.java
│       ├── ui/
│       │   ├── CommandParser.java
│       │   ├── CommandProcessor.java
│       │   └── UIFormatter.java
│       └── util/
│           └── InputUtil.java
├── build/                              # Clases compiladas (.class)
├── dist/                               # JAR generado y Javadoc
├── archivos_guardados.txt              # Persistencia del disco virtual
└── README.md
```

---

## Compilación y Ejecución

### Opción A — NetBeans IDE *(recomendado)*

1. Abrir NetBeans.
2. Ir a **File → Open Project** y seleccionar la carpeta `SO-Lab3/`.
3. Esperar a que NetBeans cargue el proyecto.
4. Presionar **F6** o el botón **Run Project** para compilar y ejecutar.

---

### Opción B — Consola con Apache Ant

**1. Abrir una terminal y ubicarse en la carpeta del proyecto:**

```bash
cd "ruta/hacia/Sistema Archivos/SO-Lab3"
```

**2. Limpiar y compilar:**

```bash
ant clean
ant jar
```

**3. Ejecutar el JAR generado:**

```bash
java -jar dist/SO-Lab3.jar
```

---

### Generar Documentación Javadoc

Desde **NetBeans**: clic derecho sobre el proyecto → **Generate Javadoc**.

Desde **consola**:

```bash
ant javadoc
```

La documentación se genera en `dist/javadoc/`. Para abrirla, acceder a:

```
dist/javadoc/index.html
```

---

## Uso del Programa

Al ejecutar, el programa muestra el siguiente menú interactivo:

```
=== Simulador de Sistema de Archivos ===

1. Inicializar sistema de archivos.
2. Crear archivo.
3. Leer archivo.
4. Eliminar archivo.
5. Mostrar estado del sistema.
6. Mostrar bitmap.
7. Salir.
8. Simular fragmentacion.

Seleccione una opcion:
```

El sistema solo acepta los números listados. Si se ingresa una letra, texto vacío o un número fuera del rango, se mostrará un mensaje de error y se volverá a solicitar la opción.

---

## Flujo Recomendado de Uso

1. **Opción 1** — Inicializar el sistema de archivos.
2. **Opción 2** — Crear archivos.
3. **Opción 3** — Leer archivos.
4. **Opción 4** — Eliminar archivos.
5. **Opción 5** — Consultar el estado general del sistema.
6. **Opción 6** — Consultar el bitmap.
7. **Opción 8** — Simular fragmentación.
8. **Opción 7** — Salir del programa.

---

## Descripción de Opciones

### Opción 1 — Inicializar Sistema de Archivos

Inicializa el disco virtual con 100 bloques de 64 caracteres. **Debe ejecutarse antes de cualquier otra operación.**

```
Seleccione una opcion: 1
Sistema de archivos inicializado con 100 bloques de 64 caracteres.
```

Si el sistema ya fue inicializado, se solicita confirmación antes de reiniciarlo:

```
El sistema ya esta inicializado. Desea reiniciarlo? (S/N):
```

- Respuesta `S` → el sistema se reinicia.
- Respuesta `N` → la operación se cancela.

---

### Opción 2 — Crear Archivo

Crea un archivo dentro del sistema. El contenido se divide automáticamente en bloques de 64 caracteres.

**Pasos:**
1. Ingresar el nombre del archivo.
2. Escribir el contenido.
3. Finalizar escribiendo `FIN` en una línea separada (acepta `fin`, `Fin`, `FIN`, etc.).

```
Seleccione una opcion: 2
Nombre del archivo: Lab3.txt
Contenido del archivo. Escriba FIN en una linea separada para terminar:
Hola familia
FIN
Archivo creado correctamente. Bloques asignados: [0]
```

**Nombres de archivo válidos** — se permiten letras, números, punto, guion y guion bajo:

| Válido | Inválido |
|--------|----------|
| `Lab3.txt` | `mi archivo.txt` |
| `archivo_1` | `/carpeta/archivo.txt` |
| `tarea-final` | `archivo?.txt` |
| `Notas2026.txt` | `..` |

---

### Opción 3 — Leer Archivo

Reconstruye y muestra el contenido de un archivo existente a partir de sus bloques asignados.

```
Seleccione una opcion: 3
Nombre del archivo a leer: Lab3.txt
Contenido:
Hola familia
```

Si el archivo no existe, el sistema muestra un mensaje de error.

---

### Opción 4 — Eliminar Archivo

Elimina un archivo, libera sus bloques y actualiza el bitmap. Solicita confirmación antes de proceder.

```
Seleccione una opcion: 4
Nombre del archivo a eliminar: Lab3.txt
Esta seguro que desea eliminar Lab3.txt? (S/N): S
Archivo eliminado correctamente.
```

- Respuesta `N` → la eliminación se cancela:

```
Eliminacion cancelada.
```

---

### Opción 5 — Mostrar Estado del Sistema

Muestra información completa del disco virtual: bloques libres y ocupados, bitmap, archivos almacenados con sus bloques asignados y estado de fragmentación.

```
Seleccione una opcion: 5
=== Estado del Sistema de Archivos ===
Total de bloques  : 100
Tamano de bloque  : 64 caracteres
Bloques libres    : 99
Bloques ocupados  : 1
Bitmap            : 1000000000 0000000000 ...
Archivos almacenados:
  - Lab3.txt (12 caracteres) -> bloques [0]
Fragmentacion: no se detectan archivos fragmentados
```

---

### Opción 6 — Mostrar Bitmap

Muestra el estado actual de cada bloque del disco.

- `1` → bloque ocupado
- `0` → bloque libre

```
Seleccione una opcion: 6
Bitmap: 1000000000 0000000000 0000000000 ...
```

---

### Opción 7 — Salir

Finaliza la ejecución del programa.

```
Seleccione una opcion: 7
Saliendo del sistema...
```

---

### Opción 8 — Simular Fragmentación

Ejecuta una demostración automática de fragmentación: crea archivos A, B y C, elimina B y luego crea D. El archivo D reutiliza los bloques liberados, pudiendo quedar en posiciones no contiguas del disco.

```
Seleccione una opcion: 8
Simulacion realizada: se crearon A, B y C; se elimino B; luego se creo D.
El archivo D reutiliza huecos libres y puede quedar en bloques no contiguos.
Bitmap: 1001100000 ...
  - A -> bloques [0]
  - C -> bloques [2, 3]
  - D -> bloques [1, 4]   <- fragmentado
```

---

## Validaciones del Sistema

| # | Validación |
|---|------------|
| 1 | Solo acepta opciones numéricas válidas del menú. |
| 2 | No permite operar antes de inicializar el sistema. |
| 3 | No permite nombres de archivo vacíos. |
| 4 | No permite nombres con espacios, barras ni caracteres inválidos. |
| 5 | No permite archivos duplicados. |
| 6 | Verifica espacio suficiente antes de crear un archivo. |
| 7 | Muestra error al intentar leer un archivo inexistente. |
| 8 | Muestra error al intentar eliminar un archivo inexistente. |
| 9 | Solicita confirmación antes de eliminar un archivo. |
| 10 | Solicita confirmación antes de reinicializar el sistema. |
| 11 | Acepta el marcador `FIN` sin distinguir mayúsculas o minúsculas. |

---

## Parámetros del Disco Virtual

| Parámetro | Valor |
|-----------|-------|
| Total de bloques | 100 |
| Tamaño de bloque | 64 caracteres |
| Capacidad total | 6 400 caracteres |
| Inodos máximos | 100 |

---

## Persistencia

El sistema guarda el estado de los archivos en `archivos_guardados.txt`, ubicado en la raíz del proyecto. Este archivo se actualiza automáticamente con cada operación de creación o eliminación.

---

## Autora

**Carolain Quesada Soto**  
Ingeniería en Sistemas de Información  
Universidad Nacional — Sede Regional Brunca, Campus Coto  
Curso: Sistemas Operativos
