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
import com.example.rosadowning.nocrastinate.Fragments.NotificationSettingsFragment;
import com.example.rosadowning.nocrastinate.Fragments.StatisticsFragment;
import com.example.rosadowning.nocrastinate.Fragments.ToDoFragment;
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
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;

@RunWith(AndroidJUnit4.class)
public class NoCrastinateInstrumentedTest {

    private Context appContext;
    @Rule
    public ActivityTestRule<MainActivity> myActivityTestRule =
            new ActivityTestRule<>(MainActivity.class, true, true);

    @Before
    public void setUp() throws Exception {
        this.appContext = InstrumentationRegistry.getTargetContext();
        this.myActivityTestRule.launchActivity(null);
    }

    /*
    Tests the BlockedService class's getForegroundApp() method
     */

//    @Test
//    public void blockedAppsService_foregroundAppIsCorrect() {
//
//        // Launches NoCrastinate app for the purposes of testing
//        Intent startMain = new Intent(appContext, MainActivity.class);
//        startMain.addCategory(Intent.CATEGORY_LAUNCHER);
//        startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
//        appContext.startActivity(startMain);
//
//        // Thread must sleep for 1s so that the app fully launches
//        try {
//            Thread.sleep(1000);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//
//        // Creates a new BlockedAppsService object and calls its method getForegroundApp()
//        BlockedAppsService blockedAppsService = new BlockedAppsService(appContext);
//        String foregroundApp = blockedAppsService.getForegroundApp();
//        assertEquals("Foreground app is NoCrastinate", "com.example.rosadowning.nocrastinate", foregroundApp);
//    }

    @Test
    public void addToDoTest(){
        String toDoName = "Finish Dissertation";
        onView(withId(R.id.navigation_stats)).perform(click());
        onView(withId(R.id.button_add_new_todo)).perform(click());
        onView(withId(R.id.addToDoName)).perform(typeText(toDoName));
        onView(withId(R.id.button_save_new_to_do)).perform(click());

        ToDoDBContract.ToDoListDbHelper toDoListDbHelper = new ToDoDBContract.ToDoListDbHelper(appContext);
        List<ToDoItem> toDos = toDoListDbHelper.getToDoList(false);

        boolean toDoFound = false;
        for (ToDoItem t : toDos){
            if (t.getName().equals(toDoName)){
             toDoFound = true;
             break;
            }
        }
        assertEquals("ToDo should be inserted into the database", true, toDoFound);
    }
}
