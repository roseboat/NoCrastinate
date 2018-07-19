package com.example.rosadowning.nocrastinate.Fragments;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
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
import com.example.rosadowning.nocrastinate.CustomUsageStats;
import com.example.rosadowning.nocrastinate.DBHelpers.StatsDBContract;
import com.example.rosadowning.nocrastinate.R;
import com.example.rosadowning.nocrastinate.DBHelpers.ToDoReaderContract;
import com.example.rosadowning.nocrastinate.StatsIconData;

import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
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

import org.joda.time.Duration;
import org.joda.time.format.PeriodFormatter;
import org.joda.time.format.PeriodFormatterBuilder;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class StatisticsFragment extends Fragment {

    private static final String TAG = "STATISTICS FRAGMENT";
    private UsageStatsManager mUsageStatsManager;
    private AppStatisticsAdapter mUsageListAdapter;
    private RecyclerView mRecyclerView;
    private RecyclerView.LayoutManager mLayoutManager;
    private Button mOpenUsageSettingButton;
    private Spinner mSpinner;
    private LinearLayout mUsagePopUp;
    private StatsIconData dailyStats;

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


    public void onStart(){
        super.onStart();
        getDailyIconData();
    }

    public void onResume(){
        super.onResume();
        getDailyIconData();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mUsageStatsManager = (UsageStatsManager) getActivity().getSystemService(Context.USAGE_STATS_SERVICE);
    }

    @Override
    public void onViewCreated(View rootView, Bundle savedInstanceState) {
        super.onViewCreated(rootView, savedInstanceState);

        mUsageListAdapter = new AppStatisticsAdapter(getContext());
        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.recyclerview_app_usage);
        mLayoutManager = mRecyclerView.getLayoutManager();
        mRecyclerView.scrollToPosition(0);
        mRecyclerView.setAdapter(mUsageListAdapter);
        mUsagePopUp = (LinearLayout) rootView.findViewById(R.id.settings_popup);
        mOpenUsageSettingButton = (Button) rootView.findViewById(R.id.button_open_usage_setting);
        mSpinner = (Spinner) rootView.findViewById(R.id.spinner_time_span);

        getDailyIconData();

        SpinnerAdapter spinnerAdapter = ArrayAdapter.createFromResource(getActivity(),
                R.array.action_list, android.R.layout.simple_spinner_dropdown_item);
        mSpinner.setAdapter(spinnerAdapter);
        mSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            String[] strings = getResources().getStringArray(R.array.action_list);

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                StatsUsageInterval statsUsageInterval = StatsUsageInterval
                        .getValue(strings[position]);
                if (statsUsageInterval != null) {
                    List<UsageStats> usageStatsList =
                            getUsageStatistics(statsUsageInterval.mInterval);
                    Collections.sort(usageStatsList, new UsedMostOftenComparatorDesc());
                    usageStatsList = usageStatsList.subList(0,20);
                    updateAppsList(usageStatsList);
                }
                if (statsUsageInterval.mStringRepresentation.equals("Daily")){
                    getDailyIconData();
                } else {

                StatsDBContract.StatsDBHelper statsHelper = new StatsDBContract.StatsDBHelper(getContext());
                SQLiteDatabase db = statsHelper.getReadableDatabase();

                Log.d(TAG, statsUsageInterval.mStringRepresentation);
                ArrayList<StatsIconData> statsFromInterval = statsHelper.getStatsForInterval(statsUsageInterval.mStringRepresentation);
                int collectedUnlocks = dailyStats.getNoOfUnlocks();
                long collectedCompleted = dailyStats.getTasksCompleted();
                long collectedTime = dailyStats.getOverallTime();

                for (StatsIconData stats: statsFromInterval){
                    collectedUnlocks += stats.getNoOfUnlocks();
                    collectedCompleted += stats.getTasksCompleted();
                    collectedTime += stats.getOverallTime();
                }
                setIconStats(collectedUnlocks, collectedTime, collectedCompleted);
                }

            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }



    public List<UsageStats> getUsageStatistics(int intervalType) {
        // Get the app statistics since one year ago from the current time.
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.YEAR, -1);

        List<UsageStats> queryUsageStats = mUsageStatsManager
                .queryUsageStats(intervalType, cal.getTimeInMillis(),
                        System.currentTimeMillis());

        if (queryUsageStats.size() == 0) {
            Log.i(TAG, "The user may not allow the access to apps usage. ");
            SharedPreferences sharedPreferences = getActivity().getSharedPreferences("StatisticsInfo", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putInt("noOfUnlocks", 0);
            editor.apply();
            mUsagePopUp.setVisibility(View.VISIBLE);
            mUsagePopUp.bringToFront();
            mOpenUsageSettingButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS));
                    mUsagePopUp.setVisibility(View.GONE);
                }
            });
        }
        return queryUsageStats;
    }

    public void getDailyIconData(){

        SharedPreferences sharedPreferences = getContext().getSharedPreferences("StatisticsInfo", Context.MODE_PRIVATE);

        int unlocks = sharedPreferences.getInt("noOfUnlocks",0);
        long overallTime = calculateOverallTime();

        ToDoReaderContract.ToDoListDbHelper toDoHelper = new ToDoReaderContract.ToDoListDbHelper(getContext());
        SQLiteDatabase sql = toDoHelper.getWritableDatabase();
        long tasksCompleted = toDoHelper.getNoOfCompletedToDos();
        setIconStats(unlocks, overallTime, tasksCompleted);

        dailyStats = new StatsIconData();
        dailyStats.setTasksCompleted(tasksCompleted);
        dailyStats.setNoOfUnlocks(unlocks);
        dailyStats.setOverallTime(overallTime);
    }

    public void setIconStats(int unlocks, long time, long tasksCompleted){

        TextView textViewUnlocks = (TextView) getView().findViewById(R.id.text_view_no_of_unlocks);
        textViewUnlocks.setText(unlocks + " unlocks");

        String timeString = timeToString(time);
        TextView textViewOverallTime = (TextView) getView().findViewById(R.id.text_view_overall_time);
        textViewOverallTime.setText(timeString);

        TextView textViewTasks = (TextView) getView().findViewById(R.id.text_view_tasks_completed);
        textViewTasks.setText(tasksCompleted + " tasks");

    }

    public String timeToString(long duration) {

        Duration dur = new Duration(duration);

        PeriodFormatter formatter = new PeriodFormatterBuilder()
                .appendDays()
                .appendSuffix("d")
                .appendHours()
                .appendSuffix("h")
                .appendMinutes()
                .appendSuffix("m")
                .toFormatter();
        String formatted = formatter.print(dur.toPeriod());
        return formatted;

    }

    public long calculateOverallTime(){

        SharedPreferences sharedPreferences = this.getContext().getSharedPreferences("StatisticsInfo", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        long screenOn = sharedPreferences.getLong("screenOn", 0);
        long newDuration = 0;

        if (screenOn != 0) {
            long currentTime = System.currentTimeMillis();
            long difference = currentTime - screenOn;
            long currentDuration = sharedPreferences.getLong("totalDuration", 0);
            newDuration = difference + currentDuration;
            editor.putLong("totalDuration", newDuration);
            editor.putLong("screenOn", System.currentTimeMillis());
        } else
            newDuration = 0;

        return newDuration;
    }

    void updateAppsList(List<UsageStats> usageStatsList) {
        List<CustomUsageStats> customUsageStatsList = new ArrayList<>();
        for (int i = 0; i < usageStatsList.size(); i++) {
            CustomUsageStats customUsageStats = new CustomUsageStats();
            customUsageStats.usageStats = usageStatsList.get(i);
            try {
                Drawable appIcon = getActivity().getPackageManager()
                        .getApplicationIcon(customUsageStats.usageStats.getPackageName());
                customUsageStats.appIcon = appIcon;
            } catch (PackageManager.NameNotFoundException e) {
                Log.w(TAG, String.format("App Icon is not found for %s",
                        customUsageStats.usageStats.getPackageName()));
                customUsageStats.appIcon = getActivity().getDrawable(R.drawable.ic_launcher_background);;
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

    static enum StatsUsageInterval {
        DAILY("Daily", UsageStatsManager.INTERVAL_DAILY),
        WEEKLY("Weekly", UsageStatsManager.INTERVAL_WEEKLY),
        MONTHLY("Monthly", UsageStatsManager.INTERVAL_MONTHLY),
        YEARLY("Yearly", UsageStatsManager.INTERVAL_YEARLY);

        private int mInterval;
        private String mStringRepresentation;

        StatsUsageInterval(String stringRepresentation, int interval) {
            mStringRepresentation = stringRepresentation;
            mInterval = interval;
        }

        static StatsUsageInterval getValue(String stringRepresentation) {
            for (StatsUsageInterval statsUsageInterval : values()) {
                if (statsUsageInterval.mStringRepresentation.equals(stringRepresentation)) {
                    return statsUsageInterval;
                }
            }
            return null;
        }
    }
}
