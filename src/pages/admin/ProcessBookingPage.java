package src.pages.admin;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

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
import src.models.SpecialEmoji;
import src.models.UserBooking;

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
    public Subpage copy()
    {
        ProcessBookingPage clone = new ProcessBookingPage();

        clone.selection = this.selection;
        clone.id = this.id;
        clone.label = this.label;
        clone.isLocked = this.isLocked;
        clone.date = this.date;
        clone.isSublocked = this.isSublocked;
        clone.subselection = this.subselection;
        clone.subselectionOffset = this.subselectionOffset;

        if (this.list != null)
            clone.list = Arrays.copyOf(this.list, this.list.length);

        if (this.sessions != null)
        {
            clone.sessions = new LinkedHashMap<>();

            for (Map.Entry<String, Booking> entry : this.sessions.entrySet())
            {
                clone.sessions.put(entry.getKey(), entry.getValue().copy());
            }
        }

        return clone;
    }

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

    public final LinkedHashMap<String, Booking> getPendingSessions()
    {
        if (sessions == null)
            return null;

        LinkedHashMap<String, Booking> pendingSessions = new LinkedHashMap<>();

        for (String session : this.sessions.keySet())
        {
            if (sessions.get(session).getPending().isEmpty())
                continue;

            pendingSessions.put(session, sessions.get(session));
        }

        return pendingSessions;
    }

    public int getPendingSessionSize()
    {
        if (sessions == null)
            return 0;

        int counter = 0;

        for (String session : this.sessions.keySet())
        {
            if (sessions.get(session).getPending().isEmpty())
                continue;

            counter++;
        }

        return counter;
    }

    @Override
    public void render(StringBuilder frame)
    {
        ArrayList<ArrayList<AnsiBuilder>> tableBuilder = new ArrayList<>();
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
                        10
                    ).pastTextColor(Ansi.FG_DARK_GRAY);
                }
                else
                {
                    yield new Calendar(
                        this.date,
                        Ansi.FG_GREEN,
                        Ansi.FG_BLACK,
                        Ansi.BG_GREEN,
                        10
                    ).pastTextColor(new RGB(0, 100, 0));
                }
            }
            default -> new Calendar(
                this.date,
                Ansi.FG_DARK_GRAY,
                Ansi.FG_BLACK,
                Ansi.BG_DARK_GRAY,
                10
            );
        };

        ArrayList<HighlightedDate> highlightedDates = new ArrayList<>();

        for (String sessionDateString : Database.Booking.getAll().get(label).keySet())
        {
            LocalDate sessionDate = LocalDate.parse(sessionDateString);

            if (sessionDate.getMonthValue() == this.date.getMonthValue())
            {
                for (String session : Database.Booking.getAll().get(label).get(sessionDateString).keySet())
                {
                    if (Database.Booking.getAll().get(label).get(sessionDateString).get(session).getPending().size() > 0)
                    {
                        highlightedDates.add(new HighlightedDate(sessionDate, Ansi.FG_BLACK, Ansi.BG_LIGHT_RED));
                    }
                }
            }
        }

        calendar.setHighlightDates(highlightedDates);

        tableBuilder.add(new ArrayList<>(Arrays.asList(new AnsiBuilder("Facility name: " + label))));
        tableBuilder.add(new ArrayList<>(Arrays.asList(new AnsiBuilder(
            new Ansi("Date: " + this.date.toString() + "\n\n"),
            calendar.toAnsiBuilder(),
            new Ansi(" ".repeat(40))
        ))));

        final LinkedHashMap<String, Booking> pendingSessions = getPendingSessions();
        AnsiBuilder sessionListBuilder = new AnsiBuilder();

        if (pendingSessions != null && pendingSessions.size() > 0)
        {
            int counter = 1;

            for (String session : pendingSessions.keySet())
            {
                if (this.selection + 1 > MAX_SELECTION && this.selection + 1 - MAX_SELECTION == counter)
                    sessionListBuilder.append("> ");
                else
                    sessionListBuilder.append("  ");

                sessionListBuilder.append(new Ansi(counter + ". " + session, this.isLocked && this.selection + 1 > MAX_SELECTION && this.selection + 1 - MAX_SELECTION == counter ? Ansi.BG_GREEN : Ansi.FG_WHITE));

                if (this.isLocked && this.selection + 1 > MAX_SELECTION && this.selection + 1 - MAX_SELECTION == counter)
                    sessionListBuilder.append(' ').append(SpecialEmoji.EYE);

                if (counter < pendingSessions.keySet().size())
                    sessionListBuilder.append('\n');

                counter++;
            }
        }
        else
        {
            sessionListBuilder.append("NULL");
        }

        ArrayList<ArrayList<AnsiBuilder>> subtable = new ArrayList<>();
        AnsiBuilder requestListBuilder = new AnsiBuilder("Pending Requests:" + " ".repeat(20) + '\n');

        if (this.isLocked && this.selection + 1 > MAX_SELECTION)
        {
            for (int i = this.subselectionOffset; i < this.subselectionOffset + (this.list.length < VISIBLE_SUBSELECTION_NUMBER ? this.list.length : VISIBLE_SUBSELECTION_NUMBER); i++)
            {
                if (subselection + this.subselectionOffset == i)
                {
                    requestListBuilder.append("> ");
                }
                else
                {
                    requestListBuilder.append("  ");
                }
                requestListBuilder.append(new Ansi(Integer.toString(i + 1) + ". " + this.list[i], this.isSublocked && this.subselection + this.subselectionOffset == i ? Ansi.BG_GREEN : Ansi.FG_WHITE));

                if (this.isSublocked && this.subselection + this.subselectionOffset == i)
                    requestListBuilder.append(' ').append(SpecialEmoji.EYE);

                if (i + 1 < (this.list.length < VISIBLE_SUBSELECTION_NUMBER ? this.list.length : VISIBLE_SUBSELECTION_NUMBER))
                    requestListBuilder.append('\n');
            }

            if (requestListBuilder.size() <= 1)
                requestListBuilder.append("NULL");

            subtable.add(new ArrayList<>(Arrays.asList(requestListBuilder)));
        }

        AnsiBuilder processPanelBuilder = new AnsiBuilder();

        if (this.list != null && this.list.length > 0 && this.isSublocked)
        {
            int sessionIdx = this.selection - MAX_SELECTION;
            String session = new ArrayList<>(pendingSessions.keySet()).get(sessionIdx);
            BookingInfo bookingInfo = pendingSessions.get(session).getPending().get(this.subselection + this.subselectionOffset);
            
            if (bookingInfo.getRemark() != null)
            {
                MemoField remarkField = new MemoField(bookingInfo.getRemark(), 4, 50, "", false);

                processPanelBuilder.append("Remark:\n")
                                   .append(remarkField.toAnsiBuilder());
            }
            else
            {
                processPanelBuilder.append("Remark:" + " ".repeat(43) + '\n')
                                   .append("NULL\n");
            }

            processPanelBuilder.append('\n')
                               .append(" ".repeat(21))
                               .append(new Ansi("Approve", Ansi.BG_GREEN));
            subtable.add(new ArrayList<>(Arrays.asList(processPanelBuilder)));
        }

        AnsiBuilder subPanelBuilder = new AnsiBuilder(
            new Ansi("Available Sessions:\n"),
            sessionListBuilder
        );

        if (subtable.size() > 0)
            subPanelBuilder.append(new Ansi('\n')).append(Table.toAnsiBuilder(subtable));

        tableBuilder.add(new ArrayList<>(Arrays.asList(subPanelBuilder)));

        Table.render(frame, tableBuilder);
    }

    private ArrayList<UserBooking> filterApproved(ArrayList<UserBooking> bookings, String email)
    {
        ArrayList<UserBooking> approvedList = new ArrayList<>();

        for (UserBooking booking : bookings)
        {
            if (Database.Booking.Session.getStatus(
                booking.getFacilityName(),
                booking.getDate(),
                booking.getSession(),
                email
            ) == Database.Booking.Session.Status.APPROVED)
                approvedList.add(booking);
        }

        return approvedList;
    }

    public BookingInfo getSelectedBookingInfo()
    {
        return getPendingSessions().get(getSelectedSession()).getPending().get(this.subselection + this.subselectionOffset);
    }

    public String getSelectedSession()
    {
        int sessionIdx = this.selection - MAX_SELECTION;
        String session = new ArrayList<>(getPendingSessions().keySet()).get(sessionIdx);

        return session;
    }

    public void approve()
    {
        int sessionIdx = this.selection - MAX_SELECTION;
        String selectedEmail = getSelectedBookingInfo().getEmail();

        int response = JOptionPane.showConfirmDialog(
            null,
            "Are you sure you want to approve this student?\n" +
            "This action cannot be undone. Other students' booking requests for this session will be discarded.\n" +
            (filterApproved(Database.User.get(getSelectedBookingInfo().getEmail()).getBookings(), selectedEmail).size() + 2 > 5
            ? "This session will the 5th approved sessions which is the max number approved sessions that this student can own.\n" +
              "the other the pending sessions from this student will be discarded."
            : ""),
            "Approve warning.",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE
        );

        if (response == JOptionPane.YES_OPTION && filterApproved(Database.User.get(selectedEmail).getBookings(), selectedEmail).size() + 1 <= 5)
        {
            getPendingSessions().get(getSelectedSession()).setApproved(selectedEmail);
            getPendingSessions().get(getSelectedSession()).getPending().clear();

            if (filterApproved(Database.User.get(selectedEmail).getBookings(), selectedEmail).size() + 1 > 5)
            {
                for (UserBooking userBooking : Database.User.get(selectedEmail).getBookings())
                {
                    var facilityData = Database.Booking.getAll().get(userBooking.getFacilityName());
                    if (facilityData != null && facilityData.get(userBooking.getDate()) != null)
                    {
                        src.models.Booking sessionData = Database.Booking.getAll().get(userBooking.getFacilityName()).get(userBooking.getDate()).get(userBooking.getSession());

                        if (sessionData != null)
                        {
                            ArrayList<BookingInfo> userBookingInfoList = sessionData.getPending();

                            for (int i = 0; i < Database.Booking.getAll().get(userBooking.getFacilityName()).get(userBooking.getDate()).get(userBooking.getSession()).getPending().size(); i++)
                            {
                                if (userBookingInfoList.get(i).getEmail().equals(selectedEmail))
                                {
                                    userBookingInfoList.remove(i);
                                    i--;
                                }
                            }
                        }
                    }
                }
            }

            Database.Booking.save();

            src.models.User student = Database.User.get(selectedEmail);
            if (student != null) {
                String facilityName = this.label; 
                String bookingDate = this.date.toString(); 
                
                // Grab the currently logged-in Admin's name!
                String currentAdminName = src.utils.Global.getUser().getName();

                student.addNotification(new src.models.Notification(
                    "[APPROVED] Your booking for " + facilityName + " on " + bookingDate + " has been approved.", 
                    currentAdminName + " (Admin)"
                ));
                Database.User.save();
            }

            isSublocked = false;
            isLocked = false;
            subselection = 0;

            if (sessionIdx + 1 > getPendingSessions().size())
            {
                this.selection--;
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
                }
            }
        }
        else
        {
            switch (action)
            {
                case "DOWN" -> {
                    if (this.selection + 1 < MAX_SELECTION + getPendingSessionSize())
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
                        this.selection = MAX_SELECTION + getPendingSessionSize() - 1;
                    }
                }
                case "ENTER" -> {
                    this.isLocked = true;
                    switch (this.selection)
                    {
                        case 0 -> {}
                        default -> {
                            final LinkedHashMap<String, Booking> pendingSessions = getPendingSessions();
                            int sessionIdx = this.selection - MAX_SELECTION;
                            String session = new ArrayList<>(pendingSessions.keySet()).get(sessionIdx);

                            if (pendingSessions.get(session) != null)
                            {
                                this.list = pendingSessions.get(session).getPending().stream().map(bookingInfo -> bookingInfo.getEmail()).toArray(String[]::new);
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

    public void handleSessionAction(String action)
    {
        switch (action)
        {
            case "UP" -> {
                if (isSublocked)
                    return;

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
                if (isSublocked)
                    return;

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
                    this.isSublocked = true;
                else
                {
                    approve();
                }
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
