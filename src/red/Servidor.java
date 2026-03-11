/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package red;
import instagram.*;
import java.io.*;
import java.net.*;
import java.util.ArrayList;

/**
 *
 * @author nasry
 */
public class Servidor {
    private static final int PUERTO = 5000;
    // Aquí usamos tu lista enlazada con NODOS
    private static ListaUsuarios usuariosActivos = new ListaUsuarios(); 
    private static Sistema sistema = new Sistema(); // Tu lógica actual

    public static void main(String[] args) {
        System.out.println("Servidor Instagram iniciado...");
        try (ServerSocket serverSocket = new ServerSocket(PUERTO)) {
            while (true) {
                Socket cliente = serverSocket.accept();
                System.out.println("Cliente conectado: " + cliente.getInetAddress());
                // Creamos un hilo para atender a cada cliente
                new HiloCliente(cliente).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    // Clase interna para manejar clientes
    static class HiloCliente extends Thread {
        private Socket socket;
        private ObjectInputStream entrada;
        private ObjectOutputStream salida;

        public HiloCliente(Socket socket) {
            this.socket = socket;
            try {
                salida = new ObjectOutputStream(socket.getOutputStream());
                entrada = new ObjectInputStream(socket.getInputStream());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void run() {
            try {
                Object objeto;
                while ((objeto = entrada.readObject()) != null) {
                    // Aquí procesamos lo que envía la ventana
                    if (objeto instanceof String) {
                        String comando = (String) objeto;
                        if (comando.startsWith("LOGIN:")) {
                            String[] parts = comando.split(":");
                            boolean ok = sistema.login(parts[1], parts[2]);
                            salida.writeObject(ok);
                            if(ok) usuariosActivos.agregar(sistema.getUsuarioActual()); // Agregar a lista enlazada
                        }
                        // Agregar más comandos: REGISTRO, BUSCAR, etc.
                    } else if (objeto instanceof Mensaje) {
                        // POLIMORFISMO: No importa si es Texto o Sticker
                        Mensaje m = (Mensaje) objeto;
                        sistema.guardarMensaje(m); // Necesitarás crear este método en Sistema
                        
                        // AQUÍ IRIA LA LÓGICA "LIVE": 
                        // Buscar si el receptor está conectado en 'usuariosActivos' y enviarle el mensaje.
                    }
                }
            } catch (Exception e) {
                System.out.println("Cliente desconectado.");
            }
        }
    }
}
