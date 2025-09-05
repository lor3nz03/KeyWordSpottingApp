package com.example.KeyWord_Spotting;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;

import org.tensorflow.lite.support.audio.TensorAudio;
import org.tensorflow.lite.task.audio.classifier.AudioClassifier;
import org.tensorflow.lite.task.audio.classifier.Classifications;
import org.tensorflow.lite.support.label.Category;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class SensitiveWordService extends Service {

    private AudioRecord micInput;
    private boolean isListening = false;
    private final int audioSampleRate = 44100;
    private Thread listeningThread;

    @Override
    public void onCreate() {
        super.onCreate();
        setupNotificationChannelAndForeground();
        startListeningForKeywords();
    }

    // Crea il canale di notifica e avvia il servizio in foreground (necessario per servizi che registrano audio)
    private void setupNotificationChannelAndForeground() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    "keyword_alert_channel",
                    "KeyWord Alerts",
                    NotificationManager.IMPORTANCE_HIGH
            );
            // Registro il canale di notifica
            NotificationManager notifManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            notifManager.createNotificationChannel(channel);
            // Creo notifica persistente
            Notification notification = new NotificationCompat.Builder(this, "keyword_alert_channel")
                    .setContentTitle("Recording active")
                    .setContentText("The service is detecting sensitive words in the background")
                    .setSmallIcon(R.mipmap.ic_launcher_foreground)
                    .build();

            startForeground(1, notification);
        }
    }

    // Avvia un thread che ascolta il microfono e cerca parole chiave usando il modello TensorFlow Lite
    private void startListeningForKeywords() {
        if (androidx.core.content.ContextCompat.checkSelfPermission(
                this, android.Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            Log.e("AudioBackground", "RECORD_AUDIO permission not granted. Service stopped.");
            stopSelf();
            return;
        }

        listeningThread = new Thread(() -> {
            try {
                // Carica il modello di riconoscimento vocale
                AudioClassifier classifier = AudioClassifier.createFromFile(this, "speech_commands.tflite");
                TensorAudio tensorAudio = classifier.createInputTensorAudio();

                // Imposta il formato audio per la registrazione
                AudioFormat format = new AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(audioSampleRate)
                        .setChannelMask(AudioFormat.CHANNEL_IN_MONO)
                        .build();

                micInput = new AudioRecord.Builder()
                        .setAudioSource(MediaRecorder.AudioSource.MIC)
                        .setAudioFormat(format)
                        .build();

                micInput.startRecording();
                isListening = true;

                short[] shortBuffer = new short[audioSampleRate];
                float[] floatBuffer = new float[audioSampleRate];

                // Ciclo che continua finché il servizio è attivo
                while (isListening) {
                    int read = micInput.read(shortBuffer, 0, shortBuffer.length);

                    // Conversione da short a float per TensorFlow Lite (-1,1)
                    for (int i = 0; i < read; i++) {
                        floatBuffer[i] = shortBuffer[i] / 32768.0f;
                    }

                    tensorAudio.load(floatBuffer);
                    List<Classifications> results = classifier.classify(tensorAudio);

                    // Analizza i risultati del modello
                    for (Classifications classification : results) {
                        for (Category category : classification.getCategories()) {
                            String detectedWord = category.getLabel().toLowerCase();
                            float confidence = category.getScore();

                            // Se la parola rilevata ha confidenza alta e non è "background", la salva e mostra notifica
                            if (confidence > 0.82f && !detectedWord.equals("background")) {
                                Log.d("AudioBackground", "Detected word: " + detectedWord + " (" + confidence + ")");
                                saveDetectedEvent(detectedWord,confidence);
                                showDetectedNotification(detectedWord);

                                // Pausa di 1.5 secondi per evitare notifiche duplicate
                                try {
                                    Thread.sleep(1500);
                                } catch (InterruptedException e) {
                                    isListening = false;
                                    break;
                                }
                            }
                        }
                    }
                }

            } catch (IOException e) {
                Log.e("AudioBackground", "Error in audio service", e);
            }
        });

        listeningThread.start();
    }

    // Salva la parola rilevata nella cronologia degli eventi (usa SharedPreferences protette)
    private void saveDetectedEvent(String word,float confidence) {
        try {
            MasterKey masterKey = new MasterKey.Builder(this)
                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                    .build();

            SharedPreferences prefs = EncryptedSharedPreferences.create(
                    this,
                    "ENCRYPTED_LOG_EVENTS",
                    masterKey,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            );

            String previousLog = prefs.getString("event_log_text", "");
            String timestamp = new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(new Date());
            float percent = ((int)(confidence * 1000)) / 10.0f;
            String entry = "Word: " + word + " - " + timestamp + "  Confidence: "+ percent+"%" +"\n";
            String updatedLog = previousLog + entry;

            prefs.edit().putString("event_log_text", updatedLog).apply();

        } catch (Exception e) {
            Log.e("AudioBackground", "Error saving log", e);
        }
    }

    // Mostra una notifica locale quando viene rilevata una parola chiave
    private void showDetectedNotification(String word) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "keyword_alert_channel")
                .setSmallIcon(R.mipmap.ic_launcher_foreground)
                .setContentTitle("Detected word")
                .setContentText("You said: " + word)
                .setPriority(NotificationCompat.PRIORITY_HIGH);

        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        manager.notify((int) System.currentTimeMillis(), builder.build());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        isListening = false;

        // Ferma e rilascia il microfono, interrompe il thread
        if (micInput != null) {
            micInput.stop();
            micInput.release();
        }

        if (listeningThread != null) {
            listeningThread.interrupt();
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        // Questo servizio non supporta il binding
        return null;
    }
}