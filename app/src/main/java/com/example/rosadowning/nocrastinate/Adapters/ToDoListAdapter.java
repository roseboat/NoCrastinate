package com.example.rosadowning.nocrastinate.Adapters;

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

public class ToDoListAdapter extends RecyclerView.Adapter<ToDoListAdapter.ViewHolder> {

    private ArrayList<ToDoItem> toDoList;
    private final OnItemClickListener listeners;

    public ToDoListAdapter(ArrayList<ToDoItem> toDoList, OnItemClickListener listeners) {
        this.toDoList = toDoList;
        this.listeners = listeners;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {

        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.todo_list_item, viewGroup, false);
        return new ViewHolder(view);

    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder viewHolder, int position) {
        viewHolder.bind(toDoList.get(position), listeners);

        String name = toDoList.get(position).getName();
        Date date = toDoList.get(position).getDueDate();
        Date alarm = toDoList.get(position).getAlarmDate();
        Boolean starred = toDoList.get(position).getStarred();
        Boolean completed = toDoList.get(position).getCompleted();

        viewHolder.isCompleted.setOnCheckedChangeListener(null);
        viewHolder.isStarred.setOnCheckedChangeListener(null);

        viewHolder.getName().setText(name);

        if (date.getTime() != 0) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
            viewHolder.getDueDate().setText("Due date: " + sdf.format(date));
        } else {
            viewHolder.getDueDate().setText("");
        }

        if (alarm.getTime() != 0){
            viewHolder.getHasAlarm().setVisibility(View.VISIBLE);
        }

        if (completed == true)
            viewHolder.getIsCompleted().setChecked(true);
        else viewHolder.getIsCompleted().setChecked(false);


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

        if (starred) {
            viewHolder.getIsStarred().setChecked(true);
        } else {
            viewHolder.getIsStarred().setChecked(false);
        }

        viewHolder.isStarred.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    toDoList.get(viewHolder.getAdapterPosition()).setStarred(true);
                    listeners.onStarCheck(toDoList.get(viewHolder.getAdapterPosition()));
                    viewHolder.wholeToDoItem.setBackgroundResource(R.color.colorGrey);

                } else {
                    toDoList.get(viewHolder.getAdapterPosition()).setStarred(false);
                    listeners.onStarUncheck(toDoList.get(viewHolder.getAdapterPosition()));
                    viewHolder.wholeToDoItem.setBackgroundResource(R.color.colorToDoBackground);

                }
            }
        });
    }

    public void remove(int position){

        toDoList.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, toDoList.size());
    }

    @Override
    public int getItemCount() {
        return toDoList.size();
    }

    public interface OnItemClickListener {
        void onItemClick(ToDoItem item);

        void onItemCheck(ToDoItem item, int position);

        void onStarCheck(ToDoItem item);

        void onStarUncheck(ToDoItem item);

    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        CheckBox isCompleted, isStarred;
        ImageView hasAlarm;
        TextView name, dueDate;
        LinearLayout toDoLayout;
        RelativeLayout wholeToDoItem;

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
        public ImageView getHasAlarm() { return hasAlarm;};
        public RelativeLayout getWholeToDoItem() {
            return wholeToDoItem;
        }

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



