package com.example.rosadowning.nocrastinate.Fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.rosadowning.nocrastinate.R;

import org.w3c.dom.Text;

public class SettingsFragment extends Fragment {


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
                getFragmentManager().beginTransaction().replace(R.id.fragment_container, new HelpCenterFragment()).commit();
            }
        });

        notiSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getFragmentManager().beginTransaction().replace(R.id.fragment_container, new NotificationSettingsFragment()).commit();
            }
        });

        blockApps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getFragmentManager().beginTransaction().replace(R.id.fragment_container, new BlockAppsFragment()).commit();
            }
        });
        return view;
    }

}
