package src.components;

public class Ansi {
    public static final String FORMAT = "\u001B[";
    public static final int DEFAULT = 0;
    public static final int FG_BLACK = 30;
    public static final int BG_BLACK = 40;
    public static final int FG_RED = 31;
    public static final int BG_RED = 41;
    public static final int FG_GREEN = 32;
    public static final int BG_GREEN = 42;
    public static final int FG_YELLOW = 33;
    public static final int BG_YELLOW = 43;
    public static final int FG_BLUE = 34;
    public static final int BG_BLUE = 44;
    public static final int FG_MAGENTA = 35;
    public static final int BG_MAGENTA = 45;
    public static final int FG_CYAN = 36;
    public static final int BG_CYAN = 46;
    public static final int FG_LIGHT_GRAY = 37;
    public static final int BG_LIGHT_GRAY = 47;
    public static final int FG_DARK_GRAY = 90;
    public static final int BG_DARK_GRAY = 100;
    public static final int FG_LIGHT_RED = 91;
    public static final int BG_LIGHT_RED = 101;
    public static final int FG_LIGHT_GREEN = 92;
    public static final int BG_LIGHT_GREEN = 102;
    public static final int FG_LIGHT_YELLOW = 93;
    public static final int BG_LIGHT_YELLOW = 103;
    public static final int FG_LIGHT_BLUE = 94;
    public static final int BG_LIGHT_BLUE = 104;
    public static final int FG_LIGHT_MAGENTA = 95;
    public static final int BG_LIGHT_MAGENTA = 105;
    public static final int FG_LIGHT_CYAN = 96;
    public static final int BG_LIGHT_CYAN = 106;
    public static final int FG_WHITE = 97;
    public static final int BG_WHITE = 107;
    public static final int BOLD = 1;
    public static final int UNDERLINE = 4;
    public static final int NO_UNDERLINE = 24;
    public static final int REVERSE_TEXT = 7;
    public static final int POSITIVE_TEXT = 27;

    String text;
    int[] codes;

    public Ansi(String text, int... codes)
    {
        this.text = text;
        this.codes = codes;
    }

    @Override
    public String toString()
    {
        if (this.codes.length == 0)
            return this.text;

        StringBuilder ansiBuilder = new StringBuilder(FORMAT);

        for (int i = 0; i < this.codes.length; i++) {
            ansiBuilder.append(Integer.toString(this.codes[i]));

            if (i < this.codes.length - 1)
                ansiBuilder.append(';');
        }

        ansiBuilder.append('m').append(this.text).append(FORMAT).append(DEFAULT).append('m');

        return ansiBuilder.toString();
    }

    public static String toString(int code)
    {
        StringBuilder ansiBuilder = new StringBuilder();

        ansiBuilder.append(FORMAT).append(code).append('m');

        return ansiBuilder.toString();
    }

    public int length()
    {
        return this.text.length();
    }

    public Ansi[] split(String regex)
    {
        String[] substrings = text.split(regex);
        Ansi[] result = new Ansi[substrings.length];

        for (int i = 0; i < substrings.length; i++)
        {
            result[i] = new Ansi(substrings[i], codes);
        }

        return result;
    }
}
