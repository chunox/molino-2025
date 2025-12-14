# Prompt de Implementación: Arquitectura MVC + Observer + RMI + Persistencia

Este documento contiene el prompt que debes usar para implementar esta arquitectura en cualquier nuevo proyecto de juego multijugador en Java Swing.

---

## PROMPT PARA CLAUDE/IA

```
Necesito implementar un juego multijugador en Java Swing con la siguiente arquitectura:

## REQUISITOS TÉCNICOS

### 1. Patrón MVC (Modelo-Vista-Controlador)

**Estructura de directorios**:
```
src/
├── model/
│   ├── clases/
│   ├── interfaces/
│   ├── enums/
│   └── excepciones/
├── view/
│   ├── vistas/
│   ├── frames/
│   └── interfaces/
├── controller/
├── servidor/
├── cliente/
└── serializacion/
```

**Modelo**:
- Debe ser Singleton
- Extender `ar.edu.unlu.rmimvc.observer.ObservableRemoto`
- Contener toda la lógica del juego
- NO tener referencias a elementos visuales (Swing)
- Notificar cambios mediante el patrón Observer
- Todos los métodos públicos deben lanzar `RemoteException`

Ejemplo de interfaz del modelo:
```java
public interface IModelo extends IObservableRemoto {
    // Gestión de partidas
    IPartida crearPartida(int parametros...) throws RemoteException;
    List<IPartida> getPartidas() throws RemoteException;
    IPartida getPartida(int id) throws RemoteException;
    void empezarPartida(int id) throws RemoteException;

    // Gestión de jugadores
    void registrarUsuario(String nombre, String password) throws RemoteException, JugadorExistente;
    void iniciarSesion(String nombre, String password) throws RemoteException, JugadorNoExistente;

    // Lógica específica del juego
    [AGREGAR MÉTODOS ESPECÍFICOS DE TU JUEGO]

    // Gestión de estados
    boolean verificarFinDelJuego(int id) throws RemoteException;

    // Persistencia
    void desconectarJugador(String nombre, int idPartida) throws RemoteException;
    void reconectarJugador(String nombre, int idPartida) throws RemoteException;
    Map<Integer, IPartida> getPartidasGuardadas(String nombreJugador) throws RemoteException;
}

public class Modelo extends ObservableRemoto implements IModelo, Serializable {
    private static IModelo instancia = null;

    private ISesion usuarios;
    private Map<Integer, IPartida> partidas;
    private IPartidaGuardada partidasGuardadas;
    private IRanking ranking;

    public static IModelo getInstancia() throws RemoteException {
        if (instancia == null) {
            instancia = new Modelo();
        }
        return instancia;
    }

    private Modelo() {
        usuarios = Sesion.getInstancia();
        partidas = new HashMap<>();
        ranking = Ranking.getInstancia();
        partidasGuardadas = PartidaGuardada.getInstancia();
        cargarPartidasPersistidas();
    }

    // Implementar métodos...
}
```

**Vista**:
- Interfaz `IVista` con métodos de navegación
- Múltiples implementaciones posibles (VistaGrafica, VistaConsola)
- Mantener referencia al Controlador
- Mantener estado actual (enum Estados)
- NO acceder directamente al Modelo

Ejemplo de interfaz de vista:
```java
public interface IVista {
    void login();
    void menu();
    void mostrarPartida() throws RemoteException;
    void mostrarGameOver();
    void mostrarGameWin() throws RemoteException;

    Estados getEstado();
    void setEstado(Estados estado);
    Controller getControlador();
}

public class VistaGrafica implements IVista {
    private Controller controlador;
    private Estados estado;

    // JFrames para cada pantalla
    [AGREGAR TUS FRAMES ESPECÍFICOS]

