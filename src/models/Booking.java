package src.models;

import java.util.ArrayList;

public class Booking {
    private ArrayList<BookingInfo> pending;
    private String approved;

    public Booking()
    {
        this.pending = new ArrayList<>();
    }

    public Booking(ArrayList<BookingInfo> pending, String approved)
    {
        this.pending = pending;
        this.approved = approved;
    }

    public ArrayList<BookingInfo> getPending()
    {
        return this.pending;
    }

    public void setPending(ArrayList<BookingInfo> pending)
    {
        this.pending = pending;
    }

    public void addPending(BookingInfo info) {
        this.pending.add(info);
    }

    public String getApproved()
    {
        return this.approved;
    }

    public void setApproved(String email)
    {
        this.approved = email;
    }

    public boolean pendingContains(String email)
    {
        for (BookingInfo bookingInfo : this.pending)
        {
            if (bookingInfo.getEmail().equals(email))
            {
                return true;
            }
        }
        return false;
    }
}