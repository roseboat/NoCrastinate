package com.example.rosadowning.nocrastinate.DBHelpers;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.util.Log;

import com.example.rosadowning.nocrastinate.DataModels.StatsIconData;

import java.util.ArrayList;
import java.util.Date;

import static com.example.rosadowning.nocrastinate.DBHelpers.ToDoReaderContract.TableEntry.TABLE_NAME;

public class AlarmDBContract {

    private AlarmDBContract() {

    }

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

    public static class AlarmDBHelper extends SQLiteOpenHelper {
        public static final int DATABASE_VERSION = 1;
        public static final String DATABASE_NAME = "AlarmDB.db";
        public static final String TAG = "ALARM DB";

        public AlarmDBHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        public void onCreate(SQLiteDatabase db) {
            db.execSQL(AlarmEntry.SQL_CREATE_ENTRIES);
        }

        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL(AlarmEntry.SQL_DELETE_ENTRIES);
            onCreate(db);
        }

        public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            onUpgrade(db, oldVersion, newVersion);
        }

        public void insertAlarm(long date) {
            SQLiteDatabase db = this.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put(AlarmEntry.COLUMN_NAME_DATE, date);

            db.insertWithOnConflict(AlarmEntry.TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_REPLACE);
            db.close();
        }

        public boolean isAlarmSet(long date) {

            SQLiteDatabase db = this.getReadableDatabase();
            boolean exists = false;

            Cursor cursor = db.rawQuery("SELECT EXISTS (SELECT 1 FROM " + AlarmEntry.TABLE_NAME + " WHERE " + AlarmEntry.COLUMN_NAME_DATE + " = " + date + ");", null);

            if (cursor.moveToFirst()) {
                exists = cursor.getInt(0) != 0;
            }
            cursor.close();
            db.close();
            return exists;
        }

        public long getNoAlarmEntries(){

            SQLiteDatabase db = this.getReadableDatabase();
            long noOfEntries = 0;
            noOfEntries = DatabaseUtils.queryNumEntries(db, AlarmEntry.TABLE_NAME, AlarmEntry.COLUMN_NAME_DATE + ";");
            db.close();
            return noOfEntries;
        }

        public ArrayList<Long> getAlarmDates() {

            ArrayList<Long> allAlarms = new ArrayList<>();
            SQLiteDatabase db = this.getReadableDatabase();

            Cursor cursor = db.rawQuery("SELECT * FROM " + AlarmEntry.TABLE_NAME, null);
            int dateIndex = cursor.getColumnIndex(AlarmEntry.COLUMN_NAME_DATE);

            if (cursor.moveToFirst()) {
                do {
                    allAlarms.add(cursor.getLong(dateIndex));
                } while (cursor.moveToNext());
            }
            cursor.close();
            db.close();
            return allAlarms;
        }

        }


    }
