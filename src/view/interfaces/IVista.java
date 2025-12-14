package view.interfaces;

import controller.Controller;
import model.enums.Estados;
import java.rmi.RemoteException;

/**
 * Interfaz para las vistas del juego
 */
public interface IVista {
    void login();
    void menu();
    void mostrarPartida() throws RemoteException;
    void mostrarGameOver();
    void mostrarGameWin() throws RemoteException;
    void buscarPartidas() throws RemoteException;
    void salaEspera() throws RemoteException;

    Estados getEstado();
    void setEstado(Estados estado);
    Controller getControlador();
}
