package hms.model;

/** Domain class: bed within a ward. */
public class Bed {
    private int bedId;
    private String bedNumber;
    private int wardId;
    private String wardName;
    private String status;

    public Bed() { }

    public int getBedId() { return bedId; }
    public void setBedId(int bedId) { this.bedId = bedId; }
    public String getBedNumber() { return bedNumber; }
    public void setBedNumber(String bedNumber) { this.bedNumber = bedNumber; }
    public int getWardId() { return wardId; }
    public void setWardId(int wardId) { this.wardId = wardId; }
    public String getWardName() { return wardName; }
    public void setWardName(String wardName) { this.wardName = wardName; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    @Override public String toString() {
        return bedNumber + (wardName != null ? " - " + wardName : "") + " [" + status + "]";
    }
}
