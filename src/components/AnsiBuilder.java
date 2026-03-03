package src.components;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AnsiBuilder {
    List<Ansi> texts;

    public AnsiBuilder(String text)
    {
        this(new Ansi(text));
    }

    public AnsiBuilder(String text, int... codes)
    {
        this(new Ansi(text, codes));
    }

    public AnsiBuilder(Ansi ...texts)
    {
        this.texts = new ArrayList<>(Arrays.asList(texts));
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

    public int length()
    {
        int result = 0;

        for (Ansi text : this.texts)
        {
            result += text.length();
        }

        return result;
    }
}
