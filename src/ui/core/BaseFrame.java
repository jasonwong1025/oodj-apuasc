package ui.core;

import javax.swing.*;
import ui.shared.SharedStyles;
import java.awt.BorderLayout;

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
        setLayout(new BorderLayout());
    }

    /**
     * Subclasses must call this at the end of their constructor
     * or after initializing their local fields.
     */
    public void init() {
        initContent();
        revalidate();
        repaint();
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
