package ui.auth;

import javax.swing.*;
import java.awt.*;
import ui.core.BaseFrame;
import ui.shared.SharedStyles;

public class RegisterFrame extends BaseFrame {

    private JTextField fullNameField;
    private JTextField emailField;
    private JTextField contactField;
    private JPasswordField passwordField;
    private JPasswordField confirmPasswordField;

    public RegisterFrame() {
        super("APU Automotive Service Centre - Register");
        init();
    }

    @Override
    protected void initContent() {
        setSize(960, 680);
        setMinimumSize(new Dimension(820, 600));
        setLocationRelativeTo(null);

        JPanel root = AuthUiKit.createRootPanel();
        root.add(AuthUiKit.createBrandPanel(
                "Join\nAPU ASC",
                "Create your customer account to book services, manage vehicles, and leave reviews.",
                "Already have staff access? Sign in from the login page."), BorderLayout.WEST);

        JPanel formShell = AuthUiKit.createFormShell(
                "Create account",
                "Register as a customer in a few quick steps.");
        JPanel authCard = AuthUiKit.extractFormCard(formShell);
        JPanel form = AuthUiKit.getCardContent(authCard);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        fullNameField = AuthUiKit.createTextField(20);
        AuthUiKit.addFormRow(form, gbc, 0, "Full name", fullNameField);

        emailField = AuthUiKit.createTextField(20);
        AuthUiKit.addFormRow(form, gbc, 2, "Email address", emailField);

        contactField = AuthUiKit.createTextField(20);
        AuthUiKit.addFormRow(form, gbc, 4, "Contact number", contactField);

        passwordField = AuthUiKit.createPasswordField();
        JButton togglePass = new JButton();
        AuthUiKit.setupPasswordToggle(passwordField, togglePass);
        JPanel passwordRow = AuthUiKit.createPasswordRow(passwordField, togglePass);
        AuthUiKit.addFormRow(form, gbc, 6, "Password", passwordRow);

        confirmPasswordField = AuthUiKit.createPasswordField();
        JButton toggleConfirm = new JButton();
        AuthUiKit.setupPasswordToggle(confirmPasswordField, toggleConfirm);
        JPanel confirmRow = AuthUiKit.createPasswordRow(confirmPasswordField, toggleConfirm);
        AuthUiKit.addFormRow(form, gbc, 8, "Confirm password", confirmRow);

        java.awt.event.KeyAdapter enterKeyHandler = new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent e) {
                if (e.getKeyCode() == java.awt.event.KeyEvent.VK_ENTER) {
                    performRegistration();
                }
            }
        };
        fullNameField.addKeyListener(enterKeyHandler);
        emailField.addKeyListener(enterKeyHandler);
        contactField.addKeyListener(enterKeyHandler);
        passwordField.addKeyListener(enterKeyHandler);
        confirmPasswordField.addKeyListener(enterKeyHandler);

        JButton registerButton = AuthUiKit.createPrimaryButton("Create account");
        registerButton.addActionListener(e -> performRegistration());
        gbc.gridx = 0;
        gbc.gridy = 10;
        gbc.gridwidth = 1;
        gbc.weightx = 1.0;
        gbc.weighty = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.insets = new Insets(22, 0, 12, 0);
        form.add(registerButton, gbc);

        JButton backToLoginLink = AuthUiKit.createSecondaryLink("Back to sign in");
        backToLoginLink.addActionListener(e -> {
            new LoginFrame().setVisible(true);
            dispose();
        });
        JPanel linkRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        linkRow.setOpaque(false);
        linkRow.add(backToLoginLink);
        gbc.gridx = 0;
        gbc.gridy = 11;
        gbc.gridwidth = 1;
        gbc.weightx = 1.0;
        gbc.weighty = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.insets = new Insets(0, 0, 0, 0);
        form.add(linkRow, gbc);

        root.add(formShell, BorderLayout.CENTER);
        add(root, BorderLayout.CENTER);
    }

    private void performRegistration() {
        service_layer.RegistrationService registrationService = new service_layer.RegistrationService();
        utils.Result<model.users.Customer> result = registrationService.registerCustomer(
            fullNameField.getText(),
            emailField.getText(),
            contactField.getText(),
            new String(passwordField.getPassword()),
            new String(confirmPasswordField.getPassword())
        );

        if (result.isSuccess()) {
            SharedStyles.showMessage(this, "Registration successful! You can now login.");
            new LoginFrame().setVisible(true);
            dispose();
        } else {
            SharedStyles.showError(this, result.getError());
        }
    }
}
