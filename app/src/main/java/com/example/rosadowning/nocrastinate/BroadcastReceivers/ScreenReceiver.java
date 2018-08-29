package com.example.rosadowning.nocrastinate.BroadcastReceivers;
/*
ScreenReceiver class receives intents from the phone's hardware which are used to set alarms and register unlocks for the Statistics Fragment.
 */

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import com.example.rosadowning.nocrastinate.DBHelpers.AlarmDBContract;

import org.joda.time.DateTime;

import java.util.concurrent.locks.ReentrantLock;

import static android.content.Context.ALARM_SERVICE;

public class ScreenReceiver extends BroadcastReceiver {

    private SharedPreferences statsPreferences;
    private Context context;

    @Override
    public void onReceive(final Context context, final Intent intent) {
        String TAG = "SCREEN RECEIVER";
        this.context = context;
        ReentrantLock reentrantLock = new ReentrantLock();
        statsPreferences = context.getSharedPreferences("StatisticsInfo", Context.MODE_PRIVATE);
        SharedPreferences.Editor statsEditor = statsPreferences.edit();

        // If the intent received is ACTION_SCREEN_ON (when the phone's screen is ON, but the phone is LOCKED), call the resetAlarm() method
        if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)) {
            Log.e(TAG, "SCREEN ON");
            resetAlarm();
            // If the intent is ACTION_USER_PRESENT, this represents the unlocking of the phone
        } else if (intent.getAction().equals(Intent.ACTION_USER_PRESENT)) {
            Log.e(TAG, "PHONE UNLOCKED");

            try {
                reentrantLock.lock(); // locks are used as SharedPreference variables are accessed and edited

                // Gets the current number of unlocks, increments it by 1
                int preUnlocks = statsPreferences.getInt("noOfUnlocks", 0);
                int postUnlocks = ++preUnlocks;
                statsEditor.putInt("noOfUnlocks", postUnlocks);
                statsEditor.apply();

                // If the number of unlocks is a multiple of 25, check if the user has set their notification preferences to alert them at such a time.
                if (postUnlocks % 25 == 0) {
                    SharedPreferences notificationCheckboxes = context.getSharedPreferences("NotificationCheckboxes", Context.MODE_PRIVATE);
                    boolean notificationSettings = notificationCheckboxes.getBoolean("checkbox1", false);
                    if (notificationSettings) {
                        unlockNotification();
                    }
                }
            } finally {
                reentrantLock.unlock();
            }
            // If the intent represents the phone being rebooted (switched on) call the resetAlarm() method
        } else if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
            Log.e(TAG, "PHONE TURNED ON");
            resetAlarm();
        }
    }

    // Method is called if the user has set their notification preferences to frequency 1 and they have unlocked their phone a multiple of 25 times
    public void unlockNotification() {

        // Schedules an alarm to be sent to the NotificationReceiver class to be turned into a push notification
        int unlocks = statsPreferences.getInt("noOfUnlocks", 0);
        Intent intent = new Intent(context, NotificationReceiver.class);
        intent.putExtra("Title", "NoCrastinate Unlock Alert!");
        intent.putExtra("AlarmID", 10002);
        intent.putExtra("Content", "You've unlocked your phone " + unlocks + " times today! :(");
        PendingIntent startPIntent = PendingIntent.getBroadcast(context, 0, intent, 0);
        AlarmManager alarm = (AlarmManager) context.getSystemService(ALARM_SERVICE);
        alarm.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), startPIntent);
    }

    // Method determines whether or not the MidnightDataResetAlarm has been fired that day. If not, the alarm is scheduled immediately.
    public void resetAlarm() {
        DateTime today = new DateTime().withTimeAtStartOfDay(); // 12am that day
        AlarmDBContract.AlarmDBHelper alarmDBHelper = new AlarmDBContract.AlarmDBHelper(context);

        if (!alarmDBHelper.isAlarmSet(today.getMillis())) {
            Intent midnightIntent = new Intent(context, MidnightDataResetReceiver.class);
            PendingIntent startPIntent = PendingIntent.getBroadcast(context, 0, midnightIntent, 0);
            AlarmManager alarm = (AlarmManager) context.getSystemService(ALARM_SERVICE);
            alarm.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), startPIntent);
        }

    }
}