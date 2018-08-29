package com.example.rosadowning.nocrastinate.BroadcastReceivers;
/*
Class which is called at midnight or immediately when the phone is switched on for the first time after midnight. Stores all statistics data in a database and resets all SharedPreference statistics variables
 */

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import com.example.rosadowning.nocrastinate.DBHelpers.AlarmDBContract;
import com.example.rosadowning.nocrastinate.DBHelpers.AppStatsDBContract;
import com.example.rosadowning.nocrastinate.DBHelpers.StatsDBContract;
import com.example.rosadowning.nocrastinate.DBHelpers.ToDoDBContract;
import com.example.rosadowning.nocrastinate.DataModels.CustomAppHolder;
import com.example.rosadowning.nocrastinate.DataModels.StatsData;
import com.example.rosadowning.nocrastinate.Fragments.StatisticsFragment;

import org.joda.time.DateTime;

import java.util.Date;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

import static android.content.Context.ALARM_SERVICE;

public class MidnightDataResetReceiver extends BroadcastReceiver {

    public static final String TAG = "MIDNIGHT RECEIVER";

    @Override
    public void onReceive(Context context, Intent intent) {

        Log.d(TAG, "Midnight Receiver Reached");

        ReentrantLock reentrantLock = new ReentrantLock();

        DateTime today = new DateTime().withTimeAtStartOfDay();
        Date yesterday = today.minusDays(1).withTimeAtStartOfDay().toDate();

        boolean alarmAlreadySet = false;
        long totalDuration;
        long tasksCompleted;
        int unlocks;

        try {
            reentrantLock.lock();
            AlarmDBContract.AlarmDBHelper alarmDBHelper = new AlarmDBContract.AlarmDBHelper(context);

            // Determines whether the class has already been reached, if so it sets alarmAlreadySet to true, if not it stores the time in the Alarm database
            if (alarmDBHelper.isAlarmSet(today.getMillis())) {
                alarmAlreadySet = true;
            } else {
                alarmDBHelper.insertAlarm(today.getMillis());
            }
        } finally {
            reentrantLock.unlock();
        }

        // If the alarm has not been set, execute the following code.
        if (!alarmAlreadySet) {

            try {
                // Locks are used as SharedPreference objects are being accessed
                reentrantLock.lock();

                // Gets the number of tasks completed that day from the ToDoDB database
                ToDoDBContract.ToDoListDbHelper toDoHelper = new ToDoDBContract.ToDoListDbHelper(context);
                tasksCompleted = toDoHelper.getNoOfCompletedToDos(yesterday.getTime(), today.getMillis());

                // Gets the total time the user has been on their phone and the number of times they have unlocked their phone that day from SharedPreferences
                SharedPreferences sharedPreferences = context.getSharedPreferences("StatisticsInfo", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                totalDuration = sharedPreferences.getLong("totalDuration", 0);
                unlocks = sharedPreferences.getInt("noOfUnlocks", 0);

                // Clears the "StatisticsInfo" SharedPreferences variables for the day ahead
                editor.clear();
                editor.apply();

            } finally {
                reentrantLock.unlock();
            }

            // Creates a new StatsData object, giving it the date and the attributes gathered above from the ToDoDB and the SharedPreferences objects
            StatsData midnightStat = new StatsData();
            midnightStat.setDate(yesterday);
            midnightStat.setOverallTime(totalDuration);
            midnightStat.setNoOfUnlocks(unlocks);
            midnightStat.setTasksCompleted(tasksCompleted);

            // Inserts the StatsData object into the Stats database
            StatsDBContract.StatsDBHelper statsHelper = new StatsDBContract.StatsDBHelper(context);
            statsHelper.insertNewStat(midnightStat);

            // Gets a list of the users installed applications and their total time in the phone's foreground for the previous day
            StatisticsFragment statsFragment = new StatisticsFragment();
            List<CustomAppHolder> appList = statsFragment.updateAppsList(statsFragment.getStats("Yesterday", context), context);

            // Gets a list of all the columns in the AppStats database - ie. the app package names that have already been registered in the table
            AppStatsDBContract.AppStatsDbHelper appsHelper = new AppStatsDBContract.AppStatsDbHelper(context);
            String[] appsAlreadyInTable = appsHelper.getApps();

            // Loops through these columns and compares them with the apps in appList.
            // If the columns are missing one of the applications that are in the appList, a new column is created with that application's package name.
            for (int i = 0; i < appList.size(); i++) {
                int j;
                boolean found = false;
                for (j = 0; j < appsAlreadyInTable.length; j++) {
                    if (appsAlreadyInTable[j].equals(appList.get(i).packageName)) {
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    appsHelper.addAppColumn(appList.get(i).packageName);
                }
            }

            // Once new columns have been potentially added, the appList is added to the database along with yesterday's date
            appsHelper.addStats(yesterday, appList);

            // The next midnight alarm is scheduled for 12am
            DateTime tomorrow = today.withTimeAtStartOfDay().plusDays(1);
            Intent midnightIntent = new Intent(context, MidnightDataResetReceiver.class);
            PendingIntent startPIntent = PendingIntent.getBroadcast(context, 0, midnightIntent, 0);
            AlarmManager alarm = (AlarmManager) context.getSystemService(ALARM_SERVICE);
            alarm.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, tomorrow.getMillis(), startPIntent);
        }
    }
}

