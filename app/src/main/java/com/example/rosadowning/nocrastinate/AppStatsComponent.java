package com.example.rosadowning.nocrastinate;


import android.app.usage.UsageStats;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.util.ArrayList;

public abstract class AppStatsComponent {

    protected int noOfUnlocks;
    protected double overallTime;
    protected ArrayList<CustomUsageStats> applications;

    public AppStatsComponent(){

        noOfUnlocks = 0;
        overallTime = 0;
        applications = new ArrayList<>();
    }

    public int getNoOfUnlocks(){
        return noOfUnlocks;
    }
    public void setNoOfUnlocks(int noOfUnlocks){
        this.noOfUnlocks = noOfUnlocks;
    }
    public double getOverallTime(){
        return overallTime;
    }
    public void setOverallTime(double overallTime){
        this.overallTime = overallTime;
    }
    public ArrayList<CustomUsageStats> getApplications(){
        return applications;
    }
    public void setApplications(ArrayList<CustomUsageStats> applications){
        this.applications = applications;
    }
//    public void addApplication(UsageStats additionalApplication){
//        this.applications.add(additionalApplication);
//    }
//    public void deleteApplication(UsageStats deleteApplication){
//        this.applications.remove(deleteApplication);
//    }

}
