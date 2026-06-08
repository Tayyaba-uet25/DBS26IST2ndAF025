package hms.ui;

import hms.dao.UserDao;
import hms.model.User;
import hms.util.AppLogger;
import hms.util.SessionManager;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.sql.SQLException;

/** Login screen with username and password fields. */
public class LoginFrame extends JFrame {

    private final JTextField usernameField = new JTextField(18);
    private final JPasswordField passwordField = new JPasswordField(18);
    private final UserDao userDao = new UserDao();

    public LoginFrame() {
        setTitle("HMS - Login");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(820, 480);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        add(buildBanner(), BorderLayout.WEST);
        add(buildForm(), BorderLayout.CENTER);

        getRootPane().setDefaultButton(loginButton);
    }

    private final JButton loginButton = new JButton("Login");

    private JPanel buildBanner() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(UiUtils.PRIMARY);
        panel.setPreferredSize(new Dimension(330, 0));

        JPanel inner = new JPanel();
        inner.setOpaque(false);
        inner.setLayout(new javax.swing.BoxLayout(inner, javax.swing.BoxLayout.Y_AXIS));

        JLabel icon = new JLabel("⚕");   // medical symbol
        icon.setFont(new Font("Segoe UI Symbol", Font.BOLD, 72));
        icon.setForeground(Color.WHITE);
        icon.setAlignmentX(CENTER_ALIGNMENT);

        JLabel title = new JLabel("City Care Hospital");
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        title.setForeground(Color.WHITE);
        title.setAlignmentX(CENTER_ALIGNMENT);

        JLabel sub = new JLabel("Management System");
        sub.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        sub.setForeground(new Color(220, 235, 245));
        sub.setAlignmentX(CENTER_ALIGNMENT);

        inner.add(icon);
        inner.add(javax.swing.Box.createVerticalStrut(10));
        inner.add(title);
        inner.add(sub);
        panel.add(inner);
        return panel;
    }

    private JPanel buildForm() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));

        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(8, 8, 8, 8);
        g.anchor = GridBagConstraints.WEST;
        g.fill = GridBagConstraints.HORIZONTAL;

        JLabel heading = new JLabel("Sign in to continue");
        heading.setFont(new Font("Segoe UI", Font.BOLD, 20));
        heading.setForeground(UiUtils.PRIMARY_DARK);
        g.gridx = 0; g.gridy = 0; g.gridwidth = 2;
        panel.add(heading, g);

        g.gridwidth = 1;
        g.gridx = 0; g.gridy = 1;
        panel.add(label("Username"), g);
        g.gridy = 2;
        usernameField.setFont(UiUtils.labelFont());
        usernameField.setPreferredSize(new Dimension(0, 32));
        panel.add(usernameField, g);

        g.gridy = 3;
        panel.add(label("Password"), g);
        g.gridy = 4;
        passwordField.setFont(UiUtils.labelFont());
        passwordField.setPreferredSize(new Dimension(0, 32));
        panel.add(passwordField, g);

        loginButton.setBackground(UiUtils.PRIMARY);
        loginButton.setForeground(Color.WHITE);
        loginButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        loginButton.setFocusPainted(false);
        loginButton.setPreferredSize(new Dimension(0, 38));
        loginButton.addActionListener(e -> doLogin());
        g.gridy = 5; g.insets = new Insets(20, 8, 8, 8);
        panel.add(loginButton, g);

        JLabel hint = new JLabel("<html><i>Default: admin / admin123 &nbsp; | &nbsp; reception / recep123</i></html>");
        hint.setForeground(Color.GRAY);
        hint.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        g.gridy = 6; g.insets = new Insets(4, 8, 8, 8);
        panel.add(hint, g);

        return panel;
    }

    private JLabel label(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        l.setForeground(new Color(80, 80, 80));
        return l;
    }

    private void doLogin() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            UiUtils.error(this, "Please enter both username and password.");
            return;
        }
        try {
            User user = userDao.authenticate(username, password);
            if (user == null) {
                UiUtils.error(this, "Invalid username or password.");
                AppLogger.info("Failed login attempt for user: " + username);
                return;
            }
            SessionManager.login(user);
            dispose();
            new MainFrame().setVisible(true);
        } catch (SQLException ex) {
            AppLogger.error("Login error", ex);
            UiUtils.error(this, "Login failed due to a database error:\n" + ex.getMessage());
        }
    }
}
