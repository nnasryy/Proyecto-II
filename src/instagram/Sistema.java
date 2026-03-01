package instagram;

import enums.EstadoCuenta;
import enums.TipoCuenta;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
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
    // REGISTRAR USUARIO (Actualizado con todos los campos)
    // ---------------------------
    public boolean registrarUsuario(String username, String password, String nombreCompleto, 
                                    char genero, int edad, String fotoPerfil, TipoCuenta tipoCuenta) {

        if (existeUsername(username)) {
            System.out.println("Error: El username ya existe.");
            return false;
        }

        // Por defecto la cuenta se crea ACTIVA
        EstadoCuenta estado = EstadoCuenta.ACTIVO;
        LocalDate fechaRegistro = LocalDate.now();

        // Crear objeto usuario (nota: tu clase Usuario debe aceptar estos parámetros)
        Usuario nuevo = new Usuario(username, password, nombreCompleto, genero, edad, 
                                    fotoPerfil, fechaRegistro, tipoCuenta, estado);

        try (FileWriter writer = new FileWriter(RUTA_USERS, true)) {
            // Formato guardado: username|password|nombre|genero|edad|foto|fecha|tipo|estado
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
    // LOGIN (Actualizado para leer nuevos campos)
    // ---------------------------
    public boolean login(String username, String password) {
        try (Scanner sc = new Scanner(new File(RUTA_USERS))) {
            while (sc.hasNextLine()) {
                String linea = sc.nextLine();
                String[] datos = linea.split("\\|");

                // datos[0] = username, datos[1] = password
                if (datos[0].equals(username) && datos[1].equals(password)) {
                    
                    // Verificar estado de la cuenta
                    EstadoCuenta estado = EstadoCuenta.valueOf(datos[8]); // El último campo
                    if (estado == EstadoCuenta.DESACTIVADO) {
                        System.out.println("Esta cuenta está desactivada.");
                        return false;
                    }

                    // Reconstruir el objeto Usuario con todos los datos
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
    private boolean existeUsername(String username) {
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

    // ---------------------------
    // CREAR ESTRUCTURA DE ARCHIVOS POR USUARIO 
    // ---------------------------
    private void crearEstructuraUsuario(String username) {
        String rutaUser = RUTA_RAIZ + "/" + username;
        File carpetaUsuario = new File(rutaUser);
        
        if (!carpetaUsuario.exists()) {
            carpetaUsuario.mkdir();
        }

        // Crear archivos .ins vacíos
        String[] archivos = {"followers.ins", "following.ins", "insta.ins", "inbox.ins", "stickers.ins"};
        for (String archivo : archivos) {
            File f = new File(rutaUser + "/" + archivo);
            try {
                if (!f.exists()) f.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // Crear subcarpetas necesarias
        new File(rutaUser + "/imagenes").mkdir();
        new File(rutaUser + "/stickers_personales").mkdir();
        // folders_personales se puede crear si se necesita, el PDF lo menciona.
        new File(rutaUser + "/folders_personales").mkdir(); 
    }
        // ---------------------------
    // SEGUIR USUARIO 
    // ---------------------------
    public boolean seguirUsuario(String usernameObjetivo) {
        if (usuarioActual == null) return false; // No hay login
        if (usernameObjetivo.equals(usuarioActual.getUsername())) return false; // No se puede seguir a sí mismo

        // Rutas de archivos
        String miFollowingPath = RUTA_RAIZ + "/" + usuarioActual.getUsername() + "/following.ins";
        String suFollowersPath = RUTA_RAIZ + "/" + usernameObjetivo + "/followers.ins";

        // Validar si ya lo sigo
        if (verificarEnArchivo(miFollowingPath, usernameObjetivo)) {
            System.out.println("Ya sigues a este usuario."); //Se imprime
            return false;
        }

        // Escribir en mi archivo de following
        try (FileWriter fw = new FileWriter(miFollowingPath, true)) {
            fw.write(usernameObjetivo + "\n");
        } catch (IOException e) { e.printStackTrace(); }

        // Escribir en su archivo de followers
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
    
    
    
}