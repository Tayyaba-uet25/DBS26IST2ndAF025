package hms.ui;

import hms.dao.MedicineDao;
import hms.model.Medicine;
import hms.service.MedicineService;
import hms.util.ValidationException;
import hms.util.ValidationUtil;

import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import java.awt.Color;
import java.sql.SQLException;
import java.util.List;

/** Medicines module with an extra "Dispense" action (uses stored procedure). */
public class MedicinePanel extends AbstractCrudPanel<Medicine> {

    private final MedicineDao dao = new MedicineDao();
    private final MedicineService service = new MedicineService();

    public MedicinePanel() {
        super("Medicines (Pharmacy)");
        refresh();
    }

    @Override protected String[] columns() {
        return new String[]{"ID", "Name", "Manufacturer", "Unit Price", "Stock", "Reorder", "Expiry"};
    }

    @Override protected List<Medicine> fetch(String search) throws SQLException {
        return dao.findAll();
    }

    @Override protected Object[] rowOf(Medicine m) {
        return new Object[]{
                m.getMedicineId(), m.getName(),
                m.getManufacturer() == null ? "" : m.getManufacturer(),
                m.getUnitPrice(), m.getStockQty(), m.getReorderLevel(),
                m.getExpiryDate() == null ? "" : m.getExpiryDate().toString()
        };
    }

    @Override protected void openForm(Medicine item) {
        new MedicineDialog((java.awt.Frame) SwingUtilities.getWindowAncestor(this), item).setVisible(true);
    }

    @Override protected void delete(Medicine item) throws SQLException {
        dao.delete(item.getMedicineId());
    }

    @Override protected void addExtraButtons(JPanel toolbar) {
        var btn = makeButton("Dispense", new Color(230, 126, 34));
        btn.addActionListener(e -> dispense());
        toolbar.add(btn);
    }

    private void dispense() {
        int viewRow = table.getSelectedRow();
        if (viewRow < 0) {
            UiUtils.error(this, "Please select a medicine row first.");
            return;
        }
        Medicine m = data.get(table.convertRowIndexToModel(viewRow));
        String input = JOptionPane.showInputDialog(this,
                "Dispense quantity of '" + m.getName() + "' (current stock: " + m.getStockQty() + "):",
                "Dispense Medicine", JOptionPane.QUESTION_MESSAGE);
        if (input == null) return;
        try {
            int qty = ValidationUtil.positiveInt(input, "Quantity");
            service.dispense(m.getMedicineId(), qty);   // stored procedure validates stock
            UiUtils.info(this, "Dispensed " + qty + " unit(s) of " + m.getName() + ".", "Success");
            refresh();
        } catch (ValidationException ve) {
            UiUtils.error(this, ve.getMessage());
        } catch (SQLException ex) {
            UiUtils.error(this, "Dispense failed:\n" + ex.getMessage());
        }
    }
}
