package hms.ui;

import hms.dao.DoctorDao;
import hms.dao.LabOrderDao;
import hms.dao.LabTestDao;
import hms.dao.PatientDao;
import hms.model.Doctor;
import hms.model.LabOrder;
import hms.model.LabTest;
import hms.model.Patient;
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
import javax.swing.JTextArea;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

/** Add/Edit form for a lab order. */
public class LabOrderDialog extends JDialog {

    private final LabOrderDao dao = new LabOrderDao();
    private final LabOrder order;
    private final JComboBox<Patient> patient = new JComboBox<>();
    private final JComboBox<Doctor> doctor = new JComboBox<>();
    private final JComboBox<LabTest> test = new JComboBox<>();
    private final JComboBox<String> status =
            new JComboBox<>(new String[]{"Pending", "Completed", "Cancelled"});
    private final JTextArea result = new JTextArea(3, 18);

    public LabOrderDialog(Frame owner, LabOrder existing) {
        super(owner, true);
        this.order = existing;
        setTitle(existing == null ? "New Lab Order" : "Edit Lab Order #" + existing.getOrderId());
        setSize(500, 420);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout());
        setJMenuBar(UiUtils.buildMenuBar(null, e -> dispose(), "Close"));

        try {
            for (Patient p : new PatientDao().findAll()) patient.addItem(p);
            for (Doctor d : new DoctorDao().findAll()) doctor.addItem(d);
            for (LabTest t : new LabTestDao().findAll()) test.addItem(t);
        } catch (Exception e) { AppLogger.error("Load lab order combos failed", e); }

        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(Color.WHITE);
        p.setBorder(BorderFactory.createEmptyBorder(14, 18, 14, 18));
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(7, 8, 7, 8);
        g.anchor = GridBagConstraints.WEST; g.fill = GridBagConstraints.HORIZONTAL;
        int r = 0;
        combo(p, g, r++, "Patient *", patient);
        combo(p, g, r++, "Doctor *", doctor);
        combo(p, g, r++, "Test *", test);
        combo(p, g, r++, "Status", status);
        result.setLineWrap(true); result.setWrapStyleWord(true);
        JScrollPane sc = new JScrollPane(result);
        sc.setPreferredSize(new Dimension(240, 70));
        g.gridx=0; g.gridy=r; g.weightx=0; g.anchor = GridBagConstraints.NORTHWEST;
        p.add(new JLabel("Result"), g);
        g.gridx=1; g.gridy=r; g.weightx=1; p.add(sc, g);
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

        if (existing != null) populate(existing);
    }

    private void combo(JPanel p, GridBagConstraints g, int r, String label, JComboBox<?> c) {
        g.gridx=0; g.gridy=r; g.weightx=0; p.add(new JLabel(label), g);
        g.gridx=1; g.gridy=r; g.weightx=1; p.add(c, g);
    }

    private void populate(LabOrder o) {
        for (int i = 0; i < patient.getItemCount(); i++)
            if (patient.getItemAt(i).getPatientId() == o.getPatientId()) { patient.setSelectedIndex(i); break; }
        for (int i = 0; i < doctor.getItemCount(); i++)
            if (doctor.getItemAt(i).getDoctorId() == o.getDoctorId()) { doctor.setSelectedIndex(i); break; }
        for (int i = 0; i < test.getItemCount(); i++)
            if (test.getItemAt(i).getTestId() == o.getTestId()) { test.setSelectedIndex(i); break; }
        status.setSelectedItem(o.getStatus());
        result.setText(o.getResult());
    }

    private void onSave() {
        try {
            ValidationUtil.requireSelection(patient.getSelectedItem(), "patient");
            ValidationUtil.requireSelection(doctor.getSelectedItem(), "doctor");
            ValidationUtil.requireSelection(test.getSelectedItem(), "test");
            LabOrder o = (order == null) ? new LabOrder() : order;
            o.setPatientId(((Patient) patient.getSelectedItem()).getPatientId());
            o.setDoctorId(((Doctor) doctor.getSelectedItem()).getDoctorId());
            o.setTestId(((LabTest) test.getSelectedItem()).getTestId());
            o.setStatus((String) status.getSelectedItem());
            o.setResult(result.getText().trim().isEmpty() ? null : result.getText().trim());
            if (order == null) dao.insert(o); else dao.update(o);
            AppLogger.info("Lab order saved");
            dispose();
        } catch (ValidationException ve) {
            UiUtils.error(this, ve.getMessage());
        } catch (Exception ex) {
            AppLogger.error("Saving lab order failed", ex);
            UiUtils.error(this, "Could not save lab order:\n" + ex.getMessage());
        }
    }
}
