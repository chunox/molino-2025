package model.clases;

import ar.edu.unlu.rmimvc.observer.ObservableRemoto;
import model.enums.*;
import model.excepciones.*;
import model.interfaces.*;
import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.*;

/**
 * ============================================================================
 * MODELO - LÓGICA DE NEGOCIO CENTRALIZADA DEL JUEGO
 * ============================================================================
 *
 * El Modelo es el componente central de la aplicación distribuida.
 * Reside en el SERVIDOR y es compartido por TODOS los clientes.
 *
 * RESPONSABILIDADES:
 *
 * 1. GESTIÓN DE ESTADO:
 *    - Mantiene el estado de todas las partidas activas
 *    - Gestiona usuarios
 *    - Mantiene el ranking de jugadores
 *
 * 2. LÓGICA DE NEGOCIO:
 *    - Valida movimientos y reglas del juego
 *    - Detecta formación de molinos (3 en línea)
 *    - Determina ganadores
 *    - Controla turnos y fases del juego
 *
 * 3. PATRÓN OBSERVER DISTRIBUIDO:
 *    - Extiende ObservableRemoto (de la librería rmimvc)
 *    - Notifica a TODOS los clientes cuando ocurre un cambio
 *    - Permite sincronización automática de todos los jugadores
 *
 * 4. PATRÓN SINGLETON:
 *    - Solo existe UNA instancia del Modelo en el servidor
 *    - Todos los clientes comparten esta misma instancia
 *
 * ARQUITECTURA:
 *
 *    ╔═══════════════════════════════════════════════════════════╗
 *    ║                     SERVIDOR                              ║
 *    ║  ┌───────────────────────────────────────────────────┐   ║
 *    ║  │              MODELO (Singleton)                   │   ║
 *    ║  │  ┌─────────────────────────────────────────────┐ │   ║
 *    ║  │  │  ObservableRemoto                           │ │   ║
 *    ║  │  │  (Notifica cambios a clientes)              │ │   ║
 *    ║  │  └─────────────────────────────────────────────┘ │   ║
 *    ║  │                                                   │   ║
 *    ║  │  • usuarios: Gestión de sesiones                 │   ║
 *    ║  │  • partidas: Map<ID, Partida>                    │   ║
 *    ║  │  • ranking: Puntuaciones                         │   ║
 *    ║  └───────────────────────────────────────────────────┘   ║
 *    ╚═══════════════════════════════════════════════════════════╝
 *                           │ RMI
 *                           │ (Notificaciones)
 *         ┌─────────────────┼─────────────────┐
 *         ▼                 ▼                 ▼
 *    ┌─────────┐      ┌─────────┐      ┌─────────┐
 *    │Cliente 1│      │Cliente 2│      │Cliente 3│
 *    └─────────┘      └─────────┘      └─────────┘
 *
 * FLUJO DE UNA ACCIÓN TÍPICA (Ejemplo: Colocar Pieza):
 *
 *   [1] Cliente A llama: modelo.colocarPieza(idPartida, posicion)
 *        ↓
 *   [2] Modelo valida el movimiento
 *        ↓
 *   [3] Si es válido: actualiza el estado de la partida
 *        ↓
 *   [4] Detecta si se formó un molino o hay ganador
 *        ↓
 *   [5] Notifica a TODOS los observadores (clientes):
 *       notificarObservadores(new ManejadorEventos(id, PIEZA_COLOCADA))
 *        ↓
 *   [6] TODOS los clientes reciben el evento y actualizan su interfaz
 *       (Tanto el que hizo el movimiento como su oponente)
 *
 * EVENTOS QUE NOTIFICA:
 *
 * - CAMBIO_TURNO: Cambió el turno o comenzó la partida
 * - PIEZA_COLOCADA: Se colocó una pieza
 * - PIEZA_MOVIDA: Se movió una pieza
 * - PIEZA_ELIMINADA: Se eliminó una pieza
 * - FORMACION_MOLINO: Se formó un molino (3 en línea)
 * - GAME_WIN: Hay un ganador
 *
 * IMPORTANTE:
 * - Todos los métodos públicos pueden ser llamados remotamente (RMI)
 * - Cada cambio de estado DEBE notificar a los observadores
 * - La notificación es AUTOMÁTICA a todos los clientes suscritos
 */
public class Modelo extends ObservableRemoto implements IModelo, Serializable {
    private static final long serialVersionUID = 1L;

    // ===================================================================
    // PATRÓN SINGLETON
    // ===================================================================
    /**
     * Instancia única del Modelo (Singleton)
     * Compartida por todos los clientes conectados al servidor
     */
    private static IModelo instancia = null;

    // ===================================================================
    // COMPONENTES DEL MODELO
    // ===================================================================

    /**
     * Gestión de usuarios
     */
    private ISesion usuarios;

    /**
     * Todas las partidas activas
     * Map<ID_Partida, Partida>
     * Permite acceso rápido a cualquier partida por su ID
     */
    private Map<Integer, IPartida> partidas;

    /**
     * Sistema de puntuación
     * Mantiene estadísticas de victorias de cada jugador
     */
    private IRanking ranking;

    /**
     * Contador para asignar IDs únicos a nuevas partidas
     */
    private int contadorPartidas;

    public static IModelo getInstancia() throws RemoteException {
        if (instancia == null) {
            instancia = new Modelo();
        }
        return instancia;
    }

    private Modelo() throws RemoteException {
        super();
        usuarios = Sesion.getInstancia();
        partidas = new HashMap<>();
        ranking = Ranking.getInstancia();
        contadorPartidas = 0;
    }

