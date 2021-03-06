package com.example.rosadowning.nocrastinate.DBHelpers;
/*
Class which sets up a database for all of the user's incomplete and complete to do's.
 */

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

import com.example.rosadowning.nocrastinate.DataModels.ToDoItem;

import java.util.ArrayList;
import java.util.Date;

import static com.example.rosadowning.nocrastinate.DBHelpers.ToDoDBContract.TableEntry.TABLE_NAME;

public class ToDoDBContract {

    private ToDoDBContract() {
    }

    /* Inner class that defines the table contents */
    public static class TableEntry implements BaseColumns {
        public static final String TABLE_NAME = "toDoList";
        public static final String COLUMN_NAME_NAME = "name";
        public static final String COLUMN_NAME_DUE_DATE = "due_date";
        public static final String COLUMN_NAME_COMPLETED_DATE = "completed_date";
        public static final String COLUMN_NAME_COMPLETED = "is_completed";
        public static final String COLUMN_NAME_STARRED = "is_starred";
        public static final String COLUMN_NAME_NOTE = "note";
        public static final String COLUMN_NAME_ALARM = "alarm";
        public static final String COLUMN_NAME_ADDED_DATE = "added_date";


        private static final String SQL_CREATE_ENTRIES =
                "CREATE TABLE " + TABLE_NAME + "(" +
                        TableEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                        TableEntry.COLUMN_NAME_NAME + " TEXT NOT NULL," +
                        TableEntry.COLUMN_NAME_NOTE + " TEXT," +
                        TableEntry.COLUMN_NAME_DUE_DATE + " LONG," +
                        TableEntry.COLUMN_NAME_COMPLETED_DATE + " LONG," +
                        TableEntry.COLUMN_NAME_COMPLETED + " BOOLEAN," +
                        TableEntry.COLUMN_NAME_ALARM + " LONG," +
                        TableEntry.COLUMN_NAME_ADDED_DATE + " LONG NOT NULL," +
                        TableEntry.COLUMN_NAME_STARRED + " BOOLEAN);";

        private static final String SQL_DELETE_ENTRIES =
                "DROP TABLE IF EXISTS " + TABLE_NAME;
    }

    // ToDoListDbHelper class which provides an interface between the rest of the code and the database
    // Provides a variety of useful methods with which to interact with the To Do database
    public static class ToDoListDbHelper extends SQLiteOpenHelper {
        public static final int DATABASE_VERSION = 6;
        public static final String DATABASE_NAME = "ToDoList.db";

        public ToDoListDbHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        // Create the database
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(TableEntry.SQL_CREATE_ENTRIES);
        }

