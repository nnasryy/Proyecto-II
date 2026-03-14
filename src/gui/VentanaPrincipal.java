package gui;

import enums.EstadoCuenta;
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
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import javax.swing.filechooser.FileNameExtensionFilter;

public class VentanaPrincipal extends JFrame {

    private Sistema sistema;
    private Socket socket;
    private ObjectOutputStream salida;
    private Timer chatTimer;
    private int lastMessageCount = 0;

    // --- COLORES ---
    private final Color COLOR_FONDO = Color.WHITE;
    private final Color COLOR_BOTTON = new Color(64, 155, 230);
    private final Color COLOR_DISABLED = new Color(42, 107, 161);
    private final Color COLOR_TEXT_DISABLED = new Color(200, 202, 204);
    private final Color COLOR_FIELD = new Color(242, 247, 247);
    private final Color COLOR_FONT = new Color(143, 140, 140);
    private final Color COLOR_PLACEHOLDER = new Color(180, 180, 180);
    private final Color COLOR_ERROR = new Color(250, 89, 95);

    // ICONOS LOGIN
    private ImageIcon iconEyeClosed;
    private ImageIcon iconEyeOpen;
    private ImageIcon iconCheck;

    // MAPA PARA ICONOS SIDEBAR
    private Map<String, ImageIcon> sidebarIconsNormal = new HashMap<>();
    private Map<String, ImageIcon> sidebarIconsBold = new HashMap<>();

    // VARIABLE PARA SABER EN QUÉ VISTA ESTAMOS
    private String vistaActual = "Home";

    public VentanaPrincipal(Sistema sistema) {
        this.sistema = sistema;
        cargarIconos();
        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (sistema.getUsuarioActual() != null) {
                    sistema.logout();
                }
            }
        });
        configurarVentana();
        inicializarComponentesLogin();
    }

    private void cargarIconos() {
        try {
            // Iconos Login
            iconEyeClosed = new ImageIcon(getClass().getResource("/images/ojocerrado.png"));
            iconEyeOpen = new ImageIcon(getClass().getResource("/images/ojo.png"));
            iconCheck = new ImageIcon(getClass().getResource("/images/check.png"));
        } catch (Exception e) {
            System.out.println("Error cargando iconos básicos: " + e.getMessage());
        }

        // CACHÉ DE ICONOS SIDEBAR
        // Asegúrate que los archivos coincidan exactamente con estos nombres en tu carpeta images
        int size = 24; // Tamaño ideal para sidebar

        // Estructura: (Key, NombreNormal, NombreBold, Tamaño)
        cargarIconoSidebar("Home", "homeicon.png", "bhomeicon.png", size);
        cargarIconoSidebar("Search", "searchicon.png", "bsearchicon.png", size);
        cargarIconoSidebar("Messages", "messageicon.png", "bmessageicon.png", size);
        cargarIconoSidebar("Create", "createicon.png", "bcreateicon.png", size);
        cargarIconoSidebar("Notifications", "hearticon.png", "bhearticon.png", size);
        cargarIconoSidebar("Profile", "profileicon.png", "bprofileicon.png", size);
        cargarIconoSidebar("Like", "hearticon.png", "bhearticon.png", size);
        cargarIconoSidebar("Comment", "commenticon.png", "commenticon.png", size);
        cargarIconoSidebar("Share", "messageicon.png", "bmessageicon.png", size);

    }

    private void cargarIconoSidebar(String key, String normalName, String boldName, int size) {
        try {
            // Cargar Normal
            URL urlNormal = getClass().getResource("/images/" + normalName);
            if (urlNormal != null) {
                ImageIcon icon = new ImageIcon(urlNormal);
                Image img = icon.getImage().getScaledInstance(size, size, Image.SCALE_SMOOTH);
                sidebarIconsNormal.put(key, new ImageIcon(img));
            } else {
                System.err.println("Archivo no encontrado: " + normalName);
            }

            // Cargar Bold
            URL urlBold = getClass().getResource("/images/" + boldName);
            if (urlBold != null) {
                ImageIcon icon = new ImageIcon(urlBold);
                Image img = icon.getImage().getScaledInstance(size, size, Image.SCALE_SMOOTH);
                sidebarIconsBold.put(key, new ImageIcon(img));
            } else {
                System.err.println("Archivo no encontrado: " + boldName);
            }
        } catch (Exception e) {
            System.err.println("Error cargando icono sidebar " + key + ": " + e.getMessage());
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
    // VISTA 1: LOGIN
    // ---------------------------------------------------------
    // ---------------------------------------------------------
    private void inicializarComponentesLogin() {
        if (chatTimer != null) {
            chatTimer.stop();
        }
        getContentPane().removeAll();
        getContentPane().setLayout(null);
        getContentPane().setBackground(COLOR_FONDO);

        JLabel lblLogo = new JLabel();
        try {
            ImageIcon icono = new ImageIcon(getClass().getResource("/images/instagramlogoblack.png"));
            Image img = icono.getImage().getScaledInstance(280, 158, Image.SCALE_SMOOTH);
            lblLogo.setIcon(new ImageIcon(img));
        } catch (Exception e) {
            lblLogo.setText("INSTAGRAM");
            lblLogo.setHorizontalAlignment(SwingConstants.LEFT);
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

        JPasswordField txtPass = (JPasswordField) panelPass.getComponent(0);

        DocumentListener validationListener = new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                validar();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                validar();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                validar();
            }

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
                if (userValido) {
                    resetBorder(txtUser);
                }
                if (passValida) {
                    resetBorder(panelPass);
                }
                lblErrorUser.setText("");
                lblErrorPass.setText("");
            }
        };

        txtUser.getDocument().addDocumentListener(validationListener);
        txtPass.getDocument().addDocumentListener(validationListener);

        // --- ACCIÓN DEL BOTÓN LOGIN ---
        btnLogin.addActionListener(e -> {
            String user = txtUser.getText();
            String pass = String.valueOf(txtPass.getPassword());

            // ESCENARIO 1: Verificar sesión duplicada
            if (sistema.sesionActiva(user)) {
                JOptionPane.showMessageDialog(this,
                        "Este usuario ya tiene una sesión activa.\nPor favor, cierre la otra ventana primero.",
                        "Sesión Duplicada", JOptionPane.WARNING_MESSAGE);
                return;
            }

            // Validación normal de usuario
            Usuario u = sistema.buscarUsuario(user);
            if (u == null) {
                lblErrorUser.setText("El usuario no existe.");
                ponerBordeRojo(txtUser);
                return;
            }

            // Validación de cuenta desactivada
            if (u.getEstadoCuenta() == EstadoCuenta.DESACTIVADO) {
                int opcion = JOptionPane.showConfirmDialog(this,
                        "Esta cuenta está desactivada. ¿Desea reactivarla?",
                        "Cuenta Inactiva", JOptionPane.YES_NO_OPTION);

                if (opcion == JOptionPane.YES_OPTION) {
                    sistema.reactivarCuenta(user);
                } else {
                    return;
                }
            }

            // Intento de Login
            boolean passCorrecta = sistema.login(user, pass);
            if (!passCorrecta) {
                lblErrorPass.setText("La contraseña es incorrecta.");
                ponerBordeRojo(panelPass);
                return;
            }

            // Si todo OK, cargar vista
            cargarVistaFeed();
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

        // --- NUEVO: SELECTOR DE FOTO ---
        JLabel lblFotoPreview = new JLabel();
        lblFotoPreview.setHorizontalAlignment(SwingConstants.CENTER);
        lblFotoPreview.setOpaque(true);
        lblFotoPreview.setBackground(new Color(230, 230, 230));
        lblFotoPreview.setBorder(new LineBorder(new Color(200, 200, 200), 2, true));
        lblFotoPreview.setCursor(new Cursor(Cursor.HAND_CURSOR));
        lblFotoPreview.setToolTipText("Haz clic para subir foto");

        // Variable temporal para guardar el archivo seleccionado
        final File[] archivoSeleccionado = {null};

        // Acción para seleccionar imagen
        lblFotoPreview.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                JFileChooser fc = new JFileChooser();
                fc.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Imágenes", "jpg", "png", "jpeg"));
                int res = fc.showOpenDialog(null);
                if (res == JFileChooser.APPROVE_OPTION) {
                    archivoSeleccionado[0] = fc.getSelectedFile();
                    // Mostrar preview
                    ImageIcon icon = new ImageIcon(archivoSeleccionado[0].getAbsolutePath());
                    Image img = icon.getImage().getScaledInstance(100, 100, Image.SCALE_SMOOTH);
                    lblFotoPreview.setIcon(new ImageIcon(img));
                    lblFotoPreview.setText("");
                }
            }
        });

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

        int anchoInputs = 320;
        int altoInput = 36;
        int xInputs = (1366 - anchoInputs) / 2;
        int y = 80;

        lblTitulo.setBounds(xInputs, y, anchoInputs, 40);
        y += 50;

        // Posicionar preview de foto
        int anchoFoto = 100;
        lblFotoPreview.setBounds(xInputs + (anchoInputs - anchoFoto) / 2, y, anchoFoto, anchoFoto);
        lblFotoPreview.setText("📷"); // Texto inicial
        lblFotoPreview.setFont(new Font("Arial", Font.PLAIN, 30));
        y += 120; // Espacio debajo de la foto

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
        add(lblFotoPreview); // Añadir preview
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

        JTextField txtUser = (JTextField) panelUser.getComponent(0);
        JPasswordField txtPass = (JPasswordField) panelPass.getComponent(0);
        JLabel lblIconoCheck = (JLabel) panelUser.getComponent(1);

        DocumentListener valListener = new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                validarTodo();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                validarTodo();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                validarTodo();
            }

            private void validarTodo() {
                boolean todoOK = true;
                String nombre = txtNombre.getText();
                if (nombre.equals("Nombre Completo") || nombre.isEmpty()) {
                    todoOK = false;
                }

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
                    if (iconCheck != null) {
                        lblIconoCheck.setIcon(iconCheck);
                    } else {
                        lblIconoCheck.setText("✓");
                    }
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

    String rutaFoto;

    // --- LÓGICA CORREGIDA DE FOTO ---
    if (archivoSeleccionado[0] != null) {
        // Usamos el método que RECORTA en cuadrado (ideal para perfil)
        String nombreImg = "profile_" + System.currentTimeMillis();
        rutaFoto = sistema.procesarImagenPerfil(archivoSeleccionado[0], user, nombreImg);
    } else {
        rutaFoto = ""; // Avatar por defecto
    }

    boolean exito = sistema.registrarUsuario(user, pass, nombre, genero, edad, rutaFoto, tipo);
    if (exito) {
        sistema.login(user, pass);
        cargarVistaFeed();
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
        txt.setBorder(BorderFactory.createCompoundBorder(new LineBorder(new Color(200, 200, 200)), BorderFactory.createEmptyBorder(0, 10, 0, 10)));
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
        if (iconEyeClosed != null) {
            btnEye.setIcon(iconEyeClosed);
        } else {
            btnEye.setText("👁");
        }
        btnEye.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (String.valueOf(txtPass.getPassword()).equals("Password")) {
                    return;
                }
                if (txtPass.getEchoChar() != 0) {
                    txtPass.setEchoChar((char) 0);
                    if (iconEyeOpen != null) {
                        btnEye.setIcon(iconEyeOpen);
                    } else {
                        btnEye.setText("Ocultar");
                    }
                } else {
                    txtPass.setEchoChar('●');
                    if (iconEyeClosed != null) {
                        btnEye.setIcon(iconEyeClosed);
                    } else {
                        btnEye.setText("👁");
                    }
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
            c.setBorder(BorderFactory.createCompoundBorder(new LineBorder(new Color(200, 200, 200)), BorderFactory.createEmptyBorder(0, 10, 0, 10)));
        } else if (c instanceof JSpinner) {
            ((JSpinner.DefaultEditor) ((JSpinner) c).getEditor()).getTextField().setBorder(null);
        }
    }

    // ---------------------------------------------------------
    // VISTA 3: FEED
    // ---------------------------------------------------------
    private void cargarVistaFeed() {
        vistaActual = "Home"; // IMPORTANTE: Definir vista actual
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
                panelContenido.add(crearPanelPost(p));
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
                return jbutton;
            }
        });
    }
