package src.models;

import java.util.ArrayList;

public class User {
    public enum Role
    {
        STUDENT(0), STAFF(1), ADMIN(2);

        int val = 0;

        private Role(int val)
        {
            this.val = val;
        }

        public int getVal()
        {
            return this.val;
        }

        public static Role cast(int val)
        {
            for (Role role : Role.values())
            {
                if (role.getVal() == val)
                {
                    return role;
                }
            }
            return null;
        }

        public static Role cast(String val)
        {
            for (Role role : Role.values())
            {
                if (role.name() == val)
                {
                    return role;
                }
            }
            return null;
        }
    }
    private String name;
    private String email;
    private String password;
    private String phoneNumber;
    private Role role;
    private ArrayList<UserBooking> bookings = new ArrayList<>();

    public User(String name, String email, String password, String phoneNumber, Role role)
    {
        this.name = name;
        this.email = email.toLowerCase();
        this.password = password;
        this.role = role;
    }

    public User(String name, String email, String password, Role role, String phoneNumber, ArrayList<UserBooking> bookings)
    {
        this(name, email.toLowerCase(), password, phoneNumber, role);
        this.bookings = bookings;
    }

    public String getName()
    {
        return this.name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getEmail()
    {
        return this.email.toLowerCase();
    }

    public void setEmail(String email)
    {
        this.email = email.toLowerCase();
    }

    public String getPassword()
    {
        return this.password;
    }

    public void setPassword(String password)
    {
        this.password = password;
    }

    public String getPhoneNumber()
    {
        return this.phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber)
    {
        this.phoneNumber = phoneNumber;
    }

    public Role getRole()
    {
        return this.role;
    }

    public ArrayList<UserBooking> getBookings()
    {
        return this.bookings;
    }

    public void setBookings(ArrayList<UserBooking> bookings)
    {
        this.bookings = bookings;
    }

    public void addBooking(UserBooking booking)
    {
        this.bookings.add(booking);
    }
}