package ui;

import abstracts.AbstractUser;
import javax.swing.*;
import java.awt.*;

public class CounterStaffDashboard extends JFrame {
    public CounterStaffDashboard(AbstractUser user) {
        setTitle("Counter Staff Dashboard");
        setSize(1200, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        JLabel welcomeLabel = new JLabel("Welcome Counter Staff, " + user.getFullName(), SwingConstants.CENTER);
        welcomeLabel.setFont(new Font("SansSerif", Font.BOLD, 24));
        add(welcomeLabel, BorderLayout.CENTER);

        JButton logoutButton = new JButton("Logout");
        logoutButton.addActionListener(e -> {
            new LoginFrame().setVisible(true);
            this.dispose();
        });
        JPanel bottomPanel = new JPanel();
        bottomPanel.add(logoutButton);
        add(bottomPanel, BorderLayout.SOUTH);
    }
}
