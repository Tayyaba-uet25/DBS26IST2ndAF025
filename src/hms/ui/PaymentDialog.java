package hms.ui;

import hms.model.Bill;
import hms.service.PaymentService;
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

/** Record a payment against a bill (transaction). */
public class PaymentDialog extends JDialog {

    private final PaymentService service = new PaymentService();
    private final Bill bill;
    private final JTextField amount = new JTextField(12);
    private final JComboBox<String> method = new JComboBox<>(new String[]{"Cash", "Card", "Online"});
    private final JTextField reference = new JTextField(12);
    private boolean saved = false;

    public PaymentDialog(Frame owner, Bill bill) {
        super(owner, true);
        this.bill = bill;
        setTitle("Record Payment - Bill #" + bill.getBillId());
        setSize(440, 330);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout());
        setJMenuBar(UiUtils.buildMenuBar(null, e -> dispose(), "Close"));

        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(Color.WHITE);
        p.setBorder(BorderFactory.createEmptyBorder(14, 18, 14, 18));
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(7, 8, 7, 8);
        g.anchor = GridBagConstraints.WEST; g.fill = GridBagConstraints.HORIZONTAL;

        info(p, g, 0, "Patient:", bill.getPatientName());
        info(p, g, 1, "Total:", String.format("Rs %.2f", bill.getTotalAmount()));
        info(p, g, 2, "Already Paid:", String.format("Rs %.2f", bill.getPaidAmount()));
        info(p, g, 3, "Balance:", String.format("Rs %.2f", bill.getBalance()));

        g.gridx=0; g.gridy=4; p.add(new JLabel("Amount *"), g);
        g.gridx=1; g.gridy=4; g.weightx=1; p.add(amount, g);
        g.gridx=0; g.gridy=5; g.weightx=0; p.add(new JLabel("Method"), g);
        g.gridx=1; g.gridy=5; p.add(method, g);
        g.gridx=0; g.gridy=6; p.add(new JLabel("Reference No"), g);
        g.gridx=1; g.gridy=6; p.add(reference, g);
        add(p, BorderLayout.CENTER);

        amount.setText(String.format("%.2f", bill.getBalance()));

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
        btns.setBackground(new Color(240, 242, 245));
        JButton save = new JButton("Record Payment");
        save.setBackground(UiUtils.ACCENT); save.setForeground(Color.WHITE); save.setFocusPainted(false);
        save.addActionListener(e -> onSave());
        JButton cancel = new JButton("Cancel");
        cancel.addActionListener(e -> dispose());
        btns.add(cancel); btns.add(save);
        add(btns, BorderLayout.SOUTH);
    }

    private void info(JPanel p, GridBagConstraints g, int r, String label, String value) {
        g.gridx=0; g.gridy=r; g.weightx=0;
        JLabel l = new JLabel(label); l.setForeground(new Color(100,100,100));
        p.add(l, g);
        g.gridx=1; g.gridy=r; g.weightx=1;
        JLabel v = new JLabel(value); v.setFont(v.getFont().deriveFont(java.awt.Font.BOLD));
        p.add(v, g);
    }

    private void onSave() {
        try {
            double amt = ValidationUtil.positiveOrZeroNumber(amount.getText(), "Amount");
            if (amt <= 0) throw new ValidationException("Amount must be greater than zero.");
            service.recordPayment(bill.getBillId(), amt,
                    (String) method.getSelectedItem(),
                    reference.getText().trim().isEmpty() ? null : reference.getText().trim());
            saved = true;
            UiUtils.info(this, "Payment recorded successfully.", "Success");
            dispose();
        } catch (ValidationException ve) {
            UiUtils.error(this, ve.getMessage());
        } catch (Exception ex) {
            AppLogger.error("Recording payment failed", ex);
            UiUtils.error(this, "Could not record payment:\n" + ex.getMessage());
        }
    }

    public boolean isSaved() { return saved; }
}
