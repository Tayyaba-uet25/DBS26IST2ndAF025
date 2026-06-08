package hms.service;

import hms.model.Prescription;
import hms.model.PrescriptionItem;
import hms.util.AppLogger;
import hms.util.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Saves a prescription together with all of its medicine items in one
 * atomic operation.  TRANSACTION EXAMPLE #2 - the prescription header and
 * every item commit together, or none of them do. (Medicine stock is
 * reduced separately by the pharmacy "Dispense" action.)
 */
public class PrescriptionService {

    public int savePrescription(Prescription rx) throws SQLException {
        if (rx.getItems() == null || rx.getItems().isEmpty()) {
            throw new SQLException("A prescription must have at least one medicine.");
        }
        Connection c = null;
        try {
            c = DatabaseConnection.getConnection();
            c.setAutoCommit(false);

            int rxId;
            try (PreparedStatement ps = c.prepareStatement(
                    "INSERT INTO prescriptions(patient_id, doctor_id, prescribed_date, notes) VALUES (?,?,?,?)",
                    Statement.RETURN_GENERATED_KEYS)) {
                ps.setInt(1, rx.getPatientId());
                ps.setInt(2, rx.getDoctorId());
                ps.setTimestamp(3, java.sql.Timestamp.valueOf(java.time.LocalDateTime.now()));
                ps.setString(4, rx.getNotes());
                ps.executeUpdate();
                try (ResultSet k = ps.getGeneratedKeys()) {
                    k.next();
                    rxId = k.getInt(1);
                }
            }

            // Insert each prescribed medicine line. (Stock is reduced separately
            // by the pharmacy "Dispense" action, not when the doctor prescribes,
            // so a prescription can always be saved.)
            try (PreparedStatement ins = c.prepareStatement(
                     "INSERT INTO prescription_items(prescription_id, medicine_id, dosage, quantity, instructions) "
                   + "VALUES (?,?,?,?,?)")) {
                for (PrescriptionItem it : rx.getItems()) {
                    ins.setInt(1, rxId);
                    ins.setInt(2, it.getMedicineId());
                    ins.setString(3, it.getDosage());
                    ins.setInt(4, it.getQuantity());
                    ins.setString(5, it.getInstructions());
                    ins.executeUpdate();
                }
            }

            c.commit();
            AppLogger.info("Prescription #" + rxId + " saved with " + rx.getItems().size() + " item(s)");
            return rxId;
        } catch (SQLException e) {
            BillingService.rollback(c);
            AppLogger.error("savePrescription failed - rolled back", e);
            throw e;
        } finally {
            BillingService.close(c);
        }
    }
}
