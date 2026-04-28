package src.pages;

import java.time.LocalTime;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.swing.JOptionPane;

import src.components.Ansi;
import src.components.AnsiBuilder;
import src.components.Container;
import src.components.Table;
import src.models.UserBooking;
import src.models.BookingInfo;
import src.pages.cores.Page;
import src.utils.Database;
import src.utils.Global;
import src.utils.Renderer;
import src.utils.Router;
import src.utils.Util;

public class MyBookingPage extends Page {
    
    private int scrollOffset = 0;
    private static final int MAX_VISIBLE = 8;
    
    private HashMap<UserBooking, BookingInfo> cancelledCache = new HashMap<>();

    public MyBookingPage() {
        this.selection = 0;
    }

    @Override
    public void render(StringBuilder frame) {
        ArrayList<ArrayList<AnsiBuilder>> table = new ArrayList<>();
        ArrayList<UserBooking> bookings = Global.getUser().getBookings();
        
        // --- LEFT COLUMN: THE BOOKING LIST ---
        AnsiBuilder leftCol = new AnsiBuilder();
        leftCol.append(new Container("My Bookings", 72, Container.Alignment.CENTER, Ansi.BG_BLACK, Ansi.FG_LIGHT_GRAY).toAnsi()).append("\n");
        leftCol.append(new Ansi("   Facility                  Date         Session       Status          \n", Ansi.BG_DARK_GRAY, Ansi.FG_WHITE));

        if (bookings.isEmpty()) {
            leftCol.append(new Container("No bookings found in your history.", 72, Container.Alignment.CENTER, Ansi.BG_WHITE, Ansi.FG_DARK_GRAY).toAnsi()).append("\n");
        } else {
            for (int i = scrollOffset; i < Math.min(bookings.size(), scrollOffset + MAX_VISIBLE); i++) {
                UserBooking b = bookings.get(i);
                Database.Booking.Session.Status masterStatus = Database.Booking.Session.getStatus(b.getFacilityName(), b.getDate(), b.getSession(), Global.getUser().getEmail());
                
                String statusText = "UNKNOWN";
                int statusColor = Ansi.FG_WHITE;

                if (cancelledCache.containsKey(b)) {
                    statusText = "CANCELED";
                    statusColor = Ansi.FG_DARK_GRAY;
                } 
                else if (masterStatus != null) {
                    switch (masterStatus) {
                        case PENDING -> { statusText = "PENDING"; statusColor = Ansi.FG_YELLOW; }
                        case APPROVED -> { statusText = "APPROVED"; statusColor = Ansi.FG_GREEN; }
                        case DISCARDED -> { statusText = "DISCARDED"; statusColor = Ansi.FG_RED; }
                        case EXPIRED -> { statusText = "EXPIRED"; statusColor = Ansi.FG_MAGENTA; }
                        case UNDER_MAINTENANCE -> { statusText = "MAINTENANCE"; statusColor = Ansi.FG_YELLOW; }
                        case NOT_FOUND -> { statusText = "NOT FOUND"; statusColor = Ansi.FG_BLUE; }
                    }
                }

                boolean isSelected = (i == selection);
                int bgColor = isSelected ? Ansi.BG_LIGHT_GREEN : Ansi.BG_WHITE;
                int fgColor = isSelected ? Ansi.FG_BLACK : Ansi.FG_BLACK;

                String rowStr = String.format(" %-25s %-12s %-13s ", 
                    Util.textOverflowEllipsis(b.getFacilityName(), 25), 
                    b.getDate(), 
                    b.getSession());

                leftCol.append(new Ansi(isSelected ? ">" : " ", bgColor, fgColor));
                leftCol.append(new Ansi(rowStr, bgColor, fgColor));
                leftCol.append(new Ansi(String.format("%-16s", statusText), bgColor, statusColor));
                leftCol.append(new Ansi(" \n", bgColor, fgColor));
            }
        }
        leftCol.append("\n").append(new Ansi(" [UP/DOWN] Navigate   [DEL] Cancel/Clear   [ENTER] Re-Add   [ESC] Exit  ", Ansi.BG_BLACK, Ansi.FG_DARK_GRAY));


        // --- RIGHT COLUMN: PERSONAL ANALYTICS (Requirement 2f) ---
        int totalFacilitiesBooked = 0;
        HashMap<String, Integer> facilityFreq = new HashMap<>();
        long totalMinutes = 0;
        String userEmail = Global.getUser().getEmail();

        // Safely extract the master database to scan for absolute historical truth
        @SuppressWarnings("unchecked")
        Map<String, LinkedHashMap<String, LinkedHashMap<String, src.models.Booking>>> allBookings = 
            (Map<String, LinkedHashMap<String, LinkedHashMap<String, src.models.Booking>>>) (Object) Database.Booking.getAll();

        for (Map.Entry<String, LinkedHashMap<String, LinkedHashMap<String, src.models.Booking>>> facEntry : allBookings.entrySet()) {
            String facName = facEntry.getKey();
            for (Map.Entry<String, LinkedHashMap<String, src.models.Booking>> dateEntry : facEntry.getValue().entrySet()) {
                for (Map.Entry<String, src.models.Booking> sessionEntry : dateEntry.getValue().entrySet()) {
                    src.models.Booking b = sessionEntry.getValue();
                    
                    // Count it if this specific user was ever APPROVED for this slot (whether active or expired!)
                    if (b.getApproved() != null && b.getApproved().equals(userEmail)) {
                        totalFacilitiesBooked++;
                        facilityFreq.put(facName, facilityFreq.getOrDefault(facName, 0) + 1);
                        
                        // Parse the "HH:mm-HH:mm" session string to calculate exact duration
                        String[] times = sessionEntry.getKey().split("-");
                        try {
                            LocalTime start = LocalTime.parse(times[0]);
                            LocalTime end = LocalTime.parse(times[1]);
                            totalMinutes += Duration.between(start, end).toMinutes();
                        } catch (Exception e) {
                            // Ignore corrupted session times
                        }
                    }
                }
            }
        }

        String topFacility = "N/A";
        int maxBookings = 0;
        for (Map.Entry<String, Integer> entry : facilityFreq.entrySet()) {
            if (entry.getValue() > maxBookings) {
                maxBookings = entry.getValue();
                topFacility = entry.getKey();
            }
        }

        double totalHours = totalMinutes / 60.0;

        AnsiBuilder rightCol = new AnsiBuilder();
        rightCol.append(new Container(" Personal Analytics ", 30, Container.Alignment.CENTER, Ansi.BG_DARK_GRAY, Ansi.FG_WHITE).toAnsi()).append("\n");
        rightCol.append(new Container(String.format(" Total Bookings : %d", totalFacilitiesBooked), 30, Container.Alignment.LEFT, Ansi.BG_WHITE, Ansi.FG_BLACK).toAnsi()).append("\n");
        rightCol.append(new Container("", 30, Container.Alignment.LEFT, Ansi.BG_WHITE, Ansi.FG_BLACK).toAnsi()).append("\n");
        rightCol.append(new Container(" Top Facility :", 30, Container.Alignment.LEFT, Ansi.BG_WHITE, Ansi.FG_BLACK).toAnsi()).append("\n");
        rightCol.append(new Container(String.format(" %s", Util.textOverflowEllipsis(topFacility, 28)), 30, Container.Alignment.LEFT, Ansi.BG_WHITE, Ansi.FG_BLACK).toAnsi()).append("\n");
        rightCol.append(new Container(String.format(" (%d times)", maxBookings), 30, Container.Alignment.LEFT, Ansi.BG_WHITE, Ansi.FG_BLACK).toAnsi()).append("\n");
        rightCol.append(new Container("", 30, Container.Alignment.LEFT, Ansi.BG_WHITE, Ansi.FG_BLACK).toAnsi()).append("\n");
        rightCol.append(new Container(String.format(" Total Hours    : %.1f hrs", totalHours), 30, Container.Alignment.LEFT, Ansi.BG_WHITE, Ansi.FG_BLACK).toAnsi());

        // --- MERGE COLUMNS INTO TABLE ---
        table.add(new ArrayList<>(Arrays.asList(leftCol, rightCol)));
        
        Table.render(frame, table);
    }

