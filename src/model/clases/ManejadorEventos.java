package model.clases;

import model.enums.Eventos;
import model.interfaces.IManejadorEventos;
import java.io.Serializable;

/**
 * Clase que encapsula información sobre eventos del juego
 */
public class ManejadorEventos implements IManejadorEventos, Serializable {
    private static final long serialVersionUID = 1L;

    private final int id;
    private final Eventos evento;

    public ManejadorEventos(int id, Eventos evento) {
        this.id = id;
        this.evento = evento;
    }

    @Override
    public Eventos getEvento() {
        return evento;
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public boolean esIgual(int id) {
        return this.id == id;
    }

    @Override
    public String toString() {
        return "ManejadorEventos{" +
                "id=" + id +
                ", evento=" + evento +
                '}';
    }
}
