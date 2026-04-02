package src.components;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import src.models.SpecialEmoji;

public class AnsiBuilder {
    List<Ansi> texts = new ArrayList<>();

    public int size()
    {
        return texts.size();
    }

    public AnsiBuilder(String text)
    {
        this(new Ansi(text));
    }

    public AnsiBuilder(int i)
    {
        this(new Ansi(Integer.toString(i)));
    }

    public AnsiBuilder(Ansi ...texts)
    {
        this.texts = new ArrayList<>(Arrays.asList(texts));
    }

    public AnsiBuilder(AnsiBuilder other)
    {
        for (Ansi ansi : other.getTexts())
            this.texts.add(ansi);
    }

    public AnsiBuilder(Object ...objs)
    {
        for (Object obj : objs)
        {
            if (obj instanceof Ansi ansi)
            {
                this.texts.add(ansi);
            }
            else if (obj instanceof AnsiBuilder ansiBuilder)
            {
                for (Ansi ansi : ansiBuilder.getTexts())
                    this.texts.add(ansi);
            }
            else if (obj instanceof String str)
            {
                this.texts.add(new Ansi(str));
            }
            else if (obj instanceof SpecialEmoji emoji)
            {
                this.texts.add(new Ansi(emoji));
            }
        }
    }

    public AnsiBuilder(SpecialEmoji emoji)
    {
        this(new Ansi(emoji));
    }

    public AnsiBuilder append(SpecialEmoji emoji)
    {
        this.texts.add(new Ansi(emoji));

        return this;
    }

    public AnsiBuilder append(String text)
    {
        this.texts.add(new Ansi(text));

        return this;
    }

    public AnsiBuilder append(AnsiBuilder other)
    {
        for (Ansi ansi : other.getTexts())
            this.texts.add(ansi);

        return this;
    }

    public AnsiBuilder append(int i)
    {
        this.texts.add(new Ansi(Integer.toString(i)));

        return this;
    }

    public AnsiBuilder append(char c)
    {
        this.texts.add(new Ansi(Character.toString(c)));

        return this;
    }

    public AnsiBuilder append(Ansi text)
    {
        this.texts.add(text);

        return this;
    }

    @Override
    public String toString()
    {
        StringBuilder ansiBuilder = new StringBuilder();

        for (Ansi text : this.texts)
        {
            ansiBuilder.append(text);
        }

        return ansiBuilder.toString();
    }

    public AnsiBuilder()
    {
        this.texts = new ArrayList<>();
    }

    public AnsiBuilder[] split(String regex)
    {
        List<AnsiBuilder> lines = new ArrayList<>();
        AnsiBuilder currentLine = new AnsiBuilder();

        for (Ansi text : this.texts)
        {
            Ansi[] parts = text.split(regex);
            
            for (int i = 0; i < parts.length; i++)
            {
                currentLine.append(parts[i]);

                if (i < parts.length - 1)
                {
                    lines.add(currentLine);
                    currentLine = new AnsiBuilder();
                }
            }
        }

        lines.add(currentLine);

        return lines.toArray(new AnsiBuilder[0]);
    }

    public boolean isEmpty()
    {
        return texts.size() == 0;
    }

    /*
    public AnsiBuilder[] split(String regex)
    {
        Ansi[][] splitAnsis = new Ansi[this.texts.size()][];
        List<AnsiBuilder> result = new ArrayList<>();

        for (int i = 0; i < this.texts.size(); i++)
        {
            splitAnsis[i] = this.texts.get(i).split(regex);
        }

        if (splitAnsis.length == 1)
        {
            for (Ansi ansi : splitAnsis[0])
                result.add(new AnsiBuilder(ansi));
        }
        else if (splitAnsis.length > 1)
        {
            for (int i = 0; i < splitAnsis.length; i++)
            {
                if (i + 1 < splitAnsis.length)
                {
                    Ansi[] current = splitAnsis[i];
                    Ansi[] next = splitAnsis[i + 1];

                    if (i == 0)
                    {
                        if (current.length == 1)
                        {
                            result.add(new AnsiBuilder(current[0], next[0]));
                        }
                        else if (current.length > 1)
                        {
                            for (int j = 0; j < current.length - 1; j++)
                            {
                                result.add(new AnsiBuilder(current[j]));
                            }
                            result.add(new AnsiBuilder(current[current.length - 1], next[0]));
                        }
                    }
                    else if (i > 0)
                    {
                        if (current.length == 1)
                        {
                            continue;
                        }
                        else if (current.length > 1)
                        {
                            for (int j = 1; j < current.length - 1; j++)
                            {
                                result.add(new AnsiBuilder(current[j]));
                            }
                            result.add(new AnsiBuilder(current[current.length - 1], next[0]));

                            if (i + 1 == splitAnsis.length - 1 && next.length > 1)
                            {
                                for (int j = 1; j < next.length; j++)
                                {
                                    result.add(new AnsiBuilder(next[j]));
                                }
                            }
                        }
                    }
                }
            }
        }

        return result.toArray(new AnsiBuilder[0]);
    }
    */

    public int length()
    {
        int result = 0;

        for (Ansi text : this.texts)
        {
            result += text.length();
        }

        return result;
    }

    public Ansi[] getTexts()
    {
        return this.texts.toArray(Ansi[]::new);
    }
}
