package hms.service;

import hms.util.AppLogger;
import hms.util.DatabaseConnection;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * Dispenses medicine using the stored procedure sp_dispense_medicine,
 * which validates available stock and raises an error if insufficient.
 */
public class MedicineService {

    public void dispense(int medicineId, int quantity) throws SQLException {
        try (Connection c = DatabaseConnection.getConnection();
             CallableStatement cs = c.prepareCall("{CALL sp_dispense_medicine(?, ?)}")) {
            cs.setInt(1, medicineId);
            cs.setInt(2, quantity);
            cs.execute();
            AppLogger.info("Dispensed " + quantity + " unit(s) of medicine #" + medicineId);
        } catch (SQLException e) {
            AppLogger.error("Dispense failed for medicine #" + medicineId, e);
            throw e;
        }
    }
}
