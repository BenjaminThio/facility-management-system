package src.pages.admin;

import java.time.LocalDate;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import src.components.Ansi;
import src.components.AnsiBuilder;
import src.components.Container;
import src.components.Table;
import src.pages.cores.Page;
import src.pages.ListViewPage;
import src.utils.Database;
import src.utils.Renderer;
import src.utils.Router;

public class AnalyticsPage extends Page {

    public enum TimeFrame {
        LIFETIME("Lifetime (All Records)"),
        CURRENT_MONTH("Current Month"),
        CURRENT_SEMESTER("Current Semester (Last 6 Months)"),
        CURRENT_YEAR("Current Year");

        private String label;
        TimeFrame(String label) { this.label = label; }
        public String getLabel() { return label; }
        
        public static TimeFrame cast(int val) {
            if (val >= 0 && val < values().length) return values()[val];
            return LIFETIME;
        }
    }

    private TimeFrame selectedTimeFrame = TimeFrame.LIFETIME;
    // --- UPDATED: We now have 2 selectable items (Filter & Detailed View Button) ---
    private static final int MAX_SELECTION = 2; 
    // -------------------------------------------------------------------------------

    public AnalyticsPage() {
        this.selection = 0;
    }

    private boolean isDateInTimeFrame(String dateStr, TimeFrame tf) {
        if (tf == TimeFrame.LIFETIME) return true;
        
        try {
            LocalDate date = LocalDate.parse(dateStr);
            LocalDate now = LocalDate.now();
            
            return switch(tf) {
                case CURRENT_MONTH -> date.getYear() == now.getYear() && date.getMonthValue() == now.getMonthValue();
                case CURRENT_SEMESTER -> !date.isBefore(now.minusMonths(6)); 
                case CURRENT_YEAR -> date.getYear() == now.getYear();
                default -> true;
            };
        } catch (Exception e) {
            return false; 
        }
    }

