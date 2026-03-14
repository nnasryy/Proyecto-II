package instagram;

import enums.EstadoCuenta;
import enums.TipoCuenta;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Scanner;

public class Sistema {

    private final String RUTA_RAIZ = "INSTA_RAIZ";
    private final String RUTA_USERS = RUTA_RAIZ + "/users.ins";

    private Usuario usuarioActual;

    public Sistema() {
        verificarEstructura();
    }

    public Usuario getUsuarioActual() {
        return usuarioActual;
    }

    private void verificarEstructura() {
        File raiz = new File(RUTA_RAIZ);
        if (!raiz.exists()) {
            raiz.mkdir();
        }

        // Crear carpeta de stickers globales si no existe
        File stickersGlobal = new File(RUTA_RAIZ + "/stickers_globales");
        if (!stickersGlobal.exists()) {
            stickersGlobal.mkdirs();
        }
        
        // CREAR STICKERS POR DEFECTO DESDE RECURSOS
        crearStickersPorDefecto();

        File users = new File(RUTA_USERS);
        try {
            if (!users.exists()) {
                users.createNewFile();
            }
        } catch (IOException e) {
            System.out.println("Error crítico al crear archivos del sistema.");
            e.printStackTrace();
        }
    }
    
    // ---------------------------------------------------------
    // NUEVO: COPIAR STICKERS DESDE RECURSOS DEL PROYECTO
    // ---------------------------------------------------------
    private void crearStickersPorDefecto() {
        String rutaCarpeta = RUTA_RAIZ + "/stickers_globales";
        File carpeta = new File(rutaCarpeta);
        if (!carpeta.exists()) carpeta.mkdirs();

        // Estructura: { Nombre final en carpeta , Nombre archivo en /images }
        String[][] stickers = {
            {"feliz.png", "happy.png"},
            {"triste.png", "crying.png"},
            {"corazon.png", "hearteyes.png"},
            {"risa.png", "tearsofjoy.png"},
            {"aplauso.png", "thumbup.png"}
        };

        for (String[] par : stickers) {
            String nombreFinal = par[0];
            String nombreOriginal = par[1];
            
            File archivoDestino = new File(rutaCarpeta + "/" + nombreFinal);
            
            // Solo copiamos si no existe ya
            if (!archivoDestino.exists()) {
                try (InputStream is = getClass().getResourceAsStream("/images/" + nombreOriginal)) {
                    if (is != null) {
                        java.nio.file.Files.copy(is, archivoDestino.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                    } else {
                        System.err.println("No se encontró el recurso: /images/" + nombreOriginal + ". Asegúrate de agregar la imagen al paquete.");
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    // ---------------------------
    // REGISTRAR USUARIO
    // ---------------------------
    public boolean registrarUsuario(String username, String password, String nombreCompleto,
            char genero, int edad, String fotoPerfil, TipoCuenta tipoCuenta) {

        if (existeUsername(username)) {
            System.out.println("Error: El username ya existe.");
            return false;
        }

        EstadoCuenta estado = EstadoCuenta.ACTIVO;
        LocalDate fechaRegistro = LocalDate.now();

        Usuario nuevo = new Usuario(username, password, nombreCompleto, genero, edad,
                fotoPerfil, fechaRegistro, tipoCuenta, estado);

        try (FileWriter writer = new FileWriter(RUTA_USERS, true)) {
            writer.write(
                    nuevo.getUsername() + "|"
                    + nuevo.getPassword() + "|"
                    + nuevo.getNombreCompleto() + "|"
                    + nuevo.getGenero() + "|"
                    + nuevo.getEdad() + "|"
                    + nuevo.getFotoPerfil() + "|"
                    + nuevo.getFechaRegistro().format(DateTimeFormatter.ISO_LOCAL_DATE) + "|"
                    + nuevo.getTipoCuenta().name() + "|"
                    + nuevo.getEstadoCuenta().name() + "\n"
            );

            crearEstructuraUsuario(username);
            System.out.println("Usuario registrado correctamente.");
            return true;

        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    // ---------------------------
    // LOGIN
    // ---------------------------
    public boolean login(String username, String password) {
        try (Scanner sc = new Scanner(new File(RUTA_USERS))) {
            while (sc.hasNextLine()) {
                String linea = sc.nextLine();
                String[] datos = linea.split("\\|");

                if (datos[0].equals(username) && datos[1].equals(password)) {

                    EstadoCuenta estado = EstadoCuenta.valueOf(datos[8]);
                    if (estado == EstadoCuenta.DESACTIVADO) {
                        System.out.println("Esta cuenta está desactivada.");
                        return false;
                    }

                    String nombre = datos[2];
                    char genero = datos[3].charAt(0);
                    int edad = Integer.parseInt(datos[4]);
                    String foto = datos[5];
                    LocalDate fecha = LocalDate.parse(datos[6]);
                    TipoCuenta tipo = TipoCuenta.valueOf(datos[7]);

                    usuarioActual = new Usuario(username, password, nombre, genero, edad, foto, fecha, tipo, estado);
                    return true;
                }
            }
            return false;

        } catch (FileNotFoundException e) {
            System.out.println("Archivo de usuarios no encontrado.");
            return false;
        }
    }

    // ---------------------------
    // VERIFICAR USERNAME
    // ---------------------------
    public boolean existeUsername(String username) {
        try (Scanner sc = new Scanner(new File(RUTA_USERS))) {
            while (sc.hasNextLine()) {
                String linea = sc.nextLine();
                String[] datos = linea.split("\\|");
                if (datos[0].equals(username)) {
                    return true;
                }
            }
        } catch (FileNotFoundException e) {
            return false;
        }
        return false;
    }

    // -----------------------------
    // BUSCAR USUARIO
    // ------------------------
    public Usuario buscarUsuario(String username) {
        try (Scanner sc = new Scanner(new File(RUTA_USERS))) {
            while (sc.hasNextLine()) {
                String linea = sc.nextLine();
                String[] datos = linea.split("\\|");
                if (datos[0].equals(username)) {
                    String nombre = datos[2];
                    char genero = datos[3].charAt(0);
                    int edad = Integer.parseInt(datos[4]);
                    String foto = datos[5];
                    LocalDate fecha = LocalDate.parse(datos[6]);
                    TipoCuenta tipo = TipoCuenta.valueOf(datos[7]);
                    EstadoCuenta estado = EstadoCuenta.valueOf(datos[8]);

                    return new Usuario(username, datos[1], nombre, genero, edad, foto, fecha, tipo, estado);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    // ---------------------------
    // CREAR ESTRUCTURA DE ARCHIVOS POR USUARIO
    // ---------------------------
    private void crearEstructuraUsuario(String username) {
        String rutaUser = RUTA_RAIZ + "/" + username;
        File carpetaUsuario = new File(rutaUser);

        if (!carpetaUsuario.exists()) {
            carpetaUsuario.mkdir();
        }

        String[] archivos = {
            "followers.ins", "following.ins", "insta.ins", 
            "inbox.ins", "stickers.ins", "solicitudes.ins",
            "likes.ins", "comments.ins"
        };
        
        for (String archivo : archivos) {
            File f = new File(rutaUser + "/" + archivo);
            try {
                if (!f.exists()) {
                    f.createNewFile();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        new File(rutaUser + "/imagenes").mkdir();
        new File(rutaUser + "/stickers_personales").mkdir();
        new File(rutaUser + "/folders_personales").mkdir();
    }

    // ---------------------------
    // CREAR PUBLICACIÓN (CON VALIDACIÓN 220 CHARS)
    // ---------------------------
    public boolean crearPublicacion(String contenido, String rutaImagen, String hashtags, String menciones) {
        if (usuarioActual == null) {
            return false;
        }

        if (contenido.length() > 220) {
            System.out.println("Error: El contenido excede los 220 caracteres.");
            return false;
        }

        Publicacion nueva = new Publicacion(
                usuarioActual.getUsername(),
                contenido,
                rutaImagen,
                hashtags,
                menciones
        );

        String rutaInsta = RUTA_RAIZ + "/" + usuarioActual.getUsername() + "/insta.ins";

        try (FileWriter fw = new FileWriter(rutaInsta, true)) {
            fw.write(nueva.toFileString() + "\n");
            System.out.println("Publicación creada exitosamente.");
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    // ---------------------------
    // MÉTODO AUXILIAR: Verificar si existe texto en archivo
    // ---------------------------
    private boolean verificarEnArchivo(String rutaArchivo, String textoBuscar) {
        File archivo = new File(rutaArchivo);
        if (!archivo.exists()) {
            return false;
        }

        try (Scanner sc = new Scanner(archivo)) {
            while (sc.hasNextLine()) {
                if (sc.nextLine().trim().equals(textoBuscar)) {
                    return true;
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return false;
    }

    // ---------------------------
    // CERRAR SESIÓN
    // ---------------------------
    public void logout() {
        this.usuarioActual = null;
    }

    // ---------------------------
    // OBTENER TIMELINE (FEED)
    // ---------------------------
    public ArrayList<Publicacion> getTimeline() {
        ArrayList<Publicacion> timeline = new ArrayList<>();

        if (usuarioActual == null) {
            return timeline;
        }

        leerPublicacionesUsuario(usuarioActual.getUsername(), timeline);

        String rutaFollowing = RUTA_RAIZ + "/" + usuarioActual.getUsername() + "/following.ins";
        File fFollowing = new File(rutaFollowing);

        if (fFollowing.exists()) {
            try (Scanner sc = new Scanner(fFollowing)) {
                while (sc.hasNextLine()) {
                    String usernameSeguido = sc.nextLine().trim();
                    if (!usernameSeguido.isEmpty()) {
                        leerPublicacionesUsuario(usernameSeguido, timeline);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        timeline.sort(Comparator.comparing(Publicacion::getFecha)
                .thenComparing(Publicacion::getHora)
                .reversed());

        return timeline;
    }

    // ---------------------------
    // MÉTODO AUXILIAR LEER PUBLICACIONES
    // ---------------------------
    private void leerPublicacionesUsuario(String username, ArrayList<Publicacion> lista) {
        String rutaInsta = RUTA_RAIZ + "/" + username + "/insta.ins";
        File archivo = new File(rutaInsta);

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

    // ---------------------------
    // OBTENER PUBLICACIONES DE UN USUARIO ESPECÍFICO (CON PRIVACIDAD)
    // ---------------------------
    public ArrayList<Publicacion> getPublicacionesDeUsuario(String username) {
        ArrayList<Publicacion> lista = new ArrayList<>();
        
        Usuario userObjetivo = buscarUsuario(username);
        if (userObjetivo == null || userObjetivo.getEstadoCuenta() == EstadoCuenta.DESACTIVADO) {
            return lista;
        }

        if (userObjetivo.getTipoCuenta() == TipoCuenta.PRIVADA) {
            if (usuarioActual == null || !usuarioActual.getUsername().equals(username)) {
                String rutaFollowers = RUTA_RAIZ + "/" + username + "/followers.ins";
                if (usuarioActual == null || !verificarEnArchivo(rutaFollowers, usuarioActual.getUsername())) {
                    return lista;
                }
            }
        }

        String rutaInsta = RUTA_RAIZ + "/" + username + "/insta.ins";
        File archivo = new File(rutaInsta);

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

        lista.sort(Comparator.comparing(Publicacion::getFecha).thenComparing(Publicacion::getHora).reversed());
        return lista;
    }

    // ---------------------------
    // CONTAR LÍNEAS EN UN ARCHIVO
    // ---------------------------
    private int contarLineasArchivo(String ruta) {
        File archivo = new File(ruta);
        if (!archivo.exists()) {
            return 0;
        }

        int count = 0;
        try (Scanner sc = new Scanner(archivo)) {
            while (sc.hasNextLine()) {
                sc.nextLine();
                count++;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return count;
    }

    public int getCantidadFollowers(String username) {
        return contarLineasArchivo(RUTA_RAIZ + "/" + username + "/followers.ins");
    }

    public int getCantidadFollowing(String username) {
        return contarLineasArchivo(RUTA_RAIZ + "/" + username + "/following.ins");
    }

    public int getCantidadPosts(String username) {
        return contarLineasArchivo(RUTA_RAIZ + "/" + username + "/insta.ins");
    }

    public boolean yaLoSigo(String usernameObjetivo) {
        if (usuarioActual == null) {
            return false;
        }
        String rutaFollowing = RUTA_RAIZ + "/" + usuarioActual.getUsername() + "/following.ins";
        return verificarEnArchivo(rutaFollowing, usernameObjetivo);
    }

    // ---------------------------
    // BUSCAR USUARIOS
    // ---------------------------
    public ArrayList<Usuario> buscarUsuarios(String criterio) {
        ArrayList<Usuario> resultados = new ArrayList<>();
        if (criterio == null || criterio.isEmpty()) {
            return resultados;
        }

        try (Scanner sc = new Scanner(new File(RUTA_USERS))) {
            while (sc.hasNextLine()) {
                String linea = sc.nextLine();
                String[] datos = linea.split("\\|");

                String usernameArchivo = datos[0];
                EstadoCuenta estado = EstadoCuenta.valueOf(datos[8]);

                if (usernameArchivo.toLowerCase().contains(criterio.toLowerCase())
                        && !usernameArchivo.equals(usuarioActual.getUsername())
                        && estado == EstadoCuenta.ACTIVO) {

                    String nombre = datos[2];
                    char genero = datos[3].charAt(0);
                    int edad = Integer.parseInt(datos[4]);
                    String foto = datos[5];
                    LocalDate fecha = LocalDate.parse(datos[6]);
                    TipoCuenta tipo = TipoCuenta.valueOf(datos[7]);

                    Usuario u = new Usuario(usernameArchivo, "", nombre, genero, edad, foto, fecha, tipo, estado);
                    resultados.add(u);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return resultados;
    }

    // ---------------------------
    // INBOX: LÓGICA DE MENSAJERÍA
    // ---------------------------
    public boolean guardarMensaje(Mensaje m) {
        String rutaInboxReceptor = RUTA_RAIZ + "/" + m.getReceptor() + "/inbox.ins";
        try (FileWriter fw = new FileWriter(rutaInboxReceptor, true)) {
            fw.write(m.toFileString() + "\n");
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    public boolean puedeEnviarMensaje(String usernameReceptor) {
        Usuario receptor = buscarUsuario(usernameReceptor);
        if (receptor == null) {
            return false;
        }

        if (receptor.getTipoCuenta() == TipoCuenta.PUBLICA) {
            return true;
        }

        boolean yoLoSigo = verificarEnArchivo(RUTA_RAIZ + "/" + usuarioActual.getUsername() + "/following.ins", usernameReceptor);
        boolean elMeSigue = verificarEnArchivo(RUTA_RAIZ + "/" + usernameReceptor + "/followers.ins", usuarioActual.getUsername());

        return yoLoSigo && elMeSigue;
    }

    public boolean enviarMensaje(String receptorUsername, String contenido, String tipo) {
        if (usuarioActual == null) {
            return false;
        }
        
        if (contenido.length() > 300) {
            System.out.println("Error: El mensaje excede los 300 caracteres.");
            return false;
        }

        Mensaje nuevo;

        if ("STICKER".equals(tipo)) {
            nuevo = new MensajeSticker(usuarioActual.getUsername(), receptorUsername, contenido);
        } else {
            nuevo = new MensajeTexto(usuarioActual.getUsername(), receptorUsername, contenido);
        }

        guardarMensaje(nuevo);

        nuevo.setEstado("LEIDO"); 
        String rutaInboxMio = RUTA_RAIZ + "/" + usuarioActual.getUsername() + "/inbox.ins";
        try (FileWriter fw = new FileWriter(rutaInboxMio, true)) {
            fw.write(nuevo.toFileString() + "\n");
        } catch (IOException e) {
            e.printStackTrace();
        }

        return true;
    }

    public ArrayList<Mensaje> getConversacion(String otroUsuario) {
        ArrayList<Mensaje> conversacion = new ArrayList<>();
        if (usuarioActual == null) return conversacion;

        String rutaInbox = RUTA_RAIZ + "/" + usuarioActual.getUsername() + "/inbox.ins";
        File archivo = new File(rutaInbox);

        if (!archivo.exists()) {
            return conversacion;
        }

        try (Scanner sc = new Scanner(archivo)) {
            while (sc.hasNextLine()) {
                String linea = sc.nextLine();
                Mensaje m = Mensaje.fromFileString(linea);
                if (m != null) {
                    if ((m.getEmisor().equals(otroUsuario) && m.getReceptor().equals(usuarioActual.getUsername()))
                            || (m.getEmisor().equals(usuarioActual.getUsername()) && m.getReceptor().equals(otroUsuario))) {
                        conversacion.add(m);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return conversacion;
    }

    public void marcarComoLeido(String otroUsuario) {
        if (usuarioActual == null) return;

        String rutaInbox = RUTA_RAIZ + "/" + usuarioActual.getUsername() + "/inbox.ins";
        File archivo = new File(rutaInbox);
        if (!archivo.exists()) {
            return;
        }

        ArrayList<String> lineasActualizadas = new ArrayList<>();

        try (Scanner sc = new Scanner(archivo)) {
            while (sc.hasNextLine()) {
                String linea = sc.nextLine();
                Mensaje m = Mensaje.fromFileString(linea);
                if (m != null && m.getEmisor().equals(otroUsuario) && m.getEstado().equals("NO_LEIDO")) {
                    m.setEstado("LEIDO");
                    lineasActualizadas.add(m.toFileString());
                } else if (m != null) {
                    lineasActualizadas.add(linea);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        try (FileWriter fw = new FileWriter(rutaInbox, false)) {
            for (String l : lineasActualizadas) {
                fw.write(l + "\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public ArrayList<String> getChatsRecientes() {
        ArrayList<String> usuarios = new ArrayList<>();
        if(usuarioActual == null) return usuarios;
        
        String rutaInbox = RUTA_RAIZ + "/" + usuarioActual.getUsername() + "/inbox.ins";
        File archivo = new File(rutaInbox);

        if (!archivo.exists()) {
            return usuarios;
        }

        try (Scanner sc = new Scanner(archivo)) {
            while (sc.hasNextLine()) {
                Mensaje m = Mensaje.fromFileString(sc.nextLine());
                if (m != null) {
                    String otro = m.getEmisor().equals(usuarioActual.getUsername()) ? m.getReceptor() : m.getEmisor();
                    if (!usuarios.contains(otro)) {
                        usuarios.add(otro);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return usuarios;
    }

    // BUSCAR POR HASHTAG
    public ArrayList<Publicacion> buscarPorHashtag(String hashtag) {
        ArrayList<Publicacion> resultados = new ArrayList<>();
        if (hashtag == null || hashtag.isEmpty()) {
            return resultados;
        }

        if (!hashtag.startsWith("#")) {
            hashtag = "#" + hashtag;
        }

        File raiz = new File(RUTA_RAIZ);
        String[] users = raiz.list();
        if (users != null) {
            for (String u : users) {
                File f = new File(RUTA_RAIZ + "/" + u);
                if(f.isDirectory() && !u.equals("stickers_globales")){
                    ArrayList<Publicacion> posts = getPublicacionesDeUsuario(u);
                    for (Publicacion p : posts) {
                        if (p.getContenido() != null && p.getContenido().contains(hashtag)) {
                            resultados.add(p);
                        }
                    }
                }
            }
        }
        return resultados;
    }

    // ---------------------------------------------------------
    // GESTIÓN DE ESTADO DE CUENTA
    // ---------------------------------------------------------

    public void cambiarEstadoCuenta(EstadoCuenta nuevoEstado) {
        if (usuarioActual == null) {
            return;
        }

        usuarioActual.setEstadoCuenta(nuevoEstado);

        File archivo = new File(RUTA_USERS);
        ArrayList<String> lineasActualizadas = new ArrayList<>();

        try (Scanner sc = new Scanner(archivo)) {
            while (sc.hasNextLine()) {
                String linea = sc.nextLine();
                String[] datos = linea.split("\\|");

                if (datos.length >= 9 && datos[0].equals(usuarioActual.getUsername())) {
                    datos[8] = nuevoEstado.name();
                    linea = String.join("|", datos);
                }
                lineasActualizadas.add(linea);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        try (FileWriter fw = new FileWriter(RUTA_USERS)) {
            for (String l : lineasActualizadas) {
                fw.write(l + "\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void reactivarCuenta(String username) {
        File archivo = new File(RUTA_USERS);
        ArrayList<String> lineas = new ArrayList<>();

        try (Scanner sc = new Scanner(archivo)) {
            while (sc.hasNextLine()) {
                String linea = sc.nextLine();
                String[] datos = linea.split("\\|");

                if (datos.length >= 9 && datos[0].equals(username)) {
                    datos[8] = EstadoCuenta.ACTIVO.name();
                    linea = String.join("|", datos);
                }
                lineas.add(linea);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        try (FileWriter fw = new FileWriter(RUTA_USERS)) {
            for (String l : lineas) {
                fw.write(l + "\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // ---------------------------------------------------------
    // STICKERS
    // ---------------------------------------------------------
    public ArrayList<String> getStickersGlobales() {
        ArrayList<String> lista = new ArrayList<>();
        String rutaCarpeta = RUTA_RAIZ + "/stickers_globales";
        File carpeta = new File(rutaCarpeta);

        if (!carpeta.exists()) {
            carpeta.mkdirs();
        }

        String[] archivos = carpeta.list((dir, name)
                -> name.toLowerCase().endsWith(".png")
                || name.toLowerCase().endsWith(".jpg"));

        if (archivos != null) {
            for (String f : archivos) {
                lista.add(rutaCarpeta + "/" + f);
            }
        }
        return lista;
    }

    public boolean guardarStickerPersonal(File origen, String username) {
        try {
            String rutaCarpeta = RUTA_RAIZ + "/" + username + "/stickers_personales";
            File carpeta = new File(rutaCarpeta);
            if (!carpeta.exists()) carpeta.mkdirs();
            
            String nombre = "sticker_" + System.currentTimeMillis() + ".png";
            File destino = new File(rutaCarpeta + "/" + nombre);
            
            java.nio.file.Files.copy(origen.toPath(), destino.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public ArrayList<String> getTodosStickers(String username) {
        ArrayList<String> lista = new ArrayList<>();
        lista.addAll(getStickersGlobales());
        
        String rutaCarpeta = RUTA_RAIZ + "/" + username + "/stickers_personales";
        File carpeta = new File(rutaCarpeta);
        if (carpeta.exists()) {
            String[] archivos = carpeta.list((dir, name) -> 
                name.toLowerCase().endsWith(".png") || name.toLowerCase().endsWith(".jpg"));
            if (archivos != null) {
                for (String f : archivos) lista.add(rutaCarpeta + "/" + f);
            }
        }
        return lista;
    }

    // ---------------------------------------------------------
    // GESTIÓN DE SOLICITUDES Y FOLLOWS
    // ---------------------------------------------------------
    
    public boolean seguirUsuario(String usernameObjetivo) {
        if (usuarioActual == null) return false; 
        if (usernameObjetivo.equals(usuarioActual.getUsername())) return false; 

        Usuario objetivo = buscarUsuario(usernameObjetivo);
        
        if (objetivo.getTipoCuenta() == TipoCuenta.PUBLICA) {
            String miFollowingPath = RUTA_RAIZ + "/" + usuarioActual.getUsername() + "/following.ins";
            String suFollowersPath = RUTA_RAIZ + "/" + usernameObjetivo + "/followers.ins";

            if (verificarEnArchivo(miFollowingPath, usernameObjetivo)) return false;

            try (FileWriter fw = new FileWriter(miFollowingPath, true)) { fw.write(usernameObjetivo + "\n"); } catch (IOException e) {}
            try (FileWriter fw = new FileWriter(suFollowersPath, true)) { fw.write(usuarioActual.getUsername() + "\n"); } catch (IOException e) {}
            
            return true;
        } else {
            return enviarSolicitud(usernameObjetivo);
        }
    }

    public boolean dejarDeSeguir(String usernameObjetivo) {
        if (usuarioActual == null) return false;
        
        String miFollowingPath = RUTA_RAIZ + "/" + usuarioActual.getUsername() + "/following.ins";
        String suFollowersPath = RUTA_RAIZ + "/" + usernameObjetivo + "/followers.ins";

        eliminarLineaDeArchivo(miFollowingPath, usernameObjetivo);
        eliminarLineaDeArchivo(suFollowersPath, usuarioActual.getUsername());
        
        return true;
    }

    private boolean enviarSolicitud(String usernameObjetivo) {
        String rutaSolicitudes = RUTA_RAIZ + "/" + usernameObjetivo + "/solicitudes.ins";
        
        if (verificarEnArchivo(rutaSolicitudes, usuarioActual.getUsername())) return false;
        
        try (FileWriter fw = new FileWriter(rutaSolicitudes, true)) {
            fw.write(usuarioActual.getUsername() + "\n");
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    public void aceptarSolicitud(String usernameSolicitante) {
        String rutaSolicitudes = RUTA_RAIZ + "/" + usuarioActual.getUsername() + "/solicitudes.ins";
        String rutaFollowers = RUTA_RAIZ + "/" + usuarioActual.getUsername() + "/followers.ins";
        String rutaFollowingSolicitante = RUTA_RAIZ + "/" + usernameSolicitante + "/following.ins";

        try (FileWriter fw = new FileWriter(rutaFollowers, true)) { fw.write(usernameSolicitante + "\n"); } catch (IOException e) {}
        try (FileWriter fw = new FileWriter(rutaFollowingSolicitante, true)) { fw.write(usuarioActual.getUsername() + "\n"); } catch (IOException e) {}

        eliminarLineaDeArchivo(rutaSolicitudes, usernameSolicitante);
    }

    public void rechazarSolicitud(String usernameSolicitante) {
        String rutaSolicitudes = RUTA_RAIZ + "/" + usuarioActual.getUsername() + "/solicitudes.ins";
        eliminarLineaDeArchivo(rutaSolicitudes, usernameSolicitante);
    }

    public ArrayList<String> getSolicitudes() {
        ArrayList<String> lista = new ArrayList<>();
        if(usuarioActual == null) return lista;
        String ruta = RUTA_RAIZ + "/" + usuarioActual.getUsername() + "/solicitudes.ins";
        File f = new File(ruta);
        if(f.exists()){
            try (Scanner sc = new Scanner(f)) {
                while(sc.hasNextLine()) lista.add(sc.nextLine());
            } catch (Exception e) {}
        }
        return lista;
    }

    // ---------------------------------------------------------
    // GESTIÓN DE LIKES Y COMENTARIOS
    // ---------------------------------------------------------
    
    public boolean darLike(String autorPost, String fechaPost) {
        if (usuarioActual == null) return false;
        
        String rutaLikes = RUTA_RAIZ + "/" + autorPost + "/likes.ins";
        String lineaLike = autorPost + "|" + fechaPost + "|" + usuarioActual.getUsername();

        if (verificarEnArchivo(rutaLikes, lineaLike)) return false;

        try (FileWriter fw = new FileWriter(rutaLikes, true)) {
            fw.write(lineaLike + "\n");
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    public boolean yaDioLike(String autorPost, String fechaPost) {
        if (usuarioActual == null) return false;
        String rutaLikes = RUTA_RAIZ + "/" + autorPost + "/likes.ins";
        String idBusqueda = autorPost + "|" + fechaPost + "|" + usuarioActual.getUsername();
        return verificarEnArchivo(rutaLikes, idBusqueda);
    }

    public boolean toggleLike(String autorPost, String fechaPost) {
        if (usuarioActual == null) return false;
        
        String rutaLikes = RUTA_RAIZ + "/" + autorPost + "/likes.ins";
        String idLike = autorPost + "|" + fechaPost + "|" + usuarioActual.getUsername();

        if (verificarEnArchivo(rutaLikes, idLike)) {
            eliminarLineaDeArchivo(rutaLikes, idLike);
            return false;
        } else {
            try (FileWriter fw = new FileWriter(rutaLikes, true)) {
                fw.write(idLike + "\n");
                return true;
            } catch (IOException e) { return true; }
        }
    }

    public void agregarComentario(String autorPost, String fechaPost, String comentario) {
        if (usuarioActual == null) return;
        String rutaComments = RUTA_RAIZ + "/" + autorPost + "/comments.ins";
        String linea = fechaPost + "|" + usuarioActual.getUsername() + "|" + comentario;
        
        try (FileWriter fw = new FileWriter(rutaComments, true)) {
            fw.write(linea + "\n");
        } catch (IOException e) { e.printStackTrace(); }
    }

    public ArrayList<String> getComentarios(String autorPost, String fechaPost) {
        ArrayList<String> lista = new ArrayList<>();
        String rutaComments = RUTA_RAIZ + "/" + autorPost + "/comments.ins";
        File f = new File(rutaComments);
        if (!f.exists()) return lista;

        try (Scanner sc = new Scanner(f)) {
            while (sc.hasNextLine()) {
                String[] datos = sc.nextLine().split("\\|");
                if (datos.length >= 3 && datos[0].equals(fechaPost)) {
                    lista.add(datos[1] + ": " + datos[2]);
                }
            }
        } catch (Exception e) {}
        return lista;
    }
    
    public void compartirPost(String usernameDestino, String autorPost, String rutaImagen, String contenido) {
        String msg = "POST COMPARTIDO DE " + autorPost + ":\n" + contenido + "\n[Ver imagen: " + rutaImagen + "]";
        enviarMensaje(usernameDestino, msg, "TEXTO");
    }

    // ---------------------------------------------------------
    // MÉTODOS AUXILIARES ARCHIVOS
    // ---------------------------------------------------------
    
    private void eliminarLineaDeArchivo(String ruta, String texto) {
        File archivo = new File(ruta);
        ArrayList<String> lineas = new ArrayList<>();
        try (Scanner sc = new Scanner(archivo)) {
            while (sc.hasNextLine()) {
                String l = sc.nextLine();
                if (!l.trim().equals(texto)) lineas.add(l);
            }
        } catch (Exception e) {}
        try (FileWriter fw = new FileWriter(ruta)) {
            for (String l : lineas) fw.write(l + "\n");
        } catch (IOException e) {}
    }
    
    // ---------------------------------------------------------
    // PROCESAMIENTO DE IMÁGENES
    // ---------------------------------------------------------
    
    public String procesarYGuardarImagen(File original, String username, String nombreArchivo) {
        try {
            BufferedImage imgOriginal = javax.imageio.ImageIO.read(original);
            
            int anchoFinal = 600; 
            int altoFinal;
            
            double ratio = (double) imgOriginal.getHeight() / imgOriginal.getWidth();
            altoFinal = (int) (anchoFinal * ratio);
            
            if (altoFinal > 800) altoFinal = 800; 

            BufferedImage imgFinal = new BufferedImage(anchoFinal, altoFinal, BufferedImage.TYPE_INT_RGB);
            Graphics2D g2d = imgFinal.createGraphics();
            g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g2d.drawImage(imgOriginal, 0, 0, anchoFinal, altoFinal, null);
            g2d.dispose();
            
            String rutaCarpeta = RUTA_RAIZ + "/" + username + "/imagenes";
            new File(rutaCarpeta).mkdirs();
            String rutaFinal = rutaCarpeta + "/" + nombreArchivo + ".jpg";
            javax.imageio.ImageIO.write(imgFinal, "jpg", new File(rutaFinal));
            
            return rutaFinal;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    public void eliminarConversacion(String otroUsuario) {
        if(usuarioActual == null) return;
        String rutaInbox = RUTA_RAIZ + "/" + usuarioActual.getUsername() + "/inbox.ins";
        File archivo = new File(rutaInbox);
        if (!archivo.exists()) return;

        ArrayList<String> lineas = new ArrayList<>();
        
        try (Scanner sc = new Scanner(archivo)) {
            while (sc.hasNextLine()) {
                String linea = sc.nextLine();
                Mensaje m = Mensaje.fromFileString(linea);
                if (m != null && 
                   !( (m.getEmisor().equals(otroUsuario) && m.getReceptor().equals(usuarioActual.getUsername())) ||
                      (m.getEmisor().equals(usuarioActual.getUsername()) && m.getReceptor().equals(otroUsuario)) )) {
                    lineas.add(linea);
                }
            }
        } catch (Exception e) {} 
        
        try (FileWriter fw = new FileWriter(rutaInbox)) {
            for(String l : lineas) fw.write(l + "\n");
        } catch (IOException e) {}
    }
    
    public ArrayList<Publicacion> getMenciones() {
        if (usuarioActual == null) return new ArrayList<>();
        ArrayList<Publicacion> menciones = new ArrayList<>();
        
        String miUsername = usuarioActual.getUsername();
        File raiz = new File(RUTA_RAIZ);
        String[] carpetas = raiz.list();
        
        if (carpetas != null) {
            for (String userFolder : carpetas) {
                File f = new File(RUTA_RAIZ + "/" + userFolder);
                if (f.isDirectory() && !userFolder.equals("stickers_globales")) {
                    ArrayList<Publicacion> postsUsuario = getPublicacionesDeUsuario(userFolder);
                    for (Publicacion p : postsUsuario) {
                        if (p.getContenido() != null && p.getContenido().contains("@" + miUsername)) {
                            menciones.add(p);
                        }
                    }
                }
            }
        }
        return menciones;
    }

    public ArrayList<String> getListaFollowers(String username) {
        ArrayList<String> lista = new ArrayList<>();
        String ruta = RUTA_RAIZ + "/" + username + "/followers.ins";
        File f = new File(ruta);
        if (f.exists()) {
            try (Scanner sc = new Scanner(f)) {
                while (sc.hasNextLine()) lista.add(sc.nextLine());
            } catch (Exception e) {}
        }
        return lista;
    }

    public ArrayList<String> getListaFollowing(String username) {
        ArrayList<String> lista = new ArrayList<>();
        String ruta = RUTA_RAIZ + "/" + username + "/following.ins";
        File f = new File(ruta);
        if (f.exists()) {
            try (Scanner sc = new Scanner(f)) {
                while (sc.hasNextLine()) lista.add(sc.nextLine());
            } catch (Exception e) {}
        }
        return lista;
    }

    public boolean actualizarFotoPerfil(String username, String nuevaRuta) {
        File archivo = new File(RUTA_USERS);
        ArrayList<String> lineas = new ArrayList<>();
        
        try (Scanner sc = new Scanner(archivo)) {
            while (sc.hasNextLine()) {
                String linea = sc.nextLine();
                String[] datos = linea.split("\\|");
                if (datos[0].equals(username)) {
                    datos[5] = nuevaRuta;
                    linea = String.join("|", datos);
                    if (usuarioActual != null && usuarioActual.getUsername().equals(username)) {
                        usuarioActual.setFotoPerfil(nuevaRuta);
                    }
                }
                lineas.add(linea);
            }
        } catch (Exception e) { return false; }
        
        try (FileWriter fw = new FileWriter(RUTA_USERS)) {
            for (String l : lineas) fw.write(l + "\n");
        } catch (IOException e) { return false; }
        
        return true;
    }
    
    public ArrayList<String> getNotificacionesLikes() {
        ArrayList<String> notifs = new ArrayList<>();
        if(usuarioActual == null) return notifs;
        String ruta = RUTA_RAIZ + "/" + usuarioActual.getUsername() + "/likes.ins";
        File f = new File(ruta);
        if(f.exists()){
            try (Scanner sc = new Scanner(f)) {
                while(sc.hasNextLine()) {
                    notifs.add(sc.nextLine()); 
                }
            } catch (Exception e) {}
        }
        return notifs;
    }
}