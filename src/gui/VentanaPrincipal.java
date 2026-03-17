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
import red.ClienteSocket;

public class VentanaPrincipal extends JFrame {

    private Sistema sistema;
    private Timer chatTimer;
    private Timer feedTimer;
    private int lastMessageCount = 0;
    private int lastFeedCount = 0;
    private Timer globalTimer;
    private String lastMessageSignature = "";
    private ClienteSocket clienteSocket = new ClienteSocket();

    private static final Color C_WHITE = Color.WHITE;
    private static final Color C_BG = new Color(250, 250, 250);
    private static final Color C_BLUE = new Color(64, 155, 230);
    private static final Color C_BLUE_DIM = new Color(42, 107, 161);
    private static final Color C_FIELD = new Color(250, 250, 250);
    private static final Color C_BORDER = new Color(219, 219, 219);
    private static final Color C_PLACEHOLDER = new Color(168, 168, 168);
    private static final Color C_TEXT = new Color(38, 38, 38);
    private static final Color C_TEXT_LIGHT = new Color(142, 142, 142);
    private static final Color C_ERROR = new Color(237, 73, 86);
    private static final Color C_HOVER = new Color(245, 245, 245);

    private static final Font F_REGULAR = new Font("Arial", Font.PLAIN, 13);
    private static final Font F_BOLD = new Font("Arial", Font.BOLD, 13);
    private static final Font F_SMALL = new Font("Arial", Font.PLAIN, 11);
    private static final Font F_H1 = new Font("Arial", Font.BOLD, 22);
    private static final Font F_H2 = new Font("Arial", Font.BOLD, 16);

    private ImageIcon iconEyeClosed, iconEyeOpen, iconCheck;
    private Map<String, ImageIcon> iconsNormal = new HashMap<>();
    private Map<String, ImageIcon> iconsBold = new HashMap<>();
    private String vistaActual = "Home";

