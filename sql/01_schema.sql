-- =====================================================================
--  HOSPITAL MANAGEMENT SYSTEM  -  DATABASE SCHEMA
--  Course: CSC-104L Database System Lab - Semester Final Project
--  Engine: MySQL 8.0 (InnoDB for FK + transaction support)
--  This file: 18 tables + 10+ named constraints (PK, FK, UNIQUE,
--             CHECK, NOT NULL, DEFAULT)
-- =====================================================================

DROP DATABASE IF EXISTS hms;
CREATE DATABASE hms CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE hms;

-- ------------------------------------------------------------------
-- 1. roles
-- ------------------------------------------------------------------
CREATE TABLE roles (
    role_id     INT AUTO_INCREMENT PRIMARY KEY,
    role_name   VARCHAR(40) NOT NULL,
    description VARCHAR(150),
    CONSTRAINT uq_role_name UNIQUE (role_name)
) ENGINE=InnoDB;

-- ------------------------------------------------------------------
-- 2. users  (login accounts)
-- ------------------------------------------------------------------
CREATE TABLE users (
    user_id       INT AUTO_INCREMENT PRIMARY KEY,
    username      VARCHAR(40)  NOT NULL,
    password_hash VARCHAR(100) NOT NULL,
    full_name     VARCHAR(80)  NOT NULL,
    email         VARCHAR(120),
    role_id       INT          NOT NULL,
    is_active     TINYINT      NOT NULL DEFAULT 1,
    created_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_username UNIQUE (username),
    CONSTRAINT uq_user_email UNIQUE (email),
    CONSTRAINT fk_user_role FOREIGN KEY (role_id) REFERENCES roles(role_id),
    CONSTRAINT chk_user_active CHECK (is_active IN (0,1))
) ENGINE=InnoDB;

-- ------------------------------------------------------------------
-- 3. departments
-- ------------------------------------------------------------------
CREATE TABLE departments (
    department_id INT AUTO_INCREMENT PRIMARY KEY,
    name          VARCHAR(80) NOT NULL,
    location      VARCHAR(80),
    phone         VARCHAR(20),
    CONSTRAINT uq_department_name UNIQUE (name)
) ENGINE=InnoDB;

-- ------------------------------------------------------------------
-- 4. doctors
-- ------------------------------------------------------------------
CREATE TABLE doctors (
    doctor_id        INT AUTO_INCREMENT PRIMARY KEY,
    first_name       VARCHAR(50) NOT NULL,
    last_name        VARCHAR(50) NOT NULL,
    gender           VARCHAR(10) NOT NULL,
    specialization   VARCHAR(80),
    department_id    INT,
    phone            VARCHAR(20),
    email            VARCHAR(120),
    consultation_fee DECIMAL(10,2) NOT NULL DEFAULT 0,
    hire_date        DATE,
    is_active        TINYINT NOT NULL DEFAULT 1,
    CONSTRAINT uq_doctor_email UNIQUE (email),
    CONSTRAINT fk_doctor_department FOREIGN KEY (department_id) REFERENCES departments(department_id),
    CONSTRAINT chk_doctor_gender CHECK (gender IN ('Male','Female','Other')),
    CONSTRAINT chk_doctor_fee CHECK (consultation_fee >= 0)
) ENGINE=InnoDB;

-- ------------------------------------------------------------------
-- 5. patients
-- ------------------------------------------------------------------
CREATE TABLE patients (
    patient_id    INT AUTO_INCREMENT PRIMARY KEY,
    first_name    VARCHAR(50) NOT NULL,
    last_name     VARCHAR(50) NOT NULL,
    gender        VARCHAR(10) NOT NULL,
    date_of_birth DATE,
    blood_group   VARCHAR(5),
    phone         VARCHAR(20),
    email         VARCHAR(120),
    address       TEXT,
    registered_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_patient_gender CHECK (gender IN ('Male','Female','Other')),
    CONSTRAINT chk_blood_group CHECK (blood_group IN ('A+','A-','B+','B-','AB+','AB-','O+','O-') OR blood_group IS NULL)
) ENGINE=InnoDB;

-- ------------------------------------------------------------------
-- 6. wards
-- ------------------------------------------------------------------
CREATE TABLE wards (
    ward_id        INT AUTO_INCREMENT PRIMARY KEY,
    name           VARCHAR(60) NOT NULL,
    ward_type      VARCHAR(20) NOT NULL DEFAULT 'General',
    charge_per_day DECIMAL(10,2) NOT NULL DEFAULT 0,
    CONSTRAINT uq_ward_name UNIQUE (name),
    CONSTRAINT chk_ward_type CHECK (ward_type IN ('General','Private','ICU','Emergency')),
    CONSTRAINT chk_ward_charge CHECK (charge_per_day >= 0)
) ENGINE=InnoDB;

