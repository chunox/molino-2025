package model.interfaces;

import java.io.Serializable;
import java.util.Map;

/**
 * Interfaz para la gestión de sesiones y usuarios
 */
public interface ISesion extends Serializable {
    boolean existeJugador(String nombre);
    IJugador getJugador(String nombre);
    Map<String, IJugador> getUsuarios();
}
