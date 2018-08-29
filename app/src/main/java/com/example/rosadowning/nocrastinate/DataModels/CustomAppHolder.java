package com.example.rosadowning.nocrastinate.DataModels;

/*
Models an application object. Can have a app name, package name (unique), app icon, the time it has been in the phone's foreground and a boolean representing whether or not it has been blocked by the user.
 */

import android.graphics.drawable.Drawable;

public class CustomAppHolder {
    public Drawable appIcon;
    public long timeInForeground;
    public String appName, packageName;
    public Boolean isBlocked = false;

    // Default constructor with no parameters
    public CustomAppHolder() {
    }
    // Overrides other constructor. Constructs a CustomAppHolder object with a package name.
    public CustomAppHolder(String packageNameKey) {
        this.packageName = packageNameKey;
    }
}