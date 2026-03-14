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

public class VentanaPrincipal extends JFrame {

    private Sistema sistema;
    private Socket socket;
    private ObjectOutputStream salida;
    private Timer chatTimer;

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

            // 1. Validación visual local (rapida)
            Usuario u = sistema.buscarUsuario(user);
            if (u == null) {
                lblErrorUser.setText("El usuario no existe.");
                ponerBordeRojo(txtUser);
                return;
            }
            if (u.getEstadoCuenta() == EstadoCuenta.DESACTIVADO) {
                int opcion = JOptionPane.showConfirmDialog(this,
                        "Esta cuenta está desactivada. ¿Desea reactivarla?",
                        "Cuenta Inactiva", JOptionPane.YES_NO_OPTION);

                if (opcion == JOptionPane.YES_OPTION) {
                    sistema.reactivarCuenta(user); // Actualiza el archivo
                } else {
                    return; // Detiene el proceso si no quiere reactivar
                }
            }

            // 2. Intento de Login Local (Para que funcione tu lógica actual)
            boolean passCorrecta = sistema.login(user, pass);
            if (!passCorrecta) {
                lblErrorPass.setText("La contraseña es incorrecta.");
                ponerBordeRojo(panelPass);
                return;
            }

            // 3. Intento de conexión Socket (Añadido para el requisito del profesor)
            // Esto se ejecuta solo si el login local fue exitoso
            try {
                socket = new Socket("localhost", 5000);
                salida = new ObjectOutputStream(socket.getOutputStream());

                salida.writeObject("LOGIN:" + user + ":" + pass);
                salida.flush();

                ObjectInputStream entrada = new ObjectInputStream(socket.getInputStream());
                boolean exito = (boolean) entrada.readObject();

                if (exito) {
                    System.out.println("Conectado al servidor correctamente.");
                } else {
                    System.out.println("El servidor rechazó la conexión.");
                }
            } catch (Exception ex) {
                System.out.println("No se pudo conectar al servidor (modo local activo).");
            }

            // 4. Cargar la vista
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
            String foto = "default_profile.png";

            boolean exito = sistema.registrarUsuario(user, pass, nombre, genero, edad, foto, tipo);
            if (exito) {
                lblTitulo.setText("¡Cuenta Creada!");
                lblTitulo.setForeground(new Color(0, 150, 0));
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ignored) {
                }
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

