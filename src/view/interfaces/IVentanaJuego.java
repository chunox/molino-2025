package view.interfaces;

import java.rmi.RemoteException;

/**
 * Interfaz común para las ventanas de juego
 * Permite a VistaGrafica trabajar de forma polimórfica
 * con diferentes tipos de ventanas (gráfica o consola)
 */
public interface IVentanaJuego {
    /**
     * Actualiza la interfaz con el estado actual del juego
     */
    void actualizarInterfaz() throws RemoteException;

    /**
     * Hace visible la ventana
     */
    void setVisible(boolean visible);

    /**
     * Libera los recursos de la ventana
     */
    void dispose();
}
