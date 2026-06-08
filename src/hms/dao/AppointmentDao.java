package hms.dao;

import hms.model.Appointment;
import hms.util.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/** Data access for appointments. */
public class AppointmentDao {

    private static final String BASE =
            "SELECT a.*, CONCAT(p.first_name,' ',p.last_name) AS patient_name, "
          + "CONCAT('Dr. ',d.first_name,' ',d.last_name) AS doctor_name "
          + "FROM appointments a "
          + "JOIN patients p ON a.patient_id = p.patient_id "
          + "JOIN doctors d ON a.doctor_id = d.doctor_id ";

    public List<Appointment> findAll() throws SQLException {
        List<Appointment> list = new ArrayList<>();
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(BASE + "ORDER BY a.appointment_date DESC");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(map(rs));
        }
        return list;
    }

    public List<Appointment> findBetween(LocalDate from, LocalDate to) throws SQLException {
        List<Appointment> list = new ArrayList<>();
        String sql = BASE + "WHERE DATE(a.appointment_date) BETWEEN ? AND ? ORDER BY a.appointment_date";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setDate(1, java.sql.Date.valueOf(from));
            ps.setDate(2, java.sql.Date.valueOf(to));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        }
        return list;
    }

    public int insert(Appointment a) throws SQLException {
        String sql = "INSERT INTO appointments(patient_id,doctor_id,appointment_date,reason,status,fee) "
                   + "VALUES (?,?,?,?,?,?)";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            bind(ps, a);
            ps.executeUpdate();
            try (ResultSet k = ps.getGeneratedKeys()) {
                if (k.next()) a.setAppointmentId(k.getInt(1));
            }
            return a.getAppointmentId();
        }
    }

    public void update(Appointment a) throws SQLException {
        String sql = "UPDATE appointments SET patient_id=?,doctor_id=?,appointment_date=?,reason=?,status=?,fee=? "
                   + "WHERE appointment_id=?";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            bind(ps, a);
            ps.setInt(7, a.getAppointmentId());
            ps.executeUpdate();
        }
    }

    public void delete(int id) throws SQLException {
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement("DELETE FROM appointments WHERE appointment_id=?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    private void bind(PreparedStatement ps, Appointment a) throws SQLException {
        ps.setInt(1, a.getPatientId());
        ps.setInt(2, a.getDoctorId());
        ps.setTimestamp(3, Timestamp.valueOf(a.getAppointmentDate()));
        ps.setString(4, a.getReason());
        ps.setString(5, a.getStatus());
        ps.setDouble(6, a.getFee());
    }

    private Appointment map(ResultSet rs) throws SQLException {
        Appointment a = new Appointment();
        a.setAppointmentId(rs.getInt("appointment_id"));
        a.setPatientId(rs.getInt("patient_id"));
        a.setPatientName(rs.getString("patient_name"));
        a.setDoctorId(rs.getInt("doctor_id"));
        a.setDoctorName(rs.getString("doctor_name"));
        Timestamp ts = rs.getTimestamp("appointment_date");
        a.setAppointmentDate(ts == null ? null : ts.toLocalDateTime());
        a.setReason(rs.getString("reason"));
        a.setStatus(rs.getString("status"));
        a.setFee(rs.getDouble("fee"));
        return a;
    }
}
