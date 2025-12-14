package model.excepciones;

/**
 * Excepción lanzada cuando se intenta acceder a un jugador que no existe
 */
public class JugadorNoExistente extends Exception {
    public JugadorNoExistente() {
        super("El jugador no existe en el sistema");
    }

    public JugadorNoExistente(String mensaje) {
        super(mensaje);
    }
}
