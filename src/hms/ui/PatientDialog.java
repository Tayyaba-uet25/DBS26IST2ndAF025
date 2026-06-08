package hms.ui;

import com.toedter.calendar.JDateChooser;
import hms.dao.PatientDao;
import hms.model.Patient;
import hms.util.AppLogger;
import hms.util.ValidationException;
import hms.util.ValidationUtil;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

/**
 * Add/Edit form for a patient. The SAME dialog handles both cases - if
 * the constructor gets an existing patient it pre-fills the fields and
 * updates on save; otherwise it inserts a new record.
 *
 * Demonstrates: text fields, radio buttons (gender), date selector (DOB),
 * dropdown (blood group), text area (address), panels, and a File menu.
 */
public class PatientDialog extends JDialog {

    private final PatientDao dao = new PatientDao();
    private final Patient patient;       // null => add mode
    private boolean saved = false;

    private final JTextField firstName = new JTextField(16);
    private final JTextField lastName = new JTextField(16);
    private final JRadioButton male = new JRadioButton("Male");
    private final JRadioButton female = new JRadioButton("Female");
    private final JRadioButton other = new JRadioButton("Other");
    private final JDateChooser dob = new JDateChooser();
    private final JComboBox<String> bloodGroup =
            new JComboBox<>(new String[]{"", "A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-"});
    private final JTextField phone = new JTextField(16);
    private final JTextField email = new JTextField(16);
    private final JTextArea address = new JTextArea(4, 16);

    public PatientDialog(Frame owner, Patient existing) {
        super(owner, true);
        this.patient = existing;
        setTitle(existing == null ? "Add Patient" : "Edit Patient #" + existing.getPatientId());
        setSize(560, 560);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout());

        setJMenuBar(UiUtils.buildMenuBar(null, e -> dispose(), "Close"));

        add(buildBody(), BorderLayout.CENTER);
        add(buildButtons(), BorderLayout.SOUTH);

