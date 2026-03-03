package src.components;

import java.util.ArrayList;
import java.util.Arrays;

public class Table {
    public static void printTable(ArrayList<ArrayList<AnsiBuilder>> table)
    {
        int colCount = table.stream().mapToInt(ArrayList::size).max().orElse(0);

        for (int y = 0; y < table.size(); y++)
        {
            while (table.get(y).size() < colCount)
            {
                table.get(y).add(new AnsiBuilder(""));
            }
        }

        int[] colLengths = new int[colCount];
        int[] rowHeights = new int[table.size()];

        for (int col = 0; col < colCount; col++)
        {
            int maxColLength = 0;

            for (int row = 0; row < table.size(); row++)
            {
                int maxLineLength = 0;
                AnsiBuilder cell = table.get(row).get(col);
                AnsiBuilder[] cellLines = cell.split("\n");

                if (cellLines.length > 1)
                {
                    for (AnsiBuilder line : cellLines)
                    {
                        maxLineLength = Math.max(line.length(), maxLineLength);
                    }
                    rowHeights[row] = Math.max(cellLines.length, rowHeights[row]);
                }
                else
                {
                    maxLineLength = cell.length();
                    rowHeights[row] = Math.max(1, rowHeights[row]);
                }
                maxColLength = Math.max(maxLineLength, maxColLength);
            }
            colLengths[col] = maxColLength;
        }

        StringBuilder tableBuilder = new StringBuilder();

        for (int row = 0; row < table.size(); row++)
        {
            tableBuilder.append('+');
            for (int col = 0; col < colCount; col++)
            {
                tableBuilder.append("-".repeat(colLengths[col])).append('+');
            }
            tableBuilder.append('\n');

            AnsiBuilder[][] rowCellLines = new AnsiBuilder[colCount][];

            for (int col = 0; col < colCount; col++)
            {
                rowCellLines[col] = table.get(row).get(col).split("\n");
            }

            for (int lineIdx = 0; lineIdx < rowHeights[row]; lineIdx++)
            {
                tableBuilder.append('|');
                for (int col = 0; col < colCount; col++)
                {
                    if (lineIdx < rowCellLines[col].length)
                    {
                        tableBuilder.append(rowCellLines[col][lineIdx]);
                        if (rowCellLines[col][lineIdx].length() < colLengths[col])
                        {
                            tableBuilder.append(" ".repeat(colLengths[col] - rowCellLines[col][lineIdx].length()));
                        }
                    }
                    else
                    {
                        tableBuilder.append(" ".repeat(colLengths[col]));
                    }
                    tableBuilder.append('|');
                }
                tableBuilder.append('\n');
            }
        }

        tableBuilder.append('+');
        for (int col = 0; col < colCount; col++)
        {
            tableBuilder.append("-".repeat(colLengths[col])).append('+');
        }

        System.out.println(tableBuilder.toString());
    }

    public static void main(String[] args) {
        printTable(new ArrayList<>(Arrays.asList(
            new ArrayList<>(Arrays.asList(new AnsiBuilder(new Ansi("test\nwo", Ansi.FG_RED), new Ansi("ihjk\njghjkjh", Ansi.FG_BLUE), new Ansi("hjkjh\nwowgyuvijb", Ansi.FG_YELLOW)), new AnsiBuilder("IDK Who I am"), new AnsiBuilder("WOW"))),
            new ArrayList<>(Arrays.asList(new AnsiBuilder("t"), new AnsiBuilder("123"), new AnsiBuilder("Benjamin")))
        )));
    }
}
