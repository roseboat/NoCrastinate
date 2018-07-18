package com.example.rosadowning.nocrastinate;

import java.util.ArrayList;

public class StatsComposite extends StatsComponent {


    protected ArrayList<StatsComponent> collectionOfStats;

    public StatsComposite(){
        super();
        collectionOfStats = new ArrayList<>();
    }
    public void addStat(StatsComponent addThisStat){
        collectionOfStats.add(addThisStat);
    }

    public void deleteStat(StatsComponent deleteThisStat){
        collectionOfStats.remove(deleteThisStat);
    }

    public ArrayList<StatsComponent> getCollectionOfStats(){
        return collectionOfStats;
    }


    public int getNoOfUnlocks(){

        for(StatsComponent x : collectionOfStats){
            noOfUnlocks += x.getNoOfUnlocks();
        }
        return noOfUnlocks;
    }
    public void setNoOfUnlocks(int noOfUnlocks){
        this.noOfUnlocks = noOfUnlocks;
    }



    public long getOverallTime(){

        for (StatsComponent x : collectionOfStats){
            overallTime += x.getOverallTime();
        }
        return overallTime;
    }
    public void setOverallTime(long overallTime){
        this.overallTime = overallTime;
    }

    public long getTasksCompleted(){
        for (StatsComponent x : collectionOfStats){
            tasksCompleted += x.getTasksCompleted();
        }
        return tasksCompleted;
    }


}
