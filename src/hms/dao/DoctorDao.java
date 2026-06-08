package hms.dao;

import hms.model.Doctor;
import hms.util.DatabaseConnection;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

/** Data access for doctors. */
public class DoctorDao {

    private static final String BASE =
            "SELECT d.*, dep.name AS dept_name FROM doctors d "
          + "LEFT JOIN departments dep ON d.department_id = dep.department_id ";

    public List<Doctor> findAll() throws SQLException {
        List<Doctor> list = new ArrayList<>();
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(BASE + "ORDER BY d.doctor_id DESC");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(map(rs));
        }
        return list;
    }

    public List<Doctor> findByDepartment(int departmentId) throws SQLException {
        List<Doctor> list = new ArrayList<>();
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(BASE + "WHERE d.department_id = ? ORDER BY d.first_name")) {
            ps.setInt(1, departmentId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        }
        return list;
    }

    public int insert(Doctor d) throws SQLException {
        String sql = "INSERT INTO doctors(first_name,last_name,gender,specialization,department_id,"
                   + "phone,email,consultation_fee,hire_date,is_active) VALUES (?,?,?,?,?,?,?,?,?,?)";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            bind(ps, d);
            ps.executeUpdate();
            try (ResultSet k = ps.getGeneratedKeys()) {
                if (k.next()) d.setDoctorId(k.getInt(1));
            }
            return d.getDoctorId();
        }
    }

    public void update(Doctor d) throws SQLException {
        String sql = "UPDATE doctors SET first_name=?,last_name=?,gender=?,specialization=?,department_id=?,"
                   + "phone=?,email=?,consultation_fee=?,hire_date=?,is_active=? WHERE doctor_id=?";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            bind(ps, d);
            ps.setInt(11, d.getDoctorId());
            ps.executeUpdate();
        }
    }

    public void delete(int id) throws SQLException {
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement("DELETE FROM doctors WHERE doctor_id=?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    private void bind(PreparedStatement ps, Doctor d) throws SQLException {
        ps.setString(1, d.getFirstName());
        ps.setString(2, d.getLastName());
        ps.setString(3, d.getGender());
        ps.setString(4, d.getSpecialization());
        if (d.getDepartmentId() > 0) ps.setInt(5, d.getDepartmentId()); else ps.setNull(5, Types.INTEGER);
        ps.setString(6, d.getPhone());
        ps.setString(7, d.getEmail());
        ps.setDouble(8, d.getConsultationFee());
        ps.setDate(9, d.getHireDate() == null ? null : Date.valueOf(d.getHireDate()));
        ps.setInt(10, d.isActive() ? 1 : 0);
    }

    private Doctor map(ResultSet rs) throws SQLException {
        Doctor d = new Doctor();
        d.setDoctorId(rs.getInt("doctor_id"));
        d.setFirstName(rs.getString("first_name"));
        d.setLastName(rs.getString("last_name"));
        d.setGender(rs.getString("gender"));
        d.setSpecialization(rs.getString("specialization"));
        d.setDepartmentId(rs.getInt("department_id"));
        d.setDepartmentName(rs.getString("dept_name"));
        d.setPhone(rs.getString("phone"));
        d.setEmail(rs.getString("email"));
        d.setConsultationFee(rs.getDouble("consultation_fee"));
        Date hd = rs.getDate("hire_date");
        d.setHireDate(hd == null ? null : hd.toLocalDate());
        d.setActive(rs.getInt("is_active") == 1);
        return d;
    }
}
