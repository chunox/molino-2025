# Arquitectura MVC + Observer + RMI + Persistencia - Juego del Molino

Este documento describe la arquitectura implementada en el proyecto del Juego del Molino.

## Estructura del Proyecto

```
src/
├── model/                      # Modelo (Lógica de negocio)
│   ├── clases/                 # Implementaciones concretas
│   │   ├── Modelo.java         # Modelo principal (Singleton + Observable)
│   │   ├── Partida.java        # Lógica de una partida
│   │   ├── Jugador.java        # Representa un jugador
│   │   ├── Tablero.java        # Lógica del tablero
│   │   ├── Posicion.java       # Posición en el tablero
│   │   ├── Molino.java         # Representa un molino (3 fichas alineadas)
│   │   ├── ManejadorEventos.java  # Encapsula eventos
│   │   ├── Sesion.java         # Gestión de usuarios (Singleton)
│   │   ├── PartidaGuardada.java   # Persistencia de partidas (Singleton)
│   │   └── Ranking.java        # Ranking de jugadores (Singleton)
│   ├── interfaces/             # Contratos del modelo
│   │   ├── IModelo.java
│   │   ├── IPartida.java
│   │   ├── IJugador.java
│   │   ├── ISesion.java
│   │   ├── IRanking.java
│   │   ├── IPartidaGuardada.java
│   │   └── IManejadorEventos.java
│   ├── enums/                  # Enumeraciones
│   │   ├── Estados.java        # Estados de la vista
│   │   ├── EstadoPartida.java  # Estados de una partida
│   │   ├── EstadoJugador.java  # Estado de conexión del jugador
│   │   ├── Eventos.java        # Tipos de eventos
│   │   └── FaseJuego.java      # Fases del juego
│   └── excepciones/            # Excepciones personalizadas
│       ├── JugadorExistente.java
│       ├── JugadorNoExistente.java
│       ├── PasswordIncorrecta.java
│       ├── PartidaNoEncontrada.java
│       └── MovimientoInvalido.java
├── view/                       # Vista (Presentación)
│   ├── vistas/                 # Implementaciones de vista
│   │   └── VistaGrafica.java   # Vista gráfica principal
│   └── interfaces/             # Contratos de vista
│       └── IVista.java
├── controller/                 # Controlador (Coordinación)
│   ├── Controller.java         # Controlador principal (Observer)
│   └── IControladorRemoto.java # Interfaz del controlador remoto
├── servidor/                   # Servidor RMI
│   └── AppServidor.java        # Aplicación servidor
├── cliente/                    # Cliente RMI
│   └── AppCliente.java         # Aplicación cliente
├── serializacion/              # Persistencia
│   └── Serializador.java       # Clase genérica para serialización
└── data/                       # Archivos de datos (generados en runtime)
    ├── usuarios.dat            # Usuarios registrados
    ├── partidas.dat            # Partidas guardadas
    └── ranking.dat             # Ranking de jugadores
```

## Patrones Implementados

### 1. MVC (Modelo-Vista-Controlador)

#### Modelo (`model/`)
- **Responsabilidad**: Contiene toda la lógica del negocio y el estado del juego
- **Características**:
  - Independiente de la vista y controlador
  - Notifica cambios mediante el patrón Observer
  - No conoce elementos visuales (Swing)
  - Gestiona la persistencia de datos

**Clase principal**: `Modelo.java` (Singleton)
- Gestiona partidas, usuarios y ranking
- Extiende `ObservableRemoto` para notificar cambios
- Persiste datos en cada acción importante

#### Vista (`view/`)
- **Responsabilidad**: Presenta información al usuario y captura acciones
- **Características**:
  - No contiene lógica de negocio
  - Se comunica con el Modelo solo a través del Controlador
  - Responde a notificaciones del Observer

**Clase principal**: `VistaGrafica.java`
- Implementa `IVista`
- Mantiene referencia al Controlador
- Mantiene estado actual (enum `Estados`)

#### Controlador (`controller/`)
- **Responsabilidad**: Coordina interacciones entre Modelo y Vista
- **Características**:
  - Recibe acciones del usuario desde la Vista
  - Invoca métodos del Modelo
  - Actúa como Observer del Modelo
  - Actualiza la Vista cuando el Modelo cambia

**Clase principal**: `Controller.java`
- Implementa `IControladorRemoto`
- Filtra eventos por ID de partida y estado de vista
- Delega acciones al Modelo

### 2. Observer (Observador)

**Implementación con RMI**:
- El `Modelo` extiende `ObservableRemoto` (de la librería rmimvc)
- El `Controller` implementa `IObservadorRemoto`
- Los cambios en el Modelo se notifican automáticamente a todos los Controladores conectados

