package vista;

import javax.swing.*;
import java.awt.*;

public class PanelControl extends JPanel {

    private JLabel labelTurno;
    private JButton botonReiniciar;

    public PanelControl() {
        setLayout(new BorderLayout(10, 10));
        setBackground(new Color(250, 250, 245));
        setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        inicializarComponentes();
    }

    private void inicializarComponentes() {
        // Panel de información del turno
        JPanel panelInfo = new JPanel(new BorderLayout());
        panelInfo.setOpaque(false);

        labelTurno = new JLabel("Turno: Jugador 1", SwingConstants.CENTER);
        labelTurno.setFont(new Font("Arial", Font.BOLD, 16));
        labelTurno.setForeground(new Color(60, 60, 60));
        labelTurno.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200), 2, true),
                BorderFactory.createEmptyBorder(10, 15, 10, 15)
        ));
        labelTurno.setBackground(Color.WHITE);
        labelTurno.setOpaque(true);

        panelInfo.add(labelTurno, BorderLayout.CENTER);

        // Botón de reiniciar
        botonReiniciar = new JButton("🔄 Nuevo Juego");
        botonReiniciar.setFont(new Font("Arial", Font.BOLD, 14));
        botonReiniciar.setFocusPainted(false);
        botonReiniciar.setBackground(new Color(70, 130, 180));
        botonReiniciar.setForeground(Color.WHITE);
        botonReiniciar.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        botonReiniciar.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Efecto hover
        botonReiniciar.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                botonReiniciar.setBackground(new Color(90, 150, 200));
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                botonReiniciar.setBackground(new Color(70, 130, 180));
            }
        });

        // Layout principal
        add(panelInfo, BorderLayout.CENTER);
        add(botonReiniciar, BorderLayout.EAST);
    }

    public void setTurnoTexto(String texto) {
        labelTurno.setText(texto);
    }

    public JButton getBotonReiniciar() {
        return botonReiniciar;
    }
}