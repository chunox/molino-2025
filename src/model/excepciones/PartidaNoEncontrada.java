package model.excepciones;

/**
 * Excepción lanzada cuando no se encuentra una partida
 */
public class PartidaNoEncontrada extends Exception {
    public PartidaNoEncontrada() {
        super("La partida solicitada no fue encontrada");
    }

    public PartidaNoEncontrada(String mensaje) {
        super(mensaje);
    }
}
