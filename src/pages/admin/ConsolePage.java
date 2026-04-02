package src.pages.admin;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Locale;

import javax.swing.JOptionPane;

import src.pages.cores.Subpage;
import src.utils.Database;
import src.utils.Renderer;
import src.utils.Router;
import src.components.Ansi;
import src.components.AnsiBuilder;
import src.components.Calendar;
import src.components.Table;
import src.components.fields.MemoField;
import src.models.Booking;
import src.models.BookingInfo;
import src.models.HighlightedDate;
import src.models.RGB;

public class ConsolePage extends Subpage {
    private static final int MAX_SELECTION = 5;
    private static final int DURATION_HOUR_STEP = 1;
    private static final int DURATION_MINUTE_STEP = 30;
    private static final LocalTime MIN_DURATION = LocalTime.of(0, DURATION_MINUTE_STEP);
    private static final int MAX_SESSION_QUANTITY = 10;
    private static final DateTimeFormatter FORMATTER_12_HOURS = DateTimeFormatter.ofPattern("hh:mma", Locale.ENGLISH);
    private boolean isLocked = false;
    private int durationComponent = 0;
    private int sessionComponent = 0;
    private LocalDate date;
    private LocalTime duration = MIN_DURATION;
    private LocalTime sessionStartTime = LocalTime.of(0, 0);
    private LinkedHashMap<String, Booking> sessions;

    public Subpage copy()
    {
        ConsolePage clone = new ConsolePage();

        clone.isLocked = this.isLocked;
        clone.durationComponent = this.durationComponent;
        clone.sessionComponent = this.sessionComponent;
        clone.date = this.date;
        clone.duration = this.duration;
        clone.sessionStartTime = this.sessionStartTime;
        clone.sessions = this.sessions;

        return clone;
    }

    @Override
    public void init()
    {
        this.selection = 1;
        updateSessions();
    }

    public ConsolePage()
    {
        this.date = LocalDate.now();
    }

    public LocalTime getSessionEndTime()
    {
        LocalTime sessionEndTime = this.sessionStartTime;

        sessionEndTime = sessionEndTime.plusHours(duration.getHour());
        sessionEndTime = sessionEndTime.plusMinutes(duration.getMinute());

        return sessionEndTime;
    }

    public String getSession()
    {
        return sessionStartTime.toString() + '-' + getSessionEndTime().toString();
    }

    public String get12HourSession()
    {
        return sessionStartTime.format(FORMATTER_12_HOURS) + '-' + getSessionEndTime().format(FORMATTER_12_HOURS);
    }

    public void createSession()
    {
        if (LocalDateTime.of(this.date, this.sessionStartTime).isBefore(LocalDateTime.now()))
        {
            JOptionPane.showMessageDialog(
                null,
                "The session you are trying to create (" +
                getSession() +
                ") is in the past.",
                "Invalid Session",
                JOptionPane.ERROR_MESSAGE
            );
            return;
        }

        if (this.sessions != null)
        {
            if (this.sessions.size() + 1 > MAX_SESSION_QUANTITY)
            {
                JOptionPane.showMessageDialog(
                    null,
                    "The maximum number of sessions allowed for this facility is " +
                    MAX_SESSION_QUANTITY +
                    '.',
                    "Maximum Session Limit Reached",
                    JOptionPane.ERROR_MESSAGE
                );
                return;
            }

            for (String session : this.sessions.keySet())
            {
                String[] sessionBounds = session.split("-");
                LocalTime starTime = LocalTime.parse(sessionBounds[0]);
                LocalTime endTime = LocalTime.parse(sessionBounds[1]);

                if (sessionStartTime.isBefore(endTime) && getSessionEndTime().isAfter(starTime))
                {
                    JOptionPane.showMessageDialog(
                        null,
                        "The session you are trying to create (" +
                        getSession() +
                        ") overlaps with an existing session (" +
                        session + ").",
                        "Session Overlap",
                        JOptionPane.ERROR_MESSAGE
                    );
                    return;
                }
            }
        }

        Database.Booking.getAll()
                        .computeIfAbsent(label, k -> new LinkedHashMap<>())
                        .computeIfAbsent(date.toString(), k -> new LinkedHashMap<>())
                        .put(getSession(), new Booking());
        updateSessions();
        Database.Booking.save();
    }

    public void updateSessions()
    {
        this.sessions = Database.Booking.Session.getAll(label, date);
    }

    public int getSessionsSize()
    {
        if (this.sessions != null)
            return this.sessions.size();
        return 0;
    }

