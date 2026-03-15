package gui;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;

/**
 * InstaDialog — reemplaza JOptionPane con diálogos estilo Instagram.
 * Sin decoración del SO, flota sobre la ventana padre con overlay oscuro.
 */
public class InstaDialog {

    private static final Color BG        = Color.WHITE;
    private static final Color OVERLAY   = new Color(0, 0, 0, 120);
    private static final Color BLUE      = new Color(64, 155, 230);
    private static final Color DANGER    = new Color(237, 73, 86);
    private static final Color DIVIDER   = new Color(219, 219, 219);
    private static final Color TEXT_MAIN = new Color(38,  38,  38);
    private static final Color TEXT_GRAY = new Color(142, 142, 142);
    private static final Font  FONT_TITLE = new Font("Arial", Font.BOLD,  15);
    private static final Font  FONT_BODY  = new Font("Arial", Font.PLAIN, 13);
    private static final Font  FONT_BTN   = new Font("Arial", Font.BOLD,  13);
    private static final int   RADIUS     = 14;

    // ──────────────────────────────────────────────────────────────
    // MENSAJE SIMPLE
    // ──────────────────────────────────────────────────────────────
    public static void showMessage(Component parent, String message) {
        showMessage(parent, message, false);
    }

    public static void showMessage(Component parent, String message, boolean danger) {
        JDialog d = buildBase(parent, 320, 160);

        JPanel card = buildCard(320, 160);
        card.setLayout(new BorderLayout());

        JLabel lbl = new JLabel(
            "<html><div style='text-align:center;'>" + message + "</div></html>",
            SwingConstants.CENTER);
        lbl.setFont(FONT_BODY);
        lbl.setForeground(TEXT_MAIN);
        lbl.setBorder(BorderFactory.createEmptyBorder(22, 20, 10, 20));
        card.add(lbl, BorderLayout.CENTER);

        JButton ok = makeButton("OK", danger ? DANGER : BLUE, Color.WHITE);
        ok.addActionListener(e -> d.dispose());

        JPanel south = buildSouth(ok);
        card.add(south, BorderLayout.SOUTH);

        finalize(d, card);
    }

    // ──────────────────────────────────────────────────────────────
    // CONFIRMACIÓN
    // ──────────────────────────────────────────────────────────────
    public static boolean showConfirm(Component parent, String message,
                                      String yesLabel, boolean dangerYes) {
        boolean[] result = {false};
        JDialog d = buildBase(parent, 320, 170);

        JPanel card = buildCard(320, 170);
        card.setLayout(new BorderLayout());

        JLabel lbl = new JLabel(
            "<html><div style='text-align:center;'>" + message + "</div></html>",
            SwingConstants.CENTER);
        lbl.setFont(FONT_BODY);
        lbl.setForeground(TEXT_MAIN);
        lbl.setBorder(BorderFactory.createEmptyBorder(26, 20, 12, 20));
        card.add(lbl, BorderLayout.CENTER);

        JButton btnYes = makeButton(yesLabel, dangerYes ? DANGER : BLUE, Color.WHITE);
        JButton btnNo  = makeButton("Cancelar", new Color(248, 248, 248), TEXT_MAIN);

        btnYes.addActionListener(e -> { result[0] = true; d.dispose(); });
        btnNo.addActionListener(e -> d.dispose());

        card.add(buildSouthPair(btnNo, btnYes), BorderLayout.SOUTH);

        finalize(d, card);
        return result[0];
    }

    // ──────────────────────────────────────────────────────────────
    // INPUT
    // ──────────────────────────────────────────────────────────────
    public static String showInput(Component parent, String title, String placeholder) {
        String[] result = {null};
        JDialog d = buildBase(parent, 340, 195);

        JPanel card = buildCard(340, 195);
        card.setLayout(new BorderLayout());

        JLabel lbl = new JLabel(title, SwingConstants.CENTER);
        lbl.setFont(FONT_TITLE);
        lbl.setForeground(TEXT_MAIN);
        lbl.setBorder(BorderFactory.createEmptyBorder(20, 20, 8, 20));
        card.add(lbl, BorderLayout.NORTH);

        JTextField txt = buildInputField(placeholder);
        JPanel mid = new JPanel(new BorderLayout());
        mid.setBackground(BG);
        mid.setBorder(BorderFactory.createEmptyBorder(6, 20, 6, 20));
        mid.add(txt, BorderLayout.CENTER);
        card.add(mid, BorderLayout.CENTER);

        JButton ok  = makeButton("Aceptar",  BLUE, Color.WHITE);
        JButton can = makeButton("Cancelar", new Color(248, 248, 248), TEXT_MAIN);

        ok.addActionListener(e -> {
            String val = txt.getText().trim();
            if (!val.isEmpty() && !val.equals(placeholder)) result[0] = val;
            d.dispose();
        });
        can.addActionListener(e -> d.dispose());
        txt.addActionListener(e -> ok.doClick());

        card.add(buildSouthPair(can, ok), BorderLayout.SOUTH);

        finalize(d, card);
        return result[0];
    }

    // ──────────────────────────────────────────────────────────────
    // HELPERS INTERNOS
    // ──────────────────────────────────────────────────────────────

