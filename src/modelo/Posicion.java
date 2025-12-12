package modelo;

public class Posicion {

    private String id;
    private Jugador ocupante;

    public Posicion(String id) {
        this.id = id;
        this.ocupante = null;
    }

    public String getId() {
        return id;
    }

    public Jugador getOcupante() {
        return ocupante;
    }

    public void ocupar(Jugador jugador) {
        this.ocupante = jugador;
    }

    public void liberar() {
        this.ocupante = null;
    }

    public boolean estaLibre() {
        return ocupante == null;
    }

    public boolean ocupadaPor(Jugador jugador) {
        return ocupante != null && ocupante == jugador;
    }
}
