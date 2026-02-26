package test;

import java.io.*;
import java.util.*;

// --- ENUMS ---
enum BookingStatus { PENDING, APPROVED, REJECTED, CANCELLED }
enum MaintenanceStatus { REPORTED, IN_PROGRESS, RESOLVED }
enum Role { STUDENT, STAFF, ADMIN }

class DatabaseManager {
    private static final String USERS_FILE = "users.txt";
    private static final String FACILITIES_FILE = "facilities.txt";
    private static final String BOOKINGS_FILE = "bookings.txt";
    private static final String ISSUES_FILE = "issues.txt";

    // --- SAVING DATA ---
    public static void saveAllData(List<User> users, List<Facility> facilities, List<Booking> bookings, List<IssueReport> issues) {
        System.out.println("Saving database to .txt files...");
        saveUsers(users);
        saveFacilities(facilities);
        saveBookings(bookings);
        // Teammate task: saveIssues(issues); can be added here easily
    }

    private static void saveUsers(List<User> users) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(USERS_FILE))) {
            for (User u : users) {
                // Format: Role, ID, Password, Name, Faculty(if applicable)
                if (u.getRole() == Role.ADMIN) {
                    bw.write("ADMIN," + u.getUserId() + "," + u.password + "," + u.getName() + ",NONE");
                } else {
                    StandardUser su = (StandardUser) u;
                    bw.write(su.getRole() + "," + su.getUserId() + "," + su.password + "," + su.getName() + "," + su.getFaculty());
                }
                bw.newLine();
            }
        } catch (IOException e) {
            System.err.println("Error saving users: " + e.getMessage());
        }
    }

    private static void saveFacilities(List<Facility> facilities) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(FACILITIES_FILE))) {
            for (Facility f : facilities) {
                // Format: ID, Type, isAvailable, issueCount
                bw.write(f.getId() + "," + f.getType() + "," + f.isAvailable() + "," + f.getIssueCount());
                bw.newLine();
            }
        } catch (IOException e) {
            System.err.println("Error saving facilities: " + e.getMessage());
        }
    }

    private static void saveBookings(List<Booking> bookings) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(BOOKINGS_FILE))) {
            for (Booking b : bookings) {
                // Format: BookingID, UserID, FacilityID, Date, TimeSlot, Status
                bw.write(b.getBookingId() + "," + b.getBookedBy().getUserId() + "," + b.getFacility().getId() + "," + 
                         b.getDate() + "," + b.getTimeSlot() + "," + b.getStatus());
                bw.newLine();
            }
        } catch (IOException e) {
            System.err.println("Error saving bookings: " + e.getMessage());
        }
    }

    // --- LOADING DATA ---
    // Order matters: Must load Users and Facilities BEFORE Bookings, because Bookings rely on them.
    public static void loadAllData(List<User> users, List<Facility> facilities, List<Booking> bookings) {
        System.out.println("Loading database from .txt files...");
        loadUsers(users);
        loadFacilities(facilities);
        loadBookings(users, facilities, bookings);
    }

    private static void loadUsers(List<User> users) {
        File file = new File(USERS_FILE);
        if (!file.exists()) return;

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] data = line.split(",");
                if (data.length < 5) continue;
                
                String roleStr = data[0];
                String id = data[1];
                String pass = data[2];
                String name = data[3];
                String faculty = data[4];

                if (roleStr.equals("ADMIN")) {
                    users.add(new Admin(id, pass, name));
                } else {
                    Role role = Role.valueOf(roleStr); // STUDENT or STAFF
                    users.add(new StandardUser(id, pass, name, role, faculty));
                }
            }
        } catch (IOException e) {
            System.err.println("Error loading users: " + e.getMessage());
        }
    }

    private static void loadFacilities(List<Facility> facilities) {
        File file = new File(FACILITIES_FILE);
        if (!file.exists()) return;

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] data = line.split(",");
                Facility f = new Facility(data[0], data[1]);
                f.setAvailable(Boolean.parseBoolean(data[2]));
                // Note: to fully restore issue count, you'd add a setter for it in the Facility class
                facilities.add(f); 
            }
        } catch (IOException e) {
            System.err.println("Error loading facilities: " + e.getMessage());
        }
    }

    private static void loadBookings(List<User> users, List<Facility> facilities, List<Booking> bookings) {
        File file = new File(BOOKINGS_FILE);
        if (!file.exists()) return;

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] data = line.split(",");
                String bookingId = data[0];
                String userId = data[1];
                String facilityId = data[2];
                String date = data[3];
                String timeSlot = data[4];
                BookingStatus status = BookingStatus.valueOf(data[5]);

                // Find the actual User and Facility objects based on the IDs
                User bookedBy = null;
                for (User u : users) if (u.getUserId().equals(userId)) bookedBy = u;

                Facility targetFacility = null;
                for (Facility f : facilities) if (f.getId().equals(facilityId)) targetFacility = f;

                if (bookedBy != null && targetFacility != null) {
                    Booking b = new Booking(bookedBy, targetFacility, date, timeSlot);
                    b.setStatus(status); // Restore previous status
                    bookings.add(b);
                }
            }
        } catch (IOException | IllegalArgumentException e) {
            System.err.println("Error loading bookings: " + e.getMessage());
        }
    }
}

