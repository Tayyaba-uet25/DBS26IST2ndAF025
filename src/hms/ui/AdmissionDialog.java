package hms.ui;

import hms.dao.BedDao;
import hms.dao.DoctorDao;
import hms.dao.PatientDao;
import hms.model.Bed;
import hms.model.Doctor;
import hms.model.Patient;
import hms.service.AdmissionService;
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

/** Admit-patient form. Uses the stored procedure via AdmissionService. */
public class AdmissionDialog extends JDialog {

    private final AdmissionService service = new AdmissionService();
    private final JComboBox<Patient> patient = new JComboBox<>();
    private final JComboBox<Bed> bed = new JComboBox<>();
    private final JComboBox<Doctor> doctor = new JComboBox<>();
    private final JTextArea diagnosis = new JTextArea(4, 18);
    private boolean saved = false;

    public AdmissionDialog(Frame owner) {
        super(owner, true);
        setTitle("Admit Patient");
        setSize(500, 430);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout());
        setJMenuBar(UiUtils.buildMenuBar(null, e -> dispose(), "Close"));

        try {
            for (Patient p : new PatientDao().findAll()) patient.addItem(p);
            for (Bed b : new BedDao().findAvailable()) bed.addItem(b);
            for (Doctor d : new DoctorDao().findAll()) doctor.addItem(d);
        } catch (Exception e) { AppLogger.error("Load admit combos failed", e); }

        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(Color.WHITE);
        p.setBorder(BorderFactory.createEmptyBorder(14, 18, 14, 18));
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(8, 8, 8, 8);
        g.anchor = GridBagConstraints.WEST; g.fill = GridBagConstraints.HORIZONTAL;
        int r = 0;
        combo(p, g, r++, "Patient *", patient);
        combo(p, g, r++, "Available Bed *", bed);
        combo(p, g, r++, "Attending Doctor *", doctor);
        diagnosis.setLineWrap(true); diagnosis.setWrapStyleWord(true);
        JScrollPane sc = new JScrollPane(diagnosis);
        sc.setPreferredSize(new Dimension(240, 90));
        g.gridx=0; g.gridy=r; g.weightx=0; g.anchor = GridBagConstraints.NORTHWEST;
        p.add(new JLabel("Diagnosis"), g);
        g.gridx=1; g.gridy=r; g.weightx=1; p.add(sc, g);
        add(p, BorderLayout.CENTER);

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
        btns.setBackground(new Color(240, 242, 245));
        JButton save = new JButton("Admit");
        save.setBackground(UiUtils.ACCENT); save.setForeground(Color.WHITE); save.setFocusPainted(false);
        save.addActionListener(e -> onAdmit());
        JButton cancel = new JButton("Cancel");
        cancel.addActionListener(e -> dispose());
        btns.add(cancel); btns.add(save);
        add(btns, BorderLayout.SOUTH);
    }

    private void combo(JPanel p, GridBagConstraints g, int r, String label, JComboBox<?> c) {
        g.gridx=0; g.gridy=r; g.weightx=0; p.add(new JLabel(label), g);
        g.gridx=1; g.gridy=r; g.weightx=1; p.add(c, g);
    }

    private void onAdmit() {
        try {
            ValidationUtil.requireSelection(patient.getSelectedItem(), "patient");
            ValidationUtil.requireSelection(bed.getSelectedItem(), "bed");
            ValidationUtil.requireSelection(doctor.getSelectedItem(), "doctor");
            int id = service.admitPatient(
                    ((Patient) patient.getSelectedItem()).getPatientId(),
                    ((Bed) bed.getSelectedItem()).getBedId(),
                    ((Doctor) doctor.getSelectedItem()).getDoctorId(),
                    diagnosis.getText().trim().isEmpty() ? null : diagnosis.getText().trim());
            saved = true;
            UiUtils.info(this, "Patient admitted successfully (Admission #" + id + ").", "Success");
            dispose();
        } catch (ValidationException ve) {
            UiUtils.error(this, ve.getMessage());
        } catch (Exception ex) {
            AppLogger.error("Admit failed", ex);
            UiUtils.error(this, "Admission failed:\n" + ex.getMessage());
        }
    }

    public boolean isSaved() { return saved; }
}
