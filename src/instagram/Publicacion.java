package instagram;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class Publicacion {

    private String autor; 
    private String contenido;
    private LocalDate fecha;
    private LocalTime hora;
    private String hashtags;
    private String menciones;
    private String rutaImagen;
    private String tipoMultimedia;   

 
    public Publicacion(String autor, String contenido, String rutaImagen, String hashtags, String menciones) {
        this.autor = autor;
        this.contenido = contenido;
        this.rutaImagen = rutaImagen;
        this.fecha = LocalDate.now();
        this.hora = LocalTime.now();
        this.tipoMultimedia = "CUADRADA"; // Valor por defecto según tu código
        this.hashtags = hashtags;
        this.menciones = menciones;
    }

    public Publicacion() {
    }

    public String toFileString() {
        // autor|fecha|hora|contenido|hashtags|menciones|ruta|tipo
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("HH:mm:ss");
        return autor + "|"
                + fecha.toString() + "|"
                + hora.format(dtf) + "|"
                + contenido + "|"
                + hashtags + "|"
                + menciones + "|"
                + rutaImagen + "|"
                + tipoMultimedia;
    }

    // --- MÉTODO ESTÁTICO PARA LEER DESDE ARCHIVO ---
    public static Publicacion fromFileString(String linea) {
        try {
            String[] datos = linea.split("\\|");
            if (datos.length < 8) {
                return null; 
            }
            Publicacion p = new Publicacion();
            p.autor = datos[0];
            p.fecha = LocalDate.parse(datos[1]);
            p.hora = LocalTime.parse(datos[2], DateTimeFormatter.ofPattern("HH:mm:ss"));
            p.contenido = datos[3];
            p.hashtags = datos[4];
            p.menciones = datos[5];
            p.rutaImagen = datos[6];
            p.tipoMultimedia = datos[7];

            return p;
        } catch (Exception e) {
            System.out.println("Error al parsear publicación: " + e.getMessage());
            return null;
        }
    }

    // --- GETTERS ---
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

    // --- SETTERS ---
    public void setAutor(String autor) {
        this.autor = autor;
    }

    public void setContenido(String contenido) {
        this.contenido = contenido;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    public void setHora(LocalTime hora) {
        this.hora = hora;
    }

    public void setHashtags(String hashtags) {
        this.hashtags = hashtags;
    }

    public void setMenciones(String menciones) {
        this.menciones = menciones;
    }

    public void setRutaImagen(String rutaImagen) {
        this.rutaImagen = rutaImagen;
    }

    public void setTipoMultimedia(String tipoMultimedia) {
        this.tipoMultimedia = tipoMultimedia;
    }

    public String getHoraFormateada() {
        return this.hora.format(DateTimeFormatter.ofPattern("HH:mm"));
    }
}
