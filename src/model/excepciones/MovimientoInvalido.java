package model.excepciones;

/**
 * Excepción lanzada cuando se intenta realizar un movimiento inválido
 */
public class MovimientoInvalido extends Exception {
    public MovimientoInvalido() {
        super("El movimiento solicitado no es válido");
    }

    public MovimientoInvalido(String mensaje) {
        super(mensaje);
    }
}
