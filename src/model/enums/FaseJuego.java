package model.enums;

/**
 * Enumeración que representa las fases del juego del Molino
 */
public enum FaseJuego {
    COLOCACION,     // Fase de colocación de piezas (inicial)
    MOVIMIENTO,     // Fase de movimiento de piezas (solo a adyacentes)
    VUELO           // Fase de vuelo (cuando un jugador tiene 3 piezas o menos)
}