//POST
private JPanel crearPanelPost(Publicacion p) {
    // 1. DEFINIR DIMENSIONES FIJAS DESKTOP (Punto 4.1)
    int anchoImg = 600;
    int altoFinal = 600; // Default: Cuadrada (600x600)

    // Lógica para detectar si es Vertical u Horizontal
    try {
        if (p.getRutaImagen() != null && !p.getRutaImagen().isEmpty()) {
            File f = new File(p.getRutaImagen());
            if (f.exists()) {
                ImageIcon iconoOriginal = new ImageIcon(p.getRutaImagen());
                int anchoOrig = iconoOriginal.getIconWidth();
                int altoOrig = iconoOriginal.getIconHeight();
                
                if (anchoOrig > 0) {
                    double ratio = (double) altoOrig / anchoOrig;
                    
                    if (ratio > 1.1) { 
                        altoFinal = 750; // Vertical (600x750)
                    } else if (ratio < 0.9) { 
                        altoFinal = 400; // Horizontal (600x400)
                    } else { 
                        altoFinal = 600; // Cuadrada
                    }
                }
            }
        }
    } catch (Exception e) {
        System.out.println("Error detectando dimensiones: " + e.getMessage());
    }

    JPanel panel = new JPanel(new BorderLayout());
    panel.setBackground(Color.WHITE);
    // Ajustamos el tamaño máximo del panel según la imagen calculada
    panel.setMaximumSize(new Dimension(anchoImg, altoFinal + 200)); 
    panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createEmptyBorder(10, 10, 10, 10),
            BorderFactory.createLineBorder(new Color(230, 230, 230))));

    // 2. HEADER (Autor y Opciones)
    // Usamos BorderLayout para poner el autor a la izquierda y opciones a la derecha
    JPanel header = new JPanel(new BorderLayout());
    header.setBackground(Color.WHITE);
    
    // Panel izquierdo: Foto + Nombre
    JPanel userInfo = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
    userInfo.setBackground(Color.WHITE);

    JLabel lblFoto = new JLabel();
    lblFoto.setPreferredSize(new Dimension(40, 40));
    ImageIcon iconoCircular = cargarFotoPerfil(p.getAutor(), 40);
    if (iconoCircular != null) {
        lblFoto.setIcon(iconoCircular);
    } else {
        lblFoto.setIcon(crearAvatarDefault(p.getAutor(), 40));
    }
    lblFoto.setOpaque(false); 

    JLabel lblUser = new JLabel(p.getAutor());
    lblUser.setFont(new Font("Arial", Font.BOLD, 14));
    lblUser.setCursor(new Cursor(Cursor.HAND_CURSOR));
    lblUser.addMouseListener(new MouseAdapter() {
        @Override
        public void mouseClicked(MouseEvent e) {
            cargarVistaPerfil(p.getAutor());
        }
    });
    
    userInfo.add(lblFoto);
    userInfo.add(lblUser);
    header.add(userInfo, BorderLayout.WEST);

    // --- NUEVO: BOTÓN ELIMINAR (Solo si soy el autor) ---
    if (sistema.getUsuarioActual() != null && p.getAutor().equals(sistema.getUsuarioActual().getUsername())) {
        JButton btnOpciones = new JButton("⋯"); // Icono de tres puntos
        btnOpciones.setFont(new Font("Arial", Font.BOLD, 18));
        btnOpciones.setBorderPainted(false);
        btnOpciones.setContentAreaFilled(false);
        btnOpciones.setFocusPainted(false);
        btnOpciones.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JPopupMenu menuOpciones = new JPopupMenu();
        JMenuItem itemEliminar = new JMenuItem("Eliminar Publicación");
        itemEliminar.setForeground(COLOR_ERROR); // Rojo
        itemEliminar.setFont(new Font("Arial", Font.BOLD, 12));
        
        itemEliminar.addActionListener(ev -> {
            int confirm = JOptionPane.showConfirmDialog(this, 
                "¿Estás seguro de que deseas eliminar esta publicación?", 
                "Confirmar Eliminación", JOptionPane.YES_NO_OPTION);
            
            if (confirm == JOptionPane.YES_OPTION) {
                boolean exito = sistema.eliminarPublicacion(p);
                if (exito) {
                    // Recargar la vista actual (Feed o Perfil)
                    if (vistaActual.equals("Profile")) {
                        cargarVistaPerfil(sistema.getUsuarioActual().getUsername());
                    } else {
                        cargarVistaFeed();
                    }
                } else {
                    JOptionPane.showMessageDialog(this, "No se pudo eliminar la publicación.");
                }
            }
        });
        
        menuOpciones.add(itemEliminar);
        
        btnOpciones.addActionListener(ev -> {
            menuOpciones.show(btnOpciones, 0, btnOpciones.getHeight());
        });
        
        header.add(btnOpciones, BorderLayout.EAST);
    }

    // 3. IMAGEN
    JLabel lblImagen = new JLabel();
    lblImagen.setHorizontalAlignment(SwingConstants.CENTER);
    lblImagen.setBackground(Color.LIGHT_GRAY);
    lblImagen.setOpaque(true);
    
    try {
        if (p.getRutaImagen() != null && !p.getRutaImagen().isEmpty()) {
            File f = new File(p.getRutaImagen());
            if (f.exists()) {
                ImageIcon iconoOriginal = new ImageIcon(p.getRutaImagen());
                Image img = iconoOriginal.getImage().getScaledInstance(anchoImg, altoFinal, Image.SCALE_SMOOTH);
                lblImagen.setIcon(new ImageIcon(img));
                lblImagen.setPreferredSize(new Dimension(anchoImg, altoFinal));
            } else {
                lblImagen.setText("Imagen no encontrada");
                lblImagen.setPreferredSize(new Dimension(anchoImg, 400));
            }
        } else {
            lblImagen.setText("Sin Imagen");
            lblImagen.setPreferredSize(new Dimension(anchoImg, 400));
        }
    } catch (Exception e) {
        lblImagen.setText("Error img");
        lblImagen.setPreferredSize(new Dimension(anchoImg, 400));
    }

    // 4. ACCIONES (LIKE, COMMENT, SHARE)
    JPanel panelAcciones = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 5));
    panelAcciones.setBackground(Color.WHITE);

    // --- BOTÓN LIKE ---
    JButton btnLike = new JButton();
    btnLike.setBorderPainted(false);
    btnLike.setBackground(Color.WHITE);
    btnLike.setFocusPainted(false);
    btnLike.setCursor(new Cursor(Cursor.HAND_CURSOR));

    boolean tieneLike = sistema.yaDioLike(p.getAutor(), p.getFecha().toString());
    actualizarIconoLike(btnLike, tieneLike);

    btnLike.addActionListener(ev -> {
        boolean estadoActual = sistema.toggleLike(p.getAutor(), p.getFecha().toString());
        actualizarIconoLike(btnLike, estadoActual);
    });

    // --- BOTÓN COMENTAR ---
    JButton btnComment = new JButton();
    if (sidebarIconsNormal.containsKey("Comment")) {
        btnComment.setIcon(sidebarIconsNormal.get("Comment"));
    } else {
        btnComment.setText("💬");
    }
    btnComment.setBorderPainted(false);
    btnComment.setBackground(Color.WHITE);
    btnComment.setFocusPainted(false);
    btnComment.setCursor(new Cursor(Cursor.HAND_CURSOR));
    btnComment.addActionListener(ev -> abrirDialogoComentarios(p));

    // --- BOTÓN SHARE ---
    JButton btnShare = new JButton();
    if (sidebarIconsNormal.containsKey("Share")) {
        btnShare.setIcon(sidebarIconsNormal.get("Share"));
    } else {
        btnShare.setText("➤");
    }
    btnShare.setBorderPainted(false);
    btnShare.setBackground(Color.WHITE);
    btnShare.setFocusPainted(false);
    btnShare.setCursor(new Cursor(Cursor.HAND_CURSOR));

    btnShare.addActionListener(ev -> {
        String destino = JOptionPane.showInputDialog(this, "¿A qué usuario quieres enviar este post?");
        if (destino != null && !destino.isEmpty()) {
            if (!sistema.puedeCompartirPost(destino, p.getAutor())) {
                JOptionPane.showMessageDialog(this, 
                    "No puedes compartir este post. El autor tiene cuenta privada y no son mutuales.", 
                    "Error de Privacidad", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            sistema.compartirPost(destino, p.getAutor(), p.getRutaImagen(), p.getContenido());
            JOptionPane.showMessageDialog(this, "Post enviado a " + destino);
            
            if (sidebarIconsBold.containsKey("Share")) btnShare.setIcon(sidebarIconsBold.get("Share"));
            Timer t = new Timer(1000, e -> { 
                if(sidebarIconsNormal.containsKey("Share")) btnShare.setIcon(sidebarIconsNormal.get("Share")); 
            });
            t.setRepeats(false); 
            t.start();
        }
    });
    
    panelAcciones.add(btnLike);
    panelAcciones.add(btnComment);
    panelAcciones.add(btnShare);

     // 5. FOOTER (Contenido y Fecha) - MODIFICADO PARA CLICS
    JPanel footer = new JPanel();
    footer.setLayout(new BoxLayout(footer, BoxLayout.Y_AXIS));
    footer.setBackground(Color.WHITE);
    footer.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

    // Usamos JEditorPane para detectar clics en HTML
    JEditorPane editorContenido = new JEditorPane();
    editorContenido.setContentType("text/html");
    editorContenido.setEditable(false);
    editorContenido.setOpaque(false);
    
    // Convertimos el texto a HTML con enlaces
    String contenidoHtml = convertirTextoAHtml(p.getContenido());
    String textoFinal = "<html><font face='Arial' size='3'><b>" + p.getAutor() + "</b> " + contenidoHtml + "</font></html>";
    editorContenido.setText(textoFinal);
    
    // Listener para detectar clics en los enlaces
    editorContenido.addHyperlinkListener(e -> {
        if (e.getEventType() == javax.swing.event.HyperlinkEvent.EventType.ACTIVATED) {
            String enlace = e.getDescription();
            if (enlace.startsWith("#")) {
                // Buscar Hashtag
                cargarVistaBusquedaHashtag(enlace);
            } else if (enlace.startsWith("@")) {
                // Ir al perfil
                String username = enlace.replace("@", "");
                cargarVistaPerfil(username);
            }
        }
    });

    JLabel lblFecha = new JLabel(p.getFecha().toString() + " " + p.getHoraFormateada());
    lblFecha.setForeground(COLOR_FONT);
    lblFecha.setFont(new Font("Arial", Font.ITALIC, 11));

    footer.add(panelAcciones); 
    footer.add(editorContenido);
    footer.add(lblFecha);

    panel.add(header, BorderLayout.NORTH);
    panel.add(lblImagen, BorderLayout.CENTER);
    panel.add(footer, BorderLayout.SOUTH);
    return panel;
}

    // Método auxiliar para cambiar el icono del like
    private void actualizarIconoLike(JButton btn, boolean activo) {
        if (activo) {
            if (sidebarIconsBold.containsKey("Like")) {
                btn.setIcon(sidebarIconsBold.get("Like"));
            } else {
                btn.setText("❤️");
            }
        } else {
            if (sidebarIconsNormal.containsKey("Like")) {
                btn.setIcon(sidebarIconsNormal.get("Like"));
            } else {
                btn.setText("🤍");
            }
        }
    }

    // Método auxiliar para abrir el diálogo de comentarios
    private void abrirDialogoComentarios(Publicacion p) {
        JDialog dialog = new JDialog(this, "Comentarios", false); // false = no modal para ver el post
        dialog.setSize(400, 500);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());

        // Lista de comentarios
        DefaultListModel<String> model = new DefaultListModel<>();
        JList<String> lista = new JList<>(model);
        JScrollPane scroll = new JScrollPane(lista);

        // Cargar comentarios existentes
        ArrayList<String> comments = sistema.getComentarios(p.getAutor(), p.getFecha().toString());
        for (String c : comments) {
            model.addElement(c);
        }

        // Input para nuevo comentario
        JPanel panelInput = new JPanel(new BorderLayout());
        JTextField txtComment = new JTextField();
        JButton btnSend = new JButton("Publicar");
        btnSend.setBackground(COLOR_BOTTON);
        btnSend.setForeground(Color.WHITE);

        btnSend.addActionListener(e -> {
            if (!txtComment.getText().isEmpty()) {
                sistema.agregarComentario(p.getAutor(), p.getFecha().toString(), txtComment.getText());
                model.addElement(sistema.getUsuarioActual().getUsername() + ": " + txtComment.getText());
                txtComment.setText("");
            }
        });

        panelInput.add(txtComment, BorderLayout.CENTER);
        panelInput.add(btnSend, BorderLayout.EAST);

        dialog.add(scroll, BorderLayout.CENTER);
        dialog.add(panelInput, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

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

    // --- ACCIÓN SELECCIONAR IMAGEN CON FILTRO ---
    btnSeleccionar.addActionListener(e -> {
        JFileChooser fc = new JFileChooser();
        // FILTRO: Solo aceptar imágenes
        FileNameExtensionFilter filter = new FileNameExtensionFilter("Archivos de Imagen", "jpg", "png", "jpeg", "gif");
        fc.setFileFilter(filter);
        fc.setAcceptAllFileFilterUsed(false); // Desactivar opción "Todos los archivos"

        int res = fc.showOpenDialog(this);
        if (res == JFileChooser.APPROVE_OPTION) {
            // Validación extra por si acaso
            String nombre = fc.getSelectedFile().getName().toLowerCase();
            if (nombre.endsWith(".jpg") || nombre.endsWith(".png") || nombre.endsWith(".jpeg") || nombre.endsWith(".gif")) {
                rutaSeleccionada[0] = fc.getSelectedFile().getAbsolutePath();
                lblRuta.setText(fc.getSelectedFile().getName());
            } else {
                JOptionPane.showMessageDialog(this, "Solo se permiten archivos JPG, PNG, JPEG o GIF.");
            }
        }
    });

    // --- ACCIÓN PUBLICAR ---
    btnPublicar.addActionListener(e -> {
        String texto = txtContenido.getText();
        if (texto.isEmpty() || rutaSeleccionada[0].isEmpty()) {
            JOptionPane.showMessageDialog(dialog, "Debes escribir algo y seleccionar una imagen.");
            return;
        }

        // Extraer Hashtags y Menciones
        StringBuilder hashtags = new StringBuilder();
        StringBuilder menciones = new StringBuilder();
        for (String palabra : texto.split(" ")) {
            if (palabra.startsWith("#")) hashtags.append(palabra).append(" ");
            else if (palabra.startsWith("@")) menciones.append(palabra).append(" ");
        }

        String nombreImg = "post_" + System.currentTimeMillis();
        File archivoOriginal = new File(rutaSeleccionada[0]);
        String rutaFinal = sistema.procesarYGuardarImagen(archivoOriginal, sistema.getUsuarioActual().getUsername(), nombreImg);

        if (rutaFinal != null) {
            boolean exito = sistema.crearPublicacion(texto, rutaFinal, hashtags.toString().trim(), menciones.toString().trim());
            if (exito) {
                JOptionPane.showMessageDialog(dialog, "¡Publicado con éxito!");
                dialog.dispose();
                cargarVistaFeed();
            } else {
                JOptionPane.showMessageDialog(dialog, "Error al guardar la publicación.");
            }
        } else {
            JOptionPane.showMessageDialog(dialog, "Error al procesar la imagen.");
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

        // LOGO
        JLabel lblLogoSide = new JLabel();
        try {
            ImageIcon logoIcon = new ImageIcon(getClass().getResource("/images/instagramlogoblack.png"));
            Image img = logoIcon.getImage().getScaledInstance(120, 70, Image.SCALE_SMOOTH);
            lblLogoSide.setIcon(new ImageIcon(img));
        } catch (Exception e) {
            lblLogoSide.setText("Instagram");
            lblLogoSide.setFont(new Font("Arial", Font.BOLD, 24));
        }
        lblLogoSide.setAlignmentX(Component.CENTER_ALIGNMENT);
        lblLogoSide.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));

        // CREAR BOTONES
        // Pasamos la clave (Key) para identificar qué icono usar
        JButton btnInicio = crearBotonSidebar("Inicio", "Home");
        JButton btnBuscar = crearBotonSidebar("Buscar", "Search");
        JButton btnMensajes = crearBotonSidebar("Mensajes", "Messages");
        JButton btnNotificaciones = crearBotonSidebar("Notificaciones", "Notifications");
        JButton btnNuevo = crearBotonSidebar("Crear", "Create");
        JButton btnPerfil = crearBotonSidebar("Mi Perfil", "Profile");
        JButton btnCerrarSesion = crearBotonSidebar("Cerrar Sesión", null); // Sin icono

        panelSidebar.add(lblLogoSide);
        panelSidebar.add(btnInicio);
        panelSidebar.add(btnBuscar);
        panelSidebar.add(btnMensajes);
        panelSidebar.add(btnNuevo);
        panelSidebar.add(btnNotificaciones);
        panelSidebar.add(btnPerfil);
        panelSidebar.add(Box.createVerticalGlue());
        panelSidebar.add(btnCerrarSesion);
        panelSidebar.add(Box.createVerticalStrut(20));

        // EVENTOS
        btnInicio.addActionListener(e -> cargarVistaFeed());
        btnBuscar.addActionListener(e -> cargarVistaBusqueda());
        btnMensajes.addActionListener(e -> cargarVistaInbox());
        btnNotificaciones.addActionListener(e -> cargarVistaNotificaciones());
        btnNuevo.addActionListener(e -> abrirDialogoNuevaPublicacion());
        btnPerfil.addActionListener(e -> cargarVistaPerfil(sistema.getUsuarioActual().getUsername()));
        btnCerrarSesion.addActionListener(e -> {
            if (chatTimer != null) {
                chatTimer.stop();
            }
            sistema.logout();
            inicializarComponentesLogin();
        });

        return panelSidebar;
    }

    private JButton crearBotonSidebar(String texto, String key) {
        JButton btn = new JButton("  " + texto);
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);
        btn.setMaximumSize(new Dimension(230, 45));
        btn.setFont(new Font("Arial", Font.PLAIN, 14));
        btn.setBorderPainted(false);
        btn.setBackground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setHorizontalAlignment(SwingConstants.LEFT);

        // LÓGICA DE ICONOS ACTIVOS
        if (key != null) {
            // Si la vista actual coincide con el botón, usamos el BOLD
            if (vistaActual.equals(key) && sidebarIconsBold.containsKey(key)) {
                btn.setIcon(sidebarIconsBold.get(key));
                btn.setFont(new Font("Arial", Font.BOLD, 14)); // Opcional: texto en negrita
            } // Si no, usamos el normal
            else if (sidebarIconsNormal.containsKey(key)) {
                btn.setIcon(sidebarIconsNormal.get(key));
            }
        }

        return btn;
    }

    // ---------------------------------------------------------
    // VISTA 4: PERFIL
    // ---------------------------------------------------------
    private void cargarVistaPerfil(String usernameVisitar) {
        vistaActual = "Profile"; // Definir vista actual
         if (sistema.getUsuarioActual() != null && usernameVisitar.equals(sistema.getUsuarioActual().getUsername())) {
        vistaActual = "Profile"; // Solo ponemos "Profile" si ES mi perfil
    } else {
        vistaActual = ""; // Si es ajeno, no marcamos ningún botón del sidebar
    }
        getContentPane().removeAll();
        setLayout(new BorderLayout());
        getContentPane().setBackground(COLOR_FONDO);
        add(crearPanelSidebar(), BorderLayout.WEST); // Recrear sidebar con nuevo estado

        JPanel panelContenido = new JPanel(new BorderLayout());
        panelContenido.setBackground(COLOR_FONDO);
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(COLOR_FONDO);
        header.setBorder(BorderFactory.createEmptyBorder(40, 50, 20, 50));

        // --- FOTO DE PERFIL ---
        JLabel lblFoto = new JLabel();
        lblFoto.setPreferredSize(new Dimension(150, 150));
        lblFoto.setHorizontalAlignment(SwingConstants.CENTER);
        lblFoto.setOpaque(false);
        ImageIcon iconoCircular = cargarFotoPerfil(usernameVisitar, 150);
        if (iconoCircular != null) {
            lblFoto.setIcon(iconoCircular);
        } else {
         lblFoto.setIcon(crearAvatarDefault(usernameVisitar, 150));
        }
        lblFoto.setBorder(new LineBorder(new Color(230, 230, 230), 2, true));
        header.add(lblFoto, BorderLayout.WEST);

        // --- PANEL DE INFORMACIÓN (Derecha de la foto) ---
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setBackground(COLOR_FONDO);

        // FILA 1: Username y Botones
        JPanel fila1 = new JPanel(new FlowLayout(FlowLayout.LEFT));
        fila1.setBackground(COLOR_FONDO);
        JLabel lblUsername = new JLabel(usernameVisitar);
        lblUsername.setFont(new Font("Arial", Font.BOLD, 24));
        fila1.add(lblUsername);

        // Lógica de botones según si es mi perfil o ajeno
        // REEMPLAZA EL BLOQUE DEL BOTÓN SEGUIR EN cargarVistaPerfil
        if (!usernameVisitar.equals(sistema.getUsuarioActual().getUsername())) {
            // --- PERFIL AJENO ---
            JButton btnSeguir = new JButton();
            btnSeguir.setFont(new Font("Arial", Font.BOLD, 12));
            btnSeguir.setBorderPainted(false);
            btnSeguir.setOpaque(true);
            btnSeguir.setCursor(new Cursor(Cursor.HAND_CURSOR));

            // 1. Verificar estados
            boolean loSigo = sistema.yaLoSigo(usernameVisitar);
            boolean pendiente = sistema.solicitudPendiente(usernameVisitar);
            Usuario objetivo = sistema.buscarUsuario(usernameVisitar);

            // 2. Configurar texto y color según estado
            if (loSigo) {
                btnSeguir.setText("Dejar de seguir");
                btnSeguir.setBackground(new Color(240, 240, 240));
                btnSeguir.setForeground(Color.BLACK);
            } else if (pendiente) {
                btnSeguir.setText("Solicitud enviada");
                btnSeguir.setBackground(new Color(240, 240, 240));
                btnSeguir.setForeground(Color.GRAY);
                btnSeguir.setEnabled(false); // Opcional: desactivar el botón
            } else {
                btnSeguir.setText("Seguir");
                btnSeguir.setBackground(COLOR_BOTTON);
                btnSeguir.setForeground(Color.WHITE);
            }

            // 3. Acción del botón
            btnSeguir.addActionListener(e -> {
                if (loSigo) {
                    sistema.dejarDeSeguir(usernameVisitar);
                } else if (!pendiente) {
                    sistema.seguirUsuario(usernameVisitar);
                }
                // Recargar siempre para actualizar el estado
                cargarVistaPerfil(usernameVisitar);
            });

            fila1.add(Box.createHorizontalStrut(20));
            fila1.add(btnSeguir);
        } else {
            // --- MI PERFIL: EDITAR Y DESACTIVAR ---
            JButton btnEditar = new JButton("Editar Perfil");
            btnEditar.setFont(new Font("Arial", Font.BOLD, 12));
            btnEditar.setBackground(COLOR_FIELD);
            btnEditar.setBorderPainted(false);
            btnEditar.setCursor(new Cursor(Cursor.HAND_CURSOR));

                      // Acción para cambiar foto
            btnEditar.addActionListener(ev -> {
                JFileChooser fc = new JFileChooser();
                 abrirDialogoEditarPerfil();
            });

            // Botón Desactivar Cuenta
            JButton btnToggle = new JButton();
            if (sistema.getUsuarioActual().getEstadoCuenta() == EstadoCuenta.ACTIVO) {
                btnToggle.setText("Desactivar Cuenta");
                btnToggle.setBackground(COLOR_ERROR);
            } else {
                btnToggle.setText("Activar Cuenta");
                btnToggle.setBackground(new Color(0, 150, 0));
            }
            btnToggle.setForeground(Color.WHITE);
            btnToggle.setFont(new Font("Arial", Font.BOLD, 12));
            btnToggle.setBorderPainted(false);
            btnToggle.setOpaque(true);
            btnToggle.setFocusPainted(false);
            btnToggle.setCursor(new Cursor(Cursor.HAND_CURSOR));

            btnToggle.addActionListener(ev -> {
                int confirm = JOptionPane.showConfirmDialog(this,
                        "¿Seguro que desea desactivar su cuenta? No aparecerá en búsquedas.",
                        "Confirmar", JOptionPane.YES_NO_OPTION);

                if (confirm == JOptionPane.YES_OPTION) {
                    sistema.cambiarEstadoCuenta(EstadoCuenta.DESACTIVADO);
                    sistema.logout();
                    if (chatTimer != null) {
                        chatTimer.stop();
                    }
                    inicializarComponentesLogin();
                }
            });

            fila1.add(Box.createHorizontalStrut(20));
            fila1.add(btnEditar);
            fila1.add(Box.createHorizontalStrut(10));
            fila1.add(btnToggle);
        }

        // FILA 2: ESTADÍSTICAS (Posts, Followers, Following)
        JPanel fila2 = new JPanel(new FlowLayout(FlowLayout.LEFT));
        fila2.setBackground(COLOR_FONDO);
        int posts = sistema.getCantidadPosts(usernameVisitar);
        int followers = sistema.getCantidadFollowers(usernameVisitar);
        int following = sistema.getCantidadFollowing(usernameVisitar);

        fila2.add(crearLabelStat(posts, "publicaciones"));
        fila2.add(Box.createHorizontalStrut(25));

        // Panel de Seguidores (Clickeable)
        JPanel panelFollowers = crearLabelStat(followers, "seguidores");
        panelFollowers.setCursor(new Cursor(Cursor.HAND_CURSOR));
        panelFollowers.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                mostrarListaUsuarios(sistema.getListaFollowers(usernameVisitar), "Seguidores");
            }
        });
        fila2.add(panelFollowers);
        fila2.add(Box.createHorizontalStrut(25));

        // Panel de Seguidos (Clickeable)
        JPanel panelFollowing = crearLabelStat(following, "seguidos");
        panelFollowing.setCursor(new Cursor(Cursor.HAND_CURSOR));
        panelFollowing.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                mostrarListaUsuarios(sistema.getListaFollowing(usernameVisitar), "Siguiendo");
            }
        });
        fila2.add(panelFollowing);

        // FILA 3: NOMBRE REAL
        Usuario userVisitar = sistema.buscarUsuario(usernameVisitar);
        String nombreReal = (userVisitar != null) ? userVisitar.getNombreCompleto() : "Nombre";
        JLabel lblNombreReal = new JLabel(nombreReal);
        lblNombreReal.setFont(new Font("Arial", Font.BOLD, 14));
        JPanel fila3 = new JPanel(new FlowLayout(FlowLayout.LEFT));
        fila3.setBackground(COLOR_FONDO);
        fila3.add(lblNombreReal);

         //--- FILA 4: DATOS COMPACTOS (Punto 13) ---
        JPanel fila4 = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        fila4.setBackground(COLOR_FONDO);
        
        // Formateamos género para que se lea mejor
        String generoStr = (userVisitar.getGenero() == 'M') ? "Masculino" : "Femenino";
        String estadoStr = userVisitar.getEstadoCuenta().name(); // ACTIVO o DESACTIVADO
        String tipoStr = userVisitar.getTipoCuenta().name(); // PUBLICA o PRIVADA
        
        // Texto compacto: "Masculino | 25 años | Pública | Activo | Desde 2023"
        String datosCompactos = generoStr + " | " + userVisitar.getEdad() + " años | " 
                              + tipoStr + " | " + estadoStr + " | Desde " + userVisitar.getFechaRegistro();
        
        JLabel lblDatosCompactos = new JLabel(datosCompactos);
        lblDatosCompactos.setFont(new Font("Arial", Font.PLAIN, 11)); // Letra pequeña
        lblDatosCompactos.setForeground(Color.GRAY); // Color gris para no robar atención
        
        fila4.add(lblDatosCompactos);
        // Agregamos filas al panel de información
        infoPanel.add(fila1);
        infoPanel.add(fila2);
        infoPanel.add(fila3);
        infoPanel.add(fila4);

        header.add(infoPanel, BorderLayout.CENTER);
        panelContenido.add(header, BorderLayout.NORTH);

        // --- GRID DE PUBLICACIONES (ABAJO) ---
        JPanel gridPanel = new JPanel(new GridLayout(0, 4, 2, 2)); // 4 columnas para Desktop
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
                miniPanel.setBorder(new LineBorder(new Color(230, 230, 230)));

                JLabel lblImg = new JLabel();
                try {
                    if (p.getRutaImagen() != null) {
                        ImageIcon icono = new ImageIcon(p.getRutaImagen());
                        Image img = icono.getImage().getScaledInstance(200, 200, Image.SCALE_SMOOTH);
                        lblImg.setIcon(new ImageIcon(img));
                    }
                } catch (Exception ex) {
                    lblImg.setText("Error");
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
    // CARGA DE FOTO PERFIL
    // ---------------------------------------------------------
private ImageIcon cargarFotoPerfil(String username, int diametro) {
    Usuario u = sistema.buscarUsuario(username);

    if (u == null) {
        return crearAvatarDefault(username, diametro);
    }

    String ruta = u.getFotoPerfil();
    
    // Validación robustez: ruta nula, vacía o literal "null"
    if (ruta == null || ruta.isEmpty() || ruta.equals("null")) {
        return crearAvatarDefault(username, diametro);
    }

    File f = new File(ruta);
    if (f.exists()) {
        try {
            // Usamos ImageIO para asegurar que la imagen es válida
            BufferedImage imgBuffer = javax.imageio.ImageIO.read(f);
            if (imgBuffer != null) {
                return crearIconoCircular(new ImageIcon(imgBuffer), diametro);
            }
        } catch (Exception e) {
            System.out.println("Error cargando imagen: " + e.getMessage());
        }
    } 
    
    // Si el archivo no existe o falló la carga
    return crearAvatarDefault(username, diametro);
}

    private ImageIcon crearAvatarDefault(String username, int diametro) {
    // Creamos una imagen con canal alfa (transparencia)
    BufferedImage img = new BufferedImage(diametro, diametro, BufferedImage.TYPE_INT_ARGB);
    Graphics2D g2 = img.createGraphics();
    
    // Activar antialiasing para que el círculo sea suave
    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    
    // 1. Dibujar el círculo de fondo (Gris claro)
    g2.setColor(new Color(230, 230, 230));
    g2.fillOval(0, 0, diametro, diametro);
    
    // 2. Dibujar la letra (Gris oscuro)
    g2.setColor(Color.DARK_GRAY);
    g2.setFont(new Font("Arial", Font.BOLD, diametro / 2));
    
    String letra = username.isEmpty() ? "?" : username.substring(0, 1).toUpperCase();
    FontMetrics fm = g2.getFontMetrics();
    int x = (diametro - fm.stringWidth(letra)) / 2;
    int y = (fm.getAscent() + (diametro - fm.getAscent()) / 2);
    g2.drawString(letra, x, y);
    
    g2.dispose();
    return new ImageIcon(img);
}

private ImageIcon crearIconoCircular(ImageIcon iconoOriginal, int diametro) {
    try {
        // Escalar imagen original al tamaño deseado
        Image imgEscalada = iconoOriginal.getImage().getScaledInstance(diametro, diametro, Image.SCALE_SMOOTH);
        
        // Crear buffer con transparencia
        BufferedImage buffer = new BufferedImage(diametro, diametro, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = buffer.createGraphics();
        
        // Configurar calidad
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        
        // Crear el clip circular
        g2.setClip(new java.awt.geom.Ellipse2D.Double(0, 0, diametro, diametro));
        
        // Dibujar la imagen
        g2.drawImage(imgEscalada, 0, 0, null);
        
        // Opcional: Dibujar un borde gris suave
        g2.setColor(new Color(200, 200, 200));
        g2.setStroke(new BasicStroke(1f)); // Borde de 1 pixel
        g2.drawOval(0, 0, diametro-1, diametro-1);
        
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
        vistaActual = "Search";
        getContentPane().removeAll();
        setLayout(new BorderLayout());
        getContentPane().setBackground(COLOR_FONDO);
        add(crearPanelSidebar(), BorderLayout.WEST);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(COLOR_FONDO);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(40, 40, 40, 40));

        // Panel de búsqueda
        JPanel searchPanel = new JPanel(new BorderLayout());
        searchPanel.setBackground(Color.WHITE);
        searchPanel.setBorder(new LineBorder(new Color(200, 200, 200), 1, true));

        JTextField txtBuscar = new JTextField();
        txtBuscar.setFont(new Font("Arial", Font.PLAIN, 16));
        txtBuscar.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
        txtBuscar.setText("Buscar usuario o #hashtag...");
        txtBuscar.setForeground(COLOR_PLACEHOLDER);

        JButton btnBuscar = new JButton("Buscar");
        btnBuscar.setBackground(COLOR_BOTTON);
        btnBuscar.setForeground(Color.WHITE);

        searchPanel.add(txtBuscar, BorderLayout.CENTER);
        searchPanel.add(btnBuscar, BorderLayout.EAST);
        mainPanel.add(searchPanel, BorderLayout.NORTH);

        // Panel de resultados
        JPanel resultsPanel = new JPanel();
        resultsPanel.setLayout(new BoxLayout(resultsPanel, BoxLayout.Y_AXIS));
        resultsPanel.setBackground(COLOR_FONDO);

        JScrollPane scroll = new JScrollPane(resultsPanel);
        scroll.setBorder(null);
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        mainPanel.add(scroll, BorderLayout.CENTER);

        add(mainPanel, BorderLayout.CENTER);

        // --- ACCIÓN DE BÚSQUEDA UNIFICADA ---
        ActionListener buscarAction = e -> {
            String query = txtBuscar.getText().trim();
            if (query.isEmpty() || query.equals("Buscar usuario o #hashtag...")) {
                return;
            }

            resultsPanel.removeAll();

            // LÓGICA: Si empieza con # es Hashtag, si no es Usuario
            if (query.startsWith("#")) {
                ArrayList<Publicacion> posts = sistema.buscarPorHashtag(query);
                if (posts.isEmpty()) {
                    resultsPanel.add(new JLabel("No hay publicaciones con " + query));
                } else {
                    for (Publicacion p : posts) {
                        resultsPanel.add(crearPanelPost(p)); // Reutilizas tu método de posts
                        resultsPanel.add(Box.createVerticalStrut(10));
                    }
                }
            } else {
                ArrayList<Usuario> users = sistema.buscarUsuarios(query);
                if (users.isEmpty()) {
                    resultsPanel.add(new JLabel("No se encontraron usuarios."));
                } else {
                    for (Usuario u : users) {
                        resultsPanel.add(crearPanelResultadoUsuario(u)); // Reutilizas tu método de usuarios
                    }
                }
            }
            resultsPanel.revalidate();
            resultsPanel.repaint();
        };

        btnBuscar.addActionListener(buscarAction);
        txtBuscar.addActionListener(buscarAction); // Enter también busca

        revalidate();
        repaint();
    }

    private JPanel crearPanelResultadoUsuario(Usuario u) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setMaximumSize(new Dimension(600, 60));
        panel.setBorder(BorderFactory.createCompoundBorder(new LineBorder(new Color(230, 230, 230)), BorderFactory.createEmptyBorder(10, 10, 10, 10)));
        JPanel info = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        info.setBackground(Color.WHITE);
        JLabel lblFoto = new JLabel();
        lblFoto.setPreferredSize(new Dimension(40, 40));
        ImageIcon icono = cargarFotoPerfil(u.getUsername(), 40);
        if (icono != null) {
            lblFoto.setIcon(icono);
        }
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
// VISTA 6: INBOX (CON FOTOS EN LISTA)
// ---------------------------------------------------------
private void cargarVistaInbox() {
    vistaActual = "Messages";
    if (chatTimer != null) chatTimer.stop();
    
    getContentPane().removeAll(); setLayout(new BorderLayout());
    add(crearPanelSidebar(), BorderLayout.WEST);

    JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
    split.setDividerLocation(250); split.setBorder(null);

    // --- IZQUIERDA: LISTA CHATS CON CAJITAS ---
    JPanel panelLista = new JPanel(new BorderLayout());
    panelLista.setBackground(new Color(250, 250, 250)); // Fondo gris claro
    
    JLabel lblTitulo = new JLabel("  Mensajes", SwingConstants.LEFT);
    lblTitulo.setFont(new Font("Arial", Font.BOLD, 20));
    lblTitulo.setBorder(BorderFactory.createEmptyBorder(15, 5, 15, 5));
    panelLista.add(lblTitulo, BorderLayout.NORTH);

    // Contenedor de las cajitas
    JPanel contenedorChats = new JPanel();
    contenedorChats.setLayout(new BoxLayout(contenedorChats, BoxLayout.Y_AXIS));
    contenedorChats.setBackground(new Color(250, 250, 250));
    contenedorChats.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10)); // Margen general

    JScrollPane scrollLista = new JScrollPane(contenedorChats);
    scrollLista.setBorder(null);
    scrollLista.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
    personalizarScrollBar(scrollLista);
    
    // Velocidad del scroll
    scrollLista.getVerticalScrollBar().setUnitIncrement(16); 
    
    panelLista.add(scrollLista, BorderLayout.CENTER);

    JButton btnNuevo = new JButton("Nuevo Mensaje");
    btnNuevo.setBackground(COLOR_BOTTON); btnNuevo.setForeground(Color.WHITE);
    panelLista.add(btnNuevo, BorderLayout.SOUTH);

    // --- DERECHA: PLACEHOLDER ---
    JPanel panelChat = new JPanel(new BorderLayout());
    panelChat.setBackground(COLOR_FONDO);
    panelChat.add(new JLabel("Selecciona un chat", SwingConstants.CENTER), BorderLayout.CENTER);

    split.setLeftComponent(panelLista);
    split.setRightComponent(panelChat);
    add(split, BorderLayout.CENTER);

    // --- POBLAR LISTA ---
    ArrayList<String> chats = sistema.getChatsRecientes();
    for (String u : chats) {
        // "La Cajita"
        JPanel row = new JPanel(new BorderLayout());
        row.setBackground(Color.WHITE);
        // Borde visible: LineBorder (el cuadro) + EmptyBorder (espacio interno)
        row.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220, 220, 220), 1, true), // Borde redondeado
            BorderFactory.createEmptyBorder(10, 10, 10, 10) // Relleno
        ));
        row.setCursor(new Cursor(Cursor.HAND_CURSOR));
        // Tamaño fijo para que no ocupen todo
        row.setMaximumSize(new Dimension(230, 70));
        row.setAlignmentX(Component.CENTER_ALIGNMENT); // Centrar la cajita

        JLabel lblFoto = new JLabel();
        lblFoto.setIcon(crearIconoCircular(cargarFotoPerfil(u, 45), 45));
        row.add(lblFoto, BorderLayout.WEST);

        JLabel lblNombre = new JLabel("  " + u);
        lblNombre.setFont(new Font("Arial", Font.BOLD, 14));
        row.add(lblNombre, BorderLayout.CENTER);

        // Interacción
        row.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) { 
                lastMessageCount = 0; // Resetear scroll al abrir chat
                mostrarChatLive(panelChat, u); 
            }
            public void mouseEntered(MouseEvent e) { row.setBackground(new Color(245,245,245)); }
            public void mouseExited(MouseEvent e) { row.setBackground(Color.WHITE); }
        });

        contenedorChats.add(row);
        contenedorChats.add(Box.createVerticalStrut(8)); // Espacio entre cajitas
    }

    // Evento nuevo mensaje
    btnNuevo.addActionListener(e -> {
        String destino = JOptionPane.showInputDialog(this, "Username:");
        if (destino != null && !destino.isEmpty()) {
            if (sistema.buscarUsuario(destino) == null) {
                JOptionPane.showMessageDialog(this, "No existe.");
            } else if (!sistema.puedeEnviarMensaje(destino)) {
                 JOptionPane.showMessageDialog(this, "No puedes enviar (Privado).");
            } else {
                mostrarChatLive(panelChat, destino);
            }
        }
    });

    revalidate(); repaint();
}

