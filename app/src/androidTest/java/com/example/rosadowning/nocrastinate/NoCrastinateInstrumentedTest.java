package com.example.rosadowning.nocrastinate;

/*
Instrumented, functional end-to-end tests. Requires a device or emulator.
Also requires the device to have at least one installed application (other than NoCrastinate)
and at least one to-do in the to-do list.
 */

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.Espresso;
import android.support.test.espresso.NoMatchingViewException;
import android.support.test.espresso.UiController;
import android.support.test.espresso.ViewAction;
import android.support.test.espresso.ViewAssertion;
import android.support.test.espresso.contrib.RecyclerViewActions;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Checkable;

import com.example.rosadowning.nocrastinate.DBHelpers.BlockedAppsDBContract;
import com.example.rosadowning.nocrastinate.DBHelpers.ToDoDBContract;
import com.example.rosadowning.nocrastinate.DataModels.CustomAppHolder;
import com.example.rosadowning.nocrastinate.DataModels.ToDoItem;
import com.example.rosadowning.nocrastinate.Fragments.AddToDoFragment;
import com.example.rosadowning.nocrastinate.Fragments.BlockAppsFragment;
import com.example.rosadowning.nocrastinate.Fragments.CompletedToDoFragment;
import com.example.rosadowning.nocrastinate.Fragments.NotificationSettingsFragment;
import com.example.rosadowning.nocrastinate.Fragments.StatisticsFragment;
import com.example.rosadowning.nocrastinate.Fragments.ToDoFragment;
import com.example.rosadowning.nocrastinate.Services.BlockedAppsService;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;

import static android.support.test.espresso.Espresso.onData;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.RootMatchers.isDialog;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withSpinnerText;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.CoreMatchers.anything;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.isA;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

@RunWith(AndroidJUnit4.class)
public class NoCrastinateInstrumentedTest {

    private Context context;

    // Launches the main activity of the application
    @Rule
    public ActivityTestRule<MainActivity> myActivityTestRule =
            new ActivityTestRule<>(MainActivity.class, true, true);

