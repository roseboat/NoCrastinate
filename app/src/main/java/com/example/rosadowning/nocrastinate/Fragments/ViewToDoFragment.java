package com.example.rosadowning.nocrastinate.Fragments;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TimePicker;

import com.example.rosadowning.nocrastinate.R;
import com.example.rosadowning.nocrastinate.BroadcastReceivers.ToDoAlarmReceiver;
import com.example.rosadowning.nocrastinate.DataModels.ToDoItem;
import com.example.rosadowning.nocrastinate.DBHelpers.ToDoReaderContract;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class ViewToDoFragment extends Fragment {

    private static final String TAG = "VIEW TO DO FRAGMENT";
    private ToDoItem toDoItem;
    private String name, note, alarmDateString;
    private Date dueDate, oldAlarm, alarmDate;
    private Boolean isCompleted, isStarred;
    private EditText et_name, et_dueDate, et_note, et_alarm;
    private CheckBox completed_checkBox;
    private Button editButton, deleteButton;
    private DatePickerDialog.OnDateSetListener mDueDateSetListener, mAlarmDateSetListener;
    private ToDoReaderContract.ToDoListDbHelper dbHelper;
    private Calendar todaysDate, alarmCal;
    private Context context;
    private int year, month, day, hour, minute, alarmDay, alarmMonth, alarmYear, alarmHour, alarmMinute, alarmID, oldAlarmID;
    private final long ONE_DAY_LONG = 86400000;


    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_view_todo, null);
        context = getContext();

        toDoItem = (ToDoItem) getArguments().getSerializable("todo");

        et_name = (EditText) view.findViewById(R.id.toDoName);
        et_dueDate = (EditText) view.findViewById(R.id.toDoDueDate);
        et_note = (EditText) view.findViewById(R.id.toDoNote);
        et_alarm = (EditText) view.findViewById(R.id.toDoAlarmDate);
        completed_checkBox = (CheckBox) view.findViewById(R.id.to_do_check_box);
        editButton = (Button) view.findViewById(R.id.button_edit_to_do);
        deleteButton = (Button) view.findViewById(R.id.button_delete_to_do);

        name = toDoItem.getName();
        note = toDoItem.getNote();
        dueDate = toDoItem.getDueDate();
        isCompleted = toDoItem.getCompleted();
        alarmDate = toDoItem.getAlarmDate();
        oldAlarm = null;

        this.todaysDate = Calendar.getInstance();
        this.year = todaysDate.get(Calendar.YEAR);
        this.month = todaysDate.get(Calendar.MONTH);
        this.day = todaysDate.get(Calendar.DAY_OF_MONTH);
        this.hour = todaysDate.get(Calendar.HOUR_OF_DAY);
        this.minute = todaysDate.get(Calendar.MINUTE);

        et_name.setText(name);

        if (dueDate.getTime() != 0) {
            SimpleDateFormat sdfDate = new SimpleDateFormat("dd/MM/yyyy");
            et_dueDate.setText(sdfDate.format(dueDate));
        }
        if (alarmDate.getTime() != 0) {
            SimpleDateFormat sdfAlarm = new SimpleDateFormat("dd/MM/yyyy, HH:mm");
            et_alarm.setText(sdfAlarm.format(alarmDate));
        }
        if (!note.equals(null)) {
            et_note.setText(note);
        }
        if (isCompleted) {
            completed_checkBox.setChecked(true);
        } else
            completed_checkBox.setChecked(false);

        et_name.setEnabled(false);
        et_dueDate.setEnabled(false);
        et_note.setEnabled(false);
        et_alarm.setEnabled(false);
        completed_checkBox.setEnabled(false);

        editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                et_name.setEnabled(true);
                et_dueDate.setEnabled(true);
                et_note.setEnabled(true);
                et_alarm.setEnabled(true);
                completed_checkBox.setEnabled(true);
                datePickerSetUp();
                alarmPickerSetUp();
                editButton.setText("Save To Do");
                editButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        name = et_name.getText().toString();
                        note = et_note.getText().toString();
                        isCompleted = completed_checkBox.isChecked();
                        isStarred = toDoItem.getStarred();


                        if (dueDate.getTime() > 0 && dueDate.getTime() < System.currentTimeMillis()){
                            AlertDialog alertDialog = new AlertDialog.Builder(getContext())
                                    .setMessage(R.string.dialog_message_due_date_past)
                                    .setTitle(R.string.dialog_title_due_date_past)
                                    .setPositiveButton("Okay", new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            dueDate = null;
                                            et_dueDate.setText("");
                                        }
                                    }).create();
                            alertDialog.show();
                        }

                        if (alarmDate.getTime() > 0 && alarmDate.getTime() < System.currentTimeMillis()){
                            AlertDialog alertDialog = new AlertDialog.Builder(getContext())
                                    .setMessage(R.string.dialog_message_alarm_date_past)
                                    .setTitle(R.string.dialog_title_alarm_date_past)
                                    .setPositiveButton("Okay", new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            alarmDate = null;
                                            et_alarm.setText("");
                                        }
                                    }).create();
                            alertDialog.show();
                        }

                        ToDoItem editedToDo = new ToDoItem(name);
                        editedToDo.setDueDate(dueDate);
                        editedToDo.setAlarmDate(alarmDate);
                        editedToDo.setNote(note);
                        editedToDo.setCompleted(isCompleted);
                        editedToDo.setStarred(isStarred);

                        dbHelper = new ToDoReaderContract.ToDoListDbHelper(getContext());
                        SQLiteDatabase sql = dbHelper.getWritableDatabase();

                        if (oldAlarm != null) {
                            int oldAlarmId = dbHelper.getID(toDoItem);
                            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
                            notificationManager.cancel(oldAlarmId);
                            Log.d(TAG, "old alarm = " + oldAlarmId + " deleted");
                        }

                        if (!toDoItem.equals(editedToDo)) {
                            dbHelper.deleteToDo(toDoItem);
                            dbHelper.insertNewToDo(editedToDo);
                        }

                        Intent intent = new Intent(context, ToDoAlarmReceiver.class);
                        intent.putExtra("ToDoName", name);
                        int alarmId = dbHelper.getID(editedToDo);
                        intent.putExtra("AlarmID", alarmId);
                        AlarmManager alarm = (AlarmManager) getContext().getSystemService(Context.ALARM_SERVICE);
                        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0);
                        alarm.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, alarmDate.getTime(), pendingIntent);

                        ToDoFragment newFragment = new ToDoFragment();
                        FragmentTransaction transaction = getFragmentManager().beginTransaction();
                        transaction.replace(R.id.fragment_container, newFragment);
                        transaction.addToBackStack(null);
                        transaction.commit();
                    }
                });
            }
        });

        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog alertDialog = new AlertDialog.Builder(getContext())
                        .setMessage(R.string.dialog_message_confirm_delete)
                        .setTitle(R.string.dialog_title_confirm_delete)
                        .setPositiveButton("I'm sure!", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dbHelper = new ToDoReaderContract.ToDoListDbHelper(getContext());
                                SQLiteDatabase sql = dbHelper.getWritableDatabase();
                                int deletedToDoAlarmID = dbHelper.getID(toDoItem);
                                NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
                                notificationManager.cancel(deletedToDoAlarmID);
                                dbHelper.deleteToDo(toDoItem);
                                ToDoFragment newFragment = new ToDoFragment();
                                FragmentTransaction transaction = getFragmentManager().beginTransaction();
                                transaction.replace(R.id.fragment_container, newFragment);
                                transaction.addToBackStack(null);
                                transaction.commit();
                            }
                        }).setNegativeButton("Cancel", null).create();
                alertDialog.show();
            }
        });

        return view;
    }

    public void datePickerSetUp() {

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
                String dueDateString = day + "/" + month + "/" + year;

                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
                try {
                    dueDate = sdf.parse(dueDateString);

                    if ((todaysDate.getTimeInMillis() - ONE_DAY_LONG) > dueDate.getTime()) {
                        AlertDialog alertDialog = new AlertDialog.Builder(context)
                                .setMessage(R.string.dialog_message_input_error_date)
                                .setTitle(R.string.dialog_title_input_error_date)
                                .setPositiveButton("Ok!", null).create();
                        alertDialog.show();
                        dueDate = null;
                    } else {
                        et_dueDate.setText(sdf.format(dueDate));
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        };
    }

    public void alarmPickerSetUp() {

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

                alarmCal = Calendar.getInstance();
                alarmCal.clear();
                alarmCal.set(alarmYear, alarmMonth, alarmDay);

                try {

                    if ((todaysDate.getTimeInMillis() - ONE_DAY_LONG) > alarmCal.getTimeInMillis()) {
                        AlertDialog alertDialog = new AlertDialog.Builder(context)
                                .setMessage(R.string.dialog_message_input_error_date)
                                .setTitle(R.string.dialog_title_input_error_date)
                                .setPositiveButton("Ok!", null).create();
                        alertDialog.show();
                        alarmCal = null;
                    } else {
                        TimePickerDialog timeDialog = new TimePickerDialog(context, android.R.style.Theme_DeviceDefault_Dialog_MinWidth, new TimePickerDialog.OnTimeSetListener() {
                            @Override
                            public void onTimeSet(TimePicker timePicker, int hourOfDay, int minuteOfDay) {

                                alarmHour = hourOfDay;
                                alarmMinute = minuteOfDay;

                                if (alarmCal != null && (Integer) alarmHour != null && (Integer) alarmMinute != null) {

                                    alarmCal.set(alarmYear, alarmMonth, alarmDay, alarmHour, alarmMinute);

                                    if (System.currentTimeMillis() > alarmCal.getTimeInMillis()) {
                                        AlertDialog alertDialog = new AlertDialog.Builder(context)
                                                .setMessage(R.string.dialog_message_input_error_time)
                                                .setTitle(R.string.dialog_title_input_error_time)
                                                .setPositiveButton("Ok!", null).create();
                                        alertDialog.show();
                                        alarmCal = null;
                                    } else {
                                        oldAlarm = alarmDate;
                                        alarmDate = new Date(alarmCal.getTimeInMillis());
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
    }
}
