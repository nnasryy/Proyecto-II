package gui; // Asegúrate que coincida con tu paquete

import enums.ModoVista;
import instagram.Sistema;
import interfaces.ConfiguracionVisual;
import javax.swing.*;
import java.awt.*;

public class VentanaPrincipal extends JFrame {

    private Sistema sistema;
    private ModoVista modoActual;

    // ------------------------------------------------------
    // MAIN PARA PRUEBAS RÁPIDAS (Ejecuta esto para ver la ventana)
    // ------------------------------------------------------
    public static void main(String[] args) {
        // Esto permite que la ventana se vea moderna (opcional)
        try { 
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); 
        } catch (Exception e) {}

        // Creamos una instancia para visualizar
        // Pasamos 'null' al sistema solo para esta prueba visual
        VentanaPrincipal ventana = new VentanaPrincipal(null);
        ventana.setVisible(true);
    }

    // ------------------------------------------------------
    // CONSTRUCTOR
    // ------------------------------------------------------
    public VentanaPrincipal(Sistema sistema) {
        this.sistema = sistema;

        // --- AQUÍ CAMBIAS EL MODO PARA PROBAR ---
        // Cambia esto a ModoVista.DESKTOP para ver la versión grande
        this.modoActual = ModoVista.MOBILE; 

        // Configuración Básica
        configurarVentana();
        
        // Iniciar componentes (Login por ahora)
        inicializarComponentesLogin();
    }

    private void configurarVentana() {
        // Obtenemos dimensiones de tu interfaz ConfiguracionVisual
        int ancho = ConfiguracionVisual.getAncho(modoActual);
        int alto = ConfiguracionVisual.getAlto(modoActual);

        setTitle("InstaRAIZ - " + modoActual);
        setSize(ancho, alto);
        setLocationRelativeTo(null); // Centrar en pantalla
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        // IMPORTANTE: Esto permite usar coordenadas exactas (x,y)
        setLayout(null); 
        setResizable(false); // Para que el usuario no estire la ventana
    }

    private void inicializarComponentesLogin() {
        
        // LIMPIAMOS LA VENTANA (por si venimos de otra pantalla)
        getContentPane().removeAll();

        // --- Lógica de Colores (Simulando Instagram) ---
        Color colorFondo = (modoActual == ModoVista.MOBILE) ? Color.WHITE : new Color(250, 250, 250);
        Color colorTexto = (modoActual == ModoVista.MOBILE) ? Color.BLACK : Color.BLACK;
        getContentPane().setBackground(colorFondo);

        // --- Componentes ---
        JLabel lblTitulo = new JLabel("INSTAGRAM", SwingConstants.CENTER);
        lblTitulo.setFont(new Font("SansSerif", Font.BOLD, 24));
        lblTitulo.setForeground(colorTexto);
        
        JTextField txtUser = new JTextField();
        txtUser.setToolTipText("Username");
        
        JPasswordField txtPass = new JPasswordField();
        txtPass.setToolTipText("Password");

        JButton btnLogin = new JButton("Iniciar Sesión");
        JButton btnRegistro = new JButton("Registrar");

        // --- POSICIONAMIENTO (Coordenadas) ---
        
        if (modoActual == ModoVista.MOBILE) {
            // DISEÑO MÓVIL (390x844)
            // Centrado horizontal: (390 - 280) / 2 = 55 aprox
            
            lblTitulo.setBounds(55, 200, 280, 50);
            txtUser.setBounds(55, 300, 280, 40); // x, y, ancho, alto
            txtPass.setBounds(55, 350, 280, 40);
            btnLogin.setBounds(55, 410, 280, 40);
            btnRegistro.setBounds(55, 470, 280, 40);
            
        } else {
            // DISEÑO DESKTOP (1366x768)
            // Centrado horizontal: (1366 - 300) / 2 = 533 aprox
            
            lblTitulo.setBounds(533, 200, 300, 50);
            txtUser.setBounds(533, 300, 300, 40);
            txtPass.setBounds(533, 360, 300, 40);
            btnLogin.setBounds(533, 430, 300, 40);
            btnRegistro.setBounds(533, 490, 300, 40);
        }

        // AGREGAR COMPONENTES A LA VENTANA
        add(lblTitulo);
        add(txtUser);
        add(txtPass);
        add(btnLogin);
        add(btnRegistro);

        // --- ACCIONES (Eventos) ---
        btnLogin.addActionListener(e -> {
            if(sistema == null) {
                JOptionPane.showMessageDialog(this, "Modo prueba visual (Sistema no conectado)");
                return;
            }
            // Aquí iría la lógica real
            String user = txtUser.getText();
            String pass = new String(txtPass.getPassword());
            // sistema.login(user, pass);
        });

        // Refrescar la pantalla
        revalidate();
        repaint();
    }
}