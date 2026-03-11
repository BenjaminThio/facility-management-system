package src.models;

public class BookingInfo {
    private String email;
    private String remark;

    public BookingInfo(String email)
    {
        this.email = email;
    }

    public BookingInfo(String email, String remark)
    {
        this.email = email;
        this.remark = remark;
    }

    public String getEmail()
    {
        return this.email;
    }

    public String getRemark()
    {
        return this.remark;
    }
}