// --- ENTITY CLASSES ---
class Facility {
    private String id;
    private String type;
    private boolean isAvailable;
    private int issueCount;

    public Facility(String id, String type) {
        this.id = id;
        this.type = type;
        this.isAvailable = true;
        this.issueCount = 0;
    }

    public String getId() { return id; }
    public String getType() { return type; }
    public boolean isAvailable() { return isAvailable; }
    public void setAvailable(boolean available) { this.isAvailable = available; }
    public void incrementIssueCount() { this.issueCount++; }
    public int getIssueCount() { return issueCount; }

    @Override
    public String toString() {
        return id + " (" + type + ") - " + (isAvailable ? "Available" : "Under Maintenance");
    }
}

class Booking {
    private String bookingId;
    private User bookedBy;
    private Facility facility;
    private String date;
    private String timeSlot;
    private BookingStatus status;

    public Booking(User bookedBy, Facility facility, String date, String timeSlot) {
        this.bookingId = UUID.randomUUID().toString().substring(0, 8);
        this.bookedBy = bookedBy;
        this.facility = facility;
        this.date = date;
        this.timeSlot = timeSlot;
        this.status = BookingStatus.PENDING;
    }

    public String getBookingId() { return bookingId; }
    public BookingStatus getStatus() { return status; }
    public void setStatus(BookingStatus status) { this.status = status; }
    public User getBookedBy() { return bookedBy; }
    public Facility getFacility() { return facility; }

    @Override
    public String toString() {
        return "Booking ID: " + bookingId + " | Facility: " + facility.getId() + " | Date: " + date + " | Slot: " + timeSlot + " | Status: " + status;
    }
}

class IssueReport {
    private String reportId;
    private Facility facility;
    private String description;
    private MaintenanceStatus status;

    public IssueReport(Facility facility, String description) {
        this.reportId = UUID.randomUUID().toString().substring(0, 8);
        this.facility = facility;
        this.description = description;
        this.status = MaintenanceStatus.REPORTED;
    }

    public void setStatus(MaintenanceStatus status) { this.status = status; }

    @Override
    public String toString() {
        return "Report ID: " + reportId + " | Facility: " + facility.getId() + " | Issue: " + description + " | Status: " + status;
    }
}

// --- USER ABSTRACTION ---
abstract class User {
    protected String userId;
    protected String password;
    protected String name;
    protected Role role;

    public User(String userId, String password, String name, Role role) {
        this.userId = userId;
        this.password = password;
        this.name = name;
        this.role = role;
    }

    public String getUserId() { return userId; }
    public String getName() { return name; }
    public Role getRole() { return role; }
    public boolean checkPassword(String input) { return this.password.equals(input); }
}

class StandardUser extends User {
    private String faculty;

    public StandardUser(String userId, String password, String name, Role role, String faculty) {
        super(userId, password, name, role);
        this.faculty = faculty;
    }
}

class Admin extends User {
    public Admin(String userId, String password, String name) {
        super(userId, password, name, Role.ADMIN);
    }
}

// --- MAIN APPLICATION ENGINE ---
public class Main {
    // In-memory databases (To be replaced with .txt file I/O by your team)
    static List<User> usersDatabase = new ArrayList<>();
    static List<Facility> facilitiesDatabase = new ArrayList<>();
    static List<Booking> bookingsDatabase = new ArrayList<>();
    static List<IssueReport> issuesDatabase = new ArrayList<>();
    
    static Scanner scanner = new Scanner(System.in);
    static User loggedInUser = null;

    public static void main(String[] args) {
        // 1. Load data from text files at startup
        DatabaseManager.loadAllData(usersDatabase, facilitiesDatabase, bookingsDatabase);
        
        // If the database is completely empty (first run), add a default admin and some facilities
        if (usersDatabase.isEmpty()) {
            usersDatabase.add(new Admin("admin", "admin123", "System Admin"));
            facilitiesDatabase.add(new Facility("KB207", "Lecture Hall"));
            facilitiesDatabase.add(new Facility("MPH", "Multipurpose Hall"));
        }

        System.out.println("=====================================================");
        System.out.println(" UTAR Smart Campus Booking & Maintenance System");
        System.out.println("=====================================================");

        while (true) {
            if (loggedInUser == null) {
                displayGuestMenu();
            } else if (loggedInUser.getRole() == Role.ADMIN) {
                displayAdminMenu();
            } else {
                displayStudentMenu();
            }
        }
    }

