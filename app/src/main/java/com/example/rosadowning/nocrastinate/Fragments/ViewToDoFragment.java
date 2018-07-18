package com.example.rosadowning.nocrastinate.Fragments;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
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
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;

import com.example.rosadowning.nocrastinate.R;
import com.example.rosadowning.nocrastinate.ToDoItem;
import com.example.rosadowning.nocrastinate.DBHelpers.ToDoReaderContract;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class ViewToDoFragment extends Fragment {

    private ToDoItem toDoItem;
    private String name, note;
    private Date dueDate;
    private Boolean isCompleted;
    private EditText et_name, et_dueDate, et_note;
    private CheckBox completed_checkBox;
    private Button editButton, deleteButton;
    private DatePickerDialog.OnDateSetListener mDateSetListener;
    private ToDoReaderContract.ToDoListDbHelper dbHelper;


    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_view_todo, null);

        toDoItem = (ToDoItem) getArguments().getSerializable("todo");

        et_name = (EditText) view.findViewById(R.id.toDoName);
        et_dueDate = (EditText) view.findViewById(R.id.toDoDueDate);
        et_note = (EditText) view.findViewById(R.id.toDoNote);
        completed_checkBox = (CheckBox) view.findViewById(R.id.to_do_check_box);
        editButton = (Button) view.findViewById(R.id.button_edit_to_do);
        deleteButton = (Button) view.findViewById(R.id.button_delete_to_do);

        name = toDoItem.getName();
        note = toDoItem.getNote();
        dueDate = toDoItem.getDueDate();
        isCompleted = toDoItem.getCompleted();
        Log.d("CHECKING TO DOS", "ischecked = " + isCompleted, null);


        et_name.setText(name);

        if (dueDate.getTime() != 0) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
            et_dueDate.setText(sdf.format(dueDate));
        } else {
            et_dueDate.setText("");
        }
        if (!note.equals(null)) {
            et_note.setText(note);
        } else {
            et_note.setText("");
        }
        if (isCompleted){
            completed_checkBox.setChecked(true);
        } else
            completed_checkBox.setChecked(false);

        et_name.setEnabled(false);
        et_dueDate.setEnabled(false);
        et_note.setEnabled(false);
        completed_checkBox.setEnabled(false);

        editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                et_name.setEnabled(true);
                et_dueDate.setEnabled(true);
                et_note.setEnabled(true);
                completed_checkBox.setEnabled(true);
                datePickerSetUp();
                editButton.setText("Save To Do");
                editButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        name = et_name.getText().toString();
                        note = et_note.getText().toString();
                        isCompleted = completed_checkBox.isChecked();
                        Boolean star = toDoItem.getStarred();

                        ToDoItem editedToDo = new ToDoItem(name);
                        editedToDo.setDueDate(dueDate);
                        editedToDo.setNote(note);
                        editedToDo.setCompleted(isCompleted);
                        editedToDo.setStarred(star);

                        if (!toDoItem.equals(editedToDo)) {
                            dbHelper = new ToDoReaderContract.ToDoListDbHelper(getContext());
                            SQLiteDatabase sql = dbHelper.getWritableDatabase();
                            dbHelper.deleteToDo(toDoItem);
                            dbHelper.insertNewToDo(editedToDo);
                        }
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
                Calendar todaysDate = Calendar.getInstance();
                int year = todaysDate.get(Calendar.YEAR);
                int month = todaysDate.get(Calendar.MONTH);
                int day = todaysDate.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog dialog = new DatePickerDialog(getContext(), android.R.style.Theme_DeviceDefault_Dialog_MinWidth,
                        mDateSetListener, year, month, day);
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                dialog.show();
            }
        });

        mDateSetListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                month++;
                String dueDateString = day + "/" + month + "/" + year;

                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
                try {
                    dueDate = sdf.parse(dueDateString);

                    if (System.currentTimeMillis() - 846000 > dueDate.getTime()) {
                        Log.d("ERROR", "date is not valid", null);
                        AlertDialog alertDialog = new AlertDialog.Builder(getContext())
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

}
