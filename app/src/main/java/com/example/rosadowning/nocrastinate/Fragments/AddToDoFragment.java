package com.example.rosadowning.nocrastinate.Fragments;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
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
import android.widget.Toast;

import com.example.rosadowning.nocrastinate.R;
import com.example.rosadowning.nocrastinate.ToDoItem;
import com.example.rosadowning.nocrastinate.DBHelpers.ToDoReaderContract;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class AddToDoFragment extends Fragment {

    private String name, dueDateString, note;
    private Date dueDateDate;
    private EditText et_name, et_dueDate, et_note;
    private DatePickerDialog.OnDateSetListener mDateSetListener;
    private static final String TAG = "AddToDoFragment";
    private ToDoReaderContract.ToDoListDbHelper dbHelper;
    private ToDoItem addToDo;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_add_todo_screen, null);

        et_name = (EditText) view.findViewById(R.id.addToDoName);
        et_dueDate = (EditText) view.findViewById(R.id.addToDoDueDate);
        et_note = (EditText) view.findViewById(R.id.addToDoNote);
        dueDateDate = null;
        dueDateString = null;

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
                dueDateString = day + "/" + month + "/" + year;

                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
                try {
                    dueDateDate = sdf.parse(dueDateString);

                    if ((System.currentTimeMillis() - 86400000) > dueDateDate.getTime()) {
                        Log.d("ERROR", "date is not valid", null);
                        AlertDialog alertDialog = new AlertDialog.Builder(getContext())
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
        return view;
    }


    public void addToDo() {

        dbHelper = new ToDoReaderContract.ToDoListDbHelper(getContext());
        SQLiteDatabase dbRead = dbHelper.getReadableDatabase();
        ArrayList<String> names = dbHelper.getToDoNames();


        name = et_name.getText().toString();

        if (name.equals(null) || name.equals("")) {
            Log.d("ERROR", "name is not valid", null);
            AlertDialog alertDialog = new AlertDialog.Builder(getContext())
                    .setMessage(R.string.dialog_message_input_error_name)
                    .setTitle(R.string.dialog_title_input_error_name)
                    .setPositiveButton("Ok!", null).create();
            alertDialog.show();
        } else {
            boolean nameConflict = false;
            for (String dbName: names){
                if (name.equals(dbName)){
                    nameConflict = true;
                    AlertDialog alertDialog = new AlertDialog.Builder(getContext())
                            .setMessage(R.string.dialog_message_name_conflict)
                            .setTitle(R.string.dialog_title_name_conflict)
                            .setPositiveButton("Ok!", null).create();
                    alertDialog.show();
                }
            }
            if (!nameConflict) {

                addToDo = new ToDoItem(name);
                note = et_note.getText().toString();

                if (!note.equals(null) || !note.equals("")) {
                    addToDo.setNote(note);
                }
                if (dueDateDate != null) {
                    addToDo.setDueDate(dueDateDate);
                }

                dbHelper = new ToDoReaderContract.ToDoListDbHelper(getContext());
                SQLiteDatabase dbWrite = dbHelper.getWritableDatabase();
                dbHelper.insertNewToDo(addToDo);
                Toast.makeText(getContext(), "To do \"" + name + "\" added", Toast.LENGTH_LONG).show();

                ToDoFragment newFragment = new ToDoFragment();
                FragmentTransaction transaction = getFragmentManager().beginTransaction();
                transaction.replace(R.id.fragment_container, newFragment);
                transaction.addToBackStack(null);
                transaction.commit();
            }
        }

    }


}
