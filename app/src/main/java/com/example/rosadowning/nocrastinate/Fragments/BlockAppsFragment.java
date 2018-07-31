package com.example.rosadowning.nocrastinate.Fragments;

import android.content.ReceiverCallNotAllowedException;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;

import com.example.rosadowning.nocrastinate.Adapters.AppStatisticsAdapter;
import com.example.rosadowning.nocrastinate.Adapters.BlockAppsAdapter;
import com.example.rosadowning.nocrastinate.DataModels.CustomUsageStats;
import com.example.rosadowning.nocrastinate.R;

import java.util.List;

import jp.wasabeef.recyclerview.adapters.AlphaInAnimationAdapter;

public class BlockAppsFragment extends Fragment {

    private RecyclerView mRecyclerView;
    private BlockAppsAdapter mBlockAppsAdapter;
    private List<CustomUsageStats> appsList;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_block_apps, null);

        mRecyclerView = (RecyclerView) view.findViewById(R.id.block_apps_recycler_view);
        mBlockAppsAdapter = new BlockAppsAdapter(getContext());
        mRecyclerView.scrollToPosition(0);
        mRecyclerView.setAdapter(new AlphaInAnimationAdapter(mBlockAppsAdapter));

        return view;
    }





}