    /**
     * Construye el diálogo base sin decoración del SO.
     * Instala un glass pane con overlay oscuro sobre la ventana padre
     * para que el diálogo se sienta parte de la app, no del sistema.
     */
    private static JDialog buildBase(Component parent, int w, int h) {
        Window win = (parent instanceof Window) ? (Window) parent
                   : (parent != null ? SwingUtilities.getWindowAncestor(parent) : null);

        // Siempre usamos Frame null como owner para garantizar setUndecorated(true).
        // Si el parent es un JDialog hijo, el undecorated no funciona con Dialog como owner.
        // La modalidad la manejamos manualmente bloqueando el glass pane.
        JDialog d = new JDialog((Frame) null, true);
        d.setUndecorated(true);

        // Transparencia — orden crítico: undecorated antes de setOpaque/setBackground
        try {
            d.getRootPane().setOpaque(false);
            d.getContentPane().setBackground(new Color(0, 0, 0, 0));
            d.setBackground(new Color(0, 0, 0, 0));
        } catch (Exception ignored) {}

        d.setSize(w, h);
        d.setLocationRelativeTo(parent);

        // Glass pane oscuro sobre la ventana raíz (JFrame) mientras el diálogo está abierto
        JFrame rootFrame = null;
        if (win instanceof JFrame) {
            rootFrame = (JFrame) win;
        } else if (win != null) {
            // Buscar el JFrame raíz subiendo por la jerarquía
            Window owner = win;
            while (owner != null && !(owner instanceof JFrame)) {
                owner = owner.getOwner();
            }
            if (owner instanceof JFrame) rootFrame = (JFrame) owner;
        }

        if (rootFrame != null) {
            final JFrame frame = rootFrame;
            Component oldGlass = frame.getGlassPane();

            JPanel glass = new JPanel() {
                @Override protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setColor(OVERLAY);
                    g2.fillRect(0, 0, getWidth(), getHeight());
                    g2.dispose();
                }
            };
            glass.setOpaque(false);
            glass.addMouseListener(new MouseAdapter() {}); // bloquear clicks al fondo

            frame.setGlassPane(glass);
            glass.setVisible(true);

            d.addWindowListener(new WindowAdapter() {
                @Override public void windowClosed(WindowEvent e) {
                    glass.setVisible(false);
                    frame.setGlassPane(oldGlass);
                    frame.repaint();
                }
            });
        }

        return d;
    }

    private static JPanel buildCard(int w, int h) {
        JPanel card = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // Sombra suave
                g2.setColor(new Color(0, 0, 0, 30));
                g2.fill(new RoundRectangle2D.Double(3, 4, getWidth()-3, getHeight()-3, RADIUS*2, RADIUS*2));
                // Fondo blanco
                g2.setColor(BG);
                g2.fill(new RoundRectangle2D.Double(0, 0, getWidth()-2, getHeight()-2, RADIUS*2, RADIUS*2));
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setPreferredSize(new Dimension(w, h));
        return card;
    }

    private static void finalize(JDialog d, JPanel card) {
        d.setContentPane(card);
        // Asegurar transparencia en el content pane también
        card.setOpaque(false);
        d.pack();
        d.setVisible(true);
    }

    /** Panel sur con un solo botón */
    private static JPanel buildSouth(JButton btn) {
        JSeparator sep = new JSeparator();
        sep.setForeground(DIVIDER); sep.setBackground(DIVIDER);
        JPanel btnRow = new JPanel(new GridLayout(1, 1));
        btnRow.setBackground(BG);
        btnRow.add(btn);
        JPanel south = new JPanel(new BorderLayout());
        south.setBackground(BG);
        south.add(sep, BorderLayout.NORTH);
        south.add(btnRow, BorderLayout.CENTER);
        return south;
    }

    /** Panel sur con dos botones separados por línea vertical */
    private static JPanel buildSouthPair(JButton left, JButton right) {
        JSeparator topSep = new JSeparator();
        topSep.setForeground(DIVIDER); topSep.setBackground(DIVIDER);

        JPanel lw = new JPanel(new GridLayout(1,1)); lw.setBackground(BG); lw.add(left);
        JPanel rw = new JPanel(new GridLayout(1,1)); rw.setBackground(BG); rw.add(right);
        lw.setPreferredSize(new Dimension(159, 44));
        rw.setPreferredSize(new Dimension(159, 44));

        JSeparator vs = new JSeparator(SwingConstants.VERTICAL);
        vs.setPreferredSize(new Dimension(1, 44));
        vs.setForeground(DIVIDER);

        JPanel pair = new JPanel(new BorderLayout());
        pair.setBackground(BG);
        pair.add(lw, BorderLayout.WEST);
        pair.add(vs, BorderLayout.CENTER);
        pair.add(rw, BorderLayout.EAST);

        JPanel south = new JPanel(new BorderLayout());
        south.setBackground(BG);
        south.add(topSep, BorderLayout.NORTH);
        south.add(pair,   BorderLayout.CENTER);
        return south;
    }

    private static JButton makeButton(String text, Color bg, Color fg) {
        JButton b = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isPressed() ? bg.darker() : bg);
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
                super.paintComponent(g);
            }
        };
        b.setFont(FONT_BTN);
        b.setForeground(fg);
        b.setBackground(bg);
        b.setBorderPainted(false);
        b.setFocusPainted(false);
        b.setContentAreaFilled(false);
        b.setOpaque(false);
        b.setPreferredSize(new Dimension(0, 44));
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return b;
    }

    private static JTextField buildInputField(String placeholder) {
        JTextField txt = new JTextField();
        txt.setFont(FONT_BODY);
        txt.setText(placeholder);
        txt.setForeground(TEXT_GRAY);
        txt.setBackground(new Color(250, 250, 250));
        txt.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(DIVIDER, 1, true),
            BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
        txt.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent e) {
                if (txt.getText().equals(placeholder)) {
                    txt.setText(""); txt.setForeground(TEXT_MAIN);
                }
            }
            public void focusLost(FocusEvent e) {
                if (txt.getText().isEmpty()) {
                    txt.setText(placeholder); txt.setForeground(TEXT_GRAY);
                }
            }
        });
        return txt;
    }
}