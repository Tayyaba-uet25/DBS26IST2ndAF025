package hms.dao;

import hms.model.Bed;
import hms.util.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/** Data access for beds. */
public class BedDao {

    private static final String BASE =
            "SELECT b.*, w.name AS ward_name FROM beds b JOIN wards w ON b.ward_id = w.ward_id ";

    public List<Bed> findAll() throws SQLException {
        List<Bed> list = new ArrayList<>();
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(BASE + "ORDER BY w.name, b.bed_number");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(map(rs));
        }
        return list;
    }

    public List<Bed> findAvailable() throws SQLException {
        List<Bed> list = new ArrayList<>();
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(BASE + "WHERE b.status='Available' ORDER BY w.name, b.bed_number");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(map(rs));
        }
        return list;
    }

    public int insert(Bed b) throws SQLException {
        String sql = "INSERT INTO beds(bed_number,ward_id,status) VALUES (?,?,?)";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, b.getBedNumber());
            ps.setInt(2, b.getWardId());
            ps.setString(3, b.getStatus() == null ? "Available" : b.getStatus());
            ps.executeUpdate();
            try (ResultSet k = ps.getGeneratedKeys()) {
                if (k.next()) b.setBedId(k.getInt(1));
            }
            return b.getBedId();
        }
    }

    public void update(Bed b) throws SQLException {
        String sql = "UPDATE beds SET bed_number=?,ward_id=?,status=? WHERE bed_id=?";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, b.getBedNumber());
            ps.setInt(2, b.getWardId());
            ps.setString(3, b.getStatus());
            ps.setInt(4, b.getBedId());
            ps.executeUpdate();
        }
    }

    public void delete(int id) throws SQLException {
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement("DELETE FROM beds WHERE bed_id=?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    private Bed map(ResultSet rs) throws SQLException {
        Bed b = new Bed();
        b.setBedId(rs.getInt("bed_id"));
        b.setBedNumber(rs.getString("bed_number"));
        b.setWardId(rs.getInt("ward_id"));
        b.setWardName(rs.getString("ward_name"));
        b.setStatus(rs.getString("status"));
        return b;
    }
}
