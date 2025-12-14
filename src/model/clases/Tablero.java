package model.clases;

import model.interfaces.IJugador;
import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Clase que representa el tablero del juego del Molino
 */
public class Tablero implements Serializable {
    private static final long serialVersionUID = 1L;

    private final Map<String, Posicion> posiciones;
    private final Map<String, List<String>> adyacencias;
    private final List<Molino> molinos;

    public Tablero() {
        this.posiciones = new HashMap<>();
        this.adyacencias = new HashMap<>();
        this.molinos = new ArrayList<>();

        inicializarPosiciones();
        inicializarAdyacencias();
        inicializarMolinos();
    }

    private void inicializarPosiciones() {
        String[] ids = {
                "A1", "D1", "G1",
                "B2", "D2", "F2",
                "C3", "D3", "E3",
                "A4", "B4", "C4", "E4", "F4", "G4",
                "C5", "D5", "E5",
                "B6", "D6", "F6",
                "A7", "D7", "G7"
        };

        for (String id : ids) {
            posiciones.put(id, new Posicion(id));
        }
    }

    private void inicializarAdyacencias() {
        // Fila superior exterior
        agregarAdyacencia("A1", "D1", "A4");
        agregarAdyacencia("D1", "A1", "G1", "D2");
        agregarAdyacencia("G1", "D1", "G4");

        // Fila superior media
        agregarAdyacencia("B2", "D2", "B4");
        agregarAdyacencia("D2", "B2", "F2", "D1", "D3");
        agregarAdyacencia("F2", "D2", "F4");

        // Fila superior interior
        agregarAdyacencia("C3", "D3", "C4");
        agregarAdyacencia("D3", "C3", "E3", "D2");
        agregarAdyacencia("E3", "D3", "E4");

        // Fila central
        agregarAdyacencia("A4", "A1", "B4", "A7");
        agregarAdyacencia("B4", "A4", "B2", "C4", "B6");
        agregarAdyacencia("C4", "B4", "C3", "C5");
        agregarAdyacencia("E4", "E3", "F4", "E5");
        agregarAdyacencia("F4", "E4", "F2", "G4", "F6");
        agregarAdyacencia("G4", "F4", "G1", "G7");

        // Fila inferior interior
        agregarAdyacencia("C5", "C4", "D5");
        agregarAdyacencia("D5", "C5", "E5", "D6");
        agregarAdyacencia("E5", "D5", "E4");

        // Fila inferior media
        agregarAdyacencia("B6", "B4", "D6");
        agregarAdyacencia("D6", "B6", "F6", "D5", "D7");
        agregarAdyacencia("F6", "D6", "F4");

        // Fila inferior exterior
        agregarAdyacencia("A7", "A4", "D7");
        agregarAdyacencia("D7", "A7", "G7", "D6");
        agregarAdyacencia("G7", "D7", "G4");
    }

    private void agregarAdyacencia(String posicion, String... adyacentes) {
        this.adyacencias.put(posicion, List.of(adyacentes));
    }

    private void inicializarMolinos() {
        // Horizontales
        molinos.add(new Molino("A1", "D1", "G1"));
        molinos.add(new Molino("B2", "D2", "F2"));
        molinos.add(new Molino("C3", "D3", "E3"));
        molinos.add(new Molino("A4", "B4", "C4"));
        molinos.add(new Molino("E4", "F4", "G4"));
        molinos.add(new Molino("C5", "D5", "E5"));
        molinos.add(new Molino("B6", "D6", "F6"));
        molinos.add(new Molino("A7", "D7", "G7"));

        // Verticales
        molinos.add(new Molino("A1", "A4", "A7"));
        molinos.add(new Molino("B2", "B4", "B6"));
        molinos.add(new Molino("C3", "C4", "C5"));
        molinos.add(new Molino("D1", "D2", "D3"));
        molinos.add(new Molino("D5", "D6", "D7"));
        molinos.add(new Molino("E3", "E4", "E5"));
        molinos.add(new Molino("F2", "F4", "F6"));
        molinos.add(new Molino("G1", "G4", "G7"));
    }

    public boolean colocarPieza(String id, IJugador jugador) {
        Posicion pos = posiciones.get(id);
        if (pos == null || !pos.estaLibre()) {
            return false;
        }

        pos.ocupar(jugador);
        jugador.incrementarPiezasColocadas();
        return true;
    }

    public boolean moverPieza(String origen, String destino, IJugador jugador, boolean puedeVolar) {
        Posicion posOrigen = posiciones.get(origen);
        Posicion posDestino = posiciones.get(destino);

        if (posOrigen == null || posDestino == null) {
            return false;
        }

        if (!posOrigen.ocupadaPor(jugador) || !posDestino.estaLibre()) {
            return false;
        }

        // Si no puede volar, verificar que sea adyacente
        if (!puedeVolar && !esAdyacente(origen, destino)) {
            return false;
        }

        posOrigen.liberar();
        posDestino.ocupar(jugador);
        return true;
    }

    public boolean eliminarPieza(String id, IJugador jugador) {
        Posicion pos = posiciones.get(id);
        if (pos == null || !pos.ocupadaPor(jugador)) {
            return false;
        }

        pos.liberar();
        jugador.decrementarPiezasEnTablero();
        return true;
    }

    public boolean formaMolino(String posicion, IJugador jugador) {
        for (Molino molino : molinos) {
            if (molino.contiene(posicion) && molino.estaFormadoPor(jugador, posiciones)) {
                return true;
            }
        }
        return false;
    }

    public boolean esAdyacente(String pos1, String pos2) {
        List<String> adyacentesPos1 = adyacencias.get(pos1);
        return adyacentesPos1 != null && adyacentesPos1.contains(pos2);
    }

    public List<String> getPosicionesOcupadasPor(IJugador jugador) {
        return posiciones.entrySet().stream()
                .filter(entry -> entry.getValue().ocupadaPor(jugador))
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    public boolean tieneMovimientosDisponibles(String posicion, boolean puedeVolar) {
        if (puedeVolar) {
            // Puede moverse a cualquier posición libre
            return posiciones.values().stream().anyMatch(Posicion::estaLibre);
        } else {
            // Solo puede moverse a adyacentes libres
            List<String> adyacentesPosicion = adyacencias.get(posicion);
            if (adyacentesPosicion == null) {
                return false;
            }

            return adyacentesPosicion.stream()
                    .map(posiciones::get)
                    .anyMatch(Posicion::estaLibre);
        }
    }

    public Map<String, Posicion> getPosiciones() {
        return Collections.unmodifiableMap(posiciones);
    }

    public List<String> getAdyacentes(String posicion) {
        return adyacencias.getOrDefault(posicion, Collections.emptyList());
    }
}