    @Override
    public void handleAction(String action) {
        ArrayList<UserBooking> bookings = Global.getUser().getBookings();

        switch (action) {
            case "UP" -> {
                if (selection > 0) {
                    selection--;
                    if (selection < scrollOffset) scrollOffset--;
                }
            }
            case "DOWN" -> {
                if (selection < bookings.size() - 1) {
                    selection++;
                    if (selection >= scrollOffset + MAX_VISIBLE) scrollOffset++;
                }
            }
            case "ESC" -> {
                if (!cancelledCache.isEmpty()) {
                    for (UserBooking b : cancelledCache.keySet()) {
                        bookings.remove(b);
                    }
                    Database.User.save();
                }
                Router.back();
            }
            case "DELETE" -> {
                if (bookings.isEmpty()) return;
                handleDelete(bookings.get(selection), bookings);
            }
            case "ENTER" -> {
                if (bookings.isEmpty()) return;
                handleRebook(bookings.get(selection));
            }
        }
        Renderer.refresh();
    }

    private void handleDelete(UserBooking b, ArrayList<UserBooking> bookings) {
        if (cancelledCache.containsKey(b)) {
            int response = JOptionPane.showConfirmDialog(null, "Permanently delete this canceled request?", "Confirm Delete", JOptionPane.YES_NO_OPTION);
            if (response == JOptionPane.YES_OPTION) {
                bookings.remove(b);
                cancelledCache.remove(b);
                Database.User.save();
                if (selection >= bookings.size() && selection > 0) selection--;
            }
            return;
        }

        Database.Booking.Session.Status status = Database.Booking.Session.getStatus(b.getFacilityName(), b.getDate(), b.getSession(), Global.getUser().getEmail());
        if (status == Database.Booking.Session.Status.NOT_FOUND || 
            status == Database.Booking.Session.Status.EXPIRED || 
            status == Database.Booking.Session.Status.DISCARDED) {
            bookings.remove(b);
            Database.User.save();
            if (selection >= bookings.size() && selection > 0) selection--;
        } 
        else if (status == Database.Booking.Session.Status.PENDING) {
            var facilityData = Database.Booking.getAll().get(b.getFacilityName());
            var sessionData = facilityData.get(b.getDate()).get(b.getSession());
            
            for (int i = 0; i < sessionData.getPending().size(); i++) {
                BookingInfo info = sessionData.getPending().get(i);
                if (info.getEmail().equals(Global.getUser().getEmail())) {
                    cancelledCache.put(b, info);
                    sessionData.getPending().remove(i);
                    break;
                }
            }
            Database.Booking.save();
        } 
        else if (status == Database.Booking.Session.Status.APPROVED) {
            JOptionPane.showMessageDialog(null, "You cannot cancel an APPROVED booking. Please contact Admin.", "Action Denied", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void handleRebook(UserBooking b) {
        if (cancelledCache.containsKey(b)) {
            var facilityData = Database.Booking.getAll().get(b.getFacilityName());
            var sessionData = facilityData.get(b.getDate()).get(b.getSession());
            
            BookingInfo restoredInfo = cancelledCache.get(b);
            sessionData.getPending().add(restoredInfo);
            Database.Booking.save();
            
            cancelledCache.remove(b); 
        }
    }
}
/*
package src.pages;

import java.util.ArrayList;
import java.util.Arrays;

import src.components.Ansi;
import src.components.AnsiBuilder;
import src.components.Table;
import src.models.UserBooking;
import src.pages.cores.Page;
import src.utils.Database;
import src.utils.Global;
import src.utils.Router;

public class MyBookingPage extends Page {
    @Override
    public void render(StringBuilder frame)
    {
        ArrayList<ArrayList<AnsiBuilder>> table = new ArrayList<>(Arrays.asList(
            new ArrayList<>(Arrays.asList(
                new AnsiBuilder(new Ansi("No.", Ansi.BOLD)),
                new AnsiBuilder(new Ansi("Bookings", Ansi.BOLD)),
                new AnsiBuilder(new Ansi("Status", Ansi.BOLD))
            ))
        ));
        ArrayList<UserBooking> bookings = Global.getUser().getBookings();

        if (bookings.size() > 0)
        {
            for (int i = 0; i < bookings.size(); i++)
            {
                Database.Booking.Session.Status status = Database.Booking.Session.getStatus(
                    bookings.get(i).getFacilityName(),
                    bookings.get(i).getDate(),
                    bookings.get(i).getSession(),
                    Global.getUser().getEmail()
                );
                int statusColor = switch (status)
                {
                    case APPROVED -> Ansi.FG_GREEN;
                    case PENDING -> Ansi.FG_LIGHT_YELLOW;
                    case UNDER_MAINTENANCE -> Ansi.FG_YELLOW;
                    case DISCARDED -> Ansi.FG_RED;
                    case EXPIRED -> Ansi.FG_MAGENTA;
                    case NOT_FOUND -> Ansi.FG_LIGHT_BLUE;
                };
                String statusCircle = switch (status)
                {
                    case APPROVED -> "🟢";
                    case PENDING -> "🟡";
                    case UNDER_MAINTENANCE -> "🟠";
                    case DISCARDED -> "🔴";
                    case EXPIRED -> "🟣";
                    case NOT_FOUND -> "🔵";
                };

                table.add(new ArrayList<>(Arrays.asList(
                    new AnsiBuilder(String.format("%d.", i + 1)),
                    new AnsiBuilder(bookings.get(i).toString()),
                    new AnsiBuilder(new Ansi('\n' + status.toString() + ' ' + statusCircle, statusColor))
                )));
            }
        }
        else
        {
            table.add(new ArrayList<>(Arrays.asList(new AnsiBuilder(""), new AnsiBuilder("You haven't book any facility."))));
        }

        Table.render(frame, table);
    }

    @Override
    public void handleAction(String action)
    {
        switch (action)
        {
            case "ESC" -> {
                Router.back();
            }
        }
    }
}
*/