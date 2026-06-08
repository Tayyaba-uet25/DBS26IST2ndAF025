package hms.ui;

import hms.dao.BedDao;
import hms.dao.WardDao;
import hms.model.Bed;
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

/** Add/Edit form for a bed. */
public class BedDialog extends JDialog {

    private final BedDao dao = new BedDao();
    private final Bed bed;
    private final JTextField number = new JTextField(16);
    private final JComboBox<Ward> ward = new JComboBox<>();
    private final JComboBox<String> status =
            new JComboBox<>(new String[]{"Available", "Occupied", "Maintenance"});

    public BedDialog(Frame owner, Bed existing) {
        super(owner, true);
        this.bed = existing;
        setTitle(existing == null ? "Add Bed" : "Edit Bed #" + existing.getBedId());
        setSize(420, 270);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout());
        setJMenuBar(UiUtils.buildMenuBar(null, e -> dispose(), "Close"));

        try { for (Ward w : new WardDao().findAll()) ward.addItem(w); }
        catch (Exception e) { AppLogger.error("Load wards failed", e); }

        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(Color.WHITE);
        p.setBorder(BorderFactory.createEmptyBorder(16, 18, 16, 18));
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(8, 8, 8, 8);
        g.anchor = GridBagConstraints.WEST; g.fill = GridBagConstraints.HORIZONTAL;
        g.gridx=0; g.gridy=0; p.add(new JLabel("Bed Number *"), g);
        g.gridx=1; g.gridy=0; g.weightx=1; p.add(number, g);
        g.gridx=0; g.gridy=1; g.weightx=0; p.add(new JLabel("Ward *"), g);
        g.gridx=1; g.gridy=1; p.add(ward, g);
        g.gridx=0; g.gridy=2; p.add(new JLabel("Status"), g);
        g.gridx=1; g.gridy=2; p.add(status, g);
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
            number.setText(existing.getBedNumber());
            for (int i = 0; i < ward.getItemCount(); i++)
                if (ward.getItemAt(i).getWardId() == existing.getWardId()) { ward.setSelectedIndex(i); break; }
            status.setSelectedItem(existing.getStatus());
        }
    }

    private void onSave() {
        try {
            ValidationUtil.requireText(number.getText(), "Bed Number");
            ValidationUtil.requireSelection(ward.getSelectedItem(), "ward");
            Bed b = (bed == null) ? new Bed() : bed;
            b.setBedNumber(number.getText().trim());
            b.setWardId(((Ward) ward.getSelectedItem()).getWardId());
            b.setStatus((String) status.getSelectedItem());
            if (bed == null) dao.insert(b); else dao.update(b);
            AppLogger.info("Bed saved: " + b.getBedNumber());
            dispose();
        } catch (ValidationException ve) {
            UiUtils.error(this, ve.getMessage());
        } catch (Exception ex) {
            AppLogger.error("Saving bed failed", ex);
            UiUtils.error(this, "Could not save bed:\n" + ex.getMessage());
        }
    }
}
