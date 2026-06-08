package hms.ui;

import hms.dao.WardDao;
import hms.model.Ward;
import hms.util.AppLogger;
import hms.util.ValidationException;
import hms.util.ValidationUtil;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
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

/** Add/Edit form for a ward. */
public class WardDialog extends JDialog {

    private final WardDao dao = new WardDao();
    private final Ward ward;
    private final JTextField name = new JTextField(16);
    private final JComboBox<String> type =
            new JComboBox<>(new String[]{"General", "Private", "ICU", "Emergency"});
    private final JTextField charge = new JTextField(16);

    public WardDialog(Frame owner, Ward existing) {
        super(owner, true);
        this.ward = existing;
        setTitle(existing == null ? "Add Ward" : "Edit Ward #" + existing.getWardId());
        setSize(420, 270);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout());
        setJMenuBar(UiUtils.buildMenuBar(null, e -> dispose(), "Close"));

        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(Color.WHITE);
        p.setBorder(BorderFactory.createEmptyBorder(16, 18, 16, 18));
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(8, 8, 8, 8);
        g.anchor = GridBagConstraints.WEST; g.fill = GridBagConstraints.HORIZONTAL;
        g.gridx=0; g.gridy=0; p.add(new JLabel("Name *"), g);
        g.gridx=1; g.gridy=0; g.weightx=1; p.add(name, g);
        g.gridx=0; g.gridy=1; g.weightx=0; p.add(new JLabel("Type *"), g);
        g.gridx=1; g.gridy=1; p.add(type, g);
        g.gridx=0; g.gridy=2; p.add(new JLabel("Charge/Day"), g);
        g.gridx=1; g.gridy=2; p.add(charge, g);
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
            type.setSelectedItem(existing.getWardType());
            charge.setText(String.valueOf(existing.getChargePerDay()));
        }
    }

    private void onSave() {
        try {
            ValidationUtil.requireText(name.getText(), "Name");
            double c = charge.getText().trim().isEmpty() ? 0
                    : ValidationUtil.positiveOrZeroNumber(charge.getText(), "Charge/Day");
            Ward w = (ward == null) ? new Ward() : ward;
            w.setName(name.getText().trim());
            w.setWardType((String) type.getSelectedItem());
            w.setChargePerDay(c);
            if (ward == null) dao.insert(w); else dao.update(w);
            AppLogger.info("Ward saved: " + w.getName());
            dispose();
        } catch (ValidationException ve) {
            UiUtils.error(this, ve.getMessage());
        } catch (Exception ex) {
            AppLogger.error("Saving ward failed", ex);
            UiUtils.error(this, "Could not save ward:\n" + ex.getMessage());
        }
    }
}
