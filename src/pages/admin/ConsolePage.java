package src.pages.admin;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;

import src.pages.cores.Subpage;
import src.utils.Database;
import src.utils.Renderer;
import src.utils.Router;
import src.components.Ansi;
import src.components.Calendar;
import src.models.Booking;

public class ConsolePage extends Subpage {
    private static final int MAX_SELECTION = 4;
    private static final int DURATION_HOUR_STEP = 1;
    private static final int DURATION_MINUTE_STEP = 30;
    private static final LocalTime MIN_DURATION = LocalTime.of(0, DURATION_MINUTE_STEP);
    private static final int MAX_SESSION_QUANTITY = 10;
    private boolean isLocked = false;
    private int durationComponent = 0;
    private int sessionComponent = 0;
    private LocalDate date;
    private LocalTime duration = MIN_DURATION;
    private LocalTime sessionStartTime = LocalTime.of(0, 0);
    private LinkedHashMap<String, Booking> sessions;

    @Override
    public void init()
    {
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
        return sessionStartTime.toString() + "-" + getSessionEndTime().toString();
    }

    public void createSession()
    {
        if (this.sessions != null)
        {
            if (this.sessions.size() > MAX_SESSION_QUANTITY)
            {
                return;
            }

            for (String session : this.sessions.keySet())
            {
                String[] sessionBounds = session.split("-");
                LocalTime starTime = LocalTime.parse(sessionBounds[0]);
                LocalTime endTime = LocalTime.parse(sessionBounds[1]);

                if (sessionStartTime.isBefore(endTime) && getSessionEndTime().isAfter(starTime))
                {
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
    public void render()
    {
        Calendar calendar = new Calendar(
            this.date,
            Ansi.FG_DARK_GRAY,
            Ansi.FG_BLACK,
            Ansi.BG_DARK_GRAY);

        switch (selection)
        {
            case 0 -> {
                if (isLocked)
                {
                    calendar = new Calendar(
                        this.date,
                        Ansi.FG_WHITE,
                        Ansi.FG_BLACK,
                        Ansi.BG_LIGHT_BLUE);
                }
                else
                {
                    calendar = new Calendar(
                        this.date,
                        Ansi.FG_GREEN,
                        Ansi.FG_BLACK,
                        Ansi.BG_GREEN);
                }
            }
        }

        System.out.println("Facility name: " + label);
        System.out.println();
        System.out.print("Date: ");
        System.out.println(this.date);
        System.out.println();
        System.out.println(calendar);
        System.out.println("Session duration:");

        switch (selection)
        {
            case 1 -> {
                if (isLocked)
                {
                    System.out.println(
                    new Ansi(String.format("%2d", duration.getHour()), durationComponent == 0 ? Ansi.BG_LIGHT_BLUE : Ansi.BG_WHITE, Ansi.FG_BLACK).toString() + " hours " +
                    new Ansi(String.format("%2d", duration.getMinute()), durationComponent == 1 ? Ansi.BG_LIGHT_BLUE : Ansi.BG_WHITE, Ansi.FG_BLACK).toString() + " minutes");
                }
                else
                {
                    System.out.println(
                    new Ansi(String.format("%2d", duration.getHour()), Ansi.BG_GREEN, Ansi.FG_BLACK).toString() + " hours " +
                    new Ansi(String.format("%2d", duration.getMinute()), Ansi.BG_GREEN, Ansi.FG_BLACK).toString() + " minutes");
                }
            }
            default -> {
                System.out.println(
                new Ansi(String.format("%2d", duration.getHour()), Ansi.BG_DARK_GRAY, Ansi.FG_BLACK).toString() + " hours " +
                new Ansi(String.format("%2d", duration.getMinute()), Ansi.BG_DARK_GRAY, Ansi.FG_BLACK).toString() + " minutes");
            }
        }

        System.out.println();
        System.out.println("Create a new session with a start time:");

        switch (selection)
        {
            case 2 -> {
                if (isLocked)
                {
                    System.out.println(
                    new Ansi(String.format("%2d", this.sessionStartTime.getHour()), sessionComponent == 0 ? Ansi.BG_LIGHT_BLUE : Ansi.BG_WHITE, Ansi.FG_BLACK) + ":" +
                    new Ansi(String.format("%2d", this.sessionStartTime.getMinute()), sessionComponent == 1 ? Ansi.BG_LIGHT_BLUE : Ansi.BG_WHITE, Ansi.FG_BLACK));
                }
                else
                {
                    System.out.println(
                    new Ansi(String.format("%2d", this.sessionStartTime.getHour()), Ansi.BG_GREEN, Ansi.FG_BLACK) + ":" +
                    new Ansi(String.format("%2d", this.sessionStartTime.getMinute()), Ansi.BG_GREEN, Ansi.FG_BLACK));
                }
            }
            default -> {
                System.out.println(
                new Ansi(String.format("%2d", this.sessionStartTime.getHour()), Ansi.BG_DARK_GRAY, Ansi.FG_BLACK) + ":" +
                new Ansi(String.format("%2d", this.sessionStartTime.getMinute()), Ansi.BG_DARK_GRAY, Ansi.FG_BLACK));
            }
        }

        System.out.println();
        System.out.println("Session Preview: " + getSession());
        System.out.println();
        System.out.println(new Ansi("Create session", selection == 3 ? Ansi.BG_GREEN : Ansi.BG_WHITE, Ansi.FG_BLACK));
        System.out.println();
        System.out.println("Sessions created:");

        if (this.sessions != null && this.sessions.size() > 0)
        {
            int counter = 1;

            for (String session : this.sessions.keySet())
            {
                if (selection + 1 > MAX_SELECTION && selection + 1 - MAX_SELECTION == counter)
                    System.out.print("> ");
                else
                    System.out.print("  ");

                System.out.print(counter + ". " + session);

                if (selection + 1 > MAX_SELECTION && selection + 1 - MAX_SELECTION == counter)
                    System.out.print(" " + new Ansi("REMOVE", Ansi.FG_RED, Ansi.UNDERLINE).toString());

                System.out.println();
                counter++;
            }
        }
        else
        {
            System.out.println("NULL");
        }
    }

    @Override
    public void handleAction(String action)
    {
        if (isLocked)
        {
            switch (selection)
            {
                case 0 -> {
                    handleCalenderAction(action);
                }
                case 1 -> {
                    handleDurationAction(action);
                }
                case 2 -> {
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
                        case 0, 1, 2 ->
                        {
                            isLocked = true;
                        }
                        case 3 -> {
                            createSession();
                        }
                        default -> {
                            if (selection + 1 > MAX_SELECTION)
                            {
                                int sessionIdx = selection - MAX_SELECTION;

                                this.sessions.remove(new ArrayList<>(this.sessions.keySet()).get(sessionIdx));

                                if (sessionIdx >= this.sessions.size())
                                    selection--;
                                
                                if (this.sessions.size() == 0)
                                {
                                    Database.Booking.Session.remove(label, date);
                                    sessions = null;
                                }

                                Database.Booking.save();
                            }
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

    public void handleCalenderAction(String action)
    {
        switch (action)
        {
            case "LEFT" -> {
                this.date = this.date.minusDays(1);
            }
            case "RIGHT" -> {
                this.date = this.date.plusDays(1);
            }
            case "UP" -> {
                this.date = this.date.minusWeeks(1);
            }
            case "DOWN" -> {
                this.date = this.date.plusWeeks(1);
            }
            case "ENTER" -> {
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
            case "ENTER" -> {
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
            case "ENTER" -> {
                this.isLocked = false;
            }
        }
    }
}
