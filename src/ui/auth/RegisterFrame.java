package ui.auth;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
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

        JPanel registerPanel = new JPanel(new GridBagLayout());
        registerPanel.setBackground(Color.WHITE);
        registerPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(230, 230, 230), 1),
                new EmptyBorder(30, 60, 30, 60)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Form Fields
        addFormField(registerPanel, "Full Name:", fullNameField = SharedStyles.createFilterField(20), 1, gbc);
        addFormField(registerPanel, "Email:", emailField = SharedStyles.createFilterField(20), 2, gbc);
        addFormField(registerPanel, "Contact:", contactField = SharedStyles.createFilterField(20), 3, gbc);
        
        // Password fields
        passwordField = SharedStyles.createPasswordField();
        JButton togglePass = SharedStyles.createPasswordToggleButton();
        SharedStyles.setupPasswordToggle(passwordField, togglePass);
        addPasswordFormField(registerPanel, "Password:", passwordField, togglePass, 4, gbc);

        confirmPasswordField = SharedStyles.createPasswordField();
        JButton toggleConfirm = SharedStyles.createPasswordToggleButton();
        SharedStyles.setupPasswordToggle(confirmPasswordField, toggleConfirm);
        addPasswordFormField(registerPanel, "Confirm Password:", confirmPasswordField, toggleConfirm, 5, gbc);

        // Register Button
        JButton registerButton = SharedStyles.createActionButton("Register", SharedStyles.BTN_BLUE);
        registerButton.setFont(new Font("SansSerif", Font.BOLD, 16));
        registerButton.setPreferredSize(new Dimension(300, 45));
        registerButton.addActionListener(e -> performRegistration());
        
        gbc.gridx = 0; gbc.gridy = 6; gbc.gridwidth = 2;
        gbc.insets = new Insets(20, 5, 10, 5);
        registerPanel.add(registerButton, gbc);

        // Back link
        JButton backToLoginLink = SharedStyles.createLinkButton("Back to Login");
        backToLoginLink.addActionListener(e -> {
            new LoginFrame().setVisible(true);
            this.dispose();
        });
        gbc.gridy = 7; gbc.insets = new Insets(10, 5, 5, 5);
        registerPanel.add(backToLoginLink, gbc);

        frameGbc.gridy = 1;
        frameGbc.insets = new Insets(0, 0, 0, 0);
        add(registerPanel, frameGbc);
    }

    private void addFormField(JPanel panel, String labelText, JTextField field, int gridy, GridBagConstraints gbc) {
        JLabel label = new JLabel(labelText);
        label.setFont(new Font("SansSerif", Font.PLAIN, 14));
        gbc.gridx = 0; gbc.gridy = gridy; gbc.gridwidth = 1;
        gbc.insets = new Insets(5, 5, 5, 5);
        panel.add(label, gbc);

        field.setPreferredSize(new Dimension(300, 35));
        gbc.gridx = 1;
        panel.add(field, gbc);
    }

    private void addPasswordFormField(JPanel panel, String labelText, JPasswordField field, JButton toggleBtn, int gridy, GridBagConstraints gbc) {
        JLabel label = new JLabel(labelText);
        label.setFont(new Font("SansSerif", Font.PLAIN, 14));
        gbc.gridx = 0; gbc.gridy = gridy; gbc.gridwidth = 1;
        gbc.insets = new Insets(5, 5, 5, 5);
        panel.add(label, gbc);

        JPanel container = SharedStyles.createPasswordContainer(field, toggleBtn);
        gbc.gridx = 1;
        panel.add(container, gbc);
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
            this.dispose();
        } else {
            SharedStyles.showError(this, result.getError());
        }
    }
}
