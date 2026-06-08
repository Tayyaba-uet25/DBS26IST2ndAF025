-- =====================================================================
--  STORED PROCEDURES (>=3, here 5) + TRIGGERS (>=2, here 4)
-- =====================================================================
USE hms;

-- =====================  TRIGGERS  ====================================
DROP TRIGGER IF EXISTS trg_billitem_after_insert;
DROP TRIGGER IF EXISTS trg_billitem_after_delete;
DROP TRIGGER IF EXISTS trg_payment_after_insert;
DROP TRIGGER IF EXISTS trg_payment_after_delete;
DROP TRIGGER IF EXISTS trg_medicine_after_update;
DROP TRIGGER IF EXISTS trg_appointment_after_insert;

DELIMITER //

-- 1. When a bill item is inserted -> recompute bill total + status
CREATE TRIGGER trg_billitem_after_insert
AFTER INSERT ON bill_items
FOR EACH ROW
BEGIN
    UPDATE bills
       SET total_amount = (SELECT COALESCE(SUM(amount),0) FROM bill_items WHERE bill_id = NEW.bill_id)
     WHERE bill_id = NEW.bill_id;

    UPDATE bills
       SET status = CASE
                       WHEN paid_amount >= total_amount AND total_amount > 0 THEN 'Paid'
                       WHEN paid_amount > 0 THEN 'Partial'
                       ELSE 'Unpaid'
                    END
     WHERE bill_id = NEW.bill_id;
END//

-- 2. When a bill item is deleted -> recompute bill total
CREATE TRIGGER trg_billitem_after_delete
AFTER DELETE ON bill_items
FOR EACH ROW
BEGIN
    UPDATE bills
       SET total_amount = (SELECT COALESCE(SUM(amount),0) FROM bill_items WHERE bill_id = OLD.bill_id)
     WHERE bill_id = OLD.bill_id;
END//

-- 3. When a payment is inserted -> update paid amount + status on the bill
CREATE TRIGGER trg_payment_after_insert
AFTER INSERT ON payments
FOR EACH ROW
BEGIN
    UPDATE bills
       SET paid_amount = (SELECT COALESCE(SUM(amount),0) FROM payments WHERE bill_id = NEW.bill_id)
     WHERE bill_id = NEW.bill_id;

    UPDATE bills
       SET status = CASE
                       WHEN paid_amount >= total_amount AND total_amount > 0 THEN 'Paid'
                       WHEN paid_amount > 0 THEN 'Partial'
                       ELSE 'Unpaid'
                    END
     WHERE bill_id = NEW.bill_id;
END//

-- 3b. When a payment is DELETED -> recompute paid amount + status on the bill
--     (so removing a payment, from the app OR by raw SQL, keeps the bill correct)
CREATE TRIGGER trg_payment_after_delete
AFTER DELETE ON payments
FOR EACH ROW
BEGIN
    UPDATE bills
       SET paid_amount = (SELECT COALESCE(SUM(amount),0) FROM payments WHERE bill_id = OLD.bill_id)
     WHERE bill_id = OLD.bill_id;

    UPDATE bills
       SET status = CASE
                       WHEN paid_amount >= total_amount AND total_amount > 0 THEN 'Paid'
                       WHEN paid_amount > 0 THEN 'Partial'
                       ELSE 'Unpaid'
                    END
     WHERE bill_id = OLD.bill_id;
END//

-- 4. Audit medicine stock changes
CREATE TRIGGER trg_medicine_after_update
AFTER UPDATE ON medicines
FOR EACH ROW
BEGIN
    IF OLD.stock_qty <> NEW.stock_qty THEN
        INSERT INTO audit_log(entity, action, details)
        VALUES ('medicines','STOCK_CHANGE',
                CONCAT('Medicine #',NEW.medicine_id,' (',NEW.name,') stock ',
                       OLD.stock_qty,' -> ',NEW.stock_qty));
    END IF;
END//

-- 5. Audit new appointments
CREATE TRIGGER trg_appointment_after_insert
AFTER INSERT ON appointments
FOR EACH ROW
BEGIN
    INSERT INTO audit_log(entity, action, details)
    VALUES ('appointments','INSERT',
            CONCAT('Appointment #',NEW.appointment_id,' for patient #',NEW.patient_id,
                   ' with doctor #',NEW.doctor_id));
END//

DELIMITER ;

-- =====================  STORED PROCEDURES  ===========================
DROP PROCEDURE IF EXISTS sp_admit_patient;
DROP PROCEDURE IF EXISTS sp_discharge_patient;
DROP PROCEDURE IF EXISTS sp_record_payment;
DROP PROCEDURE IF EXISTS sp_dispense_medicine;
DROP PROCEDURE IF EXISTS sp_revenue_report;

