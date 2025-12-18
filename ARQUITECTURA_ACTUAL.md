# ARQUITECTURA ACTUAL DEL JUEGO DEL MOLINO

## Fecha de última actualización: 2025-12-17

---

## ÍNDICE

1. [Resumen Ejecutivo](#resumen-ejecutivo)
2. [Arquitectura General](#arquitectura-general)
3. [Flujo del Juego](#flujo-del-juego)
4. [Sistema de Matchmaking](#sistema-de-matchmaking)
5. [Componentes Principales](#componentes-principales)
6. [Comunicación Cliente-Servidor](#comunicación-cliente-servidor)
7. [Selección de Vista](#selección-de-vista)
8. [Gestión de Estados](#gestión-de-estados)
9. [Eventos del Juego](#eventos-del-juego)

---

## RESUMEN EJECUTIVO

El Juego del Molino es una aplicación distribuida que utiliza **RMI (Remote Method Invocation)**
para permitir que múltiples jugadores jueguen en red desde diferentes clientes.

### Características Principales:

- **Matchmaking Automático**: Los jugadores son emparejados automáticamente sin necesidad de crear lobbys manualmente
- **Sistema Distribuido**: Arquitectura cliente-servidor con RMI
- **Dos Tipos de Vista**: Cada jugador puede elegir entre vista gráfica (Swing) o vista de consola (texto)
- **Observador Remoto**: Sincronización automática de todos los clientes mediante el patrón Observer
- **Sin Autenticación**: No hay sistema de contraseñas ni sesiones persistentes
- **Sin Reconexión**: Si un jugador se desconecta, la partida termina automáticamente

---

## ARQUITECTURA GENERAL

### Diagrama de Arquitectura

```
┌─────────────────────────────────────────────────────────────┐
│                        SERVIDOR                             │
│  ┌───────────────────────────────────────────────────────┐  │
│  │              MODELO (Singleton)                       │  │
│  │  • Gestión de partidas activas                        │  │
│  │  • Lógica de negocio del juego                        │  │
│  │  • Sistema de matchmaking                             │  │
│  │  • Ranking de jugadores                               │  │
│  │  • Notificaciones a observadores (RMI callbacks)      │  │
│  └───────────────────────────────────────────────────────┘  │
└─────────────────────────┬───────────────────────────────────┘
                          │ RMI
         ┌────────────────┼────────────────┐
         │                │                │
         ▼                ▼                ▼
    ┌─────────┐      ┌─────────┐      ┌─────────┐
    │Cliente 1│      │Cliente 2│      │Cliente 3│
    │         │      │         │      │         │
    │ Vista   │      │ Vista   │      │ Vista   │
    │   ▲     │      │   ▲     │      │   ▲     │
    │   │     │      │   │     │      │   │     │
    │Controller│     │Controller│     │Controller│
    └─────────┘      └─────────┘      └─────────┘
```

### Patrón MVC Distribuido

- **Modelo (Servidor)**: Reside en el servidor, compartido por todos los clientes
- **Vista (Cliente)**: Interfaz gráfica local en cada cliente
- **Controlador (Cliente)**: Intermediario entre Vista y Modelo remoto

---

## FLUJO DEL JUEGO

### 1. Inicio del Cliente

```
[Paso 1] Usuario ejecuta AppCliente
         ↓
[Paso 2] Selecciona puerto local (9999, 10000, 10001, etc.)
         ↓
[Paso 3] Ingresa su nombre de jugador
         ↓
[Paso 4] Se crea la Vista y Controller
         ↓
[Paso 5] Se conecta al Servidor RMI (127.0.0.1:8888)
         ↓
[Paso 6] Controller se suscribe al Modelo como observador
         ↓
[Paso 7] Se muestra el Menú Principal
```

### 2. Búsqueda de Partida (Matchmaking Automático)

```
[Paso 1] Jugador hace clic en "Buscar Partida"
         ↓
[Paso 2] Sistema pregunta: ¿Vista Gráfica o Vista Consola?
         ↓
[Paso 3] Se envía solicitud al Modelo: buscarPartida(nombreJugador)
         ↓
[Paso 4] Modelo ejecuta algoritmo de matchmaking:

         ┌─────────────────────────────────────┐
         │ ¿Existe partida EN_ESPERA con       │
         │   1 jugador?                        │
         └───────┬──────────────┬──────────────┘
                 │ SÍ           │ NO
                 ▼              ▼
         ┌───────────────┐  ┌──────────────────┐
         │ Unir a        │  │ Crear nueva      │
         │ partida       │  │ partida EN_ESPERA│
         │ existente     │  │ con 1 jugador    │
         └───────┬───────┘  └────────┬─────────┘
                 │                   │
                 ▼                   ▼
         ┌───────────────┐  ┌──────────────────┐
         │ Partida       │  │ Mostrar diálogo  │
         │ COMPLETA      │  │ "Esperando       │
         │ (2 jugadores) │  │  oponente..."    │
         └───────┬───────┘  └────────┬─────────┘
                 │                   │
                 ▼                   │
         ┌───────────────────────────┘
         │
         ▼
[Paso 5] Modelo notifica CAMBIO_TURNO a todos los observadores
         ↓
[Paso 6] Ambos clientes reciben evento CAMBIO_TURNO
         ↓
[Paso 7] Se crean las ventanas de juego (VentanaPrincipal o VentanaConsola)
         ↓
[Paso 8] ¡Comienza la partida!
```

### 3. Durante la Partida

```
[Evento] Jugador 1 coloca una pieza en A1
         ↓
[Cliente 1] Vista detecta clic → Controller.colocarPieza("A1")
         ↓
[RMI] Controller → Modelo.colocarPieza(idPartida, "A1")
         ↓
[Servidor] Modelo valida movimiento y actualiza estado
         ↓
[Servidor] Modelo.notificarObservadores(PIEZA_COLOCADA)
         ↓
[RMI Callbacks] Todos los Controllers reciben actualizar(evento)
         ↓
[Ambos Clientes] Vista.mostrarPartida() actualiza interfaz
         ↓
[Resultado] Ambos jugadores ven la pieza en A1 actualizada
```

### 4. Fin de Partida

```
[Evento] Se detecta un ganador
         ↓
[Servidor] Modelo.notificarObservadores(GAME_WIN)
         ↓
[Clientes] Vista muestra mensaje de victoria/derrota
         ↓
[Clientes] Jugadores cierran ventana y vuelven al menú
         ↓
[Opcional] Jugadores pueden buscar nueva partida
```

---

## SISTEMA DE MATCHMAKING

### Algoritmo de Emparejamiento Automático

```java
// En Modelo.buscarPartida()

1. Buscar partida EN_ESPERA con 1 jugador:

   FOR cada partida in partidas.values():
       IF partida.estado == EN_ESPERA AND
          partida.jugadores.size() == 1 AND
          partida.jugadores[0].nombre != nombreJugador:

          → Agregar jugador2 a la partida
          → Cambiar estado a EN_JUEGO
          → Notificar CAMBIO_TURNO
          → RETURN partida

2. Si no hay partidas disponibles:

   → Crear nueva partida con jugador1
   → Estado = EN_ESPERA
   → Esperar a que otro jugador se conecte
   → RETURN nueva partida
```

### Estados de Partida

- **EN_ESPERA**: Partida creada con 1 jugador, esperando al segundo
- **EN_JUEGO**: Partida completa con 2 jugadores, en curso
- **FINALIZADA**: Partida terminada con ganador

---

## COMPONENTES PRINCIPALES

### Servidor

#### AppServidor
- **Ubicación**: `src/servidor/AppServidor.java`
- **Responsabilidad**: Punto de entrada del servidor
- **Función**: Inicia el servidor RMI y registra el Modelo

#### Modelo (Singleton)
- **Ubicación**: `src/model/clases/Modelo.java`
- **Responsabilidad**: Lógica de negocio centralizada
- **Funciones**:
  - Gestionar partidas activas (Map<ID, Partida>)
  - Algoritmo de matchmaking automático
  - Validar movimientos del juego
  - Detectar molinos y ganadores
  - Actualizar ranking
  - Notificar eventos a observadores

#### Partida
- **Ubicación**: `src/model/clases/Partida.java`
- **Responsabilidad**: Representa una partida individual
- **Componentes**:
  - Tablero de juego
  - Jugadores (1 o 2)
  - Estado actual (EN_ESPERA / EN_JUEGO)
  - Fase de juego (COLOCACION / MOVIMIENTO / VUELO)
  - Lógica de molinos y eliminación

### Cliente

#### AppCliente
- **Ubicación**: `src/cliente/AppCliente.java`
- **Responsabilidad**: Punto de entrada del cliente
- **Flujo**:
  1. Solicitar puerto local
  2. Solicitar nombre de jugador
  3. Crear Vista y Controller
  4. Conectar al servidor RMI
  5. Mostrar menú principal

#### VistaGrafica
- **Ubicación**: `src/view/vistas/VistaGrafica.java`
- **Responsabilidad**: Gestión de interfaz gráfica
- **Funciones**:
  - Mostrar menú principal
  - Coordinar búsqueda de partida
  - Seleccionar tipo de vista (gráfica/consola)
  - Crear y mostrar ventanas de juego
  - Gestionar estados de la vista

#### Controller
- **Ubicación**: `src/controller/Controller.java`
- **Responsabilidad**: Intermediario entre Vista y Modelo
- **Funciones**:
  - Invocar métodos del Modelo remoto
  - Recibir notificaciones del Modelo (Observer)
  - Filtrar eventos por partida y estado
  - Actualizar la Vista según eventos

#### VentanaPrincipal
- **Ubicación**: `src/view/frames/VentanaPrincipal.java`
- **Responsabilidad**: Vista gráfica del tablero (Swing)
- **Componentes**:
  - PanelTablero: Dibuja el tablero con gráficos
  - PanelControl: Muestra turno, fase, piezas
  - Manejo de clics en posiciones

#### VentanaConsola
- **Ubicación**: `src/view/frames/VentanaConsola.java`
- **Responsabilidad**: Vista de consola del tablero (texto)
- **Componentes**:
  - Área de consola para comandos
  - Área de tablero (ASCII art)
  - Área de mapa de posiciones
  - Comandos: colocar, mover, eliminar, ayuda

---

## COMUNICACIÓN CLIENTE-SERVIDOR

### Llamadas RMI (Cliente → Servidor)

```java
// El cliente invoca métodos remotos del Modelo

Controller.colocarPieza(posicion)
    → Modelo.colocarPieza(idPartida, posicion)

Controller.moverPieza(origen, destino)
    → Modelo.moverPieza(idPartida, origen, destino)

Controller.eliminarPiezaOponente(posicion)
    → Modelo.eliminarPiezaOponente(idPartida, posicion)

Controller.buscarPartida()
    → Modelo.buscarPartida(nombreJugador)

Controller.getRanking()
    → Modelo.getRanking()
```

### Notificaciones RMI (Servidor → Cliente)

```java
// El Modelo notifica cambios a todos los observadores (Controllers)

Modelo.notificarObservadores(evento)
    → Controller.actualizar(observable, evento) [en cada cliente]
        → Vista.mostrarPartida() [actualiza interfaz]
```

### Eventos Notificados

| Evento | Cuándo se dispara | Efecto en Cliente |
|--------|-------------------|-------------------|
| **CAMBIO_TURNO** | Cambia el turno o se inicia partida | Actualiza indicador de turno |
| **PIEZA_COLOCADA** | Se coloca una pieza | Redibuja tablero |
| **PIEZA_MOVIDA** | Se mueve una pieza | Redibuja tablero |
| **PIEZA_ELIMINADA** | Se elimina una pieza | Redibuja tablero |
| **FORMACION_MOLINO** | Se forma un molino (3 en línea) | Muestra mensaje "MOLINO" |
| **GAME_WIN** | Hay un ganador | Muestra pantalla de victoria/derrota |

---

## SELECCIÓN DE VISTA

Cada jugador puede elegir independientemente su tipo de vista preferida.

### Flujo de Selección

```
[1] Usuario hace clic en "Buscar Partida"
    ↓
[2] MenuPrincipal muestra diálogo:

    ┌─────────────────────────────────┐
    │  Selecciona el tipo de vista:   │
    │                                  │
    │  [ Vista Gráfica ]               │
    │  [ Vista Consola ]               │
    └─────────────────────────────────┘
    ↓
[3] Se guarda la preferencia en:
    VistaGrafica.usarVistaGrafica = true/false
    ↓
[4] Cuando la partida comienza, VistaGrafica.mostrarPartida()
    crea la ventana apropiada según la preferencia
    ↓
[5] Jugador 1 puede ver Vista Gráfica mientras
    Jugador 2 ve Vista Consola (o viceversa)
```

### Tipos de Vista

#### Vista Gráfica (VentanaPrincipal)
- **Interfaz**: Swing con gráficos 2D
- **Interacción**: Clic en posiciones del tablero
- **Ventajas**: Intuitiva, visual, fácil de usar

#### Vista Consola (VentanaConsola)
- **Interfaz**: Texto con ASCII art
- **Interacción**: Comandos de texto
- **Comandos disponibles**:
  - `colocar A1` - Coloca pieza en A1
  - `mover A1 D1` - Mueve pieza de A1 a D1
  - `eliminar A1` - Elimina pieza del oponente en A1
  - `tablero` - Muestra el tablero
  - `estado` - Muestra estado del juego
  - `ayuda` - Muestra comandos disponibles

---

## GESTIÓN DE ESTADOS

### Estados de la Vista (Cliente)

La Vista mantiene un estado local que determina qué interfaz mostrar:

| Estado | Descripción | Interfaz Mostrada |
|--------|-------------|-------------------|
| **EN_MENU** | Usuario en menú principal | MenuPrincipal |
| **EN_ESPERANDO_JUGADORES** | Esperando emparejamiento | Diálogo "Esperando oponente..." |
| **EN_JUEGO** | Jugando partida activa | VentanaPrincipal / VentanaConsola |
| **EN_RANKING** | Viendo ranking | Diálogo de ranking |

### Filtrado de Eventos

El Controller filtra eventos según:

1. **ID de Partida**: Solo procesa eventos de su partida actual
2. **Estado de Vista**: Solo actualiza si está en el estado apropiado

```java
// En Controller.actualizar()

if (idPartidaActual != evento.getId()) {
    return; // Ignorar evento de otra partida
}

if (estadoVista == EN_JUEGO) {
    vista.mostrarPartida(); // Actualizar juego
} else if (estadoVista == EN_ESPERANDO_JUGADORES && evento == CAMBIO_TURNO) {
    vista.mostrarPartida(); // Iniciar partida
}
```

---

## EVENTOS DEL JUEGO

### Flujo de un Evento Típico

```
┌──────────────────────────────────────────────────────┐
│  EJEMPLO: Jugador coloca pieza en A1                 │
└──────────────────────────────────────────────────────┘

[Cliente 1 - Vista]
    Usuario hace clic en posición A1
    ↓
[Cliente 1 - Controller]
    Controller.colocarPieza("A1")
    ↓
[RMI →]
    Llamada remota: Modelo.colocarPieza(idPartida, "A1")
    ↓
[Servidor - Modelo]
    1. Valida que la posición esté vacía
    2. Valida que sea el turno correcto
    3. Coloca la pieza en el tablero
    4. Detecta si se formó un molino
    5. Cambia el turno (si no hay molino)
    ↓
[Servidor - Modelo]
    Notifica a TODOS los observadores:
    - Si hay molino: FORMACION_MOLINO
    - Si no: PIEZA_COLOCADA + CAMBIO_TURNO
    ↓
[← RMI Callbacks]
    Todos los Controllers reciben:
    Controller.actualizar(evento)
    ↓
[Ambos Clientes - Controller]
    1. Filtran evento (¿es de mi partida?)
    2. Filtran estado (¿estoy en juego?)
    3. Llaman a Vista.mostrarPartida()
    ↓
[Ambos Clientes - Vista]
    1. VentanaPrincipal.actualizarInterfaz()
    2. Consulta Modelo.getPartidaActual()
    3. Obtiene estado actualizado del tablero
    4. Redibuja interfaz con nueva pieza
    ↓
[Resultado Final]
    ✓ Ambos jugadores ven la pieza en A1
    ✓ Indicador de turno actualizado
    ✓ Contador de piezas actualizado
```

### Ejemplo de Código

```java
// CLIENTE 1: Usuario hace clic
panelTablero.manejarClicEnPosicion("A1")
    → controlador.colocarPieza("A1")

// SERVIDOR: Valida y actualiza
public void colocarPieza(int idPartida, String posicion) {
    IPartida partida = partidas.get(idPartida);
    if (partida.colocarPieza(posicion)) {
        if (partida.isEsperandoEliminar()) {
            notificarObservadores(new ManejadorEventos(idPartida, FORMACION_MOLINO));
        } else {
            notificarObservadores(new ManejadorEventos(idPartida, PIEZA_COLOCADA));
            notificarObservadores(new ManejadorEventos(idPartida, CAMBIO_TURNO));
        }
    }
}

// CLIENTE 1 y 2: Reciben notificación
public void actualizar(IObservableRemoto observable, Object evento) {
    if (evento instanceof ManejadorEventos e) {
        if (idPartidaActual == e.getId() && estadoVista == EN_JUEGO) {
            vista.mostrarPartida(); // Actualiza interfaz
        }
    }
}
```

---

## VENTAJAS DE LA ARQUITECTURA ACTUAL

1. **Simplicidad**:
   - No hay sistema complejo de lobbys
   - Matchmaking automático e instantáneo
   - Sin autenticación ni sesiones

2. **Escalabilidad**:
   - Múltiples partidas simultáneas
   - Modelo singleton gestiona todo centralizadamente
   - RMI maneja concurrencia automáticamente

3. **Sincronización Automática**:
   - Patrón Observer distribuido
   - Todos los clientes se actualizan automáticamente
   - Sin polling ni consultas constantes

4. **Flexibilidad de Vista**:
   - Cada jugador elige su vista preferida
   - Gráfica o consola funcionan igual
   - Fácil agregar nuevos tipos de vista

5. **Desacoplamiento**:
   - MVC bien definido
   - Vista, Controlador y Modelo independientes
   - Fácil mantenimiento y testing

---

## LIMITACIONES CONOCIDAS

1. **Sin Reconexión**:
   - Si un cliente se desconecta, la partida termina
   - No hay sistema de guardado de partidas
   - **Razón**: Simplifica la arquitectura, evita complejidad innecesaria

2. **Sin Autenticación**:
   - No hay sistema de usuarios/contraseñas
   - Cualquiera puede usar cualquier nombre
   - **Razón**: Juego casual, no requiere seguridad avanzada

3. **RMI Local**:
   - Configurado para localhost (127.0.0.1)
   - No está preparado para Internet público
   - **Razón**: Proyecto educativo, red local

4. **Puertos Manuales**:
   - Cada cliente debe seleccionar puerto diferente
   - **Razón**: Limitación de RMI, cada cliente necesita puerto para callbacks

---

## PRÓXIMOS PASOS (Posibles Mejoras)

1. **Sistema de Chat**: Comunicación entre jugadores durante la partida
2. **Temporizador de Turnos**: Límite de tiempo para cada movimiento
3. **Historial de Partidas**: Guardar estadísticas de partidas jugadas
4. **Modo Espectador**: Permitir observar partidas en curso
5. **Configuración de Red**: Facilitar conexión por Internet (port forwarding, etc.)

---

## CONCLUSIÓN

La arquitectura actual del Juego del Molino es **simple, robusta y eficiente**.
Utiliza RMI para comunicación distribuida y el patrón Observer para sincronización
automática. El sistema de matchmaking automático elimina la complejidad de gestión
de lobbys, y la flexibilidad de vistas permite a cada jugador elegir su experiencia
preferida.

El código está bien documentado, sigue principios SOLID, y es fácil de mantener
y extender. Es una excelente base para futuras mejoras y aprendizaje de sistemas
distribuidos en Java.

---

**Fin de la documentación**
