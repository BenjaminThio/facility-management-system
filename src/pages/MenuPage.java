package src.pages;

import src.models.Facility;
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

import java.util.ArrayList;
import src.components.Ansi;

public class MenuPage extends Page {
    public enum GuestSelection {
        VIEW_FACILITIES(0), SIGN_IN(1), SIGN_UP(2), EXIT(3);
        
        private final int val;
        private GuestSelection(int val) { this.val = val; }
        public int getVal() { return this.val; }
        
        public static GuestSelection cast(int val) {
            for (GuestSelection selection : values()) {
                if (selection.getVal() == val) return selection;
            }
            return null;
        }
    }

    public enum StandardSelection {
        VIEW_FACILITIES(0), SEARCH_FACILITY(1), MY_BOOKINGS(2), NOTIFICATIONS(3), PROFILE(4), REPORT_ISSUE(5), LOGOUT(6), EXIT(7);
        
        private final int val;
        private StandardSelection(int val) { this.val = val; }
        public int getVal() { return this.val; }
        
        public static StandardSelection cast(int val) {
            for (StandardSelection selection : values()) {
                if (selection.getVal() == val) return selection;
            }
            return null;
        }
    }

    public enum AdminSelection {
        MANAGE_BOOKING_SESSIONS(0), PROCESS_BOOKING_REQUESTS(1), MANAGE_FACILITY_MAINTENANCE(2), GENERATE_ANALYTICS_REPORT(3), NOTIFICATIONS(4), PROFILE(5), LOGOUT(6), EXIT(7);
        
        private final int val;
        private AdminSelection(int val) { this.val = val; }
        public int getVal() { return this.val; }
        
        public static AdminSelection cast(int val) {
            for (AdminSelection selection : values()) {
                if (selection.getVal() == val) return selection;
            }
            return null;
        }
    }

    public int getSelectionSize() {
        if (Global.getUser() != null) {
            switch (Global.getUser().getRole()) {
                case STUDENT:
                case STAFF:
                    return StandardSelection.values().length;
                case ADMIN:
                    return AdminSelection.values().length;
            }
        }
        return GuestSelection.values().length;
    }

    @Override
    public void render(StringBuilder frame) {
        if (Global.getUser() != null) {
            frame.append("Welcome ")
                 .append(Global.getUser().getName())
                 .append('!')
                 .append(String.valueOf(' ').repeat(10))
                 .append("Role: ")
                 .append(Global.getUser().getRole().name())
                 .append('\n');
        }

        frame.append('\n')
             .append("💡 Press the arrow keys to navigate the selections.").append('\n')
             .append("💡 Press [Enter] to select.").append('\n')
             .append("💡 Press [ESC] to return to the previous page.").append('\n')
             .append('\n');

        if (Global.getUser() != null) {
            // --- DYNAMIC NOTIFICATION INDICATOR LOGIC ---
            int unreadCount = Global.getUser().getUnreadNotificationCount();
            String notificationLabel = "Notifications";
            if (unreadCount > 0) {
                notificationLabel = "Notifications (" + unreadCount + " New) *";
            }
            // --------------------------------------------

            switch (Global.getUser().getRole()) {
                case STUDENT:
                case STAFF:
                    for (StandardSelection s : StandardSelection.values()) {
                        if (selection == s.getVal())
                            frame.append("> ");
                        else
                            frame.append(String.valueOf(' ').repeat(2));
                        
                        if (s == StandardSelection.NOTIFICATIONS) {
                            frame.append(notificationLabel).append('\n');
                        } else {
                            frame.append(Util.toTitleCase(s.name())).append('\n');
                        }
                    }
                    break;
                case ADMIN:
                    for (AdminSelection s : AdminSelection.values()) {
                        if (selection == s.getVal())
                            frame.append("> ");
                        else
                            frame.append(String.valueOf(' ').repeat(2));
                        
                        if (s == AdminSelection.NOTIFICATIONS) {
                            frame.append(notificationLabel).append('\n');
                        } else {
                            frame.append(Util.toTitleCase(s.name())).append('\n');
                        }
                    }
                    break;
            }
        } else {
            for (GuestSelection s : GuestSelection.values()) {
                if (selection == s.getVal())
                    frame.append("> ");
                else
                    frame.append(String.valueOf(' ').repeat(2));
                
                frame.append(Util.toTitleCase(s.name())).append('\n');
            }
        }
    }

