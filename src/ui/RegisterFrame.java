package ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class RegisterFrame extends JFrame {

    private JTextField fullNameField;
    private JTextField emailField;
    private JTextField contactField;
    private JPasswordField passwordField;
    private JPasswordField confirmPasswordField;
    private JButton registerButton;
    private boolean isPasswordVisible = false;
    private boolean isConfirmPasswordVisible = false;

    public RegisterFrame() {
        // basic frame setup
        setTitle("APU Automotive Service Centre - Register");
        setSize(1200, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        getContentPane().setBackground(new Color(245, 245, 245));
        setLayout(new GridBagLayout());

        GridBagConstraints frameGbc = new GridBagConstraints();
        frameGbc.gridx = 0;
        frameGbc.insets = new Insets(20, 20, 20, 20);
        frameGbc.fill = GridBagConstraints.NONE;
        frameGbc.anchor = GridBagConstraints.CENTER;

        // Heading: APU Automotive Service Centre (APU – ASC) - Now outside the white box
        JLabel headingLabel = new JLabel("APU Automotive Service Centre (APU – ASC)", SwingConstants.CENTER);
        headingLabel.setFont(new Font("SansSerif", Font.BOLD, 22));
        frameGbc.gridy = 0;
        frameGbc.insets = new Insets(0, 0, 20, 0); // Spacing below heading
        add(headingLabel, frameGbc);

        // Register Panel (the white box)
        JPanel registerPanel = new JPanel();
        registerPanel.setBackground(Color.WHITE);
        registerPanel.setLayout(new GridBagLayout());
        registerPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(230, 230, 230), 1),
                new EmptyBorder(30, 60, 30, 60)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Form Fields
        addFormField(registerPanel, "Full Name:", fullNameField = createTextField(), 1, gbc);
        addFormField(registerPanel, "Email:", emailField = createTextField(), 2, gbc);
        addFormField(registerPanel, "Contact:", contactField = createTextField(), 3, gbc);
        
        // Password
        addPasswordFormField(registerPanel, "Password:", passwordField = createPasswordField(), 4, gbc, "togglePassword");
        // Confirm Password
        addPasswordFormField(registerPanel, "Confirm Password:", confirmPasswordField = createPasswordField(), 5, gbc, "toggleConfirmPassword");

        // Register Button
        registerButton = new JButton("Register");
        registerButton.setFont(new Font("SansSerif", Font.BOLD, 16));
        registerButton.setPreferredSize(new Dimension(300, 45));
        registerButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        registerButton.addActionListener(e -> {
            service_layer.RegistrationService registrationService = new service_layer.RegistrationService();
            utils.Result<model.users.Customer> result = registrationService.registerCustomer(
                fullNameField.getText(),
                emailField.getText(),
                contactField.getText(),
                new String(passwordField.getPassword()),
                new String(confirmPasswordField.getPassword())
            );

            if (result.isSuccess()) {
                JOptionPane.showMessageDialog(this, "Registration successful! You can now login.", "Success", JOptionPane.INFORMATION_MESSAGE);
                new LoginFrame().setVisible(true);
                this.dispose();
            } else {
                JOptionPane.showMessageDialog(this, result.getError(), "Registration Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        
        gbc.gridx = 0;
        gbc.gridy = 6;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(20, 5, 10, 5);
        registerPanel.add(registerButton, gbc);

        // Back to Login link
        JButton backToLoginLink = createLinkButton("Back to Login");
        backToLoginLink.addActionListener(e -> {
            new LoginFrame().setVisible(true);
            this.dispose();
        });
        gbc.gridy = 7;
        gbc.insets = new Insets(10, 5, 5, 5);
        registerPanel.add(backToLoginLink, gbc);

        // Add the register panel to the frame
        frameGbc.gridy = 1;
        frameGbc.insets = new Insets(0, 0, 0, 0);
        add(registerPanel, frameGbc);
    }

    private void addFormField(JPanel panel, String labelText, JTextField field, int gridy, GridBagConstraints gbc) {
        JLabel label = new JLabel(labelText);
        label.setFont(new Font("SansSerif", Font.PLAIN, 14));
        gbc.gridx = 0;
        gbc.gridy = gridy;
        gbc.gridwidth = 1;
        gbc.insets = new Insets(5, 5, 5, 5);
        panel.add(label, gbc);

        gbc.gridx = 1;
        panel.add(field, gbc);
    }

    private void addPasswordFormField(JPanel panel, String labelText, JPasswordField field, int gridy, GridBagConstraints gbc, String toggleType) {
        JLabel label = new JLabel(labelText);
        label.setFont(new Font("SansSerif", Font.PLAIN, 14));
        gbc.gridx = 0;
        gbc.gridy = gridy;
        gbc.gridwidth = 1;
        gbc.insets = new Insets(5, 5, 5, 5);
        panel.add(label, gbc);

        JPanel container = new JPanel(new BorderLayout(0, 0));
        container.setBackground(Color.WHITE);
        container.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                BorderFactory.createEmptyBorder(0, 5, 0, 5)
        ));
        container.add(field, BorderLayout.CENTER);

        JButton toggleBtn = new JButton("\uD83D\uDC41");
        toggleBtn.setPreferredSize(new Dimension(50, 35));
        toggleBtn.setFont(new Font("Segoe UI Symbol", Font.PLAIN, 16));
        toggleBtn.setFocusPainted(false);
        toggleBtn.setBorderPainted(false);
        toggleBtn.setContentAreaFilled(false);
        toggleBtn.setOpaque(false);
        toggleBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        
        if (toggleType.equals("togglePassword")) {
            toggleBtn.addActionListener(e -> togglePasswordVisibility(field, toggleBtn, "isPasswordVisible"));
        } else {
            toggleBtn.addActionListener(e -> togglePasswordVisibility(field, toggleBtn, "isConfirmPasswordVisible"));
        }
        
        container.add(toggleBtn, BorderLayout.EAST);

        gbc.gridx = 1;
        panel.add(container, gbc);
    }

    private JTextField createTextField() {
        JTextField field = new JTextField(20);
        field.setPreferredSize(new Dimension(300, 35));
        field.setFont(new Font("SansSerif", Font.PLAIN, 14));
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                BorderFactory.createEmptyBorder(0, 5, 0, 5)
        ));
        return field;
    }

    private JPasswordField createPasswordField() {
        JPasswordField field = new JPasswordField();
        field.setPreferredSize(new Dimension(250, 35));
        field.setFont(new Font("SansSerif", Font.PLAIN, 14));
        field.setBorder(null);
        return field;
    }

    private JButton createLinkButton(String text) {
        JButton button = new JButton(text);
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setFocusPainted(false);
        button.setForeground(new Color(0, 123, 255));
        button.setFont(new Font("SansSerif", Font.PLAIN, 14));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return button;
    }

    private void togglePasswordVisibility(JPasswordField field, JButton button, String visibleFlag) {
        boolean isVisible;
        if (visibleFlag.equals("isPasswordVisible")) {
            isPasswordVisible = !isPasswordVisible;
            isVisible = isPasswordVisible;
        } else {
            isConfirmPasswordVisible = !isConfirmPasswordVisible;
            isVisible = isConfirmPasswordVisible;
        }

        if (isVisible) {
            field.setEchoChar((char) 0);
            button.setText("\uD83D\uDD76"); // Sunglasses
        } else {
            field.setEchoChar('*');
            button.setText("\uD83D\uDC41"); // Eye
        }
        field.requestFocus();
    }
}
