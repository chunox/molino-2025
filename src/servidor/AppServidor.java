package servidor;

import ar.edu.unlu.rmimvc.RMIMVCException;
import ar.edu.unlu.rmimvc.servidor.Servidor;
import model.clases.Modelo;
import model.interfaces.IModelo;
import java.rmi.RemoteException;

/**
 * Aplicación servidor que inicia el servidor RMI con el modelo del juego
 */
public class AppServidor {
    public static void main(String[] args) {
        // Configuración de red
        String ip = "127.0.0.1";
        String port = "8888";

        System.out.println("===========================================");
        System.out.println("    SERVIDOR DEL JUEGO DEL MOLINO");
        System.out.println("===========================================");
        System.out.println();

        try {
            // Obtener instancia del modelo (Singleton)
            IModelo modelo = Modelo.getInstancia();
            System.out.println("✓ Modelo inicializado correctamente");

            // Crear y arrancar servidor RMI
            Servidor servidor = new Servidor(ip, Integer.parseInt(port));
            System.out.println("✓ Servidor creado");

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

        } catch (RemoteException e) {
            System.err.println("✗ Error de RMI: " + e.getMessage());
            e.printStackTrace();
        } catch (RMIMVCException e) {
            System.err.println("✗ Error al iniciar el servidor: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
