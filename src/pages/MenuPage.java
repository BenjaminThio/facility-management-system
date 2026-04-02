package src.pages;

import java.util.ArrayList;

import src.models.Facility;
import src.models.User;
import src.pages.admin.AnalyticsPage;
import src.pages.admin.ConsolePage;
import src.pages.admin.ProcessBookingPage;
import src.pages.admin.ReviewReportPage;
import src.pages.cores.Page;
import src.utils.Database;
import src.utils.Global;
import src.utils.Renderer;
import src.utils.Router;
import src.utils.Util;

public class MenuPage extends Page {
    public enum GuestSelection
    {
        VIEW_FACILITIES(0), SIGN_IN(1), SIGN_UP(2), EXIT(3);

        private final int val;

        private GuestSelection(int val)
        {
            this.val = val;
        }

        public int getVal()
        {
            return this.val;
        }

        public static GuestSelection cast(int val)
        {
            for (GuestSelection selection : values())
            {
                if (selection.getVal() == val)
                    return selection;
            }
            return null;
        }
    }
    public enum StandardSelection
    {
        VIEW_FACILITIES(0), MY_BOOKINGS(1), REPORT_ISSUE(2), LOGOUT(3), EXIT(4);

        private final int val;

        private StandardSelection(int val)
        {
            this.val = val;
        }

        public int getVal()
        {
            return this.val;
        }

        public static StandardSelection cast(int val)
        {
            for (StandardSelection selection : values())
            {
                if (selection.getVal() == val)
                    return selection;
            }
            return null;
        }
    }
    public enum AdminSelection
    {
        MANAGE_BOOKING_SESSIONS(0), PROCESS_BOOKING_REQUESTS(1), MANAGE_FACILITY_MAINTENANCE(2), GENERATE_ANALYTICS_REPORT(3), LOGOUT(4), EXIT(5);

        private final int val;

        private AdminSelection(int val)
        {
            this.val = val;
        }

        public int getVal()
        {
            return this.val;
        }

        public static AdminSelection cast(int val)
        {
            for (AdminSelection selection : values())
            {
                if (selection.getVal() == val)
                    return selection;
            }
            return null;
        }
    }

    public int getSelectionSize()
    {
        if (Global.getUser() != null)
        {
            switch (Global.getUser().getRole())
            {
                case User.Role.STUDENT:
                case User.Role.STAFF:
                    return StandardSelection.values().length;
                case User.Role.ADMIN:
                    return AdminSelection.values().length;
            }
        }
        return GuestSelection.values().length;
    }

    @Override
    public void render(StringBuilder frame)
    {
        if (Global.getUser() != null)
        {
            frame.append("Welcome " +
                        Global.getUser().getName() +
                        '!' +
                        String.valueOf(' ').repeat(10) +
                        "Role: " +
                        Global.getUser().getRole().name()).append('\n');
        }

        frame.append('\n')
             .append("💡 Press the arrow keys to navigate the selections.").append('\n')
             .append("💡 Press [Enter] to select.").append('\n')
             .append("💡 Press [ESC] to return to the previous page.").append('\n')
             .append('\n');

        if (Global.getUser() != null)
        {
            switch (Global.getUser().getRole())
            {
                case User.Role.STUDENT:
                case User.Role.STAFF:
                    for (StandardSelection s : StandardSelection.values())
                    {
                        if (selection == s.getVal())
                            frame.append("> ");
                        else
                            frame.append(String.valueOf(' ').repeat(2));

                        frame.append(Util.toTitleCase(s.name())).append('\n');
                    }
                    break;
                case User.Role.ADMIN:
                    for (AdminSelection s : AdminSelection.values())
                    {
                        if (selection == s.getVal())
                            frame.append("> ");
                        else
                            frame.append(String.valueOf(' ').repeat(2));

                        frame.append(Util.toTitleCase(s.name())).append('\n');
                    }
                    break;
            }
        }
        else
        {
            for (GuestSelection s : GuestSelection.values())
            {
                if (selection == s.getVal())
                    frame.append("> ");
                else
                    frame.append(String.valueOf(' ').repeat(2));

                frame.append(Util.toTitleCase(s.name())).append('\n');
            }
        }
    }

