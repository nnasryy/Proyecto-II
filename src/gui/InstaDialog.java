package gui;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;

/**
 * InstaDialog — diálogos estilo Instagram sin decoración del SO.
 *
 * BUG CORREGIDO: el código anterior usaba (Frame) null como owner,
 * lo que hacía que windowClosed no disparara en algunos sistemas,
 * dejando el glasspane activo y bloqueando toda la interfaz.
 *
 * SOLUCIÓN: findFrame() obtiene el JFrame real como owner.
 * El JDialog modal bloquea el padre por sí solo — sin glasspane.
 * El overlay oscuro se dibuja dentro del propio JDialog.
 */
public class InstaDialog {

    private static final Color BG        = Color.WHITE;
    private static final Color BLUE      = new Color(64,  155, 230);
    private static final Color DANGER    = new Color(237, 73,  86);
    private static final Color DIVIDER   = new Color(219, 219, 219);
    private static final Color TEXT_MAIN = new Color(38,  38,  38);
    private static final Color TEXT_GRAY = new Color(142, 142, 142);
    private static final Color OVERLAY   = new Color(0, 0, 0, 100);
    private static final Font  FONT_TITLE = new Font("Arial", Font.BOLD,  15);
    private static final Font  FONT_BODY  = new Font("Arial", Font.PLAIN, 13);
    private static final Font  FONT_BTN   = new Font("Arial", Font.BOLD,  13);
    private static final int   RADIUS     = 14;

    // ── MENSAJE SIMPLE ───────────────────────────────────────────
    public static void showMessage(Component parent, String message) {
        showMessage(parent, message, false);
    }

    public static void showMessage(Component parent, String message, boolean danger) {
        JDialog d = buildBase(parent, 320, 165);
        JPanel card = buildCard(300, 145);
        card.setLayout(new BorderLayout());

        JLabel lbl = new JLabel(
            "<html><div style='text-align:center;'>" + htmlSafe(message) + "</div></html>",
            SwingConstants.CENTER);
        lbl.setFont(FONT_BODY);
        lbl.setForeground(TEXT_MAIN);
        lbl.setBorder(BorderFactory.createEmptyBorder(22, 18, 10, 18));
        card.add(lbl, BorderLayout.CENTER);

        JButton ok = makeButton("OK", danger ? DANGER : BLUE, Color.WHITE);
        ok.addActionListener(e -> d.dispose());
        card.add(buildSouthSingle(ok), BorderLayout.SOUTH);

        show(d, card);
    }

    // ── CONFIRMACIÓN ─────────────────────────────────────────────
    public static boolean showConfirm(Component parent, String message,
                                      String yesLabel, boolean dangerYes) {
        boolean[] result = {false};
        JDialog d = buildBase(parent, 320, 175);
        JPanel card = buildCard(300, 155);
        card.setLayout(new BorderLayout());

        JLabel lbl = new JLabel(
            "<html><div style='text-align:center;'>" + htmlSafe(message) + "</div></html>",
            SwingConstants.CENTER);
        lbl.setFont(FONT_BODY);
        lbl.setForeground(TEXT_MAIN);
        lbl.setBorder(BorderFactory.createEmptyBorder(24, 18, 10, 18));
        card.add(lbl, BorderLayout.CENTER);

        JButton btnYes = makeButton(yesLabel, dangerYes ? DANGER : BLUE, Color.WHITE);
        JButton btnNo  = makeButton("Cancelar", new Color(248, 248, 248), TEXT_MAIN);
        btnYes.addActionListener(e -> { result[0] = true; d.dispose(); });
        btnNo.addActionListener(e  -> d.dispose());
        card.add(buildSouthPair(btnNo, btnYes), BorderLayout.SOUTH);

        show(d, card);
        return result[0];
    }

    // ── INPUT ────────────────────────────────────────────────────
    public static String showInput(Component parent, String title, String placeholder) {
        String[] result = {null};
        JDialog d = buildBase(parent, 340, 200);
        JPanel card = buildCard(320, 182);
        card.setLayout(new BorderLayout());

        JLabel lbl = new JLabel(title, SwingConstants.CENTER);
        lbl.setFont(FONT_TITLE);
        lbl.setForeground(TEXT_MAIN);
        lbl.setBorder(BorderFactory.createEmptyBorder(18, 18, 6, 18));
        card.add(lbl, BorderLayout.NORTH);

        JTextField txt = buildInputField(placeholder);
        JPanel mid = new JPanel(new BorderLayout());
        mid.setBackground(BG);
        mid.setBorder(BorderFactory.createEmptyBorder(6, 18, 6, 18));
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

        show(d, card);
        return result[0];
    }

