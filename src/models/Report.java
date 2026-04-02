package src.models;

import java.time.LocalDate;

public class Report {
    public enum Severity
    {
        LOW(0), MEDIUM(1), HIGH(2);

        int val = 0;

        private Severity(int val)
        {
            this.val = val;
        }

        public int getValue()
        {
            return val;
        }

        public static Severity cast(int val)
        {
            for (Severity severity : Severity.values())
            {
                if (severity.getValue() == val)
                {
                    return severity;
                }
            }
            return null;
        }
    }
    private String title = "";
    private String description = "";
    private Severity severity = Severity.LOW;
    private String imageBase64 = "";
    private String from = "";
    private String date = "";

    /*
    public Report(String title, String description, Severity severity, Image image)
    {
        this.title = title;
        this.description = description;
        this.severity = severity;
        this.imageBase64 = image.getBase64();
    }
    */

    public Report(String title, String description, Severity severity, String imageBase64, String from)
    {
        this(title, description, severity, imageBase64, from, LocalDate.now().toString());
    }

    public Report(String title, String description, Severity severity, String imageBase64, String from, LocalDate date)
    {
        this(title, description, severity, imageBase64, from, date.toString());
    }

    public Report(String title, String description, Severity severity, String imageBase64, String from, String date)
    {
        this.title =  title;
        this.description = description;
        this.severity = severity;
        this.imageBase64 = imageBase64;
        this.from = from;
        this.date = date;
    }

    public String getTitle()
    {
        return this.title;
    }

    public void setTitle(String title)
    {
        this.title = title;
    }

    public String getDescription()
    {
        return this.description;
    }

    public void setDescription(String description)
    {
        this.description = description;
    }

    public Severity getSeverity()
    {
        return this.severity;
    }

    public void setSeverity(Severity severity)
    {
        this.severity = severity;
    }

    public String getImageBase64()
    {
        return this.imageBase64;
    }

    public void setImageBase64(String imageBase64)
    {
        this.imageBase64 = imageBase64;
    }

    public String getFrom()
    {
        return this.from;
    }

    public void setFrom(String from)
    {
        this.from = from;
    }

    public String getDate()
    {
        return this.date;
    }

    public void setDate(LocalDate date)
    {
        this.date = date.toString();
    }

    public void setDate(String date)
    {
        this.date = date;
    }

    public void setTodayAsDate()
    {
        this.date = LocalDate.now().toString();
    }
}
