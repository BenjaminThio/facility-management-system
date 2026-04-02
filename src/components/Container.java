package src.components;

import src.utils.Util;

public class Container {
    public enum Alignment
    {
        LEFT,
        CENTER,
        RIGHT
    }
    private String text;
    private int size;
    private Alignment alignment;
    private int backgroundColor = Ansi.BG_WHITE;
    private int textColor = Ansi.FG_BLACK;

    public Container(String text, int size, Alignment alignment)
    {
        this.text = text;
        this.size = size;
        this.alignment = alignment;
    }

    public Container(String text, int size, Alignment alignment, int backgroundColor, int textColor)
    {
        this(text, size, alignment);
        this.backgroundColor = backgroundColor;
        this.textColor = textColor;
    }

    public Ansi toAnsi()
    {
        if (size < 0 || size == 0)
        {
            return new Ansi();
        }

        if (size < text.length())
        {
            return new Ansi(Util.textOverflowEllipsis(text, size), backgroundColor, textColor);
        }
        else if (size == text.length())
        {
            return new Ansi(text, backgroundColor, textColor);
        }
        else // if (size > text.length())
        {
            return switch (alignment)
            {
                case LEFT ->
                    new Ansi(text + " ".repeat(size - text.length()), backgroundColor, textColor);
                case CENTER -> {
                    int padding = (size - text.length()) / 2;

                    if ((size - text.length()) % 2 == 0)
                    {
                        yield new Ansi(" ".repeat(padding) + text + " ".repeat(padding), backgroundColor, textColor);
                    }
                    else
                    {
                        yield new Ansi(" ".repeat(padding) + text + " ".repeat(padding + 1), backgroundColor, textColor);
                    }
                }
                case RIGHT ->
                    new Ansi(" ".repeat(size - text.length()) + text, backgroundColor, textColor);
                default -> null;
            };
        }
    }

    public static void main(String[] args) {
        Container container = new Container("Benjamin", 7, Alignment.CENTER);

        System.out.println(container.toAnsi() == null ? null : container.toAnsi().toString());
    }
}
