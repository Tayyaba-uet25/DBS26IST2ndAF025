package hms.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/** Domain class: prescription header (with its items). */
public class Prescription {
    private int prescriptionId;
    private int patientId;
    private String patientName;
    private int doctorId;
    private String doctorName;
    private LocalDateTime prescribedDate;
    private String notes;
    private List<PrescriptionItem> items = new ArrayList<>();

    public Prescription() { }

    public int getPrescriptionId() { return prescriptionId; }
    public void setPrescriptionId(int prescriptionId) { this.prescriptionId = prescriptionId; }
    public int getPatientId() { return patientId; }
    public void setPatientId(int patientId) { this.patientId = patientId; }
    public String getPatientName() { return patientName; }
    public void setPatientName(String patientName) { this.patientName = patientName; }
    public int getDoctorId() { return doctorId; }
    public void setDoctorId(int doctorId) { this.doctorId = doctorId; }
    public String getDoctorName() { return doctorName; }
    public void setDoctorName(String doctorName) { this.doctorName = doctorName; }
    public LocalDateTime getPrescribedDate() { return prescribedDate; }
    public void setPrescribedDate(LocalDateTime prescribedDate) { this.prescribedDate = prescribedDate; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public List<PrescriptionItem> getItems() { return items; }
    public void setItems(List<PrescriptionItem> items) { this.items = items; }
}
