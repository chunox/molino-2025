package model.interfaces;

import model.enums.Eventos;
import java.io.Serializable;

/**
 * Interfaz para el manejador de eventos
 */
public interface IManejadorEventos extends Serializable {
    Eventos getEvento();
    int getId();
}
