package main;

import ui.LoginFrame;
import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        // set Look and Feel to system for better appearance
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        SwingUtilities.invokeLater(() -> {
            new LoginFrame().setVisible(true);
        });
    }    
}
