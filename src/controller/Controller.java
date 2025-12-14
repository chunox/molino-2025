package controller;

import ar.edu.unlu.rmimvc.observer.IObservableRemoto;
import ar.edu.unlu.rmimvc.cliente.IControladorRemoto;
import model.clases.ManejadorEventos;
import model.enums.Estados;
import model.enums.Eventos;
import model.excepciones.JugadorExistente;
import model.excepciones.JugadorNoExistente;
import model.excepciones.PasswordIncorrecta;
import model.interfaces.IModelo;
import model.interfaces.IPartida;
import view.interfaces.IVista;
import java.rmi.RemoteException;

/**
 * Controlador que implementa el patrón Observer para recibir notificaciones del modelo
 * La exportación como objeto remoto es manejada por la librería RMI-MVC
 */
public class Controller implements IControladorRemoto {
    private static final long serialVersionUID = 1L;

    private IModelo modelo;
    private IVista vista;
    private int idPartidaActual;
    private String nombreJugador;
    private boolean esJugador1;

    public Controller() throws RemoteException {
        this.idPartidaActual = -1;
    }

    public void setVista(IVista vista) {
        this.vista = vista;
    }

    public IVista getVista() {
        return vista;
    }

    @Override
    public <T extends IObservableRemoto> void setModeloRemoto(T modelo) throws RemoteException {
        this.modelo = (IModelo) modelo;
    }

    // Métodos de gestión de usuarios

    public void registrarUsuario(String nombre, String password) throws RemoteException, JugadorExistente {
        modelo.registrarUsuario(nombre, password);
    }

    public void iniciarSesion(String nombre, String password) throws RemoteException, JugadorNoExistente, PasswordIncorrecta {
        modelo.iniciarSesion(nombre, password);
        this.nombreJugador = nombre;
    }

    // Métodos de gestión de partidas

    public IPartida crearPartida(String nombreJugador2) throws RemoteException {
        IPartida partida = modelo.crearPartida(nombreJugador, nombreJugador2);
        this.idPartidaActual = partida.getId();
        this.esJugador1 = true;
        return partida;
    }

    public void unirseAPartida(int idPartida) throws RemoteException {
        modelo.agregarJugadorAPartida(idPartida, nombreJugador);
        this.idPartidaActual = idPartida;
        this.esJugador1 = false;
    }

    public java.util.List<IPartida> buscarPartidas() throws RemoteException {
        return modelo.getPartidas();
    }

    public IPartida getPartidaActual() throws RemoteException {
        if (idPartidaActual == -1) {
            return null;
        }
        return modelo.getPartida(idPartidaActual);
    }

    // Métodos del juego

    public void colocarPieza(String posicion) throws RemoteException {
        if (idPartidaActual != -1) {
            modelo.colocarPieza(idPartidaActual, posicion);
        }
    }

    public void moverPieza(String origen, String destino) throws RemoteException {
        if (idPartidaActual != -1) {
            modelo.moverPieza(idPartidaActual, origen, destino);
        }
    }

    public void eliminarPiezaOponente(String posicion) throws RemoteException {
        if (idPartidaActual != -1) {
            modelo.eliminarPiezaOponente(idPartidaActual, posicion);
        }
    }

    // Métodos de ranking

    public java.util.Map<String, Integer> getRanking() throws RemoteException {
        return modelo.getRanking();
    }

    // Métodos de conexión/desconexión

    public void desconectar() throws RemoteException {
        if (idPartidaActual != -1) {
            modelo.desconectarJugador(nombreJugador, idPartidaActual);
        }
    }

    public void reconectar() throws RemoteException {
        if (idPartidaActual != -1) {
            modelo.reconectarJugador(nombreJugador, idPartidaActual);
        }
    }

    // Observer

    @Override
    public void actualizar(IObservableRemoto observable, Object evento) throws RemoteException {
        if (evento instanceof ManejadorEventos e) {
            System.out.println("[Controller-" + nombreJugador + "] Evento recibido: " + e.getEvento() +
                             " de Partida #" + e.getId());

            // Filtrar por ID de partida actual
            if (idPartidaActual != -1 && idPartidaActual != e.getId()) {
                System.out.println("[Controller-" + nombreJugador + "] Ignorando (no es mi partida)");
                return; // Ignorar evento de otra partida
            }

            // Filtrar por estado de la vista
            Estados estadoVista = vista.getEstado();
            System.out.println("[Controller-" + nombreJugador + "] Estado actual: " + estadoVista);

            switch (e.getEvento()) {
                case CAMBIO_BUSCAR_PARTIDA -> {
                    System.out.println("[Controller-" + nombreJugador + "] Procesando CAMBIO_BUSCAR_PARTIDA");
                    if (estadoVista == Estados.EN_BUSCAR_PARTIDA) {
                        vista.buscarPartidas();
                    }
                }
                case CAMBIO_ESPERANDO_JUGADORES -> {
                    System.out.println("[Controller-" + nombreJugador + "] Procesando CAMBIO_ESPERANDO_JUGADORES");
                    // Este evento indica que el segundo jugador se unió
                    if (estadoVista == Estados.EN_ESPERANDO_JUGADORES) {
                        System.out.println("[Controller-" + nombreJugador + "] Segundo jugador detectado, cerrando sala espera");
                        // La SalaEspera debería detectar esto con el timer
                    }
                }
                case CAMBIO_TURNO, PIEZA_COLOCADA, PIEZA_MOVIDA, PIEZA_ELIMINADA, FORMACION_MOLINO -> {
                    System.out.println("[Controller-" + nombreJugador + "] Procesando evento de juego: " + e.getEvento());
                    if (estadoVista == Estados.EN_JUEGO) {
                        vista.mostrarPartida();
                    }
                }
                case GAME_WIN -> {
                    if (estadoVista == Estados.EN_JUEGO) {
                        vista.mostrarGameWin();
                    }
                }
                case GAME_OVER -> {
                    if (estadoVista == Estados.EN_JUEGO) {
                        vista.mostrarGameOver();
                    }
                }
                case DESCONEXION_J, RECONEXION_J -> {
                    if (estadoVista == Estados.EN_JUEGO) {
                        vista.mostrarPartida();
                    }
                }
            }
        }
    }

    // Getters

    public int getIdPartidaActual() {
        return idPartidaActual;
    }

    public String getNombreJugador() {
        return nombreJugador;
    }

    public boolean isEsJugador1() {
        return esJugador1;
    }

    public void setIdPartidaActual(int id) {
        this.idPartidaActual = id;
    }

    public void setNombreJugador(String nombre) {
        this.nombreJugador = nombre;
    }

    public void setEsJugador1(boolean esJugador1) {
        this.esJugador1 = esJugador1;
    }
}
