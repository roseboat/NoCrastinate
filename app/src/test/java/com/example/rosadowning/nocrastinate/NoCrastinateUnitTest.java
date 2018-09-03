package com.example.rosadowning.nocrastinate;
/*
Unit test class which tests the databases of the application, the most important methods
and ensures than upon loading a fragment, all of its views are not null.
 */

import android.content.Context;
import android.support.v4.app.Fragment;
import android.view.View;

import com.example.rosadowning.nocrastinate.DBHelpers.AlarmDBContract;
import com.example.rosadowning.nocrastinate.DBHelpers.AppStatsDBContract;
import com.example.rosadowning.nocrastinate.DBHelpers.BlockedAppsDBContract;
import com.example.rosadowning.nocrastinate.DBHelpers.StatsDBContract;
import com.example.rosadowning.nocrastinate.DBHelpers.ToDoDBContract;
import com.example.rosadowning.nocrastinate.DataModels.CustomAppHolder;
import com.example.rosadowning.nocrastinate.DataModels.StatsData;
import com.example.rosadowning.nocrastinate.DataModels.TimeHelper;
import com.example.rosadowning.nocrastinate.DataModels.ToDoItem;
import com.example.rosadowning.nocrastinate.Fragments.BlockAppsFragment;
import com.example.rosadowning.nocrastinate.Fragments.NotificationSettingsFragment;
import com.example.rosadowning.nocrastinate.Fragments.SettingsFragment;
import com.example.rosadowning.nocrastinate.Fragments.StatisticsFragment;
import com.example.rosadowning.nocrastinate.Fragments.ToDoFragment;

import org.joda.time.DateTime;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(RobolectricTestRunner.class)
public class NoCrastinateUnitTest {

    private MainActivity mainActivity;
    private Context appContext;
    private ToDoDBContract.ToDoListDbHelper toDoListDbHelper;
    private BlockedAppsDBContract.BlockedAppsDBHelper blockedAppsDBHelper;
    private AlarmDBContract.AlarmDBHelper alarmDBHelper;
    private StatsDBContract.StatsDBHelper statsDBHelper;
    private StatsData testStat;

    // Sets up the tests
    @Before
    public void setUp() throws Exception {
        mainActivity = Robolectric.buildActivity(MainActivity.class).create().start().resume().visible().get();
        appContext = mainActivity.getApplicationContext();

        this.toDoListDbHelper = new ToDoDBContract.ToDoListDbHelper(appContext);
        this.blockedAppsDBHelper = new BlockedAppsDBContract.BlockedAppsDBHelper(appContext);
        this.alarmDBHelper = new AlarmDBContract.AlarmDBHelper(appContext);
        this.statsDBHelper = new StatsDBContract.StatsDBHelper(appContext);
    }

    // Sets up a StatsIcon object to be used by tests
    @Before
    public void iconStats_setUp() {

        // Set the interval's date to 2 weeks ago
        // Stat should appear in Monthly and Yearly stats but NOT Weekly
        this.testStat = new StatsData();
        testStat.setDate(new DateTime().withTimeAtStartOfDay().minusWeeks(2).toDate());
        testStat.setNoOfUnlocks(25);
        testStat.setOverallTime(1863000);
        testStat.setTasksCompleted(45);
        statsDBHelper.insertNewStat(testStat);
    }

    // Confirms that the app's context is correct
    @Test
    public void useAppContext() throws Exception {
        // Context of the app under test.
        assertEquals("com.example.rosadowning.nocrastinate", appContext.getPackageName());
    }

    /*
    STATS TESTS
     */

    // Checks if the StatsIcon object created in the iconStats_setUp is present in the weekly list of StatsIcon objects
    @Test
    public void iconStats_testIfInWeeklyInterval() {

        // TESTS WEEKLY
        List<StatsData> weeklyStats = statsDBHelper.getStatsForInterval("Weekly");
        boolean inWeekly = false;
        for (StatsData s : weeklyStats) {
            if (s.getDate().getTime() == testStat.getDate().getTime()) {
                inWeekly = true;
                break;
            }
        }
        assertEquals(false, inWeekly);
    }