-- ------------------------------------------------------------------
-- 7. beds
-- ------------------------------------------------------------------
CREATE TABLE beds (
    bed_id     INT AUTO_INCREMENT PRIMARY KEY,
    bed_number VARCHAR(20) NOT NULL,
    ward_id    INT NOT NULL,
    status     VARCHAR(15) NOT NULL DEFAULT 'Available',
    CONSTRAINT fk_bed_ward FOREIGN KEY (ward_id) REFERENCES wards(ward_id),
    CONSTRAINT uq_bed_ward UNIQUE (ward_id, bed_number),
    CONSTRAINT chk_bed_status CHECK (status IN ('Available','Occupied','Maintenance'))
) ENGINE=InnoDB;

-- ------------------------------------------------------------------
-- 8. appointments
-- ------------------------------------------------------------------
CREATE TABLE appointments (
    appointment_id   INT AUTO_INCREMENT PRIMARY KEY,
    patient_id       INT NOT NULL,
    doctor_id        INT NOT NULL,
    appointment_date DATETIME NOT NULL,
    reason           VARCHAR(200),
    status           VARCHAR(15) NOT NULL DEFAULT 'Scheduled',
    fee              DECIMAL(10,2) NOT NULL DEFAULT 0,
    created_at       DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_appt_patient FOREIGN KEY (patient_id) REFERENCES patients(patient_id),
    CONSTRAINT fk_appt_doctor  FOREIGN KEY (doctor_id)  REFERENCES doctors(doctor_id),
    CONSTRAINT chk_appt_status CHECK (status IN ('Scheduled','Completed','Cancelled')),
    CONSTRAINT chk_appt_fee CHECK (fee >= 0)
) ENGINE=InnoDB;

-- ------------------------------------------------------------------
-- 9. admissions
-- ------------------------------------------------------------------
CREATE TABLE admissions (
    admission_id   INT AUTO_INCREMENT PRIMARY KEY,
    patient_id     INT NOT NULL,
    bed_id         INT NOT NULL,
    doctor_id      INT NOT NULL,
    admit_date     DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    discharge_date DATETIME,
    status         VARCHAR(15) NOT NULL DEFAULT 'Admitted',
    diagnosis      TEXT,
    CONSTRAINT fk_adm_patient FOREIGN KEY (patient_id) REFERENCES patients(patient_id),
    CONSTRAINT fk_adm_bed     FOREIGN KEY (bed_id)     REFERENCES beds(bed_id),
    CONSTRAINT fk_adm_doctor  FOREIGN KEY (doctor_id)  REFERENCES doctors(doctor_id),
    CONSTRAINT chk_adm_status CHECK (status IN ('Admitted','Discharged'))
) ENGINE=InnoDB;

-- ------------------------------------------------------------------
-- 10. medicines
-- ------------------------------------------------------------------
CREATE TABLE medicines (
    medicine_id   INT AUTO_INCREMENT PRIMARY KEY,
    name          VARCHAR(100) NOT NULL,
    manufacturer  VARCHAR(100),
    unit_price    DECIMAL(10,2) NOT NULL DEFAULT 0,
    stock_qty     INT NOT NULL DEFAULT 0,
    reorder_level INT NOT NULL DEFAULT 10,
    expiry_date   DATE,
    CONSTRAINT uq_medicine_name UNIQUE (name),
    CONSTRAINT chk_medicine_price CHECK (unit_price >= 0),
    CONSTRAINT chk_medicine_stock CHECK (stock_qty >= 0)
) ENGINE=InnoDB;

-- ------------------------------------------------------------------
-- 11. prescriptions
-- ------------------------------------------------------------------
CREATE TABLE prescriptions (
    prescription_id INT AUTO_INCREMENT PRIMARY KEY,
    patient_id      INT NOT NULL,
    doctor_id       INT NOT NULL,
    prescribed_date DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    notes           TEXT,
    CONSTRAINT fk_presc_patient FOREIGN KEY (patient_id) REFERENCES patients(patient_id),
    CONSTRAINT fk_presc_doctor  FOREIGN KEY (doctor_id)  REFERENCES doctors(doctor_id)
) ENGINE=InnoDB;

-- ------------------------------------------------------------------
-- 12. prescription_items
-- ------------------------------------------------------------------
CREATE TABLE prescription_items (
    item_id         INT AUTO_INCREMENT PRIMARY KEY,
    prescription_id INT NOT NULL,
    medicine_id     INT NOT NULL,
    dosage          VARCHAR(60),
    quantity        INT NOT NULL DEFAULT 1,
    instructions    VARCHAR(200),
    CONSTRAINT fk_pi_prescription FOREIGN KEY (prescription_id) REFERENCES prescriptions(prescription_id) ON DELETE CASCADE,
    CONSTRAINT fk_pi_medicine     FOREIGN KEY (medicine_id)     REFERENCES medicines(medicine_id),
    CONSTRAINT chk_pi_qty CHECK (quantity > 0)
) ENGINE=InnoDB;

