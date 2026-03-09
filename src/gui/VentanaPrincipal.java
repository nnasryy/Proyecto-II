package gui;

import enums.TipoCuenta;
import instagram.Mensaje;
import instagram.Publicacion;
import instagram.Sistema;
import instagram.Usuario;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.plaf.basic.BasicScrollBarUI;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;

public class VentanaPrincipal extends JFrame {

    private Sistema sistema;

    // --- COLORES ---
    private final Color COLOR_FONDO = Color.WHITE;
    private final Color COLOR_BOTTON = new Color(64, 155, 230);
    private final Color COLOR_DISABLED = new Color(42, 107, 161);
    private final Color COLOR_TEXT_DISABLED = new Color(200, 202, 204);
    private final Color COLOR_FIELD = new Color(242, 247, 247);
    private final Color COLOR_FONT = new Color(143, 140, 140);
    private final Color COLOR_PLACEHOLDER = new Color(180, 180, 180);
    private final Color COLOR_ERROR = new Color(250, 89, 95);

    // ICONOS
    private ImageIcon iconEyeClosed;
    private ImageIcon iconEyeOpen;
    private ImageIcon iconCheck;

    public VentanaPrincipal(Sistema sistema) {
        this.sistema = sistema;
        cargarIconos();
        configurarVentana();
        inicializarComponentesLogin();
    }

    private void cargarIconos() {
        try {
            ImageIcon rawClosed = new ImageIcon(getClass().getResource("/images/ojocerrado.png"));
            Image imgC = rawClosed.getImage().getScaledInstance(20, 20, Image.SCALE_SMOOTH);
            iconEyeClosed = new ImageIcon(imgC);

            ImageIcon rawOpen = new ImageIcon(getClass().getResource("/images/ojo.png"));
            Image imgO = rawOpen.getImage().getScaledInstance(20, 20, Image.SCALE_SMOOTH);
            iconEyeOpen = new ImageIcon(imgO);

            try {
                ImageIcon rawCheck = new ImageIcon(getClass().getResource("/images/check.png"));
                Image imgCh = rawCheck.getImage().getScaledInstance(20, 20, Image.SCALE_SMOOTH);
                iconCheck = new ImageIcon(imgCh);
            } catch (Exception e) {
                iconCheck = null;
            }
        } catch (Exception e) {
            System.out.println("Error cargando iconos: " + e.getMessage());
        }
    }

    private void configurarVentana() {
        setSize(1366, 768);
        setTitle("Instagram-Desktop");
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setResizable(false);
    }

    // ---------------------------------------------------------
    // VISTA 1: LOGIN (CORREGIDO)
    // ---------------------------------------------------------
    private void inicializarComponentesLogin() {
        getContentPane().removeAll();
        // IMPORTANTE: Restablecer layout null para que el posicionamiento absoluto funcione
        getContentPane().setLayout(null);
        getContentPane().setBackground(COLOR_FONDO);

        // --- LOGO ---
        JLabel lblLogo = new JLabel();
        try {
            ImageIcon icono = new ImageIcon(getClass().getResource("/images/instagramlogoblack.png"));
            Image img = icono.getImage().getScaledInstance(280, 158, Image.SCALE_SMOOTH);
            lblLogo.setIcon(new ImageIcon(img));
        } catch (Exception e) {
            lblLogo.setText("INSTAGRAM");
            lblLogo.setHorizontalAlignment(SwingConstants.CENTER);
            lblLogo.setFont(new Font("Arial", Font.BOLD, 30));
        }

        JLabel lblErrorUser = crearLabelError();
        JTextField txtUser = crearTextField("Username");

        JLabel lblErrorPass = crearLabelError();
        JPanel panelPass = crearPanelPassword();

        JButton btnLogin = new JButton("Iniciar Sesión");
        btnLogin.setFont(new Font("Arial", Font.BOLD, 14));
        btnLogin.setOpaque(true);
        btnLogin.setBorderPainted(false);
        btnLogin.setFocusPainted(false);
        btnLogin.setBackground(COLOR_DISABLED);
        btnLogin.setForeground(COLOR_TEXT_DISABLED);
        btnLogin.setEnabled(false);

        JLabel lblRegistro = new JLabel("¿Aún no tienes cuenta? Regístrate");
        lblRegistro.setForeground(COLOR_BOTTON);
        lblRegistro.setFont(new Font("Arial", Font.BOLD, 13));
        lblRegistro.setCursor(new Cursor(Cursor.HAND_CURSOR));
        lblRegistro.setSize(lblRegistro.getPreferredSize());

        // POSICIONAMIENTO
        int anchoInputs = 280;
        int altoInput = 36;
        int xInputs = (1366 - anchoInputs) / 2;
        int yInicio = 200;

        lblLogo.setBounds(xInputs, yInicio, anchoInputs, 158);

        int y = yInicio + 170;

        txtUser.setBounds(xInputs, y, anchoInputs, altoInput);
        y += 40;
        lblErrorUser.setBounds(xInputs, y, anchoInputs, 20);
        y += 25;
        panelPass.setBounds(xInputs, y, anchoInputs, altoInput);
        y += 45;
        lblErrorPass.setBounds(xInputs, y, anchoInputs, 20);
        y += 30;
        btnLogin.setBounds(xInputs, y, anchoInputs, altoInput);
        y += 50;

        int anchoTextoReg = lblRegistro.getPreferredSize().width;
        lblRegistro.setLocation(xInputs + (anchoInputs - anchoTextoReg) / 2, y);

        add(lblLogo);
        add(txtUser);
        add(lblErrorUser);
        add(panelPass);
        add(lblErrorPass);
        add(btnLogin);
        add(lblRegistro);

        // LÓGICA
        JPasswordField txtPass = (JPasswordField) panelPass.getComponent(0);

        DocumentListener validationListener = new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) { validar(); }

            @Override
            public void removeUpdate(DocumentEvent e) { validar(); }

            @Override
            public void changedUpdate(DocumentEvent e) { validar(); }

