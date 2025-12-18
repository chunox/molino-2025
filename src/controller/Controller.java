package controller;

import ar.edu.unlu.rmimvc.observer.IObservableRemoto;
import ar.edu.unlu.rmimvc.cliente.IControladorRemoto;
import model.clases.ManejadorEventos;
import model.enums.Estados;
import model.enums.Eventos;
import model.interfaces.IModelo;
import model.interfaces.IPartida;
import model.interfaces.IJugador;
import view.interfaces.IVista;
import java.rmi.RemoteException;
import java.util.List;

/**
 * ============================================================================
 * CONTROLADOR - INTERMEDIARIO ENTRE VISTA Y MODELO
 * ============================================================================
 *
 * El Controller es el componente central que conecta la Vista (interfaz gráfica)
 * con el Modelo (lógica de negocio remota).
 *
 * RESPONSABILIDADES:
 *
 * 1. INTERMEDIARIO (Patrón MVC):
 *    - Recibe acciones del usuario desde la Vista
 *    - Traduce esas acciones en llamadas al Modelo
 *    - Actualiza la Vista cuando el Modelo cambia
 *
 * 2. OBSERVADOR REMOTO (Patrón Observer Distribuido):
 *    - Se suscribe al Modelo como observador
 *    - Recibe notificaciones cuando el Modelo cambia
 *    - Las notificaciones llegan vía RMI (callbacks remotos)
 *
 * 3. GESTOR DE ESTADO LOCAL:
 *    - Mantiene el estado local del cliente (partida actual, nombre jugador)
 *    - Filtra eventos que no son relevantes para este cliente
 *
 * FLUJO DE DATOS BIDIRECCIONAL:
 *
 * ┌─────────────────────────────────────────────────────────────┐
 * │                      FLUJO COMPLETO                         │
 * └─────────────────────────────────────────────────────────────┘
 *
 * USUARIO → SERVIDOR (Acciones):
 *
 *   Usuario hace clic en "Colocar Pieza"
 *        ↓
 *   Vista.colocarPieza() detecta el click
 *        ↓
 *   Vista llama a Controller.colocarPieza(posicion)
 *        ↓
 *   Controller llama a Modelo.colocarPieza() [RMI - va al servidor]
 *        ↓
 *   Modelo actualiza el estado del juego en el servidor
 *        ↓
 *   Modelo.notificarObservadores() envía evento a TODOS los clientes
 *
 * SERVIDOR → USUARIO (Notificaciones):
 *
 *   Modelo detecta cambio (pieza colocada, turno cambiado, etc.)
 *        ↓
 *   Modelo.notificarObservadores(evento) [RMI - va a todos los clientes]
 *        ↓
 *   Controller.actualizar(evento) recibe la notificación [RMI callback]
 *        ↓
 *   Controller filtra el evento (¿es de mi partida? ¿estoy en el estado correcto?)
 *        ↓
 *   Controller llama a Vista.mostrarPartida() o Vista.metodoCorrespondiente()
 *        ↓
 *   Vista actualiza la interfaz gráfica (tablero, turno, etc.)
 *        ↓
 *   Usuario ve el cambio en pantalla
 *
 * TIPOS DE EVENTOS MANEJADOS:
 *
 * - CAMBIO_TURNO: Cambió el turno del juego
 * - PIEZA_COLOCADA: Un jugador colocó una pieza
 * - PIEZA_MOVIDA: Un jugador movió una pieza
 * - PIEZA_ELIMINADA: Un jugador eliminó una pieza del oponente
 * - FORMACION_MOLINO: Se formó un molino (3 en línea)
 * - GAME_WIN: La partida terminó con un ganador
 * - GAME_OVER: La partida terminó
 *
 * FILTRADO DE EVENTOS:
 *
 * El Controller filtra eventos por:
 * 1. ID de partida: Solo procesa eventos de la partida en la que está
 * 2. Estado de la vista: Solo actualiza si la vista está en el estado correcto
 *
 * Ejemplo: Si llega un evento CAMBIO_TURNO pero la vista está EN_MENU,
 *          el evento se ignora porque el usuario no está viendo el juego.
 */
