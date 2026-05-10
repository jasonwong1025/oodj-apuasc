package ui.TechnicianPortal;

import abstracts.AbstractUser;
import javax.swing.JPanel;
import ui.Refreshable;
import ui.SharedStyles;

/**
 * Base class for all panels within the Technician Portal.
 */
public abstract class TechnicianTabPanel extends JPanel implements Refreshable {
    protected final TechnicianContext context;

    protected TechnicianTabPanel(TechnicianContext context) {
        this.context = context;
        setBackground(SharedStyles.MAIN_BG);
    }

    protected AbstractUser currentUser() {
        return context.currentUser();
    }
}
