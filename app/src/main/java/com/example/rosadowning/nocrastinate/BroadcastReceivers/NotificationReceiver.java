package com.example.rosadowning.nocrastinate.BroadcastReceivers;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;

import com.example.rosadowning.nocrastinate.DBHelpers.StatsDBContract;
import com.example.rosadowning.nocrastinate.DBHelpers.ToDoReaderContract;
import com.example.rosadowning.nocrastinate.DataModels.StatsIconData;
import com.example.rosadowning.nocrastinate.DataModels.TimeHelper;
import com.example.rosadowning.nocrastinate.MainActivity;
import com.example.rosadowning.nocrastinate.R;

import org.joda.time.DateTime;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import static android.content.Context.ALARM_SERVICE;
import static com.example.rosadowning.nocrastinate.MainActivity.CHANNEL_ID;


public class NotificationReceiver extends BroadcastReceiver {

    private static String TAG = "NOTIFICATION RECEIVER";
    private Context context;
    private String content;

    @Override
    public void onReceive(Context context, Intent intent) {

        this.context = context;
        int alarmID = 0;

        Bundle extras = intent.getExtras();
        int type = extras.getInt("Type");
        String title = extras.getString("Title");

        if (type == 1){
            Log.d(TAG, "Alarm type 1");

        } else if (type == 2){
            Log.d(TAG, "Alarm type 2");
            freq2setUp();
            alarmID = 0201;
        } else if (type == 3){
            Log.d(TAG, "Alarm type 3");
            freq3setUp();
            alarmID = 0301;

        }


        Bitmap brainLogo = BitmapFactory.decodeResource(context.getResources(), R.drawable.brain_graphic);

        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
        Intent alarmIntent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.brain_graphic)
                .setLargeIcon(brainLogo)
                .setContentTitle(title)
                .setSound(defaultSoundUri)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(content));
                mBuilder.build();


        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.notify(alarmID, mBuilder.build());
    }


    public void freq2setUp(){

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        Date dateDate = new Date();
        String date = sdf.format(dateDate);

        SharedPreferences sharedPreferences = context.getSharedPreferences("StatisticsInfo", Context.MODE_PRIVATE);
        long overallTime = sharedPreferences.getLong("totalDuration", 0);
        int unlocks = sharedPreferences.getInt("noOfUnlocks",0);
        ToDoReaderContract.ToDoListDbHelper toDoHelper = new ToDoReaderContract.ToDoListDbHelper(context);
        SQLiteDatabase sqlToDo = toDoHelper.getReadableDatabase();
        long tasksCompleted = toDoHelper.getNoOfCompletedToDos();

        content = date + " - You have spent " + TimeHelper.formatDuration(overallTime) + " on your phone, unlocked your phone "+ unlocks + " times and completed " + tasksCompleted + " of your tasks.";

        DateTime daily = new DateTime().withTime(22,0,0,0);
        Date dailyDate = daily.plusHours(24).toDate();
        SimpleDateFormat sdf2 = new SimpleDateFormat("dd/MM/yyyy, hh:MM:ss");
        Log.d(TAG, "next time = "+ sdf2.format(dailyDate));

        Intent nextIntent = new Intent(context, NotificationReceiver.class);
        PendingIntent startPIntent = PendingIntent.getBroadcast(context, 0, nextIntent, 0);
        AlarmManager alarm = (AlarmManager) context.getSystemService(ALARM_SERVICE);
        alarm.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, daily.getMillis(), startPIntent);

    }


    public void freq3setUp(){

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        Date now = new Date();
        String nowString = sdf.format(now);
        Date lastWeek = new DateTime().minusWeeks(1).toDate();
        String lastWeekString = sdf.format(lastWeek);
        String date = lastWeekString + " - " + nowString;
        Log.d(TAG, "Dates = "+ date);

        SharedPreferences sharedPreferences = context.getSharedPreferences("StatisticsInfo", Context.MODE_PRIVATE);
        long overallTime = sharedPreferences.getLong("totalDuration", 0);
        int unlocks = sharedPreferences.getInt("noOfUnlocks",0);
        ToDoReaderContract.ToDoListDbHelper toDoHelper = new ToDoReaderContract.ToDoListDbHelper(context);
        SQLiteDatabase sqlToDo = toDoHelper.getReadableDatabase();
        long tasksCompleted = toDoHelper.getNoOfCompletedToDos();


        StatsDBContract.StatsDBHelper statsHelper = new StatsDBContract.StatsDBHelper(context);
        SQLiteDatabase statsDB = statsHelper.getReadableDatabase();
        ArrayList<StatsIconData> stats = statsHelper.getStatsForInterval("Weekly");

        for (StatsIconData queriedStats : stats) {
            unlocks += queriedStats.getNoOfUnlocks();
            tasksCompleted += queriedStats.getTasksCompleted();
            overallTime += queriedStats.getOverallTime();
        }

        content = date + " - This week, you spent " + TimeHelper.formatDuration(overallTime) + " on your phone, unlocked your phone "+ unlocks + " times and completed " + tasksCompleted + " of your tasks.";

        DateTime weeklyReport = new DateTime().withTime(22,0,0,0);
        weeklyReport = weeklyReport.plusWeeks(1);
        Date week = weeklyReport.toDate();
        SimpleDateFormat sdf2 = new SimpleDateFormat("dd/MM/yyyy, hh:MM:ss");
        Log.d(TAG, "next alarm date = "+ sdf2.format(week));

        Intent nextIntent = new Intent(context, NotificationReceiver.class);
        PendingIntent startPIntent = PendingIntent.getBroadcast(context, 0, nextIntent, 0);
        AlarmManager alarm = (AlarmManager) context.getSystemService(ALARM_SERVICE);
        alarm.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, weeklyReport.getMillis(), startPIntent);

    }


}
