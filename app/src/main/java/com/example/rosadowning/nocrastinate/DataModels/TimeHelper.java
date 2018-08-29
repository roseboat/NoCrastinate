package com.example.rosadowning.nocrastinate.DataModels;
/*
Class with static methods that help the application to format time durations and the Statistic Page's time header
 */

import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.joda.time.LocalDate;
import org.joda.time.Period;
import org.joda.time.format.PeriodFormatter;
import org.joda.time.format.PeriodFormatterBuilder;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;


public class TimeHelper {

    // Method is given a duration in the form of a long and formats it into a string for display on the Statistics screen
    public static String formatDuration(long duration) {

        PeriodFormatter periodFormatter = new PeriodFormatterBuilder()
                .appendYears().appendSuffix("y ")
                .appendMonths().appendSuffix("m ")
                .appendWeeks().appendSuffix("w ")
                .appendDays().appendSuffix("d ")
                .appendHours().appendSuffix("h ")
                .appendMinutes().appendSuffix("m ")
                .toFormatter();
        return periodFormatter.print(new Period(new Duration(duration)).normalizedStandard());
    }

    // Method is given a string representing the time interval as chosen by the user. It then  returns a string to be displayed at the top of the Statistics Screen.
    public static String getHeadingString(String interval) {

        StringBuilder headingString = new StringBuilder();

        DateTime now = new DateTime().withTimeAtStartOfDay();
        Date today = now.toDate();

        switch (interval) {
            case "Daily":
                String todayDay = new SimpleDateFormat("EEEE, MMMM, dd, yyyy", Locale.UK).format(today.getTime());
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
