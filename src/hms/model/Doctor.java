package hms.model;

import java.time.LocalDate;

/** Domain class: doctor. */
public class Doctor {
    private int doctorId;
    private String firstName;
    private String lastName;
    private String gender;
    private String specialization;
    private int departmentId;
    private String departmentName;
    private String phone;
    private String email;
    private double consultationFee;
    private LocalDate hireDate;
    private boolean active = true;

    public Doctor() { }

    public int getDoctorId() { return doctorId; }
    public void setDoctorId(int doctorId) { this.doctorId = doctorId; }
    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }
    public String getSpecialization() { return specialization; }
    public void setSpecialization(String specialization) { this.specialization = specialization; }
    public int getDepartmentId() { return departmentId; }
    public void setDepartmentId(int departmentId) { this.departmentId = departmentId; }
    public String getDepartmentName() { return departmentName; }
    public void setDepartmentName(String departmentName) { this.departmentName = departmentName; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public double getConsultationFee() { return consultationFee; }
    public void setConsultationFee(double consultationFee) { this.consultationFee = consultationFee; }
    public LocalDate getHireDate() { return hireDate; }
    public void setHireDate(LocalDate hireDate) { this.hireDate = hireDate; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public String getFullName() { return "Dr. " + firstName + " " + lastName; }

    @Override public String toString() { return getFullName(); }
}
