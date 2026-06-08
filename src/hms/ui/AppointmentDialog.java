package hms.ui;

import com.toedter.calendar.JDateChooser;
import hms.dao.AppointmentDao;
import hms.dao.DoctorDao;
import hms.dao.PatientDao;
import hms.model.Appointment;
import hms.model.Doctor;
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
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerDateModel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Calendar;
import java.util.Date;

/** Add/Edit form for an appointment. */
public class AppointmentDialog extends JDialog {

    private final AppointmentDao dao = new AppointmentDao();
    private final Appointment appt;

    private final JComboBox<Patient> patient = new JComboBox<>();
    private final JComboBox<Doctor> doctor = new JComboBox<>();
    private final JDateChooser date = new JDateChooser();
    private final JSpinner time = new JSpinner(new SpinnerDateModel());
    private final JTextField reason = new JTextField(18);
    private final JComboBox<String> status =
            new JComboBox<>(new String[]{"Scheduled", "Completed", "Cancelled"});
    private final JTextField fee = new JTextField(18);

    public AppointmentDialog(Frame owner, Appointment existing) {
        super(owner, true);
        this.appt = existing;
        setTitle(existing == null ? "New Appointment" : "Edit Appointment #" + existing.getAppointmentId());
        setSize(500, 480);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout());
        setJMenuBar(UiUtils.buildMenuBar(null, e -> dispose(), "Close"));

        loadCombos();
        add(buildBody(), BorderLayout.CENTER);
        add(buildButtons(), BorderLayout.SOUTH);

        if (existing != null) populate(existing);
        else {
            date.setDate(new Date());
            time.setValue(new Date());
            autoFee();
        }
        doctor.addActionListener(e -> { if (appt == null) autoFee(); });
    }

    private void loadCombos() {
        try {
            for (Patient p : new PatientDao().findAll()) patient.addItem(p);
            for (Doctor d : new DoctorDao().findAll()) doctor.addItem(d);
        } catch (Exception e) {
            AppLogger.error("Loading combos failed", e);
        }
    }

    private void autoFee() {
        Doctor d = (Doctor) doctor.getSelectedItem();
        if (d != null) fee.setText(String.valueOf(d.getConsultationFee()));
    }

    private JPanel buildBody() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(Color.WHITE);
        p.setBorder(BorderFactory.createEmptyBorder(14, 18, 14, 18));
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(7, 8, 7, 8);
        g.anchor = GridBagConstraints.WEST; g.fill = GridBagConstraints.HORIZONTAL;

        int r = 0;
        combo(p, g, r++, "Patient *", patient);
        combo(p, g, r++, "Doctor *", doctor);

        date.setDateFormatString("yyyy-MM-dd");
        date.setPreferredSize(new Dimension(180, 28));
        g.gridx = 0; g.gridy = r; p.add(new JLabel("Date *"), g);
        g.gridx = 1; g.gridy = r++; p.add(date, g);

        time.setEditor(new JSpinner.DateEditor(time, "HH:mm"));
        g.gridx = 0; g.gridy = r; p.add(new JLabel("Time *"), g);
        g.gridx = 1; g.gridy = r++; p.add(time, g);

        field(p, g, r++, "Reason", reason);
        combo(p, g, r++, "Status", status);
        field(p, g, r++, "Fee", fee);
        return p;
    }

    private void combo(JPanel p, GridBagConstraints g, int r, String label, JComboBox<?> c) {
        g.gridx = 0; g.gridy = r; g.weightx = 0; p.add(new JLabel(label), g);
        g.gridx = 1; g.gridy = r; g.weightx = 1; p.add(c, g);
    }

    private void field(JPanel p, GridBagConstraints g, int r, String label, JTextField f) {
        g.gridx = 0; g.gridy = r; g.weightx = 0; p.add(new JLabel(label), g);
        g.gridx = 1; g.gridy = r; g.weightx = 1; p.add(f, g);
    }

    private JPanel buildButtons() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
        p.setBackground(new Color(240, 242, 245));
        JButton save = new JButton(appt == null ? "Save" : "Update");
        save.setBackground(UiUtils.ACCENT); save.setForeground(Color.WHITE); save.setFocusPainted(false);
        save.addActionListener(e -> onSave());
        JButton cancel = new JButton("Cancel");
        cancel.addActionListener(e -> dispose());
        p.add(cancel); p.add(save);
        return p;
    }

    private void populate(Appointment a) {
        selectPatient(a.getPatientId());
        selectDoctor(a.getDoctorId());
        if (a.getAppointmentDate() != null) {
            date.setDate(UiUtils.toDate(a.getAppointmentDate().toLocalDate()));
            time.setValue(UiUtils.toDate(a.getAppointmentDate()));
        }
        reason.setText(a.getReason());
        status.setSelectedItem(a.getStatus());
        fee.setText(String.valueOf(a.getFee()));
    }

    private void selectPatient(int id) {
        for (int i = 0; i < patient.getItemCount(); i++)
            if (patient.getItemAt(i).getPatientId() == id) { patient.setSelectedIndex(i); return; }
    }

    private void selectDoctor(int id) {
        for (int i = 0; i < doctor.getItemCount(); i++)
            if (doctor.getItemAt(i).getDoctorId() == id) { doctor.setSelectedIndex(i); return; }
    }

    private void onSave() {
        try {
            ValidationUtil.requireSelection(patient.getSelectedItem(), "patient");
            ValidationUtil.requireSelection(doctor.getSelectedItem(), "doctor");
            if (date.getDate() == null) throw new ValidationException("Please select a date.");
            double feeVal = fee.getText().trim().isEmpty() ? 0
                    : ValidationUtil.positiveOrZeroNumber(fee.getText(), "Fee");

            LocalDate d = UiUtils.toLocalDate(date.getDate());
            Calendar cal = Calendar.getInstance();
            cal.setTime((Date) time.getValue());
            LocalTime t = LocalTime.of(cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE));
            LocalDateTime when = LocalDateTime.of(d, t);

            Appointment a = (appt == null) ? new Appointment() : appt;
            a.setPatientId(((Patient) patient.getSelectedItem()).getPatientId());
            a.setDoctorId(((Doctor) doctor.getSelectedItem()).getDoctorId());
            a.setAppointmentDate(when);
            a.setReason(reason.getText().trim().isEmpty() ? null : reason.getText().trim());
            a.setStatus((String) status.getSelectedItem());
            a.setFee(feeVal);

            if (appt == null) dao.insert(a); else dao.update(a);
            AppLogger.info("Appointment saved");
            dispose();
        } catch (ValidationException ve) {
            UiUtils.error(this, ve.getMessage());
        } catch (Exception ex) {
            AppLogger.error("Saving appointment failed", ex);
            UiUtils.error(this, "Could not save appointment:\n" + ex.getMessage());
        }
    }
}
