package src.components.fields.core;

import src.components.Ansi;
import src.components.AnsiBuilder;
import src.models.Position;

public abstract class Field {
    protected int length = 20;
    protected String placeholder = "";
    protected int backgroundColor = Ansi.BG_WHITE;
    protected int textColor = Ansi.FG_BLACK;
    protected int placeholderColor = Ansi.FG_DARK_GRAY;
    protected boolean error = false;

    public void setError(boolean error)
    {
        this.error = error;
        setBackgroundColor(error ? Ansi.BG_RED : Ansi.BG_WHITE);
    }

    public boolean error()
    {
        return this.error;
    }

    public void setBackgroundColor(int ansiCode)
    {
        this.backgroundColor = ansiCode;
    }

    public void setTextColor(int ansiCode)
    {
        this.textColor = ansiCode;
    }

    public void setPlaceholderColor(int ansiCode)
    {
        this.placeholderColor = ansiCode;
    }

    abstract public String getValue();
    abstract public void render(StringBuilder frame);
    abstract public void handleInput(String input);
    abstract public void updateCaret(int offsetX, int offsetY);
    abstract public void updateCaret(Position offset);
    abstract public Field copy();
    abstract public AnsiBuilder toAnsiBuilder();
    public int length()
    {
        return length;
    }
}
