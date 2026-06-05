package util;

/**
 * Validator - input validation helpers.
 */
public class Validator {

    /** Returns true if the string is null or blank. */
    public static boolean isEmpty(String s) {
        return s == null || s.trim().isEmpty();
    }

    /** Returns true if the string is a valid email format. */
    public static boolean isValidEmail(String email) {
        if (isEmpty(email)) return false;
        return email.matches("^[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,}$");
    }

    /** Returns true if the string is a valid 10-digit phone number. */
    public static boolean isValidPhone(String phone) {
        if (isEmpty(phone)) return false;
        return phone.matches("^[0-9]{10}$");
    }

    /** Returns true if time string matches HH:mm format. */
    public static boolean isValidTime(String time) {
        if (isEmpty(time)) return false;
        return time.matches("^([01]?[0-9]|2[0-3]):[0-5][0-9]$");
    }

    /** Returns true if date string matches yyyy-MM-dd format. */
    public static boolean isValidDate(String date) {
        if (isEmpty(date)) return false;
        return date.matches("^\\d{4}-\\d{2}-\\d{2}$");
    }

    /** Returns true if the username contains only alphanumeric and underscore characters. */
    public static boolean isValidUsername(String username) {
        if (isEmpty(username)) return false;
        return username.matches("^[a-zA-Z0-9_]{4,20}$");
    }

    /** Returns true if password meets minimum length. */
    public static boolean isValidPassword(String password) {
        return !isEmpty(password) && password.length() >= 6;
    }
}
