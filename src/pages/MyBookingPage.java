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
    public void render()
    {
        ArrayList<ArrayList<AnsiBuilder>> table = new ArrayList<>(Arrays.asList(
            new ArrayList<>(Arrays.asList(
                new AnsiBuilder("No.", Ansi.BOLD),
                new AnsiBuilder("Bookings", Ansi.BOLD),
                new AnsiBuilder("Status", Ansi.BOLD)
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
                    case PENDING -> Ansi.FG_YELLOW;
                    case APPROVED -> Ansi.FG_GREEN;
                    case NOT_FOUND -> Ansi.FG_MAGENTA;
                };

                table.add(new ArrayList<>(Arrays.asList(
                    new AnsiBuilder(String.format("%d.", i + 1)),
                    new AnsiBuilder(bookings.get(i).toString()),
                    new AnsiBuilder("\n" + status.toString(), statusColor)
                )));
            }
        }
        else
        {
            table.add(new ArrayList<>(Arrays.asList(new AnsiBuilder(""), new AnsiBuilder("You haven't book any facility."))));
        }

        Table.printTable(table);
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
