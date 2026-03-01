package instagram;


import enums.ModoVista;
import interfaces.ConfiguracionVisual;
import javax.swing.*;
import java.awt.*;

public class VentanaPrincipal extends JFrame {

    private Sistema sistema;
    private ModoVista modoActual;

    // ------------------------------------------------------
    // MAIN PARA PRUEBAS RÁPIDAS
    // ------------------------------------------------------
    public static void main(String[] args) {
        try { 
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); 
        } catch (Exception e) {}

        VentanaPrincipal ventana = new VentanaPrincipal(null);
        ventana.setVisible(true);
    }

    public VentanaPrincipal(Sistema sistema) {
        this.sistema = sistema;
        this.modoActual = ModoVista.MOBILE; // Cambia a DESKTOP para probar

        configurarVentana();
        inicializarComponentesLogin();
    }

    private void configurarVentana() {
        int ancho = ConfiguracionVisual.getAncho(modoActual);
        int alto = ConfiguracionVisual.getAlto(modoActual);

        setTitle("InstaRAIZ - " + modoActual);
        setSize(ancho, alto);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(null); 
        setResizable(false);
    }

    private void inicializarComponentesLogin() {
        getContentPane().removeAll();

        Color colorFondo = (modoActual == ModoVista.MOBILE) ? Color.BLACK : new Color(250, 250, 250);
        Color colorTexto = (modoActual == ModoVista.MOBILE) ? Color.WHITE : Color.BLACK;
        getContentPane().setBackground(colorFondo);

        // ------------------------------------------------------
        // LOGO (CON IMAGEN)
        // ------------------------------------------------------
        JLabel lblLogo = new JLabel();
        
        try {
            // Cargar la imagen desde el paquete "images"
            // Nota: La ruta empieza con "/" para indicar la raíz del classpath (Source Packages)
            ImageIcon iconoOriginal = new ImageIcon(getClass().getResource("/images/instagramname.png"));
            
            // Definir tamaño deseado para el logo
            int anchoLogo = 200; 
            int altoLogo = 60;   
            
            // Escalar la imagen para que encaje
            Image imagenEscalada = iconoOriginal.getImage().getScaledInstance(anchoLogo, altoLogo, Image.SCALE_SMOOTH);
            lblLogo.setIcon(new ImageIcon(imagenEscalada));
            
        } catch (Exception e) {
            // Si la imagen no se encuentra, mostramos texto para no dejarlo vacío
            lblLogo.setText("INSTAGRAM");
            lblLogo.setForeground(colorTexto);
            lblLogo.setFont(new Font("SansSerif", Font.BOLD, 30));
            lblLogo.setHorizontalAlignment(SwingConstants.CENTER);
            System.err.println("Error: No se encontró la imagen en /images/instagramname.png");
        }

        // ------------------------------------------------------
        // COMPONENTES DE TEXTO Y BOTONES
        // ------------------------------------------------------
        JTextField txtUser = new JTextField();
        txtUser.setToolTipText("Username");
        
        JPasswordField txtPass = new JPasswordField();
        txtPass.setToolTipText("Password");

        JButton btnLogin = new JButton("Iniciar Sesión");
        JButton btnRegistro = new JButton("Registrar");

        // ------------------------------------------------------
        // POSICIONAMIENTO
        // ------------------------------------------------------
        // Definimos variables para centrar las cosas según el modo
        int anchoVentana = getWidth(); // 390 o 1366
        int anchoInputs = 280; 
        int centerXInputs = (anchoVentana - anchoInputs) / 2; 
        
        // Centro del logo (asumiendo ancho 200)
        int anchoLogo = 200;
        int centerXLogo = (anchoVentana - anchoLogo) / 2;

        if (modoActual == ModoVista.MOBILE) {
            // Posiciones Mobile (Vertical)
            lblLogo.setBounds(centerXLogo, 150, anchoLogo, 60); // x centrado, y=150
            
            txtUser.setBounds(centerXInputs, 280, anchoInputs, 40);
            txtPass.setBounds(centerXInputs, 330, anchoInputs, 40);
            btnLogin.setBounds(centerXInputs, 400, anchoInputs, 40);
            btnRegistro.setBounds(centerXInputs, 460, anchoInputs, 40);
            
        } else {
            // Posiciones Desktop (Horizontal)
            lblLogo.setBounds(centerXLogo, 150, anchoLogo, 60);
            
            txtUser.setBounds(centerXInputs, 300, anchoInputs, 40);
            txtPass.setBounds(centerXInputs, 360, anchoInputs, 40);
            btnLogin.setBounds(centerXInputs, 430, anchoInputs, 40);
            btnRegistro.setBounds(centerXInputs, 490, anchoInputs, 40);
        }

        // AGREGAR A LA VENTANA
        add(lblLogo);
        add(txtUser);
        add(txtPass);
        add(btnLogin);
        add(btnRegistro);

        // Acciones (Eventos) básicas
        btnLogin.addActionListener(e -> {
            // Lógica de login aquí
             JOptionPane.showMessageDialog(this, "Click en Login (Modo prueba)");
        });

        revalidate();
        repaint();
    }
}