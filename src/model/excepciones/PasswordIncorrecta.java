package model.excepciones;

/**
 * Excepción lanzada cuando la contraseña ingresada es incorrecta
 */
public class PasswordIncorrecta extends Exception {
    public PasswordIncorrecta() {
        super("La contraseña ingresada es incorrecta");
    }

    public PasswordIncorrecta(String mensaje) {
        super(mensaje);
    }
}
