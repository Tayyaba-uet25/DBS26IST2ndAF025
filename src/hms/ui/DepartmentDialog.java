package hms.ui;

import hms.dao.DepartmentDao;
import hms.model.Department;
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
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

/** Add/Edit form for a department. */
public class DepartmentDialog extends JDialog {

    private final DepartmentDao dao = new DepartmentDao();
    private final Department dept;
    private final JTextField name = new JTextField(18);
    private final JTextField location = new JTextField(18);
    private final JTextField phone = new JTextField(18);

    public DepartmentDialog(Frame owner, Department existing) {
        super(owner, true);
        this.dept = existing;
        setTitle(existing == null ? "Add Department" : "Edit Department #" + existing.getDepartmentId());
        setSize(420, 280);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout());
        setJMenuBar(UiUtils.buildMenuBar(null, e -> dispose(), "Close"));

        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(Color.WHITE);
        p.setBorder(BorderFactory.createEmptyBorder(16, 18, 16, 18));
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(8, 8, 8, 8);
        g.anchor = GridBagConstraints.WEST; g.fill = GridBagConstraints.HORIZONTAL;
        row(p, g, 0, "Name *", name);
        row(p, g, 1, "Location", location);
        row(p, g, 2, "Phone", phone);
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
            location.setText(existing.getLocation());
            phone.setText(existing.getPhone());
        }
    }

    private void row(JPanel p, GridBagConstraints g, int r, String label, JTextField f) {
        g.gridx = 0; g.gridy = r; g.weightx = 0; p.add(new JLabel(label), g);
        g.gridx = 1; g.gridy = r; g.weightx = 1; p.add(f, g);
    }

    private void onSave() {
        try {
            ValidationUtil.requireText(name.getText(), "Name");
            ValidationUtil.phone(phone.getText(), "Phone");
            String phoneVal = emptyToNull(phone.getText());
            if (phoneVal != null && dao.phoneExists(phoneVal, dept == null ? 0 : dept.getDepartmentId())) {
                throw new ValidationException("This phone number is already used by another department.");
            }
            Department d = (dept == null) ? new Department() : dept;
            d.setName(name.getText().trim());
            d.setLocation(emptyToNull(location.getText()));
            d.setPhone(emptyToNull(phone.getText()));
            if (dept == null) dao.insert(d); else dao.update(d);
            AppLogger.info("Department saved: " + d.getName());
            dispose();
        } catch (ValidationException ve) {
            UiUtils.error(this, ve.getMessage());
        } catch (Exception ex) {
            AppLogger.error("Saving department failed", ex);
            UiUtils.error(this, "Could not save department:\n" + ex.getMessage());
        }
    }

    private String emptyToNull(String s) {
        return (s == null || s.trim().isEmpty()) ? null : s.trim();
    }
}