**Flujo de notificación**:
1. Usuario realiza acción → Vista → Controlador
2. Controlador → Modelo (vía RMI)
3. Modelo ejecuta lógica y notifica: `notificarObservadores(evento)`
4. Todos los Controladores reciben `actualizar(observable, evento)`
5. Controladores filtran eventos relevantes
6. Controladores actualizan sus vistas

**Eventos disponibles** (`Eventos.java`):
- `CAMBIO_BUSCAR_PARTIDA`: Lista de partidas cambió
- `CAMBIO_ESPERANDO_JUGADORES`: Sala de espera cambió
- `CAMBIO_TURNO`: Cambió el turno
- `PIEZA_COLOCADA`, `PIEZA_MOVIDA`, `PIEZA_ELIMINADA`: Acciones del juego
- `FORMACION_MOLINO`: Se formó un molino
- `GAME_WIN`, `GAME_OVER`: Fin del juego
- `DESCONEXION_J`, `RECONEXION_J`: Eventos de conexión

### 3. RMI (Remote Method Invocation)

**Configuración**:
- **Servidor**: `AppServidor.java`
  - IP: 127.0.0.1
  - Puerto: 8888
  - Expone el Modelo como objeto remoto

- **Cliente**: `AppCliente.java`
  - Se conecta al servidor
  - Cada cliente tiene su propio puerto (ej: 9999, 10000, etc.)

**Características**:
- Todas las interfaces remotas extienden `Remote`
- Todos los métodos remotos lanzan `RemoteException`
- Todos los objetos transferidos son `Serializable`
- Incluyen `serialVersionUID` para compatibilidad

**Ejemplo de uso**:
```java
// Servidor
IModelo modelo = Modelo.getInstancia();
Servidor servidor = new Servidor("127.0.0.1", 8888);
servidor.iniciar(modelo);

// Cliente
Cliente cliente = new Cliente("127.0.0.1", 9999, "127.0.0.1", 8888);
cliente.iniciar(controlador);
```

### 4. Persistencia

**Clase genérica**: `Serializador.java`
- Serializa y deserializa objetos
- Crea directorios automáticamente
- Métodos:
  - `writeOneObject(Object)`: Escribe (sobreescribe)
  - `readFirstObject()`: Lee primer objeto
  - `addOneObject(Object)`: Agrega objeto
  - `readObjects()`: Lee múltiples objetos

**Clases de persistencia** (Singletons):

1. **Sesion** (`usuarios.dat`)
   - Registra usuarios con nombre y contraseña
   - Valida credenciales en login
   - Persiste automáticamente al registrar

2. **PartidaGuardada** (`partidas.dat`)
   - Guarda partidas en curso
   - Permite reconexión de jugadores
   - Se borra al finalizar la partida

3. **Ranking** (`ranking.dat`)
   - Almacena victorias de cada jugador
   - Se actualiza al ganar una partida
   - Devuelve ranking ordenado

**Cuándo se persiste**:
- Al desconectar un jugador (auto-guardado)
- Después de cada turno (via `partidasGuardadas.actualizar()`)
- Al registrar un usuario
- Al actualizar el ranking
- Al ganar una partida

## Cómo Usar el Sistema

### 1. Configurar la Librería RMI-MVC

**Requisito**: Descargar la librería `ar.edu.unlu.rmimvc`
- GitHub: https://github.com/mlapeducacionit/rmimvc
- Agregar el JAR al classpath del proyecto

### 2. Ejecutar el Servidor

```bash
# Compilar
javac src/servidor/AppServidor.java

# Ejecutar
java servidor.AppServidor
```

Salida esperada:
```
===========================================
    SERVIDOR DEL JUEGO DEL MOLINO
===========================================

✓ Modelo inicializado correctamente
✓ Servidor creado
✓ Servidor iniciado exitosamente

===========================================
  Servidor escuchando en:
  IP: 127.0.0.1
  Puerto: 8888
===========================================

Presiona Ctrl+C para detener el servidor
```

### 3. Ejecutar Clientes

Puedes ejecutar múltiples clientes simultáneamente:

```bash
# Cliente 1
java cliente.AppCliente
# Ingresar puerto: 9999

# Cliente 2 (en otra terminal)
java cliente.AppCliente
# Ingresar puerto: 10000
```

### 4. Flujo de Uso

1. **Login**:
   - Cada cliente se conecta al servidor
   - Puede registrarse o iniciar sesión

2. **Crear/Unirse a Partida**:
   - Jugador 1 crea una partida
   - Jugador 2 busca partidas y se une

3. **Jugar**:
   - Los jugadores alternan turnos
   - El Modelo notifica cada cambio
   - Ambas vistas se actualizan automáticamente

