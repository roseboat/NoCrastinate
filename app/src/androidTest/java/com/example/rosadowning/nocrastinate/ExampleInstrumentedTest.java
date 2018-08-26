package com.example.rosadowning.nocrastinate;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.Espresso;
import android.support.test.espresso.NoMatchingViewException;
import android.support.test.espresso.ViewAssertion;
import android.support.test.espresso.intent.Intents;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.support.v4.app.Fragment;
import android.view.View;

import com.example.rosadowning.nocrastinate.BroadcastReceivers.ScreenReceiver;
import com.example.rosadowning.nocrastinate.DBHelpers.AlarmDBContract;
import com.example.rosadowning.nocrastinate.DBHelpers.AppStatsDBContract;
import com.example.rosadowning.nocrastinate.DBHelpers.BlockedAppsDBContract;
import com.example.rosadowning.nocrastinate.DBHelpers.StatsDBContract;
import com.example.rosadowning.nocrastinate.DBHelpers.ToDoDBContract;
import com.example.rosadowning.nocrastinate.DataModels.CustomAppHolder;
import com.example.rosadowning.nocrastinate.DataModels.StatsData;
import com.example.rosadowning.nocrastinate.DataModels.ToDoItem;
import com.example.rosadowning.nocrastinate.Fragments.BlockAppsFragment;
import com.example.rosadowning.nocrastinate.Fragments.StatisticsFragment;
import com.example.rosadowning.nocrastinate.Services.BlockedAppsService;

import org.joda.time.DateTime;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Date;
import java.util.List;

import static android.content.Context.MODE_PRIVATE;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;

/**
 * Instrumented test, which will execute on an Android device.
 * NEEDS PHONE/EMULATOR
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class ExampleInstrumentedTest {

    private Context appContext;
    private ToDoDBContract.ToDoListDbHelper toDoListDbHelper;
    private BlockedAppsDBContract.BlockedAppsDBHelper blockedAppsDBHelper;
    private AlarmDBContract.AlarmDBHelper alarmDBHelper;
    private AppStatsDBContract.AppStatsDbHelper appStatsDbHelper;
    private StatsDBContract.StatsDBHelper statsDBHelper;
    private StatsData testStat;
    private String fakePackageName;

    @Before
    public void setUp() throws Exception {

        this.appContext = InstrumentationRegistry.getTargetContext();
        this.toDoListDbHelper = new ToDoDBContract.ToDoListDbHelper(appContext);
        this.blockedAppsDBHelper = new BlockedAppsDBContract.BlockedAppsDBHelper(appContext);
        this.alarmDBHelper = new AlarmDBContract.AlarmDBHelper(appContext);
        this.appStatsDbHelper = new AppStatsDBContract.AppStatsDbHelper(appContext);
        this.statsDBHelper = new StatsDBContract.StatsDBHelper(appContext);
        this.fakePackageName = "com.example.fake.application";

        Intents.init();
    }

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

    @Test
    public void useAppContext() throws Exception {
        // Context of the app under test.
        assertEquals("com.example.rosadowning.nocrastinate", appContext.getPackageName());
    }

    /*
    STATS TESTS
     */

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

    @Test
    public void alarmDB_checkIfAlarmSet() {

        long timeNow = System.currentTimeMillis();
        alarmDBHelper.insertAlarm(timeNow);
        boolean isAlarmSet = alarmDBHelper.isAlarmSet(timeNow);

        assertEquals("Alarm is set", true, isAlarmSet);
        alarmDBHelper.removeAlarm(timeNow);
    }

    /*
    TO DO LIST AND TO DO DATABASE TESTS
     */

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

    @Test
    public void toDo_clearToDoDatabase() {

        toDoListDbHelper.clearToDoList();
        List<ToDoItem> items = toDoListDbHelper.getToDoList(false);
        assertEquals("Initial clearing. Database size should = 0", 0, items.size());
    }

    @Test
    public void toDo_addingMultipleToDosToDatabase() {

        toDoListDbHelper.clearToDoList();
        toDoListDbHelper.insertNewToDo(new ToDoItem("to do 1"));
        toDoListDbHelper.insertNewToDo(new ToDoItem("to do 2"));
        toDoListDbHelper.insertNewToDo(new ToDoItem("to do 3"));

        List<ToDoItem> afterAddingItems = toDoListDbHelper.getToDoList(false);
        assertEquals("After adding items. Database size should = 3", 3, afterAddingItems.size());

    }

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

    @Test
    public void toDo_setToDoAsCompleted() {

        toDoListDbHelper.clearToDoList();

        ToDoItem toDoItem = new ToDoItem("Switching from incomplete to complete");
        toDoListDbHelper.insertNewToDo(toDoItem); // default isCompleted = false
        toDoListDbHelper.setCompleted(toDoItem, true);

        List<ToDoItem> completeToDos = toDoListDbHelper.getToDoList(true);
        assertEquals("toDoItem in Completed To-Do List", "Switching from incomplete to complete", completeToDos.get(0).getName());
    }

    @Test
    public void toDo_setToDoAsStarred() {

        toDoListDbHelper.clearToDoList();

        ToDoItem toDoItem = new ToDoItem("Switching from unstarred to starred");
        toDoListDbHelper.insertNewToDo(toDoItem); // default isStarred = false
        toDoListDbHelper.setStarred(toDoItem, true);

        List<ToDoItem> toDoItems = toDoListDbHelper.getToDoList(toDoItem.getCompleted());
        assertEquals("toDoItem is starred", true, toDoItems.get(0).getStarred());
    }

    /*
    TESTS BLOCKED APPS DATABASE AND GETAPPS() METHOD IN BLOCKEDAPPSFRAGMENT
     */
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

    @Test
    public void blockedApps_fragmentMethodDoesNotReturnFakeApp() {

        blockedAppsDBHelper.insertApp("FakeApp");

        BlockAppsFragment blockAppsFragment = new BlockAppsFragment();
        List<CustomAppHolder> blockedApps = blockAppsFragment.getApps(appContext);

        boolean containsFakeApp = false;
        for (CustomAppHolder app : blockedApps) {
            if (app.packageName.equals("FakeApp")) {
                containsFakeApp = true;
                break;
            }
        }

        assertEquals("'FakeApp' should not be in blocked apps", false, containsFakeApp);
        blockedAppsDBHelper.removeApp("FakeApp");
    }

    @Test
    public void blockedAppsService_foregroundAppIsCorrect() {

        // Launches NoCrastinate app for the purposes of testing
        Intent startMain = new Intent(appContext, MainActivity.class);
        startMain.addCategory(Intent.CATEGORY_LAUNCHER);
        startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        appContext.startActivity(startMain);

        // Thread must sleep for 1s so that the app fully launches
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Creates a new BlockedAppsService object and calls its method getForegroundApp()
        BlockedAppsService blockedAppsService = new BlockedAppsService(appContext);
        String foregroundApp = blockedAppsService.getForegroundApp();
        assertEquals("Foreground app is NoCrastinate", "com.example.rosadowning.nocrastinate", foregroundApp);
    }

    @After
    public void tearDown() {

        blockedAppsDBHelper.clearDatabase();
        toDoListDbHelper.clearToDoList();
        statsDBHelper.deleteStat(testStat);
        Intents.release();
    }

}
