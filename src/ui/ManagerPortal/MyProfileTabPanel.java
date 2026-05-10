package ui.ManagerPortal;

import abstracts.AbstractUser;
import model.users.User;
import service_layer.UserService;
import ui.core.Refreshable;
import ui.shared.SharedStyles;
import utils.ValidationUtil;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class MyProfileTabPanel extends JPanel implements Refreshable {
    private final JFrame owner;
    private final AbstractUser currentUser;
    private final UserService userService;

    public MyProfileTabPanel(JFrame owner, AbstractUser currentUser, UserService userService) {
        this.owner = owner;
        this.currentUser = currentUser;
        this.userService = userService;
        setLayout(new GridBagLayout());
        setBackground(SharedStyles.MAIN_BG);
        setBorder(new EmptyBorder(24, 24, 24, 24));
        refresh();
    }

    @Override
    public void refresh() {
        removeAll();
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.anchor = GridBagConstraints.WEST;

        JLabel h = new JLabel("My Profile");
        h.setFont(new Font("SansSerif", Font.BOLD, 22));
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        add(h, gbc);
        gbc.gridwidth = 1;

        User self = userService.findByUserId(currentUser.getUserId());
        if (self == null) {
            gbc.gridy = 1;
            add(new JLabel("Could not load profile."), gbc);
            return;
        }

        JTextField fullName = SharedStyles.createFilterField(28); fullName.setText(self.getFullName());
        JTextField email = SharedStyles.createFilterField(28); email.setText(self.getEmail());
        JTextField contact = SharedStyles.createFilterField(28); contact.setText(self.getContact());
        JPasswordField pass = new JPasswordField(28);
        pass.setBorder(fullName.getBorder());

        int y = 1;
        addProfileRow(gbc, y++, "Full Name:", fullName);
        addProfileRow(gbc, y++, "Email:", email);
        addProfileRow(gbc, y++, "Contact:", contact);
        addProfileRow(gbc, y++, "New Password (optional):", pass);

        JButton save = SharedStyles.createActionButton("Save Profile", SharedStyles.BTN_GREEN);
        gbc.gridx = 1; gbc.gridy = y; gbc.anchor = GridBagConstraints.EAST;
        save.addActionListener(e -> {
            User u = userService.findByUserId(currentUser.getUserId());
            if (u == null) return;
            u.setFullName(fullName.getText().trim());
            u.setEmail(email.getText().trim());
            u.setContact(contact.getText().trim());
            String np = new String(pass.getPassword());
            utils.Result<Void> result = userService.updateUser(u, ValidationUtil.isNotEmpty(np) ? np : null);
            if (result.isFailure()) {
                JOptionPane.showMessageDialog(owner, result.getError(), "Profile", JOptionPane.ERROR_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(owner, "Profile updated.", "Profile", JOptionPane.INFORMATION_MESSAGE);
                pass.setText("");
            }
        });
        add(save, gbc);
        revalidate();
        repaint();
    }

    private void addProfileRow(GridBagConstraints gbc, int y, String label, JComponent field) {
        gbc.gridx = 0; gbc.gridy = y; gbc.anchor = GridBagConstraints.EAST;
        add(new JLabel(label), gbc);
        gbc.gridx = 1; gbc.anchor = GridBagConstraints.WEST;
        add(field, gbc);
    }
}
