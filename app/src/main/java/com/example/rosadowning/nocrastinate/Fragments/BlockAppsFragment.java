package com.example.rosadowning.nocrastinate.Fragments;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.rosadowning.nocrastinate.Adapters.BlockAppsAdapter;
import com.example.rosadowning.nocrastinate.BlockedAppsService;
import com.example.rosadowning.nocrastinate.DBHelpers.BlockedAppsDBContract;
import com.example.rosadowning.nocrastinate.DBHelpers.ToDoReaderContract;
import com.example.rosadowning.nocrastinate.DataModels.CustomAppHolder;
import com.example.rosadowning.nocrastinate.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import jp.wasabeef.recyclerview.adapters.AlphaInAnimationAdapter;

public class BlockAppsFragment extends Fragment {

    private RecyclerView mRecyclerView;
    private BlockAppsAdapter mBlockAppsAdapter;
    private SQLiteDatabase sql;
    private BlockedAppsDBContract.BlockedAppsDBHelper dbHelper;
    private List<String> blockedApps;
    private static final String TAG = "BLOCK APPS FRAG";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_block_apps, null);

        if (!BlockedAppsService.hasUsagePermission(getContext())){

            AlertDialog alertDialog = new AlertDialog.Builder(getContext())
                    .setMessage(R.string.dialog_message_noti_settings_off)
                    .setTitle(R.string.dialog_title_noti_settings_off)
                    .setPositiveButton("Take me there!", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            startActivity(new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS));
                        }
                    }).setNegativeButton("Cancel", null).create();
            alertDialog.show();
        } else {

            dbHelper = new BlockedAppsDBContract.BlockedAppsDBHelper(getContext());
            sql = dbHelper.getReadableDatabase();
            blockedApps = dbHelper.getBlockedApps();
            mRecyclerView = (RecyclerView) view.findViewById(R.id.block_apps_recycler_view);
            mBlockAppsAdapter = new BlockAppsAdapter(getApps(), new BlockAppsAdapter.OnItemClickListener() {
                @Override
                public void onSwitchCheck(CustomAppHolder customAppHolder) {
                    sql = dbHelper.getWritableDatabase();
                    dbHelper.insertApp(customAppHolder.packageName);
                }

                @Override
                public void onSwitchUncheck(CustomAppHolder customAppHolder) {
                    sql = dbHelper.getWritableDatabase();
                    dbHelper.removeApp(customAppHolder.packageName);
                }
            });
            mRecyclerView.scrollToPosition(0);
            mRecyclerView.setAdapter(new AlphaInAnimationAdapter(mBlockAppsAdapter));
            mBlockAppsAdapter.notifyDataSetChanged();
            mRecyclerView.scrollToPosition(0);
        }
        return view;
    }

    public List<CustomAppHolder> getApps() {

        List<CustomAppHolder> customAppHolders = new ArrayList<>();

        List<PackageInfo> installedPackages = getContext().getPackageManager()
                .getInstalledPackages(0);

        for (PackageInfo packageInfo : installedPackages) {

            if ((packageInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0) {
                if (!packageInfo.packageName.contains("nocrastinate")){
                CustomAppHolder customAppHolder = new CustomAppHolder();
                customAppHolder.appName = packageInfo.applicationInfo.loadLabel(getContext().getPackageManager()).toString();
                customAppHolder.packageName = packageInfo.packageName;
                customAppHolder.isBlocked = false;

                for (String blockedAppName : blockedApps) {
                    if (blockedAppName.equals(customAppHolder.packageName)) {
                        customAppHolder.isBlocked = true;
                    }
                }
                customAppHolder.appIcon = getActivity().getPackageManager()
                        .getApplicationIcon(packageInfo.applicationInfo);
                customAppHolders.add(customAppHolder);
            }}
        }
        Collections.sort(customAppHolders, new AppAlphaOrder());
        Collections.reverse(customAppHolders);
        return customAppHolders;
    }


    private static class AppAlphaOrder implements Comparator<CustomAppHolder> {

        @Override
        public int compare(CustomAppHolder left, CustomAppHolder right) {
            return right.appName.compareTo(left.appName);
        }
    }
    }
