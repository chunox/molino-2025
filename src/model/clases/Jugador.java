package model.clases;

import model.enums.EstadoJugador;
import model.interfaces.IJugador;
import java.io.Serializable;

/**
 * Clase que representa un jugador del juego
 */
public class Jugador implements IJugador, Serializable {
    private static final long serialVersionUID = 1L;

    private String nombre;
    private String password;
    private char simbolo; // 'X' o 'O'
    private int piezasColocadas;
    private int piezasEnTablero;
    private EstadoJugador estadoConexion;

    public Jugador(String nombre, String password, char simbolo) {
        this.nombre = nombre;
        this.password = password;
        this.simbolo = simbolo;
        this.piezasColocadas = 0;
        this.piezasEnTablero = 0;
        this.estadoConexion = EstadoJugador.CONECTADO;
    }

    @Override
    public String getNombre() {
        return nombre;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public boolean validarPassword(String password) {
        return this.password.equals(password);
    }

    @Override
    public char getSimbolo() {
        return simbolo;
    }

    @Override
    public int getPiezasColocadas() {
        return piezasColocadas;
    }

    @Override
    public int getPiezasEnTablero() {
        return piezasEnTablero;
    }

    @Override
    public void incrementarPiezasColocadas() {
        piezasColocadas++;
        piezasEnTablero++;
    }

    @Override
    public void incrementarPiezasEnTablero() {
        piezasEnTablero++;
    }

    @Override
    public void decrementarPiezasEnTablero() {
        piezasEnTablero--;
    }

    @Override
    public EstadoJugador getEstadoConexion() {
        return estadoConexion;
    }

    @Override
    public void setEstadoConexion(EstadoJugador estado) {
        this.estadoConexion = estado;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Jugador jugador = (Jugador) o;
        return nombre.equals(jugador.nombre);
    }

    @Override
    public int hashCode() {
        return nombre.hashCode();
    }

    @Override
    public String toString() {
        return nombre + " (" + simbolo + ")";
    }
}
