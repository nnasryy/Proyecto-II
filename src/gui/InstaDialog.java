package gui;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;

/**
 * InstaDialog — diálogos estilo Instagram sin ninguna ventana del SO.
 *
 * SOLUCIÓN DEFINITIVA AL PROBLEMA DE LA BARRA DE WINDOWS:
 * ────────────────────────────────────────────────────────
 * Todos los JDialog (con o sin setUndecorated) crean una ventana
 * nativa del sistema operativo. En Windows, incluso con
 * setUndecorated(true), la barra de la taskbar aparece.
 *
 * Esta implementación NO crea ninguna ventana del SO:
 * - El diálogo es un JPanel pintado sobre el JFrame como glasspane.
 * - El bloqueo modal usa SecondaryLoop — la API oficial de Java
 *   (disponible desde Java 6) para crear bucles de eventos secundarios.
 *   Es exactamente lo que usa JDialog internamente, sin reflexión.
 * - Cero ventanas del SO → cero barra en la taskbar → cero decoración.
 * ────────────────────────────────────────────────────────
 */
public class InstaDialog {

    private static final Color BG        = Color.WHITE;
    private static final Color OVERLAY   = new Color(0, 0, 0, 150);
    private static final Color BLUE      = new Color(64,  155, 230);
    private static final Color DANGER    = new Color(237, 73,  86);
    private static final Color DIVIDER   = new Color(219, 219, 219);
    private static final Color TEXT_MAIN = new Color(38,  38,  38);
    private static final Color TEXT_GRAY = new Color(142, 142, 142);
    private static final Font  FONT_TITLE = new Font("Arial", Font.BOLD,  15);
    private static final Font  FONT_BODY  = new Font("Arial", Font.PLAIN, 13);
    private static final Font  FONT_BTN   = new Font("Arial", Font.BOLD,  13);

    // ── Estado de la instancia ───────────────────────────────────
    private boolean confirmed   = false;
    private String  inputResult = null;

    // ── Componentes del glasspane ────────────────────────────────
    private final JFrame  frame;
    private final JPanel  overlay;
    private final Component oldGlass;
    private SecondaryLoop loop;

    // ════════════════════════════════════════════════════════════
    //  API PÚBLICA
    // ════════════════════════════════════════════════════════════

    public static void showMessage(Component parent, String message) {
        showMessage(parent, message, false);
    }

    public static void showMessage(Component parent, String message, boolean danger) {
        JFrame f = findFrame(parent);
        if (f == null) { JOptionPane.showMessageDialog(null, message); return; }
        new InstaDialog(f).runMessage(message, danger);
    }

    public static boolean showConfirm(Component parent, String message,
                                       String yesLabel, boolean dangerYes) {
        JFrame f = findFrame(parent);
        if (f == null) return JOptionPane.showConfirmDialog(null, message, "",
                JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION;
        return new InstaDialog(f).runConfirm(message, yesLabel, dangerYes);
    }

    public static String showInput(Component parent, String title, String placeholder) {
        JFrame f = findFrame(parent);
        if (f == null) return JOptionPane.showInputDialog(null, title);
        return new InstaDialog(f).runInput(title, placeholder);
    }

    // ════════════════════════════════════════════════════════════
    //  CONSTRUCTOR
    // ════════════════════════════════════════════════════════════
    private InstaDialog(JFrame frame) {
        this.frame    = frame;
        this.oldGlass = frame.getGlassPane();

        // Overlay: cubre todo el frame con semitransparencia
        overlay = new JPanel(null) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(OVERLAY);
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
            }
        };
        overlay.setOpaque(false);
        overlay.setFocusable(true);

