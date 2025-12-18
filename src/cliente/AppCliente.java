package cliente;

import ar.edu.unlu.rmimvc.RMIMVCException;
import ar.edu.unlu.rmimvc.cliente.Cliente;
import view.vistas.VistaGrafica;
import view.interfaces.IVista;
import javax.swing.*;
import java.rmi.RemoteException;
import java.net.ServerSocket;
import java.io.IOException;

/**
 * ============================================================================
 * APLICACIÓN CLIENTE DEL JUEGO DEL MOLINO
 * ============================================================================
 *
 * Este es el PUNTO DE ENTRADA del cliente. Aquí comienza la ejecución de cada
 * jugador que se conecta al servidor.
 *
 * RESPONSABILIDADES:
 * 1. Crear la Vista (interfaz gráfica) y su Controlador
 * 2. Conectarse al servidor RMI para obtener acceso al Modelo remoto
 * 3. Suscribir el Controlador como observador del Modelo (patrón Observer)
 * 4. Iniciar la interfaz gráfica del jugador
 *
 * FLUJO DE EJECUCIÓN:
 *
 *   [1] Se ejecuta AppCliente.main()
 *        ↓
 *   [2] Se solicita el nombre del jugador (JOptionPane)
 *        ↓
 *   [3] Se crea la Vista (VistaGrafica)
 *        - Internamente crea un Controller
 *        - El Controller se asocia a la Vista
 *        - Se establece el estado inicial: EN_MENU
 *        ↓
 *   [4] Se crea el Cliente RMI con:
 *        - IP y puerto del cliente (para recibir callbacks)
 *        - IP y puerto del servidor (donde está el Modelo)
 *        ↓
 *   [5] Se inicia la conexión: cliente.iniciar(controlador)
 *        - El cliente se conecta al servidor RMI
 *        - Obtiene una referencia remota al Modelo
 *        - El Modelo se asigna al Controller: setModeloRemoto()
 *        - El Controller se SUSCRIBE al Modelo como observador
 *        ↓
 *   [6] Se muestra el menú principal: vista.menu()
 *        - El jugador puede: buscar partida (matchmaking automático), ver ranking
 *        ↓
 *   [7] A partir de aquí, el flujo es controlado por eventos:
 *        - Interacciones del usuario → Controller → Modelo (RMI)
 *        - Cambios en el Modelo → Notificaciones → Controller → Vista
 *
 * ARQUITECTURA DE COMPONENTES DEL CLIENTE:
 *
 *    ┌─────────────────────────────────────────┐
 *    │          APLICACIÓN CLIENTE             │
 *    ├─────────────────────────────────────────┤
 *    │                                         │
 *    │  ┌──────────┐     ┌────────────────┐    │
 *    │  │  Vista   │◄────┤   Controller   │    │
 *    │  │ Gráfica  │     │  (Observador)  │    │
 *    │  └──────────┘     └───────┬────────┘    │
 *    │      ▲                    │             │
 *    │      │                    ▼             │
 *    │  [Usuario]         ┌──────────┐         │
 *    │  Interactúa        │ Cliente  │         │
 *    │                    │   RMI    │         │
 *    └────────────────────┴────┬─────┴────────-┘
 *                              │
 *                              │ RMI
 *                              │
 *    ┌─────────────────────────▼──────────────┐
 *    │           SERVIDOR REMOTO              │
 *    │         ┌────────────────┐             │
 *    │         │     Modelo     │             │
 *    │         │ (Observable)   │             │
 *    │         └────────────────┘             │
 *    └────────────────────────────────────────┘
 *
 * COMUNICACIÓN BIDIRECCIONAL:
 *
 * Cliente → Servidor (Llamadas RMI):
 *   - Usuario hace clic → Controller.metodo() → Modelo.metodo() [RMI]
 *   - Ejemplo: Colocar pieza, mover pieza, buscar partida
 *
 * Servidor → Cliente (Notificaciones Observer):
 *   - Modelo cambia → notificarObservadores() → Controller.actualizar() [RMI]
 *   - Controller actualiza la Vista
 *   - Ejemplo: Otro jugador movió, se formó un molino
 *
 * IMPORTANTE:
 * - Pueden ejecutarse múltiples clientes simultáneamente
 * - Cada cliente debe usar un puerto diferente
 * - El servidor debe estar ejecutándose antes de iniciar clientes
 * - Todos los clientes comparten el mismo Modelo en el servidor
 */
