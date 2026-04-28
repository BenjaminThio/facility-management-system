package src.pages;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;

import javax.swing.JOptionPane;

import src.pages.cores.Subpage;
import src.utils.Database;
import src.utils.Global;
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
import src.models.SpecialEmoji;
import src.models.UserBooking;

public class ViewFacilityPage extends Subpage {
    private static final int MAX_SELECTION = 1;
    private static final int MAX_SUBSELECTION = 2;
    private boolean isLocked = false;
    private LocalDate date;
    private LinkedHashMap<String, Booking> sessions;
    private int subselection = 0;
    private MemoField remarkField = new MemoField(4, 50, "Add any special requests (optional)", false);
    
    // --- NEW: Temporary variable for Search Auto-Selection ---
    private String targetSessionToSelect = null;

    @Override
    public Subpage copy()
    {
        ViewFacilityPage clone = new ViewFacilityPage();
        clone.selection = this.selection;
        clone.id = this.id;
        clone.label = this.label;

        clone.isLocked = this.isLocked;
        clone.subselection = this.subselection;
        clone.remarkField = this.remarkField.copy();
        
        // --- ADD THE MISSING DATE COPY HERE ---
        clone.date = this.date;
        // --------------------------------------
        
        clone.targetSessionToSelect = this.targetSessionToSelect;

        return clone;
    }

    public ViewFacilityPage()
    {
        this.date = LocalDate.now();
    }

    public ViewFacilityPage(String facilityName)
    {
        this();
        this.label = facilityName;
    }

    // --- NEW: Overloaded Constructor to catch Search Data ---
    public ViewFacilityPage(String facilityName, LocalDate searchedDate, String searchedSession) {
        this();
        this.label = facilityName;
        this.date = searchedDate; // INSTANTLY shifts the Calendar to the searched date!
        this.targetSessionToSelect = searchedSession; 
    }

