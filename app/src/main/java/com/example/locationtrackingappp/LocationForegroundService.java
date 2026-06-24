package com.example.locationtrackingappp;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

import com.example.locationtrackingappp.AppDatabase;
import com.example.locationtrackingappp.LocationDao;
import com.example.locationtrackingappp.LocationEntity;
import com.example.locationtrackingappp.MainActivity;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;

import java.util.concurrent.TimeUnit;

public class LocationForegroundService extends Service {

    private static final String TAG = "LocationService";
    private static final String CHANNEL_ID = "location_channel";
    private FusedLocationProviderClient fusedLocationClient;
    private Handler handler;
    private Runnable locationRunnable;
    private AppDatabase db;

    private LocationDao locationDao;

    @Override
    public void onCreate() {
        super.onCreate();
        db = AppDatabase.getDatabase(this);
        locationDao = db.locationDao();
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        createNotificationChannel();
        startForeground(1, getNotification());
        handler = new Handler(Looper.getMainLooper());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        startLocationUpdates();
        return START_STICKY;
    }

    private void startLocationUpdates() {
        int intervalMinutes = getIntervalMinutes();
        long intervalMs = TimeUnit.MINUTES.toMillis(intervalMinutes);

        locationRunnable = () -> {
            getCurrentLocation();
            handler.postDelayed(locationRunnable, intervalMs);
        };
        handler.post(locationRunnable);
    }

    private void getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                .addOnSuccessListener(location -> {
                    if (location != null) {
                        saveLocation(location.getLatitude(), location.getLongitude());
                        Toast.makeText(getApplicationContext(),
                                "Location: " + location.getLatitude() + ", " + location.getLongitude(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void saveLocation(double lat, double lng) {
        LocationEntity entity = new LocationEntity();
        entity.latitude = lat;
        entity.longitude = lng;
        entity.timestamp = System.currentTimeMillis();
        new Thread(() -> locationDao.insert(entity)).start();
    }

    private int getIntervalMinutes() {
        SharedPreferences prefs = getSharedPreferences("LocationPrefs", MODE_PRIVATE);
        return prefs.getInt("interval_minutes", 5);
    }

    private Notification getNotification() {
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent,
                PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);

        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Location Tracking Active")
                .setContentText("Running in background")
                .setSmallIcon(android.R.drawable.ic_menu_mylocation)
                .setContentIntent(pendingIntent)
                .setOngoing(true)
                .build();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID,
                    "Location Service Channel", NotificationManager.IMPORTANCE_LOW);
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) manager.createNotificationChannel(channel);
        }
    }

    @Override
    public void onDestroy() {
        if (handler != null && locationRunnable != null) {
            handler.removeCallbacks(locationRunnable);
        }
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}