private void mostrarChatLive(JPanel panelChat, String otro) {
    if (chatTimer != null) chatTimer.stop();

    panelChat.removeAll();
    panelChat.setLayout(new BorderLayout());
    panelChat.setBackground(COLOR_FONDO);

    // 1. PANEL MENSAJES
    JPanel panelMensajes = new JPanel();
    panelMensajes.setLayout(new BoxLayout(panelMensajes, BoxLayout.Y_AXIS));
    panelMensajes.setBackground(COLOR_FONDO);

    // 2. SCROLL PANE (Configuración clave)
    JScrollPane scrollMensajes = new JScrollPane(panelMensajes);
    scrollMensajes.setBorder(null);
    scrollMensajes.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
    scrollMensajes.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER); // SIN BARRA ABAJO
    personalizarScrollBar(scrollMensajes); // Tu método de estilo

    // 3. HEADER
    JPanel header = new JPanel(new BorderLayout());
    header.setBackground(Color.WHITE);
    header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(220, 220, 220)));
    JLabel lblNombre = new JLabel("  " + otro);
    lblNombre.setFont(new Font("Arial", Font.BOLD, 16));
    JButton btnDelete = new JButton("Eliminar Chat");
    btnDelete.setFont(new Font("Arial", Font.PLAIN, 10));
    btnDelete.setForeground(COLOR_ERROR);
    btnDelete.setBackground(Color.WHITE);
    btnDelete.setBorderPainted(false);
    btnDelete.addActionListener(del -> {
        int confirm = JOptionPane.showConfirmDialog(this, "¿Borrar historial con " + otro + "?", "Confirmar", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            sistema.eliminarConversacion(otro);
            lastMessageCount = 0; // Resetear contador
            refrescarMensajes(panelMensajes, otro);
        }
    });
    header.add(lblNombre, BorderLayout.WEST);
    header.add(btnDelete, BorderLayout.EAST);

    // 4. FOOTER (Inputs)
    JPanel footer = new JPanel(new BorderLayout());
    footer.setBackground(Color.WHITE);
    footer.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    JTextField txtInput = new JTextField();
    txtInput.setFont(new Font("Arial", Font.PLAIN, 14));
    JButton btnSend = new JButton("Enviar");
    btnSend.setBackground(COLOR_BOTTON);
    btnSend.setForeground(Color.WHITE);
    btnSend.setFont(new Font("Arial", Font.BOLD, 12));

    // Botón Sticker
    JButton btnSticker = new JButton("😀");
    btnSticker.setFont(new Font("Arial", Font.PLAIN, 16));
    btnSticker.setBackground(COLOR_FIELD);
    btnSticker.setFocusPainted(false);
    btnSticker.setCursor(new Cursor(Cursor.HAND_CURSOR));
    btnSticker.addActionListener(ev -> {
        ArrayList<String> stickers = sistema.getTodosStickers(sistema.getUsuarioActual().getUsername());
        DefaultListModel<String> model = new DefaultListModel<>();
        model.addElement("--> Importar nuevo sticker...");
        for (String ruta : stickers) model.addElement(new File(ruta).getName());
        JList<String> listaVisual = new JList<>(model);
        int opcion = JOptionPane.showConfirmDialog(this, new JScrollPane(listaVisual), "Mis Stickers", JOptionPane.OK_CANCEL_OPTION);
        if (opcion == JOptionPane.OK_OPTION && listaVisual.getSelectedValue() != null) {
            String seleccionVisual = listaVisual.getSelectedValue();
            if (seleccionVisual.equals("--> Importar nuevo sticker...")) {
                JFileChooser fc = new JFileChooser();
                fc.setFileFilter(new FileNameExtensionFilter("Imágenes", "png", "jpg", "jpeg"));
                if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                    sistema.guardarStickerPersonal(fc.getSelectedFile(), sistema.getUsuarioActual().getUsername());
                }
            } else {
                String rutaCompleta = null;
                for (String ruta : stickers) if (ruta.endsWith(seleccionVisual)) rutaCompleta = ruta;
                if (rutaCompleta != null) {
                    sistema.enviarMensaje(otro, rutaCompleta, "STICKER");
                    lastMessageCount = 0; // Forzar refresco visual inmediato
                    refrescarMensajes(panelMensajes, otro);
                }
            }
        }
    });

    footer.add(txtInput, BorderLayout.CENTER);
    footer.add(btnSend, BorderLayout.EAST);
    footer.add(btnSticker, BorderLayout.WEST);

    // Añadir todo al panel
    panelChat.add(header, BorderLayout.NORTH);
    panelChat.add(scrollMensajes, BorderLayout.CENTER);
    panelChat.add(footer, BorderLayout.SOUTH);

    // Acciones
    ActionListener sendAction = e -> {
        if (!txtInput.getText().isEmpty()) {
            sistema.enviarMensaje(otro, txtInput.getText(), "TEXTO");
            txtInput.setText("");
            lastMessageCount = 0; // Forzar refresco
            refrescarMensajes(panelMensajes, otro);
        }
    };
    btnSend.addActionListener(sendAction);
    txtInput.addActionListener(sendAction);

    // Carga inicial y Timer
    lastMessageCount = 0; // Reseteamos al abrir chat nuevo
    refrescarMensajes(panelMensajes, otro);
    chatTimer = new Timer(2000, ev -> refrescarMensajes(panelMensajes, otro));
    chatTimer.start();

    panelChat.revalidate();
    panelChat.repaint();
}

    private void refrescarMensajes(JPanel panelMsgs, String otro) {
        sistema.marcarComoLeido(otro);

        ArrayList<Mensaje> hist = sistema.getConversacion(otro);

        // OPTIMIZACIÓN GLITCH: Solo refrescar si cambió el contenido
        if (hist.size() == lastMessageCount) {
            return;
        }
        lastMessageCount = hist.size();

        // LÓGICA DE SCROLL:
        // Guardamos la posición actual ANTES de tocar el panel
        JScrollPane scrollParent = (JScrollPane) SwingUtilities.getAncestorOfClass(JScrollPane.class, panelMsgs);
        JScrollBar verticalBar = (scrollParent != null) ? scrollParent.getVerticalScrollBar() : null;
        int currentScrollValue = (verticalBar != null) ? verticalBar.getValue() : 0;
        boolean wasAtBottom = (verticalBar != null && (verticalBar.getValue() >= verticalBar.getMaximum() - 200)); // 200px de margen

        panelMsgs.removeAll();

        Mensaje ultimoEnviado = null;

        for (Mensaje m : hist) {
            boolean soyYo = m.getEmisor().equals(sistema.getUsuarioActual().getUsername());
            if (soyYo) ultimoEnviado = m;

            JPanel rowPanel = new JPanel(new BorderLayout());
            rowPanel.setOpaque(false);
            rowPanel.setBorder(BorderFactory.createEmptyBorder(2, 10, 2, 10));

            JPanel burbuja = new JPanel(new BorderLayout());
            burbuja.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));

            boolean esSticker = m instanceof instagram.MensajeSticker;

            if (esSticker) {
                JLabel lblImg = new JLabel();
                try {
                    File f = new File(m.getContenido());
                    if (f.exists()) {
                        ImageIcon icon = new ImageIcon(m.getContenido());
                        Image img = icon.getImage().getScaledInstance(100, 100, Image.SCALE_SMOOTH);
                        lblImg.setIcon(new ImageIcon(img));
                    } else { lblImg.setText("[Sticker]"); }
                } catch (Exception e) { lblImg.setText("[Error]"); }
                burbuja.add(lblImg, BorderLayout.CENTER);
                burbuja.setBackground(Color.WHITE);
            } else {
                String texto = m.getContenido();
                if (texto.startsWith("SHARE|")) {
                    // --- POST COMPARTIDO VISUAL ---
                    String[] datos = texto.split("\\|");
                    JPanel card = new JPanel(new BorderLayout());
                    card.setBackground(Color.LIGHT_GRAY);
                    card.setBorder(new LineBorder(Color.GRAY));
                    card.setCursor(new Cursor(Cursor.HAND_CURSOR)); // Manito
                    
                    // Intentar cargar imagen
                    JLabel imgLabel = new JLabel();
                    try {
                        File f = new File(datos[2]);
                        if(f.exists()){
                            ImageIcon icon = new ImageIcon(datos[2]);
                            Image scaled = icon.getImage().getScaledInstance(150, 150, Image.SCALE_SMOOTH);
                            imgLabel.setIcon(new ImageIcon(scaled));
                        }
                    } catch (Exception ex) {}
                    card.add(imgLabel, BorderLayout.WEST);

                    JPanel info = new JPanel(new GridLayout(2,1));
                    info.setBackground(Color.WHITE);
                    info.add(new JLabel("De: " + datos[1]));
                    JTextArea txt = new JTextArea(datos.length > 3 ? datos[3] : "Ver post");
                    txt.setWrapStyleWord(true); txt.setLineWrap(true); txt.setOpaque(false); txt.setEditable(false);
                    info.add(txt);
                    card.add(info, BorderLayout.CENTER);
                    
                    // Evento: Click para ver imagen completa
                    card.addMouseListener(new MouseAdapter() {
                        @Override
                        public void mouseClicked(MouseEvent e) {
                            try {
                                File imgFile = new File(datos[2]);
                                if(imgFile.exists()){
                                    // Abrimos un diálogo simple con la imagen
                                    JDialog d = new JDialog((Frame)null, "Post Compartido", true);
                                    d.setSize(650, 700);
                                 JLabel bigImg = new JLabel(new ImageIcon(new ImageIcon(datos[2]).getImage().getScaledInstance(600, 600, Image.SCALE_SMOOTH)));
                                    d.add(new JScrollPane(bigImg));
                                    d.setLocationRelativeTo(null);
                                    d.setVisible(true);
                                }
                            } catch (Exception ex) {}
                        }
                    });

                    burbuja.add(card, BorderLayout.CENTER);
                    burbuja.setBackground(Color.WHITE);
                } else {
                    // Texto normal
                    JTextArea txtMsg = new JTextArea(texto);
                    txtMsg.setLineWrap(true); txtMsg.setWrapStyleWord(true);
                    txtMsg.setFont(new Font("Arial", Font.PLAIN, 13)); txtMsg.setEditable(false); txtMsg.setOpaque(false);
                    burbuja.add(txtMsg, BorderLayout.CENTER);
                }
            }

            // Alineación
            if (soyYo) {
                if (!esSticker && (m.getContenido() == null || !m.getContenido().startsWith("SHARE|"))) {
                    burbuja.setBackground(COLOR_BOTTON);
                } else {
                    burbuja.setBackground(Color.WHITE); // Stickers/Shares alineados derecha pero fondo neutro
                }
                rowPanel.add(burbuja, BorderLayout.EAST);
            } else {
                if (!esSticker && (m.getContenido() == null || !m.getContenido().startsWith("SHARE|"))) {
                    burbuja.setBackground(Color.WHITE);
                    burbuja.setBorder(BorderFactory.createLineBorder(new Color(230, 230, 230)));
                }
                rowPanel.add(burbuja, BorderLayout.WEST);
            }

            panelMsgs.add(rowPanel);

            // Fecha
            JLabel lblFecha = new JLabel(m.getFecha() + " " + m.getHoraFormateada());
            lblFecha.setFont(new Font("Arial", Font.PLAIN, 9));
            lblFecha.setForeground(COLOR_FONT);
            lblFecha.setBorder(BorderFactory.createEmptyBorder(0, 10, 5, 10));
            JPanel dateRow = new JPanel(new BorderLayout());
            dateRow.setOpaque(false);
            if (soyYo) dateRow.add(lblFecha, BorderLayout.EAST);
            else dateRow.add(lblFecha, BorderLayout.WEST);
            panelMsgs.add(dateRow);
        }

        // Estado "Leído"
        if (ultimoEnviado != null) {
            // Solo mostramos "Leído" si el estado en el archivo es LEIDO
            // Gracias a la lógica nueva en Sistema.java, esto funcionará
            boolean esLeido = ultimoEnviado.getEstado().equals("LEIDO");
            JLabel lblEstado = new JLabel(esLeido ? "Leído ✓✓" : "Enviado ✓");
            lblEstado.setFont(new Font("Arial", Font.ITALIC, 10));
            lblEstado.setForeground(esLeido ? COLOR_BOTTON : COLOR_FONT);
            lblEstado.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
            
            JPanel statusRow = new JPanel(new BorderLayout());
            statusRow.setOpaque(false);
            statusRow.add(lblEstado, BorderLayout.EAST);
            panelMsgs.add(statusRow);
        }

        panelMsgs.revalidate();
        panelMsgs.repaint();

        // LÓGICA DE SCROLL FINAL
        SwingUtilities.invokeLater(() -> {
            if (verticalBar != null) {
                // Si estaba abajo, se queda abajo (para ver el nuevo mensaje)
                if (wasAtBottom) {
                    verticalBar.setValue(verticalBar.getMaximum());
                } else {
                    // Si estaba leyendo arriba, se queda donde estaba (NO SALTA)
                    verticalBar.setValue(currentScrollValue);
                }
            }
        });
    }
    //Cargar Vista Notificaciones
