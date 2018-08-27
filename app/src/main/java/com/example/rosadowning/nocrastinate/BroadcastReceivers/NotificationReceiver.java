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
import java.util.Locale;
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
            setNextAlarm(2);
            content = freq2setUp();
            subheading = "Click here to view your Daily Report";
        } else if (alarmID == FREQ_3_ALARM_1) {
            setNextAlarm(3);
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
            ToDoDBContract.ToDoListDbHelper toDoHelper = new ToDoDBContract.ToDoListDbHelper(context);

            long beginTime = new DateTime().withTimeAtStartOfDay().getMillis();
            long endTime = new DateTime().plusDays(1).withTimeAtStartOfDay().getMillis();

            this.overallTime = sharedPreferences.getLong("totalDuration", 0);
            this.unlocks = sharedPreferences.getInt("noOfUnlocks", 0);
            this.tasksCompleted = toDoHelper.getNoOfCompletedToDos(beginTime, endTime);
        } finally {
            reentrantLock.unlock();
        }
    }

    public String freq2setUp() {

        getDailyStatsSoFar();
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.UK);
        String date = sdf.format(new Date());

        return date + " - You have spent " + TimeHelper.formatDuration(this.overallTime) + " on your phone, unlocked your phone " + this.unlocks + " times and completed " + this.tasksCompleted + " of your tasks.";
    }

    public String freq3setUp() {

        getDailyStatsSoFar();
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.UK);
        String nowString = sdf.format(new Date());
        Date lastWeek = new DateTime().minusWeeks(1).toDate();
        String lastWeekString = sdf.format(lastWeek);
        String date = lastWeekString + " - " + nowString;

        StatsDBContract.StatsDBHelper statsHelper = new StatsDBContract.StatsDBHelper(context);
        ArrayList<StatsData> stats = statsHelper.getStatsForInterval("Weekly");

        for (StatsData queriedStats : stats) {
            unlocks += queriedStats.getNoOfUnlocks();
            tasksCompleted += queriedStats.getTasksCompleted();
            overallTime += queriedStats.getOverallTime();
        }

        return date + " - This week, you spent " + TimeHelper.formatDuration(overallTime) + " on your phone, unlocked your phone " + unlocks + " times and completed " + tasksCompleted + " of your tasks.";

    }

    public void setNextAlarm(int id) {

        DateTime nextReport = null;
        Intent nextIntent = new Intent(context, NotificationReceiver.class);

        if (id == 2) {
            nextReport = new DateTime().withTime(22, 0, 0, 0).plusDays(1);
            nextIntent.putExtra("Title", "NoCrastinate Daily Report");
            nextIntent.putExtra("AlarmID", FREQ_2_ALARM_1);

        } else if (id == 3) {
            nextReport = new DateTime().withTime(22, 0, 0, 0).plusWeeks(1);
            nextIntent.putExtra("Title", "NoCrastinate Weekly Report");
            nextIntent.putExtra("AlarmID", FREQ_3_ALARM_1);
        }
        PendingIntent startPIntent = PendingIntent.getBroadcast(context, 0, nextIntent, 0);
        AlarmManager alarm = (AlarmManager) context.getSystemService(ALARM_SERVICE);
        alarm.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, nextReport.getMillis(), startPIntent);

    }

}
