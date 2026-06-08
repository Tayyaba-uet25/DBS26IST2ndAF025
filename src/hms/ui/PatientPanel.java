package hms.ui;

import hms.dao.PatientDao;
import hms.model.Patient;

import javax.swing.SwingUtilities;
import java.sql.SQLException;
import java.util.List;

/** Patients module: searchable list with add/edit/delete. */
public class PatientPanel extends AbstractCrudPanel<Patient> {

    private final PatientDao dao = new PatientDao();

    public PatientPanel() {
        super("Patients");
        refresh();
    }

    @Override protected boolean isSearchable() { return true; }

    @Override protected String[] columns() {
        return new String[]{"ID", "Name", "Gender", "DOB", "Blood", "Phone", "Email", "Address"};
    }

    @Override protected List<Patient> fetch(String search) throws SQLException {
        return dao.search(search);
    }

    @Override protected Object[] rowOf(Patient p) {
        return new Object[]{
                p.getPatientId(), p.getFullName(), p.getGender(),
                p.getDateOfBirth() == null ? "" : p.getDateOfBirth().toString(),
                p.getBloodGroup() == null ? "" : p.getBloodGroup(),
                p.getPhone() == null ? "" : p.getPhone(),
                p.getEmail() == null ? "" : p.getEmail(),
                p.getAddress() == null ? "" : p.getAddress()
        };
    }

    @Override protected void openForm(Patient item) {
        PatientDialog d = new PatientDialog((java.awt.Frame) SwingUtilities.getWindowAncestor(this), item);
        d.setVisible(true);
    }

    @Override protected void delete(Patient item) throws SQLException {
        dao.delete(item.getPatientId());
    }
}