        // Capturar Escape para cerrar
        overlay.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
               .put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "close");
        overlay.getActionMap().put("close", new AbstractAction() {
            public void actionPerformed(ActionEvent e) { close(); }
        });

        // Bloquear clicks que pasen a través del overlay
        overlay.addMouseListener(new MouseAdapter() {});
        overlay.addMouseMotionListener(new MouseMotionAdapter() {});
    }

    // ════════════════════════════════════════════════════════════
    //  MÉTODOS DE EJECUCIÓN
    // ════════════════════════════════════════════════════════════
    private void runMessage(String message, boolean danger) {
        int cW = 300, cH = 148;
        JPanel card = buildCard(cW, cH);
        card.setLayout(new BorderLayout());

        JLabel lbl = bodyLabel(message);
        lbl.setBorder(BorderFactory.createEmptyBorder(22, 18, 10, 18));
        card.add(lbl, BorderLayout.CENTER);

        JButton ok = makeBtn("OK", danger ? DANGER : BLUE, Color.WHITE);
        ok.addActionListener(e -> close());
        card.add(southSingle(ok), BorderLayout.SOUTH);

        display(card, cW, cH);
    }

    private boolean runConfirm(String message, String yesLabel, boolean dangerYes) {
        int cW = 300, cH = 158;
        JPanel card = buildCard(cW, cH);
        card.setLayout(new BorderLayout());

        JLabel lbl = bodyLabel(message);
        lbl.setBorder(BorderFactory.createEmptyBorder(24, 18, 10, 18));
        card.add(lbl, BorderLayout.CENTER);

        JButton yes = makeBtn(yesLabel,  dangerYes ? DANGER : BLUE, Color.WHITE);
        JButton no  = makeBtn("Cancelar", new Color(248, 248, 248), TEXT_MAIN);
        yes.addActionListener(e -> { confirmed = true; close(); });
        no.addActionListener(e  -> close());
        card.add(southPair(no, yes), BorderLayout.SOUTH);

        display(card, cW, cH);
        return confirmed;
    }

    private String runInput(String title, String placeholder) {
        int cW = 320, cH = 184;
        JPanel card = buildCard(cW, cH);
        card.setLayout(new BorderLayout());

        JLabel lbl = new JLabel(title, SwingConstants.CENTER);
        lbl.setFont(FONT_TITLE); lbl.setForeground(TEXT_MAIN);
        lbl.setBorder(BorderFactory.createEmptyBorder(18, 18, 6, 18));
        card.add(lbl, BorderLayout.NORTH);

        JTextField txt = buildInputField(placeholder);
        JPanel mid = new JPanel(new BorderLayout());
        mid.setBackground(BG);
        mid.setBorder(BorderFactory.createEmptyBorder(6, 18, 6, 18));
        mid.add(txt, BorderLayout.CENTER);
        card.add(mid, BorderLayout.CENTER);

        JButton ok  = makeBtn("Aceptar",  BLUE, Color.WHITE);
        JButton can = makeBtn("Cancelar", new Color(248, 248, 248), TEXT_MAIN);
        ok.addActionListener(e -> {
            String v = txt.getText().trim();
            if (!v.isEmpty() && !v.equals(placeholder)) inputResult = v;
            close();
        });
        can.addActionListener(e -> close());
        txt.addActionListener(e -> ok.doClick());
        card.add(southPair(can, ok), BorderLayout.SOUTH);

        display(card, cW, cH);

        // Foco en el campo de texto
        SwingUtilities.invokeLater(txt::requestFocusInWindow);

        return inputResult;
    }

    // ════════════════════════════════════════════════════════════
    //  NÚCLEO: MOSTRAR Y BLOQUEAR CON SecondaryLoop
    // ════════════════════════════════════════════════════════════
    private void display(JPanel card, int cW, int cH) {
        // Centrar la tarjeta en el overlay
        int x = (frame.getWidth()  - cW) / 2;
        int y = (frame.getHeight() - cH) / 2 - 20; // ligero desplazamiento hacia arriba
        overlay.setBounds(0, 0, frame.getWidth(), frame.getHeight());
        card.setBounds(x, y, cW, cH);
        overlay.add(card);

        // Instalar como glasspane
        frame.setGlassPane(overlay);
        overlay.setVisible(true);
        overlay.requestFocusInWindow();

        // Crear y entrar en el SecondaryLoop — bloquea hasta que close() llame loop.exit()
        // SecondaryLoop es la API oficial de Java (java.awt.SecondaryLoop) para esto
        EventQueue eq = Toolkit.getDefaultToolkit().getSystemEventQueue();
        loop = eq.createSecondaryLoop();
        loop.enter(); // BLOQUEA aquí — retorna cuando loop.exit() es llamado
    }

    private void close() {
        overlay.setVisible(false);
        frame.setGlassPane(oldGlass);
        oldGlass.setVisible(false);
        frame.repaint();
        if (loop != null) {
            loop.exit(); // DESBLOQUEA el display()
            loop = null;
        }
    }

    // ════════════════════════════════════════════════════════════
    //  HELPERS UI
    // ════════════════════════════════════════════════════════════
    private static JPanel buildCard(int w, int h) {
        JPanel card = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // Sombra
                g2.setColor(new Color(0, 0, 0, 40));
                g2.fill(new RoundRectangle2D.Double(4, 5, w - 4, h - 4, 28, 28));
                // Fondo blanco
                g2.setColor(BG);
                g2.fill(new RoundRectangle2D.Double(0, 0, w - 2, h - 2, 28, 28));
                g2.dispose();
            }
        };
        card.setOpaque(false);
        return card;
    }

    private static JLabel bodyLabel(String message) {
        String html = "<html><div style='text-align:center;'>"
                + htmlSafe(message) + "</div></html>";
        JLabel l = new JLabel(html, SwingConstants.CENTER);
        l.setFont(FONT_BODY); l.setForeground(TEXT_MAIN);
        return l;
    }

    private static JPanel southSingle(JButton btn) {
        JSeparator sep = new JSeparator(); sep.setForeground(DIVIDER); sep.setBackground(DIVIDER);
        JPanel row = new JPanel(new GridLayout(1, 1)); row.setBackground(BG); row.add(btn);
        JPanel s = new JPanel(new BorderLayout()); s.setBackground(BG);
        s.add(sep, BorderLayout.NORTH); s.add(row, BorderLayout.CENTER);
        return s;
    }

    private static JPanel southPair(JButton left, JButton right) {
        JSeparator top = new JSeparator(); top.setForeground(DIVIDER); top.setBackground(DIVIDER);
        JPanel lw = new JPanel(new GridLayout(1, 1)); lw.setBackground(BG); lw.add(left);  lw.setPreferredSize(new Dimension(149, 44));
        JPanel rw = new JPanel(new GridLayout(1, 1)); rw.setBackground(BG); rw.add(right); rw.setPreferredSize(new Dimension(149, 44));
        JSeparator vs = new JSeparator(SwingConstants.VERTICAL);
        vs.setPreferredSize(new Dimension(1, 44)); vs.setForeground(DIVIDER);
        JPanel pair = new JPanel(new BorderLayout()); pair.setBackground(BG);
        pair.add(lw, BorderLayout.WEST); pair.add(vs, BorderLayout.CENTER); pair.add(rw, BorderLayout.EAST);
        JPanel s = new JPanel(new BorderLayout()); s.setBackground(BG);
        s.add(top, BorderLayout.NORTH); s.add(pair, BorderLayout.CENTER);
        return s;
    }

    private static JButton makeBtn(String text, Color bg, Color fg) {
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
        b.setFont(FONT_BTN); b.setForeground(fg); b.setBackground(bg);
        b.setBorderPainted(false); b.setFocusPainted(false);
        b.setContentAreaFilled(false); b.setOpaque(false);
        b.setPreferredSize(new Dimension(0, 44));
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return b;
    }

    private static JTextField buildInputField(String placeholder) {
        JTextField txt = new JTextField();
        txt.setFont(FONT_BODY); txt.setText(placeholder); txt.setForeground(TEXT_GRAY);
        txt.setBackground(new Color(250, 250, 250));
        txt.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(DIVIDER, 1, true),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)));
        txt.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent e) {
                if (txt.getText().equals(placeholder)) { txt.setText(""); txt.setForeground(TEXT_MAIN); }
            }
            public void focusLost(FocusEvent e) {
                if (txt.getText().isEmpty()) { txt.setText(placeholder); txt.setForeground(TEXT_GRAY); }
            }
        });
        return txt;
    }

    private static JFrame findFrame(Component c) {
        if (c instanceof JFrame) return (JFrame) c;
        Window w = (c instanceof Window) ? (Window) c : SwingUtilities.getWindowAncestor(c);
        while (w != null) {
            if (w instanceof JFrame) return (JFrame) w;
            w = w.getOwner();
        }
        return null;
    }

    private static String htmlSafe(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;")
                   .replace("<", "&lt;")
                   .replace(">", "&gt;")
                   .replace("\n", "<br>");
    }
}