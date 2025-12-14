package model.clases;

import model.interfaces.IJugador;
import java.io.Serializable;

/**
 * Clase que representa una posición en el tablero
 */
public class Posicion implements Serializable {
    private static final long serialVersionUID = 1L;

    private String id;
    private IJugador ocupante;

    public Posicion(String id) {
        this.id = id;
        this.ocupante = null;
    }

    public String getId() {
        return id;
    }

    public IJugador getOcupante() {
        return ocupante;
    }

    public void ocupar(IJugador jugador) {
        this.ocupante = jugador;
    }

    public void liberar() {
        this.ocupante = null;
    }

    public boolean estaLibre() {
        return ocupante == null;
    }

    public boolean ocupadaPor(IJugador jugador) {
        return ocupante != null && ocupante.equals(jugador);
    }
}
