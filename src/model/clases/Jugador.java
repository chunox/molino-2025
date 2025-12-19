package model.clases;

import model.interfaces.IJugador;
import java.io.Serializable;

/**
 * ============================================================================
 * JUGADOR - INFORMACIÓN Y ESTADO DE UN JUGADOR
 * ============================================================================
 *
 * La clase Jugador encapsula toda la información de un jugador del juego del Molino.
 *
 * RESPONSABILIDADES:
 *
 * 1. IDENTIFICACIÓN:
 *    - Nombre único del jugador
 *    - Símbolo visual ('X' o 'O')
 *
 * 2. CONTADORES DE ESTADO:
 *    - piezasColocadas: Total de piezas que ha colocado (máximo 9)
 *    - piezasEnTablero: Piezas actualmente en el tablero (puede decrecer si le eliminan)
 *
 * DIFERENCIA ENTRE CONTADORES:
 *
 * - piezasColocadas: Contador que solo AUMENTA durante la fase COLOCACION
 *   - Empieza en 0, llega hasta 9 (una por cada pieza inicial)
 *   - NO disminuye cuando le eliminan piezas
 *   - Se usa para determinar cuándo termina la fase de colocación
 *
 * - piezasEnTablero: Contador de piezas ACTUALES en el tablero
 *   - Empieza en 0, llega hasta 9 durante colocación
 *   - SÍ disminuye cuando le eliminan piezas
 *   - Se usa para:
 *     * Determinar si puede volar (cuando tiene exactamente 3)
 *     * Verificar condición de derrota (si llega a 2 o menos)
 *
 * EJEMPLO DE USO:
 *   - Fase COLOCACION: El jugador coloca sus 9 piezas
 *     piezasColocadas: 0 -> 1 -> 2 -> ... -> 9
 *     piezasEnTablero: 0 -> 1 -> 2 -> ... -> 9
 *
 *   - Fase MOVIMIENTO: Le eliminan 4 piezas
 *     piezasColocadas: 9 (NO cambia)
 *     piezasEnTablero: 9 -> 8 -> 7 -> 6 -> 5
 *
 * SERIALIZABLE:
 * Implementa Serializable para poder ser enviado a través de RMI.
 */
public class Jugador implements IJugador, Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * Nombre del jugador (identificador único)
     */
    private String nombre;

    /**
     * Símbolo del jugador: 'X' para jugador 1, 'O' para jugador 2
     */
    private char simbolo;

    /**
     * Total de piezas que ha colocado (0-9, solo aumenta)
     */
    private int piezasColocadas;

    /**
     * Piezas actualmente en el tablero (puede aumentar y disminuir)
     */
    private int piezasEnTablero;

    /**
     * CONSTRUCTOR DEL JUGADOR
     *
     * Crea un nuevo jugador con nombre y símbolo específicos.
     * Inicializa los contadores de piezas en 0.
     *
     * @param nombre Nombre único del jugador
     * @param simbolo Símbolo visual del jugador ('X' o 'O')
     *
     * RELACIONES CON OTRAS FUNCIONES:
     * - Es llamado por Modelo.buscarPartida() para crear nuevos jugadores
     * - Es llamado por Partida constructor para crear jugadores de una partida
     */
    public Jugador(String nombre, char simbolo) {
        this.nombre = nombre;
        this.simbolo = simbolo;
        this.piezasColocadas = 0;
        this.piezasEnTablero = 0;
    }

    /**
     * OBTENER NOMBRE DEL JUGADOR
     *
     * @return Nombre del jugador
     */
    @Override
    public String getNombre() {
        return nombre;
    }

    /**
     * OBTENER SÍMBOLO DEL JUGADOR
     *
     * @return Símbolo del jugador ('X' o 'O')
     */
    @Override
    public char getSimbolo() {
        return simbolo;
    }

    /**
     * OBTENER TOTAL DE PIEZAS COLOCADAS
     *
     * Devuelve el total de piezas que el jugador ha colocado desde el inicio.
     * Este contador NO disminuye cuando le eliminan piezas.
     *
     * @return Número de piezas colocadas (0-9)
     *
     * RELACIONES CON OTRAS FUNCIONES:
     * - Es llamado por Partida.actualizarFase() para verificar si terminó la colocación
     * - Es llamado por Partida.verificarCondicionesVictoria() para validar victoria
     */
    @Override
    public int getPiezasColocadas() {
        return piezasColocadas;
    }

    /**
     * OBTENER PIEZAS ACTUALMENTE EN EL TABLERO
     *
     * Devuelve la cantidad de piezas del jugador que están actualmente en el tablero.
     * Este contador disminuye cuando le eliminan piezas.
     *
     * @return Número de piezas en el tablero (0-9)
     *
     * RELACIONES CON OTRAS FUNCIONES:
     * - Es llamado por Partida.moverPieza() para verificar si puede volar (==3)
     * - Es llamado por Partida.verificarCondicionesVictoria() para verificar derrota (<=2)
     * - Es llamado por Partida.jugadorPuedeMoverse() para verificar vuelo
     */
    @Override
    public int getPiezasEnTablero() {
        return piezasEnTablero;
    }

    /**
     * INCREMENTAR PIEZAS COLOCADAS
     *
     * Incrementa ambos contadores cuando el jugador COLOCA una pieza nueva
     * durante la fase de COLOCACION.
     *
     * IMPORTANTE: Solo se usa durante la fase de colocación.
     *
     * RELACIONES CON OTRAS FUNCIONES:
     * - Es llamado por Tablero.colocarPieza() cuando se coloca una pieza
     */
    @Override
    public void incrementarPiezasColocadas() {
        piezasColocadas++;
        piezasEnTablero++;
    }

    /**
     * DECREMENTAR PIEZAS EN TABLERO
     *
     * Decrementa el contador de piezas en tablero cuando se elimina una pieza.
     * NO decrementa piezasColocadas.
     *
     * RELACIONES CON OTRAS FUNCIONES:
     * - Es llamado por Tablero.eliminarPieza() cuando se elimina una pieza
     */
    @Override
    public void decrementarPiezasEnTablero() {
        piezasEnTablero--;
    }

    /**
     * COMPARAR IGUALDAD ENTRE JUGADORES
     *
     * Dos jugadores son iguales si tienen el mismo nombre.
     *
     * @param o Objeto a comparar
     * @return true si tienen el mismo nombre, false en caso contrario
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Jugador jugador = (Jugador) o;
        return nombre.equals(jugador.nombre);
    }

    //deprecado
    /**
     * OBTENER CÓDIGO HASH
     *
     * Usa el nombre como base para el hashCode.
     *
     * @return Código hash basado en el nombre
     */
    @Override
    public int hashCode() {
        return nombre.hashCode();
    }

    /**
     * REPRESENTACIÓN EN TEXTO DEL JUGADOR
     *
     * @return String en formato "Nombre (Símbolo)" (ej: "Juan (X)")
     */
    @Override
    public String toString() {
        return nombre + " (" + simbolo + ")";
    }
}
