

import enums.EstadoCuenta;
import enums.TipoCuenta;
import instagram.Mensaje;
import instagram.Publicacion;
import instagram.Sistema;
import instagram.Usuario;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.plaf.basic.BasicScrollBarUI;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import javax.swing.filechooser.FileNameExtensionFilter;

public class VentanaPrincipal extends JFrame {

    private Sistema sistema;
    private Timer chatTimer;
    private int lastMessageCount = 0;

    // ── PALETA INSTAGRAM 2017-2018 ──────────────────────────────
    private static final Color C_WHITE       = Color.WHITE;
    private static final Color C_BG          = new Color(250, 250, 250);
    private static final Color C_BLUE        = new Color(64,  155, 230);   // azul acción
    private static final Color C_BLUE_DIM    = new Color(42,  107, 161);   // azul deshabilitado
    private static final Color C_FIELD       = new Color(250, 250, 250);
    private static final Color C_BORDER      = new Color(219, 219, 219);
    private static final Color C_PLACEHOLDER = new Color(168, 168, 168);
    private static final Color C_TEXT        = new Color(38,  38,  38);
    private static final Color C_TEXT_LIGHT  = new Color(142, 142, 142);
    private static final Color C_ERROR       = new Color(237, 73,  86);
    private static final Color C_SIDEBAR_BG  = Color.WHITE;
    private static final Color C_HOVER       = new Color(250, 250, 250);

    // ── TIPOGRAFÍA ───────────────────────────────────────────────
    private static final Font F_REGULAR = new Font("Arial", Font.PLAIN, 13);
    private static final Font F_BOLD    = new Font("Arial", Font.BOLD,  13);
    private static final Font F_SMALL   = new Font("Arial", Font.PLAIN, 11);
    private static final Font F_H1      = new Font("Arial", Font.BOLD,  22);
    private static final Font F_H2      = new Font("Arial", Font.BOLD,  16);

    // ── ICONOS ───────────────────────────────────────────────────
    private ImageIcon iconEyeClosed, iconEyeOpen, iconCheck;
    private Map<String, ImageIcon> iconsNormal = new HashMap<>();
    private Map<String, ImageIcon> iconsBold   = new HashMap<>();

    private String vistaActual = "Home";

