package com.example.rosadowning.nocrastinate.Adapters;
/*
Adapter class for the recycler view which presents the user's installed applications and the time in foreground of each application for a certain interval. Used by the StatisticsFragment to present app statistics.
 */

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.rosadowning.nocrastinate.DataModels.CustomAppHolder;
import com.example.rosadowning.nocrastinate.DataModels.TimeHelper;
import com.example.rosadowning.nocrastinate.R;

import java.util.ArrayList;
import java.util.List;

public class AppStatisticsAdapter extends RecyclerView.Adapter<AppStatisticsAdapter.ViewHolder> {

    private List<CustomAppHolder> customAppHolders = new ArrayList<>();
    private Context context;

    // Constructor
    public AppStatisticsAdapter(Context context) {
        this.context = context;
    }

    // Sets the layout resource 'app_statistics_item' as the viewholder for a certain application
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View v = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.app_statistics_item, viewGroup, false);

        return new ViewHolder(v);
    }

    // Binds a viewholder to the information contained in a CustomAppHolder object in the customAppHolders list.
    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position) {

        viewHolder.mAppName.setText(customAppHolders.get(position).appName);
        viewHolder.mAppIcon.setImageDrawable(customAppHolders.get(position).appIcon);
        viewHolder.mOverallTime.setText(TimeHelper.formatDuration(customAppHolders.get(position).timeInForeground));
    }

    // Returns the number of elements in the customAppHolders list / the recycler view
    @Override
    public int getItemCount() {
        return customAppHolders.size();
    }

    // Sets the list of CustomAppHolders containing the application stats
    public void setCustomAppList(List<CustomAppHolder> customAppList) {
        customAppHolders = customAppList;
    }

    // ViewHolder inner class
    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView mAppName;
        private final ImageView mAppIcon;
        private final TextView mOverallTime;

        // Constructor for a ViewHolder object. Gets the elements from a app_statistics item.
        public ViewHolder(View v) {
            super(v);
            mAppName = (TextView) v.findViewById(R.id.textview_package_name);
            mOverallTime = (TextView) v.findViewById(R.id.text_view_app_time_spent);
            mAppIcon = (ImageView) v.findViewById(R.id.app_icon);
        }
    }
}