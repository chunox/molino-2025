package servidor;

import ar.edu.unlu.rmimvc.RMIMVCException;
import ar.edu.unlu.rmimvc.servidor.Servidor;
import model.clases.Modelo;
import model.interfaces.IModelo;
import java.rmi.RemoteException;

/**
 * ============================================================================
 * APLICACIÓN SERVIDOR DEL JUEGO DEL MOLINO
 * ============================================================================
 *
 * Este es el PUNTO DE ENTRADA del servidor. Aquí comienza la ejecución del
 * lado servidor de la aplicación distribuida.
 *
 * RESPONSABILIDADES:
 * 1. Inicializar el Modelo del juego (lógica de negocio centralizada)
 * 2. Crear y arrancar el servidor RMI que expondrá el Modelo remotamente
 * 3. Permitir que múltiples clientes se conecten y compartan el mismo Modelo
 *
 * FLUJO DE EJECUCIÓN:
 *
 *   [1] Se ejecuta AppServidor.main()
 *        ↓
 *   [2] Se obtiene la instancia única del Modelo (patrón Singleton)
 *        - El Modelo extiende ObservableRemoto (patrón Observer distribuido)
 *        - Se inicializan: usuarios, partidas, ranking, persistencia
 *        ↓
 *   [3] Se crea el servidor RMI con IP:Puerto configurados
 *        ↓
 *   [4] Se inicia el servidor pasándole el Modelo
 *        - El Modelo se exporta como objeto remoto RMI
 *        - El servidor queda escuchando conexiones de clientes
 *        ↓
 *   [5] El servidor queda en ejecución continua esperando:
 *        - Conexiones de nuevos clientes
 *        - Llamadas a métodos remotos del Modelo
 *        - Notificaciones del patrón Observer a clientes suscritos
 *
 * ARQUITECTURA RMI-MVC:
 *
 *    SERVIDOR                           CLIENTE(S)
 *    ┌──────────┐                      ┌──────────┐
 *    │  Modelo  │◄─────RMI────────────►│Controller│
 *    │(Lógica de│  (Llamadas remotas)  │(Observa) │
 *    │ negocio) │                      └─────┬────┘
 *    │          │                            │
 *    │Observable│──────Eventos───────────────┤
 *    │  Remoto  │   (Notificaciones          │
 *    └──────────┘    distribuidas)      ┌────▼────┐
 *                                       │  Vista  │
 *                                       └─────────┘
 *
 * IMPORTANTE:
 * - Este servidor debe estar ejecutándose ANTES de iniciar cualquier cliente
 * - El puerto 8888 debe estar disponible
 * - El Modelo es compartido por todos los clientes conectados
 * - Todas las modificaciones al juego pasan por el Modelo centralizado
 */
public class AppServidor {
    public static void main(String[] args) {
        // ===================================================================
        // CONFIGURACIÓN DE RED
        // ===================================================================
        // Define dónde escuchará el servidor RMI
        String ip = "127.0.0.1";    // Localhost (solo conexiones locales)
        String port = "8888";        // Puerto donde escuchará el servidor

        System.out.println("===========================================");
        System.out.println("    SERVIDOR DEL JUEGO DEL MOLINO");
        System.out.println("===========================================");
        System.out.println();

        try {
            // ===============================================================
            // PASO 1: INICIALIZACIÓN DEL MODELO
            // ===============================================================
            // Se obtiene la instancia única del Modelo (Singleton)
            // El Modelo:
            // - Contiene toda la lógica del juego
            // - Gestiona usuarios, partidas, ranking
            // - Extiende ObservableRemoto para notificar cambios
            // - Es compartido por TODOS los clientes
            IModelo modelo = Modelo.getInstancia();
            System.out.println("✓ Modelo inicializado correctamente");

            // ===============================================================
            // PASO 2: CREACIÓN DEL SERVIDOR RMI
            // ===============================================================
            // Servidor es una clase de la librería rmimvc que:
            // - Exporta objetos Java como objetos remotos RMI
            // - Permite que clientes remotos invoquen métodos del Modelo
            // - Gestiona el registro RMI y la comunicación de red
            Servidor servidor = new Servidor(ip, Integer.parseInt(port));
            System.out.println("✓ Servidor creado");

            // ===============================================================
            // PASO 3: INICIAR EL SERVIDOR
            // ===============================================================
            // Al iniciar el servidor con el Modelo:
            // 1. El Modelo se exporta como objeto RMI
            // 2. Se crea el registro RMI en el puerto especificado
            // 3. El servidor queda escuchando conexiones entrantes
            // 4. Los clientes podrán obtener una referencia remota al Modelo
            servidor.iniciar(modelo);
            System.out.println("✓ Servidor iniciado exitosamente");
            System.out.println();
            System.out.println("===========================================");
            System.out.println("  Servidor escuchando en:");
            System.out.println("  IP: " + ip);
            System.out.println("  Puerto: " + port);
            System.out.println("===========================================");
            System.out.println();
            System.out.println("Presiona Ctrl+C para detener el servidor");

            // ===============================================================
            // El servidor ahora está en ejecución continua
            // - Acepta conexiones de múltiples clientes
            // - Procesa llamadas remotas al Modelo
            // - Notifica eventos a clientes suscritos
            // ===============================================================

        } catch (RemoteException e) {
            System.err.println("✗ Error de RMI: " + e.getMessage());
            e.printStackTrace();
        } catch (RMIMVCException e) {
            System.err.println("✗ Error al iniciar el servidor: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
