package com.example.rosadowning.nocrastinate.Services;

import android.app.Service;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.IBinder;
import android.util.Log;

import com.example.rosadowning.nocrastinate.DBHelpers.BlockedAppsDBContract;
import com.example.rosadowning.nocrastinate.MainActivity;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

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
            SQLiteDatabase sqlBlockedApps = dbHelper.getReadableDatabase();
            List<String> blockedNames = dbHelper.getBlockedApps();
            for (String packageName : blockedNames) {
                if (packageName != null && getTopPackage() != null)
                    if (getTopPackage().equals(packageName)) {
                        showHomeScreen();
                    }
            }
        }
    }

    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "Stopped");
    }

    public String getTopPackage() {
        long ts = System.currentTimeMillis();
        UsageStatsManager mUsageStatsManager = (UsageStatsManager) getSystemService(Context.USAGE_STATS_SERVICE);
        List<UsageStats> usageStats = mUsageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_BEST, ts - 86400000, ts);
        if (usageStats == null || usageStats.size() == 0) {
            return null;
        }
        Collections.sort(usageStats, new RecentUseComparator());
        return usageStats.get(0).getPackageName();

    }

    public static class RecentUseComparator implements Comparator<UsageStats> {

        @Override
        public int compare(UsageStats lhs, UsageStats rhs) {
            return Long.compare(rhs.getLastTimeUsed(), lhs.getLastTimeUsed());
        }
    }

}