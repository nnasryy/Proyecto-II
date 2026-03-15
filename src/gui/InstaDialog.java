/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package gui;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;

/**
 * InstaDialog — reemplaza JOptionPane con diálogos estilo Instagram 2017-2018.
 * Minimalista, bordes redondeados, tipografía limpia, sin chrome del SO.
 */
public class InstaDialog {

    // Paleta coherente con VentanaPrincipal
    private static final Color BG          = Color.WHITE;
    private static final Color OVERLAY     = new Color(0, 0, 0, 160);
    private static final Color BLUE        = new Color(64, 155, 230);
    private static final Color DANGER      = new Color(250, 89, 95);
    private static final Color DIVIDER     = new Color(235, 235, 235);
    private static final Color TEXT_MAIN   = new Color(38,  38,  38);
    private static final Color TEXT_GRAY   = new Color(142, 142, 142);
    private static final Font  FONT_TITLE  = new Font("Arial", Font.BOLD,   15);
    private static final Font  FONT_BODY   = new Font("Arial", Font.PLAIN,  13);
    private static final Font  FONT_BTN    = new Font("Arial", Font.BOLD,   13);
    private static final int   RADIUS      = 14;

    // ──────────────────────────────────────────────────────────────
    // MENSAJE SIMPLE  (tipo showMessageDialog)
    // ──────────────────────────────────────────────────────────────
    public static void showMessage(Component parent, String message) {
        showMessage(parent, message, false);
    }

    public static void showMessage(Component parent, String message, boolean danger) {
        JDialog d = buildBase(parent, 320, 160);

        JPanel card = buildCard(320, 160);
        card.setLayout(new BorderLayout());

        JLabel lbl = new JLabel("<html><div style='text-align:center;'>" + message + "</div></html>", SwingConstants.CENTER);
        lbl.setFont(FONT_BODY);
        lbl.setForeground(TEXT_MAIN);
        lbl.setBorder(BorderFactory.createEmptyBorder(20, 20, 10, 20));
        card.add(lbl, BorderLayout.CENTER);

        JSeparator sep = new JSeparator();
        sep.setForeground(DIVIDER);
        sep.setBackground(DIVIDER);

        JButton ok = makeButton("OK", danger ? DANGER : BLUE, Color.WHITE);
        ok.addActionListener(e -> d.dispose());
        JPanel btnRow = new JPanel(new GridLayout(1, 1));
        btnRow.setBackground(BG);
        btnRow.add(ok);

        JPanel south = new JPanel(new BorderLayout());
        south.setBackground(BG);
        south.add(sep, BorderLayout.NORTH);
        south.add(btnRow, BorderLayout.CENTER);
        card.add(south, BorderLayout.SOUTH);

        finalize(d, card);
    }

    // ──────────────────────────────────────────────────────────────
    // CONFIRMACIÓN  (tipo showConfirmDialog → YES/NO)
    // ──────────────────────────────────────────────────────────────
    /** @return true si el usuario eligió "Sí" */
    public static boolean showConfirm(Component parent, String message,
                                      String yesLabel, boolean dangerYes) {
        boolean[] result = {false};
        JDialog d = buildBase(parent, 320, 170);

        JPanel card = buildCard(320, 170);
        card.setLayout(new BorderLayout());

        JLabel lbl = new JLabel("<html><div style='text-align:center;'>" + message + "</div></html>", SwingConstants.CENTER);
        lbl.setFont(FONT_BODY);
        lbl.setForeground(TEXT_MAIN);
        lbl.setBorder(BorderFactory.createEmptyBorder(24, 20, 12, 20));
        card.add(lbl, BorderLayout.CENTER);

        JButton btnYes = makeButton(yesLabel, dangerYes ? DANGER : BLUE, Color.WHITE);
        JButton btnNo  = makeButton("Cancelar", new Color(248, 248, 248), TEXT_MAIN);
        btnNo.setForeground(TEXT_MAIN);

        btnYes.addActionListener(e -> { result[0] = true; d.dispose(); });
        btnNo.addActionListener(e -> d.dispose());

        JPanel btnRow = new JPanel(new GridLayout(1, 2, 0, 0));
        btnRow.setBackground(BG);
        btnRow.add(btnNo);

        JSeparator vsep = new JSeparator(SwingConstants.VERTICAL);
        vsep.setForeground(DIVIDER);

        JPanel btnYesWrapper = new JPanel(new GridLayout(1,1));
        btnYesWrapper.setBackground(BG);
        btnYesWrapper.add(btnYes);

        // Separador vertical entre botones
        JPanel pair = new JPanel(new BorderLayout());
        pair.setBackground(BG);
        JPanel leftBtn = new JPanel(new GridLayout(1,1));
        leftBtn.setBackground(BG);
        leftBtn.add(btnNo);
        JPanel rightBtn = new JPanel(new GridLayout(1,1));
        rightBtn.setBackground(BG);
        rightBtn.add(btnYes);
        JSeparator mid = new JSeparator(SwingConstants.VERTICAL);
        mid.setPreferredSize(new Dimension(1, 44));
        mid.setForeground(DIVIDER);
        pair.add(leftBtn, BorderLayout.WEST);
        pair.add(mid, BorderLayout.CENTER);
        pair.add(rightBtn, BorderLayout.EAST);
        leftBtn.setPreferredSize(new Dimension(159, 44));
        rightBtn.setPreferredSize(new Dimension(159, 44));

        JSeparator topSep = new JSeparator();
        topSep.setForeground(DIVIDER);
        topSep.setBackground(DIVIDER);

        JPanel south = new JPanel(new BorderLayout());
        south.setBackground(BG);
        south.add(topSep, BorderLayout.NORTH);
        south.add(pair, BorderLayout.CENTER);
        card.add(south, BorderLayout.SOUTH);

        finalize(d, card);
        return result[0];
    }

