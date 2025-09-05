package com.example.KeyWord_Spotting;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;

import android.content.SharedPreferences;

public class RegisterActivity extends AppCompatActivity {

    private EditText edtUser, edtPass;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        initializeInputFields();
        setupLoginRedirect();
    }

    // Inizializza i campi di input per username e password
    private void initializeInputFields() {
        edtUser = findViewById(R.id.username);
        edtPass = findViewById(R.id.password);
    }

    // Imposta il link per tornare al login
    private void setupLoginRedirect() {
        TextView txtGoToLogin = findViewById(R.id.goToLoginText);
        if (txtGoToLogin != null) {
            txtGoToLogin.setOnClickListener(v -> {
                startActivity(new Intent(this, LoginActivity.class));
                finish();
            });
        }
    }

    // Gestisce la registrazione quando si preme il bottone
    public void register(View view) {
        String userValue = edtUser.getText().toString().trim();
        String passValue = edtPass.getText().toString().trim();

        if (!userValue.isEmpty() && !passValue.isEmpty()) {
            if (userValue.length() < 4) {
                Toast.makeText(this, "Username must be at least 4 characters", Toast.LENGTH_SHORT).show();
                return;
            }
            if (passValue.length() < 6) {
                Toast.makeText(this, "Password must be at least 6 characters ", Toast.LENGTH_SHORT).show();
                return;
            }
            if (!passValue.matches(".*\\d.*") || !passValue.matches(".*[!@#$%^&*()_+=\\-{}\\[\\]:;\"'<>,.?/].*")) {
                Toast.makeText(this, "Password must be Contain at least one number and one special character", Toast.LENGTH_SHORT).show();
                return;
            }
            if (userValue.equals(passValue)) {
                Toast.makeText(this, "Username and password must be different", Toast.LENGTH_SHORT).show();
                return;
            } else {
                saveUserCredentials(userValue, passValue);
            }
        } else {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
        }
    }

    // Salva le credenziali dell'utente nelle preferenze protette e torna al login
    private void saveUserCredentials(String username, String password) {
        try {
            MasterKey masterKey = new MasterKey.Builder(this)
                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                    .build();

            SharedPreferences userPrefs = EncryptedSharedPreferences.create(
                    this,
                    "USER_DATA",
                    masterKey,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            );

            userPrefs.edit()
                    .putString("username", username)
                    .putString("password", password)
                    .apply();

            Toast.makeText(this, "Registration completed!", Toast.LENGTH_SHORT).show();

            Intent intent = new Intent(this, LoginActivity.class);
            intent.putExtra("registered", true);
            startActivity(intent);
            finish();

        } catch (Exception e) {
            Toast.makeText(this, "Error during registration", Toast.LENGTH_SHORT).show();
        }
    }
}