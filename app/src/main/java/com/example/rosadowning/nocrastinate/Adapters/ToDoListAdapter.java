package com.example.rosadowning.nocrastinate.Adapters;
/*
Adapter class for the recycler view in ToDoFragment. Presents a recycler view of all of the user's uncompleted to-dos.
*/
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;


import com.example.rosadowning.nocrastinate.R;
import com.example.rosadowning.nocrastinate.DataModels.ToDoItem;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class ToDoListAdapter extends RecyclerView.Adapter<ToDoListAdapter.ViewHolder> {

    private ArrayList<ToDoItem> toDoList;
    private final OnItemClickListener listeners;

    // ToDoListAdapter is constructed with a list of ToDoItems and an OnItemClickListener
    public ToDoListAdapter(ArrayList<ToDoItem> toDoList, OnItemClickListener listeners) {
        this.toDoList = toDoList;
        this.listeners = listeners;
    }

    // Sets the layout resource 'todo_list_item' as the viewholder for a certain to do
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.todo_list_item, viewGroup, false);
        return new ViewHolder(view);
    }
    // Binds a viewholder to the information contained in a ToDoItem object in the toDoList list.
    @Override
    public void onBindViewHolder(@NonNull final ViewHolder viewHolder, int position) {
        // Binds a listener to the item
        viewHolder.bind(toDoList.get(position), listeners);

        // Gets information from a ToDoItem
        String name = toDoList.get(position).getName();
        Date date = toDoList.get(position).getDueDate();
        Date alarm = toDoList.get(position).getAlarmDate();
        Boolean starred = toDoList.get(position).getStarred();
        Boolean completed = toDoList.get(position).getCompleted();

        viewHolder.isCompleted.setOnCheckedChangeListener(null);
        viewHolder.isStarred.setOnCheckedChangeListener(null);

        // Sets elements in the ViewHolder to information from the ToDoItem depending on conditions
        viewHolder.name.setText(name);

        if (date.getTime() != 0) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.UK);
            viewHolder.dueDate.setText("Due date: " + sdf.format(date));
        } else {
            viewHolder.dueDate.setText("");
        }
        if (alarm.getTime() != 0) {
            viewHolder.hasAlarm.setVisibility(View.VISIBLE);
        }
        if (completed)
            viewHolder.isCompleted.setChecked(true);
        else viewHolder.isCompleted.setChecked(false);

        // Sets an onlick listener to the isCompleted Checkbox. If checked the specific to-do item is passed to the ToDoFragment for the database to be updated and it is removed from the Recycler View
        viewHolder.isCompleted.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    int position = viewHolder.getAdapterPosition();
                    toDoList.get(position).setCompleted(true);
                    listeners.onItemCheck(toDoList.get(position), position);
                    toDoList.remove(position);
                }
            }
        });

        // If an item is starred, its star is checked and the color of the to do item is changed
        if (starred) {
            viewHolder.isStarred.setChecked(true);
            viewHolder.wholeToDoItem.setBackgroundResource(R.color.colorStarredToDo);
        } else {
            viewHolder.isStarred.setChecked(false);
            viewHolder.wholeToDoItem.setBackgroundResource(R.color.colorToDoBackground);
        }
        // Sets an on click listener to the to-do's star, if checked it is passed to the ToDoFragment for the database to be updated
        viewHolder.isStarred.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    toDoList.get(viewHolder.getAdapterPosition()).setStarred(true);
                    listeners.onStarCheck(toDoList.get(viewHolder.getAdapterPosition()));
                    viewHolder.wholeToDoItem.setBackgroundResource(R.color.colorStarredToDo);

                } else {
                    toDoList.get(viewHolder.getAdapterPosition()).setStarred(false);
                    listeners.onStarUncheck(toDoList.get(viewHolder.getAdapterPosition()));
                    viewHolder.wholeToDoItem.setBackgroundResource(R.color.colorToDoBackground);

                }
            }
        });
    }

    // Gets number of elements in the recycler view/completed to do list
    @Override
    public int getItemCount() {
        return toDoList.size();
    }

    // Sets onclick listener interface which communicates with the ToDoFragment
    public interface OnItemClickListener {
        void onItemClick(ToDoItem item);
        void onItemCheck(ToDoItem item, int position);
        void onStarCheck(ToDoItem item);
        void onStarUncheck(ToDoItem item);
    }

    // ViewHolder inner class
    public static class ViewHolder extends RecyclerView.ViewHolder {

        CheckBox isCompleted, isStarred;
        ImageView hasAlarm;
        TextView name, dueDate;
        LinearLayout toDoLayout;
        RelativeLayout wholeToDoItem;

        // Constructor for a ViewHolder object. Gets the elements from a todo_list_item.
        public ViewHolder(View itemView) {
            super(itemView);

            name = (TextView) itemView.findViewById(R.id.toDoName);
            dueDate = (TextView) itemView.findViewById(R.id.toDoDueDate);
            isCompleted = (CheckBox) itemView.findViewById(R.id.toDoCheckBox);
            isStarred = (CheckBox) itemView.findViewById(R.id.toDoStar);
            hasAlarm = (ImageView) itemView.findViewById(R.id.toDoAlarm);
            toDoLayout = (LinearLayout) itemView.findViewById(R.id.toDoLinearLayout);
            wholeToDoItem = (RelativeLayout) itemView.findViewById(R.id.to_do_item);

        }
        //  Binds an onclick listener to each item. When clicked, the item is passed to the ToDoFragment
        public void bind(final ToDoItem item, final OnItemClickListener listener) {
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onItemClick(item);
                }
            });
        }
    }
}



