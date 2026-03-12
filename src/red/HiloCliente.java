/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package red;

import instagram.Mensaje;
import instagram.Sistema;
import java.io.*;
import java.net.Socket;

public class HiloCliente extends Thread {
    private Socket socket;
    private Sistema sistema;

    // Constructor que recibe el socket y la instancia del sistema
    public HiloCliente(Socket socket, Sistema sistema) {
        this.socket = socket;
        this.sistema = sistema;
    }

    @Override
    public void run() {
        try {
            ObjectOutputStream salida = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream entrada = new ObjectInputStream(socket.getInputStream());

            Object objeto;
            while ((objeto = entrada.readObject()) != null) {
                
                if (objeto instanceof String) {
                    String comando = (String) objeto;
                    if (comando.startsWith("LOGIN:")) {
                        String[] parts = comando.split(":");
                        boolean ok = sistema.login(parts[1], parts[2]);
                        salida.writeObject(ok);
                        salida.flush();
                    }
                } else if (objeto instanceof Mensaje) {
                    // Guardar mensaje usando el sistema
                    sistema.guardarMensaje((Mensaje) objeto);
                }
            }
        } catch (Exception e) {
            // Error típico cuando un cliente se desconecta (no es crítico)
            System.out.println("Cliente desconectado o error de comunicación.");
        } finally {
            try {
                if (socket != null) socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
