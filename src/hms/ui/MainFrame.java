package hms.ui;

import hms.util.AppLogger;
import hms.util.SessionManager;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;

/** Main application window: menu bar, sidebar navigation and content area. */
public class MainFrame extends JFrame {

    private final CardLayout cards = new CardLayout();
    private final JPanel content = new JPanel(cards);
    private final JPanel sidebar = new JPanel();
    private final Map<String, Supplier<JComponent>> modules = new LinkedHashMap<>();
    private final Map<String, JComponent> created = new LinkedHashMap<>();

    public MainFrame() {
        setTitle("Hospital Management System - City Care Hospital");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1180, 720);
        setMinimumSize(new Dimension(940, 600));
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        setJMenuBar(buildMenuBar());
        registerModules();

        sidebar.setLayout(new javax.swing.BoxLayout(sidebar, javax.swing.BoxLayout.Y_AXIS));
        sidebar.setBackground(UiUtils.PRIMARY_DARK);
        sidebar.setPreferredSize(new Dimension(210, 0));
        buildSidebar();

        JScrollPane sideScroll = new JScrollPane(sidebar,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        sideScroll.setBorder(null);
        sideScroll.getVerticalScrollBar().setUnitIncrement(16);

        content.setBackground(UiUtils.BG);

        add(sideScroll, BorderLayout.WEST);
        add(content, BorderLayout.CENTER);
        add(buildStatusBar(), BorderLayout.SOUTH);

        show("Dashboard");
    }

    private void registerModules() {
        modules.put("Dashboard", DashboardPanel::new);
        modules.put("Patients", PatientPanel::new);
        modules.put("Doctors", DoctorPanel::new);
        modules.put("Departments", DepartmentPanel::new);
        modules.put("Appointments", AppointmentPanel::new);
        modules.put("Admissions", AdmissionPanel::new);
        modules.put("Wards", WardPanel::new);
        modules.put("Beds", BedPanel::new);
        modules.put("Medicines", MedicinePanel::new);
        modules.put("Prescriptions", PrescriptionPanel::new);
        modules.put("Lab Tests", LabTestPanel::new);
        modules.put("Lab Orders", LabOrderPanel::new);
        modules.put("Billing", BillingPanel::new);
        modules.put("Reports", ReportsPanel::new);
        modules.put("Audit Log", AuditPanel::new);
    }

    private void buildSidebar() {
        JLabel brand = new JLabel("  City Care HMS");
        brand.setForeground(Color.WHITE);
        brand.setFont(new Font("Segoe UI", Font.BOLD, 18));
        brand.setBorder(BorderFactory.createEmptyBorder(18, 8, 18, 8));
        brand.setAlignmentX(Component.LEFT_ALIGNMENT);
        sidebar.add(brand);

        for (String name : modules.keySet()) {
            sidebar.add(navButton(name));
        }
    }

    // A JLabel is used (instead of JButton) because the Windows look-and-feel
    // ignores custom button colors, which made the text appear washed out.
    // JLabel renders the chosen foreground/background reliably under any L&F.
    private JComponent navButton(String name) {
        JLabel b = new JLabel(name);
        b.setOpaque(true);
        b.setAlignmentX(Component.LEFT_ALIGNMENT);
        b.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        b.setPreferredSize(new Dimension(210, 44));
        b.setHorizontalAlignment(SwingConstants.LEFT);
        b.setBackground(UiUtils.PRIMARY_DARK);
        b.setForeground(Color.WHITE);
        b.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(255, 255, 255, 30)),
                BorderFactory.createEmptyBorder(10, 20, 10, 8)));
        b.setFont(new Font("Segoe UI", Font.BOLD, 14));
        b.setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.HAND_CURSOR));
        b.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseEntered(java.awt.event.MouseEvent e) { b.setBackground(UiUtils.PRIMARY); }
            @Override public void mouseExited(java.awt.event.MouseEvent e) { b.setBackground(UiUtils.PRIMARY_DARK); }
            @Override public void mouseClicked(java.awt.event.MouseEvent e) { show(name); }
        });
        return b;
    }

    private void show(String name) {
        try {
            if (!created.containsKey(name)) {
                JComponent panel = modules.get(name).get();
                created.put(name, panel);
                content.add(panel, name);
            } else if (created.get(name) instanceof Refreshable) {
                ((Refreshable) created.get(name)).refresh();
            }
            cards.show(content, name);
            setTitle("Hospital Management System  -  " + name);
        } catch (Exception ex) {
            AppLogger.error("Failed to open module " + name, ex);
            UiUtils.error(this, "Could not open " + name + ":\n" + ex.getMessage());
        }
    }

    private JMenuBar buildMenuBar() {
        JMenuBar bar = new JMenuBar();
        JMenu file = new JMenu("File");
        JMenuItem logout = new JMenuItem("Logout");
        logout.addActionListener(e -> {
            SessionManager.logout();
            dispose();
            new LoginFrame().setVisible(true);
        });
        JMenuItem exit = new JMenuItem("Exit");
        exit.addActionListener(e -> System.exit(0));
        file.add(logout);
        file.addSeparator();
        file.add(exit);
        bar.add(file);

        JMenu help = new JMenu("Help");
        JMenuItem about = new JMenuItem("About");
        about.addActionListener(e -> UiUtils.info(this,
                "Hospital Management System\nCSC-104L Database System Lab - Semester Final Project\n"
              + "Java + Swing + MySQL\nVersion 1.0", "About"));
        help.add(about);
        bar.add(help);
        return bar;
    }

    private JPanel buildStatusBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(new Color(230, 233, 237));
        bar.setBorder(BorderFactory.createEmptyBorder(4, 10, 4, 10));
        JLabel left = new JLabel("Logged in as: " + SessionManager.currentUserName()
                + "  (" + (SessionManager.getCurrentUser() == null ? "" :
                SessionManager.getCurrentUser().getRoleName()) + ")");
        left.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        JLabel right = new JLabel("City Care Hospital  |  CSC-104L Project");
        right.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        bar.add(left, BorderLayout.WEST);
        bar.add(right, BorderLayout.EAST);
        return bar;
    }
}
