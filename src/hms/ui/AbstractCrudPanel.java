package hms.ui;

import hms.util.AppLogger;
import hms.util.PdfReportGenerator;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Reusable base for every data module: a title, a toolbar (search +
 * Add New + Refresh + Print PDF + custom buttons), and a scrollable
 * JTable whose last column holds Edit/Delete buttons.
 *
 * Subclasses fill in the entity-specific bits. The SAME dialog is used
 * for both Add and Edit (openForm(null) vs openForm(item)).
 */
public abstract class AbstractCrudPanel<T> extends JPanel implements Refreshable {

    private final String title;
    protected DefaultTableModel model;
    protected JTable table;
    protected JTextField searchField;
    protected List<T> data = new ArrayList<>();
    private final JPanel toolbar;

    protected AbstractCrudPanel(String title) {
        this.title = title;
        setLayout(new BorderLayout());
        setBackground(UiUtils.BG);
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        // Header
        JLabel heading = new JLabel(title);
        heading.setFont(new Font("Segoe UI", Font.BOLD, 20));
        heading.setForeground(UiUtils.PRIMARY_DARK);
        heading.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        add(heading, BorderLayout.NORTH);

        JPanel center = new JPanel(new BorderLayout(0, 8));
        center.setOpaque(false);

        // Toolbar
        toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        toolbar.setOpaque(false);

        if (isSearchable()) {
            searchField = new JTextField(18);
            searchField.setPreferredSize(new Dimension(200, 30));
            searchField.addActionListener(e -> refresh());
            // Live search: filter the list as the user types (not only on Enter / Go).
            searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
                @Override public void insertUpdate(javax.swing.event.DocumentEvent e) { refresh(); }
                @Override public void removeUpdate(javax.swing.event.DocumentEvent e) { refresh(); }
                @Override public void changedUpdate(javax.swing.event.DocumentEvent e) { refresh(); }
            });
            toolbar.add(new JLabel("Search:"));
            toolbar.add(searchField);
            JButton searchBtn = makeButton("Go", new Color(127, 140, 141));
            searchBtn.addActionListener(e -> refresh());
            toolbar.add(searchBtn);
        }

        JButton addBtn = makeButton("+ Add New", UiUtils.ACCENT);
        addBtn.addActionListener(e -> { openForm(null); refresh(); });
        toolbar.add(addBtn);

        JButton refreshBtn = makeButton("Refresh", new Color(52, 152, 219));
        refreshBtn.addActionListener(e -> refresh());
        toolbar.add(refreshBtn);

        JButton pdfBtn = makeButton("Print PDF", new Color(155, 89, 182));
        pdfBtn.addActionListener(e -> exportPdf());
        toolbar.add(pdfBtn);

        addExtraButtons(toolbar);
        center.add(toolbar, BorderLayout.NORTH);

        // Table
        String[] cols = buildColumns();
        model = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int row, int col) {
                return col == getColumnCount() - 1;   // only the Actions column
            }
        };
        table = new JTable(model);
        table.setRowHeight(30);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        table.getTableHeader().setReorderingAllowed(false);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        table.setGridColor(new Color(225, 225, 225));

        // Install Edit/Delete buttons on the last column
        TableColumn actionCol = table.getColumnModel().getColumn(cols.length - 1);
        TableActionColumn ac = new TableActionColumn(table, new TableActionColumn.RowActionListener() {
            @Override public void onEdit(int modelRow) {
                if (modelRow >= 0 && modelRow < data.size()) {
                    openForm(data.get(modelRow));
                    refresh();
                }
            }
            @Override public void onDelete(int modelRow) {
                if (modelRow >= 0 && modelRow < data.size()) {
                    deleteWithConfirm(data.get(modelRow));
                }
            }
        });
        actionCol.setCellRenderer(ac);
        actionCol.setCellEditor(ac);
        actionCol.setPreferredWidth(150);
        actionCol.setMinWidth(150);

        // narrow the Sr# column
        table.getColumnModel().getColumn(0).setMaxWidth(55);
        table.getColumnModel().getColumn(0).setPreferredWidth(45);

        // Give every other column a comfortable width so long values
        // (timestamps, names, addresses) are not cut off. A horizontal
        // scroll bar appears when the table is wider than the window.
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        for (int i = 1; i < cols.length - 1; i++) {
            table.getColumnModel().getColumn(i).setPreferredWidth(140);
        }

        JScrollPane scroll = new JScrollPane(table);   // provides the scroll bar
        scroll.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220)));
        center.add(scroll, BorderLayout.CENTER);

        add(center, BorderLayout.CENTER);
    }

    protected JButton makeButton(String text, Color bg) {
        JButton b = new JButton(text);
        b.setBackground(bg);
        b.setForeground(Color.WHITE);
        b.setFocusPainted(false);
        b.setFont(new Font("Segoe UI", Font.BOLD, 12));
        return b;
    }

    // Final columns = "Sr#" (running serial) + subclass columns + "Actions"
    private String[] buildColumns() {
        String[] base = columns();
        String[] all = new String[base.length + 2];
        all[0] = "Sr#";
        System.arraycopy(base, 0, all, 1, base.length);
        all[all.length - 1] = "Actions";
        return all;
    }

    /** Reload data from the database and repaint the table. */
    public void refresh() {
        try {
            String search = (searchField != null) ? searchField.getText().trim() : "";
            data = fetch(search);
            model.setRowCount(0);
            int sr = 1;
            for (T item : data) {
                Object[] base = rowOf(item);
                Object[] row = new Object[base.length + 2];
                row[0] = sr++;                                  // running serial number
                System.arraycopy(base, 0, row, 1, base.length);
                row[row.length - 1] = "";                       // Actions cell
                model.addRow(row);
            }
        } catch (SQLException e) {
            AppLogger.error("Failed to load " + title, e);
            UiUtils.error(this, "Could not load data:\n" + e.getMessage());
        }
    }

    private void deleteWithConfirm(T item) {
        if (!UiUtils.confirm(this, "Are you sure you want to delete this record?")) {
            return;
        }
        try {
            delete(item);
            AppLogger.info(title + ": record deleted");
            refresh();
        } catch (SQLException e) {
            AppLogger.error("Delete failed in " + title, e);
            String msg;
            if (e instanceof java.sql.SQLIntegrityConstraintViolationException) {
                msg = "This record cannot be deleted because other records depend on it\n"
                    + "(for example appointments, bills, prescriptions or lab orders).\n\n"
                    + "Delete those related records first, then try again.";
            } else {
                msg = "Could not delete this record:\n" + e.getMessage();
            }
            UiUtils.error(this, msg);
        }
    }

    private void exportPdf() {
        List<String[]> rows = new ArrayList<>();
        String[] base = columns();
        String[] cols = new String[base.length + 1];
        cols[0] = "Sr#";
        System.arraycopy(base, 0, cols, 1, base.length);
        int sr = 1;
        for (T item : data) {
            Object[] vals = rowOf(item);
            String[] r = new String[vals.length + 1];
            r[0] = String.valueOf(sr++);
            for (int i = 0; i < vals.length; i++) {
                r[i + 1] = vals[i] == null ? "" : vals[i].toString();
            }
            rows.add(r);
        }
        try {
            String file = title.replaceAll("\\s+", "_") + ".pdf";
            PdfReportGenerator.buildTableReport(file, title + " - List", null, cols, rows);
        } catch (Exception e) {
            UiUtils.error(this, "Could not create PDF:\n" + e.getMessage());
        }
    }

    // -------- hooks subclasses implement --------
    protected abstract String[] columns();
    protected abstract List<T> fetch(String search) throws SQLException;
    protected abstract Object[] rowOf(T item);
    /** Open the shared add/edit dialog. item == null means "add new". */
    protected abstract void openForm(T item);
    protected abstract void delete(T item) throws SQLException;

    // -------- optional overrides --------
    protected boolean isSearchable() { return false; }
    protected void addExtraButtons(JPanel toolbar) { }
}