private void cargarVistaNotificaciones() {
    vistaActual = "Notifications";
    if (chatTimer != null) {
        chatTimer.stop();
    }

    getContentPane().removeAll();
    setLayout(new BorderLayout());
    add(crearPanelSidebar(), BorderLayout.WEST);

    JPanel mainPanel = new JPanel(new BorderLayout());
    mainPanel.setBackground(COLOR_FONDO);
    mainPanel.setBorder(new EmptyBorder(40, 40, 40, 40));

    JLabel lblTitulo = new JLabel("Actividad");
    lblTitulo.setFont(new Font("Arial", Font.BOLD, 24));
    mainPanel.add(lblTitulo, BorderLayout.NORTH);

    JPanel content = new JPanel();
    content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
    content.setBackground(COLOR_FONDO);
    JScrollPane scroll = new JScrollPane(content);
    scroll.setBorder(null);
    mainPanel.add(scroll, BorderLayout.CENTER);

    add(mainPanel, BorderLayout.CENTER);

    boolean hayNotificaciones = false;

    // --- 1. SECCIÓN SOLICITUDES ---
    ArrayList<String> solicitudes = sistema.getSolicitudes();
    if (!solicitudes.isEmpty()) {
        hayNotificaciones = true;
        content.add(crearTituloSeccion("Solicitudes de Seguimiento"));
        for (String user : solicitudes) {
            content.add(crearPanelSolicitud(user));
            content.add(Box.createVerticalStrut(5));
        }
        content.add(Box.createVerticalStrut(20));
    }

    // --- 2. NUEVO: SECCIÓN GENERAL (SEGUIDORES Y MENCIONES) ---
    ArrayList<String> notifs = sistema.getNotificacionesGenerales();
    if (!notifs.isEmpty()) {
        hayNotificaciones = true;
        content.add(crearTituloSeccion("Hoy")); // Estilo Instagram
        
        for (String linea : notifs) {
            // Formato esperado: TIPO|DATOS...
            String[] datos = linea.split("\\|");
            String tipo = datos[0];
            
            JPanel panelNotif = new JPanel(new FlowLayout(FlowLayout.LEFT));
            panelNotif.setBackground(Color.WHITE);
            panelNotif.setMaximumSize(new Dimension(500, 50));
            panelNotif.setBorder(BorderFactory.createLineBorder(new Color(230, 230, 230)));
            
            JLabel lblIcono = new JLabel();
            lblIcono.setFont(new Font("Arial", Font.PLAIN, 16));
            
            JLabel lblTexto = new JLabel();
            lblTexto.setFont(new Font("Arial", Font.PLAIN, 13));
            
            if (tipo.equals("SEGUIDOR")) {
                // Formato: SEGUIDOR|username|fecha
                String quien = datos[1];
                lblIcono.setText("👤");
                lblTexto.setText("<html><b>" + quien + "</b> ha empezado a seguirte.</html>");
                lblTexto.setCursor(new Cursor(Cursor.HAND_CURSOR));
                lblTexto.addMouseListener(new MouseAdapter() {
                    public void mouseClicked(MouseEvent e) { cargarVistaPerfil(quien); }
                });
            } else if (tipo.equals("MENCION")) {
                // Formato: MENCION|autor|contenido|fecha
                String autor = datos[1];
                lblIcono.setText("💬");
                lblTexto.setText("<html><b>" + autor + "</b> te mencionó en un comentario.</html>");
                lblTexto.setCursor(new Cursor(Cursor.HAND_CURSOR));
                lblTexto.addMouseListener(new MouseAdapter() {
                    public void mouseClicked(MouseEvent e) { cargarVistaPerfil(autor); }
                });
            }
            
            panelNotif.add(lblIcono);
            panelNotif.add(Box.createHorizontalStrut(10));
            panelNotif.add(lblTexto);
            content.add(panelNotif);
            content.add(Box.createVerticalStrut(5));
        }
    }

    // --- 3. SECCIÓN LIKES ---
    ArrayList<String> likes = sistema.getNotificacionesLikes();
    if (!likes.isEmpty()) {
        hayNotificaciones = true;
        content.add(crearTituloSeccion("Likes en tus publicaciones"));

        for (int i = likes.size() - 1; i >= 0; i--) {
            String[] datos = likes.get(i).split("\\|");
            if (datos.length >= 3) {
                String fecha = datos[1];
                String quien = datos[2];
                content.add(crearPanelNotificacionLike(quien, fecha));
                content.add(Box.createVerticalStrut(5));
            }
        }
    }

    if (!hayNotificaciones) {
        content.add(new JLabel("No tienes notificaciones nuevas."));
    }

    revalidate();
    repaint();
}

    // Métodos auxiliares visuales para esta vista
    private JPanel crearTituloSeccion(String texto) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT));
        p.setBackground(COLOR_FONDO);
        JLabel l = new JLabel(texto);
        l.setFont(new Font("Arial", Font.BOLD, 16));
        l.setForeground(COLOR_BOTTON);
        p.add(l);
        return p;
    }

    private JPanel crearPanelSolicitud(String username) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setMaximumSize(new Dimension(500, 50));
        panel.setBorder(BorderFactory.createLineBorder(new Color(230, 230, 230)));

        JLabel lblUser = new JLabel("  " + username + " quiere seguirte.");
        lblUser.setFont(new Font("Arial", Font.PLAIN, 13));

        JPanel botones = new JPanel();
        JButton btnAceptar = new JButton("Aceptar");
        btnAceptar.setBackground(new Color(0, 150, 0));
        btnAceptar.setForeground(Color.WHITE);
        JButton btnRechazar = new JButton("Rechazar");
        btnRechazar.setBackground(COLOR_ERROR);
        btnRechazar.setForeground(Color.WHITE);

        btnAceptar.addActionListener(e -> {
            sistema.aceptarSolicitud(username);
            cargarVistaNotificaciones(); // Refrescar
        });
        btnRechazar.addActionListener(e -> {
            sistema.rechazarSolicitud(username);
            cargarVistaNotificaciones(); // Refrescar
        });

        botones.add(btnAceptar);
        botones.add(btnRechazar);

        panel.add(lblUser, BorderLayout.CENTER);
        panel.add(botones, BorderLayout.EAST);
        return panel;
    }

    private JPanel crearPanelNotificacionLike(String username, String fecha) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.setBackground(Color.WHITE);
        panel.setMaximumSize(new Dimension(500, 40));
        panel.setBorder(BorderFactory.createLineBorder(new Color(230, 230, 230)));

        JLabel lbl = new JLabel("  A " + username + " le gustó tu publicación del " + fecha);
        lbl.setFont(new Font("Arial", Font.PLAIN, 13));
        panel.add(lbl);
        return panel;
    }

    // Diálogo simple para mostrar lista de usuarios
    private void mostrarListaUsuarios(ArrayList<String> usuarios, String titulo) {
        if (usuarios.isEmpty()) {
            JOptionPane.showMessageDialog(this, "La lista está vacía.");
            return;
        }

        // Convertimos a array para el JList
        String[] datos = usuarios.toArray(new String[0]);
        JList<String> lista = new JList<>(datos);
        JScrollPane scroll = new JScrollPane(lista);
        scroll.setPreferredSize(new Dimension(300, 400));

        // Evento para ver perfil al hacer clic
        lista.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 1) { // Un solo clic
                    String seleccionado = lista.getSelectedValue();
                    if (seleccionado != null) {
                        cargarVistaPerfil(seleccionado);
                        // Cerramos el diálogo (necesitamos referencia, usamos una variable final)
                        ((JDialog) SwingUtilities.getWindowAncestor(lista)).dispose();
                    }
                }
            }
        });

        JOptionPane.showMessageDialog(this, scroll, titulo, JOptionPane.PLAIN_MESSAGE);
    }
    private void abrirDialogoEditarPerfil() {
    JDialog dialog = new JDialog(this, "Editar Perfil", true);
    dialog.setSize(400, 350);
    dialog.setLocationRelativeTo(this);
    dialog.setLayout(new BorderLayout());
    dialog.getContentPane().setBackground(COLOR_FONDO);

    // Panel de campos
    JPanel panelCampos = new JPanel();
    panelCampos.setLayout(new BoxLayout(panelCampos, BoxLayout.Y_AXIS));
    panelCampos.setBackground(COLOR_FONDO);
    panelCampos.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

    // Campo Nombre
    JLabel lblNombre = new JLabel("Nombre Completo:");
    JTextField txtNombre = new JTextField(sistema.getUsuarioActual().getNombreCompleto());
    txtNombre.setFont(new Font("Arial", Font.PLAIN, 14));
    
    // Campo Contraseña
    JLabel lblPass = new JLabel("Nueva Contraseña (dejar vacío para no cambiar):");
    JPasswordField txtPass = new JPasswordField();
    txtPass.setFont(new Font("Arial", Font.PLAIN, 14));
    
    // Botón Foto
    JButton btnFoto = new JButton("Cambiar Foto de Perfil");
    btnFoto.setBackground(COLOR_BOTTON);
    btnFoto.setForeground(Color.WHITE);
    btnFoto.setFocusPainted(false);
    
    // Variable para guardar la nueva ruta de foto
    final String[] nuevaRutaFoto = {null};

    btnFoto.addActionListener(e -> {
        JFileChooser fc = new JFileChooser();
        fc.setFileFilter(new FileNameExtensionFilter("Imágenes", "jpg", "png", "jpeg"));
        if (fc.showOpenDialog(dialog) == JFileChooser.APPROVE_OPTION) {
            File archivo = fc.getSelectedFile();
            String nombreImg = "profile_" + System.currentTimeMillis();
            String ruta = sistema.procesarImagenPerfil(archivo, sistema.getUsuarioActual().getUsername(), nombreImg);
            if (ruta != null) {
                nuevaRutaFoto[0] = ruta;
                JOptionPane.showMessageDialog(dialog, "Nueva foto seleccionada. ¡Guarda los cambios!");
            }
        }
    });

    panelCampos.add(lblNombre);
    panelCampos.add(Box.createVerticalStrut(5));
    panelCampos.add(txtNombre);
    panelCampos.add(Box.createVerticalStrut(15));
    panelCampos.add(lblPass);
    panelCampos.add(Box.createVerticalStrut(5));
    panelCampos.add(txtPass);
    panelCampos.add(Box.createVerticalStrut(20));
    panelCampos.add(btnFoto);

    // Panel de botones (Guardar/Cancelar)
    JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.RIGHT));
    panelBotones.setBackground(COLOR_FONDO);
    JButton btnGuardar = new JButton("Guardar Cambios");
    btnGuardar.setBackground(new Color(0, 150, 0));
    btnGuardar.setForeground(Color.WHITE);
    btnGuardar.setFocusPainted(false);
    
    JButton btnCancelar = new JButton("Cancelar");

    btnGuardar.addActionListener(e -> {
        String nombre = txtNombre.getText().trim();
        String pass = new String(txtPass.getPassword()).trim();
        
        if (nombre.isEmpty()) {
            JOptionPane.showMessageDialog(dialog, "El nombre no puede estar vacío.");
            return;
        }

        // 1. Actualizar Nombre y Password en users.ins
        boolean exito = sistema.actualizarDatosUsuario(nombre, pass);
        
        // 2. Si se cambió la foto, actualizarla
        if (nuevaRutaFoto[0] != null) {
            sistema.actualizarFotoPerfil(sistema.getUsuarioActual().getUsername(), nuevaRutaFoto[0]);
        }

        if (exito) {
            JOptionPane.showMessageDialog(dialog, "Perfil actualizado correctamente.");
            dialog.dispose();
            cargarVistaPerfil(sistema.getUsuarioActual().getUsername()); // Refrescar vista
        } else {
            JOptionPane.showMessageDialog(dialog, "Error al guardar cambios.");
        }
    });

    btnCancelar.addActionListener(e -> dialog.dispose());

    panelBotones.add(btnCancelar);
    panelBotones.add(btnGuardar);

    dialog.add(panelCampos, BorderLayout.CENTER);
    dialog.add(panelBotones, BorderLayout.SOUTH);
    dialog.setVisible(true);
}
    // Método auxiliar para convertir #tags y @menciones en enlaces HTML
