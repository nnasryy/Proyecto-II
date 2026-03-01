/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package interfaces;

import enums.ModoVista;

public interface ConfiguracionVisual {
    
    // Método para obtener el ancho de la ventana según el modo
    static int getAncho(ModoVista modo) {
        return (modo == ModoVista.MOBILE) ? 390 : 1366;
    }

    // Método para obtener la altura
    static int getAlto(ModoVista modo) {
        return (modo == ModoVista.MOBILE) ? 844 : 768;
    }
    
    // Dimensiones de imagen para FEED 
    static int[] getDimensionesFeed(ModoVista modo, String tipo) {
      //Mis Vistas Cuadrada, Vertical, Horizontal
        if (modo == ModoVista.MOBILE) {
            switch (tipo) {
                case "VERTICAL": return new int[]{1080, 1350};
                case "HORIZONTAL": return new int[]{1080, 566};
                default: return new int[]{1080, 1080}; // Cuadrada
            }
        } else { // DESKTOP
            switch (tipo) {
                case "VERTICAL": return new int[]{600, 750};
                case "HORIZONTAL": return new int[]{600, 400};
                default: return new int[]{600, 600}; // Cuadrada
            }
        }
    }
}
