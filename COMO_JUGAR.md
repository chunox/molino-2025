# Cómo Jugar - Juego del Molino Multijugador

## Arquitectura Cliente-Servidor

El juego utiliza una arquitectura **cliente-servidor con RMI**:

- **1 Servidor Central** (AppServidor): Se ejecuta una sola vez
- **N Clientes** (AppCliente): Uno por cada jugador

## Pasos para Jugar

### 1. Iniciar el Servidor (Solo una vez)

```
Ejecutar: servidor/AppServidor.java
```

El servidor se iniciará en:
- IP: 127.0.0.1 (localhost)
- Puerto: 8888

**Consola del servidor mostrará:**
```
===========================================
    SERVIDOR DEL JUEGO DEL MOLINO
===========================================
Servidor iniciado en 127.0.0.1:8888
Esperando conexiones de clientes...
```

---

### 2. Jugador 1: Crear un Lobby

**a) Ejecutar AppCliente:**
```
Ejecutar: cliente/AppCliente.java
```

**b) Configuración inicial:**
- Ingresar puerto del cliente (ej: `9999`)
- Ingresar tu nombre (ej: `Juan`)

**c) Menú Principal - Opciones:**
```
┌─────────────────────────────┐
│   JUEGO DEL MOLINO          │
├─────────────────────────────┤
│ Jugador: Juan               │
│                             │
│ [Crear Nueva Partida]       │
│ [Unirse a Partida]          │
│ [Ver Ranking]               │
│ [Salir]                     │
└─────────────────────────────┘
```

**d) Crear Lobby:**
1. Click en **"Crear Nueva Partida"**
2. Seleccionar tu vista preferida:
   - **Gráfica**: Interfaz con tablero visual y mouse
   - **Consola**: Interfaz de terminal con comandos
3. Click en **"Crear Partida"**

**e) Sala de Espera:**
```
┌─────────────────────────────┐
│      🎮 Lobby Creado        │
│                             │
│      ┌──────────────┐       │
│      │  Lobby #1    │       │
│      └──────────────┘       │
│                             │
│  Creado por: Juan           │
│  ⏳ Esperando jugador...    │
│                             │
│  [▓▓▓▓▓▓▓▓▓▓▓]             │
│                             │
│      [Cancelar]             │
└─────────────────────────────┘
```

Tu ventana de juego se abrirá en segundo plano, esperando al oponente.

---

### 3. Jugador 2: Unirse al Lobby

**a) Ejecutar otro AppCliente** (en la misma o diferente máquina):
```
Ejecutar: cliente/AppCliente.java
```

**b) Configuración inicial:**
- Ingresar puerto diferente (ej: `10000`)
- Ingresar tu nombre (ej: `María`)

**c) Menú Principal:**
```
┌─────────────────────────────┐
│   JUEGO DEL MOLINO          │
├─────────────────────────────┤
│ Jugador: María              │
│                             │
│ [Crear Nueva Partida]       │
│ [Unirse a Partida]    ← AQUÍ│
│ [Ver Ranking]               │
│ [Salir]                     │
└─────────────────────────────┘
```

**d) Lista de Lobbys Disponibles:**
Click en **"Unirse a Partida"**

```
┌──────────────────────────────────────┐
│  Partidas Disponibles   [🔄 Actualizar]│
├──────────────────────────────────────┤
│ ID │ Jugador 1 │ Estado       │ Jug. │
├────┼───────────┼──────────────┼──────┤
│ 1  │ Juan      │ Esperando... │ 1/2  │
│ 3  │ Pedro     │ Esperando... │ 1/2  │
└──────────────────────────────────────┘

Tu Vista: [Gráfica ▼]

              [Unirse a Partida] [Cerrar]
```

**e) Unirse:**
1. Seleccionar un lobby de la tabla
2. Elegir tu vista preferida (Gráfica o Consola)
3. Click en **"Unirse a Partida"**

**f) ¡El juego comienza!**
- El Jugador 1 verá: "¡María se ha unido!"
- Su Sala de Espera se cerrará automáticamente
- Ambos jugadores verán sus ventanas de juego activas
- Comienza la partida (Jugador 1 tiene el primer turno)

