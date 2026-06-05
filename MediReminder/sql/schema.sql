-- ============================================
-- Remote Medicine Reminder & Monitoring System
-- Database Schema
-- ============================================

CREATE DATABASE IF NOT EXISTS medi_reminder;
USE medi_reminder;

-- Users table (Doctor/Guardian and Patient accounts)
CREATE TABLE IF NOT EXISTS users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    email VARCHAR(100),
    phone VARCHAR(20),
    role ENUM('DOCTOR', 'PATIENT') NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Patients table (links patient user to their doctor)
CREATE TABLE IF NOT EXISTS patients (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    doctor_id INT NOT NULL,
    date_of_birth DATE,
    medical_notes TEXT,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (doctor_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Medicines / Schedules table
CREATE TABLE IF NOT EXISTS medicines (
    id INT AUTO_INCREMENT PRIMARY KEY,
    patient_id INT NOT NULL,
    doctor_id INT NOT NULL,
    medicine_name VARCHAR(100) NOT NULL,
    dosage VARCHAR(50) NOT NULL,
    instructions TEXT,
    reminder_time TIME NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    is_active TINYINT(1) DEFAULT 1,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (patient_id) REFERENCES patients(id) ON DELETE CASCADE,
    FOREIGN KEY (doctor_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Medication Status (tracks each dose event)
CREATE TABLE IF NOT EXISTS medication_status (
    id INT AUTO_INCREMENT PRIMARY KEY,
    medicine_id INT NOT NULL,
    patient_id INT NOT NULL,
    scheduled_date DATE NOT NULL,
    scheduled_time TIME NOT NULL,
    status ENUM('PENDING', 'TAKEN', 'MISSED', 'SNOOZED', 'SKIPPED') DEFAULT 'PENDING',
    action_time TIMESTAMP NULL,
    notes VARCHAR(255),
    FOREIGN KEY (medicine_id) REFERENCES medicines(id) ON DELETE CASCADE,
    FOREIGN KEY (patient_id) REFERENCES patients(id) ON DELETE CASCADE
);

-- Sample doctor account (password: doctor123)
INSERT INTO users (username, password, full_name, email, phone, role)
VALUES ('doctor1', 'doctor123', 'Dr. Arjun Sharma', 'arjun@clinic.com', '9876543210', 'DOCTOR')
ON DUPLICATE KEY UPDATE username = username;

-- Sample patient account (password: patient123)
INSERT INTO users (username, password, full_name, email, phone, role)
VALUES ('patient1', 'patient123', 'Priya Mehta', 'priya@email.com', '9123456789', 'PATIENT')
ON DUPLICATE KEY UPDATE username = username;
