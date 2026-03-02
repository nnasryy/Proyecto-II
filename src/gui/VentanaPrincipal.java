package gui;

import enums.TipoCuenta;
import instagram.Sistema;
import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.*;

public class VentanaPrincipal extends JFrame {

    private Sistema sistema; // Objeto lógico

    // COLORES
    private final Color COLOR_FONDO = Color.WHITE;
    private final Color COLOR_BOTTON = new Color(64, 155, 230);
    private final Color COLOR_FIELD = new Color(242, 247, 247);
    private final Color COLOR_FONT = new Color(143, 140, 140);
    private final Color COLOR_PLACEHOLDER = new Color(180, 180, 180);

    // --- ATRIBUTOS PARA LOS ICONOS (SOLUCIÓN AL ERROR) ---
    private ImageIcon iconEyeClosed;
    private ImageIcon iconEyeOpen;

    public VentanaPrincipal(Sistema sistema) {
        this.sistema = sistema;
        configurarVentana();
        inicializarComponentesLogin();
    }

    private void configurarVentana() {
        // --- MODO DESKTOP FIJO ---
        setSize(1366, 768); 
        setTitle("InstaRAIZ - Desktop");
        setLocationRelativeTo(null); // Centrar
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(null);
        setResizable(false);
    }

    // ---------------------------------------------------------
    // VISTA 1: LOGIN
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

        // --- CAMPO USUARIO ---
        JTextField txtUser = new JTextField();
        txtUser.setBackground(COLOR_FIELD);
        txtUser.setFont(new Font("Arial", Font.BOLD, 14));
        txtUser.setText("Username");
        txtUser.setForeground(COLOR_PLACEHOLDER);
        txtUser.setBorder(BorderFactory.createCompoundBorder(new LineBorder(new Color(200, 200, 200)), BorderFactory.createEmptyBorder(0, 5, 0, 5)));
        
