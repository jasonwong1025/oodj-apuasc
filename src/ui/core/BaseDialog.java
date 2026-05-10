package ui.core;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Base dialog class for standardized modal popups.
 */
public abstract class BaseDialog extends JDialog {

    public BaseDialog(Window owner, String title) {
        super(owner, title, ModalityType.APPLICATION_MODAL);
        getContentPane().setBackground(Color.WHITE);
        
        // Basic layout setup
        setLayout(new BorderLayout());
    }

    /**
     * Standard method to finalize and show the dialog centered.
     */
    public void showCentered() {
        pack();
        setLocationRelativeTo(getOwner());
        setVisible(true);
    }
    
    protected JPanel createContentPanel() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(Color.WHITE);
        p.setBorder(new EmptyBorder(24, 30, 24, 30));
        return p;
    }
}
