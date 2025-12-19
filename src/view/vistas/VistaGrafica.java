package view.vistas;

import controller.Controller;
import model.enums.Estados;
import model.interfaces.IJugador;
import model.interfaces.IPartida;
import view.frames.*;
import view.interfaces.IVista;
import view.interfaces.IVentanaJuego;
import javax.swing.*;
import java.awt.*;
import java.rmi.RemoteException;

/**
 * ============================================================================
 * VISTA GRÁFICA - GESTIÓN DE LA INTERFAZ DE USUARIO
 * ============================================================================
 *
 * La Vista es responsable de mostrar la interfaz gráfica al usuario y
 * gestionar las diferentes pantallas (ventanas) del juego.
 *
 * RESPONSABILIDADES:
 *
 * 1. GESTIÓN DE VENTANAS:
 *    - Crea y destruye ventanas según sea necesario
 *    - Mantiene referencias a las ventanas activas
 *    - Controla la visibilidad de cada ventana
 *
 * 2. GESTIÓN DE ESTADOS:
 *    - Mantiene el estado actual de la vista (EN_MENU, EN_JUEGO, etc.)
 *    - Cambia de estado según las acciones del usuario
 *    - El Controller usa el estado para filtrar eventos
 *
 * 3. ACTUALIZACIÓN DE INTERFAZ:
 *    - Recibe comandos del Controller para actualizar la UI
 *    - Muestra partidas, estados, ranking, etc.
 *    - Sincroniza con eventos del Modelo remoto
 *
 * CICLO DE VIDA DE UNA PARTIDA:
 *
 *   ┌─────────────────────────────────────────────────────────┐
 *   │                   FLUJO DE PANTALLAS                    │
 *   └─────────────────────────────────────────────────────────┘
 *
 *   [1] MenuPrincipal (estado: EN_MENU)
 *        - Usuario ve opciones: Buscar partida, Ver Ranking, Salir
 *        ↓
 *   [2] Buscar partida (matchmaking automático)
 *        - Si hay partida disponible → Emparejamiento automático
 *        - Si NO hay partida → Crear nueva y esperar
 *        - Muestra mensaje de espera si es necesario
 *        - Cuando hay 2 jugadores → VentanaPrincipal/VentanaConsola
 *        ↓
 *   [3] VentanaPrincipal + VentanaConsola (estado: EN_JUEGO)
 *        - Tablero de juego visible
 *        - Se actualiza con cada movimiento (eventos del Modelo)
 *        - Muestra turno, piezas, molinos, ganador
 *        ↓
 *   [4] Fin del juego
 *        - Muestra ganador
 *        - Puede volver al menú
 *
 * ESTADOS POSIBLES:
 *
 * - EN_MENU: Usuario en menú principal
 * - EN_ESPERANDO_JUGADORES: Usuario esperando emparejamiento
 * - EN_JUEGO: Usuario jugando una partida activa
 *
 * VENTANAS DISPONIBLES:
 *
 * - MenuPrincipal: Pantalla inicial con opciones
 * - VentanaPrincipal: Tablero gráfico del juego (Swing)
 * - VentanaConsola: Tablero por consola (alternativa texto)
 *
 * IMPORTANTE:
 * - Cada método público puede ser llamado por el Controller
 * - Los métodos usan SwingUtilities.invokeLater() para thread safety
 * - El estado se usa para filtrar eventos irrelevantes
 */
public class VistaGrafica implements IVista {
    // ===================================================================
    // COMPONENTES
    // ===================================================================

    /**
     * Controlador asociado a esta vista
     * Intermediario entre Vista y Modelo
     */
    private Controller controlador;

    /**
     * Estado actual de la vista
     * Usado para filtrar eventos y determinar qué mostrar
     */
    private Estados estado;

    /**
     * Tipo de vista preferida por el usuario
     * true = Gráfica (Swing), false = Consola (Texto)
     */
    private boolean usarVistaGrafica;

    // ===================================================================
    // VENTANAS (PANTALLAS)
    // ===================================================================
    // Solo se mantienen referencias a ventanas activas
    // Las ventanas se crean y destruyen según sea necesario

    /**
     * Menú principal - Primera pantalla que ve el usuario
     */
    private MenuPrincipal menuPrincipal;

    /**
     * Ventana del juego activa - Puede ser gráfica o consola
     * Usa polimorfismo mediante la interfaz IVentanaJuego
     */
    private IVentanaJuego ventanaJuego;

    /**
     * CONSTRUCTOR
     *
     * Inicializa la vista creando el Controller y estableciendo
     * el estado inicial.
     *
     * FLUJO:
     * 1. Se crea un nuevo Controller
     * 2. El Controller se asocia con esta Vista
     * 3. El estado inicial es EN_MENU
     */
    public VistaGrafica() throws RemoteException {
        this.controlador = new Controller();
        this.controlador.setVista(this);
        this.estado = Estados.EN_MENU;
    }


