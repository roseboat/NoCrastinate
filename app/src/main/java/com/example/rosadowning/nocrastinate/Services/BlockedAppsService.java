package com.example.rosadowning.nocrastinate.Services;

import android.app.ActivityManager;
import android.app.Service;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import com.example.rosadowning.nocrastinate.DBHelpers.BlockedAppsDBContract;
import com.example.rosadowning.nocrastinate.MainActivity;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.SortedMap;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeMap;

public class BlockedAppsService extends Service {

    private static Timer timer = new Timer();
    private Context context;
    private final String TAG = "BLOCKING SERVICE";

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

    public void onCreate() {
        super.onCreate();
        context = this;
        startService();
    }

    private void startService() {
        if (MainActivity.hasUsagePermission(context)) {
            timer.scheduleAtFixedRate(new mainTask(), 0, 500);
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    public boolean showHomeScreen() {
        Intent startMain = new Intent(Intent.ACTION_MAIN);
        startMain.addCategory(Intent.CATEGORY_HOME);
        startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(startMain);
        return true;
    }

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

    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "Stopped");
    }

    public String getForegroundApp() {
        String currentApp = "NULL";
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            UsageStatsManager usm = (UsageStatsManager) context.getSystemService(Context.USAGE_STATS_SERVICE);
            long time = System.currentTimeMillis();
            List<UsageStats> appList = usm.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, time - 86400000, time);
            if (appList != null && appList.size() > 0) {
                SortedMap<Long, UsageStats> mySortedMap = new TreeMap<Long, UsageStats>();
                for (UsageStats usageStats : appList) {
                    mySortedMap.put(usageStats.getLastTimeUsed(), usageStats);
                }
                if (mySortedMap != null && !mySortedMap.isEmpty()) {
                    currentApp = mySortedMap.get(mySortedMap.lastKey()).getPackageName();
                }
            }
        } else {
            ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
            List<ActivityManager.RunningAppProcessInfo> tasks = am.getRunningAppProcesses();
            currentApp = tasks.get(0).processName;
        }
        return currentApp;
    }
}