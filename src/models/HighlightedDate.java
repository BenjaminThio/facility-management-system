package src.models;

import java.time.LocalDate;

import src.components.Ansi;

public class HighlightedDate {
    private LocalDate date;
    private int textColor = Ansi.FG_BLACK;
    private int backgroundColor = Ansi.BG_WHITE;

    public HighlightedDate(LocalDate date, int textColor, int backgroundColor)
    {
        this.date = date;
        this.textColor = textColor;
        this.backgroundColor = backgroundColor;
    }

    public LocalDate getDate()
    {
        return this.date;
    }

    public int getTextColor()
    {
        return this.textColor;
    }

    public int getBackgroundColor()
    {
        return this.backgroundColor;
    }
}