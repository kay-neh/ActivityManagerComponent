package com.example.activitymanagercomponent;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

public class NightDayReceiver extends BroadcastReceiver {

    public static final String ACTION_DAY_NIGHT = "UPDATE_DAY_NIGHT";
    public static final String EXTRA_TIME_OF_DAY = "TIME_OF_DAY";

    public static final int DAY_ID = 0;
    public static final int NIGHT_ID = 1;

    @Override
    public void onReceive(Context context, Intent intent) {
        int id = intent.getIntExtra(Constants.ALARM_ID, -1);
        // test
        Log.e("NightDay Broadcast", String.valueOf(id));

        boolean isDayNight = id == DAY_ID;

        Intent dayNightIntent = new Intent(ACTION_DAY_NIGHT);
        dayNightIntent.putExtra(EXTRA_TIME_OF_DAY, isDayNight);
        LocalBroadcastManager.getInstance(context).sendBroadcast(dayNightIntent);

    }

}