    // Set up before the tests
    @Before
    public void setUp() throws Exception {
        this.context = InstrumentationRegistry.getTargetContext();

        SharedPreferences notiPreferences = context.getSharedPreferences("NotificationCheckboxes", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = notiPreferences.edit();
        editor.putBoolean("checkbox1", false);
        editor.apply();
    }

    // Tests the app's navigation through each fragment
    @Test
    public void navigateThroughoutApp() {
        goToFragment(new ToDoFragment());

        onView(withId(R.id.navigation_stats)).perform(click());
        onView(withId(R.id.stats_layout)).check(matches(isDisplayed()));

        onView(withId(R.id.navigation_todo)).perform(click());
        onView(withId(R.id.to_do_recycler_view)).check(matches(isDisplayed()));

        onView(withId(R.id.navigation_settings)).perform(click());
        onView(withId(R.id.settings_layout)).check(matches(isDisplayed()));

        onView(withId(R.id.help_center)).perform(click());
        onView(withId(R.id.help_center_scrollview)).check(matches(isDisplayed()));
        Espresso.pressBack();

        onView(withId(R.id.notification_settings)).perform(click());
        onView(withId(R.id.notification_checkbox_1)).check(matches(isDisplayed()));
        onView(withId(R.id.notification_checkbox_2)).check(matches(isDisplayed()));
        onView(withId(R.id.notification_checkbox_3)).check(matches(isDisplayed()));
        Espresso.pressBack();

        onView(withId(R.id.block_apps)).perform(click());
        onView(withId(R.id.block_apps_recycler_view)).check(matches(isDisplayed()));
        Espresso.pressBack();
        Espresso.pressBack();

        onView(withId(R.id.button_add_new_todo)).perform(click());
        onView(withId(R.id.addToDo_layout)).check(matches(isDisplayed()));

        Espresso.pressBack();
        onView(withId(R.id.button_view_completed_todos)).perform(click());
        onView(withId(R.id.completed_to_do_recycler_view)).check(matches(isDisplayed()));

        Espresso.pressBack();
        onView(withId(R.id.to_do_recycler_view))
                .perform(RecyclerViewActions.actionOnItemAtPosition(0, click()));
        onView(withId(R.id.viewToDo_layout)).check(matches(isDisplayed()));

    }

    // Checks that the StatisticsFragment's spinner displays the correct time intervals
    @Test
    public void stats_checkStatsSpinner() {
        goToFragment(new StatisticsFragment());

        onView(withId(R.id.spinner_time_span)).perform(click());
        onData(anything()).atPosition(0).perform(click());
        onView(withId(R.id.spinner_time_span)).check(matches(withSpinnerText(containsString("Daily"))));

        onView(withId(R.id.spinner_time_span)).perform(click());
        onData(anything()).atPosition(1).perform(click());
        onView(withId(R.id.spinner_time_span)).check(matches(withSpinnerText(containsString("Weekly"))));

        onView(withId(R.id.spinner_time_span)).perform(click());
        onData(anything()).atPosition(2).perform(click());
        onView(withId(R.id.spinner_time_span)).check(matches(withSpinnerText(containsString("Monthly"))));

        onView(withId(R.id.spinner_time_span)).perform(click());
        onData(anything()).atPosition(3).perform(click());
        onView(withId(R.id.spinner_time_span)).check(matches(withSpinnerText(containsString("Yearly"))));
    }

    // Adds a valid to-do to the To Do List
    @Test
    public void toDo_addValidToDo() {
        goToFragment(new AddToDoFragment());
        String toDoName = "Finish Dissertation";
        onView(withId(R.id.addToDoName)).perform(typeText(toDoName));
        Espresso.closeSoftKeyboard();
        onView(withId(R.id.button_save_new_to_do)).perform(click());

        ToDoDBContract.ToDoListDbHelper toDoListDbHelper = new ToDoDBContract.ToDoListDbHelper(context);
        List<ToDoItem> toDos = toDoListDbHelper.getToDoList(false);

        boolean toDoFound = false;
        for (ToDoItem t : toDos) {
            if (t.getName().equals(toDoName)) {
                toDoFound = true;
                break;
            }
        }
        assertEquals("ToDo should be inserted into the database", true, toDoFound);
    }

    // Attempts to add an invalid to-do to the To Do List - gets an error dialog
    @Test
    public void toDo_addInvalidToDo() {
        goToFragment(new AddToDoFragment());
        String toDoNote = "This to do is invalid";
        onView(withId(R.id.addToDoNote)).perform(typeText(toDoNote));
        Espresso.closeSoftKeyboard();
        onView(withId(R.id.button_save_new_to_do)).perform(click());
        String dialogMessage = myActivityTestRule.getActivity().getResources().getString(R.string.dialog_message_input_error_name);
        onView(withText(dialogMessage)).check(matches(isDisplayed()));
    }

    // Checks a to-do as completed and then confirms that it has been removed from the incompleted to-dos list
    @Test
    public void toDo_checkToDoAsCompleted() {
        goToFragment(new ToDoFragment());

        ToDoDBContract.ToDoListDbHelper toDoListDbHelper = new ToDoDBContract.ToDoListDbHelper(context);

        // To Do list before an item is deleted
        List<ToDoItem> preList = toDoListDbHelper.getToDoList(false);

        // Checks a to-do as completed
        onView(withId(R.id.to_do_recycler_view)).perform(
                RecyclerViewActions.actionOnItemAtPosition(0, MyViewAction.clickChildViewWithId(R.id.toDoCheckBox)));

        // To Do List after an item is deleted
        List<ToDoItem> postList = toDoListDbHelper.getToDoList(false);

        // Checks that the item is no longer in the list
        boolean isPresent = false;
        for (ToDoItem item : postList) {
            if (item.equals(preList.get(0))) {
                isPresent = true;
                break;
            }
        }
        assertEquals(false, isPresent);
    }

    // Stars a to-do and then confirms it has been starred in the database
    @Test
    public void toDo_starToDo() {
        goToFragment(new ToDoFragment());

        // Initially makes sure the first to do is NOT STARRED
        onView(withId(R.id.to_do_recycler_view)).perform(
                RecyclerViewActions.actionOnItemAtPosition(0, MyViewAction.setChecked(R.id.toDoStar, false)));

        // Gets the boolean for whether the first item in the list is starred
        ToDoDBContract.ToDoListDbHelper toDoListDbHelper = new ToDoDBContract.ToDoListDbHelper(context);
        List<ToDoItem> preList = toDoListDbHelper.getToDoList(false);
        boolean preItemStar = preList.get(0).getStarred();

        // Stars the to do
        onView(withId(R.id.to_do_recycler_view)).perform(
                RecyclerViewActions.actionOnItemAtPosition(0, MyViewAction.clickChildViewWithId(R.id.toDoStar)));

        // After starring the to-do, gets whether or not the to do has been starred
        List<ToDoItem> postList = toDoListDbHelper.getToDoList(false);
        boolean postItemStar = postList.get(0).getStarred();

        assertEquals("To do was initially NOT STARRED", false, preItemStar);
        assertEquals("To do was then starred", true, postItemStar);

    }

    // Compares the contents of the ToDoDB with the amount of elements in the To Do List's recycler view
    @Test
    public void toDo_recyclerViewHasCorrectNumOfElements() {
        goToFragment(new ToDoFragment());
        ToDoDBContract.ToDoListDbHelper dbHelper = new ToDoDBContract.ToDoListDbHelper(context);
        int expected = dbHelper.getToDoList(false).size();
        onView(withId(R.id.to_do_recycler_view)).check(new RecyclerViewItemCountAssertion(expected));
    }

    // Deletes a to-do from the application entirely
    @Test
    public void completedToDoList_deleteAnItemEntirely() {
        goToFragment(new CompletedToDoFragment());

        // Gets the first to-do item in the CompletedToDoFragment's recycler view
        ToDoDBContract.ToDoListDbHelper toDoListDbHelper = new ToDoDBContract.ToDoListDbHelper(context);
        List<ToDoItem> completedToDos = toDoListDbHelper.getToDoList(true);
        ToDoItem testItem = completedToDos.get(0);

        // Clicks on the first to-do item in the recycler view
        onView(withId(R.id.completed_to_do_recycler_view))
                .perform(RecyclerViewActions.actionOnItemAtPosition(0, click()));
        // Checks that the ViewToDoFragment has been displayed
        onView(withId(R.id.viewToDo_layout)).check(matches(isDisplayed()));
        // Clicks delete
        onView(withId(R.id.button_delete_to_do)).perform(click());
        // Gets the dialog message that appears when you attempt to delete a to do
        String dialogMessage = myActivityTestRule.getActivity().getResources().getString(R.string.dialog_message_confirm_delete);
        // Checks that the dialog displays the correct message
        onView(withText(dialogMessage)).check(matches(isDisplayed()));
        // Clicks the positive button in the dialog
        onView(withText("I'm sure!")).inRoot(isDialog())
                .check(matches(isDisplayed()))
                .perform(click());

        // Gets the completed to-do list post deletion
        List<ToDoItem> completedToDosPostDeletion = toDoListDbHelper.getToDoList(true);

        // Confirms that the to-do is no longer in the list
        boolean isDeleted = true;
        for (ToDoItem item : completedToDosPostDeletion) {
            if (item.equals(testItem)) {
                isDeleted = false;
                break;
            }
        }
        assertEquals("The To Do has been deleted", true, isDeleted);
    }

    // Compares the number of completed to dos in the ToDoDB to the number of elements in the CompletedToDoFragment's Recycler View
    @Test
    public void completedToDoList_recyclerViewHasCorrectNumOfElements() {
        goToFragment(new CompletedToDoFragment());
        ToDoDBContract.ToDoListDbHelper dbHelper = new ToDoDBContract.ToDoListDbHelper(context);
        int expected = dbHelper.getToDoList(true).size();
        onView(withId(R.id.completed_to_do_recycler_view)).check(new RecyclerViewItemCountAssertion(expected));
    }

    // Selects a notification preference and confirms the selection in SharedPreferences
    @Test
    public void notifications_selectPreference() {
        goToFragment(new NotificationSettingsFragment());
        onView(withId(R.id.notification_checkbox_1)).perform(click());

        SharedPreferences notiPreferences = context.getSharedPreferences("NotificationCheckboxes", Context.MODE_PRIVATE);
        boolean isOneChecked = notiPreferences.getBoolean("checkbox1", false);

        assertEquals("Notifications Frequency 1 should be checked", true, isOneChecked);
    }

    // Compares the number of installed applications on the user's phone to those displayed in the BlockAppsFragment's Recycler view
    @Test
    public void blockedApps_recyclerViewHasCorrectNumOfElements() {
        goToFragment(new BlockAppsFragment());
        BlockAppsFragment blockAppsFragment = new BlockAppsFragment();
        int expected = blockAppsFragment.getApps(context).size();
        onView(withId(R.id.block_apps_recycler_view)).check(new RecyclerViewItemCountAssertion(expected));
    }

    // Sets an app as 'blocked' in the BlockAppsFragment
    @Test
    public void blockedApps_setAppAsBlocked() {
        goToFragment(new BlockAppsFragment());

        // Initially makes sure the first app is NOT CHECKED
        onView(withId(R.id.block_apps_recycler_view)).perform(
                RecyclerViewActions.actionOnItemAtPosition(0, MyViewAction.setChecked(R.id.block_apps_switch, false)));

        // Checks the app
        onView(withId(R.id.block_apps_recycler_view)).perform(
                RecyclerViewActions.actionOnItemAtPosition(0, MyViewAction.clickChildViewWithId(R.id.block_apps_switch)));

        // Confirms that the app has been placed in the BlockedAppsDB
        BlockAppsFragment blockAppsFragment = new BlockAppsFragment();
        List<CustomAppHolder> apps = blockAppsFragment.getApps(context);

        BlockedAppsDBContract.BlockedAppsDBHelper blockedAppsDBHelper = new BlockedAppsDBContract.BlockedAppsDBHelper(context);
        List<String> blockedApps = blockedAppsDBHelper.getBlockedApps();

        boolean appsAreBlocked = false;
        if (blockedApps.contains(apps.get(0).packageName)) {
            appsAreBlocked = true;
        }

        assertEquals(true, appsAreBlocked);
    }

    // Tests the BlockedService class's getForegroundApp() method
    @Test
    public void blockedAppsService_foregroundAppIsCorrect() {

        // Launches NoCrastinate app for the purposes of testing
        Intent startMain = new Intent(context, MainActivity.class);
        startMain.addCategory(Intent.CATEGORY_LAUNCHER);
        startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        context.startActivity(startMain);

        // Thread must sleep for 1s so that the app fully launches
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Creates a new BlockedAppsService object and calls its method getForegroundApp()
        BlockedAppsService blockedAppsService = new BlockedAppsService(context);
        String foregroundApp = blockedAppsService.getForegroundApp();
        assertEquals("Foreground app is NoCrastinate", "com.example.rosadowning.nocrastinate", foregroundApp);
    }

    // Helper method to commit a Fragment
    public void goToFragment(Fragment fragment) {
        myActivityTestRule.getActivity()
                .getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, fragment).commit();

    }

