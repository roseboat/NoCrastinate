//package com.example.rosadowning.nocrastinate;
//
//import android.app.ActivityManager;
//import android.app.Service;
//import android.content.Context;
//import android.content.Intent;
//import android.content.pm.ApplicationInfo;
//import android.content.pm.PackageInfo;
//import android.content.pm.PackageManager;
//import android.content.pm.ResolveInfo;
//import android.os.Handler;
//import android.os.IBinder;
//import android.os.Message;
//import android.widget.Toast;
//
//import java.util.Collections;
//import java.util.List;
//import java.util.Timer;
//import java.util.TimerTask;
//
//import static android.widget.Toast.*;
//
//public class BlockedAppsService extends Service {
//
//    private static Timer timer = new Timer();
//    public Boolean userAuth = false;
//    private Context ctx;
//    public String pActivity = "";
//
//    public IBinder onBind(Intent arg0) {
//        return null;
//    }
//
//    public void onCreate() {
//        super.onCreate();
//        ctx = this;
//        startService();
//    }
//
//    private void startService() {
//        timer.scheduleAtFixedRate(new mainTask(), 0, 500);
//    }
//
//    @Override
//    public int onStartCommand(Intent intent, int flags, int startId) {
//
//        PackageManager packageManager = getPackageManager();
//        Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
//        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
//        List<ResolveInfo> appList = packageManager.queryIntentActivities(mainIntent, 0);
//        Collections.sort(appList, new ResolveInfo.DisplayNameComparator(packageManager));
//        List<PackageInfo> packs = packageManager.getInstalledPackages(0);
//        for(int i=0; i < packs.size(); i++) {
//            PackageInfo p = packs.get(i);
//            ApplicationInfo a = p.applicationInfo;
//            // skip system apps if they shall not be included
//            //apps.add(p.packageName);
//        }
//
//        ActivityManager mActivityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
//        List<ActivityManager.RunningTaskInfo> RunningTask = mActivityManager.getRunningTasks(1);
//        ActivityManager.RunningTaskInfo ar = RunningTask.get(0);
//        String activityOnTop = ar.topActivity.getClassName();
//
//
//        if(!activityOnTop.equals("com.example.lock")){
//            Intent lockIntent = new Intent(this, LockScreen.class);
//            lockIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//            this.startActivity(lockIntent);
//        }
//        Toast.makeText(this, "My Service Running", Toast.LENGTH_LONG).show();
//        return super.onStartCommand(intent, flags, startId);
//    }
//
//    private class mainTask extends TimerTask {
//        public void run() {
//            toastHandler.sendEmptyMessage(0);
//        }
//    }
//
//    public void onDestroy() {
//        super.onDestroy();
//        makeText(this, "Service Stopped ...", LENGTH_SHORT).show();
//    }
//
//
//}