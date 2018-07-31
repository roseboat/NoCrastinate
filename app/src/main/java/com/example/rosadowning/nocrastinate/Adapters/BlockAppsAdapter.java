package com.example.rosadowning.nocrastinate.Adapters;

import android.content.Context;
import android.content.pm.PackageManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import com.example.rosadowning.nocrastinate.DataModels.CustomUsageStats;
import com.example.rosadowning.nocrastinate.DataModels.TimeHelper;
import com.example.rosadowning.nocrastinate.R;

import java.util.ArrayList;
import java.util.List;

public class BlockAppsAdapter extends RecyclerView.Adapter<BlockAppsAdapter.ViewHolder> {

    private List<CustomUsageStats> mApplicationsList = new ArrayList<>();
    private PackageManager packageManager;
    private Context context;


    public BlockAppsAdapter(Context context){
        this.context = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View v = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.block_applications_item, viewGroup, false);

        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position) {

        viewHolder.getPackageName().setText(mApplicationsList.get(position).appName);
        viewHolder.getAppIcon().setImageDrawable(mApplicationsList.get(position).appIcon);
//        viewHolder.getmSwitch().setChecked();
    }

    @Override
    public int getItemCount() {
        return mApplicationsList.size();
    }

    public void setApplicationsList(List<CustomUsageStats> customUsageStats) {
//        mApplicationsList = ;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView mPackageName;
        private final ImageView mAppIcon;
        private final Switch mSwitch;

        public ViewHolder(View v) {
            super(v);
            mPackageName = (TextView) v.findViewById(R.id.textview_package_name);
            mAppIcon = (ImageView) v.findViewById(R.id.app_icon);
            mSwitch = (Switch) v.findViewById(R.id.block_apps_switch);

        }

        public TextView getPackageName() {
            return mPackageName;
        }

        public ImageView getAppIcon() {
            return mAppIcon;
        }

        public Switch getmSwitch() { return mSwitch; }

    }
}