    // Checks if the StatsIcon object created in the iconStats_setUp is present in the monthly list of StatsIcon objects
    @Test
    public void iconStats_testIfInMonthlyInterval() {
        // TESTS MONTHLY
        List<StatsData> monthlyStats = statsDBHelper.getStatsForInterval("Monthly");
        boolean inMonthly = false;
        for (StatsData s : monthlyStats) {
            if (s.getDate().getTime() == testStat.getDate().getTime()) {
                inMonthly = true;
                break;
            }
        }
        assertEquals(true, inMonthly);
    }

    // Checks if the StatsIcon object created in the iconStats_setUp is present in the yearly list of StatsIcon objects
    @Test
    public void iconStats_testIfInYearlyInterval() {
        // TESTS YEARLY
        List<StatsData> yearlyStats = statsDBHelper.getStatsForInterval("Yearly");
        boolean inYearly = false;
        for (StatsData s : yearlyStats) {
            if (s.getDate().getTime() == testStat.getDate().getTime()) {
                inYearly = true;
                break;
            }
        }
        assertEquals(true, inYearly);
    }

    // Inserts a fake app into a list of CustomAppHolders and then calls a method to filter out any non-system, non-installed apps
    @Test
    public void usageStats_isCorrect() {

        StatisticsFragment statisticsFragment = new StatisticsFragment();
        List<CustomAppHolder> statsList = statisticsFragment.getStats("Daily", appContext);

        CustomAppHolder fakeApp = new CustomAppHolder("fakeApp");
        statsList.add(fakeApp);
        List<CustomAppHolder> updatedStatsList = statisticsFragment.updateAppsList(statsList, appContext);

        boolean fakeAppPresent = false;
        for (CustomAppHolder app : updatedStatsList) {
            if (app.packageName.equals("fakeApp")) {
                fakeAppPresent = true;
                break;
            }
        }
        assertEquals("Fake App should not be present", false, fakeAppPresent);
    }

    // Checks that an alarm is successfully set in the alarm database
    @Test
    public void alarmDB_checkIfAlarmSet() {

        long timeNow = System.currentTimeMillis();
        alarmDBHelper.insertAlarm(timeNow);
        boolean isAlarmSet = alarmDBHelper.isAlarmSet(timeNow);

        assertEquals("Alarm is set", true, isAlarmSet);
        alarmDBHelper.removeAlarm(timeNow);
    }

    // Checks whether a to do is inserted and deleted successfully from the To Do database
    @Test
    public void toDo_InsertedThenDeletedFromDatabase() {

        ToDoItem testToDo = new ToDoItem("I am to be deleted");
        testToDo.setNote("I am doomed");
        testToDo.setAddedDate(new Date(System.currentTimeMillis()));
        testToDo.setDueDate(new DateTime().plusDays(4).toDate());
        testToDo.setAlarmDate(new DateTime().plusHours(48).toDate());
        testToDo.setCompleted(false);
        testToDo.setStarred(false);

        toDoListDbHelper.insertNewToDo(testToDo);
        int testToDoID = toDoListDbHelper.getID(testToDo);
        boolean matched = false;

        List<ToDoItem> toDoItems = toDoListDbHelper.getToDoList(testToDo.getCompleted());
        for (ToDoItem todo : toDoItems) {
            int toDoID = toDoListDbHelper.getID(todo);
            if (toDoID == testToDoID) {
                matched = true;
                break;
            }
        }
        assertEquals("Test to do has been inserted", true, matched);

        toDoListDbHelper.deleteToDo(testToDoID);

        boolean deleted = true;
        List<ToDoItem> afterDeletionItems = toDoListDbHelper.getToDoList(testToDo.getCompleted());
        for (ToDoItem todo : afterDeletionItems) {
            int toDoID = toDoListDbHelper.getID(todo);
            if (toDoID == testToDoID) {
                deleted = false;
                break;
            }
        }

        assertEquals("Test to do has been successfully deleted", true, deleted);
    }

    // Checks whether the To Do database is cleared successfully
    @Test
    public void toDo_clearToDoDatabase() {

        toDoListDbHelper.clearToDoList();
        List<ToDoItem> items = toDoListDbHelper.getToDoList(false);
        assertEquals("Initial clearing. Database size should = 0", 0, items.size());
    }

