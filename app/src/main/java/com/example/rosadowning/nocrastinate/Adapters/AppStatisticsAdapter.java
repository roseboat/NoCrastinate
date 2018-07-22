package com.example.rosadowning.nocrastinate.Adapters;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.pm.PackageManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.rosadowning.nocrastinate.CustomUsageStats;
import com.example.rosadowning.nocrastinate.R;
import com.example.rosadowning.nocrastinate.TimeHelper;

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

        viewHolder.getPackageName().setText(mCustomUsageStatsList.get(position).appName);
        viewHolder.getAppIcon().setImageDrawable(mCustomUsageStatsList.get(position).appIcon);
        long dur = mCustomUsageStatsList.get(position).usageStats.getTotalTimeInForeground();
        String time = TimeHelper.formatDuration(dur);
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