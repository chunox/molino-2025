package view.frames;

import controller.Controller;
import model.enums.Estados;
import model.interfaces.IPartida;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.rmi.RemoteException;
import java.util.List;

/**
 * Ventana que muestra la lista de partidas disponibles
 */
public class ListaPartidas extends JFrame {

    private Controller controlador;
    private JTable tablaPartidas;
    private DefaultTableModel modeloTabla;
    private JButton btnActualizar;
    private JButton btnUnirse;
    private JButton btnCerrar;
    private JComboBox<String> comboVista;

    public ListaPartidas(Controller controlador) {
        this.controlador = controlador;

        setTitle("Partidas Disponibles");
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        // Panel superior
        JPanel panelSuperior = new JPanel(new BorderLayout());
        panelSuperior.setBorder(BorderFactory.createEmptyBorder(10, 10, 5, 10));

        JLabel titulo = new JLabel("Partidas Disponibles");
        titulo.setFont(new Font("Arial", Font.BOLD, 16));
        panelSuperior.add(titulo, BorderLayout.WEST);

        btnActualizar = new JButton("🔄 Actualizar");
        btnActualizar.addActionListener(e -> cargarPartidas());
        panelSuperior.add(btnActualizar, BorderLayout.EAST);

        add(panelSuperior, BorderLayout.NORTH);

        // Tabla de partidas
        String[] columnas = {"ID", "Jugador 1", "Estado", "Jugadores"};
        modeloTabla = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tablaPartidas = new JTable(modeloTabla);
        tablaPartidas.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tablaPartidas.setFont(new Font("Arial", Font.PLAIN, 12));
        tablaPartidas.setRowHeight(25);
        tablaPartidas.getColumnModel().getColumn(0).setPreferredWidth(50);
        tablaPartidas.getColumnModel().getColumn(1).setPreferredWidth(150);
        tablaPartidas.getColumnModel().getColumn(2).setPreferredWidth(120);
        tablaPartidas.getColumnModel().getColumn(3).setPreferredWidth(80);

        JScrollPane scrollPane = new JScrollPane(tablaPartidas);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
        add(scrollPane, BorderLayout.CENTER);

        // Panel inferior
        JPanel panelInferior = new JPanel(new BorderLayout(10, 0));
        panelInferior.setBorder(BorderFactory.createEmptyBorder(5, 10, 10, 10));

        // Panel de vista
        JPanel panelVista = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panelVista.add(new JLabel("Tu Vista:"));
        comboVista = new JComboBox<>(new String[]{"Gráfica", "Consola"});
        panelVista.add(comboVista);
        panelInferior.add(panelVista, BorderLayout.WEST);

        // Panel de botones
        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        btnUnirse = new JButton("Unirse a Partida");
        btnUnirse.setFont(new Font("Arial", Font.BOLD, 12));
        btnUnirse.addActionListener(e -> unirsePartida());
        panelBotones.add(btnUnirse);

        btnCerrar = new JButton("Cerrar");
        btnCerrar.addActionListener(e -> dispose());
        panelBotones.add(btnCerrar);

        panelInferior.add(panelBotones, BorderLayout.EAST);
        add(panelInferior, BorderLayout.SOUTH);

        // Cargar partidas al abrir
        cargarPartidas();
    }

    private void cargarPartidas() {
        try {
            List<IPartida> partidas = controlador.buscarPartidas();
            modeloTabla.setRowCount(0);

            if (partidas.isEmpty()) {
                // No hay partidas en absoluto
                Object[] fila = {
                    "-",
                    "No hay lobbys disponibles",
                    "Crea uno desde el menú",
                    "-"
                };
                modeloTabla.addRow(fila);
            } else {
                for (IPartida partida : partidas) {
                    // Solo mostrar partidas que esperan jugadores
                    if (partida.getJugadores().size() < 2) {
                        Object[] fila = {
                            partida.getId(),
                            partida.getJugadores().get(0).getNombre(),
                            "Esperando...",
                            partida.getJugadores().size() + "/2"
                        };
                        modeloTabla.addRow(fila);
                    }
                }

                // Si hay partidas pero todas están llenas
                if (modeloTabla.getRowCount() == 0) {
                    Object[] fila = {
                        "-",
                        "Todos los lobbys están llenos",
                        "Crea uno nuevo",
                        "-"
                    };
                    modeloTabla.addRow(fila);
                }
            }

        } catch (RemoteException e) {
            JOptionPane.showMessageDialog(this,
                "Error al cargar partidas: " + e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private void unirsePartida() {
        int filaSeleccionada = tablaPartidas.getSelectedRow();

        if (filaSeleccionada == -1) {
            JOptionPane.showMessageDialog(this,
                "Por favor selecciona una partida",
                "Aviso",
                JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            Object idObj = modeloTabla.getValueAt(filaSeleccionada, 0);

            // Verificar que sea una partida válida (no el mensaje de "No hay lobbys")
            if (idObj.equals("-")) {
                JOptionPane.showMessageDialog(this,
                    "No hay partidas disponibles para unirse",
                    "Aviso",
                    JOptionPane.WARNING_MESSAGE);
                return;
            }

            int idPartida = (int) idObj;

            System.out.println("[ListaPartidas-" + controlador.getNombreJugador() + "] Uniéndose a Lobby #" + idPartida);

            // Unirse a la partida
            controlador.unirseAPartida(idPartida);
            controlador.setEsJugador1(false);

            System.out.println("[ListaPartidas-" + controlador.getNombreJugador() + "] Unido exitosamente, cambiando estado a EN_JUEGO");

            // Cambiar estado a EN_JUEGO
            controlador.getVista().setEstado(Estados.EN_JUEGO);

            // Crear la ventana de juego
            String nombreJ2 = controlador.getNombreJugador();
            String vista = (String) comboVista.getSelectedItem();

            if ("Gráfica".equals(vista)) {
                VentanaPrincipal ventana = new VentanaPrincipal(nombreJ2, false, controlador);
                // Registrar en VistaGrafica
                if (controlador.getVista() instanceof view.vistas.VistaGrafica) {
                    ((view.vistas.VistaGrafica) controlador.getVista()).setVentanaPrincipal(ventana);
                }
                ventana.setVisible(true);
            } else {
                VentanaConsola consola = new VentanaConsola(nombreJ2, false, controlador);
                // Registrar en VistaGrafica
                if (controlador.getVista() instanceof view.vistas.VistaGrafica) {
                    ((view.vistas.VistaGrafica) controlador.getVista()).setVentanaConsola(consola);
                }
                consola.setVisible(true);
            }

            // Cerrar lista de partidas sin pop-up
            dispose();

        } catch (RemoteException e) {
            JOptionPane.showMessageDialog(this,
                "Error al unirse a la partida: " + e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }
}
