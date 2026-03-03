package instagram;

import enums.EstadoCuenta;
import enums.TipoCuenta;
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
                    nuevo.getUsername() + "|" +
                    nuevo.getPassword() + "|" +
                    nuevo.getNombreCompleto() + "|" +
                    nuevo.getGenero() + "|" +
                    nuevo.getEdad() + "|" +
                    nuevo.getFotoPerfil() + "|" +
                    nuevo.getFechaRegistro().format(DateTimeFormatter.ISO_LOCAL_DATE) + "|" +
                    nuevo.getTipoCuenta().name() + "|" +
                    nuevo.getEstadoCuenta().name() + "\n"
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
                    System.out.println("Bienvenido, " + nombre);
                    return true;
                }
            }
            System.out.println("Credenciales incorrectas.");
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
                // Reconstruir usuario básico para validación
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
                if (!f.exists()) f.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        new File(rutaUser + "/imagenes").mkdir();
        new File(rutaUser + "/stickers_personales").mkdir();
        new File(rutaUser + "/folders_personales").mkdir(); 
    }
    
    // ---------------------------
    // SEGUIR USUARIO 
    // ---------------------------
    public boolean seguirUsuario(String usernameObjetivo) {
        if (usuarioActual == null) return false; 
        if (usernameObjetivo.equals(usuarioActual.getUsername())) return false; 

        String miFollowingPath = RUTA_RAIZ + "/" + usuarioActual.getUsername() + "/following.ins";
        String suFollowersPath = RUTA_RAIZ + "/" + usernameObjetivo + "/followers.ins";

        if (verificarEnArchivo(miFollowingPath, usernameObjetivo)) {
            System.out.println("Ya sigues a este usuario.");
            return false;
        }

        try (FileWriter fw = new FileWriter(miFollowingPath, true)) {
            fw.write(usernameObjetivo + "\n");
        } catch (IOException e) { e.printStackTrace(); }

        try (FileWriter fw = new FileWriter(suFollowersPath, true)) {
            fw.write(usuarioActual.getUsername() + "\n");
        } catch (IOException e) { e.printStackTrace(); }

        System.out.println("Ahora sigues a " + usernameObjetivo);
        return true;
    }

    // ---------------------------
    // CREAR PUBLICACIÓN 
    // ---------------------------
    public boolean crearPublicacion(String contenido, String rutaImagen, String hashtags, String menciones) {
        if (usuarioActual == null) return false;

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
        if (!archivo.exists()) return false;

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

        if (usuarioActual == null) return timeline;

        // 1. Mis propias publicaciones
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
        
        if (!archivo.exists()) return;

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
}