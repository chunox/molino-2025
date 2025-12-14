package view.vistas;

import controller.Controller;
import model.enums.Estados;
import view.frames.*;
import view.interfaces.IVista;
import javax.swing.*;
import java.rmi.RemoteException;

/**
 * Vista gráfica del juego que implementa IVista
 * Usa las vistas originales adaptadas para RMI
 */
public class VistaGrafica implements IVista {
    private Controller controlador;
    private Estados estado;

    private MenuPrincipal menuPrincipal;
    private SalaEspera salaEspera;
    private ListaPartidas listaPartidas;
    private VentanaPrincipal ventanaPrincipal;
    private VentanaConsola ventanaConsola;

    public VistaGrafica() throws RemoteException {
        this.controlador = new Controller();
        this.controlador.setVista(this);
        this.estado = Estados.EN_MENU;
    }

    @Override
    public void login() {
        // En esta versión simplificada, vamos directo al menú
        menu();
    }

    @Override
    public void menu() {
        this.estado = Estados.EN_MENU;
        SwingUtilities.invokeLater(() -> {
            if (menuPrincipal == null) {
                menuPrincipal = new MenuPrincipal(controlador);
            }
            menuPrincipal.setVisible(true);
        });
    }

    @Override
    public void mostrarPartida() throws RemoteException {
        this.estado = Estados.EN_JUEGO;
        // Actualizar las ventanas activas
        SwingUtilities.invokeLater(() -> {
            if (ventanaPrincipal != null) {
                ventanaPrincipal.actualizarInterfaz();
            }
            if (ventanaConsola != null) {
                ventanaConsola.onActualizacionJuego();
            }
        });
    }

    @Override
    public void mostrarGameOver() {
        System.out.println("Game Over");
    }

    @Override
    public void mostrarGameWin() throws RemoteException {
        System.out.println("Victoria");
    }

    @Override
    public void buscarPartidas() throws RemoteException {
        this.estado = Estados.EN_BUSCAR_PARTIDA;
        SwingUtilities.invokeLater(() -> {
            if (listaPartidas != null) {
                listaPartidas.dispose();
            }
            listaPartidas = new ListaPartidas(controlador);
            listaPartidas.setVisible(true);
        });
    }

    @Override
    public void salaEspera() throws RemoteException {
        this.estado = Estados.EN_ESPERANDO_JUGADORES;
        SwingUtilities.invokeLater(() -> {
            if (salaEspera != null) {
                salaEspera.dispose();
            }
            salaEspera = new SalaEspera(controlador);
            salaEspera.setVisible(true);
        });
    }

    @Override
    public Estados getEstado() {
        return estado;
    }

    @Override
    public void setEstado(Estados estado) {
        this.estado = estado;
    }

    @Override
    public Controller getControlador() {
        return controlador;
    }

    // Métodos para registrar ventanas activas
    public void setVentanaPrincipal(VentanaPrincipal ventana) {
        this.ventanaPrincipal = ventana;
    }

    public void setVentanaConsola(VentanaConsola ventana) {
        this.ventanaConsola = ventana;
    }
}
