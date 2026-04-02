package src.models;

public class SpecialEmoji {
    public static final SpecialEmoji EYE = new SpecialEmoji("👁 ", 2);

    private String emoji;
    private int length;

    public SpecialEmoji(String emoji, int length)
    {
        this.emoji = emoji;
        this.length = length;
    }

    public String getEmoji()
    {
        return this.emoji;
    }

    public int length()
    {
        return this.length;
    }
}
