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
import android.widget.LinearLayout;

import com.example.rosadowning.nocrastinate.Services.BlockedAppsService;
import com.example.rosadowning.nocrastinate.BroadcastReceivers.NotificationReceiver;
import com.example.rosadowning.nocrastinate.MainActivity;
import com.example.rosadowning.nocrastinate.R;

import org.joda.time.DateTime;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.locks.ReentrantLock;

import static android.content.Context.ALARM_SERVICE;
import static java.util.Calendar.SECOND;

public class NotificationSettingsFragment extends Fragment {

    private static final String TAG = "NOTIFICATIONSETTINGS";
    private CheckBox freq1, freq2, freq3;
    private Context context;
    private NotificationManagerCompat notificationManager;
    private SharedPreferences notiPreferences;
    private SharedPreferences.Editor editor;
    private final int FREQ_2_ALARM_1 = 20001;
    private final int FREQ_3_ALARM_1 = 30001;
    private final String FREQ_2_ALARM_TITLE = "NoCrastinate Daily Report";
    private final String FREQ_3_ALARM_TITLE = "NoCrastinate Weekly Report";
    private ReentrantLock reentrantLock;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_notifications, null);
        this.context = getContext();
        this.reentrantLock = new ReentrantLock();

        notiPreferences = getContext().getSharedPreferences("NotificationCheckboxes", Context.MODE_PRIVATE);
        editor = notiPreferences.edit();

        notificationManager = NotificationManagerCompat.from(context);

        freq1 = (CheckBox) view.findViewById(R.id.notification_checkbox_1);
        freq2 = (CheckBox) view.findViewById(R.id.notification_checkbox_2);
        freq3 = (CheckBox) view.findViewById(R.id.notification_checkbox_3);

        LinearLayout settingsPopup = (LinearLayout) view.findViewById(R.id.settings_popup);

        if (!MainActivity.hasUsagePermission(context)) {
            settingsPopup.setVisibility(View.VISIBLE);
            settingsPopup.bringToFront();

            freq1.setEnabled(false);
            freq2.setEnabled(false);
            freq3.setEnabled(false);

        } else {

            settingsPopup.setVisibility(View.GONE);

            freq1.setChecked(notiPreferences.getBoolean("checkbox1", false));
            freq2.setChecked(notiPreferences.getBoolean("checkbox2", false));
            freq3.setChecked(notiPreferences.getBoolean("checkbox3", false));

            freq1.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    if (b) {
                        try {
                            reentrantLock.lock();
                            Log.d(TAG, "Freq 1 selected");
                            editor.putBoolean("checkbox1", true);
                            editor.putBoolean("checkbox2", false);
                            editor.putBoolean("checkbox3", false);
                            editor.apply();
                            freq2.setChecked(false);
                            freq3.setChecked(false);
                        } finally {
                            reentrantLock.unlock();
                        }
                    } else {
                        reentrantLock.lock();
                        editor.putBoolean("checkbox1", false);
                        editor.commit();
                        reentrantLock.unlock();
                    }
                }
            });

            freq2.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {

                    if (b) {
                        try {
                            reentrantLock.lock();
                            Log.d(TAG, "Freq 2 selected");
                            editor.putBoolean("checkbox1", false);
                            editor.putBoolean("checkbox2", true);
                            editor.putBoolean("checkbox3", false);
                            editor.commit();
                            freq1.setChecked(false);
                            freq3.setChecked(false);
                        } finally {
                            reentrantLock.unlock();
                            freqTwoNotificationSetUp();
                        }
                    } else {
                        Log.d(TAG, "2 unchecked");
                        deleteNotificationAlarm(FREQ_2_ALARM_1);
                        reentrantLock.lock();
                        editor.putBoolean("checkbox2", false);
                        editor.commit();
                        reentrantLock.unlock();
                    }
                }
            });

            freq3.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    if (b) {
                        try {
                            reentrantLock.lock();
                            Log.d(TAG, "Freq 3 selected");
                            editor.putBoolean("checkbox1", false);
                            editor.putBoolean("checkbox2", false);
                            editor.putBoolean("checkbox3", true);
                            editor.commit();
                            freq1.setChecked(false);
                            freq2.setChecked(false);
                        } finally {
                            reentrantLock.unlock();
                            freqThreeNotificationSetUp();
                        }

                    } else {
                        Log.d(TAG, "3 unchecked");
                        deleteNotificationAlarm(FREQ_3_ALARM_1);
                        reentrantLock.lock();
                        editor.putBoolean("checkbox3", false);
                        editor.commit();
                        reentrantLock.unlock();
                    }
                }
            });
        }
        return view;
    }

    public void freqTwoNotificationSetUp() {

        DateTime dailyReport = new DateTime().withTime(22, 0, 0, 0);

        Intent freq2intent = new Intent(getContext(), NotificationReceiver.class);
        freq2intent.putExtra("Title", FREQ_2_ALARM_TITLE);
        freq2intent.putExtra("AlarmID", FREQ_2_ALARM_1);

        PendingIntent startPIntent = PendingIntent.getBroadcast(context, 0, freq2intent, PendingIntent.FLAG_CANCEL_CURRENT);
        AlarmManager alarm = (AlarmManager) context.getSystemService(ALARM_SERVICE);

        alarm.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, dailyReport.getMillis(), startPIntent);
    }

    public void freqThreeNotificationSetUp() {

        DateTime weeklyReport = new DateTime().withTime(22, 0, 0, 0);
        weeklyReport = weeklyReport.plusWeeks(1);

        Date week = weeklyReport.toDate();
        SimpleDateFormat sdf2 = new SimpleDateFormat("dd/MM/yyyy, hh:MM:ss");

        Intent freq3intent = new Intent(getContext(), NotificationReceiver.class);
        freq3intent.putExtra("Title", FREQ_3_ALARM_TITLE);
        freq3intent.putExtra("AlarmID", FREQ_3_ALARM_1);
        PendingIntent startPIntent = PendingIntent.getBroadcast(context, 0, freq3intent, PendingIntent.FLAG_CANCEL_CURRENT);
        AlarmManager alarm = (AlarmManager) context.getSystemService(ALARM_SERVICE);
        alarm.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, weeklyReport.getMillis(), startPIntent);
    }

    public void deleteNotificationAlarm(int deleteNotificationID) {

        String title = "";

        if (deleteNotificationID == FREQ_2_ALARM_1) {
            title = FREQ_2_ALARM_TITLE;
        } else if (deleteNotificationID == FREQ_3_ALARM_1) {
            title = FREQ_3_ALARM_TITLE;
        }

        Intent intent = new Intent(getContext(), NotificationReceiver.class);
        intent.putExtra("Title", title);
        intent.putExtra("AlarmID", deleteNotificationID);
        MainActivity.stopAlarm(getContext(), intent);
        notificationManager.cancel(deleteNotificationID);
    }

}
