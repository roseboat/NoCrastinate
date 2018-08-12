package com.example.rosadowning.nocrastinate.DataModels;


import android.app.usage.UsageStats;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class StatsData {

    private Date date;
    private int noOfUnlocks;
    private long overallTime;
    private long tasksCompleted;
    private List<CustomAppHolder> appsList;

    public StatsData() {

        date = null;
        noOfUnlocks = 0;
        overallTime = 0;
        tasksCompleted = 0;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public Date getDate() {
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

    public void setTasksCompleted(long tasksCompleted) {
        this.tasksCompleted = tasksCompleted;
    }

    public long getTasksCompleted() {
        return tasksCompleted;
    }

    public List<CustomAppHolder> getAppsList() {
        return appsList;
    }

    public void setAppsList(List<CustomAppHolder> apps) {
        this.appsList = apps;
    }

}
