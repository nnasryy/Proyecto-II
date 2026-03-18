package instagram;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class PublicacionFoto extends Publicacion {

    private int anchoPixeles;
    private int altoPixeles;

    public PublicacionFoto(String autor, String contenido, String rutaImagen,
                           String hashtags, String menciones,
                           int anchoPixeles, int altoPixeles) {
        super(autor, contenido, rutaImagen, hashtags, menciones);  // HERENCIA: llama al padre
        this.anchoPixeles = anchoPixeles;
        this.altoPixeles  = altoPixeles;

    
        if (anchoPixeles > 0 && altoPixeles > 0) {
            double ratio = (double) altoPixeles / anchoPixeles;
            if (ratio > 1.1)      setTipoMultimedia("VERTICAL");
            else if (ratio < 0.9) setTipoMultimedia("HORIZONTAL");
            else                  setTipoMultimedia("CUADRADA");
        }
    }

    public PublicacionFoto(String autor, String contenido, String rutaImagen,
                           String hashtags, String menciones) {
        super(autor, contenido, rutaImagen, hashtags, menciones);
        this.anchoPixeles = 0;
        this.altoPixeles  = 0;
    }


    @Override
    public String toFileString() {
    
        return super.toFileString();  
    }

    public String getDescripcionTipo() {
        if (anchoPixeles == 0) return "Foto";
        return "Foto " + getTipoMultimedia().toLowerCase()
               + " (" + anchoPixeles + "×" + altoPixeles + ")";
    }


    public static PublicacionFoto desdeArchivo(String linea) {
        Publicacion base = Publicacion.fromFileString(linea);
        if (base == null) return null;

        PublicacionFoto pf = new PublicacionFoto(
            base.getAutor(),
            base.getContenido(),
            base.getRutaImagen(),
            base.getHashtags(),
            base.getMenciones()
        );
        pf.setFecha(base.getFecha());
        pf.setHora(base.getHora());
        pf.setTipoMultimedia(base.getTipoMultimedia());
        return pf;
    }

    public int getAnchoPixeles() { return anchoPixeles; }
    public int getAltoPixeles()  { return altoPixeles;  }
}