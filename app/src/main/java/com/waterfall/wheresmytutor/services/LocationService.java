package com.waterfall.wheresmytutor.services;

import static android.content.ContentValues.TAG;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.waterfall.wheresmytutor.R;
import com.waterfall.wheresmytutor.utils.DatabaseController;


public class LocationService extends Service implements android.location.LocationListener {
    private LocationManager locationManager;
    DatabaseController db;
    FirebaseAuth mAuth;

    @Override
    public void onCreate() {
        super.onCreate();
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        FirebaseApp.initializeApp(this);
        db = new DatabaseController(this);
        mAuth = FirebaseAuth.getInstance();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // If Permission is granted, start location updates
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED)
        {
            NotificationChannel channel = new NotificationChannel(getString(R.string.foreground_location_service_channel_ID_text),getString(R.string.foreground_location_service_channel_text), NotificationManager.IMPORTANCE_DEFAULT);
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);

            // Service Notification
            NotificationCompat.Builder builder = new NotificationCompat.Builder(this, getString(R.string.foreground_location_service_channel_ID_text)).setCategory(NotificationCompat.CATEGORY_STATUS)
                    .setContentTitle(getString(R.string.app_name))
                    .setContentText(getString(R.string.broadcasting_your_location_text))
                    .setSmallIcon(R.drawable.ic_location_pin_icon)
                    .setColor(getColor(R.color.primary_pink))
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setAutoCancel(false);

            Notification locationServiceNotification = builder.build();

            startForeground(1, locationServiceNotification); // start service in foreground
            long minTimeBetweenLocationUpdates = 1000L; // 1 Second
            float minDistanceBetweenLocationUpdates = 0; // 0 Meters for Demo only, this would be set to a more reasonable number
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, minTimeBetweenLocationUpdates, minDistanceBetweenLocationUpdates, this);
        }
        return START_STICKY;
    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    @Override
    public void onDestroy() {
        // Stop location updates
        locationManager.removeUpdates(this);
        super.onDestroy();
    }



    @Override
    public void onLocationChanged(@NonNull Location location) {
        Log.e(TAG, "onLocationChanged: " + location); // debug
        // Post location to database.
        db.postTutorLocation(mAuth.getCurrentUser().getUid(), location.getLatitude(), location.getLongitude());

    }
}