    @Override
    public void render(StringBuilder frame)
    {
        ArrayList<ArrayList<AnsiBuilder>> tableBuilder;

        if (!Database.Facility.get(label).isAvailable())
        {
            tableBuilder = new ArrayList<>();
            tableBuilder.add(new ArrayList<>(Arrays.asList(new AnsiBuilder("Facility name: " + label))));
            tableBuilder.add(
                new ArrayList<>(Arrays.asList(new AnsiBuilder(
                    "Note:\n",
                    new MemoField(
                        "This facility is locked for maintenance currently.",
                        4,
                        50,
                        "",
                        false
                    ).toAnsiBuilder(),
                    "\n\n" + " ".repeat(15),
                    new Ansi("  Unlock Facility  ", Ansi.BG_GREEN, Ansi.FG_BLACK)
                )))
            );

            Table.render(frame, tableBuilder);
            return;
        }

        Calendar calendar = switch (selection)
        {
            case 1 -> {
                if (isLocked)
                {
                    yield new Calendar(
                        this.date,
                        Ansi.FG_WHITE,
                        Ansi.FG_BLACK,
                        Ansi.BG_LIGHT_BLUE,
                        9
                    ).pastTextColor(Ansi.FG_DARK_GRAY);
                }
                else
                {
                    yield new Calendar(
                        this.date,
                        Ansi.FG_GREEN,
                        Ansi.FG_BLACK,
                        Ansi.BG_GREEN,
                        9
                    ).pastTextColor(new RGB(0, 100, 0));
                }
            }
            default -> new Calendar(
                this.date,
                Ansi.FG_DARK_GRAY,
                Ansi.FG_BLACK,
                Ansi.BG_DARK_GRAY,
                9
            );
        };

        if (Database.Booking.getAll().get(label) != null)
        {
            ArrayList<HighlightedDate> highlightedDates = new ArrayList<>();

            for (String sessionDateString : Database.Booking.getAll().get(label).keySet())
            {
                LocalDate sessionDate = LocalDate.parse(sessionDateString);
                // frame.append(sessionDate).append('\n');

                if (sessionDate.getMonthValue() == this.date.getMonthValue()
                    && !LocalDateTime.of(sessionDate, getSessionEndTime()).isBefore(LocalDateTime.now()))
                {
                    // frame.append("IN");
                    highlightedDates.add(new HighlightedDate(sessionDate, Ansi.FG_BLACK, Ansi.BG_LIGHT_CYAN));
                }
            }

            calendar.setHighlightDates(highlightedDates);
        }

        tableBuilder = new ArrayList<>(Arrays.asList(
            new ArrayList<>(Arrays.asList(new AnsiBuilder(
                "Facility name: " + label + "   ",
                new Ansi(
                    "Lock Facility",
                    selection == 0 ? Ansi.BG_RED : Ansi.BG_WHITE,
                    Ansi.FG_BLACK
                )
            ))),
            new ArrayList<>(Arrays.asList(new AnsiBuilder(
                new Ansi("Date: " + this.date.toString() + "\n\n"),
                calendar.toAnsiBuilder()
            ))),
            new ArrayList<>(Arrays.asList(
                switch (selection)
                {
                    case 2 -> {
                        if (isLocked)
                        {
                            yield
                                new AnsiBuilder(
                                    new Ansi("Session duration:\n" + " ".repeat(11) + "▲" + " ".repeat(8) + "▲\n" + " ".repeat(10)),
                                    new Ansi(
                                        String.format("%2d", duration.getHour()),
                                        durationComponent == 0 ? Ansi.BG_LIGHT_BLUE : Ansi.BG_WHITE,
                                        Ansi.FG_BLACK
                                    ),
                                    new Ansi(" hours "),
                                    new Ansi(
                                        String.format("%2d", duration.getMinute()),
                                        durationComponent == 1 ? Ansi.BG_LIGHT_BLUE : Ansi.BG_WHITE,
                                        Ansi.FG_BLACK
                                    ),
                                    new Ansi(" minutes\n" + " ".repeat(11) + "▼" + " ".repeat(8) + "▼")
                                );
                        }
                        else
                        {
                            yield
                                new AnsiBuilder(
                                    new Ansi("Session duration:\n\n" + " ".repeat(10)),
                                    new Ansi(
                                        String.format("%2d", duration.getHour()),
                                        Ansi.FG_BLACK
                                    ).background(durationComponent == 0 ? new RGB(0, 170, 0) : new RGB(0, 128, 0)),
                                    new Ansi(" hours "),
                                    new Ansi(
                                        String.format("%2d", duration.getMinute()),
                                        Ansi.FG_BLACK
                                    ).background(durationComponent == 1 ? new RGB(0, 170, 0) : new RGB(0, 128, 0)),
                                    new Ansi(" minutes\n")
                                );
                        }
                    }
                    default -> new AnsiBuilder(
                                new Ansi("Session duration:\n\n" + " ".repeat(10)),
                                new Ansi(
                                    String.format("%2d", duration.getHour()),
                                    Ansi.FG_BLACK
                                ).background(durationComponent == 0 ? new RGB(169, 169, 169) : new RGB(128, 128, 128)),
                                new Ansi(" hours "),
                                new Ansi(
                                    String.format("%2d", duration.getMinute()),
                                    Ansi.FG_BLACK
                                ).background(durationComponent == 1 ? new RGB(169, 169, 169) : new RGB(128, 128, 128)),
                                new Ansi(" minutes\n")
                            );
                }
            )),
            new ArrayList<>(Arrays.asList(
                switch (selection)
                {
                    case 3 -> {
                        if (isLocked)
                        {
                            yield
                                new AnsiBuilder(
                                    new Ansi("Create a new session with a start time:\n" + " ".repeat(18) + "▲" + " ".repeat(1) + " ▲\n" + " ".repeat(17)),
                                    new Ansi(
                                        String.format("%2d", this.sessionStartTime.getHour()),
                                        sessionComponent == 0 ? Ansi.BG_LIGHT_BLUE : Ansi.BG_WHITE,
                                        Ansi.FG_BLACK
                                    ),
                                    new Ansi(':'),
                                    new Ansi(
                                        String.format("%2d", this.sessionStartTime.getMinute()),
                                        sessionComponent == 1 ? Ansi.BG_LIGHT_BLUE : Ansi.BG_WHITE,
                                        Ansi.FG_BLACK
                                    ),
                                    new Ansi("\n" + " ".repeat(18) + "▼" + " ".repeat(1) + " ▼"),
                                    new Ansi("\n" + " ".repeat(3) + "Session Preview: " + getSession() + '\n' +
                                            " ".repeat(20) + get12HourSession() + "\n\n" +
                                            " ".repeat(12)),
                                    new Ansi(
                                        "Create session",
                                        Ansi.BG_WHITE,
                                        Ansi.FG_BLACK
                                    )
                                );
                        }
                        else
                        {
                            yield
                                new AnsiBuilder(
                                    new Ansi("Create a new session with a start time:\n\n" + " ".repeat(17)),
                                    new Ansi(
                                        String.format("%2d", this.sessionStartTime.getHour()),
                                        Ansi.FG_BLACK
                                    ).background(sessionComponent == 0 ? new RGB(0, 170, 0) : new RGB(0, 128, 0)),
                                    new Ansi(':'),
                                    new Ansi(
                                        String.format("%2d", this.sessionStartTime.getMinute()),
                                        Ansi.FG_BLACK
                                    ).background(sessionComponent == 1 ? new RGB(0, 170, 0) : new RGB(0, 128, 0)),
                                    new Ansi("\n\n" + " ".repeat(3) + "Session Preview: " + getSession() + '\n' +
                                            " ".repeat(20) + get12HourSession() + "\n\n" +
                                            " ".repeat(12)),
                                    new Ansi(
                                        "Create session",
                                        Ansi.BG_WHITE,
                                        Ansi.FG_BLACK
                                    )
                                );
                        }
                    }
                    default -> new AnsiBuilder(
                        new Ansi("Create a new session with a start time:\n\n" + " ".repeat(17)),
                        new Ansi(
                            String.format("%2d", this.sessionStartTime.getHour()),
                            Ansi.FG_BLACK
                        ).background(sessionComponent == 0 ? new RGB(169, 169, 169) : new RGB(128, 128, 128)),
                        new Ansi(':'),
                        new Ansi(
                            String.format("%2d", this.sessionStartTime.getMinute()),
                            Ansi.FG_BLACK
                        ).background(sessionComponent == 1 ? new RGB(169, 169, 169) : new RGB(128, 128, 128)),
                        new Ansi("\n\n" + " ".repeat(3) + "Session Preview: " + getSession() + '\n' +
                                " ".repeat(20) + get12HourSession() + "\n\n" +
                                " ".repeat(12)),
                        new Ansi(
                            "Create session",
                            selection == 4 ? Ansi.BG_GREEN : Ansi.BG_WHITE,
                            Ansi.FG_BLACK
                        )
                    );
                }
            ))
        ));

        AnsiBuilder sessionListBuilder = new AnsiBuilder();

        if (this.sessions != null && this.sessions.size() > 0)
        {
            int counter = 1;

            for (String session : this.sessions.keySet())
            {
                if (selection + 1 > MAX_SELECTION && selection + 1 - MAX_SELECTION == counter)
                    sessionListBuilder.append("> ");
                else
                    sessionListBuilder.append("  ");

                if (getApproved(session) != null)
                {
                    sessionListBuilder.append(new Ansi(
                        counter + ". " + session,
                        Ansi.FG_RED,
                        Ansi.UNDERLINE)
                    );
                }
                else if (getPending(session).size() > 0)
                {
                    sessionListBuilder.append(new Ansi(
                        counter + ". " + session,
                        Ansi.FG_LIGHT_CYAN,
                        Ansi.UNDERLINE)
                    );
                }
                else
                {
                    sessionListBuilder.append(new Ansi(
                        counter + ". " + session,
                        Ansi.UNDERLINE)
                    );
                }

                if (selection + 1 > MAX_SELECTION && selection + 1 - MAX_SELECTION == counter)
                    sessionListBuilder.append(' ').append(new Ansi("REMOVE", Ansi.FG_RED, Ansi.UNDERLINE)).append("🚮");

                if (counter < this.sessions.keySet().size())
                    sessionListBuilder.append('\n');
                counter++;
            }
        }
        else
        {
            sessionListBuilder.append("NULL");
        }

        tableBuilder.add(new ArrayList<>(Arrays.asList(new AnsiBuilder(
            new Ansi("Sessions created:\n"),
            sessionListBuilder
        ))));

        Table.render(frame, tableBuilder);
    }

