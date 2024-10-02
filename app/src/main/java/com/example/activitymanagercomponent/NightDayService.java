package com.example.activitymanagercomponent;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.android.gms.location.DetectedActivity;

import java.util.Calendar;

public class NightDayService extends Service {

    private boolean isDayNight;

    private ContextManager contextManager;

    private final BroadcastReceiver dayNightUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (NightDayReceiver.ACTION_DAY_NIGHT.equals(intent.getAction())) {
                boolean dayNightType = intent.getBooleanExtra(NightDayReceiver.EXTRA_TIME_OF_DAY, false);

                Log.e("dayNightType", String.valueOf(dayNightType));
                // update context manager accordingly for still or new detection
                contextManager.notifyDayNightState(dayNightType);
            }
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        contextManager = ContextManager.getInstance(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //getTimeOfDay();
        createAlarm(this);
        IntentFilter filter = new IntentFilter(NightDayReceiver.ACTION_DAY_NIGHT);
        LocalBroadcastManager.getInstance(this).registerReceiver(dayNightUpdateReceiver, filter);
        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public boolean getTimeOfDay() {
        Calendar c = Calendar.getInstance();
        int timeOfDay = c.get(Calendar.HOUR_OF_DAY);
        if(timeOfDay >= 6 && timeOfDay < 18){
            // DAY
            isDayNight = true;
        }else{
            // NIGHT
            isDayNight = false;
        }
        return isDayNight;
    }

    // Create Night/Day triggers
    public void createAlarm(Context context) {
        //Trigger time array
        int[] triggerTime = {6,18};

        for(int i = 0; i < triggerTime.length; i++){
            //Create the time of day you would like it to go off. Use a calendar
            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.HOUR_OF_DAY, triggerTime[i]);
            calendar.set(Calendar.MINUTE, 0);

            if(calendar.before(Calendar.getInstance())) {
                calendar.add(Calendar.DATE, 1);
            }

            //Create an intent that points to the receiver. The system will notify the app about the current time, and send a broadcast to the app
            Intent intent = new Intent(context, NightDayReceiver.class);
            intent.putExtra(Constants.ALARM_ID,i);
            PendingIntent pendingIntent;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                pendingIntent = PendingIntent.getBroadcast(context, i,intent, PendingIntent.FLAG_MUTABLE);
            }
            else
            {
                pendingIntent = PendingIntent.getBroadcast(context, i,intent, PendingIntent.FLAG_UPDATE_CURRENT);
            }

            //Create an alarm manager
            AlarmManager alarmManager = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
            //initialize the alarm by using inexactrepeating. This allows the system to scheduler your alarm at the most efficient time around your
            //set time, it is usually a few seconds off your requested time.
            // you can also use setExact however this is not recommended. Use this only if it must be done then.
            //alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC,calendar.getTimeInMillis(), pendingIntent);
            alarmManager.setInexactRepeating(AlarmManager.RTC,calendar.getTimeInMillis(),AlarmManager.INTERVAL_DAY, pendingIntent);
            Log.e("Created alarm " +i, "TRUE");

        }

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(dayNightUpdateReceiver);
    }
}
