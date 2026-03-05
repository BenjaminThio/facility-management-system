package src.models;

public class Report {
    public enum Severity
    {
        LOW,
        MEDIUM,
        HIGH
    }
    private String title = "";
    private String description = "";
    private Severity severity = Severity.LOW;

    public Report(String title, String description, Severity severity)
    {
        this.title = title;
        this.description = description;
        this.severity = severity;
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
}