    // Inserts two different to-dos to the to do database, each with different isCompleted booleans - then confirms that they appear in the correct kind of lists when queried
    @Test
    public void toDo_testCompleteAndIncompleteToDoLists() {

        toDoListDbHelper.clearToDoList();

        ToDoItem test1 = new ToDoItem("Incomplete");
        ToDoItem test2 = new ToDoItem("Complete");

        test1.setCompleted(false);
        test2.setCompleted(true);

        toDoListDbHelper.insertNewToDo(test1);
        toDoListDbHelper.insertNewToDo(test2);

        List<ToDoItem> incompleteToDos = toDoListDbHelper.getToDoList(false);
        List<ToDoItem> completeToDos = toDoListDbHelper.getToDoList(true);

        assertEquals("Uncompleted To Do List Has One Element", 1, incompleteToDos.size());
        assertEquals("Completed To Do List Has One Element", 1, completeToDos.size());
        assertEquals("Uncompleted To Do Name = 'Incomplete'", "Incomplete", incompleteToDos.get(0).getName());
        assertEquals("Completed To Do Name = 'Complete'", "Complete", completeToDos.get(0).getName());
    }

    // Tests that to dos are edited correctly
    @Test
    public void toDo_setToDoAsCompleted() {

        toDoListDbHelper.clearToDoList();

        ToDoItem toDoItem = new ToDoItem("Switching from incomplete to complete");
        toDoListDbHelper.insertNewToDo(toDoItem); // default isCompleted = false
        toDoListDbHelper.setCompleted(toDoItem, true);

        List<ToDoItem> completeToDos = toDoListDbHelper.getToDoList(true);
        assertEquals("toDoItem in Completed To-Do List", "Switching from incomplete to complete", completeToDos.get(0).getName());
    }

    // Tests that to dos are edited correctly
    @Test
    public void toDo_setToDoAsStarred() {

        toDoListDbHelper.clearToDoList();

        ToDoItem toDoItem = new ToDoItem("Switching from unstarred to starred");
        toDoListDbHelper.insertNewToDo(toDoItem); // default isStarred = false
        toDoListDbHelper.setStarred(toDoItem, true);

        List<ToDoItem> toDoItems = toDoListDbHelper.getToDoList(toDoItem.getCompleted());
        assertEquals("toDoItem is starred", true, toDoItems.get(0).getStarred());
    }

    // Inserts a fake app into the BlockedAppsDB
    @Test
    public void blockedApps_databaseInsertion() {

        blockedAppsDBHelper.insertApp("FakeAppInserted");
        List<String> blockedApps = blockedAppsDBHelper.getBlockedApps();

        boolean containsFake = false;
        for (String s : blockedApps) {
            if (s.equals("FakeAppInserted")) {
                containsFake = true;
                break;
            }
        }
        assertEquals("Insertion of fake app successful", true, containsFake);
    }

    // Inserts and deletes a fake app
    @Test
    public void blockedApps_databaseDeletion() {

        blockedAppsDBHelper.insertApp("ToBeDeleted");
        List<String> blockedApps = blockedAppsDBHelper.getBlockedApps();

        boolean containsToBeDeleted = false;
        for (String s : blockedApps) {
            if (s.equals("ToBeDeleted")) {
                containsToBeDeleted = true;
                break;
            }
        }
        assertEquals("ToBeDeleted inserted successfully", true, containsToBeDeleted);

        blockedAppsDBHelper.removeApp("ToBeDeleted");
        List<String> blockedAppsPostDelete = blockedAppsDBHelper.getBlockedApps();
        containsToBeDeleted = false;

        for (String s : blockedAppsPostDelete) {
            if (s.equals("ToBeDeleted")) {
                containsToBeDeleted = true;
                break;
            }
        }
        assertEquals("ToBeDeleted deleted successfully", false, containsToBeDeleted);
    }

    // Inserts a fake app into the BlockedAppsDB then checks that the BlockAppsFragment method getApps does NOT return the fake app
    @Test
    public void blockedApps_fragmentMethodDoesNotReturnFakeApp() {

        blockedAppsDBHelper.insertApp("fakeApp");

        BlockAppsFragment blockAppsFragment = new BlockAppsFragment();
        List<CustomAppHolder> blockedApps = blockAppsFragment.getApps(appContext);

        boolean containsFakeApp = false;
        for (CustomAppHolder app : blockedApps) {
            if (app.packageName.equals("fakeApp")) {
                containsFakeApp = true;
                break;
            }
        }

        assertEquals("'fakeApp' should not be in blocked apps", false, containsFakeApp);
        blockedAppsDBHelper.removeApp("fakeApp");
    }

