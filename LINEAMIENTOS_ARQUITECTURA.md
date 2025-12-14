# Lineamientos de Arquitectura para Juegos en Java Swing
## Implementación de MVC, Observer, RMI y Persistencia

Este documento describe los lineamientos para implementar una arquitectura robusta en aplicaciones de juegos multijugador usando Java Swing.

---

## 1. PATRÓN MVC (Modelo-Vista-Controlador)

### 1.1 Estructura de Directorios
```
src/
├── model/          # Lógica de negocio
│   ├── clases/     # Implementaciones concretas
│   ├── interfaces/ # Contratos del modelo
│   ├── enums/      # Enumeraciones
│   └── excepciones/# Excepciones personalizadas
├── view/           # Capa de presentación
│   ├── vistas/     # Implementaciones de vista (Consola/Gráfica)
│   ├── frames/     # Ventanas Swing
│   └── interfaces/ # Contratos de vista
└── controller/     # Coordinación Modelo-Vista
```

### 1.2 Responsabilidades

#### Modelo (Model)
**Propósito**: Contiene toda la lógica del negocio y el estado del juego.

**Características**:
- Es independiente de la vista y el controlador
- Notifica cambios a través del patrón Observer
- Contiene las reglas del juego
- Maneja la persistencia de datos
- No debe conocer Swing ni elementos visuales

**Ejemplo de estructura**:
```java
public interface IModelo extends IObservableRemoto {
    // Métodos de lógica de negocio
    IPartida crearPartida(int cantidadJugadores);
    void empezarPartida(int id);
    boolean gameOver(int id);
    // Gestión de jugadores
    void registrarUsuario(String nombre, String password);
    // Etc.
}

public class Modelo extends ObservableRemoto implements IModelo {
    private static IModelo instancia; // Singleton
    private Map<Integer, IPartida> partidas;
    private ISesion usuarios;
    private IRanking ranking;

    // Implementación de lógica
}
```

**Patrones aplicados en el Modelo**:
- **Singleton**: Una única instancia del modelo
- **Factory Method**: Creación de entidades del juego
- **Strategy**: Para diferentes lógicas de juego

#### Vista (View)
**Propósito**: Presenta la información al usuario y captura sus acciones.

**Características**:
- No contiene lógica de negocio
- Puede tener múltiples implementaciones (Consola, Gráfica)
- Se comunica con el Modelo solo a través del Controlador
- Responde a notificaciones del Observer para actualizar la UI

**Ejemplo de estructura**:
```java
public interface IVista {
    void login();
    void menu();
    void mostrarPartida() throws RemoteException;
    void mostrarGameOver();
    Estados getEstado();
    void setEstado(Estados estado);
    Controller getControlador();
}

public class VistaGrafica implements IVista {
    private Controller controlador;
    private Login login;
    private Menu menu;
    private PartidaEnJuego partidaEnJuego;
    private Estados estado;

    // Métodos de presentación
}
```

**Principios de la Vista**:
- Delegar acciones al controlador
- Actualizar UI en respuesta a eventos del Observer
- Mantener estado de navegación
- No acceder directamente al modelo

#### Controlador (Controller)
**Propósito**: Coordina las interacciones entre Modelo y Vista.

**Características**:
- Recibe acciones del usuario desde la Vista
- Invoca métodos del Modelo
- Actúa como Observer del Modelo
- Actualiza la Vista cuando el Modelo cambia

**Ejemplo de estructura**:
```java
public class Controller implements IControladorRemoto {
    private IModelo modelo;
    private IVista vista;

    public void setVista(IVista vista) {
        this.vista = vista;
    }

    @Override
    public <T extends IObservableRemoto> void setModeloRemoto(T modelo) {
        this.modelo = (IModelo) modelo;
    }

    // Métodos que delegan al modelo
    public void crearPartida(int cantJugadores) throws RemoteException {
        IPartida partida = modelo.crearPartida(cantJugadores);
        // Actualizar estado local si es necesario
    }

    @Override
    public void actualizar(IObservableRemoto observable, Object evento) {
        // Procesar eventos y actualizar vista
        if (evento instanceof ManejadorEventos e) {
            switch (e.getEvento()) {
                case CAMBIO_TURNO -> vista.mostrarPartida();
                case GAME_OVER -> vista.mostrarGameOver();
                // Etc.
            }
        }
    }
}
```

