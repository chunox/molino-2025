package model.clases;

import model.enums.*;
import model.interfaces.IJugador;
import model.interfaces.IPartida;
import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

/**
 * ============================================================================
 * PARTIDA - GESTIÓN DE UNA PARTIDA DEL JUEGO DEL MOLINO
 * ============================================================================
 *
 * La clase Partida encapsula toda la lógica y estado de una partida individual
 * del juego del Molino (Nine Men's Morris).
 *
 * RESPONSABILIDADES:
 *
 * 1. GESTIÓN DEL ESTADO:
 *    - Administra el tablero de juego
 *    - Gestiona los 2 jugadores de la partida
 *    - Controla el turno actual
 *    - Mantiene la fase del juego (COLOCACION o MOVIMIENTO)
 *    - Rastrea el estado de la partida (EN_ESPERA, EN_JUEGO, FINALIZADA)
 *
 * 2. LÓGICA DEL JUEGO:
 *    - Valida y ejecuta colocación de piezas
 *    - Valida y ejecuta movimiento de piezas
 *    - Detecta formación de molinos (3 en línea)
 *    - Gestiona eliminación de piezas del oponente
 *    - Detecta condiciones de victoria
 *
 * 3. FASES DEL JUEGO:
 *    - COLOCACION: Cada jugador coloca sus 9 piezas por turnos
 *    - MOVIMIENTO: Los jugadores mueven piezas a posiciones adyacentes
 *    - VUELO: Cuando un jugador tiene solo 3 piezas, puede "volar" a cualquier posición
 *
 * 4. REGLAS ESPECIALES:
 *    - Molino: 3 piezas del mismo jugador en línea
 *    - Al formar molino: se puede eliminar una pieza del oponente
 *    - No se puede eliminar una pieza que forme parte de un molino
 *      (a menos que todas las piezas del oponente estén en molinos)
 *    - Victoria por reducir al oponente a 2 piezas o menos
 *    - Victoria si el oponente no puede moverse
 *
 * ESTADOS DE LA PARTIDA:
 *
 * - EstadoPartida: EN_ESPERA (esperando segundo jugador), EN_JUEGO, FINALIZADA
 * - EstadoJuego: EN_CURSO (jugando normalmente), ESPERANDO_ELIMINAR (formó molino),
 *                FINALIZADO (hay ganador)
 * - FaseJuego: COLOCACION (colocando piezas iniciales), MOVIMIENTO (moviendo piezas)
 */
