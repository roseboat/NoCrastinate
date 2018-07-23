package com.example.rosadowning.nocrastinate;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.example.rosadowning.nocrastinate.DBHelpers.StatsDBContract;
import com.example.rosadowning.nocrastinate.DBHelpers.ToDoReaderContract;

import org.joda.time.DateTime;

import java.util.Calendar;
import java.util.Date;

import static android.content.Context.ALARM_SERVICE;

public class MidnightDataResetReceiver extends BroadcastReceiver {

    public static final String TAG = "MIDNIGHT RECEIVER";

    @Override
    public void onReceive(Context context, Intent intent) {

        Log.d(TAG, "Midnight Receiver reached");

        StatsIconData midnightStat = new StatsIconData();

        DateTime today = new DateTime().withTimeAtStartOfDay();
        DateTime yesterday = today.minusDays(1).withTimeAtStartOfDay();
        Date yesterdayDate = new Date (yesterday.getMillis());

        SharedPreferences sharedPreferences = context.getSharedPreferences("StatisticsInfo", Context.MODE_PRIVATE);
        long overallTime = sharedPreferences.getLong("totalDuration", 0);
        int unlocks = sharedPreferences.getInt("noOfUnlocks",0);

        ToDoReaderContract.ToDoListDbHelper toDoHelper = new ToDoReaderContract.ToDoListDbHelper(context);
        SQLiteDatabase sqlToDo = toDoHelper.getReadableDatabase();
        long tasksCompleted = toDoHelper.getNoOfCompletedToDos();

        midnightStat.setDate(yesterdayDate);
        midnightStat.setOverallTime(overallTime);
        midnightStat.setNoOfUnlocks(unlocks);
        midnightStat.setTasksCompleted(tasksCompleted);

        StatsDBContract.StatsDBHelper statsHelper = new StatsDBContract.StatsDBHelper(context);
        SQLiteDatabase sqlStats = statsHelper.getWritableDatabase();
        statsHelper.insertNewStat(midnightStat);

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt("totalDuration", 0);
        editor.clear();
        editor.apply();

        DateTime tomorrow = today.plusDays(1).withTimeAtStartOfDay();
        Intent midnightIntent = new Intent(context, MidnightDataResetReceiver.class);
        PendingIntent startPIntent = PendingIntent.getBroadcast(context, 0, midnightIntent, 0);
        AlarmManager alarm = (AlarmManager) context.getSystemService(ALARM_SERVICE);
        alarm.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, tomorrow.getMillis(), startPIntent);

    }
}
