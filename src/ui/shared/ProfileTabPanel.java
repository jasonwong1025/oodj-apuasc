package ui.shared;

import model.users.Role;
import model.users.User;
import ui.auth.AuthUiKit;
import ui.core.PortalContext;
import utils.Result;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Profile management panel shared across all portals.
 */
public class ProfileTabPanel extends JPanel {

    private final PortalContext context;
    private JTextField nameField;
    private JTextField emailField;
    private JTextField contactField;
    private JPasswordField passwordField;
    private JPasswordField confirmPasswordField;
    private User loadedUser;

    public ProfileTabPanel(PortalContext context) {
        this.context = context;
        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(24, 32, 32, 32));
        setBackground(SharedStyles.MAIN_BG);
        refresh();
    }

    public void refresh() {
        removeAll();
        loadedUser = context.userService().findByUserId(context.currentUser().getUserId());
        if (loadedUser == null) {
            revalidate();
            repaint();
            return;
        }

        add(buildHeader(), BorderLayout.NORTH);

        JPanel body = new JPanel(new GridBagLayout());
        body.setOpaque(false);
        GridBagConstraints bgbc = new GridBagConstraints();
        bgbc.gridx = 0;
        bgbc.weightx = 1.0;
        bgbc.fill = GridBagConstraints.HORIZONTAL;
        bgbc.anchor = GridBagConstraints.NORTHWEST;
        bgbc.insets = new Insets(0, 0, 20, 0);

        int row = 0;
        bgbc.gridy = row++;
        body.add(buildSummaryCard(loadedUser), bgbc);

        bgbc.gridy = row++;
        body.add(buildDetailsCard(), bgbc);

        bgbc.gridy = row++;
        body.add(buildSecurityCard(), bgbc);

        bgbc.gridy = row;
        bgbc.insets = new Insets(4, 0, 0, 0);
        body.add(buildActions(), bgbc);

        JScrollPane scroll = new JScrollPane(body);
        scroll.setBorder(null);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.getViewport().setBackground(SharedStyles.MAIN_BG);
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        add(scroll, BorderLayout.CENTER);

        revalidate();
        repaint();
    }

    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setBorder(new EmptyBorder(0, 0, 20, 0));

        JLabel title = new JLabel("My Profile");
        title.setFont(new Font("SansSerif", Font.BOLD, 28));
        title.setForeground(AuthUiKit.TEXT_PRIMARY);

        JTextArea subtitle = AuthUiKit.createWrappingText(
                "View your account details and update your personal information.",
                new Font("SansSerif", Font.PLAIN, 14),
                AuthUiKit.TEXT_MUTED);

        JPanel text = new JPanel(new BorderLayout(0, 6));
        text.setOpaque(false);
        text.add(title, BorderLayout.NORTH);
        text.add(subtitle, BorderLayout.CENTER);
        header.add(text, BorderLayout.CENTER);
        return header;
    }

    private JPanel buildSummaryCard(User user) {
        JPanel section = createSection("Account overview");
        JPanel card = sectionCard(section);
        card.setLayout(new GridBagLayout());

        JLabel avatar = new JLabel(getInitials(user.getFullName()), SwingConstants.CENTER) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(SharedStyles.BTN_BLUE);
                g2.fillOval(0, 0, getWidth(), getHeight());
                g2.dispose();
                super.paintComponent(g);
            }
        };
        avatar.setPreferredSize(new Dimension(64, 64));
        avatar.setMinimumSize(new Dimension(64, 64));
        avatar.setFont(new Font("SansSerif", Font.BOLD, 20));
        avatar.setForeground(Color.WHITE);
        avatar.setOpaque(false);

        JPanel meta = new JPanel(new GridBagLayout());
        meta.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(0, 0, 6, 0);

        JLabel nameLabel = new JLabel(user.getFullName());
        nameLabel.setFont(new Font("SansSerif", Font.BOLD, 20));
        nameLabel.setForeground(AuthUiKit.TEXT_PRIMARY);
        meta.add(nameLabel, gbc);

        gbc.gridy = 1;
        gbc.insets = new Insets(0, 0, 8, 0);
        meta.add(createBadge(user.getRole()), gbc);

        int metaRow = 2;
        if (user.getRole() == Role.TECHNICIAN && user.getTechnicianServiceType() != null
                && !user.getTechnicianServiceType().isBlank() && !"-".equals(user.getTechnicianServiceType())) {
            gbc.gridy = metaRow++;
            gbc.insets = new Insets(0, 0, 4, 0);
            JLabel specialty = new JLabel("Specialty: " + user.getTechnicianServiceType());
            specialty.setFont(new Font("SansSerif", Font.PLAIN, 13));
            specialty.setForeground(AuthUiKit.TEXT_MUTED);
            meta.add(specialty, gbc);
        }

        gbc.gridy = metaRow;
        gbc.insets = new Insets(0, 0, 0, 0);
        JLabel status = new JLabel(user.isActive() ? "Status: Active" : "Status: Inactive");
        status.setFont(new Font("SansSerif", Font.PLAIN, 13));
        status.setForeground(user.isActive() ? new Color(34, 139, 72) : new Color(180, 60, 50));
        meta.add(status, gbc);

        GridBagConstraints cardGbc = new GridBagConstraints();
        cardGbc.gridx = 0;
        cardGbc.gridy = 0;
        cardGbc.insets = new Insets(0, 0, 0, 16);
        cardGbc.anchor = GridBagConstraints.NORTHWEST;
        card.add(avatar, cardGbc);

        cardGbc.gridx = 1;
        cardGbc.weightx = 1.0;
        cardGbc.fill = GridBagConstraints.HORIZONTAL;
        cardGbc.insets = new Insets(0, 0, 0, 0);
        card.add(meta, cardGbc);
        return section;
    }

    private JPanel buildDetailsCard() {
        JPanel section = createSection("Personal information");
        JPanel card = sectionCard(section);
        card.setLayout(new GridBagLayout());

        nameField = AuthUiKit.createResponsiveTextField(20);
        nameField.setText(loadedUser.getFullName());
        emailField = AuthUiKit.createResponsiveTextField(20);
        emailField.setText(loadedUser.getEmail());
        contactField = AuthUiKit.createResponsiveTextField(20);
        contactField.setText(loadedUser.getContact());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        AuthUiKit.addFormRow(card, gbc, 0, "Full name", nameField);
        AuthUiKit.addFormRow(card, gbc, 2, "Email address", emailField);
        AuthUiKit.addFormRow(card, gbc, 4, "Contact number", contactField);
        return section;
    }

    private JPanel buildSecurityCard() {
        JPanel section = createSection("Security");
        JPanel card = sectionCard(section);
        card.setLayout(new GridBagLayout());

        passwordField = AuthUiKit.createResponsivePasswordField();
        confirmPasswordField = AuthUiKit.createResponsivePasswordField();
        JButton togglePass = new JButton();
        JButton toggleConfirm = new JButton();
        AuthUiKit.setupPasswordToggle(passwordField, togglePass);
        AuthUiKit.setupPasswordToggle(confirmPasswordField, toggleConfirm);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        AuthUiKit.addFormRow(card, gbc, 0, "New password",
                AuthUiKit.createResponsivePasswordRow(passwordField, togglePass));
        AuthUiKit.addFormRow(card, gbc, 2, "Confirm new password",
                AuthUiKit.createResponsivePasswordRow(confirmPasswordField, toggleConfirm));

        gbc.gridy = 4;
        gbc.insets = new Insets(4, 0, 0, 0);
        JTextArea hint = AuthUiKit.createWrappingText(
                "Leave blank to keep your current password. New passwords must meet the system requirements.",
                new Font("SansSerif", Font.PLAIN, 12),
                AuthUiKit.TEXT_MUTED);
        card.add(hint, gbc);
        return section;
    }

    private JPanel buildActions() {
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        actions.setOpaque(false);

        JButton saveBtn = SharedStyles.createActionButton("Save changes", SharedStyles.BTN_GREEN);
        saveBtn.setPreferredSize(new Dimension(150, 40));
        saveBtn.addActionListener(e -> saveProfile());

        JButton resetBtn = SharedStyles.createActionButton("Reset", SharedStyles.BTN_BLUE);
        resetBtn.setPreferredSize(new Dimension(110, 40));
        resetBtn.addActionListener(e -> refresh());

        actions.add(saveBtn);
        actions.add(resetBtn);
        return actions;
    }

    private JPanel createSection(String title) {
        JPanel section = new JPanel(new BorderLayout(0, 12));
        section.setOpaque(false);

        JLabel heading = new JLabel(title);
        heading.setFont(new Font("SansSerif", Font.BOLD, 16));
        heading.setForeground(AuthUiKit.TEXT_PRIMARY);
        section.add(heading, BorderLayout.NORTH);

        JPanel card = SharedStyles.createCardPanel();
        section.add(card, BorderLayout.CENTER);
        return section;
    }

    private static JPanel sectionCard(JPanel section) {
        return (JPanel) section.getComponent(1);
    }

    private void saveProfile() {
        if (loadedUser == null) {
            return;
        }

        loadedUser.setFullName(nameField.getText().trim());
        loadedUser.setEmail(emailField.getText().trim());
        loadedUser.setContact(contactField.getText().trim());

        String newPass = new String(passwordField.getPassword());
        String confirmPass = new String(confirmPasswordField.getPassword());

        if (!newPass.isEmpty() || !confirmPass.isEmpty()) {
            if (!newPass.equals(confirmPass)) {
                SharedStyles.showError(this, "Passwords do not match.");
                return;
            }
        }

        Result<Void> result = context.userService().updateUser(
                loadedUser,
                newPass.isEmpty() ? null : newPass);

        if (result.isSuccess()) {
            SharedStyles.showMessage(this, "Profile updated successfully.");
            context.refreshAction().run();
            refresh();
        } else {
            SharedStyles.showError(this, result.getError());
        }
    }

    private static JLabel createBadge(Role role) {
        Color bg;
        Color fg = new Color(30, 40, 55);
        switch (role) {
            case MANAGER -> bg = new Color(220, 232, 255);
            case TECHNICIAN -> bg = new Color(255, 243, 220);
            case COUNTERSTAFF -> bg = new Color(232, 245, 233);
            default -> bg = new Color(236, 238, 242);
        }
        JLabel badge = new JLabel(role.getLabel());
        badge.setFont(new Font("SansSerif", Font.BOLD, 12));
        badge.setForeground(fg);
        badge.setBackground(bg);
        badge.setOpaque(true);
        badge.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(bg.darker(), 1, true),
                new EmptyBorder(4, 12, 4, 12)));
        return badge;
    }

    private static String getInitials(String fullName) {
        if (fullName == null || fullName.isBlank()) {
            return "?";
        }
        String[] parts = fullName.trim().split("\\s+");
        if (parts.length == 1) {
            return parts[0].substring(0, 1).toUpperCase();
        }
        return (parts[0].substring(0, 1) + parts[parts.length - 1].substring(0, 1)).toUpperCase();
    }
}
