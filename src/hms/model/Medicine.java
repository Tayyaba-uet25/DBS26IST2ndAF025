package hms.model;

import java.time.LocalDate;

/** Domain class: medicine / pharmacy stock item. */
public class Medicine {
    private int medicineId;
    private String name;
    private String manufacturer;
    private double unitPrice;
    private int stockQty;
    private int reorderLevel;
    private LocalDate expiryDate;

    public Medicine() { }

    public int getMedicineId() { return medicineId; }
    public void setMedicineId(int medicineId) { this.medicineId = medicineId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getManufacturer() { return manufacturer; }
    public void setManufacturer(String manufacturer) { this.manufacturer = manufacturer; }
    public double getUnitPrice() { return unitPrice; }
    public void setUnitPrice(double unitPrice) { this.unitPrice = unitPrice; }
    public int getStockQty() { return stockQty; }
    public void setStockQty(int stockQty) { this.stockQty = stockQty; }
    public int getReorderLevel() { return reorderLevel; }
    public void setReorderLevel(int reorderLevel) { this.reorderLevel = reorderLevel; }
    public LocalDate getExpiryDate() { return expiryDate; }
    public void setExpiryDate(LocalDate expiryDate) { this.expiryDate = expiryDate; }

    @Override public String toString() { return name + " (Rs " + unitPrice + ", stock " + stockQty + ")"; }
}
