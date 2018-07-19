package com.example.rosadowning.nocrastinate;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.example.rosadowning.nocrastinate.DBHelpers.StatsDBContract;
import com.example.rosadowning.nocrastinate.DBHelpers.ToDoReaderContract;

import java.util.Calendar;
import java.util.Date;

public class MidnightDataResetReceiver extends BroadcastReceiver {

    public static final String TAG = "MIDNIGHT RECEIVER";

    @Override
    public void onReceive(Context context, Intent intent) {

        StatsLeaf midnightStat = new StatsLeaf();

        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.MINUTE, -1);
        Date yesterdayDate = cal.getTime();

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
        editor.putInt("noOfUnlocks", 0);
        editor.putLong("totalDuration", 0);
        editor.apply();

    }
}
