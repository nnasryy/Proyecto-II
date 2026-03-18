package instagram;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;

public class InicializadorCuentasDefault {

    private static final String RUTA_RAIZ  = "INSTA_RAIZ";
    private static final String RUTA_USERS = RUTA_RAIZ + "/users.ins";

    public static final String[] USERNAMES_DEFAULT = {"nasa", "starbucks", "natgeo"};

    public static final Set<String> VERIFICADOS = new HashSet<>(
        Arrays.asList("nasa", "starbucks", "natgeo")
    );

    private static final String[][] CUENTAS = {
        {"nasa",      "nasa1234",      "NASA",                "M", "65"},
        {"starbucks", "starbucks1234", "Starbucks Coffee",    "F", "53"},
        {"natgeo",    "natgeo1234",    "National Geographic", "M", "136"},
    };
private static final String[][][] POSTS = {
    // nasa — 10 posts
    {
        {"La Luna en 1969. #nasa #historia", "#nasa #historia #espacio"},
        {"$3 por cada dolar invertido. #nasa #ciencia", "#nasa #ciencia #datos"},
        {"Mas de 1,000 misiones lanzadas. #nasa #espacio", "#nasa #espacio #misiones"},
        {"El traje espacial cuesta $12 millones. #nasa", "#nasa #tecnologia #espacio"},
        {"Oxigeno producido en Marte. #nasa #marte", "#nasa #marte #tecnologia"},
        {"Mas de 2,000 patentes tecnologicas. #nasa", "#nasa #innovacion #ciencia"},
        {"Control de Misiones: 24/7 desde 1965. #nasa", "#nasa #mision #espacio"},
        {"18,000 personas trabajan en NASA. #nasa", "#nasa #ciencia #careers"},
        {"James Webb ve 13,600 millones de anos luz. #nasa", "#nasa #webb #universo"},
        {"Humanos en Marte antes del 2040. #nasa #futuro", "#nasa #marte #futuro"},
    },
    // starbucks — 10 posts
    {
        {"Seattle 1971, el inicio de todo. #starbucks", "#starbucks #historia #cafe"},
        {"El nombre viene de Moby Dick. #starbucks", "#starbucks #historia #curiosidad"},
        {"3% del cafe mundial es nuestro. #starbucks", "#starbucks #cafe #datos"},
        {"El Frappuccino nacio en Boston 1995. #starbucks", "#starbucks #frappuccino #historia"},
        {"170,000 combinaciones posibles. #starbucks", "#starbucks #cafe #personaliza"},
        {"Cold brew: 20 horas de preparacion. #starbucks", "#starbucks #coldbrew #cafe"},
        {"Seguro medico para empleados desde 1988. #starbucks", "#starbucks #historia #empresa"},
        {"La sirena: figura mitologica griega. #starbucks", "#starbucks #logo #historia"},
        {"30 millones en Starbucks Rewards. #starbucks", "#starbucks #rewards #datos"},
        {"Las tazas rojas anuncian la Navidad. #starbucks", "#starbucks #navidad #redcup"},
    },
    // natgeo — 10 posts
    {
        {"Fundada en 1888, ciencia sin fronteras. #natgeo", "#natgeo #historia #ciencia"},
        {"Cubierta amarilla iconica desde 1910. #natgeo", "#natgeo #revista #historia"},
        {"14,000 proyectos cientificos financiados. #natgeo", "#natgeo #ciencia #exploracion"},
        {"Primera foto bajo el agua en 1926. #natgeo", "#natgeo #foto #historia"},
        {"3 millones de miembros activos. #natgeo", "#natgeo #comunidad #ciencia"},
        {"440 millones de hogares nos ven. #natgeo", "#natgeo #television #datos"},
        {"400 exploradores activos en el mundo. #natgeo", "#natgeo #exploradores #ciencia"},
        {"La nina afgana de McCurry, 1985. #natgeo #foto", "#natgeo #foto #historia"},
        {"Contenido en 45 idiomas diferentes. #natgeo", "#natgeo #educacion #idiomas"},
        {"Disney adquirio NatGeo en 2019. #natgeo", "#natgeo #historia #empresa"},
    },
};
    private static final String[] FOTOS_PERFIL = {"nasapfp.png", "starbuckspfp.png", "natgeopfp.png"};
    private static final String[] IMG_PREFIJOS  = {"nasa", "starbucks", "natgeo"};

