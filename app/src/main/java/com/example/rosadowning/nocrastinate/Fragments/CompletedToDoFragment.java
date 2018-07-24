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

import com.example.rosadowning.nocrastinate.Adapters.CompletedToDoListAdapter;
import com.example.rosadowning.nocrastinate.R;
import com.example.rosadowning.nocrastinate.DataModels.ToDoItem;
import com.example.rosadowning.nocrastinate.DBHelpers.ToDoReaderContract;

import java.util.ArrayList;

public class CompletedToDoFragment extends Fragment {


    private static final String TAG = "Completed To Do Fragment";
    private RecyclerView mRecyclerView;
    private CompletedToDoListAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private ArrayList<ToDoItem> completedToDoList;
    private ToDoReaderContract.ToDoListDbHelper dbHelper;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_completed_todo, null);

        dbHelper = new ToDoReaderContract.ToDoListDbHelper(getContext());
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        completedToDoList = dbHelper.getToDoList(true);
        mAdapter = new CompletedToDoListAdapter(completedToDoList, new CompletedToDoListAdapter.OnItemClickListener() {
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
            }});
        mRecyclerView = (RecyclerView) view.findViewById(R.id.completed_to_do_recycler_view);
        mLayoutManager = mRecyclerView.getLayoutManager();
        mRecyclerView.setAdapter(mAdapter);
        return view;
    }
}
