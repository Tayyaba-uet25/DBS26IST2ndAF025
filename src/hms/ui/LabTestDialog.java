package hms.ui;

import hms.dao.LabTestDao;
import hms.model.LabTest;
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

/** Add/Edit form for a lab test. */
public class LabTestDialog extends JDialog {

    private final LabTestDao dao = new LabTestDao();
    private final LabTest test;
    private final JTextField name = new JTextField(18);
    private final JTextField price = new JTextField(18);
    private final JTextField description = new JTextField(18);

    public LabTestDialog(Frame owner, LabTest existing) {
        super(owner, true);
        this.test = existing;
        setTitle(existing == null ? "Add Lab Test" : "Edit Lab Test #" + existing.getTestId());
        setSize(440, 270);
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
        row(p, g, 1, "Price *", price);
        row(p, g, 2, "Description", description);
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
            price.setText(String.valueOf(existing.getPrice()));
            description.setText(existing.getDescription());
        }
    }

    private void row(JPanel p, GridBagConstraints g, int r, String label, JTextField f) {
        g.gridx=0; g.gridy=r; g.weightx=0; p.add(new JLabel(label), g);
        g.gridx=1; g.gridy=r; g.weightx=1; p.add(f, g);
    }

    private void onSave() {
        try {
            ValidationUtil.requireText(name.getText(), "Name");
            double pr = ValidationUtil.positiveOrZeroNumber(price.getText(), "Price");
            LabTest t = (test == null) ? new LabTest() : test;
            t.setName(name.getText().trim());
            t.setPrice(pr);
            t.setDescription(description.getText().trim().isEmpty() ? null : description.getText().trim());
            if (test == null) dao.insert(t); else dao.update(t);
            AppLogger.info("Lab test saved: " + t.getName());
            dispose();
        } catch (ValidationException ve) {
            UiUtils.error(this, ve.getMessage());
        } catch (Exception ex) {
            AppLogger.error("Saving lab test failed", ex);
            UiUtils.error(this, "Could not save lab test:\n" + ex.getMessage());
        }
    }
}
