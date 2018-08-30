package com.example.rosadowning.nocrastinate.Fragments;
/*
Displays the user's Completed to-dos
 */

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
import com.example.rosadowning.nocrastinate.DBHelpers.ToDoDBContract;
import com.example.rosadowning.nocrastinate.DataModels.ToDoItem;
import com.example.rosadowning.nocrastinate.R;

import java.util.ArrayList;

import jp.wasabeef.recyclerview.adapters.AlphaInAnimationAdapter;

public class CompletedToDoFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflates the view with the fragment_completed_todo layout
        View view = inflater.inflate(R.layout.fragment_completed_todo, null);

        // Gets all todos from the ToDoDB where the boolean isCompleted = true
        ToDoDBContract.ToDoListDbHelper dbHelper = new ToDoDBContract.ToDoListDbHelper(getContext());
        ArrayList<ToDoItem> completedToDoList = dbHelper.getToDoList(true);

        // Sets the CompletedToDoList's adapter to the list of completed to dos from the database
        CompletedToDoListAdapter mAdapter = new CompletedToDoListAdapter(completedToDoList, new CompletedToDoListAdapter.OnItemClickListener() {
            // Sets on click listeners to each to do item in the list
            // If clicked, the ToDoItem is bundled and passed with an intent to the ViewCompletedToDoFragment where it can be edited or deleted
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
        });
        // Gets the Recycler View in the layout and sets its adapter and animator
        RecyclerView mRecyclerView = (RecyclerView) view.findViewById(R.id.completed_to_do_recycler_view);
        RecyclerView.LayoutManager mLayoutManager = mRecyclerView.getLayoutManager();
        mRecyclerView.setAdapter(new AlphaInAnimationAdapter(mAdapter));
        return view;
    }
}
