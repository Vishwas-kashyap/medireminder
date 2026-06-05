package database;

import model.Patient;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * PatientDAO - Data Access Object for the patients table.
 */
public class PatientDAO {

    /** Add a patient record linked to a doctor. */
    public boolean addPatient(Patient patient) throws SQLException {
        String sql = "INSERT INTO patients (user_id, doctor_id, date_of_birth, medical_notes) VALUES (?,?,?,?)";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, patient.getUserId());
            ps.setInt(2, patient.getDoctorId());
            ps.setDate(3, patient.getDateOfBirth());
            ps.setString(4, patient.getMedicalNotes());
            int rows = ps.executeUpdate();
            if (rows > 0) {
                ResultSet keys = ps.getGeneratedKeys();
                if (keys.next()) patient.setId(keys.getInt(1));
            }
            return rows > 0;
        }
    }

    /** Get all patients assigned to a specific doctor (with patient name via JOIN). */
    public List<Patient> getPatientsByDoctor(int doctorId) throws SQLException {
        String sql = "SELECT p.*, u.full_name AS patient_name, u.username AS patient_username " +
                     "FROM patients p JOIN users u ON p.user_id = u.id " +
                     "WHERE p.doctor_id = ? ORDER BY u.full_name";
        List<Patient> list = new ArrayList<>();
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, doctorId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapPatient(rs));
        }
        return list;
    }

    /** Search patients by name for a given doctor. */
    public List<Patient> searchPatients(int doctorId, String keyword) throws SQLException {
        String sql = "SELECT p.*, u.full_name AS patient_name, u.username AS patient_username " +
                     "FROM patients p JOIN users u ON p.user_id = u.id " +
                     "WHERE p.doctor_id = ? AND u.full_name LIKE ? ORDER BY u.full_name";
        List<Patient> list = new ArrayList<>();
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, doctorId);
            ps.setString(2, "%" + keyword + "%");
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapPatient(rs));
        }
        return list;
    }

    /** Get a patient record by their user_id. */
    public Patient getPatientByUserId(int userId) throws SQLException {
        String sql = "SELECT p.*, u.full_name AS patient_name, u.username AS patient_username, " +
                     "d.full_name AS doctor_name " +
                     "FROM patients p " +
                     "JOIN users u ON p.user_id = u.id " +
                     "JOIN users d ON p.doctor_id = d.id " +
                     "WHERE p.user_id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                Patient pat = mapPatient(rs);
                pat.setDoctorName(rs.getString("doctor_name"));
                return pat;
            }
        }
        return null;
    }

    /** Get patient record by patient table ID. */
    public Patient getPatientById(int patientId) throws SQLException {
        String sql = "SELECT p.*, u.full_name AS patient_name, u.username AS patient_username " +
                     "FROM patients p JOIN users u ON p.user_id = u.id WHERE p.id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, patientId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapPatient(rs);
        }
        return null;
    }

    /** Check if a user is already registered as a patient under a doctor. */
    public boolean isAlreadyPatient(int userId, int doctorId) throws SQLException {
        String sql = "SELECT id FROM patients WHERE user_id = ? AND doctor_id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, doctorId);
            return ps.executeQuery().next();
        }
    }

    private Patient mapPatient(ResultSet rs) throws SQLException {
        Patient p = new Patient();
        p.setId(rs.getInt("id"));
        p.setUserId(rs.getInt("user_id"));
        p.setDoctorId(rs.getInt("doctor_id"));
        p.setDateOfBirth(rs.getDate("date_of_birth"));
        p.setMedicalNotes(rs.getString("medical_notes"));
        p.setPatientName(rs.getString("patient_name"));
        p.setPatientUsername(rs.getString("patient_username"));
        return p;
    }
}
