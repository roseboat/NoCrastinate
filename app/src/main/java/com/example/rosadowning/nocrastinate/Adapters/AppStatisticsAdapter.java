package com.example.rosadowning.nocrastinate.Adapters;

import java.sql.Time;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.zip.CheckedOutputStream;

import android.app.usage.UsageStats;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.FeatureInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PermissionInfo;
import android.content.pm.ResolveInfo;
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
import org.joda.time.Duration;
import org.joda.time.format.PeriodFormatter;
import org.joda.time.format.PeriodFormatterBuilder;

import static java.security.AccessController.getContext;

public class AppStatisticsAdapter extends RecyclerView.Adapter<AppStatisticsAdapter.ViewHolder> {

    private List<CustomUsageStats> mCustomUsageStatsList = new ArrayList<>();
    private DateFormat mDateFormat = new SimpleDateFormat();
    private PackageManager packageManager;
    private Context context;


    public AppStatisticsAdapter(Context context){
        this.context = context;
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

        List<PackageInfo> installedPackages = context.getPackageManager()
                .getInstalledPackages(0);

        for (int i = 0; i < installedPackages.size(); i++) {
            PackageInfo packageInfo = installedPackages.get(i);
            if (packageInfo.packageName.equals(appName)) {
                appName = packageInfo.applicationInfo.loadLabel(context.getPackageManager()).toString();
            }
        }

        viewHolder.getPackageName().setText(appName);
        viewHolder.getAppIcon().setImageDrawable(mCustomUsageStatsList.get(position).appIcon);
        long dur = mCustomUsageStatsList.get(position).usageStats.getTotalTimeInForeground();
        String time = timeToString(dur);
        viewHolder.mOverallTime.setText(time);

    }
    public String timeToString(long duration) {

        Duration dur = new Duration(duration);
        PeriodFormatter formatter = new PeriodFormatterBuilder()
                .appendDays()
                .appendSuffix("d")
                .appendHours()
                .appendSuffix("h")
                .appendMinutes()
                .appendSuffix("m")
                .toFormatter();
        String formatted = formatter.print(dur.toPeriod());
        return formatted;
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
        private final ImageView mAppIcon;
        private final TextView mOverallTime;

        public ViewHolder(View v) {
            super(v);
            mPackageName = (TextView) v.findViewById(R.id.textview_package_name);
            mOverallTime = (TextView) v.findViewById(R.id.text_view_app_time_spent);
            mAppIcon = (ImageView) v.findViewById(R.id.app_icon);
        }


        public TextView getmOverallTime() { return mOverallTime; }

        public TextView getPackageName() {
            return mPackageName;
        }

        public ImageView getAppIcon() {
            return mAppIcon;
        }
    }
}