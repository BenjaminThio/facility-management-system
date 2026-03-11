package src.pages.admin;

import src.components.Ansi;
import src.pages.cores.Subpage;
import src.utils.Database;
import src.utils.Image;
import src.utils.Router;

public class ReviewReportPage extends Subpage {
    @Override
    public void render()
    {
        Route[] route = getRoute();
        System.out.println("Title:");
        System.out.println(Database.Report.getAll().get(route[0].getLabel()).get(route[1].getId()).getTitle());
        System.out.println();
        System.out.println("Description:");
        System.out.println(Database.Report.getAll().get(route[0].getLabel()).get(route[1].getId()).getDescription());
        System.out.println();
        System.out.println("Severity:");
        System.out.println(Database.Report.getAll().get(route[0].getLabel()).get(route[1].getId()).getSeverity());
        System.out.println();
        System.out.println(new Ansi("Image Preview", Ansi.BG_GREEN, Ansi.FG_BLACK));
    }

    @Override
    public void handleAction(String action)
    {
        switch (action)
        {
            case "ESC" -> {
                Router.back();
            }
            case "ENTER" -> {
                Route[] route = getRoute();

                Image.show(Database.Report.getAll().get(route[0].getLabel()).get(route[1].getId()).getImageBase64());
            }
        }
    }
}
