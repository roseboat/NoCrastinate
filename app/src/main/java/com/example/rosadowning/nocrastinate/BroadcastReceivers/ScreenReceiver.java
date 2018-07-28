package com.example.rosadowning.nocrastinate.BroadcastReceivers;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import org.joda.time.DateTime;

import java.util.concurrent.locks.ReentrantLock;

import static android.content.Context.ALARM_SERVICE;

public class ScreenReceiver extends BroadcastReceiver {

    private SharedPreferences statsPreferences, bootPreferences;
    private SharedPreferences.Editor statsEditor, bootEditor;
    private Context context;
    private ReentrantLock reentrantLock;
    private final int FREQ_1_ALARM_2 = 10002;


    @Override
    public void onReceive(final Context context, final Intent intent) {

        this.context = context;
        this.reentrantLock = new ReentrantLock();
        statsPreferences = context.getSharedPreferences("StatisticsInfo", Context.MODE_PRIVATE);
        bootPreferences = context.getSharedPreferences("BootData", Context.MODE_PRIVATE);
        statsEditor = statsPreferences.edit();
        bootEditor = bootPreferences.edit();

        if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
            Log.e("SCREEN RECEIVER", "PHONE LOCKED");

            long screenOff = System.currentTimeMillis();

            try {
                reentrantLock.lock();
                statsEditor.putLong("screenOff", screenOff);
                statsEditor.apply();
            } finally {
                reentrantLock.lock();
            }

        } else if (intent.getAction().equals(Intent.ACTION_USER_PRESENT)) {
            Log.e("SCREEN RECEIVER", "PHONE UNLOCKED");

            long twelveAM = new DateTime().withTimeAtStartOfDay().getMillis();
            long midnightReceiver = bootPreferences.getLong("MidnightReceiver", 0);

            if (midnightReceiver < twelveAM){
                Intent midnightIntent = new Intent(context, MidnightDataResetReceiver.class);
                PendingIntent startPIntent = PendingIntent.getBroadcast(context, 0, midnightIntent, 0);
                AlarmManager alarm = (AlarmManager) context.getSystemService(ALARM_SERVICE);
                alarm.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), startPIntent);
            }

            try {
                reentrantLock.lock();

                int unlocks = statsPreferences.getInt("noOfUnlocks", 0);
                statsEditor.putInt("noOfUnlocks", ++unlocks);
                statsEditor.apply();
                unlocks = statsPreferences.getInt("noOfUnlocks", 0);
                if (unlocks % 25 == 0) {
                    SharedPreferences notiPreferences = context.getSharedPreferences("NotificationCheckboxes", Context.MODE_PRIVATE);
                    boolean notiSettings = notiPreferences.getBoolean("checkbox1", false);
                    if (notiSettings) {
                        unlockNotification();
                    }
                }

                long screenOn = statsPreferences.getLong("screenOn", 0);

                if (screenOn != 0) {
                    long screenOff = statsPreferences.getLong("screenOff", 0);
                    long duration = (screenOff - screenOn);
                    long currentDuration = statsPreferences.getLong("totalDuration", 0);
                    long newDuration = currentDuration + duration;
                    statsEditor.putLong("totalDuration", newDuration);
                    statsEditor.apply();
                }

                screenOn = System.currentTimeMillis();
                statsEditor.putLong("screenOn", screenOn);
                statsEditor.apply();

            } finally {
                reentrantLock.unlock();
            }
//        } else if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)){
        }

    }

    public void unlockNotification() {

        int unlocks = statsPreferences.getInt("noOfUnlocks", 0);
        Intent intent = new Intent(context, NotificationReceiver.class);
        intent.putExtra("Title", "NoCrastinate Unlock Alert!");
        intent.putExtra("AlarmID", FREQ_1_ALARM_2);
        intent.putExtra("Content", "You've unlocked your phone " + unlocks + " times today! :(");
        PendingIntent startPIntent = PendingIntent.getBroadcast(context, 0, intent, 0);
        AlarmManager alarm = (AlarmManager) context.getSystemService(ALARM_SERVICE);
        alarm.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), startPIntent);

    }
}