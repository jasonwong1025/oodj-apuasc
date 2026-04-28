package ui.ManagerPortal;

import ui.Refreshable;
import ui.SharedStyles;

import javax.swing.*;
import java.awt.*;

public class PlaceholderTabPanel extends JPanel implements Refreshable {
    private final String title;
    private final String body;

    public PlaceholderTabPanel(String title, String body) {
        this.title = title;
        this.body = body;
        setLayout(new GridBagLayout());
        setBackground(SharedStyles.MAIN_BG);
        refresh();
    }

    @Override
    public void refresh() {
        removeAll();
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(24, 24, 8, 24);
        JLabel h = new JLabel(title);
        h.setFont(new Font("SansSerif", Font.BOLD, 22));
        add(h, gbc);
        gbc.gridy = 1;
        gbc.insets = new Insets(0, 24, 24, 24);
        JLabel t = new JLabel("<html><div style='width:520px'>" + body + "</div></html>");
        t.setFont(new Font("SansSerif", Font.PLAIN, 14));
        add(t, gbc);
        revalidate();
        repaint();
    }
}
