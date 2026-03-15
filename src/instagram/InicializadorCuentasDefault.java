/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package instagram;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Scanner;

/**
 * InicializadorCuentasDefault
 *
 * Crea 3 cuentas predeterminadas (NASA, Starbucks, NatGeo) con sus imágenes
 * cargadas desde /images (classpath) y copiadas a INSTA_RAIZ al primer arranque.
 *
 * Temas cubiertos: Herencia (PublicacionFoto), Archivos texto, Archivos binarios (backup)
 */
public class InicializadorCuentasDefault {

    private static final String RUTA_RAIZ  = "INSTA_RAIZ";
    private static final String RUTA_USERS = RUTA_RAIZ + "/users.ins";

    // Usernames usados también en Sistema.java para el feed de nuevos usuarios
    public static final String[] USERNAMES_DEFAULT = {"nasa", "starbucks", "natgeo"};

    // ── Datos de cada cuenta ─────────────────────────────────────
    // { username, password, nombre completo, genero, edad }
    private static final String[][] CUENTAS = {
        {"nasa",      "nasa1234",      "NASA",               "M", "65"},
        {"starbucks", "starbucks1234", "Starbucks Coffee",   "F", "53"},
        {"natgeo",    "natgeo1234",    "National Geographic","M", "136"},
    };

    // ── Contenido de los posts de cada cuenta ───────────────────
    // Cada fila: { contenido (máx 220 chars), hashtags }
    private static final String[][][] POSTS = {
        // nasa — 10 posts
        {
            {"El universo tiene aproximadamente 13.8 mil millones de años. Cada estrella es una historia de luz viajando hacia ti. #espacio #nasa #universo",    "#espacio #nasa #universo"},
            {"La ISS orbita la Tierra a 400 km de altitud a 28.000 km/h. ¡Una puesta de sol cada 90 minutos! #ISS #nasa #orbita",                               "#ISS #nasa #orbita"},
            {"Marte tiene el volcán más grande del sistema solar: Olympus Mons, con 22 km de altura. Tres veces más alto que el Everest. #marte #nasa",          "#marte #nasa #planeta"},
            {"El sonido no viaja en el espacio porque no hay moléculas que lo transmitan. El cosmos es el silencio más profundo. #ciencia #nasa",                "#ciencia #espacio #nasa"},
            {"La Gran Mancha Roja de Júpiter es una tormenta activa desde hace más de 350 años. Tiene el tamaño de dos planetas Tierra. #jupiter #nasa",         "#jupiter #nasa #tormenta"},
            {"La sonda Voyager 1 fue lanzada en 1977 y hoy está a más de 23 mil millones de km de la Tierra. Aún nos envía señales. #voyager #nasa",             "#voyager #nasa #espacio"},
            {"La Luna se aleja de la Tierra 3.8 cm cada año. En millones de años los eclipses totales dejarán de existir. #luna #nasa #ciencia",                "#luna #nasa #ciencia"},
            {"Un día en Venus dura más que su año orbital. Rota tan lento que su día es más largo que su año completo. #venus #nasa #planeta",                   "#venus #nasa #planeta"},
            {"El agujero negro M87 tiene masa equivalente a 6.5 mil millones de soles. Fotografiado por primera vez en 2019. #nasa #espacio",                    "#agujeronegro #nasa #espacio"},
            {"Saturno flotaría en agua. Su densidad es menor que la del agua líquida. Sus anillos miden 270.000 km de diámetro. #saturno #nasa",                 "#saturno #nasa #anillos"},
        },
        // starbucks — 10 posts
        {
            {"Empieza tu mañana con el ritual perfecto. Un café bien preparado puede transformar todo tu día. ☕ #starbucks #cafe #morning",                      "#starbucks #cafe #morning"},
            {"El Pumpkin Spice Latte ha vuelto. Otoño llegó con su sabor favorito. ¿Ya pediste el tuyo? #starbucks #psl #otoño",                                 "#starbucks #psl #otoño"},
            {"Nuestros baristas preparan cada bebida con dedicación. Detrás de cada taza hay una historia de pasión por el café. #starbucks #barista",            "#starbucks #barista #artesanal"},
            {"El Frappuccino fue inventado en 1995 en Boston. Hoy es una de las bebidas más pedidas en el mundo entero. #starbucks #frappuccino",                "#starbucks #frappuccino #historia"},
            {"Café de origen único significa que cada grano viene de una sola región. El terroir del café es tan complejo como el del vino. #starbucks",         "#starbucks #cafe #origen"},
            {"El cold brew se prepara durante 20 horas en agua fría. Resultado: café suave, sin acidez, con sabor profundo. #starbucks #coldbrew",               "#starbucks #coldbrew #cafe"},
            {"Nuestra red de agricultores abarca más de 30 países. Cada sorbo conecta con manos que cuidan la tierra. #starbucks #sostenible",                   "#starbucks #sostenible #cafe"},
            {"El matcha llegó para quedarse. Verde, cremoso y lleno de antioxidantes. ¿Matcha latte o matcha frappuccino? #starbucks #matcha",                   "#starbucks #matcha #te"},
            {"El nombre Starbucks viene de la novela Moby Dick. Starbuck era el primer oficial del Pequod. Café y literatura. #starbucks #historia",             "#starbucks #historia #cafe"},
            {"Cada año diseñamos las tazas rojas de invierno. Son más que un vaso: son el inicio de la temporada navideña. #starbucks #navidad",                 "#starbucks #navidad #redcup"},
        },
        // natgeo — 10 posts
        {
            {"El 80% del océano aún no ha sido explorado. En sus profundidades pueden existir criaturas que nunca hemos visto. #natgeo #oceano",                  "#natgeo #oceano #exploracion"},
            {"La migración del ñu en el Serengeti es el espectáculo natural más grande de la Tierra. Dos millones de animales. #natgeo #africa",                 "#natgeo #africa #fauna"},
            {"Los pulpos tienen tres corazones y sangre azul. Son los maestros del camuflaje del reino animal. #natgeo #pulpo #oceano",                          "#natgeo #pulpo #oceano"},
            {"El Amazonas produce el 20% del oxígeno de la Tierra. Sus árboles son los pulmones del planeta. #natgeo #amazonia",                                 "#natgeo #amazonia #medioambiente"},
            {"Los elefantes se saludan entrelazando sus trompas. Tienen memoria perfecta y reconocen familiares después de años. #natgeo",                       "#natgeo #elefantes #africa"},
            {"El Monte Everest crece 4 mm cada año por presión tectónica. La Tierra está viva y en constante movimiento. #natgeo #everest",                      "#natgeo #everest #geologia"},
            {"Las ballenas jorobadas componen canciones que duran horas y se escuchan a miles de kilómetros. #natgeo #ballenas #oceano",                         "#natgeo #ballenas #oceano"},
            {"El Gran Cañón tardó 5 millones de años en formarse. El río Colorado esculpió 446 km de historia geológica. #natgeo",                               "#natgeo #canyon #geologia"},
            {"Los pingüinos emperador bucean hasta 500 metros y aguantan 22 minutos sin respirar. Maestros de la supervivencia. #natgeo",                        "#natgeo #pinguinos #antartida"},
            {"La Aurora Boreal ocurre cuando partículas solares chocan con la atmósfera. Un fenómeno que pintó cielos por millones de años. #natgeo",            "#natgeo #aurora #naturaleza"},
        },
    };

