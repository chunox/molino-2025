package model.clases;

import model.interfaces.IJugador;
import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

/**
 * ============================================================================
 * TABLERO - GESTIÓN DEL TABLERO DEL JUEGO DEL MOLINO
 * ============================================================================
 *
 * El Tablero representa la estructura del juego del Molino (Nine Men's Morris).
 * Es un tablero con 24 posiciones organizadas en 3 cuadrados concéntricos.
 *
 * ESTRUCTURA DEL TABLERO:
 *
 *   A1-------D1-------G1
 *   |  B2----D2----F2  |
 *   |  |  C3-D3-E3  |  |
 *   A4-B4-C4   E4-F4-G4
 *   |  |  C5-D5-E5  |  |
 *   |  B6----D6----F6  |
 *   A7-------D7-------G7
 *
 * RESPONSABILIDADES:
 *
 * 1. GESTIÓN DE POSICIONES:
 *    - 24 posiciones identificadas por su código (A1-G7)
 *    - Cada posición puede estar libre u ocupada por un jugador
 *
 * 2. GESTIÓN DE ADYACENCIAS:
 *    - Define qué posiciones están conectadas entre sí
 *    - Necesario para validar movimientos (solo a posiciones adyacentes)
 *
 * 3. GESTIÓN DE MOLINOS:
 *    - Define los 16 molinos posibles (3 piezas en línea)
 *    - 8 molinos horizontales + 8 molinos verticales
 *    - Detecta cuándo se forma un molino
 *
 * 4. OPERACIONES:
 *    - Colocar piezas
 *    - Mover piezas (con o sin vuelo)
 *    - Eliminar piezas
 *    - Verificar formación de molinos
 *    - Verificar movimientos disponibles
 *
 * SERIALIZABLE:
 * Implementa Serializable para poder ser enviado a través de RMI.
 */
