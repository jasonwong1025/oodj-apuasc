package ui.core;

import javax.swing.*;
import ui.shared.SharedStyles;

/**
 * Base frame class that centralizes window configuration.
 */
public abstract class BaseFrame extends JFrame {

    public BaseFrame(String title) {
        setTitle(title);
        setSize(1200, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        getContentPane().setBackground(SharedStyles.MAIN_BG);
        
        // Initialize specific content
        initContent();
    }

    /**
     * Subclasses must implement this to build their specific UI.
     */
    protected abstract void initContent();

    /**
     * Refresh the frame content if needed.
     */
    public void refresh() {
        revalidate();
        repaint();
    }
}