    // --- MENUS ---
    private static void displayGuestMenu() {
        System.out.println("\n1. Login [cite: 9]");
        System.out.println("2. Register New User [cite: 9]");
        System.out.println("3. Exit");
        System.out.print("Select: ");

        String choice = scanner.nextLine();
        switch (choice) {
            case "1": handleLogin(); break;
            case "2": handleRegistration(); break;
            case "3": 
                DatabaseManager.saveAllData(usersDatabase, facilitiesDatabase, bookingsDatabase, issuesDatabase);
                System.out.println("Data saved. Exiting system...");
                System.exit(0); 
                break;
            default: System.out.println("Invalid choice.");
        }
    }

    private static void displayStudentMenu() {
        System.out.println("\n--- Student/Staff Menu (" + loggedInUser.getName() + ") ---");
        System.out.println("1. View Facilities [cite: 11]");
        System.out.println("2. Book a Facility [cite: 12]");
        System.out.println("3. My Bookings (View/Cancel) [cite: 13, 14, 23]");
        System.out.println("4. Report a Facility Issue [cite: 15]");
        System.out.println("9. Logout");
        System.out.print("Select: ");

        String choice = scanner.nextLine();
        switch (choice) {
            case "1": viewFacilities(); break;
            case "2": bookFacility(); break;
            case "3": manageMyBookings(); break;
            case "4": reportIssue(); break;
            case "9": loggedInUser = null; break;
            default: System.out.println("Invalid choice.");
        }
    }

    private static void displayAdminMenu() {
        System.out.println("\n--- Admin Menu (" + loggedInUser.getName() + ") ---");
        System.out.println("1. Process Booking Requests [cite: 18]");
        System.out.println("2. Manage Facility Maintenance [cite: 19, 20]");
        System.out.println("3. Generate Analytics Report [cite: 21, 22]");
        System.out.println("9. Logout");
        System.out.print("Select: ");

        String choice = scanner.nextLine();
        switch (choice) {
            case "1": processBookings(); break;
            case "2": manageMaintenance(); break;
            case "3": generateAnalytics(); break;
            case "9": loggedInUser = null; break;
            default: System.out.println("Invalid choice.");
        }
    }

    // --- CORE FUNCTIONS ---
    private static void handleLogin() {
        System.out.print("Enter User ID: ");
        String id = scanner.nextLine();
        System.out.print("Enter Password: ");
        String pass = scanner.nextLine();

        for (User u : usersDatabase) {
            if (u.getUserId().equals(id) && u.checkPassword(pass)) {
                loggedInUser = u;
                System.out.println("Login successful! Welcome, " + u.getName());
                return;
            }
        }
        System.out.println("Invalid credentials.");
    }

    private static void handleRegistration() {
        System.out.println("\n--- User Registration ---");
        System.out.print("Enter New User ID: ");
        String id = scanner.nextLine();
        System.out.print("Enter Password: ");
        String pass = scanner.nextLine();
        System.out.print("Enter Full Name: ");
        String name = scanner.nextLine();
        
        System.out.println("Select Account Type:");
        System.out.println("1. Student");
        System.out.println("2. Staff");
        System.out.println("3. Admin");
        System.out.print("Select (1-3): ");
        String roleChoice = scanner.nextLine();
        
        if (roleChoice.equals("3")) {
            // Instantiate an Admin object
            usersDatabase.add(new Admin(id, pass, name));
            System.out.println("Admin registration successful! You can now log in.");
        } else {
            // Instantiate a StandardUser (Student or Staff)
            System.out.print("Enter Faculty/Department: ");
            String faculty = scanner.nextLine();
            
            Role role = roleChoice.equals("2") ? Role.STAFF : Role.STUDENT;
            usersDatabase.add(new StandardUser(id, pass, name, role, faculty));
            
            System.out.println(role + " registration successful! You can now log in.");
        }
    }

    private static void viewFacilities() {
        System.out.println("\n--- Campus Facilities ---");
        for (Facility f : facilitiesDatabase) {
            System.out.println(f.toString());
        }
    }

    private static void bookFacility() {
        viewFacilities();
        System.out.print("Enter Facility ID to book: ");
        String facId = scanner.nextLine();
        
        Facility target = null;
        for (Facility f : facilitiesDatabase) {
            if (f.getId().equalsIgnoreCase(facId) && f.isAvailable()) {
                target = f; break;
            }
        }

        if (target == null) {
            System.out.println("Facility not found or currently under maintenance.");
            return;
        }

        System.out.print("Enter Date (DD/MM/YYYY): ");
        String date = scanner.nextLine();
        System.out.print("Enter Time Slot (e.g., 14:00-16:00): ");
        String time = scanner.nextLine();

        bookingsDatabase.add(new Booking(loggedInUser, target, date, time));
        System.out.println("Booking request submitted! Awaiting Admin approval.");
    }

