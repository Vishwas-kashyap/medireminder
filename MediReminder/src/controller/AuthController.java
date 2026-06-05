package controller;

import database.UserDAO;
import database.PatientDAO;
import model.User;
import model.Patient;
import util.Validator;
import java.sql.SQLException;

/**
 * AuthController - handles login and registration logic.
 */
public class AuthController {

    private final UserDAO    userDAO    = new UserDAO();
    private final PatientDAO patientDAO = new PatientDAO();

    /**
     * Attempts to log in with given credentials.
     * @return authenticated User or null if invalid.
     */
    public User login(String username, String password) throws SQLException {
        if (Validator.isEmpty(username) || Validator.isEmpty(password)) return null;
        return userDAO.login(username.trim(), password.trim());
    }

    /**
     * Registers a new user account.
     * @return null on success, or an error message string on failure.
     */
    public String register(String username, String password, String fullName,
                           String email, String phone, String role) {
        // Validate inputs
        if (!Validator.isValidUsername(username))
            return "Username must be 4-20 alphanumeric characters.";
        if (!Validator.isValidPassword(password))
            return "Password must be at least 6 characters.";
        if (Validator.isEmpty(fullName))
            return "Full name is required.";
        if (!Validator.isEmpty(email) && !Validator.isValidEmail(email))
            return "Please enter a valid email address.";
        if (!Validator.isEmpty(phone) && !Validator.isValidPhone(phone))
            return "Phone must be a 10-digit number.";

        try {
            if (userDAO.usernameExists(username))
                return "Username already taken. Please choose another.";

            User user = new User();
            user.setUsername(username.trim());
            user.setPassword(password.trim());
            user.setFullName(fullName.trim());
            user.setEmail(email.trim());
            user.setPhone(phone.trim());
            user.setRole(User.Role.valueOf(role));

            return userDAO.register(user) ? null : "Registration failed. Please try again.";

        } catch (SQLException e) {
            return "Database error: " + e.getMessage();
        }
    }

    /**
     * Adds an existing patient-role user to a doctor's patient list.
     * @return null on success, or an error message string on failure.
     */
    public String addPatientToDoctor(String patientUsername, int doctorId,
                                     String dob, String notes) {
        if (Validator.isEmpty(patientUsername))
            return "Patient username is required.";

        try {
            // Find the user account
            User patientUser = userDAO.login(patientUsername.trim(), ""); // dummy login won't work
            // Look up by username properly
            patientUser = findUserByUsername(patientUsername.trim());
            if (patientUser == null)
                return "No user found with that username.";
            if (patientUser.getRole() != User.Role.PATIENT)
                return "That user is not registered as a Patient.";
            if (patientDAO.isAlreadyPatient(patientUser.getId(), doctorId))
                return "Patient is already in your list.";

            Patient p = new Patient();
            p.setUserId(patientUser.getId());
            p.setDoctorId(doctorId);
            p.setMedicalNotes(notes);
            if (!Validator.isEmpty(dob)) {
                try { p.setDateOfBirth(java.sql.Date.valueOf(dob)); }
                catch (Exception ignored) {}
            }

            return patientDAO.addPatient(p) ? null : "Failed to add patient.";

        } catch (SQLException e) {
            return "Database error: " + e.getMessage();
        }
    }

    /** Helper: find a user by their username (without password check). */
    private User findUserByUsername(String username) throws SQLException {
        // We use a raw DAO call – extend UserDAO with this method
        database.DBConnection conn = null;
        try {
            java.sql.Connection con = database.DBConnection.getConnection();
            java.sql.PreparedStatement ps = con.prepareStatement(
                "SELECT * FROM users WHERE username = ?");
            ps.setString(1, username);
            java.sql.ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                User u = new User();
                u.setId(rs.getInt("id"));
                u.setUsername(rs.getString("username"));
                u.setFullName(rs.getString("full_name"));
                u.setRole(User.Role.valueOf(rs.getString("role")));
                return u;
            }
        } catch (SQLException e) { throw e; }
        return null;
    }
}
