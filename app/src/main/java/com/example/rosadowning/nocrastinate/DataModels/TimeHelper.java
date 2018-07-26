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

    public static String formatDuration(long duration){

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

    public static String getHeadingString(String interval){

        StringBuilder headingString = new StringBuilder();

        DateTime now = new DateTime().withTimeAtStartOfDay();
        Date today = now.toDate();
        String todayDate = new SimpleDateFormat("dd/MM/yyyy").format(today);

        switch (interval) {
            case "Daily":
                String todayDay = new SimpleDateFormat("EEEE", Locale.ENGLISH).format(today.getTime());
                headingString.append(String.format("%s %s", todayDay, todayDate));
                break;
            case "Weekly":
                Date lastWeek = now.minusDays(7).toDate();
                String lastWeekDate = new SimpleDateFormat("dd/MM/yyyy").format(lastWeek);
                headingString.append(String.format("%s - %s", lastWeekDate, todayDate));
                break;
            case "Monthly":
                Date lastMonth = now.minusMonths(1).toDate();
                String lastMonthDate = new SimpleDateFormat("dd/MM/yyyy").format(lastMonth);
                headingString.append(String.format("%s - %s", lastMonthDate, todayDate));
                break;
            case "Yearly":
                Date lastYear = now.minusYears(1).toDate();
                String lastYearDate = new SimpleDateFormat("dd/MM/yyyy").format(lastYear);
                headingString.append(String.format("%s - %s", lastYearDate, todayDate));
                break;
        }
        return headingString.toString();
    }


}