        if (existing != null) populate(existing);
        else male.setSelected(true);
    }

    private JPanel buildBody() {
        JPanel body = new JPanel();
        body.setLayout(new javax.swing.BoxLayout(body, javax.swing.BoxLayout.Y_AXIS));
        body.setBackground(Color.WHITE);
        body.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));

        // ---- Panel 1: Personal details ----
        JPanel personal = sectionPanel("Personal Details");
        GridBagConstraints g = gbc();
        addField(personal, g, 0, "First Name *", firstName);
        addField(personal, g, 1, "Last Name *", lastName);

        JPanel genderPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        genderPanel.setOpaque(false);
        ButtonGroup bg = new ButtonGroup();
        bg.add(male); bg.add(female); bg.add(other);
        genderPanel.add(male); genderPanel.add(female); genderPanel.add(other);
        g.gridx = 0; g.gridy = 2; personal.add(new JLabel("Gender *"), g);
        g.gridx = 1; g.gridy = 2; personal.add(genderPanel, g);

        dob.setDateFormatString("yyyy-MM-dd");
        dob.setPreferredSize(new Dimension(180, 28));
        g.gridx = 0; g.gridy = 3; personal.add(new JLabel("Date of Birth"), g);
        g.gridx = 1; g.gridy = 3; personal.add(dob, g);

        g.gridx = 0; g.gridy = 4; personal.add(new JLabel("Blood Group *"), g);
        g.gridx = 1; g.gridy = 4; personal.add(bloodGroup, g);

        // ---- Panel 2: Contact ----
        JPanel contact = sectionPanel("Contact Information");
        GridBagConstraints g2 = gbc();
        addField(contact, g2, 0, "Phone", phone);
        addField(contact, g2, 1, "Email", email);
        address.setLineWrap(true);
        address.setWrapStyleWord(true);
        JScrollPane addrScroll = new JScrollPane(address);   // scroll bar for text area
        addrScroll.setPreferredSize(new Dimension(260, 80));
        g2.gridx = 0; g2.gridy = 2; g2.anchor = GridBagConstraints.NORTHWEST;
        contact.add(new JLabel("Address"), g2);
        g2.gridx = 1; g2.gridy = 2;
        contact.add(addrScroll, g2);

        body.add(personal);
        body.add(javax.swing.Box.createVerticalStrut(12));
        body.add(contact);
        return body;
    }

    private JPanel sectionPanel(String title) {
        JPanel p = new JPanel(new GridBagLayout());
        p.setOpaque(false);
        p.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(210, 210, 210)), title));
        return p;
    }

    private GridBagConstraints gbc() {
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(6, 8, 6, 8);
        g.anchor = GridBagConstraints.WEST;
        g.fill = GridBagConstraints.HORIZONTAL;
        return g;
    }

    private void addField(JPanel panel, GridBagConstraints g, int row, String label, JTextField field) {
        g.gridx = 0; g.gridy = row; g.weightx = 0;
        panel.add(new JLabel(label), g);
        g.gridx = 1; g.gridy = row; g.weightx = 1;
        panel.add(field, g);
    }

    private JPanel buildButtons() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
        p.setBackground(new Color(240, 242, 245));
        JButton save = new JButton(patient == null ? "Save" : "Update");
        save.setBackground(UiUtils.ACCENT);
        save.setForeground(Color.WHITE);
        save.setFocusPainted(false);
        save.addActionListener(e -> onSave());
        JButton cancel = new JButton("Cancel");
        cancel.addActionListener(e -> dispose());
        p.add(cancel);
        p.add(save);
        return p;
    }

    private void populate(Patient p) {
        firstName.setText(p.getFirstName());
        lastName.setText(p.getLastName());
        if ("Female".equals(p.getGender())) female.setSelected(true);
        else if ("Other".equals(p.getGender())) other.setSelected(true);
        else male.setSelected(true);
        dob.setDate(UiUtils.toDate(p.getDateOfBirth()));
        bloodGroup.setSelectedItem(p.getBloodGroup() == null ? "" : p.getBloodGroup());
        phone.setText(p.getPhone());
        email.setText(p.getEmail());
        address.setText(p.getAddress());
    }

    private String selectedGender() {
        if (female.isSelected()) return "Female";
        if (other.isSelected()) return "Other";
        return "Male";
    }

    private void onSave() {
        try {
            ValidationUtil.requireText(firstName.getText(), "First Name");
            ValidationUtil.requireText(lastName.getText(), "Last Name");
            ValidationUtil.maxLength(firstName.getText(), 50, "First Name");
            ValidationUtil.phone(phone.getText(), "Phone");
            ValidationUtil.email(email.getText(), "Email");

            Object bg = bloodGroup.getSelectedItem();
            if (bg == null || bg.toString().trim().isEmpty()) {
                throw new ValidationException("Blood Group is required.");
            }
            String emailVal = emptyToNull(email.getText());
            if (emailVal != null && dao.emailExists(emailVal, patient == null ? 0 : patient.getPatientId())) {
                throw new ValidationException("This email is already used by another patient.");
            }

            Patient p = (patient == null) ? new Patient() : patient;
            p.setFirstName(firstName.getText().trim());
            p.setLastName(lastName.getText().trim());
            p.setGender(selectedGender());
            p.setDateOfBirth(UiUtils.toLocalDate(dob.getDate()));
            Object bgSel = bloodGroup.getSelectedItem();
            p.setBloodGroup(bgSel == null || bgSel.toString().isEmpty() ? null : bgSel.toString());
            p.setPhone(emptyToNull(phone.getText()));
            p.setEmail(emptyToNull(email.getText()));
            p.setAddress(emptyToNull(address.getText()));

            if (patient == null) {
                dao.insert(p);
                AppLogger.info("Patient added: " + p.getFullName());
            } else {
                dao.update(p);
                AppLogger.info("Patient updated: #" + p.getPatientId());
            }
            saved = true;
            dispose();
        } catch (ValidationException ve) {
            UiUtils.error(this, ve.getMessage());
        } catch (Exception ex) {
            AppLogger.error("Saving patient failed", ex);
            UiUtils.error(this, "Could not save patient:\n" + ex.getMessage());
        }
    }

    private String emptyToNull(String s) {
        return (s == null || s.trim().isEmpty()) ? null : s.trim();
    }

    public boolean isSaved() { return saved; }
}
