package com.example.rosadowning.nocrastinate.DBHelpers;
/*
Database which stores the time at midnight of a day in which all statistics data has been successfully stored and reset
 */

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

public class AlarmDBContract {

    private AlarmDBContract() {

    }

    // Inner class AlarmEntry which defines all of the main Strings that will be used frequently by the DBHelper
    public static class AlarmEntry implements BaseColumns {
        public static final String TABLE_NAME = "alarmTable";
        public static final String COLUMN_NAME_DATE = "alarm_date";


        private static final String SQL_CREATE_ENTRIES =
                "CREATE TABLE " + TABLE_NAME + "(" +
                        AlarmEntry._ID + " INTEGER PRIMARY KEY," +
                        AlarmEntry.COLUMN_NAME_DATE + " LONG NOT NULL);";


        private static final String SQL_DELETE_ENTRIES =
                "DROP TABLE IF EXISTS " + TABLE_NAME;
    }

    // AlarmDBHelper class which provides an interface between the rest of the code and the database
    // Provides a variety of useful methods with which to interact Alarm database
    public static class AlarmDBHelper extends SQLiteOpenHelper {
        public static final int DATABASE_VERSION = 1;
        public static final String DATABASE_NAME = "AlarmDB.db";
        public static final String TAG = "ALARM DB";

        public AlarmDBHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        // Create the database
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(AlarmEntry.SQL_CREATE_ENTRIES);
        }

        // Delete and then recreate the database
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL(AlarmEntry.SQL_DELETE_ENTRIES);
            onCreate(db);
        }

        // If downgrading, delete the database and then create it via the onUpgrade method
        public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            onUpgrade(db, oldVersion, newVersion);
        }

        // Insert an alarm into the database, given a long representing the time
        public void insertAlarm(long date) {
            SQLiteDatabase db = this.getWritableDatabase();
            try {
                ContentValues values = new ContentValues();
                values.put(AlarmEntry.COLUMN_NAME_DATE, date);
                db.insertWithOnConflict(AlarmEntry.TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_REPLACE);
            } finally {
                db.close();
            }
        }

        // Determine whether an alarm has been set given a long of the time
        public boolean isAlarmSet(long date) {

            SQLiteDatabase db = this.getReadableDatabase();
            boolean exists = false;

            Cursor cursor = db.rawQuery("SELECT EXISTS (SELECT 1 FROM " + AlarmEntry.TABLE_NAME + " WHERE " + AlarmEntry.COLUMN_NAME_DATE + " = " + date + ");", null);
            try {
                if (cursor.moveToFirst()) {
                    exists = cursor.getInt(0) != 0;
                }
                return exists;
            } finally {
                cursor.close();
                db.close();
            }
        }

        // Method to remove an alarm from the database
        public void removeAlarm(long alarmTime) {
            SQLiteDatabase db = this.getWritableDatabase();
            try {
                db.delete(AlarmEntry.TABLE_NAME, AlarmEntry.COLUMN_NAME_DATE + " = ?", new String[]{String.valueOf(alarmTime)});
            } finally {
                db.close();
            }
        }

        // Delete all entries from the database
        public void clearDatabase() {
            SQLiteDatabase db = this.getWritableDatabase();
            db.execSQL("DELETE FROM " + ToDoDBContract.TableEntry.TABLE_NAME);
            db.close();
        }
    }
}