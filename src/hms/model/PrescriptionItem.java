package hms.model;

/** Domain class: a single medicine line on a prescription. */
public class PrescriptionItem {
    private int itemId;
    private int prescriptionId;
    private int medicineId;
    private String medicineName;
    private String dosage;
    private int quantity;
    private String instructions;

    public PrescriptionItem() { }

    public int getItemId() { return itemId; }
    public void setItemId(int itemId) { this.itemId = itemId; }
    public int getPrescriptionId() { return prescriptionId; }
    public void setPrescriptionId(int prescriptionId) { this.prescriptionId = prescriptionId; }
    public int getMedicineId() { return medicineId; }
    public void setMedicineId(int medicineId) { this.medicineId = medicineId; }
    public String getMedicineName() { return medicineName; }
    public void setMedicineName(String medicineName) { this.medicineName = medicineName; }
    public String getDosage() { return dosage; }
    public void setDosage(String dosage) { this.dosage = dosage; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public String getInstructions() { return instructions; }
    public void setInstructions(String instructions) { this.instructions = instructions; }
}