-- ------------------------------------------------------------------
-- 13. lab_tests
-- ------------------------------------------------------------------
CREATE TABLE lab_tests (
    test_id     INT AUTO_INCREMENT PRIMARY KEY,
    name        VARCHAR(100) NOT NULL,
    price       DECIMAL(10,2) NOT NULL DEFAULT 0,
    description VARCHAR(200),
    CONSTRAINT uq_labtest_name UNIQUE (name),
    CONSTRAINT chk_labtest_price CHECK (price >= 0)
) ENGINE=InnoDB;

-- ------------------------------------------------------------------
-- 14. lab_orders
-- ------------------------------------------------------------------
CREATE TABLE lab_orders (
    order_id   INT AUTO_INCREMENT PRIMARY KEY,
    patient_id INT NOT NULL,
    doctor_id  INT NOT NULL,
    test_id    INT NOT NULL,
    order_date DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    status     VARCHAR(15) NOT NULL DEFAULT 'Pending',
    result     TEXT,
    CONSTRAINT fk_lo_patient FOREIGN KEY (patient_id) REFERENCES patients(patient_id),
    CONSTRAINT fk_lo_doctor  FOREIGN KEY (doctor_id)  REFERENCES doctors(doctor_id),
    CONSTRAINT fk_lo_test    FOREIGN KEY (test_id)    REFERENCES lab_tests(test_id),
    CONSTRAINT chk_lo_status CHECK (status IN ('Pending','Completed','Cancelled'))
) ENGINE=InnoDB;

-- ------------------------------------------------------------------
-- 15. bills
-- ------------------------------------------------------------------
CREATE TABLE bills (
    bill_id      INT AUTO_INCREMENT PRIMARY KEY,
    patient_id   INT NOT NULL,
    bill_date    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    total_amount DECIMAL(12,2) NOT NULL DEFAULT 0,
    paid_amount  DECIMAL(12,2) NOT NULL DEFAULT 0,
    status       VARCHAR(15) NOT NULL DEFAULT 'Unpaid',
    notes        VARCHAR(200),
    CONSTRAINT fk_bill_patient FOREIGN KEY (patient_id) REFERENCES patients(patient_id),
    CONSTRAINT chk_bill_total CHECK (total_amount >= 0),
    CONSTRAINT chk_bill_paid  CHECK (paid_amount >= 0),
    CONSTRAINT chk_bill_status CHECK (status IN ('Unpaid','Partial','Paid'))
) ENGINE=InnoDB;

-- ------------------------------------------------------------------
-- 16. bill_items
-- ------------------------------------------------------------------
CREATE TABLE bill_items (
    bill_item_id INT AUTO_INCREMENT PRIMARY KEY,
    bill_id      INT NOT NULL,
    description  VARCHAR(150) NOT NULL,
    item_type    VARCHAR(20)  NOT NULL DEFAULT 'Other',
    quantity     INT NOT NULL DEFAULT 1,
    unit_price   DECIMAL(10,2) NOT NULL DEFAULT 0,
    amount       DECIMAL(12,2) NOT NULL DEFAULT 0,
    CONSTRAINT fk_bi_bill FOREIGN KEY (bill_id) REFERENCES bills(bill_id) ON DELETE CASCADE,
    CONSTRAINT chk_bi_type CHECK (item_type IN ('Consultation','Lab','Medicine','Ward','Other')),
    CONSTRAINT chk_bi_qty CHECK (quantity > 0),
    CONSTRAINT chk_bi_unitprice CHECK (unit_price >= 0),
    CONSTRAINT chk_bi_amount CHECK (amount >= 0)
) ENGINE=InnoDB;

-- ------------------------------------------------------------------
-- 17. payments
-- ------------------------------------------------------------------
CREATE TABLE payments (
    payment_id   INT AUTO_INCREMENT PRIMARY KEY,
    bill_id      INT NOT NULL,
    amount       DECIMAL(12,2) NOT NULL,
    payment_date DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    method       VARCHAR(15) NOT NULL DEFAULT 'Cash',
    reference_no VARCHAR(50),
    CONSTRAINT fk_pay_bill FOREIGN KEY (bill_id) REFERENCES bills(bill_id),
    CONSTRAINT chk_pay_amount CHECK (amount > 0),
    CONSTRAINT chk_pay_method CHECK (method IN ('Cash','Card','Online'))
) ENGINE=InnoDB;

-- ------------------------------------------------------------------
-- 18. audit_log  (written by triggers)
-- ------------------------------------------------------------------
CREATE TABLE audit_log (
    log_id       INT AUTO_INCREMENT PRIMARY KEY,
    entity       VARCHAR(40) NOT NULL,
    action       VARCHAR(40) NOT NULL,
    details      VARCHAR(255),
    log_time     DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    performed_by VARCHAR(60) DEFAULT 'system'
) ENGINE=InnoDB;

-- Helpful indexes
CREATE INDEX idx_appt_date ON appointments(appointment_date);
CREATE INDEX idx_bill_patient ON bills(patient_id);
CREATE INDEX idx_payment_date ON payments(payment_date);
