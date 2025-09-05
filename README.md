# Keyword Spotting App

## Descrizione

Keyword Spotting App è un’applicazione Android progettata per rilevare automaticamente la presenza di parole chiave sensibili nell’audio ambientale. Utilizza modelli di machine learning (TensorFlow Lite) per analizzare l’audio in tempo reale e avvisa l’utente quando vengono individuate parole sensibili. L’obiettivo è aiutare l’utente a proteggere la propria privacy e prevenire la divulgazione involontaria di dati personali.

---

## Funzionalità Principali

- **Rilevamento parole chiave sensibili** tramite microfono.
- **Notifiche in tempo reale** in caso di rischio.
- **Dashboard con cronologia eventi** rilevati.
- **Gestione sicura dei dati**: i log sono salvati solo in locale e cifrati.
- **Suggerimenti per la privacy** sempre visibili nel footer.

---

## Struttura del Progetto

- **`src/main/java/`**  
  Contiene il codice sorgente Java/Kotlin dell’app, suddiviso per attività, servizi e logica di rilevamento.

- **`src/main/res/`**  
  Risorse grafiche e layout XML:
  - **`layout/`**: layout delle schermate (es. `activity_dashboard.xml`)
  - **`drawable/`**: immagini e forme grafiche (es. icone, sfondi, bordi)
  - **`values/`**: colori, stringhe, stili

- **`src/main/assets/`**  
  Modelli TensorFlow Lite (`.tflite`) e altri asset necessari per l’inferenza.

- **`src/main/AndroidManifest.xml`**  
  Definisce permessi (microfono, servizio in foreground) e componenti principali dell’app.

---

## Requisiti

- **Android Studio** (versione Arctic Fox o superiore consigliata)
- **Dispositivo Android** (o emulatore con supporto audio)
- **Permessi**: microfono e servizio in foreground

---

## Installazione e Avvio

1. **Clona o scarica il progetto** su Android Studio.
2. **Assicurati che il modello `.tflite` sia presente** in `src/main/assets/`.
3. **Compila e avvia l’app** su un dispositivo reale (consigliato) o emulatore.
4. **Concedi i permessi richiesti** all’avvio (microfono).
5. **Utilizza la dashboard** per monitorare la cronologia delle rilevazioni e ricevere notifiche in tempo reale.

---

## Utilizzo

- L’app analizza l’audio ambientale in background.
- Quando viene rilevata una parola chiave sensibile, riceverai una notifica.
- Puoi consultare la cronologia degli eventi dalla dashboard.
- Nel footer troverai sempre consigli utili per la protezione della privacy.

---

## Note sulla Privacy

- Nessun dato audio viene salvato o inviato su server esterni.
- I log delle rilevazioni sono cifrati e salvati solo in locale.

---

## Personalizzazione

- Per aggiungere nuovi modelli `.tflite`, inseriscili nella cartella `assets` e aggiorna il codice di caricamento.
