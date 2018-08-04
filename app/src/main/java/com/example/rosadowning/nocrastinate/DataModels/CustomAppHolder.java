package com.example.rosadowning.nocrastinate.DataModels;

import android.app.usage.UsageStats;
import android.graphics.drawable.Drawable;

public class CustomAppHolder {
    public Drawable appIcon;
    public long timeInForeground;
    public String appName, packageName;
    public Boolean isBlocked = false;

    public CustomAppHolder() {

    }

    public CustomAppHolder(String packageNameKey) {
        this.packageName = packageNameKey;
    }

    public String getPackageName() {
        return packageName;
    }

}