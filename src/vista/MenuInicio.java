package vista;

import modelo.GestorPartidas;
import controlador.ControladorJuego;

import javax.swing.*;
import java.awt.*;

public class MenuInicio extends JFrame {

    private JTextField campoJugador1;
    private JTextField campoJugador2;
    private JComboBox<String> comboVistaJ1;
    private JComboBox<String> comboVistaJ2;
    private JButton botonNuevaPartida;

    public MenuInicio() {
        setTitle("Juego del Molino - Menú");
        setSize(450, 320);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        JPanel panelCentral = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 10, 8, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Título
        JLabel titulo = new JLabel("Nueva Partida", SwingConstants.CENTER);
        titulo.setFont(new Font("Arial", Font.BOLD, 20));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        panelCentral.add(titulo, gbc);

        // Jugador 1
        gbc.gridwidth = 1;
        gbc.gridx = 0;
        gbc.gridy = 1;
        panelCentral.add(new JLabel("Jugador 1 (Rojo):"), gbc);

        gbc.gridx = 1;
        campoJugador1 = new JTextField(15);
        campoJugador1.setText("Jugador 1");
        panelCentral.add(campoJugador1, gbc);

        // Vista Jugador 1
        gbc.gridx = 0;
        gbc.gridy = 2;
        panelCentral.add(new JLabel("Vista J1:"), gbc);

        gbc.gridx = 1;
        comboVistaJ1 = new JComboBox<>(new String[]{"Gráfica", "Consola"});
        panelCentral.add(comboVistaJ1, gbc);

        // Jugador 2
        gbc.gridx = 0;
        gbc.gridy = 3;
        panelCentral.add(new JLabel("Jugador 2 (Azul):"), gbc);

        gbc.gridx = 1;
        campoJugador2 = new JTextField(15);
        campoJugador2.setText("Jugador 2");
        panelCentral.add(campoJugador2, gbc);

        // Vista Jugador 2
        gbc.gridx = 0;
        gbc.gridy = 4;
        panelCentral.add(new JLabel("Vista J2:"), gbc);

        gbc.gridx = 1;
        comboVistaJ2 = new JComboBox<>(new String[]{"Gráfica", "Consola"});
        panelCentral.add(comboVistaJ2, gbc);

        // Botón
        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.gridwidth = 2;
        botonNuevaPartida = new JButton("Iniciar Partida");
        botonNuevaPartida.setFont(new Font("Arial", Font.BOLD, 14));
        botonNuevaPartida.addActionListener(e -> crearNuevaPartida());
        panelCentral.add(botonNuevaPartida, gbc);

        add(panelCentral, BorderLayout.CENTER);
        setVisible(true);
    }

    private void crearNuevaPartida() {
        String nombreJ1 = campoJugador1.getText().trim();
        String nombreJ2 = campoJugador2.getText().trim();

        if (nombreJ1.isEmpty() || nombreJ2.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Por favor ingresa ambos nombres", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Crear partida en el gestor
        int idPartida = GestorPartidas.getInstancia().crearPartida(nombreJ1, nombreJ2);

        // Crear vista para Jugador 1
        String vistaJ1 = (String) comboVistaJ1.getSelectedItem();
        if ("Gráfica".equals(vistaJ1)) {
            VentanaPrincipal ventanaJ1 = new VentanaPrincipal(nombreJ1, true, idPartida);
            new ControladorJuego(ventanaJ1, idPartida, true);
        } else {
            new VentanaConsola(nombreJ1, true, idPartida);
        }

        // Crear vista para Jugador 2
        String vistaJ2 = (String) comboVistaJ2.getSelectedItem();
        if ("Gráfica".equals(vistaJ2)) {
            VentanaPrincipal ventanaJ2 = new VentanaPrincipal(nombreJ2, false, idPartida);
            new ControladorJuego(ventanaJ2, idPartida, false);
        } else {
            new VentanaConsola(nombreJ2, false, idPartida);
        }

        // Limpiar campos
        campoJugador1.setText("Jugador 1");
        campoJugador2.setText("Jugador 2");
        comboVistaJ1.setSelectedIndex(0);
        comboVistaJ2.setSelectedIndex(0);
    }
}