public class Controller implements IControladorRemoto {
    private static final long serialVersionUID = 1L;

    // ===================================================================
    // ATRIBUTOS
    // ===================================================================

    /**
     * Referencia remota al Modelo (en el servidor)
     * Todas las llamadas a modelo son llamadas RMI
     */
    private IModelo modelo;

    /**
     * Referencia local a la Vista (interfaz gráfica)
     */
    private IVista vista;

    /**
     * ID de la partida en la que está jugando este cliente
     * -1 indica que no está en ninguna partida
     */
    private int idPartidaActual;

    /**
     * Nombre del jugador de este cliente
     */
    private String nombreJugador;

    /**
     * Indica si este cliente es el jugador 1 (creador) o jugador 2 (se unió)
     */
    private boolean esJugador1;

    public Controller() throws RemoteException {
        this.idPartidaActual = -1;
    }

    public void setVista(IVista vista) {
        this.vista = vista;
    }

    public IVista getVista() {
        return vista;
    }

    @Override
    public <T extends IObservableRemoto> void setModeloRemoto(T modelo) throws RemoteException {
        this.modelo = (IModelo) modelo;
    }

    // Métodos de gestión de partidas

    public IPartida buscarPartida() throws RemoteException {
        IPartida partida = modelo.buscarPartida(nombreJugador);
        this.idPartidaActual = partida.getId();

        // Determinar si es jugador 1 o 2 según el símbolo asignado
        for (IJugador j : partida.getJugadores()) {
            if (j.getNombre().equals(nombreJugador)) {
                this.esJugador1 = (j.getSimbolo() == 'X');
                break;
            }
        }

        return partida;
    }

    public IPartida getPartidaActual() throws RemoteException {
        if (idPartidaActual == -1) {
            return null;
        }
        return modelo.getPartida(idPartidaActual);
    }

    // Métodos del juego

    public void colocarPieza(String posicion) throws RemoteException {
        if (idPartidaActual != -1) {
            modelo.colocarPieza(idPartidaActual, posicion);
        }
    }

    public void moverPieza(String origen, String destino) throws RemoteException {
        if (idPartidaActual != -1) {
            modelo.moverPieza(idPartidaActual, origen, destino);
        }
    }

    public void eliminarPiezaOponente(String posicion) throws RemoteException {
        if (idPartidaActual != -1) {
            modelo.eliminarPiezaOponente(idPartidaActual, posicion);
        }
    }

    // Métodos de ranking

    public java.util.Map<String, Integer> getRanking() throws RemoteException {
        return modelo.getRanking();
    }

    // ===================================================================
    // PATRÓN OBSERVER - MÉTODO DE CALLBACK
    // ===================================================================

