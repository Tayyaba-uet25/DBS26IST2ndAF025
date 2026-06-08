package hms.dao;

import hms.model.LabTest;
import hms.util.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/** Data access for lab test catalog. */
public class LabTestDao {

    public List<LabTest> findAll() throws SQLException {
        String sql = "SELECT * FROM lab_tests ORDER BY name";
        List<LabTest> list = new ArrayList<>();
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                LabTest t = new LabTest();
                t.setTestId(rs.getInt("test_id"));
                t.setName(rs.getString("name"));
                t.setPrice(rs.getDouble("price"));
                t.setDescription(rs.getString("description"));
                list.add(t);
            }
        }
        return list;
    }

    public int insert(LabTest t) throws SQLException {
        String sql = "INSERT INTO lab_tests(name,price,description) VALUES (?,?,?)";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, t.getName());
            ps.setDouble(2, t.getPrice());
            ps.setString(3, t.getDescription());
            ps.executeUpdate();
            try (ResultSet k = ps.getGeneratedKeys()) {
                if (k.next()) t.setTestId(k.getInt(1));
            }
            return t.getTestId();
        }
    }

    public void update(LabTest t) throws SQLException {
        String sql = "UPDATE lab_tests SET name=?,price=?,description=? WHERE test_id=?";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, t.getName());
            ps.setDouble(2, t.getPrice());
            ps.setString(3, t.getDescription());
            ps.setInt(4, t.getTestId());
            ps.executeUpdate();
        }
    }

    public void delete(int id) throws SQLException {
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement("DELETE FROM lab_tests WHERE test_id=?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }
}
