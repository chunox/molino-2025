package model.enums;

/**
 * EVENTOS DEL JUEGO - Notificaciones del patrón Observer
 * Define los tipos de eventos que el Modelo notifica a todos los clientes.
 *
 * FLUJO: Modelo genera evento → ManejadorEventos → Controller.actualizar() → Vista actualiza
 *
 * RELACIONES:
 * - Usado por ManejadorEventos para identificar el tipo de evento
 * - Generado por Modelo.colocarPieza(), moverPieza(), eliminarPiezaOponente(), buscarPartida()
 * - Procesado por Controller.actualizar() para actualizar la vista
 */
public enum Eventos {
    // Eventos del juego
    CAMBIO_TURNO,       // El turno cambió a otro jugador - Generado por: colocarPieza(), moverPieza(), eliminarPiezaOponente(), buscarPartida()
    PIEZA_COLOCADA,     // Se colocó una pieza - Generado por: colocarPieza()
    PIEZA_MOVIDA,       // Se movió una pieza - Generado por: moverPieza()
    PIEZA_ELIMINADA,    // Se eliminó una pieza del oponente - Generado por: eliminarPiezaOponente()
    FORMACION_MOLINO,   // Se formó un molino (3 en línea) - Generado por: colocarPieza(), moverPieza()

    // Eventos de finalización
    GAME_WIN            // Hay un ganador - Generado por: eliminarPiezaOponente()
}
