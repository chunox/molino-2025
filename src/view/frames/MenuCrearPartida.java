package view.frames;

import controller.Controller;
import model.enums.Estados;
import model.interfaces.IPartida;

import javax.swing.*;
import java.awt.*;
import java.rmi.RemoteException;

/**
 * Menú para crear una nueva partida
 */
public class MenuCrearPartida extends JFrame {

    private Controller controlador;
    private JComboBox<String> comboVista;
    private JButton btnCrear;
    private JButton btnCancelar;

    public MenuCrearPartida(Controller controlador) {
        this.controlador = controlador;

        setTitle("Crear Nueva Partida");
        setSize(400, 280);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        // Panel central
        JPanel panelCentral = new JPanel(new GridBagLayout());
        panelCentral.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 10, 8, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Título
        JLabel titulo = new JLabel("Nueva Partida", SwingConstants.CENTER);
        titulo.setFont(new Font("Arial", Font.BOLD, 18));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        panelCentral.add(titulo, gbc);

        // Tu nombre
        gbc.gridwidth = 1;
        gbc.gridx = 0;
        gbc.gridy = 1;
        panelCentral.add(new JLabel("Tú (Rojo):"), gbc);

        gbc.gridx = 1;
        JLabel lblTuNombre = new JLabel(controlador.getNombreJugador() != null ?
                                        controlador.getNombreJugador() : "Jugador 1");
        lblTuNombre.setFont(new Font("Arial", Font.BOLD, 12));
        panelCentral.add(lblTuNombre, gbc);

        // Información
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        JLabel lblInfo = new JLabel("<html><center>Crearás un lobby público.<br>Otros jugadores podrán unirse.</center></html>");
        lblInfo.setFont(new Font("Arial", Font.ITALIC, 11));
        lblInfo.setForeground(new Color(100, 100, 100));
        panelCentral.add(lblInfo, gbc);

        // Selección de vista
        gbc.gridwidth = 1;
        gbc.gridx = 0;
        gbc.gridy = 3;
        panelCentral.add(new JLabel("Tu Vista:"), gbc);

        gbc.gridx = 1;
        comboVista = new JComboBox<>(new String[]{"Gráfica", "Consola"});
        panelCentral.add(comboVista, gbc);

        add(panelCentral, BorderLayout.CENTER);

        // Panel de botones
        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));

        btnCrear = new JButton("Crear Partida");
        btnCrear.setFont(new Font("Arial", Font.BOLD, 13));
        btnCrear.addActionListener(e -> crearPartida());
        panelBotones.add(btnCrear);

        btnCancelar = new JButton("Cancelar");
        btnCancelar.addActionListener(e -> dispose());
        panelBotones.add(btnCancelar);

        add(panelBotones, BorderLayout.SOUTH);
    }

    private void crearPartida() {
        try {
            System.out.println("[MenuCrearPartida-" + controlador.getNombreJugador() + "] Creando nuevo lobby...");

            // Crear lobby (partida esperando segundo jugador)
            // El nombre del segundo jugador será asignado cuando se una
            IPartida partida = controlador.crearPartida("Esperando jugador...");
            int idPartida = partida.getId();
            controlador.setIdPartidaActual(idPartida);
            controlador.setEsJugador1(true);

            System.out.println("[MenuCrearPartida-" + controlador.getNombreJugador() + "] Lobby #" + idPartida + " creado");

            // Cambiar estado a sala de espera
            controlador.getVista().setEstado(Estados.EN_ESPERANDO_JUGADORES);
            System.out.println("[MenuCrearPartida-" + controlador.getNombreJugador() + "] Estado cambiado a EN_ESPERANDO_JUGADORES");

            // Crear la ventana de juego pero NO mostrarla todavía
            String vista = (String) comboVista.getSelectedItem();
            String nombreJ1 = controlador.getNombreJugador();

            if ("Gráfica".equals(vista)) {
                VentanaPrincipal ventana = new VentanaPrincipal(nombreJ1, true, controlador);
                // Registrar en VistaGrafica
                if (controlador.getVista() instanceof view.vistas.VistaGrafica) {
                    ((view.vistas.VistaGrafica) controlador.getVista()).setVentanaPrincipal(ventana);
                }
                // NO hacer visible todavía - se mostrará cuando se una el segundo jugador
            } else {
                VentanaConsola consola = new VentanaConsola(nombreJ1, true, controlador);
                // Registrar en VistaGrafica
                if (controlador.getVista() instanceof view.vistas.VistaGrafica) {
                    ((view.vistas.VistaGrafica) controlador.getVista()).setVentanaConsola(consola);
                }
                // NO hacer visible todavía - se mostrará cuando se una el segundo jugador
            }

            // Mostrar sala de espera (ahora se ejecuta sincrónicamente)
            controlador.getVista().salaEspera();

            // Cerrar este menú después de que la sala de espera esté visible
            dispose();

        } catch (RemoteException e) {
            JOptionPane.showMessageDialog(this,
                "Error al crear lobby: " + e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }
}