public class AppCliente {
    public static void main(String[] args) {
        // ===================================================================
        // CONFIGURACIÓN DEL CLIENTE
        // ===================================================================
        // Cada cliente necesita su propio puerto para recibir callbacks RMI
        String ipCliente = "127.0.0.1";
        String portCliente = seleccionarPuerto();

        if (portCliente == null) {
            System.out.println("No se seleccionó puerto. Cerrando aplicación...");
            return;
        }

        // ===================================================================
        // CONFIGURACIÓN DEL SERVIDOR
        // ===================================================================
        // Dirección donde se encuentra el servidor RMI
        String ipServidor = "127.0.0.1";
        String portServidor = "8888";

        System.out.println("===========================================");
        System.out.println("    CLIENTE DEL JUEGO DEL MOLINO");
        System.out.println("===========================================");
        System.out.println();
        System.out.println("Configuración:");
        System.out.println("  IP Cliente: " + ipCliente);
        System.out.println("  Puerto Cliente: " + portCliente);
        System.out.println("  IP Servidor: " + ipServidor);
        System.out.println("  Puerto Servidor: " + portServidor);
        System.out.println();

        try {
            // ===============================================================
            // PASO 1: SOLICITAR IDENTIFICACIÓN DEL JUGADOR
            // ===============================================================
            String nombreJugador = JOptionPane.showInputDialog(
                    null,
                    "Ingrese su nombre:",
                    "Bienvenido",
                    JOptionPane.QUESTION_MESSAGE
            );

            if (nombreJugador == null || nombreJugador.trim().isEmpty()) {
                System.out.println("Nombre no ingresado. Saliendo...");
                return;
            }

            // ===============================================================
            // PASO 2: CREAR LA VISTA Y EL CONTROLADOR
            // ===============================================================
            // Al crear VistaGrafica:
            // 1. Se instancia un nuevo Controller
            // 2. El Controller se asocia con la Vista
            // 3. Se establece el estado inicial (EN_MENU)
            IVista vista = new VistaGrafica();

            // Configurar el nombre del jugador en el controlador
            vista.getControlador().setNombreJugador(nombreJugador.trim());
            System.out.println("✓ Vista creada para " + nombreJugador);

            // ===============================================================
            // PASO 3: CREAR EL CLIENTE RMI
            // ===============================================================
            // Cliente es una clase de la librería rmimvc que:
            // - Se conecta al registro RMI del servidor
            // - Obtiene referencias remotas a objetos del servidor
            // - Permite que el servidor haga callbacks al cliente
            Cliente cliente = new Cliente(
                    ipCliente, Integer.parseInt(portCliente),   // Donde escucha este cliente
                    ipServidor, Integer.parseInt(portServidor)  // Donde está el servidor
            );
            System.out.println("✓ Cliente creado");

            // ===============================================================
            // PASO 4: CONECTAR AL SERVIDOR
            // ===============================================================
            // Al llamar cliente.iniciar(controlador):
            // 1. Se conecta al servidor RMI en ipServidor:portServidor
            // 2. Obtiene una referencia remota al Modelo
            // 3. Llama a controlador.setModeloRemoto(modelo)
            // 4. El Modelo agrega al Controller como observador
            //
            // IMPORTANTE: A partir de este momento:
            // - El Controller puede llamar métodos del Modelo remotamente
            // - El Modelo puede notificar cambios al Controller
            cliente.iniciar(vista.getControlador());
            System.out.println("✓ Conexión establecida con el servidor");
            System.out.println("===========================================");

            // ===============================================================
            // PASO 5: MOSTRAR INTERFAZ GRÁFICA
            // ===============================================================
            // Se muestra el menú principal al jugador
            // A partir de aquí, el flujo es dirigido por eventos:
            // - Eventos de UI (clicks, teclas) → Controller
            // - Controller invoca métodos del Modelo (RMI)
            // - Modelo notifica cambios → Controller.actualizar()
            // - Controller actualiza la Vista
            vista.menu();

            // ===============================================================
            // El cliente ahora está en ejecución continua
            // - Responde a interacciones del usuario
            // - Recibe notificaciones del servidor
            // - Actualiza la interfaz gráfica según el estado del juego
            // ===============================================================

        } catch (RemoteException e) {
            System.err.println("✗ Error de RMI: " + e.getMessage());
            e.printStackTrace();
            JOptionPane.showMessageDialog(
                    null,
                    "Error al conectar con el servidor RMI.\nVerifique que el servidor esté en ejecución.",
                    "Error de Conexión",
                    JOptionPane.ERROR_MESSAGE
            );
        } catch (RMIMVCException e) {
            System.err.println("✗ Error al iniciar el cliente: " + e.getMessage());
            e.printStackTrace();
            JOptionPane.showMessageDialog(
                    null,
                    "Error al iniciar el cliente.\n" + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    /**
     * SELECCIONAR PUERTO DEL CLIENTE
     *
     * Muestra un diálogo con puertos recomendados para facilitar la
     * configuración del cliente. Valida que el puerto esté disponible
     * antes de aceptarlo.
     *
     * PUERTOS RECOMENDADOS:
     * - 9999: Jugador 1 (por defecto)
     * - 10000: Jugador 2
     * - 10001: Jugador 3
     * - Otro: Permite ingresar un puerto personalizado
     *
     * @return El puerto seleccionado como String, o null si se cancela
     */
    private static String seleccionarPuerto() {
        while (true) {  // Repetir hasta obtener un puerto válido
            // ===============================================================
            // Opciones de puertos recomendados
            // ===============================================================
            String[] opciones = {
                    "9999 (Jugador 1)",
                    "10000 (Jugador 2)",
                    "10001 (Jugador 3)",
                    "Otro puerto..."
            };

            // ===============================================================
            // Mostrar diálogo de selección
            // ===============================================================
            int seleccion = JOptionPane.showOptionDialog(
                    null,
                    "Seleccione el puerto del cliente:\n" +
                    "(Cada cliente debe usar un puerto diferente)",
                    "Configuración del Cliente",
                    JOptionPane.DEFAULT_OPTION,
                    JOptionPane.QUESTION_MESSAGE,
                    null,
                    opciones,
                    opciones[0]  // Opción por defecto: 9999
            );

            // ===============================================================
            // Procesar la selección
            // ===============================================================
            String puertoSeleccionado = null;
            switch (seleccion) {
                case 0:  // Puerto 9999
                    puertoSeleccionado = "9999";
                    break;
                case 1:  // Puerto 10000
                    puertoSeleccionado = "10000";
                    break;
                case 2:  // Puerto 10001
                    puertoSeleccionado = "10001";
                    break;
                case 3:  // Otro puerto (ingresar manualmente)
                    puertoSeleccionado = solicitarPuertoPersonalizado();
                    break;
                default: // Usuario canceló
                    return null;
            }

            // ===============================================================
            // Validar que el puerto esté disponible
            // ===============================================================
            if (puertoSeleccionado != null && verificarPuertoDisponible(puertoSeleccionado)) {
                return puertoSeleccionado;
            } else if (puertoSeleccionado != null) {
                // Puerto en uso, mostrar error y volver a pedir
                JOptionPane.showMessageDialog(
                        null,
                        "⚠ El puerto " + puertoSeleccionado + " ya está en uso.\n" +
                        "Por favor, seleccione otro puerto.",
                        "Puerto No Disponible",
                        JOptionPane.ERROR_MESSAGE
                );
                // El bucle while continuará y pedirá otro puerto
            } else {
                // Usuario canceló en puerto personalizado
                return null;
            }
        }
    }

    /**
     * SOLICITAR PUERTO PERSONALIZADO
     *
     * Muestra un cuadro de diálogo para que el usuario ingrese
     * un puerto personalizado. Valida el formato pero NO la disponibilidad
     * (eso se hace en seleccionarPuerto()).
     *
     * @return El puerto ingresado, o null si se cancela
     */
    private static String solicitarPuertoPersonalizado() {
        while (true) {
            String puerto = JOptionPane.showInputDialog(
                    null,
                    "Ingrese el puerto del cliente (1024-65535):",
                    "Puerto Personalizado",
                    JOptionPane.QUESTION_MESSAGE
            );

            // Usuario canceló
            if (puerto == null) {
                return null;
            }

            // Validar que no esté vacío
            if (puerto.trim().isEmpty()) {
                JOptionPane.showMessageDialog(
                        null,
                        "Debe ingresar un puerto válido.",
                        "Error",
                        JOptionPane.ERROR_MESSAGE
                );
                continue;  // Volver a pedir
            }

            // Validar que sea un número válido
            try {
                int puertoNum = Integer.parseInt(puerto.trim());
                if (puertoNum < 1024 || puertoNum > 65535) {
                    JOptionPane.showMessageDialog(
                            null,
                            "Puerto fuera de rango.\n" +
                            "Debe estar entre 1024 y 65535.",
                            "Error",
                            JOptionPane.ERROR_MESSAGE
                    );
                    continue;  // Volver a pedir
                }
                // Puerto válido
                return puerto.trim();
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(
                        null,
                        "Debe ingresar un número válido.",
                        "Error",
                        JOptionPane.ERROR_MESSAGE
                );
                // Volver a pedir
            }
        }
    }

    /**
     * VERIFICAR DISPONIBILIDAD DEL PUERTO
     *
     * Intenta abrir un ServerSocket en el puerto especificado para
     * verificar si está disponible.
     *
     * IMPORTANTE:
     * - Si el puerto está libre, el método retorna true
     * - Si el puerto está en uso, retorna false
     * - El ServerSocket se cierra inmediatamente después de la verificación
     *
     * @param puerto El puerto a verificar (como String)
     * @return true si el puerto está disponible, false si está en uso
     */
    private static boolean verificarPuertoDisponible(String puerto) {
        try {
            int puertoNum = Integer.parseInt(puerto);

            // Intentar abrir un ServerSocket en el puerto
            // Si está en uso, lanzará IOException
            try (ServerSocket serverSocket = new ServerSocket(puertoNum)) {
                // Puerto disponible
                System.out.println("✓ Puerto " + puerto + " verificado y disponible");
                return true;
            } catch (IOException e) {
                // Puerto en uso
                System.out.println("✗ Puerto " + puerto + " está en uso");
                return false;
            }
        } catch (NumberFormatException e) {
            // Puerto inválido
            return false;
        }
    }
}
