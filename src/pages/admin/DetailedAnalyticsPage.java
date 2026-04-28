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

public class DetailedAnalyticsPage extends Page {
    private static final double MAX_REPORT_THRESHOLD = 10.0;
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

    public DetailedAnalyticsPage()
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

    private String formatAnalyticString(Analytic analytic, boolean isSelected) {
        String rawStatus = analytic.getStatus().toString();
        String statusText = String.format(" %-17s ", rawStatus);
        Ansi statusAnsi = analytic.getStatus() == Analytic.Status.AVAILABLE 
            ? (isSelected ? new Ansi(statusText, Ansi.BG_GREEN, Ansi.FG_BLACK) : new Ansi(statusText, Ansi.FG_GREEN))
            : (isSelected ? new Ansi(statusText, Ansi.BG_RED, Ansi.FG_BLACK) : new Ansi(statusText, Ansi.FG_RED));
        int sessionCount = analytic.getSessionQuatity();
        String sessionVal = sessionCount > 999 ? "999+" : String.valueOf(sessionCount);
        String sessionText = String.format(" %-14s ", sessionVal);
        Ansi sessionAnsi = sessionCount > 0 
            ? (isSelected ? new Ansi(sessionText, Ansi.BG_LIGHT_CYAN, Ansi.FG_BLACK) : new Ansi(sessionText, Ansi.FG_LIGHT_CYAN))
            : (isSelected ? new Ansi(sessionText, Ansi.BG_DARK_GRAY, Ansi.FG_BLACK) : new Ansi(sessionText, Ansi.FG_DARK_GRAY));
        int pendingCount = analytic.getPendingBookingQuantity();
        String pendingVal = pendingCount > 999 ? "999+" : String.valueOf(pendingCount);
        String pendingText = String.format(" %-8s ", pendingVal);
        Ansi pendingAnsi = pendingCount > 0 
            ? (isSelected ? new Ansi(pendingText, Ansi.BG_YELLOW, Ansi.FG_BLACK) : new Ansi(pendingText, Ansi.FG_YELLOW))
            : (isSelected ? new Ansi(pendingText, Ansi.BG_DARK_GRAY, Ansi.FG_BLACK) : new Ansi(pendingText, Ansi.FG_DARK_GRAY));
        int approvedCount = analytic.getApprovedBookingQuantity();
        String approvedVal = approvedCount > 999 ? "999+" : String.valueOf(approvedCount);
        String approvedText = String.format(" %-8s ", approvedVal);
        Ansi approvedAnsi = approvedCount > 0 
            ? (isSelected ? new Ansi(approvedText, Ansi.BG_GREEN, Ansi.FG_BLACK) : new Ansi(approvedText, Ansi.FG_GREEN))
            : (isSelected ? new Ansi(approvedText, Ansi.BG_DARK_GRAY, Ansi.FG_BLACK) : new Ansi(approvedText, Ansi.FG_DARK_GRAY));
        int reports = analytic.getReportQuantity();
        String reportVal = reports > 999 ? "999+" : String.valueOf(reports);
        String reportText = String.format(" %-7s ", reportVal);
        Ansi reportAnsi = isSelected ? new Ansi(reportText, Ansi.BG_DARK_GRAY, Ansi.FG_BLACK) : new Ansi(reportText, Ansi.FG_DARK_GRAY);

        if (reports > 0) {
            double ratio = (Math.min(reports, MAX_REPORT_THRESHOLD) - 1) / (MAX_REPORT_THRESHOLD - 1);
            int r = (int) (255 - (105 * ratio)); 
            int gb = (int) (150 - (150 * ratio));

            reportAnsi = isSelected ? new Ansi(reportText, Ansi.FG_BLACK) : new Ansi(reportText);

            if (isSelected) {
                reportAnsi.background(new src.models.RGB(r, gb, gb));
            } else {
                reportAnsi.foreground(new src.models.RGB(r, gb, gb));
            }
        }

        Ansi sep = new Ansi("│", Ansi.FG_DARK_GRAY);

        return sep.toString() + 
            statusAnsi.toString() + sep.toString() + 
            sessionAnsi.toString() + sep.toString() + 
            pendingAnsi.toString() + sep.toString() + 
            approvedAnsi.toString() + sep.toString() + 
            reportAnsi.toString() + sep.toString();
    }

    @Override
    public void render(StringBuilder frame)
    {
        frame.append("Search: ");

        inputField.setBackgroundColor(selection == 0 ? Ansi.BG_GREEN : Ansi.BG_WHITE);
        inputField.setPlaceholderColor(selection == 0 ? Ansi.FG_BLACK : Ansi.FG_DARK_GRAY);
        inputField.render(frame);

        frame.append(selectionOffset > 0 ? "▲" : "").append('\n');

        int indexWidth = String.valueOf(searchResult.length).length();
        int nameWidth = Math.max(
            13, 
            Arrays.stream(searchResult).max(java.util.Comparator.comparingInt(String::length)).orElse("").length()
        );
        String headerPrefix = String.format("  %" + indexWidth + "s  %-" + nameWidth + "s", "", "Facility Name");
        String headerRest = "│ Status            │ Total Sessions │ Pendings │ Approved │ Reports │";

        frame.append(new Ansi(headerPrefix + headerRest, Ansi.FG_DARK_GRAY, Ansi.BOLD).toString()).append('\n');

        for (int i = selectionOffset; i < selectionOffset + (searchResult.length < VISIBLE_NUMBER ? searchResult.length : VISIBLE_NUMBER); i++)
        {
            Analytic currentAnalytic = analytics.get(searchResult[i]);
            boolean isSelectedRow = (selection + selectionOffset - 1 == i);
            String prefixText = String.format("%" + indexWidth + "d. %-" + nameWidth + "s", (i + 1), searchResult[i]);
            Ansi prefixAnsi = isSelectedRow ? new Ansi(prefixText, Ansi.BG_WHITE, Ansi.FG_BLACK) : new Ansi(prefixText);
            String coloredDetails = formatAnalyticString(currentAnalytic, isSelectedRow);

            if (isSelectedRow)
            {
                frame.append("> ")
                    .append(prefixAnsi.toString())
                    .append(coloredDetails)
                    .append('\n');
            }
            else
            {
                frame.append("  ")
                    .append(prefixAnsi.toString())
                    .append(coloredDetails)
                    .append('\n');
            }
        }

        frame.append(selectionOffset + VISIBLE_NUMBER < searchResult.length ? "▼" : "");
    }

    @Override
    public void updateCaret()
    {
        switch (this.selection)
        {
            case 0 -> {
                Renderer.showInputCaret();
                inputField.updateCaret("Search: ".length(), 0);
            }
            default -> {
                Renderer.hideInputCaret();
            }
        }
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