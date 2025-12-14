package view.frames;

import controller.Controller;
import model.enums.FaseJuego;
import model.interfaces.IJugador;
import model.interfaces.IPartida;

import javax.swing.*;
import java.awt.*;
import java.rmi.RemoteException;

/**
 * Ventana principal del juego adaptada para RMI
 */
public class VentanaPrincipal extends JFrame {

    private final Controller controlador;
    private final boolean esJugador1;
    private final String nombreJugador;

    private PanelTablero panelTablero;
    private PanelControl panelControl;
    private String origenSeleccionado;

    public VentanaPrincipal(String nombreJugador, boolean esJugador1, Controller controlador) {
        this.nombreJugador = nombreJugador;
        this.esJugador1 = esJugador1;
        this.controlador = controlador;
        this.origenSeleccionado = null;

        String colorJugador = esJugador1 ? "Rojo (X)" : "Azul (O)";
        setTitle("Juego del Molino - " + nombreJugador + " (" + colorJugador + ") - Partida #" +
                 controlador.getIdPartidaActual());
        setSize(800, 700);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // Panel del tablero
        panelTablero = new PanelTablero();
        panelTablero.setControlador(controlador);
        panelTablero.setClickListener(this::manejarClicEnPosicion);
        add(panelTablero, BorderLayout.CENTER);

        // Panel de control
        panelControl = new PanelControl();
        panelControl.getBotonReiniciar().addActionListener(e -> reiniciarJuego());
        add(panelControl, BorderLayout.SOUTH);

        // Actualizar vista inicial
        actualizarInterfaz();
    }

    private void manejarClicEnPosicion(String posicionId) {
        try {
            IPartida partida = controlador.getPartidaActual();
            if (partida == null || partida.hayGanador()) {
                return;
            }

            // Verificar que sea el turno correcto
            boolean esTurnoJugador1 = (partida.getJugadorActual() == partida.getJugadores().get(0));
            if (esJugador1 != esTurnoJugador1) {
                mostrarMensajeError("No es tu turno");
                return;
            }

            if (partida.isEsperandoEliminar()) {
                procesarEliminacion(posicionId);
            } else {
                switch (partida.getFaseActual()) {
                    case COLOCACION -> procesarColocacion(posicionId);
                    case MOVIMIENTO, VUELO -> procesarMovimiento(posicionId);
                }
            }

            verificarFinJuego();
            actualizarInterfaz();

        } catch (RemoteException e) {
            e.printStackTrace();
            mostrarMensajeError("Error de conexión: " + e.getMessage());
        }
    }

    private void procesarColocacion(String posicionId) throws RemoteException {
        controlador.colocarPieza(posicionId);
    }

    private void procesarMovimiento(String posicionId) throws RemoteException {
        IPartida partida = controlador.getPartidaActual();

        if (origenSeleccionado == null) {
            seleccionarOrigen(posicionId);
        } else {
            ejecutarMovimiento(posicionId);
        }
    }

    private void seleccionarOrigen(String posicionId) throws RemoteException {
        IPartida partida = controlador.getPartidaActual();
        java.util.Map<String, IJugador> estadoTablero = partida.getEstadoTablero();

        IJugador ocupante = estadoTablero.get(posicionId);
        if (ocupante != null && ocupante.equals(partida.getJugadorActual())) {
            origenSeleccionado = posicionId;
            panelTablero.setPosicionSeleccionada(origenSeleccionado);

            String fase = partida.getFaseActual() == FaseJuego.VUELO ?
                    " (puedes volar a cualquier posición)" : " (solo a posiciones adyacentes)";
            panelControl.setTurnoTexto("Mueve desde " + origenSeleccionado + fase);
        } else {
            mostrarMensajeError("Debes seleccionar una de tus piezas");
        }
    }

    private void ejecutarMovimiento(String posicionDestino) throws RemoteException {
        controlador.moverPieza(origenSeleccionado, posicionDestino);
        limpiarSeleccion();
    }

    private void procesarEliminacion(String posicionId) throws RemoteException {
        controlador.eliminarPiezaOponente(posicionId);
        limpiarSeleccion();
    }

    private void verificarFinJuego() {
        try {
            IPartida partida = controlador.getPartidaActual();
            if (partida.hayGanador()) {
                Timer timer = new Timer(300, e -> mostrarGanador(partida));
                timer.setRepeats(false);
                timer.start();
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public void actualizarInterfaz() {
        SwingUtilities.invokeLater(() -> {
            try {
                IPartida partida = controlador.getPartidaActual();
                if (partida == null) return;

                boolean esTurnoJugador1 = (partida.getJugadorActual() == partida.getJugadores().get(0));
                String indicadorTurno = (esJugador1 == esTurnoJugador1) ? "TU TURNO" : "Turno del oponente";

                if (partida.isEsperandoEliminar()) {
                    panelControl.setTurnoTexto(indicadorTurno + " - ¡MOLINO! Elimina una pieza del oponente");
                } else {
                    String fase = switch (partida.getFaseActual()) {
                        case COLOCACION -> "Colocación";
                        case MOVIMIENTO -> "Movimiento";
                        case VUELO -> "Vuelo";
                    };

                    panelControl.setTurnoTexto(indicadorTurno + " - " + fase + " - " +
                            partida.getJugadorActual().getPiezasEnTablero() + " piezas");
                }

                // Actualizar tablero
                actualizarTablero(partida);

                repaint();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        });
    }

    private void actualizarTablero(IPartida partida) throws RemoteException {
        java.util.Map<String, IJugador> estadoTablero = partida.getEstadoTablero();
        IJugador j1 = partida.getJugadores().get(0);
        IJugador j2 = partida.getJugadores().get(1);
        panelTablero.actualizarTablero(estadoTablero, j1, j2);
    }

    private void limpiarSeleccion() {
        origenSeleccionado = null;
        panelTablero.setPosicionSeleccionada(null);
    }

    private void mostrarMensajeError(String mensaje) {
        System.out.println("Error: " + mensaje);
    }

    private void mostrarGanador(IPartida partida) {
        try {
            IJugador ganador = partida.getGanador();
            String mensaje = "🎉 ¡" + ganador.getNombre() + " ha ganado el juego! 🎉";
            int opcion = JOptionPane.showConfirmDialog(
                    this,
                    mensaje + "\n\n¿Deseas jugar de nuevo?",
                    "Fin del juego",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.INFORMATION_MESSAGE
            );

            if (opcion == JOptionPane.YES_OPTION) {
                reiniciarJuego();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void reiniciarJuego() {
        JOptionPane.showMessageDialog(this,
                "Para nueva partida, usa el menú principal",
                "Información",
                JOptionPane.INFORMATION_MESSAGE);
    }

    public PanelTablero getPanelTablero() {
        return panelTablero;
    }

    public PanelControl getPanelControl() {
        return panelControl;
    }
}
