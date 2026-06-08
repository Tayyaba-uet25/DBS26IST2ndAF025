package hms.dao;

import hms.model.Ward;
import hms.util.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/** Data access for wards. */
public class WardDao {

    public List<Ward> findAll() throws SQLException {
        String sql = "SELECT * FROM wards ORDER BY name";
        List<Ward> list = new ArrayList<>();
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(map(rs));
        }
        return list;
    }

    public int insert(Ward w) throws SQLException {
        String sql = "INSERT INTO wards(name,ward_type,charge_per_day) VALUES (?,?,?)";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, w.getName());
            ps.setString(2, w.getWardType());
            ps.setDouble(3, w.getChargePerDay());
            ps.executeUpdate();
            try (ResultSet k = ps.getGeneratedKeys()) {
                if (k.next()) w.setWardId(k.getInt(1));
            }
            return w.getWardId();
        }
    }

    public void update(Ward w) throws SQLException {
        String sql = "UPDATE wards SET name=?,ward_type=?,charge_per_day=? WHERE ward_id=?";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, w.getName());
            ps.setString(2, w.getWardType());
            ps.setDouble(3, w.getChargePerDay());
            ps.setInt(4, w.getWardId());
            ps.executeUpdate();
        }
    }

    public void delete(int id) throws SQLException {
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement("DELETE FROM wards WHERE ward_id=?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    private Ward map(ResultSet rs) throws SQLException {
        Ward w = new Ward();
        w.setWardId(rs.getInt("ward_id"));
        w.setName(rs.getString("name"));
        w.setWardType(rs.getString("ward_type"));
        w.setChargePerDay(rs.getDouble("charge_per_day"));
        return w;
    }
}
