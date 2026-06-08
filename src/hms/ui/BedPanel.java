package hms.ui;

import hms.dao.BedDao;
import hms.model.Bed;

import javax.swing.SwingUtilities;
import java.sql.SQLException;
import java.util.List;

/** Beds module. */
public class BedPanel extends AbstractCrudPanel<Bed> {

    private final BedDao dao = new BedDao();

    public BedPanel() {
        super("Beds");
        refresh();
    }

    @Override protected String[] columns() {
        return new String[]{"ID", "Bed Number", "Ward", "Status"};
    }

    @Override protected List<Bed> fetch(String search) throws SQLException {
        return dao.findAll();
    }

    @Override protected Object[] rowOf(Bed b) {
        return new Object[]{b.getBedId(), b.getBedNumber(), b.getWardName(), b.getStatus()};
    }

    @Override protected void openForm(Bed item) {
        new BedDialog((java.awt.Frame) SwingUtilities.getWindowAncestor(this), item).setVisible(true);
    }

    @Override protected void delete(Bed item) throws SQLException {
        dao.delete(item.getBedId());
    }
}
