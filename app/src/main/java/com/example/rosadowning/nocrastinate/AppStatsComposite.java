package com.example.rosadowning.nocrastinate;

import android.app.usage.UsageStats;

import java.util.ArrayList;

public class AppStatsComposite extends AppStatsComponent{


    protected ArrayList<AppStatsComponent> collectionOfStats;

    public AppStatsComposite(){
        super();
        collectionOfStats = new ArrayList<>();
    }
    public void addStat(AppStatsComponent addThisStat){
        collectionOfStats.add(addThisStat);
    }

    public void deleteStat(AppStatsComponent deleteThisStat){
        collectionOfStats.remove(deleteThisStat);
    }

    public ArrayList<AppStatsComponent> getCollectionOfStats(){
        return collectionOfStats;
    }


    public int getNoOfUnlocks(){

        for(AppStatsComponent x : collectionOfStats){
            noOfUnlocks += x.getNoOfUnlocks();
        }
        return noOfUnlocks;
    }
    public void setNoOfUnlocks(int noOfUnlocks){
        this.noOfUnlocks = noOfUnlocks;
    }
    public double getOverallTime(){

        for (AppStatsComponent x : collectionOfStats){
            overallTime += x.getOverallTime();
        }
        return overallTime;
    }
    public void setOverallTime(double overallTime){
        this.overallTime = overallTime;
    }
    public ArrayList<CustomUsageStats> getApplications(){

//        for (StatsComponent x : collectionOfStats){
//        }

        return applications;
    }
//    public void setApplications(ArrayList<UsageStats> applications){
//        this.applications = applications;
//    }
//    public void addApplication(UsageStats additionalApplication){
//        this.applications.add(additionalApplication);
//    }
//    public void deleteApplication(UsageStats deleteApplication){
//        this.applications.remove(deleteApplication);
//    }


}
