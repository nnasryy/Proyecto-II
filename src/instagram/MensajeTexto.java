/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package instagram;

public class MensajeTexto extends Mensaje {
    private String contenido;

    public MensajeTexto(String emisor, String receptor, String contenido) {
        super(emisor, receptor);
        this.contenido = contenido;
    }

    @Override
    public String getContenido() {
        return contenido; // Devuelve el texto real
    }

    @Override
    public String toFileString() {
        // Formato: EMISOR|RECEPTOR|TIPO|ESTADO|CONTENIDO
        return emisor + "|" + receptor + "|TEXTO|" + estado + "|" + contenido;
    }
}