package hms.ui;

import hms.dao.AuditDao;
import hms.util.AppLogger;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.util.List;

/** Read-only view of the audit_log table (written automatically by triggers). */
public class AuditPanel extends JPanel implements Refreshable {

    private final AuditDao dao = new AuditDao();
    private final String[] cols = {"Time", "Entity", "Action", "Details"};
    private final DefaultTableModel model = new DefaultTableModel(cols, 0) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
    };
    private final JTable table = new JTable(model);

    public AuditPanel() {
        setLayout(new BorderLayout());
        setBackground(UiUtils.BG);
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        JLabel heading = new JLabel("Audit Log  (populated by database triggers)");
        heading.setFont(new Font("Segoe UI", Font.BOLD, 20));
        heading.setForeground(UiUtils.PRIMARY_DARK);
        heading.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        add(heading, BorderLayout.NORTH);

        JPanel center = new JPanel(new BorderLayout(0, 8));
        center.setOpaque(false);
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        toolbar.setOpaque(false);
        JButton refresh = new JButton("Refresh");
        refresh.setBackground(new Color(52, 152, 219));
        refresh.setForeground(Color.WHITE);
        refresh.setFocusPainted(false);
        refresh.addActionListener(e -> refresh());
        toolbar.add(refresh);
        center.add(toolbar, BorderLayout.NORTH);

        table.setRowHeight(26);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        center.add(new JScrollPane(table), BorderLayout.CENTER);
        add(center, BorderLayout.CENTER);

        refresh();
    }

    public void refresh() {
        try {
            model.setRowCount(0);
            List<String[]> rows = dao.recent(200);
            for (String[] r : rows) model.addRow(r);
        } catch (Exception e) {
            AppLogger.error("Load audit log failed", e);
            UiUtils.error(this, "Could not load audit log:\n" + e.getMessage());
        }
    }
}
