package com.example.rosadowning.nocrastinate.BroadcastReceivers;

import android.app.AlarmManager;
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
import com.example.rosadowning.nocrastinate.DBHelpers.ToDoDBContract;
import com.example.rosadowning.nocrastinate.DataModels.StatsData;
import com.example.rosadowning.nocrastinate.DataModels.TimeHelper;
import com.example.rosadowning.nocrastinate.MainActivity;
import com.example.rosadowning.nocrastinate.R;

import org.joda.time.DateTime;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.locks.ReentrantLock;

import static android.content.Context.ALARM_SERVICE;
import static com.example.rosadowning.nocrastinate.MainActivity.CHANNEL_ID;


public class NotificationReceiver extends BroadcastReceiver {

    private static String TAG = "NOTIFICATION RECEIVER";
    private ReentrantLock reentrantLock;
    private Context context;
    private long tasksCompleted, overallTime;
    private int unlocks;
    private final int FREQ_1_ALARM_1 = 10001;
    private final int FREQ_1_ALARM_2 = 10002;
    private final int FREQ_2_ALARM_1 = 20001;
    private final int FREQ_3_ALARM_1 = 30001;

    @Override
    public void onReceive(Context context, Intent intent) {

        this.context = context;
        this.reentrantLock = new ReentrantLock();

        Bundle extras = intent.getExtras();
        String title = extras.getString("Title");
        int alarmID = extras.getInt("AlarmID");
        String content = "";
        String subheading = "";


        if (alarmID == FREQ_1_ALARM_1 || alarmID == FREQ_1_ALARM_2) {
            content = extras.getString("Content", "Not found");
            subheading = content;
        } else if (alarmID == FREQ_2_ALARM_1) {
            content = freq2setUp();
            subheading = "Click here to view your Daily Report";
        } else if (alarmID == FREQ_3_ALARM_1) {
            content = freq3setUp();
            subheading = "Click here to view your Weekly Report";

        }
        Bitmap appLogo = BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_nocrastinate_logo_only_transparent);

        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
        Intent alarmIntent = new Intent(context.getApplicationContext(), MainActivity.class);
        alarmIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_SINGLE_TOP
                | Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(context.getApplicationContext(), 0, alarmIntent, PendingIntent.FLAG_ONE_SHOT);

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context.getApplicationContext(), CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_nocrastinate_logo_only_transparent)
                .setLargeIcon(appLogo)
                .setContentTitle(title)
                .setContentText(subheading)
                .setSound(defaultSoundUri)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(content));
        mBuilder.build();

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context.getApplicationContext());
        notificationManager.notify(alarmID, mBuilder.build());
    }

    public void getDailyStatsSoFar() {

        try {
            reentrantLock.lock();
            SharedPreferences sharedPreferences = context.getSharedPreferences("StatisticsInfo", Context.MODE_PRIVATE);
            this.overallTime = sharedPreferences.getLong("totalDuration", 0);
            this.unlocks = sharedPreferences.getInt("noOfUnlocks", 0);
            ToDoDBContract.ToDoListDbHelper toDoHelper = new ToDoDBContract.ToDoListDbHelper(context);
            DateTime today = new DateTime().withTimeAtStartOfDay();
            DateTime tomorrow = today.plusDays(1).withTimeAtStartOfDay();
            long beginTime = today.getMillis();
            long endTime = tomorrow.getMillis();
            this.tasksCompleted = toDoHelper.getNoOfCompletedToDos(beginTime, endTime);
        } finally {
            reentrantLock.unlock();
        }
    }

    public String freq2setUp() {

        Log.d(TAG, "in frequency 2 setup");

        getDailyStatsSoFar();
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        Date dateDate = new Date();
        String date = sdf.format(dateDate);

        String content = date + " - You have spent " + TimeHelper.formatDuration(this.overallTime) + " on your phone, unlocked your phone " + this.unlocks + " times and completed " + this.tasksCompleted + " of your tasks.";

        DateTime daily = new DateTime().withTime(22, 0, 0, 0);
        Date dailyDate = daily.plusHours(24).toDate();
        SimpleDateFormat sdf2 = new SimpleDateFormat("dd/MM/yyyy, hh:MM:ss");
        Log.d(TAG, "next time = " + sdf2.format(dailyDate));

        Intent nextIntent = new Intent(context, NotificationReceiver.class);
        nextIntent.putExtra("Type", 2);
        nextIntent.putExtra("Title", "NoCrastinate Daily Report");
        nextIntent.putExtra("AlarmID", FREQ_2_ALARM_1);
        PendingIntent startPIntent = PendingIntent.getBroadcast(context, 0, nextIntent, 0);
        AlarmManager alarm = (AlarmManager) context.getSystemService(ALARM_SERVICE);
        alarm.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, daily.getMillis(), startPIntent);

        return content;
    }

    public String freq3setUp() {
        Log.d(TAG, "in frequency 3 setup");

        getDailyStatsSoFar();
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        Date now = new Date();
        String nowString = sdf.format(now);
        Date lastWeek = new DateTime().minusWeeks(1).toDate();
        String lastWeekString = sdf.format(lastWeek);
        String date = lastWeekString + " - " + nowString;
        Log.d(TAG, "Dates = " + date);

        StatsDBContract.StatsDBHelper statsHelper = new StatsDBContract.StatsDBHelper(context);
        SQLiteDatabase statsDB = statsHelper.getReadableDatabase();
        ArrayList<StatsData> stats = statsHelper.getStatsForInterval("Weekly");

        for (StatsData queriedStats : stats) {
            unlocks += queriedStats.getNoOfUnlocks();
            tasksCompleted += queriedStats.getTasksCompleted();
            overallTime += queriedStats.getOverallTime();
        }

        String content = date + " - This week, you spent " + TimeHelper.formatDuration(overallTime) + " on your phone, unlocked your phone " + unlocks + " times and completed " + tasksCompleted + " of your tasks.";

        DateTime weeklyReport = new DateTime().withTime(22, 0, 0, 0);
        weeklyReport = weeklyReport.plusWeeks(1);
        Date week = weeklyReport.toDate();
        SimpleDateFormat sdf2 = new SimpleDateFormat("dd/MM/yyyy, hh:MM:ss");
        Log.d(TAG, "next alarm date = " + sdf2.format(week));

        Intent nextIntent = new Intent(context, NotificationReceiver.class);
        nextIntent.putExtra("Title", "NoCrastinate Weekly Report");
        nextIntent.putExtra("AlarmID", FREQ_3_ALARM_1);
        PendingIntent startPIntent = PendingIntent.getBroadcast(context, 0, nextIntent, 0);
        AlarmManager alarm = (AlarmManager) context.getSystemService(ALARM_SERVICE);
        alarm.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, weeklyReport.getMillis(), startPIntent);

        return content;
    }

}