public class Partida implements IPartida, Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * Constantes del juego
     */
    private static final int PIEZAS_TOTALES_POR_JUGADOR = 9;  // Cada jugador tiene 9 piezas
    private static final int PIEZAS_PARA_VUELO = 3;            // Con 3 piezas puede volar
    private static final int PIEZAS_MINIMAS_PARA_PERDER = 2;   // Con 2 o menos piezas, pierde

    /**
     * Atributos de la partida
     */
    private int id;                                 // ID único de la partida
    private final Tablero tablero;                 // Tablero de juego
    private final List<IJugador> jugadores;         // Lista de 2 jugadores
    private IJugador jugadorActual;                 // Jugador que tiene el turno
    private FaseJuego faseActual;                   // COLOCACION o MOVIMIENTO
    private EstadoPartida estadoPartida;            // EN_ESPERA, EN_JUEGO, FINALIZADA
    private EstadoJuego estadoJuego;                // EN_CURSO, ESPERANDO_ELIMINAR, FINALIZADO
    private IJugador ganador;                       // Ganador de la partida (null si no hay)

    /**
     * Estados internos del juego
     * - EN_CURSO: Jugando normalmente
     * - ESPERANDO_ELIMINAR: Se formó un molino, esperando que elimine una pieza
     * - FINALIZADO: La partida terminó, hay un ganador
     */
    public enum EstadoJuego { EN_CURSO, ESPERANDO_ELIMINAR, FINALIZADO }
    //esto deberia estar en un enum pero no lo voy a cambiar por que ya hice el uml, queda asi


    /**
     * CONSTRUCTOR DE LA PARTIDA
     *
     * Crea una nueva partida del juego del Molino. Puede crearse con 2 jugadores
     * (partida completa) o con 1 jugador (esperando segundo jugador).
     *
     * FLUJO:
     * 1. Asigna el ID de la partida
     * 2. Crea un nuevo tablero vacío
     * 3. Agrega el primer jugador (siempre requerido)
     * 4. Si hay segundo jugador válido:
     *    - Lo agrega a la lista
     *    - Cambia estado a EN_JUEGO
     * 5. Si no hay segundo jugador:
     *    - Deja estado en EN_ESPERA
     * 6. Inicializa:
     *    - Jugador actual (jugador1 siempre empieza)
     *    - Fase actual (COLOCACION)
     *    - Estado del juego (EN_CURSO)
     *    - Ganador (null)
     *
     * @param id ID único de la partida
     * @param jugador1 Primer jugador (obligatorio)
     * @param jugador2 Segundo jugador (puede ser null si se espera un jugador)
     *
     * RELACIONES CON OTRAS FUNCIONES:
     * - Llama a new Tablero() para crear el tablero
     * - Llama a jugadores.add() para agregar jugadores
     * - Llama a jugador2.getNombre() para validar si es un jugador real
     */
    public Partida(int id, IJugador jugador1, IJugador jugador2) {
        this.id = id;
        this.tablero = new Tablero();
        this.jugadores = new ArrayList<>();
        this.jugadores.add(jugador1);

        // Solo agregar jugador2 si no es null o placeholder
        if (jugador2 != null && !jugador2.getNombre().equals("Esperando jugador...")) {
            this.jugadores.add(jugador2);
            this.estadoPartida = EstadoPartida.EN_JUEGO;
            System.out.println("   [Partida #" + id + "] Creada con 2 jugadores: " +
                             jugador1.getNombre() + " vs " + jugador2.getNombre());
        } else {
            this.estadoPartida = EstadoPartida.EN_ESPERA;
            System.out.println("   [Partida #" + id + "] Partida en espera (1/2): " + jugador1.getNombre());
        }

        this.jugadorActual = jugador1;
        this.faseActual = FaseJuego.COLOCACION;
        this.estadoJuego = EstadoJuego.EN_CURSO;
        this.ganador = null;
    }

    /**
     * OBTENER ID DE LA PARTIDA
     *
     * @return ID único de la partida
     */
    @Override
    public int getId() {
        return id;
    }

    /**
     * ESTABLECER ID DE LA PARTIDA
     *
     * @param id Nuevo ID de la partida
     */
    @Override
    public void setId(int id) {
        this.id = id;
    }

    /**
     * OBTENER LISTA DE JUGADORES
     *
     * Devuelve una copia de la lista de jugadores para evitar modificaciones externas.
     *
     * @return Lista de jugadores (1 o 2 jugadores)
     */
    @Override
    public List<IJugador> getJugadores() {
        return new ArrayList<>(jugadores);
    }

    /**
     * AGREGAR JUGADOR A LA PARTIDA
     *
     * Agrega un segundo jugador a una partida que estaba EN_ESPERA.
     * Cuando se completa la partida (2 jugadores), cambia automáticamente
     * el estado a EN_JUEGO.
     *
     * FLUJO:
     * 1. Verifica que no haya más de 2 jugadores
     * 2. Agrega el jugador a la lista
     * 3. Si ahora hay 2 jugadores:
     *    - Cambia estadoPartida a EN_JUEGO
     *
     * @param jugador Jugador a agregar (será el segundo jugador)
     *
     * RELACIONES CON OTRAS FUNCIONES:
     * - Llama a jugadores.add() para agregar el jugador
     * - Llama a jugador.getNombre() para logging
     * - Llama a jugadores.size() para verificar cantidad
     *
     * NOTA: Esta función es llamada por Modelo.buscarPartida() cuando
     * un jugador se une a una partida en espera
     */
    @Override
    public void agregarJugador(IJugador jugador) {
        if (jugadores.size() < 2) {
            jugadores.add(jugador);
            System.out.println("   [Partida #" + id + "] " + jugador.getNombre() +
                             " se agregó (" + jugadores.size() + "/2)");

            // Si ahora hay 2 jugadores, cambiar estado a EN_JUEGO
            if (jugadores.size() == 2) {
                this.estadoPartida = EstadoPartida.EN_JUEGO;
                System.out.println("   [Partida #" + id + "] ¡Partida completa! Estado -> EN_JUEGO");
            }
        }
    }

    /**
     * OBTENER ESTADO DE LA PARTIDA
     *
     * @return Estado actual (EN_ESPERA, EN_JUEGO, FINALIZADA)
     */
    @Override
    public EstadoPartida getEstadoPartida() {
        return estadoPartida;
    }

    /**
     * ESTABLECER ESTADO DE LA PARTIDA
     *
     * @param estado Nuevo estado de la partida
     */
    @Override
    public void setEstadoPartida(EstadoPartida estado) {
        this.estadoPartida = estado;
    }

    /**
     * OBTENER FASE ACTUAL DEL JUEGO
     *
     * @return Fase actual (COLOCACION o MOVIMIENTO)
     */
    @Override
    public FaseJuego getFaseActual() {
        return faseActual;
    }

    /**
     * ESTABLECER FASE ACTUAL DEL JUEGO
     *
     * @param fase Nueva fase del juego
     */
    @Override
    public void setFaseActual(FaseJuego fase) {
        this.faseActual = fase;
    }

    /**
     * OBTENER JUGADOR ACTUAL (QUE TIENE EL TURNO)
     *
     * @return Jugador que debe hacer la siguiente acción
     */
    @Override
    public IJugador getJugadorActual() {
        return jugadorActual;
    }

    /**
     * ESTABLECER JUGADOR ACTUAL
     *
     * @param jugador Jugador que tendrá el turno
     */
    @Override
    public void setJugadorActual(IJugador jugador) {
        this.jugadorActual = jugador;
    }

    /**
     * OBTENER GANADOR DE LA PARTIDA
     *
     * @return Jugador ganador, o null si no hay ganador aún
     */
    @Override
    public IJugador getGanador() {
        return ganador;
    }

    /**
     * ESTABLECER GANADOR DE LA PARTIDA
     *
     * @param ganador Jugador que ganó la partida
     */
    @Override
    public void setGanador(IJugador ganador) {
        this.ganador = ganador;
    }

    /**
     * VERIFICAR SI HAY GANADOR
     *
     * @return true si la partida tiene un ganador, false en caso contrario
     */
    @Override
    public boolean hayGanador() {
        return ganador != null;
    }

    /**
     * COLOCAR PIEZA EN EL TABLERO
     *
     * Coloca una pieza del jugador actual en una posición específica durante
     * la fase de colocación del juego.
     *
     * FLUJO:
     * 1. Valida que esté en fase COLOCACION y estado EN_CURSO
     * 2. Intenta colocar la pieza en el tablero
     * 3. Procesa la acción:
     *    - Si forma molino: cambia a ESPERANDO_ELIMINAR
     *    - Si no: cambia turno y actualiza fase si es necesario
     *
     * @param posicion Posición donde colocar la pieza (ej: "A1", "B2", etc.)
     * @return true si la pieza se colocó exitosamente, false en caso contrario
     * @throws RemoteException si hay error de comunicación RMI
     *
     * RELACIONES CON OTRAS FUNCIONES:
     * - Llama a validarAccion(FaseJuego.COLOCACION) para validar precondiciones
     * - Llama a tablero.colocarPieza(posicion, jugadorActual) para colocar la pieza
     * - Llama a procesarDespuesDeAccion(posicion) para manejar post-acción
     *
     * NOTA: Esta función es llamada por Modelo.colocarPieza()
     */
    @Override
    public boolean colocarPieza(String posicion) throws RemoteException {
        if (!validarAccion(FaseJuego.COLOCACION)) {
            return false;
        }

        if (!tablero.colocarPieza(posicion, jugadorActual)) {
            return false;
        }

        procesarDespuesDeAccion(posicion);
        return true;
    }

    /**
     * MOVER PIEZA EN EL TABLERO
     *
     * Mueve una pieza del jugador actual de una posición a otra durante
     * la fase de movimiento del juego.
     *
     * REGLA ESPECIAL - VUELO:
     * Si el jugador tiene exactamente 3 piezas, puede "volar" a cualquier
     * posición vacía, no solo a posiciones adyacentes.
     *
     * FLUJO:
     * 1. Valida que no esté en fase COLOCACION y que esté EN_CURSO
     * 2. Determina si el jugador puede volar (tiene 3 piezas)
     * 3. Intenta mover la pieza en el tablero
     * 4. Procesa la acción:
     *    - Si forma molino: cambia a ESPERANDO_ELIMINAR
     *    - Si no: cambia turno
     *
     * @param origen Posición de origen de la pieza (ej: "A1")
     * @param destino Posición de destino de la pieza (ej: "A2")
     * @return true si la pieza se movió exitosamente, false en caso contrario
     * @throws RemoteException si hay error de comunicación RMI
     *
     * RELACIONES CON OTRAS FUNCIONES:
     * - Llama a jugadorActual.getPiezasEnTablero() para verificar si puede volar
     * - Llama a tablero.moverPieza(origen, destino, jugadorActual, puedeVolar)
     * - Llama a procesarDespuesDeAccion(destino) para manejar post-acción
     *
     * NOTA: Esta función es llamada por Modelo.moverPieza()
     */
    @Override
    public boolean moverPieza(String origen, String destino) throws RemoteException {
        if (faseActual == FaseJuego.COLOCACION || estadoJuego != EstadoJuego.EN_CURSO) {
            return false;
        }

        // El jugador puede volar solo si tiene exactamente 3 piezas (individual, no global)
        boolean puedeVolar = (jugadorActual.getPiezasEnTablero() == PIEZAS_PARA_VUELO);

        if (!tablero.moverPieza(origen, destino, jugadorActual, puedeVolar)) {
            return false;
        }

        procesarDespuesDeAccion(destino);
        return true;
    }

    /**
     * ELIMINAR PIEZA DEL OPONENTE
     *
     * Elimina una pieza del oponente después de que el jugador actual
     * haya formado un molino (3 en línea).
     *
     * REGLA IMPORTANTE:
     * No se puede eliminar una pieza que forme parte de un molino,
     * EXCEPTO si todas las piezas del oponente están en molinos.
     *
     * FLUJO:
     * 1. Verifica que el estado sea ESPERANDO_ELIMINAR
     * 2. Obtiene el jugador oponente
     * 3. Valida que la pieza NO esté en un molino (o todas estén en molinos)
     * 4. Elimina la pieza del tablero
     * 5. Vuelve al estado EN_CURSO
     * 6. Verifica condiciones de victoria:
     *    - Oponente tiene ≤2 piezas (y terminó de colocar)
     *    - Oponente no puede moverse
     * 7. Si no hay ganador: cambia turno y actualiza fase
     *
     * @param posicion Posición de la pieza del oponente a eliminar
     * @return true si se eliminó exitosamente, false en caso contrario
     * @throws RemoteException si hay error de comunicación RMI
     *
     * RELACIONES CON OTRAS FUNCIONES:
     * - Llama a getJugadorOponente() para obtener el oponente
     * - Llama a tablero.formaMolino(posicion, oponente) para validar
     * - Llama a todasLasPiezasEnMolino(oponente) para validar excepción
     * - Llama a tablero.eliminarPieza(posicion, oponente) para eliminar
     * - Llama a verificarCondicionesVictoria() para verificar fin del juego
     * - Llama a hayGanador() para verificar si terminó el juego
     * - Llama a cambiarTurno() si no hay ganador
     * - Llama a actualizarFase() para transicionar de COLOCACION a MOVIMIENTO
     *
     * NOTA: Esta función es llamada por Modelo.eliminarPiezaOponente()
     */
    @Override
    public boolean eliminarPiezaOponente(String posicion) throws RemoteException {
        if (estadoJuego != EstadoJuego.ESPERANDO_ELIMINAR) {
            return false;
        }

        IJugador oponente = getJugadorOponente();

        // Verificar que no esté en un molino a menos que todas las piezas del oponente estén en molinos
        if (tablero.formaMolino(posicion, oponente) && !todasLasPiezasEnMolino(oponente)) {
            return false;
        }

        if (!tablero.eliminarPieza(posicion, oponente)) {
            return false;
        }

        estadoJuego = EstadoJuego.EN_CURSO;

        verificarCondicionesVictoria();

        if (!hayGanador()) {
            cambiarTurno();
            actualizarFase();
        }

        return true;
    }

    /**
     * VERIFICAR SI ESTÁ ESPERANDO ELIMINAR
     *
     * Indica si el jugador actual debe eliminar una pieza del oponente
     * después de haber formado un molino.
     *
     * @return true si está esperando eliminar, false en caso contrario
     *
     * RELACIONES CON OTRAS FUNCIONES:
     * - Es llamada por Modelo.colocarPieza() y Modelo.moverPieza() para
     *   determinar qué evento notificar
     */
    @Override
    public boolean isEsperandoEliminar() {
        return estadoJuego == EstadoJuego.ESPERANDO_ELIMINAR;
    }

    // ===================================================================
    // MÉTODOS PRIVADOS AUXILIARES
    // ===================================================================

    /**
     * PROCESAR DESPUÉS DE UNA ACCIÓN (COLOCAR O MOVER)
     *
     * Maneja la lógica después de colocar o mover una pieza:
     * - Si se formó un molino: cambia a ESPERANDO_ELIMINAR
     * - Si no: cambia turno y actualiza la fase si es necesario
     *
     * @param posicionFinal Posición final de la pieza (donde quedó colocada o movida)
     *
     * RELACIONES CON OTRAS FUNCIONES:
     * - Llama a tablero.formaMolino(posicionFinal, jugadorActual) para detectar molino
     * - Llama a cambiarTurno() si no se formó molino
     * - Llama a actualizarFase() para transicionar de COLOCACION a MOVIMIENTO
     * - Es llamada por colocarPieza() y moverPieza()
     */
    private void procesarDespuesDeAccion(String posicionFinal) {
        if (tablero.formaMolino(posicionFinal, jugadorActual)) {
            estadoJuego = EstadoJuego.ESPERANDO_ELIMINAR;
        } else {
            cambiarTurno();
            actualizarFase();
        }
    }

    /**
     * VALIDAR ACCIÓN
     *
     * Verifica que se cumplan las precondiciones para realizar una acción:
     * - Que esté en la fase requerida (COLOCACION o MOVIMIENTO)
     * - Que el estado del juego sea EN_CURSO (no esperando eliminar ni finalizado)
     *
     * @param faseRequerida Fase en la que debe estar el juego
     * @return true si se puede realizar la acción, false en caso contrario
     *
     * RELACIONES CON OTRAS FUNCIONES:
     * - Es llamada por colocarPieza() con FaseJuego.COLOCACION
     */
    private boolean validarAccion(FaseJuego faseRequerida) {
        return faseActual == faseRequerida && estadoJuego == EstadoJuego.EN_CURSO;
    }

    /**
     * CAMBIAR TURNO AL OTRO JUGADOR
     *
     * Alterna el turno entre el jugador 0 y el jugador 1.
     *
     * RELACIONES CON OTRAS FUNCIONES:
     * - Es llamada por procesarDespuesDeAccion() cuando no se forma molino
     * - Es llamada por eliminarPiezaOponente() después de eliminar una pieza
     */
    private void cambiarTurno() {
        jugadorActual = (jugadorActual == jugadores.get(0)) ? jugadores.get(1) : jugadores.get(0);
    }

    /**
     * OBTENER JUGADOR OPONENTE
     *
     * Devuelve el jugador que NO es el jugador actual.
     *
     * @return El oponente del jugador actual
     *
     * RELACIONES CON OTRAS FUNCIONES:
     * - Es llamada por eliminarPiezaOponente() para obtener el oponente
     * - Es llamada por verificarCondicionesVictoria() para verificar victoria
     */
    private IJugador getJugadorOponente() {
        return (jugadorActual == jugadores.get(0)) ? jugadores.get(1) : jugadores.get(0);
    }

    /**
     * ACTUALIZAR FASE DEL JUEGO
     *
     * Transiciona de la fase COLOCACION a MOVIMIENTO cuando ambos jugadores
     * hayan colocado sus 9 piezas.
     *
     * NOTA IMPORTANTE:
     * La capacidad de VOLAR (mover a cualquier posición cuando un jugador tiene
     * 3 piezas) es INDIVIDUAL por jugador, no es una fase global de la partida.
     * Se verifica en moverPieza() y jugadorPuedeMoverse().
     *
     * RELACIONES CON OTRAS FUNCIONES:
     * - Llama a jugadores.get(0).getPiezasColocadas() para verificar jugador 1
     * - Llama a jugadores.get(1).getPiezasColocadas() para verificar jugador 2
     * - Es llamada por procesarDespuesDeAccion() y eliminarPiezaOponente()
     */
    private void actualizarFase() {
        // Solo transición de COLOCACION a MOVIMIENTO cuando ambos jugadores terminan de colocar
        if (faseActual == FaseJuego.COLOCACION &&
                jugadores.get(0).getPiezasColocadas() == PIEZAS_TOTALES_POR_JUGADOR &&
                jugadores.get(1).getPiezasColocadas() == PIEZAS_TOTALES_POR_JUGADOR) {
            faseActual = FaseJuego.MOVIMIENTO;
        }

        // NOTA: La capacidad de VOLAR es individual por jugador (cuando tiene 3 piezas),
        // no una fase global de la partida. Se verifica en moverPieza() y jugadorPuedeMoverse()
    }

    /**
     * VERIFICAR CONDICIONES DE VICTORIA
     *
     * Verifica si se cumple alguna de las condiciones de victoria:
     *
     * CONDICION 1: Oponente reducido a 2 o menos piezas
     * - Solo válido si el oponente ya colocó todas sus 9 piezas
     * - Durante COLOCACION no cuenta (podría tener 2 en tablero y 7 sin colocar)
     *
     * CONDICION 2: Oponente sin movimientos disponibles
     * - Solo en fase MOVIMIENTO
     * - El oponente no puede mover ninguna de sus piezas
     *
     * Si hay ganador, establece:
     * - ganador = jugadorActual
     * - estadoJuego = FINALIZADO
     * - estadoPartida = FINALIZADA
     *
     * RELACIONES CON OTRAS FUNCIONES:
     * - Llama a getJugadorOponente() para obtener el oponente
     * - Llama a oponente.getPiezasEnTablero() para contar piezas
     * - Llama a oponente.getPiezasColocadas() para verificar si terminó de colocar
     * - Llama a jugadorPuedeMoverse(oponente) para verificar movimientos
     * - Es llamada por eliminarPiezaOponente()
     */
    private void verificarCondicionesVictoria() {
        IJugador oponente = getJugadorOponente();

        // Gana si el oponente tiene ≤2 piezas SOLO SI YA COLOCÓ TODAS sus fichas
        // Durante la fase de colocación, aunque tenga 2 piezas en tablero, puede seguir colocando
        if (oponente.getPiezasEnTablero() <= PIEZAS_MINIMAS_PARA_PERDER &&
            oponente.getPiezasColocadas() == PIEZAS_TOTALES_POR_JUGADOR) {
            ganador = jugadorActual;
            estadoJuego = EstadoJuego.FINALIZADO;
            estadoPartida = EstadoPartida.FINALIZADA;
            return;
        }

        // Gana si el oponente no puede moverse (solo en fase de movimiento)
        if (faseActual != FaseJuego.COLOCACION && !jugadorPuedeMoverse(oponente)) {
            ganador = jugadorActual;
            estadoJuego = EstadoJuego.FINALIZADO;
            estadoPartida = EstadoPartida.FINALIZADA;
        }
    }

    /**
     * VERIFICAR SI UN JUGADOR PUEDE MOVERSE
     *
     * Determina si un jugador tiene al menos un movimiento válido disponible.
     *
     * REGLA ESPECIAL:
     * Si el jugador tiene exactamente 3 piezas, puede VOLAR a cualquier posición,
     * lo cual aumenta sus posibilidades de tener movimientos disponibles.
     *
     * @param jugador Jugador a verificar
     * @return true si el jugador puede mover al menos una pieza, false si no
     *
     * RELACIONES CON OTRAS FUNCIONES:
     * - Llama a jugador.getPiezasEnTablero() para verificar si puede volar
     * - Llama a tablero.getPosicionesOcupadasPor(jugador) para obtener piezas
     * - Llama a tablero.tieneMovimientosDisponibles(posicion, puedeVolar)
     * - Es llamada por verificarCondicionesVictoria()
     */
    private boolean jugadorPuedeMoverse(IJugador jugador) {
        // Verificar si el jugador puede volar (tiene 3 piezas) - esto es INDIVIDUAL
        boolean puedeVolar = (jugador.getPiezasEnTablero() == PIEZAS_PARA_VUELO);

        for (String posicion : tablero.getPosicionesOcupadasPor(jugador)) {
            if (tablero.tieneMovimientosDisponibles(posicion, puedeVolar)) {
                return true;
            }
        }
        return false;
    }

    /**
     * VERIFICAR SI TODAS LAS PIEZAS DEL JUGADOR ESTÁN EN MOLINOS
     *
     * Determina si todas las piezas de un jugador forman parte de molinos.
     * Esto es importante para la regla de eliminación:
     * - Normalmente NO se puede eliminar una pieza que esté en un molino
     * - EXCEPCIÓN: Si TODAS las piezas están en molinos, sí se puede eliminar
     *
     * @param jugador Jugador a verificar
     * @return true si todas sus piezas están en molinos, false si al menos una no lo está
     *
     * RELACIONES CON OTRAS FUNCIONES:
     * - Llama a tablero.getPosicionesOcupadasPor(jugador) para obtener piezas
     * - Llama a tablero.formaMolino(posicion, jugador) para cada pieza
     * - Es llamada por eliminarPiezaOponente() para validar eliminación
     */
    private boolean todasLasPiezasEnMolino(IJugador jugador) {
        for (String posicion : tablero.getPosicionesOcupadasPor(jugador)) {
            if (!tablero.formaMolino(posicion, jugador)) {
                return false;
            }
        }
        return true;
    }

    /**
     * OBTENER TABLERO
     *
     * Devuelve la referencia al tablero de la partida.
     *
     * @return El tablero de juego
     *
     * NOTA: Esta función NO está en la interfaz IPartida, es pública
     * para uso interno del modelo.
     */
    public Tablero getTablero() {
        return tablero;
    }

    /**
     * OBTENER ESTADO DEL TABLERO
     *
     * Construye un mapa con el estado actual del tablero, mostrando
     * qué jugador ocupa cada posición.
     *
     * Este método es útil para sincronizar el estado del tablero con
     * los clientes remotos, ya que solo envía la información esencial
     * (posición -> jugador) en lugar del objeto Tablero completo.
     *
     * @return Map con posiciones ocupadas y sus ocupantes
     *         Estructura: Map<Posicion, Jugador> (ej: {"A1": jugador1, "B2": jugador2})
     *         Solo incluye posiciones ocupadas, las libres no están en el mapa
     * @throws RemoteException si hay error de comunicación RMI
     *
     * RELACIONES CON OTRAS FUNCIONES:
     * - Llama a tablero.getPosiciones() para obtener todas las posiciones
     * - Llama a entry.getValue().estaLibre() para filtrar posiciones vacías
     * - Llama a entry.getValue().getOcupante() para obtener el jugador
     */
    @Override
    public java.util.Map<String, IJugador> getEstadoTablero() throws RemoteException {
        java.util.Map<String, IJugador> estado = new java.util.HashMap<>();

        for (java.util.Map.Entry<String, Posicion> entry : tablero.getPosiciones().entrySet()) {
            if (!entry.getValue().estaLibre()) {
                estado.put(entry.getKey(), entry.getValue().getOcupante());
            }
        }

        return estado;
    }
}
