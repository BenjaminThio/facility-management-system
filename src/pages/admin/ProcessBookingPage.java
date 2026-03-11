package src.pages.admin;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;

import src.pages.cores.Subpage;
import src.utils.Database;
import src.utils.Renderer;
import src.utils.Router;
import src.components.Ansi;
import src.components.Calendar;
import src.models.Booking;
import src.models.BookingInfo;

public class ProcessBookingPage extends Subpage {
    private static final int MAX_SELECTION = 1;
    private static final int VISIBLE_SUBSELECTION_NUMBER = 10;
    private boolean isLocked = false;
    private LocalDate date;
    private LinkedHashMap<String, Booking> sessions;
    
    private boolean isSublocked = false;
    private int subselection = 0;
    private int subselectionOffset = 0;
    private String[] list;
    // private String[] searchResult;

    @Override
    public void init()
    {
        updateSessions();
    }

    public ProcessBookingPage()
    {
        this.date = LocalDate.now();
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
        System.out.println("Available Sessions:");

        if (this.sessions != null && this.sessions.size() > 0)
        {
            int counter = 1;

            for (String session : this.sessions.keySet())
            {
                if (this.selection + 1 > MAX_SELECTION && this.selection + 1 - MAX_SELECTION == counter)
                    System.out.print("> ");
                else
                    System.out.print("  ");

                System.out.print(new Ansi(counter + ". " + session, this.isLocked && this.selection + 1 > MAX_SELECTION && this.selection + 1 - MAX_SELECTION == counter ? Ansi.BG_GREEN : Ansi.FG_WHITE));

                if (this.isLocked && this.selection + 1 > MAX_SELECTION && this.selection + 1 - MAX_SELECTION == counter)
                    System.out.print(" <");

                System.out.println();

                counter++;
            }
        }
        else
        {
            System.out.println("NULL");
        }

        System.out.println();

        if (this.isLocked && this.selection + 1 > MAX_SELECTION)
        {
            for (int i = this.subselectionOffset; i < this.subselectionOffset + (this.list.length < VISIBLE_SUBSELECTION_NUMBER ? this.list.length : VISIBLE_SUBSELECTION_NUMBER); i++)
            {
                System.out.println("Pending Requests:");

                if (selection + this.subselectionOffset - 1 == i)
                {
                    System.out.print("> ");
                }
                else
                {
                    System.out.print("  ");
                }
                System.out.print(new Ansi(Integer.toString(i + 1) + ". " + this.list[i], this.isSublocked && this.selection + this.subselectionOffset - 1 == i ? Ansi.BG_GREEN : Ansi.FG_WHITE));

                if (this.isSublocked && this.selection + this.subselectionOffset - 1 == i)
                    System.out.print(" <");

                System.out.println();
            }
        }

        if (this.list != null && this.list.length > 0 && this.isSublocked)
        {
            int sessionIdx = this.selection - MAX_SELECTION;
            String session = new ArrayList<>(sessions.keySet()).get(sessionIdx);
            BookingInfo bookingInfo = sessions.get(session).getPending().get(this.subselection + this.subselectionOffset);

            System.out.println();
            System.out.println("Remark:");
            System.out.println(bookingInfo.getRemark());
            System.out.println();
            System.out.println(new Ansi("Approve", Ansi.BG_GREEN));
        }
    }

    public void approve()
    {
        int sessionIdx = this.selection - MAX_SELECTION;
        String session = new ArrayList<>(sessions.keySet()).get(sessionIdx);
        BookingInfo bookingInfo = sessions.get(session).getPending().get(this.subselection + this.subselectionOffset);

        sessions.get(session).setApproved(bookingInfo.getEmail());
        Database.Booking.save();
        isSublocked = false;
    }

    @Override
    public void handleAction(String action)
    {
        if (this.isLocked)
        {
            switch (this.selection)
            {
                case 0 -> {
                    handleCalenderAction(action);
                }
                default -> {
                    handleSessionAction(action);
                }
            }
        }
        else
        {
            switch (action)
            {
                case "DOWN" -> {
                    if (this.selection + 1 < MAX_SELECTION + getSessionsSize())
                    {
                        this.selection++;
                    }
                    else
                    {
                        this.selection = 0;
                    }
                }
                case "UP" -> {
                    if (this.selection - 1 >= 0)
                    {
                        this.selection--;
                    }
                    else
                    {
                        this.selection = MAX_SELECTION + getSessionsSize() - 1;
                    }
                }
                case "ENTER" -> {
                    this.isLocked = true;
                    switch (this.selection)
                    {
                        case 0 -> {}
                        default -> {
                            int sessionIdx = this.selection - MAX_SELECTION;
                            String session = new ArrayList<>(sessions.keySet()).get(sessionIdx);

                            if (sessions.get(session) != null)
                            {
                                this.list = sessions.get(session).getPending().stream().map(bookingInfo -> bookingInfo.getEmail()).toArray(String[]::new);
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

    public void handleSessionAction(String action)
    {
        switch (action)
        {
            case "UP" -> {
                if (this.subselection - 1 >= 0)
                {
                    this.subselection--;
                }
                else if (this.subselectionOffset - 1 >= 0)
                {
                    this.subselectionOffset--;
                }
            }
            case "DOWN" -> {
                if (this.subselection + 1 < (this.list.length < VISIBLE_SUBSELECTION_NUMBER ? this.list.length : VISIBLE_SUBSELECTION_NUMBER))
                {
                    this.subselection++;
                }
                else if (this.subselection + this.subselectionOffset + 1 < this.list.length)
                {
                    this.subselectionOffset++;
                }
            }
            case "ESC" -> {
                if (isSublocked)
                    this.isSublocked = false;
                else
                    this.isLocked = false;
            }
            case "ENTER" -> {
                if (!this.isSublocked)
                    isSublocked = true;
                else
                    approve();
            }
        }
    }

    public void refreshSub()
    {
        this.isLocked = false;
        this.subselection = 0;
    }

    public void refreshMain()
    {
        this.selection = 0;
        refreshSub();
    }
}
