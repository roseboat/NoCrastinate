package com.example.rosadowning.nocrastinate;


import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;

import java.util.Date;

import static com.example.rosadowning.nocrastinate.MainActivity.CHANNEL_ID;

public class ToDoAlarmReceiver extends BroadcastReceiver {

    public static final String TAG = "TODOALARMRECEIVER";



    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "IM HERE");

        Bundle extras = intent.getExtras();
        String toDoName = extras.getString("ToDoName");
        int alarmID = extras.getInt("AlarmID");
        int oldAlarmId = extras.getInt("OldAlarmId");
        Log.d(TAG, oldAlarmId + "received");

            if (oldAlarmId != -1){

                deleteAlarm(oldAlarmId);
            }

            Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
            Intent alarmIntent = new Intent(context, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);

            NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context, CHANNEL_ID)
                    .setSmallIcon(R.drawable.brain_graphic)
                    .setContentTitle("NoCrastinate Alarm!")
                    .setContentText("Reminder: Don't forget about your to do, " + toDoName + "!")
                    .setSound(defaultSoundUri)
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setContentIntent(pendingIntent)
                    .setAutoCancel(true);

            mBuilder.build();

      NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
            notificationManager.notify(alarmID, mBuilder.build());
    }

    public static void deleteAlarm(int oldAlarmId){

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from();
        notificationManager.cancel(oldAlarmId);
        Log.d(TAG, "old alarm = " + oldAlarmId + " deleted");

    }

}