### 1.3 Flujo de Comunicación

```
Usuario → Vista → Controlador → Modelo
                              ↓
                        (notificación)
                              ↓
Usuario ← Vista ← Controlador (Observer)
```

**Ejemplo de flujo completo**:
1. Usuario hace clic en "Crear Partida"
2. Vista llama a `controlador.crearPartida(cantidadJugadores)`
3. Controlador llama a `modelo.crearPartida(cantidadJugadores)`
4. Modelo crea la partida y notifica: `notificarObservadores(evento)`
5. Controlador recibe notificación en `actualizar()`
6. Controlador actualiza la Vista: `vista.buscarPartidas()`

---

## 2. PATRÓN OBSERVER

### 2.1 Concepto
El patrón Observer permite que objetos (observadores) sean notificados automáticamente cuando el estado de otro objeto (observable) cambia.

### 2.2 Implementación con RMI-MVC

**Estructura base**:
```java
// Observable (Sujeto)
public class Modelo extends ObservableRemoto implements IModelo {
    // Cuando cambia el estado
    public void jugarTurno(int id, int zonasMano, int zonasCentro) {
        // ... lógica del juego ...
        if (cambioRealizado) {
            notificarObservadores(new ManejadorEventos(id, Eventos.ACTUALIZACION_CARTA));
        }
    }
}

// Observer (Observador)
public class Controller implements IControladorRemoto {
    @Override
    public void actualizar(IObservableRemoto observable, Object evento) {
        if (evento instanceof ManejadorEventos e) {
            // Procesar evento
            switch (e.getEvento()) {
                case ACTUALIZACION_CARTA -> vista.mostrarPartida();
            }
        }
    }
}
```

### 2.3 Manejador de Eventos

**Propósito**: Encapsular información sobre eventos que ocurren en el modelo.

```java
public class ManejadorEventos implements IManejadorEventos, Serializable {
    private final int id;           // ID de la entidad afectada
    private final Eventos evento;   // Tipo de evento

    public ManejadorEventos(int id, Eventos evento) {
        this.id = id;
        this.evento = evento;
    }

    public Eventos getEvento() { return evento; }
    public int getId() { return id; }
}
```

**Definición de tipos de eventos**:
```java
public enum Eventos {
    // Eventos de partida
    CAMBIO_BUSCAR_PARTIDA,
    CAMBIO_ESPERANDO_JUGADORES,

    // Eventos de juego
    ACTUALIZACION_CARTA,
    CAMBIO_TURNO,

    // Eventos de conexión
    DESCONEXION_E,
    RECONEXION_E,
    DESCONEXION_J,
    RECONEXION_J,

    // Eventos de finalización
    GAME_OVER,
    GAME_WIN
}
```

### 2.4 Cuándo Notificar

**Notificar cuando**:
- Cambia el estado del juego (turno, puntos, etc.)
- Se agrega/elimina un jugador
- Inicia o termina una partida
- Ocurre un error que afecta a múltiples clientes
- Se actualiza información compartida (ranking, etc.)

**Ejemplo práctico**:
```java
public void agregarJugadorAPartida(int id, String jugador) {
    IPartida partida = partidas.get(id);
    partida.agregarJugador(jugador);

    // Notificar a observadores en sala de espera
    notificarObservadores(new ManejadorEventos(id, Eventos.CAMBIO_ESPERANDO_JUGADORES));

    // Notificar para actualizar lista de partidas
    notificarObservadores(new ManejadorEventos(id, Eventos.CAMBIO_BUSCAR_PARTIDA));
}
```

