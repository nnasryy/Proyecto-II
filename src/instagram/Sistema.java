package instagram;

import enums.EstadoCuenta;
import enums.TipoCuenta;
import interfaces.Interaccion;
import interfaces.Mensajeria;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Scanner;

/**
 * Clase principal del sistema Instagram.
 *
 * Temas implementados:
 *   - Interfaces       : implementa Interaccion y Mensajeria (tus interfaces originales)
 *   - Lista enlazada   : caché de usuarios en memoria (ListaUsuarios)
 *   - Nodos            : NodoUsuario como bloque de la lista enlazada
 *   - Recursividad     : búsqueda recursiva en ListaUsuarios
 *   - Herencia         : PublicacionFoto extends Publicacion,
 *                        MensajeTexto/MensajeSticker extends Mensaje
 *   - Polimorfismo     : toFileString() sobreescrito en cada subclase
 *   - Archivos texto   : lectura/escritura de archivos .ins
 *   - Archivos binarios: backup serializado de publicaciones en .bin
 *   - Try-catch        : manejo de excepciones en todas las operaciones I/O
 */
public class Sistema implements Interaccion, Mensajeria {

    private final String RUTA_RAIZ  = "INSTA_RAIZ";
    private final String RUTA_USERS = RUTA_RAIZ + "/users.ins";

    private Usuario usuarioActual;

    // ── LISTA ENLAZADA de usuarios (NodoUsuario) en memoria ─────
    // Evita releer users.ins en cada búsqueda.
    // Se invalida al registrar o modificar usuarios.
    private ListaUsuarios cacheUsuarios = new ListaUsuarios();
    private boolean cacheValida = false;

    public Sistema() {
        verificarEstructura();
    }

    public Usuario getUsuarioActual() { return usuarioActual; }

    // ══════════════════════════════════════════════════════════════
    //  LISTA ENLAZADA — caché de usuarios en memoria
    // ══════════════════════════════════════════════════════════════

    /**
     * Carga todos los usuarios desde users.ins a la ListaUsuarios (lista enlazada).
     * Solo se ejecuta si la caché no es válida.
     * Usa try-catch para manejo de excepciones de I/O.
     */
    private void cargarCacheUsuarios() {
        if (cacheValida) return;
        cacheUsuarios.limpiar();
        try (Scanner sc = new Scanner(new File(RUTA_USERS))) {
            while (sc.hasNextLine()) {
                String linea = sc.nextLine().trim();
                if (linea.isEmpty()) continue;
                String[] d = linea.split("\\|");
                if (d.length < 9) continue;
                try {
                    Usuario u = new Usuario(d[0], d[1], d[2], d[3].charAt(0),
                            Integer.parseInt(d[4]), d[5], LocalDate.parse(d[6]),
                            TipoCuenta.valueOf(d[7]), EstadoCuenta.valueOf(d[8]));
                    cacheUsuarios.agregar(u);   // NodoUsuario → ListaUsuarios
                } catch (Exception ignorado) {}
            }
            cacheValida = true;
        } catch (FileNotFoundException e) {
            System.out.println("users.ins no encontrado al cargar caché.");
        }
    }

    /** Invalida la caché para que se recargue en la próxima operación */
    private void invalidarCache() {
        cacheValida = false;
    }

    // ══════════════════════════════════════════════════════════════
    //  IMPLEMENTACIÓN DE Interaccion
    // ══════════════════════════════════════════════════════════════

    /**
     * Implementa Interaccion.buscar()
     * Usa la lista enlazada (ListaUsuarios) con búsqueda recursiva.
     */
    @Override
    public ArrayList buscar(String criterio) {
        return buscarUsuarios(criterio);
    }

    /**
     * Implementa Interaccion.existe()
     * Usa contieneRecursivo() de ListaUsuarios.
     */
    @Override
    public boolean existe(String username) {
        return existeUsername(username);
    }

    // ══════════════════════════════════════════════════════════════
    //  IMPLEMENTACIÓN DE Mensajeria
    // ══════════════════════════════════════════════════════════════

    /**
     * Implementa Mensajeria.enviarMensaje(Mensaje)
     * Guarda el mensaje en el inbox del receptor.
     */
    @Override
    public boolean enviarMensaje(Mensaje m) {
        return guardarMensaje(m);
    }

    /**
     * Implementa Mensajeria.notificar()
     * Guarda la notificación en el archivo del usuario destino.
     */
    @Override
    public void notificar(String usernameDestino, String mensaje) {
        guardarNotificacion(usernameDestino, mensaje);
    }

    /**
     * Implementa Mensajeria.getTotalNotificacionesPendientes()
     * Retorna cuántas notificaciones tiene el usuario actual.
     */
    @Override
    public int getTotalNotificacionesPendientes() {
        if (usuarioActual == null) return 0;
        return getNotificacionesGenerales().size();
    }

    // ══════════════════════════════════════════════════════════════
    //  ESTRUCTURA BASE
    // ══════════════════════════════════════════════════════════════
    private void verificarEstructura() {
        new File(RUTA_RAIZ).mkdirs();
        new File(RUTA_RAIZ + "/stickers_globales").mkdirs();
        crearStickersPorDefecto();

        File users = new File(RUTA_USERS);
        try { if (!users.exists()) users.createNewFile(); }
        catch (IOException e) { e.printStackTrace(); }
    }

