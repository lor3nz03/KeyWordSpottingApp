package com.example.KeyWord_Spotting;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;

public class LoginActivity extends AppCompatActivity {

    private EditText edtUsername, edtPassword;
    private MasterKey encryptionKey;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        showRegistrationToastIfNeeded();
        setupRegisterLink();
        initializeInputFields();
        checkAlreadyLoggedIn();
    }

    // Mostra un toast se l'utente arriva dalla registrazione
    private void showRegistrationToastIfNeeded() {
        if (getIntent().getBooleanExtra("registered", false)) {
            Toast.makeText(this, "Registration successful! Please log in.", Toast.LENGTH_SHORT).show();
        }
    }

    // Imposta il link per andare alla schermata di registrazione
    private void setupRegisterLink() {
        TextView txtGoToRegister = findViewById(R.id.goToRegisterText);
        txtGoToRegister.setOnClickListener(v -> {
            startActivity(new Intent(this, RegisterActivity.class));
        });
    }

    // Inizializza i campi di input per username e password
    private void initializeInputFields() {
        edtUsername = findViewById(R.id.username);
        edtPassword = findViewById(R.id.password);
    }

    // Controlla se l'utente è già loggato e lo manda alla home
    private void checkAlreadyLoggedIn() {
        try {
            encryptionKey = new MasterKey.Builder(this)
                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                    .build();

            SharedPreferences sessionPrefs = EncryptedSharedPreferences.create(
                    this,
                    "USER_SESSION",
                    encryptionKey,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            );

            if (sessionPrefs.getBoolean("isLoggedIn", false)) {
                startActivity(new Intent(this, MainActivity.class));
                finish();
            }
        } catch (Exception e) {
            Toast.makeText(this, "Session error", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    // Gestisce il login quando si preme il bottone
    public void login(View view) {
        String inputUser = edtUsername.getText().toString().trim();
        String inputPass = edtPassword.getText().toString().trim();

        try {
            SharedPreferences userPrefs = EncryptedSharedPreferences.create(
                    this,
                    "USER_DATA",
                    encryptionKey,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            );

            String storedUser = userPrefs.getString("username", null);
            String storedPass = userPrefs.getString("password", null);

            if (inputUser.equals(storedUser) && inputPass.equals(storedPass)) {
                SharedPreferences sessionPrefs = EncryptedSharedPreferences.create(
                        this,
                        "USER_SESSION",
                        encryptionKey,
                        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
                );

                sessionPrefs.edit()
                        .putBoolean("isLoggedIn", true)
                        .putString("username", inputUser)
                        .apply();

                startActivity(new Intent(this, MainActivity.class));
                finish();

            } else {
                Toast.makeText(this, "Invalid username or password", Toast.LENGTH_SHORT).show();
            }

        } catch (Exception e) {
            Toast.makeText(this, "Login error", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }
}