DELIMITER //

-- 1. Admit a patient to a bed (transactional: insert admission + occupy bed)
CREATE PROCEDURE sp_admit_patient(
    IN  p_patient   INT,
    IN  p_bed       INT,
    IN  p_doctor    INT,
    IN  p_diagnosis TEXT,
    OUT p_admission INT)
proc:BEGIN
    DECLARE v_status VARCHAR(15);

    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        ROLLBACK;
        RESIGNAL;
    END;

    START TRANSACTION;

    SELECT status INTO v_status FROM beds WHERE bed_id = p_bed FOR UPDATE;
    IF v_status IS NULL THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Bed does not exist';
    END IF;
    IF v_status <> 'Available' THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Selected bed is not available';
    END IF;

    INSERT INTO admissions(patient_id, bed_id, doctor_id, diagnosis)
    VALUES (p_patient, p_bed, p_doctor, p_diagnosis);
    SET p_admission = LAST_INSERT_ID();

    UPDATE beds SET status = 'Occupied' WHERE bed_id = p_bed;

    COMMIT;
END//

-- 2. Discharge a patient (free the bed + add ward charge as bill item)
CREATE PROCEDURE sp_discharge_patient(IN p_admission INT)
BEGIN
    DECLARE v_bed INT;
    DECLARE v_patient INT;
    DECLARE v_days INT;
    DECLARE v_charge DECIMAL(10,2);
    DECLARE v_admit DATETIME;
    DECLARE v_bill INT;

    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        ROLLBACK;
        RESIGNAL;
    END;

    START TRANSACTION;

    SELECT bed_id, patient_id, admit_date
      INTO v_bed, v_patient, v_admit
      FROM admissions WHERE admission_id = p_admission;

    UPDATE admissions
       SET discharge_date = NOW(), status = 'Discharged'
     WHERE admission_id = p_admission;

    UPDATE beds SET status = 'Available' WHERE bed_id = v_bed;

    SELECT w.charge_per_day INTO v_charge
      FROM beds b JOIN wards w ON b.ward_id = w.ward_id
     WHERE b.bed_id = v_bed;

    SET v_days = GREATEST(DATEDIFF(NOW(), v_admit), 1);

    -- create a bill and ward-charge line item (trigger updates total)
    INSERT INTO bills(patient_id, notes) VALUES (v_patient, CONCAT('Admission #',p_admission,' charges'));
    SET v_bill = LAST_INSERT_ID();
    INSERT INTO bill_items(bill_id, description, item_type, quantity, unit_price, amount)
    VALUES (v_bill, CONCAT('Ward charges (',v_days,' day(s))'), 'Ward', v_days, v_charge, v_days * v_charge);

    COMMIT;
END//

-- 3. Record a payment against a bill (trigger keeps bill in sync)
CREATE PROCEDURE sp_record_payment(
    IN p_bill   INT,
    IN p_amount DECIMAL(12,2),
    IN p_method VARCHAR(15),
    IN p_ref    VARCHAR(50))
BEGIN
    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        ROLLBACK;
        RESIGNAL;
    END;
    START TRANSACTION;
    INSERT INTO payments(bill_id, amount, method, reference_no)
    VALUES (p_bill, p_amount, p_method, p_ref);
    COMMIT;
END//

-- 4. Dispense medicine (reduce stock, guard against negative stock)
CREATE PROCEDURE sp_dispense_medicine(IN p_medicine INT, IN p_qty INT)
BEGIN
    DECLARE v_stock INT;
    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        ROLLBACK;
        RESIGNAL;
    END;
    START TRANSACTION;
    SELECT stock_qty INTO v_stock FROM medicines WHERE medicine_id = p_medicine FOR UPDATE;
    IF v_stock IS NULL THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Medicine not found';
    END IF;
    IF v_stock < p_qty THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Insufficient medicine stock';
    END IF;
    UPDATE medicines SET stock_qty = stock_qty - p_qty WHERE medicine_id = p_medicine;
    COMMIT;
END//

-- 5. Parameterized revenue report between two dates
CREATE PROCEDURE sp_revenue_report(IN p_from DATE, IN p_to DATE)
BEGIN
    SELECT DATE(payment_date) AS pay_day,
           COUNT(*)           AS payments_count,
           SUM(amount)        AS total_collected
    FROM payments
    WHERE DATE(payment_date) BETWEEN p_from AND p_to
    GROUP BY DATE(payment_date)
    ORDER BY pay_day;
END//

DELIMITER ;
