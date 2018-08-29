package com.example.rosadowning.nocrastinate.Services;
/*
Service which runs every 0.5 seconds, checking the phone's foreground application against the list of blocked applications in the BlockedAppsDB. If the foreground application matched one stored in the database, an intent is immediately started to activate the user's home screen, effectively preventing them from accessing the blocked app.
 */

import android.app.ActivityManager;
import android.app.Service;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import com.example.rosadowning.nocrastinate.DBHelpers.BlockedAppsDBContract;
import com.example.rosadowning.nocrastinate.MainActivity;

import java.util.List;
import java.util.SortedMap;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeMap;

public class BlockedAppsService extends Service {

    private static Timer timer = new Timer();
    private Context context;
    private final String TAG = "BLOCKING SERVICE";

    // Constructs the service
    public BlockedAppsService(Context applicationContext) {
        super();
        context = applicationContext;
        Log.d(TAG, "STARTED!");
    }

    public BlockedAppsService() {
    }

    public IBinder onBind(Intent arg0) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    // Once the BlockedAppsService is created, the startService() method is called
    public void onCreate() {
        super.onCreate();
        context = this;
        startService();
    }

    // Service is stopped when the application is shut down
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "Stopped");
    }

    // Checks if the user has set their Usage Access Settings to on and if so, schedules the mainTask() method to run every 0.5 seconds.
    private void startService() {
        if (MainActivity.hasUsagePermission(context)) {
            timer.scheduleAtFixedRate(new mainTask(), 0, 500);
        }
    }

    // Method which gets a List of blocked application's package names from the BlockedAppsDB.
    // Loops through the package names and calls the method getForegroundApp() which returns a String package name of the application at the foreground of the user's phone (actively in use). If the package name matches any in the BlockedAppsDB the method showHomeScreen() is called.
    private class mainTask extends TimerTask {
        public void run() {
            BlockedAppsDBContract.BlockedAppsDBHelper dbHelper = new BlockedAppsDBContract.BlockedAppsDBHelper(context);
            List<String> blockedNames = dbHelper.getBlockedApps();
            for (String packageName : blockedNames) {
                if (packageName != null && getForegroundApp() != null)
                    if (getForegroundApp().equals(packageName)) {
                        showHomeScreen();
                    }
            }
        }
    }

    // Method fires up an intent to immediately show the user's home screen, effectively preventing them from accessing the blocked application.
    public boolean showHomeScreen() {
        Intent startMain = new Intent(Intent.ACTION_MAIN);
        startMain.addCategory(Intent.CATEGORY_HOME);
        startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(startMain);
        return true;
    }

    // Method which returns the package name of the app in the phone's foreground
    public String getForegroundApp() {
        String currentApp = "NULL";

        // This method is dependent on the devices API level. If it is greater than Lollipop it should use execute the following code:
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            UsageStatsManager usm = (UsageStatsManager) context.getSystemService(Context.USAGE_STATS_SERVICE);
            long time = System.currentTimeMillis();

            // Gets the UsageStats for the last day
            List<UsageStats> appList = usm.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, time - 86400000, time);

            // If the List of UsageStats is not null, create a Sorted Map of all the applications in order of the last time they were used, effectively having the last used (or currently active) application as the last key. Set the string currentApp to that application's package name and return.
            if (appList != null && appList.size() > 0) {
                SortedMap<Long, UsageStats> mySortedMap = new TreeMap<Long, UsageStats>();
                for (UsageStats usageStats : appList) {
                    mySortedMap.put(usageStats.getLastTimeUsed(), usageStats);
                }
                if (mySortedMap != null && !mySortedMap.isEmpty()) {
                    currentApp = mySortedMap.get(mySortedMap.lastKey()).getPackageName();
                }
            }

            // If the API level is lower than Lollipop, use the now depreciated method getRunningAppProcess() which will return a list of running apps in order of the last time they were active. Set the string currentApp to the first element in the list's process name and return.
        } else {
            ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
            List<ActivityManager.RunningAppProcessInfo> tasks = am.getRunningAppProcesses();
            currentApp = tasks.get(0).processName;
        }
        return currentApp;
    }
}