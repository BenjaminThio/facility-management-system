package src.pages;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;

import javax.swing.JOptionPane;

import src.components.Ansi;
import src.components.AnsiBuilder;
import src.components.Calendar;
import src.components.Table;
import src.models.Facility;
import src.models.RGB;
import src.pages.cores.Page;
import src.utils.Database;
import src.utils.Renderer;
import src.utils.Router;

public class SearchFacilityPage extends Page {
    private static final int MAX_SELECTION = 5;
    private static final int DURATION_HOUR_STEP = 1;
    private static final int DURATION_MINUTE_STEP = 30;
    private static final LocalTime MIN_DURATION = LocalTime.of(0, DURATION_MINUTE_STEP);
    private static final DateTimeFormatter FORMATTER_12_HOURS = DateTimeFormatter.ofPattern("hh:mma", Locale.ENGLISH);

    private boolean isLocked = false;
    private int durationComponent = 0;
    private int sessionComponent = 0;
    
    private LocalDate date;
    private LocalTime duration = LocalTime.of(1, 0); 
    private LocalTime sessionStartTime = LocalTime.of(8, 0); 
    private Facility.Type type = Facility.Type.LECTURE_HALL;

    public SearchFacilityPage() {
        this.date = LocalDate.now();
        this.selection = 0;
    }

    public LocalTime getSessionEndTime() {
        LocalTime sessionEndTime = this.sessionStartTime;
        sessionEndTime = sessionEndTime.plusHours(duration.getHour());
        sessionEndTime = sessionEndTime.plusMinutes(duration.getMinute());
        return sessionEndTime;
    }

    public String getSession() {
        return sessionStartTime.toString() + '-' + getSessionEndTime().toString();
    }

    public String get12HourSession() {
        return sessionStartTime.format(FORMATTER_12_HOURS) + '-' + getSessionEndTime().format(FORMATTER_12_HOURS);
    }

