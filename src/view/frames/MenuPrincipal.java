package view.frames;

import controller.Controller;
import model.enums.Estados;

import javax.swing.*;
import java.awt.*;
import java.rmi.RemoteException;

/**
 * Menú principal del juego
 */
public class MenuPrincipal extends JFrame {

    private Controller controlador;
    private JButton btnCrearPartida;
    private JButton btnUnirsePartida;
    private JButton btnVerRanking;
    private JButton btnSalir;

    public MenuPrincipal(Controller controlador) {
        this.controlador = controlador;

        setTitle("Juego del Molino - Menú Principal");
        setSize(400, 350);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        // Panel superior con título
        JPanel panelTitulo = new JPanel();
        panelTitulo.setBackground(new Color(70, 130, 180));
        JLabel lblTitulo = new JLabel("JUEGO DEL MOLINO");
        lblTitulo.setFont(new Font("Arial", Font.BOLD, 24));
        lblTitulo.setForeground(Color.WHITE);
        panelTitulo.add(lblTitulo);
        add(panelTitulo, BorderLayout.NORTH);

        // Panel central con botones
        JPanel panelCentral = new JPanel(new GridBagLayout());
        panelCentral.setBorder(BorderFactory.createEmptyBorder(20, 40, 20, 40));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(10, 0, 10, 0);

        // Mostrar nombre del jugador si está disponible
        if (controlador.getNombreJugador() != null) {
            JLabel lblJugador = new JLabel("Jugador: " + controlador.getNombreJugador());
            lblJugador.setFont(new Font("Arial", Font.PLAIN, 14));
            lblJugador.setHorizontalAlignment(SwingConstants.CENTER);
            panelCentral.add(lblJugador, gbc);
            gbc.gridy++;
        }

        // Botón crear partida
        btnCrearPartida = new JButton("Crear Nueva Partida");
        btnCrearPartida.setFont(new Font("Arial", Font.BOLD, 14));
        btnCrearPartida.setPreferredSize(new Dimension(250, 40));
        btnCrearPartida.addActionListener(e -> abrirCrearPartida());
        panelCentral.add(btnCrearPartida, gbc);
        gbc.gridy++;

        // Botón unirse a partida
        btnUnirsePartida = new JButton("Unirse a Partida");
        btnUnirsePartida.setFont(new Font("Arial", Font.BOLD, 14));
        btnUnirsePartida.setPreferredSize(new Dimension(250, 40));
        btnUnirsePartida.addActionListener(e -> abrirListaPartidas());
        panelCentral.add(btnUnirsePartida, gbc);
        gbc.gridy++;

        // Botón ver ranking
        btnVerRanking = new JButton("Ver Ranking");
        btnVerRanking.setFont(new Font("Arial", Font.BOLD, 14));
        btnVerRanking.setPreferredSize(new Dimension(250, 40));
        btnVerRanking.addActionListener(e -> verRanking());
        panelCentral.add(btnVerRanking, gbc);
        gbc.gridy++;

        // Botón salir
        btnSalir = new JButton("Salir");
        btnSalir.setFont(new Font("Arial", Font.PLAIN, 12));
        btnSalir.setPreferredSize(new Dimension(250, 35));
        btnSalir.addActionListener(e -> salir());
        panelCentral.add(btnSalir, gbc);

        add(panelCentral, BorderLayout.CENTER);
    }

    private void abrirCrearPartida() {
        try {
            MenuCrearPartida menuCrear = new MenuCrearPartida(controlador);
            menuCrear.setVisible(true);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                "Error al abrir menú de crear partida: " + e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private void abrirListaPartidas() {
        try {
            controlador.getVista().setEstado(Estados.EN_BUSCAR_PARTIDA);
            controlador.getVista().buscarPartidas();
        } catch (RemoteException e) {
            JOptionPane.showMessageDialog(this,
                "Error al buscar partidas: " + e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private void verRanking() {
        try {
            java.util.Map<String, Integer> ranking = controlador.getRanking();

            StringBuilder sb = new StringBuilder();
            sb.append("=== RANKING DE JUGADORES ===\n\n");

            if (ranking.isEmpty()) {
                sb.append("No hay jugadores en el ranking aún.");
            } else {
                int posicion = 1;
                for (var entry : ranking.entrySet()) {
                    sb.append(posicion).append(". ")
                      .append(entry.getKey()).append(" - ")
                      .append(entry.getValue()).append(" victorias\n");
                    posicion++;
                }
            }

            JTextArea textArea = new JTextArea(sb.toString());
            textArea.setEditable(false);
            textArea.setFont(new Font("Monospaced", Font.PLAIN, 12));

            JScrollPane scrollPane = new JScrollPane(textArea);
            scrollPane.setPreferredSize(new Dimension(350, 250));

            JOptionPane.showMessageDialog(this,
                scrollPane,
                "Ranking",
                JOptionPane.INFORMATION_MESSAGE);

        } catch (RemoteException e) {
            JOptionPane.showMessageDialog(this,
                "Error al obtener ranking: " + e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private void salir() {
        int opcion = JOptionPane.showConfirmDialog(this,
            "¿Seguro que deseas salir?",
            "Confirmar",
            JOptionPane.YES_NO_OPTION);

        if (opcion == JOptionPane.YES_OPTION) {
            System.exit(0);
        }
    }
}
