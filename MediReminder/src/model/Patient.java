package model;

import java.sql.Date;

/**
 * Patient - links a patient User to their assigned Doctor.
 */
public class Patient {

    private int    id;
    private int    userId;
    private int    doctorId;
    private Date   dateOfBirth;
    private String medicalNotes;

    // Convenience fields populated via JOIN queries
    private String patientName;
    private String patientUsername;
    private String doctorName;

    public Patient() {}

    // ---------- Getters & Setters ----------
    public int    getId()                      { return id; }
    public void   setId(int id)                { this.id = id; }

    public int    getUserId()                  { return userId; }
    public void   setUserId(int userId)        { this.userId = userId; }

    public int    getDoctorId()                { return doctorId; }
    public void   setDoctorId(int doctorId)    { this.doctorId = doctorId; }

    public Date   getDateOfBirth()             { return dateOfBirth; }
    public void   setDateOfBirth(Date d)       { this.dateOfBirth = d; }

    public String getMedicalNotes()            { return medicalNotes; }
    public void   setMedicalNotes(String n)    { this.medicalNotes = n; }

    public String getPatientName()             { return patientName; }
    public void   setPatientName(String n)     { this.patientName = n; }

    public String getPatientUsername()         { return patientUsername; }
    public void   setPatientUsername(String u) { this.patientUsername = u; }

    public String getDoctorName()              { return doctorName; }
    public void   setDoctorName(String n)      { this.doctorName = n; }

    @Override
    public String toString() { return patientName != null ? patientName : "Patient#" + id; }
}
