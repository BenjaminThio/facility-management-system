package src.pages.admin;

import src.pages.cores.Page;

import src.components.Ansi;
import src.components.fields.InputField;
import src.utils.Database;
import src.utils.Renderer;
import src.utils.Router;
import src.utils.SearchEngine;

import java.util.Arrays;
import java.util.LinkedHashMap;

class Analytic
{
    public enum Status
    {
        AVAILABLE,
        UNDER_MAINTENANCE
    }
    private Status status;
    private int sessionQuantity;
    private int pendingBookingQuantity;
    private int approvedBookingQuantity;
    private int reportQuantity;

    public Analytic(Status status, int sessionQuantity, int pendingBookingQuantity, int approvedBookingQuantity, int reportQuantity)
    {
        this.status = status;
        this.sessionQuantity = sessionQuantity;
        this.pendingBookingQuantity = pendingBookingQuantity;
        this.approvedBookingQuantity = approvedBookingQuantity;
        this.reportQuantity = reportQuantity;
    }

    public Status getStatus()
    {
        return this.status;
    }

    public int getSessionQuatity()
    {
        return this.sessionQuantity;
    }

    public int getPendingBookingQuantity()
    {
        return this.pendingBookingQuantity;
    }

    public int getApprovedBookingQuantity()
    {
        return this.approvedBookingQuantity;
    }

    public int getReportQuantity()
    {
        return this.reportQuantity;
    }
}

public class AnalyticsPage extends Page {
    private static final int VISIBLE_NUMBER = 30;
    private InputField inputField = new InputField(20, "Type something...");
    private int selectionOffset = 0;
    private LinkedHashMap<String, Analytic> analytics = new LinkedHashMap<>();
    private String[] searchResult;

    private int getTotalSessions(String facilityName)
    {
        if (Database.Booking.getAll().get(facilityName) != null)
        {
            int counter = 0;

            for (String sessionDateString : Database.Booking.getAll().get(facilityName).keySet())
            {
                counter += Database.Booking.getAll().get(facilityName).get(sessionDateString).size();
            }

            return counter;
        }
        else
        {
            return 0;
        }
    }

    private int getTotalPending(String facilityName)
    {
        if (Database.Booking.getAll().get(facilityName) != null)
        {
            int counter = 0;

            for (String sessionDateString : Database.Booking.getAll().get(facilityName).keySet())
            {
                for (String sessionString : Database.Booking.getAll().get(facilityName).get(sessionDateString).keySet())
                {
                    counter += Database.Booking.getAll()
                                               .get(facilityName)
                                               .get(sessionDateString)
                                               .get(sessionString)
                                               .getPending().size();
                }
            }

            return counter;
        }
        else
        {
            return 0;
        }
    }

    private int getTotalApproved(String facilityName)
    {
        if (Database.Booking.getAll().get(facilityName) != null)
        {
            int counter = 0;

            for (String sessionDateString : Database.Booking.getAll().get(facilityName).keySet())
            {
                for (String sessionString : Database.Booking.getAll().get(facilityName).get(sessionDateString).keySet())
                {
                    if (Database.Booking.getAll()
                                        .get(facilityName)
                                        .get(sessionDateString)
                                        .get(sessionString)
                                        .getApproved() != null)
                        counter++;
                }
            }

            return counter;
        }
        else
        {
            return 0;
        }
    }

    public AnalyticsPage()
    {
        for (int i = 0; i < Database.Facility.getAll().size(); i++)
        {
            String facilityName = Database.Facility.get(i).getName();

            analytics.put(facilityName, new Analytic(
                Database.Facility.get(facilityName).isAvailable() ? Analytic.Status.AVAILABLE : Analytic.Status.UNDER_MAINTENANCE,
                getTotalSessions(facilityName),
                getTotalPending(facilityName),
                getTotalApproved(facilityName),
                Database.Report.get(facilityName) == null ? 0 : Database.Report.get(facilityName).size()
            ));
        }

        updateSearchResult();
    }

    @Override
    public void render(StringBuilder frame)
    {
        frame.append("Search: ");

        inputField.setBackgroundColor(selection == 0 ? Ansi.BG_GREEN : Ansi.BG_WHITE);
        inputField.render(frame);

        frame.append('\n');

        for (int i = selectionOffset; i < selectionOffset + (searchResult.length < VISIBLE_NUMBER ? searchResult.length : VISIBLE_NUMBER); i++)
        {
            if (selection + selectionOffset - 1 == i)
            {
                frame.append("> ");
            }
            else
            {
                frame.append("  ");
            }
            frame.append(
                Integer.toString(i + 1) +
                ". " +
                searchResult[i] +
                String.format(
                    " [Status: %s, Total Sessions: %d, Pending Booking: %d, Approved Booking: %d, Report: %d]",
                    analytics.get(searchResult[i]).getStatus().toString(),
                    analytics.get(searchResult[i]).getSessionQuatity(),
                    analytics.get(searchResult[i]).getPendingBookingQuantity(),
                    analytics.get(searchResult[i]).getApprovedBookingQuantity(),
                    analytics.get(searchResult[i]).getReportQuantity()
                )
            ).append('\n');
        }
    }

    @Override
    public void updateCaret()
    {
        inputField.updateCaret("Search: ".length(), 0);
    }

    public void handleAction(String action)
    {
        switch (action)
        {
            case "UP" -> {
                if (selection - 1 >= 1)
                {
                    selection--;
                }
                else if (selectionOffset - 1 >= 0)
                {
                    selectionOffset--;
                }
                else if (selection - 1 >= 0)
                {
                    selection--;
                }
            }
            case "DOWN" -> {
                if (selection + 1 <= (searchResult.length < VISIBLE_NUMBER ? searchResult.length : VISIBLE_NUMBER))
                {
                    selection++;
                }
                else if (selection + selectionOffset + 1 <= searchResult.length)
                {
                    selectionOffset++;
                }
            }
            case "ESC" -> {
                Router.back();
            }
        }

        switch (selection)
        {
            case 0 -> {
                inputField.handleInput(action);
                updateSearchResult();
            }
        }

        Renderer.refresh();
    }

    private void updateSearchResult()
    {
        if (inputField.getValue().equals(""))
        {
            searchResult = analytics.keySet().toArray(String[]::new);
        }
        else
        {
            searchResult = Arrays.stream(
                SearchEngine.searchSimilar(
                    inputField.getValue(),
                    analytics.keySet().toArray(String[]::new),
                    0.6
                )
            ).map(result -> result.text).toArray(String[]::new);
        }
    }
}

