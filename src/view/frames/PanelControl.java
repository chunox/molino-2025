package view.frames;

import javax.swing.*;
import java.awt.*;

/**
 * Panel de control con información del juego
 * Muestra el estado actual del turno, fase y piezas
 */
public class PanelControl extends JPanel {

    private JLabel lblTurno;

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
    }

    public void setTurnoTexto(String texto) {
        lblTurno.setText(texto);
    }
}
