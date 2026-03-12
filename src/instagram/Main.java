/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
package instagram;

import gui.VentanaPrincipal;
import red.Servidor;

public class Main {
    public static void main(String[] args) {
        // 1. Intentar iniciar el Servidor
        new Thread(() -> {
            try {
                new Servidor().iniciar();
            } catch (Exception e) {
                // Si falla (porque el puerto ya está en uso), simplemente lo ignoramos
                // asumiendo que el servidor ya está corriendo en otra ventana.
                System.out.println("Servidor ya activo o puerto ocupado. Modo cliente iniciado.");
            }
        }).start();

        // 2. Iniciar la Interfaz (Ventana)
        // Pausa breve para dar tiempo al servidor a iniciar
        try { Thread.sleep(500); } catch (InterruptedException e) {}
        
        Sistema sistema = new Sistema();
        VentanaPrincipal ventana = new VentanaPrincipal(sistema);
        ventana.setVisible(true);
    }
}