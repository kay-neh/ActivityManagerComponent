package com.example.activitymanagercomponent;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;


import java.util.Calendar;

public class ContextManager {

    private static volatile ContextManager instance = null;  // Singleton instance
    private final Context context;

    boolean isRainActive = false;
    boolean isGeofenceActive = false;
    boolean isActivityActive = false;


    public static final int WEATHER_CONTEXT_CODE = 1;
    public static final int GEOFENCE_CONTEXT_CODE = 2;
    public static final int COMMUTE_CONTEXT_CODE = 3;
    public static final int RUNNING_CONTEXT_CODE = 4;
    public static final int WALKING_CONTEXT_CODE = 5;
    public static final int DAY_CONTEXT_CODE = 6;
    public static final int NIGHT_CONTEXT_CODE = 7;

    public static final int UNKNOWN_CONTEXT_CODE = -1;



    private int activeContext = -1; // initial last context

    public ContextManager(Context context) {
        this.context = context.getApplicationContext();
    }

    // Singleton instance of context manager
    public static ContextManager getInstance(Context context) {
        if (instance == null) {
            synchronized (ContextManager.class) {
                if (instance == null) {// Thread safety
                    instance = new ContextManager(context);
                }
            }
        }
        return instance;
    }
    // Initialize the context manager
    public void initialize() {
        setInitialDayOrNightContext();
        startDayNightDetection();
        startActivityRecognition();
        startWeatherDetection();
        startGeofenceDetection();
    }


    public void deInitialize() {

        stopActivityRecognition();
        stopWeatherDetection();
        stopGeofenceDetection();
    }



    // Start background services for activity, weather, and geofence detection
    private void startActivityRecognition() {
        Intent intent = new Intent(context, ActivityRecognitionService.class);
        context.getApplicationContext().startForegroundService(intent);

    }

    private void stopActivityRecognition() {
        Intent intent = new Intent(context, ActivityRecognitionService.class);
        context.getApplicationContext().stopService(intent);
    }

    private void startWeatherDetection() { /* WeatherService.start(context); */ }
    private void stopWeatherDetection() {
    }

    private void startGeofenceDetection() { /* GeofenceService.start(context); */ }
    private void stopGeofenceDetection() {
    }

    // Determine the initial context (day or night)
    private void setInitialDayOrNightContext() {
        // check if it is day or night
        // perform switching (change text)
        switchContext(getDayOrNightContext());
    }

    private void startDayNightDetection() {
        Intent intent = new Intent(context, NightDayService.class);
        context.startService(intent);
    }

    private void stopDayNightDetection() {
        Intent intent = new Intent(context, NightDayService.class);
        context.stopService(intent);
    }


    // Set or clear activity contexts,
    // 5 - walking
    // 4 - Running
    // 3 - Commute (Vehicle or Bicycle)
    public void notifyActivityEnterState(int ACTIVITY_CONTEXT_CODE) {
        if(ACTIVITY_CONTEXT_CODE != UNKNOWN_CONTEXT_CODE) {
            isActivityActive = true;
            updateContext(ACTIVITY_CONTEXT_CODE);
        }
    }

    public void notifyActivityExitState() {
        isActivityActive = false;
        checkForDefaultContext();
    }

    // Track rain and geofence states
    public void notifyWeatherEnterState() {
        isRainActive = true;
        updateContext(WEATHER_CONTEXT_CODE);  // Rain context
    }

    public void notifyWeatherExitState() {
        isRainActive = false;
        checkForDefaultContext();
    }

    public void notifyGeofenceEnterState() {
        isGeofenceActive = true;
        updateContext(GEOFENCE_CONTEXT_CODE);  // Rain context
    }

    public void notifyGeofenceExitState() {
        isGeofenceActive = false;
        checkForDefaultContext();
    }

    public void notifyDayNightState(boolean isDayNight) {
        if(isDayNight){
            updateContext(DAY_CONTEXT_CODE);
        }else{
            updateContext(NIGHT_CONTEXT_CODE);
        }
    }


    // Prioritize context switching
    private void updateContext(int newContext) {
        // check if old context == new context
        // compare for priority
        if (activeContext != newContext){
            int currentPriority = getContextPriority(activeContext);
            int newPriority = getContextPriority(newContext);

            if (newPriority > currentPriority) {
                // do switching
                switchContext(newContext);

            }
        }
    }

    // Determine priority levels for different contexts
    private int getContextPriority(int context) {
        int priorityContext;
        switch (context) {
            case WEATHER_CONTEXT_CODE:
                priorityContext =  4;  // Rain (highest priority)
                break;

            case GEOFENCE_CONTEXT_CODE:
                priorityContext =  3;  // Geofence
                break;

            case COMMUTE_CONTEXT_CODE:
            case RUNNING_CONTEXT_CODE:
            case WALKING_CONTEXT_CODE:
                priorityContext =  2;  // Activity
                break;

            case DAY_CONTEXT_CODE:
            case NIGHT_CONTEXT_CODE:
                priorityContext =  1;  // Day/Night (lowest priority)
                break;

            default: priorityContext = -1;
        }
        return priorityContext;
    }

    // Default to day or night if no other context is active
    private void checkForDefaultContext() {
        if (!isRainActive && !isGeofenceActive && !isActivityActive) {
            switchContext(getDayOrNightContext());
        }
    }

    // Get day or night based on current time
    private int getDayOrNightContext() {
        // replace with my impl
        NightDayService nightDayService = new NightDayService();
        return nightDayService.getTimeOfDay() ? DAY_CONTEXT_CODE : NIGHT_CONTEXT_CODE;  // Day or Night
//        int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
//        return (hour >= 6 && hour < 18) ? DAY_CONTEXT_CODE : NIGHT_CONTEXT_CODE;  // Day or Night
    }

    // Handle the actual context switching
    private void switchContext(int context) {
        String contextText = "";
        Log.d("Pre Switch", String.valueOf(context));


        switch (context) {
            case WEATHER_CONTEXT_CODE: contextText = "Playing RainDrops"; break;
            case GEOFENCE_CONTEXT_CODE: contextText = "Playing Geofence Playlist"; break;
            case COMMUTE_CONTEXT_CODE: contextText = "Playing Commute Playlist"; break;
            case RUNNING_CONTEXT_CODE: contextText = "Playing TrackFit Playlist"; break;
            case WALKING_CONTEXT_CODE: contextText = "Playing ToneWalker Playlist"; break;
            case DAY_CONTEXT_CODE: contextText = "Playing Daylight Playlist"; break;
            case NIGHT_CONTEXT_CODE: contextText = "Playing Night Night Playlist"; break;

        }

        Intent switchIntent = new Intent("SWITCH_UPDATE");
        switchIntent.putExtra("Context", contextText);

        LocalBroadcastManager.getInstance(this.context).sendBroadcast(switchIntent);
        Log.d("Context Switch", "Success - Context switched");
        Log.d("Context text", contextText);

        activeContext = context; // update last context.

    }

}

