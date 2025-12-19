package model.clases;

import model.interfaces.IJugador;
import model.interfaces.ISesion;
import serializacion.Serializador;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * SESION - Gestión de usuarios persistente (Singleton)
 * Almacena la información de todos los usuarios registrados y la persiste en archivo.
 * PATRÓN SINGLETON: Solo existe una instancia compartida por todo el servidor.
 * PERSISTENCIA: Los datos se guardan en src/data/usuarios.dat.
 * RELACIONES: Usado por Modelo para gestionar usuarios.
 * NOTA: Actualmente no se usa activamente en el código, está disponible para futuras funcionalidades.
 */
public class Sesion implements ISesion, Serializable {
    private static final long serialVersionUID = 1L;
    private static ISesion instancia = null;                                    // Instancia única (Singleton)
    private Serializador serializador = new Serializador("src/data/usuarios.dat");  // Persistencia
    private Map<String, IJugador> usuarios;                                     // Map<NombreUsuario, Jugador>

    /**
     * OBTENER INSTANCIA (Singleton)
     * @return La instancia única de Sesion
     * RELACIONES: Llamado por Modelo constructor
     */
    public static ISesion getInstancia() {
        if (instancia == null) {
            instancia = new Sesion();
        }
        return instancia;
    }

    /**
     * CONSTRUCTOR PRIVADO (Singleton)
     * Carga los usuarios desde el archivo usuarios.dat si existe.
     * Si no existe, crea un mapa vacío y lo guarda.
     * RELACIONES: Llama a serializador.readFirstObject() y serializador.writeOneObject()
     */
    private Sesion() {
        Object obj = serializador.readFirstObject();
        if (obj == null) {
            usuarios = new HashMap<>();
            serializador.writeOneObject(usuarios);
        } else {
            usuarios = (Map<String, IJugador>) obj;
        }
    }

    /**
     * VERIFICAR SI EXISTE UN JUGADOR
     * @param nombre Nombre del jugador
     * @return true si el jugador está registrado
     */
    @Override
    public boolean existeJugador(String nombre) {
        return usuarios.containsKey(nombre);
    }

    /**
     * OBTENER JUGADOR POR NOMBRE
     * @param nombre Nombre del jugador
     * @return El jugador, o null si no existe
     */
    @Override
    public IJugador getJugador(String nombre) {
        return usuarios.get(nombre);
    }

    /**
     * OBTENER TODOS LOS USUARIOS
     * @return Copia del mapa de usuarios
     */
    @Override
    public Map<String, IJugador> getUsuarios() {
        return new HashMap<>(usuarios);
    }
}
