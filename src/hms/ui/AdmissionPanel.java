package hms.ui;

import hms.dao.AdmissionDao;
import hms.model.Admission;
import hms.service.AdmissionService;
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

/** Admissions module: admit (SP), discharge (SP), list. */
public class AdmissionPanel extends JPanel implements Refreshable {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private final AdmissionDao dao = new AdmissionDao();
    private final AdmissionService service = new AdmissionService();
    private final String[] cols = {"ID", "Patient", "Ward / Bed", "Doctor", "Admitted", "Discharged", "Status"};
    private final DefaultTableModel model = new DefaultTableModel(cols, 0) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
    };
    private final JTable table = new JTable(model);
    private List<Admission> data = new ArrayList<>();

    public AdmissionPanel() {
        setLayout(new BorderLayout());
        setBackground(UiUtils.BG);
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        JLabel heading = new JLabel("Admissions");
        heading.setFont(new Font("Segoe UI", Font.BOLD, 20));
        heading.setForeground(UiUtils.PRIMARY_DARK);
        heading.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        add(heading, BorderLayout.NORTH);

        JPanel center = new JPanel(new BorderLayout(0, 8));
        center.setOpaque(false);

        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        toolbar.setOpaque(false);
        JButton admit = button("Admit Patient", UiUtils.ACCENT);
        admit.addActionListener(e -> onAdmit());
        JButton discharge = button("Discharge Selected", new Color(192, 57, 43));
        discharge.addActionListener(e -> onDischarge());
        JButton refresh = button("Refresh", new Color(52, 152, 219));
        refresh.addActionListener(e -> refresh());
        JButton pdf = button("Print PDF", new Color(155, 89, 182));
        pdf.addActionListener(e -> exportPdf());
        toolbar.add(admit); toolbar.add(discharge); toolbar.add(refresh); toolbar.add(pdf);
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
            for (Admission a : data) {
                model.addRow(new Object[]{
                        a.getAdmissionId(), a.getPatientName(), a.getBedInfo(), a.getDoctorName(),
                        a.getAdmitDate() == null ? "" : a.getAdmitDate().format(FMT),
                        a.getDischargeDate() == null ? "-" : a.getDischargeDate().format(FMT),
                        a.getStatus()
                });
            }
        } catch (Exception e) {
            AppLogger.error("Load admissions failed", e);
            UiUtils.error(this, "Could not load admissions:\n" + e.getMessage());
        }
    }

    private void onAdmit() {
        AdmissionDialog d = new AdmissionDialog((java.awt.Frame) SwingUtilities.getWindowAncestor(this));
        d.setVisible(true);
        if (d.isSaved()) refresh();
    }

    private void onDischarge() {
        int row = table.getSelectedRow();
        if (row < 0) { UiUtils.error(this, "Please select an admission to discharge."); return; }
        Admission a = data.get(row);
        if (!"Admitted".equals(a.getStatus())) {
            UiUtils.error(this, "This patient is already discharged.");
            return;
        }
        if (!UiUtils.confirm(this, "Discharge " + a.getPatientName()
                + "? This frees the bed and generates the ward charges bill.")) return;
        try {
            service.dischargePatient(a.getAdmissionId());
            UiUtils.info(this, "Patient discharged. A bill for ward charges has been generated.", "Done");
            refresh();
        } catch (Exception e) {
            AppLogger.error("Discharge failed", e);
            UiUtils.error(this, "Discharge failed:\n" + e.getMessage());
        }
    }

    private void exportPdf() {
        List<String[]> rows = new ArrayList<>();
        for (Admission a : data) {
            rows.add(new String[]{
                    String.valueOf(a.getAdmissionId()), a.getPatientName(), a.getBedInfo(), a.getDoctorName(),
                    a.getAdmitDate() == null ? "" : a.getAdmitDate().format(FMT),
                    a.getDischargeDate() == null ? "-" : a.getDischargeDate().format(FMT), a.getStatus()
            });
        }
        try {
            PdfReportGenerator.buildTableReport("Admissions.pdf", "Admissions Report", null, cols, rows);
        } catch (Exception e) {
            UiUtils.error(this, "Could not create PDF:\n" + e.getMessage());
        }
    }
}
