package model.interfaces;

import java.io.Serializable;
import java.util.Map;

/**
 * Interfaz para la gestión de partidas guardadas
 */
public interface IPartidaGuardada extends Serializable {
    void actualizar(IPartida partida);
    void borrarPartidaGuardada(int id);
    Map<Integer, IPartida> getPartidasGuardadas(String nombreJugador);
    Map<Integer, IPartida> getPartidasGuardadas();
}
