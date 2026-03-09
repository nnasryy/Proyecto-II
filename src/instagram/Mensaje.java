/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package instagram;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class Mensaje {
    private String emisor;
    private String receptor;
    private LocalDate fecha;
    private LocalTime hora;
    private String contenido;
    private String tipo;
    private String estado; 

    public Mensaje(String emisor, String receptor, String contenido, String tipo) {
        this.emisor = emisor;
        this.receptor = receptor;
        this.contenido = contenido;
        this.tipo = tipo;
        this.fecha = LocalDate.now();
        this.hora = LocalTime.now();
        this.estado = "NO_LEIDO";
    }

    // Constructor vacío para lectura
    public Mensaje() {}

    // Método para leer desde archivo
    public static Mensaje fromFileString(String linea) {
        try {
            String[] datos = linea.split("\\|");
            if (datos.length < 7) return null;
            
            Mensaje m = new Mensaje();
            m.emisor = datos[0];
            m.receptor = datos[1];
            m.fecha = LocalDate.parse(datos[2]);
            m.hora = LocalTime.parse(datos[3], DateTimeFormatter.ofPattern("HH:mm:ss"));
            m.contenido = datos[4];
            m.tipo = datos[5];
            m.estado = datos[6];
            return m;
        } catch (Exception e) {
            return null;
        }
    }

    public String toFileString() {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("HH:mm:ss");
        return emisor + "|" + receptor + "|" + fecha + "|" + hora.format(dtf) + "|" + contenido + "|" + tipo + "|" + estado;
    }

    // Getters necesarios
    public String getEmisor() { return emisor; }
    public String getReceptor() { return receptor; }
    public String getContenido() { return contenido; }
    public String getTipo() { return tipo; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    
    public String getHoraFormateada() {
        return hora.format(DateTimeFormatter.ofPattern("HH:mm"));
    }
}