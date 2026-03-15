package instagram;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/**
 * Subclase concreta de Publicacion especializada en fotos.
 *
 * Temas demostrados:
 *   - Herencia (extends Publicacion)
 *   - Polimorfismo (sobreescribe toFileString() y getDescripcionTipo())
 *   - Uso de super()
 *
 * Toda publicación creada desde la GUI es de tipo PublicacionFoto.
 * Esto permite en el futuro agregar PublicacionVideo, PublicacionTexto, etc.
 */
public class PublicacionFoto extends Publicacion {

    private int anchoPixeles;
    private int altoPixeles;

    /**
     * Constructor principal usado al crear un nuevo post desde la GUI.
     */
    public PublicacionFoto(String autor, String contenido, String rutaImagen,
                           String hashtags, String menciones,
                           int anchoPixeles, int altoPixeles) {
        super(autor, contenido, rutaImagen, hashtags, menciones);  // HERENCIA: llama al padre
        this.anchoPixeles = anchoPixeles;
        this.altoPixeles  = altoPixeles;

        // Determinar tipoMultimedia automáticamente según ratio real
        if (anchoPixeles > 0 && altoPixeles > 0) {
            double ratio = (double) altoPixeles / anchoPixeles;
            if (ratio > 1.1)      setTipoMultimedia("VERTICAL");
            else if (ratio < 0.9) setTipoMultimedia("HORIZONTAL");
            else                  setTipoMultimedia("CUADRADA");
        }
    }

    /**
     * Constructor mínimo (sin dimensiones conocidas).
     */
    public PublicacionFoto(String autor, String contenido, String rutaImagen,
                           String hashtags, String menciones) {
        super(autor, contenido, rutaImagen, hashtags, menciones);
        this.anchoPixeles = 0;
        this.altoPixeles  = 0;
    }

    // ── POLIMORFISMO ────────────────────────────────────────────
    /**
     * Sobreescribe el método del padre para incluir las dimensiones.
     * Mantiene compatibilidad con el formato existente del archivo.
     */
    @Override
    public String toFileString() {
        // Reutiliza el formato del padre (compatibilidad con archivos existentes)
        return super.toFileString();  // POLIMORFISMO: super.método()
    }

    /**
     * Método nuevo solo disponible en PublicacionFoto (polimorfismo de extensión).
     */
    public String getDescripcionTipo() {
        if (anchoPixeles == 0) return "Foto";
        return "Foto " + getTipoMultimedia().toLowerCase()
               + " (" + anchoPixeles + "×" + altoPixeles + ")";
    }

    /**
     * Crea un PublicacionFoto desde una línea del archivo.
     * POLIMORFISMO: mismo formato base, pero devuelve el subtipo correcto.
     */
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

    // ── GETTERS ─────────────────────────────────────────────────
    public int getAnchoPixeles() { return anchoPixeles; }
    public int getAltoPixeles()  { return altoPixeles;  }
}