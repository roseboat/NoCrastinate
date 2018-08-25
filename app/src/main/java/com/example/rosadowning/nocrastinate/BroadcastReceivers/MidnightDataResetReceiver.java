package com.example.rosadowning.nocrastinate.BroadcastReceivers;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
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
        StatsData midnightStat = new StatsData();

        DateTime today = new DateTime().withTimeAtStartOfDay();
        Date yesterday = today.minusDays(1).withTimeAtStartOfDay().toDate();

        boolean alarmAlreadySet = false;
        long overallTime;
        long tasksCompleted;
        int unlocks;

        try {
            reentrantLock.lock();
            AlarmDBContract.AlarmDBHelper alarmDBHelper = new AlarmDBContract.AlarmDBHelper(context);
            SQLiteDatabase sqlRead = alarmDBHelper.getReadableDatabase();

            if (alarmDBHelper.isAlarmSet(today.getMillis())) {
                alarmAlreadySet = true;
            } else {
                SQLiteDatabase sqlWrite = alarmDBHelper.getWritableDatabase();
                alarmDBHelper.insertAlarm(today.getMillis());
            }
        } finally {
            reentrantLock.unlock();
        }

        if (!alarmAlreadySet) {

            try {
                reentrantLock.lock();

                ToDoDBContract.ToDoListDbHelper toDoHelper = new ToDoDBContract.ToDoListDbHelper(context);
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

            StatisticsFragment statsFragment = new StatisticsFragment();
            List<CustomAppHolder> appList = statsFragment.updateAppsList(statsFragment.getStats("Yesterday", context), context);

            AppStatsDBContract.AppStatsDbHelper appsHelper = new AppStatsDBContract.AppStatsDbHelper(context);
            SQLiteDatabase sqlApps = appsHelper.getReadableDatabase();
            String[] appsAlreadyInTable = appsHelper.getApps();

            for (int i = 0; i < appList.size(); i++) {
                int j;
                for (j = 0; j < appsAlreadyInTable.length; j++) {
                    if (appList.get(i).packageName.equals(appsAlreadyInTable[j]))
                        break;
                }
                if (j == appsAlreadyInTable.length)
                    appsHelper.addAppColumn(appList.get(i).packageName);
            }

            appsHelper.addStats(yesterday, appList);

            DateTime tomorrow = today.withTimeAtStartOfDay().plusDays(1);
            Intent midnightIntent = new Intent(context, MidnightDataResetReceiver.class);
            PendingIntent startPIntent = PendingIntent.getBroadcast(context, 0, midnightIntent, 0);
            AlarmManager alarm = (AlarmManager) context.getSystemService(ALARM_SERVICE);
            alarm.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, tomorrow.getMillis(), startPIntent);
        }
    }
}

