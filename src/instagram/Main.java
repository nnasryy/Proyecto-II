/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
package instagram;

import gui.VentanaPrincipal;
import red.Servidor;

public class Main {

    public static void main(String[] args) {
        new Thread(() -> {
            try {
                new Servidor().iniciar();
            } catch (Exception e) {
                System.out.println("Servidor ya activo o puerto ocupado. Modo cliente iniciado.");
            }
        }).start();

        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
        }

        Sistema sistema = new Sistema();
        VentanaPrincipal ventana = new VentanaPrincipal(sistema);
        ventana.setVisible(true);
    }
}