4. **Finalizar**:
   - Al ganar, se actualiza el ranking
   - Se borra la partida guardada
   - Ambos jugadores son notificados

## Integración con Código Existente

El proyecto original tenía clases en `modelo/`, `vista/` y `controlador/`. La nueva arquitectura:

- **Mantiene**: La lógica del juego existente (Tablero, Molino, etc.)
- **Agrega**: RMI, Persistencia, Observer remoto
- **Estructura**: Nueva organización en `model/`, `view/`, `controller/`

### Migración Recomendada

Para adaptar las vistas existentes (`VentanaPrincipal`, `VentanaConsola`, etc.):

1. Hacer que implementen `IVista`
2. Usar `Controller` en lugar de acceder al modelo directamente
3. Actualizar UI en el método `mostrarPartida()` (llamado por el Observer)
4. Agregar manejo de estados con el enum `Estados`

**Ejemplo**:
```java
public class VentanaPrincipal extends JFrame implements IVista {
    private Controller controlador;
    private Estados estado;

    public VentanaPrincipal() throws RemoteException {
        this.controlador = new Controller();
        this.controlador.setVista(this);
        // ... inicialización UI
    }

    @Override
    public void mostrarPartida() throws RemoteException {
        // Obtener partida actual
        IPartida partida = controlador.getPartidaActual();

        // Actualizar UI
        actualizarTablero(partida);
        actualizarTurno(partida);

        repaint();
    }

    // Acciones del usuario
    private void onColocarPieza(String posicion) {
        try {
            controlador.colocarPieza(posicion);
            // La vista se actualizará automáticamente via Observer
        } catch (RemoteException e) {
            mostrarError("Error de conexión");
        }
    }
}
```

## Diagramas

### Diagrama de Clases Simplificado

```
┌─────────────────────┐
│      IModelo        │
│  (Observable)       │
└──────────┬──────────┘
           │
           │ implements
           │
┌──────────▼──────────┐
│      Modelo         │ 1
│   (Singleton)       │────────┐
└─────────────────────┘        │
                               │ *
                        ┌──────▼──────┐
                        │   IPartida  │
                        └──────┬──────┘
                               │
                        ┌──────▼──────┐
                        │   Partida   │
                        └─────────────┘

┌─────────────────────┐
│  IControladorRemoto │
│    (Observer)       │
└──────────┬──────────┘
           │
           │ implements
           │
┌──────────▼──────────┐
│    Controller       │ 1
└─────────────────────┘────────┐
                               │ 1
                        ┌──────▼──────┐
                        │   IVista    │
                        └──────┬──────┘
                               │
                        ┌──────▼──────┐
                        │VistaGrafica │
                        └─────────────┘
```

### Diagrama de Secuencia - Colocar Pieza

```
Usuario  →  Vista  →  Controller  →  [RMI]  →  Modelo
                                                  │
                                                  │ (notifica)
                                                  ▼
                        ┌────────── Controller (Observer)
                        │               │
                        │               │ (actualiza)
                        │               ▼
Usuario  ←  Vista  ←────┘
```

## Ventajas de Esta Arquitectura

1. **Escalabilidad**: Múltiples clientes pueden conectarse simultáneamente
2. **Separación de responsabilidades**: MVC bien definido
3. **Persistencia**: Los datos se guardan automáticamente
4. **Sincronización**: Todos los clientes se actualizan en tiempo real
5. **Reconexión**: Los jugadores pueden desconectarse y volver
6. **Ranking**: Se mantiene estadística de victorias
7. **Modularidad**: Fácil de extender y mantener

## Próximos Pasos

1. **Adaptar vistas existentes**: Migrar `VentanaPrincipal`, `VentanaConsola`, etc.
2. **Implementar pantallas faltantes**:
   - Login con registro
   - Menú principal
   - Buscar partidas
   - Sala de espera
   - Ranking
3. **Agregar validaciones**: Verificar permisos y estados antes de acciones
4. **Mejorar manejo de errores**: Mensajes de error más descriptivos
5. **Testing**: Pruebas unitarias e integración

## Notas Importantes

- **Orden de ejecución**: Siempre iniciar el servidor antes que los clientes
- **Puertos**: Cada cliente debe usar un puerto único
- **Persistencia**: Los archivos .dat se crean automáticamente en `src/data/`
- **Excepciones RMI**: Siempre manejar `RemoteException` en código de cliente
- **Serialización**: Todas las clases transferidas deben implementar `Serializable`

## Soporte

Para dudas o problemas, consultar:
- `LINEAMIENTOS_ARQUITECTURA.md`: Lineamientos completos
- `PROMPT_IMPLEMENTACION.md`: Prompt de implementación original
- Documentación de rmimvc: https://github.com/mlapeducacionit/rmimvc