    public VistaGrafica() throws RemoteException {
        this.controlador = new Controller();
        this.controlador.setVista(this);
        // Inicializar frames...
    }
}
```

**Controlador**:
- Implementar `ar.edu.unlu.rmimvc.cliente.IControladorRemoto`
- Actuar como Observer del Modelo
- Mantener referencias a Modelo y Vista
- Delegar acciones al Modelo
- Actualizar Vista cuando el Modelo cambia

Ejemplo de controlador:
```java
public class Controller implements IControladorRemoto {
    private IModelo modelo;
    private IVista vista;
    private int id_partida_actual;
    private String nombre_jugador;

    public void setVista(IVista vista) {
        this.vista = vista;
    }

    @Override
    public <T extends IObservableRemoto> void setModeloRemoto(T modelo) {
        this.modelo = (IModelo) modelo;
    }

    // Métodos delegados al modelo
    [AGREGAR MÉTODOS ESPECÍFICOS DE TU JUEGO]

    @Override
    public void actualizar(IObservableRemoto observable, Object evento) throws RemoteException {
        if (evento instanceof ManejadorEventos e) {
            // Filtrar por ID de partida
            if (id_partida_actual != -1 && id_partida_actual != e.getId()) {
                return;
            }

            // Actualizar vista según evento
            switch (e.getEvento()) {
                case CAMBIO_TURNO -> {
                    if (vista.getEstado() == Estados.EN_JUEGO) {
                        vista.mostrarPartida();
                    }
                }
                case GAME_OVER -> vista.mostrarGameOver();
                // [AGREGAR CASOS ESPECÍFICOS DE TU JUEGO]
            }
        }
    }
}
```

### 2. Patrón Observer

**ManejadorEventos**:
- Encapsula información del evento
- Debe ser Serializable
- Contener: ID de la entidad, tipo de evento

```java
public class ManejadorEventos implements IManejadorEventos, Serializable {
    private final int id;
    private final Eventos evento;

    public ManejadorEventos(int id, Eventos evento) {
        this.id = id;
        this.evento = evento;
    }

    public Eventos getEvento() { return evento; }
    public int getId() { return id; }
    public boolean esIgual(int id) { return this.id == id; }
}
```

**Enum de Eventos**:
```java
public enum Eventos {
    // Eventos de lobby/partida
    CAMBIO_BUSCAR_PARTIDA,
    CAMBIO_ESPERANDO_JUGADORES,

    // Eventos del juego
    CAMBIO_TURNO,
    ACTUALIZACION_ESTADO,
    [AGREGAR EVENTOS ESPECÍFICOS DE TU JUEGO]

    // Eventos de conexión
    DESCONEXION_E,      // En espera
    RECONEXION_E,       // En espera
    DESCONEXION_J,      // En juego
    RECONEXION_J,       // En juego

    // Eventos de finalización
    GAME_OVER,
    GAME_WIN
}
```

**Notificaciones en el Modelo**:
- Notificar después de cada cambio de estado
- Ejemplo:
```java
public void hacerMovimiento(int id, ...) throws RemoteException {
    IPartida partida = getPartida(id);
    if (partida.realizarAccion(...)) {
        // Persistir cambio
        partidasGuardadas.actualizar(partida);

        // Notificar a observadores
        notificarObservadores(new ManejadorEventos(id, Eventos.ACTUALIZACION_ESTADO));
    }
}
```

### 3. RMI (Remote Method Invocation)

**Dependencia**:
- Librería: `ar.edu.unlu.rmimvc`
- Descargar de: https://github.com/mlapeducacionit/rmimvc

**AppServidor**:
```java
public class AppServidor {
    public static void main(String[] args) throws RemoteException {
        String ip = "127.0.0.1";
        String port = "8888";

        IModelo modelo = Modelo.getInstancia();
        Servidor servidor = new Servidor(ip, Integer.parseInt(port));

        try {
            servidor.iniciar(modelo);
            System.out.println("Servidor iniciado en " + ip + ":" + port);
        } catch (RemoteException | RMIMVCException e) {
            e.printStackTrace();
        }
    }
}
```

**AppCliente**:
```java
public class AppCliente {
    public static void main(String[] args) throws RemoteException {
        // Configuración del cliente
        String ipCliente = "127.0.0.1";
        String portCliente = JOptionPane.showInputDialog("Puerto del cliente:", "9999");

        // Configuración del servidor
        String ipServidor = "127.0.0.1";
        String portServidor = "8888";

        // Crear vista
        IVista vista = new VistaGrafica();

        // Conectar
        Cliente cliente = new Cliente(
            ipCliente, Integer.parseInt(portCliente),
            ipServidor, Integer.parseInt(portServidor)
        );

        vista.login();

        try {
            cliente.iniciar(vista.getControlador());
        } catch (RemoteException | RMIMVCException e) {
            e.printStackTrace();
        }
    }
}
```

**Consideraciones**:
- Todas las clases que viajen por RMI deben ser `Serializable`
- Agregar `private static final long serialVersionUID = 1L;` a clases serializables
- Todos los métodos remotos deben lanzar `RemoteException`

### 4. Persistencia

**Serializador genérico**:
```java
public class Serializador {
    private String fileName;

