package database;

import model.MedicationStatus;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * MedicationStatusDAO - Manages dose event records.
 */
public class MedicationStatusDAO {

    /** Create a new PENDING status entry for a dose. */
    public boolean createPending(int medicineId, int patientId, Date scheduledDate, Time scheduledTime)
            throws SQLException {
        // Avoid duplicate entries for the same medicine+date
        if (statusExists(medicineId, scheduledDate)) return false;
        String sql = "INSERT INTO medication_status (medicine_id, patient_id, scheduled_date, scheduled_time, status) " +
                     "VALUES (?,?,?,?,'PENDING')";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, medicineId);
            ps.setInt(2, patientId);
            ps.setDate(3, scheduledDate);
            ps.setTime(4, scheduledTime);
            return ps.executeUpdate() > 0;
        }
    }

    /** Update the status of a dose event. */
    public boolean updateStatus(int statusId, MedicationStatus.Status status) throws SQLException {
        String sql = "UPDATE medication_status SET status=?, action_time=NOW() WHERE id=?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, status.name());
            ps.setInt(2, statusId);
            return ps.executeUpdate() > 0;
        }
    }

    /** Check if a status record already exists for a medicine+date. */
    public boolean statusExists(int medicineId, Date scheduledDate) throws SQLException {
        String sql = "SELECT id FROM medication_status WHERE medicine_id=? AND scheduled_date=?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, medicineId);
            ps.setDate(2, scheduledDate);
            return ps.executeQuery().next();
        }
    }

    /** Get today's pending/snoozed medicines for a patient (for reminder checks). */
    public List<MedicationStatus> getTodayPendingByPatient(int patientId) throws SQLException {
        String sql = "SELECT ms.*, m.medicine_name, m.dosage, m.instructions, u.full_name AS patient_name " +
                     "FROM medication_status ms " +
                     "JOIN medicines m ON ms.medicine_id = m.id " +
                     "JOIN patients p ON ms.patient_id = p.id " +
                     "JOIN users u ON p.user_id = u.id " +
                     "WHERE ms.patient_id = ? AND ms.scheduled_date = CURDATE() " +
                     "AND ms.status IN ('PENDING','SNOOZED') " +
                     "AND ms.scheduled_time <= CURTIME()";
        return fetchList(sql, patientId);
    }

    /** Get all statuses for a patient (history view). */
    public List<MedicationStatus> getHistoryByPatient(int patientId) throws SQLException {
        String sql = "SELECT ms.*, m.medicine_name, m.dosage, m.instructions, u.full_name AS patient_name " +
                     "FROM medication_status ms " +
                     "JOIN medicines m ON ms.medicine_id = m.id " +
                     "JOIN patients p ON ms.patient_id = p.id " +
                     "JOIN users u ON p.user_id = u.id " +
                     "WHERE ms.patient_id = ? ORDER BY ms.scheduled_date DESC, ms.scheduled_time DESC";
        return fetchList(sql, patientId);
    }

    /** Get all statuses for patients of a doctor (monitoring view). */
    public List<MedicationStatus> getStatusByDoctor(int doctorId) throws SQLException {
        String sql = "SELECT ms.*, m.medicine_name, m.dosage, m.instructions, u.full_name AS patient_name " +
                     "FROM medication_status ms " +
                     "JOIN medicines m ON ms.medicine_id = m.id AND m.doctor_id = ? " +
                     "JOIN patients p ON ms.patient_id = p.id " +
                     "JOIN users u ON p.user_id = u.id " +
                     "ORDER BY ms.scheduled_date DESC, u.full_name";
        List<MedicationStatus> list = new ArrayList<>();
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, doctorId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapStatus(rs));
        }
        return list;
    }

    /** Get status for a specific patient monitored by a doctor. */
    public List<MedicationStatus> getStatusByPatientForDoctor(int patientId) throws SQLException {
        String sql = "SELECT ms.*, m.medicine_name, m.dosage, m.instructions, u.full_name AS patient_name " +
                     "FROM medication_status ms " +
                     "JOIN medicines m ON ms.medicine_id = m.id " +
                     "JOIN patients p ON ms.patient_id = p.id " +
                     "JOIN users u ON p.user_id = u.id " +
                     "WHERE ms.patient_id = ? ORDER BY ms.scheduled_date DESC, ms.scheduled_time DESC";
        return fetchList(sql, patientId);
    }

    /** Mark overdue PENDING entries as MISSED for today. */
    public void markMissed() throws SQLException {
        String sql = "UPDATE medication_status SET status='MISSED', action_time=NOW() " +
                     "WHERE status='PENDING' AND scheduled_date = CURDATE() " +
                     "AND ADDTIME(scheduled_time, '01:00:00') < CURTIME()";
        try (Connection con = DBConnection.getConnection();
             Statement st = con.createStatement()) {
            st.executeUpdate(sql);
        }
    }

    private List<MedicationStatus> fetchList(String sql, int paramId) throws SQLException {
        List<MedicationStatus> list = new ArrayList<>();
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, paramId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapStatus(rs));
        }
        return list;
    }

    private MedicationStatus mapStatus(ResultSet rs) throws SQLException {
        MedicationStatus ms = new MedicationStatus();
        ms.setId(rs.getInt("id"));
        ms.setMedicineId(rs.getInt("medicine_id"));
        ms.setPatientId(rs.getInt("patient_id"));
        ms.setScheduledDate(rs.getDate("scheduled_date"));
        ms.setScheduledTime(rs.getTime("scheduled_time"));
        ms.setStatus(MedicationStatus.Status.valueOf(rs.getString("status")));
        ms.setActionTime(rs.getTimestamp("action_time"));
        ms.setNotes(rs.getString("notes"));
        ms.setMedicineName(rs.getString("medicine_name"));
        ms.setDosage(rs.getString("dosage"));
        ms.setInstructions(rs.getString("instructions"));
        ms.setPatientName(rs.getString("patient_name"));
        return ms;
    }
}
