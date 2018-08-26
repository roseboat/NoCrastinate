package com.example.rosadowning.nocrastinate.Fragments;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TimePicker;
import android.widget.Toast;

import com.example.rosadowning.nocrastinate.R;
import com.example.rosadowning.nocrastinate.BroadcastReceivers.ToDoAlarmReceiver;
import com.example.rosadowning.nocrastinate.DataModels.ToDoItem;
import com.example.rosadowning.nocrastinate.DBHelpers.ToDoDBContract;

import org.joda.time.DateTime;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class AddToDoFragment extends Fragment {

    private String name, dueDateString, note, format, alarmDateString;
    private Date dueDateDate;
    private EditText et_name, et_dueDate, et_note, et_alarm;
    private TimePickerDialog.OnTimeSetListener mTimeSetListener;
    private DatePickerDialog.OnDateSetListener mDueDateSetListener, mAlarmDateSetListener;
    private static final String TAG = "AddToDoFragment";
    private ToDoDBContract.ToDoListDbHelper dbHelper;
    private ToDoItem addToDo;
    private Context context;
    private Calendar todaysDate, alarmDate;
    private int day, month, year, hour, minute, alarmDay, alarmMonth, alarmYear, alarmHour, alarmMinute, alarmID;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_add_todo_screen, null);
        this.context = getContext();
        et_name = (EditText) view.findViewById(R.id.addToDoName);
        et_dueDate = (EditText) view.findViewById(R.id.addToDoDueDate);
        et_note = (EditText) view.findViewById(R.id.addToDoNote);
        et_alarm = (EditText) view.findViewById(R.id.addToDoAlarm);

        dueDateDate = null;
        dueDateString = null;
        alarmDate = null;

        this.todaysDate = Calendar.getInstance();
        this.year = todaysDate.get(Calendar.YEAR);
        this.month = todaysDate.get(Calendar.MONTH);
        this.day = todaysDate.get(Calendar.DAY_OF_MONTH);
        this.hour = todaysDate.get(Calendar.HOUR_OF_DAY);
        this.minute = todaysDate.get(Calendar.MINUTE);

        Button saveToDoButton = (Button) view.findViewById(R.id.button_save_new_to_do);
        saveToDoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addToDo();
            }
        });

        et_dueDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                DatePickerDialog dialog = new DatePickerDialog(context, android.R.style.Theme_DeviceDefault_Dialog_MinWidth,
                        mDueDateSetListener, year, month, day);
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                dialog.show();

            }
        });

        mDueDateSetListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                month++;

                dueDateString = day + "/" + month + "/" + year;

                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
                try {
                    dueDateDate = sdf.parse(dueDateString);

                    if ((new DateTime().withTimeAtStartOfDay().getMillis()) > dueDateDate.getTime()) {
                        AlertDialog alertDialog = new AlertDialog.Builder(context)
                                .setMessage(R.string.dialog_message_input_error_date)
                                .setTitle(R.string.dialog_title_input_error_date)
                                .setPositiveButton("Ok!", null).create();
                        alertDialog.show();
                        dueDateString = null;
                        dueDateDate = null;
                    } else {
                        et_dueDate.setText(dueDateString);
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        };

        // ALARM HERE

        et_alarm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DatePickerDialog dialog = new DatePickerDialog(context, android.R.style.Theme_DeviceDefault_Dialog_MinWidth,
                        mAlarmDateSetListener, year, month, day);
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                dialog.show();

            }
        });

        mAlarmDateSetListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int month, int day) {

                alarmDay = day;
                alarmMonth = month;
                alarmYear = year;

                alarmDate = Calendar.getInstance();
                alarmDate.clear();
                alarmDate.set(alarmYear, alarmMonth, alarmDay);

                try {

                    if ((new DateTime().withTimeAtStartOfDay().getMillis()) > alarmDate.getTimeInMillis()) {
                        AlertDialog alertDialog = new AlertDialog.Builder(context)
                                .setMessage(R.string.dialog_message_input_error_date)
                                .setTitle(R.string.dialog_title_input_error_date)
                                .setPositiveButton("Ok!", null).create();
                        alertDialog.show();
                        alarmDate = null;
                    } else {
                        TimePickerDialog timeDialog = new TimePickerDialog(context, android.R.style.Theme_DeviceDefault_Dialog_MinWidth, new TimePickerDialog.OnTimeSetListener() {
                            @Override
                            public void onTimeSet(TimePicker timePicker, int hourOfDay, int minuteOfDay) {

                                alarmHour = hourOfDay;
                                alarmMinute = minuteOfDay;

                                if (alarmDate != null && (Integer) alarmHour != null && (Integer) alarmMinute != null) {

                                    alarmDate.clear();
                                    alarmDate.set(alarmYear, alarmMonth, alarmDay, alarmHour, alarmMinute);

                                    if (System.currentTimeMillis() > alarmDate.getTimeInMillis()) {
                                        AlertDialog alertDialog = new AlertDialog.Builder(context)
                                                .setMessage(R.string.dialog_message_input_error_time)
                                                .setTitle(R.string.dialog_title_input_error_time)
                                                .setPositiveButton("Ok!", null).create();
                                        alertDialog.show();
                                        alarmDate = null;
                                    } else {
                                        alarmMonth++;
                                        alarmDateString = alarmDay + "/" + alarmMonth + "/" + alarmYear + ", " + alarmHour + ":" + alarmMinute;
                                        et_alarm.setText(alarmDateString);
                                    }
                                }
                            }
                        }, hour, minute, true);
                        timeDialog.show();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };

        return view;
    }

    public void addToDo() {

        dbHelper = new ToDoDBContract.ToDoListDbHelper(context);

        name = et_name.getText().toString();

        if (name.equals(null) || name.equals("")) {
            Log.d("ERROR", "name is not valid", null);
            AlertDialog alertDialog = new AlertDialog.Builder(context)
                    .setMessage(R.string.dialog_message_input_error_name)
                    .setTitle(R.string.dialog_title_input_error_name)
                    .setPositiveButton("Ok!", null).create();
            alertDialog.show();
        } else {

            addToDo = new ToDoItem(name);
            note = et_note.getText().toString();

            if (!note.equals(null) || !note.equals("")) {
                addToDo.setNote(note);
            }
            if (dueDateDate != null) {
                if (dueDateDate.getTime() > 0)
                    addToDo.setDueDate(dueDateDate);
            } else {
                addToDo.setDueDate(null);
            }
            if (alarmDate != null) {
                if (alarmDate.getTimeInMillis() > 0)
                    addToDo.setAlarmDate(new Date(alarmDate.getTimeInMillis()));
            } else {
                alarmDate = null;
                addToDo.setAlarmDate(null);
            }

            dbHelper = new ToDoDBContract.ToDoListDbHelper(context);
            SQLiteDatabase dbWrite = dbHelper.getWritableDatabase();
            dbHelper.insertNewToDo(addToDo);

            if (alarmDate != null && alarmDate.getTimeInMillis() > 0) {
                int alarmID = dbHelper.getID(addToDo);
                Intent intent = new Intent(context, ToDoAlarmReceiver.class);
                intent.putExtra("ToDoName", name);
                intent.putExtra("AlarmID", alarmID);
                Log.d(TAG, "alarm id = " + alarmID);
                AlarmManager alarm = (AlarmManager) getContext().getSystemService(Context.ALARM_SERVICE);
                PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0);
                alarm.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, alarmDate.getTimeInMillis(), pendingIntent);
            }
            Toast.makeText(context, "To do \"" + name + "\" added", Toast.LENGTH_LONG).show();

            ToDoFragment newFragment = new ToDoFragment();
            FragmentTransaction transaction = getFragmentManager().beginTransaction();
            transaction.replace(R.id.fragment_container, newFragment);
            transaction.addToBackStack(null);
            transaction.commit();

        }
    }
}

