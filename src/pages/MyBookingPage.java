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
