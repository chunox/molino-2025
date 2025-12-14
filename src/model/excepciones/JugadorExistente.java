package model.excepciones;

/**
 * Excepción lanzada cuando se intenta registrar un jugador que ya existe
 */
public class JugadorExistente extends Exception {
    public JugadorExistente() {
        super("El jugador ya existe en el sistema");
    }

    public JugadorExistente(String mensaje) {
        super(mensaje);
    }
}