    @Override
    public void render(StringBuilder frame) {
        ArrayList<ArrayList<AnsiBuilder>> table = new ArrayList<>();

        table.add(new ArrayList<>(Arrays.asList(
            new AnsiBuilder().append(new Container("Advanced Analytics Report", 72, Container.Alignment.CENTER, Ansi.BG_BLACK, Ansi.FG_LIGHT_GRAY).toAnsi())
        )));

        int totalSlots = 0;
        int usedSlots = 0;
        HashMap<String, Integer> peakHours = new HashMap<>();
        HashMap<String, Integer> typeUsage = new HashMap<>();

        @SuppressWarnings("unchecked")
        Map<String, LinkedHashMap<String, LinkedHashMap<String, src.models.Booking>>> allBookings = 
            (Map<String, LinkedHashMap<String, LinkedHashMap<String, src.models.Booking>>>) (Object) Database.Booking.getAll();

        for (Map.Entry<String, LinkedHashMap<String, LinkedHashMap<String, src.models.Booking>>> facEntry : allBookings.entrySet()) {
            String facName = facEntry.getKey();
            src.models.Facility fModel = Database.Facility.get(facName);
            String facType = (fModel != null && fModel.getType() != null) ? fModel.getType().getName() : "General";

            for (Map.Entry<String, LinkedHashMap<String, src.models.Booking>> dateEntry : facEntry.getValue().entrySet()) {
                String dateStr = dateEntry.getKey();
                
                if (!isDateInTimeFrame(dateStr, selectedTimeFrame)) continue; 

                for (Map.Entry<String, src.models.Booking> sessionEntry : dateEntry.getValue().entrySet()) {
                    totalSlots++;
                    src.models.Booking session = sessionEntry.getValue();
                    
                    if (session.getApproved() != null) {
                        usedSlots++;
                        String timeStr = sessionEntry.getKey();
                        peakHours.put(timeStr, peakHours.getOrDefault(timeStr, 0) + 1);
                        typeUsage.put(facType, typeUsage.getOrDefault(facType, 0) + 1);
                    }
                }
            }
        }

        HashMap<String, Integer> maintenanceCases = new HashMap<>();
        int totalRepairMinutes = 0;
        int resolvedReports = 0;

        for (Map.Entry<String, List<src.models.Report>> reportList : Database.Report.getAll().entrySet()) {
            String facName = reportList.getKey();
            for (src.models.Report r : reportList.getValue()) {
                
                if (!isDateInTimeFrame(r.getDate(), selectedTimeFrame)) continue;

                maintenanceCases.put(facName, maintenanceCases.getOrDefault(facName, 0) + 1);
                
                if (r.getStatus() == src.models.Report.Status.RESOLVED) {
                    resolvedReports++;
                    totalRepairMinutes += r.getRepairMinutes();
                }
            }
        }

        String topPeakHour = "N/A"; int maxPeak = 0;
        for (Map.Entry<String, Integer> e : peakHours.entrySet()) if (e.getValue() > maxPeak) { maxPeak = e.getValue(); topPeakHour = e.getKey(); }

        String topType = "N/A"; int maxType = 0;
        for (Map.Entry<String, Integer> e : typeUsage.entrySet()) if (e.getValue() > maxType) { maxType = e.getValue(); topType = e.getKey(); }

        String worstFacility = "N/A"; int maxMaint = 0;
        for (Map.Entry<String, Integer> e : maintenanceCases.entrySet()) if (e.getValue() > maxMaint) { maxMaint = e.getValue(); worstFacility = e.getKey(); }

        double utilRate = totalSlots == 0 ? 0.0 : ((double) usedSlots / totalSlots) * 100.0;
        
        int avgRepairMinutes = resolvedReports == 0 ? 0 : (totalRepairMinutes / resolvedReports);
        int avgHrsDisplay = avgRepairMinutes / 60;
        int avgMinsDisplay = avgRepairMinutes % 60;
        String repairTimeStr = String.format("%d Hrs %d Mins", avgHrsDisplay, avgMinsDisplay);

        AnsiBuilder content = new AnsiBuilder();
        
        String filterText = "   Timeframe Filter : [ " + selectedTimeFrame.getLabel() + " ]";
        content.append(new Container(filterText, 72, Container.Alignment.LEFT, selection == 0 ? Ansi.BG_LIGHT_GREEN : Ansi.BG_WHITE, Ansi.FG_BLACK).toAnsi()).append("\n");
        content.append(new Container("", 72, Container.Alignment.LEFT, Ansi.BG_WHITE, Ansi.FG_BLACK).toAnsi()).append("\n");

        content.append(new Container(" Booking Trends & Utilization ", 72, Container.Alignment.LEFT, Ansi.BG_DARK_GRAY, Ansi.FG_WHITE).toAnsi()).append("\n");
        content.append(new Container(String.format("   Overall Utilization Rate : %.1f%% (%d/%d slots)", utilRate, usedSlots, totalSlots), 72, Container.Alignment.LEFT, Ansi.BG_WHITE, Ansi.FG_BLACK).toAnsi()).append("\n");
        content.append(new Container(String.format("   Peak Booking Hour        : %s (%d bookings)", topPeakHour, maxPeak), 72, Container.Alignment.LEFT, Ansi.BG_WHITE, Ansi.FG_BLACK).toAnsi()).append("\n");
        content.append(new Container(String.format("   Most Popular Type        : %s (%d bookings)", topType, maxType), 72, Container.Alignment.LEFT, Ansi.BG_WHITE, Ansi.FG_BLACK).toAnsi()).append("\n");
        
        content.append(new Container("", 72, Container.Alignment.LEFT, Ansi.BG_WHITE, Ansi.FG_BLACK).toAnsi()).append("\n");

        content.append(new Container(" Maintenance & Reliability ", 72, Container.Alignment.LEFT, Ansi.BG_DARK_GRAY, Ansi.FG_WHITE).toAnsi()).append("\n");
        content.append(new Container(String.format("   Most Maintenance Cases   : %s (%d cases)", worstFacility, maxMaint), 72, Container.Alignment.LEFT, Ansi.BG_WHITE, Ansi.FG_BLACK).toAnsi()).append("\n");
        content.append(new Container(String.format("   Average Repair Time      : %s", repairTimeStr), 72, Container.Alignment.LEFT, Ansi.BG_WHITE, Ansi.FG_BLACK).toAnsi()).append("\n");
        
        // --- NEW: Detailed View Button ---
        content.append(new Container("", 72, Container.Alignment.LEFT, Ansi.BG_WHITE, Ansi.FG_BLACK).toAnsi()).append("\n");
        content.append(new Container("    View Detailed Facility Breakdown >>    ", 72, Container.Alignment.CENTER, selection == 1 ? Ansi.BG_LIGHT_GREEN : Ansi.BG_DARK_GRAY, selection == 1 ? Ansi.FG_BLACK : Ansi.FG_WHITE).toAnsi());
        // ---------------------------------

        table.add(new ArrayList<>(Arrays.asList(content)));
        Table.render(frame, table);
    }

    @Override
    public void handleAction(String action) {
        switch (action) {
            case "UP" -> {
                if (selection > 0) selection--;
                else selection = MAX_SELECTION - 1;
            }
            case "DOWN" -> {
                if (selection < MAX_SELECTION - 1) selection++;
                else selection = 0;
            }
            case "ESC" -> Router.back();
            case "ENTER" -> {
                if (selection == 0) {
                    Router.redirect(new ListViewPage(
                        Arrays.stream(TimeFrame.values()).map(TimeFrame::getLabel).toArray(String[]::new),
                        (index) -> {
                            this.selectedTimeFrame = TimeFrame.cast(index);
                        }
                    ));
                } else if (selection == 1) {
                    // --- NEW: Route to Detailed View ---
                    Router.redirect(new DetailedAnalyticsPage());
                }
            }
        }
        Renderer.refresh();
    }
}