package hms.ui;

import hms.dao.PrescriptionDao;
import hms.model.Prescription;
import hms.model.PrescriptionItem;
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

/** Prescriptions module: list + create (transaction) + view items + PDF. */
public class PrescriptionPanel extends JPanel implements Refreshable {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private final PrescriptionDao dao = new PrescriptionDao();
    private final String[] cols = {"Rx ID", "Patient", "Doctor", "Date", "Notes"};
    private final DefaultTableModel model = new DefaultTableModel(cols, 0) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
    };
    private final JTable table = new JTable(model);
    private List<Prescription> data = new ArrayList<>();

    public PrescriptionPanel() {
        setLayout(new BorderLayout());
        setBackground(UiUtils.BG);
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        JLabel heading = new JLabel("Prescriptions");
        heading.setFont(new Font("Segoe UI", Font.BOLD, 20));
        heading.setForeground(UiUtils.PRIMARY_DARK);
        heading.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        add(heading, BorderLayout.NORTH);

        JPanel center = new JPanel(new BorderLayout(0, 8));
        center.setOpaque(false);
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        toolbar.setOpaque(false);
        JButton add = button("+ New Prescription", UiUtils.ACCENT);
        add.addActionListener(e -> onNew());
        JButton view = button("View Items", new Color(52, 152, 219));
        view.addActionListener(e -> onView());
        JButton refresh = button("Refresh", new Color(127, 140, 141));
        refresh.addActionListener(e -> refresh());
        JButton pdf = button("Print PDF", new Color(155, 89, 182));
        pdf.addActionListener(e -> exportPdf());
        toolbar.add(add); toolbar.add(view); toolbar.add(refresh); toolbar.add(pdf);
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
            for (Prescription pr : data) {
                model.addRow(new Object[]{
                        pr.getPrescriptionId(), pr.getPatientName(), pr.getDoctorName(),
                        pr.getPrescribedDate() == null ? "" : pr.getPrescribedDate().format(FMT),
                        pr.getNotes() == null ? "" : pr.getNotes()
                });
            }
        } catch (Exception e) {
            AppLogger.error("Load prescriptions failed", e);
            UiUtils.error(this, "Could not load prescriptions:\n" + e.getMessage());
        }
    }

    private void onNew() {
        PrescriptionDialog d = new PrescriptionDialog((java.awt.Frame) SwingUtilities.getWindowAncestor(this));
        d.setVisible(true);
        if (d.isSaved()) refresh();
    }

    private void onView() {
        int row = table.getSelectedRow();
        if (row < 0) { UiUtils.error(this, "Please select a prescription."); return; }
        try {
            Prescription pr = dao.findByIdWithItems(data.get(row).getPrescriptionId());
            StringBuilder sb = new StringBuilder();
            sb.append("Prescription #").append(pr.getPrescriptionId()).append("\n")
              .append("Patient: ").append(pr.getPatientName()).append("\n")
              .append("Doctor: ").append(pr.getDoctorName()).append("\n")
              .append("Notes: ").append(pr.getNotes() == null ? "-" : pr.getNotes()).append("\n\n")
              .append("Medicines:\n");
            for (PrescriptionItem it : pr.getItems()) {
                sb.append("  - ").append(it.getMedicineName())
                  .append("  | ").append(it.getDosage())
                  .append("  | Qty: ").append(it.getQuantity())
                  .append("  | ").append(it.getInstructions() == null ? "" : it.getInstructions())
                  .append("\n");
            }
            UiUtils.info(this, sb.toString(), "Prescription Details");
        } catch (Exception e) {
            UiUtils.error(this, "Could not load items:\n" + e.getMessage());
        }
    }

    private void exportPdf() {
        List<String[]> rows = new ArrayList<>();
        for (Prescription pr : data) {
            rows.add(new String[]{
                    String.valueOf(pr.getPrescriptionId()), pr.getPatientName(), pr.getDoctorName(),
                    pr.getPrescribedDate() == null ? "" : pr.getPrescribedDate().format(FMT),
                    pr.getNotes() == null ? "" : pr.getNotes()
            });
        }
        try {
            PdfReportGenerator.buildTableReport("Prescriptions.pdf", "Prescriptions Report", null, cols, rows);
        } catch (Exception e) {
            UiUtils.error(this, "Could not create PDF:\n" + e.getMessage());
        }
    }
}
