package cliente;

import ar.edu.unlu.rmimvc.RMIMVCException;
import ar.edu.unlu.rmimvc.cliente.Cliente;
import view.vistas.VistaGrafica;
import view.interfaces.IVista;
import javax.swing.*;
import java.rmi.RemoteException;

/**
 * Aplicación cliente que se conecta al servidor RMI
 */
public class AppCliente {
    public static void main(String[] args) {
        // Configuración del cliente
        String ipCliente = "127.0.0.1";
        String portCliente = JOptionPane.showInputDialog(
                null,
                "Ingrese el puerto del cliente:",
                "Configuración del Cliente",
                JOptionPane.QUESTION_MESSAGE
        );

        if (portCliente == null || portCliente.trim().isEmpty()) {
            System.out.println("Puerto no especificado. Usando puerto por defecto: 9999");
            portCliente = "9999";
        }

        // Configuración del servidor
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
            // Solicitar nombre del jugador
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

            // Crear vista
            IVista vista = new VistaGrafica();
            vista.getControlador().setNombreJugador(nombreJugador.trim());
            System.out.println("✓ Vista creada para " + nombreJugador);

            // Crear cliente y conectar
            Cliente cliente = new Cliente(
                    ipCliente, Integer.parseInt(portCliente),
                    ipServidor, Integer.parseInt(portServidor)
            );
            System.out.println("✓ Cliente creado");

            // Iniciar conexión con el servidor
            cliente.iniciar(vista.getControlador());
            System.out.println("✓ Conexión establecida con el servidor");
            System.out.println("===========================================");

            // Mostrar menú principal
            vista.menu();

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
}
