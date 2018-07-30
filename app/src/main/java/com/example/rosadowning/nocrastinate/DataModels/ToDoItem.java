package com.example.rosadowning.nocrastinate.DataModels;


import java.io.Serializable;
import java.util.Date;

public class ToDoItem implements Serializable {

    private String name, note;
    private Boolean isStarred, isCompleted;
    private Date dueDate, completedDate, alarmDate, addedDate;

    public ToDoItem(String name) {
        this.setName(name);
        this.note = null;
        this.dueDate = null;
        this.isCompleted = false;
        this.isStarred = false;
        this.completedDate = null;
        this.alarmDate = null;
        this.addedDate = new Date(System.currentTimeMillis());
    }

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

    public Date getAlarmDate(){ return alarmDate; }

    public void setAlarmDate(Date alarmDate){ this.alarmDate = alarmDate;}

    public Date getCompletedDate(){ return completedDate; }

    public void setCompletedDate(Date completedDate){ this.completedDate = completedDate; }

    public Date getAddedDate() { return addedDate;}

    public void setAddedDate(Date addDate){ this.addedDate = addDate;}

    public boolean equals(ToDoItem otherItem) {
        boolean isEqual = false;
        if (name.equals(otherItem.getName()) && note.equals(otherItem.getNote())){
            if (isStarred = otherItem.getStarred() && isCompleted == otherItem.getCompleted()) {
                if (dueDate == otherItem.getDueDate() && alarmDate == otherItem.getAlarmDate() && addedDate == otherItem.getAddedDate()) {
                    isEqual = true;
                }
            }
        }
        return isEqual;
    }
}
