# Cómo Funciona el Sistema Multijugador

## Arquitectura Simple

### 1. GestorPartidas (Singleton)
```java
private List<Juego> partidas = new ArrayList<>();

// Crear partida: retorna ID (índice en el array)
int idPartida = GestorPartidas.getInstancia().crearPartida("J1", "J2");

// Obtener partida por ID
Juego juego = GestorPartidas.getInstancia().getPartida(idPartida);
```

### 2. Cada Ventana Sabe su ID
```java
public class VentanaPrincipal {
    private int idPartida;  // ← Guarda el ID

    public VentanaPrincipal(String nombre, boolean esJ1, int idPartida) {
        this.idPartida = idPartida;
        // ...
    }
}
```

### 3. Controlador Usa el ID
```java
public class ControladorJuego {
    private int idPartida;

    private Juego getJuego() {
        return GestorPartidas.getInstancia().getPartida(idPartida);
    }

    public void manejarClicEnPosicion(String pos) {
        Juego juego = getJuego();  // ← Obtiene SU partida
        // Procesa el click en SU partida
    }
}
```

## Flujo de Creación de Partida

```
1. Usuario → MenuInicio:
   - Nombre J1: "Ana"
   - Nombre J2: "Luis"
   - Click "Iniciar Partida"

2. MenuInicio:
   int id = GestorPartidas.crearPartida("Ana", "Luis");  // id = 0

3. Crear ventanas:
   VentanaPrincipal v1 = new VentanaPrincipal("Ana", true, 0);
   new ControladorJuego(v1, 0, true);

   VentanaPrincipal v2 = new VentanaPrincipal("Luis", false, 0);
   new ControladorJuego(v2, 0, false);

4. Ambas ventanas:
   - Comparten idPartida = 0
   - Acceden al MISMO Juego en GestorPartidas.partidas[0]
   - Los clicks actualizan el mismo objeto Juego
```

## Múltiples Partidas

```
Partida 1: Ana vs Luis    → GestorPartidas.partidas[0]
Partida 2: María vs Pedro → GestorPartidas.partidas[1]
Partida 3: Juan vs Carlos → GestorPartidas.partidas[2]

Cada ventana sabe su ID:
- Ventana de Ana    → idPartida = 0
- Ventana de Luis   → idPartida = 0
- Ventana de María  → idPartida = 1
- Ventana de Pedro  → idPartida = 1
- Ventana de Juan   → idPartida = 2
- Ventana de Carlos → idPartida = 2
```

## Ventajas

✅ **Simple**: Solo un array y un ID
✅ **Sin red**: Todo en memoria local
✅ **Sin sincronización**: Ambas ventanas acceden al mismo objeto
✅ **Escalable**: Agregar partidas = agregar al array
✅ **Fácil debug**: Todo visible en un proceso

## Código Total

- **13 archivos Java**
- **~800 líneas** (vs 2500 de la versión complicada)
- **0 archivos de red**
- **0 servidores**
- **0 sockets**
- **0 serialización**

## Concepto Clave

> Cada ventana tiene un ID. Usa ese ID para obtener SU juego del array.
> Ambas ventanas del mismo juego usan el mismo ID, accediendo al mismo objeto.
> Simple.
