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
        {"NASA fue fundada en 1958. En solo 11 años llevó al hombre a la Luna. #nasa #historia #espacio",                                    "#nasa #historia #espacio"},
        {"El presupuesto de NASA es el 0.5% del federal de EE.UU. pero genera $3 por cada dólar invertido. #nasa #ciencia",                  "#nasa #ciencia #datos"},
        {"NASA ha lanzado más de 1,000 misiones desde su fundación. Cada una empuja los límites del conocimiento humano. #nasa #espacio",     "#nasa #espacio #misiones"},
        {"El traje espacial de NASA cuesta $12 millones. Tiene 14 capas y puede resistir temperaturas de +120 a -160 °C. #nasa #tecnologia", "#nasa #tecnologia #espacio"},
        {"El rover Perseverance produce oxígeno en Marte. NASA demostró que es posible respirar en otro planeta. #nasa #marte",              "#nasa #marte #tecnologia"},
        {"NASA tiene más de 2,000 patentes tecnológicas. Memory foam, filtros de agua y CAT scans nacieron de sus investigaciones. #nasa",   "#nasa #innovacion #ciencia"},
        {"El Centro de Control de Misiones nunca ha cerrado desde 1965. Funciona 24/7 los 365 días del año. #nasa #mision #espacio",         "#nasa #mision #espacio"},
        {"NASA emplea a más de 18,000 personas entre científicos, ingenieros y técnicos en todo EE.UU. #nasa #ciencia #careers",             "#nasa #ciencia #careers"},
        {"El telescopio James Webb puede ver galaxias a 13,600 millones de años luz. La mayor inversión óptica de NASA. #nasa #webb",        "#nasa #webb #universo"},
        {"NASA planea llevar humanos a Marte antes del 2040. El proyecto Artemis es el primer paso de ese camino. #nasa #marte #futuro",     "#nasa #marte #futuro"},
    },
    // starbucks — 10 posts
    {
        {"Starbucks abrió su primera tienda en Seattle en 1971. Hoy tiene más de 36,000 locales en 80 países. #starbucks #historia #cafe",   "#starbucks #historia #cafe"},
        {"El nombre Starbucks viene de Moby Dick. Starbuck era el primer oficial del barco ballenero Pequod. #starbucks #historia",          "#starbucks #historia #curiosidad"},
        {"Starbucks compra el 3% de todo el café producido en el mundo cada año. Más de 500 millones de kg. #starbucks #cafe #datos",        "#starbucks #cafe #datos"},
        {"El Frappuccino fue inventado en 1995 en Boston. Hoy es la bebida más fotografiada en redes sociales. #starbucks #frappuccino",     "#starbucks #frappuccino #historia"},
        {"Hay más de 170,000 combinaciones posibles de bebidas en Starbucks. Tu pedido puede ser único en el mundo. #starbucks #cafe",       "#starbucks #cafe #personaliza"},
        {"El cold brew de Starbucks se prepara durante 20 horas en agua fría. Sin calor, sin acidez, puro sabor. #starbucks #coldbrew",      "#starbucks #coldbrew #cafe"},
        {"Starbucks fue la primera empresa de EE.UU. en ofrecer seguro médico a empleados de medio tiempo en 1988. #starbucks #historia",   "#starbucks #historia #empresa"},
        {"La sirena del logo de Starbucks es una figura mitológica griega llamada sirena de dos colas. Existe desde 1971. #starbucks",       "#starbucks #logo #historia"},
        {"Starbucks Rewards tiene más de 30 millones de miembros activos solo en EE.UU. Es uno de los loyalty programs más grandes. #starbucks", "#starbucks #rewards #datos"},
        {"Las tazas rojas de Starbucks se lanzaron por primera vez en 1997. Hoy son un símbolo mundial de la temporada navideña. #starbucks", "#starbucks #navidad #redcup"},
    },
    // natgeo — 10 posts
    {
        {"National Geographic fue fundada en 1888. Es una de las organizaciones científicas más antiguas del mundo. #natgeo #historia",       "#natgeo #historia #ciencia"},
        {"La revista NatGeo tiene una cubierta amarilla icónica desde 1910. Es reconocida en más de 170 países. #natgeo #revista #historia", "#natgeo #revista #historia"},
        {"National Geographic ha financiado más de 14,000 proyectos científicos en todo el mundo desde su fundación. #natgeo #ciencia",      "#natgeo #ciencia #exploracion"},
        {"NatGeo fue la primera revista en publicar fotografías bajo el agua en 1926. Cambió la fotografía para siempre. #natgeo #foto",      "#natgeo #foto #historia"},
        {"National Geographic Society tiene más de 3 millones de miembros activos alrededor del mundo. #natgeo #comunidad",                  "#natgeo #comunidad #ciencia"},
        {"El canal de TV National Geographic llega a 440 millones de hogares en más de 172 países. #natgeo #television #datos",              "#natgeo #television #datos"},
        {"NatGeo emplea a más de 400 exploradores activos que investigan desde las profundidades del mar hasta el espacio. #natgeo",          "#natgeo #exploradores #ciencia"},
        {"La fotografía más famosa de NatGeo es la niña afgana de 1985. Steve McCurry la tomó en un campo de refugiados. #natgeo #foto",     "#natgeo #foto #historia"},
        {"National Geographic produce contenido en 45 idiomas diferentes. Su misión es la educación sin fronteras. #natgeo #educacion",       "#natgeo #educacion #idiomas"},
        {"NatGeo fue adquirida por Disney en 2019. Sigue siendo la marca de ciencia y naturaleza más reconocida del mundo. #natgeo",         "#natgeo #historia #empresa"},
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
                System.out.println("Advertencia: no se encontró /images/" + nombreSinExt + " (.png/.jpg)");
                rutaImg = "";
            }

            LocalDate fecha = baseDate.plusDays(j);
            LocalTime hora  = LocalTime.of(8 + (j % 14), (j * 11) % 60, 0);

            String linea = username + "|" + fecha + "|" + hora.format(dtf) + "|"
        + contenido + "|" + hashtags + "| |" + rutaImg + "\n";
            try (FileWriter fw = new FileWriter(rutaInsta, true)) {
                fw.write(linea);
            } catch (IOException e) {
                System.out.println("Error guardando post default: " + e.getMessage());
            }
        }
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