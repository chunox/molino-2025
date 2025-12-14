# Debug de Sistema de Lobbys

## Cambios Realizados para Corregir Problemas

### 1. Mejoras en Partida.java

**Problema anterior:** El estado de la partida no se actualizaba correctamente cuando se creaba o cuando se unía el segundo jugador.

**Solución:**
- El constructor ahora establece `EstadoPartida.EN_ESPERA` cuando se crea con 1 jugador
- El constructor establece `EstadoPartida.EN_JUEGO` cuando se crea con 2 jugadores reales
- El método `agregarJugador()` cambia automáticamente el estado a `EN_JUEGO` cuando se alcanza 2 jugadores
- Agregados logs detallados en cada paso

**Logs agregados:**
```
[Partida #X] Lobby creado (1/2): NombreJugador1
[Partida #X] NombreJugador2 se agregó (2/2)
[Partida #X] ¡Partida completa! Estado -> EN_JUEGO
```

### 2. Mejoras en Modelo.java

**Problema anterior:** Las notificaciones del Observer no eran claras.

**Solución:**
- Agregados logs cuando se crea una partida
- Agregados logs cuando se buscan partidas (mostrando lobbys disponibles)
- Agregados logs cuando alguien se une a una partida
- Agregado log cuando se notifica a los observadores

**Logs agregados:**
```
📝 Lobby #X creado por NombreJugador (1/2 jugadores)
🔍 Buscando lobbys... (N total)
   - Lobby #X: NombreJugador (1/2)
   ✓ N lobbys disponibles
✅ NombreJugador se unió al Lobby #X (2/2 jugadores) - ¡Partida iniciada!
📡 Notificando evento CAMBIO_ESPERANDO_JUGADORES a observadores
```

### 3. Mejoras en Controller.java

**Problema anterior:** No había visibilidad de qué eventos se estaban recibiendo y procesando.

**Solución:**
- Agregados logs cuando se recibe un evento del Observer
- Agregados logs mostrando el estado actual de la vista
- Agregados logs específicos para cada tipo de evento procesado

**Logs agregados:**
```
[Controller-NombreJugador] Evento recibido: CAMBIO_ESPERANDO_JUGADORES de Partida #X
[Controller-NombreJugador] Estado actual: EN_ESPERANDO_JUGADORES
[Controller-NombreJugador] Procesando CAMBIO_ESPERANDO_JUGADORES
[Controller-NombreJugador] Segundo jugador detectado, cerrando sala espera
```

### 4. Mejoras en SalaEspera.java

**Problema anterior:** No había visibilidad de cuándo el timer estaba verificando jugadores.

**Solución:**
- Agregados logs cada vez que el timer verifica (cada 1 segundo)
- Agregado log cuando se detecta el segundo jugador

**Logs agregados:**
```
[SalaEspera-NombreJugador] Verificando... 1/2 jugadores
[SalaEspera-NombreJugador] Verificando... 2/2 jugadores
[SalaEspera-NombreJugador] ¡Segundo jugador detectado! Cerrando sala...
```

### 5. Mejoras en ListaPartidas.java

**Logs agregados:**
```
[ListaPartidas-NombreJugador] Uniéndose a Lobby #X
[ListaPartidas-NombreJugador] Unido exitosamente, cambiando estado a EN_JUEGO
```

### 6. Mejoras en MenuCrearPartida.java

**Logs agregados:**
```
[MenuCrearPartida-NombreJugador] Creando nuevo lobby...
[MenuCrearPartida-NombreJugador] Lobby #X creado
[MenuCrearPartida-NombreJugador] Estado cambiado a EN_ESPERANDO_JUGADORES
```

---

## Flujo Completo con Logs Esperados

### Jugador 1: Crear Lobby

**Consola Servidor:**
```
📝 Lobby #0 creado por Juan (1/2 jugadores)
   [Partida #0] Lobby creado (1/2): Juan
```

**Consola Cliente Jugador 1:**
```
[MenuCrearPartida-Juan] Creando nuevo lobby...
[MenuCrearPartida-Juan] Lobby #0 creado
[MenuCrearPartida-Juan] Estado cambiado a EN_ESPERANDO_JUGADORES
[SalaEspera-Juan] Verificando... 1/2 jugadores
[SalaEspera-Juan] Verificando... 1/2 jugadores
[SalaEspera-Juan] Verificando... 1/2 jugadores
...
```

### Jugador 2: Buscar Lobbys

**Consola Servidor:**
```
🔍 Buscando lobbys... (1 total)
   - Lobby #0: Juan (1/2)
   ✓ 1 lobbys disponibles
```

**Consola Cliente Jugador 2:**
```
# (Se muestra la tabla con Lobby #0)
```

### Jugador 2: Unirse al Lobby

**Consola Servidor:**
```
   [Partida #0] María se agregó (2/2)
   [Partida #0] ¡Partida completa! Estado -> EN_JUEGO
✅ María se unió al Lobby #0 (2/2 jugadores) - ¡Partida iniciada!
📡 Notificando evento CAMBIO_ESPERANDO_JUGADORES a observadores
```

**Consola Cliente Jugador 2:**
```
[ListaPartidas-María] Uniéndose a Lobby #0
[ListaPartidas-María] Unido exitosamente, cambiando estado a EN_JUEGO
[Controller-María] Evento recibido: CAMBIO_ESPERANDO_JUGADORES de Partida #0
[Controller-María] Estado actual: EN_JUEGO
```