    /**
     * MÉTODO CENTRAL DEL PATRÓN OBSERVER
     *
     * Este método es llamado REMOTAMENTE por el Modelo cuando ocurre un cambio.
     * Es un callback RMI que permite al servidor notificar a los clientes.
     *
     * FLUJO:
     * 1. Modelo cambia (ej: un jugador colocó una pieza)
     * 2. Modelo.notificarObservadores(evento) invoca este método en TODOS
     *    los controladores suscritos (todos los clientes conectados)
     * 3. Este método recibe el evento vía RMI
     * 4. Filtra si el evento es relevante para este cliente
     * 5. Si es relevante, actualiza la Vista correspondiente
     *
     * FILTRADO DE EVENTOS:
     * - Por ID de partida: Ignora eventos de partidas donde no está jugando
     * - Por estado de vista: Solo actualiza si la vista está en el estado correcto
     *
     * @param observable El objeto observado (el Modelo en el servidor)
     * @param evento El evento que ocurrió (encapsulado en ManejadorEventos)
     */
    @Override
    public void actualizar(IObservableRemoto observable, Object evento) throws RemoteException {
        if (evento instanceof ManejadorEventos e) {
            System.out.println("[Controller-" + nombreJugador + "] Evento recibido: " + e.getEvento() +
                             " de Partida #" + e.getId());

            // ===============================================================
            // FILTRO 1: VERIFICAR SI EL EVENTO ES DE NUESTRA PARTIDA
            // ===============================================================
            // Cada cliente solo debe reaccionar a eventos de la partida
            // en la que está jugando. Los eventos de otras partidas se ignoran.
            if (idPartidaActual != -1 && idPartidaActual != e.getId()) {
                System.out.println("[Controller-" + nombreJugador + "] Ignorando (no es mi partida)");
                return; // Ignorar evento de otra partida
            }

            // ===============================================================
            // FILTRO 2: VERIFICAR EL ESTADO ACTUAL DE LA VISTA
            // ===============================================================
            // La vista tiene estados (EN_MENU, EN_JUEGO, EN_ESPERANDO_JUGADORES, etc.)
            // Solo procesamos eventos si la vista está en el estado apropiado
            Estados estadoVista = vista.getEstado();
            System.out.println("[Controller-" + nombreJugador + "] Estado actual: " + estadoVista);

            // ===============================================================
            // PROCESAMIENTO DE EVENTOS SEGÚN SU TIPO
            // ===============================================================
            switch (e.getEvento()) {
                // -----------------------------------------------------------
                // EVENTOS DE JUEGO: Cambios durante la partida
                // Estos eventos requieren actualizar el tablero y la interfaz
                // -----------------------------------------------------------
                case CAMBIO_TURNO, PIEZA_COLOCADA, PIEZA_MOVIDA, PIEZA_ELIMINADA, FORMACION_MOLINO -> {
                    System.out.println("[Controller-" + nombreJugador + "] Procesando evento de juego: " + e.getEvento());
                    // Actualizar si está en juego O si está esperando y el evento es CAMBIO_TURNO
                    // (CAMBIO_TURNO indica que la partida comenzó con el segundo jugador)
                    if (estadoVista == Estados.EN_JUEGO) {
                        vista.mostrarPartida(); // Actualizar tablero, turno, piezas, etc.
                    } else if (estadoVista == Estados.EN_ESPERANDO_JUGADORES && e.getEvento() == Eventos.CAMBIO_TURNO) {
                        System.out.println("[Controller-" + nombreJugador + "] ✓ Segundo jugador detectado, iniciando partida");
                        System.out.println("[Controller-" + nombreJugador + "] Llamando a vista.mostrarPartida()...");
                        vista.mostrarPartida(); // Iniciar la partida
                        System.out.println("[Controller-" + nombreJugador + "] vista.mostrarPartida() completado");
                    } else {
                        System.out.println("[Controller-" + nombreJugador + "] No se procesa evento " + e.getEvento() +
                                         " (Estado actual: " + estadoVista + ")");
                    }
                }

                // -----------------------------------------------------------
                // EVENTO: Partida terminada - Hay un ganador
                // -----------------------------------------------------------
                case GAME_WIN -> {
                    if (estadoVista == Estados.EN_JUEGO) {
                        vista.mostrarGameWin(); // Mostrar mensaje de victoria/derrota
                    }
                }

                // -----------------------------------------------------------
                // EVENTO: Partida terminada - Game over
                // -----------------------------------------------------------
                case GAME_OVER -> {
                    if (estadoVista == Estados.EN_JUEGO) {
                        vista.mostrarGameOver(); // Mostrar pantalla de fin de juego
                    }
                }
            }
        }
    }

    // Getters

    public int getIdPartidaActual() {
        return idPartidaActual;
    }

    public String getNombreJugador() {
        return nombreJugador;
    }

    public boolean isEsJugador1() {
        return esJugador1;
    }

    public void setIdPartidaActual(int id) {
        this.idPartidaActual = id;
    }

    public void setNombreJugador(String nombre) {
        this.nombreJugador = nombre;
    }

    public void setEsJugador1(boolean esJugador1) {
        this.esJugador1 = esJugador1;
    }
}
