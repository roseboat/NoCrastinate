package com.example.rosadowning.nocrastinate.BroadcastReceivers;

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
import com.example.rosadowning.nocrastinate.DataModels.StatsIconData;

import org.joda.time.DateTime;

import java.util.Date;
import java.util.concurrent.locks.ReentrantLock;

import static android.content.Context.ALARM_SERVICE;

public class MidnightDataResetReceiver extends BroadcastReceiver {

    public static final String TAG = "MIDNIGHT RECEIVER";

    @Override
    public void onReceive(Context context, Intent intent) {

        Log.d(TAG, "Midnight Receiver reached");
        ReentrantLock reentrantLock = new ReentrantLock();
        StatsIconData midnightStat = new StatsIconData();

        DateTime today = new DateTime().withTimeAtStartOfDay();
        Date yesterday = today.minusDays(1).withTimeAtStartOfDay().toDate();

        long overallTime;
        long tasksCompleted;
        int unlocks;

        try {
            reentrantLock.lock();

            ToDoReaderContract.ToDoListDbHelper toDoHelper = new ToDoReaderContract.ToDoListDbHelper(context);
            SQLiteDatabase sqlToDo = toDoHelper.getReadableDatabase();
            tasksCompleted = toDoHelper.getNoOfCompletedToDos(yesterday.getTime(), today.getMillis());

            SharedPreferences sharedPreferences = context.getSharedPreferences("StatisticsInfo", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            overallTime = sharedPreferences.getLong("totalDuration", 0);
            unlocks = sharedPreferences.getInt("noOfUnlocks", 0);

            editor.clear();
            editor.commit();

        } finally {
            reentrantLock.unlock();
        }

        midnightStat.setDate(yesterday);
        midnightStat.setOverallTime(overallTime);
        midnightStat.setNoOfUnlocks(unlocks);
        midnightStat.setTasksCompleted(tasksCompleted);

        StatsDBContract.StatsDBHelper statsHelper = new StatsDBContract.StatsDBHelper(context);
        SQLiteDatabase sqlStats = statsHelper.getWritableDatabase();
        statsHelper.insertNewStat(midnightStat);

        DateTime tomorrow = today.plusDays(1).withTimeAtStartOfDay();
        Intent midnightIntent = new Intent(context, MidnightDataResetReceiver.class);
        PendingIntent startPIntent = PendingIntent.getBroadcast(context, 0, midnightIntent, 0);
        AlarmManager alarm = (AlarmManager) context.getSystemService(ALARM_SERVICE);
        alarm.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, tomorrow.getMillis(), startPIntent);

    }
}
