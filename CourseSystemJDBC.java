import java.sql.*;
import java.util.Scanner;
import java.io.Console;
import java.text.SimpleDateFormat;
import javax.mail.*;
import javax.mail.internet.*;
import java.util.Properties;

public class CourseSystemJDBC {

    static Scanner sc = new Scanner(System.in);

    public static void main(String[] args) {

        Connection con = DBConnection.getConnection();

        while (true) {

            System.out.println("\n=====================================");
            System.out.println("      COURSE REGISTRATION SYSTEM");
            System.out.println("=====================================");
            System.out.println("1. Student ");
            System.out.println("2. Admin Login");
            System.out.println("3. Exit");
            System.out.println("=====================================");

            System.out.print("Enter choice: ");
            int choice = sc.nextInt();
            sc.nextLine();

            switch (choice) {

                case 1 -> studentEntryMenu(con);
                case 2 -> adminLogin(con);

                case 3 -> {
                    System.out.println("Thank you for using the system!");
                    return;
                }

                default -> System.out.println("Invalid choice");
            }
        }
    }

   static void studentEntryMenu(Connection con) {

    while (true) {

        System.out.println("\n========= STUDENT PANEL =========");
        System.out.println("1. Login");
        System.out.println("2. Create New Account");
        System.out.println("3. Forgot Password");
        System.out.println("4. Back");
        System.out.println("=================================");

        System.out.print("Enter choice: ");
        int ch = sc.nextInt();
        sc.nextLine();

        switch (ch) {

            case 1 -> studentLoginOnly(con);

            case 2 -> registerStudent(con);

            case 3 -> {
                System.out.print("Enter your Name: ");
                String name = sc.nextLine();
                forgotPassword(con, name);
            }

            case 4 -> { return; }

            default -> System.out.println("Invalid choice");
        }
    }
}


static void studentLoginOnly(Connection con) {

    try {

        System.out.print("Enter Student Name: ");
        String name = sc.nextLine();

        String query = "SELECT * FROM students WHERE name=?";
        PreparedStatement ps = con.prepareStatement(query);
        ps.setString(1, name);

        ResultSet rs = ps.executeQuery();

        if (!rs.next()) {
            System.out.println("User not found. Please create account.");
            return;
        }

        int studentId = rs.getInt("id");
        String dbPassword = rs.getString("password");
        String email = rs.getString("email");

        Console console = System.console();
        String password;

        if (console != null) {
            char[] passArr = console.readPassword("Enter Password: ");
            password = new String(passArr);
        } else {
            System.out.print("Enter Password: ");
            password = sc.nextLine();
        }

        if (password.equals(dbPassword)) {
            System.out.println("\nLogin Successful!");
            System.out.println("Student Email: " + email);
            studentMenu(con, studentId);
        } else {
            System.out.println("Incorrect Password.");
        }

    } catch (Exception e) {
        System.out.println("Error: " + e.getMessage());
    }
}

static void registerStudent(Connection con) {

    try {

        System.out.print("Enter Name: ");
        String name = sc.nextLine();

        // check if already exists
        String check = "SELECT * FROM students WHERE name=?";
        PreparedStatement psCheck = con.prepareStatement(check);
        psCheck.setString(1, name);
        ResultSet rs = psCheck.executeQuery();

        if (rs.next()) {
            System.out.println("User already exists. Try login.");
            return;
        }

        String email;
        while (true) {
            System.out.print("Enter Email: ");
            email = sc.nextLine();
            if (email.contains("@") && email.contains(".")) break;
            System.out.println("Invalid email.");
        }

        Console console = System.console();
        String password;

        while (true) {

            if (console != null) {
                char[] passArr = console.readPassword("Create 4-digit Password: ");
                password = new String(passArr);
            } else {
                System.out.print("Create 4-digit Password: ");
                password = sc.nextLine();
            }

            if (password.length() == 4) break;

            System.out.println("Password must be 4 digits.");
        }

        String insert = "INSERT INTO students(name,email,password) VALUES(?,?,?)";
        PreparedStatement ps = con.prepareStatement(insert);

        ps.setString(1, name);
        ps.setString(2, email);
        ps.setString(3, password);

        ps.executeUpdate();

        System.out.println("Account created successfully!");

    } catch (Exception e) {
        System.out.println("Error: " + e.getMessage());
    }
}


static void forgotPassword(Connection con, String name) {
    try {

        Console console = System.console();
        String newPass;

        System.out.println("+----------------------------------+");
        System.out.println("|        PASSWORD RESET            |");
        System.out.println("+----------------------------------+");

        while (true) {

            if (console != null) {
                char[] passArr = console.readPassword("Enter New 4-digit Password: ");
                newPass = new String(passArr);
            } else {
                System.out.print("Enter New 4-digit Password: ");
                newPass = sc.nextLine();
            }

            if (newPass.length() == 4) break;

            System.out.println("Password must be exactly 4 characters.");
        }

        String update = "UPDATE students SET password=? WHERE name=?";

        PreparedStatement ps = con.prepareStatement(update);
        ps.setString(1, newPass);
        ps.setString(2, name);

        ps.executeUpdate();

        System.out.println("\nPassword updated successfully!");

    } catch (Exception e) {

        System.out.println("Error: " + e.getMessage());
    }
}

