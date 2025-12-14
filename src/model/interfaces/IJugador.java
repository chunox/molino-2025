package model.interfaces;

import model.enums.EstadoJugador;
import java.io.Serializable;

/**
 * Interfaz para representar un jugador
 */
public interface IJugador extends Serializable {
    String getNombre();
    String getPassword();
    boolean validarPassword(String password);
    char getSimbolo();
    int getPiezasColocadas();
    int getPiezasEnTablero();
    void incrementarPiezasColocadas();
    void incrementarPiezasEnTablero();
    void decrementarPiezasEnTablero();
    EstadoJugador getEstadoConexion();
    void setEstadoConexion(EstadoJugador estado);
}
