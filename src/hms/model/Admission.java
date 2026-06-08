package hms.model;

import java.time.LocalDateTime;

/** Domain class: in-patient admission. */
public class Admission {
    private int admissionId;
    private int patientId;
    private String patientName;
    private int bedId;
    private String bedInfo;
    private int doctorId;
    private String doctorName;
    private LocalDateTime admitDate;
    private LocalDateTime dischargeDate;
    private String status;
    private String diagnosis;

    public Admission() { }

    public int getAdmissionId() { return admissionId; }
    public void setAdmissionId(int admissionId) { this.admissionId = admissionId; }
    public int getPatientId() { return patientId; }
    public void setPatientId(int patientId) { this.patientId = patientId; }
    public String getPatientName() { return patientName; }
    public void setPatientName(String patientName) { this.patientName = patientName; }
    public int getBedId() { return bedId; }
    public void setBedId(int bedId) { this.bedId = bedId; }
    public String getBedInfo() { return bedInfo; }
    public void setBedInfo(String bedInfo) { this.bedInfo = bedInfo; }
    public int getDoctorId() { return doctorId; }
    public void setDoctorId(int doctorId) { this.doctorId = doctorId; }
    public String getDoctorName() { return doctorName; }
    public void setDoctorName(String doctorName) { this.doctorName = doctorName; }
    public LocalDateTime getAdmitDate() { return admitDate; }
    public void setAdmitDate(LocalDateTime admitDate) { this.admitDate = admitDate; }
    public LocalDateTime getDischargeDate() { return dischargeDate; }
    public void setDischargeDate(LocalDateTime dischargeDate) { this.dischargeDate = dischargeDate; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getDiagnosis() { return diagnosis; }
    public void setDiagnosis(String diagnosis) { this.diagnosis = diagnosis; }
}