    public ArrayList<BookingInfo> getPending(String session)
    {
        if (Database.Booking.getAll().get(label) != null &&
            Database.Booking.getAll().get(label).get(date.toString()) != null &&
            Database.Booking.getAll().get(label).get(date.toString()).get(session) != null)
        {
            return Database.Booking.getAll().get(label).get(date.toString()).get(session).getPending();
        }
        return null;
    }

    public String getApproved(String session)
    {
        if (Database.Booking.getAll().get(label) != null &&
            Database.Booking.getAll().get(label).get(date.toString()) != null &&
            Database.Booking.getAll().get(label).get(date.toString()).get(session) != null)
        {
            return Database.Booking.getAll().get(label).get(date.toString()).get(session).getApproved();
        }
        return null;
    }

    @Override
    public void handleAction(String action)
    {
        if (!Database.Facility.get(label).isAvailable())
        {
            switch (action)
            {
                case "ENTER" -> {
                    int response = JOptionPane.showConfirmDialog(
                        null,
                        "Are you sure you want to unlock the facility?",
                        "Confirmation Dialog",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.WARNING_MESSAGE
                    );

                    if (response == JOptionPane.YES_OPTION)
                    {
                        Database.Facility.get(label).setIsAvailable(true);
                        Database.Facility.save();
                        Renderer.refresh();
                    }
                }
                case "ESC" -> {
                    Router.back();
                }
            }
            return;
        }

        if (isLocked)
        {
            switch (selection)
            {
                case 1 -> {
                    handleCalenderAction(action);
                }
                case 2 -> {
                    handleDurationAction(action);
                }
                case 3 -> {
                    handleSessionAction(action);
                }
            }
        }
        else
        {
            switch (action)
            {
                case "DOWN" -> {
                    if (selection + 1 < MAX_SELECTION + getSessionsSize())
                    {
                        selection++;
                    }
                    else
                    {
                        selection = 0;
                    }
                }
                case "UP" -> {
                    if (selection - 1 >= 0)
                    {
                        selection--;
                    }
                    else
                    {
                        selection = MAX_SELECTION + getSessionsSize() - 1;
                    }
                }
                case "ENTER" -> {
                    switch (selection)
                    {
                        case 0 -> {
                            int response = JOptionPane.showConfirmDialog(
                                null,
                                "Are you sure you want to to lock this facility for maintenance?",
                                "Confirmation Dialog",
                                JOptionPane.YES_NO_OPTION,
                                JOptionPane.WARNING_MESSAGE
                            );

                            if (response == JOptionPane.OK_OPTION)
                            {
                                Database.Facility.get(label).setIsAvailable(false);
                                Database.Facility.save();
                                this.selection = 1;
                            }
                        }
                        case 1, 2, 3 ->
                        {
                            isLocked = true;
                        }
                        case 4 -> {
                            createSession();
                        }
                        default -> {
                            removeSession();
                        }
                    }
                }
                case "ESC" -> {
                    Router.back();
                }
            }
        }
        
        Renderer.refresh();
    }