    private void crearStickersPorDefecto() {
        String carpeta = RUTA_RAIZ + "/stickers_globales";
        new File(carpeta).mkdirs();

        // nombre_destino → nombre_en_/images
        String[][] stickers = {
            {"feliz.png",    "happy.png"},
            {"triste.png",   "crying.png"},
            {"corazon.png",  "hearteyes.png"},
            {"risa.png",     "tearsofjoy.png"},
            {"aplauso.png",  "thumbup.png"}
        };

        for (String[] par : stickers) {
            File dest = new File(carpeta + "/" + par[0]);
            if (!dest.exists()) {
                try (InputStream is = getClass().getResourceAsStream("/images/" + par[1])) {
                    if (is != null)
                        java.nio.file.Files.copy(is, dest.toPath(),
                            java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                    else
                        System.err.println("Recurso no encontrado: /images/" + par[1]);
                } catch (IOException e) { e.printStackTrace(); }
            }
        }
    }

    // ══════════════════════════════════════════════════════════════
    //  REGISTRO Y LOGIN
    // ══════════════════════════════════════════════════════════════
    public boolean registrarUsuario(String username, String password, String nombreCompleto,
                                    char genero, int edad, String fotoPerfil, TipoCuenta tipoCuenta) {
        if (existeUsername(username)) return false;

        Usuario nuevo = new Usuario(username, password, nombreCompleto, genero, edad,
                fotoPerfil, LocalDate.now(), tipoCuenta, EstadoCuenta.ACTIVO);

        try (FileWriter fw = new FileWriter(RUTA_USERS, true)) {
            fw.write(nuevo.getUsername() + "|"
                    + nuevo.getPassword() + "|"
                    + nuevo.getNombreCompleto() + "|"
                    + nuevo.getGenero() + "|"
                    + nuevo.getEdad() + "|"
                    + nuevo.getFotoPerfil() + "|"
                    + nuevo.getFechaRegistro().format(DateTimeFormatter.ISO_LOCAL_DATE) + "|"
                    + nuevo.getTipoCuenta().name() + "|"
                    + nuevo.getEstadoCuenta().name() + "\n");
            crearEstructuraUsuario(username);
            invalidarCache(); // lista enlazada en memoria desactualizada
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean login(String username, String password) {
        if (sesionActiva(username)) return false;

        try (Scanner sc = new Scanner(new File(RUTA_USERS))) {
            while (sc.hasNextLine()) {
                String linea = sc.nextLine().trim();
                if (linea.isEmpty()) continue;
                String[] d = linea.split("\\|");
                if (d.length < 9) continue;

                if (d[0].equals(username) && d[1].equals(password)) {
                    if (EstadoCuenta.valueOf(d[8]) == EstadoCuenta.DESACTIVADO) return false;

                    usuarioActual = new Usuario(username, password, d[2], d[3].charAt(0),
                            Integer.parseInt(d[4]), d[5], LocalDate.parse(d[6]),
                            TipoCuenta.valueOf(d[7]), EstadoCuenta.valueOf(d[8]));
                    crearBloqueoSesion();
                    return true;
                }
            }
        } catch (FileNotFoundException e) {
            System.out.println("Archivo users.ins no encontrado.");
        }
        return false;
    }

    public void logout() {
        eliminarBloqueoSesion();
        usuarioActual = null;
    }

    // ══════════════════════════════════════════════════════════════
    //  ESTRUCTURA DE ARCHIVOS POR USUARIO
    //  FIX: Separar correctamente los archivos (el bug era "comments.ins, notifications.in")
    // ══════════════════════════════════════════════════════════════
    private void crearEstructuraUsuario(String username) {
        String base = RUTA_RAIZ + "/" + username;
        new File(base).mkdir();

        // FIX CRÍTICO: antes era un solo string con coma → "comments.ins, notifications.in"
        // Ahora cada archivo está en su propia entrada del array
        String[] archivos = {
            "followers.ins",
            "following.ins",
            "insta.ins",
            "inbox.ins",
            "stickers.ins",
            "solicitudes.ins",
            "likes.ins",
            "comments.ins",
            "notifications.ins"   // ← corregido (faltaba la 's' y estaba pegado al anterior)
        };

        for (String archivo : archivos) {
            File f = new File(base + "/" + archivo);
            try { if (!f.exists()) f.createNewFile(); }
            catch (IOException e) { e.printStackTrace(); }
        }

        new File(base + "/imagenes").mkdir();
        new File(base + "/stickers_personales").mkdir();
        new File(base + "/folders_personales").mkdir();
    }

    // ══════════════════════════════════════════════════════════════
    //  BÚSQUEDA / VERIFICACIÓN — usa ListaUsuarios (NodoUsuario)
    // ══════════════════════════════════════════════════════════════

    /**
     * Verifica si existe un username.
     * Usa contieneRecursivo() de ListaUsuarios.
     */
    public boolean existeUsername(String username) {
        cargarCacheUsuarios();
        return cacheUsuarios.contiene(username); // RECURSIVIDAD interna
    }

    /**
     * Busca un usuario por username exacto.
     * Usa buscarRecursivo() — el método original del alumno, conservado.
     */
    public Usuario buscarUsuario(String username) {
        cargarCacheUsuarios();
        return cacheUsuarios.buscarRecursivo(username); // RECURSIVIDAD
    }

    /**
     * Busca usuarios por coincidencia parcial.
     * Usa buscarPorCriterio() de ListaUsuarios (recursivo).
     */
    public ArrayList<Usuario> buscarUsuarios(String criterio) {
        if (criterio == null || criterio.isEmpty()) return new ArrayList<>();
        cargarCacheUsuarios();
        String excluir = usuarioActual != null ? usuarioActual.getUsername() : null;
        return cacheUsuarios.buscarPorCriterio(criterio, excluir); // RECURSIVIDAD
    }

    // ══════════════════════════════════════════════════════════════
    //  PUBLICACIONES — usa PublicacionFoto (herencia + polimorfismo)
    // ══════════════════════════════════════════════════════════════
    public boolean crearPublicacion(String contenido, String rutaImagen,
                                    String hashtags, String menciones) {
        if (usuarioActual == null) return false;
        if (contenido.length() > 220) return false;

        // HERENCIA + POLIMORFISMO: usamos PublicacionFoto en lugar de Publicacion base
        PublicacionFoto nueva = new PublicacionFoto(
                usuarioActual.getUsername(), contenido, rutaImagen, hashtags, menciones);

        String ruta = RUTA_RAIZ + "/" + usuarioActual.getUsername() + "/insta.ins";
        try (FileWriter fw = new FileWriter(ruta, true)) {
            // toFileString() llama al método sobreescrito (polimorfismo)
            fw.write(nueva.toFileString() + "\n");

            // Notificar menciones usando Mensajeria.notificar()
            if (menciones != null && !menciones.isEmpty()) {
                for (String m : menciones.split(" ")) {
                    if (m.startsWith("@")) {
                        String mencionado = m.substring(1);
                        if (existeUsername(mencionado))
                            notificar(mencionado,    // ← Mensajeria.notificar()
                                "MENCION|" + usuarioActual.getUsername()
                                + "|" + contenido + "|" + LocalDate.now());
                    }
                }
            }

            // ARCHIVO BINARIO: guardar backup serializado del post
            guardarPublicacionBinario(nueva);

            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean eliminarPublicacion(Publicacion p) {
        if (usuarioActual == null || !p.getAutor().equals(usuarioActual.getUsername()))
            return false;

        String rutaInsta = RUTA_RAIZ + "/" + usuarioActual.getUsername() + "/insta.ins";
        File archivo = new File(rutaInsta);
        ArrayList<String> restantes = new ArrayList<>();
        boolean encontrado = false;

        try (Scanner sc = new Scanner(archivo)) {
            while (sc.hasNextLine()) {
                String linea = sc.nextLine();
                Publicacion temp = Publicacion.fromFileString(linea);
                if (temp != null
                        && temp.getAutor().equals(p.getAutor())
                        && temp.getFecha().equals(p.getFecha())
                        && temp.getHora().equals(p.getHora())) {
                    encontrado = true;
                } else {
                    restantes.add(linea);
                }
            }
        } catch (Exception e) { e.printStackTrace(); return false; }

        if (encontrado) {
            try (FileWriter fw = new FileWriter(rutaInsta)) {
                for (String l : restantes) fw.write(l + "\n");
            } catch (IOException e) { e.printStackTrace(); return false; }

            if (p.getRutaImagen() != null && !p.getRutaImagen().isEmpty())
                new File(p.getRutaImagen()).delete();
            return true;
        }
        return false;
    }

    // ── TIMELINE ────────────────────────────────────────────────
    public ArrayList<Publicacion> getTimeline() {
        ArrayList<Publicacion> timeline = new ArrayList<>();
        if (usuarioActual == null) return timeline;

        // Mis propios posts
        leerPublicacionesUsuario(usuarioActual.getUsername(), timeline);

        // Posts de cuentas que sigo
        File fFollowing = new File(RUTA_RAIZ + "/" + usuarioActual.getUsername() + "/following.ins");
        if (fFollowing.exists()) {
            try (Scanner sc = new Scanner(fFollowing)) {
                while (sc.hasNextLine()) {
                    String u = sc.nextLine().trim();
                    if (!u.isEmpty()) leerPublicacionesUsuario(u, timeline);
                }
            } catch (Exception e) { e.printStackTrace(); }
        }

        timeline.sort(Comparator.comparing(Publicacion::getFecha)
                .thenComparing(Publicacion::getHora).reversed());
        return timeline;
    }

    private void leerPublicacionesUsuario(String username, ArrayList<Publicacion> lista) {
        File archivo = new File(RUTA_RAIZ + "/" + username + "/insta.ins");
        if (!archivo.exists()) return;
        try (Scanner sc = new Scanner(archivo)) {
            while (sc.hasNextLine()) {
                String linea = sc.nextLine();
                if (!linea.trim().isEmpty()) {
                    Publicacion p = Publicacion.fromFileString(linea);
                    if (p != null) lista.add(p);
                }
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    public ArrayList<Publicacion> getPublicacionesDeUsuario(String username) {
        ArrayList<Publicacion> lista = new ArrayList<>();
        Usuario u = buscarUsuario(username);
        if (u == null || u.getEstadoCuenta() == EstadoCuenta.DESACTIVADO) return lista;

        // Verificar privacidad
        if (u.getTipoCuenta() == TipoCuenta.PRIVADA) {
            if (usuarioActual == null || !usuarioActual.getUsername().equals(username)) {
                String rutaF = RUTA_RAIZ + "/" + username + "/followers.ins";
                if (usuarioActual == null || !verificarEnArchivo(rutaF, usuarioActual.getUsername()))
                    return lista;
            }
        }

        File archivo = new File(RUTA_RAIZ + "/" + username + "/insta.ins");
        if (archivo.exists()) {
            try (Scanner sc = new Scanner(archivo)) {
                while (sc.hasNextLine()) {
                    String linea = sc.nextLine();
                    if (!linea.trim().isEmpty()) {
                        Publicacion p = Publicacion.fromFileString(linea);
                        if (p != null) lista.add(p);
                    }
                }
            } catch (Exception e) { e.printStackTrace(); }
        }

        lista.sort(Comparator.comparing(Publicacion::getFecha)
                .thenComparing(Publicacion::getHora).reversed());
        return lista;
    }

    // ── ESTADÍSTICAS ────────────────────────────────────────────
    public int getCantidadPosts(String username) {
        return contarLineas(RUTA_RAIZ + "/" + username + "/insta.ins");
    }
    public int getCantidadFollowers(String username) {
        return contarLineas(RUTA_RAIZ + "/" + username + "/followers.ins");
    }
    public int getCantidadFollowing(String username) {
        return contarLineas(RUTA_RAIZ + "/" + username + "/following.ins");
    }

    // ══════════════════════════════════════════════════════════════
    //  FOLLOWS Y SOLICITUDES
    // ══════════════════════════════════════════════════════════════
    public boolean yaLoSigo(String objetivo) {
        if (usuarioActual == null) return false;
        return verificarEnArchivo(RUTA_RAIZ + "/" + usuarioActual.getUsername() + "/following.ins", objetivo);
    }

    public boolean solicitudPendiente(String objetivo) {
        if (usuarioActual == null) return false;
        return verificarEnArchivo(RUTA_RAIZ + "/" + objetivo + "/solicitudes.ins", usuarioActual.getUsername());
    }

    /** Implementa Interaccion.seguirUsuario() */
    @Override
    public boolean seguirUsuario(String objetivo) {
        if (usuarioActual == null || objetivo.equals(usuarioActual.getUsername())) return false;
        Usuario u = buscarUsuario(objetivo);
        if (u == null) return false;

        if (u.getTipoCuenta() == TipoCuenta.PUBLICA) {
            String myFollowing   = RUTA_RAIZ + "/" + usuarioActual.getUsername() + "/following.ins";
            String theirFollowers = RUTA_RAIZ + "/" + objetivo + "/followers.ins";
            if (verificarEnArchivo(myFollowing, objetivo)) return false;

            appendLine(myFollowing, objetivo);
            appendLine(theirFollowers, usuarioActual.getUsername());
            // Usa Mensajeria.notificar() en lugar de llamar guardarNotificacion directamente
            notificar(objetivo, "SEGUIDOR|" + usuarioActual.getUsername() + "|" + LocalDate.now());
            return true;
        } else {
            return enviarSolicitud(objetivo);
        }
    }

    /** Implementa Interaccion.dejarDeSeguir() */
    @Override
    public boolean dejarDeSeguir(String objetivo) {
        if (usuarioActual == null) return false;
        eliminarLineaDeArchivo(RUTA_RAIZ + "/" + usuarioActual.getUsername() + "/following.ins", objetivo);
        eliminarLineaDeArchivo(RUTA_RAIZ + "/" + objetivo + "/followers.ins", usuarioActual.getUsername());
        return true;
    }

    public boolean eliminarSeguidor(String seguidor) {
        if (usuarioActual == null) return false;
        eliminarLineaDeArchivo(RUTA_RAIZ + "/" + usuarioActual.getUsername() + "/followers.ins", seguidor);
        eliminarLineaDeArchivo(RUTA_RAIZ + "/" + seguidor + "/following.ins", usuarioActual.getUsername());
        return true;
    }

    private boolean enviarSolicitud(String objetivo) {
        String ruta = RUTA_RAIZ + "/" + objetivo + "/solicitudes.ins";
        if (verificarEnArchivo(ruta, usuarioActual.getUsername())) return false;
        return appendLine(ruta, usuarioActual.getUsername());
    }

    public void aceptarSolicitud(String solicitante) {
        String rutaSol   = RUTA_RAIZ + "/" + usuarioActual.getUsername() + "/solicitudes.ins";
        String miFollowers = RUTA_RAIZ + "/" + usuarioActual.getUsername() + "/followers.ins";
        String suFollowing = RUTA_RAIZ + "/" + solicitante + "/following.ins";

        appendLine(miFollowers, solicitante);
        appendLine(suFollowing, usuarioActual.getUsername());
        eliminarLineaDeArchivo(rutaSol, solicitante);
    }

    public void rechazarSolicitud(String solicitante) {
        eliminarLineaDeArchivo(
            RUTA_RAIZ + "/" + usuarioActual.getUsername() + "/solicitudes.ins", solicitante);
    }

    public ArrayList<String> getSolicitudes() {
        ArrayList<String> lista = new ArrayList<>();
        if (usuarioActual == null) return lista;
        leerLineas(RUTA_RAIZ + "/" + usuarioActual.getUsername() + "/solicitudes.ins", lista);
        return lista;
    }

    public ArrayList<String> getListaFollowers(String username) {
        ArrayList<String> lista = new ArrayList<>();
        leerLineas(RUTA_RAIZ + "/" + username + "/followers.ins", lista);
        return lista;
    }

    public ArrayList<String> getListaFollowing(String username) {
        ArrayList<String> lista = new ArrayList<>();
        leerLineas(RUTA_RAIZ + "/" + username + "/following.ins", lista);
        return lista;
    }

    // ══════════════════════════════════════════════════════════════
    //  LIKES
    //  FIX: Ahora guarda también la ruta de imagen para el preview
    //       Formato: autor|fecha|quienDioLike|rutaImagen
    // ══════════════════════════════════════════════════════════════
    public boolean yaDioLike(String autorPost, String fechaPost) {
        if (usuarioActual == null) return false;
        String ruta = RUTA_RAIZ + "/" + autorPost + "/likes.ins";
        // Buscamos línea que empiece con autor|fecha|miUsername
        String prefijo = autorPost + "|" + fechaPost + "|" + usuarioActual.getUsername();
        File f = new File(ruta);
        if (!f.exists()) return false;
        try (Scanner sc = new Scanner(f)) {
            while (sc.hasNextLine()) {
                String l = sc.nextLine().trim();
                if (l.startsWith(prefijo)) return true;
            }
        } catch (Exception ignored) {}
        return false;
    }

    public boolean toggleLike(String autorPost, String fechaPost) {
        return toggleLike(autorPost, fechaPost, "");
    }

    /**
     * Versión extendida que recibe la ruta de imagen del post
     * para guardarla en la notificación y permitir preview.
     */
    public boolean toggleLike(String autorPost, String fechaPost, String rutaImagen) {
        if (usuarioActual == null) return false;

        String rutaLikes = RUTA_RAIZ + "/" + autorPost + "/likes.ins";
        String prefijo   = autorPost + "|" + fechaPost + "|" + usuarioActual.getUsername();

        // ¿Ya dio like?
        boolean yaLiked = false;
        String lineaExistente = null;
        File f = new File(rutaLikes);
        if (f.exists()) {
            try (Scanner sc = new Scanner(f)) {
                while (sc.hasNextLine()) {
                    String l = sc.nextLine().trim();
                    if (l.startsWith(prefijo)) { yaLiked = true; lineaExistente = l; break; }
                }
            } catch (Exception ignored) {}
        }

        if (yaLiked) {
            // Quitar like
            eliminarLineaDeArchivo(rutaLikes, lineaExistente);
            return false;
        } else {
            // Dar like — formato: autor|fecha|quien|rutaImagen
            String lineaLike = prefijo + "|" + (rutaImagen != null ? rutaImagen : "");
            try (FileWriter fw = new FileWriter(rutaLikes, true)) {
                fw.write(lineaLike + "\n");
            } catch (IOException e) { e.printStackTrace(); }
            return true;
        }
    }

    // ── NOTIFICACIONES DE LIKES ─────────────────────────────────
    /**
     * Cuenta cuántos likes tiene un post específico.
     */
    public int getCantidadLikes(String autorPost, String fechaPost) {
        String ruta = RUTA_RAIZ + "/" + autorPost + "/likes.ins";
        File f = new File(ruta);
        if (!f.exists()) return 0;
        int count = 0;
        String prefijo = autorPost + "|" + fechaPost + "|";
        try (Scanner sc = new Scanner(f)) {
            while (sc.hasNextLine()) {
                if (sc.nextLine().trim().startsWith(prefijo)) count++;
            }
        } catch (Exception ignored) {}
        return count;
    }

    /**
     * Devuelve notificaciones de likes con formato:
     * autor|fecha|quien|rutaImagen  (4 campos, imagen puede estar vacía)
     * Ya vienen del más reciente al más viejo (invertido).
     */
    public ArrayList<String> getNotificacionesLikes() {
        ArrayList<String> notifs = new ArrayList<>();
        if (usuarioActual == null) return notifs;
        leerLineas(RUTA_RAIZ + "/" + usuarioActual.getUsername() + "/likes.ins", notifs);
        // FIX 1: más reciente primero (el último like agregado queda al final del archivo)
        Collections.reverse(notifs);
        return notifs;
    }

    // ══════════════════════════════════════════════════════════════
    //  COMENTARIOS
    // ══════════════════════════════════════════════════════════════
    public void agregarComentario(String autorPost, String fechaPost, String comentario) {
        if (usuarioActual == null) return;
        String ruta = RUTA_RAIZ + "/" + autorPost + "/comments.ins";
        String linea = fechaPost + "|" + usuarioActual.getUsername() + "|" + comentario;
        try (FileWriter fw = new FileWriter(ruta, true)) {
            fw.write(linea + "\n");
        } catch (IOException e) { e.printStackTrace(); }

        // Notificar menciones en comentario
        for (String p : comentario.split(" ")) {
            if (p.startsWith("@")) {
                String mencionado = p.substring(1);
                if (existeUsername(mencionado))
                    guardarNotificacion(mencionado,
                        "MENCION|" + usuarioActual.getUsername()
                        + "|" + comentario + "|" + LocalDate.now());
            }
        }
    }

    public ArrayList<String> getComentarios(String autorPost, String fechaPost) {
        ArrayList<String> lista = new ArrayList<>();
        File f = new File(RUTA_RAIZ + "/" + autorPost + "/comments.ins");
        if (!f.exists()) return lista;
        try (Scanner sc = new Scanner(f)) {
            while (sc.hasNextLine()) {
                String[] d = sc.nextLine().split("\\|");
                if (d.length >= 3 && d[0].equals(fechaPost))
                    lista.add(d[1] + ": " + d[2]);
            }
        } catch (Exception ignored) {}
        return lista;
    }

    // ══════════════════════════════════════════════════════════════
    //  NOTIFICACIONES GENERALES
    //  FIX 1: más reciente primero ya garantizado por Collections.reverse
    // ══════════════════════════════════════════════════════════════
    private void guardarNotificacion(String destino, String mensaje) {
        // FIX: ahora el archivo se llama notifications.ins correctamente
        String ruta = RUTA_RAIZ + "/" + destino + "/notifications.ins";
        // Asegurar que el archivo exista (usuarios anteriores pueden no tenerlo)
        try {
            File f = new File(ruta);
            if (!f.getParentFile().exists()) f.getParentFile().mkdirs();
            if (!f.exists()) f.createNewFile();
        } catch (IOException ignored) {}

        try (FileWriter fw = new FileWriter(ruta, true)) {
            fw.write(mensaje + "\n");
        } catch (IOException e) { e.printStackTrace(); }
    }

    public ArrayList<String> getNotificacionesGenerales() {
        ArrayList<String> lista = new ArrayList<>();
        if (usuarioActual == null) return lista;
        // FIX: nombre correcto del archivo
        leerLineas(RUTA_RAIZ + "/" + usuarioActual.getUsername() + "/notifications.ins", lista);
        // FIX 1: más reciente primero
        Collections.reverse(lista);
        return lista;
    }

    // ══════════════════════════════════════════════════════════════
    //  MENSAJERÍA
    // ══════════════════════════════════════════════════════════════
    public boolean guardarMensaje(Mensaje m) {
        try (FileWriter fw = new FileWriter(RUTA_RAIZ + "/" + m.getReceptor() + "/inbox.ins", true)) {
            fw.write(m.toFileString() + "\n");
            return true;
        } catch (IOException e) { return false; }
    }

    public boolean puedeEnviarMensaje(String receptor) {
        Usuario u = buscarUsuario(receptor);
        if (u == null) return false;
        if (u.getTipoCuenta() == TipoCuenta.PUBLICA) return true;

        boolean yoLoSigo = verificarEnArchivo(
            RUTA_RAIZ + "/" + usuarioActual.getUsername() + "/following.ins", receptor);
        boolean elMeSigue = verificarEnArchivo(
            RUTA_RAIZ + "/" + receptor + "/followers.ins", usuarioActual.getUsername());
        return yoLoSigo && elMeSigue;
    }

    public boolean enviarMensaje(String receptor, String contenido, String tipo) {
        if (usuarioActual == null || contenido.length() > 300) return false;

        LocalDate fecha = LocalDate.now();
        LocalTime hora  = LocalTime.now();

        Mensaje paraReceptor = "STICKER".equals(tipo)
            ? new MensajeSticker(usuarioActual.getUsername(), receptor, contenido)
            : new MensajeTexto(usuarioActual.getUsername(), receptor, contenido);

        Mensaje miCopia = "STICKER".equals(tipo)
            ? new MensajeSticker(usuarioActual.getUsername(), receptor, contenido)
            : new MensajeTexto(usuarioActual.getUsername(), receptor, contenido);

        // Sincronizar fecha/hora en ambos objetos
        paraReceptor.fecha = fecha; paraReceptor.hora = hora; paraReceptor.setEstado("NO_LEIDO");
        miCopia.fecha      = fecha; miCopia.hora      = hora; miCopia.setEstado("NO_LEIDO");

        guardarMensaje(paraReceptor);

        try (FileWriter fw = new FileWriter(RUTA_RAIZ + "/" + usuarioActual.getUsername() + "/inbox.ins", true)) {
            fw.write(miCopia.toFileString() + "\n");
        } catch (IOException e) { e.printStackTrace(); }

        return true;
    }

    public ArrayList<Mensaje> getConversacion(String otro) {
        ArrayList<Mensaje> conv = new ArrayList<>();
        if (usuarioActual == null) return conv;

        File archivo = new File(RUTA_RAIZ + "/" + usuarioActual.getUsername() + "/inbox.ins");
        if (!archivo.exists()) return conv;

        try (Scanner sc = new Scanner(archivo)) {
            while (sc.hasNextLine()) {
                Mensaje m = Mensaje.fromFileString(sc.nextLine());
                if (m != null) {
                    boolean entrante = m.getEmisor().equals(otro)
                            && m.getReceptor().equals(usuarioActual.getUsername());
                    boolean saliente = m.getEmisor().equals(usuarioActual.getUsername())
                            && m.getReceptor().equals(otro);
                    if (entrante || saliente) conv.add(m);
                }
            }
        } catch (Exception e) { e.printStackTrace(); }
        return conv;
    }

    public void marcarComoLeido(String otro) {
        if (usuarioActual == null) return;

        String rutaMio = RUTA_RAIZ + "/" + usuarioActual.getUsername() + "/inbox.ins";
        File archivo = new File(rutaMio);
        ArrayList<String> lineas = new ArrayList<>();

        if (!archivo.exists()) return;

        try (Scanner sc = new Scanner(archivo)) {
            while (sc.hasNextLine()) {
                String linea = sc.nextLine();
                Mensaje m = Mensaje.fromFileString(linea);
                if (m != null && m.getEmisor().equals(otro) && "NO_LEIDO".equals(m.getEstado())) {
                    m.setEstado("LEIDO");
                    lineas.add(m.toFileString());
                    actualizarEstadoEnArchivoAjeno(otro, usuarioActual.getUsername(),
                        m.getFecha(), m.getHora());
                } else {
                    lineas.add(linea);
                }
            }
        } catch (Exception e) { e.printStackTrace(); }

        reescribirArchivo(rutaMio, lineas);
    }

    private void actualizarEstadoEnArchivoAjeno(String otro, String miUsername,
                                                 java.time.LocalDate fecha, java.time.LocalTime hora) {
        String rutaOtro = RUTA_RAIZ + "/" + otro + "/inbox.ins";
        File f = new File(rutaOtro);
        if (!f.exists()) return;

        ArrayList<String> lineas = new ArrayList<>();
        boolean cambio = false;

        try (Scanner sc = new Scanner(f)) {
            while (sc.hasNextLine()) {
                String linea = sc.nextLine();
                Mensaje m = Mensaje.fromFileString(linea);
                if (m != null
                        && m.getEmisor().equals(otro)
                        && m.getReceptor().equals(miUsername)
                        && m.getFecha().equals(fecha)
                        && m.getHora().equals(hora)) {
                    m.setEstado("LEIDO");
                    lineas.add(m.toFileString());
                    cambio = true;
                } else {
                    lineas.add(linea);
                }
            }
        } catch (Exception e) { e.printStackTrace(); }

        if (cambio) reescribirArchivo(rutaOtro, lineas);
    }

    public ArrayList<String> getChatsRecientes() {
        ArrayList<String> usuarios = new ArrayList<>();
        if (usuarioActual == null) return usuarios;

        File archivo = new File(RUTA_RAIZ + "/" + usuarioActual.getUsername() + "/inbox.ins");
        if (!archivo.exists()) return usuarios;

        // Leer todas las líneas primero, luego recorrer al revés
        // así el último mensaje de cada conversación define el orden (más reciente arriba)
        ArrayList<String> todasLineas = new ArrayList<>();
        try (Scanner sc = new Scanner(archivo)) {
            while (sc.hasNextLine()) todasLineas.add(sc.nextLine());
        } catch (Exception e) { e.printStackTrace(); }

        // Recorrer de abajo hacia arriba → primer aparición = más reciente
        for (int i = todasLineas.size() - 1; i >= 0; i--) {
            Mensaje m = Mensaje.fromFileString(todasLineas.get(i));
            if (m != null) {
                String otro = m.getEmisor().equals(usuarioActual.getUsername())
                    ? m.getReceptor() : m.getEmisor();
                if (!usuarios.contains(otro)) usuarios.add(otro);
            }
        }
        return usuarios;
    }

    /**
     * Cuenta mensajes NO_LEIDO recibidos de un usuario específico.
     * Usado para mostrar badge en la lista de chats.
     */
    public int getMensajesNoLeidos(String otroUsuario) {
        if (usuarioActual == null) return 0;
        File archivo = new File(RUTA_RAIZ + "/" + usuarioActual.getUsername() + "/inbox.ins");
        if (!archivo.exists()) return 0;
        int count = 0;
        try (Scanner sc = new Scanner(archivo)) {
            while (sc.hasNextLine()) {
                Mensaje m = Mensaje.fromFileString(sc.nextLine());
                if (m != null
                        && m.getEmisor().equals(otroUsuario)
                        && m.getReceptor().equals(usuarioActual.getUsername())
                        && "NO_LEIDO".equals(m.getEstado())) {
                    count++;
                }
            }
        } catch (Exception ignored) {}
        return count;
    }

    /**
     * Total de mensajes no leídos en TODOS los chats.
     * Usado para el badge del sidebar en "Mensajes".
     */
    public int getTotalMensajesNoLeidos() {
        if (usuarioActual == null) return 0;
        File archivo = new File(RUTA_RAIZ + "/" + usuarioActual.getUsername() + "/inbox.ins");
        if (!archivo.exists()) return 0;
        int count = 0;
        try (Scanner sc = new Scanner(archivo)) {
            while (sc.hasNextLine()) {
                Mensaje m = Mensaje.fromFileString(sc.nextLine());
                if (m != null
                        && m.getReceptor().equals(usuarioActual.getUsername())
                        && "NO_LEIDO".equals(m.getEstado())) {
                    count++;
                }
            }
        } catch (Exception ignored) {}
        return count;
    }

    public void eliminarConversacion(String otro) {
        if (usuarioActual == null) return;
        String ruta = RUTA_RAIZ + "/" + usuarioActual.getUsername() + "/inbox.ins";
        File archivo = new File(ruta);
        if (!archivo.exists()) return;

        ArrayList<String> lineas = new ArrayList<>();
        try (Scanner sc = new Scanner(archivo)) {
            while (sc.hasNextLine()) {
                String linea = sc.nextLine();
                Mensaje m = Mensaje.fromFileString(linea);
                boolean esConversacion = m != null
                    && ((m.getEmisor().equals(otro) && m.getReceptor().equals(usuarioActual.getUsername()))
                     || (m.getEmisor().equals(usuarioActual.getUsername()) && m.getReceptor().equals(otro)));
                if (!esConversacion) lineas.add(linea);
            }
        } catch (Exception ignored) {}
        reescribirArchivo(ruta, lineas);
    }

    public void compartirPost(String destino, String autorPost, String rutaImagen, String contenido) {
        String msg = "SHARE|" + autorPost + "|" + rutaImagen + "|" + contenido;
        enviarMensaje(destino, msg, "TEXTO");
    }

    public boolean puedeCompartirPost(String destino, String autorPost) {
        Usuario autor = buscarUsuario(autorPost);
        Usuario dst   = buscarUsuario(destino);
        if (autor == null || dst == null) return false;
        if (autor.getTipoCuenta() == TipoCuenta.PUBLICA) return true;

        boolean dSigueA = verificarEnArchivo(RUTA_RAIZ + "/" + destino + "/following.ins", autorPost);
        boolean aSigueD = verificarEnArchivo(RUTA_RAIZ + "/" + autorPost + "/following.ins", destino);
        return dSigueA && aSigueD;
    }

    // ══════════════════════════════════════════════════════════════
    //  STICKERS
    // ══════════════════════════════════════════════════════════════
    public ArrayList<String> getStickersGlobales() {
        ArrayList<String> lista = new ArrayList<>();
        File carpeta = new File(RUTA_RAIZ + "/stickers_globales");
        carpeta.mkdirs();
        String[] archivos = carpeta.list((d, n) ->
            n.toLowerCase().endsWith(".png") || n.toLowerCase().endsWith(".jpg"));
        if (archivos != null)
            for (String f : archivos) lista.add(carpeta.getPath() + "/" + f);
        return lista;
    }

    public boolean guardarStickerPersonal(File origen, String username) {
        try {
            String rutaCarpeta = RUTA_RAIZ + "/" + username + "/stickers_personales";
            new File(rutaCarpeta).mkdirs();
            String nombreFinal = "sticker_" + System.currentTimeMillis() + ".png";
            File dest = new File(rutaCarpeta + "/" + nombreFinal);
            java.nio.file.Files.copy(origen.toPath(), dest.toPath(),
                java.nio.file.StandardCopyOption.REPLACE_EXISTING);

            // FALTANTE: registrar en stickers.ins como pide el documento
            String rutaStickersIns = RUTA_RAIZ + "/" + username + "/stickers.ins";
            appendLine(rutaStickersIns, dest.getAbsolutePath());

            return true;
        } catch (IOException e) { e.printStackTrace(); return false; }
    }

    public ArrayList<String> getTodosStickers(String username) {
        ArrayList<String> lista = new ArrayList<>(getStickersGlobales());
        File carpeta = new File(RUTA_RAIZ + "/" + username + "/stickers_personales");
        if (carpeta.exists()) {
            String[] archivos = carpeta.list((d, n) ->
                n.toLowerCase().endsWith(".png") || n.toLowerCase().endsWith(".jpg"));
            if (archivos != null)
                for (String f : archivos) lista.add(carpeta.getPath() + "/" + f);
        }
        return lista;
    }

    // ══════════════════════════════════════════════════════════════
    //  BÚSQUEDA POR HASHTAG — sin duplicados (Sección 14)
    // ══════════════════════════════════════════════════════════════
    public ArrayList<Publicacion> buscarPorHashtag(String hashtag) {
        ArrayList<Publicacion> resultados = new ArrayList<>();
        if (hashtag == null || hashtag.isEmpty()) return resultados;
        if (!hashtag.startsWith("#")) hashtag = "#" + hashtag;

        // ArrayList para rastrear IDs únicos y evitar duplicados
        ArrayList<String> idsAgregados = new ArrayList<>();

        File raiz = new File(RUTA_RAIZ);
        String[] users = raiz.list();
        if (users == null) return resultados;

        for (String u : users) {
            File f = new File(RUTA_RAIZ + "/" + u);
            if (!f.isDirectory() || u.equals("stickers_globales")) continue;

            for (Publicacion p : getPublicacionesDeUsuario(u)) {
                if (p.getContenido() == null || !p.getContenido().contains(hashtag)) continue;

                // Clave única: autor + fecha + hora
                String idPost = p.getAutor() + "|" + p.getFecha() + "|" + p.getHora();
                if (!idsAgregados.contains(idPost)) {
                    idsAgregados.add(idPost);
                    resultados.add(p);
                }
            }
        }
        return resultados;
    }

    // ══════════════════════════════════════════════════════════════
    //  GESTIÓN DE CUENTA
    // ══════════════════════════════════════════════════════════════
    public void cambiarEstadoCuenta(EstadoCuenta nuevoEstado) {
        if (usuarioActual == null) return;
        usuarioActual.setEstadoCuenta(nuevoEstado);
        actualizarCampoUsuario(usuarioActual.getUsername(), 8, nuevoEstado.name());
    }

    public void reactivarCuenta(String username) {
        actualizarCampoUsuario(username, 8, EstadoCuenta.ACTIVO.name());
    }

    public boolean actualizarDatosUsuario(String nuevoNombre, String nuevaPassword) {
        if (usuarioActual == null) return false;

        File archivo = new File(RUTA_USERS);
        ArrayList<String> lineas = new ArrayList<>();

        try (Scanner sc = new Scanner(archivo)) {
            while (sc.hasNextLine()) {
                String linea = sc.nextLine();
                if (linea.trim().isEmpty()) { lineas.add(linea); continue; }
                String[] d = linea.split("\\|");
                if (d[0].equals(usuarioActual.getUsername())) {
                    d[2] = nuevoNombre;
                    if (nuevaPassword != null && !nuevaPassword.isEmpty()) d[1] = nuevaPassword;
                    linea = String.join("|", d);
                    usuarioActual.setNombreCompleto(nuevoNombre);
                }
                lineas.add(linea);
            }
        } catch (Exception e) { e.printStackTrace(); return false; }

        reescribirArchivo(RUTA_USERS, lineas);
        return true;
    }

    public boolean actualizarFotoPerfil(String username, String nuevaRuta) {
        if (usuarioActual != null && usuarioActual.getUsername().equals(username))
            usuarioActual.setFotoPerfil(nuevaRuta);
        return actualizarCampoUsuario(username, 5, nuevaRuta);
    }

    // ══════════════════════════════════════════════════════════════
    //  SESIÓN ÚNICA (LOCK FILE)
    // ══════════════════════════════════════════════════════════════
    public boolean sesionActiva(String username) {
        return new File(RUTA_RAIZ + "/" + username + "/session.lock").exists();
    }

    private void crearBloqueoSesion() {
        if (usuarioActual == null) return;
        try { new File(RUTA_RAIZ + "/" + usuarioActual.getUsername() + "/session.lock").createNewFile(); }
        catch (IOException e) { e.printStackTrace(); }
    }

    private void eliminarBloqueoSesion() {
        if (usuarioActual == null) return;
        new File(RUTA_RAIZ + "/" + usuarioActual.getUsername() + "/session.lock").delete();
    }

    // ══════════════════════════════════════════════════════════════
    //  MENCIONES
    // ══════════════════════════════════════════════════════════════
    public ArrayList<Publicacion> getMenciones() {
        ArrayList<Publicacion> menciones = new ArrayList<>();
        if (usuarioActual == null) return menciones;

        String miUsername = usuarioActual.getUsername();
        File raiz = new File(RUTA_RAIZ);
        String[] carpetas = raiz.list();
        if (carpetas == null) return menciones;

        for (String folder : carpetas) {
            File f = new File(RUTA_RAIZ + "/" + folder);
            if (f.isDirectory() && !folder.equals("stickers_globales")) {
                for (Publicacion p : getPublicacionesDeUsuario(folder)) {
                    if (p.getContenido() != null && p.getContenido().contains("@" + miUsername))
                        menciones.add(p);
                }
            }
        }
        return menciones;
    }

    // ══════════════════════════════════════════════════════════════
    //  PROCESAMIENTO DE IMÁGENES
    // ══════════════════════════════════════════════════════════════
    public String procesarImagenPerfil(File original, String username, String nombreArchivo) {
        try {
            BufferedImage img = javax.imageio.ImageIO.read(original);
            if (img == null) return null;

            int ancho = img.getWidth(), alto = img.getHeight();
            int lado = Math.min(ancho, alto);
            int x = (ancho - lado) / 2, y = (alto - lado) / 2;

            BufferedImage recortada = img.getSubimage(x, y, lado, lado);
            BufferedImage final300 = new BufferedImage(300, 300, BufferedImage.TYPE_INT_RGB);
            Graphics2D g = final300.createGraphics();
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g.drawImage(recortada, 0, 0, 300, 300, null);
            g.dispose();

            File carpeta = new File(RUTA_RAIZ + "/" + username + "/imagenes");
            carpeta.mkdirs();
            File dest = new File(carpeta, nombreArchivo + ".jpg");
            javax.imageio.ImageIO.write(final300, "jpg", dest);
            return dest.getAbsolutePath();
        } catch (Exception e) { e.printStackTrace(); return null; }
    }

    public String procesarYGuardarImagen(File original, String username, String nombreArchivo) {
        try {
            BufferedImage img = javax.imageio.ImageIO.read(original);
            if (img == null) return null;

            int anchoFinal = 600;
            double ratio = (double) img.getHeight() / img.getWidth();
            int altoFinal = Math.min((int)(anchoFinal * ratio), 800);

            BufferedImage final_ = new BufferedImage(anchoFinal, altoFinal, BufferedImage.TYPE_INT_RGB);
            Graphics2D g = final_.createGraphics();
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g.drawImage(img, 0, 0, anchoFinal, altoFinal, null);
            g.dispose();

            String ruta = RUTA_RAIZ + "/" + username + "/imagenes";
            new File(ruta).mkdirs();
            String rutaFinal = ruta + "/" + nombreArchivo + ".jpg";

            // Guardar con calidad alta (0.95) en lugar del default (~0.75)
            javax.imageio.ImageWriter writer = javax.imageio.ImageIO
                .getImageWritersByFormatName("jpg").next();
            javax.imageio.ImageWriteParam param = writer.getDefaultWriteParam();
            param.setCompressionMode(javax.imageio.ImageWriteParam.MODE_EXPLICIT);
            param.setCompressionQuality(0.95f);
            javax.imageio.stream.ImageOutputStream ios =
                javax.imageio.ImageIO.createImageOutputStream(new File(rutaFinal));
            writer.setOutput(ios);
            writer.write(null, new javax.imageio.IIOImage(final_, null, null), param);
            writer.dispose(); ios.close();
            return rutaFinal;
        } catch (Exception e) { e.printStackTrace(); return null; }
    }

    // ══════════════════════════════════════════════════════════════
    //  HELPERS PRIVADOS
    // ══════════════════════════════════════════════════════════════
    private boolean verificarEnArchivo(String ruta, String texto) {
        File f = new File(ruta);
        if (!f.exists()) return false;
        try (Scanner sc = new Scanner(f)) {
            while (sc.hasNextLine())
                if (sc.nextLine().trim().equals(texto)) return true;
        } catch (Exception ignored) {}
        return false;
    }

    private void eliminarLineaDeArchivo(String ruta, String texto) {
        File f = new File(ruta);
        if (!f.exists()) return;
        ArrayList<String> lineas = new ArrayList<>();
        try (Scanner sc = new Scanner(f)) {
            while (sc.hasNextLine()) {
                String l = sc.nextLine();
                // Para likes, texto puede ser prefijo (la línea puede tener más campos)
                if (!l.trim().equals(texto) && !l.trim().startsWith(texto + "|"))
                    lineas.add(l);
            }
        } catch (Exception ignored) {}
        reescribirArchivo(ruta, lineas);
    }

    private boolean appendLine(String ruta, String linea) {
        try (FileWriter fw = new FileWriter(ruta, true)) {
            fw.write(linea + "\n");
            return true;
        } catch (IOException e) { e.printStackTrace(); return false; }
    }

    private void leerLineas(String ruta, ArrayList<String> lista) {
        File f = new File(ruta);
        if (!f.exists()) return;
        try (Scanner sc = new Scanner(f)) {
            while (sc.hasNextLine()) {
                String l = sc.nextLine().trim();
                if (!l.isEmpty()) lista.add(l);
            }
        } catch (Exception ignored) {}
    }

    private int contarLineas(String ruta) {
        File f = new File(ruta);
        if (!f.exists()) return 0;
        int count = 0;
        try (Scanner sc = new Scanner(f)) {
            while (sc.hasNextLine()) { sc.nextLine(); count++; }
        } catch (Exception ignored) {}
        return count;
    }

    private void reescribirArchivo(String ruta, ArrayList<String> lineas) {
        try (FileWriter fw = new FileWriter(ruta)) {
            for (String l : lineas) fw.write(l + "\n");
        } catch (IOException e) { e.printStackTrace(); }
    }

    /** Actualiza un campo específico (por índice) en users.ins para un usuario. */
    private boolean actualizarCampoUsuario(String username, int indice, String valor) {
        File archivo = new File(RUTA_USERS);
        ArrayList<String> lineas = new ArrayList<>();
        try (Scanner sc = new Scanner(archivo)) {
            while (sc.hasNextLine()) {
                String linea = sc.nextLine();
                if (linea.trim().isEmpty()) { lineas.add(linea); continue; }
                String[] d = linea.split("\\|");
                if (d.length > indice && d[0].equals(username)) {
                    d[indice] = valor;
                    linea = String.join("|", d);
                }
                lineas.add(linea);
            }
        } catch (Exception e) { e.printStackTrace(); return false; }
        reescribirArchivo(RUTA_USERS, lineas);
        invalidarCache(); // el archivo cambió → lista enlazada desactualizada
        return true;
    }

    // ══════════════════════════════════════════════════════════════
    //  ARCHIVOS BINARIOS — backup serializado de publicaciones
    //
    //  Cada vez que el usuario crea un post, se guarda también una
    //  copia serializada en INSTA_RAIZ/username/backup_posts.bin
    //  Esto demuestra el uso de ObjectOutputStream/ObjectInputStream.
    // ══════════════════════════════════════════════════════════════

    /**
     * Guarda una publicación en formato binario (serialización).
     * Agrega al archivo existente leyendo primero la lista actual.
     * Usa try-catch para manejo de IOException y ClassNotFoundException.
     */
    private void guardarPublicacionBinario(Publicacion p) {
        if (usuarioActual == null) return;
        String rutaBin = RUTA_RAIZ + "/" + usuarioActual.getUsername() + "/backup_posts.bin";

        // 1. Leer publicaciones existentes del binario
        ArrayList<Publicacion> lista = leerPublicacionesBinario(usuarioActual.getUsername());

        // 2. Agregar la nueva
        lista.add(p);

        // 3. Reescribir el archivo binario completo
        try (ObjectOutputStream oos = new ObjectOutputStream(
                new java.io.FileOutputStream(rutaBin))) {
            oos.writeObject(lista);  // ARCHIVO BINARIO: serialización
        } catch (IOException e) {
            System.out.println("Aviso: No se pudo guardar backup binario: " + e.getMessage());
        }
    }

    /**
     * Lee publicaciones desde el archivo binario de backup.
     * Si el archivo no existe o está corrupto, devuelve lista vacía.
     * Demuestra uso de ObjectInputStream y manejo de excepciones.
     */
    // ══════════════════════════════════════════════════════════════
    //  DESCUBRIMIENTO DE CONTENIDO
    // ══════════════════════════════════════════════════════════════

    @SuppressWarnings("unchecked")
    public ArrayList<Publicacion> leerPublicacionesBinario(String username) {
        String rutaBin = RUTA_RAIZ + "/" + username + "/backup_posts.bin";
        File f = new File(rutaBin);
        if (!f.exists()) return new ArrayList<>();

        try (ObjectInputStream ois = new ObjectInputStream(
                new java.io.FileInputStream(rutaBin))) {
            Object obj = ois.readObject();   // ARCHIVO BINARIO: deserialización
            if (obj instanceof ArrayList) {
                return (ArrayList<Publicacion>) obj;
            }
        } catch (IOException e) {
            System.out.println("Backup binario no legible (normal en primer uso): " + e.getMessage());
        } catch (ClassNotFoundException e) {
            System.out.println("Clase no encontrada al leer binario: " + e.getMessage());
        }
        return new ArrayList<>();
    }
}