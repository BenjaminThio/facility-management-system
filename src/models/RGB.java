package src.models;

public class RGB
{
    private int r;
    private int g;
    private int b;

    public RGB(int r, int g, int b)
    {
        this.r = r;
        this.g = g;
        this.b = b;
    }

    public int getRed()
    {
        return this.r;
    }

    public int getGreen()
    {
        return this.g;
    }

    public int getBlue()
    {
        return this.b;
    }
}
