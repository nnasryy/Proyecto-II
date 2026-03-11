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
    
    public Mensaje() {} // Constructor vacío para lectura

    // --- MÉTODOS ABSTRACTOS ---
    public abstract String getContenido(); // <--- ESTO FALTABA. Soluciona el error en VentanaPrincipal
    public abstract String toFileString(); // Cada hijo guarda diferente

    // --- MÉTODO ESTÁTICO FÁBRICA (Para leer archivos) ---
    public static Mensaje fromFileString(String linea) {
        try {
            // Formato esperado: EMISOR|RECEPTOR|TIPO|ESTADO|CONTENIDO
            // Nota: Tu formato antiguo tenía fecha/hora, si tu archivo antiguo existe, 
            // este método debe adaptarse. Asumiremos el nuevo formato simplificado para el ejemplo.
            String[] datos = linea.split("\\|");
            if (datos.length < 5) return null; // Línea corrupta

            String emisor = datos[0];
            String receptor = datos[1];
            String tipo = datos[2];
            String estado = datos[3];
            String contenido = datos[4];

            Mensaje m;
            if ("STICKER".equals(tipo)) {
                m = new MensajeSticker(emisor, receptor, contenido);
            } else {
                m = new MensajeTexto(emisor, receptor, contenido);
            }
            
            // Restaurar estado (ya que el constructor pone NO_LEIDO por defecto)
            m.setEstado(estado);
            return m;
            
        } catch (Exception e) {
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
}