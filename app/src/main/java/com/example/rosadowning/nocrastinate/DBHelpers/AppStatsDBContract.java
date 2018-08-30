package com.example.rosadowning.nocrastinate.DBHelpers;
/*
Database which has columns representing each of the user's installed apps on their phone as well as a column for the date.
In the rows are stored the date in which the apps are being stored and the total time in foreground of each app.
 */

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.util.Log;

import com.example.rosadowning.nocrastinate.DataModels.CustomAppHolder;
import com.example.rosadowning.nocrastinate.DataModels.TimeHelper;

import org.joda.time.DateTime;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AppStatsDBContract {

    private AppStatsDBContract() {
    }

    /* Inner class that defines the table contents */
    public static class AppEntry implements BaseColumns {
        public static final String TABLE_NAME = "appStats";
        public static final String COLUMN_NAME_DATE = "date";

        private static final String SQL_CREATE_ENTRIES =
                "CREATE TABLE " + TABLE_NAME + "(" +
                        AppEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                        AppEntry.COLUMN_NAME_DATE + " TEXT NOT NULL);";


        private static final String SQL_DELETE_ENTRIES =
                "DROP TABLE IF EXISTS " + TABLE_NAME;
    }

    // AlarmDBHelper class which provides an interface between the rest of the code and the database
    // Provides a variety of useful methods with which to interact AppStatsDatabase
    public static class AppStatsDbHelper extends SQLiteOpenHelper {
        public static final int DATABASE_VERSION = 4;
        public static final String DATABASE_NAME = "AppStats.db";
        public static final String TAG = "App-Stats-DB";

        public AppStatsDbHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        // Create the database
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(AppEntry.SQL_CREATE_ENTRIES);
        }

        // Delete then recreate the database on upgrade
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            // This database is only a cache for online data, so its upgrade policy is
            // to simply to discard the data and start over
            Log.d(TAG, "Dropping entries");
            db.execSQL(AppEntry.SQL_DELETE_ENTRIES);
            onCreate(db);
        }

        // Same as above
        public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            onUpgrade(db, oldVersion, newVersion);
        }

        // Returns a String[] of the package names of apps stored in the database
        // Effectively returns an array of the database's column names
        public String[] getApps() {

            SQLiteDatabase db = this.getReadableDatabase();

            Cursor dbCursor = db.query(AppEntry.TABLE_NAME, null, null, null, null, null, null);
            try {
                String[] columns = dbCursor.getColumnNames();
                String[] appColumns = new String[columns.length];
                int index = 0;
                for (String s : columns) {
                    if (s.length() > 5) { // not ID or DATE
                        s = s.replaceAll("_", "\\."); // Package names are inserted into the db and any '.' in their name is replaced with '_'.
                        appColumns[index] = s;
                        index++;
                    }
                }
                return appColumns;
            } finally {
                dbCursor.close();
                db.close();
            }
        }

        // Add a new column to the table, giving it the name of an application's package name
        public void addAppColumn(String packageName) {
            // Any '.' in the package name must be replaced with '_'
            packageName = packageName.replaceAll("\\.", "_");

            Log.d(TAG, "Adding COLUMN = " + packageName);

            SQLiteDatabase db = this.getWritableDatabase();
            try {
                db.execSQL("ALTER TABLE " + AppEntry.TABLE_NAME + " ADD COLUMN '" + packageName + "' LONG DEFAULT 0");
            } finally {
                db.close();
            }
        }

        // Method called at midnight or first thing when the phone is active after midnight
        // Stores the time an app has been in the phone's foreground in the column corresponding to that app
        // Stored alongside the day's date so that the database can be queried for weekly, monthly and yearly stats
        public void addStats(Date date, List<CustomAppHolder> appList) {

            Log.d(TAG, "Adding stats");
            SQLiteDatabase db = this.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put(AppEntry.COLUMN_NAME_DATE, date.getTime());

            try {
                for (CustomAppHolder c : appList) {
                    c.packageName = c.packageName.replaceAll("\\.", "_");

                    values.put("'" + c.packageName + "'", c.timeInForeground);
                    Log.d(TAG, "ADDING = " + c.packageName + " time = " + TimeHelper.formatDuration(c.timeInForeground));
                }
                db.insertWithOnConflict(AppEntry.TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_REPLACE);
            } finally {
                db.close();
            }
        }

        // Turns a list of apps and their total time in the phone's foreground for a given interval
        public List<CustomAppHolder> getStatsForInterval(String intervalString) {

            ArrayList<CustomAppHolder> appsForInterval = new ArrayList<>();

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

            SQLiteDatabase db = this.getReadableDatabase();
            Cursor cursor = db.rawQuery("SELECT * FROM " + AppEntry.TABLE_NAME + " WHERE " + AppEntry.COLUMN_NAME_DATE + " BETWEEN " + queryTime + " AND " + now, null);
            try {
                String[] columnNames = cursor.getColumnNames();
                for (String column : columnNames) {
                    if (!column.equals(AppEntry._ID) || !column.equals(AppEntry.COLUMN_NAME_DATE)) {
                        CustomAppHolder newApp = new CustomAppHolder();
                        newApp.packageName = column.replaceAll("_", "\\.");
                        if (cursor.moveToFirst()) {
                            cursor.moveToFirst();
                            while (!cursor.isAfterLast()) {
                                newApp.timeInForeground += cursor.getLong(cursor.getColumnIndex(column));
                                cursor.moveToNext();
                            }
                        }
                        appsForInterval.add(newApp);
                    }
                }
                return appsForInterval;
            } finally {
                cursor.close();
                db.close();
            }
        }

        // Delete all entries from the database
        public void clearDatabase() {
            SQLiteDatabase db = this.getWritableDatabase();
            db.execSQL("DELETE FROM " + AppEntry.TABLE_NAME);
            db.close();
        }


        //////// FOR TESTING
        public List<CustomAppHolder> returnAllStats() {

            List<CustomAppHolder> listOfStats = new ArrayList<>();
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yy", Locale.UK);

            SQLiteDatabase db = this.getReadableDatabase();
            Cursor cursor = db.rawQuery("SELECT * FROM " + AppEntry.TABLE_NAME + ";", null);
            try {
                String[] columnNames = cursor.getColumnNames();
                for (String column : columnNames) {
                    if (!column.equals(AppEntry._ID) || !column.equals(AppEntry.COLUMN_NAME_DATE)) {
                        CustomAppHolder newApp = new CustomAppHolder();
                        newApp.packageName = column.replaceAll("_", "\\.");

                        if (cursor.moveToFirst()) {
                            cursor.moveToFirst();
                            while (!cursor.isAfterLast()) {
                                newApp.timeInForeground += cursor.getLong(cursor.getColumnIndex(column));
                                cursor.moveToNext();
                            }
                        }
                        listOfStats.add(newApp);
                    }
                }
                return listOfStats;
            } finally {
                cursor.close();
                db.close();
            }
        }
    }
}
