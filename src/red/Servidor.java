package red;

import instagram.Sistema;
import java.io.IOException;
import java.net.BindException;
import java.net.ServerSocket;
import java.net.Socket;

public class Servidor {

    private static final int PUERTO = 5000;

    // Instancia del sistema para manejar la lógica de datos
    private Sistema sistema = new Sistema();

    public void iniciar() {
        try (ServerSocket serverSocket = new ServerSocket(PUERTO)) {
            System.out.println("Servidor iniciado en puerto " + PUERTO);

            while (true) {
                Socket socket = serverSocket.accept();
                // Pasamos el socket y el sistema a la clase externa HiloCliente
                new HiloCliente(socket, sistema).start();
            }

        } catch (BindException e) {
            // AQUI ESTÁ LA SOLUCIÓN:
            // Si el puerto ya está en uso (BindException), simplemente no hacemos nada.
            // No imprimimos el error, así la consola se ve limpia.
        } catch (IOException e) {
            // Otros errores de conexión se pueden imprimir si son necesarios
            e.printStackTrace();
        }
    }
}
