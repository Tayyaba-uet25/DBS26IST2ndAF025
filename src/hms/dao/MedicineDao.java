package hms.dao;

import hms.model.Medicine;
import hms.util.DatabaseConnection;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/** Data access for medicines. */
public class MedicineDao {

    public List<Medicine> findAll() throws SQLException {
        String sql = "SELECT * FROM medicines ORDER BY medicine_id";
        List<Medicine> list = new ArrayList<>();
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(map(rs));
        }
        return list;
    }

    public List<Medicine> findLowStock() throws SQLException {
        String sql = "SELECT * FROM vw_low_stock_medicines ORDER BY name";
        List<Medicine> list = new ArrayList<>();
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Medicine m = new Medicine();
                m.setMedicineId(rs.getInt("medicine_id"));
                m.setName(rs.getString("name"));
                m.setManufacturer(rs.getString("manufacturer"));
                m.setStockQty(rs.getInt("stock_qty"));
                m.setReorderLevel(rs.getInt("reorder_level"));
                m.setUnitPrice(rs.getDouble("unit_price"));
                Date ex = rs.getDate("expiry_date");
                m.setExpiryDate(ex == null ? null : ex.toLocalDate());
                list.add(m);
            }
        }
        return list;
    }

    public int insert(Medicine m) throws SQLException {
        String sql = "INSERT INTO medicines(name,manufacturer,unit_price,stock_qty,reorder_level,expiry_date) "
                   + "VALUES (?,?,?,?,?,?)";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            bind(ps, m);
            ps.executeUpdate();
            try (ResultSet k = ps.getGeneratedKeys()) {
                if (k.next()) m.setMedicineId(k.getInt(1));
            }
            return m.getMedicineId();
        }
    }

    public void update(Medicine m) throws SQLException {
        String sql = "UPDATE medicines SET name=?,manufacturer=?,unit_price=?,stock_qty=?,reorder_level=?,expiry_date=? "
                   + "WHERE medicine_id=?";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            bind(ps, m);
            ps.setInt(7, m.getMedicineId());
            ps.executeUpdate();
        }
    }

    public void delete(int id) throws SQLException {
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement("DELETE FROM medicines WHERE medicine_id=?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    private void bind(PreparedStatement ps, Medicine m) throws SQLException {
        ps.setString(1, m.getName());
        ps.setString(2, m.getManufacturer());
        ps.setDouble(3, m.getUnitPrice());
        ps.setInt(4, m.getStockQty());
        ps.setInt(5, m.getReorderLevel());
        ps.setDate(6, m.getExpiryDate() == null ? null : Date.valueOf(m.getExpiryDate()));
    }

    private Medicine map(ResultSet rs) throws SQLException {
        Medicine m = new Medicine();
        m.setMedicineId(rs.getInt("medicine_id"));
        m.setName(rs.getString("name"));
        m.setManufacturer(rs.getString("manufacturer"));
        m.setUnitPrice(rs.getDouble("unit_price"));
        m.setStockQty(rs.getInt("stock_qty"));
        m.setReorderLevel(rs.getInt("reorder_level"));
        Date ex = rs.getDate("expiry_date");
        m.setExpiryDate(ex == null ? null : ex.toLocalDate());
        return m;
    }
}
