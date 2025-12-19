package model.enums;

/**
 * ESTADO DE LA PARTIDA
 * Define los estados de una partida desde su creación hasta su fin.
 *
 * CICLO DE VIDA:
 * 1. EN_ESPERA: Partida creada con 1 jugador, esperando al segundo
 * 2. EN_JUEGO: Partida completa (2 jugadores), en curso
 * 3. FINALIZADA: Partida terminada, hay un ganador
 *
 * RELACIONES: Usado por Partida para gestionar el ciclo de vida de la partida
 */
public enum EstadoPartida {
    EN_ESPERA,      // Esperando segundo jugador - Cambia a EN_JUEGO cuando se agrega el segundo jugador
    EN_JUEGO,       // Partida en curso con 2 jugadores - Cambia a FINALIZADA cuando hay ganador
    FINALIZADA      // Partida terminada, hay un ganador
}
