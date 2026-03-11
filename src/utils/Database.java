package src.utils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.TypeReference;

import src.models.BookingInfo;

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
    private static final String BOOKINGS_FILE = "bookings.json";
    private static final String REPORTS_FILE = "reports.json";
    private static LinkedHashMap<String, src.models.User> users = null;
    private static List<src.models.Facility> facilities = null;
    private static LinkedHashMap<String, List<src.models.Report>> reports = null;
    private static LinkedHashMap<String, LinkedHashMap<String, LinkedHashMap<String, src.models.Booking>>> bookings = null;

    public static void init()
    {
        Database.User.set(Database.User.loadAll());
        Database.Facility.set(Database.Facility.loadAll());
        Database.Report.set(Database.Report.loadAll());
        Database.Booking.set(Database.Booking.loadAll());
    }

    public static class User
    {
        public static LinkedHashMap<String, src.models.User> loadAll()
        {
            LinkedHashMap<String, src.models.User> users = new LinkedHashMap<>();

            try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(String.format("%s%s", BASE_PATH, USERS_FILE))))
            {
                users = JSON.parseObject(bis, new TypeReference<LinkedHashMap<String, src.models.User>>(){}.getType());
            }
            catch (IOException e)
            {
                System.err.println(e.getMessage());
            }

            return users;
        }

        public static void save(LinkedHashMap<String, src.models.User> users)
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
            LinkedHashMap<String, src.models.User> users = new LinkedHashMap<>();

            for (src.models.User user : new src.models.User[] {
                new src.models.User("Benjamin Thio Zi liang", "benjaminthio@utar.edu.my", "@BenjaminThio70", src.models.User.Role.STUDENT),
                new src.models.User("Tee Hue Leng", "teehueleng123@utar.edu.my", "@TeeHueLeng70", src.models.User.Role.STUDENT),
            })
            {
                users.put(user.getEmail(), user);
            }

            save(users);
        }

        public static void set(LinkedHashMap<String, src.models.User> data)
        {
            users = data;
        }

        public static void add(src.models.User user)
        {
            users.put(user.getEmail(), user);
        }

        public static LinkedHashMap<String, src.models.User> getAll()
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

        public static src.models.Facility get(int i)
        {
            return facilities.get(i);
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
                    facilities.add(new src.models.Facility(String.format("KB%d%s", floor, new DecimalFormat("00").format(room)), true));
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
        public static LinkedHashMap<String, List<src.models.Report>> loadAll()
        {
            LinkedHashMap<String, List<src.models.Report>> reports = new LinkedHashMap<>();

            try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(String.format("%s%s", BASE_PATH, REPORTS_FILE))))
            {
                reports = JSON.parseObject(bis, new TypeReference<LinkedHashMap<String, List<src.models.Report>>>(){}.getType());
            }
            catch (IOException e)
            {
                System.err.println(e.getMessage());
            }

            return reports;
        }

        public static LinkedHashMap<String, List<src.models.Report>> getAll()
        {
            return reports;
        }

        public static List<src.models.Report> get(String name)
        {
            return reports.get(name);
        }

        public static void set(LinkedHashMap<String, List<src.models.Report>> data)
        {
            reports = data;
        }

        public static void add(String name, src.models.Report report)
        {
            if (reports.get(name) != null)
                reports.get(name).add(report);
            else
            {
                reports.put(name, new ArrayList<>(Arrays.asList(report)));
            }
        }

        public static void save(LinkedHashMap<String, List<src.models.Report>> reports)
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
            LinkedHashMap<String, List<src.models.Report>> reports = new LinkedHashMap<>();

            for (int i = 10; i <= 20; i++)
            {
                List<src.models.Report> issues = new ArrayList<>();

                for (int j = 0; j < i - 9; j++)
                {
                    issues.add(new src.models.Report(String.format("Report %d", i - 9), "Benjamin wanna say hello to mum!", src.models.Report.Severity.LOW, new Image("C:\\Users\\User\\Pictures\\Latest Minecraft Server Screenshots\\photo_2024-11-01_00-19-24.jpg").getBase64()));
                }
                reports.put(String.format("KB1%d", i), issues);
            }

            save(reports);
        }
    }

    public static class Booking
    {
        public static class Session
        {
            public enum Status
            {
                PENDING,
                APPROVED,
                NOT_FOUND
            }

            public static LinkedHashMap<String, src.models.Booking> getAll(String facilityName, LocalDate date)
            {
                return getAll(facilityName, date.toString());
            }

            public static LinkedHashMap<String, src.models.Booking> getAll(String facilityName, String date)
            {
                if (bookings.get(facilityName) != null && bookings.get(facilityName).get(date) != null)
                {
                    return bookings.get(facilityName).get(date);
                }
                return null;
            }

            public static int length(String facilityName, LocalDate date)
            {
                return length(facilityName, date.toString());
            }

            public static int length(String facilityName, String date)
            {
                if (bookings.get(facilityName) != null && bookings.get(facilityName).get(date) != null)
                {
                    return bookings.get(facilityName).get(date).size();
                }
                return 0;
            }

            public static void remove(String facilityName, LocalDate date)
            {
                remove(facilityName, date.toString());
            }

            public static void remove(String facilityName, String date)
            {
                if (bookings.get(facilityName) != null && bookings.get(facilityName).get(date) != null)
                {
                    bookings.get(facilityName).remove(date);
                }
            }

            public static void addPending(String facilityName, LocalDate date, String session, String email, String remark)
            {
                addPending(facilityName, date.toString(), session, email, remark);
            }

            public static void addPending(String facilityName, String date, String session, String email, String remark)
            {
                if (bookings.get(facilityName) != null &&
                    bookings.get(facilityName).get(date) != null &&
                    bookings.get(facilityName).get(date).get(session) != null)
                {
                    bookings.get(facilityName).get(date).get(session).addPending(new BookingInfo(email, remark));
                }
            }

            public static Status getStatus(String facilityName, String date, String session, String email)
            {
                if (bookings.get(facilityName) != null &&
                    bookings.get(facilityName).get(date) != null &&
                    bookings.get(facilityName).get(date).get(session) != null)
                {
                    if (bookings.get(facilityName).get(date).get(session).getApproved() == email)
                    {
                        return Status.APPROVED;
                    }
                    else if (bookings.get(facilityName).get(date).get(session).pendingContains(email))
                    {
                        return Status.PENDING;
                    }
                }
                return Status.NOT_FOUND;
            }
        }

        public static LinkedHashMap<String, LinkedHashMap<String, LinkedHashMap<String, src.models.Booking>>> loadAll()
        {
            LinkedHashMap<String, LinkedHashMap<String, LinkedHashMap<String, src.models.Booking>>> bookings = new LinkedHashMap<>();

            try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(BASE_PATH + BOOKINGS_FILE)))
            {
                // Facility Name -> Date Schedule -> Time Sessions -> Status -> User Emails
                bookings = JSON.parseObject(bis, new TypeReference<LinkedHashMap<String, LinkedHashMap<String, LinkedHashMap<String, src.models.Booking>>>>(){}.getType());
            }
            catch (IOException e)
            {
                System.out.println(e.getMessage());
            }

            return bookings;
        }

        public static void save(LinkedHashMap<String, LinkedHashMap<String, LinkedHashMap<String, src.models.Booking>>> bookings)
        {
            try (BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(BASE_PATH + BOOKINGS_FILE)))
            {
                JSON.writeTo(bos, bookings, JSONWriter.Feature.PrettyFormat, JSONWriter.Feature.WriteNulls);
            }
            catch (IOException e)
            {
                System.out.println(e.getMessage());
            }
        }

        public static void save()
        {
            save(bookings);
        }

        public static void init()
        {
            LinkedHashMap<String, LinkedHashMap<String, LinkedHashMap<String, src.models.Booking>>> bookings = new LinkedHashMap<>();

            for (int floor = 3; floor <= 10; floor++)
            {
                for (int room = 10; room <= 30; room++)
                {
                    LinkedHashMap<String, LinkedHashMap<String, src.models.Booking>> schedule = new LinkedHashMap<>();

                    for (int day = 1; day <= 9; day++)
                    {
                        LinkedHashMap<String, src.models.Booking> sessions = new LinkedHashMap<>();

                        for (int hour = 12; hour <= 16; hour++)
                        {
                            sessions.put(String.format("%d:00-%d:00", hour, hour + 1), new src.models.Booking(
                                new ArrayList<>(Arrays.asList(new BookingInfo("benjaminthio@utar.edu.my", "Idk what to remark"), new BookingInfo("teehueleng123@utar.edu.my"))),
                                null
                            ));
                        }

                        schedule.put(String.format("2026-03-0%d", day), sessions);
                    }

                    bookings.put(String.format("KB%d%d", floor, room), schedule);
                }
            }
            save(bookings);
        }

        public static void set(LinkedHashMap<String, LinkedHashMap<String, LinkedHashMap<String, src.models.Booking>>> data)
        {
            bookings = data;
        }

        public static LinkedHashMap<String, LinkedHashMap<String, LinkedHashMap<String, src.models.Booking>>> getAll()
        {
            return bookings;
        }
    }

    public static void main(String[] args) {
        // User.init();
        // Facility.init();
        // Report.init();
        Booking.init();

        for (Map.Entry<String, src.models.User> user : User.loadAll().entrySet())
        {
            String key = user.getKey();
            src.models.User val = user.getValue();

            System.out.println(key + "," + val.getRole());
        }
    }
}
