package com.demo.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.time.temporal.ChronoUnit;
import java.util.*;

import static com.demo.core.logger.DefaultLogger.logStaticError;


public class DateTime {

    public static String getLocalDateTimeByPattern(String pattern) {
        return DateTimeFormatter.ofPattern(pattern, Locale.ENGLISH).format(LocalDateTime.now());
    }

    public static String getCurrentDayOfWeek() {
        return LocalDate.now().getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.ENGLISH);
    }

    public static String getDayOfWeek(String date) {
        LocalDate localDate = LocalDate.parse(date, DateTimeFormatter.ofPattern("MMM d, yyyy"));
        return localDate.getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.ENGLISH);
    }

    public static int getDayOfMonth(String date) {
        LocalDate localDate = LocalDate.parse(date, DateTimeFormatter.ofPattern("MMM d, yyyy"));
        return localDate.getDayOfMonth();
    }

    public static String getPreviousMonthDay(int i) {
        return Integer.toString(MonthDay.now().getDayOfMonth() - i);
    }

    public static String getCurrentMonthDay() {
        return Integer.toString(MonthDay.now().getDayOfMonth());
    }

    public static String getNextMonthDay() {
        return Integer.toString(MonthDay.now().getDayOfMonth() + 1);
    }

    public static String getNextMonthDay(int i) {
        return Integer.toString(MonthDay.now().getDayOfMonth() + i);
    }

    public static String getCurrentMonth(String pattern) {
        return YearMonth.now().format(DateTimeFormatter.ofPattern(pattern));
    }

    public static String getNextMonth() {
        return YearMonth.now().plusMonths(1).format(DateTimeFormatter.ofPattern("MM"));
    }

    public static String getCurrentYear(String pattern) {
        return Year.now().format(DateTimeFormatter.ofPattern(pattern));
    }

    public static String getCurrentHours() {
        return Integer.toString(LocalTime.now().getHour());
    }

    public static String getCurrentMinutes() {
        return LocalTime.now().format(DateTimeFormatter.ofPattern("mm"));
    }

    public static String parseTime(String time) {
        return LocalTime.parse(time, DateTimeFormatter.ofPattern("HH:mm")).format(DateTimeFormatter.ofPattern("h:mma"));
    }

    public static String parseTimeWithoutMeridiem(String time) {
        return LocalTime.parse(time, DateTimeFormatter.ofPattern("HH:mm")).format(DateTimeFormatter.ofPattern("h:mm"));
    }

    public static String getLocalDate() {
        return DateTimeFormatter.ofPattern("MMM d, yyyy", Locale.ENGLISH).format(LocalDate.now());
    }

    public static String getLocalDateOfPattern(String pattern) {
        return LocalDate.now().format(DateTimeFormatter.ofPattern(pattern));
    }

    public static String parseDate(String date) {
        return DateTimeFormatter.ofPattern("MMMM d, yyyy").format(LocalDate.parse(date));
    }

    public static String parseDate(String date, String monthFormat) {
        return DateTimeFormatter.ofPattern(String.format("%s d, yyyy", monthFormat), Locale.ENGLISH).format(LocalDate.parse(date));
    }

    public static String parseDate(Date date, String pattern) {
        SimpleDateFormat format = new SimpleDateFormat(pattern, Locale.ENGLISH);
        return format.format(date);
    }

    public static String getLocalDateWithPattern(String pattern) {
        LocalDate localDate = LocalDate.now();
        return DateTimeFormatter.ofPattern(pattern, Locale.ENGLISH).format(localDate);
    }

    public static int getDaysInMonth(int monthNumber) {
        YearMonth yearMonthObject = YearMonth.of(Year.now().getValue(), monthNumber);
        return yearMonthObject.lengthOfMonth();
    }

    public static List<Integer> getDaysInCurrentMonthByDayOfWeek(String dayOfWeek) {
        YearMonth yearMonthObject = YearMonth.of(Year.now().getValue(), YearMonth.now().getMonthValue());
        int daysInMonth = yearMonthObject.lengthOfMonth();

        List<Integer> daysList = new ArrayList<>();
        for (int i = 1; i <= daysInMonth; i++) {
            String day = LocalDate.of(Year.now().getValue(), YearMonth.now().getMonthValue(), i).getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.ENGLISH);
            if (day.equals(dayOfWeek)) {
                daysList.add(i);
            }
        }

        return daysList;
    }

    public static String plusMonthsToCurrentDateWithLastDayOfMonth(int months) {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MMMM yyyy", Locale.ENGLISH);
        calendar.add(Calendar.MONTH, months);

        return simpleDateFormat.format(calendar.getTime());
    }

    public static String plusMonthsToCurrentDateWithLastDayOfMonth(int months, String pattern) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MONTH, months);
        calendar.add(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH) - calendar.get(Calendar.DAY_OF_MONTH));
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern, Locale.ENGLISH);

        return simpleDateFormat.format(calendar.getTime());
    }

    public static String getTomorrowDate(String pattern) {
        LocalDate localDate = LocalDate.now();
        return DateTimeFormatter.ofPattern(pattern, Locale.ENGLISH).format(localDate.plusDays(1));
    }

    public static String getYesterdayDate(String pattern) {
        LocalDate localDate = LocalDate.now();
        return DateTimeFormatter.ofPattern(pattern, Locale.ENGLISH).format(localDate.minusDays(1));
    }

    public static String getDateMinusDays(String startDate, int days) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MM/dd/yyyy");
        Calendar calendar = Calendar.getInstance();
        try {
            Date parsedDate = simpleDateFormat.parse(startDate);
            calendar.setTime(parsedDate);
            calendar.add(Calendar.DAY_OF_MONTH, -days);
            return simpleDateFormat.format(calendar.getTime());
        }
        catch (ParseException e) {
            logStaticError("Error... Got mistaken getting differance ", e);
            return startDate;
        }
    }

    public static String getDatePlusDays(String startDate, int days) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MM/dd/yyyy");
        Calendar calendar = Calendar.getInstance();
        try {
            Date parsedDate = simpleDateFormat.parse(startDate);
            calendar.setTime(parsedDate);
            calendar.add(Calendar.DAY_OF_MONTH, days);
            return simpleDateFormat.format(calendar.getTime());
        }
        catch (ParseException e) {
            logStaticError("Failed to parse date '{}'. Returning original value. Days to add: {}", e, startDate, days);
            return startDate;
        }
    }

    public static String getDateWithNMonthAdded(String date, String pattern, int monthToAdd) {
        LocalDate d = LocalDate.parse(date, DateTimeFormatter.ofPattern(pattern));
        return DateTimeFormatter.ofPattern(pattern, Locale.ENGLISH).format(d.plusMonths(monthToAdd));
    }

    public static String getDateTodayPlusDaysWithFormat(int days, String pattern) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern, Locale.ENGLISH);
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE, days);
        return simpleDateFormat.format(calendar.getTime());
    }

    public static int getDaysBetweenTwoDates(String from, String to, boolean includeToDate) {
        int days = 0;
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MM/dd/yyyy");
        try {
            Date date1 = simpleDateFormat.parse(from);
            Date date2 = simpleDateFormat.parse(to);
            days = Math.toIntExact(ChronoUnit.DAYS.between(date1.toInstant(), date2.toInstant()));
            if(includeToDate) {
                ++days;
            }
        } catch (ParseException e) {
            logStaticError("Failed to parse input dates. From: '{}', To: '{}', IncludeToDate: {}", e, from, to, includeToDate);
        }
        return days;
    }

    public static String getDateWithFormat(String date, String currentPattern, String newPattern) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(currentPattern, Locale.ENGLISH);
        try {
            Date parsedDate = simpleDateFormat.parse(date);
            simpleDateFormat.applyPattern(newPattern);

            return simpleDateFormat.format(parsedDate);
        } catch (ParseException e) {
            logStaticError("Failed to reformat date. Input: '{}', From pattern: '{}', To pattern: '{}'", e, date, currentPattern, newPattern);
            return "";
        }
    }

    public static String getDateWithFormatPlusDays(String date, String currentPattern, String newPattern, int days) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(currentPattern, Locale.ENGLISH);
        try {
            Date parsedDate = simpleDateFormat.parse(date);
            Calendar cal = Calendar.getInstance();
            cal.setTime(parsedDate);
            cal.add(Calendar.DATE, days);
            simpleDateFormat.applyPattern(newPattern);

            return simpleDateFormat.format(cal.getTime());
        } catch (ParseException e) {
            logStaticError("Failed to reformat date with offset. Input: '{}', From pattern: '{}', To pattern: '{}', Days to add: {}", e, date, currentPattern, newPattern, days);
            return "";
        }
    }

    public static String getLocalDateWithFormatPlusDays(String date, String currentPattern, String newPattern, int days) {
        LocalDate localDate = LocalDate.parse(date, DateTimeFormatter.ofPattern(currentPattern));
        return localDate.plusDays(days).format(DateTimeFormatter.ofPattern(newPattern));
    }

    public static String getLocalDateWithFormatMinusDays(String date, String currentPattern, String newPattern, int days) {
        LocalDate localDate = LocalDate.parse(date, DateTimeFormatter.ofPattern(currentPattern));
        return localDate.minusDays(days).format(DateTimeFormatter.ofPattern(newPattern));
    }

    public static String getLocalDatePlusMonthsWithPatternAndLastDay(String date, int month, String pattern) {
        LocalDate localDate = LocalDate.parse(date , DateTimeFormatter.ofPattern(pattern));
        LocalDate resultDate = localDate.plusMonths(month);
        return resultDate.withDayOfMonth(resultDate.lengthOfMonth()).format(DateTimeFormatter.ofPattern(pattern));
    }

    public static String getDatePlusMonthPlusDays(String date, int month, int days) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MM/dd/yyyy");
        Calendar calendar = Calendar.getInstance();

        try {
            calendar.setTime(simpleDateFormat.parse(date));
            calendar.add(Calendar.MONTH, month);
            calendar.add(Calendar.DAY_OF_MONTH, days);
        } catch (ParseException e) {
            logStaticError("Failed to parse input date '{}'. Months to add: {}, Days to add: {}", e, date, month, days);
        }
        return simpleDateFormat.format(calendar.getTime());
    }

    public static String getExpirationDateOnDebitCard(String date) {
        LocalDate localDate = LocalDate.parse(date , DateTimeFormatter.ofPattern("MM/dd/yyyy"));
        return localDate.format(DateTimeFormatter.ofPattern("yyMM"));
    }

    public static String getMonthNumberByMonthName(String month) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MM", Locale.ENGLISH);
        try {
            Date date = new SimpleDateFormat("MMM", Locale.ENGLISH).parse(month);
            Calendar cal = Calendar.getInstance();
            cal.setTime(date);
            return simpleDateFormat.format(cal.getTime());
        } catch (ParseException e) {
            logStaticError("Failed to parse month name '{}'", e, month);
            return "";
        }
    }

    public static String getLastTwoDigitsOfYear(String year) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yy", Locale.ENGLISH);
        try {
            Date date = new SimpleDateFormat("yyyy", Locale.ENGLISH).parse(year);
            Calendar cal = Calendar.getInstance();
            cal.setTime(date);
            return simpleDateFormat.format(cal.getTime());
        } catch (ParseException e) {
            logStaticError("Failed to parse full year '{}'", e, year);
            return "";
        }
    }

    public static boolean isDateBefore(String actualDate, String expectedDate, String pattern) {
        LocalDate actual = LocalDate.parse(actualDate , DateTimeFormatter.ofPattern(pattern));
        LocalDate expected = LocalDate.parse(expectedDate , DateTimeFormatter.ofPattern(pattern));

        return actual.isBefore(expected);
    }

    public static boolean isDateAfter(String actualDate, String expectedDate, String pattern) {
        LocalDate actual = LocalDate.parse(actualDate , DateTimeFormatter.ofPattern(pattern));
        LocalDate expected = LocalDate.parse(expectedDate , DateTimeFormatter.ofPattern(pattern));

        return actual.isAfter(expected);
    }

    public static boolean isDateEqual(String actualDate, String expectedDate, String pattern) {
        LocalDate actual = LocalDate.parse(actualDate , DateTimeFormatter.ofPattern(pattern));
        LocalDate expected = LocalDate.parse(expectedDate , DateTimeFormatter.ofPattern(pattern));

        return actual.isEqual(expected);
    }

    public static String getDatePlusYearsWithFormat(int years, String pattern) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern, Locale.ENGLISH);
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.YEAR, years);
        return simpleDateFormat.format(calendar.getTime());
    }

    public static String getDateMinusMonth(String date, int month) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MM/dd/yyyy");
        Calendar calendar = Calendar.getInstance();

        try {
            calendar.setTime(simpleDateFormat.parse(date));
            calendar.add(Calendar.MONTH, -month);
        } catch (ParseException e) {
            logStaticError("Failed to parse date '{}'. Months to subtract: {}", e, date, month);
        }
        return simpleDateFormat.format(calendar.getTime());
    }

    public static String getDatePlusMonth(String date, int month) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MM/dd/yyyy");
        Calendar calendar = Calendar.getInstance();

        try {
            calendar.setTime(simpleDateFormat.parse(date));
            calendar.add(Calendar.MONTH, month);
        } catch (ParseException e) {
            logStaticError("Failed to parse date '{}'. Months to add: {}", e, date, month);
        }
        return simpleDateFormat.format(calendar.getTime());
    }
}