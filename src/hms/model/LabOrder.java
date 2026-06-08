package hms.model;

import java.time.LocalDateTime;

/** Domain class: lab test order for a patient. */
public class LabOrder {
    private int orderId;
    private int patientId;
    private String patientName;
    private int doctorId;
    private String doctorName;
    private int testId;
    private String testName;
    private LocalDateTime orderDate;
    private String status;
    private String result;

    public LabOrder() { }

    public int getOrderId() { return orderId; }
    public void setOrderId(int orderId) { this.orderId = orderId; }
    public int getPatientId() { return patientId; }
    public void setPatientId(int patientId) { this.patientId = patientId; }
    public String getPatientName() { return patientName; }
    public void setPatientName(String patientName) { this.patientName = patientName; }
    public int getDoctorId() { return doctorId; }
    public void setDoctorId(int doctorId) { this.doctorId = doctorId; }
    public String getDoctorName() { return doctorName; }
    public void setDoctorName(String doctorName) { this.doctorName = doctorName; }
    public int getTestId() { return testId; }
    public void setTestId(int testId) { this.testId = testId; }
    public String getTestName() { return testName; }
    public void setTestName(String testName) { this.testName = testName; }
    public LocalDateTime getOrderDate() { return orderDate; }
    public void setOrderDate(LocalDateTime orderDate) { this.orderDate = orderDate; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getResult() { return result; }
    public void setResult(String result) { this.result = result; }
}
