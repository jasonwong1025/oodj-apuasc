package ui.shared;

import abstracts.AbstractUser;
import model.users.User;
import ui.core.PortalContext;
import utils.Result;
import javax.swing.*;
import java.awt.*;

/**
 * Consolidated Profile management panel used across all portals.
 */
public class ProfileTabPanel extends JPanel {

    private final PortalContext context;

    public ProfileTabPanel(PortalContext context) {
        this.context = context;
        setLayout(new GridBagLayout());
        refresh();
    }

    public void refresh() {
        removeAll();
        
        JPanel card = SharedStyles.createCardPanel();
        card.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.anchor = GridBagConstraints.WEST;

        AbstractUser currentUser = context.currentUser();
        User self = context.userService().findByUserId(currentUser.getUserId());
        if (self == null) return;

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
            self.setFullName(nameF.getText().trim());
            self.setEmail(emailF.getText().trim());
            self.setContact(contactF.getText().trim());

            String newPass = new String(passF.getPassword());
            Result<Void> result = context.userService().updateUser(self, newPass.length() > 0 ? newPass : null);
            if (result.isSuccess()) {
                JOptionPane.showMessageDialog(this, "Profile updated successfully!");
                context.refreshAction().run();
            } else {
                JOptionPane.showMessageDialog(this, result.getError(), "Update Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        card.add(saveBtn, gbc);
        add(card);
        
        revalidate();
        repaint();
    }
}
