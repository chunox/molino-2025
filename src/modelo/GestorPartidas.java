package modelo;

import java.util.ArrayList;
import java.util.List;

public class GestorPartidas {

    private static GestorPartidas instancia;
    private List<Juego> partidas;

    private GestorPartidas() {
        this.partidas = new ArrayList<>();
    }

    public static GestorPartidas getInstancia() {
        if (instancia == null) {
            instancia = new GestorPartidas();
        }
        return instancia;
    }

    public int crearPartida(String nombreJugador1, String nombreJugador2) {
        Juego nuevaPartida = new Juego(nombreJugador1, nombreJugador2);
        partidas.add(nuevaPartida);
        return partidas.size() - 1; // retorna el ID de la partida
    }

    public Juego getPartida(int idPartida) {
        if (idPartida >= 0 && idPartida < partidas.size()) {
            return partidas.get(idPartida);
        }
        return null;
    }

    public int getCantidadPartidas() {
        return partidas.size();
    }

    public void eliminarPartida(int idPartida) {
        if (idPartida >= 0 && idPartida < partidas.size()) {
            partidas.set(idPartida, null);
        }
    }
}
