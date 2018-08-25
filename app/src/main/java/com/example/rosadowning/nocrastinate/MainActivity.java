package com.example.rosadowning.nocrastinate;

import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;

import com.example.rosadowning.nocrastinate.BroadcastReceivers.MidnightDataResetReceiver;
import com.example.rosadowning.nocrastinate.BroadcastReceivers.ScreenReceiver;
import com.example.rosadowning.nocrastinate.DBHelpers.AlarmDBContract;
import com.example.rosadowning.nocrastinate.DBHelpers.AppStatsDBContract;
import com.example.rosadowning.nocrastinate.Fragments.*;
import com.example.rosadowning.nocrastinate.Services.BlockedAppsService;

import org.joda.time.DateTime;

public class MainActivity extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener {

    public static final String TAG = "MAIN ACTIVITY";
    private BroadcastReceiver mReceiver;
    private BlockedAppsService mBlockingService;
    public static final String CHANNEL_ID = "4855";
    public static final CharSequence CHANNEL_NAME = "com.example.rosadowning.nocrastinate";
    public static final String CHANNEL_DESCRIPTION = "NoCrastinate Notification Channel";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme);
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        BottomNavigationView navigation = findViewById(R.id.navigation_bar);
        navigation.setOnNavigationItemSelectedListener(this);

        loadFragment(new StatisticsFragment());

        mReceiver = new ScreenReceiver();
        final IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_OFF);
        filter.addAction(Intent.ACTION_BOOT_COMPLETED);
        filter.addAction(Intent.ACTION_USER_PRESENT);
        filter.addAction(Intent.ACTION_SCREEN_ON);
        filter.addAction(Intent.ACTION_SHUTDOWN);
        registerReceiver(mReceiver, filter);

        mBlockingService = new BlockedAppsService(this);
        Intent mServiceIntent = new Intent(this, mBlockingService.getClass());
        if (!isMyServiceRunning(mBlockingService.getClass())) {
            startService(mServiceIntent);
        }
        scheduleResetAlarm();
        createNotificationChannel();
    }

    public Context getContext(){
        return this.getApplicationContext();
    }

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {

        Fragment fragment = null;

        switch (menuItem.getItemId()) {

            case R.id.navigation_stats:
                fragment = new StatisticsFragment();
                break;
            case R.id.navigation_todo:
                fragment = new ToDoFragment();
                break;
            case R.id.navigation_settings:
                fragment = new SettingsFragment();
                break;
        }
        return loadFragment(fragment);
    }

    private boolean loadFragment(Fragment fragment) {
        if (fragment != null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, fragment).addToBackStack(null).commit();
            return true;
        }
        return false;
    }

    private void scheduleResetAlarm() {

        DateTime today = new DateTime().withTimeAtStartOfDay();
        DateTime tomorrow = today.plusDays(1).withTimeAtStartOfDay();

        Intent midnightIntent = new Intent(this, MidnightDataResetReceiver.class);
        PendingIntent startPIntent = PendingIntent.getBroadcast(this, 0, midnightIntent, 0);
        AlarmManager alarm = (AlarmManager) this.getSystemService(ALARM_SERVICE);
        alarm.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, tomorrow.getMillis(), startPIntent);

        AlarmDBContract.AlarmDBHelper alarmDBHelper = new AlarmDBContract.AlarmDBHelper(this);
        SQLiteDatabase sqlRead = alarmDBHelper.getReadableDatabase();

        Log.d(TAG, "Reset alarm set today = " + alarmDBHelper.isAlarmSet(today.getMillis()));

        if (!alarmDBHelper.isAlarmSet(today.getMillis())) {
            alarm.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), startPIntent);
        }
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance);
            channel.setDescription(CHANNEL_DESCRIPTION);
            channel.enableLights(false);
            channel.enableVibration(true);
            channel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mReceiver);

        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_nocrastinate_logo_only_transparent)
                .setContentTitle("You Closed NoCrastinate! :(")
                .setContentText("All phone usage monitoring will now be paused. Click here to resume!")
                .setSound(defaultSoundUri)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        notificationManager.notify(0, mBuilder.build());

    }

    public static void stopAlarm(Context context, Intent intent) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0);
        alarmManager.cancel(pendingIntent);
    }

    public static boolean hasUsagePermission(Context context) {
        SharedPreferences usagePref = context.getSharedPreferences("UsageSettings", Context.MODE_PRIVATE);
        return usagePref.getBoolean("SettingsOn", false);
    }

}

