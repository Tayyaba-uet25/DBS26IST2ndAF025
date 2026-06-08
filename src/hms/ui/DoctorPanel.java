package hms.ui;

import hms.dao.DoctorDao;
import hms.model.Doctor;

import javax.swing.SwingUtilities;
import java.sql.SQLException;
import java.util.List;

/** Doctors module. */
public class DoctorPanel extends AbstractCrudPanel<Doctor> {

    private final DoctorDao dao = new DoctorDao();

    public DoctorPanel() {
        super("Doctors");
        refresh();
    }

    @Override protected String[] columns() {
        return new String[]{"ID", "Name", "Gender", "Specialization", "Department", "Phone", "Email", "Fee", "Status"};
    }

    @Override protected List<Doctor> fetch(String search) throws SQLException {
        return dao.findAll();
    }

    @Override protected Object[] rowOf(Doctor d) {
        return new Object[]{
                d.getDoctorId(), d.getFullName(), d.getGender(),
                d.getSpecialization() == null ? "" : d.getSpecialization(),
                d.getDepartmentName() == null ? "" : d.getDepartmentName(),
                d.getPhone() == null ? "" : d.getPhone(),
                d.getEmail() == null ? "" : d.getEmail(),
                d.getConsultationFee(), d.isActive() ? "Active" : "Inactive"
        };
    }

    @Override protected void openForm(Doctor item) {
        new DoctorDialog((java.awt.Frame) SwingUtilities.getWindowAncestor(this), item).setVisible(true);
    }

    @Override protected void delete(Doctor item) throws SQLException {
        dao.delete(item.getDoctorId());
    }
}
