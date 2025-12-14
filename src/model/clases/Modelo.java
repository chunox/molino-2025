package model.clases;

import ar.edu.unlu.rmimvc.observer.ObservableRemoto;
import model.enums.*;
import model.excepciones.*;
import model.interfaces.*;
import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.*;

/**
 * Clase Singleton que representa el modelo principal del juego
 * Extiende ObservableRemoto para notificar cambios a los observadores remotos
 */
public class Modelo extends ObservableRemoto implements IModelo, Serializable {
    private static final long serialVersionUID = 1L;
    private static IModelo instancia = null;

    private ISesion usuarios;
    private Map<Integer, IPartida> partidas;
    private IPartidaGuardada partidasGuardadas;
    private IRanking ranking;
    private int contadorPartidas;

    public static IModelo getInstancia() throws RemoteException {
        if (instancia == null) {
            instancia = new Modelo();
        }
        return instancia;
    }

    private Modelo() throws RemoteException {
        super();
        usuarios = Sesion.getInstancia();
        partidas = new HashMap<>();
        ranking = Ranking.getInstancia();
        partidasGuardadas = PartidaGuardada.getInstancia();
        contadorPartidas = 0;
        cargarPartidasPersistidas();
    }

    private void cargarPartidasPersistidas() {
        Map<Integer, IPartida> partidasGuardadasMap = partidasGuardadas.getPartidasGuardadas();
        for (Map.Entry<Integer, IPartida> entry : partidasGuardadasMap.entrySet()) {
            partidas.put(entry.getKey(), entry.getValue());
            if (entry.getKey() >= contadorPartidas) {
                contadorPartidas = entry.getKey() + 1;
            }
        }
    }

    @Override
    public IPartida crearPartida(String nombreJugador1, String nombreJugador2) throws RemoteException {
        // Crear los jugadores para esta partida
        IJugador jugador1 = new Jugador(nombreJugador1, "", 'X');
        IJugador jugador2 = new Jugador(nombreJugador2, "", 'O');

        // Crear la partida
        IPartida partida = new Partida(contadorPartidas++, jugador1, jugador2);
        partidas.put(partida.getId(), partida);

        System.out.println("📝 Lobby #" + partida.getId() + " creado por " + nombreJugador1 +
                          " (" + partida.getJugadores().size() + "/2 jugadores)");

        // Notificar que hay una nueva partida
        notificarObservadores(new ManejadorEventos(partida.getId(), Eventos.CAMBIO_BUSCAR_PARTIDA));

        return partida;
    }

    @Override
    public List<IPartida> getPartidas() throws RemoteException {
        System.out.println("🔍 Buscando lobbys... (" + partidas.size() + " total)");
        int disponibles = 0;
        for (IPartida p : partidas.values()) {
            if (p.getJugadores().size() < 2) {
                disponibles++;
                System.out.println("   - Lobby #" + p.getId() + ": " +
                                  p.getJugadores().get(0).getNombre() +
                                  " (" + p.getJugadores().size() + "/2)");
            }
        }
        System.out.println("   ✓ " + disponibles + " lobbys disponibles");
        return new ArrayList<>(partidas.values());
    }

    @Override
    public IPartida getPartida(int id) throws RemoteException {
        return partidas.get(id);
    }

    @Override
    public void empezarPartida(int id) throws RemoteException {
        IPartida partida = partidas.get(id);
        if (partida != null) {
            partida.setEstadoPartida(EstadoPartida.EN_JUEGO);
            notificarObservadores(new ManejadorEventos(id, Eventos.CAMBIO_TURNO));
        }
    }

    @Override
    public void agregarJugadorAPartida(int id, String nombreJugador) throws RemoteException {
        IPartida partida = partidas.get(id);
        if (partida != null && partida.getJugadores().size() < 2) {
            IJugador nuevoJugador = new Jugador(nombreJugador, "", 'O');
            partida.agregarJugador(nuevoJugador);
            // agregarJugador() ya cambia el estado a EN_JUEGO internamente

            System.out.println("✅ " + nombreJugador + " se unió al Lobby #" + id +
                              " (2/2 jugadores) - ¡Partida iniciada!");

            // Notificar a todos los observadores (ambos jugadores)
            System.out.println("📡 Notificando evento CAMBIO_ESPERANDO_JUGADORES a observadores");
            notificarObservadores(new ManejadorEventos(id, Eventos.CAMBIO_ESPERANDO_JUGADORES));
        }
    }

    @Override
    public void registrarUsuario(String nombre, String password) throws RemoteException, JugadorExistente {
        usuarios.registrarse(nombre, password);
    }

    @Override
    public void iniciarSesion(String nombre, String password) throws RemoteException, JugadorNoExistente, PasswordIncorrecta {
        usuarios.iniciarSesion(nombre, password);
    }