        // Delete then recreate the database
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            // This database is only a cache for online data, so its upgrade policy is
            // to simply to discard the data and start over
            db.execSQL(TableEntry.SQL_DELETE_ENTRIES);
            onCreate(db);
        }

        // Same as above
        public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            onUpgrade(db, oldVersion, newVersion);
        }

        // Gets the unique ID of a to do item
        public int getID(ToDoItem toDoItem) {

            SQLiteDatabase db = this.getReadableDatabase();
            Cursor cursor = db.rawQuery("SELECT " + TableEntry._ID + " FROM " + TABLE_NAME + " WHERE " + TableEntry.COLUMN_NAME_ADDED_DATE + " = ?", new String[]{String.valueOf(toDoItem.getAddedDate().getTime())});

            int id = -1;

            try {
                if (cursor != null) {
                    cursor.moveToFirst();
                }
                if (cursor == null) {
                } else if (cursor.moveToFirst()) {
                    do {
                        id = cursor.getInt(cursor.getColumnIndex(TableEntry._ID));
                    } while (cursor.moveToNext());
                    cursor.close();
                }
                return id;
            } finally {
                db.close();
            }
        }

        // Insert a new to do item into the database
        public void insertNewToDo(ToDoItem toDo) {
            SQLiteDatabase db = this.getWritableDatabase();

            try {
                ContentValues values = new ContentValues();
                values.put(TableEntry.COLUMN_NAME_NAME, toDo.getName());
                values.put(TableEntry.COLUMN_NAME_NOTE, toDo.getNote());
                values.put(TableEntry.COLUMN_NAME_COMPLETED, toDo.getCompleted());
                values.put(TableEntry.COLUMN_NAME_STARRED, toDo.getStarred());
                values.put(TableEntry.COLUMN_NAME_ADDED_DATE, toDo.getAddedDate().getTime());


                if (toDo.getDueDate() != null) {
                    values.put(TableEntry.COLUMN_NAME_DUE_DATE, toDo.getDueDate().getTime());
                }
                if (toDo.getAlarmDate() != null) {
                    values.put(TableEntry.COLUMN_NAME_ALARM, toDo.getAlarmDate().getTime());
                }

                if (toDo.getStarred() == true && toDo.getCompletedDate() == null) {
                    long completedTime = System.currentTimeMillis();
                    values.put(TableEntry.COLUMN_NAME_COMPLETED_DATE, completedTime);
                }

                db.insertWithOnConflict(TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_REPLACE);
            } finally {
                db.close();
            }
        }

        // Set a to do's Completed column as true or false
        public void setCompleted(ToDoItem toDoItem, boolean isCompleted) {

            SQLiteDatabase db = this.getReadableDatabase();
            int isCompletedInt = 0;
            long completedTime = 0;
            if (isCompleted) {
                isCompletedInt = 1;
                completedTime = System.currentTimeMillis();
            }
            try {
                db.execSQL("UPDATE " + TABLE_NAME + " SET " + TableEntry.COLUMN_NAME_COMPLETED_DATE + " = " + completedTime + " WHERE " + TableEntry.COLUMN_NAME_ADDED_DATE + " = '" + toDoItem.getAddedDate().getTime() + "';");
                db.execSQL("UPDATE " + TABLE_NAME + " SET " + TableEntry.COLUMN_NAME_COMPLETED + " = " + isCompletedInt + " WHERE " + TableEntry.COLUMN_NAME_ADDED_DATE + " = '" + toDoItem.getAddedDate().getTime() + "';");
            } finally {
                db.close();
            }
        }

        // Set a to do's Starred column as true or false
        public void setStarred(ToDoItem toDoItem, boolean isStarred) {
            int isStarredInt = 0;
            if (isStarred) {
                isStarredInt = 1;
            }
            SQLiteDatabase db = this.getReadableDatabase();
            try {
                db.execSQL("UPDATE " + TABLE_NAME + " SET " + TableEntry.COLUMN_NAME_STARRED + " = " + isStarredInt + " WHERE " + TableEntry.COLUMN_NAME_ADDED_DATE + " = '" + toDoItem.getAddedDate().getTime() + "';");
            } finally {
                db.close();
            }
        }

        // Gets the number of completed to dos for a time interval
        public long getNoOfCompletedToDos(long beginTime, long endTime) {

            long noOfCompletedToDos = 0;
            String newQuery = TableEntry.COLUMN_NAME_COMPLETED_DATE + " BETWEEN " + beginTime + " AND " + endTime + ";";
            SQLiteDatabase db = this.getReadableDatabase();
            try {
                noOfCompletedToDos = DatabaseUtils.queryNumEntries(db, TABLE_NAME, newQuery); // returns the number of entries that match the condition

                return noOfCompletedToDos;
            } finally {
                db.close();
            }
        }

        // Clear all the entries in the to-do list
        public void clearToDoList() {
            SQLiteDatabase db = this.getWritableDatabase();
            db.execSQL("DELETE FROM " + TableEntry.TABLE_NAME);
            db.close();
        }

        // Delete a particular to-do
        public void deleteToDo(int toDoID) {
            SQLiteDatabase db = this.getWritableDatabase();
            try {
                db.delete(TABLE_NAME, TableEntry._ID + " = ?", new String[]{String.valueOf(toDoID)});
            } finally {
                db.close();
            }
        }

        // Method to get a list of to do items. Is given a parameter 'true' or 'false' representing whether the to dos are incomplete or complete
        // ToDoFragment calls this method passing 'false'
        // CompletedToDoFragment calls this method passing 'true'
        public ArrayList<ToDoItem> getToDoList(boolean isCompleted) {

            String query = null;

            if (isCompleted) {
                query = "SELECT * FROM " + TABLE_NAME + " WHERE " + TableEntry.COLUMN_NAME_COMPLETED + " = 1 ORDER BY " + TableEntry.COLUMN_NAME_COMPLETED_DATE + " DESC;";

            } else {
                query = "SELECT * FROM " + TABLE_NAME + " WHERE " + TableEntry.COLUMN_NAME_COMPLETED + " = 0 ORDER BY (CASE WHEN " + TableEntry.COLUMN_NAME_DUE_DATE + " IS NULL OR " + TableEntry.COLUMN_NAME_DUE_DATE + " = 0 THEN 1 ELSE 0 END), " + TableEntry.COLUMN_NAME_DUE_DATE + ";";
            }

            ArrayList<ToDoItem> toDoList = new ArrayList<>();
            SQLiteDatabase db = this.getReadableDatabase();
            Cursor cursor = db.rawQuery(query, null);

            try {

                int nameIndex = cursor.getColumnIndex(TableEntry.COLUMN_NAME_NAME);
                int noteIndex = cursor.getColumnIndex(TableEntry.COLUMN_NAME_NOTE);
                int dateIndex = cursor.getColumnIndex(TableEntry.COLUMN_NAME_DUE_DATE);
                int starIndex = cursor.getColumnIndex(TableEntry.COLUMN_NAME_STARRED);
                int completedIndex = cursor.getColumnIndex(TableEntry.COLUMN_NAME_COMPLETED);
                int completedDateIndex = cursor.getColumnIndex(TableEntry.COLUMN_NAME_COMPLETED_DATE);
                int alarmDateIndex = cursor.getColumnIndex(TableEntry.COLUMN_NAME_ALARM);
                int addedDateIndex = cursor.getColumnIndex(TableEntry.COLUMN_NAME_ADDED_DATE);

                if (cursor.moveToFirst()) {

                    do {
                        ToDoItem newestToDo = new ToDoItem(cursor.getString(nameIndex));
                        newestToDo.setNote(cursor.getString(noteIndex));
                        newestToDo.setDueDate(new Date(cursor.getLong(dateIndex)));
                        newestToDo.setCompletedDate(new Date(cursor.getLong(completedDateIndex)));
                        newestToDo.setCompleted(cursor.getInt(completedIndex) > 0);
                        newestToDo.setStarred(cursor.getInt(starIndex) > 0);
                        newestToDo.setAlarmDate(new Date(cursor.getLong(alarmDateIndex)));
                        newestToDo.setAddedDate(new Date(cursor.getLong(addedDateIndex)));
                        toDoList.add(newestToDo);
                    } while (cursor.moveToNext());
                }


                return toDoList;
            } finally {
                cursor.close();
                db.close();

            }
        }
    }
}

