package gui;

import enums.TipoCuenta;
import instagram.Publicacion;
import instagram.Sistema;
import instagram.Usuario;

import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.util.ArrayList;

public class VentanaPrincipal extends JFrame {

    private Sistema sistema;

    // --- COLORES PERSONALIZADOS ---
    private final Color COLOR_FONDO = Color.WHITE;
    private final Color COLOR_BOTTON = new Color(64, 155, 230);
    
    // COLOR PARA BOTÓN DESHABILITADO (Solicitud del usuario)
    private final Color COLOR_DISABLED = new Color(42, 107, 161); 
    // COLOR DE TEXTO PARA BOTÓN DESHABILITADO
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
        setTitle("InstaRAIZ - Desktop");
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(null);
        setResizable(false);
    }

    // ---------------------------------------------------------
    // VISTA 1: LOGIN (Sin panel contenedor, componentes sueltos)
    // ---------------------------------------------------------
    private void inicializarComponentesLogin() {
        getContentPane().removeAll();
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

        // --- COMPONENTES ---
        JLabel lblErrorUser = crearLabelError();
        JTextField txtUser = crearTextField("Username");
        
        JLabel lblErrorPass = crearLabelError();
        JPanel panelPass = crearPanelPassword(); 
        
        JButton btnLogin = new JButton("Iniciar Sesión");
        btnLogin.setFont(new Font("Arial", Font.BOLD, 14));
        btnLogin.setOpaque(true); 
        btnLogin.setBorderPainted(false);
        btnLogin.setFocusPainted(false);
        
        // Estado inicial: Deshabilitado con colores personalizados
        btnLogin.setBackground(COLOR_DISABLED); 
        btnLogin.setForeground(COLOR_TEXT_DISABLED);
        btnLogin.setEnabled(false);

        JLabel lblRegistro = new JLabel("¿Aún no tienes cuenta? Regístrate");
        lblRegistro.setForeground(COLOR_BOTTON);
        lblRegistro.setFont(new Font("Arial", Font.BOLD, 13));
        lblRegistro.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        // IMPORTANTE: Ajustar tamaño al contenido para evitar clicks en blanco
        lblRegistro.setSize(lblRegistro.getPreferredSize()); 

        // --- POSICIONAMIENTO (Sin panel, más espacio vertical) ---
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
        lblRegistro.setLocation(xInputs + (anchoInputs - anchoTextoReg)/2, y);

        add(lblLogo);
        add(txtUser);
        add(lblErrorUser);
        add(panelPass);
        add(lblErrorPass);
        add(btnLogin);
        add(lblRegistro);

        // --- LÓGICA DE VALIDACIÓN ---
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
                
                // --- LÓGICA BOTÓN (Cambio de colores manual) ---
                if (userValido && passValida) {
                    btnLogin.setEnabled(true);
                    btnLogin.setBackground(COLOR_BOTTON);
                    btnLogin.setForeground(Color.WHITE);
                } else {
                    btnLogin.setEnabled(false);
                    btnLogin.setBackground(COLOR_DISABLED); // Color deshabilitado
                    btnLogin.setForeground(COLOR_TEXT_DISABLED); // Texto deshabilitado
                }
                
                // Limpiar errores visuales mientras escribe
                if (userValido) resetBorder(txtUser);
                if (passValida) resetBorder(panelPass);
                lblErrorUser.setText("");
                lblErrorPass.setText("");
            }
        };

        txtUser.getDocument().addDocumentListener(validationListener);
        txtPass.getDocument().addDocumentListener(validationListener);

        // --- EVENTO LOGIN ---
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

        revalidate(); repaint();
    }

    // ---------------------------------------------------------
    // VISTA 2: REGISTRO
    // ---------------------------------------------------------
    private void cargarVistaRegistro() {
        getContentPane().removeAll();
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
        
        // Estado inicial
        btnRegistrar.setBackground(COLOR_DISABLED);
        btnRegistrar.setForeground(COLOR_TEXT_DISABLED);
        btnRegistrar.setEnabled(false);

        JLabel btnVolver = new JLabel("¿Ya tienes cuenta? Volver");
        btnVolver.setForeground(COLOR_BOTTON);
        btnVolver.setFont(new Font("Arial", Font.BOLD, 12));
        btnVolver.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnVolver.setSize(btnVolver.getPreferredSize());

        // Layout
        int anchoInputs = 320;
        int altoInput = 36;
        int xInputs = (1366 - anchoInputs) / 2;
        int y = 120;

        lblTitulo.setBounds(xInputs, y, anchoInputs, 40); y += 50;
        
        txtNombre.setBounds(xInputs, y, anchoInputs, altoInput); 
        lblErrorNombre.setBounds(xInputs, y + altoInput + 2, anchoInputs, 20); y += 60;
        
        panelUser.setBounds(xInputs, y, anchoInputs, altoInput);
        lblErrorUser.setBounds(xInputs, y + altoInput + 2, anchoInputs, 20); y += 60;
        
        panelPass.setBounds(xInputs, y, anchoInputs, altoInput);
        lblErrorPass.setBounds(xInputs, y + altoInput + 2, anchoInputs, 20); y += 60;
        
        JLabel lblEdad = new JLabel("Edad:"); lblEdad.setFont(new Font("Arial", Font.BOLD, 12));
        lblEdad.setBounds(xInputs, y, 100, 20); 
        spnEdad.setBounds(xInputs, y + 20, 80, 30);
        lblErrorEdad.setBounds(xInputs, y + 52, 150, 15); 
        
        JLabel lblGenero = new JLabel("Género:"); lblGenero.setFont(new Font("Arial", Font.BOLD, 12));
        lblGenero.setBounds(xInputs + 160, y, 100, 20);
        cmbGenero.setBounds(xInputs + 160, y + 20, 140, 30);
        y += 75;

        JLabel lblTipo = new JLabel("Tipo Cuenta:"); lblTipo.setFont(new Font("Arial", Font.BOLD, 12));
        lblTipo.setBounds(xInputs, y, 100, 20);
        cmbTipo.setBounds(xInputs, y + 20, anchoInputs, 30);
        y += 60;

        btnRegistrar.setBounds(xInputs, y, anchoInputs, altoInput); y += 50;
        
        int anchoTextoVolver = btnVolver.getPreferredSize().width;
        btnVolver.setLocation(xInputs + (anchoInputs - anchoTextoVolver)/2, y);

        add(lblTitulo);
        add(txtNombre); add(lblErrorNombre);
        add(panelUser); add(lblErrorUser);
        add(panelPass); add(lblErrorPass);
        add(spnEdad); add(cmbGenero); add(lblEdad); add(lblGenero); add(lblErrorEdad);
        add(cmbTipo); add(lblTipo);
        add(btnRegistrar); add(btnVolver);

        // --- LÓGICA DE VALIDACIÓN ---
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

                // 1. Nombre
                String nombre = txtNombre.getText();
                if (nombre.equals("Nombre Completo") || nombre.isEmpty()) todoOK = false;

                // 2. Username
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

                // 3. Password
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

                // 4. Edad
                int edad = (int) spnEdad.getValue();
                lblErrorEdad.setText("");
                resetBorder(spnEdad);
                
                if (edad < 18) {
                    lblErrorEdad.setText("Debes ser mayor de 18 años.");
                    ponerBordeRojo(spnEdad);
                    todoOK = false;
                }

                // --- LÓGICA BOTÓN REGISTRO ---
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
            String foto = "default_profile.png"; 

            boolean exito = sistema.registrarUsuario(user, pass, nombre, genero, edad, foto, tipo);

            if (exito) {
                lblTitulo.setText("¡Cuenta Creada! :)");
                lblTitulo.setForeground(new Color(0, 150, 0));
                try { Thread.sleep(1000); } catch (InterruptedException ignored) {} 
                inicializarComponentesLogin(); 
            } else {
                lblErrorUser.setText("Error inesperado al registrar.");
            }
        });

        revalidate(); repaint();
    }

    // ---------------------------------------------------------
    // MÉTODOS AUXILIARES DE UI
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
                    if (iconEyeOpen != null) btnEye.setIcon(iconEyeOpen); else btnEye.setText("Ocultar");
                } else {
                    txtPass.setEchoChar('●');
                    if (iconEyeClosed != null) btnEye.setIcon(iconEyeClosed); else btnEye.setText("👁");
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
             ((JSpinner.DefaultEditor)((JSpinner)c).getEditor()).getTextField().setBorder(null);
        }
    }

       // ---------------------------------------------------------
    // VISTA 3: FEED (ESQUELETO DESKTOP)
    // ---------------------------------------------------------
    private void cargarVistaFeed() {
        getContentPane().removeAll();
        setLayout(new BorderLayout()); // Estructura principal Desktop
        getContentPane().setBackground(COLOR_FONDO);

        // --- 1. SIDEBAR (Barra lateral izquierda) ---
        JPanel panelSidebar = new JPanel();
        panelSidebar.setPreferredSize(new Dimension(250, 768));
        panelSidebar.setBackground(Color.WHITE);
        panelSidebar.setLayout(new BoxLayout(panelSidebar, BoxLayout.Y_AXIS));
        panelSidebar.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, new Color(220, 220, 220)));

        JLabel lblLogoSide = new JLabel("InstaRAIZ");
        lblLogoSide.setFont(new Font("Arial", Font.BOLD, 24));
        lblLogoSide.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        // Botones del menú
        JButton btnInicio = crearBotonSidebar("Inicio", "🏠");
        JButton btnNuevo = crearBotonSidebar("Crear", "➕");
        JButton btnPerfil = crearBotonSidebar("Mi Perfil", "👤");
        JButton btnCerrarSesion = crearBotonSidebar("Cerrar Sesión", "➡️");

        // Espaciado y agregado
        panelSidebar.add(Box.createVerticalStrut(20));
        panelSidebar.add(lblLogoSide);
        panelSidebar.add(Box.createVerticalStrut(30));
        panelSidebar.add(btnInicio);
        panelSidebar.add(btnNuevo);
        panelSidebar.add(btnPerfil);
        panelSidebar.add(Box.createVerticalGlue()); // Empuja abajo lo que sigue
        panelSidebar.add(btnCerrarSesion);
        panelSidebar.add(Box.createVerticalStrut(20));

        // --- 2. CONTENEDOR DEL FEED (Centro) ---
        JPanel panelContenido = new JPanel();
        panelContenido.setBackground(COLOR_FONDO);
        panelContenido.setLayout(new BoxLayout(panelContenido, BoxLayout.Y_AXIS)); 

        JScrollPane scrollPane = new JScrollPane(panelContenido);
        scrollPane.setBorder(null);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16); // Scroll más suave

        // --- 3. CARGAR PUBLICACIONES REALES ---
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
                panelContenido.add(Box.createVerticalStrut(15)); // Espacio entre posts
            }
        }

        // --- 4. ENSAMBLAR VENTANA ---
        add(panelSidebar, BorderLayout.WEST);
        add(scrollPane, BorderLayout.CENTER);

        // --- 5. EVENTOS ---
        btnCerrarSesion.addActionListener(e -> {
            sistema.logout();
            inicializarComponentesLogin();
        });
        
        btnNuevo.addActionListener(e -> {
            abrirDialogoNuevaPublicacion();
        });

        revalidate();
        repaint();
    }
    
    // Método auxiliar para crear botones del Sidebar
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
    
    // Método para dibujar un Post individual
    private JPanel crearPanelPost(Publicacion p) {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setMaximumSize(new Dimension(600, 700)); // Ancho fijo Desktop
        
        // Borde suave
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(10, 10, 10, 10),
                BorderFactory.createLineBorder(new Color(230, 230, 230))
        ));

        // A. HEADER (Foto Perfil + Username)
        JPanel header = new JPanel(new FlowLayout(FlowLayout.LEFT));
        header.setBackground(Color.WHITE);
        
        JLabel lblFoto = new JLabel();
        lblFoto.setPreferredSize(new Dimension(40, 40));
        try {
            // Intentar cargar foto del autor si existe
            ImageIcon icono = new ImageIcon(getClass().getResource("/images/default_profile.png"));
            // Si tuvieras la ruta real: new ImageIcon(p.getRutaFotoAutor())
            lblFoto.setIcon(new ImageIcon(icono.getImage().getScaledInstance(40, 40, Image.SCALE_SMOOTH)));
        } catch (Exception e) {
            lblFoto.setText("😎");
        }
        
        JLabel lblUser = new JLabel(p.getAutor());
        lblUser.setFont(new Font("Arial", Font.BOLD, 14));
        
        header.add(lblFoto);
        header.add(lblUser);

        // B. IMAGEN DEL POST
        JLabel lblImagen = new JLabel();
        lblImagen.setPreferredSize(new Dimension(600, 500)); 
        lblImagen.setHorizontalAlignment(SwingConstants.CENTER);
        lblImagen.setBackground(Color.LIGHT_GRAY);
        lblImagen.setOpaque(true);
        
        // Intentar cargar la imagen del post
        try {
            if (p.getRutaImagen() != null && !p.getRutaImagen().isEmpty()) {
                File f = new File(p.getRutaImagen());
                if(f.exists()) {
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

        // C. CONTENIDO TEXTO
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

        // Ensamblar Post
        panel.add(header, BorderLayout.NORTH);
        panel.add(lblImagen, BorderLayout.CENTER);
        panel.add(footer, BorderLayout.SOUTH);

        return panel;
    }
    
    // ---------------------------------------------------------
    // DIÁLOGO: CREAR NUEVA PUBLICACIÓN
    // ---------------------------------------------------------
    private void abrirDialogoNuevaPublicacion() {
        JDialog dialog = new JDialog(this, "Nueva Publicación", true);
        dialog.setSize(450, 400);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());
        dialog.getContentPane().setBackground(COLOR_FONDO);

        // Panel Superior (Imagen)
        JPanel panelImg = new JPanel();
        panelImg.setBackground(COLOR_FONDO);
        JButton btnSeleccionar = new JButton("Seleccionar Imagen");
        JLabel lblRuta = new JLabel("Ninguna imagen seleccionada");
        
        // Panel Central (Texto)
        JTextArea txtContenido = new JTextArea();
        txtContenido.setLineWrap(true);
        txtContenido.setWrapStyleWord(true);
        txtContenido.setFont(new Font("Arial", Font.PLAIN, 13));
        JScrollPane scrollTxt = new JScrollPane(txtContenido);
        scrollTxt.setBorder(BorderFactory.createTitledBorder("Contenido (Max 220 chars)"));

        // Panel Inferior (Botones)
        JPanel panelBotones = new JPanel();
        JButton btnPublicar = new JButton("Publicar");
        JButton btnCancelar = new JButton("Cancelar");
        panelBotones.add(btnPublicar);
        panelBotones.add(btnCancelar);

        // Ruta imagen temporal
        final String[] rutaSeleccionada = {""};

        btnSeleccionar.addActionListener(e -> {
            JFileChooser fc = new JFileChooser();
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
            
            // Hashtags y menciones simulados (podrías parsearlos del texto)
            boolean exito = sistema.crearPublicacion(texto, rutaSeleccionada[0], "", "");
            
            if (exito) {
                JOptionPane.showMessageDialog(dialog, "¡Publicado con éxito!");
                dialog.dispose();
                cargarVistaFeed(); // Recargar el feed para ver el nuevo post
            } else {
                JOptionPane.showMessageDialog(dialog, "Error al guardar la publicación.");
            }
        });

        btnCancelar.addActionListener(e -> dialog.dispose());

        panelImg.add(btnSeleccionar);
        panelImg.add(lblRuta);

        dialog.add(panelImg, BorderLayout.NORTH);
        dialog.add(scrollTxt, BorderLayout.CENTER);
        dialog.add(panelBotones, BorderLayout.SOUTH);

        dialog.setVisible(true);
    }
}