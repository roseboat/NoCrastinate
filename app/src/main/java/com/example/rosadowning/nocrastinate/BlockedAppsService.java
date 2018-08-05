package com.example.rosadowning.nocrastinate;

import android.app.AppOpsManager;
import android.app.Service;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.os.IBinder;
import android.util.Log;

import com.example.rosadowning.nocrastinate.DBHelpers.BlockedAppsDBContract;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class BlockedAppsService extends Service {

    private static Timer timer = new Timer();
    private Context context;

    public BlockedAppsService(Context applicationContext) {
        super();
        context = applicationContext;
        Log.d("BLOCKED SERVICE", "STARTED!");
    }
    public BlockedAppsService(){}


    public IBinder onBind(Intent arg0) {
        return null;
    }

    public void onCreate() {
        super.onCreate();
        context = this;
        startService();
    }

    private void startService() {
        if (hasUsagePermission(context)){
//        timer.scheduleAtFixedRate(new mainTask(), 0, 500);
            timer.schedule(new mainTask(), 0, 500);

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
        Log.d("BLOCKED SERVICE", "Stopped");
    }

    public String getTopPackage() {
        long ts = System.currentTimeMillis();
        UsageStatsManager mUsageStatsManager = (UsageStatsManager) getSystemService(Context.USAGE_STATS_SERVICE);
        List<UsageStats> usageStats = mUsageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_BEST, ts - 1000, ts);
        if (usageStats == null || usageStats.size() == 0) {
            return null;
        }
        Collections.sort(usageStats, new RecentUseComparator());
        return usageStats.get(0).getPackageName();
    }

    public static class RecentUseComparator implements Comparator<UsageStats> {

        @Override
        public int compare(UsageStats lhs, UsageStats rhs) {
            return (lhs.getLastTimeUsed() > rhs.getLastTimeUsed()) ? -1 : (lhs.getLastTimeUsed() == rhs.getLastTimeUsed()) ? 0 : 1;
        }
    }

//    public static boolean needPermissionForBlocking(Context context) {
//        try {
//            PackageManager packageManager = context.getPackageManager();
//            ApplicationInfo applicationInfo = packageManager.getApplicationInfo(context.getPackageName(), 0);
//            AppOpsManager appOpsManager = (AppOpsManager) context.getSystemService(Context.APP_OPS_SERVICE);
//
//                int mode = appOpsManager.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, applicationInfo.uid, applicationInfo.packageName);
//                return (mode != AppOpsManager.MODE_ALLOWED);
//
//        } catch (PackageManager.NameNotFoundException e) {
//            return true;
//        }
//    }

    public static  boolean hasUsagePermission(Context context) {
        SharedPreferences usagePref = context.getSharedPreferences("UsageSettings", Context.MODE_PRIVATE);
        return usagePref.getBoolean("SettingsOn", false);
    }

}