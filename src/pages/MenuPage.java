package src.pages;

import src.models.User;
import src.pages.core.Page;
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
        PROCESS_BOOKING_REQUESTS(0), MANAGE_FACILITY_MAINTENANCE(1), GENERATE_ANALYTICS_REPORT(2), LOGOUT(3), EXIT(4);

        private final int val;

        private AdminSelection(int val)
        {
            this.val = val;
        }

        public int getVal()
        {
            return this.val;
        }
    }

    // @Override
    public int getSelectionSize()
    {
        switch (Global.user.getRole())
        {
            case User.Role.STUDENT:
            case User.Role.STAFF:
                return StandardSelection.values().length;
            case User.Role.ADMIN:
                return AdminSelection.values().length;
        }
        return GuestSelection.values().length;
    }

    @Override
    public void render()
    {
        System.out.println("Welcome " + Global.user.getname() + '!' + String.valueOf(' ').repeat(10) + "Role: " + Global.user.getRole().name());
        System.out.println();
        System.out.println("💡 Press the arrow keys to navigate the selections.");
        System.out.println("💡 Press [Enter] to select.");
        System.out.println("💡 Press [ESC] to return to the previous page.");
        System.out.println();

        switch (Global.user.getRole())
        {
            case User.Role.STUDENT:
            case User.Role.STAFF:
                for (StandardSelection s : StandardSelection.values())
                {
                    if (selection == s.getVal())
                        System.out.print("> ");
                    else
                        System.out.print(String.valueOf(' ').repeat(2));

                    System.out.println(Util.toTitleCase(s.name()));
                }
                break;
            case User.Role.ADMIN:
                for (AdminSelection s : AdminSelection.values())
                {
                    if (selection == s.getVal())
                        System.out.print("> ");
                    else
                        System.out.print(String.valueOf(' ').repeat(2));

                    System.out.println(Util.toTitleCase(s.name()));
                }
                break;
        }
    }

    public void select()
    {
        switch (Global.user.getRole())
        {
            case User.Role.STUDENT:
            case User.Role.STAFF:
                switch (StandardSelection.cast(selection))
                {
                    case StandardSelection.VIEW_FACILITIES:
                        Router.redirect(new FacilityListPage(FacilityListPage.Subpage.VIEW_FACILITY));
                        break;
                    case StandardSelection.MY_BOOKINGS:
                        break;
                    case StandardSelection.REPORT_ISSUE:
                        Router.redirect(new FacilityListPage(FacilityListPage.Subpage.REPORT_ISSUE));
                        break;
                    case StandardSelection.LOGOUT:
                        break;
                    case StandardSelection.EXIT:
                        System.exit(0);
                        break;
                }
                break;
            case  User.Role.ADMIN:
                break;
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
}