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
 * Temas implementados: - Interfaces : implementa Interaccion y Mensajeria -
 * Lista enlazada : caché de usuarios en memoria (ListaUsuarios) - Nodos :
 * NodoUsuario como bloque de la lista enlazada - Recursividad : búsqueda
 * recursiva en ListaUsuarios - Herencia : PublicacionFoto extends Publicacion,
 * MensajeTexto/MensajeSticker extends Mensaje - Polimorfismo : toFileString()
 * sobreescrito en cada subclase - Archivos texto : lectura/escritura de
 * archivos .ins - Archivos binarios: backup serializado de publicaciones en
 * .bin - Try-catch : manejo de excepciones en todas las operaciones I/O
 */
public class Sistema implements Interaccion, Mensajeria {

    private final String RUTA_RAIZ = "INSTA_RAIZ";
    private final String RUTA_USERS = RUTA_RAIZ + "/users.ins";

    private Usuario usuarioActual;

    // ── LISTA ENLAZADA de usuarios en memoria ────────────────────
    private ListaUsuarios cacheUsuarios = new ListaUsuarios();
    private boolean cacheValida = false;

    public Sistema() {
        verificarEstructura();
        InicializadorCuentasDefault.inicializar();
    }

    public Usuario getUsuarioActual() {
        return usuarioActual;
    }

    // ══════════════════════════════════════════════════════════════
    //  CACHÉ DE USUARIOS
    // ══════════════════════════════════════════════════════════════
    private void cargarCacheUsuarios() {
        if (cacheValida) {
            return;
        }
        cacheUsuarios.limpiar();
        try (Scanner sc = new Scanner(new File(RUTA_USERS))) {
            while (sc.hasNextLine()) {
                String linea = sc.nextLine().trim();
                if (linea.isEmpty()) {
                    continue;
                }
                String[] d = linea.split("\\|");
                if (d.length < 9) {
                    continue;
                }
                try {
                    Usuario u = new Usuario(d[0], d[1], d[2], d[3].charAt(0),
                            Integer.parseInt(d[4]), d[5], LocalDate.parse(d[6]),
                            TipoCuenta.valueOf(d[7]), EstadoCuenta.valueOf(d[8]));
                    cacheUsuarios.agregar(u);
                } catch (Exception ignorado) {
                }
            }
            cacheValida = true;
        } catch (FileNotFoundException e) {
            System.out.println("users.ins no encontrado al cargar caché.");
        }
    }

    private void invalidarCache() {
        cacheValida = false;
    }

    /**
     * Permite que la GUI fuerce recarga de la caché. Necesario tras reactivar
     * una cuenta para que su contenido vuelva a ser visible inmediatamente.
     */
    public void invalidarCachePublica() {
        invalidarCache();
    }

    // ══════════════════════════════════════════════════════════════
    //  IMPLEMENTACIÓN Interaccion
    // ══════════════════════════════════════════════════════════════
    @Override
    public ArrayList buscar(String criterio) {
        return buscarUsuarios(criterio);
    }

    @Override
    public boolean existe(String username) {
        return existeUsername(username);
    }

    @Override
    public boolean seguirUsuario(String objetivo) {
        if (usuarioActual == null || objetivo.equals(usuarioActual.getUsername())) {
            return false;
        }
        Usuario u = buscarUsuario(objetivo);
        if (u == null) {
            return false;
        }
        if (u.getTipoCuenta() == TipoCuenta.PUBLICA) {
            String myFollowing = RUTA_RAIZ + "/" + usuarioActual.getUsername() + "/following.ins";
            String theirFollowers = RUTA_RAIZ + "/" + objetivo + "/followers.ins";
            if (verificarEnArchivo(myFollowing, objetivo)) {
                return false;
            }
            appendLine(myFollowing, objetivo);
            appendLine(theirFollowers, usuarioActual.getUsername());
            notificar(objetivo, "SEGUIDOR|" + usuarioActual.getUsername() + "|" + LocalDate.now());
            return true;
        } else {
            return enviarSolicitud(objetivo);
        }
    }

    @Override
    public boolean dejarDeSeguir(String objetivo) {
        if (usuarioActual == null) {
            return false;
        }
        eliminarLineaDeArchivo(
                RUTA_RAIZ + "/" + usuarioActual.getUsername() + "/following.ins", objetivo);
        eliminarLineaDeArchivo(
                RUTA_RAIZ + "/" + objetivo + "/followers.ins", usuarioActual.getUsername());

        // ← Eliminar notificación de seguidor en el objetivo
        eliminarNotificacionSeguidor(objetivo, usuarioActual.getUsername());
        return true;
    }

    // ══════════════════════════════════════════════════════════════
    //  IMPLEMENTACIÓN Mensajeria
    // ══════════════════════════════════════════════════════════════
    @Override
    public boolean enviarMensaje(Mensaje m) {
        return guardarMensaje(m);
    }

    @Override
    public void notificar(String dest, String msg) {
        guardarNotificacion(dest, msg);
    }

