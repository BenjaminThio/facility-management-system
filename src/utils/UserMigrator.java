package src.utils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Random;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;

import src.models.User;

public class UserMigrator
{
    private static final Random random = new Random();

    public static void migrateFromCppJson(String oldJsonFilePath)
    {
        try {
            String jsonContent = new String(Files.readAllBytes(Paths.get(oldJsonFilePath)));
            JSONObject rootObject = JSON.parseObject(jsonContent);
            int count = 0;

            if (Database.User.getAll() == null) {
                Database.User.set(new java.util.LinkedHashMap<>());
            }

            System.out.println("Starting User Migration using FastJSON...");

            for (String key : rootObject.keySet()) {
                JSONObject userBlock = rootObject.getJSONObject(key);
                JSONObject infoBlock = userBlock.getJSONObject("info");

                if (infoBlock != null) {
                    // Grab the raw email from the old C++ database
                    String rawEmail = infoBlock.getString("email");
                    String password = infoBlock.getString("password");
                    String name = infoBlock.getString("username");
                    
                    // Strip the old domain and force the UTAR domain
                    String usernamePrefix = rawEmail.split("@")[0];
                    String formattedEmail = usernamePrefix + "@utar.edu.my";

                    // --- RANDOMIZERS ---
                    String phoneNumber = generateRandomPhoneNumber();
                    
                    // THE FIX: Fully random role selection!
                    User.Role role = generateRandomRole(); 
                    
                    Database.Faculty[] allFaculties = Database.Faculty.values();
                    Database.Faculty faculty = allFaculties[random.nextInt(allFaculties.length)];
                    
                    // Save the user using the newly FORMATTED email and RANDOMIZED role
                    User newUser = new User(name, formattedEmail, password, phoneNumber, role, faculty);
                    Database.User.getAll().put(formattedEmail, newUser);
                    count++;
                }
            }

            Database.User.save();
            System.out.println("Migration Complete! Successfully ported " + count + " users with randomized roles.");
        } catch (IOException e) {
            System.err.println("Error reading the old JSON file: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("FastJSON parsing error: " + e.getMessage());
        }
    }

    private static String generateRandomPhoneNumber()
    {
        int prefix = random.nextInt(10); 
        int suffix = 1000000 + random.nextInt(9000000); 
        return "01" + prefix + "-" + suffix;
    }

    // --- THE NEW ROLE RANDOMIZER ---
    private static User.Role generateRandomRole()
    {
        User.Role[] allRoles = User.Role.values();
        return allRoles[random.nextInt(allRoles.length)];
    }
    // -------------------------------

    public static void main(String[] args) {
        migrateFromCppJson("database/user.json");
    }
}