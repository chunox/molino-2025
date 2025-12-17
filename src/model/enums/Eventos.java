package model.enums;

/**
 * Enumeración que representa los diferentes eventos que pueden ocurrir en el juego
 * y que deben notificarse a los observadores
 */
public enum Eventos {
    // Eventos de lobby/partida
    CAMBIO_BUSCAR_PARTIDA,
    CAMBIO_ESPERANDO_JUGADORES,

    // Eventos del juego
    CAMBIO_TURNO,
    ACTUALIZACION_ESTADO,
    PIEZA_COLOCADA,
    PIEZA_MOVIDA,
    PIEZA_ELIMINADA,
    FORMACION_MOLINO,

    // Eventos de conexión
    DESCONEXION_E,      // Desconexión en espera
    DESCONEXION_J,      // Desconexión en juego

    // Eventos de finalización
    GAME_OVER,
    GAME_WIN
}
