package model;

/**
 * User - represents a system user (Doctor/Guardian or Patient).
 */
public class User {

    public enum Role { DOCTOR, PATIENT }

    private int    id;
    private String username;
    private String password;
    private String fullName;
    private String email;
    private String phone;
    private Role   role;

    public User() {}

    public User(int id, String username, String fullName, String email, String phone, Role role) {
        this.id       = id;
        this.username = username;
        this.fullName = fullName;
        this.email    = email;
        this.phone    = phone;
        this.role     = role;
    }

    // ---------- Getters & Setters ----------
    public int    getId()          { return id; }
    public void   setId(int id)    { this.id = id; }

    public String getUsername()              { return username; }
    public void   setUsername(String u)      { this.username = u; }

    public String getPassword()              { return password; }
    public void   setPassword(String p)      { this.password = p; }

    public String getFullName()              { return fullName; }
    public void   setFullName(String n)      { this.fullName = n; }

    public String getEmail()                 { return email; }
    public void   setEmail(String e)         { this.email = e; }

    public String getPhone()                 { return phone; }
    public void   setPhone(String p)         { this.phone = p; }

    public Role   getRole()                  { return role; }
    public void   setRole(Role r)            { this.role = r; }

    @Override
    public String toString() { return fullName + " (" + username + ")"; }
}
