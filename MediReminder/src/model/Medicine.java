package model;

import java.sql.Date;
import java.sql.Time;

/**
 * Medicine - a scheduled medication entry assigned to a patient.
 */
public class Medicine {

    private int    id;
    private int    patientId;
    private int    doctorId;
    private String medicineName;
    private String dosage;
    private String instructions;
    private Time   reminderTime;
    private Date   startDate;
    private Date   endDate;
    private boolean active;

    // Convenience fields
    private String patientName;
    private String doctorName;

    public Medicine() {}

    // ---------- Getters & Setters ----------
    public int     getId()                       { return id; }
    public void    setId(int id)                 { this.id = id; }

    public int     getPatientId()                { return patientId; }
    public void    setPatientId(int p)           { this.patientId = p; }

    public int     getDoctorId()                 { return doctorId; }
    public void    setDoctorId(int d)            { this.doctorId = d; }

    public String  getMedicineName()             { return medicineName; }
    public void    setMedicineName(String n)     { this.medicineName = n; }

    public String  getDosage()                   { return dosage; }
    public void    setDosage(String d)           { this.dosage = d; }

    public String  getInstructions()             { return instructions; }
    public void    setInstructions(String i)     { this.instructions = i; }

    public Time    getReminderTime()             { return reminderTime; }
    public void    setReminderTime(Time t)       { this.reminderTime = t; }

    public Date    getStartDate()                { return startDate; }
    public void    setStartDate(Date d)          { this.startDate = d; }

    public Date    getEndDate()                  { return endDate; }
    public void    setEndDate(Date d)            { this.endDate = d; }

    public boolean isActive()                    { return active; }
    public void    setActive(boolean a)          { this.active = a; }

    public String  getPatientName()              { return patientName; }
    public void    setPatientName(String n)      { this.patientName = n; }

    public String  getDoctorName()               { return doctorName; }
    public void    setDoctorName(String n)       { this.doctorName = n; }

    @Override
    public String toString() { return medicineName + " - " + dosage; }
}
