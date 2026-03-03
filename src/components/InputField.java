package src.components;

import org.jline.utils.InfoCmp.Capability;

import src.models.Position;
import src.utils.Global;
import src.utils.Renderer;

public class InputField {
    private String value = "";
    private int length = 10;
    private String placeholder = "";
    private int caretPosition = 0;
    private int caretOffset = 0; // x offset
    private int backgroundColor = Ansi.BG_WHITE;
    private int textColor = Ansi.FG_BLACK;
    private int placeholderColor = Ansi.FG_DARK_GRAY;
    private boolean error = false;

    public InputField(String defaultValue, int length, String placeholder)
    {
        this.value = defaultValue;
        this.length = length;
        this.placeholder = placeholder;

        if (!defaultValue.isEmpty())
        {
            if (defaultValue.length() <= this.length)
            {
                this.caretPosition = this.value.length();
            }
            else
            {
                this.caretPosition = this.length;
                this.caretOffset = defaultValue.length() - this.length;
            }
        }
    }

    public String getValue()
    {
        return value;
    }

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
        backgroundColor = ansiCode;
    }

    public void setTextColor(int ansiCode)
    {
        textColor = ansiCode;
    }

    public void render()
    {
        if (this.value.isEmpty())
        {
            if (this.placeholder.isEmpty())
                System.out.print(new Ansi(" ".repeat(this.length), backgroundColor).toString());
            else if (this.placeholder.length() < this.length)
                System.out.print(new Ansi(this.placeholder + " ".repeat(this.length - this.placeholder.length()), backgroundColor, placeholderColor).toString());
            else
                System.out.print(new Ansi(this.placeholder.substring(0, this.length - 1) + "…", backgroundColor, placeholderColor).toString());
        }
        else if (this.value.length() < this.length)
        {
            System.out.print(new Ansi(this.value + " ".repeat(this.length - this.value.length()), backgroundColor, textColor).toString());
        }
        else
        {
            System.out.print(new Ansi(this.value.substring(this.caretOffset, Math.max(this.caretPosition + this.caretOffset, this.length)), backgroundColor, Ansi.FG_BLACK).toString());
        }
        System.out.println();
    }

    public void handleInput(String input)
    {
        switch (input)
        {
            case "LEFT":
                if (this.caretOffset - 1 >= 0)
                {
                    this.caretOffset--;
                }
                else if (this.caretPosition - 1 >= 0)
                {
                    this.caretPosition--;
                }
                break;
            case "RIGHT":
                if (this.caretPosition + 1 <= Math.min(this.value.length(), this.length))
                {
                    this.caretPosition++;
                }
                else if (this.caretPosition + this.caretOffset + 1 <= this.value.length())
                {
                    this.caretOffset++;
                }
                break;
            case "BACKSPACE":
                if (!this.value.isEmpty())
                {
                    StringBuilder stringBuilder = new StringBuilder(value);

                    stringBuilder.deleteCharAt(caretPosition + caretOffset - 1);
                    this.value = stringBuilder.toString();

                    if (this.caretOffset - 1 >= 0)
                    {
                        this.caretOffset--;
                    }
                    else if (caretPosition - 1 >= 0)
                    {
                        this.caretPosition--;
                    }
                }
                break;
            default:
                switch (input.length())
                {
                    case 1:
                        StringBuilder stringBuilder = new StringBuilder(this.value);

                        stringBuilder.insert(caretPosition + caretOffset, input);
                        this.value = stringBuilder.toString();

                        if (this.caretPosition + 1 <= this.length)
                            this.caretPosition++;
                        else
                            this.caretOffset++;
                        break;
                }
                break;
        }
        Renderer.refresh();
    }

    public void updateCaret(int offsetX, int offsetY)
    {
        Global.terminal.puts(Capability.cursor_address, offsetY, offsetX + caretPosition);
    }

    public void updateCaret(Position offset)
    {
        updateCaret(offset.x, offset.y);
    }
}
