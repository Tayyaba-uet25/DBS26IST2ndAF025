package hms.model;

/** Domain class: a single line on a bill. */
public class BillItem {
    private int billItemId;
    private int billId;
    private String description;
    private String itemType = "Other";
    private int quantity = 1;
    private double unitPrice;
    private double amount;

    public BillItem() { }
    public BillItem(String description, String itemType, int quantity, double unitPrice) {
        this.description = description;
        this.itemType = itemType;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.amount = quantity * unitPrice;
    }

    public int getBillItemId() { return billItemId; }
    public void setBillItemId(int billItemId) { this.billItemId = billItemId; }
    public int getBillId() { return billId; }
    public void setBillId(int billId) { this.billId = billId; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getItemType() { return itemType; }
    public void setItemType(String itemType) { this.itemType = itemType; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public double getUnitPrice() { return unitPrice; }
    public void setUnitPrice(double unitPrice) { this.unitPrice = unitPrice; }
    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }
}