### 2.5 Filtrado de Eventos en el Controlador

**Importante**: Cada cliente puede estar en contextos diferentes, filtrar eventos relevantes.

```java
@Override
public void actualizar(IObservableRemoto observable, Object evento) {
    if (evento instanceof ManejadorEventos e) {
        // Filtrar por ID de partida actual
        if (id_partida_actual != -1 && id_partida_actual != e.getId()) {
            return; // Ignorar evento de otra partida
        }

        // Filtrar por estado de la vista
        if (vista.getEstado() == Estados.EN_JUEGO) {
            switch (e.getEvento()) {
                case CAMBIO_TURNO -> vista.mostrarPartida();
                // ...
            }
        }
    }
}
```

---

## 3. RMI (Remote Method Invocation)

### 3.1 Concepto
RMI permite invocar métodos de objetos que están en otra JVM (otro proceso, otra máquina).

### 3.2 Arquitectura Cliente-Servidor

```
[Cliente 1]     [Cliente 2]     [Cliente 3]
    ↓               ↓               ↓
    └───────────────┴───────────────┘
                    ↓
              [Servidor RMI]
                    ↓
            [Modelo (Singleton)]
```

### 3.3 Configuración del Servidor

**AppServidor.java**:
```java
public class AppServidor {
    public static void main(String[] args) throws RemoteException {
        // Configuración de red
        String ip = "127.0.0.1";
        String port = "8888";

        // Obtener instancia del modelo (Singleton)
        IModelo modelo = Modelo.getInstancia();

        // Crear y arrancar servidor RMI
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

### 3.4 Configuración del Cliente

**AppCliente.java**:
```java
public class AppCliente {
    public static void main(String[] args) throws RemoteException {
        // Configuración del cliente
        String ipCliente = "127.0.0.1";
        String portCliente = "9999";

        // Configuración del servidor
        String ipServidor = "127.0.0.1";
        String portServidor = "8888";

        // Crear vista y controlador
        IVista vista = new VistaGrafica();

        // Conectar al servidor
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

### 3.5 Interfaces Remotas

**Todas las interfaces deben extender Remote y lanzar RemoteException**:

```java
public interface IModelo extends IObservableRemoto {
    IPartida crearPartida(int cantJugadores) throws RemoteException;
    List<IPartida> getPartidas() throws RemoteException;
    void registrarUsuario(String nombre, String password)
        throws RemoteException, JugadorExistente;
    // Etc.
}

public interface IControladorRemoto extends IObservadorRemoto {
    <T extends IObservableRemoto> void setModeloRemoto(T modelo)
        throws RemoteException;
    void actualizar(IObservableRemoto observable, Object evento)
        throws RemoteException;
}
```

### 3.6 Consideraciones Importantes

**Serialización**:
- Todos los objetos que viajen por RMI deben ser `Serializable`
- Incluye: entidades del modelo, eventos, DTOs, etc.

```java
public class Partida implements IPartida, Serializable {
    private static final long serialVersionUID = 1L;
    // ...
}
```

**Manejo de excepciones**:
- Siempre capturar `RemoteException`
- Implementar reconexión en caso de pérdida de conexión
- Informar al usuario de problemas de red

**Singleton en RMI**:
- El modelo debe ser Singleton para compartir estado entre todos los clientes
- Sincronización cuando sea necesario

```java
public class Modelo extends ObservableRemoto implements IModelo {
    private static IModelo instancia = null;

    public static IModelo getInstancia() throws RemoteException {
        if (instancia == null) {
            instancia = new Modelo();
        }
        return instancia;
    }

    private Modelo() {
        // Constructor privado
    }
}
```

---

## 4. PERSISTENCIA

### 4.1 Concepto
Persistencia permite guardar el estado de la aplicación para recuperarlo después.

### 4.2 Serialización en Java

**Clase Serializador genérica**:
```java
public class Serializador {
    private String fileName;

    public Serializador(String fileName) {
        this.fileName = fileName;
    }

    // Escribir un objeto (sobreescribe el archivo)
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

    // Leer el primer objeto del archivo
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

    // Agregar un objeto (append)
    public boolean addOneObject(Object obj) {
        try {
            AddableObjectOutputStream oos = new AddableObjectOutputStream(
                new FileOutputStream(fileName, true)
            );
            oos.writeObject(obj);
            oos.close();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Leer múltiples objetos
    public Object[] readObjects() {
        ArrayList<Object> list = new ArrayList<>();
        try {
            ObjectInputStream ois = new ObjectInputStream(
                new FileInputStream(fileName)
            );
            Object obj = ois.readObject();
            while (obj != null) {
                list.add(obj);
                obj = ois.readObject();
            }
            ois.close();
        } catch (EOFException e) {
            // Fin de archivo alcanzado
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }

        return list.isEmpty() ? null : list.toArray();
    }
}
```

### 4.3 Estrategias de Persistencia

#### Opción 1: Persistencia por Entidad

**Ventaja**: Organización clara, archivos separados por tipo de dato.

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
        if (obj == null) {
            usuarios = new HashMap<>();
            serializador.writeOneObject(usuarios);
        } else {
            usuarios = (Map<String, IJugador>) obj;
        }
    }