public class Tablero implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * Mapa de todas las posiciones del tablero
     * Clave: ID de la posición (ej: "A1", "D3")
     * Valor: Objeto Posicion con estado (libre/ocupada)
     */
    private final Map<String, Posicion> posiciones;

    /**
     * Mapa de adyacencias (conexiones entre posiciones)
     * Clave: ID de la posición
     * Valor: Lista de posiciones adyacentes/conectadas
     */
    private final Map<String, List<String>> adyacencias;

    /**
     * Lista de todos los molinos posibles (16 total: 8 horizontales + 8 verticales)
     */
    private final List<Molino> molinos;

    /**
     * CONSTRUCTOR DEL TABLERO
     *
     * Crea un tablero vacío e inicializa:
     * - Las 24 posiciones del juego
     * - Las conexiones entre posiciones adyacentes
     * - Los 16 molinos posibles
     *
     * RELACIONES CON OTRAS FUNCIONES:
     * - Llama a inicializarPosiciones() para crear las 24 posiciones
     * - Llama a inicializarAdyacencias() para definir conexiones
     * - Llama a inicializarMolinos() para definir los 16 molinos
     * - Es llamado por el constructor de Partida
     */
    public Tablero() {
        this.posiciones = new HashMap<>();
        this.adyacencias = new HashMap<>();
        this.molinos = new ArrayList<>();

        inicializarPosiciones();
        inicializarAdyacencias();
        inicializarMolinos();
    }

    /**
     * INICIALIZAR LAS 24 POSICIONES DEL TABLERO
     *
     * Crea las 24 posiciones del juego organizadas en 3 cuadrados concéntricos:
     * - Cuadrado exterior: A1, D1, G1, A4, G4, A7, D7, G7
     * - Cuadrado medio: B2, D2, F2, B4, F4, B6, D6, F6
     * - Cuadrado interior: C3, D3, E3, C4, E4, C5, D5, E5
     *
     * RELACIONES: Llamada por el constructor Tablero()
     */
    private void inicializarPosiciones() {
        String[] ids = {
                "A1", "D1", "G1",
                "B2", "D2", "F2",
                "C3", "D3", "E3",
                "A4", "B4", "C4", "E4", "F4", "G4",
                "C5", "D5", "E5",
                "B6", "D6", "F6",
                "A7", "D7", "G7"
        };

        for (String id : ids) {
            posiciones.put(id, new Posicion(id));
        }
    }

    /**
     * INICIALIZAR ADYACENCIAS (CONEXIONES ENTRE POSICIONES)
     *
     * Define todas las conexiones válidas entre posiciones del tablero.
     * Solo se puede mover una pieza a una posición adyacente (a menos que pueda volar).
     *
     * RELACIONES: Llamada por el constructor Tablero(), llama a agregarAdyacencia()
     */
    private void inicializarAdyacencias() {
        // Fila superior exterior
        agregarAdyacencia("A1", "D1", "A4");
        agregarAdyacencia("D1", "A1", "G1", "D2");
        agregarAdyacencia("G1", "D1", "G4");

        // Fila superior media
        agregarAdyacencia("B2", "D2", "B4");
        agregarAdyacencia("D2", "B2", "F2", "D1", "D3");
        agregarAdyacencia("F2", "D2", "F4");

        // Fila superior interior
        agregarAdyacencia("C3", "D3", "C4");
        agregarAdyacencia("D3", "C3", "E3", "D2");
        agregarAdyacencia("E3", "D3", "E4");

        // Fila central
        agregarAdyacencia("A4", "A1", "B4", "A7");
        agregarAdyacencia("B4", "A4", "B2", "C4", "B6");
        agregarAdyacencia("C4", "B4", "C3", "C5");
        agregarAdyacencia("E4", "E3", "F4", "E5");
        agregarAdyacencia("F4", "E4", "F2", "G4", "F6");
        agregarAdyacencia("G4", "F4", "G1", "G7");

        // Fila inferior interior
        agregarAdyacencia("C5", "C4", "D5");
        agregarAdyacencia("D5", "C5", "E5", "D6");
        agregarAdyacencia("E5", "D5", "E4");

        // Fila inferior media
        agregarAdyacencia("B6", "B4", "D6");
        agregarAdyacencia("D6", "B6", "F6", "D5", "D7");
        agregarAdyacencia("F6", "D6", "F4");

        // Fila inferior exterior
        agregarAdyacencia("A7", "A4", "D7");
        agregarAdyacencia("D7", "A7", "G7", "D6");
        agregarAdyacencia("G7", "D7", "G4");
    }

    /**
     * AGREGAR ADYACENCIA
     *
     * Registra las posiciones adyacentes (conectadas) de una posición específica.
     *
     * @param posicion Posición origen
     * @param adyacentes Lista de posiciones conectadas a la posición origen
     *
     * RELACIONES: Llamada por inicializarAdyacencias()
     */
    private void agregarAdyacencia(String posicion, String... adyacentes) {
        this.adyacencias.put(posicion, List.of(adyacentes));
    }

    /**
     * INICIALIZAR MOLINOS
     *
     * Define los 16 molinos posibles del juego:
     * - 8 molinos horizontales (filas)
     * - 8 molinos verticales (columnas)
     *
     * Un molino se forma cuando un jugador tiene 3 piezas en línea.
     *
     * RELACIONES: Llamada por el constructor Tablero()
     */
    private void inicializarMolinos() {
        // Horizontales
        molinos.add(new Molino("A1", "D1", "G1"));
        molinos.add(new Molino("B2", "D2", "F2"));
        molinos.add(new Molino("C3", "D3", "E3"));
        molinos.add(new Molino("A4", "B4", "C4"));
        molinos.add(new Molino("E4", "F4", "G4"));
        molinos.add(new Molino("C5", "D5", "E5"));
        molinos.add(new Molino("B6", "D6", "F6"));
        molinos.add(new Molino("A7", "D7", "G7"));

        // Verticales
        molinos.add(new Molino("A1", "A4", "A7"));
        molinos.add(new Molino("B2", "B4", "B6"));
        molinos.add(new Molino("C3", "C4", "C5"));
        molinos.add(new Molino("D1", "D2", "D3"));
        molinos.add(new Molino("D5", "D6", "D7"));
        molinos.add(new Molino("E3", "E4", "E5"));
        molinos.add(new Molino("F2", "F4", "F6"));
        molinos.add(new Molino("G1", "G4", "G7"));
    }

    /**
     * COLOCAR PIEZA
     * Coloca una pieza del jugador en una posición específica durante la fase de colocación.
     * @param id Posición donde colocar (ej: "A1")
     * @param jugador Jugador que coloca la pieza
     * @return true si se colocó exitosamente, false si la posición no existe o está ocupada
     * RELACIONES: Llamada por Partida.colocarPieza(), llama a jugador.incrementarPiezasColocadas()
     */
    public boolean colocarPieza(String id, IJugador jugador) {
        Posicion pos = posiciones.get(id);
        if (pos == null || !pos.estaLibre()) {
            return false;
        }

        pos.ocupar(jugador);
        jugador.incrementarPiezasColocadas();
        return true;
    }

    /**
     * MOVER PIEZA
     * Mueve una pieza del jugador de una posición a otra. Si el jugador puede volar (tiene 3 piezas),
     * puede moverse a cualquier posición libre. Si no, solo a posiciones adyacentes.
     * @param origen Posición de origen
     * @param destino Posición de destino
     * @param jugador Jugador que mueve
     * @param puedeVolar true si el jugador tiene 3 piezas y puede volar
     * @return true si se movió exitosamente
     * RELACIONES: Llamada por Partida.moverPieza(), llama a esAdyacente()
     */
    public boolean moverPieza(String origen, String destino, IJugador jugador, boolean puedeVolar) {
        Posicion posOrigen = posiciones.get(origen);
        Posicion posDestino = posiciones.get(destino);

        if (posOrigen == null || posDestino == null) {
            return false;
        }

        if (!posOrigen.ocupadaPor(jugador) || !posDestino.estaLibre()) {
            return false;
        }

        // Si no puede volar, verificar que sea adyacente
        if (!puedeVolar && !esAdyacente(origen, destino)) {
            return false;
        }

        posOrigen.liberar();
        posDestino.ocupar(jugador);
        return true;
    }

    /**
     * ELIMINAR PIEZA
     * Elimina una pieza del jugador del tablero.
     * @param id Posición de la pieza a eliminar
     * @param jugador Jugador dueño de la pieza
     * @return true si se eliminó exitosamente
     * RELACIONES: Llamada por Partida.eliminarPiezaOponente(), llama a jugador.decrementarPiezasEnTablero()
     */
    public boolean eliminarPieza(String id, IJugador jugador) {
        Posicion pos = posiciones.get(id);
        if (pos == null || !pos.ocupadaPor(jugador)) {
            return false;
        }

        pos.liberar();
        jugador.decrementarPiezasEnTablero();
        return true;
    }

    /**
     * VERIFICAR SI UNA POSICIÓN FORMA MOLINO
     * Verifica si la pieza en una posición forma parte de un molino (3 en línea) del jugador.
     * @param posicion Posición a verificar
     * @param jugador Jugador propietario
     * @return true si forma parte de un molino
     * RELACIONES: Llamada por Partida.procesarDespuesDeAccion(), Partida.eliminarPiezaOponente(),
     *             Partida.todasLasPiezasEnMolino()
     */
    public boolean formaMolino(String posicion, IJugador jugador) {
        for (Molino molino : molinos) {
            if (molino.contiene(posicion) && molino.estaFormadoPor(jugador, posiciones)) {
                return true;
            }
        }
        return false;
    }

    /**
     * VERIFICAR SI DOS POSICIONES SON ADYACENTES
     * @param pos1 Primera posición
     * @param pos2 Segunda posición
     * @return true si están conectadas/adyacentes
     * RELACIONES: Llamada por moverPieza()
     */
    public boolean esAdyacente(String pos1, String pos2) {
        List<String> adyacentesPos1 = adyacencias.get(pos1);
        return adyacentesPos1 != null && adyacentesPos1.contains(pos2);
    }

    /**
     * OBTENER POSICIONES OCUPADAS POR UN JUGADOR
     * Devuelve una lista con todas las posiciones donde el jugador tiene piezas.
     * @param jugador Jugador a buscar
     * @return Lista de IDs de posiciones ocupadas por el jugador
     * RELACIONES: Llamada por Partida.jugadorPuedeMoverse(), Partida.todasLasPiezasEnMolino()
     */
    public List<String> getPosicionesOcupadasPor(IJugador jugador) {
        return posiciones.entrySet().stream()
                .filter(entry -> entry.getValue().ocupadaPor(jugador))
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    /**
     * VERIFICAR SI UNA POSICIÓN TIENE MOVIMIENTOS DISPONIBLES
     * Verifica si desde una posición se puede mover a algún lugar.
     * @param posicion Posición origen
     * @param puedeVolar Si el jugador puede volar (tiene 3 piezas)
     * @return true si tiene al menos un movimiento válido
     * RELACIONES: Llamada por Partida.jugadorPuedeMoverse()
     */
    public boolean tieneMovimientosDisponibles(String posicion, boolean puedeVolar) {
        if (puedeVolar) {
            // Puede moverse a cualquier posición libre
            return posiciones.values().stream().anyMatch(Posicion::estaLibre);
        } else {
            // Solo puede moverse a adyacentes libres
            List<String> adyacentesPosicion = adyacencias.get(posicion);
            if (adyacentesPosicion == null) {
                return false;
            }

            return adyacentesPosicion.stream()
                    .map(posiciones::get)
                    .anyMatch(Posicion::estaLibre);
        }
    }

    /**
     * OBTENER MAPA DE POSICIONES
     * Devuelve un mapa inmutable de todas las posiciones del tablero.
     * @return Mapa de posiciones (no modificable)
     * RELACIONES: Llamada por Partida.getEstadoTablero()
     */
    public Map<String, Posicion> getPosiciones() {
        return Collections.unmodifiableMap(posiciones);
    }

}
