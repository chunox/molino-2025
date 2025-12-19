package model.clases;

import model.interfaces.IJugador;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * MOLINO - Representa una formación de 3 piezas en línea
 * Define las 3 posiciones que forman un molino válido (horizontal o vertical).
 * El tablero tiene 16 molinos posibles: 8 horizontales + 8 verticales.
 * RELACIONES: Usado por Tablero.formaMolino() para detectar molinos.
 */
public class Molino implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String pos1;  // Primera posición del molino
    private final String pos2;  // Segunda posición del molino
    private final String pos3;  // Tercera posición del molino

    /**
     * CONSTRUCTOR
     * Crea un molino con las 3 posiciones especificadas.
     * @param pos1 Primera posición (ej: "A1")
     * @param pos2 Segunda posición (ej: "D1")
     * @param pos3 Tercera posición (ej: "G1")
     * RELACIONES: Llamado por Tablero.inicializarMolinos()
     */
    public Molino(String pos1, String pos2, String pos3) {
        this.pos1 = pos1;
        this.pos2 = pos2;
        this.pos3 = pos3;
    }

    /**
     * VERIFICAR SI CONTIENE UNA POSICIÓN
     * Verifica si una posición dada forma parte de este molino.
     * @param posicion Posición a verificar
     * @return true si la posición es una de las 3 del molino
     * RELACIONES: Llamado por Tablero.formaMolino()
     */
    public boolean contiene(String posicion) {
        return pos1.equals(posicion) || pos2.equals(posicion) || pos3.equals(posicion);
    }

    /**
     * VERIFICAR SI ESTÁ FORMADO POR UN JUGADOR
     * Verifica si las 3 posiciones del molino están ocupadas por el mismo jugador.
     * @param jugador Jugador a verificar
     * @param posiciones Mapa de todas las posiciones del tablero
     * @return true si las 3 posiciones están ocupadas por el jugador
     * RELACIONES: Llamado por Tablero.formaMolino()
     */
    public boolean estaFormadoPor(IJugador jugador, Map<String, Posicion> posiciones) {
        return posiciones.get(pos1).ocupadaPor(jugador) &&
                posiciones.get(pos2).ocupadaPor(jugador) &&
                posiciones.get(pos3).ocupadaPor(jugador);
    }

    /**
     * OBTENER POSICIONES DEL MOLINO
     * @return Lista con las 3 posiciones que forman el molino
     */
    public List<String> getPosiciones() {
        return List.of(pos1, pos2, pos3);
    }

    /** @return Representación en texto del molino */
    @Override
    public String toString() {
        return "Molino[" + pos1 + ", " + pos2 + ", " + pos3 + "]";
    }
}