    @Override
    public void registrarse(String nombre, String password) throws JugadorExistente {
        if (usuarios.containsKey(nombre)) {
            throw new JugadorExistente();
        }
        usuarios.put(nombre, new Jugador(nombre, password));
        serializador.writeOneObject(usuarios);
    }
}
```

**Estructura de archivos**:
```
src/data/
├── usuarios.dat
├── partidas_guardadas.dat
├── ranking.dat
└── configuracion.dat
```

#### Opción 2: Persistencia de Partidas Guardadas

```java
public class PartidaGuardada implements IPartidaGuardada, Serializable {
    private static IPartidaGuardada instancia = null;
    private Serializador serializador = new Serializador("src/data/partidas.dat");
    private Map<Integer, IPartida> partidas;

    private PartidaGuardada() {
        Object obj = serializador.readFirstObject();
        partidas = (obj != null) ? (Map<Integer, IPartida>) obj : new HashMap<>();
    }

    @Override
    public void actualizar(IPartida partida) {
        partidas.put(partida.getId(), partida);
        serializador.writeOneObject(partidas);
    }

    @Override
    public void borrarPartidaGuardada(int id) {
        if (partidas.containsKey(id)) {
            partidas.remove(id);
            serializador.writeOneObject(partidas);
        }
    }

    @Override
    public Map<Integer, IPartida> getPartidasGuardadas(String nombreJugador) {
        Map<Integer, IPartida> resultado = new HashMap<>();
        for (Map.Entry<Integer, IPartida> entry : partidas.entrySet()) {
            for (IJugador jugador : entry.getValue().getJugadores()) {
                if (jugador.getNombre().equals(nombreJugador)) {
                    resultado.put(entry.getKey(), entry.getValue());
                }
            }
        }
        return resultado;
    }
}
```

### 4.4 Cuándo Persistir

**Eventos de guardado**:
- Al finalizar cada turno (auto-guardado)
- Al desconectar un jugador (para permitir reconexión)
- Al registrar un usuario
- Al actualizar el ranking
- Al cerrar la aplicación

**Ejemplo en el Modelo**:
```java
@Override
public void desconectarJugador(String nombre, int idPartida) throws RemoteException {
    IPartida partida = getPartida(idPartida);
    partida.setEstadoJugador(nombre, EstadoJugador.DESCONECTADO);

    // PERSISTIR el estado de la partida
    partidas_guardadas.actualizar(partida);

    notificarObservadores(new ManejadorEventos(idPartida, Eventos.DESCONEXION_J));
}
```

### 4.5 Carga Inicial

**Al iniciar el servidor**, cargar datos persistidos:

```java
private Modelo() {
    usuarios = Sesion.getInstancia();
    partidas = new HashMap<>();
    ranking = Ranking.getInstancia();
    partidas_guardadas = PartidaGuardada.getInstancia();

    // Cargar partidas guardadas en memoria
    cargarPartidasPersistidas();
}

