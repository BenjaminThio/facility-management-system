package src.components;

import java.time.LocalDate;
import java.time.YearMonth;

public class Calendar {
    private int year;
    private int month;
    private int selectedDay;
    private int textColor = Ansi.FG_WHITE;
    private int highlightedTextColor = Ansi.FG_BLACK;
    private int highlightedBackgroundColor = Ansi.BG_WHITE;

    public Calendar(LocalDate date)
    {
        this(date.getYear(), date.getMonthValue(), date.getDayOfMonth());
    }

    public Calendar(LocalDate date, int textColor, int highlightedTextColor, int highlightedBackgroundColor)
    {
        this(date.getYear(), date.getMonthValue(), date.getDayOfMonth(), textColor, highlightedTextColor, highlightedBackgroundColor);
    }

    public Calendar(int year, int month, int selectedDay)
    {
        this.year = year;
        this.month = month;
        this.selectedDay = selectedDay;
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
            calenderBuilder.append(day == this.selectedDay ? new Ansi(String.format("%2d", day), highlightedBackgroundColor, highlightedTextColor) : new Ansi(String.format("%2d", day), textColor)).append(' ');

            if ((day + startDayOfWeek) % 7 == 0) calenderBuilder.append('\n');
        }
        calenderBuilder.append('\n');

        return calenderBuilder.toString();
    }

    public static void main(String[] args) {
        Calendar calendar = new Calendar(LocalDate.now());

        System.out.println(calendar);
    }
}