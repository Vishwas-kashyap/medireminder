package controller;

import database.MedicineDAO;
import database.MedicationStatusDAO;
import database.PatientDAO;
import model.Medicine;
import model.MedicationStatus;
import model.Patient;
import util.Validator;

import java.sql.Date;
import java.sql.SQLException;
import java.sql.Time;
import java.util.List;

/**
 * MedicineController - business logic for medicine schedule management.
 */
public class MedicineController {

    private final MedicineDAO          medicineDAO  = new MedicineDAO();
    private final MedicationStatusDAO  statusDAO    = new MedicationStatusDAO();
    private final PatientDAO           patientDAO   = new PatientDAO();

    /**
     * Assign a new medicine schedule. Returns null on success or an error string.
     */
    public String addMedicine(int patientId, int doctorId, String name, String dosage,
                              String instructions, String timeStr, String startStr, String endStr) {
        if (Validator.isEmpty(name))        return "Medicine name is required.";
        if (Validator.isEmpty(dosage))      return "Dosage is required.";
        if (!Validator.isValidTime(timeStr))  return "Time must be in HH:mm format (e.g. 08:00).";
        if (!Validator.isValidDate(startStr)) return "Start date must be in yyyy-MM-dd format.";
        if (!Validator.isValidDate(endStr))   return "End date must be in yyyy-MM-dd format.";

        Date start = Date.valueOf(startStr);
        Date end   = Date.valueOf(endStr);
        if (end.before(start)) return "End date must be after start date.";

        Medicine m = new Medicine();
        m.setPatientId(patientId);
        m.setDoctorId(doctorId);
        m.setMedicineName(name.trim());
        m.setDosage(dosage.trim());
        m.setInstructions(instructions != null ? instructions.trim() : "");
        m.setReminderTime(Time.valueOf(timeStr + ":00"));
        m.setStartDate(start);
        m.setEndDate(end);

        try {
            return medicineDAO.addMedicine(m) ? null : "Failed to save medicine.";
        } catch (SQLException e) {
            return "Database error: " + e.getMessage();
        }
    }

    /**
     * Update an existing medicine schedule. Returns null on success or an error string.
     */
    public String updateMedicine(int id, String name, String dosage, String instructions,
                                 String timeStr, String startStr, String endStr) {
        if (Validator.isEmpty(name))          return "Medicine name is required.";
        if (Validator.isEmpty(dosage))         return "Dosage is required.";
        if (!Validator.isValidTime(timeStr))   return "Time must be in HH:mm format.";
        if (!Validator.isValidDate(startStr))  return "Invalid start date.";
        if (!Validator.isValidDate(endStr))    return "Invalid end date.";

        Date start = Date.valueOf(startStr);
        Date end   = Date.valueOf(endStr);
        if (end.before(start)) return "End date must be after start date.";

        Medicine m = new Medicine();
        m.setId(id);
        m.setMedicineName(name.trim());
        m.setDosage(dosage.trim());
        m.setInstructions(instructions != null ? instructions.trim() : "");
        m.setReminderTime(Time.valueOf(timeStr + ":00"));
        m.setStartDate(start);
        m.setEndDate(end);

        try {
            return medicineDAO.updateMedicine(m) ? null : "Update failed.";
        } catch (SQLException e) {
            return "Database error: " + e.getMessage();
        }
    }

    /** Soft-delete a medicine schedule. */
    public String deleteMedicine(int medicineId) {
        try {
            return medicineDAO.deleteMedicine(medicineId) ? null : "Delete failed.";
        } catch (SQLException e) {
            return "Database error: " + e.getMessage();
        }
    }

    /** Retrieve all active medicines for a patient. */
    public List<Medicine> getMedicinesByPatient(int patientId) throws SQLException {
        return medicineDAO.getMedicinesByPatient(patientId);
    }

    /** Retrieve all medicines assigned by a doctor. */
    public List<Medicine> getMedicinesByDoctor(int doctorId) throws SQLException {
        return medicineDAO.getMedicinesByDoctor(doctorId);
    }

    /** Retrieve medicines for a specific patient under a doctor. */
    public List<Medicine> getMedicinesForPatient(int patientId, int doctorId) throws SQLException {
        return medicineDAO.getMedicinesByPatientAndDoctor(patientId, doctorId);
    }

    /** Get a single medicine by ID. */
    public Medicine getMedicineById(int id) throws SQLException {
        return medicineDAO.getMedicineById(id);
    }

    // ---- Status management ----

    /** Generate today's PENDING status rows for all medicines in a patient's schedule. */
    public void generateTodayStatuses(int patientId) {
        try {
            List<Medicine> medicines = medicineDAO.getMedicinesByPatient(patientId);
            Date today = new Date(System.currentTimeMillis());
            for (Medicine m : medicines) {
                statusDAO.createPending(m.getId(), patientId, today, m.getReminderTime());
            }
        } catch (SQLException e) {
            System.err.println("[ReminderSystem] Error generating statuses: " + e.getMessage());
        }
    }

    /** Get today's due, unactioned status entries for a patient. */
    public List<MedicationStatus> getTodayPending(int patientId) throws SQLException {
        return statusDAO.getTodayPendingByPatient(patientId);
    }

    /** Mark a dose as TAKEN. */
    public boolean markTaken(int statusId) throws SQLException {
        return statusDAO.updateStatus(statusId, MedicationStatus.Status.TAKEN);
    }

    /** Mark a dose as SNOOZED. */
    public boolean markSnoozed(int statusId) throws SQLException {
        return statusDAO.updateStatus(statusId, MedicationStatus.Status.SNOOZED);
    }

    /** Mark a dose as SKIPPED. */
    public boolean markSkipped(int statusId) throws SQLException {
        return statusDAO.updateStatus(statusId, MedicationStatus.Status.SKIPPED);
    }

    /** Mark all overdue PENDING doses as MISSED. */
    public void processMissed() {
        try {
            statusDAO.markMissed();
        } catch (SQLException e) {
            System.err.println("[MissedCheck] " + e.getMessage());
        }
    }

    /** Get all medication history for a patient. */
    public List<MedicationStatus> getPatientHistory(int patientId) throws SQLException {
        return statusDAO.getHistoryByPatient(patientId);
    }

    /** Get all status records for monitoring (doctor view). */
    public List<MedicationStatus> getDoctorMonitoring(int doctorId) throws SQLException {
        return statusDAO.getStatusByDoctor(doctorId);
    }

    /** Get status records for a specific patient (doctor view). */
    public List<MedicationStatus> getStatusForPatient(int patientId) throws SQLException {
        return statusDAO.getStatusByPatientForDoctor(patientId);
    }
}
