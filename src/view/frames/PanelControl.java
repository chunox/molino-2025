package view.frames;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;

/**
 * Panel de control con información del juego
 * Muestra el estado actual del turno, fase y piezas
 */
public class PanelControl extends JPanel {

    private JLabel lblTurno;
    private JButton btnSalir;

    public PanelControl() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        setBackground(new Color(240, 240, 240));

        // Panel de información
        JPanel panelInfo = new JPanel(new GridLayout(1, 1));
        panelInfo.setBackground(new Color(240, 240, 240));

        lblTurno = new JLabel("Cargando...", SwingConstants.CENTER);
        lblTurno.setFont(new Font("Arial", Font.BOLD, 16));
        panelInfo.add(lblTurno);

        add(panelInfo, BorderLayout.CENTER);

        // Botón para salir de la partida
        btnSalir = new JButton("Salir de la partida");
        btnSalir.setFont(new Font("Arial", Font.PLAIN, 12));
        btnSalir.setPreferredSize(new Dimension(150, 30));
        add(btnSalir, BorderLayout.EAST);
    }

    public void setTurnoTexto(String texto) {
        lblTurno.setText(texto);
    }

    /**
     * Establece el listener para el botón de salir
     * @param listener Listener que se ejecutará al presionar el botón
     */
    public void setSalirListener(ActionListener listener) {
        btnSalir.addActionListener(listener);
    }
}
