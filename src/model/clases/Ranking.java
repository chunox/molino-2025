package model.clases;

import model.interfaces.IRanking;
import serializacion.Serializador;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * RANKING - Sistema de puntuación persistente (Singleton)
 * Gestiona las victorias de cada jugador y las persiste en archivo (ranking.dat).
 * PATRÓN SINGLETON: Solo existe una instancia compartida por todo el servidor.
 * PERSISTENCIA: Los datos se guardan automáticamente en src/data/ranking.dat.
 * RELACIONES: Usado por Modelo para actualizar y consultar el ranking.
 */
public class Ranking implements IRanking, Serializable {
    private static final long serialVersionUID = 1L;
    private static IRanking instancia = null;                               // Instancia única (Singleton)
    private Serializador serializador = new Serializador("src/data/ranking.dat");  // Persistencia
    private Map<String, Integer> ranking;                                   // Map<NombreJugador, Victorias>

    /**
     * OBTENER INSTANCIA (Singleton)
     * @return La instancia única del Ranking
     * RELACIONES: Llamado por Modelo constructor
     */
    public static IRanking getInstancia() {
        if (instancia == null) {
            instancia = new Ranking();
        }
        return instancia;
    }

    /**
     * CONSTRUCTOR PRIVADO (Singleton)
     * Carga el ranking desde el archivo ranking.dat si existe.
     * Si no existe, crea un ranking vacío.
     * RELACIONES: Llama a serializador.readFirstObject()
     */
    private Ranking() {
        Object obj = serializador.readFirstObject();
        ranking = (obj != null) ? (Map<String, Integer>) obj : new HashMap<>();
    }

    /**
     * ACTUALIZAR VICTORIAS DE UN JUGADOR
     * Incrementa en 1 las victorias del jugador y guarda en el archivo.
     * Si el jugador no existe en el ranking, lo crea con 1 victoria.
     * @param nombreJugador Nombre del jugador ganador
     * RELACIONES: Llamado por Modelo.eliminarPiezaOponente() cuando hay ganador,
     *             llama a serializador.writeOneObject() para persistir
     */
    @Override
    public void actualizar(String nombreJugador) {
        ranking.put(nombreJugador, ranking.getOrDefault(nombreJugador, 0) + 1);
        serializador.writeOneObject(ranking);
    }

    /**
     * OBTENER RANKING COMPLETO
     * @return Copia del mapa de victorias (NombreJugador -> Victorias)
     * RELACIONES: Llamado por Modelo.getRanking()
     */
    @Override
    public Map<String, Integer> getRanking() {
        return new HashMap<>(ranking);
    }

    /**
     * OBTENER VICTORIAS DE UN JUGADOR
     * @param nombreJugador Nombre del jugador
     * @return Número de victorias (0 si no está en el ranking)
     */
    @Override
    public int getVictorias(String nombreJugador) {
        return ranking.getOrDefault(nombreJugador, 0);
    }
}
