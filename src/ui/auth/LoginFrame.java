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
        setLayout(new GridBagLayout());
        GridBagConstraints frameGbc = new GridBagConstraints();
        frameGbc.gridx = 0;
        frameGbc.insets = new Insets(20, 20, 20, 20);
        frameGbc.anchor = GridBagConstraints.CENTER;

        JLabel headingLabel = SharedStyles.createHeadingLabel("APU Automotive Service Centre (APU – ASC)");
        frameGbc.gridy = 0;
        frameGbc.insets = new Insets(0, 0, 20, 0);
        add(headingLabel, frameGbc);

        JPanel loginPanel = new JPanel(new GridBagLayout());
        loginPanel.setBackground(Color.WHITE);
        loginPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(230, 230, 230), 1),
                new EmptyBorder(40, 60, 40, 60)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Email
        JLabel emailLabel = new JLabel("Email:");
        emailLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));
        gbc.gridx = 0; gbc.gridy = 0;
        loginPanel.add(emailLabel, gbc);

        emailField = SharedStyles.createFilterField(20);
        emailField.setPreferredSize(new Dimension(300, 35));
        gbc.gridx = 1;
        loginPanel.add(emailField, gbc);

        // Password
        JLabel passwordLabel = new JLabel("Password:");
        passwordLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));
        gbc.gridx = 0; gbc.gridy = 1;
        loginPanel.add(passwordLabel, gbc);

        passwordField = SharedStyles.createPasswordField();
        JButton toggleBtn = SharedStyles.createPasswordToggleButton();
        SharedStyles.setupPasswordToggle(passwordField, toggleBtn);
        JPanel passWrap = SharedStyles.createPasswordContainer(passwordField, toggleBtn);
        gbc.gridx = 1;
        loginPanel.add(passWrap, gbc);

        // Login Button
        JButton loginButton = SharedStyles.createActionButton("Login", SharedStyles.BTN_BLUE);
        loginButton.setFont(new Font("SansSerif", Font.BOLD, 16));
        loginButton.setPreferredSize(new Dimension(300, 45));
        loginButton.addActionListener(e -> performLogin());

        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2;
        gbc.insets = new Insets(20, 5, 10, 5);
        loginPanel.add(loginButton, gbc);

        // Links
        JButton registerLink = SharedStyles.createLinkButton("Register as Customer");
        registerLink.addActionListener(e -> {
            new RegisterFrame().setVisible(true);
            this.dispose();
        });
        gbc.gridy = 3; gbc.insets = new Insets(10, 5, 5, 5);
        loginPanel.add(registerLink, gbc);

        JButton forgotLink = SharedStyles.createLinkButton("Forgot Password?");
        forgotLink.addActionListener(e -> showForgotPasswordStep1());
        gbc.gridy = 4; gbc.insets = new Insets(5, 5, 5, 5);
        loginPanel.add(forgotLink, gbc);

        frameGbc.gridy = 1;
        frameGbc.insets = new Insets(0, 0, 0, 0);
        add(loginPanel, frameGbc);
    }

    private void performLogin() {
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
            SharedStyles.showError(this, result.getError());
        }
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
        JTextField emailInput = SharedStyles.createFilterField(20);
        emailInput.setPreferredSize(new Dimension(0, 32));
        panel.add(emailInput, gbc);

        gbc.gridy++;
        gbc.insets = new Insets(16, 4, 4, 4);
        JButton sendBtn = SharedStyles.createActionButton("Send OTP", SharedStyles.BTN_BLUE);
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
                    ex.printStackTrace();
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
        dialog.setSize(420, 300);
        dialog.setLocationRelativeTo(this);

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
        JTextField otpInput = SharedStyles.createFilterField(20);
        otpInput.setFont(new Font("SansSerif", Font.BOLD, 18));
        otpInput.setHorizontalAlignment(JTextField.CENTER);
        otpInput.setPreferredSize(new Dimension(0, 36));
        panel.add(otpInput, gbc);

        gbc.gridy++;
        gbc.insets = new Insets(16, 4, 4, 4);
        JButton verifyBtn = SharedStyles.createActionButton("Verify OTP", SharedStyles.BTN_BLUE);
        panel.add(verifyBtn, gbc);

        gbc.gridy++;
        JButton backBtn = SharedStyles.createLinkButton("<- Request a new OTP");
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
        dialog.setSize(440, 360);
        dialog.setLocationRelativeTo(this);

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
        JPasswordField newPassField = SharedStyles.createPasswordField();
        newPassField.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)));
        newPassField.setPreferredSize(new Dimension(0, 32));
        panel.add(newPassField, gbc);

        gbc.gridy++;
        gbc.insets = new Insets(10, 4, 4, 4);
        JLabel confirmPassLbl = new JLabel("Confirm New Password:");
        confirmPassLbl.setFont(new Font("SansSerif", Font.BOLD, 13));
        panel.add(confirmPassLbl, gbc);

        gbc.gridy++;
        gbc.insets = new Insets(4, 4, 4, 4);
        JPasswordField confirmPassField = SharedStyles.createPasswordField();
        confirmPassField.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)));
        confirmPassField.setPreferredSize(new Dimension(0, 32));
        panel.add(confirmPassField, gbc);

        gbc.gridy++;
        gbc.insets = new Insets(18, 4, 4, 4);
        JButton resetBtn = SharedStyles.createActionButton("Reset Password", SharedStyles.BTN_GREEN);
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
