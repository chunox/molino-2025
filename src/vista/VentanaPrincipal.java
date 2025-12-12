package vista;

import javax.swing.*;
import java.awt.*;

public class VentanaPrincipal extends JFrame {

    private PanelTablero panelTablero;
    private PanelControl panelControl;
    private String nombreJugador;
    private boolean esJugador1;
    private int idPartida;

    public VentanaPrincipal(String nombreJugador, boolean esJugador1, int idPartida) {
        this.nombreJugador = nombreJugador;
        this.esJugador1 = esJugador1;
        this.idPartida = idPartida;

        String colorJugador = esJugador1 ? "Rojo" : "Azul";
        setTitle("Juego del Molino - " + nombreJugador + " (" + colorJugador + ") - Partida #" + idPartida);
        setSize(600, 800);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        panelTablero = new PanelTablero();
        panelControl = new PanelControl();

        setLayout(new BorderLayout());
        add(panelTablero, BorderLayout.CENTER);
        add(panelControl, BorderLayout.SOUTH);

        setVisible(true);
    }

    public PanelTablero getPanelTablero() {
        return panelTablero;
    }

    public PanelControl getPanelControl() {
        return panelControl;
    }

    public String getNombreJugador() {
        return nombreJugador;
    }

    public boolean isEsJugador1() {
        return esJugador1;
    }

    public int getIdPartida() {
        return idPartida;
    }
}
