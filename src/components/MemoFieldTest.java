package src.components;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.jline.utils.InfoCmp.Capability;

import src.models.Position;
import src.utils.Global;
import src.utils.Renderer;

public class MemoFieldTest {
    private List<String> lines = new ArrayList<>(Arrays.asList(""));
    private int minHeight = 10;
    private int length = 20;
    private int height = 10;
    private String placeholder = "";
    private Position caretPosition = new Position();
    private Position caretOffset = new Position();
    private int backgroundColor = Ansi.BG_WHITE;
    private int textColor = Ansi.FG_BLACK;
    private int placeholderColor = Ansi.FG_DARK_GRAY;
    private boolean overflowX = false;
    // private boolean overflowY = false;

    public MemoFieldTest(int minHeight, int length, String placeholder, boolean overflowX)
    {
        this.minHeight = minHeight;
        this.length = length;
        this.placeholder = placeholder;
        this.overflowX = overflowX;
    }

    public void render()
    {
        for (int i = 0; i < Math.max(lines.size(), minHeight); i++)
        {
            if (i < lines.size())
            {
                if (i == 0 && lines.size() == 1 && lines.get(0).isEmpty())
                {
                    if (this.placeholder.isEmpty())
                        System.out.print(new Ansi(" ".repeat(this.length), backgroundColor).toString());
                    else if (this.placeholder.length() < this.length)
                        System.out.print(new Ansi(this.placeholder + " ".repeat(this.length - this.placeholder.length()), backgroundColor, placeholderColor).toString());
                    else
                        System.out.print(new Ansi(this.placeholder.substring(0, this.length - 1) + "…", backgroundColor, placeholderColor).toString());
                }
                else if (lines.get(i).length() < this.length)
                {
                    System.out.print(new Ansi(lines.get(i) + " ".repeat(this.length - lines.get(i).length()), backgroundColor, textColor).toString());
                }
                else
                {
                    System.out.print(new Ansi(lines.get(i).substring(this.caretOffset.x, Math.max(this.caretPosition.x + this.caretOffset.x, this.length)), backgroundColor, textColor).toString());
                }
            }
            else
            {
                System.out.print(new Ansi(" ".repeat(this.length), backgroundColor).toString());
            }
            System.out.println();
        }
        Global.terminal.puts(Capability.cursor_address, this.caretPosition.y, this.caretPosition.x);
    }

    public void handleInput(String input)
    {
        switch (input)
        {
            case "LEFT":
            {
                if (this.caretOffset.x - 1 >= 0)
                {
                    this.caretOffset.x--;
                }
                else if (this.caretPosition.x - 1 >= 0)
                {
                    this.caretPosition.x--;
                }
                break;
            }
            case "RIGHT":
            {
                if (this.caretPosition.x + 1 <= Math.min(this.lines.get(caretPosition.y + caretOffset.y).length(), this.length))
                {
                    this.caretPosition.x++;
                }
                else if (this.caretPosition.x + this.caretOffset.x + 1 <= this.lines.get(caretPosition.y + caretOffset.y).length())
                {
                    this.caretOffset.x++;
                }
                break;
            }
            case "BACKSPACE":
            {
                switch (caretPosition.x + caretOffset.x)
                {
                    case 0:
                        return;
                }

                StringBuilder stringBuilder = new StringBuilder(this.lines.get(this.caretPosition.y + this.caretOffset.y));

                stringBuilder.deleteCharAt(caretPosition.x + caretOffset.x - 1);
                this.lines.set(this.caretPosition.y + this.caretOffset.y, stringBuilder.toString());

                if (this.caretOffset.x - 1 >= 0)
                {
                    this.caretOffset.x--;
                }
                else if (this.caretPosition.x - 1 >= 0)
                {
                    this.caretPosition.x--;
                }
                break;
            }
            case "ENTER":
            {
                String currentLine = getCurrentLine();

                if (caretPosition.x + caretOffset.x == currentLine.length())
                {
                    this.lines.add(caretPosition.y + caretOffset.y, currentLine);
                    this.lines.set(caretPosition.y + caretOffset.y + 1, "");
                }
                else
                {
                    this.lines.set(caretPosition.y + caretOffset.y, currentLine.substring(0, caretPosition.x + caretOffset.x));
                    this.lines.add(caretPosition.y + caretOffset.y + 1, currentLine.substring(caretPosition.x + caretOffset.x, currentLine.length()));
                }

                this.caretPosition.x = 0;
                this.caretPosition.y++;
                /*
                if (this.caretPosition.y + 1 <= this.minHeight)
                    this.caretPosition.y++;
                else
                    this.caretOffset.y++;
                */
                break;
            }
            case "UP":
                if (this.caretPosition.y - 1 >= 0)
                {
                    this.caretPosition.y--;

                    Position globalCaretPosition = getCaretGlobalPosition();

                    setCaretGlobalPosition(Math.min(globalCaretPosition.x, getCurrentLine().length()), globalCaretPosition.y);
                }
                break;
            case "DOWN":
                if (this.caretPosition.y + 1 < this.lines.size())
                {
                    this.caretPosition.y++;

                    Position globalCaretPosition = getCaretGlobalPosition();

                    setCaretGlobalPosition(Math.min(globalCaretPosition.x, getCurrentLine().length()), globalCaretPosition.y);
                }
                break;
            default:
            {
                switch (input.length())
                {
                    case 1:
                        if (!overflowX && getCurrentLine().length() + 1 > length)
                        {
                            return;
                        }

                        StringBuilder stringBuilder = new StringBuilder(getCurrentLine());

                        stringBuilder.insert(caretPosition.x + caretOffset.x, input);
                        this.lines.set(this.caretPosition.y + this.caretOffset.y, stringBuilder.toString());

                        if (this.caretPosition.x + 1 <= this.length)
                            this.caretPosition.x++;
                        else
                            this.caretOffset.x++;
                        break;
                }
                break;
            }
        }
        Renderer.refresh();
    }

    public String getCurrentLine()
    {
        return this.lines.get(this.caretPosition.y + this.caretOffset.y);
    }

    public void setCaretGlobalPosition(int x, int y)
    {
        if (x > this.length)
            this.caretPosition.x = this.length;
        else
            this.caretPosition.x = x;

        if (y > this.height)
            this.caretPosition.y = this.height;
        else
            this.caretPosition.x = x;
    }

    public void setCaretGlobalPosition(Position other)
    {
        setCaretGlobalPosition(other.x, other.y);
    }

    public Position getCaretGlobalPosition()
    {
        Position caretGlobalPosition = new Position();

        caretGlobalPosition.x = caretPosition.x + caretOffset.x;
        caretGlobalPosition.y = caretPosition.y + caretOffset.y;

        return caretGlobalPosition;
    }
}
