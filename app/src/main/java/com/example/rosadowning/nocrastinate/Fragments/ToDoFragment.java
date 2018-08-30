package com.example.rosadowning.nocrastinate.Fragments;
/*
Class which displays the user's to do list.
 */

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.OvershootInterpolator;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import com.example.rosadowning.nocrastinate.Adapters.ToDoListAdapter;
import com.example.rosadowning.nocrastinate.BroadcastReceivers.ToDoAlarmReceiver;
import com.example.rosadowning.nocrastinate.DBHelpers.ToDoDBContract;
import com.example.rosadowning.nocrastinate.DataModels.ToDoItem;
import com.example.rosadowning.nocrastinate.MainActivity;
import com.example.rosadowning.nocrastinate.R;

import java.util.ArrayList;

import jp.wasabeef.recyclerview.adapters.AlphaInAnimationAdapter;
import jp.wasabeef.recyclerview.animators.SlideInUpAnimator;

public class ToDoFragment extends Fragment {

    private RecyclerView mRecyclerView;
    private ToDoListAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private ArrayList<ToDoItem> toDoList;
    private ToDoDBContract.ToDoListDbHelper dbHelper;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_todo, null);

        dbHelper = new ToDoDBContract.ToDoListDbHelper(getContext());
        toDoList = dbHelper.getToDoList(false); // gets a list of the users incomplete to dos
        mAdapter = new ToDoListAdapter(toDoList, new ToDoListAdapter.OnItemClickListener() {
            // Adds an onclick listener to each to do item
            // When an item is clicked it is put into a Bundle and sent with an intent to the ViewToDoFragment
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

            // When an item is checked (completed) the database must be updated and any alarm set for that to do must be stopped
            // Finally, the adapter for the recycler view must be notified that the to do has been removed so that it animates it accordingly
            @Override
            public void onItemCheck(ToDoItem item, int position) {
                int id = dbHelper.getID(item);
                dbHelper.setCompleted(item, true);
                Toast.makeText(getContext(), "'" + item.getName() + "' is completed!", Toast.LENGTH_LONG).show();
                NotificationManagerCompat notificationManager = NotificationManagerCompat.from(getContext());
                notificationManager.cancel(id);

                Intent intent = new Intent(getContext(), ToDoAlarmReceiver.class);
                intent.putExtra("ToDoName", item.getName());
                intent.putExtra("AlarmID", id);
                MainActivity.stopAlarm(getContext(), intent);

                mAdapter.notifyItemRemoved(position);
            }

            // If the star is checked or unchecked, the database must be updated
            @Override
            public void onStarCheck(ToDoItem item) {
                dbHelper.setStarred(item, true);
            }

            @Override
            public void onStarUncheck(ToDoItem item) {
                dbHelper.setStarred(item, false);
            }
        });
        // Sets the recycler view's adapter and animator
        mRecyclerView = (RecyclerView) view.findViewById(R.id.to_do_recycler_view);
        mLayoutManager = mRecyclerView.getLayoutManager();
        SlideInUpAnimator animator = new SlideInUpAnimator(new OvershootInterpolator(1f));
        mRecyclerView.setItemAnimator(animator);
        mRecyclerView.getItemAnimator().setRemoveDuration(800);
        mRecyclerView.setAdapter(new AlphaInAnimationAdapter(mAdapter));

        // If the user clicks the '+' image button in the top right corner of the screen, they are taken to the AddToDoFragment
        ImageButton addButton = (ImageButton) view.findViewById(R.id.button_add_new_todo);
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getFragmentManager().beginTransaction().replace(R.id.fragment_container, new AddToDoFragment()).addToBackStack(null).commit();
            }
        });
        // If they click the 'View Completed To-Dos' button they are taken to CompletedToDosFragment
        Button completedToDos = (Button) view.findViewById(R.id.button_view_completed_todos);
        completedToDos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getFragmentManager().beginTransaction().replace(R.id.fragment_container, new CompletedToDoFragment()).addToBackStack(null).commit();
            }
        });
        return view;
    }
}
