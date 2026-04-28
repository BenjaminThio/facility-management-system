package src.pages.admin;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;

import javax.swing.JOptionPane;

import src.components.Ansi;
import src.components.AnsiBuilder;
import src.components.Container;
import src.components.Container.Alignment;
import src.components.Table;
import src.components.fields.InputField;
import src.components.fields.MemoField;
import src.models.Report;
import src.models.User;
import src.pages.ListViewPage;
import src.pages.cores.Subpage;
import src.utils.Database;
import src.utils.Image;
import src.utils.Renderer;
import src.utils.Router;
import src.utils.Util;

public class ReviewReportPage extends Subpage {
    private static final int MAX_SELECTION = 4;
    private static final int WIDTH = 50;

    private Report getTargetReport() {
        Route[] route = getRoute();
        String facilityName = route[0].getLabel();
        int filteredIdx = route[1].getId();
        
        int current = 0;
        for (Report r : Database.Report.getAll().get(facilityName)) {
            if (r.getStatus() != Report.Status.RESOLVED) {
                if (current == filteredIdx) {
                    return r;
                }
                current++;
            }
        }
        return Database.Report.getAll().get(facilityName).get(filteredIdx); 
    }

    @Override
    public void init() {
        Report report = getTargetReport();

        if (report.getImageBase64() == null || report.getImageBase64().isEmpty()) {
            this.selection = 1;
        }
    }

    @Override
    public Subpage copy() {
        ReviewReportPage clone = new ReviewReportPage();
        clone.selection = this.selection;
        clone.id = this.id;
        clone.label = this.label;
        return clone;
    }

    @Override
    public void render(StringBuilder frame) {
        Route[] route = getRoute();
        String facilityName = route[0].getLabel();
        Report report = getTargetReport();
        User reporter = Database.User.get(report.getFrom());

        ArrayList<ArrayList<AnsiBuilder>> table = new ArrayList<>();

        table.add(new ArrayList<>(Arrays.asList(
            new AnsiBuilder().append(new Container(" Review Maintenance Report: " + Util.textOverflowEllipsis(facilityName, 15), WIDTH, Alignment.CENTER, Ansi.BG_BLACK, Ansi.FG_LIGHT_GRAY).toAnsi())
        )));

        InputField titleField = new InputField(report.getTitle(), WIDTH, "", true);
        titleField.setBackgroundColor(Ansi.BG_DARK_GRAY);
        titleField.setTextColor(Ansi.FG_WHITE);

        MemoField descField = new MemoField(report.getDescription(), 14, WIDTH, "", false);
        descField.setBackgroundColor(Ansi.BG_DARK_GRAY);
        descField.setTextColor(Ansi.FG_WHITE);

        AnsiBuilder details = new AnsiBuilder()
            .append(" Title:\n")
            .append(titleField.toAnsiBuilder()).append('\n')
            .append(" Description:\n")
            .append(descField.toAnsiBuilder()).append('\n')
            .append(" Severity: ")
            .append(new Container(report.getSeverity().toString(), 10, Alignment.CENTER, Ansi.BG_WHITE, 
                    report.getSeverity() == Report.Severity.HIGH ? Ansi.FG_RED : 
                    (report.getSeverity() == Report.Severity.MEDIUM ? Ansi.FG_YELLOW : Ansi.FG_BLACK)).toAnsi()
            ).append("\n\n")
            .append(" Assigned To: ").append(new Ansi(report.getAssignedTo(), Ansi.BG_BLACK, Ansi.FG_YELLOW)).append("\n\n")
            .append(" Status: ").append(new Ansi(report.getStatus().toString().replace("_", " "), Ansi.BG_BLACK, report.getStatus() == Report.Status.RESOLVED ? Ansi.FG_GREEN : (report.getStatus() == Report.Status.IN_PROGRESS ? Ansi.FG_YELLOW : Ansi.FG_RED))).append("\n\n")
            .append(" From: ").append(reporter.getName()).append('\n')
            .append("       (").append(report.getFrom()).append(")\n")
            .append("       Contact: ").append(reporter.getPhoneNumber() != null ? reporter.getPhoneNumber() : "N/A").append('\n')
            .append(" Date: ").append(report.getDate());

        table.add(new ArrayList<>(Arrays.asList(details)));

        AnsiBuilder buttons = new AnsiBuilder();
        String padding = " ".repeat(13); 
        
        buttons.append(padding).append(new Ansi(
            "     Image Preview      ",
            (report.getImageBase64() == null || report.getImageBase64().isEmpty() ? Ansi.BG_DARK_GRAY : (selection == 0 ? Ansi.BG_LIGHT_GREEN : Ansi.BG_WHITE)),
            Ansi.FG_BLACK
        )).append("\n\n");

        buttons.append(padding).append(new Ansi(
            "    Assign Personnel    ",
            selection == 1 ? Ansi.BG_LIGHT_GREEN : Ansi.BG_WHITE,
            Ansi.FG_BLACK
        )).append("\n\n");

        buttons.append(padding).append(new Ansi(
            "  Lock for Maintenance  ",
            selection == 2 ? Ansi.BG_LIGHT_GREEN : Ansi.BG_WHITE,
            Ansi.FG_BLACK
        )).append("\n\n");

        buttons.append(padding).append(new Ansi(
            "    Mark as Resolved    ",
            selection == 3 ? Ansi.BG_LIGHT_GREEN : Ansi.BG_WHITE,
            Ansi.FG_BLACK
        ));

        table.add(new ArrayList<>(Arrays.asList(buttons)));

        Table.render(frame, table);
    }