    public void select() {
        if (Global.getUser() != null) {
            switch (Global.getUser().getRole()) {
                case STUDENT:
                case STAFF:
                    switch (StandardSelection.cast(selection)) {
                        case VIEW_FACILITIES:
                            Router.redirect(
                                new ListViewPage(
                                    new ViewFacilityPage(),
                                    Database.Booking.getActiveBookings().keySet().toArray(String[]::new)
                                )
                            );
                            break;
                        case SEARCH_FACILITY:
                            Router.redirect(new SearchFacilityPage());
                            break;
                        case MY_BOOKINGS:
                            Router.redirect(new MyBookingPage());
                            break;
                        case NOTIFICATIONS:
                            Router.redirect(new NotificationPage());
                            break;
                        case PROFILE:
                            Router.redirect(new ProfilePage());
                            break;
                        case REPORT_ISSUE:
                            Router.redirect(
                                new ListViewPage(
                                    new ReportPage(),
                                    Database.Facility.getAvailabFacilities().stream().map(Facility::getName).toArray(String[]::new)
                                )
                            );
                            break;
                        case LOGOUT:
                            logout();
                            break;
                        case EXIT:
                            System.exit(0);
                            break;
                    }
                    break;
                case ADMIN:
                    switch (AdminSelection.cast(selection)) {
                        case MANAGE_BOOKING_SESSIONS:
                            Router.redirect(new ListViewPage(new ConsolePage(), Database.Facility.getAll().stream().map(Facility::getName).toArray(String[]::new)));
                            break;
                        case PROCESS_BOOKING_REQUESTS:
                            Router.redirect(
                                new ListViewPage(
                                    new ProcessBookingPage(),
                                    Database.Booking.getPendingRequests().keySet().toArray(String[]::new)
                                )
                            );
                            break;
                        case MANAGE_FACILITY_MAINTENANCE:
                        {
                            ArrayList<String> activeFacilities = new ArrayList<>();
                            for (String fac : Database.Report.getAll().keySet()) {
                                for (src.models.Report r : Database.Report.getAll().get(fac)) {
                                    if (r.getStatus() != src.models.Report.Status.RESOLVED) {
                                        activeFacilities.add(fac);
                                        break;
                                    }
                                }
                            }

                            ListViewPage reportListPage = new ListViewPage(new ReviewReportPage(), subpage -> {
                                ArrayList<String> pendingTitles = new ArrayList<>();
                                for (src.models.Report r : Database.Report.getAll().get(subpage.getLabel())) {
                                    if (r.getStatus() != src.models.Report.Status.RESOLVED) {
                                        pendingTitles.add(r.getTitle());
                                    }
                                }
                                return pendingTitles.toArray(String[]::new);
                            });

                            reportListPage.setColorMapper((page, index, title) -> {
                                String facilityName = page.getLabel(); 
                                if (facilityName != null && Database.Report.getAll().get(facilityName) != null) {
                                    int current = 0;
                                    for (src.models.Report r : Database.Report.getAll().get(facilityName)) {
                                        if (r.getStatus() != src.models.Report.Status.RESOLVED) {
                                            if (current == index) { 
                                                if (r.getStatus() == src.models.Report.Status.PENDING) return Ansi.FG_RED;
                                                if (r.getStatus() == src.models.Report.Status.IN_PROGRESS) return Ansi.FG_YELLOW;
                                            }
                                            current++;
                                        }
                                    }
                                }
                                return Ansi.FG_WHITE;
                            });

                            Router.redirect(
                                new ListViewPage(
                                    reportListPage,
                                    activeFacilities.toArray(String[]::new)
                                )
                            );
                            break;
                        }
                        case GENERATE_ANALYTICS_REPORT:
                            Router.redirect(new AnalyticsPage());
                            break;
                        case NOTIFICATIONS:
                            Router.redirect(new NotificationPage());
                            break;
                        case PROFILE:
                            Router.redirect(new ProfilePage());
                            break;
                        case LOGOUT:
                            logout();
                            break;
                        case EXIT:
                            System.exit(0);
                            break;
                    }
                    break;
            }
        } else {
            switch (GuestSelection.cast(selection)) {
                case VIEW_FACILITIES:
                    Router.redirect(
                        new ListViewPage(
                            new ViewFacilityPage(),
                            Database.Booking.getActiveBookings().keySet().toArray(String[]::new)
                        )
                    );
                    break;
                case SIGN_IN:
                    Router.redirect(new SignInPage());
                    break;
                case SIGN_UP:
                    Router.redirect(new SignUpPage());
                    break;
                case EXIT:
                    System.exit(0);
                    break;
            }
        }
    }

    @Override
    public void handleAction(String action) {
        switch (action) {
            case "UP":
                if (selection - 1 >= 0) {
                    selection--;
                } else {
                    selection = getSelectionSize() - 1;
                }
                break;
            case "DOWN":
                if (selection + 1 < getSelectionSize()) {
                    selection++;
                } else {
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

    private void logout() {
        selection = 0;
        Global.clearSession();
    }
}