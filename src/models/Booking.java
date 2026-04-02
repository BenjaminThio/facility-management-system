package src.models;

import java.util.ArrayList;

public class Booking {
    private ArrayList<BookingInfo> pending;
    private String approved;
    private boolean expired = false;

    public Booking copy()
    {
        Booking clone = new Booking();

        if (this.pending != null)
            clone.pending = new ArrayList<BookingInfo>(this.pending);

        clone.approved = this.approved;

        return clone;
    }

    public void setIsExpired(boolean expired)
    {
        this.expired = expired;
    }

    public boolean isExpired()
    {
        return this.expired;
    }

    public Booking()
    {
        this.pending = new ArrayList<>();
    }

    public Booking(ArrayList<BookingInfo> pending, String approved, boolean expired)
    {
        this.pending = pending;
        this.approved = approved;
        this.expired = expired;
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