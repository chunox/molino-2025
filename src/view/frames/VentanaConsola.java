package view.frames;

import controller.Controller;
import model.enums.FaseJuego;
import model.interfaces.IJugador;
import model.interfaces.IPartida;
import view.interfaces.IVentanaJuego;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.rmi.RemoteException;

/**
 * Ventana de consola adaptada para RMI
 */
public class VentanaConsola extends JFrame implements IVentanaJuego {

    private final Controller controlador;
    private final boolean esJugador1;
    private final String nombreJugador;

    private JTextArea areaConsola;
    private JTextArea areaTablero;
    private JTextArea areaPosiciones;
    private JTextField campoComando;
    private JScrollPane scrollPane;

    public VentanaConsola(String nombreJugador, boolean esJugador1, Controller controlador) {
        this.nombreJugador = nombreJugador;
        this.esJugador1 = esJugador1;
        this.controlador = controlador;

        String colorJugador = esJugador1 ? "Rojo (X)" : "Azul (O)";
        setTitle("Consola - " + nombreJugador + " (" + colorJugador + ") - Partida #" +
                 controlador.getIdPartidaActual());
        setSize(800, 750);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // Área de consola (superior para mensajes)
        areaConsola = new JTextArea(12, 50);
        areaConsola.setEditable(false);
        areaConsola.setBackground(Color.BLACK);
        areaConsola.setForeground(Color.GREEN);
        areaConsola.setFont(new Font("Consolas", Font.PLAIN, 14));
        areaConsola.setTabSize(4);
        scrollPane = new JScrollPane(areaConsola);
        add(scrollPane, BorderLayout.NORTH);

        // Panel central con tablero y mapa de posiciones lado a lado
        JPanel panelCentral = new JPanel(new GridLayout(1, 2, 10, 0));
        panelCentral.setBackground(Color.BLACK);

        // Área del tablero con piezas (izquierda)
        areaTablero = new JTextArea();
        areaTablero.setEditable(false);
        areaTablero.setBackground(Color.BLACK);
        areaTablero.setForeground(Color.GREEN);
        areaTablero.setFont(new Font("Consolas", Font.PLAIN, 14));
        JScrollPane scrollTablero = new JScrollPane(areaTablero);
        panelCentral.add(scrollTablero);

        // Área del mapa de posiciones (derecha)
        areaPosiciones = new JTextArea();
        areaPosiciones.setEditable(false);
        areaPosiciones.setBackground(Color.BLACK);
        areaPosiciones.setForeground(Color.CYAN);
        areaPosiciones.setFont(new Font("Consolas", Font.PLAIN, 14));
        JScrollPane scrollPosiciones = new JScrollPane(areaPosiciones);
        panelCentral.add(scrollPosiciones);

        add(panelCentral, BorderLayout.CENTER);

        // Campo de comandos
        JPanel panelComando = new JPanel(new BorderLayout());
        JLabel prompt = new JLabel("> ");
        prompt.setForeground(Color.GREEN);
        prompt.setBackground(Color.BLACK);
        prompt.setOpaque(true);
        prompt.setFont(new Font("Monospaced", Font.PLAIN, 14));

        campoComando = new JTextField();
        campoComando.setBackground(Color.BLACK);
        campoComando.setForeground(Color.GREEN);
        campoComando.setCaretColor(Color.GREEN);
        campoComando.setFont(new Font("Monospaced", Font.PLAIN, 14));
        campoComando.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    ejecutarComando();
                }
            }
        });

        panelComando.add(prompt, BorderLayout.WEST);
        panelComando.add(campoComando, BorderLayout.CENTER);
        panelComando.setBackground(Color.BLACK);
        add(panelComando, BorderLayout.SOUTH);

        mostrarAyuda();
        mostrarEstado();
        actualizarTablero();
        mostrarMapaPosicionesEnPanel();
    }

    private void ejecutarComando() {
        String comando = campoComando.getText().trim().toLowerCase();
        campoComando.setText("");

        if (comando.isEmpty()) return;

        escribir("> " + comando);

        String[] partes = comando.split("\\s+");
        String cmd = partes[0];

        try {
            IPartida partida = controlador.getPartidaActual();
            if (partida == null) {
                escribir("ERROR: Juego no encontrado");
                return;
            }

            // Comandos que no requieren verificación de jugadores
            switch (cmd) {
                case "help", "ayuda", "?" -> {
                    mostrarAyuda();
                    return;
                }
                case "estado", "status" -> {
                    mostrarEstado();
                    return;
                }
                case "tablero", "board" -> {
                    mostrarTablero();
                    return;
                }
                case "mt", "mapa" -> {
                    mostrarMapaPosiciones();
                    return;
                }
                case "limpiar", "clear", "cls" -> {
                    limpiarConsola();
                    return;
                }
                case "salir", "exit" -> {
                    salirDePartida();
                    return;
                }
            }

            // Para comandos de juego, verificar que haya 2 jugadores
            if (partida.getJugadores().size() < 2) {
                escribir("ERROR: Esperando que se una el segundo jugador");
                return;
            }

            // Verificar turno
            boolean esTurnoJugador1 = (partida.getJugadorActual() == partida.getJugadores().get(0));
            boolean esMiTurno = (esJugador1 == esTurnoJugador1);

            switch (cmd) {
                case "colocar", "place" -> {
                    if (!esMiTurno) {
                        escribir("ERROR: No es tu turno");
                    } else if (partes.length < 2) {
                        escribir("Uso: colocar <posición> (ej: colocar A1)");
                    } else {
                        colocarPieza(partes[1].toUpperCase());
                    }
                }
                case "mover", "move" -> {
                    if (!esMiTurno) {
                        escribir("ERROR: No es tu turno");
                    } else if (partes.length < 3) {
                        escribir("Uso: mover <origen> <destino> (ej: mover A1 D1)");
                    } else {
                        moverPieza(partes[1].toUpperCase(), partes[2].toUpperCase());
                    }
                }
                case "eliminar", "remove" -> {
                    if (!esMiTurno) {
                        escribir("ERROR: No es tu turno");
                    } else if (partes.length < 2) {
                        escribir("Uso: eliminar <posición> (ej: eliminar A1)");
                    } else {
                        eliminarPieza(partes[1].toUpperCase());
                    }
                }
                default -> escribir("Comando desconocido. Escribe 'ayuda' para ver comandos.");
            }
        } catch (RemoteException e) {
            escribir("ERROR: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void limpiarConsola() {
        areaConsola.setText("");
    }

    private void colocarPieza(String posicion) {
        try {
            controlador.colocarPieza(posicion);
            escribir("✓ Pieza colocada en " + posicion);
            actualizarTablero();
            mostrarEstado();
        } catch (RemoteException e) {
            escribir("✗ No se pudo colocar pieza en " + posicion);
            e.printStackTrace();
        }
    }

    private void moverPieza(String origen, String destino) {
        try {
            controlador.moverPieza(origen, destino);
            escribir("✓ Pieza movida de " + origen + " a " + destino);
            actualizarTablero();
            mostrarEstado();
        } catch (RemoteException e) {
            escribir("✗ No se pudo mover pieza");
            e.printStackTrace();
        }
    }

    private void eliminarPieza(String posicion) {
        try {
            controlador.eliminarPiezaOponente(posicion);
            escribir("✓ Pieza eliminada en " + posicion);
            actualizarTablero();
            mostrarEstado();
        } catch (RemoteException e) {
            escribir("✗ No se pudo eliminar pieza");
            e.printStackTrace();
        }
    }

    private void mostrarAyuda() {
        escribir("+--------- COMANDOS DISPONIBLES --------+");
        escribir("| ayuda         - Muestra esta ayuda     |");
        escribir("| estado        - Muestra estado juego   |");
        escribir("| tablero       - Muestra el tablero     |");
        escribir("| mt            - Muestra mapa posiciones|");
        escribir("| colocar <pos> - Coloca (ej: colocar A1)|");
        escribir("| mover <o> <d> - Mueve (ej: mover A1 D1)|");
        escribir("| eliminar <p>  - Elimina pieza oponente |");
        escribir("| salir         - Cierra la ventana      |");
        escribir("+----------------------------------------+");
        escribir("");
    }

    private void mostrarMapaPosiciones() {
        escribir("+---------- MAPA DE POSICIONES --------+");
        escribir("|                                      |");
        escribir("|   A1-----------D1-----------G1       |");
        escribir("|   |            |            |        |");
        escribir("|   |     B2-----D2-----F2    |        |");
        escribir("|   |     |      |      |     |        |");
        escribir("|   |     |  C3--D3--E3 |     |        |");
        escribir("|   |     |  |      |   |     |        |");
        escribir("|   A4----B4-C4     E4--F4----G4       |");
        escribir("|   |     |  |      |   |     |        |");
        escribir("|   |     |  C5--D5--E5 |     |        |");
        escribir("|   |     |      |      |     |        |");
        escribir("|   |     B6-----D6-----F6    |        |");
        escribir("|   |            |            |        |");
        escribir("|   A7-----------D7-----------G7       |");
        escribir("|                                      |");
        escribir("+--------------------------------------+");
        escribir("");
    }

    private void mostrarMapaPosicionesEnPanel() {
        escribirPosiciones("+---------- MAPA DE POSICIONES --------+");
        escribirPosiciones("|                                      |");
        escribirPosiciones("|   A1-----------D1-----------G1       |");
        escribirPosiciones("|   |            |            |        |");
        escribirPosiciones("|   |     B2-----D2-----F2    |        |");
        escribirPosiciones("|   |     |      |      |     |        |");
        escribirPosiciones("|   |     |  C3--D3--E3 |     |        |");
        escribirPosiciones("|   |     |  |      |   |     |        |");
        escribirPosiciones("|   A4----B4-C4     E4--F4----G4       |");
        escribirPosiciones("|   |     |  |      |   |     |        |");
        escribirPosiciones("|   |     |  C5--D5--E5 |     |        |");
        escribirPosiciones("|   |     |      |      |     |        |");
        escribirPosiciones("|   |     B6-----D6-----F6    |        |");
        escribirPosiciones("|   |            |            |        |");
        escribirPosiciones("|   A7-----------D7-----------G7       |");
        escribirPosiciones("|                                      |");
        escribirPosiciones("+--------------------------------------+");
    }

    private void mostrarEstado() {
        try {
            IPartida partida = controlador.getPartidaActual();
            if (partida == null) return;

            // Solo mostrar estado completo si hay 2 jugadores
            if (partida.getJugadores().size() < 2) {
                escribir("+----------- ESPERANDO JUGADORES --------+");
                escribir("| Jugador: " + padRight(nombreJugador, 29) + " |");
                escribir("| Esperando que se una el segundo jugador|");
                escribir("+----------------------------------------+");
                escribir("");
                return;
            }

            boolean esTurnoJugador1 = (partida.getJugadorActual() == partida.getJugadores().get(0));
            boolean esMiTurno = (esJugador1 == esTurnoJugador1);

            escribir("+----------- ESTADO DEL JUEGO -----------+");
            escribir("| Jugador: " + padRight(nombreJugador, 29) + " |");
            escribir("| Simbolo: " + padRight(esJugador1 ? "X (Rojo)" : "O (Azul)", 29) + " |");
            escribir("+----------------------------------------+");

            if (esMiTurno) {
                escribir("|         >>> TU TURNO <<<               |");
            } else {
                escribir("|       Turno del oponente               |");
            }

            escribir("| Fase: " + padRight(getFaseNombre(partida.getFaseActual()), 32) + " |");

            if (partida.isEsperandoEliminar()) {
                escribir("| MOLINO! Elimina pieza del oponente     |");
            }

            escribir("+----------------------------------------+");
            escribir("| Piezas X (Rojo):  " + padLeft(String.valueOf(partida.getJugadores().get(0).getPiezasEnTablero()), 2) + "                 |");
            escribir("| Piezas O (Azul):  " + padLeft(String.valueOf(partida.getJugadores().get(1).getPiezasEnTablero()), 2) + "                 |");

            if (partida.hayGanador()) {
                escribir("+----------------------------------------+");
                boolean ganaste = (esJugador1 && partida.getGanador() == partida.getJugadores().get(0)) ||
                                (!esJugador1 && partida.getGanador() == partida.getJugadores().get(1));
                if (ganaste) {
                    escribir("|                                        |");
                    escribir("|       *** HAS GANADO! ***              |");
                    escribir("|                                        |");
                } else {
                    escribir("|          Has perdido                   |");
                }
            }

            escribir("+----------------------------------------+");
            escribir("");
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    private String getFaseNombre(FaseJuego fase) {
        return switch(fase) {
            case COLOCACION -> "Colocación (pon tus 9 piezas)";
            case MOVIMIENTO -> "Movimiento (adyacentes)";
            case VUELO -> "Vuelo (libre)";
        };
    }

    private String padRight(String s, int n) {
        return String.format("%-" + n + "s", s);
    }

    private String padLeft(String s, int n) {
        return String.format("%" + n + "s", s);
    }

    private void actualizarTablero() {
        try {
            IPartida partida = controlador.getPartidaActual();
            if (partida == null) return;

            // Solo mostrar tablero si hay al menos 1 jugador
            if (partida.getJugadores().isEmpty()) {
                return;
            }

            areaTablero.setText("");

            escribirTablero("+---------- TABLERO DE JUEGO ----------+");
            escribirTablero("|                                      |");
            escribirTablero("|   " + getPieza("A1") + "-----------" + getPieza("D1") + "-----------" + getPieza("G1") + "   |");
            escribirTablero("|   |           |           |          |");
            escribirTablero("|   |    " + getPieza("B2") + "------" + getPieza("D2") + "------" + getPieza("F2") + "    |   |");
            escribirTablero("|   |    |      |      |    |          |");
            escribirTablero("|   |    |  " + getPieza("C3") + "---" + getPieza("D3") + "---" + getPieza("E3") + "  |    |   |");
            escribirTablero("|   |    |  |       |  |    |          |");
            escribirTablero("|   " + getPieza("A4") + "----" + getPieza("B4") + "--" + getPieza("C4") + "       " + getPieza("E4") + "--" + getPieza("F4") + "----" + getPieza("G4") + "   |");
            escribirTablero("|   |    |  |       |  |    |          |");
            escribirTablero("|   |    |  " + getPieza("C5") + "---" + getPieza("D5") + "---" + getPieza("E5") + "  |    |   |");
            escribirTablero("|   |    |      |      |    |          |");
            escribirTablero("|   |    " + getPieza("B6") + "------" + getPieza("D6") + "------" + getPieza("F6") + "    |   |");
            escribirTablero("|   |           |           |          |");
            escribirTablero("|   " + getPieza("A7") + "-----------" + getPieza("D7") + "-----------" + getPieza("G7") + "   |");
            escribirTablero("|                                      |");
            escribirTablero("+--------------------------------------+");
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    private void mostrarTablero() {
        actualizarTablero();
    }

    private String getPieza(String posicion) {
        try {
            IPartida partida = controlador.getPartidaActual();
            java.util.Map<String, IJugador> estadoTablero = partida.getEstadoTablero();

            IJugador ocupante = estadoTablero.get(posicion);

            if (ocupante == null) {
                return "·";
            } else if (ocupante.equals(partida.getJugadores().get(0))) {
                return "X";
            } else {
                return "O";
            }
        } catch (RemoteException e) {
            return "?";
        }
    }

    private void escribir(String mensaje) {
        String padding = "    ";
        areaConsola.append(padding + mensaje + "\n");
        areaConsola.setCaretPosition(areaConsola.getDocument().getLength());
    }

    private void escribirTablero(String mensaje) {
        String padding = "    ";
        areaTablero.append(padding + mensaje + "\n");
    }

    private void escribirPosiciones(String mensaje) {
        String padding = "    ";
        areaPosiciones.append(padding + mensaje + "\n");
    }

    @Override
    public void actualizarInterfaz() {
        SwingUtilities.invokeLater(() -> {
            actualizarTablero();
            mostrarEstado();
        });
    }

    /**
     * Permite salir de la partida actual
     */
    private void salirDePartida() {
        controlador.getVista().salirDePartida();
    }
}
