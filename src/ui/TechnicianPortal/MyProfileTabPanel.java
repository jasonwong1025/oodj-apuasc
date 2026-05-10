package ui.TechnicianPortal;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import model.users.User;
import ui.SharedStyles;
import utils.Result;

public class MyProfileTabPanel extends TechnicianTabPanel {

    public MyProfileTabPanel(TechnicianContext context) {
        super(context);
        setLayout(new BorderLayout());
        refresh();
    }

    @Override
    public void refresh() {
        removeAll();
        
        JPanel root = new JPanel(new GridBagLayout());
        root.setBackground(SharedStyles.MAIN_BG);
        root.setBorder(new EmptyBorder(40, 40, 40, 40));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridy = 0;

        User user = context.userService().findByUserId(currentUser().getUserId());
        if (user == null) return;

        addLabel(root, "Full Name:", gbc, 1);
        JTextField fullNameField = createField(user.getFullName(), true, gbc, 1);

        addLabel(root, "Email:", gbc, 2);
        JTextField emailField = createField(user.getEmail(), true, gbc, 1);

        addLabel(root, "Contact:", gbc, 3);
        JTextField contactField = createField(user.getContact(), true, gbc, 1);

        gbc.gridy = 5;
        gbc.gridx = 1;
        gbc.gridwidth = 1;
        gbc.insets = new Insets(30, 10, 10, 10);
        JButton saveBtn = SharedStyles.createActionButton("Update Profile", SharedStyles.BTN_BLUE);
        root.add(saveBtn, gbc);

        saveBtn.addActionListener(e -> {
            user.setFullName(fullNameField.getText());
            user.setEmail(emailField.getText());
            user.setContact(contactField.getText());
            
            Result<Void> res = context.userService().updateUser(user);
            if (res.isSuccess()) {
                SharedStyles.showMessage(context.owner(), "Profile updated successfully!");
                context.refreshAction().run();
            } else {
                SharedStyles.showValidationError(context.owner(), res.getError());
            }
        });

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.add(root, BorderLayout.NORTH);
        add(new JScrollPane(wrapper), BorderLayout.CENTER);
        
        revalidate();
        repaint();
    }

    private void addLabel(JPanel p, String text, GridBagConstraints gbc, int row) {
        gbc.gridy = row;
        gbc.gridx = 0;
        gbc.gridwidth = 1;
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("SansSerif", Font.BOLD, 14));
        p.add(lbl, gbc);
    }

    private JTextField createField(String text, boolean editable, GridBagConstraints gbc, int col) {
        gbc.gridx = col;
        JTextField f = SharedStyles.createFilterField(25);
        f.setText(text);
        f.setEditable(editable);
        if (!editable) f.setBackground(new Color(240, 240, 240));
        return f;
    }
}
