package com.example.rosadowning.nocrastinate.Fragments;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import com.example.rosadowning.nocrastinate.BroadcastReceivers.NotificationReceiver;
import com.example.rosadowning.nocrastinate.R;

import org.joda.time.DateTime;
import org.joda.time.Duration;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import static android.content.Context.ALARM_SERVICE;

public class NotificationSettingsFragment extends Fragment {

    private static final String TAG = "NOTIFICATIONSETTINGS";
    private CheckBox freq1, freq2, freq3, freq4;
    private Context context;
    private NotificationManagerCompat notificationManager;
    private long hours, preHours, minutes, preMinutes;
    private SharedPreferences notiPreferences, statsPreferences;
    private SharedPreferences.Editor editor;
    private TimerTask timerTaskAsync;
    private static final int FREQ_1_ALARM_1 = 0101;
    private static final int FREQ_1_ALARM_2 = 0102;
    private final int FREQ_2_ALARM_1 = 0201;
    private final int FREQ_3_ALARM_1 = 0301;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_notifications, null);
        this.context = getContext();
        notiPreferences = getContext().getSharedPreferences("NotificationCheckboxes", Context.MODE_PRIVATE);
        editor = notiPreferences.edit();

        notificationManager = NotificationManagerCompat.from(context);

        freq1 = (CheckBox) view.findViewById(R.id.notification_checkbox_1);
        freq1.setChecked(notiPreferences.getBoolean("checkbox1", false));
        freq2 = (CheckBox) view.findViewById(R.id.notification_checkbox_2);
        freq2.setChecked(notiPreferences.getBoolean("checkbox2", false));
        freq3 = (CheckBox) view.findViewById(R.id.notification_checkbox_3);
        freq3.setChecked(notiPreferences.getBoolean("checkbox3", false));

        if (freq1.isChecked()){
            freqOneNotificationSetUp();
        }


        freq1.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    Log.d(TAG, "Freq 1 selected");
                    editor.putBoolean("checkbox1", true);
                    editor.putBoolean("checkbox2", false);
                    editor.putBoolean("checkbox3", false);
                    editor.commit();
                    freq2.setChecked(false);
                    freq3.setChecked(false);
                    freqOneNotificationSetUp();

                } else {
                    notificationManager.cancel(FREQ_1_ALARM_1);
                    notificationManager.cancel(FREQ_1_ALARM_2);
                    if (timerTaskAsync != null){
                        timerTaskAsync.cancel();
                    }
                    editor.putBoolean("checkbox1", false);
                    editor.commit();
                }

            }
        });
        ;
        freq2.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    Log.d(TAG, "Freq 2 selected");
                    editor.putBoolean("checkbox1", false);
                    editor.putBoolean("checkbox2", true);
                    editor.putBoolean("checkbox3", false);
                    editor.commit();
                    freq1.setChecked(false);
                    freq3.setChecked(false);
                    freqTwoNotificationSetUp();

                }else {
                    notificationManager.cancel(FREQ_2_ALARM_1);
                    editor.putBoolean("checkbox2", false);
                    editor.commit();
                }

            }
        });

        freq3.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    Log.d(TAG, "Freq 3 selected");
                    editor.putBoolean("checkbox1", false);
                    editor.putBoolean("checkbox2", false);
                    editor.putBoolean("checkbox3", true);
                    editor.commit();
                    freq1.setChecked(false);
                    freq2.setChecked(false);
                    freqThreeNotificationSetUp();

                } else {
                    notificationManager.cancel(FREQ_3_ALARM_1);
                    editor.putBoolean("checkbox3", false);
                    editor.commit();

                }

            }
        });

        return view;
    }

    public void freqOneNotificationSetUp() {

       statsPreferences = context.getSharedPreferences("StatisticsInfo", Context.MODE_PRIVATE);
        long overallTime = statsPreferences.getLong("totalDuration", 0);
        Duration remainingTime = Duration.millis(overallTime);
        hours = remainingTime.getStandardHours();
        preHours = hours;

        Timer timerAsync = new Timer();
        TimerTask timerTaskAsync = new TimerTask() {
            @Override
            public void run() {
                synchronized (this) {
                    long overallTime = statsPreferences.getLong("totalDuration", 0);
                    Duration remainingTime = Duration.millis(overallTime);
                    hours = remainingTime.getStandardHours();
                    if (preHours != hours){
                        Log.d(TAG, "UPDATE : prehours = "+ preHours + " hours = " + hours);
                        Intent intent = new Intent(context, NotificationReceiver.class);
                        intent.putExtra("Type", 1);
                        intent.putExtra("Title", "NoCrastinate Usage Time Alert!");
                        intent.putExtra("AlarmID", FREQ_1_ALARM_1);
                        intent.putExtra("Content", "You've been using your phone for " + hours + " hours today! :(");
                        PendingIntent startPIntent = PendingIntent.getBroadcast(context, 0, intent, 0);
                        AlarmManager alarm = (AlarmManager) context.getSystemService(ALARM_SERVICE);
                        alarm.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), startPIntent);
                        preHours = hours;
                    }

                }
            }
        };
        timerAsync.schedule(timerTaskAsync, 0, 30000);
    }

    public void freqTwoNotificationSetUp() {

//        DateTime dailyReport = new DateTime().withTime(22, 0, 0, 0);
        DateTime dailyReport = new DateTime().withTime(18, 40, 0, 0);



        Intent intent = new Intent(getContext(), NotificationReceiver.class);
        intent.putExtra("Type", 2);
        intent.putExtra("Title", "NoCrastinate Daily Report");
        intent.putExtra("AlarmID", FREQ_2_ALARM_1);

        PendingIntent startPIntent = PendingIntent.getBroadcast(context, 0, intent, 0);
        AlarmManager alarm = (AlarmManager) context.getSystemService(ALARM_SERVICE);
        alarm.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, dailyReport.getMillis(), startPIntent);
    }

    public void freqThreeNotificationSetUp() {

        DateTime weeklyReport = new DateTime().withTime(22, 0, 0, 0);
        weeklyReport = weeklyReport.plusWeeks(1);

        Date week = weeklyReport.toDate();
        SimpleDateFormat sdf2 = new SimpleDateFormat("dd/MM/yyyy, hh:MM:ss");
        Log.d(TAG, "next week date = " + sdf2.format(week));

        Intent intent = new Intent(getContext(), NotificationReceiver.class);
        intent.putExtra("Type", 3);
        intent.putExtra("Title", "NoCrastinate Weekly Report");
        intent.putExtra("AlarmID", FREQ_3_ALARM_1);
        PendingIntent startPIntent = PendingIntent.getBroadcast(context, 0, intent, 0);
        AlarmManager alarm = (AlarmManager) context.getSystemService(ALARM_SERVICE);
        alarm.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, weeklyReport.getMillis(), startPIntent);

    }

}
