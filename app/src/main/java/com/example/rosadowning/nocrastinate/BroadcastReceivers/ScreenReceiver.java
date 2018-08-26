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
import com.example.rosadowning.nocrastinate.Fragments.StatisticsFragment;

import org.joda.time.DateTime;

import java.util.concurrent.locks.ReentrantLock;

import static android.content.Context.ALARM_SERVICE;

public class ScreenReceiver extends BroadcastReceiver {

    private SharedPreferences statsPreferences;
    private Context context;
    private final String TAG = "SCREEN RECEIVER";


    @Override
    public void onReceive(final Context context, final Intent intent) {

        this.context = context;
        ReentrantLock reentrantLock = new ReentrantLock();
        statsPreferences = context.getSharedPreferences("StatisticsInfo", Context.MODE_PRIVATE);
        SharedPreferences.Editor statsEditor = statsPreferences.edit();

        if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)) {
            Log.e(TAG, "SCREEN ON");
            resetAlarm();
        } else if (intent.getAction().equals(Intent.ACTION_USER_PRESENT)) {
            Log.e(TAG, "PHONE UNLOCKED");

            try {
                reentrantLock.lock();

                // HANDLES UNLOCKS
                int unlocks = statsPreferences.getInt("noOfUnlocks", 0);
                statsEditor.putInt("noOfUnlocks", ++unlocks);
                statsEditor.apply();
                unlocks = statsPreferences.getInt("noOfUnlocks", 0);

                // If the number of unlocks is a multiple of 25, check if the user has set their notification preferences to alert them at such a time.
                if (unlocks % 25 == 0) {
                    SharedPreferences notificationCheckboxes = context.getSharedPreferences("NotificationCheckboxes", Context.MODE_PRIVATE);
                    boolean notificationSettings = notificationCheckboxes.getBoolean("checkbox1", false);
                    if (notificationSettings) {
                        unlockNotification();
                    }
                }
            } finally {
                reentrantLock.unlock();
            }
        } else if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
            Log.e(TAG, "PHONE TURNED ON");
            resetAlarm();
        }
    }

    public void unlockNotification() {

        int unlocks = statsPreferences.getInt("noOfUnlocks", 0);
        Intent intent = new Intent(context, NotificationReceiver.class);
        intent.putExtra("Title", "NoCrastinate Unlock Alert!");
        intent.putExtra("AlarmID", 10002);
        intent.putExtra("Content", "You've unlocked your phone " + unlocks + " times today! :(");
        PendingIntent startPIntent = PendingIntent.getBroadcast(context, 0, intent, 0);
        AlarmManager alarm = (AlarmManager) context.getSystemService(ALARM_SERVICE);
        alarm.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), startPIntent);
    }

    public void resetAlarm() {

        DateTime today = new DateTime().withTimeAtStartOfDay();

        AlarmDBContract.AlarmDBHelper alarmDBHelper = new AlarmDBContract.AlarmDBHelper(context);

        if (!alarmDBHelper.isAlarmSet(today.getMillis())) {
            Intent midnightIntent = new Intent(context, MidnightDataResetReceiver.class);
            PendingIntent startPIntent = PendingIntent.getBroadcast(context, 0, midnightIntent, 0);
            AlarmManager alarm = (AlarmManager) context.getSystemService(ALARM_SERVICE);
            alarm.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), startPIntent);
        }

    }
}