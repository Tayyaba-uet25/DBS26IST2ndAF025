package hms.dao;

import hms.util.DatabaseConnection;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Powers every PDF report. A generic runner turns any SQL result-set
 * into a ReportData (column names + string rows); the methods below
 * supply the queries, several of them parameterized, plus views and a
 * stored procedure.
 */
public class ReportDao {

    /** Generic: run any SELECT and capture columns + rows as strings. */
    private ReportData run(String sql, Object... params) throws SQLException {
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) {
                ps.setObject(i + 1, params[i]);
            }
            try (ResultSet rs = ps.executeQuery()) {
                return extract(rs);
            }
        }
    }

    private ReportData extract(ResultSet rs) throws SQLException {
        ResultSetMetaData md = rs.getMetaData();
        int n = md.getColumnCount();
        String[] cols = new String[n];
        for (int i = 0; i < n; i++) {
            cols[i] = prettify(md.getColumnLabel(i + 1));
        }
        List<String[]> rows = new ArrayList<>();
        while (rs.next()) {
            String[] row = new String[n];
            for (int i = 0; i < n; i++) {
                Object v = rs.getObject(i + 1);
                row[i] = (v == null) ? "" : String.valueOf(v);
            }
            rows.add(row);
        }
        return new ReportData(cols, rows);
    }

    private String prettify(String column) {
        String s = column.replace('_', ' ').trim();
        if (s.isEmpty()) return s;
        StringBuilder sb = new StringBuilder();
        for (String word : s.split(" ")) {
            if (word.isEmpty()) continue;
            sb.append(Character.toUpperCase(word.charAt(0)))
              .append(word.length() > 1 ? word.substring(1) : "")
              .append(' ');
        }
        return sb.toString().trim();
    }

    // ---------------- Report queries ----------------

    public ReportData patients() throws SQLException {
        return run("SELECT patient_id AS 'ID', CONCAT(first_name,' ',last_name) AS 'Name', "
                 + "gender AS 'Gender', date_of_birth AS 'DOB', blood_group AS 'Blood', "
                 + "phone AS 'Phone', email AS 'Email' FROM patients ORDER BY patient_id");
    }

    public ReportData doctors(Integer departmentId) throws SQLException {
        if (departmentId == null) {
            return run("SELECT doctor_id AS 'ID', doctor_name AS 'Doctor', gender AS 'Gender', "
                     + "specialization AS 'Specialization', department AS 'Department', "
                     + "consultation_fee AS 'Fee', status AS 'Status' FROM vw_doctor_directory ORDER BY doctor_id");
        }
        return run("SELECT doctor_id AS 'ID', doctor_name AS 'Doctor', gender AS 'Gender', "
                 + "specialization AS 'Specialization', department AS 'Department', "
                 + "consultation_fee AS 'Fee', status AS 'Status' FROM vw_doctor_directory "
                 + "WHERE department = (SELECT name FROM departments WHERE department_id = ?) ORDER BY doctor_id",
                 departmentId);
    }

    public ReportData appointments(LocalDate from, LocalDate to) throws SQLException {
        return run("SELECT appointment_id AS 'ID', patient_name AS 'Patient', doctor_name AS 'Doctor', "
                 + "department AS 'Department', appointment_date AS 'Date/Time', status AS 'Status', fee AS 'Fee' "
                 + "FROM vw_appointment_details WHERE DATE(appointment_date) BETWEEN ? AND ? ORDER BY appointment_date",
                 java.sql.Date.valueOf(from), java.sql.Date.valueOf(to));
    }

    public ReportData admissions(String status) throws SQLException {
        if (status == null || status.equals("All")) {
            return run("SELECT admission_id AS 'ID', patient_name AS 'Patient', ward_name AS 'Ward', "
                     + "bed_number AS 'Bed', doctor_name AS 'Doctor', admit_date AS 'Admitted', "
                     + "discharge_date AS 'Discharged', status AS 'Status' FROM vw_admission_details ORDER BY admission_id");
        }
        return run("SELECT admission_id AS 'ID', patient_name AS 'Patient', ward_name AS 'Ward', "
                 + "bed_number AS 'Bed', doctor_name AS 'Doctor', admit_date AS 'Admitted', "
                 + "discharge_date AS 'Discharged', status AS 'Status' FROM vw_admission_details "
                 + "WHERE status = ? ORDER BY admission_id", status);
    }

    public ReportData prescriptions(Integer patientId) throws SQLException {
        if (patientId == null) {
            return run("SELECT prescription_id AS 'Rx', patient_name AS 'Patient', doctor_name AS 'Doctor', "
                     + "prescribed_date AS 'Date', medicine AS 'Medicine', dosage AS 'Dosage', "
                     + "quantity AS 'Qty', instructions AS 'Instructions' FROM vw_prescription_details ORDER BY prescription_id");
        }
        return run("SELECT prescription_id AS 'Rx', patient_name AS 'Patient', doctor_name AS 'Doctor', "
                 + "prescribed_date AS 'Date', medicine AS 'Medicine', dosage AS 'Dosage', "
                 + "quantity AS 'Qty', instructions AS 'Instructions' FROM vw_prescription_details pd "
                 + "WHERE pd.prescription_id IN (SELECT prescription_id FROM prescriptions WHERE patient_id = ?) "
                 + "ORDER BY prescription_id", patientId);
    }

    public ReportData medicineStock() throws SQLException {
        return run("SELECT medicine_id AS 'ID', name AS 'Medicine', manufacturer AS 'Manufacturer', "
                 + "unit_price AS 'Unit Price', stock_qty AS 'Stock', reorder_level AS 'Reorder', "
                 + "expiry_date AS 'Expiry' FROM medicines ORDER BY name");
    }

    public ReportData lowStock() throws SQLException {
        return run("SELECT medicine_id AS 'ID', name AS 'Medicine', manufacturer AS 'Manufacturer', "
                 + "stock_qty AS 'Stock', reorder_level AS 'Reorder', unit_price AS 'Unit Price' "
                 + "FROM vw_low_stock_medicines ORDER BY name");
    }

    public ReportData billingSummary() throws SQLException {
        return run("SELECT patient_id AS 'ID', patient_name AS 'Patient', total_bills AS 'Bills', "
                 + "total_billed AS 'Billed', total_paid AS 'Paid', balance AS 'Balance' "
                 + "FROM vw_patient_billing_summary WHERE total_bills > 0 ORDER BY balance DESC");
    }

    public ReportData wardOccupancy() throws SQLException {
        return run("SELECT ward_name AS 'Ward', ward_type AS 'Type', total_beds AS 'Total Beds', "
                 + "occupied_beds AS 'Occupied', available_beds AS 'Available', charge_per_day AS 'Charge/Day' "
                 + "FROM vw_ward_occupancy ORDER BY ward_name");
    }

    public ReportData departmentRevenue() throws SQLException {
        return run("SELECT department AS 'Department', appointments AS 'Appointments', total_fee AS 'Total Fee' "
                 + "FROM vw_revenue_by_department ORDER BY total_fee DESC");
    }

    public ReportData labOrders(String status) throws SQLException {
        String base = "SELECT lo.order_id AS 'ID', CONCAT(p.first_name,' ',p.last_name) AS 'Patient', "
                    + "t.name AS 'Test', CONCAT('Dr. ',d.first_name,' ',d.last_name) AS 'Doctor', "
                    + "lo.order_date AS 'Ordered', lo.status AS 'Status', lo.result AS 'Result' "
                    + "FROM lab_orders lo JOIN patients p ON lo.patient_id=p.patient_id "
                    + "JOIN doctors d ON lo.doctor_id=d.doctor_id JOIN lab_tests t ON lo.test_id=t.test_id ";
        if (status == null || status.equals("All")) {
            return run(base + "ORDER BY lo.order_id DESC");
        }
        return run(base + "WHERE lo.status = ? ORDER BY lo.order_id DESC", status);
    }

    public ReportData dailyCollection(LocalDate day) throws SQLException {
        return run("SELECT pay.payment_id AS 'ID', CONCAT(p.first_name,' ',p.last_name) AS 'Patient', "
                 + "pay.bill_id AS 'Bill', pay.amount AS 'Amount', pay.method AS 'Method', "
                 + "pay.reference_no AS 'Reference', pay.payment_date AS 'Time' "
                 + "FROM payments pay JOIN bills b ON pay.bill_id=b.bill_id "
                 + "JOIN patients p ON b.patient_id=p.patient_id "
                 + "WHERE DATE(pay.payment_date)=? ORDER BY pay.payment_id",
                 java.sql.Date.valueOf(day));
    }

    /** Uses the stored procedure sp_revenue_report (parameterized). */
    public ReportData revenue(LocalDate from, LocalDate to) throws SQLException {
        try (Connection c = DatabaseConnection.getConnection();
             CallableStatement cs = c.prepareCall("{CALL sp_revenue_report(?, ?)}")) {
            cs.setDate(1, java.sql.Date.valueOf(from));
            cs.setDate(2, java.sql.Date.valueOf(to));
            try (ResultSet rs = cs.executeQuery()) {
                return extract(rs);
            }
        }
    }

    public ReportData audit() throws SQLException {
        return run("SELECT log_time AS 'Time', entity AS 'Entity', action AS 'Action', details AS 'Details' "
                 + "FROM audit_log ORDER BY log_id DESC LIMIT 200");
    }
}