    private static void manageMyBookings() {
        System.out.println("\n--- My Bookings ---");
        List<Booking> myBookings = new ArrayList<>();
        
        for (Booking b : bookingsDatabase) {
            if (b.getBookedBy().getUserId().equals(loggedInUser.getUserId())) {
                myBookings.add(b);
                System.out.println(b.toString());
            }
        }

        if (myBookings.isEmpty()) {
            System.out.println("You have no bookings.");
            return;
        }

        System.out.print("Enter Booking ID to cancel (or press Enter to go back): ");
        String cancelId = scanner.nextLine();
        if (!cancelId.isEmpty()) {
            for (Booking b : myBookings) {
                if (b.getBookingId().equals(cancelId)) {
                    b.setStatus(BookingStatus.CANCELLED);
                    System.out.println("Booking cancelled successfully.");
                    return;
                }
            }
            System.out.println("Invalid Booking ID.");
        }
    }

    private static void reportIssue() {
        viewFacilities();
        System.out.print("Enter Facility ID to report an issue: ");
        String facId = scanner.nextLine();
        
        for (Facility f : facilitiesDatabase) {
            if (f.getId().equalsIgnoreCase(facId)) {
                System.out.print("Describe the issue (e.g., broken AC): ");
                String desc = scanner.nextLine();
                issuesDatabase.add(new IssueReport(f, desc));
                f.incrementIssueCount();
                
                // Analytics Alert Logic
                if(f.getIssueCount() >= 3) {
                    System.out.println("SYSTEM ALERT: This facility has multiple issues reported. Admin has been notified. [cite: 24]");
                }
                
                System.out.println("Issue reported successfully.");
                return;
            }
        }
        System.out.println("Facility not found.");
    }

    // --- ADMIN FUNCTIONS ---
    private static void processBookings() {
        System.out.println("\n--- Pending Bookings ---");
        boolean hasPending = false;
        for (Booking b : bookingsDatabase) {
            if (b.getStatus() == BookingStatus.PENDING) {
                System.out.println(b.toString());
                hasPending = true;
            }
        }

        if (!hasPending) {
            System.out.println("No pending bookings.");
            return;
        }

        System.out.print("Enter Booking ID to process: ");
        String bId = scanner.nextLine();
        for (Booking b : bookingsDatabase) {
            if (b.getBookingId().equals(bId)) {
                System.out.print("Approve (A) or Reject (R)? ");
                String action = scanner.nextLine();
                if (action.equalsIgnoreCase("A")) {
                    b.setStatus(BookingStatus.APPROVED);
                    System.out.println("Booking approved.");
                } else if (action.equalsIgnoreCase("R")) {
                    b.setStatus(BookingStatus.REJECTED);
                    System.out.println("Booking rejected.");
                }
                return;
            }
        }
    }

    private static void manageMaintenance() {
        System.out.println("\n--- Active Issue Reports ---");
        for (IssueReport r : issuesDatabase) {
            System.out.println(r.toString());
        }
        
        System.out.print("Enter Facility ID to lock for maintenance: ");
        String facId = scanner.nextLine();
        for (Facility f : facilitiesDatabase) {
            if (f.getId().equalsIgnoreCase(facId)) {
                f.setAvailable(false);
                System.out.println(f.getId() + " is now locked for maintenance and cannot be booked.");
                return;
            }
        }
    }

    private static void generateAnalytics() {
        System.out.println("\n--- System Analytics Report ---");
        System.out.println("Total Registered Users: " + usersDatabase.size());
        System.out.println("Total Bookings Processed: " + bookingsDatabase.size());
        System.out.println("Total Issues Reported: " + issuesDatabase.size());
        
        System.out.println("\nFacility Damage Report:");
        for(Facility f : facilitiesDatabase) {
            System.out.println(f.getId() + " - Total Maintenance Issues: " + f.getIssueCount());
        }
        System.out.println("-------------------------------");
    }

    private static void initializeData() {
        usersDatabase.add(new Admin("admin", "admin123", "System Admin"));
        usersDatabase.add(new StandardUser("1001", "pass", "John Doe", Role.STUDENT, "FCI"));
        
        facilitiesDatabase.add(new Facility("KB207", "Lecture Hall"));
        facilitiesDatabase.add(new Facility("MPH", "Multipurpose Hall"));
        facilitiesDatabase.add(new Facility("CL1", "Computer Lab"));
    }
}