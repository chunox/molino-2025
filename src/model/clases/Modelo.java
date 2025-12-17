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
 *    - Gestiona usuarios y autenticación
 *    - Mantiene el ranking de jugadores
 *    - Persiste el estado del juego en disco
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
 *    ║  │  • partidasGuardadas: Persistencia               │   ║
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
 *   [4] Persiste el cambio en disco
 *        ↓
 *   [5] Detecta si se formó un molino o hay ganador
 *        ↓
 *   [6] Notifica a TODOS los observadores (clientes):
 *       notificarObservadores(new ManejadorEventos(id, PIEZA_COLOCADA))
 *        ↓
 *   [7] TODOS los clientes reciben el evento y actualizan su interfaz
 *       (Tanto el que hizo el movimiento como su oponente)
 *
 * EVENTOS QUE NOTIFICA:
 *
 * - CAMBIO_BUSCAR_PARTIDA: Lista de lobbys cambió
 * - CAMBIO_ESPERANDO_JUGADORES: Segundo jugador se unió
 * - PIEZA_COLOCADA: Se colocó una pieza
 * - PIEZA_MOVIDA: Se movió una pieza
 * - PIEZA_ELIMINADA: Se eliminó una pieza
 * - FORMACION_MOLINO: Se formó un molino (3 en línea)
 * - CAMBIO_TURNO: Cambió el turno
 * - GAME_WIN: Hay un ganador
 * - DESCONEXION_J: Jugador se desconectó (causa derrota automática)
 *
 * IMPORTANTE:
 * - Todos los métodos públicos pueden ser llamados remotamente (RMI)
 * - Cada cambio de estado DEBE notificar a los observadores
 * - La notificación es AUTOMÁTICA a todos los clientes suscritos
 * - El estado de partidas activas se persiste automáticamente
 * - La desconexión durante una partida resulta en derrota automática
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
     * Gestión de usuarios y autenticación
     */
    private ISesion usuarios;

    /**
     * Todas las partidas activas
     * Map<ID_Partida, Partida>
     * Permite acceso rápido a cualquier partida por su ID
     */
    private Map<Integer, IPartida> partidas;

    /**
     * Sistema de persistencia de partidas
     * Guarda partidas en disco para permitir reconexiones
     */
    private IPartidaGuardada partidasGuardadas;

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
        partidasGuardadas = PartidaGuardada.getInstancia();
        contadorPartidas = 0;
        cargarPartidasPersistidas();
    }

    private void cargarPartidasPersistidas() {
        Map<Integer, IPartida> partidasGuardadasMap = partidasGuardadas.getPartidasGuardadas();
        for (Map.Entry<Integer, IPartida> entry : partidasGuardadasMap.entrySet()) {
            partidas.put(entry.getKey(), entry.getValue());
            if (entry.getKey() >= contadorPartidas) {
                contadorPartidas = entry.getKey() + 1;
            }
        }
    }

    /**
     * CREAR NUEVA PARTIDA (LOBBY)
     *
     * Crea un nuevo lobby donde un jugador espera a que otro se una.
     *
     * FLUJO:
     * 1. Se crea el jugador 1 (creador del lobby)
     * 2. Se crea el jugador 2 (placeholder vacío)
     * 3. Se crea la partida con ID único
     * 4. Se agrega a la lista de partidas activas
     * 5. Se notifica a TODOS los clientes que hay un nuevo lobby
     *
     * NOTIFICACIÓN:
     * - Evento: CAMBIO_BUSCAR_PARTIDA
     * - Destinatarios: TODOS los clientes conectados
     * - Efecto: Clientes en "Buscar Partida" refrescan su lista de lobbys
     *
     * @param nombreJugador1 Nombre del jugador que crea la partida
     * @param nombreJugador2 Nombre del segundo jugador (placeholder)
     * @return La partida creada
     */
    @Override
    public IPartida crearPartida(String nombreJugador1, String nombreJugador2) throws RemoteException {
        // ===============================================================
        // PASO 1: Crear los jugadores
        // ===============================================================
        IJugador jugador1 = new Jugador(nombreJugador1, "", 'X');
        IJugador jugador2 = new Jugador(nombreJugador2, "", 'O');

        // ===============================================================
        // PASO 2: Crear la partida con ID único
        // ===============================================================
        IPartida partida = new Partida(contadorPartidas++, jugador1, jugador2);
        partidas.put(partida.getId(), partida);

        System.out.println("📝 Lobby #" + partida.getId() + " creado por " + nombreJugador1 +
                          " (" + partida.getJugadores().size() + "/2 jugadores)");

        // ===============================================================
        // PASO 3: Notificar a todos los observadores
        // ===============================================================
        // Esta notificación hace que todos los clientes que estén viendo
        // la lista de partidas la actualicen automáticamente
        notificarObservadores(new ManejadorEventos(partida.getId(), Eventos.CAMBIO_BUSCAR_PARTIDA));

        return partida;
    }

    /**
     * OBTENER LISTA DE PARTIDAS DISPONIBLES
     *
     * Retorna solo las partidas que NO están finalizadas.
     * Filtra automáticamente las partidas terminadas para mantener
     * la lista de lobbys limpia.
     *
     * FILTROS APLICADOS:
     * - Excluye partidas con estado FINALIZADA
     * - Incluye partidas EN_ESPERA (lobbys esperando jugadores)
     * - Incluye partidas EN_JUEGO (partidas en curso, no unibles)
     *
     * @return Lista de partidas activas (no finalizadas)
     * @throws RemoteException si hay error de comunicación RMI
     */
    @Override
    public List<IPartida> getPartidas() throws RemoteException {
        System.out.println("🔍 Buscando lobbys... (" + partidas.size() + " total)");

        List<IPartida> partidasActivas = new ArrayList<>();
        int disponibles = 0;
        int finalizadas = 0;

        for (IPartida p : partidas.values()) {
            // ===============================================================
            // FILTRO 1: Excluir partidas FINALIZADAS
            // ===============================================================
            if (p.getEstadoPartida() == EstadoPartida.FINALIZADA) {
                finalizadas++;
                continue; // No incluir en la lista
            }

            // Agregar partida activa a la lista
            partidasActivas.add(p);

            // Contar lobbys disponibles (esperando jugadores)
            if (p.getJugadores().size() < 2) {
                disponibles++;
                System.out.println("   - Lobby #" + p.getId() + ": " +
                                  p.getJugadores().get(0).getNombre() +
                                  " (" + p.getJugadores().size() + "/2) [" +
                                  p.getEstadoPartida() + "]");
            }
        }

        System.out.println("   ✓ " + disponibles + " lobbys disponibles");
        System.out.println("   ✓ " + partidasActivas.size() + " partidas activas");
        System.out.println("   ⊗ " + finalizadas + " partidas finalizadas (filtradas)");

        return partidasActivas;
    }

    @Override
    public IPartida getPartida(int id) throws RemoteException {
        return partidas.get(id);
    }

    @Override
    public void empezarPartida(int id) throws RemoteException {
        IPartida partida = partidas.get(id);
        if (partida != null) {
            partida.setEstadoPartida(EstadoPartida.EN_JUEGO);
            notificarObservadores(new ManejadorEventos(id, Eventos.CAMBIO_TURNO));
        }
    }

    /**
     * UNIR JUGADOR A UNA PARTIDA EXISTENTE
     *
     * Agrega el segundo jugador a un lobby que estaba esperando.
     * Cuando se completa, la partida comienza automáticamente.
     *
     * FLUJO:
     * 1. Se busca la partida por ID
     * 2. VALIDACIONES:
     *    - Verifica que la partida exista
     *    - Verifica que esté en estado EN_ESPERA
     *    - Verifica que tenga espacio (menos de 2 jugadores)
     * 3. Se crea y agrega el segundo jugador
     * 4. La partida cambia su estado a EN_JUEGO
     * 5. Se notifica a AMBOS jugadores que la partida comenzó
     *
     * NOTIFICACIÓN:
     * - Evento: CAMBIO_ESPERANDO_JUGADORES
     * - Destinatarios: AMBOS jugadores de esta partida
     * - Efecto en Jugador 1: Sale de sala de espera, entra al juego
     * - Efecto en Jugador 2: Sale de lista de partidas, entra al juego
     *
     * @param id ID de la partida a la que se une
     * @param nombreJugador Nombre del jugador que se une
     * @throws RemoteException si hay error de comunicación RMI
     * @throws IllegalStateException si la partida no está disponible
     */
    @Override
    public void agregarJugadorAPartida(int id, String nombreJugador) throws RemoteException {
        IPartida partida = partidas.get(id);

        // ===============================================================
        // VALIDACIONES ROBUSTAS
        // ===============================================================
        if (partida == null) {
            throw new IllegalStateException("La partida #" + id + " no existe");
        }

        if (partida.getEstadoPartida() != EstadoPartida.EN_ESPERA) {
            throw new IllegalStateException("La partida #" + id + " no está esperando jugadores (Estado: " +
                                           partida.getEstadoPartida() + ")");
        }

        if (partida.getJugadores().size() >= 2) {
            throw new IllegalStateException("La partida #" + id + " ya está completa");
        }

        // Verificar que el jugador no esté ya en la partida
        for (IJugador j : partida.getJugadores()) {
            if (j.getNombre().equals(nombreJugador)) {
                throw new IllegalStateException("El jugador " + nombreJugador + " ya está en esta partida");
            }
        }

        // ===============================================================
        // PASO 1: Crear y agregar el segundo jugador
        // ===============================================================
        IJugador nuevoJugador = new Jugador(nombreJugador, "", 'O');
        partida.agregarJugador(nuevoJugador);
        // agregarJugador() internamente cambia el estado a EN_JUEGO

        System.out.println("✅ " + nombreJugador + " se unió al Lobby #" + id +
                          " (2/2 jugadores) - ¡Partida iniciada!");

        // ===============================================================
        // PASO 2: Notificar que la partida está completa y comenzando
        // ===============================================================
        // Esta notificación llega a AMBOS jugadores:
        // - El que estaba esperando (jugador 1)
        // - El que acaba de unirse (jugador 2)
        // Ambos salen de sus pantallas actuales y entran al juego
        System.out.println("📡 Notificando evento CAMBIO_ESPERANDO_JUGADORES a observadores");
        notificarObservadores(new ManejadorEventos(id, Eventos.CAMBIO_ESPERANDO_JUGADORES));
    }

    @Override
    public void registrarUsuario(String nombre, String password) throws RemoteException, JugadorExistente {
        usuarios.registrarse(nombre, password);
    }

    @Override
    public void iniciarSesion(String nombre, String password) throws RemoteException, JugadorNoExistente, PasswordIncorrecta {
        usuarios.iniciarSesion(nombre, password);
    }

    @Override
    public void colocarPieza(int idPartida, String posicion) throws RemoteException {
        IPartida partida = partidas.get(idPartida);
        if (partida != null) {
            if (partida.colocarPieza(posicion)) {
                // Persistir cambio
                partidasGuardadas.actualizar(partida);

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
                // Persistir cambio
                partidasGuardadas.actualizar(partida);

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
                // Persistir cambio
                partidasGuardadas.actualizar(partida);

                // Notificar
                notificarObservadores(new ManejadorEventos(idPartida, Eventos.PIEZA_ELIMINADA));

                // Verificar si hay ganador
                if (partida.hayGanador()) {
                    // Actualizar ranking
                    ranking.actualizar(partida.getGanador().getNombre());

                    // Borrar partida guardada
                    partidasGuardadas.borrarPartidaGuardada(idPartida);

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

    /**
     * DESCONECTAR JUGADOR
     *
     * Al desconectarse, el jugador PIERDE AUTOMÁTICAMENTE:
     * - Si está EN_ESPERA: Se elimina del lobby (vuelve a estar disponible)
     * - Si está EN_JUEGO: Pierde la partida automáticamente
     *
     * FLUJO:
     * 1. Busca la partida del jugador
     * 2. Si está EN_ESPERA: Elimina al jugador, lobby vuelve a estar disponible
     * 3. Si está EN_JUEGO: El oponente gana automáticamente
     * 4. Finaliza la partida y notifica a todos
     * 5. Actualiza el ranking con la victoria del oponente
     *
     * @param nombre Nombre del jugador que se desconecta
     * @param idPartida ID de la partida
     * @throws RemoteException si hay error de comunicación RMI
     */
    @Override
    public void desconectarJugador(String nombre, int idPartida) throws RemoteException {
        IPartida partida = partidas.get(idPartida);
        if (partida == null) {
            return;
        }

        System.out.println("⚠ Jugador " + nombre + " se desconectó de partida #" + idPartida);

        // ===============================================================
        // CASO 1: Lobby esperando jugadores (EN_ESPERA)
        // ===============================================================
        if (partida.getEstadoPartida() == EstadoPartida.EN_ESPERA) {
            System.out.println("   → Lobby EN_ESPERA: eliminando jugador del lobby");

            // Eliminar jugador del lobby
            partida.removerJugador(nombre);

            // Si el lobby quedó vacío, eliminarlo completamente
            if (partida.getJugadores().isEmpty()) {
                partidas.remove(idPartida);
                partidasGuardadas.borrarPartidaGuardada(idPartida);
                System.out.println("   → Lobby #" + idPartida + " eliminado (sin jugadores)");
            } else {
                // Lobby aún tiene jugadores esperando
                System.out.println("   → Lobby #" + idPartida + " sigue activo (" +
                                  partida.getJugadores().size() + "/2 jugadores)");
            }

            // Notificar cambio en lobbys
            notificarObservadores(new ManejadorEventos(idPartida, Eventos.CAMBIO_BUSCAR_PARTIDA));
            return;
        }

        // ===============================================================
        // CASO 2: Partida en juego (EN_JUEGO)
        // ===============================================================
        if (partida.getEstadoPartida() == EstadoPartida.EN_JUEGO) {
            System.out.println("   → Partida EN_JUEGO: jugador pierde por desconexión");

            // Encontrar al oponente (el que NO se desconectó)
            IJugador ganador = null;
            for (IJugador j : partida.getJugadores()) {
                if (!j.getNombre().equals(nombre)) {
                    ganador = j;
                    break;
                }
            }

            if (ganador != null) {
                // El oponente gana automáticamente
                partida.setGanador(ganador);
                partida.setEstadoPartida(EstadoPartida.FINALIZADA);

                System.out.println("   → " + ganador.getNombre() + " gana por desconexión del oponente");

                // Actualizar ranking
                ranking.actualizar(ganador.getNombre());

                // Eliminar partida guardada (ya terminó)
                partidasGuardadas.borrarPartidaGuardada(idPartida);

                // Notificar fin del juego
                notificarObservadores(new ManejadorEventos(idPartida, Eventos.GAME_WIN));
            } else {
                // Caso extraño: solo había un jugador
                partida.setEstadoPartida(EstadoPartida.FINALIZADA);
                partidasGuardadas.borrarPartidaGuardada(idPartida);
            }
        }
    }

    @Override
    public Map<Integer, IPartida> getPartidasGuardadas(String nombreJugador) throws RemoteException {
        return partidasGuardadas.getPartidasGuardadas(nombreJugador);
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
