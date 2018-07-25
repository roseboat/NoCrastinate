package com.example.rosadowning.nocrastinate.BroadcastReceivers;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;

import com.example.rosadowning.nocrastinate.Fragments.NotificationSettingsFragment;
import com.example.rosadowning.nocrastinate.Fragments.StatisticsFragment;
import com.example.rosadowning.nocrastinate.MainActivity;
import com.example.rosadowning.nocrastinate.R;

import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.joda.time.Minutes;

import java.util.concurrent.locks.ReentrantLock;

import static android.content.Context.ALARM_SERVICE;
import static com.example.rosadowning.nocrastinate.MainActivity.CHANNEL_ID;

public class ScreenReceiver extends BroadcastReceiver {

    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private Context context;
    private ReentrantLock reentrantLock;
    private final int FREQ_1_ALARM_2 = 10002;


    @Override
    public void onReceive(final Context context, final Intent intent) {

        this.context = context;
        this.reentrantLock = new ReentrantLock();

        Log.e("test", "onReceive");
        sharedPreferences = context.getSharedPreferences("StatisticsInfo", Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();

        if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
            Log.e("SCREEN RECEIVER", "PHONE LOCKED");

            long screenOff = System.currentTimeMillis();

            try {
                reentrantLock.lock();
                editor.putLong("screenOff", screenOff);
                editor.apply();
            } finally {
                reentrantLock.lock();
            }

        } else if (intent.getAction().equals(Intent.ACTION_USER_PRESENT)) {
            Log.e("SCREEN RECEIVER", "PHONE UNLOCKED");

            try {
                reentrantLock.lock();

                int unlocks = sharedPreferences.getInt("noOfUnlocks", 0);
                editor.putInt("noOfUnlocks", ++unlocks);
                editor.apply();
                unlocks = sharedPreferences.getInt("noOfUnlocks", 0);
                if (unlocks % 25 == 0) {
                    SharedPreferences notiPreferences = context.getSharedPreferences("NotificationCheckboxes", Context.MODE_PRIVATE);
                    boolean notiSettings = notiPreferences.getBoolean("checkbox1", false);
                    if (notiSettings) {
                        unlockNotification();
                    }
                }

                long screenOn = sharedPreferences.getLong("screenOn", 0);

                if (screenOn != 0) {
                    long screenOff = sharedPreferences.getLong("screenOff", 0);
                    long duration = (screenOff - screenOn);
                    long currentDuration = sharedPreferences.getLong("totalDuration", 0);
                    long newDuration = currentDuration + duration;
                    editor.putLong("totalDuration", newDuration);
                    editor.apply();
                }

                screenOn = System.currentTimeMillis();
                editor.putLong("screenOn", screenOn);
                editor.apply();

            } finally{
                reentrantLock.unlock();
            }
        }
    }

    public void unlockNotification() {

        int unlocks = sharedPreferences.getInt("noOfUnlocks", 0);
        Intent intent = new Intent(context, NotificationReceiver.class);
        intent.putExtra("Title", "NoCrastinate Unlock Alert!");
        intent.putExtra("AlarmID", FREQ_1_ALARM_2);
        intent.putExtra("Content", "You've unlocked your phone " + unlocks + " times today! :(");
        PendingIntent startPIntent = PendingIntent.getBroadcast(context, 0, intent, 0);
        AlarmManager alarm = (AlarmManager) context.getSystemService(ALARM_SERVICE);
        alarm.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), startPIntent);

    }
}