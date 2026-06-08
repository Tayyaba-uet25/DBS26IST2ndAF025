package hms.ui;

import hms.util.AppLogger;
import hms.util.DatabaseConnection;
import hms.util.SessionManager;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedHashMap;
import java.util.Map;

/** Landing screen with summary statistic cards (refreshes every time it is shown). */
public class DashboardPanel extends JPanel implements Refreshable {

    // title -> SQL used to compute the number
    private final Map<String, String> stats = new LinkedHashMap<>();
    // title -> the label that displays the number (so we can update it)
    private final Map<String, JLabel> valueLabels = new LinkedHashMap<>();

    public DashboardPanel() {
        setLayout(new BorderLayout());
        setBackground(UiUtils.BG);
        setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        JLabel welcome = new JLabel("Welcome, " + SessionManager.currentUserName() + "!");
        welcome.setFont(new Font("Segoe UI", Font.BOLD, 22));
        welcome.setForeground(UiUtils.PRIMARY_DARK);
        welcome.setBorder(BorderFactory.createEmptyBorder(0, 0, 14, 0));
        add(welcome, BorderLayout.NORTH);

        stats.put("Total Patients", "SELECT COUNT(*) FROM patients");
        stats.put("Total Doctors", "SELECT COUNT(*) FROM doctors");
        stats.put("Appointments Today", "SELECT COUNT(*) FROM appointments WHERE DATE(appointment_date)=CURDATE()");
        stats.put("Currently Admitted", "SELECT COUNT(*) FROM admissions WHERE status='Admitted'");
        stats.put("Low Stock Medicines", "SELECT COUNT(*) FROM vw_low_stock_medicines");
        stats.put("Unpaid Bills", "SELECT COUNT(*) FROM bills WHERE status<>'Paid'");
        stats.put("Available Beds", "SELECT COUNT(*) FROM beds WHERE status='Available'");
        stats.put("Pending Lab Orders", "SELECT COUNT(*) FROM lab_orders WHERE status='Pending'");
        stats.put("Total Departments", "SELECT COUNT(*) FROM departments");

        Color[] colors = {
                new Color(41, 128, 185), new Color(39, 174, 96), new Color(230, 126, 34),
                new Color(155, 89, 182), new Color(192, 57, 43), new Color(52, 73, 94),
                new Color(22, 160, 133), new Color(127, 140, 141), new Color(41, 128, 185)
        };

        JPanel cards = new JPanel(new GridLayout(0, 3, 14, 14));
        cards.setOpaque(false);
        int i = 0;
        for (String title : stats.keySet()) {
            cards.add(card(title, colors[i % colors.length]));
            i++;
        }

        JPanel wrap = new JPanel(new BorderLayout());
        wrap.setOpaque(false);
        wrap.add(cards, BorderLayout.NORTH);
        add(wrap, BorderLayout.CENTER);

        refresh();   // load initial numbers
    }

    private JPanel card(String title, Color color) {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(Color.WHITE);
        p.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 6, 0, 0, color),
                BorderFactory.createEmptyBorder(16, 18, 16, 18)));
        p.setPreferredSize(new Dimension(220, 90));

        JLabel v = new JLabel("-");
        v.setFont(new Font("Segoe UI", Font.BOLD, 30));
        v.setForeground(color);
        valueLabels.put(title, v);

        JLabel t = new JLabel(title);
        t.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        t.setForeground(new Color(90, 90, 90));

        p.add(v, BorderLayout.CENTER);
        p.add(t, BorderLayout.SOUTH);
        return p;
    }

    /** Re-run every count query and update the cards. Called whenever the screen is shown. */
    @Override
    public void refresh() {
        for (Map.Entry<String, String> e : stats.entrySet()) {
            JLabel label = valueLabels.get(e.getKey());
            if (label != null) {
                label.setText(count(e.getValue()));
            }
        }
    }

    private String count(String sql) {
        try (Connection c = DatabaseConnection.getConnection();
             Statement st = c.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            if (rs.next()) return String.valueOf(rs.getInt(1));
        } catch (SQLException e) {
            AppLogger.error("Dashboard stat failed: " + sql, e);
        }
        return "-";
    }
}
