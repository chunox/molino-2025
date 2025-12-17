package view.vistas;

import controller.Controller;
import model.enums.Estados;
import view.frames.*;
import view.interfaces.IVista;
import javax.swing.*;
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
 *        - Usuario ve opciones: Crear partida, Unirse, Ranking
 *        ↓
 *   [2a] Si crea partida → SalaEspera (estado: EN_ESPERANDO_JUGADORES)
 *        - Muestra mensaje "Esperando jugador 2..."
 *        - Timer verifica cada segundo si llegó el segundo jugador
 *        - Cuando llega el segundo jugador → VentanaPrincipal/VentanaConsola
 *        ↓
 *   [2b] Si se une → ListaPartidas (estado: EN_BUSCAR_PARTIDA)
 *        - Muestra lobbys disponibles
 *        - Usuario selecciona una partida
 *        - Automáticamente → VentanaPrincipal/VentanaConsola
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
 * - EN_BUSCAR_PARTIDA: Usuario viendo lista de lobbys
 * - EN_ESPERANDO_JUGADORES: Usuario esperando que llegue segundo jugador
 * - EN_JUEGO: Usuario jugando una partida activa
 *
 * VENTANAS DISPONIBLES:
 *
 * - MenuPrincipal: Pantalla inicial con opciones
 * - MenuCrearPartida: (no se usa directamente en esta clase)
 * - ListaPartidas: Lista de lobbys disponibles para unirse
 * - SalaEspera: Pantalla de espera para el jugador 1
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
     * Sala de espera - Donde el jugador 1 espera al jugador 2
     */
    private SalaEspera salaEspera;

    /**
     * Lista de partidas - Muestra lobbys disponibles
     */
    private ListaPartidas listaPartidas;

    /**
     * Ventana principal del juego - Tablero gráfico
     */
    private VentanaPrincipal ventanaPrincipal;

    /**
     * Ventana de consola - Tablero por texto
     */
    private VentanaConsola ventanaConsola;

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
     * - Crear una nueva partida
     * - Unirse a una partida existente
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
        // Cambiar estado de la vista
        this.estado = Estados.EN_JUEGO;

        // Ejecutar actualización en el Event Dispatch Thread
        SwingUtilities.invokeLater(() -> {
            // Actualizar ventana gráfica si existe
            if (ventanaPrincipal != null) {
                ventanaPrincipal.setVisible(true);
                ventanaPrincipal.actualizarInterfaz();  // Redibuja tablero, turno, etc.
            }

            // Actualizar ventana de consola si existe
            if (ventanaConsola != null) {
                ventanaConsola.setVisible(true);
                ventanaConsola.onActualizacionJuego();  // Actualiza texto del juego
            }
        });
    }

    @Override
    public void mostrarGameOver() {
        System.out.println("Game Over");
    }

    @Override
    public void mostrarGameWin() throws RemoteException {
        System.out.println("Victoria");
    }

    @Override
    public void buscarPartidas() throws RemoteException {
        this.estado = Estados.EN_BUSCAR_PARTIDA;
        SwingUtilities.invokeLater(() -> {
            if (listaPartidas != null) {
                listaPartidas.dispose();
            }
            listaPartidas = new ListaPartidas(controlador);
            listaPartidas.setVisible(true);
        });
    }

    @Override
    public void salaEspera() throws RemoteException {
        this.estado = Estados.EN_ESPERANDO_JUGADORES;

        // Ejecutar sincrónicamente si ya estamos en el EDT, o esperar si no lo estamos
        if (SwingUtilities.isEventDispatchThread()) {
            if (salaEspera != null) {
                salaEspera.dispose();
            }
            salaEspera = new SalaEspera(controlador);
            salaEspera.setVisible(true);
        } else {
            try {
                SwingUtilities.invokeAndWait(() -> {
                    if (salaEspera != null) {
                        salaEspera.dispose();
                    }
                    salaEspera = new SalaEspera(controlador);
                    salaEspera.setVisible(true);
                });
            } catch (Exception e) {
                throw new RemoteException("Error al crear sala de espera", e);
            }
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

    // Métodos para registrar ventanas activas
    public void setVentanaPrincipal(VentanaPrincipal ventana) {
        this.ventanaPrincipal = ventana;
    }

    public void setVentanaConsola(VentanaConsola ventana) {
        this.ventanaConsola = ventana;
    }
}