    public void select()
    {
        ArrayList<String> availableFacilityNames = new ArrayList<String>();

        for (Facility facility : Database.Facility.getAll())
        {
            if (facility.isAvailable())
            {
                availableFacilityNames.add(facility.getName());
            }
        }

        if (Global.getUser() != null)
        {
            switch (Global.getUser().getRole())
            {
                case User.Role.STUDENT:
                case User.Role.STAFF:
                    switch (StandardSelection.cast(selection))
                    {
                        case StandardSelection.VIEW_FACILITIES:
                            Router.redirect(
                                new ListPage(
                                    new ViewFacilityPage(),
                                    availableFacilityNames.toArray(String[]::new)
                                )
                            );
                            break;
                        case StandardSelection.MY_BOOKINGS:
                            Router.redirect(new MyBookingPage());
                            break;
                        case StandardSelection.REPORT_ISSUE:
                            Router.redirect(
                                new ListPage(
                                    new ReportPage(),
                                    availableFacilityNames.toArray(String[]::new)
                                )
                            );
                            break;
                        case StandardSelection.LOGOUT:
                            logout();
                            break;
                        case StandardSelection.EXIT:
                            System.exit(0);
                            break;
                    }
                    break;
                case User.Role.ADMIN:
                    switch (AdminSelection.cast(selection))
                    {
                        case MANAGE_BOOKING_SESSIONS:
                            Router.redirect(new ListPage(new ConsolePage(), Database.Facility.getAll().stream().map(Facility::getName).toArray(String[]::new)));
                            break;
                        case PROCESS_BOOKING_REQUESTS:
                        {
                            Router.redirect(
                                new ListPage(
                                    new ProcessBookingPage(),
                                    Database.Booking.getAll().keySet().toArray(String[]::new)
                                )
                            );
                            break;
                        }
                        case MANAGE_FACILITY_MAINTENANCE:
                        {
                            Router.redirect(
                                new ListPage(
                                    new ListPage(new ReviewReportPage(), subpage -> Database.Report.getAll().get(subpage.getLabel()).stream().map(report -> report.getTitle()).toArray(String[]::new)),
                                    Database.Report.getAll().keySet().toArray(String[]::new)
                                )
                            );
                            break;
                        }
                        case GENERATE_ANALYTICS_REPORT:
                            Router.redirect(new AnalyticsPage());
                            break;
                        case LOGOUT:
                            logout();
                            break;
                        case EXIT:
                            System.exit(0);
                            break;
                        default:
                            break;
                    }
                    break;
            }
        }
        else
        {
            switch (GuestSelection.cast(selection))
            {
                case GuestSelection.VIEW_FACILITIES:
                    Router.redirect(
                        new ListPage(
                            new ViewFacilityPage(),
                            availableFacilityNames.toArray(String[]::new)
                        )
                    );
                    break;
                case GuestSelection.SIGN_IN:
                    Router.redirect(new SignInPage());
                    break;
                case GuestSelection.SIGN_UP:
                    Router.redirect(new SignUpPage());
                    break;
                case GuestSelection.EXIT:
                    System.exit(0);
                    break;
            }
        }
    }

    @Override
    public void handleAction(String action)
    {
        switch (action)
        {
            case "UP":
                if (selection - 1 >= 0)
                {
                    selection--;
                }
                else
                {
                    selection = getSelectionSize() - 1;
                }
                break;
            case "DOWN":
                if (selection + 1 < getSelectionSize())
                {
                    selection++;
                }
                else
                {
                    selection = 0;
                }
                break;
            case "ENTER":
                select();
                break;
            default:
                break;
        }

        Renderer.refresh();
    }

    private void logout()
    {
        selection = 0;
        Global.clearSession();
    }
}