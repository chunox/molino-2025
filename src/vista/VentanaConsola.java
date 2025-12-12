package vista;

import modelo.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class VentanaConsola extends JFrame implements ObservadorJuego {

    private final int idPartida;
    private final boolean esJugador1;
    private final String nombreJugador;
    private JTextArea areaConsola;
    private JTextField campoComando;
    private JScrollPane scrollPane;

    public VentanaConsola(String nombreJugador, boolean esJugador1, int idPartida) {
        this.nombreJugador = nombreJugador;
        this.esJugador1 = esJugador1;
        this.idPartida = idPartida;

        String colorJugador = esJugador1 ? "Rojo (X)" : "Azul (O)";
        setTitle("Consola - " + nombreJugador + " (" + colorJugador + ") - Partida #" + idPartida);
        setSize(600, 800);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // Área de consola
        areaConsola = new JTextArea();
        areaConsola.setEditable(false);
        areaConsola.setBackground(Color.BLACK);
        areaConsola.setForeground(Color.GREEN);
        areaConsola.setFont(new Font("Consolas", Font.PLAIN, 16));
        areaConsola.setTabSize(4);
        scrollPane = new JScrollPane(areaConsola);
        add(scrollPane, BorderLayout.CENTER);

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

        // Registrarse como observador
        Juego juego = getJuego();
        if (juego != null) {
            juego.agregarObservador(this);
        }

        mostrarAyuda();
        mostrarTablero();
        mostrarEstado();
        setVisible(true);
    }

    private Juego getJuego() {
        return GestorPartidas.getInstancia().getPartida(idPartida);
    }

    @Override
    public void onActualizacionJuego() {
        SwingUtilities.invokeLater(() -> {
            limpiarConsola();
            mostrarTablero();
            mostrarEstado();
        });
    }

    private void ejecutarComando() {
        String comando = campoComando.getText().trim().toLowerCase();
        campoComando.setText("");

        if (comando.isEmpty()) return;

        escribir("> " + comando);

        String[] partes = comando.split("\\s+");
        String cmd = partes[0];

        Juego juego = getJuego();
        if (juego == null) {
            escribir("ERROR: Juego no encontrado");
            return;
        }

        // Verificar turno
        boolean esTurnoJugador1 = (juego.getJugadorActual() == juego.getJugador1());
        boolean esMiTurno = (esJugador1 == esTurnoJugador1);

        switch (cmd) {
            case "help", "ayuda", "?" -> mostrarAyuda();
            case "estado", "status" -> mostrarEstado();
            case "tablero", "board" -> mostrarTablero();
            case "mt", "mapa" -> mostrarMapaPosiciones();
            case "limpiar", "clear", "cls" -> limpiarConsola();
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
            case "salir", "exit" -> dispose();
            default -> escribir("Comando desconocido. Escribe 'ayuda' para ver comandos.");
        }
    }

    private void limpiarConsola() {
        areaConsola.setText("");
    }

    private void colocarPieza(String posicion) {
        Juego juego = getJuego();
        if (juego.colocarPieza(posicion)) {
            limpiarConsola();
            escribir("✓ Pieza colocada en " + posicion);
            mostrarTablero();
            mostrarEstado();
        } else {
            escribir("✗ No se pudo colocar pieza en " + posicion);
        }
    }

    private void moverPieza(String origen, String destino) {
        Juego juego = getJuego();
        if (juego.moverPieza(origen, destino)) {
            limpiarConsola();
            escribir("✓ Pieza movida de " + origen + " a " + destino);
            mostrarTablero();
            mostrarEstado();
        } else {
            escribir("✗ No se pudo mover pieza");
        }
    }

    private void eliminarPieza(String posicion) {
        Juego juego = getJuego();
        if (juego.eliminarPiezaOponente(posicion)) {
            limpiarConsola();
            escribir("✓ Pieza eliminada en " + posicion);
            mostrarTablero();
            mostrarEstado();
        } else {
            escribir("✗ No se pudo eliminar pieza");
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

    private void mostrarEstado() {
        Juego juego = getJuego();
        if (juego == null) return;

        boolean esTurnoJugador1 = (juego.getJugadorActual() == juego.getJugador1());
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

        escribir("| Fase: " + padRight(getFaseNombre(juego.getFaseActual()), 32) + " |");

        if (juego.isEsperandoEliminar()) {
            escribir("| MOLINO! Elimina pieza del oponente     |");
        }

        escribir("+----------------------------------------+");
        escribir("| Piezas X (Rojo):  " + padLeft(String.valueOf(juego.getJugador1().getPiezasEnTablero()), 2) + "                 |");
        escribir("| Piezas O (Azul):  " + padLeft(String.valueOf(juego.getJugador2().getPiezasEnTablero()), 2) + "                 |");

        if (juego.hayGanador()) {
            escribir("+----------------------------------------+");
            boolean ganaste = (esJugador1 && juego.getGanador() == juego.getJugador1()) ||
                            (!esJugador1 && juego.getGanador() == juego.getJugador2());
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
    }

    private String getFaseNombre(Juego.Fase fase) {
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

    private void mostrarTablero() {
        Juego juego = getJuego();
        if (juego == null) return;

        escribir("+---------- TABLERO DE JUEGO ----------+");
        escribir("|                                      |");
        escribir("|   " + getPieza("A1") + "-----------" + getPieza("D1") + "-----------" + getPieza("G1") + "   |");
        escribir("|   |           |           |          |");
        escribir("|   |    " + getPieza("B2") + "------" + getPieza("D2") + "------" + getPieza("F2") + "    |   |");
        escribir("|   |    |      |      |    |          |");
        escribir("|   |    |  " + getPieza("C3") + "---" + getPieza("D3") + "---" + getPieza("E3") + "  |    |   |");
        escribir("|   |    |  |       |  |    |          |");
        escribir("|   " + getPieza("A4") + "----" + getPieza("B4") + "--" + getPieza("C4") + "       " + getPieza("E4") + "--" + getPieza("F4") + "----" + getPieza("G4") + "   |");
        escribir("|   |    |  |       |  |    |          |");
        escribir("|   |    |  " + getPieza("C5") + "---" + getPieza("D5") + "---" + getPieza("E5") + "  |    |   |");
        escribir("|   |    |      |      |    |          |");
        escribir("|   |    " + getPieza("B6") + "------" + getPieza("D6") + "------" + getPieza("F6") + "    |   |");
        escribir("|   |           |           |          |");
        escribir("|   " + getPieza("A7") + "-----------" + getPieza("D7") + "-----------" + getPieza("G7") + "   |");
        escribir("|                                      |");
        escribir("+--------------------------------------+");
        escribir("");
    }

    private String getPieza(String posicion) {
        Juego juego = getJuego();
        Posicion pos = juego.getTablero().getPosiciones().get(posicion);

        if (pos.estaLibre()) {
            return "·";
        } else if (pos.ocupadaPor(juego.getJugador1())) {
            return "X";
        } else {
            return "O";
        }
    }

    private void escribir(String mensaje) {
        // Padding fijo a la izquierda para mejor alineación
        String padding = "    "; // 4 espacios
        areaConsola.append(padding + mensaje + "\n");
        areaConsola.setCaretPosition(areaConsola.getDocument().getLength());
    }

    public int getIdPartida() {
        return idPartida;
    }
}