    // ── Recursos de imagen por cuenta ───────────────────────────
    // Fotos de perfil en /images
    private static final String[] FOTOS_PERFIL = {"nasapfp.png", "starbucks.png", "natgeo.png"};
    // Prefijo de cada imagen de post: nasa1.png … nasa10.png, etc.
    private static final String[] IMG_PREFIJOS = {"nasa", "starbucks", "natgeo"};

    // ════════════════════════════════════════════════════════════
    //  PUNTO DE ENTRADA — llamar desde Sistema.java constructor
    // ════════════════════════════════════════════════════════════
    public static void inicializar() {
        for (int i = 0; i < CUENTAS.length; i++) {
            if (!existeUsuario(CUENTAS[i][0])) {
                crearCuenta(i);
            }
        }
    }

    // ════════════════════════════════════════════════════════════
    //  CREACIÓN DE CUENTA
    // ════════════════════════════════════════════════════════════
    private static void crearCuenta(int idx) {
        String username = CUENTAS[idx][0];
        String password = CUENTAS[idx][1];
        String nombre   = CUENTAS[idx][2];
        char   genero   = CUENTAS[idx][3].charAt(0);
        int    edad     = Integer.parseInt(CUENTAS[idx][4]);

        // 1. Carpetas y archivos
        crearEstructura(username);

        // 2. Copiar foto de perfil desde classpath → INSTA_RAIZ/username/imagenes/
        String rutaFoto = copiarRecurso(
            "/images/" + FOTOS_PERFIL[idx],
            RUTA_RAIZ + "/" + username + "/imagenes/profile.png"
        );
        if (rutaFoto == null) rutaFoto = "";

        // 3. Escribir en users.ins
        String linea = username + "|" + password + "|" + nombre + "|" + genero + "|"
                + edad + "|" + rutaFoto + "|" + LocalDate.now() + "|PUBLICA|ACTIVO\n";
        try (FileWriter fw = new FileWriter(RUTA_USERS, true)) {
            fw.write(linea);
        } catch (IOException e) {
            System.out.println("Error registrando cuenta default '" + username + "': " + e.getMessage());
            return;
        }

        // 4. Crear los 10 posts
        crearPosts(idx, username);

        System.out.println("Cuenta default creada: @" + username);
    }

