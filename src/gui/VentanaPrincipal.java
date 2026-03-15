package gui;

import enums.EstadoCuenta;
import enums.TipoCuenta;
import instagram.InicializadorCuentasDefault;
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
    private Timer feedTimer;
    private int lastMessageCount = 0;
    private int lastFeedCount = 0;

    // ── PALETA ──────────────────────────────────────────────────
    private static final Color C_WHITE       = Color.WHITE;
    private static final Color C_BG          = new Color(250, 250, 250);
    private static final Color C_BLUE        = new Color(64,  155, 230);
    private static final Color C_BLUE_DIM    = new Color(42,  107, 161);
    private static final Color C_FIELD       = new Color(250, 250, 250);
    private static final Color C_BORDER      = new Color(219, 219, 219);
    private static final Color C_PLACEHOLDER = new Color(168, 168, 168);
    private static final Color C_TEXT        = new Color(38,  38,  38);
    private static final Color C_TEXT_LIGHT  = new Color(142, 142, 142);
    private static final Color C_ERROR       = new Color(237, 73,  86);
    private static final Color C_HOVER       = new Color(245, 245, 245);

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

        int s = 26;
        loadIcon("Home",          "homeicon.png",    "bhomeicon.png",    s);
        loadIcon("Search",        "searchicon.png",  "bsearchicon.png",  s);
        loadIcon("Messages",      "messageicon.png", "bmessageicon.png", s);
        loadIcon("Create",        "createicon.png",  "bcreateicon.png",  s);
        loadIcon("Notifications", "hearticon.png",   "bhearticon.png",   s);
        loadIcon("Profile",       "profileicon.png", "bprofileicon.png", s);
        loadIcon("Like",          "hearticon.png",   "bhearticon.png",   22);
        loadIcon("Comment",       "commenticon.png", "commenticon.png",  22);
        loadIcon("Share",         "messageicon.png", "bmessageicon.png", 22);
        loadIcon("More",    "more.png",        "more.png",        20);
        loadIcon("Sticker", "stickericon.png", "stickericon.png", 22);
        loadIcon("Send",    "messageicon.png", "messageicon.png", 22);
        loadIcon("Trash",   "trashicon.png",   "trashicon.png",   18);
        loadIcon("Close",   "closeicon.png",   "closeicon.png",   14);
        loadIcon("Camera",  "cameraicon.png",  "cameraicon.png",  36);
        loadIcon("Lock",    "lockicon.png",    "lockicon.png",    14);
        // Badge de verificado
        loadIcon("Verified", "verified.png",   "verified.png",    16);
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

    // ── Helper badge verificado ──────────────────────────────────
    /**
     * Devuelve un JLabel con verified.png si el username es cuenta verificada.
     * Si no, devuelve un JLabel vacío. Siempre seguro de usar en cualquier panel.
     */
    private JLabel badgeVerificado(String username) {
        JLabel lbl = new JLabel();
        if (InicializadorCuentasDefault.VERIFICADOS.contains(username)) {
            if (iconsNormal.containsKey("Verified")) {
                lbl.setIcon(iconsNormal.get("Verified"));
            } else {
                lbl.setText("✓");
                lbl.setFont(new Font("Arial", Font.BOLD, 11));
                lbl.setForeground(new Color(29, 155, 240));
            }
            lbl.setToolTipText("Cuenta verificada");
            lbl.setBorder(BorderFactory.createEmptyBorder(0, 3, 0, 0));
        }
        return lbl;
    }

    // ════════════════════════════════════════════════════════════
    //  VISTA 1 — LOGIN
    // ════════════════════════════════════════════════════════════
    private void inicializarComponentesLogin() {
        if (chatTimer != null) chatTimer.stop();
        if (feedTimer != null) feedTimer.stop();
        getContentPane().removeAll();
        getContentPane().setLayout(null);
        getContentPane().setBackground(C_BG);

        int cardW = 350, cardH = 430;
        int cardX = (1366 - cardW) / 2;
        int cardY = (768  - cardH) / 2;

        JPanel card = roundPanel(C_WHITE);
        card.setLayout(null);
        card.setBounds(cardX, cardY, cardW, cardH);
        card.setBorder(new LineBorder(C_BORDER, 1));

        JLabel lblLogo = new JLabel();
        try {
            ImageIcon ic = new ImageIcon(getClass().getResource("/images/instagramlogoblack.png"));
            lblLogo.setIcon(new ImageIcon(ic.getImage().getScaledInstance(168, 95, Image.SCALE_SMOOTH)));
        } catch (Exception e) {
            lblLogo.setText("Instagram");
            lblLogo.setFont(new Font("Arial", Font.BOLD, 28));
        }
        lblLogo.setHorizontalAlignment(SwingConstants.CENTER);
        lblLogo.setBounds(37, 20, 276, 100);
        card.add(lblLogo);

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
    //  VISTA 2 — REGISTRO (mejorado)
    // ════════════════════════════════════════════════════════════
    private void cargarVistaRegistro() {
        getContentPane().removeAll();
        getContentPane().setLayout(null);
        getContentPane().setBackground(C_BG);

        // Card más alto para que todo respire bien
        int cardW = 400, cardH = 640;
        int cardX = (1366 - cardW) / 2;
        int cardY = Math.max(10, (768 - cardH) / 2);

        JPanel card = roundPanel(C_WHITE);
        card.setLayout(null);
        card.setBounds(cardX, cardY, cardW, cardH);
        card.setBorder(new LineBorder(C_BORDER, 1));

        // Logo pequeño arriba
        JLabel lblLogo = new JLabel();
        try {
            ImageIcon ic = new ImageIcon(getClass().getResource("/images/instagramlogoblack.png"));
            lblLogo.setIcon(new ImageIcon(ic.getImage().getScaledInstance(120, 68, Image.SCALE_SMOOTH)));
        } catch (Exception ignored) {}
        lblLogo.setHorizontalAlignment(SwingConstants.CENTER);
        lblLogo.setBounds(0, 14, cardW, 70);
        card.add(lblLogo);

        JLabel sub = new JLabel("Crea tu cuenta gratis.", SwingConstants.CENTER);
        sub.setFont(F_SMALL); sub.setForeground(C_TEXT_LIGHT);
        sub.setBounds(0, 82, cardW, 18);
        card.add(sub);

        // Avatar circular — más grande y mejor centrado
        int avatarSize = 84;
        int fX = (cardW - avatarSize) / 2;
        JLabel avatarCircle = new JLabel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(239, 239, 239));
                g2.fillOval(0, 0, getWidth(), getHeight());
                g2.setClip(new Ellipse2D.Double(0, 0, getWidth(), getHeight()));
                super.paintComponent(g);
                g2.setClip(null);
                g2.setColor(C_BORDER);
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawOval(0, 0, getWidth() - 1, getHeight() - 1);
                g2.dispose();
            }
        };
        avatarCircle.setHorizontalAlignment(SwingConstants.CENTER);
        if (iconsNormal.containsKey("Camera")) {
            avatarCircle.setIcon(iconsNormal.get("Camera"));
        } else {
            avatarCircle.setFont(new Font("Arial", Font.PLAIN, 28));
            avatarCircle.setText("+");
        }
        avatarCircle.setOpaque(false);
        avatarCircle.setBounds(fX, 108, avatarSize, avatarSize);
        avatarCircle.setCursor(new Cursor(Cursor.HAND_CURSOR));
        avatarCircle.setToolTipText("Seleccionar foto de perfil");
        card.add(avatarCircle);

        // Etiqueta debajo del avatar
        JLabel lblFotoTip = new JLabel("Foto de perfil (opcional)", SwingConstants.CENTER);
        lblFotoTip.setFont(new Font("Arial", Font.PLAIN, 10));
        lblFotoTip.setForeground(C_TEXT_LIGHT);
        lblFotoTip.setBounds(0, 196, cardW, 16);
        card.add(lblFotoTip);

        final File[] archivoSel = {null};
        avatarCircle.addMouseListener(click(() -> {
            JFileChooser fc = new JFileChooser();
            fc.setFileFilter(new FileNameExtensionFilter("Imágenes", "jpg", "png", "jpeg"));
            if (fc.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                archivoSel[0] = fc.getSelectedFile();
                ImageIcon ic  = new ImageIcon(archivoSel[0].getAbsolutePath());
                Image img     = ic.getImage().getScaledInstance(avatarSize, avatarSize, Image.SCALE_SMOOTH);
                avatarCircle.setIcon(new ImageIcon(img));
                avatarCircle.setText("");
                lblFotoTip.setText("✓ Foto seleccionada");
                lblFotoTip.setForeground(new Color(0, 160, 80));
            }
        }));

        // Campos — con más espacio entre sí
        int fx = 44, fw = cardW - 88, y = 222, gap = 52;

        JTextField txtNombre = buildField("Nombre completo");
        txtNombre.setBounds(fx, y, fw, 38); card.add(txtNombre);
        JLabel errNombre = errorLabel(); errNombre.setBounds(fx, y + 38, fw, 14); card.add(errNombre);
        y += gap;

        JPanel panelUser = buildFieldWithIcon("Nombre de usuario");
        panelUser.setBounds(fx, y, fw, 38); card.add(panelUser);
        JLabel errUser = errorLabel(); errUser.setBounds(fx, y + 38, fw, 14); card.add(errUser);
        y += gap;

        JPanel panelPass = buildPassPanel();
        panelPass.setBounds(fx, y, fw, 38); card.add(panelPass);
        JLabel errPass = errorLabel(); errPass.setBounds(fx, y + 38, fw, 14); card.add(errPass);
        y += gap;

        // Fila de opciones: edad + género + tipo cuenta
        JLabel lEdad   = fieldLabel("Edad");   lEdad.setBounds(fx,       y, 60,  14); card.add(lEdad);
        JLabel lGenero = fieldLabel("Género"); lGenero.setBounds(fx+112, y, 80,  14); card.add(lGenero);
        JLabel lTipo   = fieldLabel("Cuenta"); lTipo.setBounds(fx+214,   y, 100, 14); card.add(lTipo);
        y += 16;

        JSpinner spnEdad = buildSpinner(18);
        spnEdad.setBounds(fx, y, 94, 34); card.add(spnEdad);
        JLabel errEdad = errorLabel(); errEdad.setBounds(fx, y + 36, 110, 14); card.add(errEdad);

        JComboBox<String> cmbGenero = buildCombo("M", "F");
        cmbGenero.setBounds(fx + 112, y, 86, 34); card.add(cmbGenero);

        JComboBox<String> cmbTipo = buildCombo("Pública", "Privada");
        cmbTipo.setBounds(fx + 214, y, 94, 34); card.add(cmbTipo);
        y += 52;

        JButton btnReg = buildPrimaryBtn("Crear cuenta");
        btnReg.setEnabled(false); btnReg.setBackground(C_BLUE_DIM);
        btnReg.setBounds(fx, y, fw, 38); card.add(btnReg);
        y += 48;

        JLabel lblVolver = linkLabel("¿Ya tienes cuenta? Inicia sesión");
        int lw = lblVolver.getPreferredSize().width;
        lblVolver.setBounds(fx + (fw - lw) / 2, y, lw, 18); card.add(lblVolver);

        getContentPane().add(card);

        JTextField txtUser   = (JTextField)    panelUser.getComponent(0);
        JLabel     iconCheck2 = (JLabel)       panelUser.getComponent(1);
        JPasswordField txtPass = (JPasswordField) panelPass.getComponent(0);

        Runnable validar = () -> {
            boolean ok = true;
            errNombre.setText(""); errUser.setText(""); errPass.setText(""); errEdad.setText("");
            resetFieldBorder(txtNombre); resetPanelBorder(panelUser);
            resetPanelBorder(panelPass); resetSpinnerBorder(spnEdad);
            iconCheck2.setIcon(null); iconCheck2.setText("");

            if (txtNombre.getText().equals("Nombre completo") || txtNombre.getText().isEmpty()) ok = false;

            String usr = txtUser.getText();
            if (usr.isEmpty() || usr.equals("Nombre de usuario")) { ok = false; }
            else if (usr.length() < 3) { errUser.setText("Mínimo 3 caracteres."); redBorder(panelUser); ok = false; }
            else if (sistema.existeUsername(usr)) { errUser.setText("El nombre ya existe."); redBorder(panelUser); ok = false; }
            else { iconCheck2.setIcon(iconCheck); if (iconCheck == null) iconCheck2.setText("✓"); }

            String ps = new String(txtPass.getPassword());
            if (ps.isEmpty() || ps.equals("Password")) { ok = false; }
            else if (ps.length() < 6) { errPass.setText("Mínimo 6 caracteres."); redBorder(panelPass); ok = false; }

            if ((int) spnEdad.getValue() < 18) { errEdad.setText("Debes tener 18+."); redBorder(spnEdad); ok = false; }

            btnReg.setEnabled(ok);
            btnReg.setBackground(ok ? C_BLUE : C_BLUE_DIM);
            btnReg.setForeground(ok ? C_WHITE : new Color(200, 202, 204));
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
            int    edad   = (int) spnEdad.getValue();
            char   genero = cmbGenero.getSelectedIndex() == 0 ? 'M' : 'F';
            TipoCuenta tipo = cmbTipo.getSelectedIndex() == 0 ? TipoCuenta.PUBLICA : TipoCuenta.PRIVADA;

            String rutaFoto = archivoSel[0] != null
                ? sistema.procesarImagenPerfil(archivoSel[0], user, "profile_" + System.currentTimeMillis())
                : copiarFotoPorDefecto(user);
            if (rutaFoto == null) rutaFoto = copiarFotoPorDefecto(user);

            if (sistema.registrarUsuario(user, pass, nombre, genero, edad, rutaFoto, tipo)) {
                sistema.login(user, pass);
                cargarVistaFeed();
            } else {
                errUser.setText("Error al registrar.");
            }
        });

        lblVolver.addMouseListener(click(this::inicializarComponentesLogin));
        revalidate(); repaint();
    }

    // ════════════════════════════════════════════════════════════
    //  VISTA 3 — FEED
    // ════════════════════════════════════════════════════════════
    private void cargarVistaFeed() {
        vistaActual = "Home";
        if (feedTimer != null) feedTimer.stop();
        if (chatTimer != null) chatTimer.stop();
        getContentPane().removeAll();
        setLayout(new BorderLayout());
        getContentPane().setBackground(C_BG);

        add(buildSidebar(), BorderLayout.WEST);

        // Panel central con ancho fijo — simula el feed centrado de Instagram
        JPanel panelContenido = new JPanel();
        panelContenido.setBackground(C_BG);
        panelContenido.setLayout(new BoxLayout(panelContenido, BoxLayout.Y_AXIS));

        // Wrapper para centrar el feed
        JPanel feedWrapper = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        feedWrapper.setBackground(C_BG);
        feedWrapper.add(panelContenido);

        JScrollPane scroll = styledScroll(feedWrapper);

        ArrayList<Publicacion> posts = sistema.getTimeline();
        lastFeedCount = posts.size();

        if (posts.isEmpty()) {
            JLabel lbl = new JLabel("No hay publicaciones. ¡Sigue a alguien o crea tu primer post!");
            lbl.setForeground(C_TEXT_LIGHT); lbl.setFont(F_REGULAR);
            lbl.setAlignmentX(Component.CENTER_ALIGNMENT);
            panelContenido.add(Box.createVerticalStrut(40));
            panelContenido.add(lbl);
        } else {
            for (Publicacion p : posts) {
                panelContenido.add(buildPost(p));
                // Gap mínimo entre posts — más compacto como Instagram
                panelContenido.add(Box.createVerticalStrut(4));
            }
        }

        add(scroll, BorderLayout.CENTER);
        SwingUtilities.invokeLater(() -> scroll.getVerticalScrollBar().setValue(0));

        feedTimer = new Timer(3000, ev -> {
            int nuevosCount = sistema.getTimeline().size();
            if (nuevosCount != lastFeedCount) {
                lastFeedCount = nuevosCount;
                cargarVistaFeed();
            }
        });
        feedTimer.start();

        revalidate(); repaint();
    }

    // ════════════════════════════════════════════════════════════
    //  POST CARD — imágenes cropeadas a proporción fija
    // ════════════════════════════════════════════════════════════
    private JPanel buildPost(Publicacion p) {
        final int POST_W = 600;
        // Altura fija de imagen: siempre 600px (cuadrada) — crop centrado
        // Esto imita el comportamiento del grid de Instagram
        final int IMG_H = 600;

        JPanel post = new JPanel(new BorderLayout());
        post.setBackground(C_WHITE);
        post.setMaximumSize(new Dimension(POST_W, IMG_H + 200));
        post.setPreferredSize(new Dimension(POST_W, IMG_H + 180));
        post.setBorder(new LineBorder(C_BORDER, 1));

        // ── HEADER ──
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(C_WHITE);
        header.setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));

        JPanel userInfo = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        userInfo.setBackground(C_WHITE);
        userInfo.add(new JLabel(cargarFotoCircular(p.getAutor(), 32)));

        JLabel lblUser = new JLabel(p.getAutor());
        lblUser.setFont(F_BOLD); lblUser.setForeground(C_TEXT);
        lblUser.setCursor(new Cursor(Cursor.HAND_CURSOR));
        lblUser.addMouseListener(click(() -> cargarVistaPerfil(p.getAutor())));
        userInfo.add(lblUser);
        // Badge verificado junto al username en el post
        userInfo.add(badgeVerificado(p.getAutor()));
        header.add(userInfo, BorderLayout.WEST);

        if (sistema.getUsuarioActual() != null && p.getAutor().equals(sistema.getUsuarioActual().getUsername())) {
            JButton btnOpts = new JButton();
            if (iconsNormal.containsKey("More")) btnOpts.setIcon(iconsNormal.get("More"));
            else { btnOpts.setText("..."); btnOpts.setFont(new Font("Arial", Font.BOLD, 14)); }
            btnOpts.setBorderPainted(false); btnOpts.setContentAreaFilled(false);
            btnOpts.setFocusPainted(false); btnOpts.setCursor(new Cursor(Cursor.HAND_CURSOR));
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

        // ── IMAGEN CROPEADA ──────────────────────────────────────
        // Cargamos la imagen y la cropeamos centrada a POST_W x IMG_H
        JLabel lblImg = new JLabel() {
            @Override protected void paintComponent(Graphics g) {
                g.setColor(new Color(239, 239, 239));
                g.fillRect(0, 0, getWidth(), getHeight());
                if (getIcon() != null) {
                    // La imagen ya viene cropeada desde crearImagenCropeada
                    getIcon().paintIcon(this, g, 0, 0);
                }
            }
        };
        lblImg.setPreferredSize(new Dimension(POST_W, IMG_H));
        lblImg.setMinimumSize(new Dimension(POST_W, IMG_H));
        lblImg.setMaximumSize(new Dimension(POST_W, IMG_H));

        try {
            if (p.getRutaImagen() != null && !p.getRutaImagen().isEmpty()
                    && new File(p.getRutaImagen()).exists()) {
                ImageIcon cropped = crearImagenCropeada(p.getRutaImagen(), POST_W, IMG_H);
                if (cropped != null) lblImg.setIcon(cropped);
            }
        } catch (Exception ignored) {}

        // ── ACCIONES ──
        JPanel acciones = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 6));
        acciones.setBackground(C_WHITE);

        boolean liked = sistema.yaDioLike(p.getAutor(), p.getFecha().toString());
        JButton btnLike = iconBtn(liked);
        btnLike.addActionListener(ev -> {
            boolean estado = sistema.toggleLike(p.getAutor(), p.getFecha().toString(), p.getRutaImagen());
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

        acciones.add(btnLike); acciones.add(btnComment); acciones.add(btnShare);

        // ── FOOTER ──
        JPanel footer = new JPanel();
        footer.setLayout(new BoxLayout(footer, BoxLayout.Y_AXIS));
        footer.setBackground(C_WHITE);
        footer.setBorder(BorderFactory.createEmptyBorder(0, 12, 10, 12));
        footer.add(acciones);

        JEditorPane edCaption = new JEditorPane("text/html", "");
        edCaption.setEditable(false); edCaption.setOpaque(false);
        edCaption.setText("<html><font face='Arial' size='3'><b>" + p.getAutor() + "</b> " + toHtml(p.getContenido()) + "</font></html>");
        edCaption.addHyperlinkListener(e -> {
            if (e.getEventType() == javax.swing.event.HyperlinkEvent.EventType.ACTIVATED) {
                String href = e.getDescription();
                if (href.startsWith("#")) cargarVistaBusquedaHashtag(href);
                else if (href.startsWith("@")) cargarVistaPerfil(href.substring(1));
            }
        });
        footer.add(edCaption);

        // Comentarios preview (máx 2) debajo del caption
        ArrayList<String> comentarios = sistema.getComentarios(p.getAutor(), p.getFecha().toString());
        if (!comentarios.isEmpty()) {
            int mostrar = Math.min(2, comentarios.size());
            for (int i = 0; i < mostrar; i++) {
                String c = comentarios.get(i);
                String[] parts = c.split(":", 2);
                String comUser = parts.length > 0 ? parts[0].trim() : "?";
                String comText = parts.length > 1 ? parts[1].trim() : c;
                JLabel lblCom = new JLabel("<html><font face='Arial' size='3'><b>" + comUser + "</b> " + comText + "</font></html>");
                lblCom.setFont(F_REGULAR); lblCom.setForeground(C_TEXT);
                lblCom.setBorder(BorderFactory.createEmptyBorder(2, 0, 0, 0));
                footer.add(lblCom);
            }
            if (comentarios.size() > 2) {
                JLabel verTodos = new JLabel("Ver los " + comentarios.size() + " comentarios");
                verTodos.setFont(F_SMALL); verTodos.setForeground(C_TEXT_LIGHT);
                verTodos.setCursor(new Cursor(Cursor.HAND_CURSOR));
                verTodos.setBorder(BorderFactory.createEmptyBorder(2, 0, 0, 0));
                verTodos.addMouseListener(click(() -> abrirComentarios(p)));
                footer.add(verTodos);
            }
        }

        JPanel fechaRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        fechaRow.setBackground(C_WHITE);
        JLabel lblFecha = new JLabel(p.getFecha() + "  " + p.getHoraFormateada());
        lblFecha.setFont(F_SMALL); lblFecha.setForeground(C_TEXT_LIGHT);
        lblFecha.setBorder(BorderFactory.createEmptyBorder(4, 0, 0, 0));
        fechaRow.add(lblFecha);
        footer.add(fechaRow);

        post.add(header, BorderLayout.NORTH);
        post.add(lblImg,  BorderLayout.CENTER);
        post.add(footer,  BorderLayout.SOUTH);
        return post;
    }

    /**
     * Carga una imagen y la recorta centradamente al tamaño targetW x targetH.
     * Esto garantiza que todas las imágenes del feed tengan exactamente el mismo tamaño,
     * igual que el comportamiento de Instagram real.
     */
    private ImageIcon crearImagenCropeada(String ruta, int targetW, int targetH) {
        try {
            BufferedImage src = javax.imageio.ImageIO.read(new File(ruta));
            if (src == null) return null;

            int srcW = src.getWidth(), srcH = src.getHeight();
            double scaleW = (double) targetW / srcW;
            double scaleH = (double) targetH / srcH;
            // Escalar al mínimo que cubra ambas dimensiones (fill, no fit)
            double scale  = Math.max(scaleW, scaleH);

            int scaledW = (int)(srcW * scale);
            int scaledH = (int)(srcH * scale);

            BufferedImage scaled = new BufferedImage(scaledW, scaledH, BufferedImage.TYPE_INT_RGB);
            Graphics2D g = scaled.createGraphics();
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g.drawImage(src, 0, 0, scaledW, scaledH, null);
            g.dispose();

            // Crop centrado
            int cropX = (scaledW - targetW) / 2;
            int cropY = (scaledH - targetH) / 2;
            BufferedImage cropped = scaled.getSubimage(cropX, cropY, targetW, targetH);
            return new ImageIcon(cropped);
        } catch (Exception e) {
            return null;
        }
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
        else b.setText(key.equals("Comment") ? "[\u2026]" : ">");
        return b;
    }
    private void actualizarLike(JButton b, boolean on) {
        if (on) { if (iconsBold.containsKey("Like"))  b.setIcon(iconsBold.get("Like"));  else b.setText("<3"); }
        else    { if (iconsNormal.containsKey("Like")) b.setIcon(iconsNormal.get("Like")); else b.setText("o"); }
    }

    // ════════════════════════════════════════════════════════════
    //  COMENTARIOS — DIALOGO CUSTOM (sin setUndecorated)
    // ════════════════════════════════════════════════════════════
    private void abrirComentarios(Publicacion p) {
        // FIX 5: No usar setUndecorated — causa el borde del sistema operativo
        JDialog d = new JDialog(this, "Comentarios — " + p.getAutor(), true);
        d.setSize(440, 540);
        d.setLocationRelativeTo(this);
        d.setLayout(new BorderLayout());
        d.getContentPane().setBackground(C_WHITE);

        JPanel header = buildDialogHeader("Comentarios", d);
        d.add(header, BorderLayout.NORTH);

        // Lista de comentarios con foto de perfil
        JPanel listPanel = new JPanel();
        listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));
        listPanel.setBackground(C_WHITE);
        listPanel.setBorder(BorderFactory.createEmptyBorder(8, 0, 8, 0));

        // Rellenar comentarios existentes
        Runnable rellenarComentarios = () -> {
            listPanel.removeAll();
            for (String c : sistema.getComentarios(p.getAutor(), p.getFecha().toString())) {
                String[] parts = c.split(":", 2);
                String comUser = parts.length > 0 ? parts[0].trim() : "?";
                String comText = parts.length > 1 ? parts[1].trim() : c;

                JPanel row = new JPanel(new BorderLayout(8, 0));
                row.setBackground(C_WHITE);
                row.setBorder(BorderFactory.createEmptyBorder(8, 14, 8, 14));
                row.setMaximumSize(new Dimension(440, 60));

                JLabel foto = new JLabel(cargarFotoCircular(comUser, 32));
                row.add(foto, BorderLayout.WEST);

                JLabel lbl = new JLabel("<html><b>" + comUser + "</b> " + comText + "</html>");
                lbl.setFont(F_REGULAR); lbl.setForeground(C_TEXT);
                row.add(lbl, BorderLayout.CENTER);

                listPanel.add(row);
                JSeparator sep = new JSeparator();
                sep.setForeground(new Color(240, 240, 240));
                listPanel.add(sep);
            }
            listPanel.revalidate(); listPanel.repaint();
        };
        rellenarComentarios.run();

        JScrollPane scroll = styledScroll(listPanel, false);
        d.add(scroll, BorderLayout.CENTER);

        JPanel footer = new JPanel(new BorderLayout());
        footer.setBackground(C_WHITE);
        footer.setBorder(BorderFactory.createCompoundBorder(
            new MatteBorder(1, 0, 0, 0, C_BORDER),
            BorderFactory.createEmptyBorder(10, 12, 10, 12)
        ));
        JTextField txt = new JTextField();
        txt.setFont(F_REGULAR); txt.setBackground(C_FIELD);
        txt.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(C_BORDER, 1, true),
            BorderFactory.createEmptyBorder(7, 12, 7, 12)
        ));
        txt.putClientProperty("JTextField.placeholderText", "Agrega un comentario...");
        JButton btnEnv = new JButton("Publicar");
        btnEnv.setFont(F_BOLD); btnEnv.setForeground(C_BLUE);
        btnEnv.setBorderPainted(false); btnEnv.setContentAreaFilled(false);
        btnEnv.setFocusPainted(false); btnEnv.setCursor(new Cursor(Cursor.HAND_CURSOR));
        ActionListener send = e -> {
            if (!txt.getText().trim().isEmpty()) {
                sistema.agregarComentario(p.getAutor(), p.getFecha().toString(), txt.getText().trim());
                txt.setText("");
                rellenarComentarios.run();
                // Scroll al fondo
                SwingUtilities.invokeLater(() -> scroll.getVerticalScrollBar()
                    .setValue(scroll.getVerticalScrollBar().getMaximum()));
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

        JPanel header = buildDialogHeader("Nueva publicación", d);
        d.add(header, BorderLayout.NORTH);

        JPanel center = new JPanel();
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
        center.setBackground(C_WHITE);
        center.setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));

        JLabel lblImgPreview = new JLabel() {
            @Override protected void paintComponent(Graphics g) {
                if (getIcon() == null) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(new Color(250,250,250)); g2.fillRect(0,0,getWidth(),getHeight());
                    g2.setColor(C_BORDER);
                    g2.setStroke(new BasicStroke(1.5f,BasicStroke.CAP_ROUND,BasicStroke.JOIN_ROUND,0,new float[]{6,4},0));
                    g2.drawRoundRect(1,1,getWidth()-2,getHeight()-2,8,8);
                    g2.setFont(F_REGULAR); g2.setColor(C_TEXT_LIGHT);
                    FontMetrics fm = g2.getFontMetrics();
                    String msg = "Selecciona una imagen";
                    g2.drawString(msg, (getWidth()-fm.stringWidth(msg))/2, getHeight()/2);
                    g2.dispose();
                } else { super.paintComponent(g); }
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
        center.add(imgRow); center.add(btnSel);

        JButton btnCancel = buildSecondaryBtn("Cancelar");
        JButton btnPost   = buildPrimaryBtn("Compartir");

        JTextArea txtContenido = new JTextArea();
        txtContenido.setLineWrap(true); txtContenido.setWrapStyleWord(true);
        txtContenido.setFont(F_REGULAR); txtContenido.setBackground(C_FIELD);
        txtContenido.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(C_BORDER,1,true), BorderFactory.createEmptyBorder(10,12,10,12)));
        JScrollPane scrollTxt = new JScrollPane(txtContenido);
        scrollTxt.setBorder(null); scrollTxt.setPreferredSize(new Dimension(452, 72));
        scrollTxt.setMaximumSize(new Dimension(Integer.MAX_VALUE, 72));

        JLabel lblContador = new JLabel("0 / 220", SwingConstants.RIGHT);
        lblContador.setFont(F_SMALL); lblContador.setForeground(C_TEXT_LIGHT);
        lblContador.setAlignmentX(Component.RIGHT_ALIGNMENT);

        txtContenido.getDocument().addDocumentListener(simpleDocListener(() -> {
            int len = txtContenido.getText().length();
            lblContador.setText(len + " / 220");
            if (len > 220) { lblContador.setForeground(C_ERROR); btnPost.setEnabled(false); btnPost.setBackground(C_BLUE_DIM); btnPost.setForeground(new Color(200,202,204)); }
            else { lblContador.setForeground(len > 160 ? new Color(230,140,0) : C_TEXT_LIGHT); btnPost.setEnabled(true); btnPost.setBackground(C_BLUE); btnPost.setForeground(C_WHITE); }
        }));

        center.add(scrollTxt); center.add(lblContador);
        d.add(center, BorderLayout.CENTER);

        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 12));
        footer.setBackground(C_WHITE);
        footer.setBorder(new MatteBorder(1,0,0,0,C_BORDER));
        footer.add(btnCancel); footer.add(btnPost);
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
                    ImageIcon orig = new ImageIcon(ruta[0]);
                    int maxW=452,maxH=220,iw=orig.getIconWidth(),ih=orig.getIconHeight();
                    double scale=Math.min((double)maxW/iw,(double)maxH/ih);
                    lblImgPreview.setIcon(new ImageIcon(orig.getImage().getScaledInstance((int)(iw*scale),(int)(ih*scale),Image.SCALE_SMOOTH)));
                    btnSel.setText("Cambiar imagen");
                } else { InstaDialog.showMessage(d,"Solo se permiten JPG, PNG, JPEG o GIF.",true); }
            }
        });
        btnCancel.addActionListener(e -> d.dispose());
        btnPost.addActionListener(e -> {
            String texto = txtContenido.getText().trim();
            if (texto.isEmpty() || ruta[0].isEmpty()) { InstaDialog.showMessage(d,"Escribe algo y selecciona una imagen."); return; }
            StringBuilder hashtags = new StringBuilder(), menciones = new StringBuilder();
            for (String word : texto.split(" ")) {
                if (word.startsWith("#")) hashtags.append(word).append(" ");
                else if (word.startsWith("@")) menciones.append(word).append(" ");
            }
            String rutaFinal = sistema.procesarYGuardarImagen(new File(ruta[0]), sistema.getUsuarioActual().getUsername(), "post_"+System.currentTimeMillis());
            if (rutaFinal != null && sistema.crearPublicacion(texto, rutaFinal, hashtags.toString().trim(), menciones.toString().trim())) {
                d.dispose(); cargarVistaFeed();
            } else { InstaDialog.showMessage(d,"Error al publicar.",true); }
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

        JLabel logo = new JLabel();
        try {
            ImageIcon logoIc = new ImageIcon(getClass().getResource("/images/instagramlogoblack.png"));
            logo.setIcon(new ImageIcon(logoIc.getImage().getScaledInstance(112, 63, Image.SCALE_SMOOTH)));
        } catch (Exception e) { logo.setText("Instagram"); logo.setFont(new Font("Arial", Font.BOLD, 22)); }
        logo.setAlignmentX(Component.LEFT_ALIGNMENT);
        logo.setBorder(BorderFactory.createEmptyBorder(24, 22, 28, 22));
        sidebar.add(logo);

        JButton[] btns = {
            sidebarBtn("Inicio",         "Home"),
            sidebarBtn("Buscar",         "Search"),
            sidebarBtnConBadge("Mensajes","Messages"),
            sidebarBtn("Crear",          "Create"),
            sidebarBtn("Notificaciones", "Notifications"),
            sidebarBtn("Mi perfil",      "Profile")
        };
        for (JButton b : btns) sidebar.add(b);
        sidebar.add(Box.createVerticalGlue());
        JButton btnOut = sidebarBtn("Cerrar sesión", null);
        sidebar.add(btnOut); sidebar.add(Box.createVerticalStrut(20));

        btns[0].addActionListener(e -> cargarVistaFeed());
        btns[1].addActionListener(e -> cargarVistaBusqueda());
        btns[2].addActionListener(e -> cargarVistaInbox());
        btns[3].addActionListener(e -> abrirDialogoNuevaPublicacion());
        btns[4].addActionListener(e -> cargarVistaNotificaciones());
        btns[5].addActionListener(e -> cargarVistaPerfil(sistema.getUsuarioActual().getUsername()));
        btnOut.addActionListener(e -> {
            if (chatTimer != null) chatTimer.stop();
            if (feedTimer != null) feedTimer.stop();
            sistema.logout(); inicializarComponentesLogin();
        });
        return sidebar;
    }

    private JButton sidebarBtn(String label, String key) {
        boolean active = key != null && key.equals(vistaActual);
        JButton b = new JButton();
        b.setAlignmentX(Component.LEFT_ALIGNMENT);
        b.setMaximumSize(new Dimension(220, 58));
        b.setMinimumSize(new Dimension(220, 58));
        b.setPreferredSize(new Dimension(220, 58));
        b.setBorderPainted(false); b.setFocusPainted(false);
        b.setBackground(C_WHITE); b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        b.setHorizontalAlignment(SwingConstants.LEFT);
        b.setBorder(BorderFactory.createEmptyBorder(0, 18, 0, 0));
        if (key != null) {
            Map<String,ImageIcon> map = active ? iconsBold : iconsNormal;
            if (map.containsKey(key)) { b.setIcon(map.get(key)); b.setIconTextGap(12); }
        }
        b.setText(label);
        b.setFont(active ? new Font("Arial", Font.BOLD, 15) : new Font("Arial", Font.PLAIN, 15));
        b.setForeground(key == null ? C_ERROR : C_TEXT);
        b.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { if (!active) b.setBackground(C_HOVER); }
            public void mouseExited(MouseEvent e)  { b.setBackground(C_WHITE); }
        });
        return b;
    }

    private JButton sidebarBtnConBadge(String label, String key) {
        int noLeidos = sistema.getTotalMensajesNoLeidos();
        if (noLeidos <= 0) return sidebarBtn(label, key);
        final int count = noLeidos;
        boolean active = key != null && key.equals(vistaActual);
        JButton b = new JButton(label) {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                String txt = count > 99 ? "99+" : String.valueOf(count);
                g2.setFont(new Font("Arial", Font.BOLD, 9));
                FontMetrics fm = g2.getFontMetrics();
                int tw=fm.stringWidth(txt), bw=Math.max(tw+6,16), bh=16, bx=36, by=8;
                g2.setColor(C_ERROR); g2.fillRoundRect(bx,by,bw,bh,bh,bh);
                g2.setColor(Color.WHITE); g2.drawString(txt, bx+(bw-tw)/2, by+bh-4);
                g2.dispose();
            }
        };
        b.setAlignmentX(Component.LEFT_ALIGNMENT);
        b.setMaximumSize(new Dimension(220,58)); b.setMinimumSize(new Dimension(220,58)); b.setPreferredSize(new Dimension(220,58));
        b.setBorderPainted(false); b.setFocusPainted(false); b.setBackground(C_WHITE);
        b.setCursor(new Cursor(Cursor.HAND_CURSOR)); b.setHorizontalAlignment(SwingConstants.LEFT);
        b.setBorder(BorderFactory.createEmptyBorder(0,18,0,0)); b.setIconTextGap(12);
        b.setFont(active ? new Font("Arial",Font.BOLD,15) : new Font("Arial",Font.PLAIN,15));
        b.setForeground(C_TEXT);
        Map<String,ImageIcon> iconMap = active ? iconsBold : iconsNormal;
        if (iconMap.containsKey(key)) b.setIcon(iconMap.get(key));
        b.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { if (!active) b.setBackground(C_HOVER); }
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

        JLabel lblFoto = new JLabel();
        lblFoto.setPreferredSize(new Dimension(140, 140));
        lblFoto.setIcon(cargarFotoCircular(username, 140));
        hdr.add(lblFoto, BorderLayout.WEST);

        JPanel info = new JPanel();
        info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));
        info.setBackground(C_BG);
        info.setBorder(BorderFactory.createEmptyBorder(0, 28, 0, 0));

        JPanel row1 = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        row1.setBackground(C_BG);
        JLabel lblUsr = new JLabel(username);
        lblUsr.setFont(new Font("Arial", Font.PLAIN, 22)); lblUsr.setForeground(C_TEXT);
        row1.add(lblUsr);
        // Badge verificado en el perfil
        row1.add(badgeVerificado(username));

        boolean esMiPerfil = sistema.getUsuarioActual() != null && username.equals(sistema.getUsuarioActual().getUsername());
        if (esMiPerfil) {
            JButton btnEdit = buildSecondaryBtn("Editar perfil");
            btnEdit.addActionListener(e -> abrirDialogoEditarPerfil());
            row1.add(btnEdit);
            JButton btnToggle;
            if (sistema.getUsuarioActual().getEstadoCuenta() == EstadoCuenta.ACTIVO) {
                btnToggle = buildDangerBtn("Desactivar cuenta");
                btnToggle.addActionListener(e -> {
                    boolean yes = InstaDialog.showConfirm(this,"¿Desactivar tu cuenta?","Desactivar",true);
                    if (yes) { sistema.cambiarEstadoCuenta(EstadoCuenta.DESACTIVADO); sistema.logout(); if(chatTimer!=null)chatTimer.stop(); inicializarComponentesLogin(); }
                });
            } else {
                btnToggle = buildPrimaryBtn("Activar cuenta");
                btnToggle.addActionListener(e -> { sistema.cambiarEstadoCuenta(EstadoCuenta.ACTIVO); cargarVistaPerfil(username); });
            }
            row1.add(btnToggle);
        } else {
            boolean loSigo    = sistema.yaLoSigo(username);
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

            // FIX 3: Botón Mensaje — solo si puede chatear
            if (sistema.puedeEnviarMensaje(username)) {
                JButton btnMsg = buildSecondaryBtn("Mensaje");
                btnMsg.addActionListener(e -> cargarVistaInboxCon(username));
                row1.add(btnMsg);
            }
        }
        info.add(row1);
        info.add(Box.createVerticalStrut(18));

        JPanel row2 = new JPanel(new FlowLayout(FlowLayout.LEFT, 28, 0));
        row2.setBackground(C_BG);
        JPanel pPosts     = statPanel(sistema.getCantidadPosts(username), "publicaciones");
        JPanel pFollowers = statPanel(sistema.getCantidadFollowers(username), "seguidores");
        JPanel pFollowing = statPanel(sistema.getCantidadFollowing(username), "seguidos");
        pFollowers.setCursor(new Cursor(Cursor.HAND_CURSOR));
        pFollowers.addMouseListener(click(() -> mostrarListaUsuarios(sistema.getListaFollowers(username),"Seguidores","Followers")));
        pFollowing.setCursor(new Cursor(Cursor.HAND_CURSOR));
        pFollowing.addMouseListener(click(() -> mostrarListaUsuarios(sistema.getListaFollowing(username),"Siguiendo","Following")));
        row2.add(pPosts); row2.add(pFollowers); row2.add(pFollowing);
        info.add(row2); info.add(Box.createVerticalStrut(10));

        Usuario u = sistema.buscarUsuario(username);
        String nombre = u != null ? u.getNombreCompleto() : "";
        JLabel lblNombre = new JLabel(nombre);
        lblNombre.setFont(F_BOLD); lblNombre.setForeground(C_TEXT);
        JPanel row3 = new JPanel(new FlowLayout(FlowLayout.LEFT,0,0));
        row3.setBackground(C_BG); row3.add(lblNombre);
        info.add(row3); info.add(Box.createVerticalStrut(4));

        if (u != null) {
            String generoStr = u.getGenero()=='M' ? "Masculino" : "Femenino";
            String datos = generoStr + "  ·  " + u.getEdad() + " años  ·  " + u.getTipoCuenta().name() + "  ·  Desde " + u.getFechaRegistro();
            JLabel lblDatos = new JLabel(datos);
            lblDatos.setFont(F_SMALL); lblDatos.setForeground(C_TEXT_LIGHT);
            JPanel row4 = new JPanel(new FlowLayout(FlowLayout.LEFT,0,0));
            row4.setBackground(C_BG); row4.add(lblDatos);
            info.add(row4);
        }

        hdr.add(info, BorderLayout.CENTER);
        content.add(hdr, BorderLayout.NORTH);

        JSeparator sep = new JSeparator();
        sep.setForeground(C_BORDER); sep.setBackground(C_BORDER);

        // ── GRID 4 COLUMNAS con imágenes cropeadas a cuadrado ──
        JPanel grid = new JPanel(new GridLayout(0, 4, 2, 2));
        grid.setBackground(C_BG);
        grid.setBorder(BorderFactory.createEmptyBorder(2, 60, 20, 60));

        ArrayList<Publicacion> posts = sistema.getPublicacionesDeUsuario(username);
        if (posts.isEmpty()) {
            JLabel vacio = new JLabel("No hay publicaciones.", SwingConstants.CENTER);
            vacio.setFont(F_REGULAR); vacio.setForeground(C_TEXT_LIGHT);
            grid.add(vacio);
        } else {
            for (Publicacion p : posts) {
                // Tamaño de celda: ancho disponible (1366-220-120) / 4 cols ≈ 252px
                final int CELL = 252;
                JPanel cell = new JPanel(null);
                cell.setPreferredSize(new Dimension(CELL, CELL));
                cell.setBackground(new Color(239,239,239));
                cell.setCursor(new Cursor(Cursor.HAND_CURSOR));

                JLabel img = new JLabel();
                img.setBounds(0, 0, CELL, CELL);
                img.setHorizontalAlignment(SwingConstants.CENTER);
                img.setOpaque(true); img.setBackground(new Color(239,239,239));
                try {
                    if (p.getRutaImagen() != null && new File(p.getRutaImagen()).exists()) {
                        ImageIcon cropped = crearImagenCropeada(p.getRutaImagen(), CELL, CELL);
                        if (cropped != null) img.setIcon(cropped);
                    }
                } catch (Exception ignored) {}

                // Hover overlay
                JPanel overlay = new JPanel() {
                    { setOpaque(false); setVisible(false); }
                    @Override protected void paintComponent(Graphics g) {
                        g.setColor(new Color(0,0,0,80));
                        g.fillRect(0,0,getWidth(),getHeight());
                    }
                };
                overlay.setBounds(0, 0, CELL, CELL);

                cell.add(overlay);
                cell.add(img);
                cell.addMouseListener(new MouseAdapter() {
                    public void mouseEntered(MouseEvent e) { overlay.setVisible(true); cell.repaint(); }
                    public void mouseExited(MouseEvent e)  { overlay.setVisible(false); cell.repaint(); }
                    public void mouseClicked(MouseEvent e) { abrirDetallePost(p); }
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

    // ════════════════════════════════════════════════════════════
    //  DETALLE DE POST
    // ════════════════════════════════════════════════════════════
    private void abrirDetallePost(Publicacion p) {
        JDialog d = new JDialog(this, "", true);
        d.setUndecorated(true);
        d.setSize(900, 600);
        d.setLocationRelativeTo(this);
        d.setBackground(new Color(0,0,0,0));

        Component oldGlass = getGlassPane();
        JPanel glass = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                g.setColor(new Color(0,0,0,150)); g.fillRect(0,0,getWidth(),getHeight());
            }
        };
        glass.setOpaque(false); glass.addMouseListener(new MouseAdapter() {});
        setGlassPane(glass); glass.setVisible(true);
        d.addWindowListener(new WindowAdapter() {
            @Override public void windowClosed(WindowEvent e) {
                glass.setVisible(false); setGlassPane(oldGlass); repaint();
            }
        });

        JPanel root = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2=(Graphics2D)g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(C_WHITE); g2.fill(new RoundRectangle2D.Double(0,0,getWidth(),getHeight(),12,12));
                g2.dispose();
            }
        };
        root.setOpaque(false);

        int IMG_W=520, IMG_H=600;
        JPanel imgPanel = new JPanel(new BorderLayout());
        imgPanel.setBackground(Color.BLACK);
        imgPanel.setPreferredSize(new Dimension(IMG_W, IMG_H));
        JLabel imgLbl = new JLabel();
        imgLbl.setHorizontalAlignment(SwingConstants.CENTER);
        try {
            File f = new File(p.getRutaImagen());
            if (f.exists()) {
                ImageIcon orig = new ImageIcon(p.getRutaImagen());
                int iw=orig.getIconWidth(), ih=orig.getIconHeight();
                double scale=Math.min((double)IMG_W/iw,(double)IMG_H/ih);
                imgLbl.setIcon(new ImageIcon(orig.getImage().getScaledInstance((int)(iw*scale),(int)(ih*scale),Image.SCALE_SMOOTH)));
            }
        } catch (Exception ignored) {}
        imgPanel.add(imgLbl, BorderLayout.CENTER);

        JPanel right = new JPanel(new BorderLayout());
        right.setBackground(C_WHITE); right.setPreferredSize(new Dimension(380, IMG_H));

        JPanel rHeader = new JPanel(new BorderLayout());
        rHeader.setBackground(C_WHITE);
        rHeader.setBorder(BorderFactory.createCompoundBorder(new MatteBorder(0,0,1,0,C_BORDER), BorderFactory.createEmptyBorder(12,14,12,14)));
        JPanel rHeaderLeft = new JPanel(new FlowLayout(FlowLayout.LEFT,8,0));
        rHeaderLeft.setBackground(C_WHITE);
        rHeaderLeft.add(new JLabel(cargarFotoCircular(p.getAutor(),32)));
        JLabel rUserLbl=new JLabel(p.getAutor()); rUserLbl.setFont(F_BOLD); rUserLbl.setForeground(C_TEXT);
        rHeaderLeft.add(rUserLbl);
        rHeaderLeft.add(badgeVerificado(p.getAutor()));
        JButton btnCerrar=new JButton();
        if(iconsNormal.containsKey("Close")) btnCerrar.setIcon(iconsNormal.get("Close"));
        else {btnCerrar.setText("x"); btnCerrar.setFont(F_SMALL);}
        btnCerrar.setBorderPainted(false); btnCerrar.setContentAreaFilled(false); btnCerrar.setFocusPainted(false); btnCerrar.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnCerrar.addActionListener(e->d.dispose());
        rHeader.add(rHeaderLeft,BorderLayout.WEST); rHeader.add(btnCerrar,BorderLayout.EAST);
        right.add(rHeader,BorderLayout.NORTH);

        JPanel scrollContent=new JPanel(); scrollContent.setLayout(new BoxLayout(scrollContent,BoxLayout.Y_AXIS)); scrollContent.setBackground(C_WHITE);
        if(p.getContenido()!=null&&!p.getContenido().isEmpty()) {
            JPanel captionRow=new JPanel(new BorderLayout()); captionRow.setBackground(C_WHITE); captionRow.setBorder(BorderFactory.createEmptyBorder(12,14,10,14));
            JPanel captionLeft=new JPanel(new FlowLayout(FlowLayout.LEFT,8,0)); captionLeft.setBackground(C_WHITE);
            captionLeft.add(new JLabel(cargarFotoCircular(p.getAutor(),28)));
            JLabel captionTxt=new JLabel("<html><b>"+p.getAutor()+"</b> "+p.getContenido()+"</html>"); captionTxt.setFont(F_REGULAR); captionTxt.setForeground(C_TEXT);
            captionLeft.add(captionTxt); captionRow.add(captionLeft,BorderLayout.CENTER);
            scrollContent.add(captionRow);
            JSeparator sep2=new JSeparator(); sep2.setForeground(C_BORDER); sep2.setBackground(C_BORDER); scrollContent.add(sep2);
        }

        ArrayList<String> comentarios=sistema.getComentarios(p.getAutor(),p.getFecha().toString());
        for(String c:comentarios) {
            String[] parts=c.split(":",2);
            String comUser=parts.length>0?parts[0].trim():"?";
            String comText=parts.length>1?parts[1].trim():c;
            JPanel comRow=new JPanel(new BorderLayout()); comRow.setBackground(C_WHITE); comRow.setBorder(BorderFactory.createEmptyBorder(10,14,8,14));
            JPanel comLeft=new JPanel(new FlowLayout(FlowLayout.LEFT,8,0)); comLeft.setBackground(C_WHITE);
            comLeft.add(new JLabel(cargarFotoCircular(comUser,28)));
            JLabel comLbl=new JLabel("<html><b>"+comUser+"</b> "+comText+"</html>"); comLbl.setFont(F_REGULAR); comLbl.setForeground(C_TEXT);
            comLeft.add(comLbl); comRow.add(comLeft,BorderLayout.CENTER); scrollContent.add(comRow);
        }

        JScrollPane scrollPane=styledScroll(scrollContent,false); scrollPane.setBorder(null); right.add(scrollPane,BorderLayout.CENTER);

        JPanel footer2=new JPanel(new BorderLayout()); footer2.setBackground(C_WHITE); footer2.setBorder(new MatteBorder(1,0,0,0,C_BORDER));
        JPanel likeRow=new JPanel(new FlowLayout(FlowLayout.LEFT,10,8)); likeRow.setBackground(C_WHITE);
        int[] likeCount={sistema.getCantidadLikes(p.getAutor(),p.getFecha().toString())};
        boolean[] liked={sistema.yaDioLike(p.getAutor(),p.getFecha().toString())};
        JButton btnLike=new JButton();
        if(liked[0]){if(iconsBold.containsKey("Like"))btnLike.setIcon(iconsBold.get("Like"));else btnLike.setText("<3");}
        else{if(iconsNormal.containsKey("Like"))btnLike.setIcon(iconsNormal.get("Like"));else btnLike.setText("o");}
        btnLike.setBorderPainted(false); btnLike.setContentAreaFilled(false); btnLike.setFocusPainted(false); btnLike.setCursor(new Cursor(Cursor.HAND_CURSOR));
        JLabel lblLikes=new JLabel(likeCount[0]+" Me gusta"); lblLikes.setFont(F_BOLD); lblLikes.setForeground(C_TEXT);
        btnLike.addActionListener(e->{
            sistema.toggleLike(p.getAutor(),p.getFecha().toString(),p.getRutaImagen());
            liked[0]=sistema.yaDioLike(p.getAutor(),p.getFecha().toString());
            likeCount[0]=sistema.getCantidadLikes(p.getAutor(),p.getFecha().toString());
            if(liked[0]){if(iconsBold.containsKey("Like"))btnLike.setIcon(iconsBold.get("Like"));else btnLike.setText("<3");}
            else{if(iconsNormal.containsKey("Like"))btnLike.setIcon(iconsNormal.get("Like"));else btnLike.setText("o");}
            lblLikes.setText(likeCount[0]+" Me gusta");
        });
        likeRow.add(btnLike); likeRow.add(lblLikes);

        JPanel inputRow=new JPanel(new BorderLayout(6,0)); inputRow.setBackground(C_WHITE); inputRow.setBorder(BorderFactory.createEmptyBorder(6,14,10,14));
        JTextField txtCom=new JTextField(); txtCom.setFont(F_REGULAR); txtCom.setBackground(C_FIELD);
        txtCom.setBorder(BorderFactory.createCompoundBorder(new LineBorder(C_BORDER,1,true),BorderFactory.createEmptyBorder(8,12,8,12)));
        txtCom.putClientProperty("JTextField.placeholderText","Agrega un comentario...");
        JButton btnEnviarCom=buildPrimaryBtn("Publicar"); btnEnviarCom.setPreferredSize(new Dimension(80,36)); btnEnviarCom.setFont(new Font("Arial",Font.BOLD,12));
        ActionListener enviarComentario=e->{
            String texto=txtCom.getText().trim();
            if(texto.isEmpty())return;
            sistema.agregarComentario(p.getAutor(),p.getFecha().toString(),texto);
            JPanel newRow=new JPanel(new BorderLayout()); newRow.setBackground(C_WHITE); newRow.setBorder(BorderFactory.createEmptyBorder(10,14,8,14));
            JPanel nl=new JPanel(new FlowLayout(FlowLayout.LEFT,8,0)); nl.setBackground(C_WHITE);
            nl.add(new JLabel(cargarFotoCircular(sistema.getUsuarioActual().getUsername(),28)));
            JLabel nl2=new JLabel("<html><b>"+sistema.getUsuarioActual().getUsername()+"</b> "+texto+"</html>"); nl2.setFont(F_REGULAR); nl2.setForeground(C_TEXT);
            nl.add(nl2); newRow.add(nl,BorderLayout.CENTER);
            scrollContent.add(newRow); scrollContent.revalidate(); scrollContent.repaint(); txtCom.setText("");
            SwingUtilities.invokeLater(()->scrollPane.getVerticalScrollBar().setValue(scrollPane.getVerticalScrollBar().getMaximum()));
        };
        btnEnviarCom.addActionListener(enviarComentario); txtCom.addActionListener(enviarComentario);
        inputRow.add(txtCom,BorderLayout.CENTER); inputRow.add(btnEnviarCom,BorderLayout.EAST);
        footer2.add(likeRow,BorderLayout.NORTH); footer2.add(inputRow,BorderLayout.SOUTH);
        right.add(footer2,BorderLayout.SOUTH);

        root.add(imgPanel,BorderLayout.WEST); root.add(right,BorderLayout.CENTER);
        d.setContentPane(root); d.setVisible(true);
    }

    private JPanel statPanel(int n, String label) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT,4,0)); p.setBackground(C_BG);
        JLabel num=new JLabel(String.valueOf(n)); num.setFont(F_BOLD); num.setForeground(C_TEXT);
        JLabel txt=new JLabel(label); txt.setFont(F_REGULAR); txt.setForeground(C_TEXT);
        p.add(num); p.add(txt); return p;
    }

    // ════════════════════════════════════════════════════════════
    //  VISTA 5 — BÚSQUEDA + EXPLORE GRID
    // ════════════════════════════════════════════════════════════
    private void cargarVistaBusqueda() {
        vistaActual = "Search";
        getContentPane().removeAll();
        setLayout(new BorderLayout());
        getContentPane().setBackground(C_BG);
        add(buildSidebar(), BorderLayout.WEST);

        JPanel main = new JPanel(new BorderLayout());
        main.setBackground(C_BG);
        main.setBorder(BorderFactory.createEmptyBorder(32, 40, 40, 40));

        // Barra de búsqueda
        JPanel searchBar = new JPanel(new BorderLayout());
        searchBar.setBackground(new Color(239,239,239));
        searchBar.setBorder(new LineBorder(C_BORDER,1,true));
        JTextField txtBuscar = new JTextField("Buscar usuario o #hashtag...");
        txtBuscar.setFont(F_REGULAR); txtBuscar.setBackground(new Color(239,239,239));
        txtBuscar.setForeground(C_PLACEHOLDER);
        txtBuscar.setBorder(BorderFactory.createEmptyBorder(11,16,11,12));
        txtBuscar.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent e) {
                if(txtBuscar.getText().equals("Buscar usuario o #hashtag...")){txtBuscar.setText("");txtBuscar.setForeground(C_TEXT);}
            }
            public void focusLost(FocusEvent e) {
                if(txtBuscar.getText().isEmpty()){txtBuscar.setText("Buscar usuario o #hashtag...");txtBuscar.setForeground(C_PLACEHOLDER);}
            }
        });
        JButton btnBuscar = buildPrimaryBtn("Buscar");
        btnBuscar.setPreferredSize(new Dimension(90,40));
        searchBar.add(txtBuscar, BorderLayout.CENTER);
        searchBar.add(btnBuscar, BorderLayout.EAST);
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(C_BG);
        topBar.setBorder(BorderFactory.createEmptyBorder(0,0,20,0));
        topBar.add(searchBar, BorderLayout.CENTER);
        main.add(topBar, BorderLayout.NORTH);

        // Contenedor dinámico: explore grid o resultados
        JPanel contentWrapper = new JPanel(new BorderLayout());
        contentWrapper.setBackground(C_BG);
        contentWrapper.add(styledScroll(buildExploreGrid()), BorderLayout.CENTER);
        main.add(contentWrapper, BorderLayout.CENTER);
        add(main, BorderLayout.CENTER);

        ActionListener doSearch = e -> {
            String q = txtBuscar.getText().trim();
            if (q.isEmpty() || q.equals("Buscar usuario o #hashtag...")) {
                contentWrapper.removeAll();
                contentWrapper.add(styledScroll(buildExploreGrid()), BorderLayout.CENTER);
                contentWrapper.revalidate(); contentWrapper.repaint();
                return;
            }
            JPanel resultsPanel = new JPanel();
            resultsPanel.setLayout(new BoxLayout(resultsPanel, BoxLayout.Y_AXIS));
            resultsPanel.setBackground(C_BG);
            if (q.startsWith("#")) {
                ArrayList<Publicacion> posts = sistema.buscarPorHashtag(q);
                if (posts.isEmpty()) resultsPanel.add(emptyLabel("Sin resultados para " + q));
                else for (Publicacion p : posts) { resultsPanel.add(buildPost(p)); resultsPanel.add(Box.createVerticalStrut(4)); }
            } else {
                ArrayList<Usuario> users = sistema.buscarUsuarios(q);
                if (users.isEmpty()) resultsPanel.add(emptyLabel("No se encontraron usuarios."));
                else for (Usuario u : users) resultsPanel.add(buildUserRow(u));
            }
            contentWrapper.removeAll();
            contentWrapper.add(styledScroll(resultsPanel), BorderLayout.CENTER);
            contentWrapper.revalidate(); contentWrapper.repaint();
        };
        btnBuscar.addActionListener(doSearch);
        txtBuscar.addActionListener(doSearch);
        revalidate(); repaint();
    }

    /**
     * Explore Grid — mosaico de 3 columnas con posts de cuentas que el usuario no sigue aún.
     * Siempre incluye los posts de las 3 cuentas default.
     * Click en miniatura → abre el detalle completo del post.
     */
    private JPanel buildExploreGrid() {
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(C_BG);

        JLabel titulo = new JLabel("Explorar");
        titulo.setFont(F_H2); titulo.setForeground(C_TEXT);
        titulo.setBorder(BorderFactory.createEmptyBorder(0, 0, 14, 0));
        wrapper.add(titulo, BorderLayout.NORTH);

        // Recolectar posts: defaults primero, luego otros
        ArrayList<Publicacion> explorePosts = new ArrayList<>();
        java.util.Set<String> agregados = new java.util.HashSet<>();

        for (String def : InicializadorCuentasDefault.USERNAMES_DEFAULT) {
            for (Publicacion p : sistema.getPublicacionesDeUsuario(def)) {
                String key = p.getAutor()+"|"+p.getFecha()+"|"+p.getHora();
                if (agregados.add(key)) explorePosts.add(p);
            }
        }
        // Otros usuarios públicos que no sigo y no están en mi timeline
        File raiz = new File("INSTA_RAIZ");
        String[] carpetas = raiz.list();
        if (carpetas != null) {
            for (String folder : carpetas) {
                if (folder.equals("stickers_globales")) continue;
                if (sistema.getUsuarioActual() != null && folder.equals(sistema.getUsuarioActual().getUsername())) continue;
                for (Publicacion p : sistema.getPublicacionesDeUsuario(folder)) {
                    String key = p.getAutor()+"|"+p.getFecha()+"|"+p.getHora();
                    if (agregados.add(key)) explorePosts.add(p);
                }
            }
        }

        // Mezcla aleatoria para variedad visual
        Collections.shuffle(explorePosts, new java.util.Random(System.currentTimeMillis() / 60000));

        final int COLS  = 3;
        final int GAP   = 3;
        // Ancho disponible: 1366 - 220(sidebar) - 80(padding) = 1066px / 3 cols
        final int THUMB = (1366 - 220 - 80) / COLS - GAP;

        JPanel grid = new JPanel(new GridLayout(0, COLS, GAP, GAP));
        grid.setBackground(C_BG);

        if (explorePosts.isEmpty()) {
            grid.setLayout(new BorderLayout());
            grid.add(emptyLabel("Sigue a más personas para ver contenido aquí."), BorderLayout.CENTER);
        } else {
            for (Publicacion p : explorePosts) {
                JPanel cell = new JPanel(null);
                cell.setPreferredSize(new Dimension(THUMB, THUMB));
                cell.setBackground(new Color(239,239,239));
                cell.setCursor(new Cursor(Cursor.HAND_CURSOR));

                JLabel imgLbl = new JLabel();
                imgLbl.setBounds(0, 0, THUMB, THUMB);
                imgLbl.setOpaque(true); imgLbl.setBackground(new Color(239,239,239));
                imgLbl.setHorizontalAlignment(SwingConstants.CENTER);
                try {
                    if (p.getRutaImagen()!=null && !p.getRutaImagen().isEmpty() && new File(p.getRutaImagen()).exists()) {
                        ImageIcon cr = crearImagenCropeada(p.getRutaImagen(), THUMB, THUMB);
                        if (cr != null) imgLbl.setIcon(cr);
                    }
                } catch (Exception ignored) {}

                // Overlay con username + badge verificado
                JPanel overlay = new JPanel(new BorderLayout()) {
                    { setOpaque(false); setVisible(false); }
                    @Override protected void paintComponent(Graphics g) {
                        g.setColor(new Color(0,0,0,100)); g.fillRect(0,0,getWidth(),getHeight());
                    }
                };
                overlay.setBounds(0, 0, THUMB, THUMB);
                JPanel overlayBottom = new JPanel(new FlowLayout(FlowLayout.CENTER, 4, 6));
                overlayBottom.setOpaque(false);
                JLabel overlayUser = new JLabel(p.getAutor());
                overlayUser.setFont(F_BOLD); overlayUser.setForeground(Color.WHITE);
                overlayBottom.add(overlayUser);
                // Mostrar badge en overlay si es verificado
                if (InicializadorCuentasDefault.VERIFICADOS.contains(p.getAutor())) {
                    JLabel vBadge = badgeVerificado(p.getAutor());
                    overlayBottom.add(vBadge);
                }
                overlay.add(overlayBottom, BorderLayout.SOUTH);

                cell.add(overlay);
                cell.add(imgLbl);

                cell.addMouseListener(new MouseAdapter() {
                    public void mouseEntered(MouseEvent e) { overlay.setVisible(true); cell.repaint(); }
                    public void mouseExited(MouseEvent e)  { overlay.setVisible(false); cell.repaint(); }
                    public void mouseClicked(MouseEvent e) { abrirDetallePost(p); }
                });
                grid.add(cell);
            }
        }

        wrapper.add(grid, BorderLayout.CENTER);
        return wrapper;
    }

    private JPanel buildUserRow(Usuario u) {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(C_WHITE);
        p.setMaximumSize(new Dimension(700, 68));
        p.setBorder(BorderFactory.createCompoundBorder(new LineBorder(C_BORDER,1), BorderFactory.createEmptyBorder(12,14,12,14)));
        p.setCursor(new Cursor(Cursor.HAND_CURSOR));
        p.addMouseListener(click(() -> cargarVistaPerfil(u.getUsername())));

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT,10,0));
        left.setBackground(C_WHITE);
        left.add(new JLabel(cargarFotoCircular(u.getUsername(),38)));

        JLabel nombre  = new JLabel(u.getUsername()); nombre.setFont(F_BOLD); nombre.setForeground(C_TEXT);
        JLabel nomReal = new JLabel(u.getNombreCompleto()); nomReal.setFont(F_SMALL); nomReal.setForeground(C_TEXT_LIGHT);
        JPanel textos = new JPanel(); textos.setLayout(new BoxLayout(textos,BoxLayout.Y_AXIS));
        textos.setBackground(C_WHITE);

        // Fila de nombre + badge verificado juntos
        JPanel nombreRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 2, 0));
        nombreRow.setBackground(C_WHITE);
        nombreRow.add(nombre);
        nombreRow.add(badgeVerificado(u.getUsername()));
        textos.add(nombreRow); textos.add(nomReal);
        left.add(textos);

        // Candado si es privado
        JLabel tipo = new JLabel();
        if (u.getTipoCuenta() == TipoCuenta.PRIVADA) {
            if (iconsNormal.containsKey("Lock")) tipo.setIcon(iconsNormal.get("Lock"));
            else { tipo.setText("Priv."); tipo.setFont(F_SMALL); tipo.setForeground(C_TEXT_LIGHT); }
        }

        p.add(left, BorderLayout.WEST);
        p.add(tipo, BorderLayout.EAST);

        p.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { p.setBackground(C_BG); left.setBackground(C_BG); textos.setBackground(C_BG); nombreRow.setBackground(C_BG); }
            public void mouseExited(MouseEvent e)  { p.setBackground(C_WHITE); left.setBackground(C_WHITE); textos.setBackground(C_WHITE); nombreRow.setBackground(C_WHITE); }
        });
        return p;
    }

    // ════════════════════════════════════════════════════════════
    //  VISTA 6 — INBOX
    // ════════════════════════════════════════════════════════════
    private void cargarVistaInbox() {
        cargarVistaInboxCon(null);
    }

    /** Abre el inbox y, si otroUsuario != null, abre directamente ese chat */
    private void cargarVistaInboxCon(String otroUsuario) {
        vistaActual = "Messages";
        if (chatTimer != null) chatTimer.stop();
        getContentPane().removeAll();
        setLayout(new BorderLayout());
        add(buildSidebar(), BorderLayout.WEST);

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        split.setDividerLocation(280); split.setBorder(null); split.setDividerSize(1);

        JPanel listPanel = new JPanel(new BorderLayout());
        listPanel.setBackground(C_WHITE);
        listPanel.setBorder(new MatteBorder(0,0,0,1,C_BORDER));

        JPanel listHeader = new JPanel(new BorderLayout());
        listHeader.setBackground(C_WHITE);
        listHeader.setBorder(BorderFactory.createCompoundBorder(new MatteBorder(0,0,1,0,C_BORDER), BorderFactory.createEmptyBorder(16,16,16,16)));
        JLabel lblMsgs = new JLabel(sistema.getUsuarioActual().getUsername());
        lblMsgs.setFont(F_BOLD); lblMsgs.setForeground(C_TEXT);
        JButton btnNuevo = new JButton("+");
        btnNuevo.setFont(new Font("Arial",Font.BOLD,22)); btnNuevo.setBorderPainted(false); btnNuevo.setContentAreaFilled(false);
        btnNuevo.setFocusPainted(false); btnNuevo.setCursor(new Cursor(Cursor.HAND_CURSOR)); btnNuevo.setForeground(C_TEXT);
        listHeader.add(lblMsgs, BorderLayout.WEST); listHeader.add(btnNuevo, BorderLayout.EAST);
        listPanel.add(listHeader, BorderLayout.NORTH);

        JPanel chatList = new JPanel(); chatList.setLayout(new BoxLayout(chatList,BoxLayout.Y_AXIS)); chatList.setBackground(C_WHITE);
        listPanel.add(styledScroll(chatList), BorderLayout.CENTER);

        JPanel chatPanel = new JPanel(new BorderLayout()); chatPanel.setBackground(C_BG);
        JLabel ph = new JLabel("Selecciona un mensaje", SwingConstants.CENTER);
        ph.setFont(F_REGULAR); ph.setForeground(C_TEXT_LIGHT); chatPanel.add(ph, BorderLayout.CENTER);

        split.setLeftComponent(listPanel); split.setRightComponent(chatPanel);
        add(split, BorderLayout.CENTER);

        for (String user : sistema.getChatsRecientes()) {
            int noLeidos = sistema.getMensajesNoLeidos(user);
            JPanel row = new JPanel(new BorderLayout(8,0));
            row.setBackground(C_WHITE); row.setBorder(BorderFactory.createEmptyBorder(10,16,10,16));
            row.setMaximumSize(new Dimension(280,68));
            JLabel fotoLbl=new JLabel(); fotoLbl.setIcon(cargarFotoCircular(user,44));
            row.add(fotoLbl, BorderLayout.WEST);
            JLabel nomLbl=new JLabel("  "+user); nomLbl.setFont(noLeidos>0?F_BOLD:F_REGULAR); nomLbl.setForeground(C_TEXT);
            row.add(nomLbl, BorderLayout.CENTER);
            if (noLeidos>0) {
                JLabel badge=new JLabel(noLeidos>99?"99+":String.valueOf(noLeidos)){
                    @Override protected void paintComponent(Graphics g){
                        Graphics2D g2=(Graphics2D)g.create(); g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                        g2.setColor(C_BLUE); g2.fillOval(0,0,getWidth(),getHeight()); g2.dispose(); super.paintComponent(g);
                    }
                };
                badge.setHorizontalAlignment(SwingConstants.CENTER); badge.setForeground(Color.WHITE); badge.setFont(new Font("Arial",Font.BOLD,10));
                badge.setPreferredSize(new Dimension(20,20)); badge.setOpaque(false);
                JPanel badgeWrap=new JPanel(new FlowLayout(FlowLayout.RIGHT,0,12)); badgeWrap.setBackground(C_WHITE); badgeWrap.add(badge);
                row.add(badgeWrap, BorderLayout.EAST);
            }
            row.setCursor(new Cursor(Cursor.HAND_CURSOR));
            row.addMouseListener(new MouseAdapter() {
                public void mouseClicked(MouseEvent e){lastMessageCount=0; mostrarChatLive(chatPanel,user);}
                public void mouseEntered(MouseEvent e){row.setBackground(C_HOVER);}
                public void mouseExited(MouseEvent e) {row.setBackground(C_WHITE);}
            });
            chatList.add(row);
            JSeparator s=new JSeparator(); s.setForeground(C_BORDER); s.setBackground(C_BORDER); chatList.add(s);
        }

        btnNuevo.addActionListener(e -> {
            String dest=InstaDialog.showInput(this,"Nuevo mensaje","Nombre de usuario");
            if(dest!=null){
                if(sistema.buscarUsuario(dest)==null) InstaDialog.showMessage(this,"Usuario no encontrado.",true);
                // FIX 3: Verificar si puede chatear + mostrar razón si no puede
                else if(!sistema.puedeEnviarMensaje(dest)) {
                    Usuario destU=sistema.buscarUsuario(dest);
                    if(destU!=null && destU.getTipoCuenta()==TipoCuenta.PRIVADA)
                        InstaDialog.showMessage(this,"No puedes enviar mensajes a @"+dest+".\nSu cuenta es privada — deben seguirse mutuamente primero.",true);
                    else InstaDialog.showMessage(this,"No puedes enviarle mensajes a @"+dest+".",true);
                }
                else{lastMessageCount=0; mostrarChatLive(chatPanel,dest);}
            }
        });

        revalidate(); repaint();

        // Si se abrió desde "Mensaje" en un perfil, abrir el chat directamente
        if (otroUsuario != null) {
            SwingUtilities.invokeLater(() -> mostrarChatLive(chatPanel, otroUsuario));
        }
    }

    private void mostrarChatLive(JPanel chatPanel, String otro) {
        if (chatTimer != null) chatTimer.stop();
        chatPanel.removeAll();
        chatPanel.setLayout(new BorderLayout());
        chatPanel.setBackground(C_BG);

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(C_WHITE);
        header.setBorder(BorderFactory.createCompoundBorder(new MatteBorder(0,0,1,0,C_BORDER), BorderFactory.createEmptyBorder(12,16,12,16)));
        JPanel headerLeft=new JPanel(new FlowLayout(FlowLayout.LEFT,10,0)); headerLeft.setBackground(C_WHITE);
        JLabel foto=new JLabel(); foto.setIcon(cargarFotoCircular(otro,36));
        JLabel nomLbl=new JLabel(otro); nomLbl.setFont(F_BOLD); nomLbl.setForeground(C_TEXT);
        nomLbl.setCursor(new Cursor(Cursor.HAND_CURSOR)); nomLbl.addMouseListener(click(()->cargarVistaPerfil(otro)));
        headerLeft.add(foto); headerLeft.add(nomLbl); headerLeft.add(badgeVerificado(otro));
        header.add(headerLeft,BorderLayout.WEST);
        JButton btnDel=new JButton();
        if(iconsNormal.containsKey("Trash"))btnDel.setIcon(iconsNormal.get("Trash")); else{btnDel.setText("Eliminar");btnDel.setFont(F_SMALL);}
        btnDel.setForeground(C_ERROR); btnDel.setToolTipText("Eliminar chat");
        btnDel.setBorderPainted(false); btnDel.setContentAreaFilled(false); btnDel.setFocusPainted(false); btnDel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        header.add(btnDel,BorderLayout.EAST);

        JPanel msgPanel=new JPanel(); msgPanel.setLayout(new BoxLayout(msgPanel,BoxLayout.Y_AXIS)); msgPanel.setBackground(C_BG);
        JScrollPane msgScroll=styledScroll(msgPanel,false);

        JPanel footer=new JPanel(new BorderLayout(0,0)); footer.setBackground(C_WHITE);
        footer.setBorder(BorderFactory.createCompoundBorder(new MatteBorder(1,0,0,0,C_BORDER), BorderFactory.createEmptyBorder(6,12,6,12)));

        JPanel inputRow=new JPanel(new BorderLayout(4,0)); inputRow.setBackground(C_WHITE);
        JTextField txtInput=new JTextField(); txtInput.setFont(F_REGULAR); txtInput.setBackground(C_FIELD);
        txtInput.setBorder(BorderFactory.createCompoundBorder(new LineBorder(C_BORDER,1,true), BorderFactory.createEmptyBorder(9,14,9,14)));
        txtInput.putClientProperty("JTextField.placeholderText","Escribe un mensaje...");
        JButton btnSend=new JButton();
        if(iconsNormal.containsKey("Send"))btnSend.setIcon(iconsNormal.get("Send")); else{btnSend.setText("Enviar");btnSend.setFont(F_BOLD);btnSend.setForeground(C_BLUE);}
        btnSend.setBorderPainted(false); btnSend.setContentAreaFilled(false); btnSend.setFocusPainted(false); btnSend.setCursor(new Cursor(Cursor.HAND_CURSOR));
        JButton btnSticker=new JButton();
        if(iconsNormal.containsKey("Sticker"))btnSticker.setIcon(iconsNormal.get("Sticker")); else{btnSticker.setText(":)");btnSticker.setFont(new Font("Arial",Font.PLAIN,14));}
        btnSticker.setBackground(C_FIELD); btnSticker.setBorderPainted(false); btnSticker.setFocusPainted(false); btnSticker.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JLabel lblCharCount=new JLabel("0/300",SwingConstants.RIGHT); lblCharCount.setFont(new Font("Arial",Font.PLAIN,10)); lblCharCount.setForeground(C_TEXT_LIGHT); lblCharCount.setBorder(BorderFactory.createEmptyBorder(2,0,0,4));
        txtInput.getDocument().addDocumentListener(simpleDocListener(()->{
            int len=txtInput.getText().length();
            lblCharCount.setText(len+"/300");
            if(len>270)lblCharCount.setForeground(C_ERROR); else if(len>240)lblCharCount.setForeground(new Color(230,140,0)); else lblCharCount.setForeground(C_TEXT_LIGHT);
            btnSend.setEnabled(len<=300&&len>0);
        }));

        inputRow.add(btnSticker,BorderLayout.WEST); inputRow.add(txtInput,BorderLayout.CENTER); inputRow.add(btnSend,BorderLayout.EAST);
        footer.add(inputRow,BorderLayout.CENTER); footer.add(lblCharCount,BorderLayout.SOUTH);
        chatPanel.add(header,BorderLayout.NORTH); chatPanel.add(msgScroll,BorderLayout.CENTER); chatPanel.add(footer,BorderLayout.SOUTH);

        ActionListener send=e->{
            if(!txtInput.getText().isEmpty()){
                sistema.enviarMensaje(otro,txtInput.getText(),"TEXTO");
                txtInput.setText(""); lastMessageCount=0; refrescarMensajes(msgPanel,otro);
            }
        };
        btnSend.addActionListener(send); txtInput.addActionListener(send);
        btnDel.addActionListener(e->{
            boolean yes=InstaDialog.showConfirm(chatPanel,"¿Eliminar historial con "+otro+"?","Eliminar",true);
            if(yes){sistema.eliminarConversacion(otro);lastMessageCount=0;refrescarMensajes(msgPanel,otro);}
        });

        btnSticker.addActionListener(ev -> {
            ArrayList<String> stickers=sistema.getTodosStickers(sistema.getUsuarioActual().getUsername());
            JDialog sd=new JDialog(this,"Stickers",true);
            sd.setSize(420,460); sd.setLocationRelativeTo(chatPanel);
            sd.setLayout(new BorderLayout()); sd.getContentPane().setBackground(C_WHITE);
            sd.add(buildDialogHeader("Stickers",sd),BorderLayout.NORTH);
            JPanel grid=new JPanel(new GridLayout(0,4,8,8)); grid.setBackground(C_WHITE); grid.setBorder(BorderFactory.createEmptyBorder(8,8,8,8));
            JButton btnImport=new JButton("+"); btnImport.setFont(new Font("Arial",Font.BOLD,26));
            btnImport.setPreferredSize(new Dimension(88,88)); btnImport.setMinimumSize(new Dimension(88,88));
            btnImport.setBackground(new Color(245,245,245)); btnImport.setBorderPainted(false); btnImport.setContentAreaFilled(false); btnImport.setFocusPainted(false); btnImport.setCursor(new Cursor(Cursor.HAND_CURSOR));
            btnImport.addActionListener(se->{
                JFileChooser fc=new JFileChooser(); fc.setFileFilter(new FileNameExtensionFilter("Imágenes","png","jpg","jpeg"));
                if(fc.showOpenDialog(sd)==JFileChooser.APPROVE_OPTION){sistema.guardarStickerPersonal(fc.getSelectedFile(),sistema.getUsuarioActual().getUsername());InstaDialog.showMessage(sd,"Sticker importado. ¡Ya puedes usarlo!");}
                sd.dispose();
            });
            grid.add(btnImport);
            for(String r:stickers){
                JButton btnS=new JButton(); btnS.setPreferredSize(new Dimension(88,88)); btnS.setMinimumSize(new Dimension(88,88));
                btnS.setBorderPainted(false); btnS.setContentAreaFilled(false); btnS.setFocusPainted(false); btnS.setCursor(new Cursor(Cursor.HAND_CURSOR));
                try{File f=new File(r);if(f.exists())btnS.setIcon(new ImageIcon(new ImageIcon(r).getImage().getScaledInstance(68,68,Image.SCALE_SMOOTH)));else btnS.setText("?");}catch(Exception ignored){btnS.setText("?");}
                btnS.addActionListener(se->{sistema.enviarMensaje(otro,r,"STICKER");lastMessageCount=0;refrescarMensajes(msgPanel,otro);sd.dispose();});
                grid.add(btnS);
            }
            JScrollPane sp=styledScroll(grid); sp.setPreferredSize(new Dimension(400,380)); sp.getVerticalScrollBar().setUnitIncrement(30);
            sd.add(sp,BorderLayout.CENTER); sd.setVisible(true);
        });

        lastMessageCount=0; refrescarMensajes(msgPanel,otro);
        chatTimer=new Timer(2000,ev->refrescarMensajes(msgPanel,otro)); chatTimer.start();
        chatPanel.revalidate(); chatPanel.repaint();
    }

    private void refrescarMensajes(JPanel panel, String otro) {
        sistema.marcarComoLeido(otro);
        ArrayList<Mensaje> hist = sistema.getConversacion(otro);
        if (hist.size() == lastMessageCount) return;
        lastMessageCount = hist.size();

        JScrollPane scrollP=(JScrollPane)SwingUtilities.getAncestorOfClass(JScrollPane.class,panel);
        JScrollBar vbar=scrollP!=null?scrollP.getVerticalScrollBar():null;
        int curVal=vbar!=null?vbar.getValue():0;
        boolean atBottom=vbar==null||vbar.getValue()>=vbar.getMaximum()-200;

        panel.removeAll();
        Mensaje lastSent=null;

        for (Mensaje m : hist) {
            boolean mio = m.getEmisor().equals(sistema.getUsuarioActual().getUsername());
            if (mio) lastSent=m;

            JPanel row=new JPanel(new BorderLayout()); row.setOpaque(false); row.setBorder(BorderFactory.createEmptyBorder(1,10,1,10));
            JPanel bubble=new JPanel(new BorderLayout());
            boolean esSticker=m instanceof instagram.MensajeSticker;
            boolean isMioTextBubble=mio&&!esSticker&&m.getContenido()!=null&&!m.getContenido().startsWith("SHARE|");
            Color txtColor=isMioTextBubble?Color.WHITE:C_TEXT;

            if (esSticker) {
                JLabel img=new JLabel(); img.setHorizontalAlignment(SwingConstants.CENTER);
                try{File f=new File(m.getContenido());if(f.exists())img.setIcon(new ImageIcon(new ImageIcon(m.getContenido()).getImage().getScaledInstance(96,96,Image.SCALE_SMOOTH)));else img.setText("[Sticker]");}catch(Exception ignored){img.setText("[Sticker]");}
                bubble.add(img,BorderLayout.CENTER); bubble.setOpaque(false); bubble.setBorder(BorderFactory.createEmptyBorder(4,8,4,8));
            } else {
                String texto=m.getContenido();
                if (texto.startsWith("SHARE|")) {
                    String[] d=texto.split("\\|",4);
                    String autorPost=d.length>1?d[1]:"?", rutaImgPost=d.length>2?d[2]:"", caption=d.length>3?d[3]:"";
                    JPanel card=new JPanel(new BorderLayout(0,0)){
                        @Override protected void paintComponent(Graphics g){
                            Graphics2D g2=(Graphics2D)g.create(); g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                            g2.setColor(C_WHITE); g2.fill(new RoundRectangle2D.Double(0,0,getWidth(),getHeight(),12,12));
                            g2.setColor(C_BORDER); g2.setStroke(new BasicStroke(1f)); g2.draw(new RoundRectangle2D.Double(0.5,0.5,getWidth()-1,getHeight()-1,12,12)); g2.dispose();
                        }
                    };
                    card.setOpaque(false); card.setPreferredSize(new Dimension(240,200)); card.setCursor(new Cursor(Cursor.HAND_CURSOR));
                    JLabel imgL=new JLabel(); imgL.setHorizontalAlignment(SwingConstants.CENTER); imgL.setBackground(new Color(239,239,239)); imgL.setOpaque(true);
                    try{File imgF=new File(rutaImgPost);if(imgF.exists()){Image sc=new ImageIcon(rutaImgPost).getImage().getScaledInstance(240,140,Image.SCALE_SMOOTH);imgL.setIcon(new ImageIcon(sc));}else{imgL.setText("Sin imagen");imgL.setForeground(C_TEXT_LIGHT);}}catch(Exception ignored){}
                    imgL.setPreferredSize(new Dimension(240,140)); card.add(imgL,BorderLayout.NORTH);
                    JPanel info2=new JPanel(); info2.setLayout(new BoxLayout(info2,BoxLayout.Y_AXIS)); info2.setOpaque(false); info2.setBorder(BorderFactory.createEmptyBorder(6,10,6,10));
                    JPanel hp=new JPanel(new FlowLayout(FlowLayout.LEFT,6,0)); hp.setOpaque(false); hp.add(new JLabel(cargarFotoCircular(autorPost,20)));
                    JLabel lblA=new JLabel(autorPost); lblA.setFont(F_BOLD); lblA.setForeground(C_TEXT); hp.add(lblA); info2.add(hp);
                    if(!caption.isEmpty()){String tr=caption.length()>60?caption.substring(0,57)+"…":caption;JLabel lc=new JLabel("<html><font size='2' color='gray'>"+tr+"</font></html>");lc.setBorder(BorderFactory.createEmptyBorder(2,0,0,0));info2.add(lc);}
                    card.add(info2,BorderLayout.CENTER);
                    String rutaFinal=rutaImgPost;
                    card.addMouseListener(click(()->{try{File imgF=new File(rutaFinal);if(imgF.exists()){JDialog dd=new JDialog(this,"Post de "+autorPost,true);dd.setSize(660,720);dd.setLocationRelativeTo(this);dd.setLayout(new BorderLayout());dd.getContentPane().setBackground(C_WHITE);dd.add(new JScrollPane(new JLabel(new ImageIcon(new ImageIcon(rutaFinal).getImage().getScaledInstance(600,600,Image.SCALE_SMOOTH)))),BorderLayout.CENTER);dd.setVisible(true);}}catch(Exception ignored){}}));
                    bubble.add(card,BorderLayout.CENTER); bubble.setBackground(new Color(0,0,0,0)); bubble.setOpaque(false);
                } else {
                    // FIX 6: burbujas que se adaptan al texto — usar JTextArea con width preferred calculado
                    JTextArea ta=new JTextArea(texto); ta.setLineWrap(true); ta.setWrapStyleWord(true);
                    ta.setFont(F_REGULAR); ta.setEditable(false); ta.setOpaque(false); ta.setForeground(txtColor);
                    // Calcular ancho ideal: máx 240px, mín suficiente para el texto
                    FontMetrics fm = ta.getFontMetrics(F_REGULAR);
                    int textWidth = fm.stringWidth(texto);
                    int preferredW = Math.min(240, Math.max(80, textWidth + 30));
                    ta.setPreferredSize(null); // dejar que wrap calcule el alto
                    ta.setSize(new Dimension(preferredW, Integer.MAX_VALUE));
                    bubble.add(ta,BorderLayout.CENTER);
                }
            }

            if (mio) {
                boolean isTxtBubble=!esSticker&&(m.getContenido()==null||!m.getContenido().startsWith("SHARE|"));
                if(esSticker){row.add(bubble,BorderLayout.EAST);}
                else{
                    Color bc=isTxtBubble?C_BLUE:new Color(0,0,0,0);
                    JPanel wrapper=new JPanel(new BorderLayout()){
                        @Override protected void paintComponent(Graphics g){
                            if(!isTxtBubble)return;
                            Graphics2D g2=(Graphics2D)g.create(); g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                            g2.setColor(bc); g2.fill(new RoundRectangle2D.Double(0,0,getWidth(),getHeight(),22,22)); g2.dispose();
                        }
                    };
                    wrapper.setOpaque(false); bubble.setOpaque(false); bubble.setBorder(BorderFactory.createEmptyBorder(9,14,9,14));
                    wrapper.add(bubble,BorderLayout.CENTER); wrapper.setMaximumSize(new Dimension(270,Integer.MAX_VALUE));
                    row.add(wrapper,BorderLayout.EAST);
                }
            } else {
                boolean isTxtBubble=!esSticker&&(m.getContenido()==null||!m.getContenido().startsWith("SHARE|"));
                if(esSticker){row.add(bubble,BorderLayout.WEST);}
                else{
                    JPanel wrapper=new JPanel(new BorderLayout()){
                        @Override protected void paintComponent(Graphics g){
                            if(!isTxtBubble)return;
                            Graphics2D g2=(Graphics2D)g.create(); g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                            g2.setColor(C_WHITE); g2.fill(new RoundRectangle2D.Double(0,0,getWidth(),getHeight(),22,22));
                            g2.setColor(C_BORDER); g2.setStroke(new BasicStroke(1f)); g2.draw(new RoundRectangle2D.Double(0.5,0.5,getWidth()-1,getHeight()-1,22,22)); g2.dispose();
                        }
                    };
                    wrapper.setOpaque(false); bubble.setOpaque(false); bubble.setBorder(BorderFactory.createEmptyBorder(9,14,9,14));
                    wrapper.add(bubble,BorderLayout.CENTER); wrapper.setMaximumSize(new Dimension(270,Integer.MAX_VALUE));
                    row.add(wrapper,BorderLayout.WEST);
                }
            }

            JLabel ts=new JLabel(m.getHoraFormateada()); ts.setFont(new Font("Arial",Font.PLAIN,9)); ts.setForeground(C_TEXT_LIGHT); ts.setBorder(BorderFactory.createEmptyBorder(0,14,0,14));
            JPanel tsRow=new JPanel(new BorderLayout()); tsRow.setOpaque(false); tsRow.setBorder(BorderFactory.createEmptyBorder(1,0,5,0));
            tsRow.add(ts,mio?BorderLayout.EAST:BorderLayout.WEST);
            panel.add(row); panel.add(tsRow);
        }

        // FIX 7: Estado de leído/enviado — ahora re-lee el estado actual del archivo
        if (lastSent != null) {
            // Re-verificar el estado real del mensaje desde la conversación actual
            boolean leido = false;
            for (Mensaje m : hist) {
                if (m.getEmisor().equals(sistema.getUsuarioActual().getUsername())
                        && m.getReceptor().equals(otro)) {
                    leido = "LEIDO".equals(m.getEstado());
                }
            }
            JLabel st=new JLabel(leido?"Leído ✓✓":"Enviado ✓");
            st.setFont(new Font("Arial",Font.PLAIN,10));
            st.setForeground(leido?C_BLUE:C_TEXT_LIGHT);
            st.setBorder(BorderFactory.createEmptyBorder(0,14,8,14));
            JPanel sr=new JPanel(new BorderLayout()); sr.setOpaque(false); sr.add(st,BorderLayout.EAST);
            panel.add(sr);
        }

        panel.revalidate(); panel.repaint();
        SwingUtilities.invokeLater(()->{ if(vbar!=null) vbar.setValue(atBottom?vbar.getMaximum():curVal); });
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

        JPanel main=new JPanel(new BorderLayout()); main.setBackground(C_BG); main.setBorder(BorderFactory.createEmptyBorder(40,50,40,50));
        JLabel title=new JLabel("Actividad"); title.setFont(F_H1); title.setForeground(C_TEXT); title.setBorder(BorderFactory.createEmptyBorder(0,0,20,0));
        main.add(title,BorderLayout.NORTH);
        JPanel content=new JPanel(); content.setLayout(new BoxLayout(content,BoxLayout.Y_AXIS)); content.setBackground(C_BG);
        main.add(styledScroll(content),BorderLayout.CENTER);
        add(main,BorderLayout.CENTER);

        boolean hay=false;

        ArrayList<String> solic=sistema.getSolicitudes();
        if(!solic.isEmpty()){hay=true;content.add(sectionTitle("Solicitudes de seguimiento"));for(String u:solic){content.add(buildSolicitudRow(u));content.add(Box.createVerticalStrut(6));}content.add(Box.createVerticalStrut(24));}

        ArrayList<String> notifs=sistema.getNotificacionesGenerales();
        if(!notifs.isEmpty()){
            hay=true; content.add(sectionTitle("Hoy"));
            for(String line:notifs){
                String[] d=line.split("\\|"); if(d.length<2)continue;
                String tipo=d[0],quien=d[1];
                JPanel row=new JPanel(new BorderLayout(10,0)); row.setBackground(C_WHITE); row.setMaximumSize(new Dimension(650,60));
                row.setBorder(BorderFactory.createCompoundBorder(new LineBorder(C_BORDER,1),BorderFactory.createEmptyBorder(10,14,10,14)));
                row.add(new JLabel(cargarFotoCircular(quien,36)),BorderLayout.WEST);
                JLabel txt=new JLabel(); txt.setFont(F_REGULAR); txt.setForeground(C_TEXT); txt.setCursor(new Cursor(Cursor.HAND_CURSOR)); txt.addMouseListener(click(()->cargarVistaPerfil(quien)));
                if("SEGUIDOR".equals(tipo))txt.setText("<html><b>"+quien+"</b> empezó a seguirte.</html>");
                else if("MENCION".equals(tipo))txt.setText("<html><b>"+quien+"</b> te mencionó.</html>");
                row.add(txt,BorderLayout.CENTER); content.add(row); content.add(Box.createVerticalStrut(6));
            }
            content.add(Box.createVerticalStrut(20));
        }

        ArrayList<String> likes=sistema.getNotificacionesLikes();
        if(!likes.isEmpty()){
            hay=true; content.add(sectionTitle("Me gusta en tus publicaciones"));
            for(String linea:likes){
                String[] d=linea.split("\\|"); if(d.length<3)continue;
                String fecha=d[1],quien=d[2],rutaImg=d.length>=4?d[3]:"";
                JPanel row=new JPanel(new BorderLayout(10,0)); row.setBackground(C_WHITE); row.setMaximumSize(new Dimension(650,64));
                row.setBorder(BorderFactory.createCompoundBorder(new LineBorder(C_BORDER,1),BorderFactory.createEmptyBorder(10,14,10,14)));
                row.add(new JLabel(cargarFotoCircular(quien,36)),BorderLayout.WEST);
                JLabel txt=new JLabel("<html><b>"+quien+"</b> le dio me gusta a tu publicación del "+fecha+"</html>"); txt.setFont(F_REGULAR); txt.setForeground(C_TEXT); txt.setCursor(new Cursor(Cursor.HAND_CURSOR)); txt.addMouseListener(click(()->cargarVistaPerfil(quien)));
                row.add(txt,BorderLayout.CENTER);
                if(rutaImg!=null&&!rutaImg.isEmpty()){File imgFile=new File(rutaImg);if(imgFile.exists()){try{JLabel thumb=new JLabel();thumb.setIcon(new ImageIcon(new ImageIcon(rutaImg).getImage().getScaledInstance(44,44,Image.SCALE_SMOOTH)));thumb.setBorder(new LineBorder(C_BORDER,1));row.add(thumb,BorderLayout.EAST);}catch(Exception ignored){}}}
                content.add(row); content.add(Box.createVerticalStrut(6));
            }
        }

        ArrayList<Publicacion> menciones=sistema.getMenciones();
        if(!menciones.isEmpty()){
            hay=true; content.add(Box.createVerticalStrut(8)); content.add(sectionTitle("Publicaciones donde te mencionaron"));
            for(Publicacion p:menciones){
                JPanel row=new JPanel(new BorderLayout(10,0)); row.setBackground(C_WHITE); row.setMaximumSize(new Dimension(650,70));
                row.setBorder(BorderFactory.createCompoundBorder(new LineBorder(C_BORDER,1),BorderFactory.createEmptyBorder(10,14,10,14)));
                row.setCursor(new Cursor(Cursor.HAND_CURSOR)); row.addMouseListener(click(()->cargarVistaPerfil(p.getAutor())));
                row.add(new JLabel(cargarFotoCircular(p.getAutor(),36)),BorderLayout.WEST);
                String fragmento=p.getContenido().length()>60?p.getContenido().substring(0,57)+"…":p.getContenido();
                JPanel textoPanel=new JPanel(); textoPanel.setLayout(new BoxLayout(textoPanel,BoxLayout.Y_AXIS)); textoPanel.setBackground(C_WHITE);
                JLabel lblA=new JLabel("<html><b>"+p.getAutor()+"</b> te mencionó</html>"); lblA.setFont(F_REGULAR); lblA.setForeground(C_TEXT);
                JLabel lblF=new JLabel("<html><font color='gray'>"+fragmento+"</font></html>"); lblF.setFont(F_SMALL);
                textoPanel.add(lblA); textoPanel.add(lblF); row.add(textoPanel,BorderLayout.CENTER);
                if(p.getRutaImagen()!=null&&!p.getRutaImagen().isEmpty()){File imgF=new File(p.getRutaImagen());if(imgF.exists()){try{JLabel thumb=new JLabel(new ImageIcon(new ImageIcon(p.getRutaImagen()).getImage().getScaledInstance(44,44,Image.SCALE_SMOOTH)));thumb.setBorder(new LineBorder(C_BORDER,1));row.add(thumb,BorderLayout.EAST);}catch(Exception ignored){}}}
                content.add(row); content.add(Box.createVerticalStrut(6));
            }
        }

        if(!hay){JLabel nada=emptyLabel("No hay notificaciones nuevas.");nada.setAlignmentX(Component.CENTER_ALIGNMENT);content.add(Box.createVerticalStrut(80));content.add(nada);}
        revalidate(); repaint();
    }

    private JLabel sectionTitle(String text) {
        JLabel l=new JLabel(text); l.setFont(F_BOLD); l.setForeground(C_TEXT); l.setBorder(BorderFactory.createEmptyBorder(0,0,8,0)); return l;
    }

    private JPanel buildSolicitudRow(String username) {
        JPanel p=new JPanel(new BorderLayout(10,0)); p.setBackground(C_WHITE); p.setMaximumSize(new Dimension(650,62));
        p.setBorder(BorderFactory.createCompoundBorder(new LineBorder(C_BORDER,1),BorderFactory.createEmptyBorder(10,14,10,14)));
        p.add(new JLabel(cargarFotoCircular(username,36)),BorderLayout.WEST);
        JLabel lbl=new JLabel("<html><b>"+username+"</b> quiere seguirte.</html>"); lbl.setFont(F_REGULAR); lbl.setForeground(C_TEXT); lbl.setCursor(new Cursor(Cursor.HAND_CURSOR)); lbl.addMouseListener(click(()->cargarVistaPerfil(username)));
        p.add(lbl,BorderLayout.CENTER);
        JPanel btns=new JPanel(new FlowLayout(FlowLayout.RIGHT,8,0)); btns.setBackground(C_WHITE);
        JButton ok=buildPrimaryBtn("Confirmar"); ok.setPreferredSize(new Dimension(100,30));
        JButton no=buildSecondaryBtn("Eliminar"); no.setPreferredSize(new Dimension(90,30));
        ok.addActionListener(e->{sistema.aceptarSolicitud(username);cargarVistaNotificaciones();});
        no.addActionListener(e->{sistema.rechazarSolicitud(username);cargarVistaNotificaciones();});
        btns.add(ok); btns.add(no); p.add(btns,BorderLayout.EAST); return p;
    }

    // ════════════════════════════════════════════════════════════
    //  EDITAR PERFIL
    // ════════════════════════════════════════════════════════════
    private void abrirDialogoEditarPerfil() {
        JDialog d=new JDialog(this,"Editar perfil",true); d.setSize(420,360); d.setLocationRelativeTo(this); d.setLayout(new BorderLayout()); d.getContentPane().setBackground(C_WHITE);
        d.add(buildDialogHeader("Editar perfil",d),BorderLayout.NORTH);
        JPanel body=new JPanel(); body.setLayout(new BoxLayout(body,BoxLayout.Y_AXIS)); body.setBackground(C_WHITE); body.setBorder(BorderFactory.createEmptyBorder(20,24,20,24));
        JLabel lNom=new JLabel("Nombre completo"); lNom.setFont(F_SMALL); lNom.setForeground(C_TEXT_LIGHT);
        JTextField txtNom=buildField(sistema.getUsuarioActual().getNombreCompleto()); txtNom.setText(sistema.getUsuarioActual().getNombreCompleto()); txtNom.setForeground(C_TEXT);
        JLabel lPass=new JLabel("Nueva contraseña (vacío = no cambiar)"); lPass.setFont(F_SMALL); lPass.setForeground(C_TEXT_LIGHT);
        JPasswordField txtPass=new JPasswordField(); txtPass.setBackground(C_FIELD); txtPass.setFont(F_REGULAR); txtPass.setBorder(BorderFactory.createCompoundBorder(new LineBorder(C_BORDER,1,true),BorderFactory.createEmptyBorder(9,12,9,12)));
        JButton btnFoto=buildSecondaryBtn("Cambiar foto de perfil");
        final String[] nuevaFoto={null};
        btnFoto.addActionListener(e->{JFileChooser fc=new JFileChooser();fc.setFileFilter(new FileNameExtensionFilter("Imágenes","jpg","png","jpeg"));if(fc.showOpenDialog(d)==JFileChooser.APPROVE_OPTION){String ruta=sistema.procesarImagenPerfil(fc.getSelectedFile(),sistema.getUsuarioActual().getUsername(),"profile_"+System.currentTimeMillis());if(ruta!=null){nuevaFoto[0]=ruta;btnFoto.setText("✓ Foto seleccionada");}}});
        body.add(lNom);body.add(Box.createVerticalStrut(4));body.add(txtNom);body.add(Box.createVerticalStrut(14));body.add(lPass);body.add(Box.createVerticalStrut(4));body.add(txtPass);body.add(Box.createVerticalStrut(16));body.add(btnFoto);
        d.add(body,BorderLayout.CENTER);
        JPanel footer=new JPanel(new FlowLayout(FlowLayout.RIGHT,12,12)); footer.setBackground(C_WHITE); footer.setBorder(new MatteBorder(1,0,0,0,C_BORDER));
        JButton btnCancel=buildSecondaryBtn("Cancelar"); JButton btnSave=buildPrimaryBtn("Guardar"); footer.add(btnCancel); footer.add(btnSave); d.add(footer,BorderLayout.SOUTH);
        btnCancel.addActionListener(e->d.dispose());
        btnSave.addActionListener(e->{String nom=txtNom.getText().trim();if(nom.isEmpty()){InstaDialog.showMessage(d,"El nombre no puede estar vacío.",true);return;}String pass=new String(txtPass.getPassword()).trim();boolean ok=sistema.actualizarDatosUsuario(nom,pass);if(nuevaFoto[0]!=null)sistema.actualizarFotoPerfil(sistema.getUsuarioActual().getUsername(),nuevaFoto[0]);if(ok){d.dispose();cargarVistaPerfil(sistema.getUsuarioActual().getUsername());}else InstaDialog.showMessage(d,"Error al guardar.",true);});
        d.setVisible(true);
    }

    // ════════════════════════════════════════════════════════════
    //  LISTA SEGUIDORES / SIGUIENDO
    // ════════════════════════════════════════════════════════════
    private void mostrarListaUsuarios(ArrayList<String> usuarios, String titulo, String tipo) {
        if(usuarios.isEmpty()){InstaDialog.showMessage(this,"La lista está vacía.");return;}
        JDialog d=new JDialog(this,titulo,true); d.setSize(360,520); d.setLocationRelativeTo(this); d.setLayout(new BorderLayout()); d.getContentPane().setBackground(C_WHITE);
        d.add(buildDialogHeader(titulo,d),BorderLayout.NORTH);
        JPanel list=new JPanel(); list.setLayout(new BoxLayout(list,BoxLayout.Y_AXIS)); list.setBackground(C_WHITE); list.setBorder(BorderFactory.createEmptyBorder(8,8,8,8));
        ArrayList<String> inv=new ArrayList<>(usuarios); Collections.reverse(inv);
        boolean esFollowers="Followers".equals(tipo);
        for(String usr:inv){
            JPanel row=new JPanel(new BorderLayout()); row.setBackground(C_WHITE); row.setMaximumSize(new Dimension(340,60));
            row.setBorder(BorderFactory.createCompoundBorder(new LineBorder(C_BORDER,1,true),BorderFactory.createEmptyBorder(10,12,10,12)));
            JPanel left=new JPanel(new FlowLayout(FlowLayout.LEFT,8,0)); left.setBackground(C_WHITE);
            left.add(new JLabel(cargarFotoCircular(usr,36)));
            JLabel lbl=new JLabel(usr); lbl.setFont(F_BOLD); lbl.setForeground(C_TEXT); left.add(lbl); row.add(left,BorderLayout.WEST);
            JButton btn;
            if(esFollowers){btn=buildSecondaryBtn("Eliminar");btn.setPreferredSize(new Dimension(90,30));btn.addActionListener(e->{boolean yes=InstaDialog.showConfirm(d,"¿Eliminar a "+usr+"?","Eliminar",true);if(yes){sistema.eliminarSeguidor(usr);d.dispose();mostrarListaUsuarios(sistema.getListaFollowers(sistema.getUsuarioActual().getUsername()),"Seguidores","Followers");cargarVistaPerfil(sistema.getUsuarioActual().getUsername());}});}
            else{btn=buildPrimaryBtn("Ver perfil");btn.setPreferredSize(new Dimension(100,30));btn.addActionListener(e->{d.dispose();cargarVistaPerfil(usr);});}
            JPanel rp=new JPanel(new FlowLayout(FlowLayout.RIGHT,0,0)); rp.setBackground(C_WHITE); rp.add(btn); row.add(rp,BorderLayout.EAST);
            list.add(row); list.add(Box.createVerticalStrut(6));
        }
        d.add(styledScroll(list),BorderLayout.CENTER); d.setVisible(true);
    }

    // ════════════════════════════════════════════════════════════
    //  BÚSQUEDA POR HASHTAG
    // ════════════════════════════════════════════════════════════
    private void cargarVistaBusquedaHashtag(String hashtag) {
        vistaActual="Search"; getContentPane().removeAll(); setLayout(new BorderLayout()); getContentPane().setBackground(C_BG); add(buildSidebar(),BorderLayout.WEST);
        JPanel main=new JPanel(new BorderLayout()); main.setBackground(C_BG); main.setBorder(BorderFactory.createEmptyBorder(40,40,40,40));
        JLabel title=new JLabel("Resultados: "+hashtag); title.setFont(F_H2); title.setForeground(C_TEXT); title.setBorder(BorderFactory.createEmptyBorder(0,0,20,0));
        main.add(title,BorderLayout.NORTH);
        JPanel results=new JPanel(); results.setLayout(new BoxLayout(results,BoxLayout.Y_AXIS)); results.setBackground(C_BG);
        main.add(styledScroll(results),BorderLayout.CENTER); add(main,BorderLayout.CENTER);
        ArrayList<Publicacion> posts=sistema.buscarPorHashtag(hashtag);
        if(posts.isEmpty())results.add(emptyLabel("Sin publicaciones con "+hashtag));
        else for(Publicacion p:posts){results.add(buildPost(p));results.add(Box.createVerticalStrut(4));}
        revalidate(); repaint();
    }

    // ════════════════════════════════════════════════════════════
    //  FOTOS CIRCULARES
    // ════════════════════════════════════════════════════════════
    private ImageIcon cargarFotoCircular(String username, int d) {
        Usuario u=sistema.buscarUsuario(username);
        if(u!=null){String ruta=u.getFotoPerfil();if(ruta!=null&&!ruta.isEmpty()&&!ruta.equals("null")){File f=new File(ruta);if(f.exists()){try{BufferedImage bi=javax.imageio.ImageIO.read(f);if(bi!=null)return circularIcon(new ImageIcon(bi),d);}catch(Exception ignored){}}}}
        return avatarDefault(username,d);
    }

    private ImageIcon circularIcon(ImageIcon src, int d) {
        BufferedImage out=new BufferedImage(d,d,BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2=out.createGraphics(); g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setClip(new Ellipse2D.Double(0,0,d,d)); g2.drawImage(src.getImage().getScaledInstance(d,d,Image.SCALE_SMOOTH),0,0,null);
        g2.setClip(null); g2.setColor(C_BORDER); g2.setStroke(new BasicStroke(1f)); g2.drawOval(0,0,d-1,d-1); g2.dispose();
        return new ImageIcon(out);
    }

    private ImageIcon avatarDefault(String username, int d) {
        BufferedImage img=new BufferedImage(d,d,BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2=img.createGraphics(); g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(new Color(239,239,239)); g2.fillOval(0,0,d,d); g2.setColor(new Color(142,142,142));
        g2.setFont(new Font("Arial",Font.BOLD,d/2));
        String l=username.isEmpty()?"?":username.substring(0,1).toUpperCase();
        FontMetrics fm=g2.getFontMetrics();
        g2.drawString(l,(d-fm.stringWidth(l))/2,fm.getAscent()+(d-fm.getAscent())/2); g2.dispose();
        return new ImageIcon(img);
    }

    // ════════════════════════════════════════════════════════════
    //  HELPERS UI
    // ════════════════════════════════════════════════════════════
    private JTextField buildField(String ph) {
        JTextField t=new JTextField(); t.setBackground(C_FIELD); t.setFont(F_REGULAR); t.setText(ph); t.setForeground(C_PLACEHOLDER);
        t.setBorder(BorderFactory.createCompoundBorder(new LineBorder(C_BORDER,1,true),BorderFactory.createEmptyBorder(9,12,9,12)));
        t.addFocusListener(new FocusAdapter(){public void focusGained(FocusEvent e){if(t.getText().equals(ph)){t.setText("");t.setForeground(C_TEXT);}}
        public void focusLost(FocusEvent e){if(t.getText().isEmpty()){t.setText(ph);t.setForeground(C_PLACEHOLDER);}}}); return t;
    }

    private JPanel buildPassPanel() {
        JPanel p=new JPanel(new BorderLayout()); p.setBackground(C_FIELD); p.setBorder(new LineBorder(C_BORDER,1,true));
        JPasswordField pf=new JPasswordField(); pf.setBackground(C_FIELD); pf.setFont(F_REGULAR); pf.setBorder(BorderFactory.createEmptyBorder(9,12,9,6));
        pf.setText("Password"); pf.setEchoChar((char)0); pf.setForeground(C_PLACEHOLDER);
        JLabel eye=new JLabel(); eye.setCursor(new Cursor(Cursor.HAND_CURSOR)); eye.setBorder(BorderFactory.createEmptyBorder(0,0,0,10));
        eye.setIcon(iconEyeClosed!=null?iconEyeClosed:null); if(iconEyeClosed==null)eye.setText("ver");
        eye.addMouseListener(click(()->{if(new String(pf.getPassword()).equals("Password"))return;if(pf.getEchoChar()!=0){pf.setEchoChar((char)0);eye.setIcon(iconEyeOpen);if(iconEyeOpen==null)eye.setText("ocultar");}else{pf.setEchoChar('\u25CF');eye.setIcon(iconEyeClosed);if(iconEyeClosed==null)eye.setText("ver");}}));
        pf.addFocusListener(new FocusAdapter(){public void focusGained(FocusEvent e){if(new String(pf.getPassword()).equals("Password")){pf.setText("");pf.setForeground(C_TEXT);pf.setEchoChar('●');}}
        public void focusLost(FocusEvent e){if(pf.getPassword().length==0){pf.setText("Password");pf.setEchoChar((char)0);pf.setForeground(C_PLACEHOLDER);}}});
        p.add(pf,BorderLayout.CENTER); p.add(eye,BorderLayout.EAST); return p;
    }

    private JPanel buildFieldWithIcon(String ph) {
        JPanel p=new JPanel(new BorderLayout()); p.setBackground(C_FIELD); p.setBorder(new LineBorder(C_BORDER,1,true));
        JTextField t=new JTextField(); t.setBackground(C_FIELD); t.setFont(F_REGULAR); t.setText(ph); t.setForeground(C_PLACEHOLDER); t.setBorder(BorderFactory.createEmptyBorder(9,12,9,6));
        JLabel ico=new JLabel(); ico.setPreferredSize(new Dimension(28,28)); ico.setBorder(BorderFactory.createEmptyBorder(0,0,0,6));
        t.addFocusListener(new FocusAdapter(){public void focusGained(FocusEvent e){if(t.getText().equals(ph)){t.setText("");t.setForeground(C_TEXT);}}
        public void focusLost(FocusEvent e){if(t.getText().isEmpty()){t.setText(ph);t.setForeground(C_PLACEHOLDER);}}});
        p.add(t,BorderLayout.CENTER); p.add(ico,BorderLayout.EAST); return p;
    }

    private JSpinner buildSpinner(int val) {
        JSpinner s=new JSpinner(new SpinnerNumberModel(val,1,100,1)); s.setFont(F_REGULAR);
        JComponent ed=s.getEditor(); JFormattedTextField tf=((JSpinner.DefaultEditor)ed).getTextField();
        tf.setBackground(C_FIELD); tf.setFont(F_REGULAR); tf.setBorder(null); ed.setBackground(C_FIELD);
        s.setBorder(new LineBorder(C_BORDER,1,true)); return s;
    }

    private JComboBox<String> buildCombo(String... items) {
        JComboBox<String> c=new JComboBox<>(items); c.setFont(F_REGULAR); c.setBackground(C_FIELD); return c;
    }

    private JButton buildPrimaryBtn(String text) {
        JButton b=new JButton(text); b.setFont(F_BOLD); b.setForeground(C_WHITE); b.setBackground(C_BLUE);
        b.setBorderPainted(false); b.setFocusPainted(false); b.setOpaque(true); b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        b.setPreferredSize(new Dimension(b.getPreferredSize().width,36)); return b;
    }

    private JButton buildSecondaryBtn(String text) {
        JButton b=new JButton(text); b.setFont(F_REGULAR); b.setForeground(C_TEXT); b.setBackground(new Color(239,239,239));
        b.setBorderPainted(false); b.setFocusPainted(false); b.setOpaque(true); b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        b.setPreferredSize(new Dimension(b.getPreferredSize().width,36)); return b;
    }

    private JButton buildDangerBtn(String text) { JButton b=buildSecondaryBtn(text); b.setForeground(C_ERROR); return b; }
    private JLabel errorLabel() { JLabel l=new JLabel(); l.setFont(F_SMALL); l.setForeground(C_ERROR); return l; }
    private JLabel linkLabel(String text) { JLabel l=new JLabel(text); l.setFont(F_SMALL); l.setForeground(C_BLUE); l.setCursor(new Cursor(Cursor.HAND_CURSOR)); return l; }
    private JLabel emptyLabel(String text) { JLabel l=new JLabel(text,SwingConstants.CENTER); l.setFont(F_REGULAR); l.setForeground(C_TEXT_LIGHT); return l; }
    private JLabel fieldLabel(String text)  { JLabel l=new JLabel(text); l.setFont(F_SMALL); l.setForeground(C_TEXT_LIGHT); return l; }

    private JPanel roundPanel(Color bg) {
        return new JPanel(){@Override protected void paintComponent(Graphics g){Graphics2D g2=(Graphics2D)g.create();g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);g2.setColor(bg);g2.fillRect(0,0,getWidth(),getHeight());g2.dispose();}};
    }

    private JScrollPane styledScroll(Component c) { return styledScroll(c,true); }
    private JScrollPane styledScroll(Component c, boolean scrollToTop) {
        JScrollPane sp=new JScrollPane(c); sp.setBorder(null); sp.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        sp.getVerticalScrollBar().setUnitIncrement(60); sp.getVerticalScrollBar().setBlockIncrement(220);
        sp.getVerticalScrollBar().setUI(new BasicScrollBarUI(){
            protected void configureScrollBarColors(){thumbColor=new Color(219,219,219);trackColor=C_WHITE;}
            protected JButton createDecreaseButton(int o){return zeroBtn();} protected JButton createIncreaseButton(int o){return zeroBtn();}
            private JButton zeroBtn(){JButton b=new JButton();b.setPreferredSize(new Dimension(0,0));return b;}
        });
        if(scrollToTop)SwingUtilities.invokeLater(()->sp.getVerticalScrollBar().setValue(0));
        return sp;
    }

    private JPanel buildDialogHeader(String title, JDialog d) {
        JPanel h=new JPanel(new BorderLayout()); h.setBackground(C_WHITE);
        h.setBorder(BorderFactory.createCompoundBorder(new MatteBorder(0,0,1,0,C_BORDER),BorderFactory.createEmptyBorder(14,16,14,16)));
        JLabel lbl=new JLabel(title); lbl.setFont(F_BOLD); lbl.setForeground(C_TEXT);
        JButton x=new JButton();
        if(iconsNormal.containsKey("Close"))x.setIcon(iconsNormal.get("Close")); else{x.setText("x");x.setFont(F_SMALL);x.setForeground(C_TEXT_LIGHT);}
        x.setBorderPainted(false); x.setContentAreaFilled(false); x.setFocusPainted(false); x.setCursor(new Cursor(Cursor.HAND_CURSOR)); x.addActionListener(e->d.dispose());
        h.add(lbl,BorderLayout.WEST); h.add(x,BorderLayout.EAST); return h;
    }

    private void redBorder(JComponent c){c.setBorder(new LineBorder(C_ERROR,1,true));}
    private void resetFieldBorder(JTextField t){t.setBorder(BorderFactory.createCompoundBorder(new LineBorder(C_BORDER,1,true),BorderFactory.createEmptyBorder(9,12,9,12)));}
    private void resetPanelBorder(JPanel p){p.setBorder(new LineBorder(C_BORDER,1,true));}
    private void resetSpinnerBorder(JSpinner s){s.setBorder(new LineBorder(C_BORDER,1,true));}

    private MouseAdapter click(Runnable r){return new MouseAdapter(){public void mouseClicked(MouseEvent e){r.run();}};}
    private DocumentListener simpleDocListener(Runnable r){return new DocumentListener(){public void insertUpdate(DocumentEvent e){r.run();}public void removeUpdate(DocumentEvent e){r.run();}public void changedUpdate(DocumentEvent e){r.run();}};}

    private String toHtml(String texto){
        if(texto==null)return "";StringBuilder sb=new StringBuilder();
        for(String w:texto.split(" ")){if(w.startsWith("#")||w.startsWith("@"))sb.append("<a href='").append(w).append("' style='color:#3d9be9;text-decoration:none;'>").append(w).append("</a> ");else sb.append(w).append(" ");}
        return sb.toString();
    }

    private String copiarFotoPorDefecto(String username) {
        try {
            java.io.InputStream is=getClass().getResourceAsStream("/images/default_profile.png");
            if(is==null)return "";
            String rutaCarpeta="INSTA_RAIZ/"+username+"/imagenes";
            new File(rutaCarpeta).mkdirs();
            File dest=new File(rutaCarpeta+"/profile_default.png");
            java.nio.file.Files.copy(is,dest.toPath(),java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            is.close();
            return dest.getAbsolutePath();
        } catch(Exception e){System.out.println("No se pudo copiar default_profile.png: "+e.getMessage());return "";}
    }
}