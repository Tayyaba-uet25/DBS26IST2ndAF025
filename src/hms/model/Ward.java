package hms.model;

/** Domain class: ward. */
public class Ward {
    private int wardId;
    private String name;
    private String wardType;
    private double chargePerDay;

    public Ward() { }
    public Ward(int wardId, String name) { this.wardId = wardId; this.name = name; }

    public int getWardId() { return wardId; }
    public void setWardId(int wardId) { this.wardId = wardId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getWardType() { return wardType; }
    public void setWardType(String wardType) { this.wardType = wardType; }
    public double getChargePerDay() { return chargePerDay; }
    public void setChargePerDay(double chargePerDay) { this.chargePerDay = chargePerDay; }

    @Override public String toString() { return name + " (" + wardType + ")"; }
}
