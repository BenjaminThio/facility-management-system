package src.utils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.TypeReference;

public class Database {
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
    private static final String USERS_FILE = "users.json";
    private static final String FACILITIES_FILE = "facilities.json";
    private static Map<String, src.models.User> users = null;
    private static List<src.models.Facility> facilities = null;
    private static Map<String, List<src.models.Report>> reports = null;
    // private static final String BOOKINGS_FILE = "bookings.txt";
    private static final String REPORTS_FILE = "reports.json";

    public static void init()
    {
        Database.User.set(Database.User.loadAll());
        Database.Facility.set(Database.Facility.loadAll());
        Database.Report.set(Database.Report.loadAll());
    }

    public static class User
    {
        public static Map<String, src.models.User> loadAll()
        {
            Map<String, src.models.User> users = new HashMap<>();

            try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(String.format("%s%s", BASE_PATH, USERS_FILE))))
            {
                users = JSON.parseObject(bis, new TypeReference<Map<String, src.models.User>>(){}.getType());
            }
            catch (IOException e)
            {
                System.err.println(e.getMessage());
            }

            return users;
        }

        public static void save(Map<String, src.models.User> users)
        {
            try (BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(String.format("%s%s", BASE_PATH, USERS_FILE))))
            {
                JSON.writeTo(bos, users, JSONWriter.Feature.PrettyFormat);
            }
            catch (IOException e)
            {
                System.out.println(e.getMessage());
            }
        }

        public static void save()
        {
            save(users);
        }

        public static void init()
        {
            Map<String, src.models.User> users = new HashMap<>();

            for (src.models.User user : new src.models.User[] {
                new src.models.User("Benjamin Thio Zi liang", "benjaminthio@utar.edu.my", "@BenjaminThio70", src.models.User.Role.STUDENT),
                new src.models.User("Tee Hue Leng", "teehueleng123@utar.edu.my", "@TeeHueLeng70", src.models.User.Role.STUDENT),
            })
            {
                users.put(user.getEmail(), user);
            }

            save(users);
        }

        public static void set(Map<String, src.models.User> data)
        {
            users = data;
        }

        public static void add(src.models.User user)
        {
            users.put(user.getEmail(), user);
        }

        public static Map<String, src.models.User> getAll()
        {
            return users;
        }

        public static src.models.User get(String email)
        {
            if (email == null)
                return null;

            return users.get(email);
        }

        public static boolean isExists(String email)
        {
            return users.containsKey(email);
        }
    }

    public static class Facility
    {
        public static List<src.models.Facility> loadAll()
        {
            List<src.models.Facility> facilities = new ArrayList<>();
    
            try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(String.format("%s%s", BASE_PATH, FACILITIES_FILE))))
            {
                facilities = JSON.parseObject(bis, new TypeReference<List<src.models.Facility>>(){}.getType());
            }
            catch (IOException e)
            {
                System.err.println(e.getMessage());
            }

            return facilities;
        }

        public static List<src.models.Facility> getAll()
        {
            return facilities;
        }

        public static void set(List<src.models.Facility> data)
        {
            facilities = data;
        }

        public static void init()
        {
            List<src.models.Facility> facilities = new ArrayList<>();

            for (int floor = 1; floor <= 10; floor++)
            {
                for (int room = 0; room <= 30; room++)
                    facilities.add(new src.models.Facility(String.format("%s%d%s", "KB", floor, new DecimalFormat("00").format(room)), true));
            }

            save(facilities);
        }

        public static void save(List<src.models.Facility> facilities)
        {
            try (BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(String.format("%s%s", BASE_PATH, FACILITIES_FILE))))
            {
                JSON.writeTo(bos, facilities, JSONWriter.Feature.PrettyFormat);
            }
            catch (IOException e)
            {
                System.out.println(e.getMessage());
            }
        }

        public static void save()
        {
            save(facilities);
        }
    }

    public static class Report
    {
        public static Map<String, List<src.models.Report>> loadAll()
        {
            Map<String, List<src.models.Report>> reports = new HashMap<>();

            try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(String.format("%s%s", BASE_PATH, REPORTS_FILE))))
            {
                reports = JSON.parseObject(bis, new TypeReference<Map<String, List<src.models.Report>>>(){}.getType());
            }
            catch (IOException e)
            {
                System.err.println(e.getMessage());
            }

            return reports;
        }

        public static Map<String, List<src.models.Report>> getAll()
        {
            return reports;
        }

        public static List<src.models.Report> get(String name)
        {
            return reports.get(name);
        }

        public static void set(Map<String, List<src.models.Report>> data)
        {
            reports = data;
        }

        public static void save(Map<String, List<src.models.Report>> reports)
        {
            try (BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(String.format("%s%s", BASE_PATH, REPORTS_FILE))))
            {
                JSON.writeTo(bos, reports, JSONWriter.Feature.PrettyFormat);
            }
            catch (IOException e)
            {
                System.err.println(e.getMessage());
            }
        }

        public static void save()
        {
            save(reports);
        }

        public static void init()
        {
            Map<String, List<src.models.Report>> reports = new HashMap<>();

            for (int i = 10; i <= 20; i++)
            {
                List<src.models.Report> issues = new ArrayList<>();

                for (int j = 0; j < i - 9; j++)
                {
                    issues.add(new src.models.Report(String.format("Report %d", i - 9), "Benjamin wanna say hello to mum!", src.models.Report.Severity.LOW));
                }
                reports.put(String.format("KB%d", i), issues);
            }

            save(reports);
        }
    }

    public static void main(String[] args) {
        User.init();
        Facility.init();
        Report.init();

        for (Map.Entry<String, src.models.User> user : User.loadAll().entrySet())
        {
            String key = user.getKey();
            src.models.User val = user.getValue();

            System.out.println(key + "," + val.getRole());
        }
    }
}
