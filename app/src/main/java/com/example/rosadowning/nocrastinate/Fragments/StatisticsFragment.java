package com.example.rosadowning.nocrastinate.Fragments;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.AppOpsManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.rosadowning.nocrastinate.Adapters.AppStatisticsAdapter;
import com.example.rosadowning.nocrastinate.BroadcastReceivers.NotificationReceiver;
import com.example.rosadowning.nocrastinate.DataModels.CustomAppHolder;
import com.example.rosadowning.nocrastinate.DBHelpers.StatsDBContract;
import com.example.rosadowning.nocrastinate.R;
import com.example.rosadowning.nocrastinate.DBHelpers.ToDoReaderContract;
import com.example.rosadowning.nocrastinate.DataModels.StatsIconData;
import com.example.rosadowning.nocrastinate.DataModels.TimeHelper;

import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.provider.Settings;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;

import org.joda.time.DateTime;
import org.joda.time.Duration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.locks.ReentrantLock;

import jp.wasabeef.recyclerview.adapters.AlphaInAnimationAdapter;

import static android.content.Context.ALARM_SERVICE;

public class StatisticsFragment extends Fragment {

    private static final String TAG = "STATISTICS FRAGMENT";
    private static final String STATS_PREF_NAME = "StatisticsInfo";
    private UsageStatsManager mUsageStatsManager;
    private AppStatisticsAdapter mUsageListAdapter;
    private RecyclerView mRecyclerView;
    private Button mOpenUsageSettingButton;
    private Spinner mSpinner;
    private LinearLayout mUsagePopUp;
    private String intervalString;
    private Context context;
    private TimerTask timerTaskAsync;
    private SharedPreferences sharedPreferences;
    private View statsView;
    private ReentrantLock reentrantLock;
    private long hours, preHours;
    private boolean notiSettingsOne;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_statistics, null);
    }

    public StatisticsFragment() {
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.reentrantLock = new ReentrantLock();
        this.intervalString = "Daily";
        this.context = getContext();
        this.sharedPreferences = context.getSharedPreferences(STATS_PREF_NAME, Context.MODE_PRIVATE);
        mUsageStatsManager = (UsageStatsManager) getActivity().getSystemService(Context.USAGE_STATS_SERVICE);
    }

    @Override
    public void onResume() {
        super.onResume();
        this.onCreate(null);
    }

    @Override
    public void onViewCreated(View rootView, Bundle savedInstanceState) {
        super.onViewCreated(rootView, savedInstanceState);
        this.statsView = getView();

        // Gets the elements in the view
        mUsagePopUp = (LinearLayout) rootView.findViewById(R.id.settings_popup);
        mOpenUsageSettingButton = (Button) rootView.findViewById(R.id.button_open_usage_setting);
        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.recyclerview_app_usage);
        mSpinner = (Spinner) rootView.findViewById(R.id.spinner_time_span);

        mOpenUsageSettingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS));
            }

        });

        // Sets the Recylcer view's Adapter and Animator
        mUsageListAdapter = new AppStatisticsAdapter(context);
        mRecyclerView.scrollToPosition(0);
        mRecyclerView.setAdapter(new AlphaInAnimationAdapter(mUsageListAdapter));

        // Sets the Spinner's Adapter and onClick listener
        SpinnerAdapter spinnerAdapter = ArrayAdapter.createFromResource(getActivity(),
                R.array.action_list, android.R.layout.simple_spinner_dropdown_item);
        mSpinner.setAdapter(spinnerAdapter);
        mSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            String[] strings = getResources().getStringArray(R.array.action_list);

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                intervalString = strings[position];

                if (intervalString != null) {

                    SharedPreferences usagePref = context.getSharedPreferences("UsageSettings", Context.MODE_PRIVATE);
                    final SharedPreferences.Editor editor = usagePref.edit();

                    final Timer timerCheckUsage = new Timer();
                    TimerTask timerTaskCheckUsage = new TimerTask() {
                        @Override
                        public void run() {

                            List<UsageStats> usageStatsList = getUsageStatistics(intervalString);

                            if (usageStatsList.size() == 0 || usageStatsList.isEmpty()) {
                                Log.i(TAG, "The user may not allow the access to apps usage. ");

                                editor.putBoolean("SettingsOn", false);
                                editor.apply();

                                getActivity().runOnUiThread(new Runnable() {

                                    @Override
                                    public void run() {
                                        mUsagePopUp.setVisibility(View.VISIBLE);
                                        mUsagePopUp.bringToFront();
                                    }
                                });
                            } else {

                                getActivity().runOnUiThread(new Runnable() {

                                    @Override
                                    public void run() {
                                        mUsagePopUp.setVisibility(View.GONE);
                                        updateAppsList(getUsageStatistics(intervalString));
                                    }
                                });
                                editor.putBoolean("SettingsOn", true);
                                editor.apply();
                                timerCheckUsage.cancel();
                            }
                        }
                    };
                    timerCheckUsage.schedule(timerTaskCheckUsage, 0, 3000);

                    long overallTime = sharedPreferences.getLong("totalDuration", 0);
                    SharedPreferences notiPreferences = context.getSharedPreferences("NotificationCheckboxes", Context.MODE_PRIVATE);
                    notiSettingsOne = notiPreferences.getBoolean("checkbox1", false);

                    if (notiSettingsOne) {
                        Duration remainingTime = Duration.millis(overallTime);
                        hours = remainingTime.getStandardHours();
                        preHours = hours;
                    }
                    Timer timerAsync = new Timer();
                    timerTaskAsync = new TimerTask() {
                        @Override
                        public void run() {
                            UpdateIcons newIcons = new UpdateIcons();
                            synchronized (newIcons) {
                                newIcons.execute(intervalString);
                            }
                        }
                    };
                    timerAsync.schedule(timerTaskAsync, 0, 5000);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    public static boolean hasPermission(@NonNull final Context context) {
        // Usage Stats is theoretically available on API v19+, but official/reliable support starts with API v21.
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            return false;
        }

        final AppOpsManager appOpsManager = (AppOpsManager) context.getSystemService(Context.APP_OPS_SERVICE);

        if (appOpsManager == null) {
            return false;
        }

        final int mode = appOpsManager.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, android.os.Process.myUid(), context.getPackageName());
        if (mode != AppOpsManager.MODE_ALLOWED) {
            return false;
        }

        // Verify that access is possible. Some devices "lie" and return MODE_ALLOWED even when it's not.
        final long now = System.currentTimeMillis();
        final UsageStatsManager mUsageStatsManager = (UsageStatsManager) context.getSystemService(Context.USAGE_STATS_SERVICE);
        final List<UsageStats> stats = mUsageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, now - 1000 * 10, now);
        return (stats != null && !stats.isEmpty());
    }

    public List<UsageStats> getUsageStatistics(String intervalType) {

        long queryTime = 0;
        switch (intervalType) {

            case "Daily":
                queryTime = new DateTime().withTimeAtStartOfDay().getMillis();
                break;
            case "Weekly":
                queryTime = new DateTime().minusWeeks(1).getMillis();
                break;
            case "Monthly":
                queryTime = new DateTime().minusMonths(1).getMillis();
                break;
            case "Yearly":
                queryTime = new DateTime().minusYears(1).getMillis();
                break;
        }
        Log.d(TAG, "Query time = " + new Date(queryTime).toString());

        Map<String, UsageStats> usageStats = mUsageStatsManager.queryAndAggregateUsageStats(queryTime, System.currentTimeMillis());
        List<UsageStats> queryUsageStats = new ArrayList<>();
        queryUsageStats.addAll(usageStats.values());
        return queryUsageStats;
    }

    private void updateAppsList(List<UsageStats> usageStatsList) {
        List<CustomAppHolder> customAppHolders = new ArrayList<>();

        List<PackageInfo> installedPackages = context.getPackageManager()
                .getInstalledPackages(0);

        for (PackageInfo packageInfo : installedPackages) {

            if ((packageInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0) {

                for (int i = 0; i < usageStatsList.size(); i++) {
                    CustomAppHolder customAppHolder = new CustomAppHolder();
                    customAppHolder.usageStats = usageStatsList.get(i);
                    if (packageInfo.packageName.equals(usageStatsList.get(i).getPackageName())) {
                        customAppHolder.appName = packageInfo.applicationInfo.loadLabel(context.getPackageManager()).toString();
                        try {
                            customAppHolder.appIcon = getActivity().getPackageManager()
                                    .getApplicationIcon(customAppHolder.usageStats.getPackageName());
                        } catch (PackageManager.NameNotFoundException e) {
                            Log.w(TAG, String.format("App Icon is not found for %s",
                                    customAppHolder.usageStats.getPackageName()));
                            customAppHolder.appIcon = getActivity().getDrawable(R.drawable.ic_nocrastinate_logo_only_transparent);
                        }
                        customAppHolders.add(customAppHolder);
                    }
                }
            }
        }
        Collections.sort(customAppHolders, new UsedMostOftenComparatorDesc());
        mUsageListAdapter.setCustomAppList(customAppHolders);
        mUsageListAdapter.notifyDataSetChanged();
        mRecyclerView.scrollToPosition(0);
    }

    private static class UsedMostOftenComparatorDesc implements Comparator<CustomAppHolder> {

        @Override
        public int compare(CustomAppHolder left, CustomAppHolder right) {

            return Long.compare(right.usageStats.getTotalTimeInForeground(), left.usageStats.getTotalTimeInForeground());
        }
    }

    private class UpdateIcons extends AsyncTask<String, Void, StatsIconData> {

        private String timeInterval;

        @Override
        protected StatsIconData doInBackground(String... strings) {

            this.timeInterval = strings[0];
            StatsIconData stats = new StatsIconData();

            SharedPreferences.Editor editor = sharedPreferences.edit();

            try {
                reentrantLock.lock();
                int unlocks = sharedPreferences.getInt("noOfUnlocks", 0);
                long screenOn = sharedPreferences.getLong("screenOn", 0);
                long screenOff = sharedPreferences.getLong("screenOff", 0);
                long overallTime = sharedPreferences.getLong("totalDuration", 0);

                if (screenOn != 0) {
                    if (screenOn > screenOff) {
                        long currentTime = System.currentTimeMillis();
                        long difference = currentTime - screenOn;
                        overallTime = difference + overallTime;
                        editor.putLong("totalDuration", overallTime);
                        editor.putLong("screenOn", System.currentTimeMillis());

                        if (notiSettingsOne) {
                            Duration remainingTime = Duration.millis(overallTime);
                            hours = remainingTime.getStandardHours();
                            if (preHours != hours && hours != 0 && hours - preHours == 1) {
                                Log.d(TAG, "UPDATE : prehours = " + preHours + " hours = " + hours);
                                Intent intent = new Intent(context, NotificationReceiver.class);
                                intent.putExtra("Title", "NoCrastinate Usage Time Alert!");
                                intent.putExtra("AlarmID", 10001);
                                intent.putExtra("Content", "You've been using your phone for " + hours + " hours today! :(");
                                PendingIntent startPIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
                                AlarmManager alarm = (AlarmManager) context.getSystemService(ALARM_SERVICE);
                                alarm.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), startPIntent);
                                preHours = hours;
                            }
                            else preHours = hours;
                        }
                    }
                } else {
                    editor.putLong("screenOn", System.currentTimeMillis());
                    editor.putLong("totalDuration", 0);
                }
                editor.apply();

                ToDoReaderContract.ToDoListDbHelper toDoHelper = new ToDoReaderContract.ToDoListDbHelper(context);
                SQLiteDatabase sql = toDoHelper.getWritableDatabase();
                long beginTime = new DateTime().withTimeAtStartOfDay().getMillis();
                long endTime = new DateTime().plusDays(1).withTimeAtStartOfDay().getMillis();
                long tasksCompleted = toDoHelper.getNoOfCompletedToDos(beginTime, endTime);

                stats.setTasksCompleted(tasksCompleted);
                stats.setNoOfUnlocks(unlocks);
                stats.setOverallTime(overallTime);
            } finally {
                reentrantLock.unlock();
            }

            if (!timeInterval.equals("Daily")) {

                StatsDBContract.StatsDBHelper statsHelper = new StatsDBContract.StatsDBHelper(context);
                SQLiteDatabase db = statsHelper.getReadableDatabase();
                ArrayList<StatsIconData> statsFromInterval = statsHelper.getStatsForInterval(timeInterval);

                int collectedUnlocks = stats.getNoOfUnlocks();
                long collectedCompleted = stats.getTasksCompleted();
                long collectedTime = stats.getOverallTime();

                for (StatsIconData queriedStats : statsFromInterval) {
                    collectedUnlocks += queriedStats.getNoOfUnlocks();
                    collectedCompleted += queriedStats.getTasksCompleted();
                    collectedTime += queriedStats.getOverallTime();
                }

                stats.setTasksCompleted(collectedCompleted);
                stats.setNoOfUnlocks(collectedUnlocks);
                stats.setOverallTime(collectedTime);
            }
            return stats;
        }

        @Override
        protected synchronized void onPostExecute(StatsIconData iconData) {

            if (getView() == statsView) {

                TextView timeHeader = (TextView) getView().findViewById(R.id.stats_header);
                timeHeader.setText(TimeHelper.getHeadingString(timeInterval));

                if ((Integer) iconData.getNoOfUnlocks() != null) {
                    TextView textViewUnlocks = (TextView) getView().findViewById(R.id.text_view_no_of_unlocks);
                    textViewUnlocks.setText(iconData.getNoOfUnlocks() + "");
                }

                if ((Long) iconData.getOverallTime() != null) {
                    String timeString = "";
                    if (iconData.getOverallTime() < 60000) {
                        timeString = "0m";
                    } else {
                        timeString = TimeHelper.formatDuration(iconData.getOverallTime());
                    }
                    TextView textViewOverallTime = (TextView) getView().findViewById(R.id.text_view_overall_time);
                    textViewOverallTime.setText(timeString);
                }

                if ((Long) iconData.getTasksCompleted() != null) {
                    TextView textViewTasks = (TextView) getView().findViewById(R.id.text_view_tasks_completed);
                    textViewTasks.setText(iconData.getTasksCompleted() + "");
                }

            }
        }
    }
}