private void cargarPartidasPersistidas() {
    Map<Integer, IPartida> partidasGuardadas =
        partidas_guardadas.getPartidasGuardadas();

    for (Map.Entry<Integer, IPartida> entry : partidasGuardadas.entrySet()) {
        partidas.put(entry.getKey(), entry.getValue());
    }
}
```

### 4.6 Consideraciones de Serialización

**Clases Serializables**:
```java
public class Partida implements IPartida, Serializable {
    private static final long serialVersionUID = 1L;

    private int id;
    private List<IJugador> jugadores;
    private IMazo mazo;
    private EstadoPartida estado;

    // No serializar elementos transitorios
    private transient IObservador observer; // Se marca como transient
}
```

**Usar serialVersionUID**:
```java
private static final long serialVersionUID = 1L;
```

Esto asegura compatibilidad entre versiones.

---

## 5. INTEGRACIÓN DE LOS 4 COMPONENTES

### 5.1 Diagrama de Integración

```
┌─────────────────────────────────────────────────────────┐
│                     CLIENTE                             │
│  ┌──────────┐      ┌──────────────┐      ┌──────────┐  │
│  │  Vista   │◄─────┤ Controlador  │──────►│ Observer │  │
│  └──────────┘      └──────────────┘      └──────────┘  │
│                            │                     ▲      │
└────────────────────────────┼─────────────────────┼──────┘
                             │                     │
                          [RMI]              [Notificaciones]
                             │                     │
┌────────────────────────────▼─────────────────────┼──────┐
│                      SERVIDOR                    │      │
│  ┌──────────────────────────────────────┐        │      │
│  │          Modelo (Observable)         │────────┘      │
│  │  - Lógica de negocio                 │               │
│  │  - Notifica cambios a Observers      │               │
│  └────────┬─────────────────────────────┘               │
│           │                                              │
│           ▼                                              │
│  ┌─────────────────┐                                    │
│  │  Persistencia   │                                    │
│  │  - Serializador │                                    │
│  │  - Archivos.dat │                                    │
│  └─────────────────┘                                    │
└─────────────────────────────────────────────────────────┘
```

### 5.2 Flujo Completo de una Acción

**Ejemplo: Jugador hace un movimiento**

1. **Vista**: Usuario hace clic en carta
   ```java
   // PartidaEnJuego.java
   botonCarta.addActionListener(e -> {
       controlador.jugarTurno(zonasMano, zonasCentro);
   });
   ```

2. **Controlador**: Recibe acción y delega al modelo
   ```java
   // Controller.java
   public void jugarTurno(int zonasMano, int zonasCentro) throws RemoteException {
       modelo.jugarTurno(id_partida_actual, zonasMano, zonasCentro);
   }
   ```

3. **RMI**: La llamada viaja al servidor

4. **Modelo**: Ejecuta lógica y persiste si es necesario
   ```java
   // Modelo.java
   public void jugarTurno(int id, int zonasMano, int zonasCentro) throws RemoteException {
       if (getPartida(id).jugarTurno(zonasMano, zonasCentro)) {
           // Persistir cambio
           partidas_guardadas.actualizar(getPartida(id));

           // Notificar a todos los observadores
           notificarObservadores(new ManejadorEventos(id, Eventos.ACTUALIZACION_CARTA));
       }
   }
   ```

5. **Observer**: Todos los controladores son notificados
   ```java
   // Controller.java
   public void actualizar(IObservableRemoto observable, Object evento) {
       if (evento instanceof ManejadorEventos e) {
           if (id_partida_actual == e.getId()) {
               switch (e.getEvento()) {
                   case ACTUALIZACION_CARTA -> vista.mostrarPartida();
               }
           }
       }
   }
   ```

6. **Vista**: Se actualiza la interfaz
   ```java
   // VistaGrafica.java
   public void mostrarPartida() throws RemoteException {
       partidaEnJuego.mostrarCartas();
       partidaEnJuego.mostrarTablero();
       partidaEnJuego.mostrarTurno();
   }
   ```

### 5.3 Gestión de Estados

**Enum de Estados de la Vista**:
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
```

