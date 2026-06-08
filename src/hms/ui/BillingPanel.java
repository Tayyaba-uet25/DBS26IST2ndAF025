package hms.ui;

import hms.dao.BillDao;
import hms.service.BillingService;
import hms.model.Bill;
import hms.model.BillItem;
import hms.util.AppLogger;
import hms.util.PdfReportGenerator;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/** Billing module: bills list, create (transaction), payment (transaction), invoice PDF. */
public class BillingPanel extends JPanel implements Refreshable {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private final BillDao dao = new BillDao();
    private final BillingService service = new BillingService();
    private final String[] cols = {"Bill ID", "Patient", "Date", "Total", "Paid", "Balance", "Status", "Notes"};
    private final DefaultTableModel model = new DefaultTableModel(cols, 0) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
    };
    private final JTable table = new JTable(model);
    private List<Bill> data = new ArrayList<>();

    public BillingPanel() {
        setLayout(new BorderLayout());
        setBackground(UiUtils.BG);
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        JLabel heading = new JLabel("Billing & Payments");
        heading.setFont(new Font("Segoe UI", Font.BOLD, 20));
        heading.setForeground(UiUtils.PRIMARY_DARK);
        heading.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        add(heading, BorderLayout.NORTH);

        JPanel center = new JPanel(new BorderLayout(0, 8));
        center.setOpaque(false);
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        toolbar.setOpaque(false);
        JButton add = button("+ New Bill", UiUtils.ACCENT);
        add.addActionListener(e -> onNew());
        JButton pay = button("Record Payment", new Color(230, 126, 34));
        pay.addActionListener(e -> onPay());
        JButton invoice = button("Print Invoice", new Color(52, 152, 219));
        invoice.addActionListener(e -> onInvoice());
        JButton del = button("Delete Bill", new Color(192, 57, 43));
        del.addActionListener(e -> onDelete());
        JButton refresh = button("Refresh", new Color(127, 140, 141));
        refresh.addActionListener(e -> refresh());
        toolbar.add(add); toolbar.add(pay); toolbar.add(invoice); toolbar.add(del); toolbar.add(refresh);
        center.add(toolbar, BorderLayout.NORTH);

        table.setRowHeight(28);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        center.add(new JScrollPane(table), BorderLayout.CENTER);
        add(center, BorderLayout.CENTER);

        refresh();
    }

    private JButton button(String text, Color bg) {
        JButton b = new JButton(text);
        b.setBackground(bg); b.setForeground(Color.WHITE);
        b.setFocusPainted(false); b.setFont(new Font("Segoe UI", Font.BOLD, 12));
        return b;
    }

    public void refresh() {
        try {
            data = dao.findAll();
            model.setRowCount(0);
            for (Bill b : data) {
                model.addRow(new Object[]{
                        b.getBillId(), b.getPatientName(),
                        b.getBillDate() == null ? "" : b.getBillDate().format(FMT),
                        String.format("%.2f", b.getTotalAmount()),
                        String.format("%.2f", b.getPaidAmount()),
                        String.format("%.2f", b.getBalance()),
                        b.getStatus(),
                        b.getNotes() == null ? "" : b.getNotes()
                });
            }
        } catch (Exception e) {
            AppLogger.error("Load bills failed", e);
            UiUtils.error(this, "Could not load bills:\n" + e.getMessage());
        }
    }

    private void onNew() {
        BillDialog d = new BillDialog((java.awt.Frame) SwingUtilities.getWindowAncestor(this));
        d.setVisible(true);
        if (d.isSaved()) refresh();
    }

    private Bill selected() {
        int row = table.getSelectedRow();
        if (row < 0) { UiUtils.error(this, "Please select a bill first."); return null; }
        return data.get(row);
    }

    private void onPay() {
        Bill b = selected();
        if (b == null) return;
        if ("Paid".equals(b.getStatus())) { UiUtils.info(this, "This bill is already fully paid.", "Info"); return; }
        PaymentDialog d = new PaymentDialog((java.awt.Frame) SwingUtilities.getWindowAncestor(this), b);
        d.setVisible(true);
        if (d.isSaved()) refresh();
    }

    private void onInvoice() {
        Bill b = selected();
        if (b == null) return;
        try {
            Bill full = dao.findByIdWithItems(b.getBillId());
            List<String[]> rows = new ArrayList<>();
            for (BillItem it : full.getItems()) {
                rows.add(new String[]{
                        it.getDescription(), it.getItemType(), String.valueOf(it.getQuantity()),
                        String.format("%.2f", it.getUnitPrice()), String.format("%.2f", it.getAmount())
                });
            }
            rows.add(new String[]{"", "", "", "TOTAL", String.format("%.2f", full.getTotalAmount())});
            rows.add(new String[]{"", "", "", "PAID", String.format("%.2f", full.getPaidAmount())});
            rows.add(new String[]{"", "", "", "BALANCE", String.format("%.2f", full.getBalance())});

            String subtitle = "Bill #" + full.getBillId() + "   |   Patient: " + full.getPatientName()
                    + "   |   Status: " + full.getStatus();
            PdfReportGenerator.buildTableReport("Invoice_Bill_" + full.getBillId() + ".pdf",
                    "Patient Invoice", subtitle,
                    new String[]{"Description", "Type", "Qty", "Unit Price", "Amount"}, rows);
        } catch (Exception e) {
            AppLogger.error("Invoice failed", e);
            UiUtils.error(this, "Could not create invoice:\n" + e.getMessage());
        }
    }

    private void onDelete() {
        Bill b = selected();
        if (b == null) return;
        if (!UiUtils.confirm(this,
                "Delete Bill #" + b.getBillId() + " (" + b.getPatientName() + ")?\n\n"
              + "Is bill ki SAARI payments aur items bhi delete ho jayenge.\n"
              + "Yeh wapas nahi ho sakta.")) {
            return;
        }
        try {
            service.deleteBill(b.getBillId());
            AppLogger.info("Bill #" + b.getBillId() + " deleted from Billing screen");
            refresh();
            UiUtils.info(this, "Bill deleted.", "Done");
        } catch (Exception e) {
            AppLogger.error("Delete bill failed", e);
            UiUtils.error(this, "Could not delete bill:\n" + e.getMessage());
        }
    }
}
