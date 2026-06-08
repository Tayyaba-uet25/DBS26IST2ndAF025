package hms.ui;

import com.toedter.calendar.JDateChooser;
import hms.dao.MedicineDao;
import hms.model.Medicine;
import hms.util.AppLogger;
import hms.util.ValidationException;
import hms.util.ValidationUtil;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

/** Add/Edit form for a medicine. */
public class MedicineDialog extends JDialog {

    private final MedicineDao dao = new MedicineDao();
    private final Medicine medicine;
    private final JTextField name = new JTextField(16);
    private final JTextField manufacturer = new JTextField(16);
    private final JTextField unitPrice = new JTextField(16);
    private final JTextField stock = new JTextField(16);
    private final JTextField reorder = new JTextField(16);
    private final JDateChooser expiry = new JDateChooser();

    public MedicineDialog(Frame owner, Medicine existing) {
        super(owner, true);
        this.medicine = existing;
        setTitle(existing == null ? "Add Medicine" : "Edit Medicine #" + existing.getMedicineId());
        setSize(460, 430);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout());
        setJMenuBar(UiUtils.buildMenuBar(null, e -> dispose(), "Close"));

        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(Color.WHITE);
        p.setBorder(BorderFactory.createEmptyBorder(14, 18, 14, 18));
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(7, 8, 7, 8);
        g.anchor = GridBagConstraints.WEST; g.fill = GridBagConstraints.HORIZONTAL;
        int r = 0;
        row(p, g, r++, "Name *", name);
        row(p, g, r++, "Manufacturer", manufacturer);
        row(p, g, r++, "Unit Price *", unitPrice);
        row(p, g, r++, "Stock Qty *", stock);
        row(p, g, r++, "Reorder Level", reorder);
        expiry.setDateFormatString("yyyy-MM-dd");
        expiry.setPreferredSize(new Dimension(180, 28));
        g.gridx=0; g.gridy=r; g.weightx=0; p.add(new JLabel("Expiry Date"), g);
        g.gridx=1; g.gridy=r; g.weightx=1; p.add(expiry, g);
        add(p, BorderLayout.CENTER);

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
        btns.setBackground(new Color(240, 242, 245));
        JButton save = new JButton(existing == null ? "Save" : "Update");
        save.setBackground(UiUtils.ACCENT); save.setForeground(Color.WHITE); save.setFocusPainted(false);
        save.addActionListener(e -> onSave());
        JButton cancel = new JButton("Cancel");
        cancel.addActionListener(e -> dispose());
        btns.add(cancel); btns.add(save);
        add(btns, BorderLayout.SOUTH);

        if (existing != null) {
            name.setText(existing.getName());
            manufacturer.setText(existing.getManufacturer());
            unitPrice.setText(String.valueOf(existing.getUnitPrice()));
            stock.setText(String.valueOf(existing.getStockQty()));
            reorder.setText(String.valueOf(existing.getReorderLevel()));
            expiry.setDate(UiUtils.toDate(existing.getExpiryDate()));
        } else {
            stock.setText("0");
            reorder.setText("10");
        }
    }

    private void row(JPanel p, GridBagConstraints g, int r, String label, JTextField f) {
        g.gridx=0; g.gridy=r; g.weightx=0; p.add(new JLabel(label), g);
        g.gridx=1; g.gridy=r; g.weightx=1; p.add(f, g);
    }

    private void onSave() {
        try {
            ValidationUtil.requireText(name.getText(), "Name");
            double price = ValidationUtil.positiveOrZeroNumber(unitPrice.getText(), "Unit Price");
            int qty = ValidationUtil.intOrZero(stock.getText(), "Stock Qty");
            int reorderVal = ValidationUtil.intOrZero(reorder.getText(), "Reorder Level");

            Medicine m = (medicine == null) ? new Medicine() : medicine;
            m.setName(name.getText().trim());
            m.setManufacturer(manufacturer.getText().trim().isEmpty() ? null : manufacturer.getText().trim());
            m.setUnitPrice(price);
            m.setStockQty(qty);
            m.setReorderLevel(reorderVal);
            m.setExpiryDate(UiUtils.toLocalDate(expiry.getDate()));
            if (medicine == null) dao.insert(m); else dao.update(m);
            AppLogger.info("Medicine saved: " + m.getName());
            dispose();
        } catch (ValidationException ve) {
            UiUtils.error(this, ve.getMessage());
        } catch (Exception ex) {
            AppLogger.error("Saving medicine failed", ex);
            UiUtils.error(this, "Could not save medicine:\n" + ex.getMessage());
        }
    }
}
