package instagram;

import enums.EstadoCuenta;
import enums.TipoCuenta;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
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
    // VER SI USUARIO EXISTE 
    // ------------------------
    // Agregar en Sistema.java
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

        String[] archivos = {"followers.ins", "following.ins", "insta.ins", "inbox.ins", "stickers.ins"};
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
    // CREAR PUBLICACIÓN 
    // ---------------------------
    public boolean crearPublicacion(String contenido, String rutaImagen, String hashtags, String menciones) {
        if (usuarioActual == null) {
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
    // NUEVO: CERRAR SESIÓN
    // ---------------------------
    public void logout() {
        this.usuarioActual = null;
    }

    // ---------------------------
    // NUEVO: OBTENER TIMELINE (FEED)
    // ---------------------------
    public ArrayList<Publicacion> getTimeline() {
        ArrayList<Publicacion> timeline = new ArrayList<>();

        if (usuarioActual == null) {
            return timeline;
        }

        // 1. Mis publicaciones
        leerPublicacionesUsuario(usuarioActual.getUsername(), timeline);

        // 2. Publicaciones de quienes sigo
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

        // 3. Ordenar por fecha y hora (más reciente primero)
        timeline.sort(Comparator.comparing(Publicacion::getFecha)
                .thenComparing(Publicacion::getHora)
                .reversed());

        return timeline;
    }

    // ---------------------------
    // NUEVO: MÉTODO AUXILIAR LEER PUBLICACIONES
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
    // OBTENER PUBLICACIONES DE UN USUARIO ESPECÍFICO
    // ---------------------------

    public ArrayList<Publicacion> getPublicacionesDeUsuario(String username) {
        ArrayList<Publicacion> lista = new ArrayList<>();
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
    // NUEVO: CONTAR LÍNEAS EN UN ARCHIVO (Followers/Following)
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
        return getPublicacionesDeUsuario(username).size();
    }

    // Método para verificar si ya sigo a alguien
    public boolean yaLoSigo(String usernameObjetivo) {
        if (usuarioActual == null) {
            return false;
        }
        String rutaFollowing = RUTA_RAIZ + "/" + usuarioActual.getUsername() + "/following.ins";
        return verificarEnArchivo(rutaFollowing, usernameObjetivo);
    }
    // ---------------------------
    // NUEVO: BUSCAR USUARIOS
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
    // Método genérico para guardar cualquier tipo de mensaje
    public boolean guardarMensaje(Mensaje m) {
        String rutaInboxReceptor = RUTA_RAIZ + "/" + m.getReceptor() + "/inbox.ins";
        try (FileWriter fw = new FileWriter(rutaInboxReceptor, true)) {
            // Polimorfismo: toFileString() hace lo correcto según sea Texto o Sticker
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

        Mensaje nuevo;

        // Fábrica de mensajes
        if ("STICKER".equals(tipo)) {
            nuevo = new MensajeSticker(usuarioActual.getUsername(), receptorUsername, contenido);
        } else {
            nuevo = new MensajeTexto(usuarioActual.getUsername(), receptorUsername, contenido);
        }

        // Guardar en el receptor
        guardarMensaje(nuevo);

        // Guardar en mi propio inbox (copia enviada)
        // Forzamos estado LEIDO para mí mismo
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
        String rutaInbox = RUTA_RAIZ + "/" + usuarioActual.getUsername() + "/inbox.ins";
        File archivo = new File(rutaInbox);

        if (!archivo.exists()) {
            return conversacion;
        }

        try (Scanner sc = new Scanner(archivo)) {
            while (sc.hasNextLine()) {
                String linea = sc.nextLine();
                // Usamos el método estático de la clase padre para decodificar
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

    public ArrayList<String> getChatsRecientes() {
        ArrayList<String> usuarios = new ArrayList<>();
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

    public void marcarComoLeido(String otroUsuario) {
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
                } else {
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

//Buscar por hashtag 
    // Añadir en Sistema.java
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
                // Reutilizamos métodos existentes
                ArrayList<Publicacion> posts = getPublicacionesDeUsuario(u);
                for (Publicacion p : posts) {
                    if (p.getContenido() != null && p.getContenido().contains(hashtag)) {
                        resultados.add(p);
                    }
                }
            }
        }
        return resultados;
    }
    // ---------------------------------------------------------
    // GESTIÓN DE ESTADO DE CUENTA
    // ---------------------------------------------------------

    // 1. Usado en Perfil (Usuario logueado) para Desactivar
    public void cambiarEstadoCuenta(EstadoCuenta nuevoEstado) {
        if (usuarioActual == null) {
            return;
        }

        // Actualizar en memoria
        usuarioActual.setEstadoCuenta(nuevoEstado);

        // Actualizar en el archivo users.ins
        File archivo = new File(RUTA_USERS);
        ArrayList<String> lineasActualizadas = new ArrayList<>();

        try (Scanner sc = new Scanner(archivo)) {
            while (sc.hasNextLine()) {
                String linea = sc.nextLine();
                String[] datos = linea.split("\\|");

                // Validar que la línea tenga el formato correcto (al menos 9 columnas)
                if (datos.length >= 9 && datos[0].equals(usuarioActual.getUsername())) {
                    datos[8] = nuevoEstado.name(); // ACTIVO o DESACTIVADO
                    linea = String.join("|", datos);
                }
                lineasActualizadas.add(linea);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Reescribir el archivo
        try (FileWriter fw = new FileWriter(RUTA_USERS)) {
            for (String l : lineasActualizadas) {
                fw.write(l + "\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 2. Usado en Login (Usuario fuera) para Reactivar
    public void reactivarCuenta(String username) {
        File archivo = new File(RUTA_USERS);
        ArrayList<String> lineas = new ArrayList<>();

        try (Scanner sc = new Scanner(archivo)) {
            while (sc.hasNextLine()) {
                String linea = sc.nextLine();
                String[] datos = linea.split("\\|");

                // Validar formato y buscar usuario
                if (datos.length >= 9 && datos[0].equals(username)) {
                    datos[8] = EstadoCuenta.ACTIVO.name(); // Forzar ACTIVO
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

        // Si la carpeta no existe, la creamos para que no de error
        if (!carpeta.exists()) {
            carpeta.mkdirs();
        }

        // Listamos archivos png o jpg
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
    // GUARDAR STICKER PERSONAL (Copiar archivo a carpeta del usuario)
    public boolean guardarStickerPersonal(File origen, String username) {
        try {
            String rutaCarpeta = RUTA_RAIZ + "/" + username + "/stickers_personales";
            File carpeta = new File(rutaCarpeta);
            if (!carpeta.exists()) carpeta.mkdirs();
            
            // Nombre único para no sobrescribir
            String nombre = "sticker_" + System.currentTimeMillis() + ".png";
            File destino = new File(rutaCarpeta + "/" + nombre);
            
            // Copiar archivo
            java.nio.file.Files.copy(origen.toPath(), destino.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    // OBTENER TODOS LOS STICKERS (Globales + Personales)
    public ArrayList<String> getTodosStickers(String username) {
        ArrayList<String> lista = new ArrayList<>();
        
        // 1. Globales
        lista.addAll(getStickersGlobales());
        
        // 2. Personales
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
    // GESTIÓN DE SOLICITUDES (CUENTAS PRIVADAS)
    // ---------------------------------------------------------
    
    // Método modificar "seguirUsuario" para que distinga entre público y privado
    // NOTA: Debes reemplazar tu método seguirUsuario actual con este.
    public boolean seguirUsuario(String usernameObjetivo) {
        if (usuarioActual == null) return false; 
        if (usernameObjetivo.equals(usuarioActual.getUsername())) return false; 

        Usuario objetivo = buscarUsuario(usernameObjetivo);
        
        // Si el perfil es PÚBLICO -> Sigue directo (tu lógica anterior)
        if (objetivo.getTipoCuenta() == TipoCuenta.PUBLICA) {
            String miFollowingPath = RUTA_RAIZ + "/" + usuarioActual.getUsername() + "/following.ins";
            String suFollowersPath = RUTA_RAIZ + "/" + usernameObjetivo + "/followers.ins";

            if (verificarEnArchivo(miFollowingPath, usernameObjetivo)) return false; // Ya lo sigo

            try (FileWriter fw = new FileWriter(miFollowingPath, true)) { fw.write(usernameObjetivo + "\n"); } catch (IOException e) {}
            try (FileWriter fw = new FileWriter(suFollowersPath, true)) { fw.write(usuarioActual.getUsername() + "\n"); } catch (IOException e) {}
            
            return true;
        } 
        // Si el perfil es PRIVADO -> Envía solicitud
        else {
            return enviarSolicitud(usernameObjetivo);
        }
    }

    private boolean enviarSolicitud(String usernameObjetivo) {
        String rutaSolicitudes = RUTA_RAIZ + "/" + usernameObjetivo + "/solicitudes.ins";
        
        // Verificar si ya envié solicitud
        if (verificarEnArchivo(rutaSolicitudes, usuarioActual.getUsername())) return false;
        
        try (FileWriter fw = new FileWriter(rutaSolicitudes, true)) {
            fw.write(usuarioActual.getUsername() + "\n");
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    // Aceptar solicitud (mueve de solicitudes.ins a followers.ins)
    public void aceptarSolicitud(String usernameSolicitante) {
        String rutaSolicitudes = RUTA_RAIZ + "/" + usuarioActual.getUsername() + "/solicitudes.ins";
        String rutaFollowers = RUTA_RAIZ + "/" + usuarioActual.getUsername() + "/followers.ins";
        String rutaFollowingSolicitante = RUTA_RAIZ + "/" + usernameSolicitante + "/following.ins";

        // 1. Agregar a mis followers
        try (FileWriter fw = new FileWriter(rutaFollowers, true)) { fw.write(usernameSolicitante + "\n"); } catch (IOException e) {}

        // 2. Agregarme a su following
        try (FileWriter fw = new FileWriter(rutaFollowingSolicitante, true)) { fw.write(usuarioActual.getUsername() + "\n"); } catch (IOException e) {}

        // 3. Eliminar solicitud
        eliminarLineaDeArchivo(rutaSolicitudes, usernameSolicitante);
    }

    // Rechazar solicitud
    public void rechazarSolicitud(String usernameSolicitante) {
        String rutaSolicitudes = RUTA_RAIZ + "/" + usuarioActual.getUsername() + "/solicitudes.ins";
        eliminarLineaDeArchivo(rutaSolicitudes, usernameSolicitante);
    }

    // Obtener lista de solicitudes pendientes
    public ArrayList<String> getSolicitudes() {
        ArrayList<String> lista = new ArrayList<>();
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
    // GESTIÓN DE LIKES (PARA NOTIFICACIONES)
    // ---------------------------------------------------------
    
    // Dar like a una publicación (autorPost + fechaPost identifican el post)
    public boolean darLike(String autorPost, String fechaPost) {
        if (usuarioActual == null) return false;
        
        String rutaLikes = RUTA_RAIZ + "/" + autorPost + "/likes.ins";
        String idPost = autorPost + "|" + fechaPost; // ID único del post
        String lineaLike = idPost + "|" + usuarioActual.getUsername();

        // Evitar likes duplicados
        if (verificarEnArchivo(rutaLikes, lineaLike)) return false;

        try (FileWriter fw = new FileWriter(rutaLikes, true)) {
            fw.write(lineaLike + "\n");
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    // Obtener notificaciones de likes
    public ArrayList<String> getNotificacionesLikes() {
        ArrayList<String> notifs = new ArrayList<>();
        String ruta = RUTA_RAIZ + "/" + usuarioActual.getUsername() + "/likes.ins";
        File f = new File(ruta);
        if(f.exists()){
            try (Scanner sc = new Scanner(f)) {
                while(sc.hasNextLine()) {
                    // Formato: autorPost|fechaPost|usernameQueDioLike
                    // Como soy el autor, el archivo tiene: miUsername|fechaPost|otroUsername
                    notifs.add(sc.nextLine()); 
                }
            } catch (Exception e) {}
        }
        return notifs;
    }

    // Método auxiliar para eliminar líneas
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
    
        // --- MÉTODOS PARA INTERACCIONES EN PUBLICACIONES ---

    // Verificar si ya di like (para pintar el botón al cargar)
    public boolean yaDioLike(String autorPost, String fechaPost) {
        if (usuarioActual == null) return false;
        String rutaLikes = RUTA_RAIZ + "/" + autorPost + "/likes.ins";
        String idBusqueda = autorPost + "|" + fechaPost + "|" + usuarioActual.getUsername();
        return verificarEnArchivo(rutaLikes, idBusqueda);
    }

    // Toggle Like (Dar o Quitar)
    // Retorna true si finalmente tiene like, false si no
    public boolean toggleLike(String autorPost, String fechaPost) {
        if (usuarioActual == null) return false;
        
        String rutaLikes = RUTA_RAIZ + "/" + autorPost + "/likes.ins";
        String idLike = autorPost + "|" + fechaPost + "|" + usuarioActual.getUsername();

        // Si ya existe, lo quitamos
        if (verificarEnArchivo(rutaLikes, idLike)) {
            eliminarLineaDeArchivo(rutaLikes, idLike);
            return false; // Indica que ahora NO tiene like
        } else {
            // Si no existe, lo damos
            try (FileWriter fw = new FileWriter(rutaLikes, true)) {
                fw.write(idLike + "\n");
                return true; // Indica que ahora SÍ tiene like
            } catch (IOException e) { return true; }
        }
    }

    // Agregar Comentario
    public void agregarComentario(String autorPost, String fechaPost, String comentario) {
        if (usuarioActual == null) return;
        String rutaComments = RUTA_RAIZ + "/" + autorPost + "/comments.ins";
        String linea = fechaPost + "|" + usuarioActual.getUsername() + "|" + comentario;
        
        try (FileWriter fw = new FileWriter(rutaComments, true)) {
            fw.write(linea + "\n");
        } catch (IOException e) { e.printStackTrace(); }
    }

    // Obtener Comentarios de un post específico
    public ArrayList<String> getComentarios(String autorPost, String fechaPost) {
        ArrayList<String> lista = new ArrayList<>();
        String rutaComments = RUTA_RAIZ + "/" + autorPost + "/comments.ins";
        File f = new File(rutaComments);
        if (!f.exists()) return lista;

        try (Scanner sc = new Scanner(f)) {
            while (sc.hasNextLine()) {
                String[] datos = sc.nextLine().split("\\|");
                // Formato: fecha|usuario|comentario
                if (datos.length >= 3 && datos[0].equals(fechaPost)) {
                    lista.add(datos[1] + ": " + datos[2]);
                }
            }
        } catch (Exception e) {}
        return lista;
    }
    
    // Compartir post (Envía mensaje con los datos)
    public void compartirPost(String usernameDestino, String autorPost, String rutaImagen, String contenido) {
        String msg = "POST COMPARTIDO DE " + autorPost + ":\n" + contenido + "\n[Ver imagen: " + rutaImagen + "]";
        enviarMensaje(usernameDestino, msg, "TEXTO");
    }
    
        // Método para recortar y guardar imagen en 600x600 (Desktop)
    public String procesarYGuardarImagen(File original, String username, String nombreArchivo) {
        try {
            BufferedImage imgOriginal = javax.imageio.ImageIO.read(original);
            int width = 600; // Resolución Desktop obligatoria
            int height = 600; 
            
            BufferedImage imgFinal = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            Graphics2D g2d = imgFinal.createGraphics();
            
            // Calcular recorte central (Center Crop)
            double ratioOriginal = (double) imgOriginal.getWidth() / imgOriginal.getHeight();
            int srcX, srcY, srcW, srcH;
            
            if (ratioOriginal > 1) { 
                // Horizontal: recortamos lados
                srcH = imgOriginal.getHeight();
                srcW = (int) (srcH * 1.0);
                srcX = (imgOriginal.getWidth() - srcW) / 2;
                srcY = 0;
            } else { 
                // Vertical: recortamos arriba/abajo
                srcW = imgOriginal.getWidth();
                srcH = (int) (srcW / 1.0);
                srcX = 0;
                srcY = (imgOriginal.getHeight() - srcH) / 2;
            }
            
            g2d.drawImage(imgOriginal, 0, 0, width, height, srcX, srcY, srcX + srcW, srcY + srcH, null);
            g2d.dispose();
            
            // Guardar
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
        String rutaInbox = RUTA_RAIZ + "/" + usuarioActual.getUsername() + "/inbox.ins";
        File archivo = new File(rutaInbox);
        if (!archivo.exists()) return;

        ArrayList<String> lineas = new ArrayList<>();
        
        try (Scanner sc = new Scanner(archivo)) {
            while (sc.hasNextLine()) {
                String linea = sc.nextLine();
                Mensaje m = Mensaje.fromFileString(linea);
                // Si no pertenece a esta conversación, la guardamos
                if (m != null && 
                   !( (m.getEmisor().equals(otroUsuario) && m.getReceptor().equals(usuarioActual.getUsername())) ||
                      (m.getEmisor().equals(usuarioActual.getUsername()) && m.getReceptor().equals(otroUsuario)) )) {
                    lineas.add(linea);
                }
            }
        } catch (Exception e) {} 
        
        // Reescribir
        try (FileWriter fw = new FileWriter(rutaInbox)) {
            for(String l : lineas) fw.write(l + "\n");
        } catch (IOException e) {}
    }
    
        // MÉTODO PARA OBTENER MENCIONES (Punto 10)
    public ArrayList<Publicacion> getMenciones() {
        if (usuarioActual == null) return new ArrayList<>();
        ArrayList<Publicacion> menciones = new ArrayList<>();
        
        String miUsername = usuarioActual.getUsername();
        File raiz = new File(RUTA_RAIZ);
        String[] carpetas = raiz.list();
        
        if (carpetas != null) {
            for (String userFolder : carpetas) {
                File f = new File(RUTA_RAIZ + "/" + userFolder);
                if (f.isDirectory()) {
                    // Reutilizamos el método existente que lee el archivo insta.ins
                    ArrayList<Publicacion> postsUsuario = getPublicacionesDeUsuario(userFolder);
                    for (Publicacion p : postsUsuario) {
                        // Verificamos si el contenido contiene @miUsername
                        if (p.getContenido() != null && p.getContenido().contains("@" + miUsername)) {
                            menciones.add(p);
                        }
                    }
                }
            }
        }
        return menciones;
    }
    
    
    
    
}
