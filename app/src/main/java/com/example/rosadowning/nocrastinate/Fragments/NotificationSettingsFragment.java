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

import com.example.rosadowning.nocrastinate.BroadcastReceivers.MidnightDataResetReceiver;
import com.example.rosadowning.nocrastinate.BroadcastReceivers.NotificationReceiver;
import com.example.rosadowning.nocrastinate.R;

import org.joda.time.DateTime;
import org.joda.time.Weeks;
import org.joda.time.format.DateTimeFormat;

import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;

import static android.content.Context.ALARM_SERVICE;

public class NotificationSettingsFragment extends Fragment {

    private CheckBox freq1, freq2, freq3, freq4;
    private static final String TAG = "NOTIFICATIONSETTINGS";
    private Context context;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_notifications, null);
this.context = getContext();
        SharedPreferences sharedPreferences = getContext().getSharedPreferences("NotificationCheckboxes", Context.MODE_PRIVATE);
        boolean checkBox1 = sharedPreferences.getBoolean("checkbox1",false);
        boolean checkBox2 = sharedPreferences.getBoolean("checkbox2", false);
        boolean checkBox3 = sharedPreferences.getBoolean("checkbox3", false);

        final SharedPreferences.Editor editor = sharedPreferences.edit();

        freq1 = (CheckBox) view.findViewById(R.id.notification_checkbox_1);
        freq1.setChecked(checkBox1);
        freq2 = (CheckBox) view.findViewById(R.id.notification_checkbox_2);
        freq2.setChecked(checkBox2);
        freq3 = (CheckBox) view.findViewById(R.id.notification_checkbox_3);
        freq3.setChecked(checkBox3);

        if (checkBox1 == false && checkBox2 == false && checkBox3 == false){
            removeAllNotifications();
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
                }

            }
        });

        return view;
    }

    public void freqOneNotificationSetUp(){




    }

    public void freqTwoNotificationSetUp(){

        DateTime dailyReport = new DateTime().withTime(22,0,0,0);

//        Calendar cal = Calendar.getInstance();
//        cal.add(Calendar.SECOND, 5);

        Intent intent = new Intent(getContext(), NotificationReceiver.class);
        intent.putExtra("Type", 2);
        intent.putExtra("Title", "NoCrastinate Daily Report");

        PendingIntent startPIntent = PendingIntent.getBroadcast(context, 0, intent, 0);
        AlarmManager alarm = (AlarmManager) context.getSystemService(ALARM_SERVICE);
        alarm.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, dailyReport.getMillis(), startPIntent);
//        alarm.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), startPIntent);

    }

    public void freqThreeNotificationSetUp(){

        DateTime weeklyReport = new DateTime().withTime(22,0,0,0);
        weeklyReport = weeklyReport.plusWeeks(1);

        Date week = weeklyReport.toDate();
        SimpleDateFormat sdf2 = new SimpleDateFormat("dd/MM/yyyy, hh:MM:ss");
        Log.d(TAG, "next week date = "+ sdf2.format(week));

        Intent intent = new Intent(getContext(), NotificationReceiver.class);
        intent.putExtra("Type", 3);
        intent.putExtra("Title", "NoCrastinate Weekly Report");
        PendingIntent startPIntent = PendingIntent.getBroadcast(context, 0, intent, 0);
        AlarmManager alarm = (AlarmManager) context.getSystemService(ALARM_SERVICE);
        alarm.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, weeklyReport.getMillis(), startPIntent);

    }

    public void removeAllNotifications(){

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.cancel(0201);
        notificationManager.cancel(0301);

    }


}
