package com.example.rosadowning.nocrastinate.Fragments;
/*
Class which displays three checkboxes alongside three descriptions of notification settings.
Users can only select one checkbox at a time. Upon selection, the user's preference is stored in
a SharedPreferences object. If they select frequency 2 or 3, alarms are scheduled to notify them later.
 */

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

import com.example.rosadowning.nocrastinate.BroadcastReceivers.NotificationReceiver;
import com.example.rosadowning.nocrastinate.MainActivity;
import com.example.rosadowning.nocrastinate.R;

import org.joda.time.DateTime;

import java.util.concurrent.locks.ReentrantLock;

import static android.content.Context.ALARM_SERVICE;

public class NotificationSettingsFragment extends Fragment {

    private static final String TAG = "NOTIFICATIONSETTINGS";
    private CheckBox freq1, freq2, freq3;
    private Context context;
    private NotificationManagerCompat notificationManager;
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
        this.reentrantLock = new ReentrantLock(); // Locks used as shared preference variables are accessed

        SharedPreferences notiPreferences = context.getSharedPreferences("NotificationCheckboxes", Context.MODE_PRIVATE);
        editor = notiPreferences.edit();

        notificationManager = NotificationManagerCompat.from(context);

        freq1 = (CheckBox) view.findViewById(R.id.notification_checkbox_1);
        freq2 = (CheckBox) view.findViewById(R.id.notification_checkbox_2);
        freq3 = (CheckBox) view.findViewById(R.id.notification_checkbox_3);

        LinearLayout settingsPopup = (LinearLayout) view.findViewById(R.id.settings_popup);

        // If the user has not set their Usage Access Settings for NoCrastinate to on, prompt them to do so with a pop up
        if (!MainActivity.hasUsagePermission(context)) {
            settingsPopup.setVisibility(View.VISIBLE);
            settingsPopup.bringToFront();

            freq1.setEnabled(false);
            freq2.setEnabled(false);
            freq3.setEnabled(false);

        } else {
            // If the Usage Access Settings are on, remove the popup
            settingsPopup.setVisibility(View.GONE);

            // Set the checkboxes to the boolean that has been stored in their respective SharedPreference objects
            freq1.setChecked(notiPreferences.getBoolean("checkbox1", false));
            freq2.setChecked(notiPreferences.getBoolean("checkbox2", false));
            freq3.setChecked(notiPreferences.getBoolean("checkbox3", false));

            // Set an onclick listener onto checkbox 1
            freq1.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    // if it is checked, put 'true' in the checkbox's shared preferences object and false in the others
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
                        // if unchecked, put false in the checkbox 1 shared preference
                    } else {
                        reentrantLock.lock();
                        editor.putBoolean("checkbox1", false);
                        editor.commit();
                        reentrantLock.unlock();
                    }
                }
            });

            // Set an onclick listener onto checkbox 2
            freq2.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    // if it is checked, put 'true' in the checkbox's shared preferences object and false in the others
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
                            freqTwoNotificationSetUp(); // ** Calls a method to set up the first alarm
                        }
                        // if unchecked, put false in the checkbox 2 shared preference and delete the notification
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
            // Set an onclick listener onto checkbox 2
            freq3.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    // if it is checked, put 'true' in the checkbox's shared preferences object and false in the others
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
                        // if unchecked, put false in the checkbox 3 shared preference and delete the notification
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

    // Schedules the first 'Daily Report' notification.
    public void freqTwoNotificationSetUp() {

        DateTime dailyReport = new DateTime().withTime(22, 0, 0, 0);

        Intent freq2intent = new Intent(context, NotificationReceiver.class);
        freq2intent.putExtra("Title", FREQ_2_ALARM_TITLE);
        freq2intent.putExtra("AlarmID", FREQ_2_ALARM_1);

        PendingIntent startPIntent = PendingIntent.getBroadcast(context, 0, freq2intent, PendingIntent.FLAG_CANCEL_CURRENT);
        AlarmManager alarm = (AlarmManager) context.getSystemService(ALARM_SERVICE);

        alarm.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, dailyReport.getMillis(), startPIntent);
    }

    // Schedules the first 'Weekly Report' notification
    public void freqThreeNotificationSetUp() {

        DateTime weeklyReport = new DateTime().withTime(22, 0, 0, 0);
        weeklyReport = weeklyReport.plusWeeks(1);

        Intent freq3intent = new Intent(context, NotificationReceiver.class);
        freq3intent.putExtra("Title", FREQ_3_ALARM_TITLE);
        freq3intent.putExtra("AlarmID", FREQ_3_ALARM_1);
        PendingIntent startPIntent = PendingIntent.getBroadcast(context, 0, freq3intent, PendingIntent.FLAG_CANCEL_CURRENT);
        AlarmManager alarm = (AlarmManager) context.getSystemService(ALARM_SERVICE);
        alarm.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, weeklyReport.getMillis(), startPIntent);
    }

    // Packages an intent to be given to the stopAlarm static method in MainActivity
    // Takes the notification's alarm ID and gives it to the method to stop it.
    public void deleteNotificationAlarm(int deleteNotificationID) {

        String title = "";

        if (deleteNotificationID == FREQ_2_ALARM_1) {
            title = FREQ_2_ALARM_TITLE;
        } else if (deleteNotificationID == FREQ_3_ALARM_1) {
            title = FREQ_3_ALARM_TITLE;
        }

        Intent intent = new Intent(context, NotificationReceiver.class);
        intent.putExtra("Title", title);
        intent.putExtra("AlarmID", deleteNotificationID);
        MainActivity.stopAlarm(getContext(), intent);
        notificationManager.cancel(deleteNotificationID);
    }

}
