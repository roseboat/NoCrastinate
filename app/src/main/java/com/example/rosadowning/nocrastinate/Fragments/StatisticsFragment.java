package com.example.rosadowning.nocrastinate.Fragments;

import android.annotation.TargetApi;
import android.app.AppOpsManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.ResolveInfo;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.rosadowning.nocrastinate.Adapters.AppStatisticsAdapter;
import com.example.rosadowning.nocrastinate.DataModels.CustomUsageStats;
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
import android.view.animation.OvershootInterpolator;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;

import org.joda.time.DateTime;

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
import jp.wasabeef.recyclerview.animators.SlideInUpAnimator;

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
                    List<UsageStats> usageStatsList = getUsageStatistics(intervalString);

                    SharedPreferences usagePref = context.getSharedPreferences("UsageSettings", Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = usagePref.edit();

                    if (usageStatsList.size() == 0 || usageStatsList.isEmpty()) {
                        Log.i(TAG, "The user may not allow the access to apps usage. ");

                        editor.putBoolean("SettingsOn", false);
                        editor.apply();
                        mUsagePopUp.setVisibility(View.VISIBLE);
                        mUsagePopUp.bringToFront();
                        mOpenUsageSettingButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                startActivity(new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS));
                            }

                        });

                    } else {
                        mUsagePopUp.setVisibility(View.GONE);
                        editor.putBoolean("SettingsOn", true);
                        editor.apply();

                        Collections.sort(usageStatsList, new UsedMostOftenComparatorDesc());
                        updateAppsList(usageStatsList);

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
                        timerAsync.schedule(timerTaskAsync, 0, 4000);
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
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

    void updateAppsList(List<UsageStats> usageStatsList) {
        List<CustomUsageStats> customUsageStatsList = new ArrayList<>();

        List<PackageInfo> installedPackages = context.getPackageManager()
                .getInstalledPackages(0);

        for (PackageInfo packageInfo : installedPackages) {

            if ((packageInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0) {

                for (int i = 0; i < usageStatsList.size(); i++) {
                    CustomUsageStats customUsageStats = new CustomUsageStats();
                    customUsageStats.usageStats = usageStatsList.get(i);
                    if (packageInfo.packageName.equals(usageStatsList.get(i).getPackageName())) {
                        customUsageStats.appName = packageInfo.applicationInfo.loadLabel(context.getPackageManager()).toString();
                        try {
                            customUsageStats.appIcon = getActivity().getPackageManager()
                                    .getApplicationIcon(customUsageStats.usageStats.getPackageName());
                        } catch (PackageManager.NameNotFoundException e) {
                            Log.w(TAG, String.format("App Icon is not found for %s",
                                    customUsageStats.usageStats.getPackageName()));
                            customUsageStats.appIcon = getActivity().getDrawable(R.drawable.ic_nocrastinate_logo_only_transparent);
                        }
                        customUsageStatsList.add(customUsageStats);
                    }
                }
            }
        }
        mUsageListAdapter.setCustomUsageStatsList(customUsageStatsList);
        mUsageListAdapter.notifyDataSetChanged();
        mRecyclerView.scrollToPosition(0);
    }

    private static class UsedMostOftenComparatorDesc implements Comparator<UsageStats> {

        @Override
        public int compare(UsageStats left, UsageStats right) {
            return Long.compare(right.getTotalTimeInForeground(), left.getTotalTimeInForeground());
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