private String convertirTextoAHtml(String texto) {
    StringBuilder html = new StringBuilder();
    // Dividir por espacios para analizar palabra por palabra
    String[] palabras = texto.split(" ");
    
    for (String palabra : palabras) {
        if (palabra.startsWith("#")) {
            // Es un hashtag
            html.append("<a href='").append(palabra).append("'>").append(palabra).append("</a> ");
        } else if (palabra.startsWith("@")) {
            // Es una mención
            html.append("<a href='").append(palabra).append("'>").append(palabra).append("</a> ");
        } else {
            // Texto normal
            html.append(palabra).append(" ");
        }
    }
    return html.toString();
}
private void cargarVistaBusquedaHashtag(String hashtag) {
    vistaActual = "Search";
    getContentPane().removeAll();
    setLayout(new BorderLayout());
    getContentPane().setBackground(COLOR_FONDO);
    add(crearPanelSidebar(), BorderLayout.WEST);

    JPanel mainPanel = new JPanel(new BorderLayout());
    mainPanel.setBackground(COLOR_FONDO);
    mainPanel.setBorder(BorderFactory.createEmptyBorder(40, 40, 40, 40));

    // Título
    JLabel lblTitulo = new JLabel("Resultados para: " + hashtag);
    lblTitulo.setFont(new Font("Arial", Font.BOLD, 18));
    mainPanel.add(lblTitulo, BorderLayout.NORTH);

    // Panel de resultados
    JPanel resultsPanel = new JPanel();
    resultsPanel.setLayout(new BoxLayout(resultsPanel, BoxLayout.Y_AXIS));
    resultsPanel.setBackground(COLOR_FONDO);
    JScrollPane scroll = new JScrollPane(resultsPanel);
    scroll.setBorder(null);
    mainPanel.add(scroll, BorderLayout.CENTER);

    add(mainPanel, BorderLayout.CENTER);

    // Llenar resultados automáticamente
    ArrayList<Publicacion> posts = sistema.buscarPorHashtag(hashtag);
    if (posts.isEmpty()) {
        resultsPanel.add(new JLabel("No hay publicaciones con " + hashtag));
    } else {
        for (Publicacion p : posts) {
            resultsPanel.add(crearPanelPost(p));
            resultsPanel.add(Box.createVerticalStrut(10));
        }
    }

    revalidate();
    repaint();
}
}
