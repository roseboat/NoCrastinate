package com.example.rosadowning.nocrastinate.Fragments;

import android.database.sqlite.SQLiteDatabase;
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

import com.example.rosadowning.nocrastinate.R;
import com.example.rosadowning.nocrastinate.DataModels.ToDoItem;
import com.example.rosadowning.nocrastinate.Adapters.ToDoListAdapter;
import com.example.rosadowning.nocrastinate.DBHelpers.ToDoReaderContract;

import java.util.ArrayList;

import jp.wasabeef.recyclerview.adapters.AlphaInAnimationAdapter;
import jp.wasabeef.recyclerview.adapters.SlideInBottomAnimationAdapter;
import jp.wasabeef.recyclerview.adapters.SlideInRightAnimationAdapter;
import jp.wasabeef.recyclerview.animators.SlideInLeftAnimator;
import jp.wasabeef.recyclerview.animators.SlideInUpAnimator;


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
            public void onItemCheck(ToDoItem item, int position) {

                SQLiteDatabase db = dbHelper.getWritableDatabase();
                dbHelper.setCompleted(item, true);
                int id = dbHelper.getID(item);
                NotificationManagerCompat notificationManager = NotificationManagerCompat.from(getContext());
                notificationManager.cancel(id);
                mAdapter.notifyItemRemoved(position);
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
        SlideInUpAnimator animator = new SlideInUpAnimator(new OvershootInterpolator(1f));
        mRecyclerView.setItemAnimator(animator);
        mRecyclerView.getItemAnimator().setRemoveDuration(800);
        mRecyclerView.setAdapter(new AlphaInAnimationAdapter(mAdapter));

        ImageButton addButton = (ImageButton) view.findViewById(R.id.button_add_new_todo);
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getFragmentManager().beginTransaction().replace(R.id.fragment_container, new AddToDoFragment()).addToBackStack(null).commit();
            }
        });

        Button completedToDos = (Button) view.findViewById(R.id.button_view_completed_todos);
        completedToDos.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                getFragmentManager().beginTransaction().replace(R.id.fragment_container, new CompletedToDoFragment()).addToBackStack(null).commit();
            }
        });
        return view;
    }
}
