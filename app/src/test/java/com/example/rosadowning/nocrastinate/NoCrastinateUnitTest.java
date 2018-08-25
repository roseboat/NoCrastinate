package com.example.rosadowning.nocrastinate;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.example.rosadowning.nocrastinate.DBHelpers.ToDoDBContract;
import com.example.rosadowning.nocrastinate.DataModels.ToDoItem;

import org.joda.time.DateTime;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
public class NoCrastinateUnitTest{


    private MainActivity mainActivity;
    private Context context;
    private ToDoDBContract.ToDoListDbHelper dbHelper;
    private SQLiteDatabase db;


    @Before
    public void setUp() throws Exception {

        this.mainActivity = new MainActivity();
        this.context = mainActivity.getContext();
        this.dbHelper = new ToDoDBContract.ToDoListDbHelper(context);
        this.db = dbHelper.getWritableDatabase();
    }

    @Test
    public void hasUsagePermissionGrantedOnStart() throws Exception {

        mainActivity.onCreate(null);
        assertFalse(mainActivity.hasUsagePermission(context));
    }



//    @Test
//    public void addingToDo() throws Exception{
//
//        ToDoItem newToDo = new ToDoItem("Added To Do");
//        newToDo.setNote("This is a test to-do");
//        newToDo.setAddedDate(new Date(System.currentTimeMillis()));
//        newToDo.setDueDate(new DateTime().plusDays(2).toDate());
//        newToDo.setAlarmDate(new DateTime().plusHours(36).toDate());
//        newToDo.setCompleted(false);
//        newToDo.setStarred(true);
//
//        dbHelper.insertNewToDo(newToDo);
//
//        List<ToDoItem> toDoItems = dbHelper.getToDoList(false);
//        assertTrue(toDoItems.contains(newToDo));
//    }
//
//    @After
//    public void tearDown(){
//        if (db != null){
//        db.close();}
//    }

    //    @Test
//    public void clickingStats_shouldStartStatsFragment() {
//        mainActivity.findViewById(R.id.navigation_stats).performClick();
//
//        Intent expectedIntent = new Intent(mainActivity, StatisticsFragment.class);
//        Intent actual = shadowOf(RuntimeEnvironment.application).getNextStartedActivity();
//        assertEquals(expectedIntent.getComponent(), actual.getComponent());
//    }
//
//    @Test
//    public void clickingToDo_shouldStartToDoFragment() {
//        MainActivity activity = Robolectric.setupActivity(MainActivity.class);
//        activity.findViewById(R.id.navigation_todo).performClick();
//
//        Intent expectedIntent = new Intent(activity, StatisticsFragment.class);
//        Intent actual = shadowOf(RuntimeEnvironment.application).getNextStartedActivity();
//        assertEquals(expectedIntent.getComponent(), actual.getComponent());
//    }
//
//    @Test
//    public void clickingSettings_shouldStartSettingsFragment() {
//        MainActivity activity = Robolectric.setupActivity(MainActivity.class);
//        activity.findViewById(R.id.navigation_settings).performClick();
//
//        Intent expectedIntent = new Intent(activity, StatisticsFragment.class);
//        Intent actual = shadowOf(RuntimeEnvironment.application).getNextStartedActivity();
//        assertEquals(expectedIntent.getComponent(), actual.getComponent());
//    }

    //    @Test
//    public void usageEvents_isCorrect() {
//
//        StatisticsFragment statisticsFragment = new StatisticsFragment();
//        List<CustomAppHolder> statsList = statisticsFragment.getStats("Daily");
//
//        CustomAppHolder fakeApp = new CustomAppHolder("fakeApp");
//        statsList.add(fakeApp);
//        List<CustomAppHolder> updatedStatsList = statisticsFragment.updateAppsList(statsList);
//
//        for (CustomAppHolder app : updatedStatsList) {
//            assertFalse(app.packageName.equals("fakeApp"));
//        }
//    }

}