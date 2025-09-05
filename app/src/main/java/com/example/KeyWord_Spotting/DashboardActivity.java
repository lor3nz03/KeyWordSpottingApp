package com.example.KeyWord_Spotting;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;

public class DashboardActivity extends AppCompatActivity {

    private TextView txtChronology;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        initializeUI();
        displayEventLog();
    }

    // Inizializza il riferimento alla TextView della cronologia
    private void initializeUI() {
        txtChronology = findViewById(R.id.chronologyView);
    }

    // Mostra la cronologia degli eventi salvati
    @SuppressLint("SetTextI18n")
    private void displayEventLog() {
        try {
            MasterKey masterKey = new MasterKey.Builder(this)
                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                    .build();

            SharedPreferences logPrefs = EncryptedSharedPreferences.create(
                    this,
                    "ENCRYPTED_LOG_EVENTS",
                    masterKey,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            );

            String logText = logPrefs.getString("event_log_text", "");

            if (logText.isEmpty()) {
                txtChronology.setText("No events found.");
            } else {
                txtChronology.setText(logText);
            }

        } catch (Exception e) {
            e.printStackTrace();
            txtChronology.setText("Error reading log.");
        }
    }

    // Cancella la cronologia degli eventi
    @SuppressLint("SetTextI18n")
    public void clearLog(View view) {
        try {
            MasterKey masterKey = new MasterKey.Builder(this)
                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                    .build();

            SharedPreferences logPrefs = EncryptedSharedPreferences.create(
                    this,
                    "ENCRYPTED_LOG_EVENTS",
                    masterKey,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            );

            String logText = logPrefs.getString("event_log_text", "");

            if (logText.isEmpty()) {
                Toast.makeText(this, "Nothing to clear: no events found", Toast.LENGTH_SHORT).show();
                return;
            }

            logPrefs.edit().remove("event_log_text").apply();
            txtChronology.setText("No events found.");
            Toast.makeText(this, "History cleared", Toast.LENGTH_SHORT).show();

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error while clearing history", Toast.LENGTH_SHORT).show();
        }
    }
}