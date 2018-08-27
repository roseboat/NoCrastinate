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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;

@RunWith(AndroidJUnit4.class)
public class NoCrastinateInstrumentedTest {

    private Context appContext;


    @Before
    public void setUp() throws Exception {
        this.appContext = InstrumentationRegistry.getTargetContext();
    }

    /*
    Tests the BlockedService class's getForegroundApp() method
     */

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



    //    @Test
//    public void click() {
//
//        startFragment(new NotificationSettingsFragment());
//        SharedPreferences notiPreferences = mainActivity.getSharedPreferences("NotificationCheckboxes", Context.MODE_PRIVATE);
//
//        CheckBox checkbox1 = mainActivity.findViewById(R.id.notification_checkbox_1);
//        checkbox1.setChecked(false);
//
//        boolean checkbox1checked = notiPreferences.getBoolean("checkbox1", false);
//        assertEquals(true, checkbox1checked);
//    }
}
