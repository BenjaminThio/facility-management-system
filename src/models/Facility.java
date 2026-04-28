package src.models;

public class Facility
{
    public enum Type {
        LECTURE_HALL("Lecture Hall"),
        DISCUSSION_ROOM("Discussion Room"),
        SPORTS_COURT("Sports Court"),
        COMPUTER_LAB("Computer Lab"),
        MULTIPURPOSE_HALL("Multipurpose Hall"),
        GENERAL("General");

        private final String name;

        Type(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public static Type cast(int val) {
            if (val >= 0 && val < values().length) {
                return values()[val];
            }
            return GENERAL;
        }
    }

    private String name;
    private boolean available;
    private Type type = Type.GENERAL;

    public Facility(String name, boolean available)
    {
        this.name = name;
        this.available = available;
    }

    public Facility(String name, boolean available, Type type)
    {
        this(name, available);
        this.type = type;
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

    public Type getType()
    {
        return type;
    }
    public void setType(Type type)
    {
        this.type = type;
    }
}
/*
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
*/