package com.example.rosadowning.nocrastinate.DBHelpers;
/*
Sets up and provides and interface to a databse which stores the package names of all apps that the user has selected to block.
 */

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.util.Log;

import java.util.ArrayList;

public class BlockedAppsDBContract {

    private BlockedAppsDBContract() {
    }

    // Inner class BlockedAppEntry that defines the main Strings that will be used frequently by the DBhelper below.
    public static class BlockedAppEntry implements BaseColumns {
        public static final String TABLE_NAME = "blockedApps";
        public static final String COLUMN_NAME_PACKAGE_NAME = "package_name";


        private static final String SQL_CREATE_ENTRIES =
                "CREATE TABLE " + TABLE_NAME + "(" +
                        BlockedAppEntry._ID + " INTEGER PRIMARY KEY," +
                        BlockedAppEntry.COLUMN_NAME_PACKAGE_NAME + " STRING NOT NULL);";


        private static final String SQL_DELETE_ENTRIES =
                "DROP TABLE IF EXISTS " + TABLE_NAME;
    }

    // DBHelper class provides an interface between the rest of the application's code and the BlockedAppsDB
    // Provides a variety of useful methods with which to interact with the BlockedAppsDB
    public static class BlockedAppsDBHelper extends SQLiteOpenHelper {
        public static final int DATABASE_VERSION = 1;
        public static final String DATABASE_NAME = "BlockedAppsDB.db";
        public static final String TAG = "BLOCKED APPS DB";

        public BlockedAppsDBHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        // Create the database
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(BlockedAppEntry.SQL_CREATE_ENTRIES);
        }

        // Delete then recreate the database
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL(BlockedAppEntry.SQL_DELETE_ENTRIES);
            onCreate(db);
        }

        // If downgrading, delete the database and then create it via the onUpgrade method
        public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            onUpgrade(db, oldVersion, newVersion);
        }

        // Clear all entries in the database
        public void clearDatabase() {
            SQLiteDatabase db = this.getWritableDatabase();
            db.execSQL("DELETE FROM " + BlockedAppEntry.TABLE_NAME);
            db.close();
        }

        // Insert a new app to the database - this app represents an app recently blocked by the user
        public void insertApp(String packageName) {
            Log.d(TAG, "Inserting app = " + packageName);

            SQLiteDatabase db = this.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put(BlockedAppEntry.COLUMN_NAME_PACKAGE_NAME, packageName);

            db.insertWithOnConflict(BlockedAppEntry.TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_REPLACE);
            db.close();
        }

        // Remove an app from the database - this app is no longer blocked by the use
        public void removeApp(String packageName) {
            Log.d(TAG, "removing app = " + packageName);
            SQLiteDatabase db = this.getWritableDatabase();
            db.delete(BlockedAppEntry.TABLE_NAME, BlockedAppEntry.COLUMN_NAME_PACKAGE_NAME + " = ?", new String[]{String.valueOf(packageName)});
            db.close();
        }

        // Returns an arraylist of all the package names of blocked apps
        public ArrayList<String> getBlockedApps() {

            ArrayList<String> blockedApps = new ArrayList<>();
            SQLiteDatabase db = this.getReadableDatabase();

            Cursor cursor = db.rawQuery("SELECT * FROM " + BlockedAppEntry.TABLE_NAME, null);
            int packageIndex = cursor.getColumnIndex(BlockedAppEntry.COLUMN_NAME_PACKAGE_NAME);

            if (cursor.moveToFirst()) {
                do {
                    blockedApps.add(cursor.getString(packageIndex));
                } while (cursor.moveToNext());
            }
            cursor.close();
            db.close();
            return blockedApps;
        }
    }
}
