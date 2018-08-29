package com.example.rosadowning.nocrastinate.Adapters;
/*
Adapter class for the recycler view in BlockedAppsFragment. Presents the user's installed applications alongside a switch which can be turned on to put the application in question into the BlockedApps database, or off to take that application out of the BlockedApps database.
 */

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import com.example.rosadowning.nocrastinate.DataModels.CustomAppHolder;
import com.example.rosadowning.nocrastinate.R;

import java.util.ArrayList;
import java.util.List;

public class BlockAppsAdapter extends RecyclerView.Adapter<BlockAppsAdapter.ViewHolder> {

    private List<CustomAppHolder> customAppHolders;
    private final OnItemClickListener blockAppListeners;

    // BlockAppsAdapter is constructed with a list of CustomAppHolders and an OnItemClickListener
    public BlockAppsAdapter(List<CustomAppHolder> holders, OnItemClickListener listener) {
        this.customAppHolders = holders;
        this.blockAppListeners = listener;
    }

    // Sets the layout resource 'block_applications_item' as the viewholder for a certain application
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View v = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.block_applications_item, viewGroup, false);

        return new ViewHolder(v);
    }

    // Binds a viewholder to the information contained in a CustomAppHolder object in the customAppHolders list.
    @Override
    public void onBindViewHolder(final ViewHolder viewHolder, final int position) {
        // Binds an on click listener to each viewholder position
        viewHolder.bind(customAppHolders.get(position), blockAppListeners);

        // Binds information to each view holder
        viewHolder.mPackageName.setText(customAppHolders.get(position).appName);
        viewHolder.mAppIcon.setImageDrawable(customAppHolders.get(position).appIcon);
        viewHolder.mSwitch.setChecked(customAppHolders.get(position).isBlocked);

        // If the application is blocked, show a padlock icon.
        if (customAppHolders.get(position).isBlocked) {
            viewHolder.mLockIcon.setVisibility(View.VISIBLE);
        } else {
            viewHolder.mLockIcon.setVisibility(View.INVISIBLE);
        }

        viewHolder.mSwitch.setOnCheckedChangeListener(null);

        viewHolder.mSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    blockAppListeners.onSwitchCheck(customAppHolders.get(viewHolder.getAdapterPosition()));
                    viewHolder.mLockIcon.animate().alpha(1).setDuration(1000);
                    viewHolder.mLockIcon.setVisibility(View.VISIBLE);
                } else {
                    blockAppListeners.onSwitchUncheck(customAppHolders.get(viewHolder.getAdapterPosition()));
                    viewHolder.mLockIcon.animate().alpha(0).setDuration(1000);
                }
            }
        });
    }

    // Returns the number of elements in the customAppHolders list / the recycler view
    @Override
    public int getItemCount() {
        return customAppHolders.size();
    }

    // Interface which sets the onSwitchCheck and onSwitchUncheck listeners which are used to communicate with the BlockAppsFragment
    public interface OnItemClickListener {
        void onSwitchCheck(CustomAppHolder customAppHolder);

        void onSwitchUncheck(CustomAppHolder customAppHolder);
    }

    // ViewHolder inner class
    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView mPackageName;
        private final ImageView mAppIcon, mLockIcon;
        private final Switch mSwitch;

        // Constructor for a ViewHolder object. Gets the elements from a block_applications_item.
        public ViewHolder(View v) {
            super(v);
            mPackageName = (TextView) v.findViewById(R.id.textview_package_name);
            mAppIcon = (ImageView) v.findViewById(R.id.app_icon);
            mSwitch = (Switch) v.findViewById(R.id.block_apps_switch);
            mLockIcon = (ImageView) v.findViewById(R.id.lockImage);
        }

        //  Binds an onclick listener to each item. When checked or unchecked, the item is passed to the BlockAppsFragment
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