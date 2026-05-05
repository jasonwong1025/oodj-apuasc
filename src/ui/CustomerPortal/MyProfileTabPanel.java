package ui.CustomerPortal;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.JButton;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import model.users.User;
import ui.SharedStyles;
import utils.ValidationUtil;

public class MyProfileTabPanel extends CustomerTabPanel {
    public MyProfileTabPanel(CustomerContext context) {
        super(context);
        setLayout(new GridBagLayout());
        setBorder(new javax.swing.border.EmptyBorder(16, 20, 20, 20));
        refresh();
    }

    @Override
    public void refresh() {
        removeAll();

        javax.swing.JPanel card = SharedStyles.createCardPanel();
        card.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.anchor = GridBagConstraints.WEST;

        User self = userService().findByUserId(currentUser().getUserId());
        if (self == null) {
            card.add(new javax.swing.JLabel("Could not load profile."), gbc);
            add(card);
            revalidate();
            repaint();
            return;
        }

        int y = 0;
        JTextField nameF = SharedStyles.createFilterField(25); nameF.setText(self.getFullName());
        JTextField emailF = SharedStyles.createFilterField(25); emailF.setText(self.getEmail());
        JTextField contactF = SharedStyles.createFilterField(25); contactF.setText(self.getContact());
        JPasswordField passF = new JPasswordField(25); passF.setBorder(nameF.getBorder());

        SharedStyles.addFormRow(card, gbc, y++, "Full Name:", nameF);
        SharedStyles.addFormRow(card, gbc, y++, "Email:", emailF);
        SharedStyles.addFormRow(card, gbc, y++, "Contact:", contactF);
        SharedStyles.addFormRow(card, gbc, y++, "Password:", passF);

        JButton saveBtn = SharedStyles.createActionButton("Update Profile", SharedStyles.BTN_GREEN);
        gbc.gridx = 1; gbc.gridy = y; gbc.anchor = GridBagConstraints.EAST;
        saveBtn.addActionListener(e -> {
            if (!ValidationUtil.isNotEmpty(nameF.getText()) || !ValidationUtil.isValidEmail(emailF.getText())) {
                javax.swing.JOptionPane.showMessageDialog(context.getOwner(), "Please enter valid details.");
                return;
            }
            self.setFullName(nameF.getText().trim());
            self.setEmail(emailF.getText().trim());
            self.setContact(contactF.getText().trim());
            String newPass = new String(passF.getPassword());
            if (newPass.length() > 0) {
                if (!ValidationUtil.isValidPassword(newPass)) {
                    javax.swing.JOptionPane.showMessageDialog(context.getOwner(), ValidationUtil.passwordRequirementsMessage());
                    return;
                }
                self.setPassword(newPass);
            }
            userService().updateUser(self, currentUser().getUserId());
            javax.swing.JOptionPane.showMessageDialog(context.getOwner(), "Profile updated successfully!");
            context.getRefreshAction().run();
        });
        card.add(saveBtn, gbc);

        add(card);
        revalidate();
        repaint();
    }
}
