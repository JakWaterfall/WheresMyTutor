package com.waterfall.wheresmytutor.services;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.IBinder;
import android.text.format.DateUtils;

import androidx.annotation.Nullable;

import com.waterfall.wheresmytutor.activities.NoNetworkActivity;

public class NetworkService extends Service {

    private PendingIntent networkCheckIntent;
    private ConnectivityManager connectivityManager;
    private AlarmManager alarmManager;

    @Override
    public void onCreate() {
        super.onCreate();
        connectivityManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        Intent intent = new Intent(this, NetworkService.class);
        networkCheckIntent = PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_MUTABLE);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(!isNetworkConnected())
        {
            stopSelf();
            Intent noNetworkIntent = new Intent(this, NoNetworkActivity.class);
            noNetworkIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(noNetworkIntent);
        }
        long currentTimeInMillis = System.currentTimeMillis();
        long nextNetworkCheckTimeInMillis = currentTimeInMillis + 5 * DateUtils.SECOND_IN_MILLIS; // check every 5 seconds
        alarmManager.set(AlarmManager.RTC, nextNetworkCheckTimeInMillis, networkCheckIntent);

        return START_STICKY;
    }

    private boolean isNetworkConnected() {
        NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        alarmManager.cancel(networkCheckIntent);

    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
