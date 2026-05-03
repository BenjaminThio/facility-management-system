package src.utils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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
    public enum Faculty {
        ACCOUNTANCY_AND_MANAGEMENT("Faculty of Accountancy and Management"),
        CREATIVE_INDUSTRIES("Faculty of Creative Industries"),
        ENGINEERING_AND_SCIENCE("Lee Kong Chian Faculty of Engineering and Science"),
        MEDICINE_AND_HEALTH_SCIENCES("Faculty of Medicine and Health Sciences"),
        CHINESE_STUDIES("Institute of Chinese Studies"),
        FOUNDATION_STUDIES("Centre for Foundation Studies");

        private final String name;

        Faculty(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
        
        public static Faculty cast(int val) {
            if (val >= 0 && val < values().length) {
                return values()[val];
            }
            return null;
        }
    }
    /*
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
    */
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
        try {
            java.nio.file.Files.createDirectories(java.nio.file.Paths.get(BASE_PATH));
        } catch (java.io.IOException e) {
            System.err.println(e.getMessage());
        }

        Database.User.set(Database.User.loadAll());
        Database.Facility.set(Database.Facility.loadAll());
        Database.Report.set(Database.Report.loadAll());
        Database.Booking.set(Database.Booking.loadAll());

        if (Database.User.getAll() == null) Database.User.set(new java.util.LinkedHashMap<>()); 
        if (Database.Facility.getAll() == null) Database.Facility.set(new java.util.ArrayList<>()); 
        if (Database.Report.getAll() == null) Database.Report.set(new java.util.LinkedHashMap<>()); 
        if (Database.Booking.getAll() == null) Database.Booking.set(new java.util.LinkedHashMap<>());

        // --- THE CRITICAL FIX: Initialize empty database if file was empty/null ---
        if (Database.Booking.getAll() == null) {
            Database.Booking.set(new java.util.LinkedHashMap<>()); 
        }
        // --------------------------------------------------------------------------

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

        for (String facilityName : Database.Booking.getAll().keySet()) {
            for (String dateString : Database.Booking.getAll().get(facilityName).keySet()) {
                for (String sessionString : Database.Booking.getAll().get(facilityName).get(dateString).keySet()) {
                    String[] sessionBounds = sessionString.split("-");
                    // LocalTime sessionStartTime = LocalTime.parse(sessionBounds[0]);
                    String sessionEndTime = sessionBounds[1];

                    LocalDateTime dateTime = LocalDateTime.parse(dateString + " " + sessionEndTime, formatter);
                    if (dateTime.isBefore(LocalDateTime.now())) {
                        Database.Booking.getAll().get(facilityName).get(dateString).get(sessionString).setIsExpired(true);
                    }
                }
            }
        }

        Database.Booking.save();
        Database.User.save();
        Database.Facility.save();
        Database.Report.save();
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
                new src.models.User("Benjamin Thio Zi liang", "benjaminthio@utar.edu.my", "@BenjaminThio70", "011-18985323", src.models.User.Role.STUDENT, Faculty.ENGINEERING_AND_SCIENCE),
                new src.models.User("Tee Hue Leng", "teehueleng123@utar.edu.my", "@TeeHueLeng70", "011-18985323", src.models.User.Role.STUDENT, Faculty.ENGINEERING_AND_SCIENCE)
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

        public static src.models.Facility get(String facilityName)
        {
            for (src.models.Facility facility : facilities)
            {
                if (facility.getName().equals(facilityName))
                {
                    return facility;
                }
            }
            return null;
        }

        public static src.models.Facility get(int i)
        {
            return facilities.get(i);
        }

        public static List<src.models.Facility> getAll()
        {
            return facilities;
        }

        public static List<src.models.Facility> getAvailabFacilities()
        {
            List<src.models.Facility> availableFacility = new ArrayList<>();

            for (src.models.Facility facility : facilities)
            {
                if (facility.isAvailable())
                {
                    availableFacility.add(facility);
                }
            }

            return availableFacility;
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
            if (reports == null) {
                reports = new LinkedHashMap<>();
            }

            if (reports.get(name) != null) {
                reports.get(name).add(report);
            } else {
                ArrayList<src.models.Report> newList = new ArrayList<>();
                newList.add(report);
                reports.put(name, newList);
            }
        }

        /*
        public static void add(String name, src.models.Report report)
        {
            if (reports.get(name) != null)
                reports.get(name).add(report);
            else
            {
                reports.put(name, new ArrayList<>(Arrays.asList(report)));
            }
        }
        */

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
                    issues.add(new src.models.Report(String.format("Report %d", i - 9), "Benjamin wanna say hello to mum!", src.models.Report.Severity.LOW, new Image("C:\\Users\\User\\Pictures\\Latest Minecraft Server Screenshots\\photo_2024-11-01_00-19-24.jpg").getBase64(), "benjaminthio@utar.edu.my"));
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
                UNDER_MAINTENANCE,
                EXPIRED,
                DISCARDED,
                NOT_FOUND
            }

            public static void clear(String facilityName, LocalDate date, String session)
            {
                bookings.get(facilityName).get(date.toString()).remove(session);
            }

            public static LinkedHashMap<String, src.models.Booking> getAll(String facilityName, LocalDate date)
            {
                return getAll(facilityName, date.toString());
            }

            public static LinkedHashMap<String, src.models.Booking> getAll(String facilityName, String date)
            {                
                if (bookings.get(facilityName) != null && bookings.get(facilityName).get(date) != null)
                {
                    LinkedHashMap<String, src.models.Booking> sessions = new LinkedHashMap<>();

                    for (String sessionString : bookings.get(facilityName).get(date).keySet())
                    {
                        if (!bookings.get(facilityName).get(date).get(sessionString).isExpired())
                        {
                            sessions.put(sessionString, bookings.get(facilityName).get(date).get(sessionString));
                        }
                    }
                    return sessions;
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
                    bookings.get(facilityName).get(date).get(session) != null &&
                    Database.Facility.get(facilityName) != null)
                {
                    src.models.Booking sessionData = bookings.get(facilityName).get(date).get(session);

                    if (sessionData.isExpired())
                    {
                        return Status.EXPIRED;
                    }
                    else if (sessionData.getApproved() != null)
                    {
                        if (sessionData.getApproved().equals(email)) {
                            if (Database.Facility.get(facilityName).isAvailable())
                                return Status.APPROVED;
                            else
                                return Status.UNDER_MAINTENANCE;
                        } else {
                            return Status.DISCARDED;
                        }
                    }
                    else if (sessionData.pendingContains(email))
                    {
                        return Status.PENDING;
                    }
                    else
                    {
                        return Status.DISCARDED;
                    }
                }
                else
                {
                    return Status.NOT_FOUND;
                }
            }

            /*
            public static Status getStatus(String facilityName, String date, String session, String email)
            {
                
                if (bookings.get(facilityName) != null &&
                    bookings.get(facilityName).get(date) != null &&
                    bookings.get(facilityName).get(date).get(session) != null &&
                    Database.Facility.get(facilityName) != null)
                {
                    if (bookings.get(facilityName).get(date).get(session).isExpired())
                    {
                        return Status.EXPIRED;
                    }
                    else if (bookings.get(facilityName).get(date).get(session).getApproved() != null &&
                        bookings.get(facilityName).get(date).get(session).getApproved().equals(email))
                    {
                        if (Database.Facility.get(facilityName).isAvailable())
                            return Status.APPROVED;
                        else
                            return Status.UNDER_MAINTENANCE;
                    }
                    else if (bookings.get(facilityName).get(date).get(session).pendingContains(email))
                        return Status.PENDING;
                    else
                        return Status.DISCARDED;
                }
                else
                    return Status.NOT_FOUND;
            }
            */
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
                                null,
                                false
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

        public static LinkedHashMap<String, LinkedHashMap<String, LinkedHashMap<String, src.models.Booking>>> getAll() {
            if (bookings == null)
            {
                bookings = new LinkedHashMap<>();
            }
            return bookings;
        }

        public static LinkedHashMap<String, LinkedHashMap<String, LinkedHashMap<String, src.models.Booking>>> getActiveBookings() {
            LinkedHashMap<String, LinkedHashMap<String, LinkedHashMap<String, src.models.Booking>>> activeData = new LinkedHashMap<>();

            for (Map.Entry<String, LinkedHashMap<String, LinkedHashMap<String, src.models.Booking>>> facilityEntry : bookings.entrySet()) {
                String facility = facilityEntry.getKey();
                LinkedHashMap<String, LinkedHashMap<String, src.models.Booking>> dates = facilityEntry.getValue();

                if (dates == null || dates.isEmpty() || !Database.Facility.get(facility).isAvailable()) continue;

                LinkedHashMap<String, LinkedHashMap<String, src.models.Booking>> activeDates = new LinkedHashMap<>();

                for (Map.Entry<String, LinkedHashMap<String, src.models.Booking>> dateEntry : dates.entrySet()) {
                    String date = dateEntry.getKey();
                    LinkedHashMap<String, src.models.Booking> timeSlots = dateEntry.getValue();
                    
                    if (timeSlots == null || timeSlots.isEmpty()) continue;

                    LinkedHashMap<String, src.models.Booking> activeTimeSlots = new LinkedHashMap<>();

                    for (Map.Entry<String, src.models.Booking> timeSlotEntry : timeSlots.entrySet()) {
                        String time = timeSlotEntry.getKey();
                        src.models.Booking booking = timeSlotEntry.getValue();

                        if (booking != null && !booking.isExpired()) {
                            activeTimeSlots.put(time, booking);
                        }
                    }

                    if (!activeTimeSlots.isEmpty()) {
                        activeDates.put(date, activeTimeSlots);
                    }
                }

                if (!activeDates.isEmpty()) {
                    activeData.put(facility, activeDates);
                }
            }

            return activeData;
        }

        public static LinkedHashMap<String, LinkedHashMap<String, LinkedHashMap<String, src.models.Booking>>> getPendingRequests() {
            LinkedHashMap<String, LinkedHashMap<String, LinkedHashMap<String, src.models.Booking>>> pendingData = getActiveBookings();

            pendingData.values().removeIf(datesMap -> {
                
                datesMap.values().removeIf(timeSlotsMap -> {
                    timeSlotsMap.values().removeIf(booking -> 
                        booking.getPending() == null || booking.getPending().isEmpty()
                    );

                    return timeSlotsMap.isEmpty(); 
                });

                return datesMap.isEmpty(); 
            });

            return pendingData;
        }
    }

    public static void main(String[] args) {
        // User.init();
        // Facility.init();
        Report.init();
        // Booking.init();

        for (Map.Entry<String, src.models.User> user : User.loadAll().entrySet())
        {
            String key = user.getKey();
            src.models.User val = user.getValue();

            System.out.println(key + "," + val.getRole());
        }
    }
}
