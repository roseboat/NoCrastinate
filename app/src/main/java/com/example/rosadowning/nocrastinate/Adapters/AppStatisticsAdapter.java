package com.example.rosadowning.nocrastinate.Adapters;

import java.sql.Time;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.app.usage.UsageStats;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.rosadowning.nocrastinate.CustomUsageStats;
import com.example.rosadowning.nocrastinate.R;

import org.joda.time.DateTime;

public class AppStatisticsAdapter extends RecyclerView.Adapter<AppStatisticsAdapter.ViewHolder> {

    private List<CustomUsageStats> mCustomUsageStatsList = new ArrayList<>();
    private DateFormat mDateFormat = new SimpleDateFormat();
    private PackageManager packageManager;


    public AppStatisticsAdapter(){

    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View v = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.app_statistics_item, viewGroup, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position) {

        String appName = mCustomUsageStatsList.get(position).usageStats.getPackageName();
        if (appName.length() > 12) {
            String longAppName = appName;
            String firstLetter = String.valueOf(longAppName.charAt(12));
            appName = firstLetter.toUpperCase() + longAppName.substring(13);
        }
        viewHolder.getPackageName().setText(appName);

//        long lastTimeUsed = mCustomUsageStatsList.get(position).usageStats.getLastTimeUsed();
//        viewHolder.getLastTimeUsed().setText(mDateFormat.format(new Date(lastTimeUsed)));
        viewHolder.getAppIcon().setImageDrawable(mCustomUsageStatsList.get(position).appIcon);

//        String meh = DateUtils.formatElapsedTime(mCustomUsageStatsList.get(position).usageStats.getTotalTimeInForeground() / 1000);
        DateTime appTime = new DateTime (mCustomUsageStatsList.get(position).usageStats.getTotalTimeInForeground());
        int seconds = appTime.getSecondOfMinute();
        int minutes = appTime.getMinuteOfHour();
        int hour = appTime.getHourOfDay();
        String time = null;
        if (minutes < 1){
            time = seconds + " secs";
        } else if (hour < 1){
            time = minutes + " mins";
        } else {
            time = hour + "hrs + " + minutes + " mins";
        }
//        viewHolder.mOverallTime.setText(DateUtils.formatElapsedTime(mCustomUsageStatsList.get(position).usageStats.getTotalTimeInForeground() / 1000));
    viewHolder.mOverallTime.setText(time);

    }

    @Override
    public int getItemCount() {
        return mCustomUsageStatsList.size();
    }

    public void setCustomUsageStatsList(List<CustomUsageStats> customUsageStats) {
        mCustomUsageStatsList = customUsageStats;
    }


    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView mPackageName;
//        private final TextView mLastTimeUsed;
        private final ImageView mAppIcon;
        private final TextView mOverallTime;

        public ViewHolder(View v) {
            super(v);
            mPackageName = (TextView) v.findViewById(R.id.textview_package_name);
//            mLastTimeUsed = (TextView) v.findViewById(R.id.textview_last_time_used);
            mOverallTime = (TextView) v.findViewById(R.id.text_view_app_time_spent);
            mAppIcon = (ImageView) v.findViewById(R.id.app_icon);
        }

//        public TextView getLastTimeUsed() {
//            return mLastTimeUsed;
//        }

        public TextView getmOverallTime() { return mOverallTime; }

        public TextView getPackageName() {
            return mPackageName;
        }

        public ImageView getAppIcon() {
            return mAppIcon;
        }
    }
}