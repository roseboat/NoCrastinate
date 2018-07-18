package com.example.rosadowning.nocrastinate.DBHelpers;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.util.Log;

import java.util.ArrayList;
import java.util.Date;

import com.example.rosadowning.nocrastinate.StatsComponent;
import com.example.rosadowning.nocrastinate.StatsComposite;
import com.example.rosadowning.nocrastinate.StatsLeaf;

import org.joda.time.DateTime;

import static com.example.rosadowning.nocrastinate.DBHelpers.StatsDBContract.StatsEntry.TABLE_NAME;


public class StatsDBContract {

    private StatsDBContract() {
    }

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

    public static class StatsDBHelper extends SQLiteOpenHelper {
        // If you change the database schema, you must increment the database version.
        public static final int DATABASE_VERSION = 4;
        public static final String DATABASE_NAME = "StatsList.db";

        public StatsDBHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        public void onCreate(SQLiteDatabase db) {
            db.execSQL(StatsEntry.SQL_CREATE_ENTRIES);
        }

        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            // This database is only a cache for online data, so its upgrade policy is
            // to simply to discard the data and start over
            db.execSQL(StatsEntry.SQL_DELETE_ENTRIES);
            onCreate(db);
        }

        public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            onUpgrade(db, oldVersion, newVersion);
        }

        public void insertNewStat(StatsLeaf stats) {
            SQLiteDatabase db = this.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put(StatsEntry.COLUMN_NAME_DATE, stats.getDate().getTime());
            values.put(StatsEntry.COLUMN_NAME_UNLOCKS, stats.getNoOfUnlocks());
            values.put(StatsEntry.COLUMN_NAME_TASKS_COMPLETED, stats.getTasksCompleted());
            values.put(StatsEntry.COLUMN_NAME_OVERALL_TIME, stats.getOverallTime());

            db.insertWithOnConflict(StatsEntry.TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_REPLACE);
            db.close();
        }

        public void deleteStat(StatsLeaf statsComponent) {
            SQLiteDatabase db = this.getWritableDatabase();
            db.delete(TABLE_NAME, StatsEntry.COLUMN_NAME_DATE + " = ?", new String[]{String.valueOf(statsComponent.getDate())});
            db.close();
        }

        public ArrayList<StatsComponent> getAllStats() {

            ArrayList<StatsComponent> allStats = new ArrayList<>();
            SQLiteDatabase db = this.getReadableDatabase();

            Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_NAME, null);
            int dateIndex = cursor.getColumnIndex(StatsEntry.COLUMN_NAME_DATE);
            int timeIndex = cursor.getColumnIndex(StatsEntry.COLUMN_NAME_OVERALL_TIME);
            int unlocksIndex = cursor.getColumnIndex(StatsEntry.COLUMN_NAME_UNLOCKS);
            int tasksIndex = cursor.getColumnIndex(StatsEntry.COLUMN_NAME_TASKS_COMPLETED);

            if (cursor.moveToFirst()) {
                do {
                    StatsLeaf newestStat = new StatsLeaf();
                    newestStat.setNoOfUnlocks(cursor.getInt(unlocksIndex));
                    newestStat.setDate(new Date(cursor.getLong(dateIndex)));
                    newestStat.setTasksCompleted(cursor.getLong(tasksIndex));
                    newestStat.setOverallTime(cursor.getLong(timeIndex));
                    allStats.add(newestStat);

                } while (cursor.moveToNext());
            }
            cursor.close();
            db.close();
            return allStats;
        }

        public StatsComponent getStat(Date date){

            SQLiteDatabase db = this.getReadableDatabase();
            Cursor cursor = db.rawQuery("SELECT *  FROM " + StatsEntry.TABLE_NAME + " WHERE " + StatsEntry.COLUMN_NAME_DATE + " = " + date.getTime(), null);
            int dateIndex = cursor.getColumnIndex(StatsEntry.COLUMN_NAME_DATE);
            int timeIndex = cursor.getColumnIndex(StatsEntry.COLUMN_NAME_OVERALL_TIME);
            int unlocksIndex = cursor.getColumnIndex(StatsEntry.COLUMN_NAME_UNLOCKS);
            int tasksIndex = cursor.getColumnIndex(StatsEntry.COLUMN_NAME_TASKS_COMPLETED);
            StatsLeaf stat = new StatsLeaf();

            if (cursor.moveToFirst()) {
                do {
                    stat.setNoOfUnlocks(cursor.getInt(unlocksIndex));
                    stat.setDate(new Date(cursor.getLong(dateIndex)));
                    stat.setTasksCompleted(cursor.getLong(tasksIndex));
                    stat.setOverallTime(cursor.getLong(timeIndex));
                } while (cursor.moveToNext());
            }
            db.close();
            return stat;
        }

        public StatsComponent getStatsForInterval(String intervalString){

            DateTime now = new DateTime(System.currentTimeMillis());

            DateTime queryTime = null;

            switch (intervalString) {
                case "WEEKLY":
                    queryTime = now.minusWeeks(1);
                    break;
                case "MONTHLY":
                    queryTime = now.minusMonths(1);
                    break;
                case "YEARLY":
                    queryTime = now.minusYears(1);
                    break;
            }

            SQLiteDatabase db = getReadableDatabase();
            Cursor cursor = db.rawQuery("SELECT * FROM " + StatsEntry.TABLE_NAME + " WHERE " + StatsEntry.COLUMN_NAME_DATE + " BETWEEN " + now + " AND " + queryTime, null);

            int dateIndex = cursor.getColumnIndex(StatsEntry.COLUMN_NAME_DATE);
            int timeIndex = cursor.getColumnIndex(StatsEntry.COLUMN_NAME_OVERALL_TIME);
            int unlocksIndex = cursor.getColumnIndex(StatsEntry.COLUMN_NAME_UNLOCKS);
            int tasksIndex = cursor.getColumnIndex(StatsEntry.COLUMN_NAME_TASKS_COMPLETED);
            StatsComposite composite = new StatsComposite();

            if (cursor.moveToFirst()) {
                do {
                    StatsLeaf stat = new StatsLeaf();
                    stat.setNoOfUnlocks(cursor.getInt(unlocksIndex));
                    stat.setDate(new Date(cursor.getLong(dateIndex)));
                    stat.setTasksCompleted(cursor.getLong(tasksIndex));
                    stat.setOverallTime(cursor.getLong(timeIndex));
                    composite.addStat(stat);
                } while (cursor.moveToNext());
            }
            db.close();
            return composite;
        }


    }
}