package src.pages;

import java.time.LocalDate;

import src.components.Calendar;
import src.pages.core.Page;
import src.utils.Global;

public class ViewFacilityPage extends Page {
    int facilityIdx = 0;
    String[] timestamps = {
        "9:00a.m-10:00a.m",
        "10:00a.m-11:00a.m",
        "11:00a.m-12:00p.m",
        "12:00p.m-1:00p.m",
        "1:00p.m-2:00p.m",
        "2:00p.m-3:00p.m",
        "3:00p.m-4:00p.m",
        "4:00p.m-5:00p.m",
        "5:00p.m-6:00p.m",
        "6:00p.m-7:00p.m",
        "7:00p.m-8:00p.m",
    };

    public ViewFacilityPage(int facilityIdx)
    {
        this.facilityIdx = facilityIdx;
    }

    public void render()
    {
        System.out.println("Facility Name: " + Global.facilities.get(facilityIdx).getName());
        System.out.println();
        Calendar.printCalender(LocalDate.now());
        System.out.println();
        for (String timestamp : timestamps)
        {
            System.out.println(timestamp);
        }
    }

    public void select()
    {

    }

    public void handleAction(String action)
    {

    }
}
