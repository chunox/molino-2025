package model.enums;

/**
 * Enumeración que representa los diferentes eventos que pueden ocurrir en el juego
 * y que deben notificarse a los observadores
 */
public enum Eventos {
    // Eventos del juego
    CAMBIO_TURNO,
    ACTUALIZACION_ESTADO,
    PIEZA_COLOCADA,
    PIEZA_MOVIDA,
    PIEZA_ELIMINADA,
    FORMACION_MOLINO,

    // Eventos de finalización
    GAME_OVER,
    GAME_WIN
}
