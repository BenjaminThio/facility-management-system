package src.pages;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;

import src.pages.cores.Subpage;
import src.utils.Database;
import src.utils.Global;
import src.utils.Renderer;
import src.utils.Router;
import src.components.Ansi;
import src.components.Calendar;
import src.components.MemoField;
import src.models.Booking;
import src.models.UserBooking;

public class ViewFacilityPage extends Subpage {
    private static final int MAX_SELECTION = 1;
    private static final int MAX_SUBSELECTION = 2;
    private boolean isLocked = false;
    private LocalDate date;
    private LinkedHashMap<String, Booking> sessions;
    private int subselection = 0;
    private MemoField remarkField = new MemoField(4, 50, "Add any special requests (optional)", false);

    @Override
    public void init()
    {
        updateSessions();
    }

    public ViewFacilityPage()
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
            System.out.println("Remark:");
            this.remarkField.render();
            System.out.println();
            System.out.println(new Ansi("Book Facility", this.subselection == 1 ? Ansi.BG_GREEN : Ansi.BG_WHITE, Ansi.FG_BLACK));

            switch (this.subselection)
            {
                case 0 -> {
                    this.remarkField.updateCaret(0, 15 + this.sessions.size());
                }
            }
        }
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

                    switch (this.subselection)
                    {
                        case 0 -> {
                            this.remarkField.handleInput(action);
                        }
                    }
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
            case "SHIFT_TAB" -> {
                if (this.subselection - 1 >= 0)
                {
                    this.subselection--;
                }
                else
                {
                    this.subselection = MAX_SUBSELECTION - 1;
                }
            }
            case "TAB" -> {
                if (this.subselection + 1 < MAX_SUBSELECTION)
                {
                    this.subselection++;
                }
                else
                {
                    this.subselection = 0;
                }
            }
            case "ESC" -> {
                refreshSub();
            }
            case "ENTER" -> {
                switch (this.subselection)
                {
                    // Book a facility
                    case 1 -> {
                        bookFacility();
                    }
                }
            }
        }
    }

    public void bookFacility()
    {
        int sessionIdx = this.selection - MAX_SELECTION;
        String session = new ArrayList<>(sessions.keySet()).get(sessionIdx);

        Database.Booking.Session.addPending(label, date, session, Global.getUser().getEmail(), this.remarkField.getValue());
        Global.getUser().addBooking(new UserBooking(label, date, session));
        Database.Booking.save();
        Database.User.save();

        refreshMain();
    }

    public void refreshSub()
    {
        this.isLocked = false;
        this.subselection = 0;
        this.remarkField = new MemoField(4, 50, "Add any special requests (optional)", false);
    }

    public void refreshMain()
    {
        this.selection = 0;
        refreshSub();
    }
}
