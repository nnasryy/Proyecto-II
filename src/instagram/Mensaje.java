/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package instagram;

import enums.EstadoMensaje;
import enums.TipoMensaje;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 *
 * @author nasry
 */
public class Mensaje {

    private String emisor;
    private String receptor;
    private String contenido;
    private LocalDate fecha;
    private LocalTime hora;
    private TipoMensaje tipoMensaje;
    private EstadoMensaje estadoMensaje;

    public Mensaje(String emisor, String receptor, String contenido, LocalDate fecha, LocalTime hora, TipoMensaje tipoMensaje) {
        this.emisor = emisor;
        this.receptor = receptor;
        this.contenido = contenido;
        this.fecha = LocalDate.now();
        this.hora = LocalTime.now();
        this.tipoMensaje = tipoMensaje;
    }

    public String getEmisor() {
        return emisor;
    }

    public String getReceptor() {
        return receptor;
    }

    public String getContenido() {
        return contenido;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public LocalTime getHora() {
        return hora;
    }

    public TipoMensaje getTipoMensaje() {
        return tipoMensaje;
    }

    public EstadoMensaje getEstadoMensaje() {
        return estadoMensaje;
    }


    
    
}
