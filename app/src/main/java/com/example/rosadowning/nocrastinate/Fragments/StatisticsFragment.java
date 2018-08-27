package com.example.rosadowning.nocrastinate.Fragments;

import android.annotation.TargetApi;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.usage.UsageEvents;
import android.app.usage.UsageStats;
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
import com.example.rosadowning.nocrastinate.DBHelpers.AppStatsDBContract;
import com.example.rosadowning.nocrastinate.DataModels.CustomAppHolder;
import com.example.rosadowning.nocrastinate.DBHelpers.StatsDBContract;
import com.example.rosadowning.nocrastinate.MainActivity;
import com.example.rosadowning.nocrastinate.R;
import com.example.rosadowning.nocrastinate.DBHelpers.ToDoDBContract;
import com.example.rosadowning.nocrastinate.DataModels.StatsData;
import com.example.rosadowning.nocrastinate.DataModels.TimeHelper;

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
import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
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
    private TimerTask timerTaskCheckUsage;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
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
        this.editor = sharedPreferences.edit();
        mUsageStatsManager = (UsageStatsManager) getActivity().getSystemService(Context.USAGE_STATS_SERVICE);
    }

    @Override
    public void onResume() {
        super.onResume();
        this.onCreate(null);
    }

    @Override
    public void onStart() {
        super.onStart();
        editor.putLong("statisticsLaunch", System.currentTimeMillis());
        Log.e(TAG, "STATS LAUNCHED");
        editor.apply();
    }

    @Override
    public void onViewCreated(View rootView, Bundle savedInstanceState) {
        super.onViewCreated(rootView, savedInstanceState);
        this.statsView = getView();

        editor.putBoolean("statisticsFragmentLaunched", true);

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

                    long overallTime = sharedPreferences.getLong("totalDuration", 0);
                    SharedPreferences notiPreferences = context.getSharedPreferences("NotificationCheckboxes", Context.MODE_PRIVATE);
                    notiSettingsOne = notiPreferences.getBoolean("checkbox1", false);

                    if (notiSettingsOne) {
                        Duration timeOnPhone = Duration.millis(overallTime);
                        hours = timeOnPhone.getStandardHours();
                        preHours = hours;
                    }

                    Timer timerCheckUsage = new Timer();
                    timerTaskCheckUsage = new TimerTask() {
                        @Override
                        public void run() {


                            List<UsageStats> usageStatsList = mUsageStatsManager
                                    .queryUsageStats(UsageStatsManager.INTERVAL_BEST, new DateTime().minusDays(1).getMillis(),
                                            System.currentTimeMillis());

//                            List<CustomAppHolder> usageStatsList = getStats(intervalString);

                            if (usageStatsList.size() == 0 || usageStatsList.isEmpty()) {
                                Log.i(TAG, "The user may not allow the access to apps usage. ");

                                editor.putBoolean("SettingsOn", false);
                                editor.apply();

                                getActivity().runOnUiThread(new Runnable() {

                                    @Override
                                    public void run() {
                                        mUsagePopUp.setVisibility(View.VISIBLE);
                                        mSpinner.setVisibility(View.GONE);
                                        mUsagePopUp.bringToFront();
                                    }
                                });
                            } else {
                                editor.putBoolean("SettingsOn", true);
                                editor.apply();

                                getActivity().runOnUiThread(new Runnable() {

                                    @Override
                                    public void run() {
                                        mUsagePopUp.setVisibility(View.GONE);
                                        mSpinner.setVisibility(View.VISIBLE);
                                    }
                                });

                                UpdateStats newIcons = new UpdateStats();
                                synchronized (newIcons) {
                                    newIcons.execute(intervalString);

                                }
                            }
                        }
                    };
                    if (sharedPreferences.getBoolean("SettingsOn", false)) {
                        timerCheckUsage.schedule(timerTaskCheckUsage, 1000, 3000);
                    } else {
                        timerCheckUsage.schedule(timerTaskCheckUsage, 1000, 30000);
                    }

                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    public List<CustomAppHolder> getStats(String interval, Context context) {

        long startTime = 0;
        long endTime = System.currentTimeMillis();

        if (interval.equals("Daily")) {
            startTime = new DateTime().withTimeAtStartOfDay().getMillis();
        } else if (interval.equals("Yesterday")) {
            startTime = new DateTime().withTimeAtStartOfDay().minusDays(1).getMillis();
        } else {
            AppStatsDBContract.AppStatsDbHelper dbHelper = new AppStatsDBContract.AppStatsDbHelper(context);
            return dbHelper.getStatsForInterval(interval);
        }

        mUsageStatsManager = (UsageStatsManager) context.getSystemService(Context.USAGE_STATS_SERVICE);

        UsageEvents.Event currentEvent;
        List<UsageEvents.Event> allEvents = new ArrayList<>();
        HashMap<String, CustomAppHolder> map = new HashMap<>();

        UsageEvents mEvents = mUsageStatsManager.queryEvents(startTime, endTime);

        while (mEvents.hasNextEvent()) {
            currentEvent = new UsageEvents.Event();
            mEvents.getNextEvent(currentEvent);

            if (currentEvent.getEventType() == UsageEvents.Event.MOVE_TO_FOREGROUND ||
                    currentEvent.getEventType() == UsageEvents.Event.MOVE_TO_BACKGROUND) {
                allEvents.add(currentEvent);
                String key = currentEvent.getPackageName();

                if (map.get(key) == null)
                    map.put(key, new CustomAppHolder(key));
            }
        }
        for (int i = 0; i < allEvents.size() - 1; i++) {
            UsageEvents.Event preEvent = allEvents.get(i);
            UsageEvents.Event postEvent = allEvents.get(i + 1);

            if (preEvent.getEventType() == 1 && postEvent.getEventType() == 2
                    && preEvent.getClassName().equals(postEvent.getClassName())) {
                long diff = postEvent.getTimeStamp() - preEvent.getTimeStamp();
                map.get(preEvent.getPackageName()).timeInForeground += diff;
            }
        }
        if (allEvents.size() > 0) {
            UsageEvents.Event finalEvent = allEvents.get(allEvents.size() - 1);

            if (finalEvent.getEventType() == 1 && finalEvent.getPackageName().contains("nocrastinate")) {
                long diff = System.currentTimeMillis() - finalEvent.getTimeStamp();
                map.get(finalEvent.getPackageName()).timeInForeground += diff;
            }
        }
        List<CustomAppHolder> allEventsList = new ArrayList<>(map.values());

        if (interval.equals("Daily")) {
            long totalTime = 0;
            for (CustomAppHolder event : allEventsList) {
                totalTime += event.timeInForeground;
            }
            if (editor != null){
            editor.putLong("totalDuration", totalTime);
            editor.apply();
        }}

        return allEventsList;
    }

    public List<CustomAppHolder> updateAppsList(List<CustomAppHolder> allEventsList, Context context) {

        List<CustomAppHolder> updatedAppsList = new ArrayList<>();

        List<PackageInfo> installedPackages = context.getPackageManager()
                .getInstalledPackages(0);

        for (PackageInfo packageInfo : installedPackages) {
            if ((packageInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0) {
                for (int i = 0; i < allEventsList.size(); i++) {
                    if (allEventsList.get(i).timeInForeground > 60000) {
                        CustomAppHolder customAppHolder = new CustomAppHolder();
                        if (packageInfo.packageName.equals(allEventsList.get(i).packageName)) {
                            customAppHolder.timeInForeground = allEventsList.get(i).timeInForeground;
                            customAppHolder.packageName = packageInfo.packageName;
                            customAppHolder.appName = packageInfo.applicationInfo.loadLabel(context.getPackageManager()).toString();
                            try {
                                if (getActivity() != null)
                                    customAppHolder.appIcon = getActivity().getPackageManager()
                                            .getApplicationIcon(customAppHolder.packageName);
                            } catch (PackageManager.NameNotFoundException e) {
                                Log.w(TAG, String.format("App Icon is not found for %s",
                                        customAppHolder.packageName));
                                customAppHolder.appIcon = getActivity().getDrawable(R.drawable.ic_nocrastinate_logo_only_transparent);
                            }

                            updatedAppsList.add(customAppHolder);
                        }
                    }
                }
            }
        }
        return updatedAppsList;
    }

    private static class UsedMostOftenComparatorDesc implements Comparator<CustomAppHolder> {

        @Override
        public int compare(CustomAppHolder left, CustomAppHolder right) {

            return Long.compare(right.timeInForeground, left.timeInForeground);
        }
    }

    private class UpdateStats extends AsyncTask<String, Void, StatsData> {

        private String timeInterval;

        @Override
        protected StatsData doInBackground(String... strings) {

            this.timeInterval = strings[0];
            StatsData stats = new StatsData();

            try {
                reentrantLock.lock();

                int unlocks = sharedPreferences.getInt("noOfUnlocks", 0);
                long totalDuration = sharedPreferences.getLong("totalDuration", 0);

                if (notiSettingsOne) {
                    Duration timeOnPhone = Duration.millis(totalDuration);
                    hours = timeOnPhone.getStandardHours();
                    if (preHours != hours && hours != 0) {
                        Log.d(TAG, "UPDATE : prehours = " + preHours + " hours = " + hours);
                        Intent intent = new Intent(context, NotificationReceiver.class);
                        intent.putExtra("Title", "NoCrastinate Usage Time Alert!");
                        intent.putExtra("AlarmID", 10001);
                        intent.putExtra("Content", "You've been using your phone for " + hours + " hours today! :(");
                        PendingIntent startPIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
                        AlarmManager alarm = (AlarmManager) context.getSystemService(ALARM_SERVICE);
                        alarm.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), startPIntent);
                        preHours = hours;
                    } else preHours = hours;
                }

                ToDoDBContract.ToDoListDbHelper toDoHelper = new ToDoDBContract.ToDoListDbHelper(context);
                long beginTime = new DateTime().withTimeAtStartOfDay().getMillis();
                long endTime = new DateTime().plusDays(1).withTimeAtStartOfDay().getMillis();
                long tasksCompleted = toDoHelper.getNoOfCompletedToDos(beginTime, endTime);

                stats.setTasksCompleted(tasksCompleted);
                stats.setNoOfUnlocks(unlocks);
                stats.setOverallTime(totalDuration);


            } finally {
                reentrantLock.unlock();
            }

            List<CustomAppHolder> appStatList = updateAppsList(getStats("Daily", context), context);

            if (!timeInterval.equals("Daily")) {

                StatsDBContract.StatsDBHelper statsHelper = new StatsDBContract.StatsDBHelper(context);
                SQLiteDatabase db = statsHelper.getReadableDatabase();
                ArrayList<StatsData> statsFromInterval = statsHelper.getStatsForInterval(timeInterval);

                int collectedUnlocks = stats.getNoOfUnlocks();
                long collectedCompleted = stats.getTasksCompleted();
                long collectedTime = stats.getOverallTime();

                for (StatsData queriedStats : statsFromInterval) {
                    collectedUnlocks += queriedStats.getNoOfUnlocks();
                    collectedCompleted += queriedStats.getTasksCompleted();
                    collectedTime += queriedStats.getOverallTime();
                }
                stats.setTasksCompleted(collectedCompleted);
                stats.setNoOfUnlocks(collectedUnlocks);
                stats.setOverallTime(collectedTime);

                List<CustomAppHolder> intervalList = updateAppsList(getStats(timeInterval, context), context);

                // Compares the daily appStatList with the intervalList, adds daily stats to interval stats
                // Finds the elements which are present in intervalList but not in the daily appStatList
                // Adds any missing elements to the appStatList for display
                for (int i = 0; i < intervalList.size(); i++) {
                    int j;
                    boolean found = false;
                    for (j = 0; j < appStatList.size(); j++) {
                        if (appStatList.get(j).packageName.equals(intervalList.get(i).packageName)) {
                            Long time = intervalList.get(i).timeInForeground;
                            appStatList.get(j).timeInForeground += time;
                            found = true;
                            break;
                        }
                    }
                    if (!found) {
                        appStatList.add(intervalList.get(i));
                    }
                }
            }
            Collections.sort(appStatList, new UsedMostOftenComparatorDesc());
            stats.setAppsList(appStatList);
            return stats;
        }

        @Override
        protected synchronized void onPostExecute(StatsData iconData) {

            if (getView() == statsView) {

                TextView timeHeader = (TextView) getView().findViewById(R.id.stats_header);
                TextView textViewUnlocks = (TextView) getView().findViewById(R.id.text_view_no_of_unlocks);
                TextView textViewOverallTime = (TextView) getView().findViewById(R.id.text_view_overall_time);
                TextView textViewTasks = (TextView) getView().findViewById(R.id.text_view_tasks_completed);
                TextView textViewTimeSpan = (TextView) getView().findViewById(R.id.time_span);

                long timeLaunched = sharedPreferences.getLong("statisticsLaunch", 0);

                if (timeLaunched > System.currentTimeMillis() - 1500) {
                    mRecyclerView.setAdapter(new AlphaInAnimationAdapter(mUsageListAdapter));
                    timeHeader.setAlpha(0);
                    textViewUnlocks.setAlpha(0);
                    textViewOverallTime.setAlpha(0);
                    textViewTasks.setAlpha(0);
                    timeHeader.animate().alpha(1).setDuration(200);
                    textViewUnlocks.animate().alpha(1).setDuration(200);
                    textViewOverallTime.animate().alpha(1).setDuration(200);
                    textViewTasks.animate().alpha(1).setDuration(200);
                    textViewTimeSpan.animate().alpha(1).setDuration(200);
                } else {
                    mRecyclerView.setAdapter(mUsageListAdapter);
                    textViewTimeSpan.setAlpha(1);
                }
                mUsageListAdapter.setCustomAppList(iconData.getAppsList());

                timeHeader.setText(TimeHelper.getHeadingString(timeInterval));
                textViewUnlocks.setText(iconData.getNoOfUnlocks() + "");
                textViewOverallTime.setText(TimeHelper.formatDuration(iconData.getOverallTime()));
                textViewTasks.setText(iconData.getTasksCompleted() + "");

            }
        }
    }
}