            private void validar() {
                String user = txtUser.getText();
                String pass = String.valueOf(txtPass.getPassword());

                boolean userValido = !user.equals("Username") && !user.isEmpty();
                boolean passValida = pass.length() >= 6 && !pass.equals("Password");

                if (userValido && passValida) {
                    btnLogin.setEnabled(true);
                    btnLogin.setBackground(COLOR_BOTTON);
                    btnLogin.setForeground(Color.WHITE);
                } else {
                    btnLogin.setEnabled(false);
                    btnLogin.setBackground(COLOR_DISABLED);
                    btnLogin.setForeground(COLOR_TEXT_DISABLED);
                }

                if (userValido) resetBorder(txtUser);
                if (passValida) resetBorder(panelPass);
                lblErrorUser.setText("");
                lblErrorPass.setText("");
            }
        };

        txtUser.getDocument().addDocumentListener(validationListener);
        txtPass.getDocument().addDocumentListener(validationListener);

        btnLogin.addActionListener(e -> {
            String user = txtUser.getText();
            String pass = String.valueOf(txtPass.getPassword());

            Usuario u = sistema.buscarUsuario(user);

            if (u == null) {
                lblErrorUser.setText("El usuario no existe.");
                ponerBordeRojo(txtUser);
                return;
            }

            boolean passCorrecta = sistema.login(user, pass);

            if (!passCorrecta) {
                lblErrorPass.setText("La contraseña es incorrecta.");
                ponerBordeRojo(panelPass);
            } else {
                cargarVistaFeed();
            }
        });

        lblRegistro.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                cargarVistaRegistro();
            }
        });

        revalidate();
        repaint();
    }

    // ---------------------------------------------------------
    // VISTA 2: REGISTRO
    // ---------------------------------------------------------
    private void cargarVistaRegistro() {
        getContentPane().removeAll();
        getContentPane().setLayout(null);
        getContentPane().setBackground(COLOR_FONDO);

        JLabel lblTitulo = new JLabel("Crear Cuenta");
        lblTitulo.setFont(new Font("Arial", Font.BOLD, 24));
        lblTitulo.setHorizontalAlignment(SwingConstants.CENTER);

        JLabel lblErrorNombre = crearLabelError();
        JTextField txtNombre = crearTextField("Nombre Completo");

        JLabel lblErrorUser = crearLabelError();
        JPanel panelUser = crearPanelConIconoDerecho("Username");

        JLabel lblErrorPass = crearLabelError();
        JPanel panelPass = crearPanelPassword();

        JLabel lblErrorEdad = crearLabelError();
        JSpinner spnEdad = crearSpinnerPersonalizado(18);

        JComboBox<String> cmbGenero = new JComboBox<>();
        cmbGenero.addItem("Masculino");
        cmbGenero.addItem("Femenino");
        estilizarComboBox(cmbGenero);

        JComboBox<String> cmbTipo = new JComboBox<>();
        cmbTipo.addItem("Pública");
        cmbTipo.addItem("Privada");
        estilizarComboBox(cmbTipo);

        JButton btnRegistrar = new JButton("Registrarse");
        btnRegistrar.setFont(new Font("Arial", Font.BOLD, 14));
        btnRegistrar.setOpaque(true);
        btnRegistrar.setBorderPainted(false);
        btnRegistrar.setFocusPainted(false);
        btnRegistrar.setBackground(COLOR_DISABLED);
        btnRegistrar.setForeground(COLOR_TEXT_DISABLED);
        btnRegistrar.setEnabled(false);

        JLabel btnVolver = new JLabel("¿Ya tienes cuenta? Volver");
        btnVolver.setForeground(COLOR_BOTTON);
        btnVolver.setFont(new Font("Arial", Font.BOLD, 12));
        btnVolver.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnVolver.setSize(btnVolver.getPreferredSize());

        // Layout Manual
        int anchoInputs = 320;
        int altoInput = 36;
        int xInputs = (1366 - anchoInputs) / 2;
        int y = 120;

        lblTitulo.setBounds(xInputs, y, anchoInputs, 40);
        y += 50;

        txtNombre.setBounds(xInputs, y, anchoInputs, altoInput);
        lblErrorNombre.setBounds(xInputs, y + altoInput + 2, anchoInputs, 20);
        y += 60;

        panelUser.setBounds(xInputs, y, anchoInputs, altoInput);
        lblErrorUser.setBounds(xInputs, y + altoInput + 2, anchoInputs, 20);
        y += 60;

        panelPass.setBounds(xInputs, y, anchoInputs, altoInput);
        lblErrorPass.setBounds(xInputs, y + altoInput + 2, anchoInputs, 20);
        y += 60;

        JLabel lblEdad = new JLabel("Edad:");
        lblEdad.setFont(new Font("Arial", Font.BOLD, 12));
        lblEdad.setBounds(xInputs, y, 100, 20);
        spnEdad.setBounds(xInputs, y + 20, 80, 30);
        lblErrorEdad.setBounds(xInputs, y + 52, 150, 15);

        JLabel lblGenero = new JLabel("Género:");
        lblGenero.setFont(new Font("Arial", Font.BOLD, 12));
        lblGenero.setBounds(xInputs + 160, y, 100, 20);
        cmbGenero.setBounds(xInputs + 160, y + 20, 140, 30);
        y += 75;

        JLabel lblTipo = new JLabel("Tipo Cuenta:");
        lblTipo.setFont(new Font("Arial", Font.BOLD, 12));
        lblTipo.setBounds(xInputs, y, 100, 20);
        cmbTipo.setBounds(xInputs, y + 20, anchoInputs, 30);
        y += 60;

        btnRegistrar.setBounds(xInputs, y, anchoInputs, altoInput);
        y += 50;

        int anchoTextoVolver = btnVolver.getPreferredSize().width;
        btnVolver.setLocation(xInputs + (anchoInputs - anchoTextoVolver) / 2, y);

        add(lblTitulo);
        add(txtNombre);
        add(lblErrorNombre);
        add(panelUser);
        add(lblErrorUser);
        add(panelPass);
        add(lblErrorPass);
        add(spnEdad);
        add(cmbGenero);
        add(lblEdad);
        add(lblGenero);
        add(lblErrorEdad);
        add(cmbTipo);
        add(lblTipo);
        add(btnRegistrar);
        add(btnVolver);

        // LÓGICA
        JTextField txtUser = (JTextField) panelUser.getComponent(0);
        JPasswordField txtPass = (JPasswordField) panelPass.getComponent(0);
        JLabel lblIconoCheck = (JLabel) panelUser.getComponent(1);

        DocumentListener valListener = new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) { validarTodo(); }

            @Override
            public void removeUpdate(DocumentEvent e) { validarTodo(); }

            @Override
            public void changedUpdate(DocumentEvent e) { validarTodo(); }

            private void validarTodo() {
                boolean todoOK = true;

                String nombre = txtNombre.getText();
                if (nombre.equals("Nombre Completo") || nombre.isEmpty()) todoOK = false;

                String user = txtUser.getText();
                lblErrorUser.setText("");
                resetBorder(panelUser);
                lblIconoCheck.setIcon(null);

                if (user.isEmpty() || user.equals("Username")) {
                    todoOK = false;
                } else if (user.length() < 3) {
                    lblErrorUser.setText("Debe superar los 3 caracteres.");
                    ponerBordeRojo(panelUser);
                    todoOK = false;
                } else if (sistema.existeUsername(user)) {
                    lblErrorUser.setText("El username ya existe.");
                    ponerBordeRojo(panelUser);
                    todoOK = false;
                } else {
                    if (iconCheck != null) lblIconoCheck.setIcon(iconCheck);
                    else lblIconoCheck.setText("✓");
                }

                String pass = String.valueOf(txtPass.getPassword());
                lblErrorPass.setText("");
                resetBorder(panelPass);

                if (pass.isEmpty() || pass.equals("Password")) {
                    todoOK = false;
                } else if (pass.length() < 6) {
                    lblErrorPass.setText("Ingresa al menos 6 caracteres.");
                    ponerBordeRojo(panelPass);
                    todoOK = false;
                }

                int edad = (int) spnEdad.getValue();
                lblErrorEdad.setText("");
                resetBorder(spnEdad);

                if (edad < 18) {
                    lblErrorEdad.setText("Debes ser mayor de 18 años.");
                    ponerBordeRojo(spnEdad);
                    todoOK = false;
                }

                if (todoOK) {
                    btnRegistrar.setEnabled(true);
                    btnRegistrar.setBackground(COLOR_BOTTON);
                    btnRegistrar.setForeground(Color.WHITE);
                } else {
                    btnRegistrar.setEnabled(false);
                    btnRegistrar.setBackground(COLOR_DISABLED);
                    btnRegistrar.setForeground(COLOR_TEXT_DISABLED);
                }
            }
        };

        txtNombre.getDocument().addDocumentListener(valListener);
        txtUser.getDocument().addDocumentListener(valListener);
        txtPass.getDocument().addDocumentListener(valListener);
        spnEdad.addChangeListener(e -> valListener.changedUpdate(null));

        btnVolver.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                inicializarComponentesLogin();
            }
        });

        btnRegistrar.addActionListener(e -> {
            String nombre = txtNombre.getText();
            String user = txtUser.getText();
            String pass = String.valueOf(txtPass.getPassword());
            int edad = (int) spnEdad.getValue();
            char genero = cmbGenero.getSelectedItem().toString().charAt(0);
            TipoCuenta tipo = (cmbTipo.getSelectedIndex() == 0) ? TipoCuenta.PUBLICA : TipoCuenta.PRIVADA;
            
            // Aquí definimos la foto por defecto para nuevos usuarios
            String foto = "default_profile.png";

            boolean exito = sistema.registrarUsuario(user, pass, nombre, genero, edad, foto, tipo);

            if (exito) {
                lblTitulo.setText("¡Cuenta Creada!");
                lblTitulo.setForeground(new Color(0, 150, 0));
                try { Thread.sleep(1000); } catch (InterruptedException ignored) {}
                inicializarComponentesLogin();
            } else {
                lblErrorUser.setText("Error inesperado al registrar.");
            }
        });

        revalidate();
        repaint();
    }

    // ---------------------------------------------------------
    // MÉTODOS AUXILIARES UI
    // ---------------------------------------------------------

    private JLabel crearLabelError() {
        JLabel lbl = new JLabel();
        lbl.setForeground(COLOR_ERROR);
        lbl.setFont(new Font("Arial", Font.PLAIN, 10));
        return lbl;
    }

    private JTextField crearTextField(String placeholder) {
        JTextField txt = new JTextField();
        txt.setBackground(COLOR_FIELD);
        txt.setFont(new Font("Arial", Font.BOLD, 13));
        txt.setText(placeholder);
        txt.setForeground(COLOR_PLACEHOLDER);
        txt.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(200, 200, 200)),
                BorderFactory.createEmptyBorder(0, 10, 0, 10)
        ));

        txt.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (txt.getText().equals(placeholder)) {
                    txt.setText("");
                    txt.setForeground(COLOR_FONT);
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                if (txt.getText().isEmpty()) {
                    txt.setText(placeholder);
                    txt.setForeground(COLOR_PLACEHOLDER);
                }
            }
        });
        return txt;
    }

    private JPanel crearPanelPassword() {
        JPanel panelPass = new JPanel(new BorderLayout());
        panelPass.setBackground(COLOR_FIELD);
        panelPass.setBorder(new LineBorder(new Color(200, 200, 200)));

        JPasswordField txtPass = new JPasswordField();
        txtPass.setBackground(COLOR_FIELD);
        txtPass.setFont(new Font("Arial", Font.BOLD, 13));
        txtPass.setBorder(null);
        txtPass.setText("Password");
        txtPass.setEchoChar((char) 0);
        txtPass.setForeground(COLOR_PLACEHOLDER);

        JLabel btnEye = new JLabel();
        btnEye.setCursor(new Cursor(Cursor.HAND_CURSOR));
        if (iconEyeClosed != null) btnEye.setIcon(iconEyeClosed);
        else btnEye.setText("👁");

        btnEye.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (String.valueOf(txtPass.getPassword()).equals("Password")) return;
                if (txtPass.getEchoChar() != 0) {
                    txtPass.setEchoChar((char) 0);
                    if (iconEyeOpen != null) btnEye.setIcon(iconEyeOpen);
                    else btnEye.setText("Ocultar");
                } else {
                    txtPass.setEchoChar('●');
                    if (iconEyeClosed != null) btnEye.setIcon(iconEyeClosed);
                    else btnEye.setText("👁");
                }
            }
        });

        txtPass.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (String.valueOf(txtPass.getPassword()).equals("Password")) {
                    txtPass.setText("");
                    txtPass.setForeground(COLOR_FONT);
                    txtPass.setEchoChar('●');
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                if (txtPass.getPassword().length == 0) {
                    txtPass.setText("Password");
                    txtPass.setEchoChar((char) 0);
                    txtPass.setForeground(COLOR_PLACEHOLDER);
                }
            }
        });

        panelPass.add(txtPass, BorderLayout.CENTER);
        panelPass.add(btnEye, BorderLayout.EAST);
        return panelPass;
    }

    private JPanel crearPanelConIconoDerecho(String placeholder) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(COLOR_FIELD);
        panel.setBorder(new LineBorder(new Color(200, 200, 200)));

        JTextField txt = new JTextField();
        txt.setBackground(COLOR_FIELD);
        txt.setFont(new Font("Arial", Font.BOLD, 13));
        txt.setBorder(null);
        txt.setText(placeholder);
        txt.setForeground(COLOR_PLACEHOLDER);

        JLabel icon = new JLabel();
        icon.setPreferredSize(new Dimension(30, 20));

        txt.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (txt.getText().equals(placeholder)) {
                    txt.setText("");
                    txt.setForeground(COLOR_FONT);
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                if (txt.getText().isEmpty()) {
                    txt.setText(placeholder);
                    txt.setForeground(COLOR_PLACEHOLDER);
                }
            }
        });

        panel.add(txt, BorderLayout.CENTER);
        panel.add(icon, BorderLayout.EAST);
        return panel;
    }

    private JSpinner crearSpinnerPersonalizado(int valorInicial) {
        JSpinner spinner = new JSpinner(new SpinnerNumberModel(valorInicial, 1, 100, 1));
        spinner.setFont(new Font("Arial", Font.BOLD, 13));
        JComponent editor = spinner.getEditor();
        JFormattedTextField tf = ((JSpinner.DefaultEditor) editor).getTextField();
        tf.setBackground(COLOR_FIELD);
        tf.setBorder(null);
        tf.setFont(new Font("Arial", Font.BOLD, 13));
        editor.setBackground(COLOR_FIELD);
        return spinner;
    }

    private void estilizarComboBox(JComboBox<String> cmb) {
        cmb.setFont(new Font("Arial", Font.BOLD, 12));
        cmb.setBackground(COLOR_FIELD);
    }

    private void ponerBordeRojo(JComponent c) {
        c.setBorder(new LineBorder(COLOR_ERROR, 1));
    }

    private void resetBorder(JComponent c) {
        if (c instanceof JPanel) {
            c.setBorder(new LineBorder(new Color(200, 200, 200)));
        } else if (c instanceof JTextField) {
            c.setBorder(BorderFactory.createCompoundBorder(
                    new LineBorder(new Color(200, 200, 200)),
                    BorderFactory.createEmptyBorder(0, 10, 0, 10)));
        } else if (c instanceof JSpinner) {
            ((JSpinner.DefaultEditor) ((JSpinner) c).getEditor()).getTextField().setBorder(null);
        }
    }

    // ---------------------------------------------------------
    // VISTA 3: FEED
    // ---------------------------------------------------------
    private void cargarVistaFeed() {
        getContentPane().removeAll();
        setLayout(new BorderLayout());
        getContentPane().setBackground(COLOR_FONDO);

        add(crearPanelSidebar(), BorderLayout.WEST);

        JPanel panelContenido = new JPanel();
        panelContenido.setBackground(COLOR_FONDO);
        panelContenido.setLayout(new BoxLayout(panelContenido, BoxLayout.Y_AXIS));

        JScrollPane scrollPane = new JScrollPane(panelContenido);
        scrollPane.setBorder(null);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        personalizarScrollBar(scrollPane);

        ArrayList<Publicacion> listaPosts = sistema.getTimeline();

        if (listaPosts.isEmpty()) {
            JLabel lblVacio = new JLabel("No hay publicaciones aún. ¡Sigue a alguien o crea un post!");
            lblVacio.setAlignmentX(Component.CENTER_ALIGNMENT);
            lblVacio.setFont(new Font("Arial", Font.ITALIC, 14));
            panelContenido.add(Box.createVerticalStrut(200));
            panelContenido.add(lblVacio);
        } else {
            for (Publicacion p : listaPosts) {
                JPanel panelPost = crearPanelPost(p);
                panelContenido.add(panelPost);
                panelContenido.add(Box.createVerticalStrut(15));
            }
        }

        add(scrollPane, BorderLayout.CENTER);
        revalidate();
        repaint();
    }

    private void personalizarScrollBar(JScrollPane scrollPane) {
        scrollPane.getVerticalScrollBar().setUI(new BasicScrollBarUI() {
            @Override
            protected void configureScrollBarColors() {
                this.thumbColor = new Color(220, 220, 220);
                this.trackColor = Color.WHITE;
            }

            @Override
            protected JButton createDecreaseButton(int orientation) {
                return createZeroButton();
            }

            @Override
            protected JButton createIncreaseButton(int orientation) {
                return createZeroButton();
            }

            private JButton createZeroButton() {
                JButton jbutton = new JButton();
                jbutton.setPreferredSize(new Dimension(0, 0));
                jbutton.setMinimumSize(new Dimension(0, 0));
                jbutton.setMaximumSize(new Dimension(0, 0));
                return jbutton;
            }
        });
    }

    private JPanel crearPanelPost(Publicacion p) {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setMaximumSize(new Dimension(600, 700));

        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(10, 10, 10, 10),
                BorderFactory.createLineBorder(new Color(230, 230, 230))
        ));

        // A. HEADER
        JPanel header = new JPanel(new FlowLayout(FlowLayout.LEFT));
        header.setBackground(Color.WHITE);

        JLabel lblFoto = new JLabel();
        lblFoto.setPreferredSize(new Dimension(40, 40)); // Tamaño fijo
        
        // INTENTAR CARGAR IMAGEN
        ImageIcon iconoCircular = cargarFotoPerfil(p.getAutor(), 40);
        if (iconoCircular != null) {
            lblFoto.setIcon(iconoCircular);
        } else {
            // Si falla todo, al menos ponemos un círculo gris suave como avatar
            lblFoto.setIcon(crearIconoCircular(new ImageIcon(), 40)); 
            lblFoto.setBackground(new Color(230, 230, 230));
            lblFoto.setOpaque(true);
        }

        JLabel lblUser = new JLabel(p.getAutor());
        lblUser.setFont(new Font("Arial", Font.BOLD, 14));
        lblUser.setCursor(new Cursor(Cursor.HAND_CURSOR));

        lblUser.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                cargarVistaPerfil(p.getAutor());
            }
        });

        header.add(lblFoto);
        header.add(lblUser);

        // ... (El resto del método crearPanelPost sigue igual: Imagen, Footer, etc.)
        // ... PEGA AQUÍ EL RESTO DE TU CÓDIGO DEL POST (IMAGEN Y FOOTER) ...
        
        // Nota: Si quieres el código completo del post para copiar y pegar, te lo dejo abajo abreviado:
        
        // B. IMAGEN DEL POST
        JLabel lblImagen = new JLabel();
        lblImagen.setPreferredSize(new Dimension(600, 500));
        lblImagen.setHorizontalAlignment(SwingConstants.CENTER);
        lblImagen.setBackground(Color.LIGHT_GRAY);
        lblImagen.setOpaque(true);

        try {
            if (p.getRutaImagen() != null && !p.getRutaImagen().isEmpty()) {
                File f = new File(p.getRutaImagen());
                if (f.exists()) {
                    ImageIcon icono = new ImageIcon(p.getRutaImagen());
                    Image img = icono.getImage().getScaledInstance(600, 500, Image.SCALE_SMOOTH);
                    lblImagen.setIcon(new ImageIcon(img));
                    lblImagen.setText(null);
                } else {
                    lblImagen.setText("Imagen no encontrada");
                }
            } else {
                lblImagen.setText("Sin Imagen");
            }
        } catch (Exception e) {
            lblImagen.setText("Error img");
        }

        // C. FOOTER
        JPanel footer = new JPanel();
        footer.setLayout(new BoxLayout(footer, BoxLayout.Y_AXIS));
        footer.setBackground(Color.WHITE);
        footer.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        String textoMostrar = "<html><b>" + p.getAutor() + "</b> " + p.getContenido() + "</html>";
        JLabel lblContenido = new JLabel(textoMostrar);
        lblContenido.setFont(new Font("Arial", Font.PLAIN, 13));

        JLabel lblFecha = new JLabel(p.getFecha().toString() + " " + p.getHoraFormateada());
        lblFecha.setForeground(COLOR_FONT);
        lblFecha.setFont(new Font("Arial", Font.ITALIC, 11));

        footer.add(lblContenido);
        footer.add(lblFecha);

        panel.add(header, BorderLayout.NORTH);
        panel.add(lblImagen, BorderLayout.CENTER);
        panel.add(footer, BorderLayout.SOUTH);

        return panel;
    }

    // ---------------------------------------------------------
    // NUEVA PUBLICACIÓN
    // ---------------------------------------------------------
    private void abrirDialogoNuevaPublicacion() {
        JDialog dialog = new JDialog(this, "Nueva Publicación", true);
        dialog.setSize(450, 450);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());
        dialog.getContentPane().setBackground(COLOR_FONDO);

        JPanel panelImg = new JPanel();
        panelImg.setBackground(COLOR_FONDO);
        JButton btnSeleccionar = new JButton("Seleccionar Imagen");
        btnSeleccionar.setBackground(COLOR_BOTTON);
        btnSeleccionar.setForeground(Color.WHITE);
        btnSeleccionar.setFocusPainted(false);

        JLabel lblRuta = new JLabel("Ninguna imagen seleccionada");
        lblRuta.setForeground(COLOR_FONT);

        panelImg.add(btnSeleccionar);
        panelImg.add(lblRuta);

        JTextArea txtContenido = new JTextArea();
        txtContenido.setLineWrap(true);
        txtContenido.setWrapStyleWord(true);
        txtContenido.setFont(new Font("Arial", Font.PLAIN, 13));
        JScrollPane scrollTxt = new JScrollPane(txtContenido);
        scrollTxt.setBorder(BorderFactory.createTitledBorder("Contenido (Max 220 chars)"));

        JPanel panelBotones = new JPanel();
        JButton btnPublicar = new JButton("Publicar");
        btnPublicar.setBackground(new Color(0, 150, 0));
        btnPublicar.setForeground(Color.WHITE);
        btnPublicar.setFocusPainted(false);

        JButton btnCancelar = new JButton("Cancelar");
        panelBotones.add(btnPublicar);
        panelBotones.add(btnCancelar);

        final String[] rutaSeleccionada = {""};

        btnSeleccionar.addActionListener(e -> {
            JFileChooser fc = new JFileChooser();
            fc.setBackground(Color.WHITE);
            fc.setDialogTitle("Seleccionar foto");

            int res = fc.showOpenDialog(this);
            if (res == JFileChooser.APPROVE_OPTION) {
                rutaSeleccionada[0] = fc.getSelectedFile().getAbsolutePath();
                lblRuta.setText(fc.getSelectedFile().getName());
            }
        });

        btnPublicar.addActionListener(e -> {
            String texto = txtContenido.getText();
            if (texto.isEmpty() || rutaSeleccionada[0].isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Debes escribir algo y seleccionar una imagen.");
                return;
            }

            boolean exito = sistema.crearPublicacion(texto, rutaSeleccionada[0], "", "");

            if (exito) {
                JOptionPane.showMessageDialog(dialog, "¡Publicado con éxito!");
                dialog.dispose();
                cargarVistaFeed();
            } else {
                JOptionPane.showMessageDialog(dialog, "Error al guardar la publicación.");
            }
        });

        btnCancelar.addActionListener(e -> dialog.dispose());

        dialog.add(panelImg, BorderLayout.NORTH);
        dialog.add(scrollTxt, BorderLayout.CENTER);
        dialog.add(panelBotones, BorderLayout.SOUTH);

        dialog.setVisible(true);
    }

    // ---------------------------------------------------------
    // SIDEBAR
    // ---------------------------------------------------------
    private JPanel crearPanelSidebar() {
        JPanel panelSidebar = new JPanel();
        panelSidebar.setPreferredSize(new Dimension(250, 768));
        panelSidebar.setBackground(Color.WHITE);
        panelSidebar.setLayout(new BoxLayout(panelSidebar, BoxLayout.Y_AXIS));
        panelSidebar.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, new Color(220, 220, 220)));

        JLabel lblLogoSide = new JLabel("Instagram");
        lblLogoSide.setFont(new Font("Arial", Font.BOLD, 24));
        lblLogoSide.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton btnInicio = crearBotonSidebar("Inicio", "🏠");
        JButton btnBuscar = crearBotonSidebar("Buscar", "🔍");
        JButton btnMensajes = crearBotonSidebar("Mensajes", "✉️");
        JButton btnNuevo = crearBotonSidebar("Crear", "➕");
        JButton btnPerfil = crearBotonSidebar("Mi Perfil", "👤");
        JButton btnCerrarSesion = crearBotonSidebar("Cerrar Sesión", "➡️");

        panelSidebar.add(Box.createVerticalStrut(20));
        panelSidebar.add(lblLogoSide);
        panelSidebar.add(Box.createVerticalStrut(30));
        panelSidebar.add(btnInicio);
        panelSidebar.add(btnBuscar);
        panelSidebar.add(btnMensajes);
        panelSidebar.add(btnNuevo);
        panelSidebar.add(btnPerfil);
        panelSidebar.add(Box.createVerticalGlue());
        panelSidebar.add(btnCerrarSesion);
        panelSidebar.add(Box.createVerticalStrut(20));

        btnInicio.addActionListener(e -> cargarVistaFeed());
        btnBuscar.addActionListener(e -> cargarVistaBusqueda());
        btnMensajes.addActionListener(e -> cargarVistaInbox());
        btnNuevo.addActionListener(e -> abrirDialogoNuevaPublicacion());
        btnPerfil.addActionListener(e -> cargarVistaPerfil(sistema.getUsuarioActual().getUsername()));

        btnCerrarSesion.addActionListener(e -> {
            sistema.logout();
            inicializarComponentesLogin();
        });

        return panelSidebar;
    }

    private JButton crearBotonSidebar(String texto, String icono) {
        JButton btn = new JButton(icono + "  " + texto);
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);
        btn.setMaximumSize(new Dimension(220, 40));
        btn.setFont(new Font("Arial", Font.PLAIN, 14));
        btn.setBorderPainted(false);
        btn.setBackground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    // ---------------------------------------------------------
    // VISTA 4: PERFIL (SIN FONDO GRIS)
    // ---------------------------------------------------------
    private void cargarVistaPerfil(String usernameVisitar) {
        getContentPane().removeAll();
        setLayout(new BorderLayout());
        getContentPane().setBackground(COLOR_FONDO);

        add(crearPanelSidebar(), BorderLayout.WEST);

        JPanel panelContenido = new JPanel();
        panelContenido.setLayout(new BorderLayout());
        panelContenido.setBackground(COLOR_FONDO);

        // HEADER
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(COLOR_FONDO);
        header.setBorder(BorderFactory.createEmptyBorder(40, 50, 20, 50));

        JLabel lblFoto = new JLabel();
        lblFoto.setPreferredSize(new Dimension(150, 150));
        lblFoto.setHorizontalAlignment(SwingConstants.CENTER);

        // USO DEL NUEVO MÉTODO PARA FOTO DE PERFIL
        ImageIcon iconoCircular = cargarFotoPerfil(usernameVisitar, 150);
        if (iconoCircular != null) {
            lblFoto.setIcon(iconoCircular);
        } else {
            lblFoto.setText("Foto");
        }
        
        lblFoto.setBorder(new LineBorder(new Color(230, 230, 230), 2, true));
        header.add(lblFoto, BorderLayout.WEST);

        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setBackground(COLOR_FONDO);

        JPanel fila1 = new JPanel(new FlowLayout(FlowLayout.LEFT));
        fila1.setBackground(COLOR_FONDO);
        JLabel lblUsername = new JLabel(usernameVisitar);
        lblUsername.setFont(new Font("Arial", Font.BOLD, 24));
        fila1.add(lblUsername);

        if (!usernameVisitar.equals(sistema.getUsuarioActual().getUsername())) {
            JButton btnSeguir = new JButton();
            if (sistema.yaLoSigo(usernameVisitar)) {
                btnSeguir.setText("Dejar de seguir");
            } else {
                btnSeguir.setText("Seguir");
                btnSeguir.setBackground(COLOR_BOTTON);
                btnSeguir.setForeground(Color.WHITE);
            }
            btnSeguir.setFont(new Font("Arial", Font.BOLD, 12));
            btnSeguir.setBorderPainted(false);
            btnSeguir.setOpaque(true);
            btnSeguir.setCursor(new Cursor(Cursor.HAND_CURSOR));

            btnSeguir.addActionListener(e -> {
                sistema.seguirUsuario(usernameVisitar);
                cargarVistaPerfil(usernameVisitar);
            });

            fila1.add(Box.createHorizontalStrut(20));
            fila1.add(btnSeguir);
        } else {
            JButton btnEditar = new JButton("Editar Perfil");
            btnEditar.setFont(new Font("Arial", Font.BOLD, 12));
            btnEditar.setBackground(COLOR_FIELD);
            fila1.add(Box.createHorizontalStrut(20));
            fila1.add(btnEditar);
        }

        JPanel fila2 = new JPanel(new FlowLayout(FlowLayout.LEFT));
        fila2.setBackground(COLOR_FONDO);
        int posts = sistema.getCantidadPosts(usernameVisitar);
        int followers = sistema.getCantidadFollowers(usernameVisitar);
        int following = sistema.getCantidadFollowing(usernameVisitar);

        fila2.add(crearLabelStat(posts, "publicaciones"));
        fila2.add(Box.createHorizontalStrut(25));
        fila2.add(crearLabelStat(followers, "seguidores"));
        fila2.add(Box.createHorizontalStrut(25));
        fila2.add(crearLabelStat(following, "seguidos"));

        Usuario userVisitar = sistema.buscarUsuario(usernameVisitar);
        String nombreReal = (userVisitar != null) ? userVisitar.getNombreCompleto() : "Nombre";
        JLabel lblNombreReal = new JLabel(nombreReal);
        lblNombreReal.setFont(new Font("Arial", Font.BOLD, 14));

        JPanel fila3 = new JPanel(new FlowLayout(FlowLayout.LEFT));
        fila3.setBackground(COLOR_FONDO);
        fila3.add(lblNombreReal);

        infoPanel.add(fila1);
        infoPanel.add(fila2);
        infoPanel.add(fila3);

        header.add(infoPanel, BorderLayout.CENTER);
        panelContenido.add(header, BorderLayout.NORTH);

        // GRID DE FOTOS (SIN FONDO GRIS)
        JPanel gridPanel = new JPanel();
        gridPanel.setLayout(new GridLayout(0, 4, 2, 2));
        gridPanel.setBackground(COLOR_FONDO); 
        gridPanel.setBorder(new EmptyBorder(20, 50, 20, 50));

        ArrayList<Publicacion> postsList = sistema.getPublicacionesDeUsuario(usernameVisitar);

        if (postsList.isEmpty()) {
            JLabel lblVacio = new JLabel("No hay publicaciones aún.");
            lblVacio.setHorizontalAlignment(SwingConstants.CENTER);
            gridPanel.add(lblVacio);
        } else {
            for (Publicacion p : postsList) {
                JPanel miniPanel = new JPanel(new BorderLayout());
                miniPanel.setBackground(Color.WHITE); 
                
                JLabel lblImg = new JLabel();
                try {
                    if (p.getRutaImagen() != null) {
                        ImageIcon icono = new ImageIcon(p.getRutaImagen());
                        Image img = icono.getImage().getScaledInstance(200, 200, Image.SCALE_SMOOTH);
                        lblImg.setIcon(new ImageIcon(img));
                    }
                } catch (Exception e) {
                    lblImg.setText("...");
                }
                lblImg.setHorizontalAlignment(SwingConstants.CENTER);
                miniPanel.add(lblImg, BorderLayout.CENTER);
                
                gridPanel.add(miniPanel);
            }
        }

        JScrollPane scrollGrid = new JScrollPane(gridPanel);
        scrollGrid.setBorder(null);
        scrollGrid.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        personalizarScrollBar(scrollGrid);
        
        panelContenido.add(scrollGrid, BorderLayout.CENTER);
        add(panelContenido, BorderLayout.CENTER);

        revalidate();
        repaint();
    }

    // Método auxiliar para estadísticas
    private JPanel crearLabelStat(int cantidad, String texto) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 2, 0));
        p.setBackground(COLOR_FONDO);
        JLabel lblCant = new JLabel(String.valueOf(cantidad));
        lblCant.setFont(new Font("Arial", Font.BOLD, 14));
        JLabel lblText = new JLabel(" " + texto);
        lblText.setFont(new Font("Arial", Font.PLAIN, 14));
        p.add(lblCant);
        p.add(lblText);
        return p;
    }

    // ---------------------------------------------------------
    // MÉTODO AVANZADO: Cargar Foto Perfil (ROBUSTO)
    // ---------------------------------------------------------
    private ImageIcon cargarFotoPerfil(String username, int diametro) {
        Usuario u = sistema.buscarUsuario(username);
        String ruta = null;
        
        // 1. Determinar la ruta
        if (u != null && u.getFotoPerfil() != null && !u.getFotoPerfil().isEmpty()) {
            ruta = u.getFotoPerfil();
        }

        ImageIcon icon = null;
        
        try {
            // 2. Intentar cargar la imagen del usuario (si existe y no es default)
            if (ruta != null && !ruta.equals("default_profile.png") && !ruta.equals("null")) {
                File f = new File(ruta);
                if (f.exists()) {
                    icon = new ImageIcon(ruta);
                    System.out.println("Cargando foto personalizada de: " + username);
                }
            }
            
            // 3. Si no encontró foto personalizada (o es default), cargar la default del proyecto
            if (icon == null) {
                // Intenta cargar desde resources
                java.net.URL url = getClass().getResource("/images/default_profile.png");
                
                if (url != null) {
                    icon = new ImageIcon(url);
                    System.out.println("Cargando foto DEFAULT para: " + username);
                } else {
                    // PLAN B: Si getResource falla (raro), intentar cargar desde carpeta src manual
                    // Esto pasa a veces en NetBeans si la carpeta no es "Source Package"
                    File fDefault = new File("src/images/default_profile.png"); 
                    if(fDefault.exists()) {
                        icon = new ImageIcon(fDefault.getAbsolutePath());
                        System.out.println("Cargando DEFAULT desde archivo físico.");
                    } else {
                        System.err.println("ERROR CRÍTICO: No se encuentra 'default_profile.png' en /images/ ni en src/images/");
                    }
                }
            }
            
            // 4. Aplicar circularidad si tenemos imagen
            if (icon != null && icon.getImage() != null) {
                return crearIconoCircular(icon, diametro);
            }

        } catch (Exception e) {
            System.out.println("Error procesando imagen: " + e.getMessage());
        }
        return null;
    }

    private ImageIcon crearIconoCircular(ImageIcon iconoOriginal, int diametro) {
        try {
            Image imgEscalada = iconoOriginal.getImage().getScaledInstance(diametro, diametro, Image.SCALE_SMOOTH);
            BufferedImage buffer = new BufferedImage(diametro, diametro, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2 = buffer.createGraphics();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setClip(new java.awt.geom.Ellipse2D.Double(0, 0, diametro, diametro));
            g2.drawImage(imgEscalada, 0, 0, null);
            g2.dispose();
            return new ImageIcon(buffer);
        } catch (Exception e) {
            return null;
        }
    }

    // ---------------------------------------------------------
    // VISTA 5: BÚSQUEDA
    // ---------------------------------------------------------
    private void cargarVistaBusqueda() {
        getContentPane().removeAll();
        setLayout(new BorderLayout());
        getContentPane().setBackground(COLOR_FONDO);

        add(crearPanelSidebar(), BorderLayout.WEST);

        JPanel panelMain = new JPanel(new BorderLayout());
        panelMain.setBackground(COLOR_FONDO);
        panelMain.setBorder(BorderFactory.createEmptyBorder(40, 40, 40, 40));

        JPanel panelSearchBar = new JPanel(new BorderLayout());
        panelSearchBar.setBackground(Color.WHITE);
        panelSearchBar.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(220, 220, 220), 1, true),
                BorderFactory.createEmptyBorder(10, 15, 10, 15)
        ));

        JTextField txtBuscar = new JTextField();
        txtBuscar.setFont(new Font("Arial", Font.PLAIN, 16));
        txtBuscar.setBorder(null);
        txtBuscar.setText("Escribe un username...");
        txtBuscar.setForeground(COLOR_PLACEHOLDER);

        txtBuscar.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent e) {
                if (txtBuscar.getText().equals("Escribe un username...")) {
                    txtBuscar.setText("");
                    txtBuscar.setForeground(Color.BLACK);
                }
            }

            public void focusLost(FocusEvent e) {
                if (txtBuscar.getText().isEmpty()) {
                    txtBuscar.setText("Escribe un username...");
                    txtBuscar.setForeground(COLOR_PLACEHOLDER);
                }
            }
        });

        JButton btnBuscar = new JButton("Buscar");
        btnBuscar.setBackground(COLOR_BOTTON);
        btnBuscar.setForeground(Color.WHITE);
        btnBuscar.setFont(new Font("Arial", Font.BOLD, 12));
        btnBuscar.setBorderPainted(false);

        panelSearchBar.add(txtBuscar, BorderLayout.CENTER);
        panelSearchBar.add(btnBuscar, BorderLayout.EAST);
        panelMain.add(panelSearchBar, BorderLayout.NORTH);

        JPanel panelResultados = new JPanel();
        panelResultados.setLayout(new BoxLayout(panelResultados, BoxLayout.Y_AXIS));
        panelResultados.setBackground(COLOR_FONDO);

        JScrollPane scrollResultados = new JScrollPane(panelResultados);
        scrollResultados.setBorder(null);
        scrollResultados.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        personalizarScrollBar(scrollResultados);

        panelMain.add(scrollResultados, BorderLayout.CENTER);
        add(panelMain, BorderLayout.CENTER);

        ActionListener accionBuscar = e -> {
            String criterio = txtBuscar.getText();
            if (criterio.isEmpty() || criterio.equals("Escribe un username...")) return;

            panelResultados.removeAll();
            ArrayList<Usuario> lista = sistema.buscarUsuarios(criterio);

            if (lista.isEmpty()) {
                JLabel lblNo = new JLabel("No se encontraron resultados.");
                lblNo.setAlignmentX(Component.CENTER_ALIGNMENT);
                lblNo.setFont(new Font("Arial", Font.ITALIC, 14));
                lblNo.setForeground(COLOR_FONT);
                panelResultados.add(Box.createVerticalStrut(50));
                panelResultados.add(lblNo);
            } else {
                for (Usuario u : lista) {
                    panelResultados.add(crearPanelResultadoUsuario(u));
                    panelResultados.add(Box.createVerticalStrut(10));
                }
            }
            panelResultados.revalidate();
            panelResultados.repaint();
        };

        btnBuscar.addActionListener(accionBuscar);
        txtBuscar.addActionListener(accionBuscar);

        revalidate();
        repaint();
    }

    private JPanel crearPanelResultadoUsuario(Usuario u) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setMaximumSize(new Dimension(600, 60));
        panel.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(230, 230, 230)),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));

        JPanel info = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        info.setBackground(Color.WHITE);

        JLabel lblFoto = new JLabel();
        lblFoto.setPreferredSize(new Dimension(40, 40));
        // USO DEL NUEVO MÉTODO
        ImageIcon icono = cargarFotoPerfil(u.getUsername(), 40);
        if (icono != null) lblFoto.setIcon(icono);

        JLabel lblNombre = new JLabel(u.getUsername());
        lblNombre.setFont(new Font("Arial", Font.BOLD, 14));
        lblNombre.setCursor(new Cursor(Cursor.HAND_CURSOR));

        lblNombre.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                cargarVistaPerfil(u.getUsername());
            }
        });

        info.add(lblFoto);
        info.add(lblNombre);

        JButton btnVer = new JButton("Ver Perfil");
        btnVer.setFont(new Font("Arial", Font.PLAIN, 12));
        btnVer.setBackground(COLOR_FIELD);
        btnVer.setBorderPainted(false);
        btnVer.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnVer.addActionListener(e -> cargarVistaPerfil(u.getUsername()));

        panel.add(info, BorderLayout.WEST);
        panel.add(btnVer, BorderLayout.EAST);

        return panel;
    }

    // ---------------------------------------------------------
    // VISTA 6: INBOX
    // ---------------------------------------------------------
    private void cargarVistaInbox() {
        getContentPane().removeAll();
        setLayout(new BorderLayout());
        getContentPane().setBackground(COLOR_FONDO);

        add(crearPanelSidebar(), BorderLayout.WEST);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setDividerLocation(250);
        splitPane.setBorder(null);
        splitPane.setBackground(COLOR_FONDO);

        JPanel panelLista = new JPanel(new BorderLayout());
        panelLista.setBackground(Color.WHITE);
        panelLista.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, new Color(220, 220, 220)));

        JLabel lblTituloInbox = new JLabel("Mensajes", SwingConstants.CENTER);
        lblTituloInbox.setFont(new Font("Arial", Font.BOLD, 18));
        lblTituloInbox.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        panelLista.add(lblTituloInbox, BorderLayout.NORTH);

        DefaultListModel<String> modelChats = new DefaultListModel<>();
        JList<String> listaChats = new JList<>(modelChats);
        listaChats.setFont(new Font("Arial", Font.PLAIN, 14));
        listaChats.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        JScrollPane scrollLista = new JScrollPane(listaChats);
        scrollLista.setBorder(null);
        panelLista.add(scrollLista, BorderLayout.CENTER);

        JButton btnNuevoMsg = new JButton("Nuevo Mensaje");
        btnNuevoMsg.setBackground(COLOR_BOTTON);
        btnNuevoMsg.setForeground(Color.WHITE);
        btnNuevoMsg.setFont(new Font("Arial", Font.BOLD, 12));
        panelLista.add(btnNuevoMsg, BorderLayout.SOUTH);

        ArrayList<String> chats = sistema.getChatsRecientes();
        for (String u : chats) modelChats.addElement(u);

        splitPane.setLeftComponent(panelLista);

        JPanel panelChat = new JPanel(new BorderLayout());
        panelChat.setBackground(COLOR_FONDO);

        JLabel lblSelecciona = new JLabel("Selecciona un chat", SwingConstants.CENTER);
        lblSelecciona.setFont(new Font("Arial", Font.ITALIC, 16));
        lblSelecciona.setForeground(COLOR_FONT);
        panelChat.add(lblSelecciona, BorderLayout.CENTER);

        splitPane.setRightComponent(panelChat);
        add(splitPane, BorderLayout.CENTER);

        listaChats.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                String seleccionado = listaChats.getSelectedValue();
                if (seleccionado != null) {
                    cargarConversacionEnPanel(panelChat, seleccionado);
                }
            }
        });

        btnNuevoMsg.addActionListener(e -> {
            String destino = JOptionPane.showInputDialog(this, "Username del destinatario:");
            if (destino != null && !destino.isEmpty()) {
                if (sistema.buscarUsuario(destino) == null) {
                    JOptionPane.showMessageDialog(this, "Usuario no encontrado.");
                } else if (!sistema.puedeEnviarMensaje(destino)) {
                    JOptionPane.showMessageDialog(this, "No puedes enviar mensaje a este usuario (Perfil Privado).");
                } else {
                    if (!modelChats.contains(destino)) modelChats.addElement(destino);
                    listaChats.setSelectedValue(destino, true);
                }
            }
        });

        revalidate();
        repaint();
    }

    private void cargarConversacionEnPanel(JPanel panelChat, String otroUsuario) {
        sistema.marcarComoLeido(otroUsuario);
        panelChat.removeAll();
        panelChat.setLayout(new BorderLayout());
        panelChat.setBackground(COLOR_FONDO);

        JPanel header = new JPanel(new FlowLayout(FlowLayout.LEFT));
        header.setBackground(Color.WHITE);
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(220, 220, 220)));
        JLabel lblNombre = new JLabel(otroUsuario);
        lblNombre.setFont(new Font("Arial", Font.BOLD, 16));
        header.add(lblNombre);
        panelChat.add(header, BorderLayout.NORTH);

        JPanel panelMensajes = new JPanel();
        panelMensajes.setLayout(new BoxLayout(panelMensajes, BoxLayout.Y_AXIS));
        panelMensajes.setBackground(COLOR_FONDO);

        JScrollPane scrollMensajes = new JScrollPane(panelMensajes);
        scrollMensajes.setBorder(null);

        ArrayList<Mensaje> historial = sistema.getConversacion(otroUsuario);
        for (Mensaje m : historial) {
            JPanel msgPanel = new JPanel(new BorderLayout());
            msgPanel.setBackground(COLOR_FONDO);
            msgPanel.setMaximumSize(new Dimension(400, 50));

            JTextArea txtMsg = new JTextArea(m.getContenido());
            txtMsg.setLineWrap(true);
            txtMsg.setWrapStyleWord(true);
            txtMsg.setFont(new Font("Arial", Font.PLAIN, 12));
            txtMsg.setEditable(false);
            txtMsg.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

            if (m.getEmisor().equals(sistema.getUsuarioActual().getUsername())) {
                msgPanel.add(txtMsg, BorderLayout.EAST);
                txtMsg.setBackground(COLOR_BOTTON);
                txtMsg.setForeground(Color.WHITE);
            } else {
                msgPanel.add(txtMsg, BorderLayout.WEST);
                txtMsg.setBackground(Color.WHITE);
            }
            panelMensajes.add(msgPanel);
            panelMensajes.add(Box.createVerticalStrut(5));
        }

        SwingUtilities.invokeLater(() -> scrollMensajes.getVerticalScrollBar().setValue(scrollMensajes.getVerticalScrollBar().getMaximum()));
        panelChat.add(scrollMensajes, BorderLayout.CENTER);

        JPanel footer = new JPanel(new BorderLayout());
        footer.setBackground(Color.WHITE);
        footer.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        JTextField txtInput = new JTextField();
        txtInput.setFont(new Font("Arial", Font.PLAIN, 14));

        JButton btnSend = new JButton("Enviar");
        btnSend.setBackground(COLOR_BOTTON);
        btnSend.setForeground(Color.WHITE);
        btnSend.setFont(new Font("Arial", Font.BOLD, 12));

        footer.add(txtInput, BorderLayout.CENTER);
        footer.add(btnSend, BorderLayout.EAST);
        panelChat.add(footer, BorderLayout.SOUTH);

        ActionListener sendAction = e -> {
            String texto = txtInput.getText();
            if (!texto.isEmpty()) {
                sistema.enviarMensaje(otroUsuario, texto, "TEXTO");
                txtInput.setText("");
                cargarConversacionEnPanel(panelChat, otroUsuario);
            }
        };

        btnSend.addActionListener(sendAction);
        txtInput.addActionListener(sendAction);

        panelChat.revalidate();
        panelChat.repaint();
    }
}