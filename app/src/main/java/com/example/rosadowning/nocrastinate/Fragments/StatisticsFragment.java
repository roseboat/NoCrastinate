package com.example.rosadowning.nocrastinate.Fragments;
/*
Arguably the most complex class in the project. Represents the Statistics screen on the application.
Runs a thread every 3 seconds to determine whether or not the user has given the app Usage Access priviledges.
When settings are turned on the thread then runs every 30 seconds, getting the stats for the number of times
the user has unlocked their phone, the number of to-dos on their to-do list they have checked off, the overall
time they have spent on their phone and the list of installed applications that they have on their phone and how
much time they have spent on each application. All stats can be viewed for daily, weekly, monthly or yearly intervals.
 */


import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.usage.UsageEvents;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;

import com.example.rosadowning.nocrastinate.Adapters.AppStatisticsAdapter;
import com.example.rosadowning.nocrastinate.BroadcastReceivers.NotificationReceiver;
import com.example.rosadowning.nocrastinate.DBHelpers.AppStatsDBContract;
import com.example.rosadowning.nocrastinate.DBHelpers.StatsDBContract;
import com.example.rosadowning.nocrastinate.DBHelpers.ToDoDBContract;
import com.example.rosadowning.nocrastinate.DataModels.CustomAppHolder;
import com.example.rosadowning.nocrastinate.DataModels.StatsData;
import com.example.rosadowning.nocrastinate.DataModels.TimeHelper;
import com.example.rosadowning.nocrastinate.R;

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

    // Sets up many of the fragment's instance variables
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.reentrantLock = new ReentrantLock();
        this.intervalString = "Daily";
        this.context = getContext();
        this.sharedPreferences = context.getSharedPreferences(STATS_PREF_NAME, Context.MODE_PRIVATE);
        this.editor = sharedPreferences.edit();
        mUsageStatsManager = (UsageStatsManager) getActivity().getSystemService(Context.USAGE_STATS_SERVICE);
    }

    // Recalls the onCreate() method in the event that the fragment resumes
    @Override
    public void onResume() {
        super.onResume();
        this.onCreate(null);
    }

    // When the StatisticsFragment is started up, the time is stored in a SharedPreference variable so that stats can be displayed with a fade-in
    @Override
    public void onStart() {
        super.onStart();
        editor.putLong("statisticsLaunch", System.currentTimeMillis());
        editor.apply();
    }

    // Method called when fragment is activated
    @Override
    public void onViewCreated(View rootView, Bundle savedInstanceState) {
        super.onViewCreated(rootView, savedInstanceState);
        this.statsView = getView();

        // Gets the elements in the view
        mUsagePopUp = (LinearLayout) rootView.findViewById(R.id.settings_popup);
        Button mOpenUsageSettingButton = (Button) rootView.findViewById(R.id.button_open_usage_setting);
        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.recyclerview_app_usage);
        mSpinner = (Spinner) rootView.findViewById(R.id.spinner_time_span);

        // If the mOpenUsageSettingButton is visible and is clicked by the user, an intent is started which takes them to their Usage Access Settings
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

            // The names of the different time intervals in the spinner
            String[] strings = getResources().getStringArray(R.array.action_list);

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                intervalString = strings[position];
                if (intervalString != null) {

                    SharedPreferences usagePref = context.getSharedPreferences("UsageSettings", Context.MODE_PRIVATE);
                    final SharedPreferences.Editor editor = usagePref.edit();

                    // Determines whether the user has set their notification preferences to frequency 1
                    // If the user has set their notifications to frequency 1, the variable hours and prehours are set to the number of hours the user has currently spent on their phone
                    // In the Async Task, hours will change as the time increases and will be compared with prehours to determine whether an hour has passed while the phone is active, then a notification will be scheduled
                    long overallTime = sharedPreferences.getLong("totalDuration", 0);
                    SharedPreferences notiPreferences = context.getSharedPreferences("NotificationCheckboxes", Context.MODE_PRIVATE);
                    notiSettingsOne = notiPreferences.getBoolean("checkbox1", false);

                    if (notiSettingsOne) {
                        Duration timeOnPhone = Duration.millis(overallTime);
                        hours = timeOnPhone.getStandardHours();
                        preHours = hours;
                    }

                    // Repeating thread
                    Timer timerCheckUsage = new Timer();
                    timerTaskCheckUsage = new TimerTask() {
                        @Override
                        public void run() {

                            // Gets Usage Stats for the previous day
                            // These are only used to determine whether or not the user has set their usage access settings to on
                            List<UsageStats> usageStatsList = mUsageStatsManager
                                    .queryUsageStats(UsageStatsManager.INTERVAL_BEST, new DateTime().minusDays(1).getMillis(),
                                            System.currentTimeMillis());

                            // If the usageStatsList is empty or the size is 0 then the usage access settings are off
                            if (usageStatsList.size() == 0 || usageStatsList.isEmpty()) {
                                Log.i(TAG, "The user may not allow the access to apps usage. ");

                                editor.putBoolean("SettingsOn", false);
                                editor.apply();

                                // Pop up prompting user to change their usage access settings is made visible
                                getActivity().runOnUiThread(new Runnable() {

                                    @Override
                                    public void run() {
                                        mUsagePopUp.setVisibility(View.VISIBLE);
                                        mSpinner.setVisibility(View.GONE);
                                        mUsagePopUp.bringToFront();
                                    }
                                });
                            } else {
                                // If the usage access settings are on...
                                editor.putBoolean("SettingsOn", true);
                                editor.apply();

                                // Pop up prompting user to change their settings is made invisible and the spinner is set to visible
                                getActivity().runOnUiThread(new Runnable() {

                                    @Override
                                    public void run() {
                                        mUsagePopUp.setVisibility(View.GONE);
                                        mSpinner.setVisibility(View.VISIBLE);
                                    }
                                });
                                // Calls the Async Task UpdateStats
                                UpdateStats newIcons = new UpdateStats();
                                synchronized (newIcons) {
                                    newIcons.execute(intervalString);

                                }
                            }
                        }
                    };
                    // If the settings are off, the thread should run every 3 seconds, otherwise it should run every 30 seconds
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

    // Method to get the app usage stats for a given interval
    public List<CustomAppHolder> getStats(String interval, Context context) {

        long startTime = 0;
        long endTime = 0;

        // If the interval is "Daily" set the start time to 12am today and the end time to the current time
        if (interval.equals("Daily")) {
            startTime = new DateTime().withTimeAtStartOfDay().getMillis();
            endTime = System.currentTimeMillis();
            // If the interval is "Yesterday" set the start time to 12am yesterday and the end time to 12am today
            // This is used by the MidnightDataResetReceiver when storing application stats for the day previous in a database
        } else if (interval.equals("Yesterday")) {
            startTime = new DateTime().withTimeAtStartOfDay().minusDays(1).getMillis();
            endTime = new DateTime().withTimeAtStartOfDay().getMillis();
        } else {
            // If the interval is "Weekly", "Monthly" or "Yearly", get the stats from the AppStats database
            AppStatsDBContract.AppStatsDbHelper dbHelper = new AppStatsDBContract.AppStatsDbHelper(context);
            return dbHelper.getStatsForInterval(interval); // return the stats
        }

        mUsageStatsManager = (UsageStatsManager) context.getSystemService(Context.USAGE_STATS_SERVICE);
        UsageEvents.Event currentEvent;
        List<UsageEvents.Event> allEvents = new ArrayList<>();
        HashMap<String, CustomAppHolder> map = new HashMap<>();

        // Gets all the UsageEvents between the start time and end time
        UsageEvents mEvents = mUsageStatsManager.queryEvents(startTime, endTime);

        // While mEvents has events, work through the events and compare one against another
        // If one is of type MOVE_TO_FOREGROUND or MOVE_TO_BACKGROUND, add it to the List of UsageEvents.Event
        // Store the event in the HashMap map, creating a new CustomAppHolder object with the event's package name
        while (mEvents.hasNextEvent()) {
            currentEvent = new UsageEvents.Event();
            mEvents.getNextEvent(currentEvent);

            if (currentEvent.getEventType() == UsageEvents.Event.MOVE_TO_FOREGROUND ||
                    currentEvent.getEventType() == UsageEvents.Event.MOVE_TO_BACKGROUND) {
                allEvents.add(currentEvent);
                String key = currentEvent.getPackageName();

                if (map.get(key) == null)
                    map.put(key, new CustomAppHolder(key)); // Creates a new CustomAppHolder object with the application's package name
            }
        }
        // Loop through the List allEvents, comparing one element against the next
        // If the first is MOVE_TO_FOREGROUND and the second is MOVE_TO_BACKGROUND and both events have the same class name - this represents a session where that application was active
        // Get the difference between the two timestamps and add the time to the corresponding CustomAppHolder in the Hashmap map
        for (int i = 0; i < allEvents.size() - 1; i++) {
            UsageEvents.Event preEvent = allEvents.get(i);
            UsageEvents.Event postEvent = allEvents.get(i + 1);

            if (preEvent.getEventType() == 1 && postEvent.getEventType() == 2
                    && preEvent.getClassName().equals(postEvent.getClassName())) {
                long diff = postEvent.getTimeStamp() - preEvent.getTimeStamp();
                map.get(preEvent.getPackageName()).timeInForeground += diff;
            }
        }
        // If allEvents are greater than 0, get the final event registered
        if (allEvents.size() > 0) {
            UsageEvents.Event finalEvent = allEvents.get(allEvents.size() - 1);

            // If the final event is MOVE_TO_FOREGROUND and the package name of the event contains "nocrastinate"
            // Then take the current time away from the MOVE_TO_FOREGROUND timestamp to determine the time spent on NoCrastinate up until that point
            // Add this time to the NoCrastinate CustomAppHolder's timeInForeground variable
            if (finalEvent.getEventType() == 1 && finalEvent.getPackageName().contains("nocrastinate")) {
                long diff = System.currentTimeMillis() - finalEvent.getTimeStamp();
                map.get(finalEvent.getPackageName()).timeInForeground += diff;
            }
        }
        // Convert the map of CustomAppHolders to a List
        List<CustomAppHolder> allEventsList = new ArrayList<>(map.values());

        // If the interval is "Daily", get the total time the user has spent on their phone that day by adding together all of the times of all events registered
        // Put the total time in a SharedPreferences variable "totalDuration" which can be accessed by the Async Task UpdateStats
        if (interval.equals("Daily")) {
            long totalTime = 0;
            for (CustomAppHolder event : allEventsList) {
                totalTime += event.timeInForeground;
            }
            if (editor != null) {
                editor.putLong("totalDuration", totalTime);
                editor.apply();
            }
        }
        return allEventsList; // return the list of CustomAppHolders
    }

    // Filters out any uninstalled, system applications and returns only those apps that have been installed on the phone
    public List<CustomAppHolder> updateAppsList(List<CustomAppHolder> allEventsList, Context context) {

        List<CustomAppHolder> updatedAppsList = new ArrayList<>();

        // Gets a list of all the packages on the phone
        List<PackageInfo> packages = context.getPackageManager()
                .getInstalledPackages(0);

        // Loop through all of the packages in the phone, if they are non-system apps then compare them with the package names in the allEventsList List
        // If the names match, put the app's package info (label name and app icon) and the info from the allEventsList (package name and timeInForeground) in a new CustomAppHolder object
        // Add the app to a new List of CustomAppHolders.
        for (PackageInfo packageInfo : packages) {
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

    // Used to sort the user's applications from most used to least used
    private static class UsedMostOftenComparatorDesc implements Comparator<CustomAppHolder> {

        @Override
        public int compare(CustomAppHolder left, CustomAppHolder right) {

            return Long.compare(right.timeInForeground, left.timeInForeground);
        }
    }

    // Inner Async Task which runs on a loop. Takes a String with the selected time interval as a parameter and returns a StatsData object.
    private class UpdateStats extends AsyncTask<String, Void, StatsData> {

        private String timeInterval;

        // doInBackground method handles logic of the task
        // Gets the stats for no. of unlocks, overall time on phone, to-dos completed and the list of installed apps and their time in the phone's foreground.
        @Override
        protected StatsData doInBackground(String... strings) {

            this.timeInterval = strings[0];
            StatsData stats = new StatsData();

            try {
                reentrantLock.lock(); // locks needed as we are accessing SharedPreference variables

                int unlocks = sharedPreferences.getInt("noOfUnlocks", 0);
                long totalDuration = sharedPreferences.getLong("totalDuration", 0);

                // If the user has set their notifications to frequency 1, they should receive a notification every hour they have spent on their phone
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

                // Gets the number of to-dos completed that day (since midnight) from the ToDoListDbHelper
                ToDoDBContract.ToDoListDbHelper toDoHelper = new ToDoDBContract.ToDoListDbHelper(context);
                long beginTime = new DateTime().withTimeAtStartOfDay().getMillis();
                long endTime = new DateTime().plusDays(1).withTimeAtStartOfDay().getMillis();
                long tasksCompleted = toDoHelper.getNoOfCompletedToDos(beginTime, endTime);

                // Stores unlocks, total duration and tasks completed in a StatsData object
                stats.setTasksCompleted(tasksCompleted);
                stats.setNoOfUnlocks(unlocks);
                stats.setOverallTime(totalDuration);
            } finally {
                reentrantLock.unlock();
            }

            // Gets a List of CustomAppHolder objects for the Daily interval
            List<CustomAppHolder> appStatList = updateAppsList(getStats("Daily", context), context);

            // If the time interval is NOT 'Daily', stats for the specific interval must be garnered and added to the daily stats
            if (!timeInterval.equals("Daily")) {

                // Gets the stats for the top three icons from the Stats database
                StatsDBContract.StatsDBHelper statsHelper = new StatsDBContract.StatsDBHelper(context);
                ArrayList<StatsData> statsFromInterval = statsHelper.getStatsForInterval(timeInterval);

                // Gets the daily stats for each icon storing them in new variables
                int collectedUnlocks = stats.getNoOfUnlocks();
                long collectedCompleted = stats.getTasksCompleted();
                long collectedTime = stats.getOverallTime();

                // Loops through all of the StatsData objects in the list returned from the database
                // Adds together all unlocks, time on phone and tasks completed
                for (StatsData queriedStats : statsFromInterval) {
                    collectedUnlocks += queriedStats.getNoOfUnlocks();
                    collectedCompleted += queriedStats.getTasksCompleted();
                    collectedTime += queriedStats.getOverallTime();
                }
                // Stores unlocks, total duration and tasks completed for the specific interval in the StatsData object, replacing those for the 'Daily' interval
                stats.setTasksCompleted(collectedCompleted);
                stats.setNoOfUnlocks(collectedUnlocks);
                stats.setOverallTime(collectedTime);

                // Gets the list of apps and their time in the phone's foreground for a given interval
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
            // Sorts the list of apps in order of most used
            Collections.sort(appStatList, new UsedMostOftenComparatorDesc());
            stats.setAppsList(appStatList);
            return stats; // Returns the StatsData object which is passed to the onPostExecute method automatically
        }

        // Method which runs on the UI thread and updates the page's views with the StatsData
        @Override
        protected synchronized void onPostExecute(StatsData iconData) {

            if (getView() == statsView) {

                // Gets the text views of the Statistics Fragment
                TextView timeHeader = (TextView) getView().findViewById(R.id.stats_header);
                TextView textViewUnlocks = (TextView) getView().findViewById(R.id.text_view_no_of_unlocks);
                TextView textViewOverallTime = (TextView) getView().findViewById(R.id.text_view_overall_time);
                TextView textViewTasks = (TextView) getView().findViewById(R.id.text_view_tasks_completed);
                TextView textViewTimeSpan = (TextView) getView().findViewById(R.id.time_span);

                long timeLaunched = sharedPreferences.getLong("statisticsLaunch", 0);

                // If the StatisticsFragment has been launched recently, animate the page's views
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
                // Give the adapter which displays the recycler view of apps and their time in the foreground the list of apps form iconData
                mUsageListAdapter.setCustomAppList(iconData.getAppsList());

                // Set the top header and the text on each of the top icons
                timeHeader.setText(TimeHelper.getHeadingString(timeInterval));
                textViewUnlocks.setText(iconData.getNoOfUnlocks() + "");
                textViewOverallTime.setText(TimeHelper.formatDuration(iconData.getOverallTime()));
                textViewTasks.setText(iconData.getTasksCompleted() + "");

            }
        }
    }
}