    @Override
    public void init()
    {
        updateSessions();
        
        // --- NEW: AUTO-SELECTION LOGIC ---
        if (this.targetSessionToSelect != null && this.sessions != null) {
            int counter = 0;
            for (String sessionStr : this.sessions.keySet()) {
                if (sessionStr.contains(this.targetSessionToSelect)) {
                    // MAX_SELECTION acts as the offset for the UI calendar above the list
                    this.selection = counter + MAX_SELECTION; 
                    break;
                }
                counter++;
            }
            // Clear it out so manual date changes don't force snap the cursor later!
            this.targetSessionToSelect = null; 
        }
        // ---------------------------------
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
        ArrayList<ArrayList<AnsiBuilder>> table = new ArrayList<>();

        Calendar calendar = switch (selection)
        {
            case 0 -> {
                if (isLocked)
                {
                    yield new Calendar(
                        this.date,
                        Ansi.FG_WHITE,
                        Ansi.FG_BLACK,
                        Ansi.BG_LIGHT_BLUE,
                        15
                    ).pastTextColor(Ansi.FG_DARK_GRAY);
                }
                else
                {
                    yield new Calendar(
                        this.date,
                        Ansi.FG_GREEN,
                        Ansi.FG_BLACK,
                        Ansi.BG_GREEN,
                        15
                    ).pastTextColor(new RGB(0, 100, 0));
                }
            }
            default -> new Calendar(
                this.date,
                Ansi.FG_DARK_GRAY,
                Ansi.FG_BLACK,
                Ansi.BG_DARK_GRAY,
                15
            );
        };

        ArrayList<HighlightedDate> highlightedDates = new ArrayList<>();

        if (Database.Booking.getAll().get(label) != null)
        {
            for (String sessionDateString : Database.Booking.getAll().get(label).keySet())
            {
                LocalDate sessionDate = LocalDate.parse(sessionDateString);
                if (sessionDate.getMonthValue() == this.date.getMonthValue())
                {
                    for (String session : Database.Booking.getAll().get(label).get(sessionDateString).keySet())
                    {
                        LocalTime sessionEndTime = LocalTime.parse(session.split("-")[1]);
                        if (Database.Booking.getAll().get(label).get(sessionDateString).get(session).getApproved() == null
                            && !LocalDateTime.of(sessionDate, sessionEndTime).isBefore(LocalDateTime.now())
                        )
                        {
                            highlightedDates.add(new HighlightedDate(sessionDate, Ansi.FG_BLACK, Ansi.BG_LIGHT_CYAN));
                        }
                    }
                }
            }
        }

        calendar.setHighlightDates(highlightedDates);

        table.add(new ArrayList<>(Arrays.asList(new AnsiBuilder("Facility name: " + label))));
        table.add(new ArrayList<>(Arrays.asList(new AnsiBuilder(
            new Ansi("Date: " + this.date.toString() + "\n\n"),
            calendar.toAnsiBuilder(),
            " ".repeat(50)
        ))));

        AnsiBuilder sessionListBuilder = new AnsiBuilder();

        sessionListBuilder.append("Available Sessions:\n");

        if (this.sessions != null && this.sessions.size() > 0)
        {
            int counter = 1;
            for (String session : this.sessions.keySet())
            {
                if (sessions != null && Global.getUser() != null && !containsPendingUser(sessions.get(session).getPending(), Global.getUser().getEmail()) && Database.Booking.Session.getStatus(this.label, this.date.toString(), session, Global.getUser().getEmail()) != Database.Booking.Session.Status.APPROVED && Database.Booking.Session.getStatus(this.label, this.date.toString(), session, Global.getUser().getEmail()) != Database.Booking.Session.Status.UNDER_MAINTENANCE )
                {
                    if (this.selection + 1 > MAX_SELECTION && this.selection + 1 - MAX_SELECTION == counter)
                        sessionListBuilder.append(this.isLocked ? "🔒 " : "> ");
                    else
                        sessionListBuilder.append("  ");

                    if (this.selection + 1 > MAX_SELECTION && this.selection + 1 - MAX_SELECTION == counter)
                        sessionListBuilder.append(new Ansi(counter + ". " + session, Ansi.FG_LIGHT_GREEN, Ansi.UNDERLINE));
                    else
                        sessionListBuilder.append(new Ansi(counter + ". " + session, Ansi.FG_LIGHT_GREEN));

                    if (this.isLocked && this.selection + 1 > MAX_SELECTION && this.selection + 1 - MAX_SELECTION == counter)
                    {
                        sessionListBuilder.append(" ✏️");
                    }
                }
                else
                {
                    if (this.selection + 1 > MAX_SELECTION && this.selection + 1 - MAX_SELECTION == counter)
                        sessionListBuilder.append(this.isLocked ? "🔒 " : "> ");
                    else
                        sessionListBuilder.append("  ");

                    if (sessions != null && Global.getUser() != null)
                    {
                        if (this.selection + 1 > MAX_SELECTION && this.selection + 1 - MAX_SELECTION == counter)
                            sessionListBuilder.append(new Ansi(counter + ". " + session, Ansi.FG_LIGHT_RED, Ansi.UNDERLINE));
                        else
                            sessionListBuilder.append(new Ansi(counter + ". " + session, Ansi.FG_LIGHT_RED));
                    }
                    else
                    {
                        if (this.selection + 1 > MAX_SELECTION && this.selection + 1 - MAX_SELECTION == counter)
                            sessionListBuilder.append(new Ansi(counter + ". " + session, Ansi.FG_WHITE, Ansi.UNDERLINE));
                        else
                            sessionListBuilder.append(new Ansi(counter + ". " + session, Ansi.FG_WHITE));
                    }

                    if (this.isLocked && this.selection + 1 > MAX_SELECTION && this.selection + 1 - MAX_SELECTION == counter)
                    {
                        sessionListBuilder.append(' ').append(SpecialEmoji.EYE);
                    }
                }

                if (counter < this.sessions.keySet().size())
                    sessionListBuilder.append('\n');

                counter++;
            }
        }
        else
        {
            sessionListBuilder.append("NULL");
        }

        table.add(new ArrayList<>(Arrays.asList(sessionListBuilder)));

        if (this.isLocked && this.selection + 1 > MAX_SELECTION)
        {
            AnsiBuilder bookingPanel = new AnsiBuilder();

            if (sessions != null && Global.getUser() != null && !containsPendingUser(sessions.get(getSelectedSession()).getPending(), Global.getUser().getEmail()) && Database.Booking.Session.getStatus(this.label, this.date.toString(), getSelectedSession(), Global.getUser().getEmail()) != Database.Booking.Session.Status.APPROVED && Database.Booking.Session.getStatus(this.label, this.date.toString(), getSelectedSession(), Global.getUser().getEmail()) != Database.Booking.Session.Status.UNDER_MAINTENANCE)
            {
                switch (this.subselection)
                {
                    case 0 -> {
                        this.remarkField.setBackgroundColor(Ansi.BG_GREEN);
                        this.remarkField.setPlaceholderColor(Ansi.FG_BLACK);
                    }
                    default -> {
                        this.remarkField.setBackgroundColor(Ansi.BG_WHITE);
                        this.remarkField.setPlaceholderColor(Ansi.FG_DARK_GRAY);
                    }
                }

                bookingPanel.append("Remark:\n")
                .append(this.remarkField.toAnsiBuilder())
                .append("\n\n" + " ".repeat(18))
                .append(new Ansi(
                    "Book Facility",
                    this.subselection == 1 ? Ansi.BG_GREEN : Ansi.BG_WHITE,
                    Ansi.FG_BLACK
                ));

                table.add(new ArrayList<>(Arrays.asList(bookingPanel)));
            }
            else
            {
                if (sessions != null && Global.getUser() != null)
                {
                    String remark = null;
                    if (Database.Booking.Session.getStatus(this.label, this.date.toString(), getSelectedSession(), Global.getUser().getEmail()) != Database.Booking.Session.Status.APPROVED && Database.Booking.Session.getStatus(this.label, this.date.toString(), getSelectedSession(), Global.getUser().getEmail()) != Database.Booking.Session.Status.UNDER_MAINTENANCE)
                    {
                        remark = getPendingUser(
                            sessions.get(getSelectedSession()).getPending(),
                            Global.getUser().getEmail()
                        ).getRemark();
                    }

                    if (remark != null)
                    {
                        bookingPanel.append("Your Previous Remark:\n");
                        if (!remark.equals(""))
                        {
                            bookingPanel.append(
                                new MemoField(
                                    remark, 4, 50, "", false
                                ).toAnsiBuilder()
                            ).append('\n');
                        }
                        else
                        {
                            bookingPanel.append("NULL\n\n");
                        }
                    }

                    if (Database.Booking.Session.getStatus(this.label, this.date.toString(), getSelectedSession(), Global.getUser().getEmail()) == Database.Booking.Session.Status.APPROVED)
                        bookingPanel.append(" ".repeat(5) + "*You have already booked this facility.*\n")
                        .append(" ".repeat(8) +"*Your booking has been approved.*");
                    else if (Database.Booking.Session.getStatus(this.label, this.date.toString(), getSelectedSession(), Global.getUser().getEmail()) == Database.Booking.Session.Status.UNDER_MAINTENANCE)
                        bookingPanel.append(" ".repeat(5) + "*You have already booked this facility.*\n")
                        .append(" ".repeat(8) + "*Your booking has been approved.*\n")
                        .append("*but the facility is currently under maintenance.*");
                    else
                        bookingPanel.append(" ".repeat(5) + "*You have already booked this facility.*\n")
                        .append(" ".repeat(8) + "*Please wait for admin approval.*");

                    table.add(new ArrayList<>(Arrays.asList(bookingPanel)));
                }
            }
        }

        Table.render(frame, table);
    }

