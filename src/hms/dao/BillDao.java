package hms.dao;

import hms.model.Bill;
import hms.model.BillItem;
import hms.util.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/** Read access for bills + items. Creation is done in BillingService (transaction). */
public class BillDao {

    private static final String BASE =
            "SELECT b.*, CONCAT(p.first_name,' ',p.last_name) AS patient_name "
          + "FROM bills b JOIN patients p ON b.patient_id = p.patient_id ";

    public List<Bill> findAll() throws SQLException {
        List<Bill> list = new ArrayList<>();
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(BASE + "ORDER BY b.bill_id DESC");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(map(rs));
        }
        return list;
    }

    public Bill findByIdWithItems(int billId) throws SQLException {
        Bill bill = null;
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(BASE + "WHERE b.bill_id = ?")) {
            ps.setInt(1, billId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) bill = map(rs);
            }
        }
        if (bill == null) return null;

        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement("SELECT * FROM bill_items WHERE bill_id=? ORDER BY bill_item_id")) {
            ps.setInt(1, billId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    BillItem it = new BillItem();
                    it.setBillItemId(rs.getInt("bill_item_id"));
                    it.setBillId(rs.getInt("bill_id"));
                    it.setDescription(rs.getString("description"));
                    it.setItemType(rs.getString("item_type"));
                    it.setQuantity(rs.getInt("quantity"));
                    it.setUnitPrice(rs.getDouble("unit_price"));
                    it.setAmount(rs.getDouble("amount"));
                    bill.getItems().add(it);
                }
            }
        }
        return bill;
    }

    private Bill map(ResultSet rs) throws SQLException {
        Bill b = new Bill();
        b.setBillId(rs.getInt("bill_id"));
        b.setPatientId(rs.getInt("patient_id"));
        b.setPatientName(rs.getString("patient_name"));
        Timestamp ts = rs.getTimestamp("bill_date");
        b.setBillDate(ts == null ? null : ts.toLocalDateTime());
        b.setTotalAmount(rs.getDouble("total_amount"));
        b.setPaidAmount(rs.getDouble("paid_amount"));
        b.setStatus(rs.getString("status"));
        b.setNotes(rs.getString("notes"));
        return b;
    }
}