    public void removeSession()
    {
        if (selection + 1 > MAX_SELECTION)
        {
            int sessionIdx = selection - MAX_SELECTION;
            String session = new ArrayList<>(this.sessions.keySet()).get(sessionIdx);
            int response = JOptionPane.YES_OPTION;

            if (getApproved(session) != null)
            {
                response = JOptionPane.showConfirmDialog(
                    null,
                    "Are you sure you want to remove this session - " + session + "?\n" +
                    "There is student approved for this session.",
                    "Confirmation Dialog",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE
                );
            }
            else if (getPending(session).size() > 0)
            {
                response = JOptionPane.showConfirmDialog(
                    null,
                    "Are you sure you want to remove this session - " + session + "?\n" +
                    "There " + (getPending(session).size() > 1 ? "are " : "is ") +
                    Integer.toString(getPending(session).size()) + " student" + (getPending(session).size() > 1 ? "s" : "") +
                    " requested for this session.",
                    "Confirmation Dialog",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE
                );
            }

            if (response == JOptionPane.YES_OPTION)
            {
                this.sessions.remove(session);

                if (sessionIdx >= this.sessions.size())
                    selection--;
                
                if (this.sessions.size() == 0)
                {
                    Database.Booking.Session.remove(label, date);
                    sessions = null;
                }
            }

            Database.Booking.save();
        }
    }

