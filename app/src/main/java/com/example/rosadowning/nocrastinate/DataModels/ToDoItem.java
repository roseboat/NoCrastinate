package com.example.rosadowning.nocrastinate.DataModels;

/*
Models a To Do item which may appear in the main to do list, the completed to do list or individually in the View To Do screen.
 */

import java.io.Serializable;
import java.util.Date;

// Implements Serializable so that it can be passed in a Bundle
public class ToDoItem implements Serializable {

    // Instance variables which represent the various attributes a To Do Item may have
    private String name, note;
    private Boolean isStarred, isCompleted;
    private Date dueDate, alarmDate, completedDate, addedDate;

    // Default constructor for a ToDoItem. They must have a name. All other attributes are set to null or false with the exception of the addedDate which functions as a primary key, and is set to the exact time in which the ToDoItem is created.
    public ToDoItem(String name) {
        this.name = name;
        this.note = null;
        this.dueDate = null;
        this.isCompleted = false;
        this.isStarred = false;
        this.completedDate = null;
        this.alarmDate = null;
        this.addedDate = new Date(System.currentTimeMillis());
    }

    // Accessor and mutator methods for a ToDoItem
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public Boolean getStarred() {
        return isStarred;
    }

    public void setStarred(Boolean starred) {
        isStarred = starred;
    }

    public Boolean getCompleted() {
        return isCompleted;
    }

    public void setCompleted(Boolean completed) {
        isCompleted = completed;
    }

    public Date getDueDate() {
        return dueDate;
    }

    public void setDueDate(Date dueDate) {
        this.dueDate = dueDate;
    }

    public Date getAlarmDate() {
        return alarmDate;
    }

    public void setAlarmDate(Date alarmDate) {
        this.alarmDate = alarmDate;
    }

    public Date getCompletedDate() {
        return completedDate;
    }

    public void setCompletedDate(Date completedDate) {
        this.completedDate = completedDate;
    }

    public Date getAddedDate() {
        return addedDate;
    }

    public void setAddedDate(Date addDate) {
        this.addedDate = addDate;
    }

    // Equals method determines whether or not one ToDoItem is equal to another. Compares all of their attributes against one another, if any are different then the ToDoItems are not equal.
    public boolean equals(ToDoItem otherItem) {
        boolean isEqual = false;
        if (name.equals(otherItem.getName()) && note.equals(otherItem.getNote())) {
            if (isStarred = otherItem.getStarred() && isCompleted == otherItem.getCompleted()) {
                if (dueDate == otherItem.getDueDate() && alarmDate == otherItem.getAlarmDate() && addedDate == otherItem.getAddedDate()) {
                    isEqual = true;
                }
            }
        }
        return isEqual;
    }
}
