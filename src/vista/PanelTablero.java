package vista;

import controlador.ControladorJuego;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.LinkedHashMap;
import java.util.Map;

public class PanelTablero extends JPanel {

    private controlador.ControladorJuego controlador;
    private String posicionSeleccionada;
    private Map<String, Point> posicionesGraficas;

    private final int RADIO_POSICION = 18;
    private final int RADIO_PIEZA = 14;
    private final int MARGEN = 60;

    public PanelTablero() {
        posicionesGraficas = new LinkedHashMap<>();
        setPreferredSize(new Dimension(700, 700));
        setBackground(new Color(245, 245, 240));

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                String pos = detectarPosicionClic(e.getPoint());
                if (pos != null && controlador != null) {
                    controlador.manejarClicEnPosicion(pos);
                }
            }
        });
    }

    private void calcularPosiciones() {
        posicionesGraficas.clear();

        int width = getWidth();
        int height = getHeight();
        int size = Math.min(width, height) - 2 * MARGEN;

        // Coordenadas base para los tres cuadrados concéntricos
        int[] offsets = {0, size / 6, size / 3}; // Exterior, medio, interior

        // Ancho de cada cuadrado
        int[] tamaños = {size, size * 2 / 3, size / 3};

        // Cuadrado exterior (índice 0)
        agregarPosicionesCuadrado("A1", "D1", "G1", "A4", "G4", "A7", "D7", "G7",
                MARGEN, size, 0);

        // Cuadrado medio (índice 1)
        agregarPosicionesCuadrado("B2", "D2", "F2", "B4", "F4", "B6", "D6", "F6",
                MARGEN + size / 6, size * 2 / 3, 1);

        // Cuadrado interior (índice 2)
        agregarPosicionesCuadrado("C3", "D3", "E3", "C4", "E4", "C5", "D5", "E5",
                MARGEN + size / 3, size / 3, 2);
    }

    private void agregarPosicionesCuadrado(String esqSupIzq, String centroSup, String esqSupDer,
                                           String centroIzq, String centroDer,
                                           String esqInfIzq, String centroInf, String esqInfDer,
                                           int offset, int tamaño, int nivel) {
        // Esquina superior izquierda
        posicionesGraficas.put(esqSupIzq, new Point(offset, offset));

        // Centro superior
        posicionesGraficas.put(centroSup, new Point(offset + tamaño / 2, offset));

        // Esquina superior derecha
        posicionesGraficas.put(esqSupDer, new Point(offset + tamaño, offset));

        // Centro izquierdo
        posicionesGraficas.put(centroIzq, new Point(offset, offset + tamaño / 2));

        // Centro derecho
        posicionesGraficas.put(centroDer, new Point(offset + tamaño, offset + tamaño / 2));

        // Esquina inferior izquierda
        posicionesGraficas.put(esqInfIzq, new Point(offset, offset + tamaño));

        // Centro inferior
        posicionesGraficas.put(centroInf, new Point(offset + tamaño / 2, offset + tamaño));

        // Esquina inferior derecha
        posicionesGraficas.put(esqInfDer, new Point(offset + tamaño, offset + tamaño));
    }

    public void setControlador(controlador.ControladorJuego controlador) {
        this.controlador = controlador;
    }

    public void setPosicionSeleccionada(String pos) {
        this.posicionSeleccionada = pos;
        repaint();
    }

    private String detectarPosicionClic(Point click) {
        for (Map.Entry<String, Point> entry : posicionesGraficas.entrySet()) {
            if (click.distance(entry.getValue()) <= RADIO_POSICION + 5) {
                return entry.getKey();
            }
        }
        return null;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        calcularPosiciones();

        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

        int width = getWidth();
        int height = getHeight();
        int size = Math.min(width, height) - 2 * MARGEN;

        // Coordenadas de los tres cuadrados
        int outer = MARGEN;
        int middle = MARGEN + size / 6;
        int inner = MARGEN + size / 3;

        int sizeOuter = size;
        int sizeMiddle = size * 2 / 3;
        int sizeInner = size / 3;

        g2.setColor(new Color(60, 60, 60));
        g2.setStroke(new BasicStroke(2.5f));

        // Dibujar los tres cuadrados concéntricos
        g2.drawRect(outer, outer, sizeOuter, sizeOuter);
        g2.drawRect(middle, middle, sizeMiddle, sizeMiddle);
        g2.drawRect(inner, inner, sizeInner, sizeInner);

        // Líneas que conectan los cuadrados (horizontales y verticales)
        int centerX = MARGEN + size / 2;
        int centerY = MARGEN + size / 2;

        // Línea vertical superior
        g2.drawLine(centerX, outer, centerX, inner);

        // Línea vertical inferior
        g2.drawLine(centerX, inner + sizeInner, centerX, outer + sizeOuter);

        // Línea horizontal izquierda
        g2.drawLine(outer, centerY, inner, centerY);

        // Línea horizontal derecha
        g2.drawLine(inner + sizeInner, centerY, outer + sizeOuter, centerY);

        // Dibujar las posiciones disponibles
        g2.setStroke(new BasicStroke(2));
        for (Map.Entry<String, Point> entry : posicionesGraficas.entrySet()) {
            Point p = entry.getValue();

            // Círculo blanco con borde
            g2.setColor(Color.WHITE);
            g2.fillOval(p.x - RADIO_POSICION, p.y - RADIO_POSICION,
                    RADIO_POSICION * 2, RADIO_POSICION * 2);

            g2.setColor(new Color(100, 100, 100));
            g2.drawOval(p.x - RADIO_POSICION, p.y - RADIO_POSICION,
                    RADIO_POSICION * 2, RADIO_POSICION * 2);
        }

        // Dibujar las piezas
        if (controlador != null) {
            Map<String, Character> piezas = controlador.getPiezasGraficas();
            for (Map.Entry<String, Character> entry : piezas.entrySet()) {
                Point p = posicionesGraficas.get(entry.getKey());
                if (p == null) continue;

                // Color de la pieza
                Color colorPieza = entry.getValue() == 'X' ?
                        new Color(220, 50, 50) : new Color(50, 100, 200);

                // Sombra sutil
                g2.setColor(new Color(0, 0, 0, 30));
                g2.fillOval(p.x - RADIO_PIEZA + 2, p.y - RADIO_PIEZA + 2,
                        RADIO_PIEZA * 2, RADIO_PIEZA * 2);

                // Pieza principal
                g2.setColor(colorPieza);
                g2.fillOval(p.x - RADIO_PIEZA, p.y - RADIO_PIEZA,
                        RADIO_PIEZA * 2, RADIO_PIEZA * 2);

                // Brillo
                g2.setColor(new Color(255, 255, 255, 100));
                g2.fillOval(p.x - RADIO_PIEZA / 2, p.y - RADIO_PIEZA / 2,
                        RADIO_PIEZA, RADIO_PIEZA);

                // Borde
                g2.setColor(new Color(0, 0, 0, 150));
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawOval(p.x - RADIO_PIEZA, p.y - RADIO_PIEZA,
                        RADIO_PIEZA * 2, RADIO_PIEZA * 2);
            }
        }

        // Resaltar posición seleccionada
        if (posicionSeleccionada != null && posicionesGraficas.containsKey(posicionSeleccionada)) {
            Point p = posicionesGraficas.get(posicionSeleccionada);
            g2.setColor(new Color(100, 200, 100, 200));
            g2.setStroke(new BasicStroke(3.5f));
            g2.drawOval(p.x - RADIO_POSICION - 3, p.y - RADIO_POSICION - 3,
                    (RADIO_POSICION + 3) * 2, (RADIO_POSICION + 3) * 2);
        }
    }
}