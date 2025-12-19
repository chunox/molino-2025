package model.clases;

import model.interfaces.IJugador;
import java.io.Serializable;

/**
 * POSICION - Representa una casilla del tablero
 * Cada posición tiene un ID (ej: "A1", "D3") y puede estar libre u ocupada por un jugador.
 * RELACIONES: Usada por Tablero para gestionar el estado de cada casilla.
 */
public class Posicion implements Serializable {
    private static final long serialVersionUID = 1L;

    private String id;          // ID de la posición (ej: "A1", "B2", etc.)
    private IJugador ocupante;  // Jugador que ocupa la posición (null si está libre)

    /**
     * CONSTRUCTOR
     * Crea una posición libre con el ID especificado.
     * @param id Identificador de la posición
     * RELACIONES: Llamado por Tablero.inicializarPosiciones()
     */
    public Posicion(String id) {
        this.id = id;
        this.ocupante = null;
    }

    /** @return ID de la posición */
    public String getId() {
        return id;
    }

    /**
     * @return Jugador que ocupa la posición, o null si está libre
     * RELACIONES: Llamado por Partida.getEstadoTablero()
     */
    public IJugador getOcupante() {
        return ocupante;
    }

    /**
     * OCUPAR - Marca la posición como ocupada por un jugador
     * @param jugador Jugador que ocupará la posición
     * RELACIONES: Llamado por Tablero.colocarPieza() y Tablero.moverPieza()
     */
    public void ocupar(IJugador jugador) {
        this.ocupante = jugador;
    }

    /**
     * LIBERAR - Marca la posición como libre
     * RELACIONES: Llamado por Tablero.eliminarPieza() y Tablero.moverPieza()
     */
    public void liberar() {
        this.ocupante = null;
    }

    /**
     * VERIFICAR SI ESTÁ LIBRE
     * @return true si no hay ningún jugador en esta posición
     * RELACIONES: Llamado por Tablero.colocarPieza(), Tablero.moverPieza(),
     *             Tablero.tieneMovimientosDisponibles()
     */
    public boolean estaLibre() {
        return ocupante == null;
    }

    /**
     * VERIFICAR SI ESTÁ OCUPADA POR UN JUGADOR ESPECÍFICO
     * @param jugador Jugador a verificar
     * @return true si este jugador ocupa la posición
     * RELACIONES: Llamado por Tablero.moverPieza(), Tablero.eliminarPieza(),
     *             Tablero.getPosicionesOcupadasPor(), Molino.estaFormadoPor()
     */
    public boolean ocupadaPor(IJugador jugador) {
        return ocupante != null && ocupante.equals(jugador);
    }
}
