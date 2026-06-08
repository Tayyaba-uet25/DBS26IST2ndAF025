package hms.dao;

import hms.model.User;
import hms.util.DatabaseConnection;
import hms.util.PasswordUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/** Data access for users / authentication. */
public class UserDao {

    public User authenticate(String username, String plainPassword) throws SQLException {
        String sql = "SELECT u.*, r.role_name FROM users u "
                   + "JOIN roles r ON u.role_id = r.role_id "
                   + "WHERE u.username = ? AND u.is_active = 1";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String stored = rs.getString("password_hash");
                    if (PasswordUtil.matches(plainPassword, stored)) {
                        return map(rs);
                    }
                }
            }
        }
        return null;
    }

    public List<User> findAll() throws SQLException {
        String sql = "SELECT u.*, r.role_name FROM users u JOIN roles r ON u.role_id=r.role_id ORDER BY u.username";
        List<User> list = new ArrayList<>();
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(map(rs));
            }
        }
        return list;
    }

    private User map(ResultSet rs) throws SQLException {
        User u = new User();
        u.setUserId(rs.getInt("user_id"));
        u.setUsername(rs.getString("username"));
        u.setPasswordHash(rs.getString("password_hash"));
        u.setFullName(rs.getString("full_name"));
        u.setEmail(rs.getString("email"));
        u.setRoleId(rs.getInt("role_id"));
        u.setRoleName(rs.getString("role_name"));
        u.setActive(rs.getInt("is_active") == 1);
        return u;
    }
}
