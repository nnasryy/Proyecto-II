/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package instagram;

import java.time.format.DateTimeFormatter;

public class MensajeTexto extends Mensaje {
    private String contenido;

    public MensajeTexto(String emisor, String receptor, String contenido) {
        super(emisor, receptor);
        this.contenido = contenido;
    }

    @Override
    public String getContenido() {
        return contenido;
    }

    @Override
    public String toFileString() {

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("HH:mm:ss");
        return emisor + "|" + receptor + "|TEXTO|" + estado + "|" + fecha.toString() + "|" + hora.format(dtf) + "|" + contenido;
    }
}