    /**
     * BUSCAR O CREAR PARTIDA AUTOMÁTICAMENTE
     *
     * Empareja automáticamente al jugador con otro jugador esperando,
     * o crea una nueva partida si no hay nadie esperando.
     *
     * FLUJO:
     * 1. Busca si hay alguna partida EN_ESPERA con 1 jugador
     * 2. Si existe:
     *    - Une al jugador como segundo jugador
     *    - Inicia la partida automáticamente
     *    - Notifica a ambos jugadores
     * 3. Si NO existe:
     *    - Crea nueva partida EN_ESPERA
     *    - El jugador espera a que otro se conecte
     *
     * @param nombreJugador Nombre del jugador que busca partida
     * @return La partida asignada (nueva o existente)
     * @throws RemoteException si hay error de comunicación RMI
     */
    @Override
    public IPartida buscarPartida(String nombreJugador) throws RemoteException {
        System.out.println("🔍 " + nombreJugador + " busca partida...");

        // ===============================================================
        // PASO 1: Buscar partida disponible (EN_ESPERA con 1 jugador)
        // ===============================================================
        for (IPartida p : partidas.values()) {
            if (p.getEstadoPartida() == EstadoPartida.EN_ESPERA &&
                p.getJugadores().size() == 1) {

                // Verificar que no sea el mismo jugador
                if (p.getJugadores().get(0).getNombre().equals(nombreJugador)) {
                    continue;
                }

                // ===============================================================
                // Partida encontrada - Unir al jugador
                // ===============================================================
                IJugador jugador2 = new Jugador(nombreJugador, 'O');
                p.agregarJugador(jugador2);
                // agregarJugador() cambia automáticamente el estado a EN_JUEGO

                System.out.println("✅ " + nombreJugador + " se unió a la partida #" + p.getId() +
                                  " (2/2 jugadores) - ¡Partida iniciada!");
                System.out.println("📡 Notificando CAMBIO_TURNO a todos los observadores...");

                // Notificar a ambos jugadores que la partida comenzó
                notificarObservadores(new ManejadorEventos(p.getId(), Eventos.CAMBIO_TURNO));

                System.out.println("✓ Evento CAMBIO_TURNO notificado");
                return p;
            }
        }

        // ===============================================================
        // PASO 2: No hay partidas disponibles - Crear nueva
        // ===============================================================
        IJugador jugador1 = new Jugador(nombreJugador, 'X');
        IPartida nuevaPartida = new Partida(contadorPartidas++, jugador1, null);
        partidas.put(nuevaPartida.getId(), nuevaPartida);

        System.out.println("📝 Nueva partida #" + nuevaPartida.getId() + " creada. " +
                          nombreJugador + " esperando oponente...");

        return nuevaPartida;
    }

    @Override
    public IPartida getPartida(int id) throws RemoteException {
        return partidas.get(id);
    }

    @Override
    public void colocarPieza(int idPartida, String posicion) throws RemoteException {
        IPartida partida = partidas.get(idPartida);
        if (partida != null) {
            if (partida.colocarPieza(posicion)) {
                // Notificar
                if (partida.isEsperandoEliminar()) {
                    notificarObservadores(new ManejadorEventos(idPartida, Eventos.FORMACION_MOLINO));
                } else {
                    notificarObservadores(new ManejadorEventos(idPartida, Eventos.PIEZA_COLOCADA));
                    notificarObservadores(new ManejadorEventos(idPartida, Eventos.CAMBIO_TURNO));
                }
            }
        }
    }

    @Override
    public void moverPieza(int idPartida, String origen, String destino) throws RemoteException {
        IPartida partida = partidas.get(idPartida);
        if (partida != null) {
            if (partida.moverPieza(origen, destino)) {
                // Notificar
                if (partida.isEsperandoEliminar()) {
                    notificarObservadores(new ManejadorEventos(idPartida, Eventos.FORMACION_MOLINO));
                } else {
                    notificarObservadores(new ManejadorEventos(idPartida, Eventos.PIEZA_MOVIDA));
                    notificarObservadores(new ManejadorEventos(idPartida, Eventos.CAMBIO_TURNO));
                }
            }
        }
    }

    @Override
    public void eliminarPiezaOponente(int idPartida, String posicion) throws RemoteException {
        IPartida partida = partidas.get(idPartida);
        if (partida != null) {
            if (partida.eliminarPiezaOponente(posicion)) {
                // Notificar
                notificarObservadores(new ManejadorEventos(idPartida, Eventos.PIEZA_ELIMINADA));

                // Verificar si hay ganador
                if (partida.hayGanador()) {
                    // Actualizar ranking
                    ranking.actualizar(partida.getGanador().getNombre());

                    // Notificar fin del juego
                    notificarObservadores(new ManejadorEventos(idPartida, Eventos.GAME_WIN));
                } else {
                    notificarObservadores(new ManejadorEventos(idPartida, Eventos.CAMBIO_TURNO));
                }
            }
        }
    }

    @Override
    public boolean verificarFinDelJuego(int id) throws RemoteException {
        IPartida partida = partidas.get(id);
        return partida != null && partida.hayGanador();
    }

    @Override
    public boolean hayGanador(int id) throws RemoteException {
        IPartida partida = partidas.get(id);
        return partida != null && partida.hayGanador();
    }

    @Override
    public IJugador getGanador(int id) throws RemoteException {
        IPartida partida = partidas.get(id);
        return partida != null ? partida.getGanador() : null;
    }

    @Override
    public Map<String, Integer> getRanking() throws RemoteException {
        return ranking.getRanking();
    }

    @Override
    public void actualizarRanking(String nombreJugador) throws RemoteException {
        ranking.actualizar(nombreJugador);
    }
}
