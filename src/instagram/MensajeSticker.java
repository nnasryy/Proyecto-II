/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package instagram;

import java.time.format.DateTimeFormatter;

public class MensajeSticker extends Mensaje {
    private String rutaSticker;

    public MensajeSticker(String emisor, String receptor, String rutaSticker) {
        super(emisor, receptor);
        this.rutaSticker = rutaSticker;
    }

    @Override
    public String getContenido() {
        return rutaSticker; 
    }

    @Override
    public String toFileString() {
        // FORMATO ACTUALIZADO
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("HH:mm:ss");
        return emisor + "|" + receptor + "|STICKER|" + estado + "|" + fecha.toString() + "|" + hora.format(dtf) + "|" + rutaSticker;
    }
}