        txtUser.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (txtUser.getText().equals("Username")) { txtUser.setText(""); txtUser.setForeground(COLOR_FONT); }
            }
            @Override
            public void focusLost(FocusEvent e) {
                if (txtUser.getText().isEmpty()) { txtUser.setText("Username"); txtUser.setForeground(COLOR_PLACEHOLDER); }
            }
        });

        // --- PANEL PASSWORD ---
        JPanel panelPass = new JPanel(new BorderLayout());
        panelPass.setBackground(COLOR_FIELD);
        panelPass.setBorder(BorderFactory.createCompoundBorder(new LineBorder(new Color(200, 200, 200)), BorderFactory.createEmptyBorder(0, 5, 0, 5)));

        JPasswordField txtPass = new JPasswordField();
        txtPass.setBackground(COLOR_FIELD); txtPass.setFont(new Font("Arial", Font.BOLD, 14)); txtPass.setBorder(null);
        txtPass.setText("Password"); txtPass.setEchoChar((char) 0); txtPass.setForeground(COLOR_PLACEHOLDER);

        // --- BOTÓN OJO (Ver/Ocultar Contraseña) ---
        JLabel btnEye = new JLabel();
        btnEye.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        // CARGAR ICONOS (Usando los atributos de la clase)
        try {
            ImageIcon rawClosed = new ImageIcon(getClass().getResource("/images/ojocerrado.png"));
            ImageIcon rawOpen = new ImageIcon(getClass().getResource("/images/ojo.png"));
            
            // Escalar y guardar en los atributos de la clase
            Image imgC = rawClosed.getImage().getScaledInstance(20, 20, Image.SCALE_SMOOTH);
            Image imgO = rawOpen.getImage().getScaledInstance(20, 20, Image.SCALE_SMOOTH);
            
            iconEyeClosed = new ImageIcon(imgC);
            iconEyeOpen = new ImageIcon(imgO);
            
            // Asignar icono inicial
            btnEye.setIcon(iconEyeClosed); 
            
        } catch (Exception e) {
             // Si fallan las imágenes, usamos texto
             btnEye.setText("👁"); 
             btnEye.setFont(new Font("Arial", Font.PLAIN, 16));
        }

        // Lógica del ojo
        btnEye.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                String current = String.valueOf(txtPass.getPassword());
                if (current.equals("Password")) return;
                
                // Usamos los atributos de la clase (ya no dan error)
                if (iconEyeClosed != null && iconEyeOpen != null) {
                    if (txtPass.getEchoChar() != 0) { 
                        txtPass.setEchoChar((char) 0);
                        btnEye.setIcon(iconEyeOpen); // Ojo ABIERTO
                    } else { 
                        txtPass.setEchoChar('●');
                        btnEye.setIcon(iconEyeClosed); // Ojo CERRADO
                    }
                } else {
                    // Lógica de respaldo con texto si las imágenes fallaron
                    if (txtPass.getEchoChar() != 0) {
                        txtPass.setEchoChar((char) 0);
                        btnEye.setText("Ocultar");
                    } else {
                        txtPass.setEchoChar('●');
                        btnEye.setText("👁");
                    }
                }
            }
        });

        // Lógica Placeholder Password
        txtPass.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                String current = String.valueOf(txtPass.getPassword());
                if (current.equals("Password")) { txtPass.setText(""); txtPass.setForeground(COLOR_FONT); txtPass.setEchoChar('●'); }
            }
            @Override
            public void focusLost(FocusEvent e) {
                if (txtPass.getPassword().length == 0) { txtPass.setText("Password"); txtPass.setEchoChar((char) 0); txtPass.setForeground(COLOR_PLACEHOLDER); }
            }
        });
        
        panelPass.add(txtPass, BorderLayout.CENTER);
        panelPass.add(btnEye, BorderLayout.EAST);

        // --- BOTÓN LOGIN ---
        JButton btnLogin = new JButton("Iniciar Sesión");
        btnLogin.setBackground(COLOR_BOTTON); btnLogin.setForeground(Color.WHITE);
        btnLogin.setFont(new Font("Arial", Font.BOLD, 14));
        btnLogin.setOpaque(true); btnLogin.setBorderPainted(false);

        // --- ENLACE REGISTRO (Texto con acción) ---
        JLabel lblRegistro = new JLabel("¿Aún no tienes cuenta? Regístrate");
        lblRegistro.setForeground(COLOR_BOTTON);
        lblRegistro.setFont(new Font("Arial", Font.BOLD, 13));
        lblRegistro.setCursor(new Cursor(Cursor.HAND_CURSOR)); 
        lblRegistro.setOpaque(false); 

        // --- POSICIONAMIENTO EXACTO (Desktop 1366x768) ---
        int anchoInputs = 280;
        int altoInput = 40;
        int xInputs = (1366 - anchoInputs) / 2; 

        int yInicio = 200; 
        
        lblLogo.setBounds(xInputs, yInicio, anchoInputs, 158); 
        txtUser.setBounds(xInputs, yInicio + 180, anchoInputs, altoInput); 
        panelPass.setBounds(xInputs, yInicio + 230, anchoInputs, altoInput); 
        btnLogin.setBounds(xInputs, yInicio + 290, anchoInputs, altoInput); 
        lblRegistro.setBounds(xInputs, yInicio + 350, anchoInputs, 30); 

        // --- AGREGAR Y EVENTOS ---
        add(lblLogo); add(txtUser); add(panelPass); add(btnLogin); add(lblRegistro);

        // Evento Login
        btnLogin.addActionListener(e -> {
            String user = txtUser.getText();
            String pass = String.valueOf(txtPass.getPassword());

            if(user.equals("Username") || pass.equals("Password")) {
                JOptionPane.showMessageDialog(this, "Por favor complete los campos", "Error", JOptionPane.WARNING_MESSAGE);
                return;
            }

            boolean exito = sistema.login(user, pass);
            
            if (exito) {
                JOptionPane.showMessageDialog(this, "¡Bienvenido " + sistema.getUsuarioActual().getNombreCompleto() + "!");
                cargarVistaFeed(); 
            } else {
                JOptionPane.showMessageDialog(this, "Usuario o contraseña incorrectos.", "Error de Login", JOptionPane.ERROR_MESSAGE);
            }
        });

        // Evento Click en Texto de Registro
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

        JTextField txtNombre = new JTextField();
        txtNombre.setBackground(COLOR_FIELD); txtNombre.setText("Nombre Completo"); txtNombre.setForeground(COLOR_PLACEHOLDER);

        JTextField txtUser = new JTextField();
        txtUser.setBackground(COLOR_FIELD); txtUser.setText("Username"); txtUser.setForeground(COLOR_PLACEHOLDER);

        JPasswordField txtPass = new JPasswordField();
        txtPass.setBackground(COLOR_FIELD); txtPass.setText("Password"); txtPass.setEchoChar((char) 0); txtPass.setForeground(COLOR_PLACEHOLDER);

        JSpinner spnEdad = new JSpinner(new SpinnerNumberModel(18, 1, 100, 1));
        spnEdad.setFont(new Font("Arial", Font.PLAIN, 14));
        
        JComboBox<String> cmbGenero = new JComboBox<>();
        cmbGenero.addItem("Masculino");
        cmbGenero.addItem("Femenino");
        
        JComboBox<String> cmbTipo = new JComboBox<>();
        cmbTipo.addItem("Pública");
        cmbTipo.addItem("Privada");

        JTextField txtFoto = new JTextField("default_profile.png"); 
        txtFoto.setBackground(COLOR_FIELD);

        JButton btnRegistrar = new JButton("Registrarse");
        btnRegistrar.setBackground(COLOR_BOTTON); btnRegistrar.setForeground(Color.WHITE);
        btnRegistrar.setFont(new Font("Arial", Font.BOLD, 14));
        btnRegistrar.setOpaque(true); btnRegistrar.setBorderPainted(false);

        JLabel btnVolver = new JLabel("¿Ya tienes cuenta? Volver");
        btnVolver.setForeground(COLOR_BOTTON);
        btnVolver.setFont(new Font("Arial", Font.BOLD, 12));
        btnVolver.setCursor(new Cursor(Cursor.HAND_CURSOR));

        int anchoInputs = 280;
        int altoInput = 40;
        int xInputs = (1366 - anchoInputs) / 2;
        int y = 120;

        lblTitulo.setBounds(xInputs, y, anchoInputs, 40); y += 50;
        txtNombre.setBounds(xInputs, y, anchoInputs, altoInput); y += 50;
        txtUser.setBounds(xInputs, y, anchoInputs, altoInput); y += 50;
        txtPass.setBounds(xInputs, y, anchoInputs, altoInput); y += 50;
        
        JLabel lblEdad = new JLabel("Edad:"); lblEdad.setBounds(xInputs, y, 100, 20); 
        spnEdad.setBounds(xInputs, y + 20, 100, 30);
        
        JLabel lblGenero = new JLabel("Género:"); lblGenero.setBounds(xInputs + 140, y, 100, 20);
        cmbGenero.setBounds(xInputs + 140, y + 20, 140, 30);
        y += 60;

        JLabel lblTipo = new JLabel("Tipo Cuenta:"); lblTipo.setBounds(xInputs, y, 100, 20);
        cmbTipo.setBounds(xInputs, y + 20, anchoInputs, 30);
        y += 60;

        btnRegistrar.setBounds(xInputs, y, anchoInputs, altoInput); y += 50;
        btnVolver.setBounds(xInputs, y, anchoInputs, 30);

        add(lblTitulo);
        add(txtNombre); add(txtUser); add(txtPass);
        add(spnEdad); add(cmbGenero); add(lblEdad); add(lblGenero);
        add(cmbTipo); add(lblTipo);
        add(txtFoto);
        add(btnRegistrar); add(btnVolver);

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
            String foto = txtFoto.getText();
            
            TipoCuenta tipo = (cmbTipo.getSelectedIndex() == 0) ? TipoCuenta.PUBLICA : TipoCuenta.PRIVADA;

            if(nombre.equals("Nombre Completo") || user.equals("Username") || pass.equals("Password")) {
                JOptionPane.showMessageDialog(this, "Rellene todos los campos.", "Error", JOptionPane.WARNING_MESSAGE);
                return;
            }

            boolean exito = sistema.registrarUsuario(user, pass, nombre, genero, edad, foto, tipo);

            if (exito) {
                JOptionPane.showMessageDialog(this, "Cuenta creada exitosamente.");
                inicializarComponentesLogin(); 
            } else {
                JOptionPane.showMessageDialog(this, "No se pudo crear el usuario (¿Usuario ya existe?).", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        revalidate();
        repaint();
    }

    // ---------------------------------------------------------
    // VISTA 3: FEED
    // ---------------------------------------------------------
    private void cargarVistaFeed() {
        getContentPane().removeAll();
        repaint();
        JOptionPane.showMessageDialog(this, "Cargando Feed...");
    }
}