    // ════════════════════════════════════════════════════════════
    //  CREACIÓN DE POSTS
    // ════════════════════════════════════════════════════════════
    private static void crearPosts(int idx, String username) {
        String rutaInsta = RUTA_RAIZ + "/" + username + "/insta.ins";
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("HH:mm:ss");

        // Post 0 = más antiguo, post 9 = más reciente → feed ordenado correctamente
        LocalDate baseDate = LocalDate.now().minusDays(POSTS[idx].length);

        for (int j = 0; j < POSTS[idx].length; j++) {
            String contenido = POSTS[idx][j][0];
            String hashtags  = POSTS[idx][j][1];

            // Intentar primero .png, luego .jpg
            String nombreSinExt = IMG_PREFIJOS[idx] + (j + 1);
            String destBase     = RUTA_RAIZ + "/" + username + "/imagenes/" + nombreSinExt;

            String rutaImg = copiarRecurso("/images/" + nombreSinExt + ".png", destBase + ".png");
            if (rutaImg == null) {
                rutaImg = copiarRecurso("/images/" + nombreSinExt + ".jpg", destBase + ".jpg");
            }
            if (rutaImg == null) {
                System.out.println("Advertencia: no se encontró /images/" + nombreSinExt + " (.png/.jpg)");
                rutaImg = "";
            }

            LocalDate fecha = baseDate.plusDays(j);
            // Horas variadas para que el orden sea natural
            LocalTime hora  = LocalTime.of(8 + (j % 14), (j * 11) % 60, 0);

            // Formato: autor|fecha|hora|contenido|hashtags|menciones|rutaImagen|tipo
            String linea = username + "|" + fecha + "|" + hora.format(dtf) + "|"
                    + contenido + "|" + hashtags + "| |" + rutaImg + "|CUADRADA\n";

            try (FileWriter fw = new FileWriter(rutaInsta, true)) {
                fw.write(linea);
            } catch (IOException e) {
                System.out.println("Error guardando post default: " + e.getMessage());
            }
        }
    }

    // ════════════════════════════════════════════════════════════
    //  HELPERS
    // ════════════════════════════════════════════════════════════

    /**
     * Copia un recurso del classpath a una ruta absoluta del sistema de archivos.
     * Devuelve la ruta absoluta del archivo copiado, o null si el recurso no existe.
     */
    private static String copiarRecurso(String rutaClasspath, String rutaDestino) {
        try (InputStream is = InicializadorCuentasDefault.class.getResourceAsStream(rutaClasspath)) {
            if (is == null) return null;
            File dest = new File(rutaDestino);
            dest.getParentFile().mkdirs();
            java.nio.file.Files.copy(is, dest.toPath(),
                    java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            return dest.getAbsolutePath();
        } catch (IOException e) {
            System.out.println("Error copiando " + rutaClasspath + ": " + e.getMessage());
            return null;
        }
    }

    private static boolean existeUsuario(String username) {
        File f = new File(RUTA_USERS);
        if (!f.exists()) return false;
        try (Scanner sc = new Scanner(f)) {
            while (sc.hasNextLine()) {
                String[] d = sc.nextLine().split("\\|");
                if (d.length > 0 && d[0].equals(username)) return true;
            }
        } catch (Exception ignored) {}
        return false;
    }

    private static void crearEstructura(String username) {
        String base = RUTA_RAIZ + "/" + username;
        new File(base).mkdirs();
        String[] archivos = {
            "followers.ins", "following.ins", "insta.ins", "inbox.ins",
            "stickers.ins",  "solicitudes.ins", "likes.ins",
            "comments.ins",  "notifications.ins"
        };
        for (String archivo : archivos) {
            File f = new File(base + "/" + archivo);
            try { if (!f.exists()) f.createNewFile(); }
            catch (IOException ignored) {}
        }
        new File(base + "/imagenes").mkdirs();
        new File(base + "/stickers_personales").mkdirs();
        new File(base + "/folders_personales").mkdirs();
    }
}