package com.example.rosadowning.nocrastinate.BroadcastReceivers;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.example.rosadowning.nocrastinate.Fragments.StatisticsFragment;

import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.joda.time.Minutes;

import static com.example.rosadowning.nocrastinate.MainActivity.CHANNEL_ID;

public class ScreenReceiver extends BroadcastReceiver {

    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;


    @Override
    public void onReceive(final Context context, final Intent intent) {
        Log.e("test", "onReceive");
        sharedPreferences = context.getSharedPreferences("StatisticsInfo", Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();

        if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
            Log.e("SCREEN RECEIVER", "PHONE LOCKED");
            long screenOff = System.currentTimeMillis();
            editor.putLong("screenOff", screenOff);
            editor.apply();

        } else if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)) {
            Log.e("SCREEN RECEIVER", "SCREEN ON");
        }else if (intent.getAction().equals(Intent.ACTION_USER_PRESENT)) {
            Log.e("SCREEN RECEIVER", "PHONE UNLOCKED");


            int unlocks = sharedPreferences.getInt("noOfUnlocks", 0);
            editor.putInt("noOfUnlocks", ++unlocks);

            long screenOn = sharedPreferences.getLong("screenOn", 0);

            if(screenOn != 0){
            long screenOff = sharedPreferences.getLong("screenOff", 0);
            long duration = (screenOff - screenOn);
            long currentDuration = sharedPreferences.getLong("totalDuration", 0);
            long newDuration = currentDuration + duration;
                editor.putLong("totalDuration", newDuration);
            }

            screenOn = System.currentTimeMillis();
            editor.putLong("screenOn", screenOn);
            editor.commit();

        }
    }
}