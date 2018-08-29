package com.example.rosadowning.nocrastinate.Adapters;
/*
Adapter class for the recycler view in ViewToDoFragment. Presents a recycler view of all of the user's completed to-dos.
*/

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.rosadowning.nocrastinate.R;
import com.example.rosadowning.nocrastinate.DataModels.ToDoItem;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class CompletedToDoListAdapter extends RecyclerView.Adapter<CompletedToDoListAdapter.ViewHolder> {

    private ArrayList<ToDoItem> toDoList;
    private final OnItemClickListener listeners;

    // CompletedToDoListAdapter is constructed with a list of ToDoItems and an OnItemClickListener
    public CompletedToDoListAdapter(ArrayList<ToDoItem> toDoList, OnItemClickListener listeners) {
        this.toDoList = toDoList;
        this.listeners = listeners;
    }

    // Sets the layout resource 'todo_list_item' as the viewholder for a certain to do
    @NonNull
    @Override
    public CompletedToDoListAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.todo_list_item, viewGroup, false);
        return new ViewHolder(view);
    }

    // Binds a viewholder to the information contained in a ToDoItem object in the toDoList list.
    @Override
    public void onBindViewHolder(@NonNull CompletedToDoListAdapter.ViewHolder viewHolder, int position) {
        // Binds a listener to the item
        viewHolder.bind(toDoList.get(position), listeners);

        // Gets information from a ToDoItem
        String name = toDoList.get(position).getName();
        Date date = toDoList.get(position).getDueDate();
        Boolean star = toDoList.get(position).getStarred();
        Boolean completed = toDoList.get(position).getCompleted();

        // Sets elements in the ViewHolder to information from the ToDoItem depending on conditions
        viewHolder.name.setText(name);

        if (date.getTime() != 0) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.UK);
            viewHolder.dueDate.setText("Due date: " + sdf.format(date));
        } else {
            viewHolder.dueDate.setText("");
        }
        if (star)
            viewHolder.isStarred.setChecked(true);
        else viewHolder.isStarred.setChecked(false);

        if (completed)
            viewHolder.isCompleted.setChecked(true);
        else viewHolder.isCompleted.setChecked(false);

    }
    // Gets number of elements in the recycler view/completed to do list
    @Override
    public int getItemCount() {
        return toDoList.size();
    }

    // ViewHolder inner class
    public static class ViewHolder extends RecyclerView.ViewHolder {

        CheckBox isCompleted;
        CheckBox isStarred;
        TextView name;
        TextView dueDate;
        RelativeLayout toDoLayout;

        // Constructor for a ViewHolder object. Gets the elements from a todo_list_item.
        public ViewHolder(View itemView) {
            super(itemView);

            toDoLayout = (RelativeLayout) itemView.findViewById(R.id.to_do_item);
            toDoLayout.setBackgroundResource(R.color.colorRed);

            name = (TextView) itemView.findViewById(R.id.toDoName);
            dueDate = (TextView) itemView.findViewById(R.id.toDoDueDate);
            isCompleted = (CheckBox) itemView.findViewById(R.id.toDoCheckBox);
            isStarred = (CheckBox) itemView.findViewById(R.id.toDoStar);

            // All fields in the CompletedToDoList are not enabled
            name.setEnabled(false);
            dueDate.setEnabled(false);
            isCompleted.setEnabled(false);
            isStarred.setEnabled(false);

        }
        //  Binds an onclick listener to each item. When clicked, the item is passed to the CompletedToDoFragment
        public void bind(final ToDoItem item, final CompletedToDoListAdapter.OnItemClickListener listener) {
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onItemClick(item);
                }
            });
        }

    }
    // Sets OnItemClickListener interface
    public interface OnItemClickListener {
        void onItemClick(ToDoItem item);
    }
}
