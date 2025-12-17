package model.interfaces;

import ar.edu.unlu.rmimvc.observer.IObservableRemoto;
import model.enums.EstadoJugador;
import model.excepciones.JugadorExistente;
import model.excepciones.JugadorNoExistente;
import model.excepciones.PasswordIncorrecta;
import java.rmi.RemoteException;
import java.util.List;
import java.util.Map;

/**
 * Interfaz del modelo principal del juego
 * Extiende IObservableRemoto para notificar cambios a los observadores
 */
public interface IModelo extends IObservableRemoto {

    // Gestión de partidas
    IPartida crearPartida(String nombreJugador1, String nombreJugador2) throws RemoteException;
    List<IPartida> getPartidas() throws RemoteException;
    IPartida getPartida(int id) throws RemoteException;
    void empezarPartida(int id) throws RemoteException;
    void agregarJugadorAPartida(int id, String nombreJugador) throws RemoteException;

    // Gestión de jugadores
    void registrarUsuario(String nombre, String password) throws RemoteException, JugadorExistente;
    void iniciarSesion(String nombre, String password) throws RemoteException, JugadorNoExistente, PasswordIncorrecta;

    // Lógica del juego
    void colocarPieza(int idPartida, String posicion) throws RemoteException;
    void moverPieza(int idPartida, String origen, String destino) throws RemoteException;
    void eliminarPiezaOponente(int idPartida, String posicion) throws RemoteException;

    // Gestión de estados
    boolean verificarFinDelJuego(int id) throws RemoteException;
    boolean hayGanador(int id) throws RemoteException;
    IJugador getGanador(int id) throws RemoteException;

    // Persistencia y desconexión
    void desconectarJugador(String nombre, int idPartida) throws RemoteException;
    Map<Integer, IPartida> getPartidasGuardadas(String nombreJugador) throws RemoteException;

    // Ranking
    Map<String, Integer> getRanking() throws RemoteException;
    void actualizarRanking(String nombreJugador) throws RemoteException;
}
