package com.example.rosadowning.nocrastinate.Adapters;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;


import com.example.rosadowning.nocrastinate.R;
import com.example.rosadowning.nocrastinate.DataModels.ToDoItem;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class CompletedToDoListAdapter extends RecyclerView.Adapter<CompletedToDoListAdapter.ViewHolder> {

    private ArrayList<ToDoItem> toDoList;
    private final OnItemClickListener listeners;

    public CompletedToDoListAdapter(ArrayList<ToDoItem> toDoList, OnItemClickListener listeners) {
        this.toDoList = toDoList;
        this.listeners = listeners;

    }

    @NonNull
    @Override
    public CompletedToDoListAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {

        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.todo_list_item, viewGroup, false);
        return new ViewHolder(view);

    }

    @Override
    public void onBindViewHolder(@NonNull CompletedToDoListAdapter.ViewHolder viewHolder, int position) {

        viewHolder.bind(toDoList.get(position), listeners);


        ToDoItem item = toDoList.get(position);
        String name = toDoList.get(position).getName();
        Date date = toDoList.get(position).getDueDate();
        Boolean star = toDoList.get(position).getStarred();
        Boolean completed = toDoList.get(position).getCompleted();

        viewHolder.getName().setText(name);

        if (date.getTime() != 0) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
            viewHolder.getDueDate().setText("Due date: " + sdf.format(date));
        } else {
            viewHolder.getDueDate().setText("");
        }
        if (star == true)
            viewHolder.getIsStarred().setChecked(true);
        else viewHolder.getIsStarred().setChecked(false);

        if (completed == true)
            viewHolder.getIsCompleted().setChecked(true);
        else viewHolder.getIsCompleted().setChecked(false);

    }

    @Override
    public int getItemCount() {
        return toDoList.size();
    }


    public static class ViewHolder extends RecyclerView.ViewHolder {

        CheckBox isCompleted;
        CheckBox isStarred;
        TextView name;
        TextView dueDate;
        LinearLayout toDoLayout;


        public ViewHolder(View itemView) {
            super(itemView);

            toDoLayout = (LinearLayout) itemView.findViewById(R.id.to_do_item);
            toDoLayout.setBackgroundResource(R.color.colorCompleted);

            name = (TextView) itemView.findViewById(R.id.toDoName);
            dueDate = (TextView) itemView.findViewById(R.id.toDoDueDate);
            isCompleted = (CheckBox) itemView.findViewById(R.id.toDoCheckBox);
            isStarred = (CheckBox) itemView.findViewById(R.id.toDoStar);
            name.setEnabled(false);
            dueDate.setEnabled(false);
            isCompleted.setEnabled(false);
            isStarred.setEnabled(false);

        }

        public CheckBox getIsCompleted() {
            return isCompleted;
        }

        public CheckBox getIsStarred() {
            return isStarred;
        }

        public TextView getName() {
            return name;
        }

        public TextView getDueDate() {
            return dueDate;
        }

        public void bind(final ToDoItem item, final CompletedToDoListAdapter.OnItemClickListener listener) {
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onItemClick(item);
                }
            });
        }

    }
    public interface OnItemClickListener {
        void onItemClick(ToDoItem item);}


}
