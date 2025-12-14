package view.frames;

import controller.Controller;
import model.interfaces.IJugador;
import model.interfaces.IPartida;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Map;

/**
 * Panel del tablero del juego adaptado para RMI
 */
public class PanelTablero extends JPanel {

    private Controller controlador;
    private Map<String, Point> posiciones;
    private Map<String, Circle> circulos;
    private String posicionSeleccionada;

    private static final int RADIO = 15;
    private static final Color COLOR_LINEA = Color.BLACK;
    private static final Color COLOR_VACIO = Color.WHITE;
    private static final Color COLOR_JUGADOR1 = new Color(255, 100, 100); // Rojo
    private static final Color COLOR_JUGADOR2 = new Color(100, 150, 255); // Azul
    private static final Color COLOR_SELECCION = new Color(100, 255, 100); // Verde

    public PanelTablero() {
        setPreferredSize(new Dimension(600, 600));
        setBackground(new Color(245, 222, 179)); // Beige

        posiciones = new HashMap<>();
        circulos = new HashMap<>();
        posicionSeleccionada = null;

        inicializarPosiciones();
        inicializarCirculos();

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                manejarClick(e.getPoint());
            }
        });
    }

    public void setControlador(Controller controlador) {
        this.controlador = controlador;
    }

    private void inicializarPosiciones() {
        int margen = 50;
        int ancho = 500;

        // Cuadrado exterior
        posiciones.put("A1", new Point(margen, margen));
        posiciones.put("D1", new Point(margen + ancho/2, margen));
        posiciones.put("G1", new Point(margen + ancho, margen));

        // Cuadrado medio
        int offset1 = ancho / 6;
        posiciones.put("B2", new Point(margen + offset1, margen + offset1));
        posiciones.put("D2", new Point(margen + ancho/2, margen + offset1));
        posiciones.put("F2", new Point(margen + ancho - offset1, margen + offset1));

        // Cuadrado interior
        int offset2 = ancho / 3;
        posiciones.put("C3", new Point(margen + offset2, margen + offset2));
        posiciones.put("D3", new Point(margen + ancho/2, margen + offset2));
        posiciones.put("E3", new Point(margen + ancho - offset2, margen + offset2));

        // Línea central
        posiciones.put("A4", new Point(margen, margen + ancho/2));
        posiciones.put("B4", new Point(margen + offset1, margen + ancho/2));
        posiciones.put("C4", new Point(margen + offset2, margen + ancho/2));
        posiciones.put("E4", new Point(margen + ancho - offset2, margen + ancho/2));
        posiciones.put("F4", new Point(margen + ancho - offset1, margen + ancho/2));
        posiciones.put("G4", new Point(margen + ancho, margen + ancho/2));

        // Cuadrado interior inferior
        posiciones.put("C5", new Point(margen + offset2, margen + ancho - offset2));
        posiciones.put("D5", new Point(margen + ancho/2, margen + ancho - offset2));
        posiciones.put("E5", new Point(margen + ancho - offset2, margen + ancho - offset2));

        // Cuadrado medio inferior
        posiciones.put("B6", new Point(margen + offset1, margen + ancho - offset1));
        posiciones.put("D6", new Point(margen + ancho/2, margen + ancho - offset1));
        posiciones.put("F6", new Point(margen + ancho - offset1, margen + ancho - offset1));

        // Cuadrado exterior inferior
        posiciones.put("A7", new Point(margen, margen + ancho));
        posiciones.put("D7", new Point(margen + ancho/2, margen + ancho));
        posiciones.put("G7", new Point(margen + ancho, margen + ancho));
    }

    private void inicializarCirculos() {
        for (Map.Entry<String, Point> entry : posiciones.entrySet()) {
            circulos.put(entry.getKey(), new Circle(entry.getValue(), COLOR_VACIO));
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        dibujarLineas(g2d);
        dibujarCirculos(g2d);
    }

    private void dibujarLineas(Graphics2D g2d) {
        g2d.setColor(COLOR_LINEA);
        g2d.setStroke(new BasicStroke(2));

        // Cuadrado exterior
        dibujarLinea(g2d, "A1", "D1");
        dibujarLinea(g2d, "D1", "G1");
        dibujarLinea(g2d, "G1", "G4");
        dibujarLinea(g2d, "G4", "G7");
        dibujarLinea(g2d, "G7", "D7");
        dibujarLinea(g2d, "D7", "A7");
        dibujarLinea(g2d, "A7", "A4");
        dibujarLinea(g2d, "A4", "A1");

        // Cuadrado medio
        dibujarLinea(g2d, "B2", "D2");
        dibujarLinea(g2d, "D2", "F2");
        dibujarLinea(g2d, "F2", "F4");
        dibujarLinea(g2d, "F4", "F6");
        dibujarLinea(g2d, "F6", "D6");
        dibujarLinea(g2d, "D6", "B6");
        dibujarLinea(g2d, "B6", "B4");
        dibujarLinea(g2d, "B4", "B2");

        // Cuadrado interior
        dibujarLinea(g2d, "C3", "D3");
        dibujarLinea(g2d, "D3", "E3");
        dibujarLinea(g2d, "E3", "E4");
        dibujarLinea(g2d, "E4", "E5");
        dibujarLinea(g2d, "E5", "D5");
        dibujarLinea(g2d, "D5", "C5");
        dibujarLinea(g2d, "C5", "C4");
        dibujarLinea(g2d, "C4", "C3");

        // Líneas conectoras
        dibujarLinea(g2d, "D1", "D2");
        dibujarLinea(g2d, "D2", "D3");
        dibujarLinea(g2d, "D7", "D6");
        dibujarLinea(g2d, "D6", "D5");
        dibujarLinea(g2d, "A4", "B4");
        dibujarLinea(g2d, "B4", "C4");
        dibujarLinea(g2d, "G4", "F4");
        dibujarLinea(g2d, "F4", "E4");
    }

    private void dibujarLinea(Graphics2D g2d, String pos1, String pos2) {
        Point p1 = posiciones.get(pos1);
        Point p2 = posiciones.get(pos2);
        g2d.drawLine(p1.x, p1.y, p2.x, p2.y);
    }

    private void dibujarCirculos(Graphics2D g2d) {
        for (Map.Entry<String, Circle> entry : circulos.entrySet()) {
            Circle c = entry.getValue();
            Point p = c.posicion;

            // Dibujar círculo
            g2d.setColor(c.color);
            g2d.fillOval(p.x - RADIO, p.y - RADIO, RADIO * 2, RADIO * 2);

            // Borde
            g2d.setColor(Color.BLACK);
            g2d.setStroke(new BasicStroke(2));
            g2d.drawOval(p.x - RADIO, p.y - RADIO, RADIO * 2, RADIO * 2);

            // Si está seleccionado, dibujar borde verde
            if (entry.getKey().equals(posicionSeleccionada)) {
                g2d.setColor(COLOR_SELECCION);
                g2d.setStroke(new BasicStroke(3));
                g2d.drawOval(p.x - RADIO - 3, p.y - RADIO - 3, RADIO * 2 + 6, RADIO * 2 + 6);
            }
        }
    }

    private void manejarClick(Point punto) {
        String posicion = encontrarPosicionCercana(punto);
        if (posicion != null && controlador != null) {
            // Llamar al método del controlador principal que maneja la lógica
            notificarClickPosicion(posicion);
        }
    }

    private void notificarClickPosicion(String posicion) {
        // Este método será llamado desde VentanaPrincipal
        // que tiene acceso a la lógica completa del juego
    }

    private String encontrarPosicionCercana(Point punto) {
        double distanciaMin = RADIO + 5;
        String posicionCercana = null;

        for (Map.Entry<String, Point> entry : posiciones.entrySet()) {
            Point p = entry.getValue();
            double distancia = punto.distance(p);
            if (distancia < distanciaMin) {
                distanciaMin = distancia;
                posicionCercana = entry.getKey();
            }
        }

        return posicionCercana;
    }

    public void actualizarTablero(Map<String, IJugador> estadoTablero, IJugador j1, IJugador j2) {
        for (Map.Entry<String, Circle> entry : circulos.entrySet()) {
            String posId = entry.getKey();
            Circle circle = entry.getValue();

            IJugador ocupante = estadoTablero.get(posId);

            if (ocupante == null) {
                circle.color = COLOR_VACIO;
            } else if (ocupante.equals(j1)) {
                circle.color = COLOR_JUGADOR1;
            } else {
                circle.color = COLOR_JUGADOR2;
            }
        }
        repaint();
    }

    public void setPosicionSeleccionada(String posicion) {
        this.posicionSeleccionada = posicion;
        repaint();
    }

    // Clase interna para representar un círculo
    private static class Circle {
        Point posicion;
        Color color;

        Circle(Point posicion, Color color) {
            this.posicion = posicion;
            this.color = color;
        }
    }

    // Método público para que VentanaPrincipal pueda registrar el listener
    public void setClickListener(ClickListener listener) {
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                String pos = encontrarPosicionCercana(e.getPoint());
                if (pos != null) {
                    listener.onPosicionClick(pos);
                }
            }
        });
    }

    public interface ClickListener {
        void onPosicionClick(String posicion);
    }
}
