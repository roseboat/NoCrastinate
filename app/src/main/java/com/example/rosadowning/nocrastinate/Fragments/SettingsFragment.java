package com.example.rosadowning.nocrastinate.Fragments;
/*
Represents the Settings screen of the application.
Simply sets up links to other fragments
 */

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.rosadowning.nocrastinate.R;

public class SettingsFragment extends Fragment {

    // Sets listeners on each menu item on the Statistics screen and what fragment they should redirect to if clicked.
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, null);

        TextView helpCenter = (TextView) view.findViewById(R.id.help_center);
        TextView notiSettings = (TextView) view.findViewById(R.id.notification_settings);
        TextView blockApps = (TextView) view.findViewById(R.id.block_apps);

        helpCenter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getFragmentManager().beginTransaction().replace(R.id.fragment_container, new HelpCenterFragment()).addToBackStack(null).commit();
            }
        });

        notiSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getFragmentManager().beginTransaction().replace(R.id.fragment_container, new NotificationSettingsFragment()).addToBackStack(null).commit();
            }
        });

        blockApps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getFragmentManager().beginTransaction().replace(R.id.fragment_container, new BlockAppsFragment()).addToBackStack(null).commit();
            }
        });
        return view;
    }
}
