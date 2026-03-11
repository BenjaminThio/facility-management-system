package src.models;

import java.time.LocalDate;

public class UserBooking {
    private String facilityName;
    private String date;
    private String session;

    public UserBooking(String facilityName, String date, String session)
    {
        this.facilityName = facilityName;
        this.date = date;
        this.session = session;
    }

    public UserBooking(String facilityName, LocalDate date, String session)
    {
        this(facilityName, date.toString(), session);
    }

    public String getFacilityName()
    {
        return this.facilityName;
    }

    public String getDate()
    {
        return this.date;
    }

    public String getSession()
    {
        return this.session;
    }

    @Override
    public String toString()
    {
        return "Facility Name: " + facilityName + "\nDate: " + date + "\nSession: " + session;
    }
}
