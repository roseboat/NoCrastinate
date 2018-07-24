package com.example.rosadowning.nocrastinate.DataModels;

import org.joda.time.Duration;
import org.joda.time.format.PeriodFormatter;
import org.joda.time.format.PeriodFormatterBuilder;

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
}
