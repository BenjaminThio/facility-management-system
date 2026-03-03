package src.components;

import java.time.LocalDate;
import java.time.YearMonth;

public class Calendar {
    public static void printCalender(LocalDate date) {
        printCalendar(date.getYear(), date.getMonthValue(), date.getDayOfMonth());
    }

    public static void printCalendar(int year, int month, int selectedDay) {
        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDate firstOfMonth = yearMonth.atDay(1);
        int daysInMonth = yearMonth.lengthOfMonth();
        int startDayOfWeek = firstOfMonth.getDayOfWeek().getValue() % 7;

        System.out.println("      " + yearMonth.getMonth() + " " + year);
        System.out.println("Su Mo Tu We Th Fr Sa");

        for (int i = 0; i < startDayOfWeek; i++) {
            System.out.print("   ");
        }

        for (int day = 1; day <= daysInMonth; day++) {
            if (day == selectedDay) {
                System.out.printf("\033[7m%2d\033[0m ", day);
            } else {
                System.out.printf("%2d ", day);
            }

            if ((day + startDayOfWeek) % 7 == 0) {
                System.out.println();
            }
        }
        System.out.println("\n");
    }

    public static void main(String[] args) {
        printCalendar(2026, 3, 15);
    }
}