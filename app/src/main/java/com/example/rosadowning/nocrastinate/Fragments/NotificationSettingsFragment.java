package com.example.rosadowning.nocrastinate.Fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import com.example.rosadowning.nocrastinate.R;

public class NotificationSettingsFragment extends Fragment {

    private CheckBox freq1, freq2, freq3, freq4;
    private static final String TAG = "NOTIFICATIONSETTINGS";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_notifications, null);

        freq1 = (CheckBox) view.findViewById(R.id.notification_checkbox_1);
        freq2 = (CheckBox) view.findViewById(R.id.notification_checkbox_2);
        freq3 = (CheckBox) view.findViewById(R.id.notification_checkbox_3);
        freq4 = (CheckBox) view.findViewById(R.id.notification_checkbox_4);

        freq1.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b){
                    Log.d(TAG, "Freq 1 selected");
                    freq2.setChecked(false);
                    freq3.setChecked(false);
                    freq4.setChecked(false);
                }

            }
        });

        freq2.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b){
                    Log.d(TAG, "Freq 2 selected");
                    freq1.setChecked(false);
                    freq3.setChecked(false);
                    freq4.setChecked(false);
                }

            }
        });

        freq3.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b){
                    Log.d(TAG, "Freq 3 selected");
                    freq1.setChecked(false);
                    freq2.setChecked(false);
                    freq4.setChecked(false);
                }

            }
        });

        freq4.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b){
                    Log.d(TAG, "Freq 4 selected");
                    freq1.setChecked(false);
                    freq2.setChecked(false);
                    freq3.setChecked(false);

                }

            }
        });

        return view;
    }


    public void freqOneNotificationSetUp(){

    }

    public void freqTwoNotificationSetUp(){

    }

    public void freqThreeNotificationSetUp(){

    }

    public void freqFourNotificationSetUp(){

    }

    public void unlockNotificationSetUp(){

    }

}
