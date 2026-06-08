package hms.dao;

import hms.util.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/** Read access for the audit_log table (populated by triggers). */
public class AuditDao {

    /** Returns recent audit rows as [time, entity, action, details]. */
    public List<String[]> recent(int limit) throws SQLException {
        List<String[]> list = new ArrayList<>();
        String sql = "SELECT log_time, entity, action, details FROM audit_log ORDER BY log_id DESC LIMIT ?";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new String[]{
                            String.valueOf(rs.getTimestamp("log_time")),
                            rs.getString("entity"),
                            rs.getString("action"),
                            rs.getString("details")
                    });
                }
            }
        }
        return list;
    }
}
