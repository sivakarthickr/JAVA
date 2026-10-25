import java.sql.*;
import java.util.*;

/**
 * AcademicDAO - Data Access Object for KEC R2024 Evaluation System.
 * 
 * Supports both:
 * 1. JDBC MySQL Database persistence (when MySQL server and driver are present).
 * 2. In-Memory fallback persistence (works immediately without any external DB setup).
 */
public class AcademicDAO {

    // Database connection credentials
    private static final String DB_URL = "jdbc:mysql://localhost:3306/kec_evaluation?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
    private static final String DB_USER = "root";
    private static final String DB_PASS = "root"; // Change according to your MySQL password

    private Connection connection;
    private boolean useDatabase = false;

    // --- In-Memory Fallback Storage ---
    private static class StudentRecord {
        String rollNo;
        String name;
        String dept;
        int sem;

        StudentRecord(String rollNo, String name, String dept, int sem) {
            this.rollNo = rollNo;
            this.name = name;
            this.dept = dept;
            this.sem = sem;
        }
    }

    private static class EnrollmentRecord {
        int enrollmentId;
        String rollNo;
        String courseCode;
        String courseName;
        int credits;
        Double cia1 = 0.0;
        Double cia2 = 0.0;
        Double ciaOther = 0.0;
        Double ese = 0.0;

        EnrollmentRecord(int enrollmentId, String rollNo, String courseCode, String courseName, int credits) {
            this.enrollmentId = enrollmentId;
            this.rollNo = rollNo;
            this.courseCode = courseCode;
            this.courseName = courseName;
            this.credits = credits;
        }
    }

    private final Map<String, StudentRecord> inMemoryStudents = new LinkedHashMap<>();
    private final Map<Integer, EnrollmentRecord> inMemoryEnrollments = new LinkedHashMap<>();
    private int nextEnrollmentId = 101;

    public AcademicDAO() {
        initDatabase();
    }

