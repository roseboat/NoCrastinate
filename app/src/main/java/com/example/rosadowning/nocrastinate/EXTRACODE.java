package com.example.rosadowning.nocrastinate;

public class EXTRACODE {






//    public List<UsageStats> getUsageStatistics(int intervalType) {
//        // Get the app statistics since one year ago from the current time.
//        Calendar cal = Calendar.getInstance();
//        cal.add(Calendar.YEAR, -1);
//
//        List<UsageStats> queryUsageStats = mUsageStatsManager
//                .queryUsageStats(intervalType, cal.getTimeInMillis(),
//                        System.currentTimeMillis());
//
//        if (queryUsageStats.size() == 0) {
//            Log.i(TAG, "The user may not allow the access to apps usage. ");
//            SharedPreferences sharedPreferences = getActivity().getSharedPreferences("StatisticsInfo", Context.MODE_PRIVATE);
//            SharedPreferences.Editor editor = sharedPreferences.edit();
//            editor.putInt("noOfUnlocks", 0);
//            editor.apply();
//            mUsagePopUp.setVisibility(View.VISIBLE);
//            mUsagePopUp.bringToFront();
//            mOpenUsageSettingButton.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    startActivity(new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS));
//                    mUsagePopUp.setVisibility(View.GONE);
//                }
//            });
//        }
//        return queryUsageStats;
//    }
}
