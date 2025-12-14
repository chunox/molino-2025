package model.clases;

import model.interfaces.IRanking;
import serializacion.Serializador;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Clase Singleton para gestionar el ranking de jugadores
 */
public class Ranking implements IRanking, Serializable {
    private static final long serialVersionUID = 1L;
    private static IRanking instancia = null;
    private Serializador serializador = new Serializador("src/data/ranking.dat");
    private Map<String, Integer> ranking;

    public static IRanking getInstancia() {
        if (instancia == null) {
            instancia = new Ranking();
        }
        return instancia;
    }

    private Ranking() {
        Object obj = serializador.readFirstObject();
        ranking = (obj != null) ? (Map<String, Integer>) obj : new HashMap<>();
    }

    @Override
    public void actualizar(String nombreJugador) {
        ranking.put(nombreJugador, ranking.getOrDefault(nombreJugador, 0) + 1);
        serializador.writeOneObject(ranking);
    }

    @Override
    public Map<String, Integer> getRanking() {
        return new HashMap<>(ranking);
    }

    @Override
    public int getVictorias(String nombreJugador) {
        return ranking.getOrDefault(nombreJugador, 0);
    }
}
