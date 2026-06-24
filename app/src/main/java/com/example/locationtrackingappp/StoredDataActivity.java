package com.example.locationtrackingappp;

import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class StoredDataActivity extends AppCompatActivity {

    private RecyclerView locationsRecyclerView;
    private Button deleteAllButton;
    private Button refreshButton;
    private ImageButton backButton;
    private LocationAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stored_data);

        locationsRecyclerView = findViewById(R.id.locationsRecyclerView);
        deleteAllButton = findViewById(R.id.deleteAllButton);
        refreshButton = findViewById(R.id.refreshButton);
        backButton = findViewById(R.id.backButton);

        // Back button functionality
        backButton.setOnClickListener(v -> finish());

        adapter = new LocationAdapter(new ArrayList<>());
        locationsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        locationsRecyclerView.setAdapter(adapter);

        refreshButton.setOnClickListener(v -> loadLocationData());
        deleteAllButton.setOnClickListener(v -> deleteAllData());

        loadLocationData();
    }

    private void loadLocationData() {
        // Run database query on background thread
        new Thread(() -> {
            try {
                AppDatabase db = AppDatabase.getDatabase(this);
                List<LocationEntity> locations = db.locationDao().getAllLocations();

                // Update UI on main thread
                runOnUiThread(() -> {
                    if (locations != null && !locations.isEmpty()) {
                        adapter.updateList(locations);
                    } else {
                        Toast.makeText(this, "No location data available", Toast.LENGTH_SHORT).show();
                    }
                });
            } catch (Exception e) {
                runOnUiThread(() ->
                    Toast.makeText(this, "Error loading data: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
            }
        }).start();
    }

    private void deleteAllData() {
        new Thread(() -> {
            try {
                AppDatabase db = AppDatabase.getDatabase(this);
                db.locationDao().deleteAll();

                runOnUiThread(() -> {
                    Toast.makeText(this, "All data deleted", Toast.LENGTH_SHORT).show();
                    adapter.updateList(new ArrayList<>());
                });
            } catch (Exception e) {
                runOnUiThread(() ->
                    Toast.makeText(this, "Error deleting data: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
            }
        }).start();
    }
}


