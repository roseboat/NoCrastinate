package com.example.rosadowning.nocrastinate.Fragments;

import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;

import com.example.rosadowning.nocrastinate.R;
import com.example.rosadowning.nocrastinate.ToDoItem;
import com.example.rosadowning.nocrastinate.Adapters.ToDoListAdapter;
import com.example.rosadowning.nocrastinate.DBHelpers.ToDoReaderContract;

import java.util.ArrayList;


public class ToDoFragment extends Fragment {

    private static final String TAG = "To Do Fragment";
    private RecyclerView mRecyclerView;
    private ToDoListAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private ArrayList<ToDoItem> toDoList;
    private ToDoReaderContract.ToDoListDbHelper dbHelper;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_todo, null);

        dbHelper = new ToDoReaderContract.ToDoListDbHelper(getContext());
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        toDoList = dbHelper.getToDoList(false);
        mAdapter = new ToDoListAdapter(toDoList, new ToDoListAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(ToDoItem item) {
                Bundle bundle = new Bundle();
                bundle.putSerializable("todo", item);
                ViewToDoFragment newFragment = new ViewToDoFragment();
                newFragment.setArguments(bundle);
                FragmentTransaction transaction = getFragmentManager().beginTransaction();
                transaction.replace(R.id.fragment_container, newFragment);
                transaction.addToBackStack(null);
                transaction.commit();
            }

            @Override
            public void onItemCheck(ToDoItem item) {
                SQLiteDatabase db = dbHelper.getWritableDatabase();
                dbHelper.setCompleted(item, true);
                refreshScreen();
            }

            @Override
            public void onItemUncheck(ToDoItem item) {
                SQLiteDatabase db = dbHelper.getWritableDatabase();
                dbHelper.setCompleted(item, false);
                refreshScreen();
            }

            @Override
            public void onStarCheck(ToDoItem item) {
                SQLiteDatabase db = dbHelper.getWritableDatabase();
                dbHelper.setStarred(item, true);
            }

            @Override
            public void onStarUncheck(ToDoItem item) {
                SQLiteDatabase db = dbHelper.getWritableDatabase();
                dbHelper.setStarred(item, false);
            }
        });

        mRecyclerView = (RecyclerView) view.findViewById(R.id.to_do_recycler_view);
        mLayoutManager = mRecyclerView.getLayoutManager();
        mRecyclerView.setAdapter(mAdapter);

        ImageButton addButton = (ImageButton) view.findViewById(R.id.button_add_new_todo);
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                getFragmentManager().beginTransaction().replace(R.id.fragment_container, new AddToDoFragment()).commit();

            }
        });

        Button completedToDos = (Button) view.findViewById(R.id.button_view_completed_todos);
        completedToDos.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                getFragmentManager().beginTransaction().replace(R.id.fragment_container, new CompletedToDoFragment()).commit();
            }
        });
        return view;
    }

    public void refreshScreen(){

        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.detach(this).attach(this).commit();
    }
}
