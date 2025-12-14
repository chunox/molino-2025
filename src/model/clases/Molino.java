package model.clases;

import model.interfaces.IJugador;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * Clase que representa una combinación de tres posiciones que forman un molino
 */
public class Molino implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String pos1;
    private final String pos2;
    private final String pos3;

    public Molino(String pos1, String pos2, String pos3) {
        this.pos1 = pos1;
        this.pos2 = pos2;
        this.pos3 = pos3;
    }

    /**
     * Verifica si este molino contiene la posición dada
     */
    public boolean contiene(String posicion) {
        return pos1.equals(posicion) || pos2.equals(posicion) || pos3.equals(posicion);
    }

    /**
     * Verifica si este molino está completamente formado por el jugador dado
     */
    public boolean estaFormadoPor(IJugador jugador, Map<String, Posicion> posiciones) {
        return posiciones.get(pos1).ocupadaPor(jugador) &&
                posiciones.get(pos2).ocupadaPor(jugador) &&
                posiciones.get(pos3).ocupadaPor(jugador);
    }

    /**
     * Obtiene las tres posiciones que forman este molino
     */
    public List<String> getPosiciones() {
        return List.of(pos1, pos2, pos3);
    }

    @Override
    public String toString() {
        return "Molino[" + pos1 + ", " + pos2 + ", " + pos3 + "]";
    }
}