    // ────────────────────────────────────────────────────────────
    public VentanaPrincipal(Sistema sistema) {
        this.sistema = sistema;
        cargarIconos();
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                if (sistema.getUsuarioActual() != null) sistema.logout();
            }
        });
        configurarVentana();
        inicializarComponentesLogin();
    }

    // ════════════════════════════════════════════════════════════
    //  CONFIGURACIÓN
    // ════════════════════════════════════════════════════════════
    private void configurarVentana() {
        setSize(1366, 768);
        setTitle("Instagram");
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setResizable(false);
    }

    private void cargarIconos() {
        try { iconEyeClosed = scaled("/images/ojocerrado.png", 18, 18); } catch (Exception ignored) {}
        try { iconEyeOpen   = scaled("/images/ojo.png", 18, 18);        } catch (Exception ignored) {}
        try { iconCheck     = scaled("/images/check.png", 16, 16);      } catch (Exception ignored) {}

        int s = 22;
        loadIcon("Home",          "homeicon.png",    "bhomeicon.png",    s);
        loadIcon("Search",        "searchicon.png",  "bsearchicon.png",  s);
        loadIcon("Messages",      "messageicon.png", "bmessageicon.png", s);
        loadIcon("Create",        "createicon.png",  "bcreateicon.png",  s);
        loadIcon("Notifications", "hearticon.png",   "bhearticon.png",   s);
        loadIcon("Profile",       "profileicon.png", "bprofileicon.png", s);
        loadIcon("Like",          "hearticon.png",   "bhearticon.png",   s);
        loadIcon("Comment",       "commenticon.png", "commenticon.png",  s);
        loadIcon("Share",         "messageicon.png", "bmessageicon.png", s);
    }

    private void loadIcon(String key, String normal, String bold, int size) {
        try {
            URL u1 = getClass().getResource("/images/" + normal);
            if (u1 != null) iconsNormal.put(key, new ImageIcon(new ImageIcon(u1).getImage().getScaledInstance(size, size, Image.SCALE_SMOOTH)));
            URL u2 = getClass().getResource("/images/" + bold);
            if (u2 != null) iconsBold.put(key,   new ImageIcon(new ImageIcon(u2).getImage().getScaledInstance(size, size, Image.SCALE_SMOOTH)));
        } catch (Exception ignored) {}
    }

    private ImageIcon scaled(String path, int w, int h) {
        URL u = getClass().getResource(path);
        if (u == null) return null;
        return new ImageIcon(new ImageIcon(u).getImage().getScaledInstance(w, h, Image.SCALE_SMOOTH));
    }

    // ════════════════════════════════════════════════════════════
    //  VISTA 1 — LOGIN
    // ════════════════════════════════════════════════════════════
    private void inicializarComponentesLogin() {
        if (chatTimer != null) chatTimer.stop();
        getContentPane().removeAll();
        getContentPane().setLayout(null);
        getContentPane().setBackground(C_BG);

        // Tarjeta blanca centrada
        int cardW = 350, cardH = 430;
        int cardX = (1366 - cardW) / 2;
        int cardY = (768  - cardH) / 2;

        JPanel card = roundPanel(C_WHITE, 0);
        card.setLayout(null);
        card.setBounds(cardX, cardY, cardW, cardH);
        card.setBorder(new LineBorder(C_BORDER, 1));

        // Logo — Fix 4: escalar proporcionalmente, no estirar
        JLabel lblLogo = new JLabel();
        try {
            ImageIcon ic = new ImageIcon(getClass().getResource("/images/instagramlogoblack.png"));
            int origW = ic.getIconWidth(), origH = ic.getIconHeight();
            // Ancho máximo 200px, altura proporcional
            int targetW = 200;
            int targetH = origW > 0 ? (int)((double)origH / origW * targetW) : 60;
            targetH = Math.min(targetH, 70); // cap de altura
            lblLogo.setIcon(new ImageIcon(ic.getImage().getScaledInstance(targetW, targetH, Image.SCALE_SMOOTH)));
        } catch (Exception e) {
            lblLogo.setText("Instagram");
            lblLogo.setFont(new Font("Arial", Font.BOLD, 28));
        }
        lblLogo.setHorizontalAlignment(SwingConstants.CENTER);
        lblLogo.setBounds(37, 28, 276, 72);
        card.add(lblLogo);

        // Campos
        JTextField txtUser = buildField("Nombre de usuario");
        txtUser.setBounds(37, 115, 276, 38);
        card.add(txtUser);

        JLabel lblErrUser = errorLabel(); lblErrUser.setBounds(37, 154, 276, 16);
        card.add(lblErrUser);

        JPanel panelPass = buildPassPanel();
        panelPass.setBounds(37, 172, 276, 38);
        card.add(panelPass);

        JLabel lblErrPass = errorLabel(); lblErrPass.setBounds(37, 211, 276, 16);
        card.add(lblErrPass);

        JButton btnLogin = buildPrimaryBtn("Iniciar sesión");
        btnLogin.setEnabled(false);
        btnLogin.setBackground(C_BLUE_DIM);
        btnLogin.setBounds(37, 232, 276, 36);
        card.add(btnLogin);

        // Divider "— O —"
        JLabel divider = new JLabel("── o ──", SwingConstants.CENTER);
        divider.setFont(F_SMALL);
        divider.setForeground(C_TEXT_LIGHT);
        divider.setBounds(37, 276, 276, 20);
        card.add(divider);

        JLabel lblReg = linkLabel("¿No tienes cuenta? Regístrate");
        int lw = lblReg.getPreferredSize().width;
        lblReg.setBounds(37 + (276 - lw) / 2, 300, lw, 20);
        card.add(lblReg);

        getContentPane().add(card);

        // Validation
        JPasswordField txtPass = (JPasswordField) panelPass.getComponent(0);
        DocumentListener dl = simpleDocListener(() -> {
            boolean ok = !txtUser.getText().equals("Nombre de usuario") && !txtUser.getText().isEmpty()
                      && String.valueOf(txtPass.getPassword()).length() >= 6;
            btnLogin.setEnabled(ok);
            btnLogin.setBackground(ok ? C_BLUE : C_BLUE_DIM);
            btnLogin.setForeground(ok ? C_WHITE : new Color(200, 202, 204));
            lblErrUser.setText(""); lblErrPass.setText("");
            resetFieldBorder(txtUser); resetPanelBorder(panelPass);
        });
        txtUser.getDocument().addDocumentListener(dl);
        txtPass.getDocument().addDocumentListener(dl);

        btnLogin.addActionListener(e -> {
            String user = txtUser.getText().trim();
            String pass = new String(txtPass.getPassword());
            if (sistema.sesionActiva(user)) {
                InstaDialog.showMessage(this, "Este usuario ya tiene\nuna sesión activa.");
                return;
            }
            Usuario u = sistema.buscarUsuario(user);
            if (u == null) { lblErrUser.setText("El usuario no existe."); redBorder(txtUser); return; }
            if (u.getEstadoCuenta() == EstadoCuenta.DESACTIVADO) {
                boolean yes = InstaDialog.showConfirm(this, "Cuenta desactivada.\n¿Deseas reactivarla?", "Reactivar", false);
                if (yes) sistema.reactivarCuenta(user); else return;
            }
            if (!sistema.login(user, pass)) { lblErrPass.setText("Contraseña incorrecta."); redBorder(panelPass); return; }
            cargarVistaFeed();
        });

        lblReg.addMouseListener(click(this::cargarVistaRegistro));

        revalidate(); repaint();
    }

    // ════════════════════════════════════════════════════════════
    //  VISTA 2 — REGISTRO
    // ════════════════════════════════════════════════════════════
    private void cargarVistaRegistro() {
        getContentPane().removeAll();
        getContentPane().setLayout(null);
        getContentPane().setBackground(C_BG);

        int cardW = 380, cardH = 610;
        int cardX = (1366 - cardW) / 2;
        int cardY = Math.max(10, (768 - cardH) / 2);

        JPanel card = roundPanel(C_WHITE, 0);
        card.setLayout(null);
        card.setBounds(cardX, cardY, cardW, cardH);
        card.setBorder(new LineBorder(C_BORDER, 1));

        JLabel titulo = new JLabel("Crear cuenta", SwingConstants.CENTER);
        titulo.setFont(F_H1); titulo.setForeground(C_TEXT);
        titulo.setBounds(0, 22, cardW, 32);
        card.add(titulo);

        JLabel sub = new JLabel("Regístrate para ver fotos de tus amigos.", SwingConstants.CENTER);
        sub.setFont(F_SMALL); sub.setForeground(C_TEXT_LIGHT);
        sub.setBounds(0, 58, cardW, 18);
        card.add(sub);

        // Foto perfil
        JLabel lblFotoPreview = new JLabel("📷", SwingConstants.CENTER);
        lblFotoPreview.setFont(new Font("Arial", Font.PLAIN, 26));
        lblFotoPreview.setOpaque(true);
        lblFotoPreview.setBackground(new Color(239, 239, 239));
        lblFotoPreview.setPreferredSize(new Dimension(72, 72));
        lblFotoPreview.setToolTipText("Seleccionar foto");
        lblFotoPreview.setCursor(new Cursor(Cursor.HAND_CURSOR));
        int fX = (cardW - 72) / 2;
        lblFotoPreview.setBounds(fX, 84, 72, 72);
        // Hacer la previsualización circular via paint
        JLabel avatarCircle = new JLabel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setClip(new Ellipse2D.Double(0, 0, getWidth(), getHeight()));
                super.paintComponent(g);
                g2.setColor(C_BORDER);
                g2.drawOval(0, 0, getWidth()-1, getHeight()-1);
                g2.dispose();
            }
        };
        avatarCircle.setHorizontalAlignment(SwingConstants.CENTER);
        avatarCircle.setFont(new Font("Arial", Font.PLAIN, 26));
        avatarCircle.setText("📷");
        avatarCircle.setOpaque(true);
        avatarCircle.setBackground(new Color(239, 239, 239));
        avatarCircle.setBounds(fX, 84, 72, 72);
        avatarCircle.setCursor(new Cursor(Cursor.HAND_CURSOR));
        card.add(avatarCircle);

        final File[] archivoSel = {null};
        avatarCircle.addMouseListener(click(() -> {
            JFileChooser fc = new JFileChooser();
            fc.setFileFilter(new FileNameExtensionFilter("Imágenes", "jpg","png","jpeg"));
            if (fc.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                archivoSel[0] = fc.getSelectedFile();
                ImageIcon ic = new ImageIcon(archivoSel[0].getAbsolutePath());
                Image img = ic.getImage().getScaledInstance(72, 72, Image.SCALE_SMOOTH);
                avatarCircle.setIcon(new ImageIcon(img));
                avatarCircle.setText("");
            }
        }));

        int fx = 40, fw = cardW - 80, y = 170, gap = 48;

        JTextField txtNombre = buildField("Nombre completo");  txtNombre.setBounds(fx, y, fw, 38); card.add(txtNombre);
        JLabel errNombre = errorLabel(); errNombre.setBounds(fx, y+38, fw, 16); card.add(errNombre); y+=gap;

        JPanel panelUser = buildFieldWithIcon("Nombre de usuario");
        panelUser.setBounds(fx, y, fw, 38); card.add(panelUser);
        JLabel errUser = errorLabel(); errUser.setBounds(fx, y+38, fw, 16); card.add(errUser); y+=gap;

        JPanel panelPass = buildPassPanel(); panelPass.setBounds(fx, y, fw, 38); card.add(panelPass);
        JLabel errPass = errorLabel(); errPass.setBounds(fx, y+38, fw, 16); card.add(errPass); y+=gap;

        // Fila edad + género
        JLabel lEdad = new JLabel("Edad"); lEdad.setFont(F_SMALL); lEdad.setForeground(C_TEXT_LIGHT);
        lEdad.setBounds(fx, y, 60, 16); card.add(lEdad);
        JLabel lGenero = new JLabel("Género"); lGenero.setFont(F_SMALL); lGenero.setForeground(C_TEXT_LIGHT);
        lGenero.setBounds(fx+110, y, 80, 16); card.add(lGenero);
        JLabel lTipo = new JLabel("Cuenta"); lTipo.setFont(F_SMALL); lTipo.setForeground(C_TEXT_LIGHT);
        lTipo.setBounds(fx+210, y, 80, 16); card.add(lTipo);

        y += 18;
        JSpinner spnEdad = buildSpinner(18); spnEdad.setBounds(fx, y, 90, 34); card.add(spnEdad);
        JLabel errEdad = errorLabel(); errEdad.setBounds(fx, y+36, 100, 14); card.add(errEdad);

        JComboBox<String> cmbGenero = buildCombo("M", "F"); cmbGenero.setBounds(fx+110, y, 85, 34); card.add(cmbGenero);
        JComboBox<String> cmbTipo   = buildCombo("Pública","Privada"); cmbTipo.setBounds(fx+210, y, 90, 34); card.add(cmbTipo);
        y += 50;

        JButton btnReg = buildPrimaryBtn("Registrarse");
        btnReg.setEnabled(false); btnReg.setBackground(C_BLUE_DIM);
        btnReg.setBounds(fx, y, fw, 36); card.add(btnReg);
        y += 46;

        JLabel lblVolver = linkLabel("¿Ya tienes cuenta? Inicia sesión");
        int lw = lblVolver.getPreferredSize().width;
        lblVolver.setBounds(fx + (fw - lw)/2, y, lw, 18); card.add(lblVolver);

        getContentPane().add(card);

        JTextField txtUser = (JTextField) panelUser.getComponent(0);
        JLabel iconCheck2 = (JLabel) panelUser.getComponent(1);
        JPasswordField txtPass = (JPasswordField) panelPass.getComponent(0);

        Runnable validar = () -> {
            boolean ok = true;
            errNombre.setText(""); errUser.setText(""); errPass.setText(""); errEdad.setText("");
            resetFieldBorder(txtNombre); resetPanelBorder(panelUser); resetPanelBorder(panelPass); resetSpinnerBorder(spnEdad);
            iconCheck2.setIcon(null); iconCheck2.setText("");

            String nom = txtNombre.getText();
            if (nom.equals("Nombre completo") || nom.isEmpty()) ok = false;

            String usr = txtUser.getText();
            if (usr.isEmpty() || usr.equals("Nombre de usuario")) { ok = false; }
            else if (usr.length() < 3) { errUser.setText("Mínimo 3 caracteres."); redBorder(panelUser); ok = false; }
            else if (sistema.existeUsername(usr)) { errUser.setText("El nombre ya existe."); redBorder(panelUser); ok = false; }
            else { iconCheck2.setIcon(iconCheck); if (iconCheck == null) iconCheck2.setText("✓"); }

            String ps = new String(txtPass.getPassword());
            if (ps.isEmpty() || ps.equals("Password")) { ok = false; }
            else if (ps.length() < 6) { errPass.setText("Mínimo 6 caracteres."); redBorder(panelPass); ok = false; }

            int edad = (int) spnEdad.getValue();
            if (edad < 18) { errEdad.setText("Debes ser mayor de 18 años."); redBorder(spnEdad); ok = false; }

            btnReg.setEnabled(ok);
            btnReg.setBackground(ok ? C_BLUE : C_BLUE_DIM);
            btnReg.setForeground(ok ? C_WHITE : new Color(200,202,204));
        };

        DocumentListener dl = simpleDocListener(validar);
        txtNombre.getDocument().addDocumentListener(dl);
        txtUser.getDocument().addDocumentListener(dl);
        txtPass.getDocument().addDocumentListener(dl);
        spnEdad.addChangeListener(e -> validar.run());

        btnReg.addActionListener(e -> {
            String nombre = txtNombre.getText().trim();
            String user   = txtUser.getText().trim();
            String pass   = new String(txtPass.getPassword());
            int edad      = (int) spnEdad.getValue();
            char genero   = cmbGenero.getSelectedIndex() == 0 ? 'M' : 'F';
            TipoCuenta tipo = cmbTipo.getSelectedIndex() == 0 ? TipoCuenta.PUBLICA : TipoCuenta.PRIVADA;
            String rutaFoto = archivoSel[0] != null
                ? sistema.procesarImagenPerfil(archivoSel[0], user, "profile_" + System.currentTimeMillis())
                : "";
            if (sistema.registrarUsuario(user, pass, nombre, genero, edad, rutaFoto, tipo)) {
                sistema.login(user, pass);
                cargarVistaFeed();
            } else { errUser.setText("Error al registrar."); }
        });

        lblVolver.addMouseListener(click(this::inicializarComponentesLogin));

        revalidate(); repaint();
    }

    // ════════════════════════════════════════════════════════════
    //  VISTA 3 — FEED
    // ════════════════════════════════════════════════════════════
    private void cargarVistaFeed() {
        vistaActual = "Home";
        getContentPane().removeAll();
        setLayout(new BorderLayout());
        getContentPane().setBackground(C_BG);

        add(buildSidebar(), BorderLayout.WEST);

        JPanel panelContenido = new JPanel();
        panelContenido.setBackground(C_BG);
        panelContenido.setLayout(new BoxLayout(panelContenido, BoxLayout.Y_AXIS));
        JScrollPane scroll = styledScroll(panelContenido);

        ArrayList<Publicacion> posts = sistema.getTimeline();
        if (posts.isEmpty()) {
            JLabel lbl = new JLabel("No hay publicaciones. ¡Sigue a alguien o crea tu primer post!");
            lbl.setForeground(C_TEXT_LIGHT); lbl.setFont(F_REGULAR);
            lbl.setAlignmentX(Component.CENTER_ALIGNMENT);
            panelContenido.add(Box.createVerticalStrut(200));
            panelContenido.add(lbl);
        } else {
            for (Publicacion p : posts) {
                panelContenido.add(buildPost(p));
                panelContenido.add(Box.createVerticalStrut(12));
            }
        }

        add(scroll, BorderLayout.CENTER);
        revalidate(); repaint();
    }

    // ════════════════════════════════════════════════════════════
    //  POST CARD
    // ════════════════════════════════════════════════════════════
    private JPanel buildPost(Publicacion p) {
        int anchoImg = 600;
        int altoFinal = 600;

        try {
            if (p.getRutaImagen() != null && !p.getRutaImagen().isEmpty()) {
                File f = new File(p.getRutaImagen());
                if (f.exists()) {
                    ImageIcon ic = new ImageIcon(p.getRutaImagen());
                    double ratio = (double) ic.getIconHeight() / Math.max(1, ic.getIconWidth());
                    if (ratio > 1.1) altoFinal = 750;
                    else if (ratio < 0.9) altoFinal = 400;
                }
            }
        } catch (Exception ignored) {}

        JPanel post = new JPanel(new BorderLayout());
        post.setBackground(C_WHITE);
        post.setMaximumSize(new Dimension(anchoImg, altoFinal + 220));
        post.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createEmptyBorder(0, 0, 0, 0),
            new LineBorder(C_BORDER, 1)
        ));

        // ── HEADER ──
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(C_WHITE);
        header.setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));

        JPanel userInfo = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        userInfo.setBackground(C_WHITE);

        JLabel lblFoto = new JLabel();
        lblFoto.setIcon(cargarFotoCircular(p.getAutor(), 32));
        userInfo.add(lblFoto);

        JLabel lblUser = new JLabel(p.getAutor());
        lblUser.setFont(F_BOLD); lblUser.setForeground(C_TEXT);
        lblUser.setCursor(new Cursor(Cursor.HAND_CURSOR));
        lblUser.addMouseListener(click(() -> cargarVistaPerfil(p.getAutor())));
        userInfo.add(lblUser);
        header.add(userInfo, BorderLayout.WEST);

        if (sistema.getUsuarioActual() != null && p.getAutor().equals(sistema.getUsuarioActual().getUsername())) {
            JButton btnOpts = new JButton("•••");
            btnOpts.setFont(new Font("Arial", Font.BOLD, 12));
            btnOpts.setBorderPainted(false); btnOpts.setContentAreaFilled(false);
            btnOpts.setFocusPainted(false); btnOpts.setCursor(new Cursor(Cursor.HAND_CURSOR));
            btnOpts.setForeground(C_TEXT);

            JPopupMenu menu = new JPopupMenu();
            menu.setBorder(new LineBorder(C_BORDER, 1));
            JMenuItem itemDel = new JMenuItem("Eliminar publicación");
            itemDel.setFont(F_BOLD); itemDel.setForeground(C_ERROR);
            itemDel.addActionListener(ev -> {
                boolean ok = InstaDialog.showConfirm(this, "¿Eliminar esta publicación?", "Eliminar", true);
                if (ok) {
                    sistema.eliminarPublicacion(p);
                    if ("Profile".equals(vistaActual)) cargarVistaPerfil(sistema.getUsuarioActual().getUsername());
                    else cargarVistaFeed();
                }
            });
            menu.add(itemDel);
            btnOpts.addActionListener(ev -> menu.show(btnOpts, 0, btnOpts.getHeight()));
            header.add(btnOpts, BorderLayout.EAST);
        }

        // ── IMAGEN ──
        JLabel lblImg = new JLabel();
        lblImg.setHorizontalAlignment(SwingConstants.CENTER);
        lblImg.setBackground(new Color(239, 239, 239)); lblImg.setOpaque(true);
        try {
            if (p.getRutaImagen() != null && !p.getRutaImagen().isEmpty() && new File(p.getRutaImagen()).exists()) {
                Image img = new ImageIcon(p.getRutaImagen()).getImage().getScaledInstance(anchoImg, altoFinal, Image.SCALE_SMOOTH);
                lblImg.setIcon(new ImageIcon(img));
            }
        } catch (Exception ignored) {}
        lblImg.setPreferredSize(new Dimension(anchoImg, altoFinal));

        // ── ACCIONES ──
        JPanel acciones = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 6));
        acciones.setBackground(C_WHITE);

        boolean liked = sistema.yaDioLike(p.getAutor(), p.getFecha().toString());
        JButton btnLike = iconBtn(liked);
        btnLike.addActionListener(ev -> {
            boolean estado = sistema.toggleLike(p.getAutor(), p.getFecha().toString());
            actualizarLike(btnLike, estado);
        });

        JButton btnComment = iconBtn("Comment", false);
        btnComment.addActionListener(ev -> abrirComentarios(p));

        JButton btnShare = iconBtn("Share", false);
        btnShare.addActionListener(ev -> {
            String dest = InstaDialog.showInput(this, "Enviar post a...", "Nombre de usuario");
            if (dest != null) {
                if (!sistema.puedeCompartirPost(dest, p.getAutor())) {
                    InstaDialog.showMessage(this, "No puedes compartir este post.\nEl autor tiene cuenta privada.", true);
                    return;
                }
                sistema.compartirPost(dest, p.getAutor(), p.getRutaImagen(), p.getContenido());
                InstaDialog.showMessage(this, "Post enviado a " + dest + " ✓");
            }
        });

        acciones.add(btnLike);
        acciones.add(btnComment);
        acciones.add(btnShare);

        // ── FOOTER ──
        JPanel footer = new JPanel();
        footer.setLayout(new BoxLayout(footer, BoxLayout.Y_AXIS));
        footer.setBackground(C_WHITE);
        footer.setBorder(BorderFactory.createEmptyBorder(0, 12, 10, 12));

        footer.add(acciones);

        JEditorPane edCaption = new JEditorPane("text/html", "");
        edCaption.setEditable(false); edCaption.setOpaque(false);
        String html = "<html><font face='Arial' size='3'><b>" + p.getAutor() + "</b> " + toHtml(p.getContenido()) + "</font></html>";
        edCaption.setText(html);
        edCaption.addHyperlinkListener(e -> {
            if (e.getEventType() == javax.swing.event.HyperlinkEvent.EventType.ACTIVATED) {
                String href = e.getDescription();
                if (href.startsWith("#")) cargarVistaBusquedaHashtag(href);
                else if (href.startsWith("@")) cargarVistaPerfil(href.substring(1));
            }
        });
        footer.add(edCaption);

        JLabel lblFecha = new JLabel(p.getFecha() + "  " + p.getHoraFormateada());
        lblFecha.setFont(F_SMALL); lblFecha.setForeground(C_TEXT_LIGHT);
        lblFecha.setBorder(BorderFactory.createEmptyBorder(4, 0, 0, 0));
        footer.add(lblFecha);

        post.add(header, BorderLayout.NORTH);
        post.add(lblImg,  BorderLayout.CENTER);
        post.add(footer,  BorderLayout.SOUTH);
        return post;
    }

    private JButton iconBtn(boolean liked) {
        JButton b = new JButton();
        b.setBorderPainted(false); b.setBackground(C_WHITE);
        b.setFocusPainted(false); b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        actualizarLike(b, liked);
        return b;
    }
    private JButton iconBtn(String key, boolean bold) {
        JButton b = new JButton();
        b.setBorderPainted(false); b.setBackground(C_WHITE);
        b.setFocusPainted(false); b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        Map<String,ImageIcon> map = bold ? iconsBold : iconsNormal;
        if (map.containsKey(key)) b.setIcon(map.get(key));
        else b.setText(key.equals("Comment") ? "💬" : "➤");
        return b;
    }
    private void actualizarLike(JButton b, boolean on) {
        if (on) { if (iconsBold.containsKey("Like"))   b.setIcon(iconsBold.get("Like"));   else b.setText("❤️"); }
        else    { if (iconsNormal.containsKey("Like"))  b.setIcon(iconsNormal.get("Like")); else b.setText("🤍"); }
    }

    // ════════════════════════════════════════════════════════════
    //  COMENTARIOS — DIALOGO CUSTOM
    // ════════════════════════════════════════════════════════════
    private void abrirComentarios(Publicacion p) {
        JDialog d = new JDialog(this, "Comentarios", false);
        d.setUndecorated(false);
        d.setSize(420, 520);
        d.setLocationRelativeTo(this);
        d.setLayout(new BorderLayout());
        d.getContentPane().setBackground(C_WHITE);

        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(C_WHITE);
        header.setBorder(BorderFactory.createCompoundBorder(
            new MatteBorder(0,0,1,0,C_BORDER),
            BorderFactory.createEmptyBorder(12,16,12,16)
        ));
        JLabel lbl = new JLabel("Comentarios"); lbl.setFont(F_BOLD); lbl.setForeground(C_TEXT);
        header.add(lbl, BorderLayout.WEST);
        JButton btnClose = new JButton("✕");
        btnClose.setFont(F_SMALL); btnClose.setBorderPainted(false);
        btnClose.setContentAreaFilled(false); btnClose.setFocusPainted(false);
        btnClose.setCursor(new Cursor(Cursor.HAND_CURSOR)); btnClose.setForeground(C_TEXT_LIGHT);
        btnClose.addActionListener(e -> d.dispose());
        header.add(btnClose, BorderLayout.EAST);
        d.add(header, BorderLayout.NORTH);

        // Lista
        DefaultListModel<String> model = new DefaultListModel<>();
        for (String c : sistema.getComentarios(p.getAutor(), p.getFecha().toString())) model.addElement(c);
        JList<String> lista = new JList<>(model);
        lista.setFont(F_REGULAR); lista.setBackground(C_WHITE);
        lista.setSelectionBackground(C_BG);
        lista.setBorder(BorderFactory.createEmptyBorder(8,16,8,16));
        lista.setCellRenderer(new DefaultListCellRenderer() {
            public Component getListCellRendererComponent(JList<?> l, Object v, int i, boolean sel, boolean focus) {
                JLabel lbl = (JLabel) super.getListCellRendererComponent(l,v,i,sel,focus);
                lbl.setBorder(BorderFactory.createEmptyBorder(6,0,6,0));
                lbl.setBackground(C_WHITE); lbl.setForeground(C_TEXT);
                return lbl;
            }
        });
        JScrollPane scroll = styledScroll(lista);
        d.add(scroll, BorderLayout.CENTER);

        // Footer input
        JPanel footer = new JPanel(new BorderLayout());
        footer.setBackground(C_WHITE);
        footer.setBorder(BorderFactory.createCompoundBorder(
            new MatteBorder(1,0,0,0,C_BORDER),
            BorderFactory.createEmptyBorder(10,12,10,12)
        ));
        JTextField txt = new JTextField();
        txt.setFont(F_REGULAR); txt.setBackground(C_BG);
        txt.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(C_BORDER,1,true),
            BorderFactory.createEmptyBorder(7,12,7,12)
        ));
        JButton btnEnv = new JButton("Publicar");
        btnEnv.setFont(F_BOLD); btnEnv.setForeground(C_BLUE);
        btnEnv.setBorderPainted(false); btnEnv.setContentAreaFilled(false);
        btnEnv.setFocusPainted(false); btnEnv.setCursor(new Cursor(Cursor.HAND_CURSOR));
        ActionListener send = e -> {
            if (!txt.getText().isEmpty()) {
                sistema.agregarComentario(p.getAutor(), p.getFecha().toString(), txt.getText());
                model.addElement(sistema.getUsuarioActual().getUsername() + ": " + txt.getText());
                txt.setText("");
            }
        };
        btnEnv.addActionListener(send); txt.addActionListener(send);
        footer.add(txt, BorderLayout.CENTER);
        footer.add(btnEnv, BorderLayout.EAST);
        d.add(footer, BorderLayout.SOUTH);
        d.setVisible(true);
    }

    // ════════════════════════════════════════════════════════════
    //  NUEVA PUBLICACIÓN — DIALOGO CUSTOM
    // ════════════════════════════════════════════════════════════
    private void abrirDialogoNuevaPublicacion() {
        JDialog d = new JDialog(this, "Nueva publicación", true);
        d.setSize(520, 540);
        d.setLocationRelativeTo(this);
        d.setLayout(new BorderLayout());
        d.getContentPane().setBackground(C_WHITE);

        // Header
        JPanel header = buildDialogHeader("Nueva publicación", d);
        d.add(header, BorderLayout.NORTH);

        // Centro
        JPanel center = new JPanel();
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
        center.setBackground(C_WHITE);
        center.setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));

        // Zona imagen — Fix 2: preview real de la imagen seleccionada
        // Usamos un JLabel con tamaño fijo; al seleccionar imagen se muestra el preview
        JLabel lblImgPreview = new JLabel() {
            @Override protected void paintComponent(Graphics g) {
                if (getIcon() == null) {
                    // Estado vacío: fondo rayado con mensaje
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(new Color(250,250,250));
                    g2.fillRect(0,0,getWidth(),getHeight());
                    g2.setColor(C_BORDER);
                    g2.setStroke(new BasicStroke(1.5f,BasicStroke.CAP_ROUND,BasicStroke.JOIN_ROUND,0,new float[]{6,4},0));
                    g2.drawRoundRect(1,1,getWidth()-2,getHeight()-2,8,8);
                    g2.setFont(F_REGULAR); g2.setColor(C_TEXT_LIGHT);
                    FontMetrics fm = g2.getFontMetrics();
                    String msg = "Selecciona una imagen";
                    g2.drawString(msg, (getWidth()-fm.stringWidth(msg))/2, getHeight()/2);
                    g2.dispose();
                } else {
                    super.paintComponent(g);
                }
            }
        };
        lblImgPreview.setHorizontalAlignment(SwingConstants.CENTER);
        lblImgPreview.setOpaque(false);
        lblImgPreview.setPreferredSize(new Dimension(452, 220));
        lblImgPreview.setMaximumSize(new Dimension(Integer.MAX_VALUE, 220));

        JPanel imgRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 6));
        imgRow.setBackground(C_WHITE);
        imgRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 235));
        imgRow.add(lblImgPreview);

        JButton btnSel = buildSecondaryBtn("Seleccionar imagen");
        btnSel.setAlignmentX(Component.CENTER_ALIGNMENT);

        center.add(imgRow);
        center.add(btnSel);

        // Textarea
        JTextArea txtContenido = new JTextArea();
        txtContenido.setLineWrap(true); txtContenido.setWrapStyleWord(true);
        txtContenido.setFont(F_REGULAR); txtContenido.setBackground(C_FIELD);
        txtContenido.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(C_BORDER,1,true),
            BorderFactory.createEmptyBorder(10,12,10,12)
        ));
        JScrollPane scrollTxt = new JScrollPane(txtContenido);
        scrollTxt.setBorder(null); scrollTxt.setPreferredSize(new Dimension(452, 80));
        scrollTxt.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));
        center.add(scrollTxt);

        d.add(center, BorderLayout.CENTER);

        // Footer botones
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 12));
        footer.setBackground(C_WHITE);
        footer.setBorder(new MatteBorder(1,0,0,0,C_BORDER));
        JButton btnCancel = buildSecondaryBtn("Cancelar");
        JButton btnPost   = buildPrimaryBtn("Compartir");
        footer.add(btnCancel);
        footer.add(btnPost);
        d.add(footer, BorderLayout.SOUTH);

        final String[] ruta = {""};

        btnSel.addActionListener(e -> {
            JFileChooser fc = new JFileChooser();
            fc.setFileFilter(new FileNameExtensionFilter("Imágenes","jpg","png","jpeg","gif"));
            fc.setAcceptAllFileFilterUsed(false);
            if (fc.showOpenDialog(d) == JFileChooser.APPROVE_OPTION) {
                String nombre = fc.getSelectedFile().getName().toLowerCase();
                if (nombre.matches(".*\\.(jpg|png|jpeg|gif)$")) {
                    ruta[0] = fc.getSelectedFile().getAbsolutePath();
                    // Fix 2: mostrar preview real de la imagen
                    ImageIcon orig = new ImageIcon(ruta[0]);
                    // Escalar manteniendo aspecto dentro de 452x220
                    int maxW = 452, maxH = 220;
                    int iw = orig.getIconWidth(), ih = orig.getIconHeight();
                    double scale = Math.min((double)maxW/iw, (double)maxH/ih);
                    int nw = (int)(iw*scale), nh = (int)(ih*scale);
                    lblImgPreview.setIcon(new ImageIcon(orig.getImage().getScaledInstance(nw, nh, Image.SCALE_SMOOTH)));
                    btnSel.setText("Cambiar imagen");
                } else {
                    InstaDialog.showMessage(d, "Solo se permiten JPG, PNG, JPEG o GIF.", true);
                }
            }
        });

        btnCancel.addActionListener(e -> d.dispose());
        btnPost.addActionListener(e -> {
            String texto = txtContenido.getText().trim();
            if (texto.isEmpty() || ruta[0].isEmpty()) {
                InstaDialog.showMessage(d, "Escribe algo y selecciona una imagen.");
                return;
            }
            StringBuilder hashtags = new StringBuilder(), menciones = new StringBuilder();
            for (String word : texto.split(" ")) {
                if (word.startsWith("#")) hashtags.append(word).append(" ");
                else if (word.startsWith("@")) menciones.append(word).append(" ");
            }
            String rutaFinal = sistema.procesarYGuardarImagen(new File(ruta[0]), sistema.getUsuarioActual().getUsername(), "post_"+System.currentTimeMillis());
            if (rutaFinal != null && sistema.crearPublicacion(texto, rutaFinal, hashtags.toString().trim(), menciones.toString().trim())) {
                d.dispose(); cargarVistaFeed();
            } else {
                InstaDialog.showMessage(d, "Error al publicar.", true);
            }
        });

        d.setVisible(true);
    }

    // ════════════════════════════════════════════════════════════
    //  SIDEBAR
    // ════════════════════════════════════════════════════════════
    private JPanel buildSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setPreferredSize(new Dimension(220, 768));
        sidebar.setBackground(C_WHITE);
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBorder(new MatteBorder(0,0,0,1,C_BORDER));

        // Logo — Fix 4: proporcional, no estirado
        JLabel logo = new JLabel();
        try {
            ImageIcon logoIc = new ImageIcon(getClass().getResource("/images/instagramlogoblack.png"));
            int origW = logoIc.getIconWidth(), origH = logoIc.getIconHeight();
            int targetW = 120;
            int targetH = origW > 0 ? (int)((double)origH / origW * targetW) : 40;
            targetH = Math.min(targetH, 48);
            logo.setIcon(new ImageIcon(logoIc.getImage().getScaledInstance(targetW, targetH, Image.SCALE_SMOOTH)));
        } catch (Exception e) {
            logo.setText("Instagram"); logo.setFont(new Font("Arial", Font.BOLD, 22));
        }
        logo.setAlignmentX(Component.LEFT_ALIGNMENT);
        logo.setBorder(BorderFactory.createEmptyBorder(24, 22, 28, 22));
        sidebar.add(logo);

        // Botones
        JButton[] btns = {
            sidebarBtn("Inicio",        "Home"),
            sidebarBtn("Buscar",        "Search"),
            sidebarBtn("Mensajes",      "Messages"),
            sidebarBtn("Crear",         "Create"),
            sidebarBtn("Notificaciones","Notifications"),
            sidebarBtn("Mi perfil",     "Profile")
        };
        for (JButton b : btns) sidebar.add(b);

        sidebar.add(Box.createVerticalGlue());

        JButton btnOut = sidebarBtn("Cerrar sesión", null);
        sidebar.add(btnOut);
        sidebar.add(Box.createVerticalStrut(20));

        // Eventos
        btns[0].addActionListener(e -> cargarVistaFeed());
        btns[1].addActionListener(e -> cargarVistaBusqueda());
        btns[2].addActionListener(e -> cargarVistaInbox());
        btns[3].addActionListener(e -> abrirDialogoNuevaPublicacion());
        btns[4].addActionListener(e -> cargarVistaNotificaciones());
        btns[5].addActionListener(e -> cargarVistaPerfil(sistema.getUsuarioActual().getUsername()));
        btnOut.addActionListener(e -> {
            if (chatTimer != null) chatTimer.stop();
            sistema.logout(); inicializarComponentesLogin();
        });

        return sidebar;
    }

    private JButton sidebarBtn(String label, String key) {
        boolean active = key != null && key.equals(vistaActual);
        JButton b = new JButton();
        b.setAlignmentX(Component.LEFT_ALIGNMENT);
        // Fix 5: botones más altos y anchos
        b.setMaximumSize(new Dimension(220, 58));
        b.setMinimumSize(new Dimension(220, 58));
        b.setPreferredSize(new Dimension(220, 58));
        b.setBorderPainted(false); b.setFocusPainted(false);
        b.setBackground(C_WHITE);
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        b.setHorizontalAlignment(SwingConstants.LEFT);
        // Fix 5: padding izquierdo generoso para que el icono+texto respire
        b.setBorder(BorderFactory.createEmptyBorder(0, 18, 0, 0));

        // Icono (tamaño aumentado a 26px para Fix 5)
        if (key != null) {
            Map<String,ImageIcon> map = active ? iconsBold : iconsNormal;
            if (map.containsKey(key)) {
                // Re-escalar a 26px para que sea más visible
                ImageIcon base = map.get(key);
                Image rescaled = base.getImage().getScaledInstance(26, 26, Image.SCALE_SMOOTH);
                b.setIcon(new ImageIcon(rescaled));
                b.setIconTextGap(12);
            }
        }

        b.setText(label);
        b.setFont(active ? new Font("Arial", Font.BOLD, 15) : new Font("Arial", Font.PLAIN, 15));
        b.setForeground(key == null ? C_ERROR : C_TEXT);

        // Hover
        b.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { if (!active) b.setBackground(new Color(245,245,245)); }
            public void mouseExited(MouseEvent e)  { b.setBackground(C_WHITE); }
        });

        return b;
    }

    // ════════════════════════════════════════════════════════════
    //  VISTA 4 — PERFIL
    // ════════════════════════════════════════════════════════════
    private void cargarVistaPerfil(String username) {
        vistaActual = (sistema.getUsuarioActual() != null && username.equals(sistema.getUsuarioActual().getUsername())) ? "Profile" : "";
        getContentPane().removeAll();
        setLayout(new BorderLayout());
        getContentPane().setBackground(C_BG);
        add(buildSidebar(), BorderLayout.WEST);

        JPanel content = new JPanel(new BorderLayout());
        content.setBackground(C_BG);

        // ── HEADER PERFIL ──
        JPanel hdr = new JPanel(new BorderLayout());
        hdr.setBackground(C_BG);
        hdr.setBorder(BorderFactory.createEmptyBorder(44, 60, 20, 60));

        // Foto
        JLabel lblFoto = new JLabel();
        lblFoto.setPreferredSize(new Dimension(140, 140));
        lblFoto.setIcon(cargarFotoCircular(username, 140));
        hdr.add(lblFoto, BorderLayout.WEST);

        // Info
        JPanel info = new JPanel();
        info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));
        info.setBackground(C_BG);
        info.setBorder(BorderFactory.createEmptyBorder(0, 28, 0, 0));

        // Fila 1: username + botones
        JPanel row1 = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        row1.setBackground(C_BG);

        JLabel lblUsr = new JLabel(username);
        lblUsr.setFont(new Font("Arial", Font.PLAIN, 22)); lblUsr.setForeground(C_TEXT);
        row1.add(lblUsr);

        boolean esMiPerfil = sistema.getUsuarioActual() != null && username.equals(sistema.getUsuarioActual().getUsername());
        if (esMiPerfil) {
            JButton btnEdit = buildSecondaryBtn("Editar perfil");
            btnEdit.addActionListener(e -> abrirDialogoEditarPerfil());
            row1.add(btnEdit);

            JButton btnToggle;
            if (sistema.getUsuarioActual().getEstadoCuenta() == EstadoCuenta.ACTIVO) {
                btnToggle = buildDangerBtn("Desactivar cuenta");
                btnToggle.addActionListener(e -> {
                    boolean yes = InstaDialog.showConfirm(this, "¿Desactivar tu cuenta?", "Desactivar", true);
                    if (yes) {
                        sistema.cambiarEstadoCuenta(EstadoCuenta.DESACTIVADO);
                        sistema.logout();
                        if (chatTimer != null) chatTimer.stop();
                        inicializarComponentesLogin();
                    }
                });
            } else {
                btnToggle = buildPrimaryBtn("Activar cuenta");
                btnToggle.addActionListener(e -> { sistema.cambiarEstadoCuenta(EstadoCuenta.ACTIVO); cargarVistaPerfil(username); });
            }
            row1.add(btnToggle);
        } else {
            boolean loSigo   = sistema.yaLoSigo(username);
            boolean pendiente = sistema.solicitudPendiente(username);
            JButton btnSeg;
            if (loSigo) {
                btnSeg = buildSecondaryBtn("Siguiendo");
                btnSeg.addActionListener(e -> { sistema.dejarDeSeguir(username); cargarVistaPerfil(username); });
            } else if (pendiente) {
                btnSeg = buildSecondaryBtn("Solicitud enviada");
                btnSeg.setEnabled(false);
            } else {
                btnSeg = buildPrimaryBtn("Seguir");
                btnSeg.addActionListener(e -> { sistema.seguirUsuario(username); cargarVistaPerfil(username); });
            }
            row1.add(btnSeg);
        }
        info.add(row1);
        info.add(Box.createVerticalStrut(18));

        // Fila 2: estadísticas
        JPanel row2 = new JPanel(new FlowLayout(FlowLayout.LEFT, 28, 0));
        row2.setBackground(C_BG);
        JPanel pPosts = statPanel(sistema.getCantidadPosts(username), "publicaciones");
        JPanel pFollowers = statPanel(sistema.getCantidadFollowers(username), "seguidores");
        JPanel pFollowing = statPanel(sistema.getCantidadFollowing(username), "seguidos");
        pFollowers.setCursor(new Cursor(Cursor.HAND_CURSOR));
        pFollowers.addMouseListener(click(() -> mostrarListaUsuarios(sistema.getListaFollowers(username), "Seguidores", "Followers")));
        pFollowing.setCursor(new Cursor(Cursor.HAND_CURSOR));
        pFollowing.addMouseListener(click(() -> mostrarListaUsuarios(sistema.getListaFollowing(username), "Siguiendo", "Following")));
        row2.add(pPosts); row2.add(pFollowers); row2.add(pFollowing);
        info.add(row2);
        info.add(Box.createVerticalStrut(10));

        // Fila 3: nombre real
        Usuario u = sistema.buscarUsuario(username);
        String nombre = u != null ? u.getNombreCompleto() : "";
        JLabel lblNombre = new JLabel(nombre);
        lblNombre.setFont(F_BOLD); lblNombre.setForeground(C_TEXT);
        JPanel row3 = new JPanel(new FlowLayout(FlowLayout.LEFT,0,0));
        row3.setBackground(C_BG); row3.add(lblNombre);
        info.add(row3);
        info.add(Box.createVerticalStrut(4));

        // Fila 4: datos compactos
        if (u != null) {
            String generoStr = u.getGenero() == 'M' ? "Masculino" : "Femenino";
            String datos = generoStr + "  ·  " + u.getEdad() + " años  ·  " + u.getTipoCuenta().name() + "  ·  Desde " + u.getFechaRegistro();
            JLabel lblDatos = new JLabel(datos);
            lblDatos.setFont(F_SMALL); lblDatos.setForeground(C_TEXT_LIGHT);
            JPanel row4 = new JPanel(new FlowLayout(FlowLayout.LEFT,0,0));
            row4.setBackground(C_BG); row4.add(lblDatos);
            info.add(row4);
        }

        hdr.add(info, BorderLayout.CENTER);
        content.add(hdr, BorderLayout.NORTH);

        // ── SEPARADOR ──
        JSeparator sep = new JSeparator();
        sep.setForeground(C_BORDER); sep.setBackground(C_BORDER);

        // ── GRID DE PUBLICACIONES ──
        JPanel grid = new JPanel(new GridLayout(0, 4, 3, 3));
        grid.setBackground(C_BG);
        grid.setBorder(BorderFactory.createEmptyBorder(3, 60, 20, 60));

        ArrayList<Publicacion> posts = sistema.getPublicacionesDeUsuario(username);
        if (posts.isEmpty()) {
            JLabel vacio = new JLabel("No hay publicaciones.", SwingConstants.CENTER);
            vacio.setFont(F_REGULAR); vacio.setForeground(C_TEXT_LIGHT);
            grid.add(vacio);
        } else {
            for (Publicacion p : posts) {
                JPanel cell = new JPanel(new BorderLayout());
                // Fix 3: sin fondo gris, imagen directa
                cell.setBackground(C_BG);
                cell.setOpaque(false);
                JLabel img = new JLabel();
                img.setHorizontalAlignment(SwingConstants.CENTER);
                try {
                    if (p.getRutaImagen() != null && new File(p.getRutaImagen()).exists()) {
                        img.setIcon(new ImageIcon(new ImageIcon(p.getRutaImagen()).getImage().getScaledInstance(200,200,Image.SCALE_SMOOTH)));
                    }
                } catch (Exception ignored) {}
                // Hover overlay
                JPanel overlay = new JPanel() {
                    { setOpaque(false); setVisible(false); }
                    @Override protected void paintComponent(Graphics g) {
                        g.setColor(new Color(0,0,0,90));
                        g.fillRect(0,0,getWidth(),getHeight());
                    }
                };
                cell.add(img, BorderLayout.CENTER);
                cell.addMouseListener(new MouseAdapter() {
                    public void mouseEntered(MouseEvent e) { overlay.setVisible(true); cell.repaint(); }
                    public void mouseExited(MouseEvent e)  { overlay.setVisible(false); cell.repaint(); }
                });
                grid.add(cell);
            }
        }

        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setBackground(C_BG);
        centerPanel.add(sep, BorderLayout.NORTH);
        centerPanel.add(styledScroll(grid), BorderLayout.CENTER);
        content.add(centerPanel, BorderLayout.CENTER);

        add(content, BorderLayout.CENTER);
        revalidate(); repaint();
    }

    private JPanel statPanel(int n, String label) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        p.setBackground(C_BG);
        JLabel num = new JLabel(String.valueOf(n)); num.setFont(F_BOLD); num.setForeground(C_TEXT);
        JLabel txt = new JLabel(label); txt.setFont(F_REGULAR); txt.setForeground(C_TEXT);
        p.add(num); p.add(txt);
        return p;
    }

    // ════════════════════════════════════════════════════════════
    //  VISTA 5 — BÚSQUEDA
    // ════════════════════════════════════════════════════════════
    private void cargarVistaBusqueda() {
        vistaActual = "Search";
        getContentPane().removeAll();
        setLayout(new BorderLayout());
        getContentPane().setBackground(C_BG);
        add(buildSidebar(), BorderLayout.WEST);

        JPanel main = new JPanel(new BorderLayout());
        main.setBackground(C_BG);
        main.setBorder(BorderFactory.createEmptyBorder(40,40,40,40));

        // Barra de búsqueda
        JPanel searchBar = new JPanel(new BorderLayout());
        searchBar.setBackground(C_FIELD);
        searchBar.setBorder(new LineBorder(C_BORDER,1,true));
        JTextField txtBuscar = new JTextField("Buscar usuario o #hashtag...");
        txtBuscar.setFont(F_REGULAR); txtBuscar.setBackground(C_FIELD);
        txtBuscar.setForeground(C_PLACEHOLDER);
        txtBuscar.setBorder(BorderFactory.createEmptyBorder(11,16,11,12));
        txtBuscar.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent e) {
                if (txtBuscar.getText().equals("Buscar usuario o #hashtag...")) { txtBuscar.setText(""); txtBuscar.setForeground(C_TEXT); }
            }
            public void focusLost(FocusEvent e) {
                if (txtBuscar.getText().isEmpty()) { txtBuscar.setText("Buscar usuario o #hashtag..."); txtBuscar.setForeground(C_PLACEHOLDER); }
            }
        });
        JButton btnBuscar = buildPrimaryBtn("Buscar");
        btnBuscar.setPreferredSize(new Dimension(90, 40));
        searchBar.add(txtBuscar, BorderLayout.CENTER);
        searchBar.add(btnBuscar, BorderLayout.EAST);
        main.add(searchBar, BorderLayout.NORTH);

        JPanel results = new JPanel();
        results.setLayout(new BoxLayout(results, BoxLayout.Y_AXIS));
        results.setBackground(C_BG);
        results.setBorder(BorderFactory.createEmptyBorder(20,0,0,0));
        main.add(styledScroll(results), BorderLayout.CENTER);

        add(main, BorderLayout.CENTER);

        ActionListener doSearch = e -> {
            String q = txtBuscar.getText().trim();
            if (q.isEmpty() || q.equals("Buscar usuario o #hashtag...")) return;
            results.removeAll();
            if (q.startsWith("#")) {
                ArrayList<Publicacion> posts = sistema.buscarPorHashtag(q);
                if (posts.isEmpty()) { results.add(emptyLabel("Sin resultados para " + q)); }
                else for (Publicacion p : posts) { results.add(buildPost(p)); results.add(Box.createVerticalStrut(10)); }
            } else {
                ArrayList<Usuario> users = sistema.buscarUsuarios(q);
                if (users.isEmpty()) { results.add(emptyLabel("No se encontraron usuarios.")); }
                else for (Usuario u : users) results.add(buildUserRow(u));
            }
            results.revalidate(); results.repaint();
        };
        btnBuscar.addActionListener(doSearch);
        txtBuscar.addActionListener(doSearch);

        revalidate(); repaint();
    }

    private JPanel buildUserRow(Usuario u) {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(C_WHITE);
        p.setMaximumSize(new Dimension(700, 62));
        p.setBorder(BorderFactory.createCompoundBorder(new LineBorder(C_BORDER,1), BorderFactory.createEmptyBorder(12,14,12,14)));

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT,10,0));
        left.setBackground(C_WHITE);
        JLabel foto = new JLabel(); foto.setIcon(cargarFotoCircular(u.getUsername(),36));
        JLabel nombre = new JLabel(u.getUsername()); nombre.setFont(F_BOLD); nombre.setForeground(C_TEXT);
        nombre.setCursor(new Cursor(Cursor.HAND_CURSOR));
        nombre.addMouseListener(click(() -> cargarVistaPerfil(u.getUsername())));
        left.add(foto); left.add(nombre);

        JButton btnVer = buildPrimaryBtn("Ver perfil");
        btnVer.setPreferredSize(new Dimension(100, 30));
        btnVer.addActionListener(e -> cargarVistaPerfil(u.getUsername()));

        p.add(left, BorderLayout.WEST);
        p.add(btnVer, BorderLayout.EAST);
        return p;
    }

    // ════════════════════════════════════════════════════════════
    //  VISTA 6 — INBOX
    // ════════════════════════════════════════════════════════════
    private void cargarVistaInbox() {
        vistaActual = "Messages";
        if (chatTimer != null) chatTimer.stop();
        getContentPane().removeAll();
        setLayout(new BorderLayout());
        add(buildSidebar(), BorderLayout.WEST);

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        split.setDividerLocation(280);
        split.setBorder(null);
        split.setDividerSize(1);

        // ── LISTA CHATS ──
        JPanel listPanel = new JPanel(new BorderLayout());
        listPanel.setBackground(C_WHITE);
        listPanel.setBorder(new MatteBorder(0,0,0,1,C_BORDER));

        JPanel listHeader = new JPanel(new BorderLayout());
        listHeader.setBackground(C_WHITE);
        listHeader.setBorder(BorderFactory.createCompoundBorder(
            new MatteBorder(0,0,1,0,C_BORDER),
            BorderFactory.createEmptyBorder(16,16,16,16)
        ));
        JLabel lblMsgs = new JLabel(sistema.getUsuarioActual().getUsername());
        lblMsgs.setFont(F_BOLD); lblMsgs.setForeground(C_TEXT);
        // Fix 7: ícono "+" en lugar del lápiz vacío
        JButton btnNuevo = new JButton("+");
        btnNuevo.setFont(new Font("Arial", Font.BOLD, 22));
        btnNuevo.setBorderPainted(false); btnNuevo.setContentAreaFilled(false);
        btnNuevo.setFocusPainted(false); btnNuevo.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnNuevo.setForeground(C_TEXT);
        listHeader.add(lblMsgs, BorderLayout.WEST);
        listHeader.add(btnNuevo, BorderLayout.EAST);
        listPanel.add(listHeader, BorderLayout.NORTH);

        JPanel chatList = new JPanel();
        chatList.setLayout(new BoxLayout(chatList, BoxLayout.Y_AXIS));
        chatList.setBackground(C_WHITE);
        JScrollPane chatScroll = styledScroll(chatList);
        listPanel.add(chatScroll, BorderLayout.CENTER);

        // Placeholder derecha
        JPanel chatPanel = new JPanel(new BorderLayout());
        chatPanel.setBackground(C_BG);
        JLabel ph = new JLabel("Selecciona un mensaje", SwingConstants.CENTER);
        ph.setFont(F_REGULAR); ph.setForeground(C_TEXT_LIGHT);
        chatPanel.add(ph, BorderLayout.CENTER);

        split.setLeftComponent(listPanel);
        split.setRightComponent(chatPanel);
        add(split, BorderLayout.CENTER);

        for (String user : sistema.getChatsRecientes()) {
            JPanel row = new JPanel(new BorderLayout());
            row.setBackground(C_WHITE);
            row.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));
            row.setMaximumSize(new Dimension(280, 68));

            JLabel fotoLbl = new JLabel(); fotoLbl.setIcon(cargarFotoCircular(user, 44));
            row.add(fotoLbl, BorderLayout.WEST);
            JLabel nomLbl = new JLabel("  " + user); nomLbl.setFont(F_BOLD); nomLbl.setForeground(C_TEXT);
            row.add(nomLbl, BorderLayout.CENTER);

            row.setCursor(new Cursor(Cursor.HAND_CURSOR));
            row.addMouseListener(new MouseAdapter() {
                public void mouseClicked(MouseEvent e) { lastMessageCount = 0; mostrarChatLive(chatPanel, user); }
                public void mouseEntered(MouseEvent e) { row.setBackground(C_HOVER); }
                public void mouseExited(MouseEvent e)  { row.setBackground(C_WHITE); }
            });

            chatList.add(row);
            JSeparator s = new JSeparator(); s.setForeground(C_BORDER); s.setBackground(C_BORDER);
            chatList.add(s);
        }

        btnNuevo.addActionListener(e -> {
            String dest = InstaDialog.showInput(this, "Nuevo mensaje", "Nombre de usuario");
            if (dest != null) {
                if (sistema.buscarUsuario(dest) == null) InstaDialog.showMessage(this, "Usuario no encontrado.", true);
                else if (!sistema.puedeEnviarMensaje(dest)) InstaDialog.showMessage(this, "No puedes enviarle mensajes.", true);
                else { lastMessageCount = 0; mostrarChatLive(chatPanel, dest); }
            }
        });

        revalidate(); repaint();
    }

    private void mostrarChatLive(JPanel chatPanel, String otro) {
        if (chatTimer != null) chatTimer.stop();
        chatPanel.removeAll();
        chatPanel.setLayout(new BorderLayout());
        chatPanel.setBackground(C_BG);

        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(C_WHITE);
        header.setBorder(BorderFactory.createCompoundBorder(
            new MatteBorder(0,0,1,0,C_BORDER),
            BorderFactory.createEmptyBorder(12,16,12,16)
        ));
        JPanel headerLeft = new JPanel(new FlowLayout(FlowLayout.LEFT,10,0));
        headerLeft.setBackground(C_WHITE);
        JLabel foto = new JLabel(); foto.setIcon(cargarFotoCircular(otro, 36));
        JLabel nomLbl = new JLabel(otro); nomLbl.setFont(F_BOLD); nomLbl.setForeground(C_TEXT);
        nomLbl.setCursor(new Cursor(Cursor.HAND_CURSOR));
        nomLbl.addMouseListener(click(() -> cargarVistaPerfil(otro)));
        headerLeft.add(foto); headerLeft.add(nomLbl);
        header.add(headerLeft, BorderLayout.WEST);

        JButton btnDel = new JButton("Eliminar chat");
        btnDel.setFont(F_SMALL); btnDel.setForeground(C_ERROR);
        btnDel.setBorderPainted(false); btnDel.setContentAreaFilled(false);
        btnDel.setFocusPainted(false); btnDel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        header.add(btnDel, BorderLayout.EAST);

        // Mensajes
        JPanel msgPanel = new JPanel();
        msgPanel.setLayout(new BoxLayout(msgPanel, BoxLayout.Y_AXIS));
        msgPanel.setBackground(C_BG);
        // Fix 1: chat NO hace scroll al top automático (usa su propia lógica)
        JScrollPane msgScroll = styledScroll(msgPanel, false);

        // Footer input
        JPanel footer = new JPanel(new BorderLayout());
        footer.setBackground(C_WHITE);
        footer.setBorder(BorderFactory.createCompoundBorder(
            new MatteBorder(1,0,0,0,C_BORDER),
            BorderFactory.createEmptyBorder(10,12,10,12)
        ));
        JTextField txtInput = new JTextField();
        txtInput.setFont(F_REGULAR); txtInput.setBackground(C_FIELD);
        txtInput.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(C_BORDER,1,true),
            BorderFactory.createEmptyBorder(9,14,9,14)
        ));
        txtInput.putClientProperty("JTextField.placeholderText","Escribe un mensaje...");

        JButton btnSend = new JButton("Enviar");
        btnSend.setFont(F_BOLD); btnSend.setForeground(C_BLUE);
        btnSend.setBorderPainted(false); btnSend.setContentAreaFilled(false);
        btnSend.setFocusPainted(false); btnSend.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JButton btnSticker = new JButton("😀");
        btnSticker.setFont(new Font("Arial",Font.PLAIN,18));
        btnSticker.setBackground(C_FIELD);
        btnSticker.setBorderPainted(false); btnSticker.setFocusPainted(false);
        btnSticker.setCursor(new Cursor(Cursor.HAND_CURSOR));

        footer.add(btnSticker, BorderLayout.WEST);
        footer.add(txtInput,   BorderLayout.CENTER);
        footer.add(btnSend,    BorderLayout.EAST);

        chatPanel.add(header,    BorderLayout.NORTH);
        chatPanel.add(msgScroll, BorderLayout.CENTER);
        chatPanel.add(footer,    BorderLayout.SOUTH);

        // Eventos
        ActionListener send = e -> {
            if (!txtInput.getText().isEmpty()) {
                sistema.enviarMensaje(otro, txtInput.getText(), "TEXTO");
                txtInput.setText("");
                lastMessageCount = 0;
                refrescarMensajes(msgPanel, otro);
            }
        };
        btnSend.addActionListener(send);
        txtInput.addActionListener(send);

        btnDel.addActionListener(e -> {
            boolean yes = InstaDialog.showConfirm(chatPanel, "¿Eliminar historial con " + otro + "?", "Eliminar", true);
            if (yes) { sistema.eliminarConversacion(otro); lastMessageCount = 0; refrescarMensajes(msgPanel, otro); }
        });

        btnSticker.addActionListener(ev -> {
            ArrayList<String> stickers = sistema.getTodosStickers(sistema.getUsuarioActual().getUsername());

            // Fix 6: dialogo con grid visual de previews, no lista de nombres
            JDialog sd = new JDialog(this, "Stickers", true);
            sd.setSize(360, 340); sd.setLocationRelativeTo(chatPanel);
            sd.setLayout(new BorderLayout());
            sd.getContentPane().setBackground(C_WHITE);

            JPanel sh = buildDialogHeader("Stickers", sd);
            sd.add(sh, BorderLayout.NORTH);

            // Grid de previews
            JPanel grid = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
            grid.setBackground(C_WHITE);
            grid.setBorder(BorderFactory.createEmptyBorder(8,8,8,8));

            // Primer botón: importar
            JButton btnImport = new JButton("+");
            btnImport.setFont(new Font("Arial", Font.BOLD, 22));
            btnImport.setPreferredSize(new Dimension(72, 72));
            btnImport.setBackground(new Color(245,245,245));
            btnImport.setBorderPainted(true); btnImport.setFocusPainted(false);
            btnImport.setToolTipText("Importar nuevo sticker");
            btnImport.setCursor(new Cursor(Cursor.HAND_CURSOR));
            btnImport.addActionListener(se -> {
                JFileChooser fc = new JFileChooser();
                fc.setFileFilter(new FileNameExtensionFilter("Imágenes","png","jpg","jpeg"));
                if (fc.showOpenDialog(sd) == JFileChooser.APPROVE_OPTION)
                    sistema.guardarStickerPersonal(fc.getSelectedFile(), sistema.getUsuarioActual().getUsername());
                sd.dispose();
            });
            grid.add(btnImport);

            // Un botón por sticker con su preview
            for (String r : stickers) {
                JButton btnS = new JButton();
                btnS.setPreferredSize(new Dimension(72, 72));
                btnS.setBorderPainted(true); btnS.setFocusPainted(false);
                btnS.setCursor(new Cursor(Cursor.HAND_CURSOR));
                btnS.setBackground(C_WHITE);
                try {
                    File f = new File(r);
                    if (f.exists()) {
                        ImageIcon ic = new ImageIcon(new ImageIcon(r).getImage().getScaledInstance(62,62,Image.SCALE_SMOOTH));
                        btnS.setIcon(ic);
                    } else { btnS.setText("?"); }
                } catch (Exception ignored) { btnS.setText("?"); }
                btnS.addActionListener(se -> {
                    sistema.enviarMensaje(otro, r, "STICKER");
                    lastMessageCount = 0;
                    refrescarMensajes(msgPanel, otro);
                    sd.dispose();
                });
                grid.add(btnS);
            }

            JScrollPane sp = styledScroll(grid);
            sp.setPreferredSize(new Dimension(340, 240));
            sd.add(sp, BorderLayout.CENTER);
            sd.setVisible(true);
        });

        lastMessageCount = 0;
        refrescarMensajes(msgPanel, otro);
        chatTimer = new Timer(2000, ev -> refrescarMensajes(msgPanel, otro));
        chatTimer.start();

        chatPanel.revalidate(); chatPanel.repaint();
    }

    private void refrescarMensajes(JPanel panel, String otro) {
        sistema.marcarComoLeido(otro);
        ArrayList<Mensaje> hist = sistema.getConversacion(otro);
        if (hist.size() == lastMessageCount) return;
        lastMessageCount = hist.size();

        JScrollPane scrollP = (JScrollPane) SwingUtilities.getAncestorOfClass(JScrollPane.class, panel);
        JScrollBar vbar = scrollP != null ? scrollP.getVerticalScrollBar() : null;
        int curVal = vbar != null ? vbar.getValue() : 0;
        boolean atBottom = vbar == null || vbar.getValue() >= vbar.getMaximum() - 200;

        panel.removeAll();

        Mensaje lastSent = null;
        for (Mensaje m : hist) {
            boolean mio = m.getEmisor().equals(sistema.getUsuarioActual().getUsername());
            if (mio) lastSent = m;

            JPanel row = new JPanel(new BorderLayout());
            row.setOpaque(false);
            row.setBorder(BorderFactory.createEmptyBorder(2, 12, 2, 12));

            JPanel bubble = new JPanel(new BorderLayout());

            boolean esSticker = m instanceof instagram.MensajeSticker;
            // Fix 8: determinar color de texto antes de crear el contenido
            boolean isMioTextBubble = mio && !esSticker && m.getContenido() != null && !m.getContenido().startsWith("SHARE|");
            Color txtColor = isMioTextBubble ? Color.WHITE : C_TEXT;

            if (esSticker) {
                JLabel img = new JLabel();
                try {
                    File f = new File(m.getContenido());
                    if (f.exists()) img.setIcon(new ImageIcon(new ImageIcon(m.getContenido()).getImage().getScaledInstance(90,90,Image.SCALE_SMOOTH)));
                    else img.setText("[Sticker]");
                } catch (Exception ignored) { img.setText("[Sticker]"); }
                bubble.add(img, BorderLayout.CENTER);
                bubble.setBackground(C_WHITE);
            } else {
                String texto = m.getContenido();
                if (texto.startsWith("SHARE|")) {
                    String[] d = texto.split("\\|");
                    JPanel card = new JPanel(new BorderLayout());
                    card.setBackground(C_FIELD); card.setBorder(new LineBorder(C_BORDER));
                    JLabel imgL = new JLabel();
                    try { File f=new File(d[2]); if(f.exists()) imgL.setIcon(new ImageIcon(new ImageIcon(d[2]).getImage().getScaledInstance(120,120,Image.SCALE_SMOOTH))); } catch(Exception ignored){}
                    card.add(imgL, BorderLayout.WEST);
                    JPanel info = new JPanel(new GridLayout(2,1)); info.setBackground(C_WHITE);
                    info.add(new JLabel("  De: " + d[1]));
                    JTextArea ta = new JTextArea(d.length>3?d[3]:""); ta.setWrapStyleWord(true); ta.setLineWrap(true); ta.setOpaque(false); ta.setEditable(false); ta.setFont(F_SMALL);
                    info.add(ta); card.add(info, BorderLayout.CENTER);
                    card.setCursor(new Cursor(Cursor.HAND_CURSOR));
                    card.addMouseListener(click(() -> {
                        try { File f=new File(d[2]); if(f.exists()){ JDialog dd=new JDialog((Frame)null,"Post",true); dd.setSize(660,700); dd.add(new JScrollPane(new JLabel(new ImageIcon(new ImageIcon(d[2]).getImage().getScaledInstance(600,600,Image.SCALE_SMOOTH))))); dd.setLocationRelativeTo(null); dd.setVisible(true); } } catch(Exception ignored){}
                    }));
                    bubble.add(card, BorderLayout.CENTER); bubble.setBackground(C_WHITE);
                } else {
                    // Fix 8: texto con color correcto (blanco si burbuja azul)
                    JTextArea ta = new JTextArea(texto);
                    ta.setLineWrap(true); ta.setWrapStyleWord(true);
                    ta.setFont(F_REGULAR); ta.setEditable(false); ta.setOpaque(false);
                    ta.setForeground(txtColor);
                    bubble.add(ta, BorderLayout.CENTER);
                }
            }

            if (mio) {
                boolean isTxtBubble = !esSticker && (m.getContenido()==null||!m.getContenido().startsWith("SHARE|"));
                Color bubbleColor = isTxtBubble ? C_BLUE : C_WHITE;
                final Color bc = bubbleColor;
                // Fix 8: burbuja con esquinas redondeadas reales via paintComponent
                JPanel wrapper = new JPanel(new BorderLayout()) {
                    @Override protected void paintComponent(Graphics g) {
                        Graphics2D g2=(Graphics2D)g.create();
                        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                        g2.setColor(bc);
                        g2.fill(new RoundRectangle2D.Double(0,0,getWidth(),getHeight(),20,20));
                        g2.dispose();
                    }
                };
                wrapper.setOpaque(false);
                bubble.setOpaque(false);
                bubble.setBorder(BorderFactory.createEmptyBorder(8,12,8,12));
                wrapper.add(bubble, BorderLayout.CENTER);
                wrapper.setMaximumSize(new Dimension(340, Integer.MAX_VALUE));
                row.add(wrapper, BorderLayout.EAST);
            } else {
                boolean isTxtBubble = !esSticker && (m.getContenido()==null||!m.getContenido().startsWith("SHARE|"));
                // Fix 8: burbuja recibida con fondo blanco y borde, esquinas redondeadas
                final Color bc = C_WHITE;
                JPanel wrapper = new JPanel(new BorderLayout()) {
                    @Override protected void paintComponent(Graphics g) {
                        Graphics2D g2=(Graphics2D)g.create();
                        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                        g2.setColor(bc);
                        g2.fill(new RoundRectangle2D.Double(0,0,getWidth(),getHeight(),20,20));
                        if (isTxtBubble) {
                            g2.setColor(C_BORDER);
                            g2.setStroke(new BasicStroke(1f));
                            g2.draw(new RoundRectangle2D.Double(0.5,0.5,getWidth()-1,getHeight()-1,20,20));
                        }
                        g2.dispose();
                    }
                };
                wrapper.setOpaque(false);
                bubble.setOpaque(false);
                bubble.setBorder(BorderFactory.createEmptyBorder(8,12,8,12));
                wrapper.add(bubble, BorderLayout.CENTER);
                wrapper.setMaximumSize(new Dimension(340, Integer.MAX_VALUE));
                row.add(wrapper, BorderLayout.WEST);
            }
            panel.add(row);

            // Timestamp
            JLabel ts = new JLabel(m.getFecha() + "  " + m.getHoraFormateada());
            ts.setFont(new Font("Arial",Font.PLAIN,9)); ts.setForeground(C_TEXT_LIGHT);
            ts.setBorder(BorderFactory.createEmptyBorder(0,12,4,12));
            JPanel tsRow = new JPanel(new BorderLayout()); tsRow.setOpaque(false);
            tsRow.add(ts, mio ? BorderLayout.EAST : BorderLayout.WEST);
            panel.add(tsRow);
        }

        // Estado leído
        if (lastSent != null) {
            boolean leido = "LEIDO".equals(lastSent.getEstado());
            JLabel st = new JLabel(leido ? "Leído ✓✓" : "Enviado ✓");
            st.setFont(new Font("Arial",Font.ITALIC,10));
            st.setForeground(leido ? C_BLUE : C_TEXT_LIGHT);
            st.setBorder(BorderFactory.createEmptyBorder(0,12,10,12));
            JPanel sr = new JPanel(new BorderLayout()); sr.setOpaque(false); sr.add(st, BorderLayout.EAST);
            panel.add(sr);
        }

        panel.revalidate(); panel.repaint();
        SwingUtilities.invokeLater(() -> {
            if (vbar != null) vbar.setValue(atBottom ? vbar.getMaximum() : curVal);
        });
    }

    // ════════════════════════════════════════════════════════════
    //  VISTA 7 — NOTIFICACIONES
    // ════════════════════════════════════════════════════════════
    private void cargarVistaNotificaciones() {
        vistaActual = "Notifications";
        if (chatTimer != null) chatTimer.stop();
        getContentPane().removeAll();
        setLayout(new BorderLayout());
        add(buildSidebar(), BorderLayout.WEST);

        JPanel main = new JPanel(new BorderLayout());
        main.setBackground(C_BG);
        main.setBorder(BorderFactory.createEmptyBorder(40,50,40,50));

        JLabel title = new JLabel("Actividad"); title.setFont(F_H1); title.setForeground(C_TEXT);
        title.setBorder(BorderFactory.createEmptyBorder(0,0,20,0));
        main.add(title, BorderLayout.NORTH);

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(C_BG);
        main.add(styledScroll(content), BorderLayout.CENTER);
        add(main, BorderLayout.CENTER);

        boolean hay = false;

        // Solicitudes
        ArrayList<String> solic = sistema.getSolicitudes();
        if (!solic.isEmpty()) {
            hay = true;
            content.add(sectionTitle("Solicitudes de seguimiento"));
            for (String u : solic) { content.add(buildSolicitudRow(u)); content.add(Box.createVerticalStrut(6)); }
            content.add(Box.createVerticalStrut(24));
        }

        // Generales
        ArrayList<String> notifs = sistema.getNotificacionesGenerales();
        if (!notifs.isEmpty()) {
            hay = true;
            content.add(sectionTitle("Hoy"));
            for (String line : notifs) {
                String[] d = line.split("\\|");
                JPanel row = notifRow();
                JLabel ico = new JLabel(); ico.setFont(new Font("Arial",Font.PLAIN,16));
                JLabel txt = new JLabel(); txt.setFont(F_REGULAR); txt.setForeground(C_TEXT);
                if ("SEGUIDOR".equals(d[0])) {
                    ico.setText("👤"); txt.setText("<html><b>" + d[1] + "</b> empezó a seguirte.</html>");
                    txt.setCursor(new Cursor(Cursor.HAND_CURSOR));
                    txt.addMouseListener(click(() -> cargarVistaPerfil(d[1])));
                } else if ("MENCION".equals(d[0])) {
                    ico.setText("💬"); txt.setText("<html><b>" + d[1] + "</b> te mencionó.</html>");
                    txt.setCursor(new Cursor(Cursor.HAND_CURSOR));
                    txt.addMouseListener(click(() -> cargarVistaPerfil(d[1])));
                }
                row.add(ico); row.add(Box.createHorizontalStrut(10)); row.add(txt);
                content.add(row); content.add(Box.createVerticalStrut(6));
            }
            content.add(Box.createVerticalStrut(24));
        }

        // Likes
        ArrayList<String> likes = sistema.getNotificacionesLikes();
        if (!likes.isEmpty()) {
            hay = true;
            content.add(sectionTitle("Me gusta en tus publicaciones"));
            for (int i = likes.size()-1; i>=0; i--) {
                String[] d = likes.get(i).split("\\|");
                if (d.length >= 3) {
                    JPanel row = notifRow();
                    JLabel ico = new JLabel("❤️"); ico.setFont(new Font("Arial",Font.PLAIN,16));
                    JLabel txt = new JLabel("<html>A <b>" + d[2] + "</b> le gustó tu publicación del " + d[1] + "</html>");
                    txt.setFont(F_REGULAR); txt.setForeground(C_TEXT);
                    row.add(ico); row.add(Box.createHorizontalStrut(10)); row.add(txt);
                    content.add(row); content.add(Box.createVerticalStrut(6));
                }
            }
        }

        if (!hay) {
            JLabel nada = emptyLabel("No hay notificaciones nuevas.");
            nada.setAlignmentX(Component.CENTER_ALIGNMENT);
            content.add(Box.createVerticalStrut(80));
            content.add(nada);
        }

        revalidate(); repaint();
    }

    private JPanel notifRow() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        p.setBackground(C_WHITE);
        p.setMaximumSize(new Dimension(600, 52));
        p.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(C_BORDER,1), BorderFactory.createEmptyBorder(12,16,12,16)
        ));
        return p;
    }

    private JLabel sectionTitle(String text) {
        JLabel l = new JLabel(text); l.setFont(F_BOLD); l.setForeground(C_TEXT);
        l.setBorder(BorderFactory.createEmptyBorder(0,0,8,0));
        return l;
    }

    private JPanel buildSolicitudRow(String username) {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(C_WHITE);
        p.setMaximumSize(new Dimension(600, 56));
        p.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(C_BORDER,1), BorderFactory.createEmptyBorder(10,14,10,14)
        ));
        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT,8,0)); left.setBackground(C_WHITE);
        left.add(new JLabel()); // espacio foto
        JLabel lbl = new JLabel("<html><b>" + username + "</b> quiere seguirte.</html>");
        lbl.setFont(F_REGULAR); lbl.setForeground(C_TEXT); left.add(lbl);

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT,8,0)); btns.setBackground(C_WHITE);
        JButton ok = buildPrimaryBtn("Confirmar"); ok.setPreferredSize(new Dimension(100,30));
        JButton no = buildSecondaryBtn("Eliminar"); no.setPreferredSize(new Dimension(90,30));
        ok.addActionListener(e -> { sistema.aceptarSolicitud(username); cargarVistaNotificaciones(); });
        no.addActionListener(e -> { sistema.rechazarSolicitud(username); cargarVistaNotificaciones(); });
        btns.add(ok); btns.add(no);

        p.add(left, BorderLayout.WEST);
        p.add(btns, BorderLayout.EAST);
        return p;
    }

    // ════════════════════════════════════════════════════════════
    //  EDITAR PERFIL
    // ════════════════════════════════════════════════════════════
    private void abrirDialogoEditarPerfil() {
        JDialog d = new JDialog(this, "Editar perfil", true);
        d.setSize(420, 360);
        d.setLocationRelativeTo(this);
        d.setLayout(new BorderLayout());
        d.getContentPane().setBackground(C_WHITE);

        JPanel header = buildDialogHeader("Editar perfil", d);
        d.add(header, BorderLayout.NORTH);

        JPanel body = new JPanel();
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setBackground(C_WHITE);
        body.setBorder(BorderFactory.createEmptyBorder(20,24,20,24));

        JLabel lNom = new JLabel("Nombre completo"); lNom.setFont(F_SMALL); lNom.setForeground(C_TEXT_LIGHT);
        JTextField txtNom = buildField(sistema.getUsuarioActual().getNombreCompleto());
        txtNom.setText(sistema.getUsuarioActual().getNombreCompleto());
        txtNom.setForeground(C_TEXT);

        JLabel lPass = new JLabel("Nueva contraseña (dejar vacío para no cambiar)"); lPass.setFont(F_SMALL); lPass.setForeground(C_TEXT_LIGHT);
        JPasswordField txtPass = new JPasswordField();
        txtPass.setBackground(C_FIELD); txtPass.setFont(F_REGULAR);
        txtPass.setBorder(BorderFactory.createCompoundBorder(new LineBorder(C_BORDER,1,true), BorderFactory.createEmptyBorder(9,12,9,12)));

        JButton btnFoto = buildSecondaryBtn("Cambiar foto de perfil");
        final String[] nuevaFoto = {null};
        btnFoto.addActionListener(e -> {
            JFileChooser fc = new JFileChooser();
            fc.setFileFilter(new FileNameExtensionFilter("Imágenes","jpg","png","jpeg"));
            if (fc.showOpenDialog(d) == JFileChooser.APPROVE_OPTION) {
                String ruta = sistema.procesarImagenPerfil(fc.getSelectedFile(), sistema.getUsuarioActual().getUsername(), "profile_"+System.currentTimeMillis());
                if (ruta != null) { nuevaFoto[0] = ruta; btnFoto.setText("✓ Foto seleccionada"); }
            }
        });

        body.add(lNom); body.add(Box.createVerticalStrut(4));
        body.add(txtNom); body.add(Box.createVerticalStrut(14));
        body.add(lPass); body.add(Box.createVerticalStrut(4));
        body.add(txtPass); body.add(Box.createVerticalStrut(16));
        body.add(btnFoto);
        d.add(body, BorderLayout.CENTER);

        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT,12,12));
        footer.setBackground(C_WHITE);
        footer.setBorder(new MatteBorder(1,0,0,0,C_BORDER));
        JButton btnCancel = buildSecondaryBtn("Cancelar");
        JButton btnSave   = buildPrimaryBtn("Guardar");
        footer.add(btnCancel); footer.add(btnSave);
        d.add(footer, BorderLayout.SOUTH);

        btnCancel.addActionListener(e -> d.dispose());
        btnSave.addActionListener(e -> {
            String nom = txtNom.getText().trim();
            if (nom.isEmpty()) { InstaDialog.showMessage(d,"El nombre no puede estar vacío.",true); return; }
            String pass = new String(txtPass.getPassword()).trim();
            boolean ok = sistema.actualizarDatosUsuario(nom, pass);
            if (nuevaFoto[0] != null) sistema.actualizarFotoPerfil(sistema.getUsuarioActual().getUsername(), nuevaFoto[0]);
            if (ok) { d.dispose(); cargarVistaPerfil(sistema.getUsuarioActual().getUsername()); }
            else InstaDialog.showMessage(d,"Error al guardar.",true);
        });

        d.setVisible(true);
    }

    // ════════════════════════════════════════════════════════════
    //  LISTA SEGUIDORES / SIGUIENDO
    // ════════════════════════════════════════════════════════════
    private void mostrarListaUsuarios(ArrayList<String> usuarios, String titulo, String tipo) {
        if (usuarios.isEmpty()) { InstaDialog.showMessage(this, "La lista está vacía."); return; }

        JDialog d = new JDialog(this, titulo, true);
        d.setSize(360, 520); d.setLocationRelativeTo(this);
        d.setLayout(new BorderLayout());
        d.getContentPane().setBackground(C_WHITE);

        JPanel header = buildDialogHeader(titulo, d);
        d.add(header, BorderLayout.NORTH);

        JPanel list = new JPanel();
        list.setLayout(new BoxLayout(list, BoxLayout.Y_AXIS));
        list.setBackground(C_WHITE);
        list.setBorder(BorderFactory.createEmptyBorder(8,8,8,8));

        ArrayList<String> inv = new ArrayList<>(usuarios);
        Collections.reverse(inv);
        boolean esFollowers = "Followers".equals(tipo);

        for (String usr : inv) {
            JPanel row = new JPanel(new BorderLayout());
            row.setBackground(C_WHITE);
            row.setMaximumSize(new Dimension(340, 60));
            row.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(C_BORDER,1,true),
                BorderFactory.createEmptyBorder(10,12,10,12)
            ));

            JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT,8,0)); left.setBackground(C_WHITE);
            left.add(new JLabel(cargarFotoCircular(usr,36)));
            JLabel lbl = new JLabel(usr); lbl.setFont(F_BOLD); lbl.setForeground(C_TEXT);
            left.add(lbl); row.add(left, BorderLayout.WEST);

            JButton btn;
            if (esFollowers) {
                btn = buildSecondaryBtn("Eliminar");
                btn.setPreferredSize(new Dimension(90,30));
                btn.addActionListener(e -> {
                    boolean yes = InstaDialog.showConfirm(d,"¿Eliminar a " + usr + "?","Eliminar",true);
                    if (yes) {
                        sistema.eliminarSeguidor(usr); d.dispose();
                        mostrarListaUsuarios(sistema.getListaFollowers(sistema.getUsuarioActual().getUsername()), "Seguidores","Followers");
                        cargarVistaPerfil(sistema.getUsuarioActual().getUsername());
                    }
                });
            } else {
                btn = buildPrimaryBtn("Ver perfil");
                btn.setPreferredSize(new Dimension(100,30));
                btn.addActionListener(e -> { d.dispose(); cargarVistaPerfil(usr); });
            }
            JPanel rp = new JPanel(new FlowLayout(FlowLayout.RIGHT,0,0)); rp.setBackground(C_WHITE); rp.add(btn);
            row.add(rp, BorderLayout.EAST);

            list.add(row); list.add(Box.createVerticalStrut(6));
        }

        d.add(styledScroll(list), BorderLayout.CENTER);
        d.setVisible(true);
    }

    // ════════════════════════════════════════════════════════════
    //  BÚSQUEDA POR HASHTAG
    // ════════════════════════════════════════════════════════════
    private void cargarVistaBusquedaHashtag(String hashtag) {
        vistaActual = "Search";
        getContentPane().removeAll();
        setLayout(new BorderLayout());
        getContentPane().setBackground(C_BG);
        add(buildSidebar(), BorderLayout.WEST);

        JPanel main = new JPanel(new BorderLayout());
        main.setBackground(C_BG);
        main.setBorder(BorderFactory.createEmptyBorder(40,40,40,40));

        JLabel title = new JLabel("Resultados: " + hashtag); title.setFont(F_H2); title.setForeground(C_TEXT);
        title.setBorder(BorderFactory.createEmptyBorder(0,0,20,0));
        main.add(title, BorderLayout.NORTH);

        JPanel results = new JPanel();
        results.setLayout(new BoxLayout(results, BoxLayout.Y_AXIS));
        results.setBackground(C_BG);
        main.add(styledScroll(results), BorderLayout.CENTER);
        add(main, BorderLayout.CENTER);

        ArrayList<Publicacion> posts = sistema.buscarPorHashtag(hashtag);
        if (posts.isEmpty()) results.add(emptyLabel("Sin publicaciones con " + hashtag));
        else for (Publicacion p : posts) { results.add(buildPost(p)); results.add(Box.createVerticalStrut(10)); }

        revalidate(); repaint();
    }

    // ════════════════════════════════════════════════════════════
    //  FOTOS DE PERFIL CIRCULARES
    // ════════════════════════════════════════════════════════════
    private ImageIcon cargarFotoCircular(String username, int d) {
        Usuario u = sistema.buscarUsuario(username);
        if (u != null) {
            String ruta = u.getFotoPerfil();
            if (ruta != null && !ruta.isEmpty() && !ruta.equals("null")) {
                File f = new File(ruta);
                if (f.exists()) {
                    try {
                        BufferedImage bi = javax.imageio.ImageIO.read(f);
                        if (bi != null) return circularIcon(new ImageIcon(bi), d);
                    } catch (Exception ignored) {}
                }
            }
        }
        return avatarDefault(username, d);
    }

    private ImageIcon circularIcon(ImageIcon src, int d) {
        BufferedImage out = new BufferedImage(d, d, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = out.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setClip(new Ellipse2D.Double(0,0,d,d));
        g2.drawImage(src.getImage().getScaledInstance(d,d,Image.SCALE_SMOOTH),0,0,null);
        g2.setClip(null);
        g2.setColor(C_BORDER); g2.setStroke(new BasicStroke(1f));
        g2.drawOval(0,0,d-1,d-1);
        g2.dispose();
        return new ImageIcon(out);
    }

    private ImageIcon avatarDefault(String username, int d) {
        BufferedImage img = new BufferedImage(d,d,BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = img.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(new Color(239,239,239)); g2.fillOval(0,0,d,d);
        g2.setColor(new Color(142,142,142));
        g2.setFont(new Font("Arial",Font.BOLD,d/2));
        String l = username.isEmpty() ? "?" : username.substring(0,1).toUpperCase();
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(l, (d-fm.stringWidth(l))/2, fm.getAscent()+(d-fm.getAscent())/2);
        g2.dispose();
        return new ImageIcon(img);
    }

    // ════════════════════════════════════════════════════════════
    //  HELPERS UI
    // ════════════════════════════════════════════════════════════

    private JTextField buildField(String ph) {
        JTextField t = new JTextField();
        t.setBackground(C_FIELD); t.setFont(F_REGULAR); t.setText(ph); t.setForeground(C_PLACEHOLDER);
        t.setBorder(BorderFactory.createCompoundBorder(new LineBorder(C_BORDER,1,true), BorderFactory.createEmptyBorder(9,12,9,12)));
        t.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent e) { if(t.getText().equals(ph)){ t.setText(""); t.setForeground(C_TEXT); } }
            public void focusLost(FocusEvent e)   { if(t.getText().isEmpty()){ t.setText(ph); t.setForeground(C_PLACEHOLDER); } }
        });
        return t;
    }

    private JPanel buildPassPanel() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(C_FIELD);
        p.setBorder(new LineBorder(C_BORDER,1,true));
        JPasswordField pf = new JPasswordField();
        pf.setBackground(C_FIELD); pf.setFont(F_REGULAR); pf.setBorder(BorderFactory.createEmptyBorder(9,12,9,6));
        pf.setText("Password"); pf.setEchoChar((char)0); pf.setForeground(C_PLACEHOLDER);
        JLabel eye = new JLabel(); eye.setCursor(new Cursor(Cursor.HAND_CURSOR));
        eye.setBorder(BorderFactory.createEmptyBorder(0,0,0,10));
        eye.setIcon(iconEyeClosed != null ? iconEyeClosed : null);
        if (iconEyeClosed == null) eye.setText("👁");
        eye.addMouseListener(click(() -> {
            if (new String(pf.getPassword()).equals("Password")) return;
            if (pf.getEchoChar() != 0) { pf.setEchoChar((char)0); eye.setIcon(iconEyeOpen); if(iconEyeOpen==null)eye.setText("Ocultar"); }
            else { pf.setEchoChar('●'); eye.setIcon(iconEyeClosed); if(iconEyeClosed==null)eye.setText("👁"); }
        }));
        pf.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent e) { if(new String(pf.getPassword()).equals("Password")){ pf.setText(""); pf.setForeground(C_TEXT); pf.setEchoChar('●'); } }
            public void focusLost(FocusEvent e)   { if(pf.getPassword().length==0){ pf.setText("Password"); pf.setEchoChar((char)0); pf.setForeground(C_PLACEHOLDER); } }
        });
        p.add(pf, BorderLayout.CENTER);
        p.add(eye, BorderLayout.EAST);
        return p;
    }

    private JPanel buildFieldWithIcon(String ph) {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(C_FIELD);
        p.setBorder(new LineBorder(C_BORDER,1,true));
        JTextField t = new JTextField();
        t.setBackground(C_FIELD); t.setFont(F_REGULAR); t.setText(ph); t.setForeground(C_PLACEHOLDER);
        t.setBorder(BorderFactory.createEmptyBorder(9,12,9,6));
        JLabel ico = new JLabel(); ico.setPreferredSize(new Dimension(28,28));
        ico.setBorder(BorderFactory.createEmptyBorder(0,0,0,6));
        t.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent e) { if(t.getText().equals(ph)){ t.setText(""); t.setForeground(C_TEXT); } }
            public void focusLost(FocusEvent e)   { if(t.getText().isEmpty()){ t.setText(ph); t.setForeground(C_PLACEHOLDER); } }
        });
        p.add(t, BorderLayout.CENTER);
        p.add(ico, BorderLayout.EAST);
        return p;
    }

    private JSpinner buildSpinner(int val) {
        JSpinner s = new JSpinner(new SpinnerNumberModel(val,1,100,1));
        s.setFont(F_REGULAR);
        JComponent ed = s.getEditor();
        JFormattedTextField tf = ((JSpinner.DefaultEditor)ed).getTextField();
        tf.setBackground(C_FIELD); tf.setFont(F_REGULAR); tf.setBorder(null);
        ed.setBackground(C_FIELD);
        s.setBorder(new LineBorder(C_BORDER,1,true));
        return s;
    }

    private JComboBox<String> buildCombo(String... items) {
        JComboBox<String> c = new JComboBox<>(items);
        c.setFont(F_REGULAR); c.setBackground(C_FIELD);
        return c;
    }

    private JButton buildPrimaryBtn(String text) {
        JButton b = new JButton(text);
        b.setFont(F_BOLD); b.setForeground(C_WHITE); b.setBackground(C_BLUE);
        b.setBorderPainted(false); b.setFocusPainted(false); b.setOpaque(true);
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        b.setPreferredSize(new Dimension(b.getPreferredSize().width, 36));
        return b;
    }

    private JButton buildSecondaryBtn(String text) {
        JButton b = new JButton(text);
        b.setFont(F_REGULAR); b.setForeground(C_TEXT); b.setBackground(new Color(239,239,239));
        b.setBorderPainted(false); b.setFocusPainted(false); b.setOpaque(true);
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        b.setPreferredSize(new Dimension(b.getPreferredSize().width, 36));
        return b;
    }

    private JButton buildDangerBtn(String text) {
        JButton b = buildSecondaryBtn(text);
        b.setForeground(C_ERROR);
        return b;
    }

    private JLabel errorLabel() {
        JLabel l = new JLabel(); l.setFont(F_SMALL); l.setForeground(C_ERROR);
        return l;
    }

    private JLabel linkLabel(String text) {
        JLabel l = new JLabel(text); l.setFont(F_SMALL); l.setForeground(C_BLUE);
        l.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return l;
    }

    private JLabel emptyLabel(String text) {
        JLabel l = new JLabel(text, SwingConstants.CENTER);
        l.setFont(F_REGULAR); l.setForeground(C_TEXT_LIGHT);
        return l;
    }

    private JPanel roundPanel(Color bg, int arc) {
        return new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(bg); g2.fillRect(0,0,getWidth(),getHeight());
                g2.dispose();
            }
        };
    }

    private JScrollPane styledScroll(Component c) {
        return styledScroll(c, true);
    }

    private JScrollPane styledScroll(Component c, boolean scrollToTop) {
        JScrollPane sp = new JScrollPane(c);
        sp.setBorder(null);
        sp.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        // Fix 1: velocidad del scroll mucho más rápida
        sp.getVerticalScrollBar().setUnitIncrement(60);
        sp.getVerticalScrollBar().setBlockIncrement(220);
        sp.getVerticalScrollBar().setUI(new BasicScrollBarUI() {
            protected void configureScrollBarColors() { thumbColor = new Color(219,219,219); trackColor = C_WHITE; }
            protected JButton createDecreaseButton(int o) { return zeroBtn(); }
            protected JButton createIncreaseButton(int o) { return zeroBtn(); }
            private JButton zeroBtn() { JButton b=new JButton(); b.setPreferredSize(new Dimension(0,0)); return b; }
        });
        // Fix 1: iniciar desde arriba salvo cuando se pide lo contrario (ej. chat)
        if (scrollToTop) {
            SwingUtilities.invokeLater(() -> sp.getVerticalScrollBar().setValue(0));
        }
        return sp;
    }

    private JPanel buildDialogHeader(String title, JDialog d) {
        JPanel h = new JPanel(new BorderLayout());
        h.setBackground(C_WHITE);
        h.setBorder(BorderFactory.createCompoundBorder(
            new MatteBorder(0,0,1,0,C_BORDER),
            BorderFactory.createEmptyBorder(14,16,14,16)
        ));
        JLabel lbl = new JLabel(title); lbl.setFont(F_BOLD); lbl.setForeground(C_TEXT);
        JButton x = new JButton("✕"); x.setFont(F_SMALL); x.setForeground(C_TEXT_LIGHT);
        x.setBorderPainted(false); x.setContentAreaFilled(false); x.setFocusPainted(false);
        x.setCursor(new Cursor(Cursor.HAND_CURSOR));
        x.addActionListener(e -> d.dispose());
        h.add(lbl, BorderLayout.WEST); h.add(x, BorderLayout.EAST);
        return h;
    }

    // ── BORDERS ─────────────────────────────────────────────────
    private void redBorder(JComponent c) { c.setBorder(new LineBorder(C_ERROR,1,true)); }
    private void resetFieldBorder(JTextField t) {
        t.setBorder(BorderFactory.createCompoundBorder(new LineBorder(C_BORDER,1,true), BorderFactory.createEmptyBorder(9,12,9,12)));
    }
    private void resetPanelBorder(JPanel p)   { p.setBorder(new LineBorder(C_BORDER,1,true)); }
    private void resetSpinnerBorder(JSpinner s){ s.setBorder(new LineBorder(C_BORDER,1,true)); }

    // ── EVENTOS HELPER ──────────────────────────────────────────
    private MouseAdapter click(Runnable r) {
        return new MouseAdapter() { public void mouseClicked(MouseEvent e) { r.run(); } };
    }

    private DocumentListener simpleDocListener(Runnable r) {
        return new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { r.run(); }
            public void removeUpdate(DocumentEvent e) { r.run(); }
            public void changedUpdate(DocumentEvent e){ r.run(); }
        };
    }

    // ── HTML HELPER ─────────────────────────────────────────────
    private String toHtml(String texto) {
        if (texto == null) return "";
        StringBuilder sb = new StringBuilder();
        for (String w : texto.split(" ")) {
            if (w.startsWith("#") || w.startsWith("@"))
                sb.append("<a href='").append(w).append("' style='color:#3d9be9;text-decoration:none;'>").append(w).append("</a> ");
            else sb.append(w).append(" ");
        }
        return sb.toString();
    }

    // ── BORDE DASHED ────────────────────────────────────────────
    private static class DashedBorder extends AbstractBorder {
        private final Color color; private final int radius;
        DashedBorder(Color c, int r) { color=c; radius=r; }
        public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
            Graphics2D g2=(Graphics2D)g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(color); g2.setStroke(new BasicStroke(1.5f,BasicStroke.CAP_ROUND,BasicStroke.JOIN_ROUND,0,new float[]{6,4},0));
            g2.drawRoundRect(x,y,w-1,h-1,radius,radius); g2.dispose();
        }
        public Insets getBorderInsets(Component c) { return new Insets(6,6,6,6); }
    }
}