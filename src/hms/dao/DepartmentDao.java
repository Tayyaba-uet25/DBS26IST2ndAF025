package hms.dao;

import hms.model.Department;
import hms.util.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/** Data access for departments. */
public class DepartmentDao {

    public List<Department> findAll() throws SQLException {
        String sql = "SELECT * FROM departments ORDER BY department_id";
        List<Department> list = new ArrayList<>();
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(map(rs));
            }
        }
        return list;
    }

    public int insert(Department d) throws SQLException {
        String sql = "INSERT INTO departments(name,location,phone) VALUES (?,?,?)";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, d.getName());
            ps.setString(2, d.getLocation());
            ps.setString(3, d.getPhone());
            ps.executeUpdate();
            try (ResultSet k = ps.getGeneratedKeys()) {
                if (k.next()) d.setDepartmentId(k.getInt(1));
            }
            return d.getDepartmentId();
        }
    }

    public void update(Department d) throws SQLException {
        String sql = "UPDATE departments SET name=?,location=?,phone=? WHERE department_id=?";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, d.getName());
            ps.setString(2, d.getLocation());
            ps.setString(3, d.getPhone());
            ps.setInt(4, d.getDepartmentId());
            ps.executeUpdate();
        }
    }

    public void delete(int id) throws SQLException {
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement("DELETE FROM departments WHERE department_id=?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    /** True if another department already uses this phone number. */
    public boolean phoneExists(String phone, int excludeId) throws SQLException {
        if (phone == null || phone.trim().isEmpty()) return false;
        String sql = "SELECT 1 FROM departments WHERE phone=? AND department_id<>? LIMIT 1";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, phone.trim());
            ps.setInt(2, excludeId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    private Department map(ResultSet rs) throws SQLException {
        Department d = new Department();
        d.setDepartmentId(rs.getInt("department_id"));
        d.setName(rs.getString("name"));
        d.setLocation(rs.getString("location"));
        d.setPhone(rs.getString("phone"));
        return d;
    }
}
