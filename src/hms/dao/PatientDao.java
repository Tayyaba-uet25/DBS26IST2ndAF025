package hms.dao;

import hms.model.Patient;
import hms.util.DatabaseConnection;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/** Data access for patients. */
public class PatientDao {

    public List<Patient> findAll() throws SQLException {
        return search("");
    }

    public List<Patient> search(String keyword) throws SQLException {
        // Every word the user types must appear somewhere in the patient's
        // full name / phone / email. So "Ali", "Hassan", "Ali Hassan",
        // "Hassan Ali" (any order), a phone number or an email all work.
        String key = keyword == null ? "" : keyword.trim();
        String[] words = key.isEmpty() ? new String[0] : key.split("\\s+");
        String field = "CONCAT(first_name,' ',last_name,' ',"
                     + "COALESCE(phone,''),' ',COALESCE(email,''))";
        StringBuilder sql = new StringBuilder("SELECT * FROM patients WHERE 1=1");
        for (int i = 0; i < words.length; i++) {
            sql.append(" AND ").append(field).append(" LIKE ?");
        }
        sql.append(" ORDER BY patient_id DESC");

        List<Patient> list = new ArrayList<>();
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql.toString())) {
            for (int i = 0; i < words.length; i++) {
                ps.setString(i + 1, "%" + words[i] + "%");
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(map(rs));
                }
            }
        }
        return list;
    }

    /** True if another patient already uses this email (case-insensitive). */
    public boolean emailExists(String email, int excludeId) throws SQLException {
        if (email == null || email.trim().isEmpty()) return false;
        String sql = "SELECT 1 FROM patients WHERE LOWER(email)=LOWER(?) AND patient_id<>? LIMIT 1";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, email.trim());
            ps.setInt(2, excludeId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    public Patient findById(int id) throws SQLException {
        String sql = "SELECT * FROM patients WHERE patient_id = ?";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return map(rs);
                }
            }
        }
        return null;
    }

    public int insert(Patient p) throws SQLException {
        String sql = "INSERT INTO patients(first_name,last_name,gender,date_of_birth,blood_group,phone,email,address) "
                   + "VALUES (?,?,?,?,?,?,?,?)";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            bind(ps, p);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    p.setPatientId(keys.getInt(1));
                }
            }
            return p.getPatientId();
        }
    }

    public void update(Patient p) throws SQLException {
        String sql = "UPDATE patients SET first_name=?,last_name=?,gender=?,date_of_birth=?,"
                   + "blood_group=?,phone=?,email=?,address=? WHERE patient_id=?";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            bind(ps, p);
            ps.setInt(9, p.getPatientId());
            ps.executeUpdate();
        }
    }

    public void delete(int id) throws SQLException {
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement("DELETE FROM patients WHERE patient_id=?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    private void bind(PreparedStatement ps, Patient p) throws SQLException {
        ps.setString(1, p.getFirstName());
        ps.setString(2, p.getLastName());
        ps.setString(3, p.getGender());
        ps.setDate(4, p.getDateOfBirth() == null ? null : Date.valueOf(p.getDateOfBirth()));
        ps.setString(5, p.getBloodGroup());
        ps.setString(6, p.getPhone());
        ps.setString(7, p.getEmail());
        ps.setString(8, p.getAddress());
    }

    private Patient map(ResultSet rs) throws SQLException {
        Patient p = new Patient();
        p.setPatientId(rs.getInt("patient_id"));
        p.setFirstName(rs.getString("first_name"));
        p.setLastName(rs.getString("last_name"));
        p.setGender(rs.getString("gender"));
        Date dob = rs.getDate("date_of_birth");
        p.setDateOfBirth(dob == null ? null : dob.toLocalDate());
        p.setBloodGroup(rs.getString("blood_group"));
        p.setPhone(rs.getString("phone"));
        p.setEmail(rs.getString("email"));
        p.setAddress(rs.getString("address"));
        return p;
    }
}
