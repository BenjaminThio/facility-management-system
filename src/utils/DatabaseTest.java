/*
package src.utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import src.models.Facility;
import src.models.User;

public class DatabaseTest {
    public enum Faculty
    {
        MEDICINE_AND_HEALTH_SCIENCES,
        ENGINEERING_AND_SCIENCE,
        ENGINEERING_AND_GREEN_TECHNOLOGY,
        INFORMATION_AND_COMMUNICATION_TECHNOLOGY,
        SCIENCE,
        ACCOUNTANCY_AND_MANAGEMENT,
        BUSSINESS_AND_FINANCE,
        ARTS_AND_SOCIAL_SCIENCE,
        CREATIVE_INDUSTRIES,
        CHINESE_STUDIES,
        EDUCATION
    }
    private static final String BASE_PATH = "database/";
    private static final String USERS_FILE = "users.txt";
    private static final String FACILITIES_FILE = "facilities.txt";
    // private static final String BOOKINGS_FILE = "bookings.txt";
    // private static final String ISSUES_FILE = "issues.txt";

    public static void main(String[] args) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(BASE_PATH + FACILITIES_FILE)))
        {
            for (int floor = 1; floor <= 10; floor++)
            {
                for (int room = 0; room <= 30; room++)
                    bw.write("KB" + Integer.toString(floor) + new DecimalFormat("00").format(room) + ",true\n");
            }
        }
        catch (IOException e)
        {
            System.out.println(e.getMessage());
        }
    }

    public static List<User> loadUsers()
    {
        List<User> users = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(BASE_PATH + USERS_FILE)))
        {
            String line;

            while ((line = br.readLine()) != null)
            {
                String[] data = line.split(",");
                String name = data[0];
                String email = data[1];
                String password = data[2];
                User.Role role = User.Role.cast(Integer.parseInt(data[3]));

                Global.users.add(new User(name, email, password, role));
            }
        }
        catch (IOException e)
        {
            System.err.println(e.getMessage());
        }

        return users;
    }

    public static boolean isUserExists(String name)
    {
        for (User user : Global.users)
        {
            if (user.getName() == name)
            {
                return true;
            }
        }
        return false;
    }

    public static User getUser(String email)
    {
        for (User user : Global.users)
        {
            if (user.getEmail() == email)
            {
                return user;
            }
        }
        return null;
    }

    public static void saveUsers(List<User> users)
    {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(BASE_PATH + USERS_FILE)))
        {
            for (User user : users)
            {
                StringBuilder lineBuilder = new StringBuilder();

                lineBuilder.append(user.getName()).append(',').append(user.getEmail()).append(',').append(user.getPassword()).append(',').append(user.getRole().getVal());

                bw.write(lineBuilder.toString());
                bw.newLine();
            }
        }
        catch (IOException e)
        {
            System.err.println(e.getMessage());
        }
    }

    public static void addNewUser(User user)
    {
        Global.users.add(user);
        saveUsers(Global.users);
    }

    public static List<Facility> loadFacilities()
    {
        List<Facility> facilities = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(BASE_PATH + FACILITIES_FILE)))
        {
            String line;

            while ((line = br.readLine()) != null)
            {
                String[] data = line.split(",");
                String name = data[0];
                boolean isAvailable = Boolean.parseBoolean(data[1]);

                facilities.add(new Facility(name, isAvailable));
            }
        }
        catch (IOException e)
        {
            System.err.println(e.getMessage());
        }

        return facilities;
    }
}
*/