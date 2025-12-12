package controlador;

import modelo.*;
import vista.*;

import javax.swing.*;
import java.util.HashMap;
import java.util.Map;

public class ControladorJuego implements ObservadorJuego {

    private final int idPartida;
    private final VentanaPrincipal ventana;
    private final boolean esJugador1;
    private final Map<String, Character> piezasGraficas;
    private String origenSeleccionado;

    public ControladorJuego(VentanaPrincipal ventana, int idPartida, boolean esJugador1) {
        this.ventana = ventana;
        this.idPartida = idPartida;
        this.esJugador1 = esJugador1;
        this.piezasGraficas = new HashMap<>();
        this.origenSeleccionado = null;

        // Registrarse como observador
        Juego juego = getJuego();
        if (juego != null) {
            juego.agregarObservador(this);
        }

        conectarEventos();
        sincronizarPiezas();
        actualizarInterfaz();
    }

    private Juego getJuego() {
        return modelo.GestorPartidas.getInstancia().getPartida(idPartida);
    }

    @Override
    public void onActualizacionJuego() {
        SwingUtilities.invokeLater(() -> {
            sincronizarPiezas();
            actualizarInterfaz();
        });
    }

    private void sincronizarPiezas() {
        Juego juego = getJuego();
        if (juego == null) return;

        piezasGraficas.clear();
        juego.getTablero().getPosiciones().forEach((posId, posicion) -> {
            if (!posicion.estaLibre()) {
                if (posicion.ocupadaPor(juego.getJugador1())) {
                    piezasGraficas.put(posId, 'X');
                } else {
                    piezasGraficas.put(posId, 'O');
                }
            }
        });
    }

    private void conectarEventos() {
        ventana.getPanelTablero().setControlador(this);
        ventana.getPanelControl().getBotonReiniciar().addActionListener(e -> reiniciarJuego());
    }

    public void manejarClicEnPosicion(String posicionId) {
        Juego juego = getJuego();
        if (juego == null || juego.hayGanador()) {
            return;
        }

        // Verificar que sea el turno correcto
        boolean esTurnoJugador1 = (juego.getJugadorActual() == juego.getJugador1());
        if (esJugador1 != esTurnoJugador1) {
            mostrarMensajeError("No es tu turno");
            return;
        }

        if (juego.isEsperandoEliminar()) {
            procesarEliminacion(posicionId);
        } else {
            switch (juego.getFaseActual()) {
                case COLOCACION -> procesarColocacion(posicionId);
                case MOVIMIENTO, VUELO -> procesarMovimiento(posicionId);
            }
        }

        verificarFinJuego();
    }

    private void procesarColocacion(String posicionId) {
        Juego juego = getJuego();
        if (!juego.colocarPieza(posicionId)) {
            mostrarMensajeError("No puedes colocar una pieza ahí");
        }
        // La actualización se hace automáticamente vía observador
    }

    private void procesarMovimiento(String posicionId) {
        if (origenSeleccionado == null) {
            seleccionarOrigen(posicionId);
        } else {
            ejecutarMovimiento(posicionId);
        }
    }

    private void seleccionarOrigen(String posicionId) {
        Juego juego = getJuego();
        Posicion pos = juego.getTablero().getPosiciones().get(posicionId);

        if (pos != null && pos.ocupadaPor(juego.getJugadorActual())) {
            origenSeleccionado = posicionId;
            ventana.getPanelTablero().setPosicionSeleccionada(origenSeleccionado);

            String fase = juego.getFaseActual() == Juego.Fase.VUELO ?
                    " (puedes volar a cualquier posición)" : " (solo a posiciones adyacentes)";
            ventana.getPanelControl().setTurnoTexto(
                    "Mueve desde " + origenSeleccionado + fase
            );
        } else {
            mostrarMensajeError("Debes seleccionar una de tus piezas");
        }
    }

    private void ejecutarMovimiento(String posicionDestino) {
        Juego juego = getJuego();
        if (juego.moverPieza(origenSeleccionado, posicionDestino)) {
            limpiarSeleccion();
        } else {
            String mensaje = juego.getFaseActual() == Juego.Fase.VUELO ?
                    "No puedes mover ahí (posición ocupada)" :
                    "No puedes mover ahí (posición ocupada o no adyacente)";
            mostrarMensajeError(mensaje);
        }
        // La actualización se hace automáticamente vía observador
    }

    private void procesarEliminacion(String posicionId) {
        Juego juego = getJuego();
        if (juego.eliminarPiezaOponente(posicionId)) {
            limpiarSeleccion();
        } else {
            Jugador oponente = (juego.getJugadorActual() == juego.getJugador1()) ?
                    juego.getJugador2() : juego.getJugador1();

            if (juego.getTablero().formaMolino(posicionId, oponente)) {
                mostrarMensajeError("No puedes eliminar una pieza que está en un molino (a menos que todas lo estén)");
            } else {
                mostrarMensajeError("Debes seleccionar una pieza del oponente");
            }
        }
        // La actualización se hace automáticamente vía observador
    }

    private void verificarFinJuego() {
        Juego juego = getJuego();
        if (juego.hayGanador()) {
            Timer timer = new Timer(300, e -> {
                mostrarGanador(juego.getGanador());
            });
            timer.setRepeats(false);
            timer.start();
        }
    }

    private void actualizarInterfaz() {
        Juego juego = getJuego();
        if (juego == null) return;

        boolean esTurnoJugador1 = (juego.getJugadorActual() == juego.getJugador1());
        String indicadorTurno = (esJugador1 == esTurnoJugador1) ? "TU TURNO" : "Turno del oponente";

        if (juego.isEsperandoEliminar()) {
            ventana.getPanelControl().setTurnoTexto(
                    indicadorTurno + " - ¡MOLINO! Elimina una pieza del oponente"
            );
        } else {
            String fase = switch (juego.getFaseActual()) {
                case COLOCACION -> "Colocación";
                case MOVIMIENTO -> "Movimiento";
                case VUELO -> "Vuelo";
            };

            ventana.getPanelControl().setTurnoTexto(
                    indicadorTurno + " - " + fase + " - " +
                            juego.getJugadorActual().getPiezasEnTablero() + " piezas"
            );
        }

        ventana.repaint();
    }

    private void limpiarSeleccion() {
        origenSeleccionado = null;
        ventana.getPanelTablero().setPosicionSeleccionada(null);
    }

    private void mostrarMensajeError(String mensaje) {
        // Opcional: puedes mostrar un tooltip o mensaje en la interfaz
        System.out.println("Error: " + mensaje);
    }

    private void mostrarGanador(Jugador ganador) {
        String mensaje = "🎉 ¡" + ganador.getNombre() + " ha ganado el juego! 🎉";
        int opcion = JOptionPane.showConfirmDialog(
                ventana,
                mensaje + "\n\n¿Deseas jugar de nuevo?",
                "Fin del juego",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.INFORMATION_MESSAGE
        );

        if (opcion == JOptionPane.YES_OPTION) {
            reiniciarJuego();
        }
    }

    private void reiniciarJuego() {
        JOptionPane.showMessageDialog(ventana,
                "Para nueva partida, usa el menú principal",
                "Información",
                JOptionPane.INFORMATION_MESSAGE);
    }

    public Map<String, Character> getPiezasGraficas() {
        return piezasGraficas;
    }
}