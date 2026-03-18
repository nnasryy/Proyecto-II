package red;

import instagram.Mensaje;
import java.io.*;
import java.net.Socket;

public class HiloCliente extends Thread {
    private Socket socket;
    private ObjectOutputStream salida;

    public HiloCliente(Socket socket) {
        this.socket = socket;
    }

    public void enviarEvento(EventoSocket evento) {
        try {
            if (salida != null) {
                salida.writeObject(evento);
                salida.flush();
            }
        } catch (IOException ignored) {}
    }

    @Override
    public void run() {
        try {
            salida = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream entrada = new ObjectInputStream(socket.getInputStream());
            Object objeto;
            while ((objeto = entrada.readObject()) != null) {
                if (objeto instanceof EventoSocket) {
                    Servidor.broadcast((EventoSocket) objeto, this);
                }
            }
        } catch (Exception e) {
            System.out.println("Cliente desconectado.");
        } finally {
            Servidor.clientes.remove(this);
            try { if (socket != null) socket.close(); } 
            catch (IOException ignored) {}
        }
    }
}