package com.example.rosadowning.nocrastinate.BroadcastReceivers;


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

import com.example.rosadowning.nocrastinate.MainActivity;
import com.example.rosadowning.nocrastinate.R;

import java.util.Date;

import static com.example.rosadowning.nocrastinate.MainActivity.CHANNEL_ID;

public class ToDoAlarmReceiver extends BroadcastReceiver {

    public static final String TAG = "TODOALARMRECEIVER";
    private Context context;


    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "IM HERE");
        this.context = context;
        Bundle extras = intent.getExtras();
        String toDoName = extras.getString("ToDoName");
        int alarmID = extras.getInt("AlarmID");

        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
        Intent alarmIntent = new Intent(context.getApplicationContext(), MainActivity.class);
        alarmIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, alarmIntent, 0);

        String text = "Don't forget about your to do, \"" + toDoName + "\"!";

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_nocrastinate_logo_only_transparent)
                .setContentTitle("NoCrastinate Alarm!")
                .setContentText(text)
                .setSound(defaultSoundUri)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(text));
        mBuilder.build();

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.notify(alarmID, mBuilder.build());
    }

}
