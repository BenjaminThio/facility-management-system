package src.pages.admin;

import src.pages.cores.Page;
import src.utils.Database;
import src.utils.Router;

public class AnalyticsPage extends Page {
    @Override
    public void render()
    {
        for (int i = 0; i < Database.Facility.getAll().size(); i++)
        {
            String facilityName = Database.Facility.get(i).getName();

            System.out.println(String.format("%d. %s [Booking: 0; Report: %d]", i + 1, facilityName, Database.Report.get(facilityName) == null ? 0 : Database.Report.get(facilityName).size()));
        }
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
