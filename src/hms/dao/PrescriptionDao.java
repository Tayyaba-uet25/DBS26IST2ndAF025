package hms.dao;

import hms.model.Prescription;
import hms.model.PrescriptionItem;
import hms.util.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/** Read access for prescriptions + items. Creation is in PrescriptionService (transaction). */
public class PrescriptionDao {

    private static final String BASE =
            "SELECT pr.*, CONCAT(p.first_name,' ',p.last_name) AS patient_name, "
          + "CONCAT('Dr. ',d.first_name,' ',d.last_name) AS doctor_name "
          + "FROM prescriptions pr "
          + "JOIN patients p ON pr.patient_id=p.patient_id "
          + "JOIN doctors d ON pr.doctor_id=d.doctor_id ";

    public List<Prescription> findAll() throws SQLException {
        List<Prescription> list = new ArrayList<>();
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(BASE + "ORDER BY pr.prescription_id DESC");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(map(rs));
        }
        return list;
    }

    public Prescription findByIdWithItems(int id) throws SQLException {
        Prescription pr = null;
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(BASE + "WHERE pr.prescription_id=?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) pr = map(rs);
            }
        }
        if (pr == null) return null;

        String sql = "SELECT pi.*, m.name AS medicine_name FROM prescription_items pi "
                   + "JOIN medicines m ON pi.medicine_id=m.medicine_id WHERE pi.prescription_id=?";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    PrescriptionItem it = new PrescriptionItem();
                    it.setItemId(rs.getInt("item_id"));
                    it.setPrescriptionId(rs.getInt("prescription_id"));
                    it.setMedicineId(rs.getInt("medicine_id"));
                    it.setMedicineName(rs.getString("medicine_name"));
                    it.setDosage(rs.getString("dosage"));
                    it.setQuantity(rs.getInt("quantity"));
                    it.setInstructions(rs.getString("instructions"));
                    pr.getItems().add(it);
                }
            }
        }
        return pr;
    }

    private Prescription map(ResultSet rs) throws SQLException {
        Prescription pr = new Prescription();
        pr.setPrescriptionId(rs.getInt("prescription_id"));
        pr.setPatientId(rs.getInt("patient_id"));
        pr.setPatientName(rs.getString("patient_name"));
        pr.setDoctorId(rs.getInt("doctor_id"));
        pr.setDoctorName(rs.getString("doctor_name"));
        Timestamp ts = rs.getTimestamp("prescribed_date");
        pr.setPrescribedDate(ts == null ? null : ts.toLocalDateTime());
        pr.setNotes(rs.getString("notes"));
        return pr;
    }
}
