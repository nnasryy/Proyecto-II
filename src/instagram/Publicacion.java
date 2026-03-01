/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package instagram;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class Publicacion {
    private String autor; // Username
    private String contenido;
    private LocalDate fecha;
    private LocalTime hora;            
    private String hashtags;         
    private String menciones;          
    private String rutaImagen;          
    private String tipoMultimedia;      //CUADRAD, VERTICAL, HORIZONTAL

    // Constructor y Getters/Setters
    public Publicacion(String autor, String contenido, String rutaImagen, String hashtags, String menciones) {
        this.autor = autor;
        this.contenido = contenido;
        this.rutaImagen = rutaImagen;
        this.fecha = LocalDate.now();
        this.hora = LocalTime.now();
        this.tipoMultimedia = "CUADRADA"; //Solo por mock
    }
    // Método para convertir la publicación a texto para guardar en archivo
    public String toFileString() {
        // Formato: autor|fecha|hora|contenido|hashtags|menciones|ruta|tipo
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("HH:mm:ss");
        return autor + "|" + 
               fecha.toString() + "|" + 
               hora.format(dtf) + "|" + 
               contenido + "|" + 
               hashtags + "|" + 
               menciones + "|" + 
               rutaImagen + "|" + 
               tipoMultimedia;
    }

    public String getAutor() {
        return autor;
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

    public String getHashtags() {
        return hashtags;
    }

    public String getMenciones() {
        return menciones;
    }

    public String getRutaImagen() {
        return rutaImagen;
    }

    public String getTipoMultimedia() {
        return tipoMultimedia;
    }
  
    
    
    
}