**Control de estado en el Controlador**:
```java
@Override
public void actualizar(IObservableRemoto observable, Object evento) {
    if (evento instanceof ManejadorEventos e) {
        switch (e.getEvento()) {
            case CAMBIO_TURNO -> {
                if (vista.getEstado() == Estados.EN_JUEGO) {
                    vista.mostrarPartida();
                }
            }
            case CAMBIO_BUSCAR_PARTIDA -> {
                if (vista.getEstado() == Estados.EN_BUSCAR_PARTIDA) {
                    vista.buscarPartidas();
                }
            }
        }
    }
}
```

---

## 6. BUENAS PRÁCTICAS

### 6.1 Separación de Responsabilidades
- **Modelo**: NO debe tener referencias a Swing (JFrame, JButton, etc.)
- **Vista**: NO debe tener lógica de negocio
- **Controlador**: NO debe tener lógica compleja, solo coordinación

### 6.2 Uso de Interfaces
- Definir interfaces para todas las capas
- Programar contra interfaces, no implementaciones
- Facilita testing y cambios futuros

### 6.3 Singleton con Precaución
- Usar Singleton solo cuando tenga sentido (Modelo, Sesión, Ranking)
- NO abusar del patrón

### 6.4 Manejo de Errores
```java
try {
    controlador.crearPartida(cantJugadores);
} catch (RemoteException e) {
    JOptionPane.showMessageDialog(this,
        "Error de conexión con el servidor",
        "Error",
        JOptionPane.ERROR_MESSAGE);
}
```

### 6.5 Sincronización en Servidor
```java
public synchronized void agregarJugadorAPartida(int id, String nombre) {
    // Código thread-safe
}
```

### 6.6 Documentación
- Documentar interfaces públicas
- Comentar lógica compleja
- Mantener README actualizado

---

## 7. CHECKLIST DE IMPLEMENTACIÓN

### MVC
- [ ] Estructura de carpetas separada (model, view, controller)
- [ ] Interfaces definidas para cada capa
- [ ] Vista no accede directamente al modelo
- [ ] Controlador actúa como intermediario

### Observer
- [ ] Modelo extiende ObservableRemoto
- [ ] Controlador implementa IObservadorRemoto
- [ ] ManejadorEventos implementado
- [ ] Enum de eventos definido
- [ ] Filtrado de eventos en controlador

### RMI
- [ ] AppServidor configurado
- [ ] AppCliente configurado
- [ ] Todas las interfaces extienden Remote
- [ ] Métodos lanzan RemoteException
- [ ] Objetos transferidos son Serializable

### Persistencia
- [ ] Serializador genérico implementado
- [ ] Clases implementan Serializable
- [ ] serialVersionUID definido
- [ ] Guardado en momentos clave
- [ ] Carga al iniciar servidor

---

## 8. RECURSOS ADICIONALES

**Librerías utilizadas**:
- `ar.edu.unlu.rmimvc`: Framework RMI-MVC
- Java Swing: Interfaz gráfica
- Java Serialization: Persistencia

**Documentación**:
- Java RMI Tutorial: https://docs.oracle.com/javase/tutorial/rmi/
- Observer Pattern: https://refactoring.guru/design-patterns/observer
- MVC Pattern: https://en.wikipedia.org/wiki/Model%E2%80%93view%E2%80%93controller

---

**Fin del documento de lineamientos**
