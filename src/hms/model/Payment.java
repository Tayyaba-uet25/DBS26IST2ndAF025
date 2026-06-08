package hms.model;

import java.time.LocalDateTime;

/** Domain class: payment against a bill. */
public class Payment {
    private int paymentId;
    private int billId;
    private double amount;
    private LocalDateTime paymentDate;
    private String method = "Cash";
    private String referenceNo;

    public Payment() { }

    public int getPaymentId() { return paymentId; }
    public void setPaymentId(int paymentId) { this.paymentId = paymentId; }
    public int getBillId() { return billId; }
    public void setBillId(int billId) { this.billId = billId; }
    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }
    public LocalDateTime getPaymentDate() { return paymentDate; }
    public void setPaymentDate(LocalDateTime paymentDate) { this.paymentDate = paymentDate; }
    public String getMethod() { return method; }
    public void setMethod(String method) { this.method = method; }
    public String getReferenceNo() { return referenceNo; }
    public void setReferenceNo(String referenceNo) { this.referenceNo = referenceNo; }
}