    /**
     * Initializes database connection and creates required tables.
     * Falls back to in-memory mode if connection is not available.
     */
    private void initDatabase() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
            createTablesIfNotExist();
            useDatabase = true;
            System.out.println("[Connected to MySQL Database: kec_evaluation]");
        } catch (Exception e) {
            useDatabase = false;
            System.out.println("[Note: MySQL database not connected (" + e.getMessage() + "). Running in In-Memory Mode]");
        }
    }

    private void createTablesIfNotExist() throws SQLException {
        String createStudents = "CREATE TABLE IF NOT EXISTS students ("
                + "roll_no VARCHAR(20) PRIMARY KEY, "
                + "name VARCHAR(100) NOT NULL, "
                + "dept VARCHAR(20) NOT NULL, "
                + "sem INT NOT NULL"
                + ");";

        String createEnrollments = "CREATE TABLE IF NOT EXISTS enrollments ("
                + "enrollment_id INT AUTO_INCREMENT PRIMARY KEY, "
                + "roll_no VARCHAR(20) NOT NULL, "
                + "course_code VARCHAR(20) NOT NULL, "
                + "course_name VARCHAR(100) NOT NULL, "
                + "credits INT NOT NULL, "
                + "cia1 DOUBLE DEFAULT 0.0, "
                + "cia2 DOUBLE DEFAULT 0.0, "
                + "cia_other DOUBLE DEFAULT 0.0, "
                + "ese DOUBLE DEFAULT 0.0, "
                + "FOREIGN KEY (roll_no) REFERENCES students(roll_no) ON DELETE CASCADE"
                + ");";

        try (Statement stmt = connection.createStatement()) {
            stmt.execute(createStudents);
            stmt.execute(createEnrollments);
        }
    }

    /**
     * 1. Register a student and auto-create default semester course enrollments.
     */
    public void addStudent(String roll, String name, String dept, int sem) throws SQLException {
        if (useDatabase) {
            String insertSql = "INSERT INTO students (roll_no, name, dept, sem) VALUES (?, ?, ?, ?) "
                    + "ON DUPLICATE KEY UPDATE name=?, dept=?, sem=?";
            try (PreparedStatement ps = connection.prepareStatement(insertSql)) {
                ps.setString(1, roll);
                ps.setString(2, name);
                ps.setString(3, dept);
                ps.setInt(4, sem);
                ps.setString(5, name);
                ps.setString(6, dept);
                ps.setInt(7, sem);
                ps.executeUpdate();
            }

            // Auto-enroll default courses for this semester if not already enrolled
            autoEnrollDefaultCoursesDB(roll, dept, sem);
        } else {
            StudentRecord student = new StudentRecord(roll, name, dept, sem);
            inMemoryStudents.put(roll, student);
            autoEnrollDefaultCoursesMem(roll, dept, sem);
        }
    }

    private void autoEnrollDefaultCoursesDB(String roll, String dept, int sem) throws SQLException {
        String checkSql = "SELECT COUNT(*) FROM enrollments WHERE roll_no = ?";
        try (PreparedStatement ps = connection.prepareStatement(checkSql)) {
            ps.setString(1, roll);
            ResultSet rs = ps.executeQuery();
            if (rs.next() && rs.getInt(1) > 0) {
                return; // already enrolled
            }
        }

        String[][] courses = getDefaultCourses(dept, sem);
        String insertCourse = "INSERT INTO enrollments (roll_no, course_code, course_name, credits) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(insertCourse, Statement.RETURN_GENERATED_KEYS)) {
            System.out.println("\nRegistered Subjects & Enrollment IDs for " + roll + ":");
            for (String[] c : courses) {
                ps.setString(1, roll);
                ps.setString(2, c[0]);
                ps.setString(3, c[1]);
                ps.setInt(4, Integer.parseInt(c[2]));
                ps.executeUpdate();

                ResultSet generatedKeys = ps.getGeneratedKeys();
                if (generatedKeys.next()) {
                    int eid = generatedKeys.getInt(1);
                    System.out.println("  -> [E-ID: " + eid + "] " + c[0] + " - " + c[1] + " (" + c[2] + " Credits)");
                }
            }
        }
    }

    private void autoEnrollDefaultCoursesMem(String roll, String dept, int sem) {
        String[][] courses = getDefaultCourses(dept, sem);
        System.out.println("\nRegistered Subjects & Enrollment IDs for " + roll + ":");
        for (String[] c : courses) {
            int eid = nextEnrollmentId++;
            EnrollmentRecord er = new EnrollmentRecord(eid, roll, c[0], c[1], Integer.parseInt(c[2]));
            inMemoryEnrollments.put(eid, er);
            System.out.println("  -> [E-ID: " + eid + "] " + c[0] + " - " + c[1] + " (" + c[2] + " Credits)");
        }
    }

    private String[][] getDefaultCourses(String dept, int sem) {
        // Typical KEC R2024 Course set
        return new String[][] {
            {"24CST11", "Programming in C and Java", "4"},
            {"24MAT11", "Matrices and Differential Calculus", "4"},
            {"24PHT11", "Engineering Physics", "3"},
            {"24EET11", "Basics of Electrical & Electronics", "3"}
        };
    }

    /**
     * 2. Update marks incrementally for a given phase: CIA1, CIA2, CIA_OTHER, ESE.
     */
    public void updateMarks(int eid, String phase, double marks) throws SQLException {
        String col;
        switch (phase.trim().toUpperCase()) {
            case "CIA1":
                col = "cia1";
                break;
            case "CIA2":
                col = "cia2";
                break;
            case "CIA_OTHER":
                col = "cia_other";
                break;
            case "ESE":
                col = "ese";
                break;
            default:
                throw new IllegalArgumentException("Invalid Phase: " + phase + ". Must be CIA1, CIA2, CIA_OTHER, or ESE.");
        }

        if (useDatabase) {
            String updateSql = "UPDATE enrollments SET " + col + " = ? WHERE enrollment_id = ?";
            try (PreparedStatement ps = connection.prepareStatement(updateSql)) {
                ps.setDouble(1, marks);
                ps.setInt(2, eid);
                int rows = ps.executeUpdate();
                if (rows == 0) {
                    throw new IllegalArgumentException("Enrollment ID " + eid + " not found!");
                }
            }
        } else {
            EnrollmentRecord record = inMemoryEnrollments.get(eid);
            if (record == null) {
                throw new IllegalArgumentException("Enrollment ID " + eid + " not found!");
            }
            switch (col) {
                case "cia1": record.cia1 = marks; break;
                case "cia2": record.cia2 = marks; break;
                case "cia_other": record.ciaOther = marks; break;
                case "ese": record.ese = marks; break;
            }
        }
    }

    /**
     * 3. Generate and print the KEC R2024 Grade Sheet.
     */
    public void generateGradeSheet(String roll) throws SQLException {
        if (useDatabase) {
            generateGradeSheetDB(roll);
        } else {
            generateGradeSheetMem(roll);
        }
    }

    private void generateGradeSheetDB(String roll) throws SQLException {
        String studentSql = "SELECT * FROM students WHERE roll_no = ?";
        String name = "";
        String dept = "";
        int sem = 1;

        try (PreparedStatement ps = connection.prepareStatement(studentSql)) {
            ps.setString(1, roll);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                name = rs.getString("name");
                dept = rs.getString("dept");
                sem = rs.getInt("sem");
            } else {
                System.out.println("No student found with Roll No: " + roll);
                return;
            }
        }

        String enrollSql = "SELECT * FROM enrollments WHERE roll_no = ?";
        List<EnrollmentRecord> list = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement(enrollSql)) {
            ps.setString(1, roll);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                EnrollmentRecord er = new EnrollmentRecord(
                    rs.getInt("enrollment_id"),
                    rs.getString("roll_no"),
                    rs.getString("course_code"),
                    rs.getString("course_name"),
                    rs.getInt("credits")
                );
                er.cia1 = rs.getDouble("cia1");
                er.cia2 = rs.getDouble("cia2");
                er.ciaOther = rs.getDouble("cia_other");
                er.ese = rs.getDouble("ese");
                list.add(er);
            }
        }

        printReport(roll, name, dept, sem, list);
    }

    private void generateGradeSheetMem(String roll) {
        StudentRecord student = inMemoryStudents.get(roll);
        if (student == null) {
            System.out.println("No student found with Roll No: " + roll);
            return;
        }

        List<EnrollmentRecord> list = new ArrayList<>();
        for (EnrollmentRecord er : inMemoryEnrollments.values()) {
            if (er.rollNo.equalsIgnoreCase(roll)) {
                list.add(er);
            }
        }

        printReport(student.rollNo, student.name, student.dept, student.sem, list);
    }

    /**
     * Prints the formatted grade sheet according to KEC R2024 calculation rules.
     * 
     * KEC R2024 Weightages:
     * - CIA Total: Max 40 marks (CIA1 + CIA2 + CIA_OTHER)
     * - ESE Total: Max 60 marks
     * - Total Marks: Max 100 marks
     * 
     * Passing Criteria:
     * - Minimum 40% in ESE (24/60)
     * - Minimum 50% overall (50/100)
     */
    private void printReport(String roll, String name, String dept, int sem, List<EnrollmentRecord> list) {
        String lineEq = "========================================================================================";
        String lineDash = "----------------------------------------------------------------------------------------";
        System.out.println("\n" + lineEq);
        System.out.println("                 KONGU ENGINEERING COLLEGE (Autonomous)");
        System.out.println("               Regulation 2024 (R2024) - Grade Sheet");
        System.out.println(lineEq);
        System.out.printf("Roll No     : %-20s  Name     : %s\n", roll, name);
        System.out.printf("Department  : %-20s  Semester : %d\n", dept, sem);
        System.out.println(lineDash);
        System.out.printf("%-5s %-10s %-25s %-4s %-6s %-6s %-6s %-7s %-7s %-6s %-5s %-3s\n",
                "E-ID", "Code", "Course Title", "Cred", "CIA1", "CIA2", "OTHER", "CIA(40)", "ESE(60)", "Total", "Grade", "GP");
        System.out.println(lineDash);

        int totalCredits = 0;
        double weightedPoints = 0.0;
        boolean allPassed = true;

        for (EnrollmentRecord er : list) {
            // CIA calculation: scaled to 40 marks
            // In standard format: CIA1 (/15) + CIA2 (/15) + CIA_OTHER (/10) = 40
            double ciaTotal = (er.cia1 != null ? er.cia1 : 0.0) 
                            + (er.cia2 != null ? er.cia2 : 0.0) 
                            + (er.ciaOther != null ? er.ciaOther : 0.0);
            if (ciaTotal > 40.0) ciaTotal = 40.0;

            double eseMarks = (er.ese != null ? er.ese : 0.0);
            if (eseMarks > 60.0) eseMarks = 60.0;

            double total = ciaTotal + eseMarks;

            // Passing condition: ESE >= 24 (40%) and Total >= 50
            boolean passed = (eseMarks >= 24.0) && (total >= 50.0);
            if (!passed) allPassed = false;

            String grade = getGrade(total, passed);
            int gp = getGradePoint(grade);

            totalCredits += er.credits;
            weightedPoints += (er.credits * gp);

            String title = er.courseName.length() > 24 ? er.courseName.substring(0, 21) + "..." : er.courseName;

            System.out.printf("%-5d %-10s %-25s %-4d %-6.1f %-6.1f %-6.1f %-7.1f %-7.1f %-6.1f %-5s %-3d\n",
                    er.enrollmentId, er.courseCode, title, er.credits,
                    er.cia1, er.cia2, er.ciaOther, ciaTotal, eseMarks, total, grade, gp);
        }

        System.out.println(lineDash);
        double gpa = (totalCredits > 0) ? (weightedPoints / totalCredits) : 0.0;
        System.out.printf("Total Credits Registered: %-4d | Earned Grade Points: %-6.1f | SGPA: %.2f\n",
                totalCredits, weightedPoints, gpa);
        System.out.println("Final Result Status     : " + (allPassed ? "PASS" : "RA (Re-Appearance)"));
        System.out.println(lineEq + "\n");
    }

    private String getGrade(double total, boolean passed) {
        if (!passed) return "U"; // Re-Appearance (Fail)
        if (total >= 90.0) return "O";
        if (total >= 80.0) return "A+";
        if (total >= 70.0) return "A";
        if (total >= 60.0) return "B+";
        if (total >= 50.0) return "B";
        return "U";
    }

    private int getGradePoint(String grade) {
        switch (grade) {
            case "O":  return 10;
            case "A+": return 9;
            case "A":  return 8;
            case "B+": return 7;
            case "B":  return 6;
            default:   return 0; // U / RA
        }
    }
}
