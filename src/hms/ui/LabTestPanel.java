package hms.ui;

import hms.dao.LabTestDao;
import hms.model.LabTest;

import javax.swing.SwingUtilities;
import java.sql.SQLException;
import java.util.List;

/** Lab tests catalog module. */
public class LabTestPanel extends AbstractCrudPanel<LabTest> {

    private final LabTestDao dao = new LabTestDao();

    public LabTestPanel() {
        super("Lab Tests");
        refresh();
    }

    @Override protected String[] columns() {
        return new String[]{"ID", "Name", "Price", "Description"};
    }

    @Override protected List<LabTest> fetch(String search) throws SQLException {
        return dao.findAll();
    }

    @Override protected Object[] rowOf(LabTest t) {
        return new Object[]{t.getTestId(), t.getName(), t.getPrice(),
                t.getDescription() == null ? "" : t.getDescription()};
    }

    @Override protected void openForm(LabTest item) {
        new LabTestDialog((java.awt.Frame) SwingUtilities.getWindowAncestor(this), item).setVisible(true);
    }

    @Override protected void delete(LabTest item) throws SQLException {
        dao.delete(item.getTestId());
    }
}