    public void handleCalenderAction(String action)
    {
        switch (action)
        {
            case "LEFT" -> {
                if (this.date.minusDays(1).isBefore(LocalDate.now()))
                {
                    return;
                }
                this.date = this.date.minusDays(1);
            }
            case "RIGHT" -> {
                this.date = this.date.plusDays(1);
            }
            case "UP" -> {
                if (this.date.minusWeeks(1).isBefore(LocalDate.now()))
                {
                    return;
                }
                this.date = this.date.minusWeeks(1);
            }
            case "DOWN" -> {
                this.date = this.date.plusWeeks(1);
            }
            case "ESC", "ENTER" -> {
                this.isLocked = false;
                return;
            }
        }
        updateSessions();
    }

    public void handleDurationAction(String action)
    {
        switch (action)
        {
            case "LEFT" -> {
                if (this.durationComponent - 1 >= 0)
                    this.durationComponent--;
                else
                    this.durationComponent = 1;
            }
            case "RIGHT" -> {
                if (this.durationComponent + 1 <= 1)
                    this.durationComponent++;
                else
                    this.durationComponent = 0;
            }
            case "UP" -> {
                switch (this.durationComponent)
                {
                    case 0 -> {
                        if (!this.duration.plusHours(DURATION_HOUR_STEP).isBefore(MIN_DURATION))
                            this.duration = this.duration.plusHours(DURATION_HOUR_STEP);
                    }
                    case 1 -> {
                        if (!this.duration.plusMinutes(DURATION_MINUTE_STEP).isBefore(MIN_DURATION))
                            this.duration = this.duration.plusMinutes(DURATION_MINUTE_STEP);
                    }
                }
            }
            case "DOWN" -> {
                switch (this.durationComponent)
                {
                    case 0 -> {
                        if (!this.duration.minusHours(DURATION_HOUR_STEP).isBefore(MIN_DURATION))
                            this.duration = this.duration.minusHours(DURATION_HOUR_STEP);
                    }
                    case 1 -> {
                        if (!this.duration.minusMinutes(DURATION_MINUTE_STEP).isBefore(MIN_DURATION))
                            this.duration = this.duration.minusMinutes(DURATION_MINUTE_STEP);
                    }
                }
            }
            case "ESC", "ENTER" -> {
                this.isLocked = false;
            }
        }
    }

    public void handleSessionAction(String action)
    {
        switch (action)
        {
            case "LEFT" -> {
                if (this.sessionComponent - 1 >= 0)
                    this.sessionComponent--;
                else
                    this.sessionComponent = 1;
            }
            case "RIGHT" -> {
                if (this.sessionComponent + 1 <= 1)
                    this.sessionComponent++;
                else
                    this.sessionComponent = 0;
            }
            case "UP" -> {
                switch (this.sessionComponent)
                {
                    case 0 -> {
                        this.sessionStartTime = this.sessionStartTime.plusHours(1);
                    }
                    case 1 -> {
                        this.sessionStartTime = this.sessionStartTime.plusMinutes(1);
                    }
                }
            }
            case "DOWN" -> {
                switch (this.sessionComponent)
                {
                    case 0 -> {
                        this.sessionStartTime = this.sessionStartTime.minusHours(1);
                    }
                    case 1 -> {
                        this.sessionStartTime = this.sessionStartTime.minusMinutes(1);
                    }
                }
            }
            case "ESC", "ENTER" -> {
                this.isLocked = false;
            }
        }
    }
}
