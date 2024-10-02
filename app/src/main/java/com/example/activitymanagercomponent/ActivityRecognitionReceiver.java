package com.example.activitymanagercomponent;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;

public class ActivityRecognitionReceiver extends BroadcastReceiver {
    public static final String ACTION_UPDATE_MANAGER = "UPDATE_MANAGER";
    public static final String EXTRA_ACTIVITY_TYPE = "activity_type";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (ActivityRecognitionResult.hasResult(intent)) {
            ActivityRecognitionResult result = ActivityRecognitionResult.extractResult(intent);
            if(result != null) {
                DetectedActivity detectedActivity = result.getMostProbableActivity();
                int activityType = detectedActivity.getType();
                int confidence = detectedActivity.getConfidence();

                logDetectedActivity(activityType, confidence);

                Intent uiIntent = new Intent(ACTION_UPDATE_MANAGER);
                uiIntent.putExtra(EXTRA_ACTIVITY_TYPE, activityType);
                LocalBroadcastManager.getInstance(context).sendBroadcast(uiIntent);
            }

        }
    }

    // debug log helper class

    public static void logDetectedActivity(int activityType, int confidence) {
        String activityName = getActivityName(activityType);
        Log.d("ActivityDetection", "Activity: " + activityName + ", Confidence: " + confidence + "%");
    }

    public static String getActivityName(int activityType) {
        switch (activityType) {
            case DetectedActivity.IN_VEHICLE:
                return "In Vehicle";
            case DetectedActivity.ON_BICYCLE:
                return "On Bicycle";
            case DetectedActivity.RUNNING:
                return "Running";
            case DetectedActivity.STILL:
                return "Still";
            case DetectedActivity.WALKING:
                return "Walking";
            default:
                return "Unknown Activity";
        }
    }
}
