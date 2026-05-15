package ui.auth;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import ui.shared.SharedStyles;

/**
 * Shared visual components for login and registration screens (Swing/AWT only).
 */
public final class AuthUiKit {

    public static final Color BRAND_DARK = SharedStyles.SIDEBAR_BG;
    public static final Color BRAND_ACCENT = SharedStyles.NAV_ACTIVE_TOP;
    public static final Color BRAND_ACCENT_DARK = SharedStyles.NAV_ACTIVE_BOTTOM;
    public static final Color FORM_BG = new Color(248, 249, 252);
    public static final Color CARD_BG = Color.WHITE;
    public static final Color TEXT_PRIMARY = new Color(28, 32, 40);
    public static final Color TEXT_MUTED = new Color(110, 118, 130);
    public static final Color FIELD_BORDER = new Color(210, 216, 226);
    public static final Color FIELD_FOCUS = SharedStyles.BTN_BLUE;
    private static final Dimension FIELD_SIZE = new Dimension(320, 42);
    private static final Dimension PASSWORD_TOGGLE_SIZE = new Dimension(68, 42);

    private AuthUiKit() {}

    public static JPanel createRootPanel() {
        JPanel root = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(
                        0, 0, new Color(235, 240, 248),
                        getWidth(), getHeight(), new Color(220, 228, 242));
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
            }
        };
        root.setOpaque(false);
        return root;
    }

    public static JPanel createBrandPanel(String title, String subtitle) {
        return createBrandPanel(title, subtitle, null);
    }

    public static JPanel createBrandPanel(String title, String subtitle, String footer) {
        JPanel panel = new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(
                        0, 0, BRAND_DARK,
                        getWidth(), getHeight(), new Color(22, 28, 38));
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());

                g2.setColor(new Color(255, 255, 255, 18));
                g2.fillOval(-80, -80, 260, 260);
                g2.fillOval(getWidth() - 180, getHeight() - 200, 320, 320);
                g2.setColor(new Color(BRAND_ACCENT.getRed(), BRAND_ACCENT.getGreen(), BRAND_ACCENT.getBlue(), 40));
                g2.fillOval(getWidth() / 2 - 60, getHeight() / 3, 180, 180);
                g2.dispose();
            }
        };
        panel.setOpaque(false);
        panel.setPreferredSize(new Dimension(340, 0));
        panel.setMinimumSize(new Dimension(220, 0));
        panel.setBorder(new EmptyBorder(48, 44, 48, 44));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.NORTHWEST;

        JLabel badge = new JLabel("APU ASC");
        badge.setFont(new Font("SansSerif", Font.BOLD, 13));
        badge.setForeground(new Color(180, 210, 255));
        badge.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(255, 255, 255, 40), 1, true),
                new EmptyBorder(6, 14, 6, 14)));
        gbc.gridy = 0;
        gbc.insets = new Insets(0, 0, 28, 0);
        panel.add(badge, gbc);

        JLabel titleLabel = new JLabel("<html>" + title.replace("\n", "<br>") + "</html>");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 30));
        titleLabel.setForeground(Color.WHITE);
        gbc.gridy = 1;
        gbc.insets = new Insets(0, 0, 16, 0);
        panel.add(titleLabel, gbc);

        JTextArea subtitleArea = createWrappingTextArea(subtitle, new Font("SansSerif", Font.PLAIN, 14), new Color(210, 218, 230));
        gbc.gridy = 2;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(subtitleArea, gbc);

        if (footer != null && !footer.isBlank()) {
            JTextArea footerArea = createWrappingTextArea(footer, new Font("SansSerif", Font.PLAIN, 12), new Color(160, 170, 185));
            gbc.gridy = 3;
            gbc.weighty = 0;
            gbc.anchor = GridBagConstraints.SOUTHWEST;
            gbc.insets = new Insets(24, 0, 0, 0);
            panel.add(footerArea, gbc);
        }

        return panel;
    }

    public static JPanel createFormShell(String heading, String description) {
        JPanel shell = new JPanel(new GridBagLayout());
        shell.setOpaque(false);
        shell.setBorder(new EmptyBorder(40, 48, 40, 56));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.insets = new Insets(0, 0, 8, 0);

        JLabel headingLabel = new JLabel(heading);
        headingLabel.setFont(new Font("SansSerif", Font.BOLD, 26));
        headingLabel.setForeground(TEXT_PRIMARY);
        shell.add(headingLabel, gbc);

        gbc.gridy = 1;
        gbc.insets = new Insets(0, 0, 24, 0);
        JTextArea descArea = createWrappingTextArea(description, new Font("SansSerif", Font.PLAIN, 14), TEXT_MUTED);
        shell.add(descArea, gbc);

        JPanel card = createCardPanel();
        gbc.gridy = 2;
        gbc.weighty = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        shell.add(card, gbc);

        return shell;
    }

    public static JPanel createCardPanel() {
        JPanel card = new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(0, 0, 0, 28));
                g2.fillRoundRect(6, 8, getWidth() - 12, getHeight() - 8, 24, 24);
                g2.setColor(CARD_BG);
                g2.fillRoundRect(0, 0, getWidth() - 8, getHeight() - 8, 20, 20);
                g2.dispose();
            }
        };
        card.setOpaque(false);
        return card;
    }

    public static JPanel getCardContent(JPanel card) {
        JPanel content = new JPanel(new GridBagLayout());
        content.setOpaque(false);
        content.setBorder(new EmptyBorder(28, 32, 28, 32));
        GridBagConstraints cardGbc = new GridBagConstraints();
        cardGbc.gridx = 0;
        cardGbc.gridy = 0;
        cardGbc.weightx = 1.0;
        cardGbc.weighty = 0;
        cardGbc.fill = GridBagConstraints.HORIZONTAL;
        cardGbc.anchor = GridBagConstraints.NORTHWEST;
        card.add(content, cardGbc);
        return content;
    }

    public static JLabel createFieldLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("SansSerif", Font.BOLD, 13));
        label.setForeground(TEXT_PRIMARY);
        return label;
    }

    public static JPanel extractFormCard(JPanel formShell) {
        return (JPanel) formShell.getComponent(2);
    }

    public static JTextField createResponsiveTextField(int columns) {
        JTextField field = createTextField(columns);
        field.setPreferredSize(FIELD_SIZE);
        field.setMinimumSize(new Dimension(160, 42));
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        return field;
    }

    public static JPasswordField createResponsivePasswordField() {
        JPasswordField field = createPasswordField();
        field.setPreferredSize(FIELD_SIZE);
        field.setMinimumSize(new Dimension(160, 42));
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        return field;
    }

    public static JPanel createResponsivePasswordRow(JPasswordField field, JButton toggleBtn) {
        JPanel row = createPasswordRow(field, toggleBtn);
        row.setPreferredSize(FIELD_SIZE);
        row.setMinimumSize(new Dimension(220, 42));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        return row;
    }

    public static JTextArea createWrappingText(String text, Font font, Color color) {
        return createWrappingTextArea(text, font, color);
    }

    public static JTextField createTextField(int columns) {
        JTextField field = new JTextField(columns) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        styleField(field);
        return field;
    }

    public static JPasswordField createPasswordField() {
        JPasswordField field = new JPasswordField() {
            @Override
            protected void paintComponent(Graphics g) {
                if (!Boolean.TRUE.equals(getClientProperty("auth.passwordRowField"))) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(getBackground());
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                    g2.dispose();
                }
                super.paintComponent(g);
            }
        };
        styleField(field);
        return field;
    }

    private static void styleField(JTextField field) {
        field.setFont(new Font("SansSerif", Font.PLAIN, 14));
        field.setBackground(new Color(250, 251, 253));
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(FIELD_BORDER, 1, true),
                new EmptyBorder(10, 14, 10, 14)));
        field.setPreferredSize(FIELD_SIZE);
        field.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                field.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(FIELD_FOCUS, 2, true),
                        new EmptyBorder(9, 13, 9, 13)));
            }

            @Override
            public void focusLost(FocusEvent e) {
                field.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(FIELD_BORDER, 1, true),
                        new EmptyBorder(10, 14, 10, 14)));
            }
        });
    }

    public static JPanel createPasswordRow(JPasswordField field, JButton toggleBtn) {
        field.putClientProperty("auth.passwordRowField", Boolean.TRUE);
        field.setOpaque(false);
        for (java.awt.event.FocusListener listener : field.getFocusListeners()) {
            field.removeFocusListener(listener);
        }
        field.setBorder(new EmptyBorder(0, 0, 0, 0));
        field.setBackground(new Color(0, 0, 0, 0));
        field.setPreferredSize(new Dimension(0, 24));
        field.setMinimumSize(new Dimension(60, 24));

        toggleBtn.setPreferredSize(PASSWORD_TOGGLE_SIZE);
        toggleBtn.setMinimumSize(PASSWORD_TOGGLE_SIZE);
        toggleBtn.setFont(new Font("SansSerif", Font.BOLD, 12));
        toggleBtn.setForeground(BRAND_ACCENT);
        toggleBtn.setFocusPainted(false);
        toggleBtn.setBorderPainted(false);
        toggleBtn.setContentAreaFilled(false);
        toggleBtn.setOpaque(false);
        toggleBtn.setMargin(new Insets(0, 8, 0, 8));
        toggleBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        toggleBtn.setBorder(new EmptyBorder(0, 6, 0, 6));

        JPanel row = new JPanel(new BorderLayout(0, 0)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(250, 251, 253));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        row.setOpaque(false);
        row.setPreferredSize(FIELD_SIZE);
        row.setMinimumSize(new Dimension(220, 42));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        row.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(FIELD_BORDER, 1, true),
                new EmptyBorder(9, 13, 9, 4)));
        row.add(field, BorderLayout.CENTER);
        row.add(toggleBtn, BorderLayout.EAST);

        field.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                row.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(FIELD_FOCUS, 2, true),
                        new EmptyBorder(8, 12, 8, 3)));
                row.revalidate();
                row.repaint();
            }

            @Override
            public void focusLost(FocusEvent e) {
                row.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(FIELD_BORDER, 1, true),
                        new EmptyBorder(9, 13, 9, 4)));
                row.revalidate();
                row.repaint();
            }
        });

        return row;
    }

    public static JButton createPrimaryButton(String text) {
        Color base = SharedStyles.BTN_BLUE;
        Color hover = BRAND_ACCENT_DARK;
        JButton button = new JButton(text);
        button.setFont(new Font("SansSerif", Font.BOLD, 15));
        button.setForeground(Color.WHITE);
        button.setBackground(base);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setOpaque(true);
        button.setContentAreaFilled(true);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setPreferredSize(new Dimension(320, 46));
        button.setBorder(new EmptyBorder(10, 16, 10, 16));
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(hover);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(base);
            }
        });
        return button;
    }

    public static JButton createSecondaryLink(String text) {
        JButton link = new JButton(text);
        link.setFont(new Font("SansSerif", Font.PLAIN, 13));
        link.setForeground(BRAND_ACCENT);
        link.setBackground(new Color(0, 0, 0, 0));
        link.setBorderPainted(false);
        link.setContentAreaFilled(false);
        link.setFocusPainted(false);
        link.setOpaque(false);
        link.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        link.setBorder(new EmptyBorder(4, 8, 4, 8));
        return link;
    }

    public static void addFormRow(JPanel form, GridBagConstraints gbc, int row, String label, JComponent field) {
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 1;
        gbc.weightx = 1.0;
        gbc.weighty = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(row == 0 ? 0 : 14, 0, 4, 0);
        form.add(createFieldLabel(label), gbc);

        gbc.gridy = row + 1;
        gbc.insets = new Insets(0, 0, 0, 0);
        form.add(field, gbc);
    }

    public static void setupPasswordToggle(JPasswordField field, JButton button) {
        final char hiddenEchoChar = field.getEchoChar() == (char) 0 ? '\u2022' : field.getEchoChar();
        button.addActionListener(e -> {
            if (field.getEchoChar() == (char) 0) {
                field.setEchoChar(hiddenEchoChar);
                button.setText("Show");
            } else {
                field.setEchoChar((char) 0);
                button.setText("Hide");
            }
            field.requestFocus();
        });
        field.setEchoChar(hiddenEchoChar);
        button.setText("Show");
        button.setToolTipText("Show or hide password");
    }

    private static JTextArea createWrappingTextArea(String text, Font font, Color color) {
        JTextArea area = new JTextArea(text);
        area.setFont(font);
        area.setForeground(color);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setOpaque(false);
        area.setEditable(false);
        area.setFocusable(false);
        area.setBorder(null);
        area.setRows(1);
        area.addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentResized(java.awt.event.ComponentEvent e) {
                updateWrappingTextAreaSize((JTextArea) e.getComponent());
            }
        });
        area.addHierarchyListener(e -> {
            if ((e.getChangeFlags() & java.awt.event.HierarchyEvent.SHOWING_CHANGED) != 0 && area.isShowing()) {
                updateWrappingTextAreaSize(area);
            }
        });
        return area;
    }

    private static void updateWrappingTextAreaSize(JTextArea ta) {
        int width = ta.getWidth();
        if (width <= 0) {
            Container parent = ta.getParent();
            if (parent != null && parent.getWidth() > 0) {
                width = parent.getWidth();
            } else {
                return;
            }
        }
        JTextArea measure = new JTextArea(ta.getText());
        measure.setFont(ta.getFont());
        measure.setLineWrap(true);
        measure.setWrapStyleWord(true);
        measure.setSize(Math.max(width, 1), 1);
        int height = measure.getPreferredSize().height;
        ta.setPreferredSize(new Dimension(width, height));
        ta.setMaximumSize(new Dimension(Integer.MAX_VALUE, height));
        ta.revalidate();
    }
}
