package instagram;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

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
        this.tipoMultimedia = "CUADRADA"; 
        this.hashtags = hashtags;
        this.menciones = menciones;
    }

    public Publicacion() {
    }

    public String toFileString() {
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


      
    public static Publicacion fromFileString(String linea) {
    try {
        String[] datos = linea.split("\\|", 8);
        if (datos.length < 7) return null;
        Publicacion p = new Publicacion();
        p.autor          = datos[0].trim();
        p.fecha          = LocalDate.parse(datos[1].trim());
        p.hora           = LocalTime.parse(datos[2].trim(), DateTimeFormatter.ofPattern("HH:mm:ss"));
        p.contenido      = datos[3].trim();
        p.hashtags       = datos[4].trim();
        p.menciones      = datos[5].trim();
        p.rutaImagen     = datos[6].trim();
        p.tipoMultimedia = datos.length >= 8 ? datos[7].trim() : "CUADRADA";
        return p;
    } catch (Exception e) {
        System.out.println("Error al parsear publicación: " + e.getMessage());
        return null;
    }
}
    public ArrayList<String> extraerHashtags() {
    ArrayList<String> lista = new ArrayList<>();
    String[] palabras = contenido.split(" ");
    for (String p : palabras) {
        if (p.startsWith("#")) lista.add(p);
    }
    return lista;
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
