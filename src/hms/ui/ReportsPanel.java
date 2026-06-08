package hms.ui;

import com.toedter.calendar.JDateChooser;
import hms.dao.DepartmentDao;
import hms.dao.PatientDao;
import hms.dao.ReportData;
import hms.dao.ReportDao;
import hms.model.Department;
import hms.model.Patient;
import hms.util.AppLogger;
import hms.util.PdfReportGenerator;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.time.LocalDate;
import java.util.function.Supplier;

/**
 * Reports module. 14 business reports, 7 of them parameterized (date
 * ranges, single dates, department / patient / status filters). Uses
 * views and the sp_revenue_report stored procedure under the hood.
 */
public class ReportsPanel extends JPanel {

    private final ReportDao dao = new ReportDao();

    public ReportsPanel() {
        setLayout(new BorderLayout());
        setBackground(UiUtils.BG);
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        JLabel heading = new JLabel("Reports  (PDF, parameter-based)");
        heading.setFont(new Font("Segoe UI", Font.BOLD, 20));
        heading.setForeground(UiUtils.PRIMARY_DARK);
        heading.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        add(heading, BorderLayout.NORTH);

        JPanel list = new JPanel();
        list.setLayout(new BoxLayout(list, BoxLayout.Y_AXIS));
        list.setBackground(UiUtils.BG);

        // Reports shown strictly in number order (1 -> 14)
        list.add(simpleRow("1. Patients List", "Patients", () -> safe(dao::patients)));
        list.add(doctorsByDeptRow());                                   // 2
        list.add(appointmentsRow());                                    // 3
        list.add(admissionsRow());                                      // 4
        list.add(prescriptionsRow());                                   // 5
        list.add(simpleRow("6. Medicine Stock Report", "Medicine_Stock", () -> safe(dao::medicineStock)));
        list.add(simpleRow("7. Low Stock Medicines (view)", "Low_Stock", () -> safe(dao::lowStock)));
        list.add(simpleRow("8. Patient Billing Summary (view)", "Billing_Summary", () -> safe(dao::billingSummary)));
        list.add(simpleRow("9. Ward Occupancy (view)", "Ward_Occupancy", () -> safe(dao::wardOccupancy)));
        list.add(simpleRow("10. Department Revenue (view)", "Department_Revenue", () -> safe(dao::departmentRevenue)));
        list.add(labOrdersRow());                                       // 11
        list.add(dailyCollectionRow());                                 // 12
        list.add(revenueRow());                                         // 13
        list.add(simpleRow("14. Audit Trail", "Audit_Trail", () -> safe(dao::audit)));

        JScrollPane scroll = new JScrollPane(list);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        scroll.setBorder(null);
        add(scroll, BorderLayout.CENTER);
    }

    // ---- helpers ----

    private ReportData safe(DataSupplierThrows s) {
        try {
            return s.get();
        } catch (Exception e) {
            AppLogger.error("Report query failed", e);
            UiUtils.error(this, "Report failed:\n" + e.getMessage());
            return null;
        }
    }

    private interface DataSupplierThrows { ReportData get() throws Exception; }

    private void generate(String file, String title, String subtitle, ReportData rd) {
        if (rd == null) return;
        try {
            PdfReportGenerator.buildTableReport(file + ".pdf", title, subtitle, rd.columns, rd.rows);
        } catch (Exception e) {
            AppLogger.error("PDF build failed", e);
            UiUtils.error(this, "Could not create PDF:\n" + e.getMessage());
        }
    }

