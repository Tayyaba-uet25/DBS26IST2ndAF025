package hms.ui;

import hms.dao.PatientDao;
import hms.model.Bill;
import hms.model.BillItem;
import hms.model.Patient;
import hms.service.BillingService;
import hms.util.AppLogger;
import hms.util.ValidationException;
import hms.util.ValidationUtil;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.List;

/** New bill form with line items (transaction on save). */
public class BillDialog extends JDialog {

    private final BillingService service = new BillingService();
    private final JComboBox<Patient> patient = new JComboBox<>();
    private final JTextField notes = new JTextField(20);

    private final JTextField desc = new JTextField(16);
    private final JComboBox<String> type =
            new JComboBox<>(new String[]{"Consultation", "Lab", "Medicine", "Ward", "Other"});
    private final JTextField qty = new JTextField(4);
    private final JTextField price = new JTextField(7);

    private final String[] itemCols = {"Description", "Type", "Qty", "Unit Price", "Amount"};
    private final DefaultTableModel itemModel = new DefaultTableModel(itemCols, 0) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
    };
    private final JTable itemTable = new JTable(itemModel);
    private final List<BillItem> items = new ArrayList<>();
    private final JLabel totalLabel = new JLabel("Total: Rs 0.00");
    private boolean saved = false;

    public BillDialog(Frame owner) {
        super(owner, true);
        setTitle("New Bill");
        setSize(720, 560);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout());
        setJMenuBar(UiUtils.buildMenuBar(null, e -> dispose(), "Close"));

        try { for (Patient p : new PatientDao().findAll()) patient.addItem(p); }
        catch (Exception e) { AppLogger.error("Load patients failed", e); }

        add(buildHeader(), BorderLayout.NORTH);
        add(buildItems(), BorderLayout.CENTER);
        add(buildButtons(), BorderLayout.SOUTH);
    }

    private JPanel buildHeader() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(Color.WHITE);
        p.setBorder(BorderFactory.createEmptyBorder(12, 16, 8, 16));
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(6, 8, 6, 8);
        g.anchor = GridBagConstraints.WEST; g.fill = GridBagConstraints.HORIZONTAL;
        g.gridx=0; g.gridy=0; p.add(new JLabel("Patient *"), g);
        g.gridx=1; g.gridy=0; g.weightx=1; p.add(patient, g);
        g.gridx=0; g.gridy=1; g.weightx=0; p.add(new JLabel("Notes"), g);
        g.gridx=1; g.gridy=1; g.weightx=1; p.add(notes, g);
        return p;
    }

    private JPanel buildItems() {
        JPanel wrap = new JPanel(new BorderLayout(0, 6));
        wrap.setBackground(Color.WHITE);
        wrap.setBorder(BorderFactory.createEmptyBorder(0, 16, 8, 16));
        JLabel l = new JLabel("Bill Items");
        l.setFont(new Font("Segoe UI", Font.BOLD, 14));
        wrap.add(l, BorderLayout.NORTH);

        JPanel addRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 4));
        addRow.setOpaque(false);
        addRow.add(new JLabel("Description:")); addRow.add(desc);
        addRow.add(new JLabel("Type:")); addRow.add(type);
        addRow.add(new JLabel("Qty:")); addRow.add(qty);
        addRow.add(new JLabel("Unit Price:")); addRow.add(price);
        JButton add = new JButton("+ Add");
        add.setBackground(UiUtils.ACCENT); add.setForeground(Color.WHITE); add.setFocusPainted(false);
        add.addActionListener(e -> addItem());
        addRow.add(add);

        itemTable.setRowHeight(26);
        JScrollPane sc = new JScrollPane(itemTable);
        sc.setPreferredSize(new Dimension(0, 210));

        JButton remove = new JButton("Remove Selected");
        remove.addActionListener(e -> removeItem());
        JPanel south = new JPanel(new BorderLayout());
        south.setOpaque(false);
        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT));
        left.setOpaque(false); left.add(remove);
        totalLabel.setFont(new Font("Segoe UI", Font.BOLD, 15));
        totalLabel.setForeground(UiUtils.PRIMARY_DARK);
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        right.setOpaque(false); right.add(totalLabel);
        south.add(left, BorderLayout.WEST);
        south.add(right, BorderLayout.EAST);

        JPanel inner = new JPanel(new BorderLayout());
        inner.setOpaque(false);
        inner.add(addRow, BorderLayout.NORTH);
        inner.add(sc, BorderLayout.CENTER);
        inner.add(south, BorderLayout.SOUTH);
        wrap.add(inner, BorderLayout.CENTER);
        return wrap;
    }

    private JPanel buildButtons() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
        p.setBackground(new Color(240, 242, 245));
        JButton save = new JButton("Save Bill");
        save.setBackground(UiUtils.PRIMARY); save.setForeground(Color.WHITE); save.setFocusPainted(false);
        save.addActionListener(e -> onSave());
        JButton cancel = new JButton("Cancel");
        cancel.addActionListener(e -> dispose());
        p.add(cancel); p.add(save);
        return p;
    }

    private void addItem() {
        try {
            ValidationUtil.requireText(desc.getText(), "Description");
            int q = ValidationUtil.positiveInt(qty.getText(), "Qty");
            double up = ValidationUtil.positiveOrZeroNumber(price.getText(), "Unit Price");
            BillItem it = new BillItem(desc.getText().trim(), (String) type.getSelectedItem(), q, up);
            items.add(it);
            itemModel.addRow(new Object[]{it.getDescription(), it.getItemType(), q, up, it.getAmount()});
            desc.setText(""); qty.setText(""); price.setText("");
            updateTotal();
        } catch (ValidationException ve) {
            UiUtils.error(this, ve.getMessage());
        }
    }

    private void removeItem() {
        int row = itemTable.getSelectedRow();
        if (row >= 0) { items.remove(row); itemModel.removeRow(row); updateTotal(); }
    }

    private void updateTotal() {
        double t = 0;
        for (BillItem it : items) t += it.getAmount();
        totalLabel.setText(String.format("Total: Rs %.2f", t));
    }

    private void onSave() {
        try {
            ValidationUtil.requireSelection(patient.getSelectedItem(), "patient");
            if (items.isEmpty()) throw new ValidationException("Add at least one bill item.");
            Bill bill = new Bill();
            bill.setPatientId(((Patient) patient.getSelectedItem()).getPatientId());
            bill.setNotes(notes.getText().trim().isEmpty() ? null : notes.getText().trim());
            bill.setItems(items);
            int id = service.createBill(bill);   // TRANSACTION
            saved = true;
            UiUtils.info(this, "Bill #" + id + " created successfully.", "Success");
            dispose();
        } catch (ValidationException ve) {
            UiUtils.error(this, ve.getMessage());
        } catch (Exception ex) {
            AppLogger.error("Saving bill failed", ex);
            UiUtils.error(this, "Could not save bill:\n" + ex.getMessage());
        }
    }

    public boolean isSaved() { return saved; }
}