    public Serializador(String fileName) {
        this.fileName = fileName;
    }

    public boolean writeOneObject(Object obj) {
        try {
            ObjectOutputStream oos = new ObjectOutputStream(
                new FileOutputStream(fileName)
            );
            oos.writeObject(obj);
            oos.close();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public Object readFirstObject() {
        try {
            ObjectInputStream ois = new ObjectInputStream(
                new FileInputStream(fileName)
            );
            Object obj = ois.readObject();
            ois.close();
            return obj;
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }
}
```

**Clases de persistencia (Singletons)**:

1. **Sesion** (usuarios.dat):
```java
public class Sesion implements ISesion, Serializable {
    private static ISesion instancia = null;
    private Serializador serializador = new Serializador("src/data/usuarios.dat");
    private Map<String, IJugador> usuarios;

    public static ISesion getInstancia() {
        if (instancia == null) {
            instancia = new Sesion();
        }
        return instancia;
    }

    private Sesion() {
        Object obj = serializador.readFirstObject();
        usuarios = (obj != null) ? (Map<String, IJugador>) obj : new HashMap<>();
    }

    public void registrarse(String nombre, String password) throws JugadorExistente {
        if (usuarios.containsKey(nombre)) {
            throw new JugadorExistente();
        }
        usuarios.put(nombre, new Jugador(nombre, password));
        serializador.writeOneObject(usuarios);
    }

    public void iniciarSesion(String nombre, String password) throws JugadorNoExistente {
        if (!usuarios.containsKey(nombre)) {
            throw new JugadorNoExistente();
        }
        // Validar password...
    }
}
```

2. **PartidaGuardada** (partidas_guardadas.dat):
```java
public class PartidaGuardada implements IPartidaGuardada, Serializable {
    private static IPartidaGuardada instancia = null;
    private Serializador serializador = new Serializador("src/data/partidas.dat");
    private Map<Integer, IPartida> partidas;

    public static IPartidaGuardada getInstancia() {
        if (instancia == null) {
            instancia = new PartidaGuardada();
        }
        return instancia;
    }

    private PartidaGuardada() {
        Object obj = serializador.readFirstObject();
        partidas = (obj != null) ? (Map<Integer, IPartida>) obj : new HashMap<>();
    }

    public void actualizar(IPartida partida) {
        partidas.put(partida.getId(), partida);
        serializador.writeOneObject(partidas);
    }

    public void borrarPartidaGuardada(int id) {
        if (partidas.containsKey(id)) {
            partidas.remove(id);
            serializador.writeOneObject(partidas);
        }
    }

    public Map<Integer, IPartida> getPartidasGuardadas(String nombreJugador) {
        // Filtrar partidas por jugador
    }

    public Map<Integer, IPartida> getPartidasGuardadas() {
        return partidas;
    }
}
```

3. **Ranking** (ranking.dat):
```java
public class Ranking implements IRanking, Serializable {
    private static IRanking instancia = null;
    private Serializador serializador = new Serializador("src/data/ranking.dat");
    private Map<String, Integer> ranking;

    public static IRanking getInstancia() {
        if (instancia == null) {
            instancia = new Ranking();
        }
        return instancia;
    }

    private Ranking() {
        Object obj = serializador.readFirstObject();
        ranking = (obj != null) ? (Map<String, Integer>) obj : new HashMap<>();
    }

    public void actualizar(String nombreJugador) {
        ranking.put(nombreJugador, ranking.getOrDefault(nombreJugador, 0) + 1);
        serializador.writeOneObject(ranking);
    }

    public Map<String, Integer> getRanking() {
        return new HashMap<>(ranking);
    }
}
```

**Cuándo persistir**:
- Al desconectar un jugador
- Al finalizar cada turno (opcional, según complejidad)
- Al registrar un usuario
- Al actualizar el ranking
- Al terminar una partida

## ENUMS NECESARIOS

```java
public enum Estados {
    EN_LOGIN,
    EN_MENU,
    EN_BUSCAR_PARTIDA,
    EN_ESPERANDO_JUGADORES,
    EN_JUEGO,
    EN_RANKING,
    EN_OPCIONES
}

public enum EstadoPartida {
    EN_ESPERA,
    EN_JUEGO,
    FINALIZADA
}

public enum EstadoJugador {
    CONECTADO,
    DESCONECTADO
}
```

## EXCEPCIONES PERSONALIZADAS

```java
public class JugadorExistente extends Exception {
    public JugadorExistente() {
        super("El jugador ya existe");
    }
}

public class JugadorNoExistente extends Exception {
    public JugadorNoExistente() {
        super("El jugador no existe");
    }
}

public class PasswordIncorrecta extends Exception {
    public PasswordIncorrecta() {
        super("Contraseña incorrecta");
    }
}
```

## FLUJO DE TRABAJO

1. **Inicio**:
   - Ejecutar AppServidor
   - Ejecutar múltiples AppCliente
   - Cada cliente se conecta al servidor

2. **Login**:
   - Cliente ingresa usuario y contraseña
   - Controlador → Modelo.iniciarSesion()
   - Si es exitoso, ir al menú

3. **Crear/Unirse a partida**:
   - Controlador → Modelo.crearPartida() o agregarJugadorAPartida()
   - Modelo notifica: CAMBIO_ESPERANDO_JUGADORES
   - Todos los clientes en esa partida actualizan su vista

4. **Durante el juego**:
   - Usuario realiza acción → Vista → Controlador → Modelo
   - Modelo ejecuta lógica y persiste cambio
   - Modelo notifica: ACTUALIZACION_ESTADO
   - Controladores reciben notificación
   - Controladores actualizan sus vistas

5. **Desconexión/Reconexión**:
   - Al desconectar: Modelo.desconectarJugador() → persiste partida
   - Al reconectar: Modelo.reconectarJugador() → carga partida guardada

6. **Fin del juego**:
   - Modelo detecta condición de victoria/derrota
   - Modelo notifica: GAME_WIN o GAME_OVER
   - Actualizar ranking
   - Borrar partida guardada

## TAREAS DE IMPLEMENTACIÓN

Por favor, implementa lo siguiente paso a paso:

1. Estructura de carpetas y paquetes
2. Enums (Estados, EstadoPartida, EstadoJugador, Eventos)
3. Excepciones personalizadas
4. Interfaces del modelo, vista y controlador
5. Clase Serializador
6. Clases de persistencia (Sesion, PartidaGuardada, Ranking)
7. Modelo con lógica del juego [ESPECIFICAR REGLAS DEL JUEGO]
8. Controlador
9. Vista gráfica con Swing [ESPECIFICAR PANTALLAS NECESARIAS]
10. AppServidor y AppCliente
11. Testing

## REGLAS ESPECÍFICAS DEL JUEGO

[AQUÍ DEBES ESPECIFICAR LAS REGLAS ÚNICAS DE TU JUEGO]

Por ejemplo:
- Número de jugadores: min/max
- Objetivo del juego
- Mecánicas principales
- Condiciones de victoria/derrota
- Estados del juego
- Turnos (si aplica)
- Elementos del juego (cartas, fichas, tablero, etc.)

## PANTALLAS NECESARIAS

[ESPECIFICAR LAS PANTALLAS QUE NECESITA TU JUEGO]

Por ejemplo:
- Login
- Menú principal
- Crear partida
- Buscar partidas
- Sala de espera
- Pantalla de juego
- Game Over/Victoria
- Ranking
- Reglas
- Opciones

## CONSIDERACIONES ADICIONALES

- Usar JOptionPane para diálogos simples
- Implementar validaciones en el modelo
- Manejar RemoteException en todos los métodos de comunicación RMI
- Sincronizar métodos críticos en el servidor
- Cerrar recursos correctamente (ventanas, streams)
- Agregar logs para debugging

Por favor, implementa esta arquitectura completa para mi juego.
```

---

## EJEMPLO DE USO DEL PROMPT

Copia el prompt anterior y completa las secciones marcadas con [AGREGAR...] o [ESPECIFICAR...] según tu juego específico.

**Ejemplo para un juego de Ajedrez**:

```
## REGLAS ESPECÍFICAS DEL JUEGO

- Número de jugadores: 2
- Objetivo: Hacer jaque mate al rey contrario
- Mecánicas principales:
  - Cada jugador mueve una pieza por turno
  - Cada pieza tiene movimientos específicos
  - Captura de piezas
  - Enroque, peón al paso
- Condiciones de victoria: Jaque mate
- Condiciones de derrota: Abandonar, tiempo agotado
- Estados del juego: EN_ESPERA, EN_JUEGO, JAQUE, JAQUE_MATE, TABLAS
- Turnos: Alternados entre blancas y negras

## PANTALLAS NECESARIAS

- Login: Usuario y contraseña
- Menú principal: Crear partida, Unirse a partida, Ranking, Reglas, Salir
- Buscar partidas: Lista de partidas disponibles
- Sala de espera: Esperando al segundo jugador
- Pantalla de juego: Tablero 8x8, piezas, reloj, chat
- Game Over/Victoria: Resultado, estadísticas
- Ranking: Top 10 jugadores
- Reglas: Explicación del juego
```

---

## VERIFICACIÓN POST-IMPLEMENTACIÓN

Después de implementar, verifica que:

### MVC
- [ ] Modelo no tiene referencias a Swing
- [ ] Vista no accede directamente al Modelo
- [ ] Controlador coordina Modelo y Vista
- [ ] Interfaces bien definidas

### Observer
- [ ] Modelo notifica cambios
- [ ] Controlador recibe y procesa notificaciones
- [ ] ManejadorEventos implementado
- [ ] Vista se actualiza en respuesta a eventos

### RMI
- [ ] Servidor inicia correctamente
- [ ] Clientes se conectan sin errores
- [ ] Múltiples clientes pueden jugar simultáneamente
- [ ] Excepciones RMI manejadas adecuadamente

### Persistencia
- [ ] Usuarios se guardan y cargan correctamente
- [ ] Partidas se pueden guardar y reanudar
- [ ] Ranking persiste entre sesiones
- [ ] No hay pérdida de datos al cerrar

### General
- [ ] No hay errores de compilación
- [ ] No hay warnings importantes
- [ ] Código documentado
- [ ] Flujo del juego funciona correctamente

---

**Fin del prompt de implementación**