    public VentanaPrincipal(Sistema sistema) {
        this.sistema = sistema;
        cargarIconos();
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                if (sistema.getUsuarioActual() != null) {
                    sistema.logout();
                }
            }
        });
        setSize(1366, 768);
        setTitle("Instagram");
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setResizable(false);
        inicializarComponentesLogin();
    }

    private void iniciarTimerGlobal() {
        if (globalTimer != null) {
            globalTimer.stop();
        }
        globalTimer = new Timer(2500, e -> {
            if (sistema.getUsuarioActual() == null) {
                return;
            }
            SwingUtilities.invokeLater(() -> {
                // Actualizar punto rojo de mensajes y notificaciones en sidebar
                boolean hayMsgs = sistema.getTotalMensajesNoLeidos() > 0;
                boolean hayNotifs = tieneNotificacionesPendientes();
                // Forzar repaint del sidebar si cambió algo
                getContentPane().repaint();
            });
        });
        globalTimer.start();
    }

    // ════════════════════════════════════════════════════════════
    //  ICONOS
    // ════════════════════════════════════════════════════════════
    private void cargarIconos() {
        try {
            iconEyeClosed = scaled("/images/ojocerrado.png", 18, 18);
        } catch (Exception ignored) {
        }
        try {
            iconEyeOpen = scaled("/images/ojo.png", 18, 18);
        } catch (Exception ignored) {
        }
        try {
            iconCheck = scaled("/images/check.png", 16, 16);
        } catch (Exception ignored) {
        }
        int s = 26;
        loadIcon("Home", "homeicon.png", "bhomeicon.png", s);
        loadIcon("Search", "searchicon.png", "bsearchicon.png", s);
        loadIcon("Messages", "messageicon.png", "bmessageicon.png", s);
        loadIcon("Create", "createicon.png", "bcreateicon.png", s);
        loadIcon("Notifications", "hearticon.png", "bhearticon.png", s);
        loadIcon("Profile", "profileicon.png", "bprofileicon.png", s);
        loadIcon("Like", "hearticon.png", "bhearticon.png", 22);
        loadIcon("Comment", "commenticon.png", "commenticon.png", 22);
        loadIcon("Share", "messageicon.png", "bmessageicon.png", 22);
        loadIcon("More", "more.png", "more.png", 20);
        loadIcon("Sticker", "stickericon.png", "stickericon.png", 22);
        loadIcon("Send", "messageicon.png", "messageicon.png", 22);
        loadIcon("Trash", "trashicon.png", "trashicon.png", 18);
        loadIcon("Close", "closeicon.png", "closeicon.png", 14);
        loadIcon("Camera", "cameraicon.png", "cameraicon.png", 36);
        loadIcon("Lock", "lockicon.png", "lockicon.png", 14);
        loadIcon("Verified", "verified.png", "verified.png", 16);
    }

    private void loadIcon(String key, String normal, String bold, int size) {
        try {
            URL u1 = getClass().getResource("/images/" + normal);
            if (u1 != null) {
                iconsNormal.put(key, new ImageIcon(new ImageIcon(u1).getImage().getScaledInstance(size, size, Image.SCALE_SMOOTH)));
            }
            URL u2 = getClass().getResource("/images/" + bold);
            if (u2 != null) {
                iconsBold.put(key, new ImageIcon(new ImageIcon(u2).getImage().getScaledInstance(size, size, Image.SCALE_SMOOTH)));
            }
        } catch (Exception ignored) {
        }
    }

    private ImageIcon scaled(String path, int w, int h) {
        URL u = getClass().getResource(path);
        if (u == null) {
            return null;
        }
        return new ImageIcon(new ImageIcon(u).getImage().getScaledInstance(w, h, Image.SCALE_SMOOTH));
    }

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
        limpiarOverlaysActivos();
        if (chatTimer != null) {
            chatTimer.stop();
        }
        if (feedTimer != null) {
            feedTimer.stop();
        }
        getContentPane().removeAll();
        getContentPane().setLayout(null);
        getContentPane().setBackground(C_BG);

        int cardW = 350, cardH = 430;
        JPanel card = roundPanel(C_WHITE);
        card.setLayout(null);
        card.setBounds((1366 - cardW) / 2, (768 - cardH) / 2, cardW, cardH);
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
        JLabel lblErrUser = errorLabel();
        lblErrUser.setBounds(37, 154, 276, 16);
        card.add(lblErrUser);
        JPanel panelPass = buildPassPanel();
        panelPass.setBounds(37, 172, 276, 38);
        card.add(panelPass);
        JLabel lblErrPass = errorLabel();
        lblErrPass.setBounds(37, 211, 276, 16);
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
            lblErrUser.setText("");
            lblErrPass.setText("");
            resetFieldBorder(txtUser);
            resetPanelBorder(panelPass);
        });
        txtUser.getDocument().addDocumentListener(dl);
        txtPass.getDocument().addDocumentListener(dl);

        btnLogin.addActionListener(e -> {
            String user = txtUser.getText().trim();
            String pass = new String(txtPass.getPassword());
            if (sistema.sesionActiva(user)) {
                InstaDialog.showMessage(this, "Este usuario ya tiene una sesión activa.");
                return;
            }
            Usuario u = sistema.buscarUsuario(user);
            if (u == null) {
                lblErrUser.setText("El usuario no existe.");
                redBorder(txtUser);
                return;
            }
            if (u.getEstadoCuenta() == EstadoCuenta.DESACTIVADO) {
                boolean yes = InstaDialog.showConfirm(this, "Cuenta desactivada.\n¿Deseas reactivarla?", "Reactivar", false);
                if (yes) {
                    sistema.reactivarCuenta(user);
                } else {
                    return;
                }
            }
            if (!sistema.login(user, pass)) {
                lblErrPass.setText("Contraseña incorrecta.");
                redBorder(panelPass);
                return;
            }
            sistema.sincronizarDefaultsAlLogin();
            SwingUtilities.invokeLater(() -> {
                limpiarOverlaysActivos();
                cargarVistaFeed();
            });
        });
        lblReg.addMouseListener(click(this::cargarVistaRegistro));
        revalidate();
        repaint();
    }

    // ════════════════════════════════════════════════════════════
    //  VISTA 2 — REGISTRO
    // ════════════════════════════════════════════════════════════
    private void cargarVistaRegistro() {
        getContentPane().removeAll();
        getContentPane().setLayout(null);
        getContentPane().setBackground(C_BG);

        int cardW = 400, cardH = 640;
        JPanel card = roundPanel(C_WHITE);
        card.setLayout(null);
        card.setBounds((1366 - cardW) / 2, Math.max(10, (768 - cardH) / 2), cardW, cardH);
        card.setBorder(new LineBorder(C_BORDER, 1));

        JLabel lblLogo = new JLabel();
        try {
            ImageIcon ic = new ImageIcon(getClass().getResource("/images/instagramlogoblack.png"));
            lblLogo.setIcon(new ImageIcon(ic.getImage().getScaledInstance(120, 68, Image.SCALE_SMOOTH)));
        } catch (Exception ignored) {
        }
        lblLogo.setHorizontalAlignment(SwingConstants.CENTER);
        lblLogo.setBounds(0, 14, cardW, 70);
        card.add(lblLogo);

        JLabel sub = new JLabel("Crea tu cuenta y comparte!.", SwingConstants.CENTER);
        sub.setFont(F_SMALL);
        sub.setForeground(C_TEXT_LIGHT);
        sub.setBounds(0, 82, cardW, 18);
        card.add(sub);

        int avatarSize = 84;
        JLabel avatarCircle = new JLabel() {
            @Override
            protected void paintComponent(Graphics g) {
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
        avatarCircle.setBounds((cardW - avatarSize) / 2, 108, avatarSize, avatarSize);
        avatarCircle.setCursor(new Cursor(Cursor.HAND_CURSOR));
        card.add(avatarCircle);

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
                Image img = new ImageIcon(archivoSel[0].getAbsolutePath()).getImage().getScaledInstance(avatarSize, avatarSize, Image.SCALE_SMOOTH);
                avatarCircle.setIcon(new ImageIcon(img));
                avatarCircle.setText("");
                lblFotoTip.setText(" Foto seleccionada!");
                lblFotoTip.setForeground(new Color(0, 160, 80));
            }
        }));

        int fx = 44, fw = cardW - 88, y = 222, gap = 52;
        JTextField txtNombre = buildField("Nombre completo");
        txtNombre.setBounds(fx, y, fw, 38);
        card.add(txtNombre);
        JLabel errNombre = errorLabel();
        errNombre.setBounds(fx, y + 38, fw, 14);
        card.add(errNombre);
        y += gap;
        JPanel panelUser = buildFieldWithIcon("Nombre de usuario");
        panelUser.setBounds(fx, y, fw, 38);
        card.add(panelUser);
        JLabel errUser = errorLabel();
        errUser.setBounds(fx, y + 38, fw, 14);
        card.add(errUser);
        y += gap;
        JPanel panelPass = buildPassPanel();
        panelPass.setBounds(fx, y, fw, 38);
        card.add(panelPass);
        JLabel errPass = errorLabel();
        errPass.setBounds(fx, y + 38, fw, 14);
        card.add(errPass);
        y += gap;

        JLabel lEdad = fieldLabel("Edad");
        lEdad.setBounds(fx, y, 60, 14);
        card.add(lEdad);
        JLabel lGen = fieldLabel("Género");
        lGen.setBounds(fx + 112, y, 80, 14);
        card.add(lGen);
        JLabel lTipo = fieldLabel("Cuenta");
        lTipo.setBounds(fx + 214, y, 100, 14);
        card.add(lTipo);
        y += 16;

        JSpinner spnEdad = buildSpinner(18);
        spnEdad.setBounds(fx, y, 94, 34);
        card.add(spnEdad);
        JLabel errEdad = errorLabel();
        errEdad.setBounds(fx, y + 36, 110, 14);
        card.add(errEdad);
        JComboBox<String> cmbGenero = buildCombo("M", "F");
        cmbGenero.setBounds(fx + 112, y, 86, 34);
        card.add(cmbGenero);
        JComboBox<String> cmbTipo = buildCombo("Pública", "Privada");
        cmbTipo.setBounds(fx + 214, y, 94, 34);
        card.add(cmbTipo);
        y += 52;

        JButton btnReg = buildPrimaryBtn("Crear cuenta");
        btnReg.setEnabled(false);
        btnReg.setBackground(C_BLUE_DIM);
        btnReg.setBounds(fx, y, fw, 38);
        card.add(btnReg);
        y += 48;
        JLabel lblVolver = linkLabel("¿Ya tienes cuenta? Inicia sesión");
        int lw = lblVolver.getPreferredSize().width;
        lblVolver.setBounds(fx + (fw - lw) / 2, y, lw, 18);
        card.add(lblVolver);
        getContentPane().add(card);

        JTextField txtUser = (JTextField) panelUser.getComponent(0);
        JLabel iconCheck2 = (JLabel) panelUser.getComponent(1);
        JPasswordField txtPass = (JPasswordField) panelPass.getComponent(0);

        Runnable validar = () -> {
            boolean ok = true;
            errNombre.setText("");
            errUser.setText("");
            errPass.setText("");
            errEdad.setText("");
            resetFieldBorder(txtNombre);
            resetPanelBorder(panelUser);
            resetPanelBorder(panelPass);
            resetSpinnerBorder(spnEdad);
            iconCheck2.setIcon(null);
            iconCheck2.setText("");
            if (txtNombre.getText().equals("Nombre completo") || txtNombre.getText().isEmpty()) {
                ok = false;
            }
            String usr = txtUser.getText();
            if (usr.isEmpty() || usr.equals("Nombre de usuario")) {
                ok = false;
            } else if (usr.length() < 3) {
                errUser.setText("Mínimo 3 caracteres.");
                redBorder(panelUser);
                ok = false;
            } else if (sistema.existeUsername(usr)) {
                errUser.setText("El nombre ya existe.");
                redBorder(panelUser);
                ok = false;
            } else {
                iconCheck2.setIcon(iconCheck);
                if (iconCheck == null) {
                    iconCheck2.setText("✓");
                }
            }
            String ps = new String(txtPass.getPassword());
            if (ps.isEmpty() || ps.equals("Password")) {
                ok = false;
            } else if (ps.length() < 6) {
                errPass.setText("Mínimo 6 caracteres.");
                redBorder(panelPass);
                ok = false;
            }
            if ((int) spnEdad.getValue() < 18) {
                errEdad.setText("Debes tener 18+.");
                redBorder(spnEdad);
                ok = false;
            }
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
            String nombre = txtNombre.getText().trim(), user = txtUser.getText().trim();
            String pass = new String(txtPass.getPassword());
            int edad = (int) spnEdad.getValue();
            char genero = cmbGenero.getSelectedIndex() == 0 ? 'M' : 'F';
            TipoCuenta tipo = cmbTipo.getSelectedIndex() == 0 ? TipoCuenta.PUBLICA : TipoCuenta.PRIVADA;
            String rutaFoto = archivoSel[0] != null
                    ? sistema.procesarImagenPerfil(archivoSel[0], user, "profile_" + System.currentTimeMillis())
                    : copiarFotoPorDefecto(user);
            if (rutaFoto == null) {
                rutaFoto = copiarFotoPorDefecto(user);
            }
            if (sistema.registrarUsuario(user, pass, nombre, genero, edad, rutaFoto, tipo)) {
                sistema.login(user, pass);
                SwingUtilities.invokeLater(() -> {
                    limpiarOverlaysActivos();
                    cargarVistaFeed();
                });
            } else {
                errUser.setText("Error al registrar.");
            }
        });
        lblVolver.addMouseListener(click(this::inicializarComponentesLogin));
        revalidate();
        repaint();
    }

    // ════════════════════════════════════════════════════════════
    //  VISTA 3 — FEED
    // ════════════════════════════════════════════════════════════
    private void cargarVistaFeed() {
        limpiarOverlaysActivos();
        vistaActual = "Home";
        if (feedTimer != null) {
            feedTimer.stop();
        }
        if (chatTimer != null) {
            chatTimer.stop();
        }
        getContentPane().removeAll();
        setLayout(new BorderLayout());
        getContentPane().setBackground(C_BG);
        add(buildSidebar(), BorderLayout.WEST);

        JPanel panelContenido = new JPanel();
        panelContenido.setBackground(C_BG);
        panelContenido.setLayout(new BoxLayout(panelContenido, BoxLayout.Y_AXIS));
        JPanel feedWrapper = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        feedWrapper.setBackground(C_BG);
        feedWrapper.add(panelContenido);
        JScrollPane scroll = styledScroll(feedWrapper);

        ArrayList<Publicacion> posts = sistema.getTimeline();
        lastFeedCount = posts.size();

        if (posts.isEmpty()) {
            JLabel lbl = new JLabel("No hay publicaciones. ¡Sigue a alguien o crea tu primer post!");
            lbl.setForeground(C_TEXT_LIGHT);
            lbl.setFont(F_REGULAR);
            lbl.setAlignmentX(Component.CENTER_ALIGNMENT);
            panelContenido.add(Box.createVerticalStrut(40));
            panelContenido.add(lbl);
        } else {
            for (Publicacion p : posts) {
                panelContenido.add(buildPost(p));
                panelContenido.add(Box.createVerticalStrut(4));
            }
        }

        add(scroll, BorderLayout.CENTER);
        SwingUtilities.invokeLater(() -> {
            scroll.getVerticalScrollBar().setValue(0);
            scroll.getViewport().setViewPosition(new Point(0, 0));
        });
        feedTimer = new Timer(3000, ev -> {
            int n = sistema.getTimeline().size();
            if (n != lastFeedCount) {
                lastFeedCount = n;
                cargarVistaFeed();
            }
        });
        feedTimer.start();
        iniciarTimerGlobal();
        conectarSocket();
        revalidate();
        repaint();
    }

    // ════════════════════════════════════════════════════════════
    //  POST CARD
    // ════════════════════════════════════════════════════════════
    private JPanel buildPost(Publicacion p) {
        final int POST_W = 600, IMG_H = 600;
        JPanel post = new JPanel(new BorderLayout());
        post.setBackground(C_WHITE);
        post.setMaximumSize(new Dimension(POST_W, Integer.MAX_VALUE));
        post.setPreferredSize(new Dimension(POST_W, IMG_H + 160));
        post.setBorder(new LineBorder(C_BORDER, 1));

        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(C_WHITE);
        header.setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));
        JPanel userInfo = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        userInfo.setBackground(C_WHITE);
        userInfo.add(new JLabel(cargarFotoCircular(p.getAutor(), 32)));
        JLabel lblUser = new JLabel(p.getAutor());
        lblUser.setFont(F_BOLD);
        lblUser.setForeground(C_TEXT);
        lblUser.setCursor(new Cursor(Cursor.HAND_CURSOR));
        lblUser.addMouseListener(click(() -> cargarVistaPerfil(p.getAutor())));
        userInfo.add(lblUser);
        userInfo.add(badgeVerificado(p.getAutor()));
        header.add(userInfo, BorderLayout.WEST);

        if (sistema.getUsuarioActual() != null && p.getAutor().equals(sistema.getUsuarioActual().getUsername())) {
            JButton btnOpts = new JButton();
            if (iconsNormal.containsKey("More")) {
                btnOpts.setIcon(iconsNormal.get("More"));
            } else {
                btnOpts.setText("...");
                btnOpts.setFont(new Font("Arial", Font.BOLD, 14));
            }
            btnOpts.setBorderPainted(false);
            btnOpts.setContentAreaFilled(false);
            btnOpts.setFocusPainted(false);
            btnOpts.setCursor(new Cursor(Cursor.HAND_CURSOR));
            JPopupMenu menu = new JPopupMenu();
            menu.setBorder(new LineBorder(C_BORDER, 1));
            JMenuItem itemDel = new JMenuItem("Eliminar publicación");
            itemDel.setFont(F_BOLD);
            itemDel.setForeground(C_ERROR);
            itemDel.addActionListener(ev -> {
                boolean ok = InstaDialog.showConfirm(this, "¿Eliminar esta publicación?", "Eliminar", true);
                if (ok) {
                    sistema.eliminarPublicacion(p);
                    if ("Profile".equals(vistaActual)) {
                        cargarVistaPerfil(sistema.getUsuarioActual().getUsername());
                    } else {
                        cargarVistaFeed();
                    }
                }
            });
            menu.add(itemDel);
            btnOpts.addActionListener(ev -> menu.show(btnOpts, 0, btnOpts.getHeight()));
            header.add(btnOpts, BorderLayout.EAST);
        }

        // Imagen cropeada — fondo blanco garantizado
        JLabel lblImg = new JLabel() {
            @Override
            protected void paintComponent(Graphics g) {
                g.setColor(C_WHITE);
                g.fillRect(0, 0, getWidth(), getHeight());
                if (getIcon() != null) {
                    getIcon().paintIcon(this, g, 0, 0);
                }
            }
        };
        lblImg.setPreferredSize(new Dimension(POST_W, IMG_H));
        lblImg.setMinimumSize(new Dimension(POST_W, IMG_H));
        lblImg.setMaximumSize(new Dimension(POST_W, IMG_H));
        try {
            if (p.getRutaImagen() != null && !p.getRutaImagen().isEmpty() && new File(p.getRutaImagen()).exists()) {
                ImageIcon cr = crearImagenCropeada(p.getRutaImagen(), POST_W, IMG_H);
                if (cr != null) {
                    lblImg.setIcon(cr);
                }
            }
        } catch (Exception ignored) {
        }

        JPanel acciones = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 8));
        acciones.setBackground(C_WHITE);
        acciones.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 0));
        boolean liked = sistema.yaDioLike(p.getAutor(), p.getFecha().toString());
        JButton btnLike = iconBtn(liked);
        btnLike.addActionListener(ev -> {
            boolean estado = sistema.toggleLike(p.getAutor(), p.getFecha().toString(), p.getRutaImagen());
            actualizarLike(btnLike, estado);
            if (estado) {
                clienteSocket.enviar(new red.EventoSocket(
                        red.EventoSocket.Tipo.NUEVA_NOTIFICACION,
                        sistema.getUsuarioActual().getUsername(), p.getAutor()));
            }
        });
        JButton btnComment = iconBtn("Comment", false);
        btnComment.addActionListener(ev -> abrirComentarios(p));

        JButton btnShare = iconBtn("Share", false);
        btnShare.addActionListener(ev -> {
            String dest = InstaDialog.showInput(this, "Enviar por mensaje a...", "Nombre de usuario");
            if (dest == null || dest.isEmpty()) {
                return;
            }
            Usuario destU = sistema.buscarUsuario(dest);
            if (destU == null) {
                InstaDialog.showMessage(this, "El usuario @" + dest + " no existe.", true);
                return;
            }
            if (destU.getEstadoCuenta() == EstadoCuenta.DESACTIVADO) {
                InstaDialog.showMessage(this, "Esa cuenta no está disponible.", true);
                return;
            }
            if (!sistema.puedeEnviarMensaje(dest)) {
                InstaDialog.showMessage(this, "No puedes enviar mensajes a @" + dest
                        + ".\nSu cuenta es privada — deben seguirse mutuamente.", true);
                return;
            }
            if (!sistema.puedeCompartirPost(dest, p.getAutor())) {
                InstaDialog.showMessage(this, "No puedes compartir este post con @" + dest
                        + ".\nEl autor tiene cuenta privada.", true);
                return;
            }
            sistema.compartirPost(dest, p.getAutor(), p.getRutaImagen(), p.getContenido());
            boolean irChat = InstaDialog.showConfirm(this,
                    "Post enviado a @" + dest + " \n¿Abrir el chat?", "Abrir", false);
            if (irChat) {
                SwingUtilities.invokeLater(() -> cargarVistaInboxCon(dest));
            }
        });

        acciones.add(btnLike);
        acciones.add(btnComment);
        acciones.add(btnShare);

        // Footer
        JPanel footer = new JPanel();
        footer.setLayout(new BoxLayout(footer, BoxLayout.Y_AXIS));
        footer.setBackground(C_WHITE);
        footer.setBorder(BorderFactory.createEmptyBorder(0, 12, 10, 12));
        footer.add(acciones);

        JEditorPane edCaption = new JEditorPane("text/html", "");
        edCaption.setEditable(false);
        edCaption.setOpaque(false);
        edCaption.setText("<html><font face='Arial' size='3'><b>" + p.getAutor() + "</b> " + toHtml(p.getContenido()) + "</font></html>");
        edCaption.addHyperlinkListener(e -> {
            if (e.getEventType() == javax.swing.event.HyperlinkEvent.EventType.ACTIVATED) {
                String h = e.getDescription();
                if (h.startsWith("#")) {
                    cargarVistaBusquedaHashtag(h);
                } else if (h.startsWith("@")) {
                    cargarVistaPerfil(h.substring(1));
                }
            }
        });
        footer.add(edCaption);

        ArrayList<String> comentarios = sistema.getComentarios(p.getAutor(), p.getFecha().toString());
        if (!comentarios.isEmpty()) {
            int mostrar = Math.min(2, comentarios.size());
            for (int i = 0; i < mostrar; i++) {
                String c = comentarios.get(i);
                String[] pts = c.split(":", 2);
                String comUser = pts[0].trim();
                String comText = pts.length > 1 ? pts[1].trim() : c;
                JPanel comRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 2));
                comRow.setBackground(C_WHITE);
                JLabel lu = new JLabel(comUser);
                lu.setFont(F_BOLD);
                lu.setForeground(C_TEXT);
                lu.setCursor(new Cursor(Cursor.HAND_CURSOR));
                lu.addMouseListener(click(() -> cargarVistaPerfil(comUser)));
                JLabel lt = new JLabel(comText.length() > 60 ? comText.substring(0, 57) + "…" : comText);
                lt.setFont(F_REGULAR);
                lt.setForeground(C_TEXT);
                comRow.add(lu);
                comRow.add(lt);
                footer.add(comRow);
            }
            if (comentarios.size() > 2) {
                JLabel verMas = new JLabel("Ver los " + comentarios.size() + " comentarios");
                verMas.setFont(F_SMALL);
                verMas.setForeground(C_TEXT_LIGHT);
                verMas.setCursor(new Cursor(Cursor.HAND_CURSOR));
                verMas.setBorder(BorderFactory.createEmptyBorder(2, 0, 0, 0));
                verMas.addMouseListener(click(() -> abrirComentarios(p)));
                footer.add(verMas);
            }
        }

        JPanel fechaRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        fechaRow.setBackground(C_WHITE);
        JLabel lblFecha = new JLabel(p.getFecha() + "  " + p.getHoraFormateada());
        lblFecha.setFont(F_SMALL);
        lblFecha.setForeground(C_TEXT_LIGHT);
        lblFecha.setBorder(BorderFactory.createEmptyBorder(4, 0, 0, 0));
        fechaRow.add(lblFecha);
        footer.add(fechaRow);

        post.add(header, BorderLayout.NORTH);
        post.add(lblImg, BorderLayout.CENTER);
        post.add(footer, BorderLayout.SOUTH);
        return post;
    }

    /**
     * Crop centrado con fondo blanco garantizado — elimina el gris de canal
     * alfa
     */
    private ImageIcon crearImagenCropeada(String ruta, int targetW, int targetH) {
        try {
            BufferedImage src = javax.imageio.ImageIO.read(new File(ruta));
            if (src == null) {
                return null;
            }
            // Aplanar sobre blanco (elimina transparencia/gris de PNG con alpha)
            BufferedImage flat = new BufferedImage(src.getWidth(), src.getHeight(), BufferedImage.TYPE_INT_RGB);
            Graphics2D gf = flat.createGraphics();
            gf.setColor(Color.WHITE);
            gf.fillRect(0, 0, flat.getWidth(), flat.getHeight());
            gf.drawImage(src, 0, 0, null);
            gf.dispose();

            int srcW = flat.getWidth(), srcH = flat.getHeight();
            double scale = Math.max((double) targetW / srcW, (double) targetH / srcH);
            int sw = Math.max(1, (int) (srcW * scale)), sh = Math.max(1, (int) (srcH * scale));

            BufferedImage scaled = new BufferedImage(sw, sh, BufferedImage.TYPE_INT_RGB);
            Graphics2D g = scaled.createGraphics();
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g.setColor(Color.WHITE);
            g.fillRect(0, 0, sw, sh);
            g.drawImage(flat, 0, 0, sw, sh, null);
            g.dispose();

            int cropX = Math.max(0, (sw - targetW) / 2), cropY = Math.max(0, (sh - targetH) / 2);
            int cw = Math.min(targetW, sw - cropX), ch = Math.min(targetH, sh - cropY);
            if (cw <= 0 || ch <= 0) {
                return null;
            }
            BufferedImage cropped = scaled.getSubimage(cropX, cropY, cw, ch);

            if (cw < targetW || ch < targetH) {
                BufferedImage canvas = new BufferedImage(targetW, targetH, BufferedImage.TYPE_INT_RGB);
                Graphics2D gc = canvas.createGraphics();
                gc.setColor(Color.WHITE);
                gc.fillRect(0, 0, targetW, targetH);
                gc.drawImage(cropped, (targetW - cw) / 2, (targetH - ch) / 2, null);
                gc.dispose();
                return new ImageIcon(canvas);
            }
            return new ImageIcon(cropped);
        } catch (Exception e) {
            return null;
        }
    }

    private JButton iconBtn(boolean liked) {
        JButton b = new JButton();
        b.setBorderPainted(false);
        b.setBackground(C_WHITE);
        b.setFocusPainted(false);
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        actualizarLike(b, liked);
        return b;
    }

    private JButton iconBtn(String key, boolean bold) {
        JButton b = new JButton();
        b.setBorderPainted(false);
        b.setBackground(C_WHITE);
        b.setFocusPainted(false);
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        Map<String, ImageIcon> map = bold ? iconsBold : iconsNormal;
        if (map.containsKey(key)) {
            b.setIcon(map.get(key));
        } else {
            b.setText(key.equals("Comment") ? "[\u2026]" : ">");
        }
        return b;
    }

    private void actualizarLike(JButton b, boolean on) {
        if (on) {
            if (iconsBold.containsKey("Like")) {
                b.setIcon(iconsBold.get("Like"));
            } else {
                b.setText("<3");
            }
        } else {
            if (iconsNormal.containsKey("Like")) {
                b.setIcon(iconsNormal.get("Like"));
            } else {
                b.setText("o");
            }
        }
    }

    // ════════════════════════════════════════════════════════════
    //  COMENTARIOS — JWindow sin decoración del SO
    // ════════════════════════════════════════════════════════════
    private void abrirComentarios(Publicacion p) {
        JWindow win = new JWindow(this);
        win.setSize(440, 520);
        Point loc = getLocationOnScreen();
        win.setLocation(loc.x + (getWidth() - 440) / 2, loc.y + (getHeight() - 520) / 2);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(C_WHITE);
        root.setBorder(new LineBorder(C_BORDER, 1));
        win.setContentPane(root);

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(C_WHITE);
        header.setBorder(BorderFactory.createCompoundBorder(new MatteBorder(0, 0, 1, 0, C_BORDER), BorderFactory.createEmptyBorder(12, 16, 12, 16)));
        JLabel lblTit = new JLabel("Comentarios");
        lblTit.setFont(F_BOLD);
        lblTit.setForeground(C_TEXT);
        header.add(lblTit, BorderLayout.WEST);
        JButton btnC = new JButton("✕");
        btnC.setFont(new Font("Arial", Font.PLAIN, 13));
        btnC.setBorderPainted(false);
        btnC.setContentAreaFilled(false);
        btnC.setFocusPainted(false);
        btnC.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnC.setForeground(C_TEXT_LIGHT);
        btnC.addActionListener(e -> win.dispose());
        header.add(btnC, BorderLayout.EAST);
        root.add(header, BorderLayout.NORTH);

        JPanel listPanel = new JPanel();
        listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));
        listPanel.setBackground(C_WHITE);
        JScrollPane scroll = styledScroll(listPanel, false);

        Runnable rellenar = () -> {
            listPanel.removeAll();
            for (String c : sistema.getComentarios(p.getAutor(), p.getFecha().toString())) {
                String[] pts = c.split(":", 2);
                String comUser = pts[0].trim();
                String comText = pts.length > 1 ? pts[1].trim() : c;
                JPanel row = new JPanel(new BorderLayout(8, 0));
                row.setBackground(C_WHITE);
                row.setBorder(BorderFactory.createEmptyBorder(8, 14, 8, 14));
                row.setMaximumSize(new Dimension(440, 60));
                row.add(new JLabel(cargarFotoCircular(comUser, 32)), BorderLayout.WEST);
                JPanel ts = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
                ts.setBackground(C_WHITE);
                JLabel nl = new JLabel(comUser);
                nl.setFont(F_BOLD);
                nl.setForeground(C_TEXT);
                nl.setCursor(new Cursor(Cursor.HAND_CURSOR));
                nl.addMouseListener(click(() -> {
                    win.dispose();
                    cargarVistaPerfil(comUser);
                }));
                ts.add(nl);
                JLabel tl = new JLabel(comText);
                tl.setFont(F_REGULAR);
                tl.setForeground(C_TEXT);
                ts.add(tl);
                row.add(ts, BorderLayout.CENTER);
                listPanel.add(row);
                JSeparator sep = new JSeparator();
                sep.setForeground(new Color(240, 240, 240));
                sep.setBackground(new Color(240, 240, 240));
                listPanel.add(sep);
            }
            listPanel.revalidate();
            listPanel.repaint();
        };
        rellenar.run();
        root.add(scroll, BorderLayout.CENTER);

        JPanel footer = new JPanel(new BorderLayout());
        footer.setBackground(C_WHITE);
        footer.setBorder(BorderFactory.createCompoundBorder(new MatteBorder(1, 0, 0, 0, C_BORDER), BorderFactory.createEmptyBorder(10, 12, 10, 12)));
        JTextField txt = new JTextField();
        txt.setFont(F_REGULAR);
        txt.setBackground(C_FIELD);
        txt.setBorder(BorderFactory.createCompoundBorder(new LineBorder(C_BORDER, 1, true), BorderFactory.createEmptyBorder(7, 12, 7, 12)));
        txt.putClientProperty("JTextField.placeholderText", "Agrega un comentario...");
        JButton btnEnv = new JButton("Publicar");
        btnEnv.setFont(F_BOLD);
        btnEnv.setForeground(C_BLUE);
        btnEnv.setBorderPainted(false);
        btnEnv.setContentAreaFilled(false);
        btnEnv.setFocusPainted(false);
        btnEnv.setCursor(new Cursor(Cursor.HAND_CURSOR));
        ActionListener send = e -> {
            if (!txt.getText().trim().isEmpty()) {
                sistema.agregarComentario(p.getAutor(), p.getFecha().toString(), txt.getText().trim());
                txt.setText("");
                rellenar.run();
                SwingUtilities.invokeLater(() -> scroll.getVerticalScrollBar().setValue(scroll.getVerticalScrollBar().getMaximum()));
            }
        };
        btnEnv.addActionListener(send);
        txt.addActionListener(send);
        footer.add(txt, BorderLayout.CENTER);
        footer.add(btnEnv, BorderLayout.EAST);
        root.add(footer, BorderLayout.SOUTH);
        win.setVisible(true);
    }

    // ════════════════════════════════════════════════════════════
    //  NUEVA PUBLICACIÓN
    // ════════════════════════════════════════════════════════════
    private void abrirDialogoNuevaPublicacion() {
        JPanel glass = mostrarOverlay();
        JDialog d = new JDialog(this, "Nueva publicación", true);
        d.setSize(520, 540);
        d.setLocationRelativeTo(this);
        d.setLayout(new BorderLayout());
        d.getContentPane().setBackground(C_WHITE);
        d.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                quitarOverlay(glass);
            }

            @Override
            public void windowClosing(WindowEvent e) {
                quitarOverlay(glass);
            }
        });
        d.add(buildDialogHeader("Nueva publicación", d), BorderLayout.NORTH);
        JPanel center = new JPanel();
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
        center.setBackground(C_WHITE);
        center.setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));
        d.setUndecorated(true);

        JLabel lblImgPreview = new JLabel() {
            @Override
            protected void paintComponent(Graphics g) {
                if (getIcon() == null) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(new Color(250, 250, 250));
                    g2.fillRect(0, 0, getWidth(), getHeight());
                    g2.setColor(C_BORDER);
                    g2.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 0, new float[]{6, 4}, 0));
                    g2.drawRoundRect(1, 1, getWidth() - 2, getHeight() - 2, 8, 8);
                    g2.setFont(F_REGULAR);
                    g2.setColor(C_TEXT_LIGHT);
                    FontMetrics fm = g2.getFontMetrics();
                    String msg = "Selecciona una imagen";
                    g2.drawString(msg, (getWidth() - fm.stringWidth(msg)) / 2, getHeight() / 2);
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

        JButton btnCancel = buildSecondaryBtn("Cancelar");
        JButton btnPost = buildPrimaryBtn("Compartir");
        JTextArea txtC = new JTextArea();
        txtC.setLineWrap(true);
        txtC.setWrapStyleWord(true);
        txtC.setFont(F_REGULAR);
        txtC.setBackground(C_FIELD);
        txtC.setBorder(BorderFactory.createCompoundBorder(new LineBorder(C_BORDER, 1, true), BorderFactory.createEmptyBorder(10, 12, 10, 12)));
        JScrollPane scrollTxt = new JScrollPane(txtC);
        scrollTxt.setBorder(null);
        scrollTxt.setPreferredSize(new Dimension(452, 72));
        scrollTxt.setMaximumSize(new Dimension(Integer.MAX_VALUE, 72));
        JLabel lblCnt = new JLabel("0 / 220", SwingConstants.RIGHT);
        lblCnt.setFont(F_SMALL);
        lblCnt.setForeground(C_TEXT_LIGHT);
        lblCnt.setAlignmentX(Component.RIGHT_ALIGNMENT);
        txtC.getDocument().addDocumentListener(simpleDocListener(() -> {
            int len = txtC.getText().length();
            lblCnt.setText(len + " / 220");
            if (len > 220) {
                lblCnt.setForeground(C_ERROR);
                btnPost.setEnabled(false);
                btnPost.setBackground(C_BLUE_DIM);
                btnPost.setForeground(new Color(200, 202, 204));
            } else {
                lblCnt.setForeground(len > 160 ? new Color(230, 140, 0) : C_TEXT_LIGHT);
                btnPost.setEnabled(true);
                btnPost.setBackground(C_BLUE);
                btnPost.setForeground(C_WHITE);
            }
        }));
        center.add(scrollTxt);
        center.add(lblCnt);
        d.add(center, BorderLayout.CENTER);

        JPanel ftr = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 12));
        ftr.setBackground(C_WHITE);
        ftr.setBorder(new MatteBorder(1, 0, 0, 0, C_BORDER));
        ftr.add(btnCancel);
        ftr.add(btnPost);
        d.add(ftr, BorderLayout.SOUTH);

        final String[] ruta = {""};
        btnSel.addActionListener(e -> {
            JFileChooser fc = new JFileChooser();
            fc.setFileFilter(new FileNameExtensionFilter("Imágenes", "jpg", "png", "jpeg", "gif"));
            fc.setAcceptAllFileFilterUsed(false);
            if (fc.showOpenDialog(d) == JFileChooser.APPROVE_OPTION) {
                String nm = fc.getSelectedFile().getName().toLowerCase();
                if (nm.matches(".*\\.(jpg|png|jpeg|gif)$")) {
                    ruta[0] = fc.getSelectedFile().getAbsolutePath();
                    ImageIcon orig = new ImageIcon(ruta[0]);
                    int mW = 452, mH = 220, iw = orig.getIconWidth(), ih = orig.getIconHeight();
                    double sc = Math.min((double) mW / iw, (double) mH / ih);
                    lblImgPreview.setIcon(new ImageIcon(orig.getImage().getScaledInstance((int) (iw * sc), (int) (ih * sc), Image.SCALE_SMOOTH)));
                    btnSel.setText("Cambiar imagen");
                } else {
                    InstaDialog.showMessage(d, "Solo se permiten JPG, PNG, JPEG o GIF.", true);
                }
            }
        });
        btnCancel.addActionListener(e -> d.dispose());
        btnPost.addActionListener(e -> {
            String texto = txtC.getText().trim();
            if (texto.isEmpty() || ruta[0].isEmpty()) {
                InstaDialog.showMessage(d, "Escribe algo y selecciona una imagen.");
                return;
            }
            StringBuilder ht = new StringBuilder(), mn = new StringBuilder();
            for (String w : texto.split(" ")) {
                if (w.startsWith("#")) {
                    ht.append(w).append(" ");
                } else if (w.startsWith("@")) {
                    mn.append(w).append(" ");
                }
            }
            String rf = sistema.procesarYGuardarImagen(new File(ruta[0]), sistema.getUsuarioActual().getUsername(), "post_" + System.currentTimeMillis());
            if (rf != null && sistema.crearPublicacion(texto, rf, ht.toString().trim(), mn.toString().trim())) {
                clienteSocket.enviar(new red.EventoSocket( // ← agregar
                        red.EventoSocket.Tipo.NUEVO_POST,
                        sistema.getUsuarioActual().getUsername(), null));
                d.dispose();
                cargarVistaFeed();
            } else {
                InstaDialog.showMessage(d, "Error al publicar.", true);
            }
        });
        d.setVisible(true);
    }

    // ════════════════════════════════════════════════════════════
    //  SIDEBAR — puntos rojos de notificación
    // ════════════════════════════════════════════════════════════
    private JPanel buildSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setPreferredSize(new Dimension(220, 768));
        sidebar.setBackground(C_WHITE);
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBorder(new MatteBorder(0, 0, 0, 1, C_BORDER));
        JLabel logo = new JLabel();
        try {
            ImageIcon li = new ImageIcon(getClass().getResource("/images/instagramlogoblack.png"));
            logo.setIcon(new ImageIcon(li.getImage().getScaledInstance(112, 63, Image.SCALE_SMOOTH)));
        } catch (Exception e) {
            logo.setText("Instagram");
            logo.setFont(new Font("Arial", Font.BOLD, 22));
        }
        logo.setAlignmentX(Component.LEFT_ALIGNMENT);
        logo.setBorder(BorderFactory.createEmptyBorder(24, 22, 28, 22));
        sidebar.add(logo);

        boolean hayNotifs = tieneNotificacionesPendientes();
        boolean hayMsgs = sistema.getTotalMensajesNoLeidos() > 0;

        JButton[] btns = {
            sidebarBtn("Inicio", "Home", false),
            sidebarBtn("Buscar", "Search", false),
            sidebarBtn("Mensajes", "Messages", hayMsgs),
            sidebarBtn("Crear", "Create", false),
            sidebarBtn("Notificaciones", "Notifications", hayNotifs),
            sidebarBtn("Mi perfil", "Profile", false)
        };
        for (JButton b : btns) {
            sidebar.add(b);
        }
        sidebar.add(Box.createVerticalGlue());
        JButton btnOut = sidebarBtn("Cerrar sesión", null, false);
        sidebar.add(btnOut);
        sidebar.add(Box.createVerticalStrut(20));

        btns[0].addActionListener(e -> cargarVistaFeed());
        btns[1].addActionListener(e -> cargarVistaBusqueda());
        btns[2].addActionListener(e -> cargarVistaInbox());
        btns[3].addActionListener(e -> abrirDialogoNuevaPublicacion());
        btns[4].addActionListener(e -> cargarVistaNotificaciones());
        btns[5].addActionListener(e -> cargarVistaPerfil(sistema.getUsuarioActual().getUsername()));
        btnOut.addActionListener(e -> {
            if (chatTimer != null) {
                chatTimer.stop();
            }
            if (feedTimer != null) {
                feedTimer.stop();
            }
            if (globalTimer != null) {
                globalTimer.stop();
            }
            clienteSocket.desconectar();
            sistema.logout();
            SwingUtilities.invokeLater(() -> {
                limpiarOverlaysActivos();
                inicializarComponentesLogin();
            });
        });
        return sidebar;
    }

    private boolean tieneNotificacionesPendientes() {
        return !sistema.getSolicitudes().isEmpty()
                || !sistema.getNotificacionesGenerales().isEmpty()
                || !sistema.getNotificacionesLikes().isEmpty();
    }

    private JButton sidebarBtn(String label, String key, boolean hasDot) {
        boolean active = key != null && key.equals(vistaActual);
        JButton b = new JButton() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (hasDot) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(C_ERROR);
                    g2.fillOval(38, 10, 8, 8);
                    g2.dispose();
                }
            }
        };
        b.setAlignmentX(Component.LEFT_ALIGNMENT);
        b.setMaximumSize(new Dimension(220, 58));
        b.setMinimumSize(new Dimension(220, 58));
        b.setPreferredSize(new Dimension(220, 58));
        b.setBorderPainted(false);
        b.setFocusPainted(false);
        b.setBackground(C_WHITE);
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        b.setHorizontalAlignment(SwingConstants.LEFT);
        b.setBorder(BorderFactory.createEmptyBorder(0, 18, 0, 0));
        if (key != null) {
            Map<String, ImageIcon> map = active ? iconsBold : iconsNormal;
            if (map.containsKey(key)) {
                b.setIcon(map.get(key));
                b.setIconTextGap(12);
            }
        }
        b.setText(label);
        b.setFont(active ? new Font("Arial", Font.BOLD, 15) : new Font("Arial", Font.PLAIN, 15));
        b.setForeground(key == null ? C_ERROR : C_TEXT);
        b.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                if (!active) {
                    b.setBackground(C_HOVER);
                }
            }

            public void mouseExited(MouseEvent e) {
                b.setBackground(C_WHITE);
            }
        });
        return b;
    }

    // ════════════════════════════════════════════════════════════
    //  VISTA 4 — PERFIL
    // ════════════════════════════════════════════════════════════
    private void cargarVistaPerfil(String username) {
        limpiarOverlaysActivos();
        sistema.invalidarCachePublica();
        Usuario uCheck = sistema.buscarUsuario(username);
        if (uCheck != null && uCheck.getEstadoCuenta() == EstadoCuenta.DESACTIVADO) {
            boolean esMio = sistema.getUsuarioActual() != null && username.equals(sistema.getUsuarioActual().getUsername());
            if (!esMio) {
                InstaDialog.showMessage(this, "Esta cuenta no está disponible.");
                return;
            }
        }

        vistaActual = (sistema.getUsuarioActual() != null && username.equals(sistema.getUsuarioActual().getUsername())) ? "Profile" : "";
        getContentPane().removeAll();
        setLayout(new BorderLayout());
        getContentPane().setBackground(C_BG);
        add(buildSidebar(), BorderLayout.WEST);

        JPanel content = new JPanel(new BorderLayout());
        content.setBackground(C_BG);
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
        lblUsr.setFont(new Font("Arial", Font.PLAIN, 22));
        lblUsr.setForeground(C_TEXT);
        row1.add(lblUsr);
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
                    boolean yes = InstaDialog.showConfirm(this, "¿Desactivar tu cuenta?", "Desactivar", true);
                    if (yes) {
                        sistema.cambiarEstadoCuenta(EstadoCuenta.DESACTIVADO);
                        sistema.logout();
                        if (chatTimer != null) {
                            chatTimer.stop();
                        }
                        inicializarComponentesLogin();
                    }
                });
            } else {
                btnToggle = buildPrimaryBtn("Activar cuenta");
                btnToggle.addActionListener(e -> {
                    sistema.cambiarEstadoCuenta(EstadoCuenta.ACTIVO);
                    cargarVistaPerfil(username);
                });
            }
            row1.add(btnToggle);
        } else {
            boolean loSigo = sistema.yaLoSigo(username), pendiente = sistema.solicitudPendiente(username);
            JButton btnSeg;
            if (loSigo) {
                btnSeg = buildSecondaryBtn("Siguiendo");
                btnSeg.addActionListener(e -> {
                    sistema.dejarDeSeguir(username);
                    cargarVistaPerfil(username);
                });
            } else if (pendiente) {
                btnSeg = buildSecondaryBtn("Solicitud enviada");
                btnSeg.setEnabled(false);
            } else {
                btnSeg = buildPrimaryBtn("Seguir");
                btnSeg.addActionListener(e -> {
                    sistema.seguirUsuario(username);
                    clienteSocket.enviar(new red.EventoSocket(
                            red.EventoSocket.Tipo.CAMBIO_SEGUIDOR,
                            sistema.getUsuarioActual().getUsername(), username));
                    cargarVistaPerfil(username);
                });
            }
            row1.add(btnSeg);
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
        JPanel pP = statPanel(sistema.getCantidadPosts(username), "publicaciones");
        JPanel pF = statPanel(sistema.getCantidadFollowers(username), "seguidores");
        JPanel pFg = statPanel(sistema.getCantidadFollowing(username), "seguidos");
        pF.setCursor(new Cursor(Cursor.HAND_CURSOR));
        pF.addMouseListener(click(() -> mostrarListaUsuarios(sistema.getListaFollowers(username), "Seguidores", "Followers", username)));
        pFg.setCursor(new Cursor(Cursor.HAND_CURSOR));
        pFg.addMouseListener(click(() -> mostrarListaUsuarios(sistema.getListaFollowing(username), "Siguiendo", "Following", username)));
        row2.add(pP);
        row2.add(pF);
        row2.add(pFg);
        info.add(row2);
        info.add(Box.createVerticalStrut(10));

        Usuario u = sistema.buscarUsuario(username);
        JLabel lblNombre = new JLabel(u != null ? u.getNombreCompleto() : "");
        lblNombre.setFont(F_BOLD);
        lblNombre.setForeground(C_TEXT);
        JPanel row3 = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        row3.setBackground(C_BG);
        row3.add(lblNombre);
        info.add(row3);
        info.add(Box.createVerticalStrut(4));
        if (u != null) {
            String estadoStr = u.getEstadoCuenta() == EstadoCuenta.ACTIVO ? "Activo" : "Desactivado";
            String datos = (u.getGenero() == 'M' ? "Masculino" : "Femenino") + "  ·  "
                    + u.getEdad() + " años  ·  "
                    + u.getTipoCuenta().name() + "  ·  "
                    + estadoStr + "  ·  "
                    + "Desde " + u.getFechaRegistro();
            JLabel lblD = new JLabel(datos);
            lblD.setFont(F_SMALL);
            lblD.setForeground(u.getEstadoCuenta() == EstadoCuenta.DESACTIVADO
                    ? C_ERROR
                    : C_TEXT_LIGHT);
            JPanel row4 = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
            row4.setBackground(C_BG);
            row4.add(lblD);
            info.add(row4);
        }
        hdr.add(info, BorderLayout.CENTER);
        content.add(hdr, BorderLayout.NORTH);

        JSeparator sep = new JSeparator();
        sep.setForeground(C_BORDER);
        sep.setBackground(C_BORDER);

        // Grid 4 cols — fondo blanco
        JPanel grid = new JPanel(new GridLayout(0, 4, 2, 2));
        grid.setBackground(C_WHITE);
        grid.setBorder(BorderFactory.createEmptyBorder(2, 60, 20, 60));
        ArrayList<Publicacion> posts = sistema.getPublicacionesDeUsuario(username);
        if (posts.isEmpty()) {
            JLabel vacio = new JLabel("No hay publicaciones.", SwingConstants.CENTER);
            vacio.setFont(F_REGULAR);
            vacio.setForeground(C_TEXT_LIGHT);
            grid.add(vacio);
        } else {
            final int CELL = 252;
            for (Publicacion p : posts) {
                JPanel cell = new JPanel(null);
                cell.setPreferredSize(new Dimension(CELL, CELL));
                cell.setBackground(C_WHITE);
                cell.setCursor(new Cursor(Cursor.HAND_CURSOR));
                JLabel img = new JLabel();
                img.setBounds(0, 0, CELL, CELL);
                img.setHorizontalAlignment(SwingConstants.CENTER);
                img.setOpaque(true);
                img.setBackground(C_WHITE);
                try {
                    if (p.getRutaImagen() != null && new File(p.getRutaImagen()).exists()) {
                        ImageIcon cr = crearImagenCropeada(p.getRutaImagen(), CELL, CELL);
                        if (cr != null) {
                            img.setIcon(cr);
                        }
                    }
                } catch (Exception ignored) {
                }
                JPanel overlay = new JPanel() {
                    {
                        setOpaque(false);
                        setVisible(false);
                    }

                    @Override
                    protected void paintComponent(Graphics g) {
                        g.setColor(new Color(0, 0, 0, 80));
                        g.fillRect(0, 0, getWidth(), getHeight());
                    }
                };
                overlay.setBounds(0, 0, CELL, CELL);
                cell.add(overlay);
                cell.add(img);
                cell.addMouseListener(new MouseAdapter() {
                    public void mouseEntered(MouseEvent e) {
                        overlay.setVisible(true);
                        cell.repaint();
                    }

                    public void mouseExited(MouseEvent e) {
                        overlay.setVisible(false);
                        cell.repaint();
                    }

                    public void mouseClicked(MouseEvent e) {
                        abrirDetallePost(p);
                    }
                });
                grid.add(cell);
            }
        }
        JPanel cp = new JPanel(new BorderLayout());
        cp.setBackground(C_BG);
        cp.add(sep, BorderLayout.NORTH);
        cp.add(styledScroll(grid), BorderLayout.CENTER);
        content.add(cp, BorderLayout.CENTER);
        add(content, BorderLayout.CENTER);
        revalidate();
        repaint();
    }

    // ════════════════════════════════════════════════════════════
    //  DETALLE DE POST
    // ════════════════════════════════════════════════════════════
    private void abrirDetallePost(Publicacion p) {
        JDialog d = new JDialog(this, "", true);
        d.setUndecorated(true);
        d.setSize(900, 600);
        d.setLocationRelativeTo(this);
        d.setBackground(new Color(0, 0, 0, 0));
        Component og = getGlassPane();
        JPanel glass = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                g.setColor(new Color(0, 0, 0, 150));
                g.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        glass.setOpaque(false);
        glass.addMouseListener(new MouseAdapter() {
        });
        setGlassPane(glass);
        glass.setVisible(true);
        d.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                glass.setVisible(false);
                setGlassPane(og);
                repaint();
            }
        });
        d.setUndecorated(true);
        JPanel root = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(C_WHITE);
                g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 12, 12));
                g2.dispose();
            }
        };
        root.setOpaque(false);
        int IW = 520, IH = 600;
        JPanel imgP = new JPanel(new BorderLayout());
        imgP.setBackground(Color.BLACK);
        imgP.setPreferredSize(new Dimension(IW, IH));
        JLabel iL = new JLabel();
        iL.setHorizontalAlignment(SwingConstants.CENTER);
        try {
            File f = new File(p.getRutaImagen());
            if (f.exists()) {
                ImageIcon orig = new ImageIcon(p.getRutaImagen());
                int iw = orig.getIconWidth(), ih = orig.getIconHeight();
                double sc = Math.min((double) IW / iw, (double) IH / ih);
                iL.setIcon(new ImageIcon(orig.getImage().getScaledInstance((int) (iw * sc), (int) (ih * sc), Image.SCALE_SMOOTH)));
            }
        } catch (Exception ignored) {
        }
        imgP.add(iL, BorderLayout.CENTER);

        JPanel right = new JPanel(new BorderLayout());
        right.setBackground(C_WHITE);
        right.setPreferredSize(new Dimension(380, IH));
        JPanel rH = new JPanel(new BorderLayout());
        rH.setBackground(C_WHITE);
        rH.setBorder(BorderFactory.createCompoundBorder(new MatteBorder(0, 0, 1, 0, C_BORDER), BorderFactory.createEmptyBorder(12, 14, 12, 14)));
        JPanel rHL = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        rHL.setBackground(C_WHITE);
        rHL.add(new JLabel(cargarFotoCircular(p.getAutor(), 32)));
        JLabel rUN = new JLabel(p.getAutor());
        rUN.setFont(F_BOLD);
        rUN.setForeground(C_TEXT);
        rUN.setCursor(new Cursor(Cursor.HAND_CURSOR));
        rUN.addMouseListener(click(() -> {
            d.dispose();
            cargarVistaPerfil(p.getAutor());
        }));
        rHL.add(rUN);
        rHL.add(badgeVerificado(p.getAutor()));
        JButton btnC2 = new JButton();
        if (iconsNormal.containsKey("Close")) {
            btnC2.setIcon(iconsNormal.get("Close"));
        } else {
            btnC2.setText("x");
            btnC2.setFont(F_SMALL);
        }
        btnC2.setBorderPainted(false);
        btnC2.setContentAreaFilled(false);
        btnC2.setFocusPainted(false);
        btnC2.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnC2.addActionListener(e -> d.dispose());
        rH.add(rHL, BorderLayout.WEST);
        rH.add(btnC2, BorderLayout.EAST);
        right.add(rH, BorderLayout.NORTH);

        JPanel sc2 = new JPanel();
        sc2.setLayout(new BoxLayout(sc2, BoxLayout.Y_AXIS));
        sc2.setBackground(C_WHITE);
        if (p.getContenido() != null && !p.getContenido().isEmpty()) {
            JPanel cr = new JPanel(new BorderLayout());
            cr.setBackground(C_WHITE);
            cr.setBorder(BorderFactory.createEmptyBorder(12, 14, 10, 14));
            JPanel cl = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
            cl.setBackground(C_WHITE);
            cl.add(new JLabel(cargarFotoCircular(p.getAutor(), 28)));
            JLabel ct = new JLabel("<html><b>" + p.getAutor() + "</b> " + p.getContenido() + "</html>");
            ct.setFont(F_REGULAR);
            ct.setForeground(C_TEXT);
            cl.add(ct);
            cr.add(cl, BorderLayout.CENTER);
            sc2.add(cr);
            JSeparator s2 = new JSeparator();
            s2.setForeground(C_BORDER);
            s2.setBackground(C_BORDER);
            sc2.add(s2);
        }
        for (String c : sistema.getComentarios(p.getAutor(), p.getFecha().toString())) {
            String[] pts = c.split(":", 2);
            String cu = pts[0].trim(), ct = pts.length > 1 ? pts[1].trim() : c;
            JPanel cr2 = new JPanel(new BorderLayout());
            cr2.setBackground(C_WHITE);
            cr2.setBorder(BorderFactory.createEmptyBorder(10, 14, 8, 14));
            JPanel cl2 = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
            cl2.setBackground(C_WHITE);
            cl2.add(new JLabel(cargarFotoCircular(cu, 28)));
            JPanel nt = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
            nt.setBackground(C_WHITE);
            JLabel nl = new JLabel(cu);
            nl.setFont(F_BOLD);
            nl.setForeground(C_TEXT);
            nl.setCursor(new Cursor(Cursor.HAND_CURSOR));
            nl.addMouseListener(click(() -> {
                d.dispose();
                cargarVistaPerfil(cu);
            }));
            nt.add(nl);
            JLabel tl = new JLabel(ct);
            tl.setFont(F_REGULAR);
            tl.setForeground(C_TEXT);
            nt.add(tl);
            cl2.add(nt);
            cr2.add(cl2, BorderLayout.CENTER);
            sc2.add(cr2);
        }
        JScrollPane sp2 = styledScroll(sc2, false);
        sp2.setBorder(null);
        right.add(sp2, BorderLayout.CENTER);

        JPanel ft2 = new JPanel(new BorderLayout());
        ft2.setBackground(C_WHITE);
        ft2.setBorder(new MatteBorder(1, 0, 0, 0, C_BORDER));
        JPanel lr = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        lr.setBackground(C_WHITE);
        int[] lc = {sistema.getCantidadLikes(p.getAutor(), p.getFecha().toString())};
        boolean[] lkd = {sistema.yaDioLike(p.getAutor(), p.getFecha().toString())};
        JButton btnL = new JButton();
        if (lkd[0]) {
            if (iconsBold.containsKey("Like")) {
                btnL.setIcon(iconsBold.get("Like"));
            } else {
                btnL.setText("<3");
            }
        } else {
            if (iconsNormal.containsKey("Like")) {
                btnL.setIcon(iconsNormal.get("Like"));
            } else {
                btnL.setText("o");
            }
        }
        btnL.setBorderPainted(false);
        btnL.setContentAreaFilled(false);
        btnL.setFocusPainted(false);
        btnL.setCursor(new Cursor(Cursor.HAND_CURSOR));
        JLabel llk = new JLabel(lc[0] + " Me gusta");
        llk.setFont(F_BOLD);
        llk.setForeground(C_TEXT);
        btnL.addActionListener(e -> {
            sistema.toggleLike(p.getAutor(), p.getFecha().toString(), p.getRutaImagen());
            lkd[0] = sistema.yaDioLike(p.getAutor(), p.getFecha().toString());
            lc[0] = sistema.getCantidadLikes(p.getAutor(), p.getFecha().toString());
            if (lkd[0]) {
                if (iconsBold.containsKey("Like")) {
                    btnL.setIcon(iconsBold.get("Like"));
                } else {
                    btnL.setText("<3");
                }
            } else {
                if (iconsNormal.containsKey("Like")) {
                    btnL.setIcon(iconsNormal.get("Like"));
                } else {
                    btnL.setText("o");
                }
            }
            llk.setText(lc[0] + " Me gusta");
        });
        lr.add(btnL);
        lr.add(llk);
        JPanel ir = new JPanel(new BorderLayout(6, 0));
        ir.setBackground(C_WHITE);
        ir.setBorder(BorderFactory.createEmptyBorder(6, 14, 10, 14));
        JTextField tc = new JTextField();
        tc.setFont(F_REGULAR);
        tc.setBackground(C_FIELD);
        tc.setBorder(BorderFactory.createCompoundBorder(new LineBorder(C_BORDER, 1, true), BorderFactory.createEmptyBorder(8, 12, 8, 12)));
        tc.putClientProperty("JTextField.placeholderText", "Agrega un comentario...");
        JButton bec = buildPrimaryBtn("Publicar");
        bec.setPreferredSize(new Dimension(80, 36));
        bec.setFont(new Font("Arial", Font.BOLD, 12));
        ActionListener ec = e -> {
            String tx = tc.getText().trim();
            if (tx.isEmpty()) {
                return;
            }
            sistema.agregarComentario(p.getAutor(), p.getFecha().toString(), tx);
            JPanel nr = new JPanel(new BorderLayout());
            nr.setBackground(C_WHITE);
            nr.setBorder(BorderFactory.createEmptyBorder(10, 14, 8, 14));
            JPanel nl2 = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
            nl2.setBackground(C_WHITE);
            nl2.add(new JLabel(cargarFotoCircular(sistema.getUsuarioActual().getUsername(), 28)));
            JLabel nl3 = new JLabel("<html><b>" + sistema.getUsuarioActual().getUsername() + "</b> " + tx + "</html>");
            nl3.setFont(F_REGULAR);
            nl3.setForeground(C_TEXT);
            nl2.add(nl3);
            nr.add(nl2, BorderLayout.CENTER);
            sc2.add(nr);
            sc2.revalidate();
            sc2.repaint();
            tc.setText("");
            SwingUtilities.invokeLater(() -> sp2.getVerticalScrollBar().setValue(sp2.getVerticalScrollBar().getMaximum()));
        };
        bec.addActionListener(ec);
        tc.addActionListener(ec);
        ir.add(tc, BorderLayout.CENTER);
        ir.add(bec, BorderLayout.EAST);
        ft2.add(lr, BorderLayout.NORTH);
        ft2.add(ir, BorderLayout.SOUTH);
        right.add(ft2, BorderLayout.SOUTH);
        root.add(imgP, BorderLayout.WEST);
        root.add(right, BorderLayout.CENTER);
        d.setContentPane(root);
        d.setVisible(true);
    }

    private JPanel statPanel(int n, String label) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        p.setBackground(C_BG);
        JLabel num = new JLabel(String.valueOf(n));
        num.setFont(F_BOLD);
        num.setForeground(C_TEXT);
        JLabel txt = new JLabel(label);
        txt.setFont(F_REGULAR);
        txt.setForeground(C_TEXT);
        p.add(num);
        p.add(txt);
        return p;
    }

    // ════════════════════════════════════════════════════════════
    //  VISTA 5 — BÚSQUEDA + EXPLORE GRID
    // ════════════════════════════════════════════════════════════
    private void cargarVistaBusqueda() {
        limpiarOverlaysActivos();
        vistaActual = "Search";
        getContentPane().removeAll();
        setLayout(new BorderLayout());
        getContentPane().setBackground(C_BG);
        add(buildSidebar(), BorderLayout.WEST);
        JPanel main = new JPanel(new BorderLayout());
        main.setBackground(C_BG);
        main.setBorder(BorderFactory.createEmptyBorder(32, 40, 40, 40));
        JPanel searchBar = new JPanel(new BorderLayout());
        searchBar.setBackground(new Color(239, 239, 239));
        searchBar.setBorder(new LineBorder(C_BORDER, 1, true));
        JTextField txtB = new JTextField("Buscar usuario o #hashtag...");
        txtB.setFont(F_REGULAR);
        txtB.setBackground(new Color(239, 239, 239));
        txtB.setForeground(C_PLACEHOLDER);
        txtB.setBorder(BorderFactory.createEmptyBorder(11, 16, 11, 12));
        txtB.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent e) {
                if (txtB.getText().equals("Buscar usuario o #hashtag...")) {
                    txtB.setText("");
                    txtB.setForeground(C_TEXT);
                }
            }

            public void focusLost(FocusEvent e) {
                if (txtB.getText().isEmpty()) {
                    txtB.setText("Buscar usuario o #hashtag...");
                    txtB.setForeground(C_PLACEHOLDER);
                }
            }
        });
        JButton btnB = buildPrimaryBtn("Buscar");
        btnB.setPreferredSize(new Dimension(90, 40));
        searchBar.add(txtB, BorderLayout.CENTER);
        searchBar.add(btnB, BorderLayout.EAST);
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(C_BG);
        topBar.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));
        topBar.add(searchBar, BorderLayout.CENTER);
        main.add(topBar, BorderLayout.NORTH);
        JPanel cw = new JPanel(new BorderLayout());
        cw.setBackground(C_BG);
        cw.add(styledScroll(buildExploreGrid()), BorderLayout.CENTER);
        main.add(cw, BorderLayout.CENTER);
        add(main, BorderLayout.CENTER);

        ActionListener doSearch = e -> {
            String q = txtB.getText().trim();
            if (q.isEmpty() || q.equals("Buscar usuario o #hashtag...")) {
                cw.removeAll();
                cw.add(styledScroll(buildExploreGrid()), BorderLayout.CENTER);
                cw.revalidate();
                cw.repaint();
                return;
            }
            JPanel res = new JPanel();
            res.setLayout(new BoxLayout(res, BoxLayout.Y_AXIS));
            res.setBackground(C_BG);
            if (q.startsWith("#")) {
                ArrayList<Publicacion> posts = sistema.buscarPorHashtag(q);
                if (posts.isEmpty()) {
                    res.add(emptyLabel("Sin resultados para " + q));
                } else {
                    for (Publicacion p : posts) {
                        res.add(buildPost(p));
                        res.add(Box.createVerticalStrut(4));
                    }
                }
            } else {
                ArrayList<Usuario> users = sistema.buscarUsuarios(q);
                if (users.isEmpty()) {
                    res.add(emptyLabel("No se encontraron usuarios."));
                } else {
                    for (Usuario u : users) {
                        JPanel ur = buildUserRow(u);
                        if (ur.getComponentCount() > 0) {
                            res.add(ur);
                        }
                    }
                }
            }
            cw.removeAll();
            cw.add(styledScroll(res), BorderLayout.CENTER);
            cw.revalidate();
            cw.repaint();
        };
        btnB.addActionListener(doSearch);
        txtB.addActionListener(doSearch);
        revalidate();
        repaint();
    }

    private JPanel buildExploreGrid() {
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(C_BG);
        JLabel titulo = new JLabel("Explorar");
        titulo.setFont(F_H2);
        titulo.setForeground(C_TEXT);
        titulo.setBorder(BorderFactory.createEmptyBorder(0, 0, 14, 0));
        wrapper.add(titulo, BorderLayout.NORTH);

        sistema.invalidarCachePublica(); // leer estados actuales
        ArrayList<Publicacion> explorePosts = new ArrayList<>();
        java.util.Set<String> ag = new java.util.HashSet<>();
        for (String def : InicializadorCuentasDefault.USERNAMES_DEFAULT) {
            for (Publicacion p : sistema.getPublicacionesDeUsuario(def)) {
                if (ag.add(p.getAutor() + "|" + p.getFecha() + "|" + p.getHora())) {
                    explorePosts.add(p);
                }
            }
        }
        File raiz = new File("INSTA_RAIZ");
        String[] cs = raiz.list();
        if (cs != null) {
            for (String f : cs) {
                if (f.equals("stickers_globales")) {
                    continue;
                }
                if (sistema.getUsuarioActual() != null && f.equals(sistema.getUsuarioActual().getUsername())) {
                    continue;
                }
                Usuario uf = sistema.buscarUsuario(f);
                if (uf != null && uf.getEstadoCuenta() == EstadoCuenta.DESACTIVADO) {
                    continue;
                }
                for (Publicacion p : sistema.getPublicacionesDeUsuario(f)) {
                    if (ag.add(p.getAutor() + "|" + p.getFecha() + "|" + p.getHora())) {
                        explorePosts.add(p);
                    }
                }
            }
        }
        Collections.shuffle(explorePosts, new java.util.Random(System.currentTimeMillis() / 60000));

        final int COLS = 3, GAP = 3, THUMB = (1366 - 220 - 80) / COLS - GAP;
        JPanel grid = new JPanel(new GridLayout(0, COLS, GAP, GAP));
        grid.setBackground(C_BG);
        if (explorePosts.isEmpty()) {
            grid.setLayout(new BorderLayout());
            grid.add(emptyLabel("Sigue a más personas para ver contenido aquí."), BorderLayout.CENTER);
        } else {
            for (Publicacion p : explorePosts) {
                JPanel cell = new JPanel(null);
                cell.setPreferredSize(new Dimension(THUMB, THUMB));
                cell.setBackground(C_WHITE);
                cell.setCursor(new Cursor(Cursor.HAND_CURSOR));
                JLabel imgL = new JLabel();
                imgL.setBounds(0, 0, THUMB, THUMB);
                imgL.setOpaque(true);
                imgL.setBackground(C_WHITE);
                imgL.setHorizontalAlignment(SwingConstants.CENTER);
                try {
                    if (p.getRutaImagen() != null && !p.getRutaImagen().isEmpty() && new File(p.getRutaImagen()).exists()) {
                        ImageIcon cr = crearImagenCropeada(p.getRutaImagen(), THUMB, THUMB);
                        if (cr != null) {
                            imgL.setIcon(cr);
                        }
                    }
                } catch (Exception ignored) {
                }
                JPanel ov = new JPanel(new BorderLayout()) {
                    {
                        setOpaque(false);
                        setVisible(false);
                    }

                    @Override
                    protected void paintComponent(Graphics g) {
                        g.setColor(new Color(0, 0, 0, 100));
                        g.fillRect(0, 0, getWidth(), getHeight());
                    }
                };
                ov.setBounds(0, 0, THUMB, THUMB);
                JPanel ob = new JPanel(new FlowLayout(FlowLayout.CENTER, 4, 6));
                ob.setOpaque(false);
                JLabel ou = new JLabel(p.getAutor());
                ou.setFont(F_BOLD);
                ou.setForeground(Color.WHITE);
                ob.add(ou);
                if (InicializadorCuentasDefault.VERIFICADOS.contains(p.getAutor())) {
                    ob.add(badgeVerificado(p.getAutor()));
                }
                ov.add(ob, BorderLayout.SOUTH);
                cell.add(ov);
                cell.add(imgL);
                cell.addMouseListener(new MouseAdapter() {
                    public void mouseEntered(MouseEvent e) {
                        ov.setVisible(true);
                        cell.repaint();
                    }

                    public void mouseExited(MouseEvent e) {
                        ov.setVisible(false);
                        cell.repaint();
                    }

                    public void mouseClicked(MouseEvent e) {
                        abrirDetallePost(p);
                    }
                });
                grid.add(cell);
            }
        }
        wrapper.add(grid, BorderLayout.CENTER);
        return wrapper;
    }

    private JPanel buildUserRow(Usuario u) {
        if (u.getEstadoCuenta() == EstadoCuenta.DESACTIVADO) {
            return new JPanel();
        }

        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(C_WHITE);
        p.setMaximumSize(new Dimension(700, 64));
        p.setPreferredSize(new Dimension(700, 64));
        p.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(C_BORDER, 1),
                BorderFactory.createEmptyBorder(10, 14, 10, 14)));
        p.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Izquierda: foto + textos alineados
        JPanel left = new JPanel(new BorderLayout(12, 0));
        left.setBackground(C_WHITE);

        JLabel foto = new JLabel(cargarFotoCircular(u.getUsername(), 40));
        foto.setVerticalAlignment(SwingConstants.CENTER);
        left.add(foto, BorderLayout.WEST);

        // Textos en columna
        JPanel textos = new JPanel();
        textos.setLayout(new BoxLayout(textos, BoxLayout.Y_AXIS));
        textos.setBackground(C_WHITE);
        textos.setBorder(BorderFactory.createEmptyBorder(2, 0, 0, 0));

        // Username + badge + candado en la misma fila
        JPanel fila1 = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        fila1.setBackground(C_WHITE);
        JLabel nombre = new JLabel(u.getUsername());
        nombre.setFont(F_BOLD);
        nombre.setForeground(C_TEXT);
        fila1.add(nombre);
        fila1.add(badgeVerificado(u.getUsername()));
        if (u.getTipoCuenta() == TipoCuenta.PRIVADA) {
            JLabel candado = new JLabel();
            if (iconsNormal.containsKey("Lock")) {
                candado.setIcon(iconsNormal.get("Lock"));
            } else {
                candado.setText("🔒");
                candado.setFont(F_SMALL);
            }
            fila1.add(candado);
        }
        textos.add(fila1);

        // Nombre real debajo
        JLabel nomReal = new JLabel(u.getNombreCompleto());
        nomReal.setFont(F_SMALL);
        nomReal.setForeground(C_TEXT_LIGHT);
        nomReal.setBorder(BorderFactory.createEmptyBorder(2, 4, 0, 0));
        textos.add(nomReal);

        left.add(textos, BorderLayout.CENTER);
        p.add(left, BorderLayout.WEST);

        // Hover
        p.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                p.setBackground(C_HOVER);
                left.setBackground(C_HOVER);
                textos.setBackground(C_HOVER);
                fila1.setBackground(C_HOVER);
            }

            public void mouseExited(MouseEvent e) {
                p.setBackground(C_WHITE);
                left.setBackground(C_WHITE);
                textos.setBackground(C_WHITE);
                fila1.setBackground(C_WHITE);
            }
        });
        p.addMouseListener(click(() -> cargarVistaPerfil(u.getUsername())));
        return p;
    }

    // ════════════════════════════════════════════════════════════
    //  VISTA 6 — INBOX
    // ════════════════════════════════════════════════════════════
    private void cargarVistaInbox() {
        cargarVistaInboxCon(null);
    }

    private void cargarVistaInboxCon(String otroUsuario) {
        limpiarOverlaysActivos();
        vistaActual = "Messages";
        if (chatTimer != null) {
            chatTimer.stop();
        }
        getContentPane().removeAll();
        setLayout(new BorderLayout());
        add(buildSidebar(), BorderLayout.WEST);
        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        split.setDividerLocation(280);
        split.setBorder(null);
        split.setDividerSize(1);
        JPanel listPanel = new JPanel(new BorderLayout());
        listPanel.setBackground(C_WHITE);
        listPanel.setBorder(new MatteBorder(0, 0, 0, 1, C_BORDER));
        JPanel lH = new JPanel(new BorderLayout());
        lH.setBackground(C_WHITE);
        lH.setBorder(BorderFactory.createCompoundBorder(new MatteBorder(0, 0, 1, 0, C_BORDER), BorderFactory.createEmptyBorder(16, 16, 16, 16)));
        JLabel lblM = new JLabel(sistema.getUsuarioActual().getUsername());
        lblM.setFont(F_BOLD);
        lblM.setForeground(C_TEXT);
        JButton btnN = new JButton("+");
        btnN.setFont(new Font("Arial", Font.BOLD, 22));
        btnN.setBorderPainted(false);
        btnN.setContentAreaFilled(false);
        btnN.setFocusPainted(false);
        btnN.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnN.setForeground(C_TEXT);
        lH.add(lblM, BorderLayout.WEST);
        lH.add(btnN, BorderLayout.EAST);
        listPanel.add(lH, BorderLayout.NORTH);
        JPanel chatList = new JPanel();
        chatList.setLayout(new BoxLayout(chatList, BoxLayout.Y_AXIS));
        chatList.setBackground(C_WHITE);
        listPanel.add(styledScroll(chatList), BorderLayout.CENTER);
        JPanel chatPanel = new JPanel(new BorderLayout());
        chatPanel.setBackground(C_BG);
        chatPanel.add(new JLabel("Selecciona un mensaje", SwingConstants.CENTER) {
            {
                setFont(F_REGULAR);
                setForeground(C_TEXT_LIGHT);
            }
        }, BorderLayout.CENTER);
        split.setLeftComponent(listPanel);
        split.setRightComponent(chatPanel);
        add(split, BorderLayout.CENTER);

        for (String user : sistema.getChatsRecientes()) {
            Usuario uCheck = sistema.buscarUsuario(user);
            if (uCheck != null && uCheck.getEstadoCuenta() == EstadoCuenta.DESACTIVADO) {
                continue;
            }
            int nl = sistema.getMensajesNoLeidos(user);
            JPanel row = new JPanel(new BorderLayout(8, 0));
            row.setBackground(C_WHITE);
            row.setBorder(BorderFactory.createEmptyBorder(10, 16, 10, 16));
            row.setMaximumSize(new Dimension(280, 68));
            row.add(new JLabel(cargarFotoCircular(user, 44)), BorderLayout.WEST);
            JLabel nomL = new JLabel("  " + user);
            nomL.setFont(nl > 0 ? F_BOLD : F_REGULAR);
            nomL.setForeground(C_TEXT);
            row.add(nomL, BorderLayout.CENTER);
            if (nl > 0) {
                JPanel dot = new JPanel() {
                    @Override
                    protected void paintComponent(Graphics g) {
                        Graphics2D g2 = (Graphics2D) g.create();
                        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                        g2.setColor(C_BLUE);
                        g2.fillOval(0, 0, getWidth(), getHeight());
                        g2.dispose();
                    }
                };
                dot.setPreferredSize(new Dimension(10, 10));
                dot.setOpaque(false);
                JPanel dw = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 17));
                dw.setBackground(C_WHITE);
                dw.add(dot);
                row.add(dw, BorderLayout.EAST);
            }
            row.setCursor(new Cursor(Cursor.HAND_CURSOR));
            row.addMouseListener(new MouseAdapter() {
                public void mouseClicked(MouseEvent e) {
                    lastMessageCount = 0;
                    mostrarChatLive(chatPanel, user);
                }

                public void mouseEntered(MouseEvent e) {
                    row.setBackground(C_HOVER);
                }

                public void mouseExited(MouseEvent e) {
                    row.setBackground(C_WHITE);
                }
            });
            chatList.add(row);
            JSeparator s = new JSeparator();
            s.setForeground(C_BORDER);
            s.setBackground(C_BORDER);
            chatList.add(s);
        }

        btnN.addActionListener(e -> {
            String dest = InstaDialog.showInput(this, "Nuevo mensaje", "Nombre de usuario");
            if (dest == null) {
                return;
            }
            Usuario destU = sistema.buscarUsuario(dest);
            if (destU == null) {
                InstaDialog.showMessage(this, "Usuario no encontrado.", true);
                return;
            }
            if (destU.getEstadoCuenta() == EstadoCuenta.DESACTIVADO) {
                InstaDialog.showMessage(this, "Esa cuenta no está disponible.", true);
                return;
            }
            if (!sistema.puedeEnviarMensaje(dest)) {
                String razon = destU.getTipoCuenta() == TipoCuenta.PRIVADA ? "Su cuenta es privada — deben seguirse mutuamente primero." : "No puedes enviarle mensajes.";
                InstaDialog.showMessage(this, "No puedes chatear con @" + dest + ".\n" + razon, true);
                return;
            }
            lastMessageCount = 0;
            mostrarChatLive(chatPanel, dest);
        });

        revalidate();
        repaint();
        if (otroUsuario != null) {
            SwingUtilities.invokeLater(() -> mostrarChatLive(chatPanel, otroUsuario));
        }
    }

    private void mostrarChatLive(JPanel chatPanel, String otro) {
        if (chatTimer != null) {
            chatTimer.stop();
        }
        chatPanel.removeAll();
        chatPanel.setLayout(new BorderLayout());
        chatPanel.setBackground(C_BG);

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(C_WHITE);
        header.setBorder(BorderFactory.createCompoundBorder(new MatteBorder(0, 0, 1, 0, C_BORDER), BorderFactory.createEmptyBorder(12, 16, 12, 16)));
        JPanel hL = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        hL.setBackground(C_WHITE);
        hL.add(new JLabel(cargarFotoCircular(otro, 36)));
        JLabel nomL = new JLabel(otro);
        nomL.setFont(F_BOLD);
        nomL.setForeground(C_TEXT);
        nomL.setCursor(new Cursor(Cursor.HAND_CURSOR));
        nomL.addMouseListener(click(() -> cargarVistaPerfil(otro)));
        hL.add(nomL);
        hL.add(badgeVerificado(otro));
        header.add(hL, BorderLayout.WEST);
        JButton btnDel = new JButton();
        if (iconsNormal.containsKey("Trash")) {
            btnDel.setIcon(iconsNormal.get("Trash"));
        } else {
            btnDel.setText("Eliminar");
            btnDel.setFont(F_SMALL);
        }
        btnDel.setForeground(C_ERROR);
        btnDel.setBorderPainted(false);
        btnDel.setContentAreaFilled(false);
        btnDel.setFocusPainted(false);
        btnDel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        header.add(btnDel, BorderLayout.EAST);

        JPanel msgPanel = new JPanel();
        msgPanel.setLayout(new BoxLayout(msgPanel, BoxLayout.Y_AXIS));
        msgPanel.setBackground(C_BG);
        JScrollPane msgScroll = styledScroll(msgPanel, false);
        JPanel footer = new JPanel(new BorderLayout(0, 0));
        footer.setBackground(C_WHITE);
        footer.setBorder(BorderFactory.createCompoundBorder(new MatteBorder(1, 0, 0, 0, C_BORDER), BorderFactory.createEmptyBorder(6, 12, 6, 12)));
        JPanel inputRow = new JPanel(new BorderLayout(4, 0));
        inputRow.setBackground(C_WHITE);
        JTextField txtI = new JTextField();
        txtI.setFont(F_REGULAR);
        txtI.setBackground(C_FIELD);
        txtI.setBorder(BorderFactory.createCompoundBorder(new LineBorder(C_BORDER, 1, true), BorderFactory.createEmptyBorder(9, 14, 9, 14)));
        txtI.putClientProperty("JTextField.placeholderText", "Escribe un mensaje...");
        JButton btnSend = new JButton();
        if (iconsNormal.containsKey("Send")) {
            btnSend.setIcon(iconsNormal.get("Send"));
        } else {
            btnSend.setText("Enviar");
            btnSend.setFont(F_BOLD);
            btnSend.setForeground(C_BLUE);
        }
        btnSend.setBorderPainted(false);
        btnSend.setContentAreaFilled(false);
        btnSend.setFocusPainted(false);
        btnSend.setCursor(new Cursor(Cursor.HAND_CURSOR));
        JButton btnStk = new JButton();
        if (iconsNormal.containsKey("Sticker")) {
            btnStk.setIcon(iconsNormal.get("Sticker"));
        } else {
            btnStk.setText(":)");
            btnStk.setFont(new Font("Arial", Font.PLAIN, 14));
        }
        btnStk.setBackground(C_FIELD);
        btnStk.setBorderPainted(false);
        btnStk.setFocusPainted(false);
        btnStk.setCursor(new Cursor(Cursor.HAND_CURSOR));
        JLabel lblCC = new JLabel("0/300", SwingConstants.RIGHT);
        lblCC.setFont(new Font("Arial", Font.PLAIN, 10));
        lblCC.setForeground(C_TEXT_LIGHT);
        lblCC.setBorder(BorderFactory.createEmptyBorder(2, 0, 0, 4));
        txtI.getDocument().addDocumentListener(simpleDocListener(() -> {
            int len = txtI.getText().length();
            lblCC.setText(len + "/300");
            if (len > 270) {
                lblCC.setForeground(C_ERROR);
            } else if (len > 240) {
                lblCC.setForeground(new Color(230, 140, 0));
            } else {
                lblCC.setForeground(C_TEXT_LIGHT);
            }
            btnSend.setEnabled(len <= 300 && len > 0);
        }));
        inputRow.add(btnStk, BorderLayout.WEST);
        inputRow.add(txtI, BorderLayout.CENTER);
        inputRow.add(btnSend, BorderLayout.EAST);
        footer.add(inputRow, BorderLayout.CENTER);
        footer.add(lblCC, BorderLayout.SOUTH);
        chatPanel.add(header, BorderLayout.NORTH);
        chatPanel.add(msgScroll, BorderLayout.CENTER);
        chatPanel.add(footer, BorderLayout.SOUTH);

        ActionListener send = e -> {
            if (!txtI.getText().isEmpty()) {
                sistema.enviarMensaje(otro, txtI.getText(), "TEXTO");
                clienteSocket.enviar(new red.EventoSocket(
                        red.EventoSocket.Tipo.NUEVO_MENSAJE,
                        sistema.getUsuarioActual().getUsername(), otro));
                txtI.setText("");
                lastMessageCount = 0;
                lastMessageSignature = "";
                refrescarMensajes(msgPanel, otro);
            }
        };
        btnSend.addActionListener(send);
        txtI.addActionListener(send);
        btnDel.addActionListener(e -> {
            boolean yes = InstaDialog.showConfirm(chatPanel, "¿Eliminar historial con " + otro + "?", "Eliminar", true);
            if (yes) {
                sistema.eliminarConversacion(otro);
                lastMessageCount = 0;
                lastMessageSignature = "";
                refrescarMensajes(msgPanel, otro);
            }
        });

        btnStk.addActionListener(ev -> {
            ArrayList<String> stickers = sistema.getTodosStickers(sistema.getUsuarioActual().getUsername());
            JDialog sd = new JDialog(this, "Stickers", true);
            sd.setSize(420, 460);
            sd.setLocationRelativeTo(chatPanel);
            sd.setLayout(new BorderLayout());
            sd.getContentPane().setBackground(C_WHITE);
            sd.add(buildDialogHeader("Stickers", sd), BorderLayout.NORTH);
            sd.setUndecorated(true);
            JPanel grid = new JPanel(new GridLayout(0, 4, 8, 8));
            grid.setBackground(C_WHITE);
            grid.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
            JButton bi = new JButton("+");
            bi.setFont(new Font("Arial", Font.BOLD, 26));
            bi.setPreferredSize(new Dimension(88, 88));
            bi.setMinimumSize(new Dimension(88, 88));
            bi.setBackground(new Color(245, 245, 245));
            bi.setBorderPainted(false);
            bi.setContentAreaFilled(false);
            bi.setFocusPainted(false);
            bi.setCursor(new Cursor(Cursor.HAND_CURSOR));
            bi.addActionListener(se -> {
                JFileChooser fc = new JFileChooser();
                fc.setFileFilter(new FileNameExtensionFilter("Imágenes", "png", "jpg", "jpeg"));
                if (fc.showOpenDialog(sd) == JFileChooser.APPROVE_OPTION) {
                    sistema.guardarStickerPersonal(fc.getSelectedFile(), sistema.getUsuarioActual().getUsername());
                    InstaDialog.showMessage(sd, "Sticker importado. ¡Ya puedes usarlo!");
                }
                sd.dispose();
            });
            grid.add(bi);
            for (String r : stickers) {
                JButton bs = new JButton();
                bs.setPreferredSize(new Dimension(88, 88));
                bs.setMinimumSize(new Dimension(88, 88));
                bs.setBorderPainted(false);
                bs.setContentAreaFilled(false);
                bs.setFocusPainted(false);
                bs.setCursor(new Cursor(Cursor.HAND_CURSOR));
                try {
                    File f = new File(r);
                    if (f.exists()) {
                        bs.setIcon(new ImageIcon(new ImageIcon(r).getImage().getScaledInstance(68, 68, Image.SCALE_SMOOTH)));
                    } else {
                        bs.setText("?");
                    }
                } catch (Exception ignored) {
                    bs.setText("?");
                }
                bs.addActionListener(se -> {
                    sistema.enviarMensaje(otro, r, "STICKER");
                    clienteSocket.enviar(new red.EventoSocket(
                            red.EventoSocket.Tipo.NUEVO_MENSAJE,
                            sistema.getUsuarioActual().getUsername(), otro));
                    lastMessageCount = 0;
                    lastMessageSignature = ""; // ← agregar
                    refrescarMensajes(msgPanel, otro);
                    sd.dispose();
                });
                grid.add(bs);
            }
            JScrollPane sp = styledScroll(grid);
            sp.setPreferredSize(new Dimension(400, 380));
            sp.getVerticalScrollBar().setUnitIncrement(30);
            sd.add(sp, BorderLayout.CENTER);
            sd.setVisible(true);
        });

        lastMessageCount = 0;
        lastMessageSignature = "";
        refrescarMensajes(msgPanel, otro);
        chatTimer = new Timer(2000, ev -> refrescarMensajes(msgPanel, otro));
        chatTimer.start();
        chatPanel.revalidate();
        chatPanel.repaint();
    }

    private void refrescarMensajes(JPanel panel, String otro) {
        sistema.marcarComoLeido(otro);
        ArrayList<Mensaje> hist = sistema.getConversacion(otro);
        String firma = hist.stream()
                .map(m -> m.getEmisor() + m.getHoraFormateada() + m.getEstado())
                .collect(java.util.stream.Collectors.joining());

        if (firma.equals(lastMessageSignature)) {
            return;
        }
        lastMessageSignature = firma;
        lastMessageCount = hist.size();
        JScrollPane scrollP = (JScrollPane) SwingUtilities.getAncestorOfClass(JScrollPane.class, panel);
        JScrollBar vbar = scrollP != null ? scrollP.getVerticalScrollBar() : null;
        int curVal = vbar != null ? vbar.getValue() : 0;
        boolean atBottom = vbar == null || vbar.getValue() >= vbar.getMaximum() - 200;
        panel.removeAll();

        for (Mensaje m : hist) {
            boolean mio = m.getEmisor().equals(sistema.getUsuarioActual().getUsername());
            JPanel row = new JPanel(new BorderLayout());
            row.setOpaque(false);
            row.setBorder(BorderFactory.createEmptyBorder(1, 10, 1, 10));
            JPanel bubble = new JPanel(new BorderLayout());
            boolean esStk = m instanceof instagram.MensajeSticker;
            boolean isMioTxt = mio && !esStk && m.getContenido() != null && !m.getContenido().startsWith("SHARE|");
            Color txtColor = isMioTxt ? Color.WHITE : C_TEXT;

            if (esStk) {
                JLabel img = new JLabel();
                img.setHorizontalAlignment(SwingConstants.CENTER);
                try {
                    File f = new File(m.getContenido());
                    if (f.exists()) {
                        img.setIcon(new ImageIcon(new ImageIcon(m.getContenido()).getImage().getScaledInstance(96, 96, Image.SCALE_SMOOTH)));
                    } else {
                        img.setText("[Sticker]");
                    }
                } catch (Exception ignored) {
                    img.setText("[Sticker]");
                }
                bubble.add(img, BorderLayout.CENTER);
                bubble.setOpaque(false);
                bubble.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
            } else {
                String texto = m.getContenido();
                if (texto.startsWith("SHARE|")) {
                    String[] da = texto.split("\\|", 4);
                    String ap = da.length > 1 ? da[1] : "?";
                    String ri = da.length > 2 ? da[2] : "";
                    String ca = da.length > 3 ? da[3] : "";

                    JPanel card = new JPanel(new BorderLayout(0, 0)) {
                        @Override
                        protected void paintComponent(Graphics g) {
                            Graphics2D g2 = (Graphics2D) g.create();
                            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                            g2.setColor(C_WHITE);
                            g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 12, 12));
                            g2.setColor(C_BORDER);
                            g2.setStroke(new BasicStroke(1f));
                            g2.draw(new RoundRectangle2D.Double(0.5, 0.5, getWidth() - 1, getHeight() - 1, 12, 12));
                            g2.dispose();
                        }
                    };
                    card.setOpaque(false);
                    card.setPreferredSize(new Dimension(230, 210));
                    card.setMinimumSize(new Dimension(230, 210));
                    card.setMaximumSize(new Dimension(230, 210));
                    card.setCursor(new Cursor(Cursor.HAND_CURSOR));

                    // ── imagen del post ──
                    JLabel iL2 = new JLabel();
                    iL2.setHorizontalAlignment(SwingConstants.CENTER);
                    iL2.setBackground(new Color(239, 239, 239));
                    iL2.setOpaque(true);
                    iL2.setPreferredSize(new Dimension(230, 150));
                    iL2.setMinimumSize(new Dimension(230, 150));
                    iL2.setMaximumSize(new Dimension(230, 150));
                    try {
                        File iF = new File(ri);
                        if (iF.exists()) {
                            ImageIcon cr = crearImagenCropeada(ri, 230, 150);
                            if (cr != null) {
                                iL2.setIcon(cr);
                            }
                        } else {
                            iL2.setText("Sin imagen");
                            iL2.setForeground(C_TEXT_LIGHT);
                        }
                    } catch (Exception ignored) {
                    }
                    card.add(iL2, BorderLayout.NORTH);

                    // ── info: avatar + username ──
                    JPanel inf = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 6));
                    inf.setOpaque(false);
                    inf.add(new JLabel(cargarFotoCircular(ap, 24)));
                    JLabel la = new JLabel(ap);
                    la.setFont(F_BOLD);
                    la.setForeground(C_TEXT);
                    inf.add(la);
                    if (!ca.isEmpty()) {
                        String tr = ca.length() > 50 ? ca.substring(0, 47) + "…" : ca;
                        JLabel lc2 = new JLabel("<html><font face='Arial' size='2' color='gray'>" + tr + "</font></html>");
                        lc2.setBorder(BorderFactory.createEmptyBorder(0, 8, 4, 8));
                        JPanel capRow = new JPanel(new BorderLayout());
                        capRow.setOpaque(false);
                        capRow.add(lc2, BorderLayout.CENTER);
                        JPanel bottom = new JPanel(new BorderLayout());
                        bottom.setOpaque(false);
                        bottom.add(inf, BorderLayout.NORTH);
                        bottom.add(capRow, BorderLayout.CENTER);
                        card.add(bottom, BorderLayout.CENTER);
                    } else {
                        card.add(inf, BorderLayout.CENTER);
                    }

                    // click → ver post ampliado
                    String rf = ri;
                    card.addMouseListener(click(() -> {
                        // Buscar la publicacion real para abrir con abrirDetallePost()
                        Publicacion postReal = null;
                        for (Publicacion p : sistema.getPublicacionesDeUsuario(ap)) {
                            if (p.getRutaImagen() != null && p.getRutaImagen().equals(rf)) {
                                postReal = p;
                                break;
                            }
                        }
                        if (postReal != null) {
                            abrirDetallePost(postReal); // reutiliza la vista exacta del feed
                        } else {
                            // Fallback si no encuentra el post (fue eliminado)
                            InstaDialog.showMessage(VentanaPrincipal.this,
                                    "Este post ya no está disponible.", true);
                        }
                    }));

                    bubble.add(card, BorderLayout.CENTER);
                    bubble.setOpaque(false);
                } else {
                    JTextArea ta = new JTextArea(texto);
                    ta.setLineWrap(true);
                    ta.setWrapStyleWord(true);
                    ta.setFont(F_REGULAR);
                    ta.setEditable(false);
                    ta.setOpaque(false);
                    ta.setForeground(txtColor);

                    // Calcular ancho real del texto
                    FontMetrics fm = ta.getFontMetrics(F_REGULAR);
                    int textWidth = fm.stringWidth(texto);
                    int padding = 28; // padding horizontal total

                    // Ancho de la burbuja: si el texto es corto cabe en 1 línea,
                    // si es largo se expande hasta 220px máximo
                    int bubbleW = Math.max(60, Math.min(220, textWidth + padding));

                    // Si el texto es muy corto (≤ 15 chars) forzar ancho mínimo ajustado
                    if (texto.length() <= 15) {
                        bubbleW = Math.min(textWidth + padding + 16, 220);
                    }

                    ta.setSize(new Dimension(bubbleW, Integer.MAX_VALUE));
                    Dimension preferred = ta.getPreferredSize();

                    // El wrapper toma el ancho calculado, no MAX
                    bubble.add(ta, BorderLayout.CENTER);
                    bubble.setPreferredSize(new Dimension(bubbleW + padding, preferred.height + 18));
                    bubble.setMaximumSize(new Dimension(bubbleW + padding, preferred.height + 18));
                }
            }

            boolean isTxt = !esStk && (m.getContenido() == null || !m.getContenido().startsWith("SHARE|"));
            if (mio) {
                if (esStk) {
                    row.add(bubble, BorderLayout.EAST);
                } else {
                    Color bc = isTxt ? C_BLUE : new Color(0, 0, 0, 0);
                    JPanel wr = new JPanel(new BorderLayout()) {
                        @Override
                        protected void paintComponent(Graphics g) {
                            if (!isTxt) {
                                return;
                            }
                            Graphics2D g2 = (Graphics2D) g.create();
                            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                            g2.setColor(bc);
                            g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 22, 22));
                            g2.dispose();
                        }
                    };
                    wr.setOpaque(false);
                    bubble.setOpaque(false);
                    bubble.setBorder(BorderFactory.createEmptyBorder(9, 14, 9, 14));
                    wr.add(bubble, BorderLayout.CENTER);
                    wr.setMaximumSize(new Dimension(260, Integer.MAX_VALUE));
                    wr.setPreferredSize(null);
                    row.add(wr, BorderLayout.EAST);
                }
            } else {
                if (esStk) {
                    row.add(bubble, BorderLayout.WEST);
                } else {
                    JPanel wr = new JPanel(new BorderLayout()) {
                        @Override
                        protected void paintComponent(Graphics g) {
                            if (!isTxt) {
                                return;
                            }
                            Graphics2D g2 = (Graphics2D) g.create();
                            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                            g2.setColor(C_WHITE);
                            g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 22, 22));
                            g2.setColor(C_BORDER);
                            g2.setStroke(new BasicStroke(1f));
                            g2.draw(new RoundRectangle2D.Double(0.5, 0.5, getWidth() - 1, getHeight() - 1, 22, 22));
                            g2.dispose();
                        }
                    };
                    wr.setOpaque(false);
                    bubble.setOpaque(false);
                    bubble.setBorder(BorderFactory.createEmptyBorder(9, 14, 9, 14));
                    wr.add(bubble, BorderLayout.CENTER);
                    wr.setMaximumSize(new Dimension(260, Integer.MAX_VALUE));
                    wr.setPreferredSize(null);
                    row.add(wr, BorderLayout.WEST);
                }
            }

            String estadoTxt = "";
            if (mio) {
                estadoTxt = "LEIDO".equals(m.getEstado()) ? "  ·  Visto" : "  ·  Enviado";
            }

            JLabel ts = new JLabel(m.getHoraFormateada() + estadoTxt);
            ts.setFont(new Font("Arial", Font.PLAIN, 9));
            ts.setForeground("LEIDO".equals(m.getEstado()) && mio
                    ? new Color(0, 149, 246)
                    : C_TEXT_LIGHT);
            ts.setBorder(BorderFactory.createEmptyBorder(1, 16, 5, 16));

            JPanel tsRow = new JPanel(new BorderLayout());
            tsRow.setOpaque(false);
            tsRow.add(ts, mio ? BorderLayout.EAST : BorderLayout.WEST);

            panel.add(row);
            panel.add(tsRow);
        }

        panel.revalidate();
        panel.repaint();
        SwingUtilities.invokeLater(() -> {
            if (vbar != null) {
                vbar.setValue(atBottom ? vbar.getMaximum() : curVal);
            }
        });
    }

    // ════════════════════════════════════════════════════════════
    //  VISTA 7 — NOTIFICACIONES
    // ════════════════════════════════════════════════════════════
    private void cargarVistaNotificaciones() {
        limpiarOverlaysActivos();
        vistaActual = "Notifications";
        if (chatTimer != null) {
            chatTimer.stop();
        }
        getContentPane().removeAll();
        setLayout(new BorderLayout());
        add(buildSidebar(), BorderLayout.WEST);
        JPanel main = new JPanel(new BorderLayout());
        main.setBackground(C_BG);
        main.setBorder(BorderFactory.createEmptyBorder(40, 50, 40, 50));
        JLabel title = new JLabel("Actividad");
        title.setFont(F_H1);
        title.setForeground(C_TEXT);
        title.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));
        main.add(title, BorderLayout.NORTH);
        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(C_BG);
        main.add(styledScroll(content), BorderLayout.CENTER);
        add(main, BorderLayout.CENTER);
        boolean hay = false;

        ArrayList<String> solic = sistema.getSolicitudes();
        if (!solic.isEmpty()) {
            hay = true;
            content.add(sectionTitle("Solicitudes de seguimiento"));
            for (String u : solic) {
                content.add(buildSolicitudRow(u));
                content.add(Box.createVerticalStrut(6));
            }
            content.add(Box.createVerticalStrut(24));
        }

        ArrayList<String> notifs = sistema.getNotificacionesGenerales();
        if (!notifs.isEmpty()) {
            hay = true;
            content.add(sectionTitle("Hoy"));
            for (String line : notifs) {
                String[] d = line.split("\\|");
                if (d.length < 2) {
                    continue;
                }
                String tipo = d[0], quien = d[1];
                JPanel row = new JPanel(new BorderLayout(10, 0));
                row.setBackground(C_WHITE);
                row.setMaximumSize(new Dimension(650, 60));
                row.setBorder(BorderFactory.createCompoundBorder(new LineBorder(C_BORDER, 1), BorderFactory.createEmptyBorder(10, 14, 10, 14)));
                row.add(new JLabel(cargarFotoCircular(quien, 36)), BorderLayout.WEST);
                JLabel txt = new JLabel();
                txt.setFont(F_REGULAR);
                txt.setForeground(C_TEXT);
                txt.setCursor(new Cursor(Cursor.HAND_CURSOR));
                txt.addMouseListener(click(() -> cargarVistaPerfil(quien)));
                if ("SEGUIDOR".equals(tipo)) {
                    txt.setText("<html><b>" + quien + "</b> empezó a seguirte.</html>");
                } else if ("MENCION".equals(tipo)) {
                    txt.setText("<html><b>" + quien + "</b> te mencionó.</html>");
                }
                row.add(txt, BorderLayout.CENTER);
                content.add(row);
                content.add(Box.createVerticalStrut(6));
            }
            content.add(Box.createVerticalStrut(20));
        }

        ArrayList<String> likes = sistema.getNotificacionesLikes();
        if (!likes.isEmpty()) {
            hay = true;
            content.add(sectionTitle("Me gusta en tus publicaciones"));
            for (String linea : likes) {
                String[] d = linea.split("\\|");
                if (d.length < 3) {
                    continue;
                }
                String fecha = d[1], quien = d[2], ri = d.length >= 4 ? d[3] : "";
                JPanel row = new JPanel(new BorderLayout(10, 0));
                row.setBackground(C_WHITE);
                row.setMaximumSize(new Dimension(650, 64));
                row.setBorder(BorderFactory.createCompoundBorder(new LineBorder(C_BORDER, 1), BorderFactory.createEmptyBorder(10, 14, 10, 14)));
                row.add(new JLabel(cargarFotoCircular(quien, 36)), BorderLayout.WEST);
                JLabel txt = new JLabel("<html><b>" + quien + "</b> le dio me gusta el " + fecha + "</html>");
                txt.setFont(F_REGULAR);
                txt.setForeground(C_TEXT);
                txt.setCursor(new Cursor(Cursor.HAND_CURSOR));
                txt.addMouseListener(click(() -> cargarVistaPerfil(quien)));
                row.add(txt, BorderLayout.CENTER);
                if (!ri.isEmpty() && new File(ri).exists()) {
                    try {
                        JLabel th = new JLabel();
                        th.setIcon(new ImageIcon(new ImageIcon(ri).getImage().getScaledInstance(44, 44, Image.SCALE_SMOOTH)));
                        th.setBorder(new LineBorder(C_BORDER, 1));
                        row.add(th, BorderLayout.EAST);
                    } catch (Exception ignored) {
                    }
                }
                content.add(row);
                content.add(Box.createVerticalStrut(6));
            }
        }

        ArrayList<Publicacion> menciones = sistema.getMenciones();
        if (!menciones.isEmpty()) {
            hay = true;
            content.add(Box.createVerticalStrut(8));
            content.add(sectionTitle("Publicaciones donde te mencionaron"));
            for (Publicacion p : menciones) {
                JPanel row = new JPanel(new BorderLayout(10, 0));
                row.setBackground(C_WHITE);
                row.setMaximumSize(new Dimension(650, 70));
                row.setBorder(BorderFactory.createCompoundBorder(new LineBorder(C_BORDER, 1), BorderFactory.createEmptyBorder(10, 14, 10, 14)));
                row.setCursor(new Cursor(Cursor.HAND_CURSOR));
                row.addMouseListener(click(() -> cargarVistaPerfil(p.getAutor())));
                row.add(new JLabel(cargarFotoCircular(p.getAutor(), 36)), BorderLayout.WEST);
                String frag = p.getContenido().length() > 60 ? p.getContenido().substring(0, 57) + "…" : p.getContenido();
                JPanel tp = new JPanel();
                tp.setLayout(new BoxLayout(tp, BoxLayout.Y_AXIS));
                tp.setBackground(C_WHITE);
                JLabel la = new JLabel("<html><b>" + p.getAutor() + "</b> te mencionó</html>");
                la.setFont(F_REGULAR);
                la.setForeground(C_TEXT);
                JLabel lf = new JLabel("<html><font color='gray'>" + frag + "</font></html>");
                lf.setFont(F_SMALL);
                tp.add(la);
                tp.add(lf);
                row.add(tp, BorderLayout.CENTER);
                if (p.getRutaImagen() != null && !p.getRutaImagen().isEmpty() && new File(p.getRutaImagen()).exists()) {
                    try {
                        JLabel th = new JLabel(new ImageIcon(new ImageIcon(p.getRutaImagen()).getImage().getScaledInstance(44, 44, Image.SCALE_SMOOTH)));
                        th.setBorder(new LineBorder(C_BORDER, 1));
                        row.add(th, BorderLayout.EAST);
                    } catch (Exception ignored) {
                    }
                }
                content.add(row);
                content.add(Box.createVerticalStrut(6));
            }
        }

        if (!hay) {
            JLabel n = emptyLabel("No hay notificaciones nuevas.");
            n.setAlignmentX(Component.CENTER_ALIGNMENT);
            content.add(Box.createVerticalStrut(80));
            content.add(n);
        }
        sistema.marcarNotificacionesVistas();
        revalidate();
        repaint();
    }

    private JLabel sectionTitle(String text) {
        JLabel l = new JLabel(text);
        l.setFont(F_BOLD);
        l.setForeground(C_TEXT);
        l.setBorder(BorderFactory.createEmptyBorder(0, 0, 8, 0));
        return l;
    }

    private JPanel buildSolicitudRow(String username) {
        JPanel p = new JPanel(new BorderLayout(10, 0));
        p.setBackground(C_WHITE);
        p.setMaximumSize(new Dimension(650, 62));
        p.setBorder(BorderFactory.createCompoundBorder(new LineBorder(C_BORDER, 1), BorderFactory.createEmptyBorder(10, 14, 10, 14)));
        p.add(new JLabel(cargarFotoCircular(username, 36)), BorderLayout.WEST);
        JLabel lbl = new JLabel("<html><b>" + username + "</b> quiere seguirte.</html>");
        lbl.setFont(F_REGULAR);
        lbl.setForeground(C_TEXT);
        lbl.setCursor(new Cursor(Cursor.HAND_CURSOR));
        lbl.addMouseListener(click(() -> cargarVistaPerfil(username)));
        p.add(lbl, BorderLayout.CENTER);
        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btns.setBackground(C_WHITE);
        JButton ok = buildPrimaryBtn("Confirmar");
        ok.setPreferredSize(new Dimension(100, 30));
        JButton no = buildSecondaryBtn("Eliminar");
        no.setPreferredSize(new Dimension(90, 30));
        ok.addActionListener(e -> {
            sistema.aceptarSolicitud(username);
            cargarVistaNotificaciones();
        });
        no.addActionListener(e -> {
            sistema.rechazarSolicitud(username);
            cargarVistaNotificaciones();
        });
        btns.add(ok);
        btns.add(no);
        p.add(btns, BorderLayout.EAST);
        return p;
    }

    private void abrirDialogoEditarPerfil() {
        JPanel glass = mostrarOverlay();
        JDialog d = new JDialog(this, "Editar perfil", true);
        d.setUndecorated(true);
        d.setSize(520, 450);
        d.setLocationRelativeTo(this);
        d.setLayout(new BorderLayout());
        d.getContentPane().setBackground(C_WHITE);
        d.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                quitarOverlay(glass);
            }
        });
        d.add(buildDialogHeader("Editar perfil", d), BorderLayout.NORTH);

        JPanel body = new JPanel();
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setBackground(C_WHITE);
        body.setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));

        JLabel lN = new JLabel("Nombre completo");
        lN.setFont(F_SMALL);
        lN.setForeground(C_TEXT_LIGHT);
        JTextField tN = buildField(sistema.getUsuarioActual().getNombreCompleto());
        tN.setText(sistema.getUsuarioActual().getNombreCompleto());
        tN.setForeground(C_TEXT);
        JLabel lP = new JLabel("Nueva contraseña (vacío = no cambiar)");
        lP.setFont(F_SMALL);
        lP.setForeground(C_TEXT_LIGHT);
        JPasswordField tP = new JPasswordField();
        tP.setBackground(C_FIELD);
        tP.setFont(F_REGULAR);
        tP.setBorder(BorderFactory.createCompoundBorder(new LineBorder(C_BORDER, 1, true), BorderFactory.createEmptyBorder(9, 12, 9, 12)));

        JButton bF = buildSecondaryBtn("Cambiar foto de perfil");
        final String[] nF = {null};
        bF.addActionListener(e -> {
            JFileChooser fc = new JFileChooser();
            fc.setFileFilter(new FileNameExtensionFilter("Imágenes", "jpg", "png", "jpeg"));
            if (fc.showOpenDialog(d) == JFileChooser.APPROVE_OPTION) {
                String r = sistema.procesarImagenPerfil(fc.getSelectedFile(),
                        sistema.getUsuarioActual().getUsername(), "profile_" + System.currentTimeMillis());
                if (r != null) {
                    nF[0] = r;
                    bF.setText("✓ Foto seleccionada");
                }
            }
        });

        body.add(lN);
        body.add(Box.createVerticalStrut(4));
        body.add(tN);
        body.add(Box.createVerticalStrut(14));
        body.add(lP);
        body.add(Box.createVerticalStrut(4));
        body.add(tP);
        body.add(Box.createVerticalStrut(16));
        body.add(bF);
        d.add(body, BorderLayout.CENTER);

        JPanel ft = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 12));
        ft.setBackground(C_WHITE);
        ft.setBorder(new MatteBorder(1, 0, 0, 0, C_BORDER));
        JButton bC = buildSecondaryBtn("Cancelar");
        JButton bS = buildPrimaryBtn("Guardar");
        ft.add(bC);
        ft.add(bS);
        d.add(ft, BorderLayout.SOUTH);

        bC.addActionListener(e -> d.dispose());
        bS.addActionListener(e -> {
            String nom = tN.getText().trim();
            if (nom.isEmpty()) {
                InstaDialog.showMessage(d, "El nombre no puede estar vacío.", true);
                return;
            }
            String pass = new String(tP.getPassword()).trim();
            boolean ok = sistema.actualizarDatosUsuario(nom, pass);
            if (nF[0] != null) {
                sistema.actualizarFotoPerfil(sistema.getUsuarioActual().getUsername(), nF[0]);
            }
            if (ok) {
                clienteSocket.enviar(new red.EventoSocket(
                        red.EventoSocket.Tipo.ACTUALIZACION_PERFIL,
                        sistema.getUsuarioActual().getUsername(), null));
                d.dispose();
                cargarVistaPerfil(sistema.getUsuarioActual().getUsername());
            } else {
                InstaDialog.showMessage(d, "Error al guardar.", true);
            }
        });
        d.setVisible(true);
    }

    private void mostrarListaUsuarios(ArrayList<String> usuarios, String titulo, String tipo, String ownerUsername) {
        if (usuarios.isEmpty()) {
            InstaDialog.showMessage(this, "La lista está vacía.");
            return;
        }

        JPanel glass = mostrarOverlay();
        JDialog d = new JDialog(this, titulo, true);
        d.setUndecorated(true);
        d.setSize(360, 520);
        d.setLocationRelativeTo(this);
        d.setLayout(new BorderLayout());
        d.getContentPane().setBackground(C_WHITE);
        d.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                quitarOverlay(glass);
            }
        });
        d.add(buildDialogHeader(titulo, d), BorderLayout.NORTH);

        JPanel list = new JPanel();
        list.setLayout(new BoxLayout(list, BoxLayout.Y_AXIS));
        list.setBackground(C_WHITE);
        list.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        ArrayList<String> inv = new ArrayList<>(usuarios);
        Collections.reverse(inv);
        boolean esFollowers = "Followers".equals(tipo);

        for (String usr : inv) {
            Usuario uCheck = sistema.buscarUsuario(usr);
            if (uCheck != null && uCheck.getEstadoCuenta() == EstadoCuenta.DESACTIVADO) {
                continue;
            }
            JPanel row = new JPanel(new BorderLayout(10, 0));
            row.setBackground(C_WHITE);
            row.setMaximumSize(new Dimension(340, 62));
            row.setBorder(BorderFactory.createCompoundBorder(
                    new LineBorder(C_BORDER, 1, true),
                    BorderFactory.createEmptyBorder(10, 12, 10, 12)));

            JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
            left.setBackground(C_WHITE);
            left.add(new JLabel(cargarFotoCircular(usr, 36)));
            JLabel lbl = new JLabel(usr);
            lbl.setFont(F_BOLD);
            lbl.setForeground(C_TEXT);
            lbl.setCursor(new Cursor(Cursor.HAND_CURSOR));
            lbl.addMouseListener(click(() -> {
                d.dispose();
                cargarVistaPerfil(usr);
            }));
            left.add(lbl);
            left.add(badgeVerificado(usr));
            row.add(left, BorderLayout.WEST);

            JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
            rightPanel.setBackground(C_WHITE);
            boolean esMiPerfil = sistema.getUsuarioActual() != null
                    && sistema.getListaFollowers(sistema.getUsuarioActual().getUsername()).equals(usuarios)
                    || titulo.equals("Seguidores") && sistema.getUsuarioActual() != null;
            if (esFollowers && ownerUsername.equals(sistema.getUsuarioActual().getUsername())) {

                JButton btnElim = new JButton();
                if (iconsNormal.containsKey("Close")) {
                    btnElim.setIcon(iconsNormal.get("Close"));
                } else {
                    btnElim.setText("✕");
                    btnElim.setFont(F_SMALL);
                }
                btnElim.setToolTipText("Eliminar seguidor");
                btnElim.setBorderPainted(false);
                btnElim.setContentAreaFilled(false);
                btnElim.setFocusPainted(false);
                btnElim.setCursor(new Cursor(Cursor.HAND_CURSOR));
                btnElim.setForeground(C_ERROR);
                btnElim.addActionListener(e -> {
                    boolean yes = InstaDialog.showConfirm(d,
                            "¿Eliminar a " + usr + " de tus seguidores?", "Eliminar", true);
                    if (yes) {
                        sistema.eliminarSeguidor(usr);
                        d.dispose();
                        mostrarListaUsuarios(
                                sistema.getListaFollowers(sistema.getUsuarioActual().getUsername()),
                                "Seguidores", "Followers", sistema.getUsuarioActual().getUsername());
                        cargarVistaPerfil(sistema.getUsuarioActual().getUsername());
                    }
                });
                rightPanel.add(btnElim);
            } else {
                JButton btnVer = buildPrimaryBtn("Ver perfil");
                btnVer.setPreferredSize(new Dimension(100, 30));
                btnVer.addActionListener(e -> {
                    d.dispose();
                    cargarVistaPerfil(usr);
                });
                rightPanel.add(btnVer);
            }
            row.add(rightPanel, BorderLayout.EAST);

            row.addMouseListener(new MouseAdapter() {
                public void mouseEntered(MouseEvent e) {
                    row.setBackground(C_HOVER);
                    left.setBackground(C_HOVER);
                    rightPanel.setBackground(C_HOVER);
                }

                public void mouseExited(MouseEvent e) {
                    row.setBackground(C_WHITE);
                    left.setBackground(C_WHITE);
                    rightPanel.setBackground(C_WHITE);
                }
            });
            list.add(row);
            list.add(Box.createVerticalStrut(6));
        }

        d.add(styledScroll(list), BorderLayout.CENTER);
        d.setVisible(true);
    }

    private void cargarVistaBusquedaHashtag(String hashtag) {
        limpiarOverlaysActivos();
        vistaActual = "Search";
        getContentPane().removeAll();
        setLayout(new BorderLayout());
        getContentPane().setBackground(C_BG);
        add(buildSidebar(), BorderLayout.WEST);
        JPanel main = new JPanel(new BorderLayout());
        main.setBackground(C_BG);
        main.setBorder(BorderFactory.createEmptyBorder(40, 40, 40, 40));
        JLabel title = new JLabel("Resultados: " + hashtag);
        title.setFont(F_H2);
        title.setForeground(C_TEXT);
        title.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));
        main.add(title, BorderLayout.NORTH);
        JPanel res = new JPanel();
        res.setLayout(new BoxLayout(res, BoxLayout.Y_AXIS));
        res.setBackground(C_BG);
        main.add(styledScroll(res), BorderLayout.CENTER);
        add(main, BorderLayout.CENTER);
        ArrayList<Publicacion> posts = sistema.buscarPorHashtag(hashtag);
        if (posts.isEmpty()) {
            res.add(emptyLabel("Sin publicaciones con " + hashtag));
        } else {
            for (Publicacion p : posts) {
                res.add(buildPost(p));
                res.add(Box.createVerticalStrut(4));
            }
        }
        revalidate();
        repaint();
    }

    // ════════════════════════════════════════════════════════════
    //  FOTOS CIRCULARES
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
                        if (bi != null) {
                            return circularIcon(new ImageIcon(bi), d);
                        }
                    } catch (Exception ignored) {
                    }
                }
            }
        }
        return avatarDefault(username, d);
    }

    private ImageIcon circularIcon(ImageIcon src, int d) {
        BufferedImage out = new BufferedImage(d, d, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = out.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setClip(new Ellipse2D.Double(0, 0, d, d));
        g2.drawImage(src.getImage().getScaledInstance(d, d, Image.SCALE_SMOOTH), 0, 0, null);
        g2.setClip(null);
        g2.setColor(C_BORDER);
        g2.setStroke(new BasicStroke(1f));
        g2.drawOval(0, 0, d - 1, d - 1);
        g2.dispose();
        return new ImageIcon(out);
    }

    private ImageIcon avatarDefault(String username, int d) {
        BufferedImage img = new BufferedImage(d, d, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = img.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(new Color(239, 239, 239));
        g2.fillOval(0, 0, d, d);
        g2.setColor(new Color(142, 142, 142));
        g2.setFont(new Font("Arial", Font.BOLD, d / 2));
        String l = username.isEmpty() ? "?" : username.substring(0, 1).toUpperCase();
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(l, (d - fm.stringWidth(l)) / 2, fm.getAscent() + (d - fm.getAscent()) / 2);
        g2.dispose();
        return new ImageIcon(img);
    }

    // ════════════════════════════════════════════════════════════
    //  HELPERS UI
    // ════════════════════════════════════════════════════════════
    private JTextField buildField(String ph) {
        JTextField t = new JTextField();
        t.setBackground(C_FIELD);
        t.setFont(F_REGULAR);
        t.setText(ph);
        t.setForeground(C_PLACEHOLDER);
        t.setBorder(BorderFactory.createCompoundBorder(new LineBorder(C_BORDER, 1, true), BorderFactory.createEmptyBorder(9, 12, 9, 12)));
        t.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent e) {
                if (t.getText().equals(ph)) {
                    t.setText("");
                    t.setForeground(C_TEXT);
                }
            }

            public void focusLost(FocusEvent e) {
                if (t.getText().isEmpty()) {
                    t.setText(ph);
                    t.setForeground(C_PLACEHOLDER);
                }
            }
        });
        return t;
    }

    private JPanel buildPassPanel() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(C_FIELD);
        p.setBorder(new LineBorder(C_BORDER, 1, true));
        JPasswordField pf = new JPasswordField();
        pf.setBackground(C_FIELD);
        pf.setFont(F_REGULAR);
        pf.setBorder(BorderFactory.createEmptyBorder(9, 12, 9, 6));
        pf.setText("Password");
        pf.setEchoChar((char) 0);
        pf.setForeground(C_PLACEHOLDER);
        JLabel eye = new JLabel();
        eye.setCursor(new Cursor(Cursor.HAND_CURSOR));
        eye.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 10));
        eye.setIcon(iconEyeClosed != null ? iconEyeClosed : null);
        if (iconEyeClosed == null) {
            eye.setText("ver");
        }
        eye.addMouseListener(click(() -> {
            if (new String(pf.getPassword()).equals("Password")) {
                return;
            }
            if (pf.getEchoChar() != 0) {
                pf.setEchoChar((char) 0);
                eye.setIcon(iconEyeOpen);
                if (iconEyeOpen == null) {
                    eye.setText("ocultar");
                }
            } else {
                pf.setEchoChar('\u25CF');
                eye.setIcon(iconEyeClosed);
                if (iconEyeClosed == null) {
                    eye.setText("ver");
                }
            }
        }));
        pf.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent e) {
                if (new String(pf.getPassword()).equals("Password")) {
                    pf.setText("");
                    pf.setForeground(C_TEXT);
                    pf.setEchoChar('●');
                }
            }

            public void focusLost(FocusEvent e) {
                if (pf.getPassword().length == 0) {
                    pf.setText("Password");
                    pf.setEchoChar((char) 0);
                    pf.setForeground(C_PLACEHOLDER);
                }
            }
        });
        p.add(pf, BorderLayout.CENTER);
        p.add(eye, BorderLayout.EAST);
        return p;
    }

    private JPanel buildFieldWithIcon(String ph) {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(C_FIELD);
        p.setBorder(new LineBorder(C_BORDER, 1, true));
        JTextField t = new JTextField();
        t.setBackground(C_FIELD);
        t.setFont(F_REGULAR);
        t.setText(ph);
        t.setForeground(C_PLACEHOLDER);
        t.setBorder(BorderFactory.createEmptyBorder(9, 12, 9, 6));
        JLabel ico = new JLabel();
        ico.setPreferredSize(new Dimension(28, 28));
        ico.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 6));
        t.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent e) {
                if (t.getText().equals(ph)) {
                    t.setText("");
                    t.setForeground(C_TEXT);
                }
            }

            public void focusLost(FocusEvent e) {
                if (t.getText().isEmpty()) {
                    t.setText(ph);
                    t.setForeground(C_PLACEHOLDER);
                }
            }
        });
        p.add(t, BorderLayout.CENTER);
        p.add(ico, BorderLayout.EAST);
        return p;
    }

    private JSpinner buildSpinner(int val) {
        JSpinner s = new JSpinner(new SpinnerNumberModel(val, 1, 100, 1));
        s.setFont(F_REGULAR);
        JComponent ed = s.getEditor();
        JFormattedTextField tf = ((JSpinner.DefaultEditor) ed).getTextField();
        tf.setBackground(C_FIELD);
        tf.setFont(F_REGULAR);
        tf.setBorder(null);
        ed.setBackground(C_FIELD);
        s.setBorder(new LineBorder(C_BORDER, 1, true));
        return s;
    }

    private JComboBox<String> buildCombo(String... items) {
        JComboBox<String> c = new JComboBox<>(items);
        c.setFont(F_REGULAR);
        c.setBackground(C_FIELD);
        return c;
    }

    private JButton buildPrimaryBtn(String text) {
        JButton b = new JButton(text);
        b.setFont(F_BOLD);
        b.setForeground(C_WHITE);
        b.setBackground(C_BLUE);
        b.setBorderPainted(false);
        b.setFocusPainted(false);
        b.setOpaque(true);
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        b.setPreferredSize(new Dimension(b.getPreferredSize().width, 36));
        return b;
    }

    private JButton buildSecondaryBtn(String text) {
        JButton b = new JButton(text);
        b.setFont(F_REGULAR);
        b.setForeground(C_TEXT);
        b.setBackground(new Color(239, 239, 239));
        b.setBorderPainted(false);
        b.setFocusPainted(false);
        b.setOpaque(true);
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
        JLabel l = new JLabel();
        l.setFont(F_SMALL);
        l.setForeground(C_ERROR);
        return l;
    }

    private JLabel linkLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(F_SMALL);
        l.setForeground(C_BLUE);
        l.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return l;
    }

    private JLabel emptyLabel(String text) {
        JLabel l = new JLabel(text, SwingConstants.CENTER);
        l.setFont(F_REGULAR);
        l.setForeground(C_TEXT_LIGHT);
        return l;
    }

    private JLabel fieldLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(F_SMALL);
        l.setForeground(C_TEXT_LIGHT);
        return l;
    }

    private void limpiarOverlaysActivos() {
        Component glass = getGlassPane();
        if (glass != null && glass.isVisible()) {
            glass.setVisible(false);
        }
        JPanel blank = new JPanel();
        blank.setOpaque(false);
        setGlassPane(blank);
        repaint();
    }

    private JPanel roundPanel(Color bg) {
        return new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(bg);
                g2.fillRect(0, 0, getWidth(), getHeight());
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
        sp.getVerticalScrollBar().setUnitIncrement(60);
        sp.getVerticalScrollBar().setBlockIncrement(220);
        sp.getVerticalScrollBar().setUI(new BasicScrollBarUI() {
            protected void configureScrollBarColors() {
                thumbColor = new Color(219, 219, 219);
                trackColor = C_WHITE;
            }

            protected JButton createDecreaseButton(int o) {
                return zBtn();
            }

            protected JButton createIncreaseButton(int o) {
                return zBtn();
            }

            private JButton zBtn() {
                JButton b = new JButton();
                b.setPreferredSize(new Dimension(0, 0));
                return b;
            }
        });
        if (scrollToTop) {
            SwingUtilities.invokeLater(() -> sp.getVerticalScrollBar().setValue(0));
        }
        return sp;
    }

    private JPanel buildDialogHeader(String title, JDialog d) {
        JPanel h = new JPanel(new BorderLayout());
        h.setBackground(C_WHITE);
        h.setBorder(BorderFactory.createCompoundBorder(new MatteBorder(0, 0, 1, 0, C_BORDER), BorderFactory.createEmptyBorder(14, 16, 14, 16)));
        JLabel lbl = new JLabel(title);
        lbl.setFont(F_BOLD);
        lbl.setForeground(C_TEXT);
        JButton x = new JButton();
        if (iconsNormal.containsKey("Close")) {
            x.setIcon(iconsNormal.get("Close"));
        } else {
            x.setText("x");
            x.setFont(F_SMALL);
            x.setForeground(C_TEXT_LIGHT);
        }
        x.setBorderPainted(false);
        x.setContentAreaFilled(false);
        x.setFocusPainted(false);
        x.setCursor(new Cursor(Cursor.HAND_CURSOR));
        x.addActionListener(e -> d.dispose());
        h.add(lbl, BorderLayout.WEST);
        h.add(x, BorderLayout.EAST);
        return h;
    }

    private void redBorder(JComponent c) {
        c.setBorder(new LineBorder(C_ERROR, 1, true));
    }

    private void resetFieldBorder(JTextField t) {
        t.setBorder(BorderFactory.createCompoundBorder(new LineBorder(C_BORDER, 1, true), BorderFactory.createEmptyBorder(9, 12, 9, 12)));
    }

    private void resetPanelBorder(JPanel p) {
        p.setBorder(new LineBorder(C_BORDER, 1, true));
    }

    private void resetSpinnerBorder(JSpinner s) {
        s.setBorder(new LineBorder(C_BORDER, 1, true));
    }

    private MouseAdapter click(Runnable r) {
        return new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                r.run();
            }
        };
    }

    private DocumentListener simpleDocListener(Runnable r) {
        return new DocumentListener() {
            public void insertUpdate(DocumentEvent e) {
                r.run();
            }

            public void removeUpdate(DocumentEvent e) {
                r.run();
            }

            public void changedUpdate(DocumentEvent e) {
                r.run();
            }
        };
    }

    private String toHtml(String texto) {
        if (texto == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (String w : texto.split(" ")) {
            if (w.startsWith("#") || w.startsWith("@")) {
                sb.append("<a href='").append(w).append("' style='color:#3d9be9;text-decoration:none;'>").append(w).append("</a> ");
            } else {
                sb.append(w).append(" ");
            }
        }
        return sb.toString();
    }

    private String copiarFotoPorDefecto(String username) {
        try {
            java.io.InputStream is = getClass().getResourceAsStream("/images/default_profile.png");
            if (is == null) {
                return "";
            }
            String rc = "INSTA_RAIZ/" + username + "/imagenes";
            new File(rc).mkdirs();
            File dest = new File(rc + "/profile_default.png");
            java.nio.file.Files.copy(is, dest.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            is.close();
            return dest.getAbsolutePath();
        } catch (Exception e) {
            return "";
        }
    }
    // ── Glasspane helper ────────────────────────────────────────

    private JPanel mostrarOverlay() {
        JPanel glass = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(new Color(0, 0, 0, 150));
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
            }
        };
        glass.setOpaque(false);
        glass.addMouseListener(new MouseAdapter() {
        }); // bloquea clicks
        setGlassPane(glass);
        glass.setVisible(true);
        return glass;
    }

    private void quitarOverlay(JPanel glass) {
        glass.setVisible(false);
        JPanel blank = new JPanel();
        blank.setOpaque(false);
        setGlassPane(blank);
        repaint();
    }

    private void conectarSocket() {
        clienteSocket.conectar(evento -> {
            switch (evento.getTipo()) {
                case NUEVO_POST:
                    // Refrescar feed si estamos en Home
                    if ("Home".equals(vistaActual)) {
                        cargarVistaFeed();
                    }
                    break;
                case NUEVO_MENSAJE:
                    // Solo refrescar si el destino somos nosotros
                    if (sistema.getUsuarioActual() != null
                            && sistema.getUsuarioActual().getUsername().equals(evento.getUsuarioDestino())) {
                        if ("Messages".equals(vistaActual)) {
                            // El chatTimer ya refresca automáticamente
                            lastMessageSignature = "";
                        }
                        // Actualizar punto azul en sidebar
                        if (feedTimer != null) {
                            buildSidebar();
                        }
                    }
                    break;
                case NUEVA_NOTIFICACION:
                    if (sistema.getUsuarioActual() != null
                            && sistema.getUsuarioActual().getUsername().equals(evento.getUsuarioDestino())) {
                        if ("Notifications".equals(vistaActual)) {
                            cargarVistaNotificaciones();
                        } else {
                            repaint(); // actualiza punto rojo en sidebar
                        }
                    }
                    break;
                case CAMBIO_SEGUIDOR:
                case ACTUALIZACION_PERFIL:
                    if ("Profile".equals(vistaActual) && sistema.getUsuarioActual() != null) {
                        cargarVistaPerfil(sistema.getUsuarioActual().getUsername());
                    }
                    break;
            }
        });
    }
}
