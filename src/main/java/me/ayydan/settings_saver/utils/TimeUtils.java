package me.ayydan.settings_saver.utils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class TimeUtils
{
    /**
     * Gets a string of today's date.
     *
     * @return A string of today's date.
     */
    public static String getCurrentDateString()
    {
        LocalDateTime localDateTime = LocalDateTime.now();

        return String.format("%d/%d/%d", localDateTime.getMonth().getValue(), localDateTime.getDayOfMonth(), localDateTime.getYear());
    }

    /**
     * Gets a string of the current time.
     *
     * @return A string of the current time.
     */
    public static String getCurrentTimeString()
    {
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("hh:mm:ss a", Locale.ROOT);
        LocalDateTime localDateTime = LocalDateTime.now();

        return localDateTime.format(dateTimeFormatter);
    }
}
