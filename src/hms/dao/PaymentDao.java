package hms.dao;

import hms.model.Payment;
import hms.util.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/** Read access for payments. Recording a payment goes through PaymentService (stored procedure). */
public class PaymentDao {

    public List<Payment> findByBill(int billId) throws SQLException {
        List<Payment> list = new ArrayList<>();
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement("SELECT * FROM payments WHERE bill_id=? ORDER BY payment_id")) {
            ps.setInt(1, billId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        }
        return list;
    }

    public List<Payment> findByDate(LocalDate day) throws SQLException {
        List<Payment> list = new ArrayList<>();
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement("SELECT * FROM payments WHERE DATE(payment_date)=? ORDER BY payment_id")) {
            ps.setDate(1, java.sql.Date.valueOf(day));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        }
        return list;
    }

    /**
     * Delete a single payment. The database trigger trg_payment_after_delete
     * automatically recomputes the parent bill's paid amount and status.
     */
    public void delete(int paymentId) throws SQLException {
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement("DELETE FROM payments WHERE payment_id = ?")) {
            ps.setInt(1, paymentId);
            ps.executeUpdate();
        }
    }

    private Payment map(ResultSet rs) throws SQLException {
        Payment p = new Payment();
        p.setPaymentId(rs.getInt("payment_id"));
        p.setBillId(rs.getInt("bill_id"));
        p.setAmount(rs.getDouble("amount"));
        Timestamp ts = rs.getTimestamp("payment_date");
        p.setPaymentDate(ts == null ? null : ts.toLocalDateTime());
        p.setMethod(rs.getString("method"));
        p.setReferenceNo(rs.getString("reference_no"));
        return p;
    }
}
