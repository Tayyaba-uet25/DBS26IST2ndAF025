package hms.ui;

import com.toedter.calendar.JDateChooser;
import hms.dao.DepartmentDao;
import hms.dao.DoctorDao;
import hms.model.Department;
import hms.model.Doctor;
import hms.util.AppLogger;
import hms.util.ValidationException;
import hms.util.ValidationUtil;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.sql.SQLException;
import java.util.List;

/** Add/Edit form for a doctor (same form for both). */
public class DoctorDialog extends JDialog {

    private final DoctorDao dao = new DoctorDao();
    private final Doctor doctor;

    private final JTextField firstName = new JTextField(16);
    private final JTextField lastName = new JTextField(16);
    private final JRadioButton male = new JRadioButton("Male");
    private final JRadioButton female = new JRadioButton("Female");
    private final JTextField specialization = new JTextField(16);
    private final JComboBox<Department> department = new JComboBox<>();
    private final JTextField phone = new JTextField(16);
    private final JTextField email = new JTextField(16);
    private final JTextField fee = new JTextField(16);
    private final JDateChooser hireDate = new JDateChooser();
    private final JCheckBox active = new JCheckBox("Active");

    public DoctorDialog(Frame owner, Doctor existing) {
        super(owner, true);
        this.doctor = existing;
        setTitle(existing == null ? "Add Doctor" : "Edit Doctor #" + existing.getDoctorId());
        setSize(520, 560);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout());
        setJMenuBar(UiUtils.buildMenuBar(null, e -> dispose(), "Close"));

        loadDepartments();
        add(buildBody(), BorderLayout.CENTER);
        add(buildButtons(), BorderLayout.SOUTH);

        if (existing != null) populate(existing);
        else { male.setSelected(true); active.setSelected(true); }
    }

    private void loadDepartments() {
        try {
            List<Department> list = new DepartmentDao().findAll();
            for (Department d : list) department.addItem(d);
        } catch (SQLException e) {
            AppLogger.error("Loading departments failed", e);
        }
    }

    private JPanel buildBody() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(Color.WHITE);
        p.setBorder(BorderFactory.createEmptyBorder(14, 18, 14, 18));
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(6, 8, 6, 8);
        g.anchor = GridBagConstraints.WEST;
        g.fill = GridBagConstraints.HORIZONTAL;

        int r = 0;
        row(p, g, r++, "First Name *", firstName);
        row(p, g, r++, "Last Name *", lastName);

        JPanel genderPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        genderPanel.setOpaque(false);
        ButtonGroup bg = new ButtonGroup(); bg.add(male); bg.add(female);
        genderPanel.add(male); genderPanel.add(female);
        g.gridx = 0; g.gridy = r; p.add(new JLabel("Gender *"), g);
        g.gridx = 1; g.gridy = r++; p.add(genderPanel, g);

        row(p, g, r++, "Specialization", specialization);

        g.gridx = 0; g.gridy = r; p.add(new JLabel("Department"), g);
        g.gridx = 1; g.gridy = r++; p.add(department, g);

        row(p, g, r++, "Phone", phone);
        row(p, g, r++, "Email", email);
        row(p, g, r++, "Consultation Fee", fee);

        hireDate.setDateFormatString("yyyy-MM-dd");
        hireDate.setPreferredSize(new Dimension(180, 28));
        g.gridx = 0; g.gridy = r; p.add(new JLabel("Hire Date"), g);
        g.gridx = 1; g.gridy = r++; p.add(hireDate, g);

        g.gridx = 1; g.gridy = r; p.add(active, g);
        return p;
    }

    private void row(JPanel p, GridBagConstraints g, int r, String label, JTextField f) {
        g.gridx = 0; g.gridy = r; g.weightx = 0; p.add(new JLabel(label), g);
        g.gridx = 1; g.gridy = r; g.weightx = 1; p.add(f, g);
    }

    private JPanel buildButtons() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
        p.setBackground(new Color(240, 242, 245));
        JButton save = new JButton(doctor == null ? "Save" : "Update");
        save.setBackground(UiUtils.ACCENT); save.setForeground(Color.WHITE); save.setFocusPainted(false);
        save.addActionListener(e -> onSave());
        JButton cancel = new JButton("Cancel");
        cancel.addActionListener(e -> dispose());
        p.add(cancel); p.add(save);
        return p;
    }

    private void populate(Doctor d) {
        firstName.setText(d.getFirstName());
        lastName.setText(d.getLastName());
        if ("Female".equals(d.getGender())) female.setSelected(true); else male.setSelected(true);
        specialization.setText(d.getSpecialization());
        for (int i = 0; i < department.getItemCount(); i++) {
            if (department.getItemAt(i).getDepartmentId() == d.getDepartmentId()) {
                department.setSelectedIndex(i); break;
            }
        }
        phone.setText(d.getPhone());
        email.setText(d.getEmail());
        fee.setText(String.valueOf(d.getConsultationFee()));
        hireDate.setDate(UiUtils.toDate(d.getHireDate()));
        active.setSelected(d.isActive());
    }

    private void onSave() {
        try {
            ValidationUtil.requireText(firstName.getText(), "First Name");
            ValidationUtil.requireText(lastName.getText(), "Last Name");
            ValidationUtil.phone(phone.getText(), "Phone");
            ValidationUtil.email(email.getText(), "Email");
            double feeVal = fee.getText().trim().isEmpty() ? 0
                    : ValidationUtil.positiveOrZeroNumber(fee.getText(), "Consultation Fee");

            Doctor d = (doctor == null) ? new Doctor() : doctor;
            d.setFirstName(firstName.getText().trim());
            d.setLastName(lastName.getText().trim());
            d.setGender(female.isSelected() ? "Female" : "Male");
            d.setSpecialization(emptyToNull(specialization.getText()));
            Department dep = (Department) department.getSelectedItem();
            d.setDepartmentId(dep == null ? 0 : dep.getDepartmentId());
            d.setPhone(emptyToNull(phone.getText()));
            d.setEmail(emptyToNull(email.getText()));
            d.setConsultationFee(feeVal);
            d.setHireDate(UiUtils.toLocalDate(hireDate.getDate()));
            d.setActive(active.isSelected());

            if (doctor == null) dao.insert(d); else dao.update(d);
            AppLogger.info("Doctor saved: " + d.getFullName());
            dispose();
        } catch (ValidationException ve) {
            UiUtils.error(this, ve.getMessage());
        } catch (Exception ex) {
            AppLogger.error("Saving doctor failed", ex);
            UiUtils.error(this, "Could not save doctor:\n" + ex.getMessage());
        }
    }

    private String emptyToNull(String s) {
        return (s == null || s.trim().isEmpty()) ? null : s.trim();
    }
}
