/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package instagram;

public class MensajeSticker extends Mensaje {
    private String rutaSticker;

    public MensajeSticker(String emisor, String receptor, String rutaSticker) {
        super(emisor, receptor);
        this.rutaSticker = rutaSticker;
    }

    @Override
    public String getContenido() {
        // Como la interfaz espera texto, devolvemos un placeholder o la ruta
        return rutaSticker; 
    }

    @Override
    public String toFileString() {
        return emisor + "|" + receptor + "|STICKER|" + estado + "|" + rutaSticker;
    }
}