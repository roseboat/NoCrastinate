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
import android.support.design.widget.TextInputEditText;
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
import android.widget.Toast;

import com.example.rosadowning.nocrastinate.MainActivity;
import com.example.rosadowning.nocrastinate.R;
import com.example.rosadowning.nocrastinate.BroadcastReceivers.ToDoAlarmReceiver;
import com.example.rosadowning.nocrastinate.DataModels.ToDoItem;
import com.example.rosadowning.nocrastinate.DBHelpers.ToDoReaderContract;

import org.joda.time.DateTime;

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
        et_note.setText(note);

        if (dueDate.getTime() != 0) {
            SimpleDateFormat sdfDate = new SimpleDateFormat("dd/MM/yyyy");
            et_dueDate.setText(sdfDate.format(dueDate));
        }
        if (alarmDate.getTime() != 0) {
            SimpleDateFormat sdfAlarm = new SimpleDateFormat("dd/MM/yyyy, HH:mm");
            et_alarm.setText(sdfAlarm.format(alarmDate));
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
                        boolean proceed = true;

                        if (!isCompleted) {
                            if (dueDate != null) {
                                Log.d(TAG, "due date is not null");
                                if (dueDate.getTime() > 0 && dueDate.getTime() < System.currentTimeMillis()) {
                                    Log.d(TAG, "due date old");
                                    proceed = false;
                                    AlertDialog alertDialog = new AlertDialog.Builder(getContext())
                                            .setMessage(R.string.dialog_message_due_date_past)
                                            .setTitle(R.string.dialog_title_due_date_past)
                                            .setPositiveButton("Okay", new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog, int id) {
                                                    dueDate = null;
                                                    et_dueDate.setText(null);

                                                }
                                            }).create();
                                    alertDialog.show();
                                    proceed = false;
                                }
                            }

                            if (alarmDate != null) {
                                Log.d(TAG, "alarm date is not null");

                                if (alarmDate.getTime() > 0 && alarmDate.getTime() < System.currentTimeMillis()) {
                                    Log.d(TAG, "alarm date is old");
                                    proceed = false;
                                    AlertDialog alertDialog = new AlertDialog.Builder(getContext())
                                            .setMessage(R.string.dialog_message_alarm_date_past)
                                            .setTitle(R.string.dialog_title_alarm_date_past)
                                            .setPositiveButton("Okay", new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog, int id) {
                                                    alarmDate = null;
                                                    et_alarm.setText(null);
                                                }
                                            }).create();
                                    alertDialog.show();
                                    proceed = false;
                                }
                            }
                        } else {
                            deleteAlarmSetUp(toDoItem);
                        }

                        if (proceed) {
                            Log.d(TAG, "due date and alarm date are fine");

                            ToDoItem editedToDo = new ToDoItem(name);
                            editedToDo.setDueDate(dueDate);
                            editedToDo.setAlarmDate(alarmDate);
                            editedToDo.setNote(note);
                            editedToDo.setCompleted(isCompleted);
                            editedToDo.setStarred(isStarred);

                            dbHelper = new ToDoReaderContract.ToDoListDbHelper(getContext());
                            SQLiteDatabase sql = dbHelper.getWritableDatabase();

                            if (oldAlarm != null) {
                                deleteAlarmSetUp(toDoItem);
                            }

                            if (!toDoItem.equals(editedToDo)) {
                                int id = dbHelper.getID(toDoItem);
                                dbHelper.deleteToDo(id);
                                dbHelper.insertNewToDo(editedToDo);
                            }


                            if (alarmDate != null && !isCompleted) {
                                if (alarmDate.getTime() > 0) {
                                    Intent intent = new Intent(context, ToDoAlarmReceiver.class);
                                    intent.putExtra("ToDoName", name);
                                    int alarmId = dbHelper.getID(editedToDo);
                                    intent.putExtra("AlarmID", alarmId);
                                    AlarmManager alarmManager = (AlarmManager) getContext().getSystemService(Context.ALARM_SERVICE);
                                    PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0);
                                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, alarmDate.getTime(), pendingIntent);
                                }
                            }
                            getFragmentManager().beginTransaction().replace(R.id.fragment_container, new ToDoFragment()).addToBackStack(null).commit();
                        }
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
                                int deletedToDoID = dbHelper.getID(toDoItem);
                                deleteAlarmSetUp(toDoItem);
                                dbHelper.deleteToDo(deletedToDoID);
                                Toast.makeText(context, "To do \"" + toDoItem.getName() + "\" Deleted", Toast.LENGTH_LONG).show();
                                getFragmentManager().beginTransaction().replace(R.id.fragment_container, new ToDoFragment()).addToBackStack(null).commit();
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

                    if ((new DateTime().withTimeAtStartOfDay().getMillis()) > dueDate.getTime()) {
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

                Log.d(TAG, "day = " + day + " , month = " + month + " , year = " + year);


                alarmCal = Calendar.getInstance();
                alarmCal.clear();
                alarmCal.set(alarmYear, alarmMonth, alarmDay);

                try {

                    if ((new DateTime().withTimeAtStartOfDay().getMillis()) > alarmCal.getTimeInMillis()) {
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

    public void deleteAlarmSetUp(ToDoItem item) {
        dbHelper = new ToDoReaderContract.ToDoListDbHelper(getContext());
        SQLiteDatabase sql = dbHelper.getWritableDatabase();
        int deletedToDoID = dbHelper.getID(item);
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.cancel(deletedToDoID);
        Intent intent = new Intent(context, ToDoAlarmReceiver.class);
        intent.putExtra("ToDoName", item.getName());
        intent.putExtra("AlarmID", deletedToDoID);
        MainActivity.stopAlarm(context, intent);
    }
}
