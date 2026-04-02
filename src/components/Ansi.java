package src.components;

import java.util.ArrayList;
import java.util.Arrays;

import src.models.RGB;
import src.models.SpecialEmoji;

public class Ansi {
    public static final String RAW_FORMAT = "\\u001B[";
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
    int customLength = -1;

    public Ansi background(RGB rgb)
    {
        ArrayList<Integer> newCodes = new ArrayList<>();

        if (this.codes != null)
        {
            for (int code : this.codes)
            {
                newCodes.add(code);
            }
        }

        newCodes.add(48);
        newCodes.add(2);
        newCodes.add(rgb.getRed());
        newCodes.add(rgb.getGreen());
        newCodes.add(rgb.getBlue());

        this.codes = new int[newCodes.size()];

        for (int i = 0; i < newCodes.size(); i++)
        {
            this.codes[i] = newCodes.get(i);
        }

        return this;
    }

    public Ansi foreground(RGB rgb)
    {
        ArrayList<Integer> newCodes = new ArrayList<>();

        if (this.codes != null)
        {
            for (int code : this.codes)
            {
                newCodes.add(code);
            }
        }

        newCodes.add(38);
        newCodes.add(2);
        newCodes.add(rgb.getRed());
        newCodes.add(rgb.getGreen());
        newCodes.add(rgb.getBlue());

        this.codes = new int[newCodes.size()];

        for (int i = 0; i < newCodes.size(); i++)
        {
            this.codes[i] = newCodes.get(i);
        }

        return this;
    }

    public Ansi(String text)
    {
        this.text = text;
        this.codes = new int[0];
    }

    public Ansi(String text, int... codes)
    {
        this.text = text;
        this.codes = codes;
    }

    public Ansi(char c, int... codes)
    {
        this.text = Character.toString(c);
        this.codes = codes;
    }

    public Ansi(int ...integers)
    {
        this.text = Integer.toString(integers[0]);
        this.codes = Arrays.copyOfRange(integers, 1, integers.length);
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

    public String getRaw()
    {
        if (this.codes.length == 0)
            return this.text;

        StringBuilder ansiBuilder = new StringBuilder(RAW_FORMAT);

        for (int i = 0; i < this.codes.length; i++) {
            ansiBuilder.append(Integer.toString(this.codes[i]));

            if (i < this.codes.length - 1)
                ansiBuilder.append(';');
        }

        ansiBuilder.append('m').append(this.text).append(RAW_FORMAT).append(DEFAULT).append('m');

        return ansiBuilder.toString();
    }

    public Ansi(SpecialEmoji emoji, int... codes)
    {
        this.text = emoji.getEmoji();
        this.codes = codes;
        this.customLength = emoji.length();
    }

    public int length()
    {
        return this.customLength != -1 ? this.customLength : this.text.length();
    }

    public Ansi[] split(String regex)
    {
        String[] substrings = text.split(regex, -1);
        Ansi[] result = new Ansi[substrings.length];

        for (int i = 0; i < substrings.length; i++)
        {
            result[i] = new Ansi(substrings[i], codes);

            if (substrings.length == 1) {
                result[i].customLength = this.customLength;
            }
        }

        return result;
    }

    /*
    public int length()
    {
        return this.text.length();
    }

    public Ansi[] split(String regex)
    {
        String[] substrings = text.split(regex, -1);
        Ansi[] result = new Ansi[substrings.length];

        for (int i = 0; i < substrings.length; i++)
        {
            result[i] = new Ansi(substrings[i], codes);
        }

        return result;
    }
    */

    /*
    public static void main(String[] args) {
        Ansi ansi = new Ansi("Test")
            .background(new RGB(0, 255, 0))
            .foreground(new RGB(0, 0, 0));

        System.out.println(ansi.getRaw());
    }
    */
}
