package com.example.KeyWord_Spotting;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;

import com.google.android.material.button.MaterialButton;

public class MainActivity extends AppCompatActivity {

    private MaterialButton btnServiceToggle;
    private boolean isServiceRunning = false;
    private SharedPreferences securePrefs;
    private TextView txtWelcome;
    private String username;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        initializeUI();
        requestMicrophonePermission();
        setupNotificationChannel();
        retrieveUserSession();
        updateWelcomeMessage();
        restoreServiceState();
        setupServiceButtonListener();
        restartServiceIfNeeded();
    }

    // Inizializza i riferimenti agli elementi della UI
    private void initializeUI() {
        btnServiceToggle = findViewById(R.id.BtnStartStop);
        txtWelcome = findViewById(R.id.welcomeText);
    }

    // Chiede il permesso per il microfono se necessario
    private void requestMicrophonePermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, 101);
        }
    }

    // Crea il canale di notifica per Android 8+
    private void setupNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    "keyword_alert_channel",
                    "Keyword Alerts",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Notifications for detected sensitive keywords");
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    // Recupera la sessione utente dalle preferenze protette
    private void retrieveUserSession() {
        username = getIntent().getStringExtra("username");
        try {
            MasterKey masterKey = new MasterKey.Builder(this)
                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                    .build();

            securePrefs = EncryptedSharedPreferences.create(
                    this,
                    "USER_SESSION",
                    masterKey,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            );

            if (username == null) {
                username = securePrefs.getString("username", "user");
            }
        } catch (Exception e) {
            Toast.makeText(this, "Error while retrieving user", Toast.LENGTH_SHORT).show();
            username = "user";
        }
    }

    // Aggiorna il messaggio di benvenuto
    private void updateWelcomeMessage() {
        txtWelcome.setText("Welcome back " + username);
    }

    // Aggiorna lo stato del servizio e il bottone
    private void restoreServiceState() {
        isServiceRunning = securePrefs.getBoolean("isServiceRunning", false);
        updateServiceButtonStyle(isServiceRunning);
    }

    // Imposta il comportamento del bottone servizio
    private void setupServiceButtonListener() {
        btnServiceToggle.setOnClickListener(v -> {
            Intent serviceIntent = new Intent(MainActivity.this, SensitiveWordService.class);

            if (!isServiceRunning) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    startForegroundService(serviceIntent);
                } else {
                    startService(serviceIntent);
                }
                Toast.makeText(MainActivity.this, "Voice service started", Toast.LENGTH_SHORT).show();
                isServiceRunning = true;
            } else {
                stopService(serviceIntent);
                Toast.makeText(MainActivity.this, "Voice service stopped", Toast.LENGTH_SHORT).show();
                isServiceRunning = false;
            }

            securePrefs.edit().putBoolean("isServiceRunning", isServiceRunning).apply();
            updateServiceButtonStyle(isServiceRunning);
        });
    }

    // Riavvia il servizio se era attivo
    private void restartServiceIfNeeded() {
        if (isServiceRunning) {
            Intent serviceIntent = new Intent(MainActivity.this, SensitiveWordService.class);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(serviceIntent);
            } else {
                startService(serviceIntent);
            }
        }
    }

    // Aggiorna lo stile del bottone in base allo stato del servizio (@SuppressLint per compatibilità colori)
    @SuppressLint("UseCompatLoadingForColorStateLists")
    private void updateServiceButtonStyle(boolean isRunning) {
        if (isRunning) {
            btnServiceToggle.setText(getString(R.string.stop_background));
            btnServiceToggle.setIcon(getDrawable(R.drawable.ic_stop));
            btnServiceToggle.setBackgroundTintList(getResources().getColorStateList(R.color.stop_button));
            btnServiceToggle.setStrokeWidth(0);
            btnServiceToggle.setIconTint(getColorStateList(R.color.white));
            btnServiceToggle.setTextColor(getColor(R.color.white));
        } else {
            btnServiceToggle.setText(getString(R.string.start_background));
            btnServiceToggle.setIcon(getDrawable(R.drawable.ic_play));
            btnServiceToggle.setBackgroundTintList(getResources().getColorStateList(R.color.primary_color));
            btnServiceToggle.setStrokeWidth(0);
            btnServiceToggle.setIconTint(getColorStateList(R.color.white));
            btnServiceToggle.setTextColor(getColor(R.color.white));
        }
    }

    // Esegue il logout e torna al login
    public void logout(View view) {
        try {
            if (securePrefs != null) {
                securePrefs.edit().clear().apply();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }

    // Apre la dashboard degli eventi
    public void openDashboard(View view) {
        startActivity(new Intent(this, DashboardActivity.class));
    }

    @Override
    protected void onResume() {
        super.onResume();
        requestNotificationPermission();
    }

    // Chiede il permesso per le notifiche su Android 13+
    private void requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS}, 102);
            }
        }
    }
}
