package ui.CustomerPortal;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.RowFilter;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import model.vehicle.Vehicle;
import ui.SharedStyles;

public class VehiclesTabPanel extends CustomerTabPanel {
    public VehiclesTabPanel(CustomerContext context) {
        super(context);
        setLayout(new BorderLayout(0, 15));
        setBorder(new EmptyBorder(16, 20, 20, 20));
        refresh();
    }

    @Override
    public void refresh() {
        removeAll();

        String[] cols = {"Vehicle ID", "Plate Number", "Brand", "Model"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        List<Vehicle> list = vehicleService().getCustomerVehicles(currentUser().getUserId());
        for (Vehicle v : list) model.addRow(new Object[]{v.getVehicleId(), v.getPlateNumber(), v.getBrand(), v.getModel()});

        JTable table = new JTable(model);
        SharedStyles.applyTableStyle(table);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(model);
        table.setRowSorter(sorter);

        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        actions.setOpaque(false);

        JButton addBtn = SharedStyles.createActionButton("Register New Vehicle", SharedStyles.BTN_GREEN);
        addBtn.addActionListener(e -> showAddVehicleDialog());
        actions.add(addBtn);

        JButton updateBtn = SharedStyles.createActionButton("Update Selected", SharedStyles.BTN_BLUE);
        updateBtn.addActionListener(e -> {
            Vehicle selected = getSelectedVehicle(table);
            if (selected == null) {
                SharedStyles.showSelectionError(context.getOwner());
                return;
            }
            showEditVehicleDialog(selected);
        });
        actions.add(updateBtn);

        JButton deleteBtn = SharedStyles.createActionButton("Delete Selected", SharedStyles.BTN_RED);
        deleteBtn.addActionListener(e -> {
            Vehicle selected = getSelectedVehicle(table);
            if (selected == null) {
                SharedStyles.showSelectionError(context.getOwner());
                return;
            }
            deleteVehicle(selected);
        });
        actions.add(deleteBtn);

        top.add(actions, BorderLayout.WEST);

        JPanel searchBar = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        searchBar.setOpaque(false);
        searchBar.add(new JLabel("Search Plate: "));
        JTextField searchField = SharedStyles.createFilterField(15);
        searchBar.add(searchField);
        top.add(searchBar, BorderLayout.EAST);

        add(top, BorderLayout.NORTH);

        searchField.addCaretListener(e -> {
            String text = searchField.getText();
            if (text.trim().length() == 0) sorter.setRowFilter(null);
            else sorter.setRowFilter(RowFilter.regexFilter("(?i)" + text, 1));
        });

        add(new JScrollPane(table), BorderLayout.CENTER);
        revalidate();
        repaint();
    }

    private Vehicle getSelectedVehicle(JTable table) {
        int viewRow = table.getSelectedRow();
        if (viewRow < 0) return null;
        int modelRow = table.convertRowIndexToModel(viewRow);
        String vehicleId = table.getModel().getValueAt(modelRow, 0).toString();
        return vehicleService().findById(vehicleId);
    }

    private void showAddVehicleDialog() {
        JTextField plate = SharedStyles.createFilterField(20);
        JTextField brand = SharedStyles.createFilterField(20);
        JTextField modelF = SharedStyles.createFilterField(20);
        Object[] msg = {"Plate:", plate, "Brand:", brand, "Model:", modelF};
        if (javax.swing.JOptionPane.showConfirmDialog(context.getOwner(), msg, "Register Vehicle", javax.swing.JOptionPane.OK_CANCEL_OPTION) == javax.swing.JOptionPane.OK_OPTION) {
            utils.Result<Vehicle> res = vehicleService().addVehicle(currentUser().getUserId(), plate.getText(), brand.getText(), modelF.getText());
            if (!res.isSuccess()) {
                SharedStyles.showValidationError(context.getOwner(), res.getError());
            } else {
                javax.swing.JOptionPane.showMessageDialog(context.getOwner(), "Vehicle registered successfully.");
                context.getRefreshAction().run();
            }
        }
    }

    private void showEditVehicleDialog(Vehicle vehicle) {
        JTextField plate = SharedStyles.createFilterField(20);
        JTextField brand = SharedStyles.createFilterField(20);
        JTextField modelF = SharedStyles.createFilterField(20);

        plate.setText(vehicle.getPlateNumber());
        brand.setText(vehicle.getBrand());
        modelF.setText(vehicle.getModel());

        Object[] msg = {"Plate:", plate, "Brand:", brand, "Model:", modelF};
        if (javax.swing.JOptionPane.showConfirmDialog(context.getOwner(), msg, "Update Vehicle", javax.swing.JOptionPane.OK_CANCEL_OPTION) == javax.swing.JOptionPane.OK_OPTION) {
            utils.Result<Vehicle> res = vehicleService().updateVehicle(
                    currentUser().getUserId(),
                    vehicle.getVehicleId(),
                    plate.getText(),
                    brand.getText(),
                    modelF.getText());
            if (!res.isSuccess()) {
                SharedStyles.showValidationError(context.getOwner(), res.getError());
            } else {
                SharedStyles.showMessage(context.getOwner(), "Vehicle updated successfully.");
                context.getRefreshAction().run();
            }
        }
    }

    private void deleteVehicle(Vehicle vehicle) {
        if (!SharedStyles.showConfirm(context.getOwner(), "Delete selected vehicle?")) return;
        String err = vehicleService().deleteVehicleForCustomer(currentUser().getUserId(), vehicle.getVehicleId());
        if (err != null) {
            SharedStyles.showValidationError(context.getOwner(), err);
            return;
        }
        SharedStyles.showMessage(context.getOwner(), "Vehicle deleted successfully.");
        context.getRefreshAction().run();
    }
}