    @Override
    public void render(StringBuilder frame) {
        ArrayList<ArrayList<AnsiBuilder>> tableBuilder = new ArrayList<>();

        tableBuilder.add(new ArrayList<>(Arrays.asList(
            new AnsiBuilder(new Ansi(" ".repeat(5) + "Search Available Facilities" + " ".repeat(6)))
        )));

        Calendar calendar = switch (selection) {
            case 0 -> {
                if (isLocked) {
                    yield new Calendar(this.date, Ansi.FG_WHITE, Ansi.FG_BLACK, Ansi.BG_LIGHT_BLUE, 9).pastTextColor(Ansi.FG_DARK_GRAY);
                } else {
                    yield new Calendar(this.date, Ansi.FG_GREEN, Ansi.FG_BLACK, Ansi.BG_GREEN, 9).pastTextColor(new RGB(0, 100, 0));
                }
            }
            default -> new Calendar(this.date, Ansi.FG_DARK_GRAY, Ansi.FG_BLACK, Ansi.BG_DARK_GRAY, 9);
        };

        tableBuilder.add(new ArrayList<>(Arrays.asList(
            new AnsiBuilder(
                new Ansi("Date: " + this.date.toString() + "\n\n"),
                calendar.toAnsiBuilder()
            )
        )));

        tableBuilder.add(new ArrayList<>(Arrays.asList(
            switch (selection) {
                case 1 -> {
                    if (isLocked) {
                        yield new AnsiBuilder(
                            new Ansi("Session duration:\n" + " ".repeat(10) + "▲" + " ".repeat(8) + "▲\n" + " ".repeat(9)),
                            new Ansi(String.format("%2d", duration.getHour()), durationComponent == 0 ? Ansi.BG_LIGHT_BLUE : Ansi.BG_WHITE, Ansi.FG_BLACK),
                            new Ansi(" hours "),
                            new Ansi(String.format("%2d", duration.getMinute()), durationComponent == 1 ? Ansi.BG_LIGHT_BLUE : Ansi.BG_WHITE, Ansi.FG_BLACK),
                            new Ansi(" minutes\n" + " ".repeat(10) + "▼" + " ".repeat(8) + "▼")
                        );
                    } else {
                        yield new AnsiBuilder(
                            new Ansi("Session duration:\n\n" + " ".repeat(9)),
                            new Ansi(String.format("%2d", duration.getHour()), Ansi.FG_BLACK).background(durationComponent == 0 ? new RGB(0, 170, 0) : new RGB(0, 128, 0)),
                            new Ansi(" hours "),
                            new Ansi(String.format("%2d", duration.getMinute()), Ansi.FG_BLACK).background(durationComponent == 1 ? new RGB(0, 170, 0) : new RGB(0, 128, 0)),
                            new Ansi(" minutes\n")
                        );
                    }
                }
                default -> new AnsiBuilder(
                    new Ansi("Session duration:\n\n" + " ".repeat(9)),
                    new Ansi(String.format("%2d", duration.getHour()), Ansi.FG_BLACK).background(durationComponent == 0 ? new RGB(169, 169, 169) : new RGB(128, 128, 128)),
                    new Ansi(" hours "),
                    new Ansi(String.format("%2d", duration.getMinute()), Ansi.FG_BLACK).background(durationComponent == 1 ? new RGB(169, 169, 169) : new RGB(128, 128, 128)),
                    new Ansi(" minutes\n")
                );
            }
        )));

        tableBuilder.add(new ArrayList<>(Arrays.asList(
            switch (selection) {
                case 2 -> {
                    if (isLocked) {
                        yield new AnsiBuilder(
                            new Ansi("Select a target start time:\n" + " ".repeat(18) + "▲" + " ".repeat(1) + " ▲\n" + " ".repeat(17)),
                            new Ansi(String.format("%2d", this.sessionStartTime.getHour()), sessionComponent == 0 ? Ansi.BG_LIGHT_BLUE : Ansi.BG_WHITE, Ansi.FG_BLACK),
                            new Ansi(':'),
                            new Ansi(String.format("%2d", this.sessionStartTime.getMinute()), sessionComponent == 1 ? Ansi.BG_LIGHT_BLUE : Ansi.BG_WHITE, Ansi.FG_BLACK),
                            new Ansi("\n" + " ".repeat(18) + "▼" + " ".repeat(1) + " ▼"),
                            new Ansi("\n   Session Preview: " + getSession() + '\n' + " ".repeat(20) + get12HourSession() + "\n")
                        );
                    } else {
                        yield new AnsiBuilder(
                            new Ansi("Select a target start time:\n\n" + " ".repeat(17)),
                            new Ansi(String.format("%2d", this.sessionStartTime.getHour()), Ansi.FG_BLACK).background(sessionComponent == 0 ? new RGB(0, 170, 0) : new RGB(0, 128, 0)),
                            new Ansi(':'),
                            new Ansi(String.format("%2d", this.sessionStartTime.getMinute()), Ansi.FG_BLACK).background(sessionComponent == 1 ? new RGB(0, 170, 0) : new RGB(0, 128, 0)),
                            new Ansi("\n\n   Session Preview: " + getSession() + '\n' + " ".repeat(20) + get12HourSession() + "\n")
                        );
                    }
                }
                default -> new AnsiBuilder(
                    new Ansi("Select a target start time:\n\n" + " ".repeat(17)),
                    new Ansi(String.format("%2d", this.sessionStartTime.getHour()), Ansi.FG_BLACK).background(sessionComponent == 0 ? new RGB(169, 169, 169) : new RGB(128, 128, 128)),
                    new Ansi(':'),
                    new Ansi(String.format("%2d", this.sessionStartTime.getMinute()), Ansi.FG_BLACK).background(sessionComponent == 1 ? new RGB(169, 169, 169) : new RGB(128, 128, 128)),
                    new Ansi("\n\n   Session Preview: " + getSession() + '\n' + " ".repeat(20) + get12HourSession() + "\n")
                );
            }
        )));

        tableBuilder.add(new ArrayList<>(Arrays.asList(
            switch (selection) {
                case 3 -> new AnsiBuilder(
                    new Ansi("Facility Type:\n\n" + " ".repeat(7)),
                    new Ansi(String.format(" %-21s ", this.type.getName()), Ansi.BG_GREEN, Ansi.FG_BLACK),
                    new Ansi("\n")
                );
                default -> new AnsiBuilder(
                    new Ansi("Facility Type:\n\n" + " ".repeat(7)),
                    new Ansi(String.format(" %-21s ", this.type.getName()), Ansi.BG_WHITE, Ansi.FG_BLACK),
                    new Ansi("\n")
                );
            }
        )));

        tableBuilder.add(new ArrayList<>(Arrays.asList(
            switch (selection) {
                case 4 -> new AnsiBuilder(
                    new Ansi("\n" + " ".repeat(7)),
                    new Ansi("   Search Facilities   ", Ansi.BG_GREEN, Ansi.FG_BLACK),
                    new Ansi("\n")
                );
                default -> new AnsiBuilder(
                    new Ansi("\n" + " ".repeat(7)),
                    new Ansi("   Search Facilities   ", Ansi.BG_WHITE, Ansi.FG_BLACK),
                    new Ansi("\n")
                );
            }
        )));

        Table.render(frame, tableBuilder);
    }