    // STUDENT MENU
    static void studentMenu(Connection con, int studentId) {

        while (true) {

            System.out.println("\n----------- STUDENT MENU -----------");
            System.out.println("1. View Courses");
System.out.println("2. Register Course");
System.out.println("3. View Registered Courses");
System.out.println("4. Drop Course");
System.out.println("5. Search Course");
System.out.println("6. Registration History");
System.out.println("7. Logout");
            System.out.println("------------------------------------");

            System.out.print("Enter choice: ");
            int ch = sc.nextInt();

            switch (ch) {

                case 1 -> viewCourses(con);
                case 2 -> registerCourse(con, studentId);
                case 3 -> viewRegisteredCourses(con, studentId);
                case 4 -> dropCourse(con, studentId);
                case 5 -> searchCourse(con);

                case 6 -> registrationHistory(con, studentId);
                case 7 -> { return; }

                default -> System.out.println("Invalid choice");
            }
        }
    }

    // VIEW COURSES
    static void viewCourses(Connection con) {

        try {

            String query =
                    "SELECT c.course_id,c.course_name,c.total_seats," +
                            "(c.total_seats - COUNT(r.course_id)) AS seats_left " +
                            "FROM courses c LEFT JOIN registrations r " +
                            "ON c.course_id=r.course_id " +
                            "GROUP BY c.course_id";

            Statement st = con.createStatement();
            ResultSet rs = st.executeQuery(query);

            System.out.println("+------------+----------------------+-------------+-------------+------------+");
            System.out.println("| Course ID  | Course Name          | Total Seats | Seats Left  | Status     |");
            System.out.println("+------------+----------------------+-------------+-------------+------------+");

            while (rs.next()) {

                int seatsLeft = rs.getInt("seats_left");
                String status = seatsLeft == 0 ? "FULL" : "AVAILABLE";

                System.out.printf("| %-10d | %-20s | %-11d | %-11d | %-10s |\n",
                        rs.getInt("course_id"),
                        rs.getString("course_name"),
                        rs.getInt("total_seats"),
                        seatsLeft,
                        status);
            }

            System.out.println("+------------+----------------------+-------------+-------------+------------+");

        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    // REGISTER COURSE
    static void registerCourse(Connection con, int studentId) {

        viewCourses(con);

        System.out.print("Enter Course ID: ");
        int cid = sc.nextInt();

        try {

            // Check course exists
            String courseCheck = "SELECT * FROM courses WHERE course_id=?";
            PreparedStatement ps1 = con.prepareStatement(courseCheck);
            ps1.setInt(1, cid);

            ResultSet courseRs = ps1.executeQuery();

            if (!courseRs.next()) {
                System.out.println("Invalid Course ID.");
                return;
            }

            // Check duplicate
            String dup = "SELECT * FROM registrations WHERE student_id=? AND course_id=?";
            PreparedStatement ps2 = con.prepareStatement(dup);
            ps2.setInt(1, studentId);
            ps2.setInt(2, cid);

            ResultSet dupRs = ps2.executeQuery();

            if (dupRs.next()) {
                System.out.println("You already registered for this course.");
                return;
            }

            // Check seats
            String seatQuery =
                    "SELECT total_seats - COUNT(r.course_id) AS seats_left " +
                            "FROM courses c LEFT JOIN registrations r " +
                            "ON c.course_id=r.course_id WHERE c.course_id=?";

            PreparedStatement ps3 = con.prepareStatement(seatQuery);
            ps3.setInt(1, cid);

            ResultSet seatRs = ps3.executeQuery();

            if (seatRs.next() && seatRs.getInt("seats_left") <= 0) {
                System.out.println("Course Full.");
                return;
            }

            // Insert registration
            String query = "INSERT INTO registrations(student_id, course_id, status) VALUES(?, ?, 'PENDING')";

PreparedStatement ps = con.prepareStatement(query);

ps.setInt(1, studentId);
ps.setInt(2, cid);

ps.executeUpdate();

System.out.println("Registration request sent to Admin for approval.");

        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    static void viewRegisteredCourses(Connection con, int studentId) {
    try {

        String query =
        "SELECT c.course_id, c.course_name " +
        "FROM courses c JOIN registrations r " +
        "ON c.course_id = r.course_id " +
        "WHERE r.student_id = ? AND r.status = 'APPROVED'";

        PreparedStatement ps = con.prepareStatement(query);
        ps.setInt(1, studentId);

        ResultSet rs = ps.executeQuery();

        boolean found = false;

        System.out.println("+------------+----------------------------+");
        System.out.println("| Course ID  | Course Name                |");
        System.out.println("+------------+----------------------------+");

        while (rs.next()) {
            found = true;
            System.out.printf("| %-10d | %-26s |\n",
                    rs.getInt("course_id"),
                    rs.getString("course_name"));
        }

        if (!found) {
            System.out.printf("| %-10s | %-26s |\n", "-", "No Approved Courses");
        }

        System.out.println("+------------+----------------------------+");

    } catch (Exception e) {
        System.out.println("Error: " + e.getMessage());
    }
}

    static void dropCourse(Connection con, int studentId) {
    System.out.print("Enter Course ID to Drop: ");
    int cid = sc.nextInt();

    try {
        // Only allow dropping APPROVED courses
        String query = "DELETE FROM registrations WHERE student_id=? AND course_id=? AND status='APPROVED'";
        PreparedStatement ps = con.prepareStatement(query);
        ps.setInt(1, studentId);
        ps.setInt(2, cid);

        int rows = ps.executeUpdate();

        if (rows > 0) {
            System.out.println("Course Dropped Successfully!");
        } else {
            System.out.println("-- Cannot drop this course. Either not registered or pending approval --");
        }

    } catch (Exception e) {
        System.out.println("Error: " + e.getMessage());
    }
}

    // SEARCH COURSE
static void searchCourse(Connection con) {

    try {

        sc.nextLine();

        System.out.print("Enter Course Name: ");
        String name = sc.nextLine();

        String query =
        "SELECT c.course_id, c.course_name, c.total_seats, " +
        "(c.total_seats - COUNT(r.course_id)) AS seats_left " +
        "FROM courses c LEFT JOIN registrations r " +
        "ON c.course_id = r.course_id " +
        "WHERE c.course_name LIKE ? " +
        "GROUP BY c.course_id";

        PreparedStatement ps = con.prepareStatement(query);
        ps.setString(1, "%" + name + "%");

        ResultSet rs = ps.executeQuery();

        boolean found = false;

        System.out.println("+------------+----------------------+-------------+-------------+");
        System.out.println("| Course ID  | Course Name          | Total Seats | Seats Left  |");
        System.out.println("+------------+----------------------+-------------+-------------+");

        while (rs.next()) {

            found = true;

            System.out.printf("| %-10d | %-20s | %-11d | %-11d |\n",
                    rs.getInt("course_id"),
                    rs.getString("course_name"),
                    rs.getInt("total_seats"),
                    rs.getInt("seats_left"));
        }

        if (!found) {

            System.out.printf("| %-10s | %-20s | %-11s | %-11s |\n",
                    "-", "Course Not Found", "-", "-");
        }

        System.out.println("+------------+----------------------+-------------+-------------+");

    } catch (Exception e) {

        System.out.println("Error: " + e.getMessage());
    }
}

// =============================
// REGISTRATION HISTORY (STUDENT) - Clean Table Format with Admin Approval
// =============================
static void registrationHistory(Connection con, int studentId) {
    try {
        // Column widths
        int idWidth = 10;
        int nameWidth = 25;
        int statusWidth = 10;
        int requestedWidth = 20;
        int approvedWidth = 20;

        // Separator line
        String line = "+"
                + "-".repeat(idWidth + 2) + "+"
                + "-".repeat(nameWidth + 2) + "+"
                + "-".repeat(statusWidth + 2) + "+"
                + "-".repeat(requestedWidth + 2) + "+"
                + "-".repeat(approvedWidth + 2) + "+";

        System.out.println(line);
        System.out.printf("| %-10s | %-25s | %-10s | %-20s | %-20s |\n",
                "Course ID", "Course Name", "Status", "Requested On", "Approved On");
        System.out.println(line);

        String query =
                "SELECT c.course_id, c.course_name, r.status, r.created_at, r.approved_at " +
                "FROM registrations r " +
                "JOIN courses c ON r.course_id = c.course_id " +
                "WHERE r.student_id=? " +
                "ORDER BY r.created_at DESC";

        PreparedStatement ps = con.prepareStatement(query);
        ps.setInt(1, studentId);
        ResultSet rs = ps.executeQuery();

        boolean found = false;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        while (rs.next()) {
            found = true;

            int cid = rs.getInt("course_id");
            String cname = rs.getString("course_name");
            String status = rs.getString("status");
            Timestamp requested = rs.getTimestamp("created_at");
            Timestamp approved = rs.getTimestamp("approved_at");

            String requestedStr = (requested != null) ? sdf.format(requested) : "-";
            String approvedStr = (approved != null) ? sdf.format(approved) : "-";

            // Truncate long course names
            if (cname.length() > nameWidth) cname = cname.substring(0, nameWidth - 3) + "...";

            System.out.printf("| %-10d | %-25s | %-10s | %-20s | %-20s |\n",
                    cid, cname, status, requestedStr, approvedStr);
        }

        if (!found) {
            System.out.printf("| %-10s | %-25s | %-10s | %-20s | %-20s |\n",
                    "-", "No registration history", "-", "-", "-");
        }

        System.out.println(line);

    } catch (Exception e) {
        System.out.println("Error: " + e.getMessage());
    }
}

// ADMIN LOGIN WITH HIDDEN PASSWORD
static void adminLogin(Connection con) {

    try {

        System.out.print("Enter Admin Username: ");
        String user = sc.nextLine().trim();

        if (user.isEmpty()) {
            System.out.println("Username cannot be empty!");
            return;
        }

        String pass;

        Console console = System.console();

        if (console != null) {

            char[] passwordArray = console.readPassword("Enter Admin Password: ");
            pass = new String(passwordArray);

        } else {

            // fallback if console not supported (IDE)
            pass = readPassword("Enter Admin Password: ");
        }

        if (pass.isEmpty()) {
            System.out.println("Password cannot be empty!");
            return;
        }

        // DATABASE CHECK (instead of hardcoded)
        String query = "SELECT * FROM admins WHERE username=? AND password=?";
        PreparedStatement ps = con.prepareStatement(query);

        ps.setString(1, user);
        ps.setString(2, pass);

        ResultSet rs = ps.executeQuery();

        if (rs.next()) {

            System.out.println("\nLogin Successful !!!");
            adminMenu(con);

        } else {

            System.out.println("\nInvalid credentials.");
        }

    } catch (Exception e) {
        System.out.println("Error: " + e.getMessage());
    }
}

    // ADMIN MENU
static void adminMenu(Connection con) {

    while (true) {

        System.out.println("----------- ADMIN MENU -----------");
System.out.println("1. View Courses");
System.out.println("2. Add Course");
System.out.println("3. Delete Course");
System.out.println("4. Dashboard");
System.out.println("5. View Students Per Course");
System.out.println("6. Approve Registrations");
System.out.println("7. Search Course");
System.out.println("8. View All Students");
System.out.println("9. Create New Admin");
System.out.println("10. Exit");
System.out.println("----------------------------------");

        System.out.print("Enter choice: ");
        int ch = sc.nextInt();

        switch(ch) {

    case 1 -> viewCourses(con);

    case 2 -> addCourse(con);

    case 3 -> deleteCourse(con);

    case 4 -> dashboard(con);

    case 5 -> studentsPerCourse(con);

    case 6 -> approveRegistrations(con);

    case 7 -> searchCourse1(con);  // Admin search option

    case 8 -> viewAllStudents(con);

    case 9 -> createStaff(con);

    case 10 -> { return; }

    default -> System.out.println("Invalid choice");
}

    }
}

    // ADD COURSE
    static void addCourse(Connection con) {

        try {

            System.out.print("Enter Course ID: ");
            int id = sc.nextInt();
            sc.nextLine();

            System.out.print("Enter Course Name: ");
            String name = sc.nextLine();

            System.out.print("Enter Total Seats: ");
            int seats = sc.nextInt();

            String query = "INSERT INTO courses VALUES(?,?,?)";

            PreparedStatement ps = con.prepareStatement(query);

            ps.setInt(1, id);
            ps.setString(2, name);
            ps.setInt(3, seats);

            ps.executeUpdate();

            System.out.println("Course Added Successfully!");

        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    // DELETE COURSE
    static void deleteCourse(Connection con) {

        try {

            System.out.print("Enter Course ID to delete: ");
            int id = sc.nextInt();

            String query = "DELETE FROM courses WHERE course_id=?";

            PreparedStatement ps = con.prepareStatement(query);
            ps.setInt(1, id);

            ps.executeUpdate();

            System.out.println("Course Deleted Successfully!");

        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    // DASHBOARD (Admin) - counts only APPROVED registrations
static void dashboard(Connection con) {
    try {
        Statement st = con.createStatement();

        // Total Students
        ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM students");
        rs.next();
        int students = rs.getInt(1);

        // Total Courses
        rs = st.executeQuery("SELECT COUNT(*) FROM courses");
        rs.next();
        int courses = rs.getInt(1);

        // Total Registrations (only APPROVED)
        rs = st.executeQuery("SELECT COUNT(*) FROM registrations WHERE status='APPROVED'");
        rs.next();
        int registrations = rs.getInt(1);

        System.out.println("\n+----------------------+----------------+");
        System.out.println("| Metric               | Count          |");
        System.out.println("+----------------------+----------------+");

        System.out.printf("| %-20s | %-14d |\n", "Total Students", students);
        System.out.printf("| %-20s | %-14d |\n", "Total Courses", courses);
        System.out.printf("| %-20s | %-14d |\n", "Total Registrations", registrations);

        System.out.println("+----------------------+----------------+");

        // Top 3 courses based on APPROVED registrations only
        String topQuery =
        "SELECT c.course_name, COUNT(r.course_id) AS students " +
        "FROM courses c LEFT JOIN registrations r " +
        "ON c.course_id = r.course_id AND r.status='APPROVED' " +  // only APPROVED
        "GROUP BY c.course_id ORDER BY students DESC LIMIT 3";

        rs = st.executeQuery(topQuery);

        System.out.println("\nTop 3 Popular Courses");
        System.out.println("+----------------------+----------------+");
        System.out.println("| Course Name          | Students       |");
        System.out.println("+----------------------+----------------+");

        while(rs.next()){
            System.out.printf("| %-20s | %-14d |\n",
                    rs.getString("course_name"),
                    rs.getInt("students"));
        }

        System.out.println("+----------------------+----------------+");

    } catch(Exception e){
        System.out.println("Error: " + e.getMessage());
    }
}

static void studentsPerCourse(Connection con) {
    try {

        String courseQuery = "SELECT course_id, course_name FROM courses";
        Statement courseStmt = con.createStatement();
        ResultSet courses = courseStmt.executeQuery(courseQuery);

        while (courses.next()) {

            int courseId = courses.getInt("course_id");
            String courseName = courses.getString("course_name");

            System.out.println("\nCourse ID: " + courseId + " | Course Name: " + courseName);
            System.out.println("+------------+----------------------+");
            System.out.println("| Student ID | Student Name         |");
            System.out.println("+------------+----------------------+");

            // Only show approved students
            String studentQuery =
                    "SELECT s.id, s.name FROM registrations r " +
                    "JOIN students s ON r.student_id = s.id " +
                    "WHERE r.course_id = ? AND r.status = 'APPROVED'";

            PreparedStatement ps = con.prepareStatement(studentQuery);
            ps.setInt(1, courseId);

            ResultSet rs = ps.executeQuery();

            boolean found = false;
            while (rs.next()) {
                found = true;
                System.out.printf("| %-10d | %-20s |\n",
                        rs.getInt("id"),
                        rs.getString("name"));
            }

            if (!found) {
                System.out.printf("| %-10s | %-20s |\n", "-", "No Students");
            }

            System.out.println("+------------+----------------------+");
        }

    } catch (Exception e) {
        System.out.println("Error: " + e.getMessage());
    }
}


// APPROVE REGISTRATIONS (Admin) - with Email Integration
static void approveRegistrations(Connection con) {
    try {

        // Fetch all pending registrations
        String query =
        "SELECT r.student_id, s.name AS student_name, s.email, " +
        "c.course_id, c.course_name " +
        "FROM registrations r " +
        "JOIN students s ON r.student_id = s.id " +
        "JOIN courses c ON r.course_id = c.course_id " +
        "WHERE r.status='PENDING'";

        PreparedStatement ps = con.prepareStatement(query);
        ResultSet rs = ps.executeQuery();

        System.out.println("+------------+----------------------+------------+------------------------+");
        System.out.println("| Student ID | Student Name         | Course ID  | Course Name            |");
        System.out.println("+------------+----------------------+------------+------------------------+");

        boolean found = false;

        while(rs.next()){
            found = true;

            int sid = rs.getInt("student_id");
            String sname = rs.getString("student_name");
            int cid = rs.getInt("course_id");
            String cname = rs.getString("course_name");

            System.out.printf("| %-10d | %-20s | %-10d | %-22s |\n",
                    sid, sname, cid, cname);
        }

        if(!found){
            System.out.printf("| %-10s | %-20s | %-10s | %-22s |\n",
                    "-", "No Pending Requests", "-", "-");
        }

        System.out.println("+------------+----------------------+------------+------------------------+");

        // Only ask if pending exists
        if(found){

            System.out.print("Enter Student ID to approve: ");
            int sid = sc.nextInt();

            System.out.print("Enter Course ID to approve: ");
            int cid = sc.nextInt();

            sc.nextLine(); // buffer fix

            //  Get student email + details
            String fetchDetails =
            "SELECT s.name, s.email, c.course_name " +
            "FROM registrations r " +
            "JOIN students s ON r.student_id = s.id " +
            "JOIN courses c ON r.course_id = c.course_id " +
            "WHERE r.student_id=? AND r.course_id=?";

            PreparedStatement psFetch = con.prepareStatement(fetchDetails);
            psFetch.setInt(1, sid);
            psFetch.setInt(2, cid);

            ResultSet rs2 = psFetch.executeQuery();

            if(!rs2.next()){
                System.out.println("Invalid selection.");
                return;
            }

            String studentName = rs2.getString("name");
            String email = rs2.getString("email");
            String courseName = rs2.getString("course_name");

            //  Update status
            String approveQuery =
            "UPDATE registrations " +
            "SET status='APPROVED', approved_at=CURRENT_TIMESTAMP " +
            "WHERE student_id=? AND course_id=? AND status='PENDING'";

            PreparedStatement ps2 = con.prepareStatement(approveQuery);
            ps2.setInt(1, sid);
            ps2.setInt(2, cid);

            int updated = ps2.executeUpdate();

            if(updated > 0){

    System.out.println("Registration Approved Successfully!");

    // Send Email
    sendEmail(email, studentName, courseName, "APPROVED");

} else {
    System.out.println("Request not found or already approved.");
}
        }

    } catch(Exception e){
        System.out.println("Error: " + e.getMessage());
    }
}

static void sendEmail(String toEmail, String studentName, String courseName, String status) {

    final String fromEmail = "mathanpmk2007@gmail.com";
    final String password = "sgovxfgdqdvypxha";

    Properties props = new Properties();
    props.put("mail.smtp.auth", "true");
props.put("mail.smtp.starttls.enable", "true");
props.put("mail.smtp.host", "smtp.gmail.com");
props.put("mail.smtp.port", "587");
props.put("mail.smtp.ssl.trust", "smtp.gmail.com");
props.put("mail.smtp.ssl.protocols", "TLSv1.2");

    // Session session = Session.getInstance(props,
    //     new Authenticator() {
    //         protected PasswordAuthentication getPasswordAuthentication() {
    //             return new PasswordAuthentication(fromEmail, password);
    //         }
    //     });
System.setProperty("https.protocols", "TLSv1.2");

Session session = Session.getInstance(props,
    new Authenticator() {
        protected PasswordAuthentication getPasswordAuthentication() {
            return new PasswordAuthentication(fromEmail, password);
        }
    });

    try {

        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress(fromEmail));
        message.setRecipients(Message.RecipientType.TO,
                InternetAddress.parse(toEmail));
session.setDebug(true);
        String subject = "Course Registration " + status + " Notification";
        message.setSubject(subject);

        String msg;

        if(status.equals("APPROVED")) {
            msg = "Hello " + studentName + ",\n\n"
                + "Congratulations !!!\n"
                + "Your registration for the course \"" + courseName + "\" has been APPROVED.\n\n"
                + "You can now attend the course.\n\n"
                + "Approved Time: " + java.time.LocalDateTime.now() + "\n\n"
                + "Best Regards,\nCourse Management Team";
        } else {
            msg = "Hello " + studentName + ",\n\n"
                + "We regret to inform you that your registration for the course \"" + courseName + "\" has been REJECTED.\n\n"
                + "Please contact admin for more details.\n\n"
                + "Checked Time: " + java.time.LocalDateTime.now() + "\n\n"
                + "Regards,\nCourse Management Team";
        }

        message.setText(msg);

        Transport.send(message);

        System.out.println("📧 Email sent successfully!");

    } catch (Exception e) {
        System.out.println("Email failed: " + e.getMessage());
    }
}

// SEARCH COURSE (Admin) - properly aligned
static void searchCourse1(Connection con) {
    try {
        sc.nextLine();
        System.out.print("Enter Course Name to search: ");
        String name = sc.nextLine();

        String query = "SELECT c.course_id, c.course_name, c.total_seats, " +
                       "(c.total_seats - COUNT(r.course_id)) AS seats_left, " +
                       "COUNT(r.student_id) AS registered " +
                       "FROM courses c LEFT JOIN registrations r " +
                       "ON c.course_id = r.course_id " +
                       "WHERE c.course_name LIKE ? " +
                       "GROUP BY c.course_id";

        PreparedStatement ps = con.prepareStatement(query);
        ps.setString(1, "%" + name + "%");
        ResultSet rs = ps.executeQuery();

        System.out.println("+------------+----------------------+-------------+-------------+------------+");
        System.out.println("| Course ID  | Course Name          | Total Seats | Seats Left  | Registered |");
        System.out.println("+------------+----------------------+-------------+-------------+------------+");

        boolean found = false;
        while(rs.next()){
            found = true;
            System.out.printf("| %-10d | %-20s | %-11d | %-11d | %-10d |\n",
                    rs.getInt("course_id"),
                    rs.getString("course_name"),
                    rs.getInt("total_seats"),
                    rs.getInt("seats_left"),
                    rs.getInt("registered"));
        }

        if(!found){
            System.out.printf("| %-10s | %-20s | %-11s | %-11s | %-10s |\n",
                    "-", "No course found", "-", "-", "-");
        }

        System.out.println("+------------+----------------------+-------------+-------------+------------+");

    } catch(Exception e){
        System.out.println("Error: " + e.getMessage());
    }
}

// =============================
// VIEW STUDENT DETAILS (Admin) - Properly Aligned
// =============================
static void viewAllStudents(Connection con) {
    try {
        // Fetch all students
        String studentQuery = "SELECT id, name, email FROM students";
        Statement st = con.createStatement();
        ResultSet students = st.executeQuery(studentQuery);

        // Define column widths
        int idWidth = 10;
        int nameWidth = 25;
        int emailWidth = 30;
        int courseWidth = 40;

        String line = "+" + "-".repeat(idWidth + 2) 
                         + "+" + "-".repeat(nameWidth + 2)
                         + "+" + "-".repeat(emailWidth + 2)
                         + "+" + "-".repeat(courseWidth + 2) + "+";

        // Table header
        System.out.println(line);
        System.out.printf("| %-10s | %-25s | %-30s | %-40s |\n",
                "Student ID", "Student Name", "Email", "Registered Courses");
        System.out.println(line);

        boolean foundStudent = false;

        while (students.next()) {
            foundStudent = true;
            int studentId = students.getInt("id");
            String name = students.getString("name");
            String email = students.getString("email");

            // Fetch courses registered and approved for this student
            String courseQuery =
                "SELECT c.course_name " +
                "FROM registrations r " +
                "JOIN courses c ON r.course_id = c.course_id " +
                "WHERE r.student_id=? AND r.status='APPROVED'";

            PreparedStatement ps = con.prepareStatement(courseQuery);
            ps.setInt(1, studentId);
            ResultSet courses = ps.executeQuery();

            StringBuilder courseList = new StringBuilder();
            while (courses.next()) {
                if (courseList.length() > 0) courseList.append(", ");
                courseList.append(courses.getString("course_name"));
            }

            String registeredCourses = courseList.length() > 0 ? courseList.toString() : "None";

            // Truncate if too long to fit column
            name = name.length() > nameWidth ? name.substring(0, nameWidth - 3) + "..." : name;
            email = email.length() > emailWidth ? email.substring(0, emailWidth - 3) + "..." : email;
            registeredCourses = registeredCourses.length() > courseWidth ? registeredCourses.substring(0, courseWidth - 3) + "..." : registeredCourses;

            System.out.printf("| %-10d | %-25s | %-30s | %-40s |\n",
                    studentId, name, email, registeredCourses);
        }

        if (!foundStudent) {
            System.out.printf("| %-10s | %-25s | %-30s | %-40s |\n",
                    "-", "No Students Found", "-", "-");
        }

        System.out.println(line);

    } catch (Exception e) {
        System.out.println("Error: " + e.getMessage());
    }
}

static void createStaff(Connection con) {

    try {

        sc.nextLine(); //FIX buffer issue

        System.out.print("Enter New Staff Username: ");
        String username = sc.nextLine().trim();

        if (username.isEmpty()) {
            System.out.println("Username cannot be empty!");
            return;
        }

        // check already exists
        String check = "SELECT * FROM admins WHERE username=?";
        PreparedStatement psCheck = con.prepareStatement(check);
        psCheck.setString(1, username);

        ResultSet rs = psCheck.executeQuery();

        if (rs.next()) {
            System.out.println("Username already exists!");
            return;
        }

        // Use reusable password method (best practice)
        String password = readPassword("Enter Password: ");

        if (password.isEmpty()) {
            System.out.println("Password cannot be empty!");
            return;
        }

        String insert = "INSERT INTO admins(username, password) VALUES(?,?)";
        PreparedStatement ps = con.prepareStatement(insert);

        ps.setString(1, username);
        ps.setString(2, password);

        int rows = ps.executeUpdate();

        if (rows > 0) {
            System.out.println("New Staff Created Successfully!");
        } else {
            System.out.println("Failed to create staff.");
        }

    } catch (Exception e) {
        System.out.println("Error: " + e.getMessage());
    }
}


static String readPassword(String prompt) {

    Console console = System.console();

    if (console != null) {
        char[] passArr = console.readPassword(prompt);
        return new String(passArr);
    } else {
        // Fallback (IDE will show password)
        System.out.print(prompt);
        return sc.nextLine();
    }
}


}