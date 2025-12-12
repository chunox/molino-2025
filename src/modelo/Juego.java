package modelo;

import java.util.ArrayList;
import java.util.List;

public class Juego {

    public enum Fase { COLOCACION, MOVIMIENTO, VUELO }

    public enum EstadoJuego { EN_CURSO, ESPERANDO_ELIMINAR, FINALIZADO }

    private final Tablero tablero;
    private final Jugador jugador1;
    private final Jugador jugador2;
    private Jugador jugadorActual;
    private Fase faseActual;
    private EstadoJuego estadoJuego;
    private Jugador ganador;
    private final List<ObservadorJuego> observadores;

    private static final int PIEZAS_TOTALES_POR_JUGADOR = 9;
    private static final int PIEZAS_PARA_VUELO = 3;
    private static final int PIEZAS_MINIMAS_PARA_PERDER = 2;

    public Juego(String nombre1, String nombre2) {
        this.jugador1 = new Jugador(nombre1, 'X');
        this.jugador2 = new Jugador(nombre2, 'O');
        this.jugadorActual = jugador1;
        this.tablero = new Tablero();
        this.faseActual = Fase.COLOCACION;
        this.estadoJuego = EstadoJuego.EN_CURSO;
        this.ganador = null;
        this.observadores = new ArrayList<>();
    }

    public void agregarObservador(ObservadorJuego observador) {
        observadores.add(observador);
    }

    private void notificarObservadores() {
        for (ObservadorJuego obs : observadores) {
            obs.onActualizacionJuego();
        }
    }

    public boolean colocarPieza(String posicion) {
        if (!validarAccion(Fase.COLOCACION)) {
            return false;
        }

        if (!tablero.colocarPieza(posicion, jugadorActual)) {
            return false;
        }

        procesarDespuesDeAccion(posicion);
        notificarObservadores();
        return true;
    }

    public boolean moverPieza(String origen, String destino) {
        if (faseActual == Fase.COLOCACION || estadoJuego != EstadoJuego.EN_CURSO) {
            return false;
        }

        boolean puedeVolar = (faseActual == Fase.VUELO);

        if (!tablero.moverPieza(origen, destino, jugadorActual, puedeVolar)) {
            return false;
        }

        procesarDespuesDeAccion(destino);
        notificarObservadores();
        return true;
    }

    public boolean eliminarPiezaOponente(String posicion) {
        if (estadoJuego != EstadoJuego.ESPERANDO_ELIMINAR) {
            return false;
        }

        Jugador oponente = getJugadorOponente();

        // Verificar que no esté en un molino a menos que todas las piezas del oponente estén en molinos
        if (tablero.formaMolino(posicion, oponente) && !todasLasPiezasEnMolino(oponente)) {
            return false;
        }

        if (!tablero.eliminarPieza(posicion, oponente)) {
            return false;
        }

        estadoJuego = EstadoJuego.EN_CURSO;

        verificarCondicionesVictoria();

        if (!hayGanador()) {
            cambiarTurno();
            actualizarFase();
        }

        notificarObservadores();
        return true;
    }

    private void procesarDespuesDeAccion(String posicionFinal) {
        if (tablero.formaMolino(posicionFinal, jugadorActual)) {
            estadoJuego = EstadoJuego.ESPERANDO_ELIMINAR;
        } else {
            cambiarTurno();
            actualizarFase();
        }
    }

    private boolean validarAccion(Fase faseRequerida) {
        return faseActual == faseRequerida && estadoJuego == EstadoJuego.EN_CURSO;
    }

    private void cambiarTurno() {
        jugadorActual = (jugadorActual == jugador1) ? jugador2 : jugador1;
    }

    private Jugador getJugadorOponente() {
        return (jugadorActual == jugador1) ? jugador2 : jugador1;
    }

    private void actualizarFase() {
        if (faseActual == Fase.COLOCACION &&
                jugador1.getPiezasColocadas() == PIEZAS_TOTALES_POR_JUGADOR &&
                jugador2.getPiezasColocadas() == PIEZAS_TOTALES_POR_JUGADOR) {
            faseActual = Fase.MOVIMIENTO;
        }

        if (faseActual == Fase.MOVIMIENTO &&
                jugadorActual.getPiezasEnTablero() == PIEZAS_PARA_VUELO) {
            faseActual = Fase.VUELO;
        }
    }

    private void verificarCondicionesVictoria() {
        Jugador oponente = getJugadorOponente();

        // Gana si el oponente tiene menos de 3 piezas
        if (oponente.getPiezasEnTablero() <= PIEZAS_MINIMAS_PARA_PERDER) {
            ganador = jugadorActual;
            estadoJuego = EstadoJuego.FINALIZADO;
            return;
        }

        // Gana si el oponente no puede moverse (solo en fase de movimiento)
        if (faseActual != Fase.COLOCACION && !jugadorPuedeMoverse(oponente)) {
            ganador = jugadorActual;
            estadoJuego = EstadoJuego.FINALIZADO;
        }
    }

    private boolean jugadorPuedeMoverse(Jugador jugador) {
        for (String posicion : tablero.getPosicionesOcupadasPor(jugador)) {
            if (tablero.tieneMovimientosDisponibles(posicion, faseActual == Fase.VUELO)) {
                return true;
            }
        }
        return false;
    }

    private boolean todasLasPiezasEnMolino(Jugador jugador) {
        for (String posicion : tablero.getPosicionesOcupadasPor(jugador)) {
            if (!tablero.formaMolino(posicion, jugador)) {
                return false;
            }
        }
        return true;
    }

    // Getters
    public boolean hayGanador() {
        return ganador != null;
    }

    public Jugador getGanador() {
        return ganador;
    }

    public Jugador getJugadorActual() {
        return jugadorActual;
    }

    public Fase getFaseActual() {
        return faseActual;
    }

    public EstadoJuego getEstadoJuego() {
        return estadoJuego;
    }

    public boolean isEsperandoEliminar() {
        return estadoJuego == EstadoJuego.ESPERANDO_ELIMINAR;
    }

    public Tablero getTablero() {
        return tablero;
    }

    public Jugador getJugador1() {
        return jugador1;
    }

    public Jugador getJugador2() {
        return jugador2;
    }
}