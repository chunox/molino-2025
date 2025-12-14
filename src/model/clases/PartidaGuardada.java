package model.clases;

import model.interfaces.IJugador;
import model.interfaces.IPartida;
import model.interfaces.IPartidaGuardada;
import serializacion.Serializador;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Clase Singleton para gestionar las partidas guardadas
 */
public class PartidaGuardada implements IPartidaGuardada, Serializable {
    private static final long serialVersionUID = 1L;
    private static IPartidaGuardada instancia = null;
    private Serializador serializador = new Serializador("src/data/partidas.dat");
    private Map<Integer, IPartida> partidas;

    public static IPartidaGuardada getInstancia() {
        if (instancia == null) {
            instancia = new PartidaGuardada();
        }
        return instancia;
    }

    private PartidaGuardada() {
        Object obj = serializador.readFirstObject();
        partidas = (obj != null) ? (Map<Integer, IPartida>) obj : new HashMap<>();
    }

    @Override
    public void actualizar(IPartida partida) {
        partidas.put(partida.getId(), partida);
        serializador.writeOneObject(partidas);
    }

    @Override
    public void borrarPartidaGuardada(int id) {
        if (partidas.containsKey(id)) {
            partidas.remove(id);
            serializador.writeOneObject(partidas);
        }
    }

    @Override
    public Map<Integer, IPartida> getPartidasGuardadas(String nombreJugador) {
        Map<Integer, IPartida> resultado = new HashMap<>();
        for (Map.Entry<Integer, IPartida> entry : partidas.entrySet()) {
            for (IJugador jugador : entry.getValue().getJugadores()) {
                if (jugador.getNombre().equals(nombreJugador)) {
                    resultado.put(entry.getKey(), entry.getValue());
                    break;
                }
            }
        }
        return resultado;
    }

    @Override
    public Map<Integer, IPartida> getPartidasGuardadas() {
        return new HashMap<>(partidas);
    }
}