    @Override
    public int getTotalNotificacionesPendientes() {
        if (usuarioActual == null) {
            return 0;
        }
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
        try {
            if (!users.exists()) {
                users.createNewFile();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void crearStickersPorDefecto() {
        String carpeta = RUTA_RAIZ + "/stickers_globales";
        new File(carpeta).mkdirs();
        String[][] stickers = {
            {"feliz.png", "happy.png"}, {"triste.png", "crying.png"},
            {"corazon.png", "hearteyes.png"}, {"risa.png", "tearsofjoy.png"},
            {"aplauso.png", "thumbup.png"}
        };
        for (String[] par : stickers) {
            File dest = new File(carpeta + "/" + par[0]);
            if (!dest.exists()) {
                try (InputStream is = getClass().getResourceAsStream("/images/" + par[1])) {
                    if (is != null) {
                        java.nio.file.Files.copy(is, dest.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    // ══════════════════════════════════════════════════════════════
    //  REGISTRO Y LOGIN
    // ══════════════════════════════════════════════════════════════
    public boolean registrarUsuario(String username, String password, String nombreCompleto,
            char genero, int edad, String fotoPerfil, TipoCuenta tipoCuenta) {
        if (!usernameDisponible(username)) {
            return false;
        }
        Usuario nuevo = new Usuario(username, password, nombreCompleto, genero, edad,
                fotoPerfil, LocalDate.now(), tipoCuenta, EstadoCuenta.ACTIVO);
        try (FileWriter fw = new FileWriter(RUTA_USERS, true)) {
            fw.write(nuevo.getUsername() + "|" + nuevo.getPassword() + "|"
                    + nuevo.getNombreCompleto() + "|" + nuevo.getGenero() + "|"
                    + nuevo.getEdad() + "|" + nuevo.getFotoPerfil() + "|"
                    + nuevo.getFechaRegistro().format(DateTimeFormatter.ISO_LOCAL_DATE) + "|"
                    + nuevo.getTipoCuenta().name() + "|" + nuevo.getEstadoCuenta().name() + "\n");
            crearEstructuraUsuario(username);
            for (String def : InicializadorCuentasDefault.USERNAMES_DEFAULT) {
                if (existeUsername(def)) {
                    String myFollowing = RUTA_RAIZ + "/" + username + "/following.ins";
                    String theirFollowers = RUTA_RAIZ + "/" + def + "/followers.ins";
                    appendLine(myFollowing, def);
                    appendLine(theirFollowers, username);
                }
            }
            invalidarCache();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean login(String username, String password) {
        if (sesionActiva(username)) {
            return false;
        }
        try (Scanner sc = new Scanner(new File(RUTA_USERS))) {
            while (sc.hasNextLine()) {
                String linea = sc.nextLine().trim();
                if (linea.isEmpty()) {
                    continue;
                }
                String[] d = linea.split("\\|");
                if (d.length < 9) {
                    continue;
                }
                if (d[0].equals(username) && d[1].equals(password)) {
                    if (EstadoCuenta.valueOf(d[8]) == EstadoCuenta.DESACTIVADO) {
                        return false;
                    }
                    usuarioActual = new Usuario(username, password, d[2], d[3].charAt(0),
                            Integer.parseInt(d[4]), d[5], LocalDate.parse(d[6]),
                            TipoCuenta.valueOf(d[7]), EstadoCuenta.valueOf(d[8]));
                    crearBloqueoSesion();
                    return true;
                }
            }
        } catch (FileNotFoundException e) {
            System.out.println("users.ins no encontrado.");
        }
        return false;
    }

    public void logout() {
        eliminarBloqueoSesion();
        usuarioActual = null;
    }

    // ══════════════════════════════════════════════════════════════
    //  ESTRUCTURA POR USUARIO
    // ══════════════════════════════════════════════════════════════
    private void crearEstructuraUsuario(String username) {
        String base = RUTA_RAIZ + "/" + username;
        new File(base).mkdir();
        String[] archivos = {"followers.ins", "following.ins", "insta.ins", "inbox.ins",
            "stickers.ins", "solicitudes.ins", "likes.ins", "comments.ins", "notifications.ins", "likes_notif.ins"};
        for (String a : archivos) {
            File f = new File(base + "/" + a);
            try {
                if (!f.exists()) {
                    f.createNewFile();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        new File(base + "/imagenes").mkdir();
        new File(base + "/stickers_personales").mkdir();
        new File(base + "/folders_personales").mkdir();
    }

    public boolean usernameDisponible(String username) {
        // Un username NO está disponible si ya existe, activo O desactivado
        cargarCacheUsuarios();
        return !cacheUsuarios.contiene(username);
    }

    // ══════════════════════════════════════════════════════════════
    //  BÚSQUEDA — usa ListaUsuarios (recursividad)
    // ══════════════════════════════════════════════════════════════
    public boolean existeUsername(String username) {
        cargarCacheUsuarios();
        return cacheUsuarios.contiene(username);
    }

    public Usuario buscarUsuario(String username) {
        cargarCacheUsuarios();
        return cacheUsuarios.buscarRecursivo(username);
    }

    public ArrayList<Usuario> buscarUsuarios(String criterio) {
        if (criterio == null || criterio.isEmpty()) {
            return new ArrayList<>();
        }
        cargarCacheUsuarios();
        String excluir = usuarioActual != null ? usuarioActual.getUsername() : null;
        ArrayList<Usuario> resultado = cacheUsuarios.buscarPorCriterio(criterio, excluir);
        // Filtrar desactivadas
        resultado.removeIf(u -> u.getEstadoCuenta() == EstadoCuenta.DESACTIVADO);
        return resultado;
    }

    // ══════════════════════════════════════════════════════════════
    //  PUBLICACIONES
    // ══════════════════════════════════════════════════════════════
    public boolean crearPublicacion(String contenido, String rutaImagen,
            String hashtags, String menciones) {
        if (usuarioActual == null) {
            return false;
        }
        // Permitir contenido vacío, solo validar longitud si hay contenido
        if (contenido != null && contenido.length() > 220) {
            return false;
        }

        String contenidoFinal = contenido != null ? contenido : "";
        PublicacionFoto nueva = new PublicacionFoto(
                usuarioActual.getUsername(), contenidoFinal, rutaImagen, hashtags, menciones);
        String ruta = RUTA_RAIZ + "/" + usuarioActual.getUsername() + "/insta.ins";
        try (FileWriter fw = new FileWriter(ruta, true)) {
            fw.write(nueva.toFileString() + "\n");
            // Menciones solo si hay contenido
            if (menciones != null && !menciones.isEmpty()) {
                for (String m : menciones.split(" ")) {
                    if (m.startsWith("@")) {
                        String men = m.substring(1);
                        if (existeUsername(men)) {
                            notificar(men, "MENCION|" + usuarioActual.getUsername()
                                    + "|" + contenidoFinal + "|" + LocalDate.now());
                        }
                    }
                }
            }
            guardarPublicacionBinario(nueva);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean eliminarPublicacion(Publicacion p) {
        if (usuarioActual == null || !p.getAutor().equals(usuarioActual.getUsername())) {
            return false;
        }
        String rutaInsta = RUTA_RAIZ + "/" + usuarioActual.getUsername() + "/insta.ins";
        ArrayList<String> restantes = new ArrayList<>();
        boolean encontrado = false;
        try (Scanner sc = new Scanner(new File(rutaInsta))) {
            while (sc.hasNextLine()) {
                String linea = sc.nextLine();
                Publicacion temp = Publicacion.fromFileString(linea);
                if (temp != null && temp.getAutor().equals(p.getAutor())
                        && temp.getFecha().equals(p.getFecha())
                        && temp.getHora().equals(p.getHora())) {
                    encontrado = true;
                } else {
                    restantes.add(linea);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        if (encontrado) {
            try (FileWriter fw = new FileWriter(rutaInsta)) {
                for (String l : restantes) {
                    fw.write(l + "\n");
                }
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
            if (p.getRutaImagen() != null && !p.getRutaImagen().isEmpty()) {
                new File(p.getRutaImagen()).delete();
            }
            return true;
        }
        return false;
    }

    // ── TIMELINE ────────────────────────────────────────────────
    public ArrayList<Publicacion> getTimeline() {
        ArrayList<Publicacion> timeline = new ArrayList<>();
        if (usuarioActual == null) {
            return timeline;
        }

        java.util.Set<String> vistos = new java.util.HashSet<>();

        // Helper para agregar sin duplicados
        java.util.function.Consumer<String> agregarPosts = (username) -> {
            if (username == null || username.isEmpty()) {
                return;
            }
            // No mostrar posts de cuentas desactivadas (excepto la propia)
            if (!username.equals(usuarioActual.getUsername())) {
                Usuario u = buscarUsuario(username);
                if (u != null && u.getEstadoCuenta() == EstadoCuenta.DESACTIVADO) {
                    return;
                }
            }
            File archivo = new File(RUTA_RAIZ + "/" + username + "/insta.ins");
            if (!archivo.exists()) {
                return;
            }
            try (Scanner sc = new Scanner(archivo)) {
                while (sc.hasNextLine()) {
                    String linea = sc.nextLine();
                    if (linea.trim().isEmpty()) {
                        continue;
                    }
                    Publicacion p = Publicacion.fromFileString(linea);
                    if (p != null) {
                        // ID único por autor + fecha + hora
                        String id = p.getAutor() + "|" + p.getFecha() + "|" + p.getHora();
                        if (vistos.add(id)) {
                            timeline.add(p); // add() devuelve false si ya existía
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        };

        // 1. Posts propios
        agregarPosts.accept(usuarioActual.getUsername());

        // 2. Siempre incluir cuentas default
        for (String def : InicializadorCuentasDefault.USERNAMES_DEFAULT) {
            agregarPosts.accept(def);
        }

        // 3. Cuentas que sigo (defaults ya están en el Set, no se duplican)
        File fFollowing = new File(RUTA_RAIZ + "/" + usuarioActual.getUsername() + "/following.ins");
        if (fFollowing.exists()) {
            try (Scanner sc = new Scanner(fFollowing)) {
                while (sc.hasNextLine()) {
                    String u = sc.nextLine().trim();
                    if (!u.isEmpty()) {
                        agregarPosts.accept(u);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // 4. Ordenar más reciente primero
        timeline.sort(Comparator.comparing(Publicacion::getFecha)
                .thenComparing(Publicacion::getHora).reversed());
        return timeline;
    }

    private void leerPublicacionesUsuario(String username, ArrayList<Publicacion> lista) {
        // No mostrar publicaciones de cuentas desactivadas (excepto la propia)
        if (usuarioActual != null && !username.equals(usuarioActual.getUsername())) {
            Usuario u = buscarUsuario(username);
            if (u != null && u.getEstadoCuenta() == EstadoCuenta.DESACTIVADO) {
                return;
            }
        }
        File archivo = new File(RUTA_RAIZ + "/" + username + "/insta.ins");
        if (!archivo.exists()) {
            return;
        }
        try (Scanner sc = new Scanner(archivo)) {
            while (sc.hasNextLine()) {
                String linea = sc.nextLine();
                if (!linea.trim().isEmpty()) {
                    Publicacion p = Publicacion.fromFileString(linea);
                    if (p != null) {
                        lista.add(p);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public ArrayList<Publicacion> getPublicacionesDeUsuario(String username) {
        ArrayList<Publicacion> lista = new ArrayList<>();
        Usuario u = buscarUsuario(username);
        if (u == null) {
            return lista;
        }

        // FIX: cuentas desactivadas solo ocultan contenido a otros, no al dueño
        if (u.getEstadoCuenta() == EstadoCuenta.DESACTIVADO) {
            boolean esMio = usuarioActual != null && username.equals(usuarioActual.getUsername());
            if (!esMio) {
                return lista;
            }
        }

        if (u.getTipoCuenta() == TipoCuenta.PRIVADA) {
            if (usuarioActual == null || !usuarioActual.getUsername().equals(username)) {
                String rutaF = RUTA_RAIZ + "/" + username + "/followers.ins";
                if (usuarioActual == null || !verificarEnArchivo(rutaF, usuarioActual.getUsername())) {
                    return lista;
                }
            }
        }

        File archivo = new File(RUTA_RAIZ + "/" + username + "/insta.ins");
        if (archivo.exists()) {
            try (Scanner sc = new Scanner(archivo)) {
                while (sc.hasNextLine()) {
                    String linea = sc.nextLine();
                    if (!linea.trim().isEmpty()) {
                        Publicacion p = Publicacion.fromFileString(linea);
                        if (p != null) {
                            lista.add(p);
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
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
        ArrayList<String> lista = new ArrayList<>();
        leerLineas(RUTA_RAIZ + "/" + username + "/followers.ins", lista);
        int count = 0;
        for (String u : lista) {
            Usuario usr = buscarUsuario(u);
            if (usr != null && usr.getEstadoCuenta() != EstadoCuenta.DESACTIVADO) {
                count++;
            }
        }
        return count;
    }

    public int getCantidadFollowing(String username) {
        ArrayList<String> lista = new ArrayList<>();
        leerLineas(RUTA_RAIZ + "/" + username + "/following.ins", lista);
        int count = 0;
        for (String u : lista) {
            Usuario usr = buscarUsuario(u);
            if (usr != null && usr.getEstadoCuenta() != EstadoCuenta.DESACTIVADO) {
                count++;
            }
        }
        return count;
    }

    public String getRutaLikesNotif() {
        if (usuarioActual == null) {
            return "";
        }
        return RUTA_RAIZ + "/" + usuarioActual.getUsername() + "/likes_notif.ins";
    }

    public ArrayList<String> getNotificacionesComentarios() {
        ArrayList<String> lista = new ArrayList<>();
        if (usuarioActual == null) {
            return lista;
        }
        File f = new File(RUTA_RAIZ + "/" + usuarioActual.getUsername() + "/likes_notif.ins");
        if (!f.exists()) {
            return lista;
        }
        try (Scanner sc = new Scanner(f)) {
            while (sc.hasNextLine()) {
                String l = sc.nextLine().trim();
                if (l.startsWith("COMENTARIO|")) {
                    lista.add(l);
                }
            }
        } catch (Exception ignored) {
        }
        Collections.reverse(lista);
        return lista;
    }

    public int getCantidadLikes(String autorPost, String fechaPost) {
        File f = new File(RUTA_RAIZ + "/" + autorPost + "/likes.ins");
        if (!f.exists()) {
            return 0;
        }
        int count = 0;
        String prefijo = autorPost + "|" + fechaPost + "|";
        try (Scanner sc = new Scanner(f)) {
            while (sc.hasNextLine()) {
                String linea = sc.nextLine().trim();
                if (linea.startsWith(prefijo)) {
                    String[] partes = linea.split("\\|");
                    if (partes.length >= 3) {
                        Usuario u = buscarUsuario(partes[2]);
                        if (u != null && u.getEstadoCuenta() != EstadoCuenta.DESACTIVADO) {
                            count++;
                        }
                    }
                }
            }
        } catch (Exception ignored) {
        }
        return count;
    }

    // ══════════════════════════════════════════════════════════════
    //  FOLLOWS Y SOLICITUDES
    // ══════════════════════════════════════════════════════════════
    public boolean yaLoSigo(String objetivo) {
        if (usuarioActual == null) {
            return false;
        }
        return verificarEnArchivo(RUTA_RAIZ + "/" + usuarioActual.getUsername() + "/following.ins", objetivo);
    }

    public boolean solicitudPendiente(String objetivo) {
        if (usuarioActual == null) {
            return false;
        }
        return verificarEnArchivo(RUTA_RAIZ + "/" + objetivo + "/solicitudes.ins", usuarioActual.getUsername());
    }

    public boolean eliminarSeguidor(String seguidor) {
        if (usuarioActual == null) {
            return false;
        }
        eliminarLineaDeArchivo(RUTA_RAIZ + "/" + usuarioActual.getUsername() + "/followers.ins", seguidor);
        eliminarLineaDeArchivo(RUTA_RAIZ + "/" + seguidor + "/following.ins", usuarioActual.getUsername());
        return true;
    }

    private boolean enviarSolicitud(String objetivo) {
        String ruta = RUTA_RAIZ + "/" + objetivo + "/solicitudes.ins";
        if (verificarEnArchivo(ruta, usuarioActual.getUsername())) {
            return false;
        }
        return appendLine(ruta, usuarioActual.getUsername());
    }

    public void aceptarSolicitud(String solicitante) {
        appendLine(RUTA_RAIZ + "/" + usuarioActual.getUsername() + "/followers.ins", solicitante);
        appendLine(RUTA_RAIZ + "/" + solicitante + "/following.ins", usuarioActual.getUsername());
        eliminarLineaDeArchivo(RUTA_RAIZ + "/" + usuarioActual.getUsername() + "/solicitudes.ins", solicitante);
    }

    public void rechazarSolicitud(String solicitante) {
        eliminarLineaDeArchivo(RUTA_RAIZ + "/" + usuarioActual.getUsername() + "/solicitudes.ins", solicitante);
    }

    public ArrayList<String> getSolicitudes() {
        ArrayList<String> lista = new ArrayList<>();
        if (usuarioActual == null) {
            return lista;
        }
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
    // ══════════════════════════════════════════════════════════════
    public boolean yaDioLike(String autorPost, String fechaPost) {
        if (usuarioActual == null) {
            return false;
        }
        String prefijo = autorPost + "|" + fechaPost + "|" + usuarioActual.getUsername();
        File f = new File(RUTA_RAIZ + "/" + autorPost + "/likes.ins");
        if (!f.exists()) {
            return false;
        }
        try (Scanner sc = new Scanner(f)) {
            while (sc.hasNextLine()) {
                if (sc.nextLine().trim().startsWith(prefijo)) {
                    return true;
                }
            }
        } catch (Exception ignored) {
        }
        return false;
    }

    public boolean toggleLike(String autorPost, String fechaPost) {
        return toggleLike(autorPost, fechaPost, "");
    }

    public boolean toggleLike(String autorPost, String fechaPost, String rutaImagen) {
        if (usuarioActual == null) {
            return false;
        }
        String rutaLikes = RUTA_RAIZ + "/" + autorPost + "/likes.ins";
        String prefijo = autorPost + "|" + fechaPost + "|" + usuarioActual.getUsername();

        boolean yaLiked = false;
        String lineaEx = null;
        File f = new File(rutaLikes);
        if (f.exists()) {
            try (Scanner sc = new Scanner(f)) {
                while (sc.hasNextLine()) {
                    String l = sc.nextLine().trim();
                    if (l.startsWith(prefijo)) {
                        yaLiked = true;
                        lineaEx = l;
                        break;
                    }
                }
            } catch (Exception ignored) {
            }
        }

        if (yaLiked) {
            eliminarLineaDeArchivo(rutaLikes, lineaEx);
            if (!usuarioActual.getUsername().equals(autorPost)) {
                eliminarNotificacionLike(autorPost, fechaPost, usuarioActual.getUsername());
            }
            return false;
        } else {
            try (FileWriter fw = new FileWriter(rutaLikes, true)) {
                fw.write(prefijo + "|" + (rutaImagen != null ? rutaImagen : "") + "\n");
            } catch (IOException e) {
                e.printStackTrace();
            }

            // ── NUEVO: escribir notificación separada ──
            if (!usuarioActual.getUsername().equals(autorPost)) { // no notificar likes propios
                String rutaNotifLikes = RUTA_RAIZ + "/" + autorPost + "/likes_notif.ins";
                try {
                    File fn = new File(rutaNotifLikes);
                    if (!fn.exists()) {
                        fn.createNewFile();
                    }
                } catch (IOException ignored) {
                }
                appendLine(rutaNotifLikes, autorPost + "|" + fechaPost + "|"
                        + usuarioActual.getUsername() + "|" + (rutaImagen != null ? rutaImagen : ""));
            }
            return true;
        }
    }

    public ArrayList<String> getNotificacionesLikes() {
        ArrayList<String> notifs = new ArrayList<>();
        if (usuarioActual == null) {
            return notifs;
        }
        File f = new File(RUTA_RAIZ + "/" + usuarioActual.getUsername() + "/likes_notif.ins");
        if (!f.exists()) {
            return notifs;
        }
        try (Scanner sc = new Scanner(f)) {
            while (sc.hasNextLine()) {
                String l = sc.nextLine().trim();
                // Solo likes, no comentarios
                if (!l.isEmpty() && !l.startsWith("COMENTARIO|")) {
                    notifs.add(l);
                }
            }
        } catch (Exception ignored) {
        }
        Collections.reverse(notifs);
        return notifs;
    }

    // ══════════════════════════════════════════════════════════════
    //  COMENTARIOS
    // ══════════════════════════════════════════════════════════════
    public void agregarComentario(String autorPost, String fechaPost, String comentario) {
        if (usuarioActual == null) {
            return;
        }
        String ruta = RUTA_RAIZ + "/" + autorPost + "/comments.ins";
        try (FileWriter fw = new FileWriter(ruta, true)) {
            fw.write(fechaPost + "|" + usuarioActual.getUsername() + "|" + comentario + "\n");
        } catch (IOException e) {
            e.printStackTrace();
        }

        // ← Notificar al autor del post si no es el mismo usuario
        if (!usuarioActual.getUsername().equals(autorPost)) {
            String rutaNotif = RUTA_RAIZ + "/" + autorPost + "/likes_notif.ins";
            try {
                File fn = new File(rutaNotif);
                if (!fn.exists()) {
                    fn.createNewFile();
                }
            } catch (IOException ignored) {
            }
            appendLine(rutaNotif, "COMENTARIO|" + fechaPost + "|"
                    + usuarioActual.getUsername() + "|" + comentario);
        }

        // menciones existentes
        for (String p : comentario.split(" ")) {
            if (p.startsWith("@")) {
                String men = p.substring(1);
                if (existeUsername(men)) {
                    guardarNotificacion(men, "MENCION|" + usuarioActual.getUsername()
                            + "|" + comentario + "|" + LocalDate.now());
                }
            }
        }
    }

    public ArrayList<String> getComentarios(String autorPost, String fechaPost) {
        ArrayList<String> lista = new ArrayList<>();
        File f = new File(RUTA_RAIZ + "/" + autorPost + "/comments.ins");
        if (!f.exists()) {
            return lista;
        }
        try (Scanner sc = new Scanner(f)) {
            while (sc.hasNextLine()) {
                String linea = sc.nextLine().trim();
                if (linea.isEmpty()) {
                    continue;
                }
                // Usar split con límite 3 para preservar texto con | dentro
                String[] d = linea.split("\\|", 3);
                if (d.length >= 3 && d[0].equals(fechaPost)) {
                    lista.add(d[1] + ": " + d[2]);
                }
            }
        } catch (Exception ignored) {
        }
        return lista;
    }

    // ══════════════════════════════════════════════════════════════
    //  NOTIFICACIONES
    // ══════════════════════════════════════════════════════════════
    private void guardarNotificacion(String destino, String mensaje) {
        String ruta = RUTA_RAIZ + "/" + destino + "/notifications.ins";
        try {
            File f = new File(ruta);
            if (!f.getParentFile().exists()) {
                f.getParentFile().mkdirs();
            }
            if (!f.exists()) {
                f.createNewFile();
            }
        } catch (IOException ignored) {
        }
        try (FileWriter fw = new FileWriter(ruta, true)) {
            fw.write(mensaje + "\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public ArrayList<String> getNotificacionesGenerales() {
        ArrayList<String> lista = new ArrayList<>();
        if (usuarioActual == null) {
            return lista;
        }
        leerLineas(RUTA_RAIZ + "/" + usuarioActual.getUsername() + "/notifications.ins", lista);
        Collections.reverse(lista);
        return lista;
    }

    private void eliminarNotificacionSeguidor(String destino, String quien) {
        String ruta = RUTA_RAIZ + "/" + destino + "/notifications.ins";
        File f = new File(ruta);
        if (!f.exists()) {
            return;
        }
        ArrayList<String> lineas = new ArrayList<>();
        try (Scanner sc = new Scanner(f)) {
            while (sc.hasNextLine()) {
                String l = sc.nextLine();
                // Eliminar líneas tipo "SEGUIDOR|quien|fecha"
                if (l.startsWith("SEGUIDOR|" + quien + "|")) {
                    continue;
                }
                lineas.add(l);
            }
        } catch (Exception ignored) {
        }
        reescribirArchivo(ruta, lineas);
    }

    private void eliminarNotificacionLike(String autorPost, String fechaPost, String quien) {
        String ruta = RUTA_RAIZ + "/" + autorPost + "/likes_notif.ins";
        File f = new File(ruta);
        if (!f.exists()) {
            return;
        }
        ArrayList<String> lineas = new ArrayList<>();
        try (Scanner sc = new Scanner(f)) {
            while (sc.hasNextLine()) {
                String l = sc.nextLine();
                if (l.startsWith(autorPost + "|" + fechaPost + "|" + quien)) {
                    continue;
                }
                lineas.add(l);
            }
        } catch (Exception ignored) {
        }
        reescribirArchivo(ruta, lineas);
    }

    // ══════════════════════════════════════════════════════════════
    //  MENSAJERÍA
    // ══════════════════════════════════════════════════════════════
    public boolean guardarMensaje(Mensaje m) {
        try (FileWriter fw = new FileWriter(RUTA_RAIZ + "/" + m.getReceptor() + "/inbox.ins", true)) {
            fw.write(m.toFileString() + "\n");
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    public boolean puedeEnviarMensaje(String receptor) {
        Usuario u = buscarUsuario(receptor);
        if (u == null) {
            return false;
        }
        if (u.getTipoCuenta() == TipoCuenta.PUBLICA) {
            return true;
        }
        boolean yoLoSigo = verificarEnArchivo(RUTA_RAIZ + "/" + usuarioActual.getUsername() + "/following.ins", receptor);
        boolean elMeSigue = verificarEnArchivo(RUTA_RAIZ + "/" + receptor + "/followers.ins", usuarioActual.getUsername());
        return yoLoSigo && elMeSigue;
    }

    public boolean enviarMensaje(String receptor, String contenido, String tipo) {
        if (usuarioActual == null || contenido.length() > 300) {
            return false;
        }
        LocalDate fecha = LocalDate.now();
        LocalTime hora = LocalTime.now();

        Mensaje paraRec = "STICKER".equals(tipo)
                ? new MensajeSticker(usuarioActual.getUsername(), receptor, contenido)
                : new MensajeTexto(usuarioActual.getUsername(), receptor, contenido);
        Mensaje miCopia = "STICKER".equals(tipo)
                ? new MensajeSticker(usuarioActual.getUsername(), receptor, contenido)
                : new MensajeTexto(usuarioActual.getUsername(), receptor, contenido);

        paraRec.fecha = fecha;
        paraRec.hora = hora;
        paraRec.setEstado("NO_LEIDO");
        miCopia.fecha = fecha;
        miCopia.hora = hora;
        miCopia.setEstado("NO_LEIDO");

        guardarMensaje(paraRec);
        try (FileWriter fw = new FileWriter(RUTA_RAIZ + "/" + usuarioActual.getUsername() + "/inbox.ins", true)) {
            fw.write(miCopia.toFileString() + "\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return true;
    }

    public ArrayList<Mensaje> getConversacion(String otro) {
        ArrayList<Mensaje> conv = new ArrayList<>();
        if (usuarioActual == null) {
            return conv;
        }
        File archivo = new File(RUTA_RAIZ + "/" + usuarioActual.getUsername() + "/inbox.ins");
        if (!archivo.exists()) {
            return conv;
        }
        try (Scanner sc = new Scanner(archivo)) {
            while (sc.hasNextLine()) {
                Mensaje m = Mensaje.fromFileString(sc.nextLine());
                if (m != null) {
                    boolean en = m.getEmisor().equals(otro) && m.getReceptor().equals(usuarioActual.getUsername());
                    boolean sa = m.getEmisor().equals(usuarioActual.getUsername()) && m.getReceptor().equals(otro);
                    if (en || sa) {
                        conv.add(m);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return conv;
    }

    public void marcarComoLeido(String otro) {
        if (usuarioActual == null) {
            return;
        }
        String rutaMio = RUTA_RAIZ + "/" + usuarioActual.getUsername() + "/inbox.ins";
        File archivo = new File(rutaMio);
        if (!archivo.exists()) {
            return;
        }
        ArrayList<String> lineas = new ArrayList<>();
        try (Scanner sc = new Scanner(archivo)) {
            while (sc.hasNextLine()) {
                String linea = sc.nextLine();
                Mensaje m = Mensaje.fromFileString(linea);
                if (m != null && m.getEmisor().equals(otro) && "NO_LEIDO".equals(m.getEstado())) {
                    m.setEstado("LEIDO");
                    lineas.add(m.toFileString());
                    actualizarEstadoEnArchivoAjeno(otro, usuarioActual.getUsername(), m.getFecha(), m.getHora());
                } else {
                    lineas.add(linea);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        reescribirArchivo(rutaMio, lineas);
    }

    private void actualizarEstadoEnArchivoAjeno(String otro, String miUser,
            java.time.LocalDate fecha, java.time.LocalTime hora) {
        String rutaOtro = RUTA_RAIZ + "/" + otro + "/inbox.ins";
        File f = new File(rutaOtro);
        if (!f.exists()) {
            return;
        }
        ArrayList<String> lineas = new ArrayList<>();
        boolean cambio = false;
        try (Scanner sc = new Scanner(f)) {
            while (sc.hasNextLine()) {
                String linea = sc.nextLine();
                Mensaje m = Mensaje.fromFileString(linea);
                if (m != null && m.getEmisor().equals(otro) && m.getReceptor().equals(miUser)
                        && m.getFecha().equals(fecha) && m.getHora().equals(hora)) {
                    m.setEstado("LEIDO");
                    lineas.add(m.toFileString());
                    cambio = true;
                } else {
                    lineas.add(linea);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (cambio) {
            reescribirArchivo(rutaOtro, lineas);
        }
    }

    public ArrayList<String> getChatsRecientes() {
        ArrayList<String> usuarios = new ArrayList<>();
        if (usuarioActual == null) {
            return usuarios;
        }
        File archivo = new File(RUTA_RAIZ + "/" + usuarioActual.getUsername() + "/inbox.ins");
        if (!archivo.exists()) {
            return usuarios;
        }
        ArrayList<String> todasLineas = new ArrayList<>();
        try (Scanner sc = new Scanner(archivo)) {
            while (sc.hasNextLine()) {
                todasLineas.add(sc.nextLine());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        for (int i = todasLineas.size() - 1; i >= 0; i--) {
            Mensaje m = Mensaje.fromFileString(todasLineas.get(i));
            if (m != null) {
                String otro = m.getEmisor().equals(usuarioActual.getUsername()) ? m.getReceptor() : m.getEmisor();
                if (!usuarios.contains(otro)) {
                    usuarios.add(otro);
                }
            }
        }
        return usuarios;
    }

    public int getMensajesNoLeidos(String otroUsuario) {
        if (usuarioActual == null) {
            return 0;
        }
        File archivo = new File(RUTA_RAIZ + "/" + usuarioActual.getUsername() + "/inbox.ins");
        if (!archivo.exists()) {
            return 0;
        }
        int count = 0;
        try (Scanner sc = new Scanner(archivo)) {
            while (sc.hasNextLine()) {
                Mensaje m = Mensaje.fromFileString(sc.nextLine());
                if (m != null && m.getEmisor().equals(otroUsuario)
                        && m.getReceptor().equals(usuarioActual.getUsername())
                        && "NO_LEIDO".equals(m.getEstado())) {
                    count++;
                }
            }
        } catch (Exception ignored) {
        }
        return count;
    }

    public int getTotalMensajesNoLeidos() {
        if (usuarioActual == null) {
            return 0;
        }
        File archivo = new File(RUTA_RAIZ + "/" + usuarioActual.getUsername() + "/inbox.ins");
        if (!archivo.exists()) {
            return 0;
        }
        int count = 0;
        try (Scanner sc = new Scanner(archivo)) {
            while (sc.hasNextLine()) {
                Mensaje m = Mensaje.fromFileString(sc.nextLine());
                if (m != null && m.getReceptor().equals(usuarioActual.getUsername())
                        && "NO_LEIDO".equals(m.getEstado())) {
                    count++;
                }
            }
        } catch (Exception ignored) {
        }
        return count;
    }

    public void eliminarConversacion(String otro) {
        if (usuarioActual == null) {
            return;
        }
        String ruta = RUTA_RAIZ + "/" + usuarioActual.getUsername() + "/inbox.ins";
        File archivo = new File(ruta);
        if (!archivo.exists()) {
            return;
        }
        ArrayList<String> lineas = new ArrayList<>();
        try (Scanner sc = new Scanner(archivo)) {
            while (sc.hasNextLine()) {
                String linea = sc.nextLine();
                Mensaje m = Mensaje.fromFileString(linea);
                boolean esConv = m != null && ((m.getEmisor().equals(otro) && m.getReceptor().equals(usuarioActual.getUsername()))
                        || (m.getEmisor().equals(usuarioActual.getUsername()) && m.getReceptor().equals(otro)));
                if (!esConv) {
                    lineas.add(linea);
                }
            }
        } catch (Exception ignored) {
        }
        reescribirArchivo(ruta, lineas);
    }

    public void compartirPost(String destino, String autorPost, String rutaImagen, String contenido) {
        enviarMensaje(destino, "SHARE|" + autorPost + "|" + rutaImagen + "|" + contenido, "TEXTO");
    }

    public boolean puedeCompartirPost(String destino, String autorPost) {
        Usuario autor = buscarUsuario(autorPost);
        Usuario dst = buscarUsuario(destino);
        if (autor == null || dst == null) {
            return false;
        }
        if (autor.getTipoCuenta() == TipoCuenta.PUBLICA) {
            return true;
        }
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
        String[] archivos = carpeta.list((d, n) -> n.toLowerCase().endsWith(".png") || n.toLowerCase().endsWith(".jpg"));
        if (archivos != null) {
            for (String f : archivos) {
                lista.add(carpeta.getPath() + "/" + f);
            }
        }
        return lista;
    }

    public boolean guardarStickerPersonal(File origen, String username) {
        try {
            String rc = RUTA_RAIZ + "/" + username + "/stickers_personales";
            new File(rc).mkdirs();
            String nf = "sticker_" + System.currentTimeMillis() + ".png";
            File dest = new File(rc + "/" + nf);
            java.nio.file.Files.copy(origen.toPath(), dest.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            appendLine(RUTA_RAIZ + "/" + username + "/stickers.ins", dest.getAbsolutePath());
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public ArrayList<String> getTodosStickers(String username) {
        ArrayList<String> lista = new ArrayList<>(getStickersGlobales());
        File carpeta = new File(RUTA_RAIZ + "/" + username + "/stickers_personales");
        if (carpeta.exists()) {
            String[] archivos = carpeta.list((d, n) -> n.toLowerCase().endsWith(".png") || n.toLowerCase().endsWith(".jpg"));
            if (archivos != null) {
                for (String f : archivos) {
                    lista.add(carpeta.getPath() + "/" + f);
                }
            }
        }
        return lista;
    }

    // ══════════════════════════════════════════════════════════════
    //  BÚSQUEDA POR HASHTAG (sin duplicados, sin desactivados)
    // ══════════════════════════════════════════════════════════════
    public ArrayList<Publicacion> buscarPorHashtag(String hashtag) {
        ArrayList<Publicacion> resultados = new ArrayList<>();
        if (hashtag == null || hashtag.isEmpty()) {
            return resultados;
        }
        if (!hashtag.startsWith("#")) {
            hashtag = "#" + hashtag;
        }
        ArrayList<String> ids = new ArrayList<>();
        File raiz = new File(RUTA_RAIZ);
        String[] users = raiz.list();
        if (users == null) {
            return resultados;
        }
        for (String u : users) {
            File f = new File(RUTA_RAIZ + "/" + u);
            if (!f.isDirectory() || u.equals("stickers_globales")) {
                continue;
            }
            // No mostrar posts de cuentas desactivadas
            Usuario usr = buscarUsuario(u);
            if (usr != null && usr.getEstadoCuenta() == EstadoCuenta.DESACTIVADO) {
                continue;
            }
            for (Publicacion p : getPublicacionesDeUsuario(u)) {
                if (p.getContenido() == null || !p.getContenido().contains(hashtag)) {
                    continue;
                }
                String id = p.getAutor() + "|" + p.getFecha() + "|" + p.getHora();
                if (!ids.contains(id)) {
                    ids.add(id);
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
        if (usuarioActual == null) {
            return;
        }
        usuarioActual.setEstadoCuenta(nuevoEstado);
        actualizarCampoUsuario(usuarioActual.getUsername(), 8, nuevoEstado.name());
    }

    public void reactivarCuenta(String username) {
        actualizarCampoUsuario(username, 8, EstadoCuenta.ACTIVO.name());
        invalidarCache(); // garantizar que la GUI vea el cambio inmediatamente
    }

    public boolean actualizarDatosUsuario(String nuevoNombre, String nuevaPassword) {
        if (usuarioActual == null) {
            return false;
        }
        File archivo = new File(RUTA_USERS);
        ArrayList<String> lineas = new ArrayList<>();
        try (Scanner sc = new Scanner(archivo)) {
            while (sc.hasNextLine()) {
                String linea = sc.nextLine();
                if (linea.trim().isEmpty()) {
                    lineas.add(linea);
                    continue;
                }
                String[] d = linea.split("\\|");
                if (d[0].equals(usuarioActual.getUsername())) {
                    d[2] = nuevoNombre;
                    if (nuevaPassword != null && !nuevaPassword.isEmpty()) {
                        d[1] = nuevaPassword;
                    }
                    linea = String.join("|", d);
                    usuarioActual.setNombreCompleto(nuevoNombre);
                }
                lineas.add(linea);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        reescribirArchivo(RUTA_USERS, lineas);
        return true;
    }

    public boolean actualizarFotoPerfil(String username, String nuevaRuta) {
        if (usuarioActual != null && usuarioActual.getUsername().equals(username)) {
            usuarioActual.setFotoPerfil(nuevaRuta);
        }
        return actualizarCampoUsuario(username, 5, nuevaRuta);
    }

    public void actualizarEdad(int nuevaEdad) {
        if (usuarioActual == null) {
            return;
        }
        usuarioActual.setEdad(nuevaEdad);
        actualizarCampoUsuario(usuarioActual.getUsername(), 4, String.valueOf(nuevaEdad));
    }

    public void actualizarGenero(char nuevoGenero) {
        if (usuarioActual == null) {
            return;
        }
        usuarioActual.setGenero(nuevoGenero);
        actualizarCampoUsuario(usuarioActual.getUsername(), 3, String.valueOf(nuevoGenero));
    }

    public void actualizarTipoCuenta(TipoCuenta nuevoTipo) {
        if (usuarioActual == null) {
            return;
        }
        usuarioActual.setTipoCuenta(nuevoTipo);
        actualizarCampoUsuario(usuarioActual.getUsername(), 7, nuevoTipo.name());
    }

    // ══════════════════════════════════════════════════════════════
    //  SESIÓN
    // ══════════════════════════════════════════════════════════════
    public boolean sesionActiva(String username) {
        return new File(RUTA_RAIZ + "/" + username + "/session.lock").exists();
    }

    private void crearBloqueoSesion() {
        if (usuarioActual == null) {
            return;
        }
        try {
            new File(RUTA_RAIZ + "/" + usuarioActual.getUsername() + "/session.lock").createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void eliminarBloqueoSesion() {
        if (usuarioActual == null) {
            return;
        }
        new File(RUTA_RAIZ + "/" + usuarioActual.getUsername() + "/session.lock").delete();
    }

    // ══════════════════════════════════════════════════════════════
    //  MENCIONES
    // ══════════════════════════════════════════════════════════════
    public ArrayList<Publicacion> getMenciones() {
        ArrayList<Publicacion> menciones = new ArrayList<>();
        if (usuarioActual == null) {
            return menciones;
        }
        String miUsername = usuarioActual.getUsername();
        File raiz = new File(RUTA_RAIZ);
        String[] carpetas = raiz.list();
        if (carpetas == null) {
            return menciones;
        }
        for (String folder : carpetas) {
            File f = new File(RUTA_RAIZ + "/" + folder);
            if (!f.isDirectory() || folder.equals("stickers_globales")) {
                continue;
            }
            // No mostrar menciones de cuentas desactivadas
            Usuario u = buscarUsuario(folder);
            if (u != null && u.getEstadoCuenta() == EstadoCuenta.DESACTIVADO) {
                continue;
            }
            for (Publicacion p : getPublicacionesDeUsuario(folder)) {
                if (p.getContenido() != null && p.getContenido().contains("@" + miUsername)) {
                    menciones.add(p);
                }
            }
        }
        return menciones;
    }

    //SINCRONIZACION DE FOLLOWERS
    public void sincronizarDefaultsAlLogin() {
        if (usuarioActual == null) {
            return;
        }
        for (String def : InicializadorCuentasDefault.USERNAMES_DEFAULT) {
            if (!existeUsername(def)) {
                continue;
            }
            String myFollowing = RUTA_RAIZ + "/" + usuarioActual.getUsername() + "/following.ins";
            String theirFollowers = RUTA_RAIZ + "/" + def + "/followers.ins";
            // Solo agregar si no los sigue ya
            if (!verificarEnArchivo(myFollowing, def)) {
                appendLine(myFollowing, def);
                appendLine(theirFollowers, usuarioActual.getUsername());
            }
        }
    }

    // ══════════════════════════════════════════════════════════════
    //  PROCESAMIENTO DE IMÁGENES
    // ══════════════════════════════════════════════════════════════
    public String procesarImagenPerfil(File original, String username, String nombreArchivo) {
        try {
            BufferedImage img = javax.imageio.ImageIO.read(original);
            if (img == null) {
                return null;
            }
            // Aplanar sobre blanco antes de recortar (evita canal alfa gris)
            BufferedImage flat = new BufferedImage(img.getWidth(), img.getHeight(), BufferedImage.TYPE_INT_RGB);
            Graphics2D gf = flat.createGraphics();
            gf.setColor(java.awt.Color.WHITE);
            gf.fillRect(0, 0, flat.getWidth(), flat.getHeight());
            gf.drawImage(img, 0, 0, null);
            gf.dispose();

            int ancho = flat.getWidth(), alto = flat.getHeight();
            int lado = Math.min(ancho, alto);
            int x = (ancho - lado) / 2, y = (alto - lado) / 2;
            BufferedImage recortada = flat.getSubimage(x, y, lado, lado);
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
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public String procesarYGuardarImagen(File original, String username, String nombreArchivo) {
        try {
            BufferedImage img = javax.imageio.ImageIO.read(original);
            if (img == null) {
                return null;
            }
            // Aplanar sobre blanco
            BufferedImage flat = new BufferedImage(img.getWidth(), img.getHeight(), BufferedImage.TYPE_INT_RGB);
            Graphics2D gf = flat.createGraphics();
            gf.setColor(java.awt.Color.WHITE);
            gf.fillRect(0, 0, flat.getWidth(), flat.getHeight());
            gf.drawImage(img, 0, 0, null);
            gf.dispose();

            int anchoFinal = 600;
            double ratio = (double) flat.getHeight() / flat.getWidth();
            int altoFinal = Math.min((int) (anchoFinal * ratio), 800);
            BufferedImage final_ = new BufferedImage(anchoFinal, altoFinal, BufferedImage.TYPE_INT_RGB);
            Graphics2D g = final_.createGraphics();
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g.drawImage(flat, 0, 0, anchoFinal, altoFinal, null);
            g.dispose();

            String ruta = RUTA_RAIZ + "/" + username + "/imagenes";
            new File(ruta).mkdirs();
            String rutaFinal = ruta + "/" + nombreArchivo + ".jpg";
            javax.imageio.ImageWriter writer = javax.imageio.ImageIO.getImageWritersByFormatName("jpg").next();
            javax.imageio.ImageWriteParam param = writer.getDefaultWriteParam();
            param.setCompressionMode(javax.imageio.ImageWriteParam.MODE_EXPLICIT);
            param.setCompressionQuality(0.95f);
            javax.imageio.stream.ImageOutputStream ios = javax.imageio.ImageIO.createImageOutputStream(new File(rutaFinal));
            writer.setOutput(ios);
            writer.write(null, new javax.imageio.IIOImage(final_, null, null), param);
            writer.dispose();
            ios.close();
            return rutaFinal;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // ══════════════════════════════════════════════════════════════
    //  HELPERS PRIVADOS
    // ══════════════════════════════════════════════════════════════
    private boolean verificarEnArchivo(String ruta, String texto) {
        File f = new File(ruta);
        if (!f.exists()) {
            return false;
        }
        try (Scanner sc = new Scanner(f)) {
            while (sc.hasNextLine()) {
                if (sc.nextLine().trim().equals(texto)) {
                    return true;
                }
            }
        } catch (Exception ignored) {
        }
        return false;
    }

    private void eliminarLineaDeArchivo(String ruta, String texto) {
        File f = new File(ruta);
        if (!f.exists()) {
            return;
        }
        ArrayList<String> lineas = new ArrayList<>();
        try (Scanner sc = new Scanner(f)) {
            while (sc.hasNextLine()) {
                String l = sc.nextLine();
                if (!l.trim().equals(texto) && !l.trim().startsWith(texto + "|")) {
                    lineas.add(l);
                }
            }
        } catch (Exception ignored) {
        }
        reescribirArchivo(ruta, lineas);
    }

    private boolean appendLine(String ruta, String linea) {
        try (FileWriter fw = new FileWriter(ruta, true)) {
            fw.write(linea + "\n");
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    private void leerLineas(String ruta, ArrayList<String> lista) {
        File f = new File(ruta);
        if (!f.exists()) {
            return;
        }
        try (Scanner sc = new Scanner(f)) {
            while (sc.hasNextLine()) {
                String l = sc.nextLine().trim();
                if (!l.isEmpty()) {
                    lista.add(l);
                }
            }
        } catch (Exception ignored) {
        }
    }

    private int contarLineas(String ruta) {
        File f = new File(ruta);
        if (!f.exists()) {
            return 0;
        }
        int count = 0;
        try (Scanner sc = new Scanner(f)) {
            while (sc.hasNextLine()) {
                sc.nextLine();
                count++;
            }
        } catch (Exception ignored) {
        }
        return count;
    }

    private void reescribirArchivo(String ruta, ArrayList<String> lineas) {
        try (FileWriter fw = new FileWriter(ruta)) {
            for (String l : lineas) {
                fw.write(l + "\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private boolean actualizarCampoUsuario(String username, int indice, String valor) {
        File archivo = new File(RUTA_USERS);
        ArrayList<String> lineas = new ArrayList<>();
        try (Scanner sc = new Scanner(archivo)) {
            while (sc.hasNextLine()) {
                String linea = sc.nextLine();
                if (linea.trim().isEmpty()) {
                    lineas.add(linea);
                    continue;
                }
                String[] d = linea.split("\\|");
                if (d.length > indice && d[0].equals(username)) {
                    d[indice] = valor;
                    linea = String.join("|", d);
                }
                lineas.add(linea);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        reescribirArchivo(RUTA_USERS, lineas);
        invalidarCache();
        return true;
    }

    // ══════════════════════════════════════════════════════════════
    //  ARCHIVOS BINARIOS — backup de publicaciones
    // ══════════════════════════════════════════════════════════════
    private void guardarPublicacionBinario(Publicacion p) {
        if (usuarioActual == null) {
            return;
        }
        String rutaBin = RUTA_RAIZ + "/" + usuarioActual.getUsername() + "/backup_posts.bin";
        ArrayList<Publicacion> lista = leerPublicacionesBinario(usuarioActual.getUsername());
        lista.add(p);
        try (ObjectOutputStream oos = new ObjectOutputStream(new java.io.FileOutputStream(rutaBin))) {
            oos.writeObject(lista);
        } catch (IOException e) {
            System.out.println("Aviso backup binario: " + e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    public ArrayList<Publicacion> leerPublicacionesBinario(String username) {
        String rutaBin = RUTA_RAIZ + "/" + username + "/backup_posts.bin";
        File f = new File(rutaBin);
        if (!f.exists()) {
            return new ArrayList<>();
        }
        try (ObjectInputStream ois = new ObjectInputStream(new java.io.FileInputStream(rutaBin))) {
            Object obj = ois.readObject();
            if (obj instanceof ArrayList) {
                return (ArrayList<Publicacion>) obj;
            }
        } catch (IOException e) {
            System.out.println("Backup binario no legible: " + e.getMessage());
        } catch (ClassNotFoundException e) {
            System.out.println("Clase no encontrada en binario.");
        }
        return new ArrayList<>();
    }

    public void marcarNotificacionesVistas() {
        if (usuarioActual == null) {
            return;
        }

        reescribirArchivo(
                RUTA_RAIZ + "/" + usuarioActual.getUsername() + "/notifications.ins",
                new ArrayList<>());
        reescribirArchivo(
                RUTA_RAIZ + "/" + usuarioActual.getUsername() + "/likes_notif.ins",
                new ArrayList<>());
    }
    
    public int getCantidadShares(String autorPost, String fechaPost) {
    // Los shares son mensajes tipo SHARE en los inboxes de todos
    // Contamos en el inbox del propio autor cuántos SHARE recibió con esa imagen
    int count = 0;
    File raiz = new File(RUTA_RAIZ);
    String[] carpetas = raiz.list();
    if (carpetas == null) return 0;
    // Buscar publicación para obtener ruta imagen
    String rutaImg = "";
    for (Publicacion p : getPublicacionesDeUsuario(autorPost)) {
        if (p.getFecha().toString().equals(fechaPost)) {
            rutaImg = p.getRutaImagen() != null ? p.getRutaImagen() : "";
            break;
        }
    }
    if (rutaImg.isEmpty()) return 0;
    final String ri = rutaImg;
    for (String carpeta : carpetas) {
        File inbox = new File(RUTA_RAIZ + "/" + carpeta + "/inbox.ins");
        if (!inbox.exists()) continue;
        try (Scanner sc = new Scanner(inbox)) {
            while (sc.hasNextLine()) {
                String linea = sc.nextLine();
                Mensaje m = Mensaje.fromFileString(linea);
                if (m != null && m.getContenido() != null
                        && m.getContenido().startsWith("SHARE|" + autorPost + "|" + ri)) {
                    count++;
                }
            }
        } catch (Exception ignored) {}
    }
    return count / 2; // cada share se guarda 2 veces (emisor + receptor)
}
    
    
}






