package hms.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/** Domain class: patient bill (with its items). */
public class Bill {
    private int billId;
    private int patientId;
    private String patientName;
    private LocalDateTime billDate;
    private double totalAmount;
    private double paidAmount;
    private String status;
    private String notes;
    private List<BillItem> items = new ArrayList<>();

    public Bill() { }

    public int getBillId() { return billId; }
    public void setBillId(int billId) { this.billId = billId; }
    public int getPatientId() { return patientId; }
    public void setPatientId(int patientId) { this.patientId = patientId; }
    public String getPatientName() { return patientName; }
    public void setPatientName(String patientName) { this.patientName = patientName; }
    public LocalDateTime getBillDate() { return billDate; }
    public void setBillDate(LocalDateTime billDate) { this.billDate = billDate; }
    public double getTotalAmount() { return totalAmount; }
    public void setTotalAmount(double totalAmount) { this.totalAmount = totalAmount; }
    public double getPaidAmount() { return paidAmount; }
    public void setPaidAmount(double paidAmount) { this.paidAmount = paidAmount; }
    public double getBalance() { return totalAmount - paidAmount; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public List<BillItem> getItems() { return items; }
    public void setItems(List<BillItem> items) { this.items = items; }
}
