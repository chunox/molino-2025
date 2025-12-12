# Juego del Molino - Multijugador Simple

Sistema simple de juego del Molino con múltiples partidas simultáneas y **dos tipos de vistas**.

## Características

### 🎮 Dos Tipos de Vistas

**Vista Gráfica**: Tablero visual con piezas de colores
**Vista Consola**: Terminal retro con comandos de texto

¡Cada jugador elige su vista favorita!

### 🔄 Sincronización Automática

- Patrón Observador
- Las vistas se actualizan automáticamente
- No importa si un jugador usa gráfica y otro consola
- Todo sincronizado en tiempo real

## Estructura

```
src/
├── modelo/
│   ├── GestorPartidas.java   ← Singleton con array de juegos
│   ├── ObservadorJuego.java  ← Interface para observadores
│   ├── Juego.java            ← Notifica a observadores
│   ├── Tablero.java
│   ├── Jugador.java
│   ├── Posicion.java
│   └── Molino.java
├── vista/
│   ├── MenuInicio.java       ← Menú para elegir vistas
│   ├── VentanaPrincipal.java ← Vista gráfica
│   ├── VentanaConsola.java   ← Vista de consola (NUEVA)
│   ├── PanelTablero.java
│   └── PanelControl.java
├── controlador/
│   └── ControladorJuego.java ← Implementa ObservadorJuego
└── Main.java
```

## Uso

```bash
# Compilar
javac -d bin src/**/*.java

# Ejecutar
java -cp bin Main
```

1. Ingresas nombres de jugadores
2. **Seleccionas vista para cada uno** (Gráfica o Consola)
3. Click en "Iniciar Partida"
4. Se abren 2 ventanas según lo elegido
5. Ambas vistas se actualizan automáticamente

## Vista Consola - Comandos

```
ayuda              - Muestra ayuda
estado             - Estado del juego
tablero            - Muestra el tablero ASCII
posiciones         - Muestra mapa de posiciones
colocar <pos>      - Coloca pieza (ej: colocar A1)
mover <ori> <dest> - Mueve pieza (ej: mover A1 D1)
eliminar <pos>     - Elimina pieza oponente
salir              - Cierra ventana
```

### Características de la Consola:
- **Auto-actualización**: El tablero y estado se muestran automáticamente después de cada movimiento
- **Mapa de posiciones**: Comando `posiciones` muestra dónde está cada casilla
- **Tablero alineado**: Usa caracteres Unicode para bordes perfectos
- **Colores**: Fondo negro, texto verde estilo terminal retro

### Ejemplo de Uso Consola:
```
> colocar A1
✓ Pieza colocada en A1

┌─────────────────────────────────────────┐
│           TABLERO DE JUEGO              │
├─────────────────────────────────────────┤
│  X──────────·──────────·                │
│  │          │          │                │
...

╔═══════════════════════════════════════════╗
║           ESTADO DEL JUEGO                ║
║         >>> TU TURNO <<<                  ║
╚═══════════════════════════════════════════╝

> mover A1 D1
✓ Pieza movida de A1 a D1
(Tablero se actualiza automáticamente)
```

## Cómo Funciona la Sincronización

```java
// 1. Juego notifica cuando cambia
public boolean colocarPieza(String pos) {
    // ... lógica ...
    notificarObservadores();  // ← Notifica a TODAS las vistas
    return true;
}

// 2. Vistas se actualizan automáticamente
@Override
public void onActualizacionJuego() {
    sincronizarPiezas();     // ← Actualiza desde el Juego
    actualizarInterfaz();    // ← Refresca la UI
}
```

## Combinaciones Posibles

- Gráfica ↔ Gráfica (clásico)
- Gráfica ↔ Consola (mixto)
- Consola ↔ Consola (retro)

¡Todas funcionan igual de bien!

## Características Técnicas

- ✅ Patrón Observador para sincronización
- ✅ Múltiples partidas simultáneas
- ✅ Cada jugador su propia vista
- ✅ Vistas independientes pero sincronizadas
- ✅ Sin red, sin servidor, sin complejidad
- ✅ Todo en memoria local

## Reglas del Juego

1. **Colocación**: Coloca 9 piezas alternando turnos
2. **Movimiento**: Mueve a posiciones adyacentes
3. **Vuelo**: Con 3 piezas, mueve a cualquier lado
4. **Molino**: 3 en línea = eliminas pieza del oponente
5. **Victoria**: Oponente con <3 piezas o sin movimientos
