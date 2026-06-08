package hms.ui;

import hms.util.AppLogger;

import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;
import javax.swing.UIManager;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

/** Shared UI helpers: look & feel, menus, date conversions, dialogs. */
public final class UiUtils {

    public static final Color PRIMARY = new Color(33, 97, 140);
    public static final Color PRIMARY_DARK = new Color(20, 60, 90);
    public static final Color ACCENT = new Color(39, 174, 96);
    public static final Color BG = new Color(245, 247, 250);

    private UiUtils() { }

    public static void applyLookAndFeel() {
        try {
            // Nimbus honours custom button background/foreground colours
            // (the Windows look-and-feel ignores them and looks washed out).
            boolean set = false;
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    set = true;
                    break;
                }
            }
            if (!set) {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            }
        } catch (Exception e) {
            AppLogger.error("Could not set look and feel", e);
        }
    }

    /** File menu present on every screen, plus a Help menu. */
    public static JMenuBar buildMenuBar(ActionListener onRefresh, ActionListener onClose,
                                        String closeLabel) {
        JMenuBar bar = new JMenuBar();
        JMenu file = new JMenu("File");
        file.setMnemonic(KeyEvent.VK_F);

        if (onRefresh != null) {
            JMenuItem refresh = new JMenuItem("Refresh");
            refresh.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0));
            refresh.addActionListener(onRefresh);
            file.add(refresh);
        }
        JMenuItem close = new JMenuItem(closeLabel == null ? "Close" : closeLabel);
        if (onClose != null) close.addActionListener(onClose);
        file.add(close);
        bar.add(file);

        JMenu help = new JMenu("Help");
        JMenuItem about = new JMenuItem("About");
        about.addActionListener(e -> info(null,
                "Hospital Management System\nCSC-104L Database Lab - Semester Final Project\nVersion 1.0",
                "About"));
        help.add(about);
        bar.add(help);
        return bar;
    }

    public static Font titleFont() { return new Font("Segoe UI", Font.BOLD, 18); }
    public static Font labelFont() { return new Font("Segoe UI", Font.PLAIN, 13); }

    public static LocalDate toLocalDate(Date d) {
        if (d == null) return null;
        return d.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }

    public static LocalDateTime toLocalDateTime(Date d) {
        if (d == null) return null;
        return d.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
    }

    public static Date toDate(LocalDate d) {
        if (d == null) return null;
        return Date.from(d.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

    public static Date toDate(LocalDateTime d) {
        if (d == null) return new Date();
        return Date.from(d.atZone(ZoneId.systemDefault()).toInstant());
    }

    public static void info(Component parent, String msg, String title) {
        JOptionPane.showMessageDialog(parent, msg, title, JOptionPane.INFORMATION_MESSAGE);
    }

    public static void error(Component parent, String msg) {
        JOptionPane.showMessageDialog(parent, msg, "Error", JOptionPane.ERROR_MESSAGE);
    }

    public static boolean confirm(Component parent, String msg) {
        return JOptionPane.showConfirmDialog(parent, msg, "Confirm",
                JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION;
    }

    public static void pad(JComponent c, int top, int left, int bottom, int right) {
        c.setBorder(javax.swing.BorderFactory.createEmptyBorder(top, left, bottom, right));
    }
}
