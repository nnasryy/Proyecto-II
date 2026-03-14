package instagram;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public abstract class Mensaje implements Serializable {
    protected String emisor;
    protected String receptor;
    protected LocalDate fecha;
    protected LocalTime hora;
    protected String estado; 

    public Mensaje(String emisor, String receptor) {
        this.emisor = emisor;
        this.receptor = receptor;
        this.fecha = LocalDate.now();
        this.hora = LocalTime.now();
        this.estado = "NO_LEIDO";
    }
    
    public Mensaje() {} 

    // --- MÉTODOS ABSTRACTOS ---
    public abstract String getContenido(); 
    public abstract String toFileString(); 

    // --- MÉTODO ESTÁTICO FÁBRICA (ACTUALIZADO) ---
    public static Mensaje fromFileString(String linea) {
        try {
            // NUEVO FORMATO: EMISOR|RECEPTOR|TIPO|ESTADO|FECHA|HORA|CONTENIDO
            String[] datos = linea.split("\\|");
            if (datos.length < 7) return null; 

            String emisor = datos[0];
            String receptor = datos[1];
            String tipo = datos[2];
            String estado = datos[3];
            LocalDate fecha = LocalDate.parse(datos[4]);
            LocalTime hora = LocalTime.parse(datos[5]);
            String contenido = datos[6];

            Mensaje m;
            if ("STICKER".equals(tipo)) {
                m = new MensajeSticker(emisor, receptor, contenido);
            } else {
                m = new MensajeTexto(emisor, receptor, contenido);
            }
            
            // Restaurar valores leídos del archivo
            m.fecha = fecha;
            m.hora = hora;
            m.setEstado(estado);
            return m;
            
        } catch (Exception e) {
            System.out.println("Error leyendo mensaje: " + e.getMessage());
            return null;
        }
    }

    // Getters comunes
    public String getEmisor() { return emisor; }
    public String getReceptor() { return receptor; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    
    public String getHoraFormateada() {
        return hora.format(DateTimeFormatter.ofPattern("HH:mm"));
    }
    
    public LocalDate getFecha() { return fecha; }
    public LocalTime getHora() { return hora; }
}