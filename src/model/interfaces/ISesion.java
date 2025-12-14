package model.interfaces;

import model.excepciones.JugadorExistente;
import model.excepciones.JugadorNoExistente;
import model.excepciones.PasswordIncorrecta;
import java.io.Serializable;
import java.util.Map;

/**
 * Interfaz para la gestión de sesiones y usuarios
 */
public interface ISesion extends Serializable {
    void registrarse(String nombre, String password) throws JugadorExistente;
    void iniciarSesion(String nombre, String password) throws JugadorNoExistente, PasswordIncorrecta;
    boolean existeJugador(String nombre);
    IJugador getJugador(String nombre);
    Map<String, IJugador> getUsuarios();
}
