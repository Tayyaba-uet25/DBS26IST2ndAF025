package hms.ui;

import hms.dao.AppointmentDao;
import hms.model.Appointment;

import javax.swing.SwingUtilities;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.List;

/** Appointments module. */
public class AppointmentPanel extends AbstractCrudPanel<Appointment> {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private final AppointmentDao dao = new AppointmentDao();

    public AppointmentPanel() {
        super("Appointments");
        refresh();
    }

    @Override protected String[] columns() {
        return new String[]{"ID", "Patient", "Doctor", "Date/Time", "Reason", "Status", "Fee"};
    }

    @Override protected List<Appointment> fetch(String search) throws SQLException {
        return dao.findAll();
    }

    @Override protected Object[] rowOf(Appointment a) {
        return new Object[]{
                a.getAppointmentId(), a.getPatientName(), a.getDoctorName(),
                a.getAppointmentDate() == null ? "" : a.getAppointmentDate().format(FMT),
                a.getReason() == null ? "" : a.getReason(),
                a.getStatus(), a.getFee()
        };
    }

    @Override protected void openForm(Appointment item) {
        new AppointmentDialog((java.awt.Frame) SwingUtilities.getWindowAncestor(this), item).setVisible(true);
    }

    @Override protected void delete(Appointment item) throws SQLException {
        dao.delete(item.getAppointmentId());
    }
}
