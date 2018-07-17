package com.example.rosadowning.nocrastinate;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import static com.example.rosadowning.nocrastinate.MainActivity.CHANNEL_ID;

public class ScreenReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(final Context context, final Intent intent) {
        Log.e("test", "onReceive");
        if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
            Log.e("SCREEN RECEIVER", "PHONE LOCKED");

        } else if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)) {
            Log.e("SCREEN RECEIVER", "SCREEN ON");

        } else if (intent.getAction().equals(Intent.ACTION_USER_PRESENT)) {
            SharedPreferences sharedPreferences = context.getSharedPreferences("StatisticsInfo", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            int unlocks = sharedPreferences.getInt("noOfUnlocks", 0);
            editor.putInt("noOfUnlocks", ++unlocks);
            editor.apply();

            long currentTime = System.currentTimeMillis();
            editor.putLong("startTime", currentTime);



            Log.e("SCREEN RECEIVER", "USER IS PRESENT. UNLOCKS = " + unlocks);
        }
    }
}