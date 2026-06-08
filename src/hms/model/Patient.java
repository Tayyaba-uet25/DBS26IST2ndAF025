package hms.model;

import java.time.LocalDate;

/** Domain class: patient. */
public class Patient {
    private int patientId;
    private String firstName;
    private String lastName;
    private String gender;
    private LocalDate dateOfBirth;
    private String bloodGroup;
    private String phone;
    private String email;
    private String address;

    public Patient() { }

    public int getPatientId() { return patientId; }
    public void setPatientId(int patientId) { this.patientId = patientId; }
    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }
    public LocalDate getDateOfBirth() { return dateOfBirth; }
    public void setDateOfBirth(LocalDate dateOfBirth) { this.dateOfBirth = dateOfBirth; }
    public String getBloodGroup() { return bloodGroup; }
    public void setBloodGroup(String bloodGroup) { this.bloodGroup = bloodGroup; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getFullName() { return firstName + " " + lastName; }

    @Override public String toString() { return getFullName() + " (#" + patientId + ")"; }
}
