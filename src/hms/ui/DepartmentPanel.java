package hms.ui;

import hms.dao.DepartmentDao;
import hms.model.Department;

import javax.swing.SwingUtilities;
import java.sql.SQLException;
import java.util.List;

/** Departments module. */
public class DepartmentPanel extends AbstractCrudPanel<Department> {

    private final DepartmentDao dao = new DepartmentDao();

    public DepartmentPanel() {
        super("Departments");
        refresh();
    }

    @Override protected String[] columns() {
        return new String[]{"ID", "Name", "Location", "Phone"};
    }

    @Override protected List<Department> fetch(String search) throws SQLException {
        return dao.findAll();
    }

    @Override protected Object[] rowOf(Department d) {
        return new Object[]{d.getDepartmentId(), d.getName(),
                d.getLocation() == null ? "" : d.getLocation(),
                d.getPhone() == null ? "" : d.getPhone()};
    }

    @Override protected void openForm(Department item) {
        new DepartmentDialog((java.awt.Frame) SwingUtilities.getWindowAncestor(this), item).setVisible(true);
    }

    @Override protected void delete(Department item) throws SQLException {
        dao.delete(item.getDepartmentId());
    }
}