    // ──────────────────────────────────────────────────────────────
    // INPUT  (tipo showInputDialog)
    // ──────────────────────────────────────────────────────────────
    /** @return texto ingresado, o null si canceló */
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
        JButton can = makeButton("Cancelar", new Color(248,248,248), TEXT_MAIN);

        ok.addActionListener(e -> {
            String val = txt.getText().trim();
            if (!val.isEmpty() && !val.equals(placeholder)) result[0] = val;
            d.dispose();
        });
        can.addActionListener(e -> d.dispose());
        txt.addActionListener(e -> ok.doClick());

        JSeparator sep = new JSeparator(); sep.setForeground(DIVIDER); sep.setBackground(DIVIDER);
        JPanel pair = makePair(can, ok);
        JPanel south = new JPanel(new BorderLayout());
        south.setBackground(BG);
        south.add(sep, BorderLayout.NORTH);
        south.add(pair, BorderLayout.CENTER);
        card.add(south, BorderLayout.SOUTH);

        finalize(d, card);
        return result[0];
    }

    // ──────────────────────────────────────────────────────────────
    // HELPER INTERNO: buildBase
    // ──────────────────────────────────────────────────────────────
    private static JDialog buildBase(Component parent, int w, int h) {
        Window win = (parent instanceof Window) ? (Window) parent
                   : (parent != null ? SwingUtilities.getWindowAncestor(parent) : null);
        JDialog d;
        if (win instanceof Frame)  d = new JDialog((Frame)  win, true);
        else if (win instanceof Dialog) d = new JDialog((Dialog) win, true);
        else d = new JDialog((Frame) null, true);

        d.setUndecorated(true);
        d.setSize(w, h);
        d.setLocationRelativeTo(parent);
        d.getRootPane().putClientProperty("apple.awt.draggableWindowBackground", Boolean.FALSE);
        d.setBackground(new Color(0,0,0,0));
        return d;
    }

    private static JPanel buildCard(int w, int h) {
        JPanel card = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(BG);
                g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), RADIUS*2, RADIUS*2));
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setPreferredSize(new Dimension(w, h));
        card.setBorder(new AbstractBorder() {
            @Override public void paintBorder(Component c, Graphics g, int x, int y, int w2, int h2) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(DIVIDER);
                g2.setStroke(new BasicStroke(1f));
                g2.draw(new RoundRectangle2D.Double(0.5, 0.5, w2-1, h2-1, RADIUS*2, RADIUS*2));
                g2.dispose();
            }
        });
        return card;
    }

    private static void finalize(JDialog d, JPanel card) {
        d.setContentPane(card);
        d.pack();
        d.setVisible(true);
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
                if (txt.getText().equals(placeholder)) { txt.setText(""); txt.setForeground(TEXT_MAIN); }
            }
            public void focusLost(FocusEvent e) {
                if (txt.getText().isEmpty()) { txt.setText(placeholder); txt.setForeground(TEXT_GRAY); }
            }
        });
        return txt;
    }

    private static JPanel makePair(JButton left, JButton right) {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(BG);
        JPanel lw = new JPanel(new GridLayout(1,1)); lw.setBackground(BG); lw.add(left);
        JPanel rw = new JPanel(new GridLayout(1,1)); rw.setBackground(BG); rw.add(right);
        JSeparator vs = new JSeparator(SwingConstants.VERTICAL);
        vs.setPreferredSize(new Dimension(1,44)); vs.setForeground(DIVIDER);
        p.add(lw, BorderLayout.WEST);
        p.add(vs, BorderLayout.CENTER);
        p.add(rw, BorderLayout.EAST);
        lw.setPreferredSize(new Dimension(169, 44));
        rw.setPreferredSize(new Dimension(169, 44));
        return p;
    }
}