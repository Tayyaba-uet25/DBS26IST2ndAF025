-- =====================================================================
--  SEED DATA  (sample records so the app + reports are not empty)
--  NOTE: password_hash uses SHA2(...,256) which matches the Java
--        PasswordUtil (lowercase hex SHA-256).  Default login below.
-- =====================================================================
USE hms;

-- Roles
INSERT INTO roles(role_name, description) VALUES
 ('Admin','Full system access'),
 ('Receptionist','Front desk operations'),
 ('Doctor','Clinical staff');

-- Users  (login: admin / admin123 , reception / recep123)
INSERT INTO users(username, password_hash, full_name, email, role_id) VALUES
 ('admin',     SHA2('admin123',256), 'System Administrator', 'admin@hms.local',    1),
 ('reception', SHA2('recep123',256), 'Front Desk',           'reception@hms.local',2);

-- Departments
INSERT INTO departments(name, location, phone) VALUES
 ('General Medicine','Block A, Floor 1','042-111-001'),
 ('Cardiology','Block B, Floor 2','042-111-002'),
 ('Orthopedics','Block C, Floor 1','042-111-003'),
 ('Pediatrics','Block A, Floor 2','042-111-004'),
 ('Emergency','Ground Floor','042-111-911');

-- Doctors
INSERT INTO doctors(first_name,last_name,gender,specialization,department_id,phone,email,consultation_fee,hire_date) VALUES
 ('Ahmed','Khan','Male','Physician',1,'0300-1112233','ahmed.khan@hms.local',1500,'2021-03-15'),
 ('Sara','Malik','Female','Cardiologist',2,'0301-2223344','sara.malik@hms.local',3000,'2020-07-01'),
 ('Bilal','Hussain','Male','Orthopedic Surgeon',3,'0302-3334455','bilal.h@hms.local',2500,'2019-11-20'),
 ('Ayesha','Raza','Female','Pediatrician',4,'0303-4445566','ayesha.raza@hms.local',2000,'2022-01-10'),
 ('Usman','Tariq','Male','Emergency Medicine',5,'0304-5556677','usman.tariq@hms.local',1800,'2023-05-05');

-- Patients
INSERT INTO patients(first_name,last_name,gender,date_of_birth,blood_group,phone,email,address) VALUES
 ('Ali','Hassan','Male','1990-04-12','B+','0311-1234567','ali.hassan@mail.com','12 Model Town, Lahore'),
 ('Fatima','Sheikh','Female','1985-09-30','O+','0312-2345678','fatima.s@mail.com','45 Gulberg, Lahore'),
 ('Hamza','Iqbal','Male','2015-02-18','A+','0313-3456789',NULL,'9 Johar Town, Lahore'),
 ('Zainab','Noor','Female','1998-12-05','AB+','0314-4567890','zainab.noor@mail.com','78 DHA, Lahore'),
 ('Omar','Farooq','Male','1972-06-25','O-','0315-5678901',NULL,'3 Cantt, Lahore');

-- Wards
INSERT INTO wards(name,ward_type,charge_per_day) VALUES
 ('General Ward A','General',2000),
 ('Private Room 1','Private',6000),
 ('ICU-1','ICU',12000),
 ('Emergency Bay','Emergency',3000);

-- Beds
INSERT INTO beds(bed_number,ward_id,status) VALUES
 ('GA-01',1,'Available'),('GA-02',1,'Available'),('GA-03',1,'Available'),
 ('PR-01',2,'Available'),('PR-02',2,'Available'),
 ('ICU-01',3,'Available'),('ICU-02',3,'Available'),
 ('ER-01',4,'Available'),('ER-02',4,'Available');

-- Medicines
INSERT INTO medicines(name,manufacturer,unit_price,stock_qty,reorder_level,expiry_date) VALUES
 ('Paracetamol 500mg','GSK',5.00,500,50,'2027-12-31'),
 ('Amoxicillin 250mg','Abbott',12.00,200,40,'2026-10-31'),
 ('Ibuprofen 400mg','Searle',8.00,30,40,'2027-06-30'),
 ('Insulin (vial)','Novo',850.00,15,20,'2026-08-31'),
 ('ORS Sachet','Hilton',20.00,300,50,'2028-01-31');

-- Lab tests
INSERT INTO lab_tests(name,price,description) VALUES
 ('CBC','600','Complete Blood Count'),
 ('Blood Sugar (Fasting)','300','Fasting glucose level'),
 ('X-Ray Chest','1200','Chest radiograph'),
 ('ECG','800','Electrocardiogram'),
 ('Lipid Profile','1500','Cholesterol panel');

-- Appointments
INSERT INTO appointments(patient_id,doctor_id,appointment_date,reason,status,fee) VALUES
 (1,1,'2026-06-02 10:00:00','Fever and cough','Scheduled',1500),
 (2,2,'2026-06-02 11:30:00','Chest pain','Scheduled',3000),
 (3,4,'2026-06-03 09:15:00','Routine checkup','Scheduled',2000),
 (4,1,'2026-05-28 14:00:00','Headache','Completed',1500),
 (5,3,'2026-05-29 16:00:00','Knee pain','Completed',2500);

-- A couple of bills with items (totals are maintained by triggers)
INSERT INTO bills(patient_id,notes) VALUES (4,'Consultation + Lab');
INSERT INTO bill_items(bill_id,description,item_type,quantity,unit_price,amount) VALUES
 (LAST_INSERT_ID(),'Consultation - Dr. Ahmed Khan','Consultation',1,1500,1500);
INSERT INTO bill_items(bill_id,description,item_type,quantity,unit_price,amount) VALUES
 (1,'CBC Test','Lab',1,600,600);

INSERT INTO bills(patient_id,notes) VALUES (5,'Consultation');
INSERT INTO bill_items(bill_id,description,item_type,quantity,unit_price,amount) VALUES
 (2,'Consultation - Dr. Bilal Hussain','Consultation',1,2500,2500);

-- A payment (trigger marks bill paid/partial)
INSERT INTO payments(bill_id,amount,method,reference_no) VALUES (1,1000,'Cash','RCP-0001');

-- A prescription with items
INSERT INTO prescriptions(patient_id,doctor_id,notes) VALUES (1,1,'Take rest, plenty of fluids');
INSERT INTO prescription_items(prescription_id,medicine_id,dosage,quantity,instructions) VALUES
 (LAST_INSERT_ID(),1,'1 tablet','10','After meals, 3 times a day'),
 (1,5,'1 sachet','5','Dissolve in water');

-- A lab order
INSERT INTO lab_orders(patient_id,doctor_id,test_id,status,result) VALUES
 (4,1,1,'Completed','Normal'),
 (2,2,4,'Pending',NULL);
