package com.example.rosadowning.nocrastinate;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import static com.example.rosadowning.nocrastinate.ToDoReaderContract.FeedEntry.TABLE_NAME;

public class ToDoReaderContract {

    // To prevent someone from accidentally instantiating the contract class,
    // make the constructor private.

    private ToDoReaderContract() {
    }

    /* Inner class that defines the table contents */
    public static class FeedEntry implements BaseColumns {
        public static final String TABLE_NAME = "toDoList";
        public static final String COLUMN_NAME_NAME = "name";
        public static final String COLUMN_NAME_DUE_DATE = "due_date";
        public static final String COLUMN_NAME_COMPLETED_DATE = "completed_date";
        public static final String COLUMN_NAME_COMPLETED = "is_completed";
        public static final String COLUMN_NAME_STARRED = "is_starred";
        public static final String COLUMN_NAME_NOTE = "note";

        private static final String SQL_CREATE_ENTRIES =
                "CREATE TABLE " + TABLE_NAME + "(" +
                        FeedEntry._ID + " INTEGER PRIMARY KEY," +
                        FeedEntry.COLUMN_NAME_NAME + " TEXT NOT NULL," +
                        FeedEntry.COLUMN_NAME_NOTE + " TEXT," +
                        FeedEntry.COLUMN_NAME_DUE_DATE + " LONG," +
                        FeedEntry.COLUMN_NAME_COMPLETED_DATE + " LONG," +
                        FeedEntry.COLUMN_NAME_COMPLETED + " BOOLEAN," +
                        FeedEntry.COLUMN_NAME_STARRED + " BOOLEAN);";

        private static final String SQL_DELETE_ENTRIES =
                "DROP TABLE IF EXISTS " + TABLE_NAME;
    }

    public static class ToDoListDbHelper extends SQLiteOpenHelper {
        // If you change the database schema, you must increment the database version.
        public static final int DATABASE_VERSION = 1;
        public static final String DATABASE_NAME = "ToDoList.db";

        public ToDoListDbHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        public void onCreate(SQLiteDatabase db) {
            db.execSQL(FeedEntry.SQL_CREATE_ENTRIES);
        }

        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            // This database is only a cache for online data, so its upgrade policy is
            // to simply to discard the data and start over
            db.execSQL(FeedEntry.SQL_DELETE_ENTRIES);
            onCreate(db);
        }

