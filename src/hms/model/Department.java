package hms.model;

/** Domain class: hospital department. */
public class Department {
    private int departmentId;
    private String name;
    private String location;
    private String phone;

    public Department() { }
    public Department(int departmentId, String name) {
        this.departmentId = departmentId;
        this.name = name;
    }

    public int getDepartmentId() { return departmentId; }
    public void setDepartmentId(int departmentId) { this.departmentId = departmentId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    @Override public String toString() { return name; }
}
