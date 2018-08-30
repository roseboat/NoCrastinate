package com.example.rosadowning.nocrastinate.DBHelpers;
/*
Database which stores the unlocks, total time on phone and tasks completed each day along with the day's date.
 */

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

import java.util.ArrayList;
import java.util.Date;

import com.example.rosadowning.nocrastinate.DataModels.StatsData;

import org.joda.time.DateTime;

import static com.example.rosadowning.nocrastinate.DBHelpers.StatsDBContract.StatsEntry.TABLE_NAME;

public class StatsDBContract {

    private StatsDBContract() {
    }

    // Inner class StatsEntry which defines all of the main Strings that will be used frequently by the DBHelper
    public static class StatsEntry implements BaseColumns {
        public static final String TABLE_NAME = "statsTable";
        public static final String COLUMN_NAME_DATE = "stats_date";
        public static final String COLUMN_NAME_OVERALL_TIME = "overall_time";
        public static final String COLUMN_NAME_UNLOCKS = "unlocks";
        public static final String COLUMN_NAME_TASKS_COMPLETED = "tasks_completed";


        private static final String SQL_CREATE_ENTRIES =
                "CREATE TABLE " + TABLE_NAME + "(" +
                        StatsEntry._ID + " INTEGER PRIMARY KEY," +
                        StatsEntry.COLUMN_NAME_DATE + " LONG NOT NULL," +
                        StatsEntry.COLUMN_NAME_OVERALL_TIME + " LONG," +
                        StatsEntry.COLUMN_NAME_UNLOCKS + " INT," +
                        StatsEntry.COLUMN_NAME_TASKS_COMPLETED + " LONG);";

        private static final String SQL_DELETE_ENTRIES =
                "DROP TABLE IF EXISTS " + TABLE_NAME;
    }

    // DBHelper class which provides an interface between the rest of the code and the database
    // Provides a variety of useful methods with which to interact with the Stats database
    public static class StatsDBHelper extends SQLiteOpenHelper {
        // If you change the database schema, you must increment the database version.
        public static final int DATABASE_VERSION = 4;
        public static final String DATABASE_NAME = "StatsList.db";
        public static final String TAG = "STATS DB";

        public StatsDBHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        // Create the database
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(StatsEntry.SQL_CREATE_ENTRIES);
        }

        // Delete then recreate the database
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            // This database is only a cache for online data, so its upgrade policy is
            // to simply to discard the data and start over
            db.execSQL(StatsEntry.SQL_DELETE_ENTRIES);
            onCreate(db);
        }

        // Same as above
        public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            onUpgrade(db, oldVersion, newVersion);
        }

        // Insert a new row of stats into the database
        public void insertNewStat(StatsData stats) {
            SQLiteDatabase db = this.getWritableDatabase();
            try {
                ContentValues values = new ContentValues();
                values.put(StatsEntry.COLUMN_NAME_DATE, stats.getDate().getTime());
                values.put(StatsEntry.COLUMN_NAME_UNLOCKS, stats.getNoOfUnlocks());
                values.put(StatsEntry.COLUMN_NAME_TASKS_COMPLETED, stats.getTasksCompleted());
                values.put(StatsEntry.COLUMN_NAME_OVERALL_TIME, stats.getOverallTime());

                db.insertWithOnConflict(StatsEntry.TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_REPLACE);
            } finally {
                db.close();
            }
        }

        // Delete a row of stats from the database
        public void deleteStat(StatsData statsComponent) {
            SQLiteDatabase db = this.getWritableDatabase();
            try {
                db.delete(TABLE_NAME, StatsEntry.COLUMN_NAME_DATE + " = ?", new String[]{String.valueOf(statsComponent.getDate())});
            } finally {
                db.close();
            }
        }

        // Get an array of StatsData objects for a given interval, Weekly, Monthly or Yearly
        public ArrayList<StatsData> getStatsForInterval(String intervalString) {

            ArrayList<StatsData> statsForInterval = new ArrayList<>();

            DateTime today = new DateTime().withTimeAtStartOfDay();
            long now = today.toDate().getTime();

            long queryTime = 0;

            switch (intervalString) {
                case "Weekly":
                    queryTime = today.minusDays(7).toDate().getTime();
                    break;
                case "Monthly":
                    queryTime = today.minusMonths(1).toDate().getTime();
                    break;
                case "Yearly":
                    queryTime = today.minusYears(1).toDate().getTime();
                    break;
            }

            SQLiteDatabase db = getReadableDatabase();
            Cursor cursor = db.rawQuery("SELECT * FROM " + StatsEntry.TABLE_NAME + " WHERE " + StatsEntry.COLUMN_NAME_DATE + " BETWEEN " + queryTime + " AND " + now, null);
            try {
                int dateIndex = cursor.getColumnIndex(StatsEntry.COLUMN_NAME_DATE);
                int timeIndex = cursor.getColumnIndex(StatsEntry.COLUMN_NAME_OVERALL_TIME);
                int unlocksIndex = cursor.getColumnIndex(StatsEntry.COLUMN_NAME_UNLOCKS);
                int tasksIndex = cursor.getColumnIndex(StatsEntry.COLUMN_NAME_TASKS_COMPLETED);

                if (cursor.moveToFirst()) {
                    do {
                        StatsData stat = new StatsData();
                        stat.setNoOfUnlocks(cursor.getInt(unlocksIndex));
                        stat.setDate(new Date(cursor.getLong(dateIndex)));
                        stat.setTasksCompleted(cursor.getLong(tasksIndex));
                        stat.setOverallTime(cursor.getLong(timeIndex));
                        statsForInterval.add(stat); // Add the StatsData object to the arraylist statsForInterval
                    } while (cursor.moveToNext());
                }
                return statsForInterval;
            } finally {
                cursor.close();
                db.close();
            }
        }
    }
}