package model.interfaces;

import ar.edu.unlu.rmimvc.observer.IObservableRemoto;
import java.rmi.RemoteException;
import java.util.List;
import java.util.Map;

/**
 * Interfaz del modelo principal del juego
 * Extiende IObservableRemoto para notificar cambios a los observadores
 */
public interface IModelo extends IObservableRemoto {

    // Gestión de partidas
    IPartida buscarPartida(String nombreJugador) throws RemoteException;
    IPartida getPartida(int id) throws RemoteException;

    // Lógica del juego
    void colocarPieza(int idPartida, String posicion) throws RemoteException;
    void moverPieza(int idPartida, String origen, String destino) throws RemoteException;
    void eliminarPiezaOponente(int idPartida, String posicion) throws RemoteException;

    // Gestión de estados
    boolean hayGanador(int id) throws RemoteException;
    IJugador getGanador(int id) throws RemoteException;

    // Ranking
    Map<String, Integer> getRanking() throws RemoteException;
}
