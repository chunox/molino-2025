package view.frames;

import controller.Controller;
import model.interfaces.IPartida;

import javax.swing.*;
import java.awt.*;
import java.rmi.RemoteException;

/**
 * Sala de espera mientras se espera al segundo jugador
 */
public class SalaEspera extends JFrame {

    private Controller controlador;
    private JLabel lblEstado;
    private JLabel lblIdPartida;
    private JProgressBar progressBar;
    private JButton btnCancelar;
    private Timer timer;

    public SalaEspera(Controller controlador) {
        this.controlador = controlador;

        setTitle("Sala de Espera");
        setSize(450, 250);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        // Panel central
        JPanel panelCentral = new JPanel(new GridBagLayout());
        panelCentral.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.anchor = GridBagConstraints.CENTER;

        // Título
        JLabel titulo = new JLabel("🎮 Lobby Creado");
        titulo.setFont(new Font("Arial", Font.BOLD, 20));
        panelCentral.add(titulo, gbc);

        // ID de partida destacado
        gbc.gridy++;
        JPanel panelId = new JPanel();
        panelId.setBackground(new Color(70, 130, 180));
        panelId.setBorder(BorderFactory.createEmptyBorder(8, 20, 8, 20));
        lblIdPartida = new JLabel("Lobby #" + controlador.getIdPartidaActual());
        lblIdPartida.setFont(new Font("Arial", Font.BOLD, 18));
        lblIdPartida.setForeground(Color.WHITE);
        panelId.add(lblIdPartida);
        panelCentral.add(panelId, gbc);

        // Información del creador
        gbc.gridy++;
        JLabel lblCreador = new JLabel("Creado por: " + controlador.getNombreJugador());
        lblCreador.setFont(new Font("Arial", Font.PLAIN, 13));
        panelCentral.add(lblCreador, gbc);

        // Estado
        gbc.gridy++;
        lblEstado = new JLabel("⏳ Esperando a que otro jugador se una...");
        lblEstado.setFont(new Font("Arial", Font.ITALIC, 12));
        lblEstado.setForeground(new Color(100, 100, 100));
        panelCentral.add(lblEstado, gbc);

        // Barra de progreso indeterminada
        gbc.gridy++;
        progressBar = new JProgressBar();
        progressBar.setIndeterminate(true);
        progressBar.setPreferredSize(new Dimension(300, 25));
        panelCentral.add(progressBar, gbc);

        add(panelCentral, BorderLayout.CENTER);

        // Panel de botones
        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.CENTER));
        btnCancelar = new JButton("Cancelar");
        btnCancelar.addActionListener(e -> cancelar());
        panelBotones.add(btnCancelar);
        add(panelBotones, BorderLayout.SOUTH);

        // Timer para verificar si el segundo jugador se unió
        timer = new Timer(1000, e -> verificarJugadores());
        timer.start();
    }

    private void verificarJugadores() {
        try {
            IPartida partida = controlador.getPartidaActual();
            if (partida != null) {
                int numJugadores = partida.getJugadores().size();
                System.out.println("[SalaEspera-" + controlador.getNombreJugador() + "] Verificando... " +
                                 numJugadores + "/2 jugadores");

                if (numJugadores == 2) {
                    // Ambos jugadores están presentes
                    timer.stop();
                    System.out.println("[SalaEspera-" + controlador.getNombreJugador() + "] ¡Segundo jugador detectado! Cerrando sala...");

                    // Cambiar estado a EN_JUEGO
                    controlador.getVista().setEstado(model.enums.Estados.EN_JUEGO);

                    // Cerrar sala de espera sin pop-up
                    dispose();
                }
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    private void cancelar() {
        timer.stop();

        int opcion = JOptionPane.showConfirmDialog(this,
            "¿Seguro que deseas cancelar la partida?",
            "Confirmar",
            JOptionPane.YES_NO_OPTION);

        if (opcion == JOptionPane.YES_OPTION) {
            try {
                // TODO: Implementar cancelación de partida en el servidor
                controlador.setIdPartidaActual(-1);
                dispose();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            timer.start();
        }
    }

    @Override
    public void dispose() {
        if (timer != null) {
            timer.stop();
        }
        super.dispose();
    }
}
