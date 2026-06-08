package hms.service;

import hms.util.AppLogger;
import hms.util.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Records a payment against a bill.  TRANSACTION EXAMPLE #3 - the payment
 * insert is wrapped in an explicit JDBC transaction; the trg_payment_after_insert
 * trigger then keeps the bill's paid amount and status in sync.
 */
public class PaymentService {

    public void recordPayment(int billId, double amount, String method, String referenceNo) throws SQLException {
        if (amount <= 0) {
            throw new SQLException("Payment amount must be greater than zero.");
        }
        Connection c = null;
        try {
            c = DatabaseConnection.getConnection();
            c.setAutoCommit(false);
            try (PreparedStatement ps = c.prepareStatement(
                    "INSERT INTO payments(bill_id, amount, payment_date, method, reference_no) VALUES (?,?,?,?,?)")) {
                ps.setInt(1, billId);
                ps.setDouble(2, amount);
                ps.setTimestamp(3, java.sql.Timestamp.valueOf(java.time.LocalDateTime.now()));
                ps.setString(4, method);
                ps.setString(5, referenceNo);
                ps.executeUpdate();
            }
            c.commit();
            AppLogger.info("Payment of " + amount + " recorded for bill #" + billId);
        } catch (SQLException e) {
            BillingService.rollback(c);
            AppLogger.error("recordPayment failed - rolled back", e);
            throw e;
        } finally {
            BillingService.close(c);
        }
    }
}
