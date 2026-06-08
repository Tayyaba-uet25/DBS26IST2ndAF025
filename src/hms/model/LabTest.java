package hms.model;

/** Domain class: lab test catalog entry. */
public class LabTest {
    private int testId;
    private String name;
    private double price;
    private String description;

    public LabTest() { }
    public LabTest(int testId, String name, double price) {
        this.testId = testId; this.name = name; this.price = price;
    }

    public int getTestId() { return testId; }
    public void setTestId(int testId) { this.testId = testId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    @Override public String toString() { return name + " (Rs " + price + ")"; }
}
