package com.example.locationtrackingappp;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ServiceInfo;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import com.google.android.gms.location.*;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class LocationForegroundService extends Service {

    private static final String CHANNEL_ID = "location_channel";
    private static final int NOTIFICATION_ID = 1;

    private FusedLocationProviderClient fusedLocationClient;
    private Handler handler = new Handler(Looper.getMainLooper());
    private Runnable locationRunnable;
    private int intervalMinutes = 5; // default

    @Override
    public void onCreate() {
        super.onCreate();
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        createNotificationChannel();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && intent.hasExtra("interval")) {
            intervalMinutes = intent.getIntExtra("interval", 5);
        }

        startForegroundService();
        startLocationUpdates();
        return START_STICKY;
    }

    private void startForegroundService() {
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent,
                PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);

        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Location Tracker Running")
                .setContentText("Tracking location in background...")
                .setSmallIcon(android.R.drawable.ic_menu_mylocation)
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setOngoing(true)
                .build();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION);
        } else {
            startForeground(NOTIFICATION_ID, notification);
        }
    }

    private void startLocationUpdates() {
        // Remove any existing callbacks first
        if (locationRunnable != null) {
            handler.removeCallbacks(locationRunnable);
        }

        locationRunnable = new Runnable() {
            @Override
            public void run() {
                getCurrentLocation();
                handler.postDelayed(this, intervalMinutes * 60 * 1000L); // interval in milliseconds
            }
        };
        handler.post(locationRunnable); // Start immediately
    }

    private void getCurrentLocation() {
        // Check permission before requesting location
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        // Updated way to create LocationRequest (not deprecated)
        LocationRequest locationRequest = new LocationRequest.Builder(
                Priority.PRIORITY_HIGH_ACCURACY, 10000)
                .build();

        fusedLocationClient.getCurrentLocation(locationRequest.getPriority(), null)
                .addOnSuccessListener(location -> {
                    if (location != null) {
                        String time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                                .format(new Date());

                        String message = "Location: " + location.getLatitude() + ", " + location.getLongitude();

                        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();

                        // Save to Room Database
                        LocationEntity entity = new LocationEntity(
                                location.getLatitude(),
                                location.getLongitude(),
                                time);

                        new Thread(() -> {
                            AppDatabase.getInstance(getApplicationContext())
                                    .locationDao()
                                    .insert(entity);
                        }).start();
                    }
                })
                .addOnFailureListener(e -> {
                    // Optional: Log error
                });
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Location Tracking Service",
                    NotificationManager.IMPORTANCE_LOW);
            channel.setDescription("Used for background location tracking");

            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (handler != null && locationRunnable != null) {
            handler.removeCallbacks(locationRunnable);
        }
    }
}