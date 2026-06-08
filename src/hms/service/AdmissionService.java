package hms.service;

import hms.util.AppLogger;
import hms.util.DatabaseConnection;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Types;

/**
 * Business operations for admissions. Uses the stored procedures
 * sp_admit_patient and sp_discharge_patient (each of which runs inside
 * its own DB transaction and raises errors via SIGNAL).
 */
public class AdmissionService {

    /** Admits a patient via sp_admit_patient. Returns new admission id. */
    public int admitPatient(int patientId, int bedId, int doctorId, String diagnosis) throws SQLException {
        try (Connection c = DatabaseConnection.getConnection();
             CallableStatement cs = c.prepareCall("{CALL sp_admit_patient(?, ?, ?, ?, ?)}")) {
            cs.setInt(1, patientId);
            cs.setInt(2, bedId);
            cs.setInt(3, doctorId);
            cs.setString(4, diagnosis);
            cs.registerOutParameter(5, Types.INTEGER);
            cs.execute();
            int admissionId = cs.getInt(5);
            AppLogger.info("Patient " + patientId + " admitted (admission #" + admissionId + ")");
            return admissionId;
        } catch (SQLException e) {
            AppLogger.error("Admit patient failed", e);
            throw e;
        }
    }

    /** Discharges via sp_discharge_patient (frees bed + creates ward-charge bill). */
    public void dischargePatient(int admissionId) throws SQLException {
        try (Connection c = DatabaseConnection.getConnection();
             CallableStatement cs = c.prepareCall("{CALL sp_discharge_patient(?)}")) {
            cs.setInt(1, admissionId);
            cs.execute();
            AppLogger.info("Admission #" + admissionId + " discharged");
        } catch (SQLException e) {
            AppLogger.error("Discharge failed for admission #" + admissionId, e);
            throw e;
        }
    }
}
