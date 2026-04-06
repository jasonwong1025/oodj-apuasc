package ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class LoginFrame extends JFrame {

    private JTextField emailField;
    private JPasswordField passwordField;
    private JButton loginButton;
    private JButton togglePasswordButton;
    private boolean isPasswordVisible = false;

    public LoginFrame() {
        // basic frame setup
        setTitle("APU Automotive Service Centre");
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

        // Login Panel (the white box)
        JPanel loginPanel = new JPanel();
        loginPanel.setBackground(Color.WHITE);
        loginPanel.setLayout(new GridBagLayout());
        loginPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(230, 230, 230), 1),
                new EmptyBorder(40, 60, 40, 60)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Email Label
        JLabel emailLabel = new JLabel("Email:");
        emailLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));
        gbc.gridx = 0;
        gbc.gridy = 0; // Starts from 0 inside the login panel
        gbc.gridwidth = 1;
        gbc.insets = new Insets(5, 5, 5, 5);
        loginPanel.add(emailLabel, gbc);

        // Email Field
        emailField = new JTextField(20);
        emailField.setPreferredSize(new Dimension(300, 35));
        emailField.setFont(new Font("SansSerif", Font.PLAIN, 14));
        emailField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                BorderFactory.createEmptyBorder(0, 5, 0, 5) // Padding to match the container
        ));
        gbc.gridx = 1;
        loginPanel.add(emailField, gbc);
        
        // Password Label
        JLabel passwordLabel = new JLabel("Password:");
        passwordLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));
        gbc.gridx = 0;
        gbc.gridy = 1;
        loginPanel.add(passwordLabel, gbc);

        // Password Container (Panel for Field + Toggle)
        JPanel passwordContainer = new JPanel(new BorderLayout(0, 0));
        passwordContainer.setBackground(Color.WHITE);
        passwordContainer.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                BorderFactory.createEmptyBorder(0, 5, 0, 5) // Padding around the field and button
        ));

        passwordField = new JPasswordField();
        passwordField.setPreferredSize(new Dimension(250, 35)); // Adjust size slightly
        passwordField.setFont(new Font("SansSerif", Font.PLAIN, 14));
        passwordField.setBorder(null); // Remove default border
        passwordContainer.add(passwordField, BorderLayout.CENTER);

        togglePasswordButton = new JButton("\uD83D\uDC41"); // Eye icon
        togglePasswordButton.setToolTipText("Show/Hide Password");
        togglePasswordButton.setPreferredSize(new Dimension(50, 35)); // Slightly wider
        togglePasswordButton.setFont(new Font("Segoe UI Symbol", Font.PLAIN, 16)); // Use font that supports symbols
        togglePasswordButton.setFocusPainted(false);
        togglePasswordButton.setBorderPainted(false);
        togglePasswordButton.setContentAreaFilled(false);
        togglePasswordButton.setOpaque(false);
        togglePasswordButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        togglePasswordButton.addActionListener(e -> togglePasswordVisibility());
        passwordContainer.add(togglePasswordButton, BorderLayout.EAST);

        gbc.gridx = 1;
        loginPanel.add(passwordContainer, gbc);

        // Login Button
        loginButton = new JButton("Login");
        loginButton.setBackground(new Color(0, 123, 255));
        loginButton.setForeground(Color.WHITE);
        loginButton.setFont(new Font("SansSerif", Font.BOLD, 16));
        loginButton.setFocusPainted(false);
        loginButton.setOpaque(true);
        loginButton.setBorderPainted(false);
        loginButton.setPreferredSize(new Dimension(300, 45)); // Match the text fields width
        loginButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        loginButton.addActionListener(e -> {
            service_layer.UserService userService = new service_layer.UserService();
            try {
                abstracts.AbstractUser user = userService.login(emailField.getText(), new String(passwordField.getPassword()));
                if ("Customer".equals(user.getRole())) {
                    new CustomerDashboard(user).setVisible(true);
                } else if ("Manager".equals(user.getRole())) {
                    new ManagerDashboard(user).setVisible(true);
                } else if ("Technician".equals(user.getRole())) {
                    new TechnicianDashboard(user).setVisible(true);
                } else if ("CounterStaff".equals(user.getRole())) {
                    new CounterStaffDashboard(user).setVisible(true);
                }
                this.dispose();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Login Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        
        gbc.gridx = 0;
        gbc.gridy = 2; // Incremented indices
        gbc.gridwidth = 2;
        gbc.insets = new Insets(20, 5, 10, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL; // Ensure it fills the width
        loginPanel.add(loginButton, gbc);

        // Register link
        JButton registerLink = createLinkButton("Register as Customer");
        registerLink.addActionListener(e -> {
            new RegisterFrame().setVisible(true);
            this.dispose();
        });
        gbc.gridy = 3;
        gbc.insets = new Insets(10, 5, 5, 5);
        loginPanel.add(registerLink, gbc);

        // Forgot password link
        JButton forgotLink = createLinkButton("Forgot Password?");
        gbc.gridy = 4;
        gbc.insets = new Insets(5, 5, 5, 5);
        loginPanel.add(forgotLink, gbc);

        // Add the login panel to the frame
        frameGbc.gridy = 1;
        frameGbc.insets = new Insets(0, 0, 0, 0);
        add(loginPanel, frameGbc);
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

    private void togglePasswordVisibility() {
        if (isPasswordVisible) {
            passwordField.setEchoChar('*');
            togglePasswordButton.setText("\uD83D\uDC41"); // Eye
        } else {
            passwordField.setEchoChar((char) 0);
            togglePasswordButton.setText("\uD83D\uDD76"); // Sunglasses
        }
        isPasswordVisible = !isPasswordVisible;
        passwordField.requestFocus();
    }
}
