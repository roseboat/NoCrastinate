package com.example.rosadowning.nocrastinate.Fragments;
/*
Class which represents the Block Applications screen.
Sets up a recycler view which displays the user's installed apps in alphabetical order
alongside a switch. When the switch is turned on, the corresponding application is placed
in a database representing apps which are blocked.

This BlockedApps database is accessed by the BlockAppsService to shut down any
blocked apps when they are brought to the phone's foreground.
 */

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.example.rosadowning.nocrastinate.Adapters.BlockAppsAdapter;
import com.example.rosadowning.nocrastinate.DBHelpers.BlockedAppsDBContract;
import com.example.rosadowning.nocrastinate.DataModels.CustomAppHolder;
import com.example.rosadowning.nocrastinate.MainActivity;
import com.example.rosadowning.nocrastinate.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import jp.wasabeef.recyclerview.adapters.AlphaInAnimationAdapter;

public class BlockAppsFragment extends Fragment {

    private Context context;
    private RecyclerView mRecyclerView;
    private BlockAppsAdapter mBlockAppsAdapter;
    private BlockedAppsDBContract.BlockedAppsDBHelper dbHelper;
    private List<String> blockedApps;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_block_apps, null);
        LinearLayout settingsPopup = (LinearLayout) view.findViewById(R.id.settings_popup);

        context = getContext();

        // Calls the static method in MainActivity, hasUsagePermission to determine whether or not the user has allowed usage access from the app
        // If Usage Access is switched off, a pop-up is made visible prompting the user to switch the settings on
        if (!MainActivity.hasUsagePermission(context)) {
            settingsPopup.setVisibility(View.VISIBLE);
            settingsPopup.bringToFront();
        } else {
            // If usage access is allowed, the pop up is made invisible
            settingsPopup.setVisibility(View.GONE);

            dbHelper = new BlockedAppsDBContract.BlockedAppsDBHelper(context);
            mRecyclerView = (RecyclerView) view.findViewById(R.id.block_apps_recycler_view);
            mBlockAppsAdapter = new BlockAppsAdapter(getApps(context), new BlockAppsAdapter.OnItemClickListener() {
                // if an item's switch is checked, the corresponding app is placed in the BlockedApps database
                @Override
                public void onSwitchCheck(CustomAppHolder customAppHolder) {
                    dbHelper.insertApp(customAppHolder.packageName);
                }

                // if an item's switch is unchecked, the corresponding app is removed from the database
                @Override
                public void onSwitchUncheck(CustomAppHolder customAppHolder) {
                    dbHelper.removeApp(customAppHolder.packageName);
                }
            });
            // Sets the recycler view's adapter to the BlockAppsAdapter
            mRecyclerView.scrollToPosition(0);
            mRecyclerView.setAdapter(new AlphaInAnimationAdapter(mBlockAppsAdapter));
            mBlockAppsAdapter.notifyDataSetChanged();
            mRecyclerView.scrollToPosition(0);
        }
        return view;
    }

    // Method to get the user's installed applications
    public List<CustomAppHolder> getApps(Context context) {

        if (dbHelper == null) { // for testing
            dbHelper = new BlockedAppsDBContract.BlockedAppsDBHelper(context);
        }

        // Gets the list of blocked apps from the BlockedApps database
        blockedApps = dbHelper.getBlockedApps();

        List<CustomAppHolder> customAppHolders = new ArrayList<>();

        // Gets the list of packages on the user's phone
        List<PackageInfo> packages = context.getPackageManager()
                .getInstalledPackages(0);

        // Loops through all packages. If they are installed packages, not system packages and not 'NoCrastinate'
        // they are made into a CustomAppHolder object
        for (PackageInfo packageInfo : packages) {

            if ((packageInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0) {
                if (!packageInfo.packageName.contains("nocrastinate")) {
                    CustomAppHolder customAppHolder = new CustomAppHolder();
                    customAppHolder.appName = packageInfo.applicationInfo.loadLabel(context.getPackageManager()).toString();
                    customAppHolder.packageName = packageInfo.packageName;
                    customAppHolder.isBlocked = false;

                    // Loops through the names of all the blocked apps from the database
                    // If the name matches the name of the new CustomAppHolder, it sets the
                    // CustomAppHolder object's boolean isBlocked to true.
                    for (String blockedAppName : blockedApps) {
                        if (blockedAppName.equals(customAppHolder.packageName)) {
                            customAppHolder.isBlocked = true;
                        }
                    }
                    // Gets the app's icon
                    customAppHolder.appIcon = context.getPackageManager()
                            .getApplicationIcon(packageInfo.applicationInfo);
                    customAppHolders.add(customAppHolder); // Adds app to customAppHolders list
                }
            }
        }
        // Sorts customAppHolders List into alphabetical order and returns it.
        Collections.sort(customAppHolders, new AppAlphaOrder());
        Collections.reverse(customAppHolders);
        return customAppHolders;
    }

    // Sorts CustomAppHolder objects in alphabetical order
    private static class AppAlphaOrder implements Comparator<CustomAppHolder> {

        @Override
        public int compare(CustomAppHolder left, CustomAppHolder right) {
            return right.appName.compareTo(left.appName);
        }
    }
}