    private JPanel baseRow(String title) {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 6));
        row.setBackground(Color.WHITE);
        row.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(225, 225, 225)),
                BorderFactory.createEmptyBorder(4, 8, 4, 8)));
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 56));
        JLabel l = new JLabel(title);
        l.setPreferredSize(new Dimension(300, 24));
        l.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        row.add(l);
        return row;
    }

    private JButton genButton() {
        JButton b = new JButton("Generate PDF");
        b.setBackground(UiUtils.PRIMARY);
        b.setForeground(Color.WHITE);
        b.setFocusPainted(false);
        b.setFont(new Font("Segoe UI", Font.BOLD, 12));
        return b;
    }

    private JPanel simpleRow(String title, String file, Supplier<ReportData> supplier) {
        JPanel row = baseRow(title);
        JButton gen = genButton();
        gen.addActionListener(e -> generate(file, title.replaceFirst("^\\d+\\.\\s*", ""), null, supplier.get()));
        row.add(gen);
        return row;
    }

    private JDateChooser dateChooser(LocalDate initial) {
        JDateChooser dc = new JDateChooser();
        dc.setDateFormatString("yyyy-MM-dd");
        dc.setPreferredSize(new Dimension(130, 26));
        dc.setDate(UiUtils.toDate(initial));
        return dc;
    }

    private JPanel doctorsByDeptRow() {
        JPanel row = baseRow("2. Doctors by Department");
        JComboBox<Department> dept = new JComboBox<>();
        dept.addItem(null);   // "All"
        try { for (Department d : new DepartmentDao().findAll()) dept.addItem(d); }
        catch (Exception e) { AppLogger.error("load dept", e); }
        row.add(new JLabel("Department:"));
        row.add(dept);
        JButton gen = genButton();
        gen.addActionListener(e -> {
            Department d = (Department) dept.getSelectedItem();
            Integer id = (d == null) ? null : d.getDepartmentId();
            String sub = "Department: " + (d == null ? "All" : d.getName());
            generate("Doctors_By_Department", "Doctors Report", sub, safe(() -> dao.doctors(id)));
        });
        row.add(gen);
        return row;
    }

    private JPanel appointmentsRow() {
        JPanel row = baseRow("3. Appointments (date range)");
        JDateChooser from = dateChooser(LocalDate.now().minusDays(30));
        JDateChooser to = dateChooser(LocalDate.now().plusDays(30));
        row.add(new JLabel("From:")); row.add(from);
        row.add(new JLabel("To:")); row.add(to);
        JButton gen = genButton();
        gen.addActionListener(e -> {
            LocalDate f = UiUtils.toLocalDate(from.getDate());
            LocalDate t = UiUtils.toLocalDate(to.getDate());
            if (f == null || t == null) { UiUtils.error(this, "Please pick both dates."); return; }
            generate("Appointments", "Appointments Report", "From " + f + " to " + t,
                    safe(() -> dao.appointments(f, t)));
        });
        row.add(gen);
        return row;
    }

    private JPanel admissionsRow() {
        JPanel row = baseRow("4. Admissions (by status)");
        JComboBox<String> status = new JComboBox<>(new String[]{"All", "Admitted", "Discharged"});
        row.add(new JLabel("Status:")); row.add(status);
        JButton gen = genButton();
        gen.addActionListener(e -> {
            String s = (String) status.getSelectedItem();
            generate("Admissions", "Admissions Report", "Status: " + s,
                    safe(() -> dao.admissions(s)));
        });
        row.add(gen);
        return row;
    }

    private JPanel prescriptionsRow() {
        JPanel row = baseRow("5. Prescriptions (by patient)");
        JComboBox<Patient> patient = new JComboBox<>();
        patient.addItem(null);
        try { for (Patient p : new PatientDao().findAll()) patient.addItem(p); }
        catch (Exception e) { AppLogger.error("load patients", e); }
        row.add(new JLabel("Patient:")); row.add(patient);
        JButton gen = genButton();
        gen.addActionListener(e -> {
            Patient p = (Patient) patient.getSelectedItem();
            Integer id = (p == null) ? null : p.getPatientId();
            generate("Prescriptions", "Prescriptions Report",
                    "Patient: " + (p == null ? "All" : p.getFullName()),
                    safe(() -> dao.prescriptions(id)));
        });
        row.add(gen);
        return row;
    }

    private JPanel labOrdersRow() {
        JPanel row = baseRow("11. Lab Orders (by status)");
        JComboBox<String> status = new JComboBox<>(new String[]{"All", "Pending", "Completed", "Cancelled"});
        row.add(new JLabel("Status:")); row.add(status);
        JButton gen = genButton();
        gen.addActionListener(e -> {
            String s = (String) status.getSelectedItem();
            generate("Lab_Orders", "Lab Orders Report", "Status: " + s, safe(() -> dao.labOrders(s)));
        });
        row.add(gen);
        return row;
    }

    private JPanel dailyCollectionRow() {
        JPanel row = baseRow("12. Daily Collection (by date)");
        JDateChooser day = dateChooser(LocalDate.now());
        row.add(new JLabel("Date:")); row.add(day);
        JButton gen = genButton();
        gen.addActionListener(e -> {
            LocalDate d = UiUtils.toLocalDate(day.getDate());
            if (d == null) { UiUtils.error(this, "Please pick a date."); return; }
            generate("Daily_Collection", "Daily Collection Report", "Date: " + d,
                    safe(() -> dao.dailyCollection(d)));
        });
        row.add(gen);
        return row;
    }

    private JPanel revenueRow() {
        JPanel row = baseRow("13. Revenue (stored procedure, date range)");
        JDateChooser from = dateChooser(LocalDate.now().minusDays(30));
        JDateChooser to = dateChooser(LocalDate.now());
        row.add(new JLabel("From:")); row.add(from);
        row.add(new JLabel("To:")); row.add(to);
        JButton gen = genButton();
        gen.addActionListener(e -> {
            LocalDate f = UiUtils.toLocalDate(from.getDate());
            LocalDate t = UiUtils.toLocalDate(to.getDate());
            if (f == null || t == null) { UiUtils.error(this, "Please pick both dates."); return; }
            generate("Revenue", "Revenue Report (sp_revenue_report)", "From " + f + " to " + t,
                    safe(() -> dao.revenue(f, t)));
        });
        row.add(gen);
        return row;
    }
}
