package src.components;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;

import src.models.HighlightedDate;
import src.models.RGB;
import src.utils.Database;

public class Calendar {
    private int year;
    private int month;
    private int selectedDay;
    private int textColor = Ansi.FG_WHITE;
    private int highlightedTextColor = Ansi.FG_BLACK;
    private int highlightedBackgroundColor = Ansi.BG_WHITE;
    private Object passTextColor = Ansi.FG_DARK_GRAY;
    private int xOffset = 0;
    private ArrayList<HighlightedDate> highlightedDates = new ArrayList<>();

    public void setHighlightDates(ArrayList<HighlightedDate> dates)
    {
        for (HighlightedDate date : dates)
        {
            if (!containsDate(date.getDate()))
            {
                highlightedDates.add(date);
            }
        }
    }

    public ArrayList<HighlightedDate> getHighlightedDates()
    {
        return this.highlightedDates;
    }

    public Calendar(LocalDate date)
    {
        this(date.getYear(), date.getMonthValue(), date.getDayOfMonth());
    }

    public Calendar(LocalDate date, int textColor, int highlightedTextColor, int highlightedBackgroundColor)
    {
        this(date.getYear(), date.getMonthValue(), date.getDayOfMonth(), textColor, highlightedTextColor, highlightedBackgroundColor);
    }

    public Calendar(LocalDate date, int textColor, int highlightedTextColor, int highlightedBackgroundColor, int xOffset)
    {
        this(date.getYear(), date.getMonthValue(), date.getDayOfMonth(), textColor, highlightedTextColor, highlightedBackgroundColor, xOffset);
    }

    public Calendar(int year, int month, int selectedDay)
    {
        this.year = year;
        this.month = month;
        this.selectedDay = selectedDay;
    }

    public Calendar pastTextColor(int textColor)
    {
        this.passTextColor = textColor;

        return this;
    }

    public Calendar pastTextColor(RGB rgb)
    {
        this.passTextColor = rgb;

        return this;
    }

    public Calendar(int year, int month, int selectedDay, int textColor, int highlightedTextColor, int highlightedBackgroundColor)
    {
        this.year = year;
        this.month = month;
        this.selectedDay = selectedDay;
        this.textColor = textColor;
        this.highlightedTextColor = highlightedTextColor;
        this.highlightedBackgroundColor = highlightedBackgroundColor;
    }

    public Calendar(int year, int month, int selectedDay, int textColor, int highlightedTextColor, int highlightedBackgroundColor, int xOffset)
    {
        this(year, month, selectedDay, textColor, highlightedTextColor, highlightedBackgroundColor);
        this.xOffset = xOffset;
    }

    public AnsiBuilder toAnsiBuilder()
    {
        AnsiBuilder calenderBuilder = new AnsiBuilder();
        YearMonth yearMonth = YearMonth.of(this.year, this.month);
        LocalDate firstOfMonth = yearMonth.atDay(1);
        int daysInMonth = yearMonth.lengthOfMonth();
        int startDayOfWeek = firstOfMonth.getDayOfWeek().getValue() % 7;

        calenderBuilder.append(" ".repeat(xOffset) + "      ").append(new Ansi(yearMonth.getMonth().toString(), textColor)).append(' ').append(new Ansi(year, textColor)).append('\n')
        .append(new Ansi(" ".repeat(xOffset) + "Su Mo Tu We Th Fr Sa\n", textColor));

        calenderBuilder.append(" ".repeat(xOffset));
        for (int i = 0; i < startDayOfWeek; i++) {
            calenderBuilder.append("   ");
        }

        for (int day = 1; day <= daysInMonth; day++) {
            if (LocalDate.of(this.year, this.month, day).isBefore(LocalDate.now()))
            {
                if (passTextColor instanceof Integer i)
                {
                    calenderBuilder.append(new Ansi(String.format("%2d", day), i.intValue())).append(' ');
                }
                else if (passTextColor instanceof RGB rgb)
                {
                    calenderBuilder.append(new Ansi(String.format("%2d", day), 38, 2, rgb.getRed(), rgb.getGreen(), rgb.getBlue())).append(' ');
                }
            }
            else if (day == this.selectedDay)
            {
                calenderBuilder.append(new Ansi(String.format("%2d", day), highlightedBackgroundColor, highlightedTextColor)).append(' ');
            }
            else if (containsDate(LocalDate.of(this.year, this.month, day)))
            {
                for (HighlightedDate highlightedDate : highlightedDates)
                {
                    if (highlightedDate.getDate().equals(LocalDate.of(this.year, this.month, day)))
                    {
                        calenderBuilder.append(new Ansi(String.format("%2d", day), highlightedDate.getBackgroundColor(), highlightedDate.getTextColor())).append(' ');
                    }
                }
            }
            else
            {
                calenderBuilder.append(new Ansi(String.format("%2d", day), textColor)).append(' ');
            }

            if ((day + startDayOfWeek) % 7 == 0) calenderBuilder.append('\n' + " ".repeat(xOffset));
        }
        calenderBuilder.append('\n');

        return calenderBuilder;
    }

