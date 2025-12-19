package model.enums;

/**
 * ESTADOS DE LA INTERFAZ DE USUARIO
 * Define los diferentes estados en los que puede estar la interfaz del cliente.
 * RELACIONES: Usado por la vista para controlar qué pantalla mostrar.
 */
public enum Estados {
    EN_MENU,                    // Pantalla del menú principal
    EN_ESPERANDO_JUGADORES,     // Esperando que otro jugador se una
    EN_JUEGO                    // En una partida activa
}