    private void performSearch() {
        if (LocalDateTime.of(this.date, this.sessionStartTime).isBefore(LocalDateTime.now())) {
            JOptionPane.showMessageDialog(null, "The session you are searching for is in the past.", "Invalid Search", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String dateStr = this.date.toString();
        String sessionStr = getSession();
        ArrayList<String> availableFacilities = new ArrayList<>();

        for (Facility f : Database.Facility.getAll()) {
            if (f.getType() != this.type) continue;
            if (!f.isAvailable()) continue;

            boolean hasAvailableSession = false;
            var facBookings = Database.Booking.getAll().get(f.getName());
            
            // --- THE FIX ---
            // The session MUST physically exist in the database and MUST NOT be approved yet!
            if (facBookings != null && facBookings.get(dateStr) != null) {
                var sessionData = facBookings.get(dateStr).get(sessionStr);
                if (sessionData != null && sessionData.getApproved() == null) {
                    hasAvailableSession = true;
                }
            }
            // ---------------

            if (hasAvailableSession) {
                availableFacilities.add(f.getName());
            }
        }

        if (availableFacilities.isEmpty()) {
            JOptionPane.showMessageDialog(null, "No " + this.type.getName() + "s are available on " + dateStr + " for " + sessionStr + ".", "No Results", JOptionPane.INFORMATION_MESSAGE);
        } else {
            // --- UPDATED LOGIC: Capture the date and session! ---
            LocalDate searchedDate = this.date;
            String searchedSession = sessionStr;

            Router.redirect(new ListViewPage(
                availableFacilities.toArray(String[]::new),
                (index) -> {
                    // Pass the data to the ViewFacilityPage AND forcefully initialize it
                    ViewFacilityPage targetPage = new ViewFacilityPage(availableFacilities.get(index), searchedDate, searchedSession);
                    targetPage.init(); 
                    Router.redirect(targetPage);
                }
            ));
            // ----------------------------------------------------
        }
    }

    @Override
    public void handleAction(String action) {
        if (isLocked) {
            switch (selection) {
                case 0 -> handleCalendarAction(action);
                case 1 -> handleDurationAction(action);
                case 2 -> handleSessionAction(action);
            }
        } else {
            switch (action) {
                case "DOWN" -> {
                    if (selection + 1 < MAX_SELECTION) selection++;
                    else selection = 0;
                }
                case "UP" -> {
                    if (selection - 1 >= 0) selection--;
                    else selection = MAX_SELECTION - 1;
                }
                case "ENTER" -> {
                    if (selection < 3) {
                        isLocked = true;
                    } else if (selection == 3) {
                        Router.redirect(new ListViewPage(
                            Arrays.stream(Facility.Type.values()).map(Facility.Type::getName).toArray(String[]::new),
                            (index) -> {
                                this.type = Facility.Type.cast(index);
                            }
                        ));
                    } else if (selection == 4) {
                        performSearch();
                    }
                }
                case "ESC" -> Router.back();
            }
        }
        Renderer.refresh();
    }

    public void handleCalendarAction(String action) {
        switch (action) {
            case "LEFT" -> {
                if (!this.date.minusDays(1).isBefore(LocalDate.now())) this.date = this.date.minusDays(1);
            }
            case "RIGHT" -> this.date = this.date.plusDays(1);
            case "UP" -> {
                if (!this.date.minusWeeks(1).isBefore(LocalDate.now())) this.date = this.date.minusWeeks(1);
            }
            case "DOWN" -> this.date = this.date.plusWeeks(1);
            case "ESC", "ENTER" -> this.isLocked = false;
        }
    }

    public void handleDurationAction(String action) {
        switch (action) {
            case "LEFT" -> this.durationComponent = (this.durationComponent - 1 >= 0) ? this.durationComponent - 1 : 1;
            case "RIGHT" -> this.durationComponent = (this.durationComponent + 1 <= 1) ? this.durationComponent + 1 : 0;
            case "UP" -> {
                if (this.durationComponent == 0 && !this.duration.plusHours(DURATION_HOUR_STEP).isBefore(MIN_DURATION)) 
                    this.duration = this.duration.plusHours(DURATION_HOUR_STEP);
                else if (this.durationComponent == 1 && !this.duration.plusMinutes(DURATION_MINUTE_STEP).isBefore(MIN_DURATION)) 
                    this.duration = this.duration.plusMinutes(DURATION_MINUTE_STEP);
            }
            case "DOWN" -> {
                if (this.durationComponent == 0 && !this.duration.minusHours(DURATION_HOUR_STEP).isBefore(MIN_DURATION)) 
                    this.duration = this.duration.minusHours(DURATION_HOUR_STEP);
                else if (this.durationComponent == 1 && !this.duration.minusMinutes(DURATION_MINUTE_STEP).isBefore(MIN_DURATION)) 
                    this.duration = this.duration.minusMinutes(DURATION_MINUTE_STEP);
            }
            case "ESC", "ENTER" -> this.isLocked = false;
        }
    }

    public void handleSessionAction(String action) {
        switch (action) {
            case "LEFT" -> this.sessionComponent = (this.sessionComponent - 1 >= 0) ? this.sessionComponent - 1 : 1;
            case "RIGHT" -> this.sessionComponent = (this.sessionComponent + 1 <= 1) ? this.sessionComponent + 1 : 0;
            case "UP" -> {
                if (this.sessionComponent == 0) this.sessionStartTime = this.sessionStartTime.plusHours(1);
                else this.sessionStartTime = this.sessionStartTime.plusMinutes(1);
            }
            case "DOWN" -> {
                if (this.sessionComponent == 0) this.sessionStartTime = this.sessionStartTime.minusHours(1);
                else this.sessionStartTime = this.sessionStartTime.minusMinutes(1);
            }
            case "ESC", "ENTER" -> this.isLocked = false;
        }
    }
}