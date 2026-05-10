package ui.auth;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import service_layer.PasswordResetService;
import utils.EmailService;
import ui.CounterStaffDashboard;
import ui.CustomerDashboard;
import ui.ManagerDashboard;
import ui.TechnicianDashboard;

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

        // Heading: APU Automotive Service Centre (APU - ASC) - Now outside the white box
        JLabel headingLabel = new JLabel("APU Automotive Service Centre (APU \u2013 ASC)", SwingConstants.CENTER);
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
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        gbc.insets = new Insets(5, 5, 5, 5);
        loginPanel.add(emailLabel, gbc);

        // Email Field
        emailField = new JTextField(20);
        emailField.setPreferredSize(new Dimension(300, 35));
        emailField.setFont(new Font("SansSerif", Font.PLAIN, 14));
        emailField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                BorderFactory.createEmptyBorder(0, 5, 0, 5)
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
                BorderFactory.createEmptyBorder(0, 5, 0, 5)
        ));

        passwordField = new JPasswordField();
        passwordField.setPreferredSize(new Dimension(250, 35));
        passwordField.setFont(new Font("SansSerif", Font.PLAIN, 14));
        passwordField.setBorder(null);
        passwordContainer.add(passwordField, BorderLayout.CENTER);

        togglePasswordButton = new JButton("\uD83D\uDC41");
        togglePasswordButton.setToolTipText("Show/Hide Password");
        togglePasswordButton.setPreferredSize(new Dimension(50, 35));
        togglePasswordButton.setFont(new Font("Segoe UI Symbol", Font.PLAIN, 16));
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
        loginButton.setFont(new Font("SansSerif", Font.BOLD, 16));
        loginButton.setPreferredSize(new Dimension(300, 45));
        loginButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        loginButton.addActionListener(e -> {
            service_layer.UserService userService = new service_layer.UserService();
            utils.Result<abstracts.AbstractUser> result = userService.login(emailField.getText(), new String(passwordField.getPassword()));
            
            if (result.isSuccess()) {
                abstracts.AbstractUser user = result.getValue();
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
            } else {
                JOptionPane.showMessageDialog(this, result.getError(), "Login Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(20, 5, 10, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
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

        // Forgot password link - wired to the multi-step flow
        JButton forgotLink = createLinkButton("Forgot Password?");
        forgotLink.addActionListener(e -> showForgotPasswordStep1());
        gbc.gridy = 4;
        gbc.insets = new Insets(5, 5, 5, 5);
        loginPanel.add(forgotLink, gbc);

        // Add the login panel to the frame
        frameGbc.gridy = 1;
        frameGbc.insets = new Insets(0, 0, 0, 0);
        add(loginPanel, frameGbc);
    }

    private void showForgotPasswordStep1() {
        JDialog dialog = new JDialog(this, "Forgot Password - Step 1 of 3", true);
        dialog.setSize(420, 260);
        dialog.setLocationRelativeTo(this);
        dialog.setResizable(false);

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(24, 30, 24, 30));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0; gbc.gridy = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        gbc.insets = new Insets(4, 4, 4, 4);

        JLabel title = new JLabel("Forgot Password");
        title.setFont(new Font("SansSerif", Font.BOLD, 18));
        panel.add(title, gbc);

        gbc.gridy++;
        JLabel sub = new JLabel("Enter your registered email address to receive an OTP.");
        sub.setFont(new Font("SansSerif", Font.PLAIN, 12));
        sub.setForeground(new Color(100, 100, 100));
        panel.add(sub, gbc);

        gbc.gridy++;
        gbc.insets = new Insets(12, 4, 4, 4);
        JLabel emailLbl = new JLabel("Email Address:");
        emailLbl.setFont(new Font("SansSerif", Font.BOLD, 13));
        panel.add(emailLbl, gbc);

        gbc.gridy++;
        gbc.insets = new Insets(4, 4, 4, 4);
        JTextField emailInput = new JTextField();
        emailInput.setFont(new Font("SansSerif", Font.PLAIN, 13));
        emailInput.setPreferredSize(new Dimension(0, 32));
        panel.add(emailInput, gbc);

        gbc.gridy++;
        gbc.insets = new Insets(16, 4, 4, 4);
        JButton sendBtn = new JButton("Send OTP");
        sendBtn.setFont(new Font("SansSerif", Font.BOLD, 13));
        sendBtn.setBackground(new Color(0, 123, 255));
        sendBtn.setForeground(Color.WHITE);
        sendBtn.setFocusPainted(false);
        sendBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        panel.add(sendBtn, gbc);

        sendBtn.addActionListener(e -> {
            String email = emailInput.getText().trim();
            if (email.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Please enter your email address.", "Validation", JOptionPane.WARNING_MESSAGE);
                return;
            }
            PasswordResetService resetService = new PasswordResetService();
            model.users.User user = resetService.findUserByEmail(email);
            if (user == null) {
                JOptionPane.showMessageDialog(dialog, "No account found with that email address.", "Not Found", JOptionPane.ERROR_MESSAGE);
                return;
            }

            String otp = resetService.generateOtp(email);

            sendBtn.setEnabled(false);
            sendBtn.setText("Sending...");

            new Thread(() -> {
                boolean mailSent = false;
                try {
                    EmailService.sendOtpEmail(email, otp);
                    mailSent = true;
                } catch (Exception ex) {
                    ex.printStackTrace();
                }

                final boolean sent = mailSent;
                SwingUtilities.invokeLater(() -> {
                    sendBtn.setEnabled(true);
                    sendBtn.setText("Send OTP");
                    dialog.dispose();

                    if (!sent) {
                        JOptionPane.showMessageDialog(null,
                            "Email service is not configured.\n\nYour OTP (for testing purposes): " + otp,
                            "OTP (Dev Mode)", JOptionPane.INFORMATION_MESSAGE);
                    } else {
                        JOptionPane.showMessageDialog(null,
                            "An OTP has been sent to " + email + ".\nIt is valid for 10 minutes.",
                            "OTP Sent", JOptionPane.INFORMATION_MESSAGE);
                    }
                    showForgotPasswordStep2(email, resetService);
                });
            }).start();
        });

        dialog.setContentPane(panel);
        dialog.setVisible(true);
    }

    private void showForgotPasswordStep2(String email, PasswordResetService resetService) {
        JDialog dialog = new JDialog(this, "Forgot Password - Step 2 of 3", true);
        dialog.setSize(420, 300);
        dialog.setLocationRelativeTo(this);
        dialog.setResizable(false);

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(24, 30, 24, 30));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0; gbc.gridy = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        gbc.insets = new Insets(4, 4, 4, 4);

        JLabel title = new JLabel("Verify OTP");
        title.setFont(new Font("SansSerif", Font.BOLD, 18));
        panel.add(title, gbc);

        gbc.gridy++;
        JLabel sub = new JLabel("<html>Enter the 6-digit OTP sent to <b>" + email + "</b>.</html>");
        sub.setFont(new Font("SansSerif", Font.PLAIN, 12));
        sub.setForeground(new Color(100, 100, 100));
        panel.add(sub, gbc);

        gbc.gridy++;
        gbc.insets = new Insets(14, 4, 4, 4);
        JLabel otpLbl = new JLabel("One-Time Password (OTP):");
        otpLbl.setFont(new Font("SansSerif", Font.BOLD, 13));
        panel.add(otpLbl, gbc);

        gbc.gridy++;
        gbc.insets = new Insets(4, 4, 4, 4);
        JTextField otpInput = new JTextField();
        otpInput.setFont(new Font("SansSerif", Font.BOLD, 18));
        otpInput.setHorizontalAlignment(JTextField.CENTER);
        otpInput.setPreferredSize(new Dimension(0, 36));
        panel.add(otpInput, gbc);

        gbc.gridy++;
        gbc.insets = new Insets(16, 4, 4, 4);
        JButton verifyBtn = new JButton("Verify OTP");
        verifyBtn.setFont(new Font("SansSerif", Font.BOLD, 13));
        verifyBtn.setBackground(new Color(0, 123, 255));
        verifyBtn.setForeground(Color.WHITE);
        verifyBtn.setFocusPainted(false);
        verifyBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        panel.add(verifyBtn, gbc);

        gbc.gridy++;
        gbc.insets = new Insets(4, 4, 4, 4);
        JButton backBtn = createLinkButton("<- Request a new OTP");
        panel.add(backBtn, gbc);

        verifyBtn.addActionListener(e -> {
            String enteredOtp = otpInput.getText().trim();
            String error = resetService.validateOtp(email, enteredOtp);
            if (error != null) {
                JOptionPane.showMessageDialog(dialog, error, "Invalid OTP", JOptionPane.ERROR_MESSAGE);
                return;
            }
            dialog.dispose();
            showForgotPasswordStep3(email, resetService);
        });

        backBtn.addActionListener(e -> {
            dialog.dispose();
            showForgotPasswordStep1();
        });

        dialog.setContentPane(panel);
        dialog.setVisible(true);
    }

    private void showForgotPasswordStep3(String email, PasswordResetService resetService) {
        JDialog dialog = new JDialog(this, "Forgot Password - Step 3 of 3", true);
        dialog.setSize(440, 360);
        dialog.setLocationRelativeTo(this);
        dialog.setResizable(false);

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(24, 30, 24, 30));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0; gbc.gridy = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        gbc.insets = new Insets(4, 4, 4, 4);

        JLabel title = new JLabel("Set New Password");
        title.setFont(new Font("SansSerif", Font.BOLD, 18));
        panel.add(title, gbc);

        gbc.gridy++;
        JLabel req = new JLabel("<html><font color='#777777'>Min. 6 characters \u2014 uppercase, lowercase, number and special character.</font></html>");
        req.setFont(new Font("SansSerif", Font.PLAIN, 11));
        panel.add(req, gbc);

        gbc.gridy++;
        gbc.insets = new Insets(12, 4, 4, 4);
        JLabel newPassLbl = new JLabel("New Password:");
        newPassLbl.setFont(new Font("SansSerif", Font.BOLD, 13));
        panel.add(newPassLbl, gbc);

        gbc.gridy++;
        gbc.insets = new Insets(4, 4, 4, 4);
        JPasswordField newPassField = new JPasswordField();
        newPassField.setFont(new Font("SansSerif", Font.PLAIN, 13));
        newPassField.setPreferredSize(new Dimension(0, 32));
        panel.add(newPassField, gbc);

        gbc.gridy++;
        gbc.insets = new Insets(10, 4, 4, 4);
        JLabel confirmPassLbl = new JLabel("Confirm New Password:");
        confirmPassLbl.setFont(new Font("SansSerif", Font.BOLD, 13));
        panel.add(confirmPassLbl, gbc);

        gbc.gridy++;
        gbc.insets = new Insets(4, 4, 4, 4);
        JPasswordField confirmPassField = new JPasswordField();
        confirmPassField.setFont(new Font("SansSerif", Font.PLAIN, 13));
        confirmPassField.setPreferredSize(new Dimension(0, 32));
        panel.add(confirmPassField, gbc);

        gbc.gridy++;
        gbc.insets = new Insets(18, 4, 4, 4);
        JButton resetBtn = new JButton("Reset Password");
        resetBtn.setFont(new Font("SansSerif", Font.BOLD, 13));
        resetBtn.setBackground(new Color(40, 167, 69));
        resetBtn.setForeground(Color.WHITE);
        resetBtn.setFocusPainted(false);
        resetBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        panel.add(resetBtn, gbc);

        resetBtn.addActionListener(e -> {
            String newPass = new String(newPassField.getPassword());
            String confirmPass = new String(confirmPassField.getPassword());

            if (newPass.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Please enter a new password.", "Validation", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (!newPass.equals(confirmPass)) {
                JOptionPane.showMessageDialog(dialog, "Passwords do not match. Please try again.", "Mismatch", JOptionPane.ERROR_MESSAGE);
                return;
            }

            String error = resetService.resetPassword(email, newPass);
            if (error != null) {
                JOptionPane.showMessageDialog(dialog, error, "Reset Failed", JOptionPane.ERROR_MESSAGE);
                return;
            }

            dialog.dispose();
            JOptionPane.showMessageDialog(this,
                "Your password has been reset successfully!\nYou can now log in with your new password.",
                "Password Reset Successful", JOptionPane.INFORMATION_MESSAGE);
        });

        dialog.setContentPane(panel);
        dialog.setVisible(true);
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
            togglePasswordButton.setText("\uD83D\uDC41");
        } else {
            passwordField.setEchoChar((char) 0);
            togglePasswordButton.setText("\uD83D\uDD76");
        }
        isPasswordVisible = !isPasswordVisible;
        passwordField.requestFocus();
    }
}