    /**
     * MOSTRAR MENÚ PRINCIPAL
     *
     * Muestra la pantalla de menú principal donde el usuario puede:
     * - Buscar partida (matchmaking automático)
     * - Ver el ranking
     *
     * IMPORTANTE: Usa SwingUtilities.invokeLater() para garantizar que
     * las operaciones de UI se ejecuten en el Event Dispatch Thread (EDT)
     */
    @Override
    public void menu() {
        // Cambiar estado de la vista
        this.estado = Estados.EN_MENU;

        // Ejecutar en el Event Dispatch Thread (thread-safe)
        SwingUtilities.invokeLater(() -> {
            // Crear menú si no existe (patrón lazy initialization)
            if (menuPrincipal == null) {
                menuPrincipal = new MenuPrincipal(controlador);
            }
            menuPrincipal.setVisible(true);
        });
    }

    /**
     * MOSTRAR PARTIDA EN JUEGO
     *
     * Actualiza y muestra las ventanas del juego activo.
     * Este método es llamado por el Controller cuando:
     * - Se inicia una nueva partida
     * - Ocurre un evento de juego (pieza colocada, turno cambiado, etc.)
     * - Se necesita refrescar la interfaz
     *
     * FLUJO:
     * 1. Cambia el estado a EN_JUEGO
     * 2. Actualiza las ventanas activas (VentanaPrincipal y/o VentanaConsola)
     * 3. Las ventanas consultan el Modelo para obtener el estado actual
     * 4. Renderiza el tablero, turno, piezas, etc.
     *
     * NOTA: Solo actualiza ventanas que ya existen (están activas)
     */
    @Override
    public void mostrarPartida() throws RemoteException {
        System.out.println("[VistaGrafica] mostrarPartida() llamado");

        // Cambiar estado de la vista
        this.estado = Estados.EN_JUEGO;

        // Ejecutar actualización en el Event Dispatch Thread
        SwingUtilities.invokeLater(() -> {
            try {
                // Crear ventana si no existe
                if (ventanaJuego == null) {
                    if (usarVistaGrafica) {
                        System.out.println("[VistaGrafica] Creando VentanaPrincipal...");
                        ventanaJuego = new VentanaPrincipal(
                            controlador.getNombreJugador(),
                            controlador.isEsJugador1(),
                            controlador
                        );
                    } else {
                        System.out.println("[VistaGrafica] Creando VentanaConsola...");
                        ventanaJuego = new VentanaConsola(
                            controlador.getNombreJugador(),
                            controlador.isEsJugador1(),
                            controlador
                        );
                    }
                }

                // Mostrar y actualizar ventana usando la interfaz común
                System.out.println("[VistaGrafica] Mostrando y actualizando ventana de juego");
                ventanaJuego.setVisible(true);
                ventanaJuego.actualizarInterfaz();

                // Ocultar menú si está visible
                if (menuPrincipal != null) {
                    menuPrincipal.setVisible(false);
                }
            } catch (Exception e) {
                System.err.println("[VistaGrafica] Error al mostrar partida: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }

    @Override
    public void mostrarGameOver() {
        SwingUtilities.invokeLater(() -> {
            try {
                IPartida partida = controlador.getPartidaActual();
                if (partida == null) return;

                String mensaje = "La partida ha finalizado.\n\n" +
                               "Volviendo al menú principal...";

                JOptionPane.showMessageDialog(
                    null,
                    mensaje,
                    "Fin del juego",
                    JOptionPane.INFORMATION_MESSAGE
                );

                volverAlMenu();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    @Override
    public void mostrarGameWin() {
        SwingUtilities.invokeLater(() -> {
            try {
                IPartida partida = controlador.getPartidaActual();
                if (partida == null) return;

                IJugador ganador = partida.getGanador();
                boolean ganaste = ganador.getNombre().equals(controlador.getNombreJugador());

                String mensaje;
                if (ganaste) {
                    mensaje = "🎉 ¡FELICIDADES! ¡HAS GANADO! 🎉\n\n" +
                            "Victoria registrada en el ranking.\n" +
                            "Volviendo al menú principal...";
                } else {
                    mensaje = "😔 HAS PERDIDO 😔\n\n" +
                            ganador.getNombre() + " ha ganado la partida.\n" +
                            "Volviendo al menú principal...";
                }

                JOptionPane.showMessageDialog(
                    null,
                    mensaje,
                    "Fin del juego",
                    JOptionPane.INFORMATION_MESSAGE
                );

                volverAlMenu();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    /**
     * VOLVER AL MENÚ PRINCIPAL
     *
     * Limpia el estado de la partida actual, cierra las ventanas de juego,
     * y muestra el menú principal.
     *
     * Este método se llama automáticamente cuando termina una partida.
     */
    private void volverAlMenu() {
        try {
            // Cerrar y limpiar ventana de juego
            if (ventanaJuego != null) {
                ventanaJuego.setVisible(false);
                ventanaJuego.dispose();
                ventanaJuego = null;
            }

            // Resetear estado del controlador
            controlador.setIdPartidaActual(-1);

            // Cambiar estado de la vista
            this.estado = Estados.EN_MENU;

            // Mostrar menú principal
            menu();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void buscarPartida() throws RemoteException {
        try {
            System.out.println("[VistaGrafica] Buscando partida...");

            // Buscar partida automáticamente (emparejamiento)
            IPartida partida = controlador.buscarPartida();

            System.out.println("[VistaGrafica] Partida obtenida ID: " + partida.getId() +
                             ", Jugadores: " + partida.getJugadores().size());

            // Verificar si la partida ya tiene 2 jugadores (emparejamiento inmediato)
            if (partida.getJugadores().size() == 2) {
                // Partida completa - iniciar juego inmediatamente
                System.out.println("[VistaGrafica] Partida completa (2 jugadores), iniciando juego");
                this.estado = Estados.EN_JUEGO;
                mostrarPartida();
            } else {
                // Solo hay 1 jugador - esperar al segundo
                // El evento CAMBIO_TURNO notificará cuando se una el segundo jugador
                System.out.println("[VistaGrafica] Esperando al segundo jugador...");
                System.out.println("[VistaGrafica] La partida comenzará automáticamente cuando se conecte otro jugador");
                this.estado = Estados.EN_ESPERANDO_JUGADORES;

                // Mostrar mensaje no modal usando un JDialog personalizado
                SwingUtilities.invokeLater(() -> {
                    final JDialog dialogEspera = new JDialog((JFrame) null, "Buscando oponente", false);
                    dialogEspera.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
                    dialogEspera.setSize(400, 150);
                    dialogEspera.setLocationRelativeTo(null);

                    JPanel panel = new JPanel(new BorderLayout(10, 10));
                    panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

                    JLabel label = new JLabel("<html><center>Esperando a que otro jugador se conecte...<br>" +
                                              "La partida comenzará automáticamente.</center></html>");
                    label.setHorizontalAlignment(SwingConstants.CENTER);
                    panel.add(label, BorderLayout.CENTER);

                    JProgressBar progressBar = new JProgressBar();
                    progressBar.setIndeterminate(true);
                    panel.add(progressBar, BorderLayout.SOUTH);

                    dialogEspera.add(panel);
                    dialogEspera.setVisible(true);

                    System.out.println("[VistaGrafica] Diálogo de espera mostrado (no modal)");

                    // Guardar referencia para poder cerrarlo cuando llegue el evento
                    // El evento CAMBIO_TURNO cerrará este diálogo automáticamente
                    Thread monitorThread = new Thread(() -> {
                        while (dialogEspera.isVisible() && estado == Estados.EN_ESPERANDO_JUGADORES) {
                            try {
                                Thread.sleep(500);
                            } catch (InterruptedException e) {
                                break;
                            }
                        }
                        SwingUtilities.invokeLater(() -> {
                            if (dialogEspera.isVisible()) {
                                dialogEspera.dispose();
                            }
                        });
                    });
                    monitorThread.setDaemon(true);
                    monitorThread.start();
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
            SwingUtilities.invokeLater(() -> {
                JOptionPane.showMessageDialog(null,
                    "Error al buscar partida: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            });
            throw new RemoteException("Error al buscar partida", e);
        }
    }

    @Override
    public Estados getEstado() {
        return estado;
    }

    @Override
    public void setEstado(Estados estado) {
        this.estado = estado;
    }

    @Override
    public Controller getControlador() {
        return controlador;
    }

    /**
     * Establece el tipo de vista preferida
     * @param usarVistaGrafica true para vista gráfica, false para vista de consola
     */
    public void setUsarVistaGrafica(boolean usarVistaGrafica) {
        this.usarVistaGrafica = usarVistaGrafica;
    }

    /**
     * SALIR DE LA PARTIDA MANUALMENTE
     *
     * Permite al usuario salir de la partida actual antes de que termine.
     * Muestra un diálogo de confirmación y vuelve al menú principal.
     */
    public void salirDePartida() {
        int opcion = JOptionPane.showConfirmDialog(
            null,
            "¿Estás seguro de que quieres salir de la partida?\n" +
            "Esta acción no se puede deshacer.",
            "Confirmar salida",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE
        );

        if (opcion == JOptionPane.YES_OPTION) {
            volverAlMenu();
        }
    }
}
