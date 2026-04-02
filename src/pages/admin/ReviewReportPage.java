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
import src.pages.ListPage;
import src.pages.cores.Subpage;
import src.utils.Database;
import src.utils.Image;
import src.utils.Renderer;
import src.utils.Router;

public class ReviewReportPage extends Subpage {
    private static final int MAX_SELECTION = 3;

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
                        selection == 0 ? Ansi.BG_GREEN : Ansi.BG_WHITE,
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
        switch (action)
        {
            case "UP" -> {
                if (selection - 1 >= 0)
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
                    selection = 0;
                }
            }
            case "ESC" -> {
                Router.back();
            }
            case "ENTER" -> {
                Route[] route = getRoute();
                String facilityName = route[0].getLabel();
                int reportIdx = route[1].getId();
                Report report = Database.Report.getAll().get(facilityName).get(reportIdx);

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
        if (Router.getPage() instanceof ListPage facilityListPage)
        {
            facilityListPage.triggerCallback();
            facilityListPage.updateSelection();

            if (Database.Report.getAll().get(facilityName).size() == 0)
            {
                Router.back();

                if (Router.getPage() instanceof ListPage reportListPage)
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
