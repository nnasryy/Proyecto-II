package gui;

import enums.ModoVista;
import instagram.Sistema;
import interfaces.ConfiguracionVisual;
import javax.swing.*;
import javax.swing.border.LineBorder; // Import estándar para bordes
import java.awt.*;

public class VentanaPrincipal extends JFrame {

    private Sistema sistema;
    private ModoVista modoActual;

    // COLORES
    private final Color COLOR_FONDO = Color.WHITE;
    private final Color COLOR_BOTTON = new Color(64, 155, 230); // Azul claro
    private final Color COLOR_FIELD = new Color(242, 247, 247); //Gris Text Field
    private final Color COLOR_FONT = new Color(143, 140, 140); //Gris Oscuro Text Field
    
    
    public static void main(String[] args) {
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch (Exception e) {}
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
        }

        JTextField txtUser = new JTextField();
        txtUser.setBackground(COLOR_FIELD);
        txtUser.setFont(new Font("Arial", Font.PLAIN, 14));
        txtUser.setForeground(COLOR_FONT);

        JPasswordField txtPass = new JPasswordField();
        txtPass.setBackground(COLOR_FIELD);
        txtPass.setFont(new Font("Arial", Font.PLAIN, 14));
        txtPass.setForeground(COLOR_FONT);

        // --- BOTONES ---
        JButton btnLogin = new JButton("Iniciar Sesión");
        btnLogin.setBackground(COLOR_BOTTON);
        btnLogin.setForeground(Color.WHITE);
        btnLogin.setFont(new Font("Arial", Font.BOLD, 14));

        JButton btnRegistro = new JButton("Registrar");
        btnRegistro.setBackground(COLOR_BOTTON);
        btnRegistro.setForeground(Color.WHITE);
        btnRegistro.setFont(new Font("Arial", Font.BOLD, 14));

        // --- POSICIONAMIENTO ---
        int anchoVentana = getWidth();
        int anchoInputs = 280;
        int anchoLogo = 280;
        int xInputs = (anchoVentana - anchoInputs) / 2;
        int xLogo = (anchoVentana - anchoLogo) / 2;

        if (modoActual == ModoVista.MOBILE) {
            lblLogo.setBounds(xLogo, 80, anchoLogo, 217);
            txtUser.setBounds(xInputs, 350, anchoInputs, 40);
            txtPass.setBounds(xInputs, 410, anchoInputs, 40);
            btnLogin.setBounds(xInputs, 480, anchoInputs, 40);
            btnRegistro.setBounds(xInputs, 540, anchoInputs, 40);
        } else {
            lblLogo.setBounds(xLogo, 100, anchoLogo, 217);
            txtUser.setBounds(xInputs, 350, anchoInputs, 40);
            txtPass.setBounds(xInputs, 420, anchoInputs, 40);
            btnLogin.setBounds(xInputs, 500, anchoInputs, 40);
            btnRegistro.setBounds(xInputs, 560, anchoInputs, 40);
        }

        add(lblLogo);
        add(txtUser);
        add(txtPass);
        add(btnLogin);
        add(btnRegistro);

        // Eventos simples
        btnLogin.addActionListener(e -> JOptionPane.showMessageDialog(this, "Login"));
        
        revalidate();
        repaint();
    }
}