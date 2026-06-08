package hms.ui;

import hms.dao.WardDao;
import hms.model.Ward;

import javax.swing.SwingUtilities;
import java.sql.SQLException;
import java.util.List;

/** Wards module. */
public class WardPanel extends AbstractCrudPanel<Ward> {

    private final WardDao dao = new WardDao();

    public WardPanel() {
        super("Wards");
        refresh();
    }

    @Override protected String[] columns() {
        return new String[]{"ID", "Name", "Type", "Charge/Day"};
    }

    @Override protected List<Ward> fetch(String search) throws SQLException {
        return dao.findAll();
    }

    @Override protected Object[] rowOf(Ward w) {
        return new Object[]{w.getWardId(), w.getName(), w.getWardType(), w.getChargePerDay()};
    }

    @Override protected void openForm(Ward item) {
        new WardDialog((java.awt.Frame) SwingUtilities.getWindowAncestor(this), item).setVisible(true);
    }

    @Override protected void delete(Ward item) throws SQLException {
        dao.delete(item.getWardId());
    }
}
