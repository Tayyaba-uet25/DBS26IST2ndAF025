package hms.dao;

import hms.model.LabOrder;
import hms.util.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/** Data access for lab orders. */
public class LabOrderDao {

    private static final String BASE =
            "SELECT lo.*, CONCAT(p.first_name,' ',p.last_name) AS patient_name, "
          + "CONCAT('Dr. ',d.first_name,' ',d.last_name) AS doctor_name, t.name AS test_name "
          + "FROM lab_orders lo "
          + "JOIN patients p ON lo.patient_id=p.patient_id "
          + "JOIN doctors d ON lo.doctor_id=d.doctor_id "
          + "JOIN lab_tests t ON lo.test_id=t.test_id ";

    public List<LabOrder> findAll() throws SQLException {
        return query(BASE + "ORDER BY lo.order_id DESC", null);
    }

    public List<LabOrder> findByStatus(String status) throws SQLException {
        return query(BASE + "WHERE lo.status=? ORDER BY lo.order_id DESC", status);
    }

    private List<LabOrder> query(String sql, String status) throws SQLException {
        List<LabOrder> list = new ArrayList<>();
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            if (status != null) ps.setString(1, status);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        }
        return list;
    }

    public int insert(LabOrder o) throws SQLException {
        String sql = "INSERT INTO lab_orders(patient_id,doctor_id,test_id,order_date,status,result) "
                   + "VALUES (?,?,?,?,?,?)";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, o.getPatientId());
            ps.setInt(2, o.getDoctorId());
            ps.setInt(3, o.getTestId());
            // set the order time from Java (local clock) so the displayed
            // time matches when the order was actually placed
            ps.setTimestamp(4, Timestamp.valueOf(java.time.LocalDateTime.now()));
            ps.setString(5, o.getStatus());
            ps.setString(6, o.getResult());
            ps.executeUpdate();
            try (ResultSet k = ps.getGeneratedKeys()) {
                if (k.next()) o.setOrderId(k.getInt(1));
            }
            return o.getOrderId();
        }
    }

    public void update(LabOrder o) throws SQLException {
        String sql = "UPDATE lab_orders SET patient_id=?,doctor_id=?,test_id=?,status=?,result=? WHERE order_id=?";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, o.getPatientId());
            ps.setInt(2, o.getDoctorId());
            ps.setInt(3, o.getTestId());
            ps.setString(4, o.getStatus());
            ps.setString(5, o.getResult());
            ps.setInt(6, o.getOrderId());
            ps.executeUpdate();
        }
    }

    public void delete(int id) throws SQLException {
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement("DELETE FROM lab_orders WHERE order_id=?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    private LabOrder map(ResultSet rs) throws SQLException {
        LabOrder o = new LabOrder();
        o.setOrderId(rs.getInt("order_id"));
        o.setPatientId(rs.getInt("patient_id"));
        o.setPatientName(rs.getString("patient_name"));
        o.setDoctorId(rs.getInt("doctor_id"));
        o.setDoctorName(rs.getString("doctor_name"));
        o.setTestId(rs.getInt("test_id"));
        o.setTestName(rs.getString("test_name"));
        Timestamp ts = rs.getTimestamp("order_date");
        o.setOrderDate(ts == null ? null : ts.toLocalDateTime());
        o.setStatus(rs.getString("status"));
        o.setResult(rs.getString("result"));
        return o;
    }
}
