package test;

import java.io.*;
import java.util.*;

enum BookingStatus { PENDING, APPROVED, REJECTED, CANCELLED }
enum MaintenanceStatus { REPORTED, IN_PROGRESS, RESOLVED }
enum Role { STUDENT, STAFF, ADMIN }

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
    public String getPassword() { return password; } // Added missing getter
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
    
    public String getFaculty() { return faculty; } // Added missing getter
}

class Admin extends User {
    public Admin(String userId, String password, String name) {
        super(userId, password, name, Role.ADMIN);
    }
}

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
    public String getDate() { return date; } // Added missing getter
    public String getTimeSlot() { return timeSlot; } // Added missing getter

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

    public String getReportId() { return reportId; }
    public Facility getFacility() { return facility; }
    public String getDescription() { return description; }
    public MaintenanceStatus getStatus() { return status; }
    public void setStatus(MaintenanceStatus status) { this.status = status; }

    @Override
    public String toString() {
        return "Report ID: " + reportId + " | Facility: " + facility.getId() + " | Issue: " + description + " | Status: " + status;
    }
}

// =========================================================================
// 3. DATABASE MANAGER (File I/O)
// =========================================================================
class DatabaseManager {
    private static final String USERS_FILE = "users.txt";
    private static final String FACILITIES_FILE = "facilities.txt";
    private static final String BOOKINGS_FILE = "bookings.txt";
    private static final String ISSUES_FILE = "issues.txt";

    public static void saveAllData(List<User> users, List<Facility> facilities, List<Booking> bookings, List<IssueReport> issues) {
        System.out.println("Saving database to .txt files...");
        saveUsers(users);
        saveFacilities(facilities);
        saveBookings(bookings);
        saveIssues(issues);
    }

    private static void saveUsers(List<User> users) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(USERS_FILE))) {
            for (User u : users) {
                if (u.getRole() == Role.ADMIN) {
                    bw.write("ADMIN," + u.getUserId() + "," + u.getPassword() + "," + u.getName() + ",NONE");
                } else {
                    StandardUser su = (StandardUser) u;
                    bw.write(su.getRole() + "," + su.getUserId() + "," + su.getPassword() + "," + su.getName() + "," + su.getFaculty());
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
                bw.write(b.getBookingId() + "," + b.getBookedBy().getUserId() + "," + b.getFacility().getId() + "," + 
                         b.getDate() + "," + b.getTimeSlot() + "," + b.getStatus());
                bw.newLine();
            }
        } catch (IOException e) {
            System.err.println("Error saving bookings: " + e.getMessage());
        }
    }

    private static void saveIssues(List<IssueReport> issues) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(ISSUES_FILE))) {
            for (IssueReport r : issues) {
                bw.write(r.getReportId() + "," + r.getFacility().getId() + "," + r.getDescription() + "," + r.getStatus());
                bw.newLine();
            }
        } catch (IOException e) {
            System.err.println("Error saving issues: " + e.getMessage());
        }
    }

    public static void loadAllData(List<User> users, List<Facility> facilities, List<Booking> bookings, List<IssueReport> issues) {
        System.out.println("Loading database from .txt files...");
        loadUsers(users);
        loadFacilities(facilities);
        loadBookings(users, facilities, bookings);
        loadIssues(facilities, issues);
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
                    Role role = Role.valueOf(roleStr); 
                    users.add(new StandardUser(id, pass, name, role, faculty));
                }
            }
        } catch (IOException e) { System.err.println("Error loading users: " + e.getMessage()); }
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
                // Loop to restore issue count
                int iCount = Integer.parseInt(data[3]);
                for(int i=0; i < iCount; i++) { f.incrementIssueCount(); }
                facilities.add(f); 
            }
        } catch (IOException e) { System.err.println("Error loading facilities: " + e.getMessage()); }
    }

    private static void loadBookings(List<User> users, List<Facility> facilities, List<Booking> bookings) {
        File file = new File(BOOKINGS_FILE);
        if (!file.exists()) return;
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] data = line.split(",");
                // String bookingId = data[0];
                String userId = data[1];
                String facilityId = data[2];
                String date = data[3];
                String timeSlot = data[4];
                BookingStatus status = BookingStatus.valueOf(data[5]);

                User bookedBy = null;
                for (User u : users) if (u.getUserId().equals(userId)) bookedBy = u;

                Facility targetFacility = null;
                for (Facility f : facilities) if (f.getId().equals(facilityId)) targetFacility = f;

                if (bookedBy != null && targetFacility != null) {
                    Booking b = new Booking(bookedBy, targetFacility, date, timeSlot);
                    b.setStatus(status); 
                    bookings.add(b);
                }
            }
        } catch (IOException | IllegalArgumentException e) { System.err.println("Error loading bookings: " + e.getMessage()); }
    }

    private static void loadIssues(List<Facility> facilities, List<IssueReport> issues) {
        File file = new File(ISSUES_FILE);
        if (!file.exists()) return;
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] data = line.split(",");
                // String reportId = data[0];
                String facilityId = data[1];
                String desc = data[2];
                MaintenanceStatus status = MaintenanceStatus.valueOf(data[3]);

                Facility targetFacility = null;
                for (Facility f : facilities) if (f.getId().equals(facilityId)) targetFacility = f;

                if (targetFacility != null) {
                    IssueReport r = new IssueReport(targetFacility, desc);
                    r.setStatus(status);
                    issues.add(r);
                }
            }
        } catch (IOException | IllegalArgumentException e) { System.err.println("Error loading issues: " + e.getMessage()); }
    }
}

