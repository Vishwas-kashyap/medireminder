package model;

import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;

/**
 * MedicationStatus - tracks the take/miss/skip event for each scheduled dose.
 */
public class MedicationStatus {

    public enum Status { PENDING, TAKEN, MISSED, SNOOZED, SKIPPED }

    private int       id;
    private int       medicineId;
    private int       patientId;
    private Date      scheduledDate;
    private Time      scheduledTime;
    private Status    status;
    private Timestamp actionTime;
    private String    notes;

    // Convenience fields
    private String medicineName;
    private String dosage;
    private String patientName;
    private String instructions;

    public MedicationStatus() {}

    // ---------- Getters & Setters ----------
    public int       getId()                          { return id; }
    public void      setId(int id)                    { this.id = id; }

    public int       getMedicineId()                  { return medicineId; }
    public void      setMedicineId(int m)             { this.medicineId = m; }

    public int       getPatientId()                   { return patientId; }
    public void      setPatientId(int p)              { this.patientId = p; }

    public Date      getScheduledDate()               { return scheduledDate; }
    public void      setScheduledDate(Date d)         { this.scheduledDate = d; }

    public Time      getScheduledTime()               { return scheduledTime; }
    public void      setScheduledTime(Time t)         { this.scheduledTime = t; }

    public Status    getStatus()                      { return status; }
    public void      setStatus(Status s)              { this.status = s; }

    public Timestamp getActionTime()                  { return actionTime; }
    public void      setActionTime(Timestamp t)       { this.actionTime = t; }

    public String    getNotes()                       { return notes; }
    public void      setNotes(String n)               { this.notes = n; }

    public String    getMedicineName()                { return medicineName; }
    public void      setMedicineName(String n)        { this.medicineName = n; }

    public String    getDosage()                      { return dosage; }
    public void      setDosage(String d)              { this.dosage = d; }

    public String    getPatientName()                 { return patientName; }
    public void      setPatientName(String n)         { this.patientName = n; }

    public String    getInstructions()                { return instructions; }
    public void      setInstructions(String i)        { this.instructions = i; }
}
