# Juego del Molino - Implementación Distribuida

Implementación del juego tradicional del Molino (Nine Men's Morris) usando Java con arquitectura MVC, RMI, Observer y Persistencia.

## 🎮 Descripción

El Juego del Molino es un juego de estrategia para dos jugadores donde cada uno intenta formar "molinos" (3 fichas en línea) para eliminar las piezas del oponente.

## 🏗️ Arquitectura

Este proyecto implementa una arquitectura robusta basada en:

- **MVC (Modelo-Vista-Controlador)**: Separación clara de responsabilidades
- **Observer**: Notificaciones automáticas de cambios
- **RMI (Remote Method Invocation)**: Comunicación cliente-servidor distribuida
- **Persistencia**: Guardado automático de datos

Ver documentación completa en: [README_ARQUITECTURA.md](README_ARQUITECTURA.md)

## 📋 Requisitos

- Java 11 o superior
- Librería RMI-MVC: https://github.com/mlapeducacionit/rmimvc

## 🚀 Cómo Ejecutar

### 1. Compilar el Proyecto

```bash
# Desde la raíz del proyecto
javac -d bin -sourcepath src src/servidor/AppServidor.java src/cliente/AppCliente.java
```

O usar tu IDE favorito (IntelliJ IDEA, Eclipse, etc.)

### 2. Iniciar el Servidor

**Terminal 1:**
```bash
java servidor.AppServidor
```

Deberías ver:
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
```

### 3. Iniciar Clientes

Puedes iniciar múltiples clientes (uno por jugador):

**Terminal 2 (Jugador 1):**
```bash
java cliente.AppCliente
```
- Cuando se solicite, ingresar puerto: `9999`

**Terminal 3 (Jugador 2):**
```bash
java cliente.AppCliente
```
- Cuando se solicite, ingresar puerto: `10000`

**IMPORTANTE**: Cada cliente debe usar un puerto diferente.

## 🎯 Características Implementadas

✅ Sistema cliente-servidor con RMI
✅ Múltiples partidas simultáneas
✅ Persistencia automática de:
  - Usuarios registrados
  - Partidas en curso
  - Ranking de victorias
✅ Reconexión de jugadores
✅ Notificaciones en tiempo real
✅ Arquitectura MVC robusta

## 📁 Estructura del Proyecto

```
src/
├── model/              # Modelo (Lógica de negocio)
│   ├── clases/         # Implementaciones
│   ├── interfaces/     # Contratos
│   ├── enums/          # Enumeraciones
│   └── excepciones/    # Excepciones personalizadas
├── view/               # Vista (Presentación)
│   ├── vistas/         # Implementaciones de vista
│   └── interfaces/     # Contratos de vista
├── controller/         # Controlador (Coordinación)
├── servidor/           # Servidor RMI
├── cliente/            # Cliente RMI
├── serializacion/      # Persistencia
└── data/               # Datos persistidos (se crea automáticamente)
```

## 🎲 Reglas del Juego

### Fase 1: Colocación
- Cada jugador tiene 9 fichas
- Los jugadores alternan colocando una ficha a la vez
- El objetivo es formar "molinos" (3 fichas en línea)

### Fase 2: Movimiento
- Una vez colocadas todas las fichas, se pueden mover a posiciones adyacentes
- Se sigue intentando formar molinos

### Fase 3: Vuelo (cuando un jugador tiene 3 fichas)
- El jugador puede mover sus fichas a cualquier posición libre

### Victoria
Un jugador gana cuando:
- El oponente queda con menos de 3 fichas
- El oponente no puede realizar movimientos

### Molino
- Cuando un jugador forma un molino, puede eliminar una ficha del oponente
- No se puede eliminar una ficha que forma parte de un molino (a menos que todas las fichas del oponente estén en molinos)

## 📊 Persistencia

Los datos se guardan automáticamente en archivos `.dat`:

- `src/data/usuarios.dat`: Usuarios registrados
- `src/data/partidas.dat`: Partidas guardadas
- `src/data/ranking.dat`: Ranking de victorias

## 🔧 Desarrollo

### Flujo de una Acción

1. Usuario realiza acción → Vista
2. Vista → Controlador (método específico)
3. Controlador → Modelo (vía RMI)
4. Modelo ejecuta lógica y persiste
5. Modelo notifica a todos los observadores
6. Controladores reciben notificación
7. Controladores actualizan sus vistas

### Agregar Nueva Funcionalidad

1. Definir método en `IModelo`
2. Implementar en `Modelo`
3. Agregar método delegado en `Controller`
4. Actualizar `IVista` si es necesario
5. Implementar en las vistas concretas

## 📖 Documentación Adicional

- [README_ARQUITECTURA.md](README_ARQUITECTURA.md) - Documentación detallada de la arquitectura
- [LINEAMIENTOS_ARQUITECTURA.md](LINEAMIENTOS_ARQUITECTURA.md) - Lineamientos de diseño
- [PROMPT_IMPLEMENTACION.md](PROMPT_IMPLEMENTACION.md) - Prompt de implementación

## 🐛 Solución de Problemas

### Error: "Connection refused"
- Verificar que el servidor esté ejecutándose
- Verificar IP y puerto en `AppServidor` y `AppCliente`

### Error: "Port already in use"
- Cambiar el puerto del cliente a uno diferente
- Verificar que no haya otro proceso usando el puerto

### Error: "ClassNotFoundException"
- Verificar que la librería RMI-MVC esté en el classpath
- Recompilar el proyecto

## 👥 Autores

Proyecto desarrollado siguiendo los lineamientos de arquitectura MVC + Observer + RMI + Persistencia.

## 📄 Licencia

Este proyecto es de código abierto para fines educativos.
