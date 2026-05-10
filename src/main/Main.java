package main;

import ui.auth.LoginFrame;
import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        // set Look and Feel to system for better appearance
        try {
            UIManager.setLookAndFeel("javax.swing.plaf.metal.MetalLookAndFeel");
        } catch (Exception e) {
            e.printStackTrace();
        }

        SwingUtilities.invokeLater(() -> {
            new LoginFrame().setVisible(true);
        });
    }    
}
