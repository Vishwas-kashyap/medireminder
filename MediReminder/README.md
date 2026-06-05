# 💊 Remote Medicine Reminder & Monitoring System

A complete Java Swing desktop application for remotely assigning medicine schedules and tracking patient compliance.

---

## 📁 Project Structure

```
MediReminder/
├── src/
│   ├── Main.java                        ← Entry point
│   ├── model/
│   │   ├── User.java                    ← User entity (Doctor/Patient)
│   │   ├── Patient.java                 ← Patient entity
│   │   ├── Medicine.java                ← Medicine schedule entity
│   │   └── MedicationStatus.java        ← Dose event entity
│   ├── database/
│   │   ├── DBConnection.java            ← Singleton JDBC connection
│   │   ├── UserDAO.java                 ← User DB operations
│   │   ├── PatientDAO.java              ← Patient DB operations
│   │   ├── MedicineDAO.java             ← Medicine DB operations
│   │   └── MedicationStatusDAO.java     ← Dose status DB operations
│   ├── controller/
│   │   ├── AuthController.java          ← Login/registration logic
│   │   ├── MedicineController.java      ← Medicine management logic
│   │   └── ReminderService.java         ← Background reminder timer
│   ├── view/
│   │   ├── LoginView.java               ← Login screen
│   │   ├── RegisterView.java            ← Registration screen
│   │   ├── DoctorDashboard.java         ← Doctor main window
│   │   ├── PatientDashboard.java        ← Patient main window
│   │   ├── AddMedicineForm.java         ← Add/Edit medicine dialog
│   │   ├── ViewMedicinesScreen.java     ← View medicines (doctor)
│   │   └── MedicationStatusScreen.java  ← Status view (doctor)
│   └── util/
│       ├── UIUtil.java                  ← Shared UI styling helpers
│       └── Validator.java               ← Input validation helpers
├── sql/
│   └── schema.sql                       ← MySQL database schema
├── lib/                                 ← Place JDBC jar here
│   └── mysql-connector-java-8.0.33.jar
├── out/                                 ← Compiled .class files (auto-created)
├── build.sh                             ← Linux/Mac build + run script
├── build.bat                            ← Windows build + run script
└── README.md
```

---

## ⚙️ Setup Instructions

### Step 1 – Install Prerequisites
- **Java JDK 11+** — https://adoptium.net
- **MySQL 8.0+** — https://dev.mysql.com/downloads/
- **MySQL Connector/J** — https://dev.mysql.com/downloads/connector/j/

### Step 2 – Set Up the Database
```sql
-- Open MySQL Workbench or mysql CLI and run:
source /path/to/MediReminder/sql/schema.sql
```
This creates the `medi_reminder` database, all tables, and two sample accounts.

### Step 3 – Add the JDBC Driver
Download `mysql-connector-java-8.0.33.jar` (or newer) and place it in:
```
MediReminder/lib/mysql-connector-java-8.0.33.jar
```
Update `build.sh` or `build.bat` with the correct filename if different.

### Step 4 – Configure Database Credentials
Edit `src/database/DBConnection.java`:
```java
private static final String USER     = "root";       // your MySQL username
private static final String PASSWORD = "your_pass";  // your MySQL password
```

### Step 5 – Build & Run

**Linux/macOS:**
```bash
chmod +x build.sh
./build.sh
```

**Windows:**
```cmd
build.bat
```

**Or in an IDE (IntelliJ / Eclipse):**
1. Import the project.
2. Add `mysql-connector-java-8.0.33.jar` to the classpath (Project Structure → Libraries).
3. Run `Main.java`.

---

## 🔑 Sample Login Credentials

| Role    | Username  | Password    |
|---------|-----------|-------------|
| Doctor  | doctor1   | doctor123   |
| Patient | patient1  | patient123  |

> The patient1 account needs to be added by doctor1 first:
> Doctor Dashboard → "Add Patient" → enter username `patient1` → Add.

---

## 🧩 How Each Module Works

### Authentication (`AuthController`, `UserDAO`)
- Passwords stored as plain text for simplicity. **In production, use BCrypt.**
- `login()` queries `users` table and returns a typed `User` object.
- `register()` validates input before inserting.
- `addPatientToDoctor()` looks up a PATIENT-role user by username and creates a `patients` row linking them to the doctor.

### Medicine Management (`MedicineController`, `MedicineDAO`)
- Doctor assigns a schedule via `AddMedicineForm`.
- All schedule data is stored in the `medicines` table with `is_active=1`.
- Deletion is a soft-delete (sets `is_active=0`).

### Reminder System (`ReminderService`)
- Starts on a **daemon thread** when the Patient dashboard opens.
- Every 60 seconds it calls `MedicationStatusDAO.getTodayPendingByPatient()`.
- Due doses trigger a **JOptionPane popup** with three options: Taken / Snooze / Skip.
- "Snooze" reschedules a popup in 15 minutes using a second Timer.
- Overdue PENDING doses (>1 hour past schedule) are automatically marked **MISSED**.

### Dose Status Tracking (`MedicationStatusDAO`)
- On Patient login (via `generateTodayStatuses()`), PENDING rows are created for each active medicine whose schedule covers today.
- Status transitions: PENDING → TAKEN / MISSED / SNOOZED / SKIPPED.
- Doctors monitor all dose events on the **Status Monitor** tab.

### Reports
- The Doctor dashboard **Reports** panel aggregates all status records for the doctor's patients.
- Summary counts for TAKEN / MISSED / PENDING are shown as colour-coded cards.

---

## 🛡️ Tech Stack

| Component     | Technology              |
|---------------|-------------------------|
| Language      | Java 11+                |
| GUI           | Java Swing              |
| Database      | MySQL 8                 |
| Connectivity  | JDBC (mysql-connector)  |
| Architecture  | MVC                     |
| Threading     | java.util.Timer (daemon)|

---

## 🚀 Extending the Project

- **Password hashing** — replace plain-text with `BCrypt` from `mindrot/jbcrypt`.
- **Email notifications** — use JavaMail to send missed-dose alerts to the doctor.
- **Export reports** — add Apache POI to generate `.xlsx` reports.
- **Multi-dose per day** — allow multiple `reminder_time` entries per medicine via a `medicine_times` table.
