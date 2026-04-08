package ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public final class CustomerPortalStyles {
    public static final Color MAIN_BG = new Color(245, 247, 250);
    public static final Color SIDEBAR_BG = new Color(44, 62, 80);
    public static final Color SIDEBAR_BUTTON = new Color(52, 73, 94);
    public static final Color SIDEBAR_BUTTON_HOVER = new Color(58, 83, 107);
    public static final Color ACCENT_COLOR = new Color(52, 152, 219);
    public static final Color TEXT_ON_DARK = Color.WHITE;
    public static final Color CARD_BG = Color.WHITE;

    private CustomerPortalStyles() {}

    public static JPanel createSidebarButton(String text, Runnable onClick) {
        JButton b = new JButton(text);
        b.setFont(new Font("SansSerif", Font.BOLD, 14));
        b.setForeground(TEXT_ON_DARK);
        b.setBackground(SIDEBAR_BUTTON);
        b.setFocusPainted(false);
        b.setBorderPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setHorizontalAlignment(SwingConstants.LEFT);
        b.setBorder(new EmptyBorder(15, 25, 15, 25));
        
        b.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                b.setBackground(SIDEBAR_BUTTON_HOVER);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                b.setBackground(SIDEBAR_BUTTON);
            }
        });
        
        b.addActionListener(e -> onClick.run());
        
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        p.add(b, BorderLayout.CENTER);
        return p;
    }

    public static JButton createPrimaryButton(String text) {
        JButton b = new JButton(text);
        b.setBackground(ACCENT_COLOR);
        b.setForeground(Color.WHITE);
        b.setFont(new Font("SansSerif", Font.BOLD, 13));
        b.setFocusPainted(false);
        b.setBorderPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setBorder(new EmptyBorder(10, 20, 10, 20));
        return b;
    }

    public static void styleCard(JPanel p) {
        p.setBackground(CARD_BG);
        p.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(230, 230, 230)),
            new EmptyBorder(20, 20, 20, 20)
        ));
    }
}
