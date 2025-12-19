package model.enums;

/**
 * FASE DEL JUEGO
 * Define las fases del juego del Molino.
 *
 * CICLO DE JUEGO:
 * 1. COLOCACION: Cada jugador coloca sus 9 piezas por turnos
 * 2. MOVIMIENTO: Los jugadores mueven piezas a posiciones adyacentes
 *
 * NOTA IMPORTANTE SOBRE VUELO:
 * - VUELO no es una fase global de la partida
 * - Es una capacidad INDIVIDUAL que tiene un jugador cuando le quedan exactamente 3 piezas
 * - Permite mover a cualquier posición libre (no solo adyacentes)
 * - Se verifica en Partida.moverPieza() y Partida.jugadorPuedeMoverse()
 *
 * RELACIONES: Usado por Partida para controlar qué acciones son válidas
 */
public enum FaseJuego {
    COLOCACION,     // Fase de colocación de piezas (inicial) - Cada jugador coloca sus 9 piezas
    MOVIMIENTO,     // Fase de movimiento de piezas - Solo a adyacentes (excepto si puede volar)
    VUELO           // NOTA: Actualmente no se usa como fase global (ver comentario arriba)
}
