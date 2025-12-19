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

    /**
     * OBTENER INSTANCIA ÚNICA DEL MODELO (Singleton)
     *
     * Implementación del patrón Singleton para garantizar que solo exista
     * una instancia del Modelo en el servidor.
     *
     * @return La instancia única del Modelo
     * @throws RemoteException si hay error en la comunicación RMI
     *
     * RELACIONES:
     * - Si no existe instancia, llama al constructor privado Modelo()
     */
    public static IModelo getInstancia() throws RemoteException {
        if (instancia == null) {
            instancia = new Modelo();
        }
        return instancia;
    }

    /**
     * CONSTRUCTOR PRIVADO DEL MODELO (Singleton)
     *
     * Inicializa todos los componentes del sistema:
     * - Sistema de usuarios (Sesion)
     * - Mapa de partidas activas
     * - Sistema de ranking
     * - Contador de partidas
     *
     * @throws RemoteException si hay error en la comunicación RMI
     *
     * RELACIONES:
     * - Llama a super() para inicializar ObservableRemoto (patrón Observer)
     * - Llama a Sesion.getInstancia() para obtener el gestor de usuarios
     * - Llama a Ranking.getInstancia() para obtener el sistema de puntuación
     */
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
     *
     * RELACIONES CON OTRAS FUNCIONES:
     * - Llama a p.getEstadoPartida() para verificar estado de cada partida
     * - Llama a p.getJugadores() para obtener lista de jugadores
     * - Llama a new Jugador() para crear un nuevo jugador
     * - Llama a p.agregarJugador() para unir jugador a partida existente
     * - Llama a notificarObservadores() para notificar evento CAMBIO_TURNO
     * - Llama a new Partida() para crear una nueva partida
     * - Llama a p.getId() para obtener el ID de la partida
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

    /**
     * OBTENER PARTIDA POR ID
     *
     * Busca y devuelve una partida específica usando su ID único.
     *
     * @param id ID único de la partida
     * @return La partida correspondiente al ID, o null si no existe
     * @throws RemoteException si hay error de comunicación RMI
     *
     * RELACIONES CON OTRAS FUNCIONES:
     * - Accede al Map partidas usando el método get()
     *
     * NOTA: Esta función es llamada frecuentemente por:
     * - colocarPieza() para obtener la partida antes de colocar una pieza
     * - moverPieza() para obtener la partida antes de mover una pieza
     * - eliminarPiezaOponente() para obtener la partida antes de eliminar
     * - verificarFinDelJuego() para verificar si hay ganador
     * - hayGanador() para verificar si hay ganador
     * - getGanador() para obtener el ganador
     */
    @Override
    public IPartida getPartida(int id) throws RemoteException {
        return partidas.get(id);
    }

    /**
     * COLOCAR PIEZA EN EL TABLERO
     *
     * Permite al jugador actual colocar una pieza en una posición específica del tablero
     * durante la fase de colocación del juego.
     *
     * FLUJO:
     * 1. Obtiene la partida usando el ID
     * 2. Intenta colocar la pieza en la posición indicada
     * 3. Si la colocación fue exitosa:
     *    a) Si se formó un molino: notifica FORMACION_MOLINO
     *    b) Si no: notifica PIEZA_COLOCADA y CAMBIO_TURNO
     *
     * @param idPartida ID de la partida activa
     * @param posicion Posición donde colocar la pieza (ej: "A1", "B2", etc.)
     * @throws RemoteException si hay error de comunicación RMI
     *
     * RELACIONES CON OTRAS FUNCIONES:
     * - Llama a partidas.get(idPartida) para obtener la partida
     * - Llama a partida.colocarPieza(posicion) para colocar la pieza
     * - Llama a partida.isEsperandoEliminar() para verificar si se formó un molino
     * - Llama a notificarObservadores() con evento FORMACION_MOLINO si hay molino
     * - Llama a notificarObservadores() con evento PIEZA_COLOCADA si no hay molino
     * - Llama a notificarObservadores() con evento CAMBIO_TURNO para cambiar turno
     *
     * EVENTOS QUE GENERA:
     * - FORMACION_MOLINO: Cuando se forma un molino (3 en línea)
     * - PIEZA_COLOCADA: Cuando se coloca una pieza sin formar molino
     * - CAMBIO_TURNO: Para cambiar el turno al otro jugador
     */
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

    /**
     * MOVER PIEZA EN EL TABLERO
     *
     * Permite al jugador actual mover una de sus piezas de una posición a otra
     * durante la fase de movimiento del juego.
     *
     * FLUJO:
     * 1. Obtiene la partida usando el ID
     * 2. Intenta mover la pieza desde origen hasta destino
     * 3. Si el movimiento fue exitoso:
     *    a) Si se formó un molino: notifica FORMACION_MOLINO
     *    b) Si no: notifica PIEZA_MOVIDA y CAMBIO_TURNO
     *
     * @param idPartida ID de la partida activa
     * @param origen Posición de origen de la pieza (ej: "A1")
     * @param destino Posición de destino de la pieza (ej: "A2")
     * @throws RemoteException si hay error de comunicación RMI
     *
     * RELACIONES CON OTRAS FUNCIONES:
     * - Llama a partidas.get(idPartida) para obtener la partida
     * - Llama a partida.moverPieza(origen, destino) para mover la pieza
     * - Llama a partida.isEsperandoEliminar() para verificar si se formó un molino
     * - Llama a notificarObservadores() con evento FORMACION_MOLINO si hay molino
     * - Llama a notificarObservadores() con evento PIEZA_MOVIDA si no hay molino
     * - Llama a notificarObservadores() con evento CAMBIO_TURNO para cambiar turno
     *
     * EVENTOS QUE GENERA:
     * - FORMACION_MOLINO: Cuando el movimiento forma un molino (3 en línea)
     * - PIEZA_MOVIDA: Cuando se mueve una pieza sin formar molino
     * - CAMBIO_TURNO: Para cambiar el turno al otro jugador
     */
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

    /**
     * ELIMINAR PIEZA DEL OPONENTE
     *
     * Permite al jugador actual eliminar una pieza del oponente después de
     * haber formado un molino (3 en línea).
     *
     * FLUJO:
     * 1. Obtiene la partida usando el ID
     * 2. Intenta eliminar la pieza del oponente en la posición indicada
     * 3. Si la eliminación fue exitosa:
     *    a) Notifica PIEZA_ELIMINADA
     *    b) Verifica si hay un ganador:
     *       - Si hay ganador: actualiza el ranking y notifica GAME_WIN
     *       - Si no hay ganador: notifica CAMBIO_TURNO
     *
     * @param idPartida ID de la partida activa
     * @param posicion Posición de la pieza del oponente a eliminar (ej: "B3")
     * @throws RemoteException si hay error de comunicación RMI
     *
     * RELACIONES CON OTRAS FUNCIONES:
     * - Llama a partidas.get(idPartida) para obtener la partida
     * - Llama a partida.eliminarPiezaOponente(posicion) para eliminar la pieza
     * - Llama a notificarObservadores() con evento PIEZA_ELIMINADA
     * - Llama a partida.hayGanador() para verificar si hay ganador
     * - Si hay ganador:
     *   - Llama a partida.getGanador() para obtener el jugador ganador
     *   - Llama a ranking.actualizar() para incrementar victorias del ganador
     *   - Llama a notificarObservadores() con evento GAME_WIN
     * - Si no hay ganador:
     *   - Llama a notificarObservadores() con evento CAMBIO_TURNO
     *
     * EVENTOS QUE GENERA:
     * - PIEZA_ELIMINADA: Siempre que se elimina una pieza
     * - GAME_WIN: Cuando hay un ganador (oponente quedó con menos de 3 piezas)
     * - CAMBIO_TURNO: Cuando no hay ganador y continúa el juego
     */
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

    /**
     * VERIFICAR SI EL JUEGO HA TERMINADO
     *
     * Verifica si la partida especificada ha finalizado (hay un ganador).
     *
     * @param id ID de la partida a verificar
     * @return true si la partida terminó (hay ganador), false en caso contrario
     * @throws RemoteException si hay error de comunicación RMI
     *
     * RELACIONES CON OTRAS FUNCIONES:
     * - Llama a partidas.get(id) para obtener la partida
     * - Llama a partida.hayGanador() para verificar si hay ganador
     *
     * NOTA: Esta función es equivalente a hayGanador() y ambas delegan
     * la verificación a partida.hayGanador()
     */
    @Override
    public boolean verificarFinDelJuego(int id) throws RemoteException {
        IPartida partida = partidas.get(id);
        return partida != null && partida.hayGanador();
    }

    /**
     * VERIFICAR SI HAY GANADOR
     *
     * Verifica si la partida especificada tiene un ganador.
     *
     * @param id ID de la partida a verificar
     * @return true si hay un ganador, false en caso contrario
     * @throws RemoteException si hay error de comunicación RMI
     *
     * RELACIONES CON OTRAS FUNCIONES:
     * - Llama a partidas.get(id) para obtener la partida
     * - Llama a partida.hayGanador() para verificar si hay ganador
     *
     * NOTA: Esta función es equivalente a verificarFinDelJuego() y ambas
     * delegan la verificación a partida.hayGanador()
     */
    @Override
    public boolean hayGanador(int id) throws RemoteException {
        IPartida partida = partidas.get(id);
        return partida != null && partida.hayGanador();
    }

    /**
     * OBTENER GANADOR DE LA PARTIDA
     *
     * Devuelve el jugador ganador de la partida especificada.
     *
     * @param id ID de la partida
     * @return El jugador ganador, o null si no hay ganador o no existe la partida
     * @throws RemoteException si hay error de comunicación RMI
     *
     * RELACIONES CON OTRAS FUNCIONES:
     * - Llama a partidas.get(id) para obtener la partida
     * - Llama a partida.getGanador() para obtener el jugador ganador
     *
     * NOTA: Debe llamarse después de verificar que hayGanador() retorna true
     */
    @Override
    public IJugador getGanador(int id) throws RemoteException {
        IPartida partida = partidas.get(id);
        return partida != null ? partida.getGanador() : null;
    }

    /**
     * OBTENER RANKING DE JUGADORES
     *
     * Devuelve el ranking completo con las puntuaciones de todos los jugadores.
     *
     * @return Map con nombres de jugadores y sus victorias
     *         Estructura: Map<NombreJugador, NumeroDeVictorias>
     * @throws RemoteException si hay error de comunicación RMI
     *
     * RELACIONES CON OTRAS FUNCIONES:
     * - Llama a ranking.getRanking() para obtener el mapa de puntuaciones
     *
     * NOTA: El ranking se actualiza automáticamente cada vez que termina
     * una partida (ver eliminarPiezaOponente() que llama a ranking.actualizar())
     */
    @Override
    public Map<String, Integer> getRanking() throws RemoteException {
        return ranking.getRanking();
    }

    /**
     * ACTUALIZAR RANKING DE UN JUGADOR
     *
     * Incrementa en 1 el número de victorias del jugador especificado en el ranking.
     * Si el jugador no existe en el ranking, lo crea con 1 victoria.
     *
     * @param nombreJugador Nombre del jugador cuyo ranking se actualizará
     * @throws RemoteException si hay error de comunicación RMI
     *
     * RELACIONES CON OTRAS FUNCIONES:
     * - Llama a ranking.actualizar(nombreJugador) para incrementar victorias
     *
     * NOTA: Esta función es llamada automáticamente por:
     * - eliminarPiezaOponente() cuando detecta un ganador
     *
     * El ranking se persiste automáticamente en el archivo ranking.dat
     */
    @Override
    public void actualizarRanking(String nombreJugador) throws RemoteException {
        ranking.actualizar(nombreJugador);
    }
}