---

## Opciones de Vista

### Vista Gráfica
- **Interfaz:** Ventana con tablero visual
- **Controles:** Click del mouse en las posiciones
- **Características:**
  - Tablero con círculos de colores (Rojo/Azul)
  - Indicador visual de posición seleccionada
  - Panel de información del turno
  - Detección automática de molinos

### Vista Consola
- **Interfaz:** Terminal estilo retro (negro con texto verde)
- **Controles:** Comandos de texto
- **Comandos disponibles:**
  ```
  ayuda              - Muestra comandos disponibles
  estado             - Muestra estado del juego
  tablero            - Muestra el tablero
  mt                 - Muestra mapa de posiciones
  colocar <pos>      - Coloca pieza (ej: colocar A1)
  mover <o> <d>      - Mueve pieza (ej: mover A1 D1)
  eliminar <pos>     - Elimina pieza oponente
  salir              - Cierra la ventana
  ```

---

## Ejemplo de Sesión Completa

### Servidor:
```bash
# Terminal 1
java servidor.AppServidor

# Output:
===========================================
    SERVIDOR DEL JUEGO DEL MOLINO
===========================================
Servidor iniciado en 127.0.0.1:8888
Esperando conexiones de clientes...
```

### Jugador 1 (Juan):
```bash
# Terminal 2
java cliente.AppCliente

# Ingresar:
Puerto: 9999
Nombre: Juan

# En la GUI:
Menú → Crear Nueva Partida → Vista: Gráfica → Crear
# Se abre ventana de juego + Sala de Espera
```

### Jugador 2 (María):
```bash
# Terminal 3 (o en otra computadora)
java cliente.AppCliente

# Ingresar:
Puerto: 10000
Nombre: María

# En la GUI:
Menú → Unirse a Partida → Seleccionar Lobby #1 → Vista: Consola → Unirse
# Se abre ventana consola y comienza el juego
```

---

## Múltiples Partidas Simultáneas

El servidor puede manejar múltiples lobbys al mismo tiempo:

```
Lobby #1: Juan vs María    (En juego)
Lobby #2: Pedro vs...      (Esperando jugador)
Lobby #3: Ana vs Luis      (En juego)
Lobby #4: Carlos vs...     (Esperando jugador)
```

Cada jugador puede:
- Crear un nuevo lobby
- Unirse a cualquier lobby disponible (1/2 jugadores)
- Jugar solo una partida a la vez

---

## Ver Ranking

Desde el menú principal, cualquier jugador puede ver el ranking:

```
┌───────────────────────────┐
│   RANKING DE JUGADORES    │
├───────────────────────────┤
│ 1. María  - 5 victorias   │
│ 2. Juan   - 3 victorias   │
│ 3. Pedro  - 2 victorias   │
│ 4. Ana    - 1 victoria    │
└───────────────────────────┘
```

El ranking se actualiza automáticamente cuando alguien gana una partida.

---

## Requisitos Técnicos

- **Java:** JDK 11 o superior
- **Librería:** LibreriaRMIMVC.jar (incluida en `/libs`)
- **Red:** Todos los clientes deben poder conectarse al servidor
  - Mismo equipo: usar `127.0.0.1`
  - Red local: usar IP del servidor (ej: `192.168.1.100`)
- **Puertos:**
  - Servidor: 8888
  - Clientes: 9999, 10000, 10001... (diferentes para cada jugador)

---

## Solución de Problemas

### "Error de conexión al servidor"
- Verificar que AppServidor esté ejecutándose
- Verificar que la IP sea correcta (127.0.0.1 para local)
- Verificar que el puerto 8888 esté disponible

### "No hay partidas disponibles"
- Otro jugador debe crear un lobby primero
- Click en "Actualizar" para refrescar la lista

### "Error al crear lobby"
- Verificar conexión al servidor
- Verificar que el nombre del jugador no esté vacío

---

¡Disfruta del Juego del Molino! 🎮
