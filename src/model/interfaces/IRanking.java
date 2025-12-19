package model.interfaces;

import java.io.Serializable;
import java.util.Map;

/**
 * Interfaz para la gestión del ranking de jugadores
 */
public interface IRanking extends Serializable {
    void actualizar(String nombreJugador);
    Map<String, Integer> getRanking();
}