// =========================================================================
// 4. MAIN APPLICATION ENGINE (The Entry Point)
// =========================================================================
public class Main {
    
    static List<User> usersDatabase = new ArrayList<>();
    static List<Facility> facilitiesDatabase = new ArrayList<>();
    static List<Booking> bookingsDatabase = new ArrayList<>();
    static List<IssueReport> issuesDatabase = new ArrayList<>();
    
    static Scanner scanner = new Scanner(System.in);
    static User loggedInUser = null;

    public static void main(String[] args) {
        DatabaseManager.loadAllData(usersDatabase, facilitiesDatabase, bookingsDatabase, issuesDatabase);
        
        if (usersDatabase.isEmpty()) {
            usersDatabase.add(new Admin("admin", "admin123", "System Admin"));
            facilitiesDatabase.add(new Facility("KB207", "Lecture Hall"));
            facilitiesDatabase.add(new Facility("MPH", "Multipurpose Hall"));
            facilitiesDatabase.add(new Facility("CL1", "Computer Lab"));
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

    // --- STRICT INPUT HANDLING HELPER ---
    private static int getValidIntInput() {
        while (true) {
            try {
                return Integer.parseInt(scanner.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.print("Invalid input! Please enter a valid number: ");
            }
        }
    }

    // --- MENUS ---
    private static void displayGuestMenu() {
        System.out.println("\n--- MAIN MENU ---");
        System.out.println("1. Login");
        System.out.println("2. Register New User");
        System.out.println("3. Exit");
        System.out.print("Select (1-3): ");

        int choice = getValidIntInput();
        switch (choice) {
            case 1: handleLogin(); break;
            case 2: handleRegistration(); break;
            case 3: 
                DatabaseManager.saveAllData(usersDatabase, facilitiesDatabase, bookingsDatabase, issuesDatabase);
                System.out.println("Data saved. Exiting system...");
                System.exit(0); 
                break;
            default: System.out.println("Invalid choice. Please select 1, 2, or 3.");
        }
    }

    private static void displayStudentMenu() {
        System.out.println("\n--- Student/Staff Menu (" + loggedInUser.getName() + ") ---");
        System.out.println("1. View Facilities");
        System.out.println("2. Book a Facility");
        System.out.println("3. My Bookings (View/Cancel)");
        System.out.println("4. Report a Facility Issue");
        System.out.println("9. Logout");
        System.out.print("Select: ");

        int choice = getValidIntInput();
        switch (choice) {
            case 1: viewFacilities(); break;
            case 2: bookFacility(); break;
            case 3: manageMyBookings(); break;
            case 4: reportIssue(); break;
            case 9: loggedInUser = null; break;
            default: System.out.println("Invalid choice.");
        }
    }

    private static void displayAdminMenu() {
        System.out.println("\n--- Admin Menu (" + loggedInUser.getName() + ") ---");
        System.out.println("1. Process Booking Requests");
        System.out.println("2. Manage Facility Maintenance");
        System.out.println("3. Generate Analytics Report");
        System.out.println("9. Logout");
        System.out.print("Select: ");

        int choice = getValidIntInput();
        switch (choice) {
            case 1: processBookings(); break;
            case 2: manageMaintenance(); break;
            case 3: generateAnalytics(); break;
            case 9: loggedInUser = null; break;
            default: System.out.println("Invalid choice.");
        }
    }

    // --- CORE FUNCTIONS ---
    private static void handleLogin() {
        System.out.print("Enter User ID: ");
        String id = scanner.nextLine().trim();
        System.out.print("Enter Password: ");
        String pass = scanner.nextLine().trim();

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
        String id = scanner.nextLine().trim();
        
        // Check if ID already exists
        for (User u : usersDatabase) {
            if(u.getUserId().equals(id)) {
                System.out.println("Error: User ID already exists!");
                return;
            }
        }

        System.out.print("Enter Password: ");
        String pass = scanner.nextLine().trim();
        System.out.print("Enter Full Name: ");
        String name = scanner.nextLine().trim();
        
        System.out.println("Select Account Type:");
        System.out.println("1. Student");
        System.out.println("2. Staff");
        System.out.println("3. Admin");
        System.out.print("Select (1-3): ");
        int roleChoice = getValidIntInput();
        
        if (roleChoice == 3) {
            usersDatabase.add(new Admin(id, pass, name));
            System.out.println("Admin registration successful! You can now log in.");
        } else if (roleChoice == 1 || roleChoice == 2) {
            System.out.print("Enter Faculty/Department: ");
            String faculty = scanner.nextLine().trim();
            Role role = (roleChoice == 2) ? Role.STAFF : Role.STUDENT;
            usersDatabase.add(new StandardUser(id, pass, name, role, faculty));
            System.out.println(role + " registration successful! You can now log in.");
        } else {
            System.out.println("Registration failed. Invalid role selected.");
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
        System.out.print("\nEnter Facility ID to book: ");
        String facId = scanner.nextLine().trim();
        
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
        String date = scanner.nextLine().trim();

        String[] allSlots = {
            "08:00 AM - 10:00 AM",
            "10:00 AM - 12:00 PM",
            "12:00 PM - 02:00 PM",
            "02:00 PM - 04:00 PM",
            "04:00 PM - 06:00 PM"
        };

        System.out.println("\n--- Available Time Slots for " + target.getId() + " on " + date + " ---");
        List<String> availableSlots = new ArrayList<>();
        
        for (int i = 0; i < allSlots.length; i++) {
            String currentSlot = allSlots[i];
            boolean isBooked = false;
            
            for (Booking b : bookingsDatabase) {
                if (b.getFacility().getId().equalsIgnoreCase(target.getId()) &&
                    b.getDate().equals(date) &&
                    b.getTimeSlot().equals(currentSlot) &&
                    (b.getStatus() == BookingStatus.PENDING || b.getStatus() == BookingStatus.APPROVED)) {
                    isBooked = true;
                    break;
                }
            }
            
            int displayIndex = i + 1;
            if (isBooked) {
                System.out.println("[ UNAVAILABLE ] " + currentSlot);
            } else {
                System.out.println("[" + displayIndex + "] " + currentSlot);
                availableSlots.add(currentSlot);
            }
        }

        if (availableSlots.isEmpty()) {
            System.out.println("\nSorry, this facility is fully booked on this date.");
            return;
        }

        System.out.print("\nSelect an available slot number (or press 0 to cancel): ");
        int slotChoice = getValidIntInput();
        
        if (slotChoice == 0) {
            System.out.println("Booking cancelled.");
            return;
        }
        
        if (slotChoice > 0 && slotChoice <= allSlots.length) {
            String selectedSlot = allSlots[slotChoice - 1];
            if (!availableSlots.contains(selectedSlot)) {
                System.out.println("Error: That slot is already taken. Please try again.");
                return;
            }
            
            bookingsDatabase.add(new Booking(loggedInUser, target, date, selectedSlot));
            System.out.println("Success! Booking request submitted for " + selectedSlot + ". Awaiting Admin approval.");
        } else {
            System.out.println("Invalid selection.");
        }
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

        System.out.print("\nEnter Booking ID to cancel (or press Enter to go back): ");
        String cancelId = scanner.nextLine().trim();
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
        String facId = scanner.nextLine().trim();
        
        for (Facility f : facilitiesDatabase) {
            if (f.getId().equalsIgnoreCase(facId)) {
                System.out.print("Describe the issue (e.g., broken AC): ");
                String desc = scanner.nextLine().trim();
                issuesDatabase.add(new IssueReport(f, desc));
                f.incrementIssueCount();
                
                if(f.getIssueCount() >= 3) {
                    System.out.println("SYSTEM ALERT: This facility has multiple issues reported. Admin has been notified.");
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
        String bId = scanner.nextLine().trim();
        for (Booking b : bookingsDatabase) {
            if (b.getBookingId().equals(bId) && b.getStatus() == BookingStatus.PENDING) {
                System.out.print("Approve (A) or Reject (R)? ");
                String action = scanner.nextLine().trim();
                if (action.equalsIgnoreCase("A")) {
                    b.setStatus(BookingStatus.APPROVED);
                    System.out.println("Booking approved.");
                } else if (action.equalsIgnoreCase("R")) {
                    b.setStatus(BookingStatus.REJECTED);
                    System.out.println("Booking rejected.");
                } else {
                    System.out.println("Invalid action.");
                }
                return;
            }
        }
        System.out.println("Valid pending booking not found.");
    }

    private static void manageMaintenance() {
        System.out.println("\n--- Active Issue Reports ---");
        for (IssueReport r : issuesDatabase) {
            System.out.println(r.toString());
        }
        
        System.out.print("Enter Facility ID to lock for maintenance: ");
        String facId = scanner.nextLine().trim();
        for (Facility f : facilitiesDatabase) {
            if (f.getId().equalsIgnoreCase(facId)) {
                f.setAvailable(false);
                System.out.println(f.getId() + " is now locked for maintenance and cannot be booked.");
                return;
            }
        }
        System.out.println("Facility not found.");
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
}