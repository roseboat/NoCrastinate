package com.example.rosadowning.nocrastinate.Fragments;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.SharedPreferences;
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
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

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


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_statistics, null);
    }

    public StatisticsFragment() {
    }

    // Factory method
    public static StatisticsFragment newInstance() {
        StatisticsFragment fragment = new StatisticsFragment();
        return fragment;
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.intervalString = "Daily";
        this.context = getContext();
        this.sharedPreferences = context.getSharedPreferences(STATS_PREF_NAME, Context.MODE_PRIVATE);
        mUsageStatsManager = (UsageStatsManager) getActivity().getSystemService(Context.USAGE_STATS_SERVICE);
    }

    public void onPause() {
        super.onPause();
        synchronized (timerTaskAsync) {
            timerTaskAsync.cancel();
        }
    }

    @Override
    public void onViewCreated(View rootView, Bundle savedInstanceState) {
        super.onViewCreated(rootView, savedInstanceState);
        this.statsView = getView();
        mUsageListAdapter = new AppStatisticsAdapter(context);
        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.recyclerview_app_usage);
        mRecyclerView.scrollToPosition(0);
        mRecyclerView.setAdapter(mUsageListAdapter);
        mUsagePopUp = (LinearLayout) rootView.findViewById(R.id.settings_popup);
        mOpenUsageSettingButton = (Button) rootView.findViewById(R.id.button_open_usage_setting);
        mSpinner = (Spinner) rootView.findViewById(R.id.spinner_time_span);

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
                    timerAsync.schedule(timerTaskAsync, 0, 3000);
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
        Log.d(TAG, "query time = " + queryTime);

        Map<String, UsageStats> usageStats = mUsageStatsManager.queryAndAggregateUsageStats(queryTime, System.currentTimeMillis());
        List<UsageStats> queryUsageStats = new ArrayList<>();
        queryUsageStats.addAll(usageStats.values());

        if (queryUsageStats.size() == 0) {
            Log.i(TAG, "The user may not allow the access to apps usage. ");
            mUsagePopUp.setVisibility(View.VISIBLE);
            mUsagePopUp.bringToFront();
            mOpenUsageSettingButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS));
                }
            });
        }
        return queryUsageStats;
    }

    void updateAppsList(List<UsageStats> usageStatsList) {
        List<CustomUsageStats> customUsageStatsList = new ArrayList<>();

        List<PackageInfo> installedPackages = context.getPackageManager()
                .getInstalledPackages(0);

        for (int i = 0; i < usageStatsList.size(); i++) {
            CustomUsageStats customUsageStats = new CustomUsageStats();
            customUsageStats.usageStats = usageStatsList.get(i);

            for (int j = 0; j < installedPackages.size(); j++) {
                PackageInfo packageInfo = installedPackages.get(j);
                if (packageInfo.packageName.equals(usageStatsList.get(i).getPackageName())) {
                    customUsageStats.appName = packageInfo.applicationInfo.loadLabel(context.getPackageManager()).toString();
                }
            }
            try {
                customUsageStats.appIcon = getActivity().getPackageManager()
                        .getApplicationIcon(customUsageStats.usageStats.getPackageName());
            } catch (PackageManager.NameNotFoundException e) {
                Log.w(TAG, String.format("App Icon is not found for %s",
                        customUsageStats.usageStats.getPackageName()));
                customUsageStats.appIcon = getActivity().getDrawable(R.drawable.brain);
            }
            customUsageStatsList.add(customUsageStats);
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

        @Override
        protected synchronized StatsIconData doInBackground(String... strings) {

            String timeInterval = strings[0];
            StatsIconData stats = new StatsIconData();

            if (sharedPreferences != null) {
                SharedPreferences.Editor editor = sharedPreferences.edit();

                int unlocks = sharedPreferences.getInt("noOfUnlocks", 0);
                long screenOn = sharedPreferences.getLong("screenOn", 0);
                long overallTime = 0;

                if (screenOn != 0) {
                    long currentTime = System.currentTimeMillis();
                    long difference = currentTime - screenOn;
                    long currentDuration = sharedPreferences.getLong("totalDuration", 0);
                    overallTime = difference + currentDuration;
                    editor.putLong("totalDuration", overallTime);
                    editor.putLong("screenOn", System.currentTimeMillis());

                } else {
                    editor.putLong("screenOn", System.currentTimeMillis());
                }
                editor.apply();

                ToDoReaderContract.ToDoListDbHelper toDoHelper = new ToDoReaderContract.ToDoListDbHelper(context);
                SQLiteDatabase sql = toDoHelper.getWritableDatabase();
                long tasksCompleted = toDoHelper.getNoOfCompletedToDos();

                stats.setTasksCompleted(tasksCompleted);
                stats.setNoOfUnlocks(unlocks);
                stats.setOverallTime(overallTime);

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
                    return stats;
                }
                return stats;
            } else return null;

        }


        @Override
        protected synchronized void onPostExecute(StatsIconData iconData) {

            if (getView() == statsView) {

                if ((Integer) iconData.getNoOfUnlocks() != null) {
                    TextView textViewUnlocks = (TextView) getView().findViewById(R.id.text_view_no_of_unlocks);
                    textViewUnlocks.setText(iconData.getNoOfUnlocks() + "");
                }

                if ((Long) iconData.getOverallTime() != null) {
                    String timeString = TimeHelper.formatDuration(iconData.getOverallTime());
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
