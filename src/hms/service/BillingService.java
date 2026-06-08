package hms.service;

import hms.model.Bill;
import hms.model.BillItem;
import hms.util.AppLogger;
import hms.util.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Creates a bill together with all of its line items as a single unit of
 * work.  TRANSACTION EXAMPLE #1 - the bill header and every item are
 * committed together; if any insert fails the whole thing is rolled back.
 */
public class BillingService {

    public int createBill(Bill bill) throws SQLException {
        if (bill.getItems() == null || bill.getItems().isEmpty()) {
            throw new SQLException("A bill must have at least one item.");
        }
        Connection c = null;
        try {
            c = DatabaseConnection.getConnection();
            c.setAutoCommit(false);                       // begin transaction

            int billId;
            try (PreparedStatement ps = c.prepareStatement(
                    "INSERT INTO bills(patient_id, bill_date, notes) VALUES (?, ?, ?)",
                    Statement.RETURN_GENERATED_KEYS)) {
                ps.setInt(1, bill.getPatientId());
                ps.setTimestamp(2, java.sql.Timestamp.valueOf(java.time.LocalDateTime.now()));
                ps.setString(3, bill.getNotes());
                ps.executeUpdate();
                try (ResultSet k = ps.getGeneratedKeys()) {
                    k.next();
                    billId = k.getInt(1);
                }
            }

            try (PreparedStatement ps = c.prepareStatement(
                    "INSERT INTO bill_items(bill_id, description, item_type, quantity, unit_price, amount) "
                  + "VALUES (?,?,?,?,?,?)")) {
                for (BillItem it : bill.getItems()) {
                    ps.setInt(1, billId);
                    ps.setString(2, it.getDescription());
                    ps.setString(3, it.getItemType());
                    ps.setInt(4, it.getQuantity());
                    ps.setDouble(5, it.getUnitPrice());
                    ps.setDouble(6, it.getQuantity() * it.getUnitPrice());
                    ps.addBatch();
                }
                ps.executeBatch();
            }

            c.commit();                                   // commit transaction
            AppLogger.info("Bill #" + billId + " created with " + bill.getItems().size() + " item(s)");
            return billId;
        } catch (SQLException e) {
            rollback(c);
            AppLogger.error("createBill failed - rolled back", e);
            throw e;
        } finally {
            close(c);
        }
    }

    /**
     * Delete a bill together with ALL its payments and items, as one
     * transaction. Payments are removed first (they block the bill via a
     * foreign key); the bill_items are removed automatically by the
     * ON DELETE CASCADE on bill_items.
     */
    public void deleteBill(int billId) throws SQLException {
        Connection c = null;
        try {
            c = DatabaseConnection.getConnection();
            c.setAutoCommit(false);                       // begin transaction

            try (PreparedStatement ps = c.prepareStatement("DELETE FROM payments WHERE bill_id = ?")) {
                ps.setInt(1, billId);
                ps.executeUpdate();
            }
            try (PreparedStatement ps = c.prepareStatement("DELETE FROM bills WHERE bill_id = ?")) {
                ps.setInt(1, billId);
                ps.executeUpdate();                       // bill_items removed by CASCADE
            }

            c.commit();                                   // commit transaction
            AppLogger.info("Bill #" + billId + " deleted (with its payments and items)");
        } catch (SQLException e) {
            rollback(c);
            AppLogger.error("deleteBill failed - rolled back", e);
            throw e;
        } finally {
            close(c);
        }
    }

    static void rollback(Connection c) {
        if (c != null) {
            try { c.rollback(); } catch (SQLException ex) { AppLogger.error("Rollback failed", ex); }
        }
    }

    static void close(Connection c) {
        if (c != null) {
            try { c.setAutoCommit(true); c.close(); } catch (SQLException ex) { AppLogger.error("Close failed", ex); }
        }
    }
}
