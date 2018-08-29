package com.example.rosadowning.nocrastinate.BroadcastReceivers;
/*
Class which handles all alarms scheduled via the user's notification preferences.
When scheduling the intent for this class, bundles are sent with more information
about the particular kind of notification.
 */

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
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;

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
        this.reentrantLock = new ReentrantLock(); // Locks are used as SharedPreference variables are accessed

        Bundle extras = intent.getExtras(); // gets the extras from the intent
        String title = extras.getString("Title");
        int alarmID = extras.getInt("AlarmID"); // Alarms have an ID to tell this receiver what kind of notification they are and how they should be handled
        String content = "";
        String subheading = "";

        // If the alarm has ID 10001 or 10002, it has been scheduled because the user has set their notification preferences to frequency 1
        // These alarms come bundled with the 'content' or text for the notification
        if (alarmID == FREQ_1_ALARM_1 || alarmID == FREQ_1_ALARM_2) {
            content = extras.getString("Content", "Not found");
            subheading = content;
            // If the alarm has ID 20001, it has been scheduled because the user has set their notification preferences to receive 'Daily Reports' on their phone usage
        } else if (alarmID == FREQ_2_ALARM_1) {
            setNextAlarm(2); // Calls method to set next alarm
            content = freq2setUp(); // Text of notification is returned by method fre2setup
            subheading = "Click here to view your Daily Report";
        } else if (alarmID == FREQ_3_ALARM_1) {
            setNextAlarm(3);
            content = freq3setUp();
            subheading = "Click here to view your Weekly Report";
        }

        // Builds and fires the push notification
        Bitmap appLogo = BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_nocrastinate_logo_only_transparent);

        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
        Intent alarmIntent = new Intent(context.getApplicationContext(), MainActivity.class);
        alarmIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_SINGLE_TOP
                | Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(this.context, 0, alarmIntent, PendingIntent.FLAG_ONE_SHOT);

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this.context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_nocrastinate_logo_only_transparent)
                .setLargeIcon(appLogo)
                .setContentTitle(title) // Title of Notification
                .setContentText(subheading) // Subheading
                .setSound(defaultSoundUri)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent) // If clicked, NoCrastinate will open
                .setAutoCancel(true)
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(content)); // Text of notification
        mBuilder.build();

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this.context);
        notificationManager.notify(alarmID, mBuilder.build());
    }

    // Method to get the day's stats (of the top three icons in StatisticsFragment) so far
    // Used by both Daily and Weekly report notifications
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

    // Returns a String which is used as the 'content' or text of a Daily Report notification
    public String freq2setUp() {

        getDailyStatsSoFar();
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yy", Locale.UK);
        String date = sdf.format(new Date());

        return date + " - You have spent " + TimeHelper.formatDuration(this.overallTime) + " on your phone, unlocked your phone " + this.unlocks + " times and completed " + this.tasksCompleted + " of your tasks.";
    }

    // Returns a String with is used as the 'content' or text of Weekly Report notification
    // Gets the daily stats, then the stats for the week prior and adds these together to inform the Weekly Report.
    public String freq3setUp() {

        getDailyStatsSoFar();
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yy", Locale.UK);
        Date lastWeek = new DateTime().minusWeeks(1).toDate();
        String date = sdf.format(lastWeek) + " - " + sdf.format(new Date());

        StatsDBContract.StatsDBHelper statsHelper = new StatsDBContract.StatsDBHelper(context);
        ArrayList<StatsData> stats = statsHelper.getStatsForInterval("Weekly"); // Gets stats for week previous via the Stats database

        // Adds the daily stats to those in the database for the week previous
        for (StatsData queriedStats : stats) {
            unlocks += queriedStats.getNoOfUnlocks();
            tasksCompleted += queriedStats.getTasksCompleted();
            overallTime += queriedStats.getOverallTime();
        }

        return date + " - This week, you spent " + TimeHelper.formatDuration(overallTime) + " on your phone, unlocked your phone " + unlocks + " times and completed " + tasksCompleted + " of your tasks.";
    }

    // Method to set the next alarm depending on the type of notification
    // Takes an id representing which kind of notification it is to set up
    public void setNextAlarm(int id) {

        DateTime nextReport = null;
        Intent nextIntent = new Intent(context, NotificationReceiver.class);

        // Sets up the next daily report for tomorrow at 22:00
        if (id == 2) {
            nextReport = new DateTime().withTime(22, 0, 0, 0).plusDays(1);
            nextIntent.putExtra("Title", "NoCrastinate Daily Report");
            nextIntent.putExtra("AlarmID", FREQ_2_ALARM_1);
            // Sets up the next weekly report for a week from now at 22:00
        } else if (id == 3) {
            nextReport = new DateTime().withTime(22, 0, 0, 0).plusWeeks(1);
            nextIntent.putExtra("Title", "NoCrastinate Weekly Report");
            nextIntent.putExtra("AlarmID", FREQ_3_ALARM_1);
        }
        // Sets the alarm
        PendingIntent startPIntent = PendingIntent.getBroadcast(context, 0, nextIntent, 0);
        AlarmManager alarm = (AlarmManager) context.getSystemService(ALARM_SERVICE);
        alarm.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, nextReport.getMillis(), startPIntent);
    }
}
