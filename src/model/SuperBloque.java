package model;

public class SuperBloque {
    private final int totalInodos;
    private final int totalBloques;
    private int usadosInodos;
    private int usadosBloques;

    public SuperBloque(int totalInodos, int totalBloques) {
        this.totalInodos = totalInodos;
        this.totalBloques = totalBloques;
        this.usadosInodos = 1; // raíz ocupada
        this.usadosBloques = 0;
    }

    public int getTotalInodos() {
        return totalInodos;
    }

    public int getTotalBloques() {
        return totalBloques;
    }

    public int getUsadosInodos() {
        return usadosInodos;
    }

    public int getUsadosBloques() {
        return usadosBloques;
    }

    public void reservarInodo() {
        if (usadosInodos < totalInodos) {
            usadosInodos++;
        }
    }

    public void liberarInodo() {
        if (usadosInodos > 0) {
            usadosInodos--;
        }
    }

    public void reservarBloques(int cantidad) {
        if (usadosBloques + cantidad <= totalBloques) {
            usadosBloques += cantidad;
        }
    }

    public void liberarBloques(int cantidad) {
        if (usadosBloques - cantidad >= 0) {
            usadosBloques -= cantidad;
        }
    }
}
