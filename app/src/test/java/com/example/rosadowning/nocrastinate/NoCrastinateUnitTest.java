package com.example.rosadowning.nocrastinate;

import com.example.rosadowning.nocrastinate.DataModels.TimeHelper;

import org.joda.time.DateTime;
import org.junit.Test;

import java.text.SimpleDateFormat;

import static org.junit.Assert.assertEquals;


public class NoCrastinateUnitTest {


    @Test
    public void unitTest_timeHelperFormatDuration(){
        String time = "51h45m";
        long testDuration = 186300000; // 51 hours, 45 mins
        String timeHelperString = TimeHelper.formatDuration(testDuration);
        assertEquals("TimeHelper string reads '51h45m'", time, timeHelperString);
    }

    @Test
    public void unitTest_timeHelperHeadingString(){

        String dailyExpected = new SimpleDateFormat("EEEE, MMMM, dd, yyyy").format(new DateTime().withTimeAtStartOfDay().getMillis());
        String dailyActual = TimeHelper.getHeadingString("Daily");
        assertEquals(dailyExpected, dailyActual);

        String weeklyExpected = "The Past Week...";
        String weeklyActual = TimeHelper.getHeadingString("Weekly");
        assertEquals(weeklyExpected, weeklyActual);

        String monthlyExpected = "The Past Month...";
        String monthlyActual = TimeHelper.getHeadingString("Monthly");
        assertEquals(monthlyExpected, monthlyActual);

        String yearlyExpected = "The Past Year...";
        String yearlyActual = TimeHelper.getHeadingString("Yearly");
        assertEquals(yearlyExpected, yearlyActual);
    }


}