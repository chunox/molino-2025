package modelo;

public class Jugador {

    private String nombre;
    private char simbolo; // 'X' o 'O'
    private int piezasColocadas;
    private int piezasEnTablero;

    public Jugador(String nombre, char simbolo) {
        this.nombre = nombre;
        this.simbolo = simbolo;
        this.piezasColocadas = 0;
        this.piezasEnTablero = 0;
    }

    public String getNombre() {
        return nombre;
    }

    public char getSimbolo() {
        return simbolo;
    }

    public void piezaColocada() {
        piezasColocadas++;
        piezasEnTablero++;
    }

    public void piezaEliminada() {
        piezasEnTablero--;
    }

    public int getPiezasColocadas() {
        return piezasColocadas;
    }

    public int getPiezasEnTablero() {
        return piezasEnTablero;
    }
}