        public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            onUpgrade(db, oldVersion, newVersion);
        }

        public void insertNewToDo(ToDoItem toDo) {
            SQLiteDatabase db = this.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put(FeedEntry.COLUMN_NAME_NAME, toDo.getName());
            values.put(FeedEntry.COLUMN_NAME_NOTE, toDo.getNote());
            values.put(FeedEntry.COLUMN_NAME_COMPLETED, toDo.getCompleted());
            values.put(FeedEntry.COLUMN_NAME_STARRED, toDo.getStarred());


            if (toDo.getDueDate() != null) {
                values.put(FeedEntry.COLUMN_NAME_DUE_DATE, toDo.getDueDate().getTime());
            }

            if (toDo.getStarred() == true && toDo.getCompletedDate() == null){
                long completedTime = System.currentTimeMillis();
                values.put(FeedEntry.COLUMN_NAME_COMPLETED_DATE, completedTime);
            }

            db.insertWithOnConflict(TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_REPLACE);
            db.close();
        }

        public ArrayList<String> getToDoNames() {

            ArrayList<String> names = new ArrayList<>();
            SQLiteDatabase db = this.getReadableDatabase();
            Cursor cursor = db.rawQuery("SELECT " + FeedEntry.COLUMN_NAME_NAME + " FROM " + TABLE_NAME + " WHERE " + FeedEntry.COLUMN_NAME_COMPLETED + " = " + 0, null);
            int nameIndex = cursor.getColumnIndex(FeedEntry.COLUMN_NAME_NAME);

            if (cursor.moveToFirst()) {
                do {
                    String name = cursor.getString(nameIndex);
                    names.add(name);
                } while (cursor.moveToNext());
            }
            db.close();
            return names;

        }

        public void setCompleted(ToDoItem toDoItem, boolean isCompleted) {

            SQLiteDatabase db = this.getReadableDatabase();
            int isCompletedInt = 0;
            long completedTime = 0;
            if (isCompleted = true) {
                isCompletedInt = 1;
                completedTime = System.currentTimeMillis();
            }
            db.execSQL("UPDATE " + TABLE_NAME + " SET " + FeedEntry.COLUMN_NAME_COMPLETED_DATE + " = " + completedTime + " WHERE " + FeedEntry.COLUMN_NAME_NAME + " = '" + toDoItem.getName() + "';");
            db.execSQL("UPDATE " + TABLE_NAME + " SET " + FeedEntry.COLUMN_NAME_COMPLETED + " = " + isCompletedInt + " WHERE " + FeedEntry.COLUMN_NAME_NAME + " = '" + toDoItem.getName() + "';");
            db.close();
        }


        public void setStarred(ToDoItem toDoItem, boolean isStarred) {
            int isStarredInt = 0;
            if (isStarred = true) {
                isStarredInt = 1;
            }
            SQLiteDatabase db = this.getReadableDatabase();
            db.execSQL("UPDATE " + TABLE_NAME + " SET " + FeedEntry.COLUMN_NAME_STARRED + " = " + isStarredInt + " WHERE " + FeedEntry.COLUMN_NAME_NAME + " = '" + toDoItem.getName() + "';");
            db.close();
        }

        public long getNoOfCompletedToDos(String timeInterval) {

            long noOfCompletedToDos = 0;
            Calendar calendar = Calendar.getInstance();
            long completedDate = calendar.getTimeInMillis();

            switch (timeInterval) {
                case "DAILY":
                    calendar.add(Calendar.HOUR_OF_DAY, -24);
                    break;
                case "WEEKLY":
                    calendar.add(Calendar.WEEK_OF_MONTH, -1);
                    break;
                case "MONTHLY":
                    calendar.add(Calendar.MONTH, -1);
                    break;
                case "YEARLY":
                    calendar.add(Calendar.YEAR, -1);
                    break;
            }

            long queryDate = calendar.getTimeInMillis();
            String newQuery = FeedEntry.COLUMN_NAME_COMPLETED_DATE + " BETWEEN " + queryDate + " AND " + completedDate + ";";
            SQLiteDatabase db = this.getReadableDatabase();
            noOfCompletedToDos = DatabaseUtils.queryNumEntries(db, TABLE_NAME, newQuery);
            db.close();
            return noOfCompletedToDos;
        }

        public void deleteToDo(ToDoItem toDoItem) {
            SQLiteDatabase db = this.getWritableDatabase();
            db.delete(TABLE_NAME, FeedEntry.COLUMN_NAME_NAME + " = ?", new String[]{String.valueOf(toDoItem.getName())});
            db.close();
        }

        public ArrayList<ToDoItem> getAllToDos() {

            ArrayList<ToDoItem> toDoList = new ArrayList<>();
            SQLiteDatabase db = this.getReadableDatabase();

            Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_NAME, null);
            int nameIndex = cursor.getColumnIndex(FeedEntry.COLUMN_NAME_NAME);
            int noteIndex = cursor.getColumnIndex(FeedEntry.COLUMN_NAME_NOTE);
            int dateIndex = cursor.getColumnIndex(FeedEntry.COLUMN_NAME_DUE_DATE);
            int starIndex = cursor.getColumnIndex(FeedEntry.COLUMN_NAME_STARRED);
            int completedIndex = cursor.getColumnIndex(FeedEntry.COLUMN_NAME_COMPLETED);
            int completedDateIndex = cursor.getColumnIndex(FeedEntry.COLUMN_NAME_COMPLETED_DATE);


            if (cursor.moveToFirst()) {
                do {
                    ToDoItem newestToDo = new ToDoItem(cursor.getString(nameIndex));
                    newestToDo.setNote(cursor.getString(noteIndex));
                    newestToDo.setDueDate(new Date(cursor.getLong(dateIndex)));
                    newestToDo.setDueDate(new Date(cursor.getLong(completedDateIndex)));
                    newestToDo.setCompleted(cursor.getInt(completedIndex) > 0);
                    newestToDo.setStarred(cursor.getInt(starIndex) > 0);
                    toDoList.add(newestToDo);
                    Log.d("DATABASE: ", "adding " + (cursor.getInt(completedIndex) > 0) + " ", null);

                } while (cursor.moveToNext());
            }
            cursor.close();
            db.close();
            return toDoList;
        }

        public ArrayList<ToDoItem> getToDoList(boolean isCompleted) {

            String query = null;

            if (isCompleted) {
                query = "SELECT * FROM " + TABLE_NAME + " WHERE " + FeedEntry.COLUMN_NAME_COMPLETED + " = 1 ORDER BY (CASE WHEN " + FeedEntry.COLUMN_NAME_DUE_DATE + " IS NULL THEN 1 ELSE 0 END), " + FeedEntry.COLUMN_NAME_DUE_DATE + " DESC;";
            } else {
                query = "SELECT * FROM " + TABLE_NAME + " WHERE " + FeedEntry.COLUMN_NAME_COMPLETED + " = 0 ORDER BY (CASE WHEN " + FeedEntry.COLUMN_NAME_DUE_DATE + " IS NULL THEN 1 ELSE 0 END), " + FeedEntry.COLUMN_NAME_DUE_DATE + ";";
            }

            ArrayList<ToDoItem> toDoList = new ArrayList<>();
            SQLiteDatabase db = this.getReadableDatabase();

            Cursor cursor = db.rawQuery(query, null);

            int nameIndex = cursor.getColumnIndex(FeedEntry.COLUMN_NAME_NAME);
            int noteIndex = cursor.getColumnIndex(FeedEntry.COLUMN_NAME_NOTE);
            int dateIndex = cursor.getColumnIndex(FeedEntry.COLUMN_NAME_DUE_DATE);
            int starIndex = cursor.getColumnIndex(FeedEntry.COLUMN_NAME_STARRED);
            int completedIndex = cursor.getColumnIndex(FeedEntry.COLUMN_NAME_COMPLETED);
            int completedDateIndex = cursor.getColumnIndex(FeedEntry.COLUMN_NAME_COMPLETED_DATE);


            if (cursor.moveToFirst()) {

                do {
                    ToDoItem newestToDo = new ToDoItem(cursor.getString(nameIndex));
                    newestToDo.setNote(cursor.getString(noteIndex));
                    newestToDo.setDueDate(new Date(cursor.getLong(dateIndex)));
                    newestToDo.setDueDate(new Date(cursor.getLong(completedDateIndex)));
                    newestToDo.setCompleted(cursor.getInt(completedIndex) > 0);
                    newestToDo.setStarred(cursor.getInt(starIndex) > 0);
                    toDoList.add(newestToDo);
                } while (cursor.moveToNext());
            }
            cursor.close();
            db.close();
            return toDoList;
        }
    }
}

