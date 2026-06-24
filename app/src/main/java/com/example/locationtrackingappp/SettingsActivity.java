package com.example.locationtrackingappp;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class SettingsActivity extends AppCompatActivity {

    private EditText intervalEditText;
    private Button saveButton;
    private ImageButton backButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        intervalEditText = findViewById(R.id.intervalEditText);
        saveButton = findViewById(R.id.saveButton);
        backButton = findViewById(R.id.backButton);

        // Back button functionality
        backButton.setOnClickListener(v -> finish());

        SharedPreferences prefs = getSharedPreferences("LocationPrefs", MODE_PRIVATE);
        int current = prefs.getInt("interval_minutes", 5);
        intervalEditText.setText(String.valueOf(current));

        saveButton.setOnClickListener(v -> {
            String str = intervalEditText.getText().toString().trim();
            if (!str.isEmpty()) {
                int interval = Integer.parseInt(str);
                if (interval > 0) {
                    prefs.edit().putInt("interval_minutes", interval).apply();
                    Toast.makeText(this, "Interval saved: " + interval + " min", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}