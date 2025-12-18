package model.interfaces;

import model.enums.*;
import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.List;

/**
 * Interfaz para representar una partida del juego
 */
public interface IPartida extends Serializable {
    int getId();
    void setId(int id);

    List<IJugador> getJugadores();
    void agregarJugador(IJugador jugador);

    EstadoPartida getEstadoPartida();
    void setEstadoPartida(EstadoPartida estado);

    FaseJuego getFaseActual();
    void setFaseActual(FaseJuego fase);

    IJugador getJugadorActual();
    void setJugadorActual(IJugador jugador);

    IJugador getGanador();
    void setGanador(IJugador ganador);

    boolean hayGanador();

    // Métodos del juego
    boolean colocarPieza(String posicion) throws RemoteException;
    boolean moverPieza(String origen, String destino) throws RemoteException;
    boolean eliminarPiezaOponente(String posicion) throws RemoteException;

    boolean isEsperandoEliminar();

    // Método para obtener estado del tablero
    java.util.Map<String, IJugador> getEstadoTablero() throws RemoteException;
}
