package com.example.rosadowning.nocrastinate.DataModels;

import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.joda.time.LocalDate;
import org.joda.time.format.PeriodFormatter;
import org.joda.time.format.PeriodFormatterBuilder;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class TimeHelper {

    public static String formatDuration(long duration) {

        Duration dur = new Duration(duration);
        PeriodFormatter formatter = new PeriodFormatterBuilder()
                .appendDays()
                .appendSuffix("d")
                .appendHours()
                .appendSuffix("h")
                .appendMinutes()
                .appendSuffix("m")
                .toFormatter();
        String formatted = formatter.print(dur.toPeriod());
        return formatted;
    }

    public static String getHeadingString(String interval) {

        StringBuilder headingString = new StringBuilder();

        DateTime now = new DateTime().withTimeAtStartOfDay();
        Date today = now.toDate();
        String todayDate = new SimpleDateFormat("dd/MM/yyyy").format(today);

        switch (interval) {
            case "Daily":
                String todayDay = new SimpleDateFormat("EEEE, MMMM, dd, yyyy").format(today.getTime());
                headingString.append(todayDay);
                break;
            case "Weekly":
                headingString.append("The Past Week...");
                break;
            case "Monthly":
                headingString.append("The Past Month...");
                break;
            case "Yearly":
                headingString.append("The Past Year...");
                break;
        }
        return headingString.toString();
    }
}
