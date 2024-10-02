package com.example.activitymanagercomponent;

import static com.example.activitymanagercomponent.ContextManager.COMMUTE_CONTEXT_CODE;
import static com.example.activitymanagercomponent.ContextManager.RUNNING_CONTEXT_CODE;
import static com.example.activitymanagercomponent.ContextManager.UNKNOWN_CONTEXT_CODE;
import static com.example.activitymanagercomponent.ContextManager.WALKING_CONTEXT_CODE;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.android.gms.location.ActivityRecognition;
import com.google.android.gms.location.ActivityRecognitionClient;
import com.google.android.gms.location.DetectedActivity;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

public class ActivityRecognitionService extends Service {

    private ActivityRecognitionClient activityRecognitionClient;
    private static final int CUSTOM_REQUEST_CODE = 201;
    private static final String CHANNEL_ID = "my_broadcast_channel";
    private static final int NOTIFICATION_ID = 300;
    ContextManager contextManager;


    private final BroadcastReceiver uiUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (ActivityRecognitionReceiver.ACTION_UPDATE_MANAGER.equals(intent.getAction())) {
                int activityType = intent.getIntExtra(ActivityRecognitionReceiver.EXTRA_ACTIVITY_TYPE, -1);

                // update context manager accordingly for still or new detection
                if(activityType == DetectedActivity.STILL){contextManager.notifyActivityExitState();}
                else{contextManager.notifyActivityEnterState(getContextCode(activityType));}

//                showNotification(context, "Broadcast", "New Detection");
            }
        }
    };

    private int getContextCode(int activityType) {
        int activityNum = UNKNOWN_CONTEXT_CODE; // default value

        switch (activityType) {
            case DetectedActivity.IN_VEHICLE:
            case DetectedActivity.ON_BICYCLE:
                activityNum = COMMUTE_CONTEXT_CODE;
                break;


            case DetectedActivity.RUNNING:
                activityNum = RUNNING_CONTEXT_CODE;
                break;

            case DetectedActivity.WALKING:
                activityNum = WALKING_CONTEXT_CODE;
                break;

        }

        return activityNum;
    }


    @Override
    public void onCreate() {
        super.onCreate();
        activityRecognitionClient = ActivityRecognition.getClient(this);
        contextManager = ContextManager.getInstance(this);
        // Start the service as a foreground service with a notification
        startForeground(NOTIFICATION_ID, createNotification());  // Ensures the service runs in the background

        Log.d("RecognitionOnCreate", "Called");

    }


    @SuppressLint("MissingPermission")
    private void requestActivityUpdates() {
        // Request activity updates every 10 seconds
        activityRecognitionClient.requestActivityUpdates(10000, getPendingIntent(this))
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Log.d("Update", "Success - Request Update");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d("Update", "Failed - Request Update");
                    }
                });
    }

    private PendingIntent getPendingIntent(Context context){
        Intent intent = new Intent(context, ActivityRecognitionReceiver.class);
        return PendingIntent.getBroadcast(context, CUSTOM_REQUEST_CODE,
                intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_MUTABLE);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        requestActivityUpdates();
        IntentFilter filter = new IntentFilter(ActivityRecognitionReceiver.ACTION_UPDATE_MANAGER);
        LocalBroadcastManager.getInstance(this).registerReceiver(uiUpdateReceiver, filter);
        Log.d("RecognitionOnStartCommand", "Called");

        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private Notification createNotification() {
        // Create the notification channel for Android O and higher
        NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                "Activity Recognition Service",
                NotificationManager.IMPORTANCE_DEFAULT);  // Default importance to avoid disturbing the user
        NotificationManager manager = getSystemService(NotificationManager.class);
        manager.createNotificationChannel(channel);

        // Build and return the notification
        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Activity Recognition Running")
                .setContentText("Detecting activities in the background")
                .setSmallIcon(android.R.drawable.ic_dialog_info)  // Replace with your app icon
                .setPriority(NotificationCompat.PRIORITY_LOW)  // Ensure the notification doesn't disturb the user too much
                .build();
    }

    
    @SuppressLint("MissingPermission")
    @Override
    public void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(uiUpdateReceiver);
        // Deregister activity recognition updates
        activityRecognitionClient
                .removeActivityUpdates(getPendingIntent(this))
                .addOnSuccessListener(aVoid -> getPendingIntent(this).cancel())
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to deregister transition updates",
                        Toast.LENGTH_SHORT).show());

        Log.d("RecognitionOnDestroy", "Called");

    }
}

