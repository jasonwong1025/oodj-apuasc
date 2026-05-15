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
import ui.core.BaseFrame;
import ui.shared.SharedStyles;

public class LoginFrame extends BaseFrame {

    private JTextField emailField;
    private JPasswordField passwordField;

    public LoginFrame() {
        super("APU Automotive Service Centre");
        init();
    }

    @Override
    protected void initContent() {
        setSize(960, 620);
        setMinimumSize(new Dimension(820, 560));
        setLocationRelativeTo(null);

        JPanel root = AuthUiKit.createRootPanel();
        root.add(AuthUiKit.createBrandPanel(
                "Automotive\nService Centre",
                "Book services, track appointments, and manage your vehicle care in one place."), BorderLayout.WEST);

        JPanel formShell = AuthUiKit.createFormShell(
                "Welcome back",
                "Sign in to continue to your dashboard.");
        JPanel authCard = AuthUiKit.extractFormCard(formShell);
        JPanel form = AuthUiKit.getCardContent(authCard);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        emailField = AuthUiKit.createResponsiveTextField(20);
        AuthUiKit.addFormRow(form, gbc, 0, "Email address", emailField);

        passwordField = AuthUiKit.createResponsivePasswordField();
        JButton toggleBtn = new JButton();
        AuthUiKit.setupPasswordToggle(passwordField, toggleBtn);
        JPanel passwordRow = AuthUiKit.createResponsivePasswordRow(passwordField, toggleBtn);
        AuthUiKit.addFormRow(form, gbc, 2, "Password", passwordRow);

        java.awt.event.KeyAdapter enterKeyHandler = new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent e) {
                if (e.getKeyCode() == java.awt.event.KeyEvent.VK_ENTER) {
                    performLogin();
                }
            }
        };
        emailField.addKeyListener(enterKeyHandler);
        passwordField.addKeyListener(enterKeyHandler);

        JButton loginButton = AuthUiKit.createPrimaryButton("Sign in");
        loginButton.addActionListener(e -> performLogin());
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 1;
        gbc.weightx = 1.0;
        gbc.weighty = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.insets = new Insets(24, 0, 12, 0);
        form.add(loginButton, gbc);

        JPanel links = new JPanel(new FlowLayout(FlowLayout.CENTER, 18, 0));
        links.setOpaque(false);
        JButton registerLink = AuthUiKit.createSecondaryLink("Create an account");
        registerLink.addActionListener(e -> {
            new RegisterFrame().setVisible(true);
            dispose();
        });
        JButton forgotLink = AuthUiKit.createSecondaryLink("Forgot password?");
        forgotLink.addActionListener(e -> showForgotPasswordStep1());
        links.add(registerLink);
        links.add(forgotLink);

        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.gridwidth = 1;
        gbc.weightx = 1.0;
        gbc.weighty = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.insets = new Insets(0, 0, 0, 0);
        form.add(links, gbc);

        root.add(formShell, BorderLayout.CENTER);
        add(root, BorderLayout.CENTER);
    }

    private void performLogin() {
        service_layer.UserService userService = new service_layer.UserService();
        utils.Result<abstracts.AbstractUser> result = userService.login(emailField.getText(), new String(passwordField.getPassword()));

        if (result.isSuccess()) {
            abstracts.AbstractUser user = result.getValue();
            if (user.getRole() == model.users.Role.CUSTOMER) {
                new CustomerDashboard(user).setVisible(true);
            } else if (user.getRole() == model.users.Role.MANAGER) {
                new ManagerDashboard(user).setVisible(true);
            } else if (user.getRole() == model.users.Role.TECHNICIAN) {
                new TechnicianDashboard(user).setVisible(true);
            } else if (user.getRole() == model.users.Role.COUNTERSTAFF) {
                new CounterStaffDashboard(user).setVisible(true);
            }
            dispose();
        } else {
            SharedStyles.showError(this, result.getError());
        }
    }

    private JPanel buildDialogPanel(String title, String subtitle) {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(28, 32, 28, 32));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        gbc.insets = new Insets(4, 4, 4, 4);

        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(new Font("SansSerif", Font.BOLD, 20));
        titleLbl.setForeground(AuthUiKit.TEXT_PRIMARY);
        panel.add(titleLbl, gbc);

        gbc.gridy++;
        JLabel subLbl = new JLabel(subtitle);
        subLbl.setFont(new Font("SansSerif", Font.PLAIN, 13));
        subLbl.setForeground(AuthUiKit.TEXT_MUTED);
        panel.add(subLbl, gbc);

        return panel;
    }

    private void showForgotPasswordStep1() {
        JDialog dialog = new JDialog(this, "Forgot Password - Step 1 of 3", true);
        dialog.setSize(440, 280);
        dialog.setLocationRelativeTo(this);
        dialog.setResizable(false);

        JPanel panel = buildDialogPanel("Forgot password", "Enter your registered email to receive a one-time code.");

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        gbc.insets = new Insets(4, 4, 4, 4);

        gbc.gridy = 2;
        gbc.insets = new Insets(16, 4, 4, 4);
        panel.add(AuthUiKit.createFieldLabel("Email address"), gbc);

        gbc.gridy++;
        gbc.insets = new Insets(4, 4, 4, 4);
        JTextField emailInput = AuthUiKit.createTextField(20);
        panel.add(emailInput, gbc);

        gbc.gridy++;
        gbc.insets = new Insets(20, 4, 4, 4);
        JButton sendBtn = AuthUiKit.createPrimaryButton("Send OTP");
        panel.add(sendBtn, gbc);

        sendBtn.addActionListener(e -> {
            String email = emailInput.getText().trim();
            if (email.isEmpty()) {
                SharedStyles.showWarning(dialog, "Please enter your email address.");
                return;
            }
            PasswordResetService resetService = new PasswordResetService();
            model.users.User user = resetService.findUserByEmail(email);
            if (user == null) {
                SharedStyles.showError(dialog, "No account found with that email address.");
                return;
            }

            String otp = resetService.generateOtp(email);
            sendBtn.setEnabled(false);
            sendBtn.setText("Sending...");

            new Thread(() -> {
                boolean success = false;
                String finalOtp = otp;
                try {
                    EmailService.sendOtpEmail(email, finalOtp);
                    success = true;
                } catch (Exception ex) {
                    utils.Logger.error("General", "I/O Error", ex);
                }

                final boolean mailSent = success;
                SwingUtilities.invokeLater(() -> {
                    sendBtn.setEnabled(true);
                    sendBtn.setText("Send OTP");
                    dialog.dispose();

                    if (!mailSent) {
                        SharedStyles.showMessage(null, "Email service not configured. OTP: " + finalOtp);
                    } else {
                        SharedStyles.showMessage(null, "OTP sent to " + email);
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
        dialog.setSize(440, 320);
        dialog.setLocationRelativeTo(this);

        JPanel panel = buildDialogPanel("Verify OTP",
                "<html>Enter the 6-digit code sent to <b>" + email + "</b>.</html>");

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        gbc.insets = new Insets(4, 4, 4, 4);

        gbc.gridy = 2;
        gbc.insets = new Insets(16, 4, 4, 4);
        panel.add(AuthUiKit.createFieldLabel("One-time password"), gbc);

        gbc.gridy++;
        JTextField otpInput = AuthUiKit.createTextField(20);
        otpInput.setFont(new Font("SansSerif", Font.BOLD, 18));
        otpInput.setHorizontalAlignment(JTextField.CENTER);
        panel.add(otpInput, gbc);

        gbc.gridy++;
        gbc.insets = new Insets(20, 4, 4, 4);
        JButton verifyBtn = AuthUiKit.createPrimaryButton("Verify OTP");
        panel.add(verifyBtn, gbc);

        gbc.gridy++;
        JButton backBtn = AuthUiKit.createSecondaryLink("Request a new code");
        panel.add(backBtn, gbc);

        verifyBtn.addActionListener(e -> {
            String enteredOtp = otpInput.getText().trim();
            String error = resetService.validateOtp(email, enteredOtp);
            if (error != null) {
                SharedStyles.showError(dialog, error);
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
        dialog.setSize(460, 380);
        dialog.setLocationRelativeTo(this);

        JPanel panel = buildDialogPanel("Set new password",
                "Use at least 6 characters with upper, lower, number and special character.");

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        gbc.insets = new Insets(4, 4, 4, 4);

        gbc.gridy = 2;
        gbc.insets = new Insets(16, 4, 4, 4);
        panel.add(AuthUiKit.createFieldLabel("New password"), gbc);

        gbc.gridy++;
        JPasswordField newPassField = AuthUiKit.createPasswordField();
        panel.add(newPassField, gbc);

        gbc.gridy++;
        gbc.insets = new Insets(12, 4, 4, 4);
        panel.add(AuthUiKit.createFieldLabel("Confirm password"), gbc);

        gbc.gridy++;
        gbc.insets = new Insets(4, 4, 4, 4);
        JPasswordField confirmPassField = AuthUiKit.createPasswordField();
        panel.add(confirmPassField, gbc);

        gbc.gridy++;
        gbc.insets = new Insets(20, 4, 4, 4);
        JButton resetBtn = AuthUiKit.createPrimaryButton("Reset password");
        panel.add(resetBtn, gbc);

        resetBtn.addActionListener(e -> {
            String newPass = new String(newPassField.getPassword());
            String confirmPass = new String(confirmPassField.getPassword());

            if (newPass.isEmpty()) {
                SharedStyles.showWarning(dialog, "Please enter a new password.");
                return;
            }
            if (!newPass.equals(confirmPass)) {
                SharedStyles.showError(dialog, "Passwords do not match.");
                return;
            }

            String error = resetService.resetPassword(email, newPass);
            if (error != null) {
                SharedStyles.showError(dialog, error);
                return;
            }

            dialog.dispose();
            SharedStyles.showMessage(this, "Password reset successfully! Please log in.");
        });

        dialog.setContentPane(panel);
        dialog.setVisible(true);
    }
}