**Consola Cliente Jugador 1:**
```
[Controller-Juan] Evento recibido: CAMBIO_ESPERANDO_JUGADORES de Partida #0
[Controller-Juan] Estado actual: EN_ESPERANDO_JUGADORES
[Controller-Juan] Procesando CAMBIO_ESPERANDO_JUGADORES
[Controller-Juan] Segundo jugador detectado, cerrando sala espera
[SalaEspera-Juan] Verificando... 2/2 jugadores
[SalaEspera-Juan] ¡Segundo jugador detectado! Cerrando sala...
```

---

## Cómo Diagnosticar Problemas

### 1. El segundo jugador no ve el lobby en la lista

**Verificar en consola del servidor:**
- ¿Aparece el mensaje `📝 Lobby #X creado...`?
- Cuando el Jugador 2 busca partidas, ¿aparece `🔍 Buscando lobbys...` con el lobby listado?

**Si NO aparece en la búsqueda:**
- El problema está en `Modelo.getPartidas()` o `ListaPartidas.cargarPartidas()`
- Verificar que la partida tenga `getJugadores().size() < 2`

### 2. La SalaEspera no se cierra cuando el segundo jugador se une

**Verificar en consola del servidor:**
- ¿Aparece `✅ NombreJugador se unió al Lobby #X`?
- ¿Aparece `📡 Notificando evento CAMBIO_ESPERANDO_JUGADORES`?

**Verificar en consola del Cliente Jugador 1:**
- ¿Aparece `[Controller-...] Evento recibido: CAMBIO_ESPERANDO_JUGADORES`?
- ¿Aparece `[SalaEspera-...] Verificando... 2/2 jugadores`?
- ¿Aparece `[SalaEspera-...] ¡Segundo jugador detectado!`?

**Si NO se cierra:**
- El problema puede ser:
  - Observer no está notificando correctamente
  - Timer de SalaEspera no está funcionando
  - `getPartidaActual()` no está retornando la partida correcta

### 3. Las ventanas de juego no se abren

**Verificar en consola del cliente:**
- Después de crear/unirse, ¿aparecen mensajes de "Vista Gráfica" o "Consola"?
- ¿Hay excepciones o errores?

### 4. El Observer no notifica eventos

**Verificar en consola del servidor:**
- Cuando se ejecuta una acción, ¿aparece `📡 Notificando evento...`?

**Verificar en consola del cliente:**
- ¿Aparece `[Controller-...] Evento recibido: ...`?
- ¿El estado de la vista es correcto?

**Si NO se reciben eventos:**
- Verificar que el cliente esté registrado como observador en el servidor
- Verificar que `idPartidaActual` coincida con el ID del evento

---

## Testing Paso a Paso

### Test 1: Crear y Unirse a un Lobby

1. Ejecutar AppServidor
2. Ejecutar AppCliente #1 (puerto 9999, nombre "Juan")
3. Juan: Crear Nueva Partida → Vista Gráfica → Crear
4. **Verificar consola servidor:** Debería aparecer "Lobby #0 creado por Juan (1/2)"
5. **Verificar consola Juan:** Debería aparecer "SalaEspera verificando... 1/2"
6. Ejecutar AppCliente #2 (puerto 10000, nombre "María")
7. María: Unirse a Partida
8. **Verificar tabla:** Debería mostrar "Lobby #0, Juan, Esperando..., 1/2"
9. María: Seleccionar Lobby #0 → Vista Consola → Unirse
10. **Verificar consola servidor:** Debería aparecer "María se unió... (2/2)"
11. **Verificar consola Juan:** SalaEspera debería cerrarse
12. **Verificar ventanas:** Ambos deberían tener sus ventanas de juego abiertas

### Test 2: Múltiples Lobbys

1. Con servidor ejecutándose
2. Cliente #1 crea Lobby
3. Cliente #2 crea Lobby
4. Cliente #3 busca partidas
5. **Verificar:** Debería ver 2 lobbys en la lista

---

## Problemas Conocidos y Soluciones

### Problema: "No hay lobbys disponibles" pero sí existen

**Causa:** La partida tiene 2 jugadores (incluido el placeholder)

**Solución:** Ya corregido en Partida.java - ahora NO agrega el jugador placeholder

### Problema: SalaEspera nunca se cierra

**Causa posible 1:** Timer no está funcionando
**Solución:** Verificar logs `[SalaEspera-...] Verificando...`

**Causa posible 2:** `getPartidaActual()` retorna null
**Solución:** Verificar que `idPartidaActual` esté asignado correctamente

**Causa posible 3:** La partida sigue teniendo 1 jugador
**Solución:** Verificar logs del servidor cuando el segundo jugador se une

### Problema: Ventanas de juego se abren pero están vacías

**Causa:** El tablero no se está actualizando
**Solución:** Verificar que `actualizarInterfaz()` se llame y que `getEstadoTablero()` funcione

---

## Logs Completos de una Sesión Exitosa

Ver archivo `LOGS_EJEMPLO.md` para un ejemplo completo de todos los logs en una sesión exitosa desde inicio hasta finalización de partida.
