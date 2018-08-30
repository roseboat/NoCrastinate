package com.example.rosadowning.nocrastinate.Fragments;
/*
When the user clicks on a to-do in the ToDoFragment or the CompletedToDoFragment,
the specific to-do is bundled with an intent that launches this fragment.
The ViewToDoFragment displays the to-do and its details with fields that can be
edited. The to-do can be edited and saved or deleted completely. If edited, checks
must be carried out to ensure that the to-do is valid, just like in the AddToDoFragment.
 */

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
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

import com.example.rosadowning.nocrastinate.BroadcastReceivers.ToDoAlarmReceiver;
import com.example.rosadowning.nocrastinate.DBHelpers.ToDoDBContract;
import com.example.rosadowning.nocrastinate.DataModels.ToDoItem;
import com.example.rosadowning.nocrastinate.MainActivity;
import com.example.rosadowning.nocrastinate.R;

import org.joda.time.DateTime;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class ViewToDoFragment extends Fragment {

    private static final String TAG = "VIEW TO DO FRAGMENT";
    private ToDoItem toDoItem;
    private String name, note, alarmDateString;
    private Date dueDate, oldAlarm, alarmDate;
    private Boolean isCompleted, isStarred;
    private EditText et_name, et_dueDate, et_note, et_alarm;
    private CheckBox completed_checkBox;
    private DatePickerDialog.OnDateSetListener mDueDateSetListener, mAlarmDateSetListener;
    private ToDoDBContract.ToDoListDbHelper dbHelper;
    private Calendar alarmCal;
    private Context context;
    private int year, month, day, hour, minute, alarmDay, alarmMonth, alarmYear, alarmHour, alarmMinute, alarmID, oldAlarmID;


    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_view_todo, null);
        context = getContext();

        // Gets the ToDoItem from the intent's bundle
        toDoItem = (ToDoItem) getArguments().getSerializable("todo");

        // Gets the elements of the Fragment's view
        et_name = (EditText) view.findViewById(R.id.toDoName);
        et_dueDate = (EditText) view.findViewById(R.id.toDoDueDate);
        et_note = (EditText) view.findViewById(R.id.toDoNote);
        et_alarm = (EditText) view.findViewById(R.id.toDoAlarmDate);
        completed_checkBox = (CheckBox) view.findViewById(R.id.to_do_check_box);
        Button saveButton = (Button) view.findViewById(R.id.button_save_to_do);
        Button deleteButton = (Button) view.findViewById(R.id.button_delete_to_do);

        // Gets the details contained in the ToDoitem
        name = toDoItem.getName();
        note = toDoItem.getNote();
        dueDate = toDoItem.getDueDate();
        isCompleted = toDoItem.getCompleted();
        alarmDate = toDoItem.getAlarmDate();
        oldAlarm = null;

        Calendar todaysDate = Calendar.getInstance();
        this.year = todaysDate.get(Calendar.YEAR);
        this.month = todaysDate.get(Calendar.MONTH);
        this.day = todaysDate.get(Calendar.DAY_OF_MONTH);
        this.hour = todaysDate.get(Calendar.HOUR_OF_DAY);
        this.minute = todaysDate.get(Calendar.MINUTE);

        // Sets parts of the view with details from the ToDoItem depending on some conditions
        et_name.setText(name);
        et_note.setText(note);

        if (dueDate.getTime() != 0) {
            SimpleDateFormat sdfDate = new SimpleDateFormat("dd/MM/yyyy", Locale.UK);
            et_dueDate.setText(sdfDate.format(dueDate));
        }
        if (alarmDate.getTime() != 0) {
            SimpleDateFormat sdfAlarm = new SimpleDateFormat("dd/MM/yyyy, HH:mm", Locale.UK);
            et_alarm.setText(sdfAlarm.format(alarmDate));
        }
        if (isCompleted) {
            completed_checkBox.setChecked(true);
        } else
            completed_checkBox.setChecked(false);

        // Sets the different fields to 'enabled' so the user can click on and edit them
        et_name.setEnabled(true);
        et_dueDate.setEnabled(true);
        et_note.setEnabled(true);
        et_alarm.setEnabled(true);
        completed_checkBox.setEnabled(true);
        datePickerSetUp(); // Sets up the DueDate's on click listener which produces a date picker dialog
        alarmPickerSetUp(); // Sets up the AlarmDate's on click listener which produces an date and time picker dialog

        // Sets an on click listener to the 'Save To-Do' button
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Gets info from each field
                name = et_name.getText().toString();
                note = et_note.getText().toString();
                isCompleted = completed_checkBox.isChecked();
                isStarred = toDoItem.getStarred();
                boolean proceed = true;

                // If the 'Completed?' checkbox is not checked, do the following:
                if (!isCompleted) {
                    if (dueDate != null) {
                        Log.d(TAG, "due date is not null");
                        // If the due date is greater than 0 (ie. it has been set) and it is less than the current time, it is not valid and an error dialog appears
                        if (dueDate.getTime() > 0 && dueDate.getTime() < System.currentTimeMillis()) {
                            Log.d(TAG, "due date old");
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
                        // If the alarm date is greater than 0 (ie. it has been set) and it is less than the current time, it is invalid and an error dialog appears
                        if (alarmDate.getTime() > 0 && alarmDate.getTime() < System.currentTimeMillis()) {
                            Log.d(TAG, "alarm date is old");
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
                    // If the to-do's checkbox 'Completed?' is true, delete the to-do's alarm
                    deleteAlarmSetUp(toDoItem);
                }

                // If the boolean proceed is true, meaning if there were no errors in the alarm or date selection:
                if (proceed) {
                    Log.d(TAG, "due date and alarm date are fine");

                    // Create a new ToDoItem editedToDo and set the variables to those in the view's textfields
                    ToDoItem editedToDo = new ToDoItem(name);
                    editedToDo.setDueDate(dueDate);
                    editedToDo.setAlarmDate(alarmDate);
                    editedToDo.setNote(note);
                    editedToDo.setCompleted(isCompleted);
                    editedToDo.setStarred(isStarred);

                    dbHelper = new ToDoDBContract.ToDoListDbHelper(context);

                    // If the old alarm is not null, delete it.
                    if (oldAlarm != null) {
                        deleteAlarmSetUp(toDoItem);
                    }
                    // If the old toDoItem and the new, editToDo are different, delete the original from the database
                    if (!toDoItem.equals(editedToDo)) {
                        int id = dbHelper.getID(toDoItem);
                        dbHelper.deleteToDo(id);
                        dbHelper.insertNewToDo(editedToDo); // insert the new, edited to do
                    }

                    // If the new alarmDate is not null and the ToDoItem is not completed, set the alarm
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
                    // Once successfully saved, go back to the ToDoFragment
                    getFragmentManager().beginTransaction().replace(R.id.fragment_container, new ToDoFragment()).addToBackStack(null).commit();
                }
            }
        });

        // Sets an onlclick listener to the delete button
        // Asks user to confirm they want to delete the to-do
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog alertDialog = new AlertDialog.Builder(getContext())
                        .setMessage(R.string.dialog_message_confirm_delete)
                        .setTitle(R.string.dialog_title_confirm_delete)
                        .setPositiveButton("I'm sure!", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                // Deletes the to-do and its alarm
                                // Sets a toast to confirm the deletion and goes back to the ToDoFragment
                                dbHelper = new ToDoDBContract.ToDoListDbHelper(getContext());
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

    // Sets onclick listener to the Due Date edit text
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
        // Gets the date from the user's input
        mDueDateSetListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                month++;
                String dueDateString = day + "/" + month + "/" + year;

                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
                try {
                    dueDate = sdf.parse(dueDateString);
                    // Checks that the dueDate is valid, if not an alert dialog is fired up
                    if ((new DateTime().withTimeAtStartOfDay().getMillis()) > dueDate.getTime()) {
                        AlertDialog alertDialog = new AlertDialog.Builder(context)
                                .setMessage(R.string.dialog_message_input_error_date)
                                .setTitle(R.string.dialog_title_input_error_date)
                                .setPositiveButton("Ok!", null).create();
                        alertDialog.show();
                        dueDate = null;
                    } else {
                        // If valid it sets the edit text to the valid due date
                        et_dueDate.setText(sdf.format(dueDate));
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        };
    }

    // Sets an onclick listener onto the alarm edit text
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
// Gets the date from the user's input
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
// Checks that the alarm DATE is valid, if not it fires up an alert dialog
                    if ((new DateTime().withTimeAtStartOfDay().getMillis()) > alarmCal.getTimeInMillis()) {
                        AlertDialog alertDialog = new AlertDialog.Builder(context)
                                .setMessage(R.string.dialog_message_input_error_date)
                                .setTitle(R.string.dialog_title_input_error_date)
                                .setPositiveButton("Ok!", null).create();
                        alertDialog.show();
                        alarmCal = null;
                    } else {

                        // If the alarm DATE is valid, a new TimePickerDialog is started
                        TimePickerDialog timeDialog = new TimePickerDialog(context, android.R.style.Theme_DeviceDefault_Dialog_MinWidth, new TimePickerDialog.OnTimeSetListener() {
                            @Override
                            public void onTimeSet(TimePicker timePicker, int hourOfDay, int minuteOfDay) {

                                alarmHour = hourOfDay;
                                alarmMinute = minuteOfDay;
// If the user has entered valid, not null inputs, set the alarmCal variable to the DATE and TIME chosen
                                if (alarmCal != null && (Integer) alarmHour != null && (Integer) alarmMinute != null) {

                                    alarmCal.set(alarmYear, alarmMonth, alarmDay, alarmHour, alarmMinute);
// Checks if alarmCal is less than the current time, if so an alert is fired up for the user to pick a date in the future
                                    if (System.currentTimeMillis() > alarmCal.getTimeInMillis()) {
                                        AlertDialog alertDialog = new AlertDialog.Builder(context)
                                                .setMessage(R.string.dialog_message_input_error_time)
                                                .setTitle(R.string.dialog_title_input_error_time)
                                                .setPositiveButton("Ok!", null).create();
                                        alertDialog.show();
                                        alarmCal = null;
                                    } else {
                                        // If alarmCal is valid, set oldAlarm to alarmDate
                                        // Set alarmDate to the alarmCal variable
                                        // Set the alarm edit text to display the string of the alarm
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

    // Method used to delete an alarm associated with a completed or deleted to do
    public void deleteAlarmSetUp(ToDoItem item) {
        dbHelper = new ToDoDBContract.ToDoListDbHelper(getContext());
        int deletedToDoID = dbHelper.getID(item);
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.cancel(deletedToDoID);
        Intent intent = new Intent(context, ToDoAlarmReceiver.class);
        intent.putExtra("ToDoName", item.getName());
        intent.putExtra("AlarmID", deletedToDoID);
        MainActivity.stopAlarm(context, intent);
    }
}