    private BookingInfo getPendingUser(ArrayList<BookingInfo> pendingUsers, String email)
    {
        for (BookingInfo pendingUser : pendingUsers)
        {
            if (pendingUser.getEmail().equals(email)) return pendingUser;
        }

        return null;
    }

    private String getSelectedSession()
    {
        int sessionIdx = this.selection - MAX_SELECTION;
        String session = new ArrayList<>(sessions.keySet()).get(sessionIdx);

        return session;
    }

    private boolean containsPendingUser(ArrayList<BookingInfo> pendingUsers, String email)
    {
        for (BookingInfo pendingUser : pendingUsers)
        {
            if (pendingUser.getEmail().equals(email)) return true;
        }

        return false;
    }

    @Override
    public void updateCaret()
    {
        if (this.isLocked && this.selection + 1 > MAX_SELECTION && sessions != null && Global.getUser() != null && !containsPendingUser(sessions.get(getSelectedSession()).getPending(), Global.getUser().getEmail()) && Database.Booking.Session.getStatus(this.label, this.date.toString(), getSelectedSession(), Global.getUser().getEmail()) != Database.Booking.Session.Status.APPROVED && Database.Booking.Session.getStatus(this.label, this.date.toString(), getSelectedSession(), Global.getUser().getEmail()) != Database.Booking.Session.Status.UNDER_MAINTENANCE)
        {
            switch (this.subselection)
            {
                case 0 -> {
                    Renderer.showInputCaret();
                    this.remarkField.updateCaret(1, 10 + new Calendar(this.date).toString().split("\n").length + this.sessions.size());
                }
                default -> {
                    Renderer.hideInputCaret();
                }
            }
        }
        else
        {
            Renderer.hideInputCaret();
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

    private ArrayList<UserBooking> filterPending(ArrayList<UserBooking> bookings)
    {
        ArrayList<UserBooking> pendingList = new ArrayList<>();
        for (UserBooking booking : bookings)
        {
            if (Database.Booking.Session.getStatus(
                booking.getFacilityName(),
                booking.getDate(),
                booking.getSession(),
                Global.getUser().getEmail()
            ) == Database.Booking.Session.Status.PENDING)
                pendingList.add(booking);
        }
        return pendingList;
    }

    private ArrayList<UserBooking> filterApproved(ArrayList<UserBooking> bookings)
    {
        ArrayList<UserBooking> approvedList = new ArrayList<>();
        for (UserBooking booking : bookings)
        {
            if (Database.Booking.Session.getStatus(
                booking.getFacilityName(),
                booking.getDate(),
                booking.getSession(),
                Global.getUser().getEmail()
            ) == Database.Booking.Session.Status.APPROVED)
                approvedList.add(booking);
        }
        return approvedList;
    }

    public void bookFacility()
    {
        int maxSessionNumber = Global.getMaxSessionNumber();
        int maxApprovedsessionNumber = Global.getMaxApprovedSessionNumber();

        if (maxSessionNumber == -1 || maxApprovedsessionNumber == -1) return;

        if (filterApproved(Database.User.get(Global.getUser().getEmail()).getBookings()).size() + 1 > maxApprovedsessionNumber)
        {
            JOptionPane.showConfirmDialog(
                null, 
                "You have reached the maximum number of approved sessions (" + Integer.toString(maxApprovedsessionNumber) + "). Please wait for an existing session to expire before booking a new one.", 
                "Approved Session Limit Reached", 
                JOptionPane.ERROR_MESSAGE
            );
            return;
        }

        if (
            filterApproved(Database.User.get(Global.getUser().getEmail()).getBookings()).size() + 1 < maxApprovedsessionNumber &&
            filterApproved(Database.User.get(Global.getUser().getEmail()).getBookings()).size() + filterPending(Database.User.get(Global.getUser().getEmail()).getBookings()).size() + 1 > maxSessionNumber
        )
        {
            JOptionPane.showConfirmDialog(
                null, 
                "You have reached the maximum total of " + Integer.toString(maxSessionNumber) + " sessions (approved + pending).\n" + 
                "Please wait for a pending request to be processed or for an approved session to expire before submitting a new request.", 
                "Total Session Limit Reached", 
                JOptionPane.ERROR_MESSAGE
            );
            return;
        }

        int response = JOptionPane.showConfirmDialog(
            null, 
            "Are you sure you want to book this facility?", 
            "Confirmation Dialog", 
            JOptionPane.YES_NO_OPTION, 
            JOptionPane.WARNING_MESSAGE
        );

        if (response == JOptionPane.YES_OPTION)
        {
            String session = getSelectedSession();

            Database.Booking.Session.addPending(label, date, session, Global.getUser().getEmail(), this.remarkField.getValue());
            Global.getUser().addBooking(new UserBooking(label, date, session));

            Database.Booking.save();
            Database.User.save();

            refreshMain();
        }
    }

    public void refreshSub()
    {
        this.remarkField = new MemoField(4, 50, "Add any special requests (optional)", false);
        this.isLocked = false;
        this.subselection = 0;
    }

    public void refreshMain()
    {
        this.selection = 0;
        refreshSub();
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
                    if (sessions != null && Global.getUser() != null && !containsPendingUser(sessions.get(getSelectedSession()).getPending(), Global.getUser().getEmail()) && Database.Booking.Session.getStatus(this.label, this.date.toString(), getSelectedSession(), Global.getUser().getEmail()) != Database.Booking.Session.Status.APPROVED && Database.Booking.Session.getStatus(this.label, this.date.toString(), getSelectedSession(), Global.getUser().getEmail()) != Database.Booking.Session.Status.UNDER_MAINTENANCE )
                    {
                        boolean handledByField = false;
                        boolean actionConsumed = false;
    
                        if (this.subselection == 0)
                        {
                            if (action.equals("UP") || action.equals("DOWN"))
                            {
                                this.remarkField.handleInput(action);
                                handledByField = true;
    
                                if (this.remarkField.isOutOfRange())
                                {
                                    handledByField = false;
                                }
                            }
                        }
    
                        if (!handledByField)
                        {
                            switch (action)
                            {
                                case "DOWN" -> {
                                    if (this.subselection + 1 < MAX_SUBSELECTION)
                                        this.subselection++;
                                    else
                                        this.subselection = 0;
                                    
                                    actionConsumed = true;
                                }
                                case "UP" -> {
                                    if (this.subselection - 1 >= 0)
                                        this.subselection--;
                                    else
                                        this.subselection = MAX_SUBSELECTION - 1;
                                    
                                    actionConsumed = true;
                                }
                                case "ESC" -> {
                                    refreshSub();
                                    actionConsumed = true;
                                }
                                case "ENTER" -> {
                                    if (this.subselection == 1)
                                    {
                                        bookFacility();
                                        actionConsumed = true;
                                    }
                                }
                            }
                        }
    
                        if (!actionConsumed && !action.equals("UP") && !action.equals("DOWN"))
                        {
                            if (this.subselection == 0)
                            {
                                this.remarkField.handleInput(action);
                            }
                        }
                    }
                    else
                    {
                        switch (action)
                        {
                            case "ESC" -> {
                                this.isLocked = false;
                            }
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
                    if (selection == 0 || sessions != null && Global.getUser() != null)
                        this.isLocked = true;
                }
                case "ESC" -> {
                    Router.back();
                }
            }
        }

        Renderer.refresh();
    }
}

/*
package src.pages;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;

import javax.swing.JOptionPane;

import src.pages.cores.Subpage;
import src.utils.Database;
import src.utils.Global;
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
import src.models.SpecialEmoji;
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
    public Subpage copy()
    {
        ViewFacilityPage clone = new ViewFacilityPage();

        clone.selection = this.selection;
        clone.id = this.id;
        clone.label = this.label;

        clone.isLocked = this.isLocked;
        clone.subselection = this.subselection;
        clone.remarkField = this.remarkField.copy();

        return clone;
    }

    public ViewFacilityPage(String facilityName)
    {
        this();
        this.label = facilityName;
    }

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
    public void render(StringBuilder frame)
    {
        ArrayList<ArrayList<AnsiBuilder>> table = new ArrayList<>();

        Calendar calendar = switch (selection)
        {
            case 0 -> {
                if (isLocked)
                {
                    yield new Calendar(
                        this.date,
                        Ansi.FG_WHITE,
                        Ansi.FG_BLACK,
                        Ansi.BG_LIGHT_BLUE,
                        15
                    ).pastTextColor(Ansi.FG_DARK_GRAY);
                }
                else
                {
                    yield new Calendar(
                        this.date,
                        Ansi.FG_GREEN,
                        Ansi.FG_BLACK,
                        Ansi.BG_GREEN,
                        15
                    ).pastTextColor(new RGB(0, 100, 0));
                }
            }
            default -> new Calendar(
                this.date,
                Ansi.FG_DARK_GRAY,
                Ansi.FG_BLACK,
                Ansi.BG_DARK_GRAY,
                15
            );
        };

        ArrayList<HighlightedDate> highlightedDates = new ArrayList<>();

        if (Database.Booking.getAll().get(label) != null)
        {
            for (String sessionDateString : Database.Booking.getAll().get(label).keySet())
            {
                LocalDate sessionDate = LocalDate.parse(sessionDateString);

                if (sessionDate.getMonthValue() == this.date.getMonthValue())
                {
                    for (String session : Database.Booking.getAll().get(label).get(sessionDateString).keySet())
                    {
                        LocalTime sessionEndTime = LocalTime.parse(session.split("-")[1]);
                        if (Database.Booking.getAll().get(label).get(sessionDateString).get(session).getApproved() == null
                            && !LocalDateTime.of(sessionDate, sessionEndTime).isBefore(LocalDateTime.now())
                        )
                        {
                            highlightedDates.add(new HighlightedDate(sessionDate, Ansi.FG_BLACK, Ansi.BG_LIGHT_CYAN));
                        }
                    }
                }
            }
        }

        calendar.setHighlightDates(highlightedDates);

        table.add(new ArrayList<>(Arrays.asList(new AnsiBuilder("Facility name: " + label))));
        table.add(new ArrayList<>(Arrays.asList(new AnsiBuilder(
            new Ansi("Date: " + this.date.toString() + "\n\n"),
            calendar.toAnsiBuilder(),
            " ".repeat(50)
        ))));

        AnsiBuilder sessionListBuilder = new AnsiBuilder();

        sessionListBuilder.append("Available Sessions:\n");

        if (this.sessions != null && this.sessions.size() > 0)
        {
            int counter = 1;

            for (String session : this.sessions.keySet())
            {
                if (sessions != null && Global.getUser() != null && !containsPendingUser(sessions.get(session).getPending(), Global.getUser().getEmail()) &&
                    Database.Booking.Session.getStatus(this.label, this.date.toString(), session, Global.getUser().getEmail()) != Database.Booking.Session.Status.APPROVED &&
                    Database.Booking.Session.getStatus(this.label, this.date.toString(), session, Global.getUser().getEmail()) != Database.Booking.Session.Status.UNDER_MAINTENANCE
                )
                {
                    if (this.selection + 1 > MAX_SELECTION && this.selection + 1 - MAX_SELECTION == counter)
                        sessionListBuilder.append(this.isLocked ? "🔒 " : "> ");
                    else
                        sessionListBuilder.append("  ");

                    
                    if (this.selection + 1 > MAX_SELECTION && this.selection + 1 - MAX_SELECTION == counter)
                        sessionListBuilder.append(new Ansi(counter + ". " + session, Ansi.FG_LIGHT_GREEN, Ansi.UNDERLINE));
                    else
                        sessionListBuilder.append(new Ansi(counter + ". " + session, Ansi.FG_LIGHT_GREEN));

                    if (this.isLocked && this.selection + 1 > MAX_SELECTION && this.selection + 1 - MAX_SELECTION == counter)
                    {
                        sessionListBuilder.append(" ✏️");
                    }
                }
                else
                {
                    if (this.selection + 1 > MAX_SELECTION && this.selection + 1 - MAX_SELECTION == counter)
                        sessionListBuilder.append(this.isLocked ? "🔒 " : "> ");
                    else
                        sessionListBuilder.append("  ");

                    if (sessions != null && Global.getUser() != null)
                    {
                        if (this.selection + 1 > MAX_SELECTION && this.selection + 1 - MAX_SELECTION == counter)
                            sessionListBuilder.append(new Ansi(counter + ". " + session, Ansi.FG_LIGHT_RED, Ansi.UNDERLINE));
                        else
                            sessionListBuilder.append(new Ansi(counter + ". " + session, Ansi.FG_LIGHT_RED));
                    }
                    else
                    {
                        if (this.selection + 1 > MAX_SELECTION && this.selection + 1 - MAX_SELECTION == counter)
                            sessionListBuilder.append(new Ansi(counter + ". " + session, Ansi.FG_WHITE, Ansi.UNDERLINE));
                        else
                            sessionListBuilder.append(new Ansi(counter + ". " + session, Ansi.FG_WHITE));
                    }

                    if (this.isLocked && this.selection + 1 > MAX_SELECTION && this.selection + 1 - MAX_SELECTION == counter)
                    {
                        sessionListBuilder.append(' ').append(SpecialEmoji.EYE);
                    }
                }

                if (counter < this.sessions.keySet().size())
                    sessionListBuilder.append('\n');

                counter++;
            }
        }
        else
        {
            sessionListBuilder.append("NULL");
        }

        table.add(new ArrayList<>(Arrays.asList(sessionListBuilder)));

        if (this.isLocked && this.selection + 1 > MAX_SELECTION)
        {
            AnsiBuilder bookingPanel = new AnsiBuilder();

            if (sessions != null && Global.getUser() != null && !containsPendingUser(sessions.get(getSelectedSession()).getPending(), Global.getUser().getEmail()) &&
                Database.Booking.Session.getStatus(this.label, this.date.toString(), getSelectedSession(), Global.getUser().getEmail()) != Database.Booking.Session.Status.APPROVED &&
                Database.Booking.Session.getStatus(this.label, this.date.toString(), getSelectedSession(), Global.getUser().getEmail()) != Database.Booking.Session.Status.UNDER_MAINTENANCE)
            {
                switch (this.subselection)
                {
                    case 0 -> {
                        this.remarkField.setBackgroundColor(Ansi.BG_GREEN);
                        this.remarkField.setPlaceholderColor(Ansi.FG_BLACK);
                    }
                    default -> {
                        this.remarkField.setBackgroundColor(Ansi.BG_WHITE);
                        this.remarkField.setPlaceholderColor(Ansi.FG_DARK_GRAY);
                    }
                }

                bookingPanel.append("Remark:\n")
                            .append(this.remarkField.toAnsiBuilder())
                            .append("\n\n" + " ".repeat(18))
                            .append(new Ansi(
                                "Book Facility",
                                this.subselection == 1
                                ?
                                Ansi.BG_GREEN
                                :
                                Ansi.BG_WHITE,
                                Ansi.FG_BLACK
                            ));
                table.add(new ArrayList<>(Arrays.asList(bookingPanel)));
            }
            else
            {
                if (sessions != null && Global.getUser() != null)
                {
                    String remark = null;

                    if (Database.Booking.Session.getStatus(this.label, this.date.toString(), getSelectedSession(), Global.getUser().getEmail()) != Database.Booking.Session.Status.APPROVED &&
                        Database.Booking.Session.getStatus(this.label, this.date.toString(), getSelectedSession(), Global.getUser().getEmail()) != Database.Booking.Session.Status.UNDER_MAINTENANCE)
                    {
                        remark = getPendingUser(
                                    sessions.get(getSelectedSession()).getPending(),
                                    Global.getUser().getEmail()
                                ).getRemark();
                    }

                    if (remark != null)
                    {
                        bookingPanel.append("Your Previous Remark:\n");

                        if (!remark.equals(""))
                        {
                            bookingPanel.append(
                                new MemoField(
                                    remark,
                                    4,
                                    50,
                                    "",
                                    false
                                ).toAnsiBuilder()
                            ).append('\n');
                        }
                        else
                        {
                            bookingPanel.append("NULL\n\n");
                        }
                    }

                    if (Database.Booking.Session.getStatus(this.label, this.date.toString(), getSelectedSession(), Global.getUser().getEmail()) == Database.Booking.Session.Status.APPROVED)
                        bookingPanel.append(" ".repeat(5) + "*You have already booked this facility.*\n")
                                    .append(" ".repeat(8) +"*Your booking has been approved.*");
                    else if (Database.Booking.Session.getStatus(this.label, this.date.toString(), getSelectedSession(), Global.getUser().getEmail()) == Database.Booking.Session.Status.UNDER_MAINTENANCE)
                        bookingPanel.append(" ".repeat(5) + "*You have already booked this facility.*\n")
                                    .append(" ".repeat(8) + "*Your booking has been approved.*\n")
                                    .append("*but the facility is currently under maintenance.*");
                    else
                        bookingPanel.append(" ".repeat(5) + "*You have already booked this facility.*\n")
                                    .append(" ".repeat(8) + "*Please wait for admin approval.*");

                    table.add(new ArrayList<>(Arrays.asList(bookingPanel)));
                }
            }
        }

        Table.render(frame, table);
    }

    private BookingInfo getPendingUser(ArrayList<BookingInfo> pendingUsers, String email)
    {
        for (BookingInfo pendingUser : pendingUsers)
        {
            if (pendingUser.getEmail().equals(email))
                return pendingUser;
        }
        return null;
    }

    private String getSelectedSession()
    {
        int sessionIdx = this.selection - MAX_SELECTION;
        String session = new ArrayList<>(sessions.keySet()).get(sessionIdx);

        return session;
    }

    private boolean containsPendingUser(ArrayList<BookingInfo> pendingUsers, String email)
    {
        for (BookingInfo pendingUser : pendingUsers)
        {
            if (pendingUser.getEmail().equals(email))
                return true;
        }
        return false;
    }

    @Override
    public void updateCaret()
    {
        if (this.isLocked && this.selection + 1 > MAX_SELECTION &&
            sessions != null && Global.getUser() != null && !containsPendingUser(sessions.get(getSelectedSession()).getPending(), Global.getUser().getEmail()) &&
            Database.Booking.Session.getStatus(this.label, this.date.toString(), getSelectedSession(), Global.getUser().getEmail()) != Database.Booking.Session.Status.APPROVED &&
            Database.Booking.Session.getStatus(this.label, this.date.toString(), getSelectedSession(), Global.getUser().getEmail()) != Database.Booking.Session.Status.UNDER_MAINTENANCE)
        {
            switch (this.subselection)
            {
                case 0 -> {
                    Renderer.showInputCaret();
                    this.remarkField.updateCaret(1, 10 + new Calendar(this.date).toString().split("\n").length + this.sessions.size());
                }
                default -> {
                    Renderer.hideInputCaret();
                }
            }
        }
        else
        {
            Renderer.hideInputCaret();
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

    private ArrayList<UserBooking> filterPending(ArrayList<UserBooking> bookings)
    {
        ArrayList<UserBooking> pendingList = new ArrayList<>();

        for (UserBooking booking : bookings)
        {
            if (Database.Booking.Session.getStatus(
                booking.getFacilityName(),
                booking.getDate(),
                booking.getSession(),
                Global.getUser().getEmail()
            ) == Database.Booking.Session.Status.PENDING)
                pendingList.add(booking);
        }

        return pendingList;
    }

    private ArrayList<UserBooking> filterApproved(ArrayList<UserBooking> bookings)
    {
        ArrayList<UserBooking> approvedList = new ArrayList<>();

        for (UserBooking booking : bookings)
        {
            if (Database.Booking.Session.getStatus(
                booking.getFacilityName(),
                booking.getDate(),
                booking.getSession(),
                Global.getUser().getEmail()
            ) == Database.Booking.Session.Status.APPROVED)
                approvedList.add(booking);
        }

        return approvedList;
    }

    public void bookFacility()
    {
        int maxSessionNumber = Global.getMaxSessionNumber();
        int maxApprovedsessionNumber = Global.getMaxApprovedSessionNumber();

        if (maxSessionNumber == -1 || maxApprovedsessionNumber == -1)
            return;

        if (filterApproved(Database.User.get(Global.getUser().getEmail()).getBookings()).size() + 1 > maxApprovedsessionNumber)
        {
            JOptionPane.showConfirmDialog(
                null,
                "You have reached the maximum number of approved sessions (" + Integer.toString(maxApprovedsessionNumber) + "). Please wait for an existing session to expire before booking a new one.",
                "Approved Session Limit Reached",
                JOptionPane.ERROR_MESSAGE
            );
            return;
        }
        if (
            filterApproved(Database.User.get(Global.getUser().getEmail()).getBookings()).size() + 1 < maxApprovedsessionNumber &&
            filterApproved(Database.User.get(Global.getUser().getEmail()).getBookings()).size() +
            filterPending(Database.User.get(Global.getUser().getEmail()).getBookings()).size() + 1 > maxSessionNumber
        )
        {
            JOptionPane.showConfirmDialog(
                null,
                "You have reached the maximum total of " + Integer.toString(maxSessionNumber) + " sessions (approved + pending).\n" +
                "Please wait for a pending request to be processed or for an approved session to expire before submitting a new request.",
                "Total Session Limit Reached",
                JOptionPane.ERROR_MESSAGE
            );
            return;
        }
    
        int response = JOptionPane.showConfirmDialog(
            null,
            "Are you sure you want to book this facility?",
            "Confirmation Dialog",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE
        );

        if (response == JOptionPane.YES_OPTION)
        {
            String session = getSelectedSession();

            Database.Booking.Session.addPending(label, date, session, Global.getUser().getEmail(), this.remarkField.getValue());
            Global.getUser().addBooking(new UserBooking(label, date, session));
            Database.Booking.save();
            Database.User.save();
            refreshMain();
        }
    }

    public void refreshSub()
    {
        this.remarkField = new MemoField(4, 50, "Add any special requests (optional)", false);
        this.isLocked = false;
        this.subselection = 0;
    }

    public void refreshMain()
    {
        this.selection = 0;
        refreshSub();
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
                    if (sessions != null && Global.getUser() != null && !containsPendingUser(sessions.get(getSelectedSession()).getPending(), Global.getUser().getEmail()) &&
                        Database.Booking.Session.getStatus(this.label, this.date.toString(), getSelectedSession(), Global.getUser().getEmail()) != Database.Booking.Session.Status.APPROVED &&
                        Database.Booking.Session.getStatus(this.label, this.date.toString(), getSelectedSession(), Global.getUser().getEmail()) != Database.Booking.Session.Status.UNDER_MAINTENANCE
                    )
                    {
                        boolean handledByField = false;
                        boolean actionConsumed = false;
                        
                        if (this.subselection == 0) {
                            if (action.equals("UP") || action.equals("DOWN")) {
                                this.remarkField.handleInput(action);
                                handledByField = true;

                                if (this.remarkField.isOutOfRange()) {
                                    handledByField = false;
                                }
                            }
                        }

                        if (!handledByField) {
                            switch (action)
                            {
                                case "DOWN" -> {
                                    if (this.subselection + 1 < MAX_SUBSELECTION) this.subselection++;
                                    else this.subselection = 0;
                                    actionConsumed = true;
                                }
                                case "UP" -> {
                                    if (this.subselection - 1 >= 0) this.subselection--;
                                    else this.subselection = MAX_SUBSELECTION - 1;
                                    actionConsumed = true;
                                }
                                case "ESC" -> {
                                    refreshSub();
                                    actionConsumed = true;
                                }
                                case "ENTER" -> {
                                    if (this.subselection == 1) {
                                        bookFacility();
                                        actionConsumed = true;
                                    }
                                }
                            }
                        }

                        if (!actionConsumed && !action.equals("UP") && !action.equals("DOWN")) {
                            if (this.subselection == 0) {
                                this.remarkField.handleInput(action);
                            }
                        }
                    }
                    else
                    {
                        switch (action)
                        {
                            case "ESC" -> {
                                this.isLocked = false;
                            }
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
                    if (selection == 0 || sessions != null && Global.getUser() != null)
                        this.isLocked = true;
                }
                case "ESC" -> {
                    Router.back();
                }
            }
        }

        Renderer.refresh();
    }
}
*/

    /*
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
                    if (sessions != null && Global.getUser() != null && !containsPendingUser(sessions.get(getSelectedSession()).getPending(), Global.getUser().getEmail()) &&
                        Database.Booking.Session.getStatus(this.label, this.date.toString(), getSelectedSession(), Global.getUser().getEmail()) != Database.Booking.Session.Status.APPROVED &&
                        Database.Booking.Session.getStatus(this.label, this.date.toString(), getSelectedSession(), Global.getUser().getEmail()) != Database.Booking.Session.Status.UNDER_MAINTENANCE
                    )
                    {
                        switch (this.subselection)
                        {
                            case 0 -> {
                                this.remarkField.handleInput(action);
                            }
                        }

                        handleSessionAction(action);
                    }
                    else
                    {
                        switch (action)
                        {
                            case "ESC" -> {
                                this.isLocked = false;
                            }
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
                    if (selection == 0 || sessions != null && Global.getUser() != null)
                        this.isLocked = true;
                }
                case "ESC" -> {
                    Router.back();
                }
            }
        }

        Renderer.refresh();
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
    */