    // Tests the static method formatDuration()
    @Test
    public void timeHelper_formatDurationIsCorrect(){
        String time = "2d 3h 45m ";
        long testDuration = 186300000; // 2 days, 3 hours, 45 minutes
        String timeHelperString = TimeHelper.formatDuration(testDuration);
        assertEquals("TimeHelper string reads \"2d 3h 45m \"", time, timeHelperString);
    }

    // Tests the static method getHeadingString()
    @Test
    public void timeHelper_headingStringIsCorrect(){

        String dailyExpected = new SimpleDateFormat("EEEE, MMMM, dd, yyyy").format(new DateTime().withTimeAtStartOfDay().getMillis());
        String dailyActual = TimeHelper.getHeadingString("Daily");
        assertEquals(dailyExpected, dailyActual);

        String weeklyExpected = "The Past Week...";
        String weeklyActual = TimeHelper.getHeadingString("Weekly");
        assertEquals(weeklyExpected, weeklyActual);

        String monthlyExpected = "The Past Month...";
        String monthlyActual = TimeHelper.getHeadingString("Monthly");
        assertEquals(monthlyExpected, monthlyActual);

        String yearlyExpected = "The Past Year...";
        String yearlyActual = TimeHelper.getHeadingString("Yearly");
        assertEquals(yearlyExpected, yearlyActual);
    }

   /*
   Tests that the views on each fragment are not null
    */
    @Test
    public void mainActivity_isNotNull(){
        assertNotNull(mainActivity);
    }

    @Test
    public void mainActivity_viewsNotNull(){
        assertNotNull(mainActivity.findViewById(R.id.fragment_container));
        assertNotNull(mainActivity.findViewById(R.id.navigation_bar));
        assertNotNull(mainActivity.findViewById(R.id.navigation_stats));
        assertNotNull(mainActivity.findViewById(R.id.navigation_todo));
        assertNotNull(mainActivity.findViewById(R.id.navigation_settings));
    }

    @Test
    public void statsFragment_viewsNotNull(){
        assertNotNull(mainActivity.findViewById(R.id.stats_header));
        assertNotNull(mainActivity.findViewById(R.id.icon_group_main));
        assertNotNull(mainActivity.findViewById(R.id.recyclerview_app_usage));
        assertNotNull(mainActivity.findViewById(R.id.spinner_group));
        assertNotNull(mainActivity.findViewById(R.id.spinner_time_span));
    }

    @Test
    public void toDoFragment_viewsNotNull(){
        startFragment(new ToDoFragment());
        assertNotNull(mainActivity.findViewById(R.id.to_do_recycler_view));
        assertNotNull(mainActivity.findViewById(R.id.button_add_new_todo));
        assertNotNull(mainActivity.findViewById(R.id.button_view_completed_todos));
    }

    @Test
    public void settingsFragment_viewsNotNull(){
        startFragment(new SettingsFragment());
        assertNotNull(mainActivity.findViewById(R.id.help_center));
        assertNotNull(mainActivity.findViewById(R.id.notification_settings));
        assertNotNull(mainActivity.findViewById(R.id.block_apps));
    }

    @Test
    public void notificationSettings_viewsNotNull(){
        startFragment(new NotificationSettingsFragment());
        assertNotNull(mainActivity.findViewById(R.id.notification_checkbox_1));
        assertNotNull(mainActivity.findViewById(R.id.notification_checkbox_2));
        assertNotNull(mainActivity.findViewById(R.id.notification_checkbox_3));

        mainActivity.findViewById(R.id.notification_checkbox_1).setSelected(false);
        mainActivity.findViewById(R.id.notification_checkbox_1).setSelected(false);
        mainActivity.findViewById(R.id.notification_checkbox_1).setSelected(false);
    }

    // Post tests tear down
    @After
    public void tearDown() {
        blockedAppsDBHelper.clearDatabase();
        toDoListDbHelper.clearToDoList();
        statsDBHelper.deleteStat(testStat);
    }

    /*
    HELPER METHODS
     */
    public String getActiveFragment(){
       return mainActivity.getSupportFragmentManager().findFragmentById(R.id.fragment_container).getClass().getSimpleName();
    }

    public void startFragment(Fragment fragment){

        mainActivity.getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, fragment).commit();

    }

}