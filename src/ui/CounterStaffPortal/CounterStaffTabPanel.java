package ui.CounterStaffPortal;

import abstracts.AbstractUser;
import javax.swing.JPanel;
import ui.core.Refreshable;
import ui.shared.SharedStyles;

/**
 * Base class for all panels within the Counter Staff Portal.
 */
public abstract class CounterStaffTabPanel extends JPanel implements Refreshable {
    protected final CounterStaffContext context;

    protected CounterStaffTabPanel(CounterStaffContext context) {
        this.context = context;
        setBackground(SharedStyles.MAIN_BG);
    }

    protected AbstractUser currentUser() {
        return context.currentUser();
    }
}
