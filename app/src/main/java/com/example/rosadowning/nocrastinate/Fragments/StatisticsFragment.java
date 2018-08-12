package com.example.rosadowning.nocrastinate.Fragments;

import android.annotation.TargetApi;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.usage.UsageEvents;
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
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
    public void onStart(){
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

                            List<CustomAppHolder> usageStatsList = getStats(intervalString);

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

                                if (!sharedPreferences.getBoolean("InReceiver", true)) {
                                    UpdateStats newIcons = new UpdateStats();
                                    synchronized (newIcons) {
                                        newIcons.execute(intervalString);
                                    }
                                }
                            }
                        }
                    };
                    timerCheckUsage.schedule(timerTaskCheckUsage, 1000, 4000);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    public List<CustomAppHolder> getStats(String interval) {

        long startTime = 0;
        long endTime = System.currentTimeMillis();

        switch (interval) {
            case "Daily":
                startTime = new DateTime().withTimeAtStartOfDay().getMillis();
                break;
            case "Weekly":
                startTime = new DateTime().minusWeeks(1).getMillis();
                break;
            case "Monthly":
                startTime = new DateTime().minusMonths(1).getMillis();
                break;
            case "Yearly":
                startTime = new DateTime().minusYears(1).getMillis();
                break;
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
        return allEventsList;
    }

    private List<CustomAppHolder> updateAppsList(List<CustomAppHolder> allEventsList) {

        List<CustomAppHolder> updatedAppsList = new ArrayList<>();

        List<PackageInfo> installedPackages = context.getPackageManager()
                .getInstalledPackages(0);

        for (PackageInfo packageInfo : installedPackages) {

            if ((packageInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0) {

                for (int i = 0; i < allEventsList.size(); i++) {
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

        Collections.sort(updatedAppsList, new UsedMostOftenComparatorDesc());
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
                long screenOn = sharedPreferences.getLong("screenOn", 0);
                long screenOff = sharedPreferences.getLong("screenOff", 0);
                long overallTime = sharedPreferences.getLong("totalDuration", 0);
                long fragmentLaunched = sharedPreferences.getLong("statisticsLaunch", 0);


                if (screenOn != 0) {
                    if (fragmentLaunched > screenOff && screenOn > screenOff) {
                        long currentTime = System.currentTimeMillis();
                        long difference = currentTime - screenOn;
                        overallTime = difference + overallTime;
                        editor.putLong("totalDuration", overallTime);
                        editor.putLong("screenOn", System.currentTimeMillis());
                        editor.apply();

                        Log.d(TAG, "duration = " + TimeHelper.formatDuration(overallTime));

                        if (notiSettingsOne) {
                            Duration timeOnPhone = Duration.millis(overallTime);
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
                    }
                } else {
                    editor.putLong("screenOn", System.currentTimeMillis());
                    editor.putLong("totalDuration", 0);
                    editor.apply();
                }

                ToDoDBContract.ToDoListDbHelper toDoHelper = new ToDoDBContract.ToDoListDbHelper(context);
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
            }

            List<CustomAppHolder> appList = updateAppsList(getStats(timeInterval));
            stats.setAppsList(appList);

            return stats;
        }

        @Override
        protected synchronized void onPostExecute(StatsData iconData) {

            if (getView() == statsView) {

                long timeLaunched = sharedPreferences.getLong("statisticsLaunch", 0);

                if (timeLaunched > System.currentTimeMillis() - 3000){
                    mRecyclerView.setAdapter(new AlphaInAnimationAdapter(mUsageListAdapter));
//                    TextView textViewUnlocks = (TextView) getView().findViewById(R.id.text_view_no_of_unlocks);
//                    textViewUnlocks.animate().alpha(0).setDuration(1500);

                }else {
                    mRecyclerView.setAdapter(mUsageListAdapter);

                }
                mUsageListAdapter.setCustomAppList(iconData.getAppsList());
                mUsageListAdapter.notifyDataSetChanged();

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
