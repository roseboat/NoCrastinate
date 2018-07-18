package com.example.rosadowning.nocrastinate;


import android.app.usage.UsageStats;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.util.ArrayList;
import java.util.Date;

public abstract class StatsComponent {

    protected Date date;
    protected int noOfUnlocks;
    protected long overallTime;
    protected long tasksCompleted;

    public StatsComponent(){

        date = null;
        noOfUnlocks = 0;
        overallTime = 0;
        tasksCompleted = 0;
    }

    public void setDate(Date date){
        this.date = date;
    }

    public Date getDate(){
        return date;
    }

    public int getNoOfUnlocks() {
        return noOfUnlocks;
    }

    public void setNoOfUnlocks(int noOfUnlocks) {
        this.noOfUnlocks = noOfUnlocks;
    }

    public long getOverallTime() {
        return overallTime;
    }

    public void setOverallTime(long overallTime) {
        this.overallTime = overallTime;
    }

    public void setTasksCompleted(long tasksCompleted){
        this.tasksCompleted = tasksCompleted;
    }

    public long getTasksCompleted(){
        return tasksCompleted;
    }


}
