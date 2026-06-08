package hms.ui;

import hms.dao.LabOrderDao;
import hms.model.LabOrder;

import javax.swing.SwingUtilities;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.List;

/** Lab orders module. */
public class LabOrderPanel extends AbstractCrudPanel<LabOrder> {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private final LabOrderDao dao = new LabOrderDao();

    public LabOrderPanel() {
        super("Lab Orders");
        refresh();
    }

    @Override protected String[] columns() {
        return new String[]{"ID", "Patient", "Doctor", "Test", "Ordered", "Status", "Result"};
    }

    @Override protected List<LabOrder> fetch(String search) throws SQLException {
        return dao.findAll();
    }

    @Override protected Object[] rowOf(LabOrder o) {
        return new Object[]{
                o.getOrderId(), o.getPatientName(), o.getDoctorName(), o.getTestName(),
                o.getOrderDate() == null ? "" : o.getOrderDate().format(FMT),
                o.getStatus(), o.getResult() == null ? "" : o.getResult()
        };
    }

    @Override protected void openForm(LabOrder item) {
        new LabOrderDialog((java.awt.Frame) SwingUtilities.getWindowAncestor(this), item).setVisible(true);
    }

    @Override protected void delete(LabOrder item) throws SQLException {
        dao.delete(item.getOrderId());
    }
}
