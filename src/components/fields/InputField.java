package src.components.fields;

import org.jline.utils.InfoCmp.Capability;

import src.components.Ansi;
import src.components.AnsiBuilder;
import src.components.fields.core.Field;
import src.models.Position;
import src.utils.Global;
import src.utils.Renderer;

public class InputField extends Field {
    private String value = "";
    private int caretPosition = 0;
    private int caretOffset = 0; // x offset
    private boolean isVisible = true;

    public InputField copy()
    {
        InputField clone = new InputField();

        clone.length = this.length;
        clone.placeholder = this.placeholder;
        clone.backgroundColor = this.backgroundColor;
        clone.textColor = this.textColor;
        clone.placeholderColor = this.placeholderColor;
        clone.error = this.error;

        clone.value = this.value;
        clone.caretPosition = this.caretPosition;
        clone.caretOffset = this.caretOffset;
        clone.isVisible = this.isVisible;

        return clone;
    }

    public InputField() {}

    public InputField(int length, String placeholder)
    {
        this.length = length;
        this.placeholder = placeholder;
    }

    public InputField(int length, String placeholder, boolean isVisible)
    {
        this.length = length;
        this.placeholder = placeholder;
        this.isVisible = isVisible;
    }

    public InputField(String defaultValue, int length, String placeholder, boolean isVisible)
    {
        this(length, placeholder, isVisible);
        this.value = defaultValue;

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

    @Override
    public String getValue()
    {
        return value.trim();
    }

    @Override
    public void render(StringBuilder frame)
    {
        String val = this.isVisible ? value : "*".repeat(this.value.length());

        if (val.isEmpty())
        {
            if (this.placeholder.isEmpty())
                frame.append(new Ansi(" ".repeat(this.length), backgroundColor).toString());
            else if (this.placeholder.length() < this.length)
                frame.append(new Ansi(this.placeholder + " ".repeat(this.length - this.placeholder.length()), backgroundColor, placeholderColor).toString());
            else
                frame.append(new Ansi(this.placeholder.substring(0, this.length - 1) + "…", backgroundColor, placeholderColor).toString());
        }
        else if (val.length() < this.length)
        {
            frame.append(new Ansi(val + " ".repeat(this.length - val.length()), backgroundColor, textColor).toString());
        }
        else
        {
            frame.append(new Ansi(val.substring(this.caretOffset, Math.max(this.caretPosition + this.caretOffset, this.length)), backgroundColor, Ansi.FG_BLACK).toString());
        }
        frame.append('\n');
    }

    @Override
    public AnsiBuilder toAnsiBuilder()
    {
        AnsiBuilder ansiBuilder = new AnsiBuilder();

        String val = this.isVisible ? value : "*".repeat(this.value.length());       

        if (val.isEmpty())
        {
            if (this.placeholder.isEmpty())
                ansiBuilder.append(new Ansi(" ".repeat(this.length), backgroundColor));
            else if (this.placeholder.length() < this.length)
                ansiBuilder.append(new Ansi(this.placeholder + " ".repeat(this.length - this.placeholder.length()), backgroundColor, placeholderColor));
            else
                ansiBuilder.append(new Ansi(this.placeholder.substring(0, this.length - 1) + "…", backgroundColor, placeholderColor));
        }
        else if (val.length() < this.length)
        {
            ansiBuilder.append(new Ansi(val + " ".repeat(this.length - val.length()), backgroundColor, textColor));
        }
        else
        {
            ansiBuilder.append(new Ansi(val.substring(this.caretOffset, Math.max(this.caretPosition + this.caretOffset, this.length)), backgroundColor, Ansi.FG_BLACK));
        }
        ansiBuilder.append('\n');

        return ansiBuilder;
    }

    @Override
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
                if (caretPosition + caretOffset > 0 && !this.value.isEmpty())
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

    @Override
    public void updateCaret(int offsetX, int offsetY)
    {
        Global.getTerminal().puts(Capability.cursor_address, offsetY, offsetX + caretPosition);
    }

    @Override
    public void updateCaret(Position offset)
    {
        updateCaret(offset.x, offset.y);
    }

    public void setVisibility(boolean isVisible)
    {
        this.isVisible = isVisible;
    }

    public boolean getVisibility()
    {
        return this.isVisible;
    }
}