    @Override
    public String toString() {
        StringBuilder calenderBuilder = new StringBuilder();
        YearMonth yearMonth = YearMonth.of(this.year, this.month);
        LocalDate firstOfMonth = yearMonth.atDay(1);
        int daysInMonth = yearMonth.lengthOfMonth();
        int startDayOfWeek = firstOfMonth.getDayOfWeek().getValue() % 7;

        calenderBuilder.append(Ansi.toString(textColor)).append("      ").append(yearMonth.getMonth()).append(' ').append(year).append('\n');
        calenderBuilder.append("Su Mo Tu We Th Fr Sa\n").append(Ansi.toString(Ansi.DEFAULT));

        for (int i = 0; i < startDayOfWeek; i++) {
            calenderBuilder.append("   ");
        }

        for (int day = 1; day <= daysInMonth; day++) {
            if (LocalDate.of(this.year, this.month, day).isBefore(LocalDate.now()))
            {
                if (passTextColor instanceof Integer i)
                {
                    calenderBuilder.append(new Ansi(String.format("%2d", day), i.intValue())).append(' ');
                }
                else if (passTextColor instanceof RGB rgb)
                {
                    calenderBuilder.append(new Ansi(String.format("%2d", day), 38, 2, rgb.getRed(), rgb.getGreen(), rgb.getBlue())).append(' ');
                }
            }
            else if (day == this.selectedDay)
            {
                calenderBuilder.append(new Ansi(String.format("%2d", day), highlightedBackgroundColor, highlightedTextColor)).append(' ');
            }
            else if (containsDate(LocalDate.of(this.year, this.month, day)))
            {
                for (HighlightedDate highlightedDate : highlightedDates)
                {
                    if (highlightedDate.getDate().equals(LocalDate.of(this.year, this.month, day)))
                    {
                        calenderBuilder.append(new Ansi(String.format("%2d", day), highlightedDate.getBackgroundColor(), highlightedDate.getTextColor())).append(' ');
                    }
                }
            }
            else
            {
                calenderBuilder.append(new Ansi(String.format("%2d", day), textColor)).append(' ');
            }

            if ((day + startDayOfWeek) % 7 == 0) calenderBuilder.append('\n');
        }
        calenderBuilder.append('\n');

        return calenderBuilder.toString();
    }

    public boolean containsDate(LocalDate date)
    {
        for (HighlightedDate highlightedDate : highlightedDates)
        {
            if (highlightedDate.getDate().equals(date))
            {
                return true;
            }
        }
        return false;
    }

    public static void main(String[] args) {
        Database.init();

        Calendar calendar = new Calendar(LocalDate.now());
        ArrayList<HighlightedDate> highlightedDates = new ArrayList<>();

        for (String facilityName : Database.Booking.getAll().keySet())
        {
            for (String sessionDateString : Database.Booking.getAll().get(facilityName).keySet())
            {
                LocalDate sessionDate = LocalDate.parse(sessionDateString);

                if (sessionDate.getMonthValue() == LocalDate.now().getMonthValue())
                {
                    highlightedDates.add(new HighlightedDate(sessionDate, Ansi.FG_BLACK, Ansi.BG_CYAN));
                }
            }
        }

        calendar.setHighlightDates(highlightedDates);
        System.out.println(calendar);
    }
}