    // ════════════════════════════════════════════════════════════
    //  NÚCLEO
    // ════════════════════════════════════════════════════════════
    private static JDialog buildBase(Component parent, int w, int h) {
        Frame owner = findFrame(parent);
        JDialog d   = new JDialog(owner, true);   // modal — bloquea sin glasspane
        d.setUndecorated(true);
        try {
            d.getRootPane().setOpaque(false);
            d.getContentPane().setBackground(new Color(0, 0, 0, 0));
            d.setBackground(new Color(0, 0, 0, 0));
        } catch (Exception ignored) {}

        d.setSize(w, h);
        d.setLocationRelativeTo(parent);

        // Cerrar con Escape
        d.getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
            .put(KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_ESCAPE, 0), "esc_close");
        d.getRootPane().getActionMap()
            .put("esc_close", new AbstractAction() {
                public void actionPerformed(ActionEvent e) { d.dispose(); }
            });
        return d;
    }

    private static void show(JDialog d, JPanel card) {
        JPanel root = new JPanel(new GridBagLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(OVERLAY);
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
            }
        };
        root.setOpaque(false);
        root.add(card);                // GridBagLayout centra automáticamente
        card.setOpaque(false);
        d.setContentPane(root);
        d.setVisible(true);            // bloquea aquí (modal); retorna al dispose()
    }

    // ════════════════════════════════════════════════════════════
    //  HELPERS
    // ════════════════════════════════════════════════════════════
    /** Sube por la jerarquía hasta el JFrame raíz. */
    private static Frame findFrame(Component c) {
        if (c instanceof Frame) return (Frame) c;
        Window w = (c instanceof Window) ? (Window) c : SwingUtilities.getWindowAncestor(c);
        while (w != null) {
            if (w instanceof Frame) return (Frame) w;
            w = w.getOwner();
        }
        return null;
    }

    private static JPanel buildCard(int w, int h) {
        JPanel card = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(0, 0, 0, 28));
                g2.fill(new RoundRectangle2D.Double(3, 4, getWidth()-3, getHeight()-3, RADIUS*2, RADIUS*2));
                g2.setColor(BG);
                g2.fill(new RoundRectangle2D.Double(0, 0, getWidth()-2, getHeight()-2, RADIUS*2, RADIUS*2));
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setPreferredSize(new Dimension(w, h));
        return card;
    }

    private static JPanel buildSouthSingle(JButton btn) {
        JSeparator sep = new JSeparator(); sep.setForeground(DIVIDER); sep.setBackground(DIVIDER);
        JPanel row = new JPanel(new GridLayout(1, 1)); row.setBackground(BG); row.add(btn);
        JPanel s = new JPanel(new BorderLayout()); s.setBackground(BG);
        s.add(sep, BorderLayout.NORTH); s.add(row, BorderLayout.CENTER);
        return s;
    }

    private static JPanel buildSouthPair(JButton left, JButton right) {
        JSeparator top = new JSeparator(); top.setForeground(DIVIDER); top.setBackground(DIVIDER);
        JPanel lw = new JPanel(new GridLayout(1,1)); lw.setBackground(BG); lw.add(left);  lw.setPreferredSize(new Dimension(159, 44));
        JPanel rw = new JPanel(new GridLayout(1,1)); rw.setBackground(BG); rw.add(right); rw.setPreferredSize(new Dimension(159, 44));
        JSeparator vs = new JSeparator(SwingConstants.VERTICAL); vs.setPreferredSize(new Dimension(1, 44)); vs.setForeground(DIVIDER);
        JPanel pair = new JPanel(new BorderLayout()); pair.setBackground(BG);
        pair.add(lw, BorderLayout.WEST); pair.add(vs, BorderLayout.CENTER); pair.add(rw, BorderLayout.EAST);
        JPanel s = new JPanel(new BorderLayout()); s.setBackground(BG);
        s.add(top, BorderLayout.NORTH); s.add(pair, BorderLayout.CENTER);
        return s;
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

    private static String htmlSafe(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;")
                   .replace("<", "&lt;")
                   .replace(">", "&gt;")
                   .replace("\n", "<br>");
    }
}