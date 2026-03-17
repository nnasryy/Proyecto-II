package red;

import java.io.IOException;
import java.net.BindException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Servidor {
    private static final int PUERTO = 5000;
    public static final List<HiloCliente> clientes = 
        Collections.synchronizedList(new ArrayList<>());

    public void iniciar() {
        try (ServerSocket serverSocket = new ServerSocket(PUERTO)) {
            System.out.println("Servidor iniciado en puerto " + PUERTO);
            while (true) {
                Socket socket = serverSocket.accept();
                HiloCliente hilo = new HiloCliente(socket);
                clientes.add(hilo);
                hilo.start();
            }
        } catch (BindException ignored) {
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void broadcast(EventoSocket evento, HiloCliente origen) {
        synchronized (clientes) {
            for (HiloCliente c : clientes) {
                if (c != origen && c.isAlive()) {
                    c.enviarEvento(evento);
                }
            }
        }
    }
}
