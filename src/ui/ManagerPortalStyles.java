package ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Visual theme for the Manager portal (header + sidebar + action buttons).
 */
public final class ManagerPortalStyles {

    public static final Color MAIN_BG = new Color(240, 240, 242);
    public static final Color HEADER_BG = new Color(250, 250, 252);
    public static final Color SIDEBAR_BG = new Color(38, 38, 42);
    public static final Color SIDEBAR_BUTTON = new Color(55, 55, 60);
    public static final Color SIDEBAR_BUTTON_HOVER = new Color(70, 70, 78);
    public static final Color NAV_ACTIVE_TOP = new Color(0, 120, 215);
    public static final Color NAV_ACTIVE_BOTTOM = new Color(0, 90, 170);
    public static final Color TEXT_ON_DARK = new Color(245, 245, 245);
    public static final Color TABLE_HEADER_BG = new Color(220, 220, 225);
    public static final Color TABLE_ZEBRA = new Color(248, 248, 250);

    public static final Color BTN_GREEN = new Color(46, 160, 67);
    public static final Color BTN_BLUE = new Color(0, 120, 215);
    public static final Color BTN_ORANGE = new Color(230, 126, 34);
    public static final Color BTN_RED = new Color(192, 57, 43);
    public static final Color BTN_LOGOUT = new Color(255, 140, 0);

    private ManagerPortalStyles() {}

    public static JPanel createSidebarButtonPanel(String text, Runnable onClick) {
        JPanel wrap = new JPanel(new BorderLayout());
        wrap.setOpaque(false);
        JButton b = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (getModel().isPressed()) {
                    g2.setColor(SIDEBAR_BUTTON_HOVER);
                } else if (getModel().isRollover()) {
                    g2.setColor(SIDEBAR_BUTTON_HOVER);
                } else {
                    g2.setColor(SIDEBAR_BUTTON);
                }
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        b.setForeground(TEXT_ON_DARK);
        b.setFont(new Font("SansSerif", Font.PLAIN, 14));
        b.setBorder(new EmptyBorder(12, 16, 12, 16));
        b.setContentAreaFilled(false);
        b.setBorderPainted(false);
        b.setFocusPainted(false);
        b.setHorizontalAlignment(SwingConstants.LEFT);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.addActionListener(e -> onClick.run());
        wrap.add(b, BorderLayout.CENTER);
        wrap.setBorder(new EmptyBorder(4, 10, 4, 10));
        return wrap;
    }

    public static void styleNavButtonActive(JButton b) {
        b.setForeground(TEXT_ON_DARK);
        b.setFont(new Font("SansSerif", Font.BOLD, 14));
        b.setBorder(new EmptyBorder(12, 16, 12, 16));
        b.setContentAreaFilled(false);
        b.setBorderPainted(false);
        b.setFocusPainted(false);
        b.setHorizontalAlignment(SwingConstants.LEFT);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    public static JButton createNavButtonActive(String text, Runnable onClick) {
        JButton b = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0, 0, NAV_ACTIVE_TOP, 0, getHeight(), NAV_ACTIVE_BOTTOM);
                g2.setPaint(gp);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        styleNavButtonActive(b);
        b.addActionListener(e -> onClick.run());
        return b;
    }

    public static JButton createActionButton(String text, Color bg) {
        JButton b = new JButton(text);
        b.setBackground(bg);
        b.setForeground(Color.WHITE);
        b.setFont(new Font("SansSerif", Font.BOLD, 13));
        b.setFocusPainted(false);
        b.setOpaque(true);
        b.setBorderPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setBorder(new EmptyBorder(10, 16, 10, 16));
        return b;
    }

    public static JTextField createFilterField(int columns) {
        JTextField f = new JTextField(columns);
        f.setFont(new Font("SansSerif", Font.PLAIN, 13));
        f.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                new EmptyBorder(6, 8, 6, 8)));
        return f;
    }

    public static JComboBox<String> createFilterCombo(String[] items) {
        JComboBox<String> c = new JComboBox<>(items);
        c.setFont(new Font("SansSerif", Font.PLAIN, 13));
        return c;
    }
}