    @Override
    public void handleAction(String action) {
        Route[] route = getRoute();
        String facilityName = route[0].getLabel();
        Report report = getTargetReport();
        
        switch (action) {
            case "UP" -> {
                int min = (report.getImageBase64() == null || report.getImageBase64().isEmpty() ? 1 : 0);
                if (selection - 1 >= min) selection--;
                else selection = MAX_SELECTION - 1;
            }
            case "DOWN" -> {
                if (selection + 1 < MAX_SELECTION) selection++;
                else selection = (report.getImageBase64() == null || report.getImageBase64().isEmpty() ? 1 : 0);
            }
            case "ESC" -> Router.back();
            case "ENTER" -> {
                switch (selection) {
                    case 0 -> {
                        if (report.getImageBase64() != null && !report.getImageBase64().isEmpty()) {
                            Image.show(report.getImageBase64());
                        }
                    }
                    case 1 -> {
                        ArrayList<String> staffList = new ArrayList<>();
                        ArrayList<String> staffEmails = new ArrayList<>(); // Track the raw email for the DB
                        
                        for (User u : Database.User.getAll().values()) {
                            if (u.getRole() == User.Role.STAFF) {
                                staffList.add(u.getName() + " (" + u.getEmail() + ")");
                                staffEmails.add(u.getEmail());
                            }
                        }
                        if (staffList.isEmpty()) {
                            // ...
                        } else {
                            Router.redirect(new ListViewPage(staffList.toArray(String[]::new), (index) -> {
                                report.setAssignedTo(staffList.get(index));
                                report.setStatus(Report.Status.IN_PROGRESS);
                                Database.Report.save();

                                // --- TRIGGER: NOTIFY THE STAFF MEMBER ---
                                User staffUser = Database.User.get(staffEmails.get(index));
                                if (staffUser != null) {
                                    // Grab the currently logged-in Admin's name!
                                    String currentAdminName = src.utils.Global.getUser().getName();
                                    
                                    staffUser.addNotification(new src.models.Notification(
                                        "[ASSIGNMENT] You have been assigned to fix: " + report.getTitle() + " at " + facilityName, 
                                        currentAdminName + " (Admin)"
                                    ));
                                    Database.User.save();
                                }
                                // ----------------------------------------

                                JOptionPane.showMessageDialog(null, "Personnel assigned! Status updated to ON_PROGRESS.", "Success", JOptionPane.INFORMATION_MESSAGE);
                            }));
                        }
                    }
                    case 2 -> {
                        if (report.getAssignedTo().equals("Unassigned")) {
                            JOptionPane.showMessageDialog(null, "You must assign a staff member before locking the facility for maintenance.", "Action Denied", JOptionPane.WARNING_MESSAGE);
                            return;
                        }
                        
                        if (JOptionPane.showConfirmDialog(null, "Lock facility?", "Confirm", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                            Database.Facility.get(facilityName).setIsAvailable(false);
                            Database.Facility.save();
                            
                            // --- NEW LOGIC: Start the clock! ---
                            report.setMaintenanceStartTime(LocalDateTime.now().toString());
                            Database.Report.save();
                            // -----------------------------------
                            
                            JOptionPane.showMessageDialog(null, "Facility is now locked for maintenance. Timer started.", "Success", JOptionPane.INFORMATION_MESSAGE);
                        }
                    }
                    case 3 -> {
                        if (report.getStatus() == Report.Status.RESOLVED) return;
                        if (JOptionPane.showConfirmDialog(null, "Mark resolved?", "Confirm", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                            resolveReport(report);
                            JOptionPane.showMessageDialog(null, "Report marked as resolved and facility is now UNLOCKED.", "Success", JOptionPane.INFORMATION_MESSAGE);
                        }
                    }
                }
            }
        }
        Renderer.refresh();
    }

    private void resolveReport(Report report) {
        Route[] route = getRoute();
        String facilityName = route[0].getLabel();

        report.setStatus(Report.Status.RESOLVED);
        report.setMaintenanceEndTime(LocalDateTime.now().toString());
        
        if (report.getMaintenanceStartTime() != null) {
            LocalDateTime start = LocalDateTime.parse(report.getMaintenanceStartTime());
            LocalDateTime end = LocalDateTime.parse(report.getMaintenanceEndTime());

            int minutes = (int) Duration.between(start, end).toMinutes();

            report.setRepairMinutes(minutes == 0 ? 1 : minutes);
        } else {
            report.setRepairMinutes(1);
        }

        Database.Facility.get(facilityName).setIsAvailable(true);
        Database.Facility.save();

        Router.back();
        if (Router.getPage() instanceof ListViewPage facilityListPage) {
            facilityListPage.triggerCallback();
            facilityListPage.updateSelection();
        }
        Database.Report.save();
    }
}
/*
package src.pages.admin;

import java.util.ArrayList;
import java.util.Arrays;

import javax.swing.JOptionPane;

import src.components.Ansi;
import src.components.AnsiBuilder;
import src.components.Container;
import src.components.Table;
import src.components.Container.Alignment;
import src.components.fields.InputField;
import src.components.fields.MemoField;
import src.models.Report;
import src.pages.ListViewPage;
import src.pages.cores.Subpage;
import src.utils.Database;
import src.utils.Image;
import src.utils.Renderer;
import src.utils.Router;

public class ReviewReportPage extends Subpage {
    private static final int MAX_SELECTION = 3;

    @Override
    public void init()
    {
        Route[] route = getRoute();
        Report report = Database.Report.getAll().get(route[0].getLabel()).get(route[1].getId());

        if (report.getImageBase64() == null)
        {
            this.selection = 1;
        }
    }

    @Override
    public Subpage copy()
    {
        ReviewReportPage clone = new ReviewReportPage();

        clone.selection = this.selection;
        clone.id = this.id;
        clone.label = this.label;

        return clone;
    }

    @Override
    public void render(StringBuilder frame)
    {
        Route[] route = getRoute();
        Report report = Database.Report.getAll().get(route[0].getLabel()).get(route[1].getId());

        InputField titleField = new InputField(
            report.getTitle(),
            50,
            "",
            true
        );
        MemoField descField = new MemoField(
            report.getDescription(),
            23,
            50,
            "",
            false
        );

        Table.render(
            frame, new ArrayList<>(Arrays.asList(
                new ArrayList<>(Arrays.asList(
                    new AnsiBuilder()
                        .append("Facility Name: " + route[0].getLabel())
                )),
                new ArrayList<>(Arrays.asList(
                new AnsiBuilder()
                    .append("Title:").append('\n')
                    .append(titleField.toAnsiBuilder()).append('\n')
                    .append("Description:").append('\n')
                    .append(descField.toAnsiBuilder()).append('\n')
                    .append("Severity: ")
                    .append(
                        new Container(report.getSeverity().toString(), 10, Alignment.CENTER).toAnsi()
                    ).append('\n')
                    .append('\n')
                    .append("From: ")
                    .append(Database.User.get(report.getFrom()).getName()).append('\n')
                    .append(" ".repeat(6) + "(" + report.getFrom() + ")").append('\n')
                    .append("Date: " + report.getDate())
            )),
            new ArrayList<>(Arrays.asList(
                new AnsiBuilder()
                    .append(" ".repeat(13)).append(new Ansi(
                        "     Image Preview      ",
                        (report.getImageBase64() == null ? 
                            Ansi.BG_DARK_GRAY
                            :
                            (selection == 0 ?
                                Ansi.BG_GREEN
                                :
                                Ansi.BG_WHITE
                            )),
                        Ansi.FG_BLACK
                    )).append('\n')
                    .append('\n')
                    .append(" ".repeat(13)).append(new Ansi(
                        "  Lock for Maintenance  ",
                        selection == 1 ? Ansi.BG_GREEN : Ansi.BG_WHITE,
                        Ansi.FG_BLACK
                    )).append('\n')
                    .append('\n')
                    .append(" ".repeat(13)).append(new Ansi(
                        "    Mark as Resolved    ",
                        selection == 2 ? Ansi.BG_GREEN : Ansi.BG_WHITE,
                        Ansi.FG_BLACK
                    ))
            ))
        ))
        );
    }

    @Override
    public void handleAction(String action)
    {
        Route[] route = getRoute();
        String facilityName = route[0].getLabel();
        int reportIdx = route[1].getId();
        Report report = Database.Report.getAll().get(facilityName).get(reportIdx);

        switch (action)
        {
            case "UP" -> {
                if (selection - 1 >= (report.getImageBase64() == null ? 1 : 0))
                {
                    selection--;
                }
                else
                {
                    selection = MAX_SELECTION - 1;
                }
            }
            case "DOWN" -> {
                if (selection + 1 < MAX_SELECTION)
                {
                    selection++;
                }
                else
                {
                    selection = (report.getImageBase64() == null ? 1 : 0);
                }
            }
            case "ESC" -> {
                Router.back();
            }
            case "ENTER" -> {
                switch (selection)
                {
                    case 0 -> {
                        Image.show(report.getImageBase64());
                    }
                    case 1 -> {
                        int response = JOptionPane.showConfirmDialog(
                            null,
                            "Are you sure you want to lock this facility for maintenance?",
                            "Confirm Resolution",
                            JOptionPane.YES_NO_OPTION,
                            JOptionPane.WARNING_MESSAGE
                        );

                        if (response == JOptionPane.YES_OPTION)
                        {
                            Database.Facility.get(facilityName).setIsAvailable(false);
                            Database.Facility.save();
                            resolveReport(facilityName, reportIdx);
                        }
                    }
                    case 2 -> {
                        int response = JOptionPane.showConfirmDialog(
                            null,
                            "Are you sure you want to mark this report as resolved?" +
                            (report.getSeverity() == Report.Severity.MEDIUM || report.getSeverity() == Report.Severity.HIGH
                                ? "\nWarning: This report has a `" + report.getSeverity().toString() + "` severity level."
                                : ""),
                            "Confirm Resolution",
                            JOptionPane.YES_NO_OPTION,
                            JOptionPane.WARNING_MESSAGE
                        );

                        if (response == JOptionPane.YES_OPTION)
                        {
                            resolveReport(facilityName, reportIdx);
                        }
                    }
                }
            }
        }
        Renderer.refresh();
    }

    public void resolveReport(String facilityName, int reportIdx)
    {
        Database.Report.getAll().get(facilityName).remove(reportIdx);
        Router.back();
        if (Router.getPage() instanceof ListViewPage facilityListPage)
        {
            facilityListPage.triggerCallback();
            facilityListPage.updateSelection();

            if (Database.Report.getAll().get(facilityName).size() == 0)
            {
                Router.back();

                if (Router.getPage() instanceof ListViewPage reportListPage)
                {
                    Database.Report.getAll().remove(facilityName);
                    reportListPage.setList(Database.Report.getAll().keySet().toArray(String[]::new));
                    reportListPage.updateSelection();
                }
            }
        }
        Database.Report.save();
    }
}
*/