    @Override
    public void colocarPieza(int idPartida, String posicion) throws RemoteException {
        IPartida partida = partidas.get(idPartida);
        if (partida != null) {
            if (partida.colocarPieza(posicion)) {
                // Persistir cambio
                partidasGuardadas.actualizar(partida);

                // Notificar
                if (partida.isEsperandoEliminar()) {
                    notificarObservadores(new ManejadorEventos(idPartida, Eventos.FORMACION_MOLINO));
                } else {
                    notificarObservadores(new ManejadorEventos(idPartida, Eventos.PIEZA_COLOCADA));
                    notificarObservadores(new ManejadorEventos(idPartida, Eventos.CAMBIO_TURNO));
                }
            }
        }
    }

    @Override
    public void moverPieza(int idPartida, String origen, String destino) throws RemoteException {
        IPartida partida = partidas.get(idPartida);
        if (partida != null) {
            if (partida.moverPieza(origen, destino)) {
                // Persistir cambio
                partidasGuardadas.actualizar(partida);

                // Notificar
                if (partida.isEsperandoEliminar()) {
                    notificarObservadores(new ManejadorEventos(idPartida, Eventos.FORMACION_MOLINO));
                } else {
                    notificarObservadores(new ManejadorEventos(idPartida, Eventos.PIEZA_MOVIDA));
                    notificarObservadores(new ManejadorEventos(idPartida, Eventos.CAMBIO_TURNO));
                }
            }
        }
    }

    @Override
    public void eliminarPiezaOponente(int idPartida, String posicion) throws RemoteException {
        IPartida partida = partidas.get(idPartida);
        if (partida != null) {
            if (partida.eliminarPiezaOponente(posicion)) {
                // Persistir cambio
                partidasGuardadas.actualizar(partida);

                // Notificar
                notificarObservadores(new ManejadorEventos(idPartida, Eventos.PIEZA_ELIMINADA));

                // Verificar si hay ganador
                if (partida.hayGanador()) {
                    // Actualizar ranking
                    ranking.actualizar(partida.getGanador().getNombre());

                    // Borrar partida guardada
                    partidasGuardadas.borrarPartidaGuardada(idPartida);

                    // Notificar fin del juego
                    notificarObservadores(new ManejadorEventos(idPartida, Eventos.GAME_WIN));
                } else {
                    notificarObservadores(new ManejadorEventos(idPartida, Eventos.CAMBIO_TURNO));
                }
            }
        }
    }

    @Override
    public boolean verificarFinDelJuego(int id) throws RemoteException {
        IPartida partida = partidas.get(id);
        return partida != null && partida.hayGanador();
    }

    @Override
    public boolean hayGanador(int id) throws RemoteException {
        IPartida partida = partidas.get(id);
        return partida != null && partida.hayGanador();
    }

    @Override
    public IJugador getGanador(int id) throws RemoteException {
        IPartida partida = partidas.get(id);
        return partida != null ? partida.getGanador() : null;
    }

    @Override
    public void desconectarJugador(String nombre, int idPartida) throws RemoteException {
        IPartida partida = partidas.get(idPartida);
        if (partida != null) {
            partida.setEstadoJugador(nombre, EstadoJugador.DESCONECTADO);

            // Persistir el estado de la partida
            partidasGuardadas.actualizar(partida);

            // Notificar desconexión
            if (partida.getEstadoPartida() == EstadoPartida.EN_ESPERA) {
                notificarObservadores(new ManejadorEventos(idPartida, Eventos.DESCONEXION_E));
            } else {
                notificarObservadores(new ManejadorEventos(idPartida, Eventos.DESCONEXION_J));
            }
        }
    }

    @Override
    public void reconectarJugador(String nombre, int idPartida) throws RemoteException {
        IPartida partida = partidas.get(idPartida);
        if (partida != null) {
            partida.setEstadoJugador(nombre, EstadoJugador.CONECTADO);

            // Notificar reconexión
            if (partida.getEstadoPartida() == EstadoPartida.EN_ESPERA) {
                notificarObservadores(new ManejadorEventos(idPartida, Eventos.RECONEXION_E));
            } else {
                notificarObservadores(new ManejadorEventos(idPartida, Eventos.RECONEXION_J));
            }
        }
    }

    @Override
    public Map<Integer, IPartida> getPartidasGuardadas(String nombreJugador) throws RemoteException {
        return partidasGuardadas.getPartidasGuardadas(nombreJugador);
    }

    @Override
    public Map<String, Integer> getRanking() throws RemoteException {
        return ranking.getRanking();
    }

    @Override
    public void actualizarRanking(String nombreJugador) throws RemoteException {
        ranking.actualizar(nombreJugador);
    }
}
