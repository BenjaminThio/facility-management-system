package src.models;

public class Facility {
    private String name;
    private boolean available;

    public Facility(String name, boolean available)
    {
        this.name = name;
        this.available = available;
    }

    public String getName()
    {
        return this.name;
    }

    public boolean isAvailable()
    {
        return this.available;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public void setIsAvailable(boolean available)
    {
        this.available = available;
    }
}
