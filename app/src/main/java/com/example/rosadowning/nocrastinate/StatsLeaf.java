package com.example.rosadowning.nocrastinate;

import java.util.Date;

public class StatsLeaf extends StatsComponent {


    protected Date date;
    protected int noOfUnlocks;
    protected long overallTime;
    protected long tasksCompleted;

    public StatsLeaf(){
       super();
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
