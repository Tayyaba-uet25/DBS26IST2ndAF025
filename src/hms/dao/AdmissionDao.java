package hms.dao;

import hms.model.Admission;
import hms.util.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/** Data access for admissions (read side; admit/discharge go through AdmissionService). */
public class AdmissionDao {

    private static final String BASE =
            "SELECT adm.*, CONCAT(p.first_name,' ',p.last_name) AS patient_name, "
          + "CONCAT(w.name,' / ',bd.bed_number) AS bed_info, "
          + "CONCAT('Dr. ',d.first_name,' ',d.last_name) AS doctor_name "
          + "FROM admissions adm "
          + "JOIN patients p ON adm.patient_id = p.patient_id "
          + "JOIN beds bd ON adm.bed_id = bd.bed_id "
          + "JOIN wards w ON bd.ward_id = w.ward_id "
          + "JOIN doctors d ON adm.doctor_id = d.doctor_id ";

    public List<Admission> findAll() throws SQLException {
        return query(BASE + "ORDER BY adm.admission_id DESC", null);
    }
    public List<Admission> findByStatus(String status) throws SQLException {
        return query(BASE + "WHERE adm.status = ? ORDER BY adm.admission_id DESC", status);
    }

    private List<Admission> query(String sql, String statusParam) throws SQLException {
        List<Admission> list = new ArrayList<>();
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            if (statusParam != null) ps.setString(1, statusParam);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        }
        return list;
    }

    private Admission map(ResultSet rs) throws SQLException {
        Admission a = new Admission();
        a.setAdmissionId(rs.getInt("admission_id"));
        a.setPatientId(rs.getInt("patient_id"));
        a.setPatientName(rs.getString("patient_name"));
        a.setBedId(rs.getInt("bed_id"));
        a.setBedInfo(rs.getString("bed_info"));
        a.setDoctorId(rs.getInt("doctor_id"));
        a.setDoctorName(rs.getString("doctor_name"));
        Timestamp ad = rs.getTimestamp("admit_date");
        a.setAdmitDate(ad == null ? null : ad.toLocalDateTime());
        Timestamp dd = rs.getTimestamp("discharge_date");
        a.setDischargeDate(dd == null ? null : dd.toLocalDateTime());
        a.setStatus(rs.getString("status"));
        a.setDiagnosis(rs.getString("diagnosis"));
        return a;
    }
}
