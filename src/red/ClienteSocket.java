package red;

import java.io.*;
import java.net.Socket;

public class ClienteSocket {
    private static final String HOST  = "localhost";
    private static final int    PUERTO = 5000;

    private Socket socket;
    private ObjectOutputStream salida;
    private Thread hiloEscucha;
    private EventoListener listener;

    public interface EventoListener {
        void onEvento(EventoSocket evento);
    }

    public boolean conectar(EventoListener listener) {
        this.listener = listener;
        try {
            socket = new Socket(HOST, PUERTO);
            salida = new ObjectOutputStream(socket.getOutputStream());
            iniciarEscucha();
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    private void iniciarEscucha() {
        hiloEscucha = new Thread(() -> {
            try {
                ObjectInputStream entrada = new ObjectInputStream(socket.getInputStream());
                Object obj;
                while ((obj = entrada.readObject()) != null) {
                    if (obj instanceof EventoSocket && listener != null) {
                        final EventoSocket ev = (EventoSocket) obj;
                        javax.swing.SwingUtilities.invokeLater(() -> listener.onEvento(ev));
                    }
                }
            } catch (Exception e) {
                System.out.println("Desconectado del servidor.");
            }
        });
        hiloEscucha.setDaemon(true);
        hiloEscucha.start();
    }

    public void enviar(EventoSocket evento) {
        try {
            if (salida != null) {
                salida.writeObject(evento);
                salida.flush();
            }
        } catch (IOException e) {
            System.out.println("Error enviando evento.");
        }
    }

    public void desconectar() {
        try { if (socket != null) socket.close(); } 
        catch (IOException ignored) {}
    }
}
