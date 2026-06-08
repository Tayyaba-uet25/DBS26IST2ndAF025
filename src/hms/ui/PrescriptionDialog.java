package hms.ui;

import hms.dao.DoctorDao;
import hms.dao.MedicineDao;
import hms.dao.PatientDao;
import hms.model.Doctor;
import hms.model.Medicine;
import hms.model.Patient;
import hms.model.Prescription;
import hms.model.PrescriptionItem;
import hms.service.PrescriptionService;
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

/** New prescription form with a medicine items sub-table (transaction on save). */
public class PrescriptionDialog extends JDialog {

    private final PrescriptionService service = new PrescriptionService();
    private final JComboBox<Patient> patient = new JComboBox<>();
    private final JComboBox<Doctor> doctor = new JComboBox<>();
    private final JTextField notes = new JTextField(20);

    private final JComboBox<Medicine> medicine = new JComboBox<>();
    private final JTextField dosage = new JTextField(8);
    private final JTextField qty = new JTextField(4);
    private final JTextField instructions = new JTextField(12);

    private final String[] itemCols = {"Medicine", "Dosage", "Qty", "Instructions"};
    private final DefaultTableModel itemModel = new DefaultTableModel(itemCols, 0) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
    };
    private final JTable itemTable = new JTable(itemModel);
    private final List<PrescriptionItem> items = new ArrayList<>();
    private boolean saved = false;

    public PrescriptionDialog(Frame owner) {
        super(owner, true);
        setTitle("New Prescription");
        setSize(720, 560);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout());
        setJMenuBar(UiUtils.buildMenuBar(null, e -> dispose(), "Close"));

        try {
            for (Patient p : new PatientDao().findAll()) patient.addItem(p);
            for (Doctor d : new DoctorDao().findAll()) doctor.addItem(d);
            for (Medicine m : new MedicineDao().findAll()) medicine.addItem(m);
        } catch (Exception e) { AppLogger.error("Load rx combos failed", e); }

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
        g.gridx=2; g.gridy=0; g.weightx=0; p.add(new JLabel("Doctor *"), g);
        g.gridx=3; g.gridy=0; g.weightx=1; p.add(doctor, g);
        g.gridx=0; g.gridy=1; g.weightx=0; p.add(new JLabel("Notes"), g);
        g.gridx=1; g.gridy=1; g.gridwidth=3; g.weightx=1; p.add(notes, g);
        return p;
    }

    private JPanel buildItems() {
        JPanel wrap = new JPanel(new BorderLayout(0, 6));
        wrap.setBackground(Color.WHITE);
        wrap.setBorder(BorderFactory.createEmptyBorder(0, 16, 8, 16));

        JLabel l = new JLabel("Medicines");
        l.setFont(new Font("Segoe UI", Font.BOLD, 14));
        wrap.add(l, BorderLayout.NORTH);

        // add-item row
        JPanel addRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 4));
        addRow.setOpaque(false);
        medicine.setPreferredSize(new Dimension(220, 26));
        addRow.add(new JLabel("Medicine:")); addRow.add(medicine);
        addRow.add(new JLabel("Dosage:")); addRow.add(dosage);
        addRow.add(new JLabel("Qty:")); addRow.add(qty);
        addRow.add(new JLabel("Instructions:")); addRow.add(instructions);
        JButton add = new JButton("+ Add");
        add.setBackground(UiUtils.ACCENT); add.setForeground(Color.WHITE); add.setFocusPainted(false);
        add.addActionListener(e -> addItem());
        addRow.add(add);

        JButton remove = new JButton("Remove Selected");
        remove.addActionListener(e -> removeItem());

        itemTable.setRowHeight(26);
        JScrollPane sc = new JScrollPane(itemTable);
        sc.setPreferredSize(new Dimension(0, 220));

        JPanel south = new JPanel(new FlowLayout(FlowLayout.LEFT));
        south.setOpaque(false);
        south.add(remove);

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
        JButton save = new JButton("Save Prescription");
        save.setBackground(UiUtils.PRIMARY); save.setForeground(Color.WHITE); save.setFocusPainted(false);
        save.addActionListener(e -> onSave());
        JButton cancel = new JButton("Cancel");
        cancel.addActionListener(e -> dispose());
        p.add(cancel); p.add(save);
        return p;
    }

    private void addItem() {
        try {
            ValidationUtil.requireSelection(medicine.getSelectedItem(), "medicine");
            int q = ValidationUtil.positiveInt(qty.getText(), "Qty");
            Medicine m = (Medicine) medicine.getSelectedItem();
            PrescriptionItem it = new PrescriptionItem();
            it.setMedicineId(m.getMedicineId());
            it.setMedicineName(m.getName());
            it.setDosage(dosage.getText().trim());
            it.setQuantity(q);
            it.setInstructions(instructions.getText().trim());
            items.add(it);
            itemModel.addRow(new Object[]{m.getName(), it.getDosage(), q, it.getInstructions()});
            dosage.setText(""); qty.setText(""); instructions.setText("");
        } catch (ValidationException ve) {
            UiUtils.error(this, ve.getMessage());
        }
    }

    private void removeItem() {
        int row = itemTable.getSelectedRow();
        if (row >= 0) {
            items.remove(row);
            itemModel.removeRow(row);
        }
    }

    private void onSave() {
        try {
            ValidationUtil.requireSelection(patient.getSelectedItem(), "patient");
            ValidationUtil.requireSelection(doctor.getSelectedItem(), "doctor");
            // If the user picked a medicine + Qty but forgot to click "+ Add",
            // add that row automatically so the prescription still saves.
            if (items.isEmpty() && medicine.getSelectedItem() != null
                    && !qty.getText().trim().isEmpty()) {
                addItem();
            }
            if (items.isEmpty()) {
                throw new ValidationException(
                        "Please add at least one medicine: pick a Medicine, type a Qty, then click \"+ Add\".");
            }

            Prescription rx = new Prescription();
            rx.setPatientId(((Patient) patient.getSelectedItem()).getPatientId());
            rx.setDoctorId(((Doctor) doctor.getSelectedItem()).getDoctorId());
            rx.setNotes(notes.getText().trim().isEmpty() ? null : notes.getText().trim());
            rx.setItems(items);

            int id = service.savePrescription(rx);   // TRANSACTION
            saved = true;
            UiUtils.info(this, "Prescription #" + id + " saved.", "Success");
            dispose();
        } catch (ValidationException ve) {
            UiUtils.error(this, ve.getMessage());
        } catch (Exception ex) {
            AppLogger.error("Saving prescription failed", ex);
            UiUtils.error(this, "Could not save prescription:\n" + ex.getMessage());
        }
    }

    public boolean isSaved() { return saved; }
}
