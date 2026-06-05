package database;

import model.Medicine;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * MedicineDAO - Data Access Object for the medicines table.
 */
public class MedicineDAO {

    /** Add a new medicine schedule. */
    public boolean addMedicine(Medicine m) throws SQLException {
        String sql = "INSERT INTO medicines (patient_id, doctor_id, medicine_name, dosage, instructions, " +
                     "reminder_time, start_date, end_date, is_active) VALUES (?,?,?,?,?,?,?,?,1)";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, m.getPatientId());
            ps.setInt(2, m.getDoctorId());
            ps.setString(3, m.getMedicineName());
            ps.setString(4, m.getDosage());
            ps.setString(5, m.getInstructions());
            ps.setTime(6, m.getReminderTime());
            ps.setDate(7, m.getStartDate());
            ps.setDate(8, m.getEndDate());
            int rows = ps.executeUpdate();
            if (rows > 0) {
                ResultSet keys = ps.getGeneratedKeys();
                if (keys.next()) m.setId(keys.getInt(1));
            }
            return rows > 0;
        }
    }

    /** Update an existing medicine schedule. */
    public boolean updateMedicine(Medicine m) throws SQLException {
        String sql = "UPDATE medicines SET medicine_name=?, dosage=?, instructions=?, " +
                     "reminder_time=?, start_date=?, end_date=? WHERE id=?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, m.getMedicineName());
            ps.setString(2, m.getDosage());
            ps.setString(3, m.getInstructions());
            ps.setTime(4, m.getReminderTime());
            ps.setDate(5, m.getStartDate());
            ps.setDate(6, m.getEndDate());
            ps.setInt(7, m.getId());
            return ps.executeUpdate() > 0;
        }
    }

    /** Soft-delete: mark medicine as inactive. */
    public boolean deleteMedicine(int medicineId) throws SQLException {
        String sql = "UPDATE medicines SET is_active = 0 WHERE id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, medicineId);
            return ps.executeUpdate() > 0;
        }
    }

    /** Get all active medicines for a patient (for patient dashboard & reminders). */
    public List<Medicine> getMedicinesByPatient(int patientId) throws SQLException {
        String sql = "SELECT m.*, u.full_name AS doctor_name FROM medicines m " +
                     "JOIN users u ON m.doctor_id = u.id " +
                     "WHERE m.patient_id = ? AND m.is_active = 1 " +
                     "AND CURDATE() BETWEEN m.start_date AND m.end_date " +
                     "ORDER BY m.reminder_time";
        List<Medicine> list = new ArrayList<>();
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, patientId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapMedicine(rs));
        }
        return list;
    }

    /** Get all medicines assigned by a doctor to their patients. */
    public List<Medicine> getMedicinesByDoctor(int doctorId) throws SQLException {
        String sql = "SELECT m.*, u.full_name AS patient_name FROM medicines m " +
                     "JOIN patients p ON m.patient_id = p.id " +
                     "JOIN users u ON p.user_id = u.id " +
                     "WHERE m.doctor_id = ? AND m.is_active = 1 ORDER BY u.full_name, m.reminder_time";
        List<Medicine> list = new ArrayList<>();
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, doctorId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Medicine med = mapMedicine(rs);
                med.setPatientName(rs.getString("patient_name"));
                list.add(med);
            }
        }
        return list;
    }

    /** Get all medicines assigned to a specific patient by a doctor. */
    public List<Medicine> getMedicinesByPatientAndDoctor(int patientId, int doctorId) throws SQLException {
        String sql = "SELECT m.*, u.full_name AS patient_name FROM medicines m " +
                     "JOIN patients p ON m.patient_id = p.id " +
                     "JOIN users u ON p.user_id = u.id " +
                     "WHERE m.patient_id = ? AND m.doctor_id = ? AND m.is_active = 1 ORDER BY m.reminder_time";
        List<Medicine> list = new ArrayList<>();
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, patientId);
            ps.setInt(2, doctorId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Medicine med = mapMedicine(rs);
                med.setPatientName(rs.getString("patient_name"));
                list.add(med);
            }
        }
        return list;
    }

    /** Get a single medicine by its ID. */
    public Medicine getMedicineById(int id) throws SQLException {
        String sql = "SELECT m.*, u.full_name AS patient_name FROM medicines m " +
                     "JOIN patients p ON m.patient_id = p.id " +
                     "JOIN users u ON p.user_id = u.id WHERE m.id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                Medicine med = mapMedicine(rs);
                med.setPatientName(rs.getString("patient_name"));
                return med;
            }
        }
        return null;
    }

    private Medicine mapMedicine(ResultSet rs) throws SQLException {
        Medicine m = new Medicine();
        m.setId(rs.getInt("id"));
        m.setPatientId(rs.getInt("patient_id"));
        m.setDoctorId(rs.getInt("doctor_id"));
        m.setMedicineName(rs.getString("medicine_name"));
        m.setDosage(rs.getString("dosage"));
        m.setInstructions(rs.getString("instructions"));
        m.setReminderTime(rs.getTime("reminder_time"));
        m.setStartDate(rs.getDate("start_date"));
        m.setEndDate(rs.getDate("end_date"));
        m.setActive(rs.getInt("is_active") == 1);
        return m;
    }
}