    private JPanel crearPanelPost(Publicacion p) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setMaximumSize(new Dimension(600, 750)); // Un poco más alto para los botones
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(10, 10, 10, 10),
                BorderFactory.createLineBorder(new Color(230, 230, 230))));

        // 1. HEADER (Autor)
        JPanel header = new JPanel(new FlowLayout(FlowLayout.LEFT));
        header.setBackground(Color.WHITE);
        JLabel lblFoto = new JLabel();
        lblFoto.setPreferredSize(new Dimension(40, 40));
        ImageIcon iconoCircular = cargarFotoPerfil(p.getAutor(), 40);
        if (iconoCircular != null) {
            lblFoto.setIcon(iconoCircular);
        } else {
            lblFoto.setIcon(crearIconoCircular(new ImageIcon(), 40));
            lblFoto.setOpaque(true);
            lblFoto.setBackground(new Color(230, 230, 230));
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

        // 2. IMAGEN
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
                } else {
                    lblImagen.setText("Imagen no encontrada");
                }
            } else {
                lblImagen.setText("Sin Imagen");
            }
        } catch (Exception e) {
            lblImagen.setText("Error img");
        }

        // 3. ACCIONES (LIKE, COMMENT, SHARE)
        JPanel panelAcciones = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 5));
        panelAcciones.setBackground(Color.WHITE);

        // --- BOTÓN LIKE ---
        JButton btnLike = new JButton();
        btnLike.setBorderPainted(false);
        btnLike.setBackground(Color.WHITE);
        btnLike.setFocusPainted(false);
        btnLike.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Verificar estado inicial
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

        btnComment.addActionListener(ev -> {
            abrirDialogoComentarios(p);
        });

        // --- BOTÓN SHARE ---
        JButton btnShare = new JButton();
        if (sidebarIconsNormal.containsKey("Share")) {
            btnShare.setIcon(sidebarIconsNormal.get("Share"));
        } else {
            btnShare.setText("✉️");
        }
        btnShare.setBorderPainted(false);
        btnShare.setBackground(Color.WHITE);
        btnShare.setFocusPainted(false);
        btnShare.setCursor(new Cursor(Cursor.HAND_CURSOR));

        btnShare.addActionListener(ev -> {
            String destino = JOptionPane.showInputDialog(this, "¿A qué usuario quieres enviar este post?");
            if (destino != null && !destino.isEmpty()) {
                sistema.compartirPost(destino, p.getAutor(), p.getRutaImagen(), p.getContenido());
                JOptionPane.showMessageDialog(this, "Post enviado a " + destino);

                // Efecto visual de "presionado"
                if (sidebarIconsBold.containsKey("Share")) {
                    btnShare.setIcon(sidebarIconsBold.get("Share"));
                }
                Timer t = new Timer(1000, e -> {
                    if (sidebarIconsNormal.containsKey("Share")) {
                        btnShare.setIcon(sidebarIconsNormal.get("Share"));
                    }
                });
                t.setRepeats(false);
                t.start();
            }
        });

        panelAcciones.add(btnLike);
        panelAcciones.add(btnComment);
        panelAcciones.add(btnShare);

        // 4. FOOTER (Contenido y Fecha)
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

        footer.add(panelAcciones); // Añadimos los botones arriba del texto
        footer.add(lblContenido);
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

            // --- NUEVO: EXTRAER HASHTAGS Y MENCIONES AUTOMÁTICAMENTE ---
            StringBuilder hashtags = new StringBuilder();
            StringBuilder menciones = new StringBuilder();

            // Separamos el texto por espacios para analizar cada palabra
            for (String palabra : texto.split(" ")) {
                if (palabra.startsWith("#")) {
                    hashtags.append(palabra).append(" "); // Agregamos espacio para separar
                } else if (palabra.startsWith("@")) {
                    menciones.append(palabra).append(" ");
                }
            }
            // -------------------------------------------------------------

            String nombreImg = "post_" + System.currentTimeMillis();
            File archivoOriginal = new File(rutaSeleccionada[0]);

            // Usamos el método mejorado de Sistema para guardar la imagen
            String rutaFinal = sistema.procesarYGuardarImagen(archivoOriginal, sistema.getUsuarioActual().getUsername(), nombreImg);

            if (rutaFinal != null) {
                // Pasamos los hashtags y menciones extraídos (trim elimina el último espacio sobrante)
                boolean exito = sistema.crearPublicacion(texto, rutaFinal, hashtags.toString().trim(), menciones.toString().trim());

                if (exito) {
                    JOptionPane.showMessageDialog(dialog, "¡Publicado con éxito!");
                    dialog.dispose();
                    cargarVistaFeed(); // Recargamos el feed para ver el nuevo post
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
    ImageIcon iconoCircular = cargarFotoPerfil(usernameVisitar, 150);
    if (iconoCircular != null) {
        lblFoto.setIcon(iconoCircular);
    } else {
        lblFoto.setText("Foto");
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
    if (!usernameVisitar.equals(sistema.getUsuarioActual().getUsername())) {
        // --- PERFIL AJENO: LÓGICA SEGUIR / DEJAR DE SEGUIR ---
        JButton btnSeguir = new JButton();
        btnSeguir.setFont(new Font("Arial", Font.BOLD, 12));
        btnSeguir.setBorderPainted(false);
        btnSeguir.setOpaque(true);
        btnSeguir.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Verificamos estado actual para poner texto y color
        if (sistema.yaLoSigo(usernameVisitar)) {
            btnSeguir.setText("Dejar de seguir");
            btnSeguir.setBackground(new Color(240, 240, 240)); // Gris
            btnSeguir.setForeground(Color.BLACK);
        } else {
            btnSeguir.setText("Seguir");
            btnSeguir.setBackground(COLOR_BOTTON); // Azul
            btnSeguir.setForeground(Color.WHITE);
        }

        // Acción inteligente del botón
        btnSeguir.addActionListener(e -> {
            if (sistema.yaLoSigo(usernameVisitar)) {
                sistema.dejarDeSeguir(usernameVisitar);
            } else {
                sistema.seguirUsuario(usernameVisitar);
            }
            // Recargamos la vista para reflejar el cambio visual y contadores
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
            fc.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Imágenes", "jpg", "png"));
            int res = fc.showOpenDialog(this);

            if (res == JFileChooser.APPROVE_OPTION) {
                File archivo = fc.getSelectedFile();
                String nombreImg = "profile_" + System.currentTimeMillis();
                String ruta = sistema.procesarYGuardarImagen(archivo, sistema.getUsuarioActual().getUsername(), nombreImg);

                if (ruta != null) {
                    sistema.actualizarFotoPerfil(sistema.getUsuarioActual().getUsername(), ruta);
                    cargarVistaPerfil(sistema.getUsuarioActual().getUsername()); // Recargar
                } else {
                    JOptionPane.showMessageDialog(this, "Error al guardar la foto.");
                }
            }
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
                if (chatTimer != null) chatTimer.stop();
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

    // Agregamos filas al panel de información
    infoPanel.add(fila1);
    infoPanel.add(fila2);
    infoPanel.add(fila3);
    
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
        String ruta = (u != null && u.getFotoPerfil() != null && !u.getFotoPerfil().isEmpty()) ? u.getFotoPerfil() : "default_profile.png";
        ImageIcon icon = null;
        try {
            if (!ruta.equals("default_profile.png") && !ruta.equals("null")) {
                File f = new File(ruta);
                if (f.exists()) {
                    icon = new ImageIcon(ruta);
                }
            }
            if (icon == null) {
                URL url = getClass().getResource("/images/default_profile.png");
                if (url != null) {
                    icon = new ImageIcon(url);
                } else {
                    return crearAvatarDefault(username, diametro);
                }
            }
            return crearIconoCircular(icon, diametro);
        } catch (Exception e) {
            return crearAvatarDefault(username, diametro);
        }
    }

    private ImageIcon crearAvatarDefault(String username, int diametro) {
        BufferedImage img = new BufferedImage(diametro, diametro, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = img.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(new Color(220, 220, 220));
        g2.fillOval(0, 0, diametro, diametro);
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
    // VISTA 6: INBOX (CON CHAT LIVE)
    // ---------------------------------------------------------
    private void cargarVistaInbox() {
        vistaActual = "Messages";
        // Detenemos cualquier timer anterior si venimos de otro chat
        if (chatTimer != null) {
            chatTimer.stop();
        }

        getContentPane().removeAll();
        setLayout(new BorderLayout());
        getContentPane().setBackground(COLOR_FONDO);
        add(crearPanelSidebar(), BorderLayout.WEST);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setDividerLocation(250);
        splitPane.setBorder(null);
        splitPane.setBackground(COLOR_FONDO);

        // --- Panel Izquierdo: Lista de Chats ---
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
        for (String u : chats) {
            modelChats.addElement(u);
        }
        splitPane.setLeftComponent(panelLista);

        // --- Panel Derecho: Área de Chat ---
        JPanel panelChat = new JPanel(new BorderLayout());
        panelChat.setBackground(COLOR_FONDO);
        JLabel lblSelecciona = new JLabel("Selecciona un chat", SwingConstants.CENTER);
        lblSelecciona.setFont(new Font("Arial", Font.ITALIC, 16));
        lblSelecciona.setForeground(COLOR_FONT);
        panelChat.add(lblSelecciona, BorderLayout.CENTER);
        splitPane.setRightComponent(panelChat);

        add(splitPane, BorderLayout.CENTER);

        // --- Evento al seleccionar un usuario de la lista ---
        listaChats.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                String seleccionado = listaChats.getSelectedValue();
                if (seleccionado != null) {
                    mostrarChatLive(panelChat, seleccionado); // NUEVO MÉTODO
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
                    if (!modelChats.contains(destino)) {
                        modelChats.addElement(destino);
                    }
                    listaChats.setSelectedValue(destino, true);
                }
            }
        });

        revalidate();
        repaint();
    }

    // --- NUEVO MÉTODO: Configura la UI del chat e inicia el Timer ---
    private void mostrarChatLive(JPanel panelChat, String otroUsuario) {
        if (chatTimer != null) {
            chatTimer.stop();
        }

        panelChat.removeAll();
        panelChat.setLayout(new BorderLayout());
        panelChat.setBackground(COLOR_FONDO);

        // --- 1. CREAR EL PANEL DE MENSAJES PRIMERO ---
        JPanel panelMensajes = new JPanel();
        panelMensajes.setLayout(new BoxLayout(panelMensajes, BoxLayout.Y_AXIS));
        panelMensajes.setBackground(COLOR_FONDO);
        JScrollPane scrollMensajes = new JScrollPane(panelMensajes);
        scrollMensajes.setBorder(null);

        // --- 2. LUEGO CREAR EL HEADER Y EL BOTÓN ELIMINAR ---
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Color.WHITE);
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(220, 220, 220)));

        JLabel lblNombre = new JLabel("  " + otroUsuario);
        lblNombre.setFont(new Font("Arial", Font.BOLD, 16));

        JButton btnDelete = new JButton("Eliminar Chat");
        btnDelete.setFont(new Font("Arial", Font.PLAIN, 10));
        btnDelete.setForeground(COLOR_ERROR);
        btnDelete.setBackground(Color.WHITE);
        btnDelete.setBorderPainted(false);

        btnDelete.addActionListener(del -> {
            int confirm = JOptionPane.showConfirmDialog(this, "¿Borrar historial con " + otroUsuario + "?", "Confirmar", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                sistema.eliminarConversacion(otroUsuario);
                // AHORA SÍ PODEMOS USAR 'panelMensajes' PORQUE YA FUE CREADO ARRIBA
                panelMensajes.removeAll();
                panelMensajes.revalidate();
                panelMensajes.repaint();
            }
        });

        header.add(lblNombre, BorderLayout.WEST);
        header.add(btnDelete, BorderLayout.EAST);

        // --- 3. AÑADIR AL PANEL PRINCIPAL ---
        panelChat.add(header, BorderLayout.NORTH);
        panelChat.add(scrollMensajes, BorderLayout.CENTER);

        // Footer (Input)
        JPanel footer = new JPanel(new BorderLayout());
        footer.setBackground(Color.WHITE);
        footer.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        JTextField txtInput = new JTextField();
        txtInput.setFont(new Font("Arial", Font.PLAIN, 14));

        JButton btnSend = new JButton("Enviar");
        btnSend.setBackground(COLOR_BOTTON);
        btnSend.setForeground(Color.WHITE);
        btnSend.setFont(new Font("Arial", Font.BOLD, 12));
        // --- NUEVO: BOTÓN STICKER ---
        JButton btnSticker = new JButton("😀"); // Icono simple
        btnSticker.setFont(new Font("Arial", Font.PLAIN, 16));
        btnSticker.setBackground(COLOR_FIELD);
        btnSticker.setFocusPainted(false);
        btnSticker.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Acción del botón Sticker
        // Acción del botón Sticker
        btnSticker.addActionListener(ev -> {
            // Obtenemos TODOS los stickers (globales + personales)
            ArrayList<String> stickers = sistema.getTodosStickers(sistema.getUsuarioActual().getUsername());

            // Creamos un modelo para la lista visual
            DefaultListModel<String> model = new DefaultListModel<>();
            model.addElement("--> Importar nuevo sticker..."); // Opción especial al inicio

            for (String ruta : stickers) {
                model.addElement(new File(ruta).getName()); // Mostramos solo el nombre
            }

            JList<String> listaVisual = new JList<>(model);
            int opcion = JOptionPane.showConfirmDialog(this,
                    new JScrollPane(listaVisual),
                    "Mis Stickers",
                    JOptionPane.OK_CANCEL_OPTION);

            if (opcion == JOptionPane.OK_OPTION) {
                String seleccionVisual = listaVisual.getSelectedValue();

                // CASO 1: El usuario quiere importar
                if (seleccionVisual != null && seleccionVisual.equals("--> Importar nuevo sticker...")) {
                    JFileChooser fc = new JFileChooser();
                    fc.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Imágenes", "png", "jpg", "jpeg"));
                    int res = fc.showOpenDialog(this);

                    if (res == JFileChooser.APPROVE_OPTION) {
                        File archivo = fc.getSelectedFile();
                        boolean exito = sistema.guardarStickerPersonal(archivo, sistema.getUsuarioActual().getUsername());
                        if (exito) {
                            JOptionPane.showMessageDialog(this, "Sticker importado con éxito.");
                        }
                    }
                } // CASO 2: El usuario seleccionó un sticker existente
                else if (seleccionVisual != null) {
                    // Buscamos la ruta completa correspondiente al nombre
                    String rutaCompleta = null;
                    for (String ruta : stickers) {
                        if (ruta.endsWith(seleccionVisual)) {
                            rutaCompleta = ruta;
                            break;
                        }
                    }

                    if (rutaCompleta != null) {
                        sistema.enviarMensaje(otroUsuario, rutaCompleta, "STICKER");
                        refrescarMensajes(panelMensajes, otroUsuario);
                    }
                }
            }
        });

        footer.add(txtInput, BorderLayout.CENTER);
        footer.add(btnSend, BorderLayout.EAST);
        footer.add(btnSticker, BorderLayout.WEST); // Añadido a la izquierda

        panelChat.add(footer, BorderLayout.SOUTH);
        // Acción de Enviar
        ActionListener sendAction = e -> {
            String texto = txtInput.getText();
            if (!texto.isEmpty()) {
                sistema.enviarMensaje(otroUsuario, texto, "TEXTO");
                txtInput.setText("");
                refrescarMensajes(panelMensajes, otroUsuario); // Refresco inmediato
            }
        };
        btnSend.addActionListener(sendAction);
        txtInput.addActionListener(sendAction);

        // Carga inicial de mensajes
        refrescarMensajes(panelMensajes, otroUsuario);

        // --- INICIO DEL TIMER (CHAT LIVE) ---
        // Cada 2000 ms (2 segundos) se actualiza el panel
        chatTimer = new Timer(2000, ev -> refrescarMensajes(panelMensajes, otroUsuario));
        chatTimer.start();

        panelChat.revalidate();
        panelChat.repaint();
    }

    // --- NUEVO MÉTODO AUXILIAR: Solo actualiza los textos ---
    private void refrescarMensajes(JPanel panelMensajes, String otroUsuario) {
        sistema.marcarComoLeido(otroUsuario);

        panelMensajes.removeAll();
        ArrayList<Mensaje> historial = sistema.getConversacion(otroUsuario);

        for (Mensaje m : historial) {
            JPanel msgPanel = new JPanel(new BorderLayout());
            msgPanel.setBackground(COLOR_FONDO);
            msgPanel.setMaximumSize(new Dimension(450, Integer.MAX_VALUE));

            // --- LÓGICA PARA DISTINGUIR TEXTO DE STICKER ---
            // Asumimos que MensajeSticker devuelve la ruta en getContenido()
            // Podemos saber si es sticker si la clase es MensajeSticker o si el contenido es una ruta conocida
            boolean esSticker = m instanceof instagram.MensajeSticker;
            // Si no tienes la instancia, puedes usar: m.getContenido().contains("stickers_globales");

            if (esSticker) {
                // MOSTRAR IMAGEN
                JLabel lblImg = new JLabel();
                lblImg.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

                try {
                    File f = new File(m.getContenido());
                    if (f.exists()) {
                        ImageIcon icon = new ImageIcon(m.getContenido());
                        // Escalamos el sticker a un tamaño fijo, ej: 100x100
                        Image img = icon.getImage().getScaledInstance(100, 100, Image.SCALE_SMOOTH);
                        lblImg.setIcon(new ImageIcon(img));
                    } else {
                        lblImg.setText("[Sticker no encontrado]");
                    }
                } catch (Exception e) {
                    lblImg.setText("[Error sticker]");
                }

                // Alineación (Derecha si soy yo, Izquierda si es otro)
                if (m.getEmisor().equals(sistema.getUsuarioActual().getUsername())) {
                    msgPanel.add(lblImg, BorderLayout.EAST);
                } else {
                    msgPanel.add(lblImg, BorderLayout.WEST);
                }

            } else {
                // MOSTRAR TEXTO (Tu código anterior)
                JTextArea txtMsg = new JTextArea(m.getContenido());
                txtMsg.setLineWrap(true);
                txtMsg.setWrapStyleWord(true);
                txtMsg.setFont(new Font("Arial", Font.PLAIN, 12));
                txtMsg.setEditable(false);
                txtMsg.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
                txtMsg.setPreferredSize(new Dimension(300, 0));

                if (m.getEmisor().equals(sistema.getUsuarioActual().getUsername())) {
                    msgPanel.add(txtMsg, BorderLayout.EAST);
                    txtMsg.setBackground(COLOR_BOTTON);
                    txtMsg.setForeground(Color.WHITE);
                } else {
                    msgPanel.add(txtMsg, BorderLayout.WEST);
                    txtMsg.setBackground(Color.WHITE);
                }
            }

            panelMensajes.add(msgPanel);
            panelMensajes.add(Box.createVerticalStrut(5));
        }

        // Auto-scroll
        SwingUtilities.invokeLater(() -> {
            Container parent = panelMensajes.getParent();
            if (parent instanceof JScrollPane) {
                JScrollBar bar = ((JScrollPane) parent).getVerticalScrollBar();
                bar.setValue(bar.getMaximum());
            }
        });

        panelMensajes.revalidate();
        panelMensajes.repaint();
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

        // Panel central con scroll
        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(COLOR_FONDO);
        JScrollPane scroll = new JScrollPane(content);
        scroll.setBorder(null);
        mainPanel.add(scroll, BorderLayout.CENTER);

        add(mainPanel, BorderLayout.CENTER);

        // --- 1. SECCIÓN SOLICITUDES ---
        ArrayList<String> solicitudes = sistema.getSolicitudes();
        if (!solicitudes.isEmpty()) {
            content.add(crearTituloSeccion("Solicitudes de Seguimiento"));
            for (String user : solicitudes) {
                content.add(crearPanelSolicitud(user));
                content.add(Box.createVerticalStrut(5));
            }
            content.add(Box.createVerticalStrut(20));
        }

        // --- 2. SECCIÓN LIKES ---
        ArrayList<String> likes = sistema.getNotificacionesLikes();
        if (!likes.isEmpty()) {
            content.add(crearTituloSeccion("Likes en tus publicaciones"));

            // Recorremos al revés para ver los más recientes primero
            for (int i = likes.size() - 1; i >= 0; i--) {
                String[] datos = likes.get(i).split("\\|");
                // datos[0]=autor (yo), datos[1]=fecha, datos[2]=quien dio like
                if (datos.length >= 3) {
                    String fecha = datos[1];
                    String quien = datos[2];
                    content.add(crearPanelNotificacionLike(quien, fecha));
                    content.add(Box.createVerticalStrut(5));
                }
            }
        }
        // --- NUEVO: 3. SECCIÓN MENCIONES ---
        ArrayList<Publicacion> menciones = sistema.getMenciones(); // Asegúrate de tener este método en Sistema
        if (!menciones.isEmpty()) {
            content.add(crearTituloSeccion("Te mencionaron en:"));
            for (Publicacion p : menciones) {
                JPanel panelM = new JPanel(new FlowLayout(FlowLayout.LEFT));
                panelM.setBackground(Color.WHITE);
                panelM.setMaximumSize(new Dimension(500, 40));
                JLabel lblM = new JLabel(p.getAutor() + " te mencionó: " + p.getContenido().substring(0, Math.min(p.getContenido().length(), 20)) + "...");
                lblM.setFont(new Font("Arial", Font.PLAIN, 12));
                panelM.add(lblM);
                content.add(panelM);
            }
        }
        if (solicitudes.isEmpty() && likes.isEmpty()) {
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
}
