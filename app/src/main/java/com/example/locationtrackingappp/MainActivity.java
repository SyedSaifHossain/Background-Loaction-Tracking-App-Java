package com.example.locationtrackingappp;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private EditText etInterval;
    private RecyclerView recyclerView;
    private LocationAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // Initialize UI components
        etInterval = findViewById(R.id.etInterval);
        Button btnStart = findViewById(R.id.btnStart);
        Button btnStop = findViewById(R.id.btnStop);
        Button btnRefresh = findViewById(R.id.btnRefresh);

        // Setup RecyclerView
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new LocationAdapter(null);
        recyclerView.setAdapter(adapter);

        checkPermissions();

        // Start background location tracking
        btnStart.setOnClickListener(v -> {
            int interval = 5; // default 5 minutes
            try {
                interval = Integer.parseInt(etInterval.getText().toString().trim());
            } catch (Exception e) {
                // Keep default value if input is invalid
            }

            Intent serviceIntent = new Intent(this, LocationForegroundService.class);
            serviceIntent.putExtra("interval", interval);
            startService(serviceIntent);
            Toast.makeText(this, "Background Tracking Started", Toast.LENGTH_SHORT).show();
        });

        // Stop background location tracking
        btnStop.setOnClickListener(v -> {
            stopService(new Intent(this, LocationForegroundService.class));
            Toast.makeText(this, "Tracking Stopped", Toast.LENGTH_SHORT).show();
        });

        // Load saved locations from database
        btnRefresh.setOnClickListener(v -> loadLocationsFromDatabase());
    }

    /**
     * Loads all saved locations from Room Database and updates RecyclerView
     */
    private void loadLocationsFromDatabase() {
        new Thread(() -> {
            List<LocationEntity> list = AppDatabase.getInstance(this)
                    .locationDao().getAllLocations();

            runOnUiThread(() -> {
                if (adapter != null) {
                    adapter.updateList(list);
                }

                if (list.isEmpty()) {
                    Toast.makeText(this, "No locations saved yet", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, list.size() + " locations loaded from database", Toast.LENGTH_SHORT).show();
                }
            });
        }).start();
    }

    private void checkPermissions() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.POST_NOTIFICATIONS
                    }, 100);
        }
    }

    // Handle permission result
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 100) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permission granted. You can start tracking.", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Location permission is required for tracking", Toast.LENGTH_LONG).show();
            }
        }
    }
}