   public static void inicializar() {
    for (int i = 0; i < CUENTAS.length; i++) {
        if (!existeUsuario(CUENTAS[i][0])) {
            crearCuenta(i);
        }
    }
    // Seguirse mutuamente después de que todas existen
    for (String username : USERNAMES_DEFAULT) {
        for (String otro : USERNAMES_DEFAULT) {
            if (!otro.equals(username)) {
                String myFollowing = RUTA_RAIZ + "/" + username + "/following.ins";
                String theirFollowers = RUTA_RAIZ + "/" + otro + "/followers.ins";
                if (!contieneLinea(myFollowing, otro)) {
                    appendLineStatic(myFollowing, otro);
                }
                if (!contieneLinea(theirFollowers, username)) {
                    appendLineStatic(theirFollowers, username);
                }
            }
        }
    }
}

private static boolean contieneLinea(String ruta, String texto) {
    File f = new File(ruta);
    if (!f.exists()) return false;
    try (Scanner sc = new Scanner(f)) {
        while (sc.hasNextLine()) {
            if (sc.nextLine().trim().equals(texto)) return true;
        }
    } catch (Exception ignored) {}
    return false;
}

    private static void crearCuenta(int idx) {
        String username = CUENTAS[idx][0];
        String password = CUENTAS[idx][1];
        String nombre   = CUENTAS[idx][2];
        char   genero   = CUENTAS[idx][3].charAt(0);
        int    edad     = Integer.parseInt(CUENTAS[idx][4]);

        crearEstructura(username);

        String rutaFoto = copiarRecurso(
            "/images/" + FOTOS_PERFIL[idx],
            RUTA_RAIZ + "/" + username + "/imagenes/profile.png"
        );
        if (rutaFoto == null) rutaFoto = "";

        String linea = username + "|" + password + "|" + nombre + "|" + genero + "|"
                + edad + "|" + rutaFoto + "|" + LocalDate.now() + "|PUBLICA|ACTIVO\n";
        try (FileWriter fw = new FileWriter(RUTA_USERS, true)) {
            fw.write(linea);
        } catch (IOException e) {
            System.out.println("Error registrando cuenta default '" + username + "': " + e.getMessage());
            return;
        }

        crearPosts(idx, username);

        for (String otro : USERNAMES_DEFAULT) {
            if (!otro.equals(username)) {
                appendLineStatic(RUTA_RAIZ + "/" + username + "/following.ins", otro);
                appendLineStatic(RUTA_RAIZ + "/" + otro + "/followers.ins", username);
            }
        }

        System.out.println("Cuenta default creada: @" + username);
    }

    private static void crearPosts(int idx, String username) {
        String rutaInsta = RUTA_RAIZ + "/" + username + "/insta.ins";
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("HH:mm:ss");
        LocalDate baseDate = LocalDate.now().minusDays(POSTS[idx].length);

        for (int j = 0; j < POSTS[idx].length; j++) {
            String contenido = POSTS[idx][j][0];
            String hashtags  = POSTS[idx][j][1];

            String nombreSinExt = IMG_PREFIJOS[idx] + (j + 1);
            String destBase     = RUTA_RAIZ + "/" + username + "/imagenes/" + nombreSinExt;

            String rutaImg = copiarRecurso("/images/" + nombreSinExt + ".png", destBase + ".png");
            if (rutaImg == null)
                rutaImg = copiarRecurso("/images/" + nombreSinExt + ".jpg", destBase + ".jpg");
            if (rutaImg == null) {
                System.out.println("Advertencia: no se encontro /images/" + nombreSinExt + " (.png/.jpg)");
                rutaImg = "";
            }

            LocalDate fecha = baseDate.plusDays(j);
            LocalTime hora  = LocalTime.of(8 + (j % 14), (j * 11) % 60, 0);

            String lineaPost = username + "|" + fecha + "|" + hora.format(dtf) + "|"
                    + contenido + "|" + hashtags + "| |" + rutaImg + "\n";
            try (FileWriter fw = new FileWriter(rutaInsta, true)) {
                fw.write(lineaPost);
            } catch (IOException e) {
                System.out.println("Error guardando post default: " + e.getMessage());
            }
        }
    }

    private static void appendLineStatic(String ruta, String linea) {
        try (FileWriter fw = new FileWriter(ruta, true)) {
            fw.write(linea + "\n");
        } catch (IOException ignored) {}
    }

    private static String copiarRecurso(String rutaClasspath, String rutaDestino) {
        try (InputStream is = InicializadorCuentasDefault.class.getResourceAsStream(rutaClasspath)) {
            if (is == null) return null;
            File dest = new File(rutaDestino);
            dest.getParentFile().mkdirs();
            java.nio.file.Files.copy(is, dest.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
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
            "stickers.ins", "solicitudes.ins", "likes.ins",
            "comments.ins", "notifications.ins", "likes_notif.ins"
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