-- =====================================================================
--  VIEWS  (requirement: at least 5)   -  total: 8
-- =====================================================================
USE hms;

-- 1. Appointment details with patient, doctor and department names
CREATE OR REPLACE VIEW vw_appointment_details AS
SELECT a.appointment_id,
       CONCAT(p.first_name,' ',p.last_name) AS patient_name,
       CONCAT('Dr. ',d.first_name,' ',d.last_name) AS doctor_name,
       dep.name AS department,
       a.appointment_date,
       a.reason,
       a.status,
       a.fee
FROM appointments a
JOIN patients p   ON a.patient_id = p.patient_id
JOIN doctors  d   ON a.doctor_id  = d.doctor_id
LEFT JOIN departments dep ON d.department_id = dep.department_id;

-- 2. Doctor directory with department
CREATE OR REPLACE VIEW vw_doctor_directory AS
SELECT d.doctor_id,
       CONCAT('Dr. ',d.first_name,' ',d.last_name) AS doctor_name,
       d.gender,
       d.specialization,
       dep.name AS department,
       d.phone,
       d.email,
       d.consultation_fee,
       CASE WHEN d.is_active=1 THEN 'Active' ELSE 'Inactive' END AS status
FROM doctors d
LEFT JOIN departments dep ON d.department_id = dep.department_id;

-- 3. Patient billing summary (total billed / paid / balance)
CREATE OR REPLACE VIEW vw_patient_billing_summary AS
SELECT p.patient_id,
       CONCAT(p.first_name,' ',p.last_name) AS patient_name,
       COUNT(b.bill_id) AS total_bills,
       COALESCE(SUM(b.total_amount),0) AS total_billed,
       COALESCE(SUM(b.paid_amount),0)  AS total_paid,
       COALESCE(SUM(b.total_amount - b.paid_amount),0) AS balance
FROM patients p
LEFT JOIN bills b ON p.patient_id = b.patient_id
GROUP BY p.patient_id, patient_name;

-- 4. Admission details (patient, ward, bed, doctor)
CREATE OR REPLACE VIEW vw_admission_details AS
SELECT adm.admission_id,
       CONCAT(p.first_name,' ',p.last_name) AS patient_name,
       w.name AS ward_name,
       bd.bed_number,
       CONCAT('Dr. ',d.first_name,' ',d.last_name) AS doctor_name,
       adm.admit_date,
       adm.discharge_date,
       adm.status,
       adm.diagnosis
FROM admissions adm
JOIN patients p ON adm.patient_id = p.patient_id
JOIN beds bd    ON adm.bed_id = bd.bed_id
JOIN wards w    ON bd.ward_id = w.ward_id
JOIN doctors d  ON adm.doctor_id = d.doctor_id;

-- 5. Prescription details (item level)
CREATE OR REPLACE VIEW vw_prescription_details AS
SELECT pr.prescription_id,
       CONCAT(p.first_name,' ',p.last_name) AS patient_name,
       CONCAT('Dr. ',d.first_name,' ',d.last_name) AS doctor_name,
       pr.prescribed_date,
       m.name AS medicine,
       pi.dosage,
       pi.quantity,
       pi.instructions
FROM prescriptions pr
JOIN patients p ON pr.patient_id = p.patient_id
JOIN doctors d  ON pr.doctor_id  = d.doctor_id
JOIN prescription_items pi ON pr.prescription_id = pi.prescription_id
JOIN medicines m ON pi.medicine_id = m.medicine_id;

-- 6. Revenue by department (from completed/paid appointment fees)
CREATE OR REPLACE VIEW vw_revenue_by_department AS
SELECT dep.department_id,
       dep.name AS department,
       COUNT(a.appointment_id) AS appointments,
       COALESCE(SUM(a.fee),0) AS total_fee
FROM departments dep
LEFT JOIN doctors d ON dep.department_id = d.department_id
LEFT JOIN appointments a ON d.doctor_id = a.doctor_id
GROUP BY dep.department_id, dep.name;

-- 7. Low stock medicines
CREATE OR REPLACE VIEW vw_low_stock_medicines AS
SELECT medicine_id, name, manufacturer, stock_qty, reorder_level, unit_price, expiry_date
FROM medicines
WHERE stock_qty <= reorder_level;

-- 8. Ward occupancy
CREATE OR REPLACE VIEW vw_ward_occupancy AS
SELECT w.ward_id,
       w.name AS ward_name,
       w.ward_type,
       COUNT(b.bed_id) AS total_beds,
       SUM(CASE WHEN b.status='Occupied' THEN 1 ELSE 0 END) AS occupied_beds,
       SUM(CASE WHEN b.status='Available' THEN 1 ELSE 0 END) AS available_beds,
       w.charge_per_day
FROM wards w
LEFT JOIN beds b ON w.ward_id = b.ward_id
GROUP BY w.ward_id, w.name, w.ward_type, w.charge_per_day;
