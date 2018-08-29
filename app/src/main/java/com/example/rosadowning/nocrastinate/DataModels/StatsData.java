package com.example.rosadowning.nocrastinate.DataModels;

/*
Models the stats present in the StatisticsFragment, both those in the top three icons and the list of CustomAppHolders in the bottom recycler view.
 */

import java.util.Date;
import java.util.List;

public class StatsData {

    private Date date;
    private int noOfUnlocks;
    private long overallTime;
    private long tasksCompleted;
    private List<CustomAppHolder> appsList;

    // Default constructor sets variables to null or 0;
    public StatsData() {
        date = null;
        noOfUnlocks = 0;
        overallTime = 0;
        tasksCompleted = 0;
    }

    // Accessor and mutator methods
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
