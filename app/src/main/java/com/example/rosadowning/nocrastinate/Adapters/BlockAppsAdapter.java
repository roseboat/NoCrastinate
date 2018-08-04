package com.example.rosadowning.nocrastinate.Adapters;

import android.content.Context;
import android.content.pm.PackageManager;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import com.example.rosadowning.nocrastinate.DataModels.CustomAppHolder;
import com.example.rosadowning.nocrastinate.DataModels.ToDoItem;
import com.example.rosadowning.nocrastinate.R;

import java.util.ArrayList;
import java.util.List;

public class BlockAppsAdapter extends RecyclerView.Adapter<BlockAppsAdapter.ViewHolder> {

    private List<CustomAppHolder> mApplicationsList = new ArrayList<>();
    private PackageManager packageManager;
    private Context context;
    private final OnItemClickListener blockAppListeners;

    public BlockAppsAdapter(List<CustomAppHolder> holders, OnItemClickListener listener){
           this.mApplicationsList = holders;
            this.blockAppListeners = listener;

    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View v = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.block_applications_item, viewGroup, false);

        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final ViewHolder viewHolder, final int position) {
        viewHolder.bind(mApplicationsList.get(position), blockAppListeners);

        viewHolder.mPackageName.setText(mApplicationsList.get(position).appName);
        viewHolder.mAppIcon.setImageDrawable(mApplicationsList.get(position).appIcon);
        viewHolder.mSwitch.setChecked(mApplicationsList.get(position).isBlocked);

        if (mApplicationsList.get(position).isBlocked) {
            viewHolder.mLockIcon.setVisibility(View.VISIBLE);
        } else {
            viewHolder.mLockIcon.setVisibility(View.INVISIBLE);
        }

        viewHolder.mSwitch.setOnCheckedChangeListener(null);
        viewHolder.mSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    blockAppListeners.onSwitchCheck(mApplicationsList.get(viewHolder.getAdapterPosition()));
                    viewHolder.mLockIcon.animate().alpha(1).setDuration(1000);
                    viewHolder.mLockIcon.setVisibility(View.VISIBLE);
                } else {
                    blockAppListeners.onSwitchUncheck(mApplicationsList.get(viewHolder.getAdapterPosition()));
                    viewHolder.mLockIcon.animate().alpha(0).setDuration(1000);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mApplicationsList.size();
    }

    public interface OnItemClickListener {

        void onSwitchCheck(CustomAppHolder customAppHolder);

        void onSwitchUncheck(CustomAppHolder customAppHolder);

    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView mPackageName;
        private final ImageView mAppIcon, mLockIcon;
        private final Switch mSwitch;

        public ViewHolder(View v) {
            super(v);
            mPackageName = (TextView) v.findViewById(R.id.textview_package_name);
            mAppIcon = (ImageView) v.findViewById(R.id.app_icon);
            mSwitch = (Switch) v.findViewById(R.id.block_apps_switch);
            mLockIcon = (ImageView) v.findViewById(R.id.lockImage);
        }

        public TextView getPackageName() {
            return mPackageName;
        }

        public ImageView getAppIcon() {
            return mAppIcon;
        }

        public Switch getSwitch() { return mSwitch; }

        public ImageView getLockIcon() { return mLockIcon; }

        public void bind(final CustomAppHolder item, final BlockAppsAdapter.OnItemClickListener listener) {
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onSwitchCheck(item);
                }
            });
        }

    }
}