    // Method to count the number of elements in a recycler view compared with an expected amount
    public class RecyclerViewItemCountAssertion implements ViewAssertion {
        private final int expectedCount;

        public RecyclerViewItemCountAssertion(int expectedCount) {
            this.expectedCount = expectedCount;
        }

        @Override
        public void check(View view, NoMatchingViewException noViewFoundException) {
            if (noViewFoundException != null) {
                throw noViewFoundException;
            }

            RecyclerView recyclerView = (RecyclerView) view;
            RecyclerView.Adapter adapter = recyclerView.getAdapter();
            assertThat(adapter.getItemCount(), is(expectedCount));
        }
    }

    // Inner class with methods to manipulate recycler view items
    public static class MyViewAction {

        // Clicks an element within a Recycler view item
        public static ViewAction clickChildViewWithId(final int id) {
            return new ViewAction() {
                @Override
                public Matcher<View> getConstraints() {
                    return null;
                }

                @Override
                public String getDescription() {
                    return "Click on a child view with specified id.";
                }

                @Override
                public void perform(UiController uiController, View view) {
                    View v = view.findViewById(id);
                    v.performClick();
                }
            };
        }

        // Method to set an element within a recycler view as checked or unchecked depending on the boolean parameter
        public static ViewAction setChecked(final int id, final boolean checked) {
            return new ViewAction() {
                @Override
                public BaseMatcher<View> getConstraints() {
                    return new BaseMatcher<View>() {
                        @Override
                        public boolean matches(Object item) {
                            return isA(Checkable.class).matches(item);
                        }

                        @Override
                        public void describeMismatch(Object item, Description mismatchDescription) {
                        }

                        @Override
                        public void describeTo(Description description) {
                        }
                    };
                }

                @Override
                public String getDescription() {
                    return null;
                }

                @Override
                public void perform(UiController uiController, View view) {
                    Checkable checkableView = (Checkable) view.findViewById(id);
                    checkableView.setChecked(checked);
                }
            };
        }
    }
}