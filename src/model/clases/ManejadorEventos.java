package model.clases;

import model.enums.Eventos;
import model.interfaces.IManejadorEventos;
import java.io.Serializable;

/**
 * MANEJADOR DE EVENTOS - Notificaciones del patrón Observer
 * Encapsula la información de un evento del juego para ser enviado a todos los clientes.
 * Se usa con el patrón Observer Distribuido (ObservableRemoto).
 *
 * FLUJO:
 * 1. El Modelo detecta un cambio (ej: pieza colocada, turno cambiado)
 * 2. Crea un ManejadorEventos con el ID de partida y el tipo de evento
 * 3. Llama a notificarObservadores(manejadorEventos)
 * 4. TODOS los clientes reciben el evento en su método actualizar()
 * 5. Los clientes filtran por ID de partida y actualizan su interfaz
 *
 * RELACIONES: Creado por Modelo.colocarPieza(), Modelo.moverPieza(),
 *             Modelo.eliminarPiezaOponente(), Modelo.buscarPartida()
 *             Recibido por los clientes en Controller.actualizar()
 */
public class ManejadorEventos implements IManejadorEventos, Serializable {
    private static final long serialVersionUID = 1L;

    private final int id;           // ID de la partida afectada
    private final Eventos evento;   // Tipo de evento (PIEZA_COLOCADA, CAMBIO_TURNO, etc.)

    /**
     * CONSTRUCTOR
     * @param id ID de la partida
     * @param evento Tipo de evento que ocurrió
     * RELACIONES: Llamado por Modelo al notificar eventos
     */
    public ManejadorEventos(int id, Eventos evento) {
        this.id = id;
        this.evento = evento;
    }

    /**
     * OBTENER TIPO DE EVENTO
     * @return El tipo de evento (PIEZA_COLOCADA, CAMBIO_TURNO, etc.)
     * RELACIONES: Llamado por Controller.actualizar() para procesar el evento
     */
    @Override
    public Eventos getEvento() {
        return evento;
    }

    /**
     * OBTENER ID DE PARTIDA
     * @return ID de la partida afectada
     * RELACIONES: Llamado por Controller.actualizar() para filtrar eventos
     */
    @Override
    public int getId() {
        return id;
    }

    /** @return Representación en texto del evento */
    @Override
    public String toString() {
        return "ManejadorEventos{" +
                "id=" + id +
                ", evento=" + evento +
                '}';
    }
}
