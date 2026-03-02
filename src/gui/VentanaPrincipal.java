package gui;

import enums.ModoVista;
import instagram.Sistema;
import interfaces.ConfiguracionVisual;
import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.*;

public class VentanaPrincipal extends JFrame {

    private Sistema sistema;
    private ModoVista modoActual;

    // COLORES
    private final Color COLOR_FONDO = Color.WHITE;
    private final Color COLOR_BOTTON = new Color(64, 155, 230); // Azul claro
    private final Color COLOR_FIELD = new Color(242, 247, 247); // Gris Text Field
    private final Color COLOR_FONT = new Color(143, 140, 140); // Gris Oscuro Text Field
    private final Color COLOR_PLACEHOLDER = new Color(180, 180, 180); // Gris claro para guías

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
        }
        VentanaPrincipal ventana = new VentanaPrincipal(null);
        ventana.setVisible(true);
    }

    public VentanaPrincipal(Sistema sistema) {
        this.sistema = sistema;
        this.modoActual = ModoVista.MOBILE; // Cambiar a DESKTOP para probar
        configurarVentana();
        inicializarComponentesLogin();
    }

    private void configurarVentana() {
        setSize(ConfiguracionVisual.getAncho(modoActual), ConfiguracionVisual.getAlto(modoActual));
        setTitle("InstaRAIZ - " + modoActual);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(null);
        setResizable(false);
    }

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

        // --- 1. CAMPO USUARIO (Con Placeholder) ---
        JTextField txtUser = new JTextField();
        txtUser.setBackground(COLOR_FIELD);
        txtUser.setFont(new Font("Arial", Font.PLAIN, 14));
        txtUser.setText("Username"); // Texto inicial
        txtUser.setForeground(COLOR_PLACEHOLDER);
        
        // Borde para que coincida con el estilo del campo de contraseña
        txtUser.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(200, 200, 200)),
                BorderFactory.createEmptyBorder(0, 5, 0, 5)
        ));

        // Lógica del Placeholder (User)
        txtUser.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (txtUser.getText().equals("Username")) {
                    txtUser.setText("");
                    txtUser.setForeground(COLOR_FONT);
                }
            }
            @Override
            public void focusLost(FocusEvent e) {
                if (txtUser.getText().isEmpty()) {
                    txtUser.setText("Username");
                    txtUser.setForeground(COLOR_PLACEHOLDER);
                }
            }
        });

        // --- 2. CAMPO CONTRASEÑA (Con Placeholder e Ícono) ---
        
        // Panel contenedor para simular el campo con el ícono adentro
        JPanel panelPass = new JPanel(new BorderLayout());
        panelPass.setBackground(COLOR_FIELD);
        panelPass.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(200, 200, 200)),
                BorderFactory.createEmptyBorder(0, 5, 0, 5)
        ));

        // Campo de contraseña real
        JPasswordField txtPass = new JPasswordField();
        txtPass.setBackground(COLOR_FIELD);
        txtPass.setFont(new Font("Arial", Font.PLAIN, 14));
        txtPass.setBorder(null); // Sin borde propio
        txtPass.setText("Password");
        txtPass.setEchoChar((char) 0); // Visible inicialmente para ver el placeholder
        txtPass.setForeground(COLOR_PLACEHOLDER);

        // Etiqueta del Ojo (Botón Ver/Ocultar)
        JLabel btnEye = new JLabel("👁");
        btnEye.setFont(new Font("Arial", Font.PLAIN, 16));
        btnEye.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Lógica del Placeholder (Password)
        txtPass.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                String current = String.valueOf(txtPass.getPassword());
                if (current.equals("Password")) {
                    txtPass.setText("");
                    txtPass.setForeground(COLOR_FONT);
                    txtPass.setEchoChar('●'); // Ocultar al escribir
                }
            }
            @Override
            public void focusLost(FocusEvent e) {
                if (txtPass.getPassword().length == 0) {
                    txtPass.setText("Password");
                    txtPass.setEchoChar((char) 0); // Mostrar texto guía
                    txtPass.setForeground(COLOR_PLACEHOLDER);
                }
            }
        });

        // Lógica Click Ojo (Mostrar/Ocultar)
        btnEye.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                String current = String.valueOf(txtPass.getPassword());
                // No hacer nada si sigue el placeholder
                if (current.equals("Password")) return;

                if (txtPass.getEchoChar() == 0) {
                    // Si está visible, ocultar
                    txtPass.setEchoChar('●');
                    btnEye.setText("👁"); // Podrías cambiar el ícono aquí
                } else {
                    // Si está oculto, mostrar
                    txtPass.setEchoChar((char) 0);
                    btnEye.setText("👁‍🗨");
                }
            }
        });

        // Ensamblar panel de contraseña
        panelPass.add(txtPass, BorderLayout.CENTER);
        panelPass.add(btnEye, BorderLayout.EAST);

        // --- BOTONES (Con corrección de color) ---
        JButton btnLogin = new JButton("Iniciar Sesión");
        btnLogin.setBackground(COLOR_BOTTON);
        btnLogin.setForeground(Color.WHITE);
        btnLogin.setFont(new Font("Arial", Font.BOLD, 14));
        btnLogin.setOpaque(true);
        btnLogin.setBorderPainted(false);

        JButton btnRegistro = new JButton("Registrar");
        btnRegistro.setBackground(COLOR_BOTTON);
        btnRegistro.setForeground(Color.WHITE);
        btnRegistro.setFont(new Font("Arial", Font.BOLD, 14));
        btnRegistro.setOpaque(true);
        btnRegistro.setBorderPainted(false);

        // --- POSICIONAMIENTO DINÁMICO (Centrado y Unido) ---
        int anchoVentana = getWidth();
        int altoVentana = getHeight();
        int anchoInputs = 280;
        int altoInput = 40;
        int altoLogo = 158;
        
        int espacioEntreInputs = 10;
        int espacioEntreBotones = 10;
        int espacioLogoInput = 20; // Espacio pequeño para "unir" logo hacia abajo

        // Cálculo de altura total del bloque
        int altoTotalBloque = altoLogo + espacioLogoInput + 
                              altoInput + espacioEntreInputs + 
                              altoInput + espacioEntreBotones + 
                              altoInput + espacioEntreBotones + 
                              altoInput;

        int yInicio = (altoVentana - altoTotalBloque) / 2;
        int xInputs = (anchoVentana - anchoInputs) / 2;
        int xLogo = (anchoVentana - 280) / 2;

        int yActual = yInicio;

        // Logo
        lblLogo.setBounds(xLogo, yActual, 280, altoLogo);
        yActual += altoLogo + espacioLogoInput;

        // User
        txtUser.setBounds(xInputs, yActual, anchoInputs, altoInput);
        yActual += altoInput + espacioEntreInputs;

        // Pass (Notese que agregamos 'panelPass' en lugar de 'txtPass')
        panelPass.setBounds(xInputs, yActual, anchoInputs, altoInput);
        yActual += altoInput + espacioEntreBotones;

        // Login
        btnLogin.setBounds(xInputs, yActual, anchoInputs, altoInput);
        yActual += altoInput + espacioEntreBotones;

        // Registro
        btnRegistro.setBounds(xInputs, yActual, anchoInputs, altoInput);

        // --- AGREGAR AL FRAME ---
        add(lblLogo);
        add(txtUser);
        add(panelPass); // Agregamos el panel, no el campo suelto
        add(btnLogin);
        add(btnRegistro);

        // Eventos
        btnLogin.addActionListener(e -> {
            String user = txtUser.getText();
            String pass = String.valueOf(txtPass.getPassword());
            // Validación simple para no loguear con placeholders
            if(user.equals("Username") || pass.equals("Password")) {
                 JOptionPane.showMessageDialog(this, "Por favor complete los campos", "Error", JOptionPane.WARNING_MESSAGE);
            } else {
                 JOptionPane.showMessageDialog(this, "Login exitoso para: " + user);
            }
        });

        revalidate();
        repaint();
    }
}