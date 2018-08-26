package com.example.rosadowning.nocrastinate;

import android.support.v4.app.Fragment;
import android.view.View;

import com.example.rosadowning.nocrastinate.DataModels.TimeHelper;
import com.example.rosadowning.nocrastinate.Fragments.SettingsFragment;
import com.example.rosadowning.nocrastinate.Fragments.StatisticsFragment;
import com.example.rosadowning.nocrastinate.Fragments.ToDoFragment;

import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;

import java.text.SimpleDateFormat;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(RobolectricTestRunner.class)
public class NoCrastinateUnitTest {

    private MainActivity mainActivity;

    @Before
    public void setUp() throws Exception {
        mainActivity = Robolectric.buildActivity(MainActivity.class).create().start().resume().visible().get();
    }

    @Test
    public void timeHelper_formatDurationIsCorrect(){
        String time = "51h45m";
        long testDuration = 186300000; // 51 hours, 45 mins
        String timeHelperString = TimeHelper.formatDuration(testDuration);
        assertEquals("TimeHelper string reads '51h45m'", time, timeHelperString);
    }

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

    @Test
    public void mainActivity_IsNotNull(){
        assertNotNull(mainActivity);
    }

    @Test
    public void mainActivity_ViewsNotNull(){
        assertNotNull(mainActivity.findViewById(R.id.fragment_container));
        assertNotNull(mainActivity.findViewById(R.id.navigation_bar));
        assertNotNull(mainActivity.findViewById(R.id.navigation_stats));
        assertNotNull(mainActivity.findViewById(R.id.navigation_todo));
        assertNotNull(mainActivity.findViewById(R.id.navigation_settings));
    }

    @Test
    public void mainActivity_clickNavigationButtons(){
        startFragment(new StatisticsFragment());

        View toDoButton = mainActivity.findViewById(R.id.navigation_todo);
        View settingsButton = mainActivity.findViewById(R.id.navigation_settings);
        View statsButton = mainActivity.findViewById(R.id.navigation_stats);

        toDoButton.performClick();
        String expected = "ToDoFragment";
        String actual = getActiveFragment();
        assertEquals(expected, actual);

        settingsButton.performClick();
        expected = "SettingsFragment";
        actual = getActiveFragment();
        assertEquals(expected, actual);

        statsButton.performClick();
        expected = "StatisticsFragment";
        actual = getActiveFragment();
        assertEquals(expected, actual);
    }

    @Test
    public void statsFragment_ViewsNotNull(){
        assertNotNull(mainActivity.findViewById(R.id.stats_header));
        assertNotNull(mainActivity.findViewById(R.id.icon_group_main));
        assertNotNull(mainActivity.findViewById(R.id.recyclerview_app_usage));
        assertNotNull(mainActivity.findViewById(R.id.spinner_group));
        assertNotNull(mainActivity.findViewById(R.id.spinner_time_span));
    }

    @Test
    public void toDoFragment_ViewsNotNull(){
        startFragment(new ToDoFragment());
        assertNotNull(mainActivity.findViewById(R.id.to_do_recycler_view));
        assertNotNull(mainActivity.findViewById(R.id.button_add_new_todo));
        assertNotNull(mainActivity.findViewById(R.id.button_view_completed_todos));
    }

    @Test
    public void toDoFragment_clickViewCompletedToDoButton(){
        startFragment(new ToDoFragment());
        mainActivity.findViewById(R.id.button_view_completed_todos).performClick();
        String expected = "CompletedToDoFragment";
        String actual = getActiveFragment();
        assertEquals(expected, actual);
    }

    @Test
    public void toDoFragment_clickAddToDoButton(){
        startFragment(new ToDoFragment());
        mainActivity.findViewById(R.id.button_add_new_todo).performClick();
        String expected = "AddToDoFragment";
        String actual = getActiveFragment();
        assertEquals(expected, actual);
    }

    @Test
    public void settingsFragment_ViewsNotNull(){
        startFragment(new SettingsFragment());
        assertNotNull(mainActivity.findViewById(R.id.help_center));
        assertNotNull(mainActivity.findViewById(R.id.notification_settings));
        assertNotNull(mainActivity.findViewById(R.id.block_apps));
    }

    @Test
    public void settingsFragment_clickMenu(){
        startFragment(new SettingsFragment());

        View helpCenterView = mainActivity.findViewById(R.id.help_center);
        View notificationsView = mainActivity.findViewById(R.id.notification_settings);
        View blockAppsView = mainActivity.findViewById(R.id.block_apps);

        helpCenterView.performClick();
        String expected = "HelpCenterFragment";
        String actual = getActiveFragment();
        assertEquals(expected, actual);

        notificationsView.performClick();
        expected = "NotificationSettingsFragment";
        actual = getActiveFragment();
        assertEquals(expected, actual);

        blockAppsView.performClick();
        expected = "BlockAppsFragment";
        actual = getActiveFragment();
        assertEquals(expected, actual);
    }

    public String getActiveFragment(){
       return mainActivity.getSupportFragmentManager().findFragmentById(R.id.fragment_container).getClass().getSimpleName();
    }

    public void startFragment(Fragment fragment){
        mainActivity.getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, fragment).addToBackStack(null).commit();
    }
}