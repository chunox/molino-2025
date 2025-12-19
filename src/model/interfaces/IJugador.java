package model.interfaces;

import java.io.Serializable;

/**
 * Interfaz para representar un jugador
 */
public interface IJugador extends Serializable {
    String getNombre();
    char getSimbolo();
    int getPiezasColocadas();
    int getPiezasEnTablero();
    void incrementarPiezasColocadas();
    void decrementarPiezasEnTablero();
}
