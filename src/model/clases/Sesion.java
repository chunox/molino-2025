package model.clases;

import model.interfaces.IJugador;
import model.interfaces.ISesion;
import serializacion.Serializador;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Clase Singleton para gestionar las sesiones y usuarios
 */
public class Sesion implements ISesion, Serializable {
    private static final long serialVersionUID = 1L;
    private static ISesion instancia = null;
    private Serializador serializador = new Serializador("src/data/usuarios.dat");
    private Map<String, IJugador> usuarios;

    public static ISesion getInstancia() {
        if (instancia == null) {
            instancia = new Sesion();
        }
        return instancia;
    }

    private Sesion() {
        Object obj = serializador.readFirstObject();
        if (obj == null) {
            usuarios = new HashMap<>();
            serializador.writeOneObject(usuarios);
        } else {
            usuarios = (Map<String, IJugador>) obj;
        }
    }

    @Override
    public boolean existeJugador(String nombre) {
        return usuarios.containsKey(nombre);
    }

    @Override
    public IJugador getJugador(String nombre) {
        return usuarios.get(nombre);
    }

    @Override
    public Map<String, IJugador> getUsuarios() {
        return new HashMap<>(usuarios);
    }
}
