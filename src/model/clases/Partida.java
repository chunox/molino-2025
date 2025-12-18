package model.clases;

import model.enums.*;
import model.interfaces.IJugador;
import model.interfaces.IPartida;
import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

/**
 * Clase que representa una partida del juego del Molino
 */
public class Partida implements IPartida, Serializable {
    private static final long serialVersionUID = 1L;

    private static final int PIEZAS_TOTALES_POR_JUGADOR = 9;
    private static final int PIEZAS_PARA_VUELO = 3;
    private static final int PIEZAS_MINIMAS_PARA_PERDER = 2;

    private int id;
    private final Tablero tablero;
    private final List<IJugador> jugadores;
    private IJugador jugadorActual;
    private FaseJuego faseActual;
    private EstadoPartida estadoPartida;
    private EstadoJuego estadoJuego;
    private IJugador ganador;

    public enum EstadoJuego { EN_CURSO, ESPERANDO_ELIMINAR, FINALIZADO }

    public Partida(int id, IJugador jugador1, IJugador jugador2) {
        this.id = id;
        this.tablero = new Tablero();
        this.jugadores = new ArrayList<>();
        this.jugadores.add(jugador1);

        // Solo agregar jugador2 si no es null o placeholder
        if (jugador2 != null && !jugador2.getNombre().equals("Esperando jugador...")) {
            this.jugadores.add(jugador2);
            this.estadoPartida = EstadoPartida.EN_JUEGO;
            System.out.println("   [Partida #" + id + "] Creada con 2 jugadores: " +
                             jugador1.getNombre() + " vs " + jugador2.getNombre());
        } else {
            this.estadoPartida = EstadoPartida.EN_ESPERA;
            System.out.println("   [Partida #" + id + "] Partida en espera (1/2): " + jugador1.getNombre());
        }

        this.jugadorActual = jugador1;
        this.faseActual = FaseJuego.COLOCACION;
        this.estadoJuego = EstadoJuego.EN_CURSO;
        this.ganador = null;
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public void setId(int id) {
        this.id = id;
    }

    @Override
    public List<IJugador> getJugadores() {
        return new ArrayList<>(jugadores);
    }

    @Override
    public void agregarJugador(IJugador jugador) {
        if (jugadores.size() < 2) {
            jugadores.add(jugador);
            System.out.println("   [Partida #" + id + "] " + jugador.getNombre() +
                             " se agregó (" + jugadores.size() + "/2)");

            // Si ahora hay 2 jugadores, cambiar estado a EN_JUEGO
            if (jugadores.size() == 2) {
                this.estadoPartida = EstadoPartida.EN_JUEGO;
                System.out.println("   [Partida #" + id + "] ¡Partida completa! Estado -> EN_JUEGO");
            }
        }
    }

    @Override
    public EstadoPartida getEstadoPartida() {
        return estadoPartida;
    }

    @Override
    public void setEstadoPartida(EstadoPartida estado) {
        this.estadoPartida = estado;
    }

    @Override
    public FaseJuego getFaseActual() {
        return faseActual;
    }

    @Override
    public void setFaseActual(FaseJuego fase) {
        this.faseActual = fase;
    }

    @Override
    public IJugador getJugadorActual() {
        return jugadorActual;
    }

    @Override
    public void setJugadorActual(IJugador jugador) {
        this.jugadorActual = jugador;
    }

    @Override
    public IJugador getGanador() {
        return ganador;
    }

    @Override
    public void setGanador(IJugador ganador) {
        this.ganador = ganador;
    }

    @Override
    public boolean hayGanador() {
        return ganador != null;
    }

    @Override
    public boolean colocarPieza(String posicion) throws RemoteException {
        if (!validarAccion(FaseJuego.COLOCACION)) {
            return false;
        }

        if (!tablero.colocarPieza(posicion, jugadorActual)) {
            return false;
        }

        procesarDespuesDeAccion(posicion);
        return true;
    }

    @Override
    public boolean moverPieza(String origen, String destino) throws RemoteException {
        if (faseActual == FaseJuego.COLOCACION || estadoJuego != EstadoJuego.EN_CURSO) {
            return false;
        }

        boolean puedeVolar = (faseActual == FaseJuego.VUELO);

        if (!tablero.moverPieza(origen, destino, jugadorActual, puedeVolar)) {
            return false;
        }

        procesarDespuesDeAccion(destino);
        return true;
    }

    @Override
    public boolean eliminarPiezaOponente(String posicion) throws RemoteException {
        if (estadoJuego != EstadoJuego.ESPERANDO_ELIMINAR) {
            return false;
        }

        IJugador oponente = getJugadorOponente();

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

        return true;
    }

    @Override
    public boolean isEsperandoEliminar() {
        return estadoJuego == EstadoJuego.ESPERANDO_ELIMINAR;
    }

    // Métodos privados auxiliares

    private void procesarDespuesDeAccion(String posicionFinal) {
        if (tablero.formaMolino(posicionFinal, jugadorActual)) {
            estadoJuego = EstadoJuego.ESPERANDO_ELIMINAR;
        } else {
            cambiarTurno();
            actualizarFase();
        }
    }

    private boolean validarAccion(FaseJuego faseRequerida) {
        return faseActual == faseRequerida && estadoJuego == EstadoJuego.EN_CURSO;
    }

    private void cambiarTurno() {
        jugadorActual = (jugadorActual == jugadores.get(0)) ? jugadores.get(1) : jugadores.get(0);
    }

    private IJugador getJugadorOponente() {
        return (jugadorActual == jugadores.get(0)) ? jugadores.get(1) : jugadores.get(0);
    }

    private void actualizarFase() {
        if (faseActual == FaseJuego.COLOCACION &&
                jugadores.get(0).getPiezasColocadas() == PIEZAS_TOTALES_POR_JUGADOR &&
                jugadores.get(1).getPiezasColocadas() == PIEZAS_TOTALES_POR_JUGADOR) {
            faseActual = FaseJuego.MOVIMIENTO;
        }

        if (faseActual == FaseJuego.MOVIMIENTO &&
                jugadorActual.getPiezasEnTablero() == PIEZAS_PARA_VUELO) {
            faseActual = FaseJuego.VUELO;
        }
    }

    private void verificarCondicionesVictoria() {
        IJugador oponente = getJugadorOponente();

        // Gana si el oponente tiene menos de 3 piezas
        if (oponente.getPiezasEnTablero() <= PIEZAS_MINIMAS_PARA_PERDER) {
            ganador = jugadorActual;
            estadoJuego = EstadoJuego.FINALIZADO;
            estadoPartida = EstadoPartida.FINALIZADA;
            return;
        }

        // Gana si el oponente no puede moverse (solo en fase de movimiento)
        if (faseActual != FaseJuego.COLOCACION && !jugadorPuedeMoverse(oponente)) {
            ganador = jugadorActual;
            estadoJuego = EstadoJuego.FINALIZADO;
            estadoPartida = EstadoPartida.FINALIZADA;
        }
    }

    private boolean jugadorPuedeMoverse(IJugador jugador) {
        for (String posicion : tablero.getPosicionesOcupadasPor(jugador)) {
            if (tablero.tieneMovimientosDisponibles(posicion, faseActual == FaseJuego.VUELO)) {
                return true;
            }
        }
        return false;
    }

    private boolean todasLasPiezasEnMolino(IJugador jugador) {
        for (String posicion : tablero.getPosicionesOcupadasPor(jugador)) {
            if (!tablero.formaMolino(posicion, jugador)) {
                return false;
            }
        }
        return true;
    }

    public Tablero getTablero() {
        return tablero;
    }

    @Override
    public java.util.Map<String, IJugador> getEstadoTablero() throws RemoteException {
        java.util.Map<String, IJugador> estado = new java.util.HashMap<>();

        for (java.util.Map.Entry<String, Posicion> entry : tablero.getPosiciones().entrySet()) {
            if (!entry.getValue().estaLibre()) {
                estado.put(entry.getKey(), entry.getValue().getOcupante());
            }
        }

        return estado;
    }
}
