# 🎤 Keyword Spotting App

> **Real-time voice keyword detection | On-device ML inference | Zero-cloud privacy**

<div align="center">

![Android](https://img.shields.io/badge/Android-API_23+-4CAF50?style=flat-square&logo=android)
![TensorFlow Lite](https://img.shields.io/badge/TensorFlow-Lite-FF6F00?style=flat-square&logo=tensorflow)
![Kotlin](https://img.shields.io/badge/Java-Android-000?style=flat-square&logo=java)
![License](https://img.shields.io/badge/License-MIT-blue?style=flat-square)

[Features](#-features) • [Quick Start](#-quick-start) • [Architecture](#-architecture) • [Status](#-project-status)

</div>

---

## 📋 What It Does

Continuously monitors ambient audio and detects when specific keywords are spoken. Uses TensorFlow Lite for on-device inference—**no audio leaves your phone**, all processing is local and encrypted.

**Use cases:**
- 🔒 Privacy-aware audio monitoring applications
- 🎯 Smart home wake word detection
- 📊 Local event logging with timestamped history
- 🧠 Learn mobile ML + Service architecture patterns

---

## ✨ Features

| Feature | Details |
|---------|---------|
| 🎯 **Real-time Detection** | 44.1 kHz continuous audio analysis with sub-second latency |
| 🔐 **100% Private** | AES-256-GCM encrypted storage, zero network calls |
| ⚡ **On-Device ML** | TensorFlow Lite quantized model (~5MB) runs directly on phone |
| 📱 **Low Resource** | Works on API 23+ (Android 6+), minimal battery impact |
| 🎨 **Polished UI** | Material Design 3, login/register, event dashboard |
| 📝 **Event History** | Timestamped detections with confidence scores |
| 🔔 **Smart Alerts** | 1.5s cooldown prevents notification spam |

---

## 🏗️ Architecture

### System Flow

```
AudioRecord (44.1 kHz)
       ↓
TensorFlow Lite Inference (speech_commands.tflite)
       ↓
Confidence > 0.82? → Log Event (AES-256 encrypted)
       ↓
Post Local Notification (no network)
       ↓
Dashboard displays encrypted event history
```

### Components

- **LoginActivity** – User authentication with encrypted credentials
- **MainActivity** – Start/stop service, manage permissions (foreground service)
- **SensitiveWordService** – Background foreground service running audio capture + TFLite inference loop
- **DashboardActivity** – View and clear encrypted event log
- **EncryptedSharedPreferences** – AES-256-GCM storage for user data + event logs

---

## 🚀 Quick Start

### Prerequisites

- Android Studio (Arctic Fox+)
- Android device or emulator (API 23+, real device recommended for audio)
- Gradle 8.13+

### Installation

```bash
# Clone
git clone https://github.com/yourusername/KeywordSpottingApp.git
cd KeywordSpottingApp_Lorenzo

# Build & run
./gradlew build
./gradlew installDebug

# Or use Android Studio: File → Open, then Shift+F10 to run
```

### First Run

1. **Register account** – Set a username/password (stored encrypted locally)
2. **Log in** – Enter your credentials
3. **Grant microphone permission** – Tap "Allow" when prompted
4. **Start service** – Tap "Start Voice Service" button
   - You'll see persistent notification: `"Recording active – The service is detecting sensitive words"`
5. **Speak test keyword** – Say "hello", "go", "yes" (speech_commands model vocabulary)
6. **View detection** → Go to Dashboard tab, see detected events with timestamps & confidence

**To stop:** Tap "Stop Voice Service" or swipe notification away

---

## 🔧 Technical Details

### Model & Inference

| Property | Value |
|----------|-------|
| **Model** | `speech_commands.tflite` |
| **Vocabulary** | ~35 English words + background noise |
| **Sample Rate** | 44.1 kHz, mono, 16-bit PCM |
| **Chunk Size** | 1 second (~44,100 samples) |
| **Confidence Threshold** | 0.82 (0–1 scale) |
| **Latency** | ~100–500ms per chunk |

### Decision Logic

```java
if (confidence > 0.82 && !label.equals("background")) {
    saveDetectedEvent(label, confidence);
    postNotification(label);
    Thread.sleep(1500);  // Cooldown to prevent spam
}
```

### Data Storage

All data encrypted with **AES-256-GCM** (AndroidX Security):

| Data | Location | Format |
|------|----------|--------|
| Credentials | `USER_DATA` | Encrypted SharedPreferences |
| Session | `USER_SESSION` | Encrypted SharedPreferences |
| Event Log | `ENCRYPTED_LOG_EVENTS` | `"Word: [label] - [HH:mm:ss]  Confidence: [XX.X]%"` |
| Audio Stream | *Not stored* | Real-time only, discarded post-inference |

---

## 📊 Data Flow Diagram

```
┌─────────────────────────────────────────┐
│         User Starts Service             │
└────────────────┬────────────────────────┘
                 │
       ┌─────────▼─────────┐
       │  Load TFLite Model │
       │ speech_commands    │
       └─────────┬─────────┘
                 │
       ┌─────────▼──────────────┐
       │  Initialize AudioRecord│
       │  44.1 kHz mono 16-bit  │
       └─────────┬──────────────┘
                 │
    ┌────────────▼───────────────┐
    │  Main Inference Loop       │
    │  1. Read 1sec audio chunk  │
    │  2. Classify with TFLite   │
    │  3. Get probabilities      │
    └────────────┬───────────────┘
                 │
      ┌──────────▼──────────┐
      │ Confidence > 0.82?  │
      │ & != "background"?  │
      └──┬─────────────┬────┘
         │             │
        YES           NO
         │             │
    ┌────▼────┐    Skip (loop continues)
    │ Log      │
    │ Notify   │
    │ Cooldown │
    │ 1.5s     │
    └──────────┘
```

---

## 🔒 Security & Privacy

- ✅ **AES-256-GCM encryption** – All stored data encrypted with industry-standard cipher
- ✅ **Zero cloud connectivity** – No network permissions; stays offline-first
- ✅ **Minimal permissions** – Only: `RECORD_AUDIO`, `FOREGROUND_SERVICE`, `POST_NOTIFICATIONS`
- ✅ **Audio never logged** – Only inference results stored, raw audio discarded immediately
- ✅ **EncryptedSharedPreferences** – Secure key derivation, handles crypto correctly

---

## 📁 Project Structure

```
app/src/main/
├── AndroidManifest.xml               # Permissions, services, activities
├── java/com/example/KeyWord_Spotting/
│   ├── LoginActivity.java            # Authentication entry point
│   ├── RegisterActivity.java         # User registration
│   ├── MainActivity.java             # Service control + home
│   ├── DashboardActivity.java        # Event log viewer & clear
│   └── SensitiveWordService.java     # ⭐ Core: audio + TFLite inference
├── assets/
│   └── speech_commands.tflite        # Pre-trained TensorFlow Lite model
└── res/
    ├── layout/                       # XML layouts
    ├── drawable/                     # Icons & graphics
    └── values/                       # Colors, strings, styles
```

---

## ⚙️ Tech Stack

```
Android SDK         minSdk 23 (Android 6+)
┣ TensorFlow Lite   2.13.0         On-device inference
┣ TFLite Audio      0.4.3          Audio classification
┣ AndroidX Security 1.1.0-alpha    Encryption
┣ Material Design   1.12.0         UI components
└ AppCompat         1.7.1          Backward compatibility
```

---

## ⚠️ Limitations

| What | Why | Workaround |
|-----|-----|-----------|
| **Fixed 0.82 confidence threshold** | Model not tunable via UI | Edit `SensitiveWordService.java` line ~120 |
| **English only** | Pre-trained model vocabulary | Train new model or use multi-language model |
| **1.5s cooldown** | Prevent notification spam | Adjust `Thread.sleep(1500)` in service |
| **No cloud sync** | Privacy-first design | Manual export from Dashboard |
| **No auto-restart** | Battery/privacy tradeoff | User manually starts service post-reboot |

---

## 🚦 Project Status

| Aspect | Status | Notes |
|--------|--------|-------|
| **Core Detection** | ✅ Stable | Tested on API 23–36 devices |
| **Encryption** | ✅ Production-ready | AES-256-GCM secure |
| **UI/UX** | ✅ Complete | Material Design 3 |
| **OTA Updates** | ⏳ Not implemented | Requires cloud integration |
| **Analytics** | 📝 Planned | See Future Improvements |
| **Multi-language** | 📝 Planned | Requires model variants |

---

## 📚 Contributing

Contributions welcome! Please:

1. Fork the repository
2. Create a feature branch: `git checkout -b feature/my-feature`
3. Commit changes: `git commit -am 'Add my feature'`
4. Push: `git push origin feature/my-feature`
5. Open a Pull Request

### Areas to Contribute
- [ ] Configurable threshold UI
- [ ] Event log export (CSV/JSON)
- [ ] Dark mode support
- [ ] Model optimization / quantization
- [ ] Unit & integration tests
- [ ] Documentation improvements

---

## 🤝 Support & Questions

**❓ How do I test this?**  
→ See [Quick Start](#-quick-start). Real device recommended; emulator needs audio input mock.

**❓ Can I add custom keywords?**  
→ You'd need to train a new TFLite model. Current model is fixed.

**❓ Why does service restart on app update?**  
→ Android lifecycle; add WorkManager for auto-restart on reboot (see Future Improvements).

**❓ Is this production-ready?**  
→ Core detection works well. No telemetry/updates yet—design for personal/research use.

---

## 📖 License

MIT License – See [LICENSE](LICENSE) file for details.

---

## 👤 Author

**Lorenzo** – Mobile Developer | ML Enthusiast  
→ Portfolio project demonstrating Android services + TensorFlow Lite integration

---

<div align="center">

**[↑ Back to top](#-keyword-spotting-app)**

Made with ❤️ for privacy-aware mobile experiences

</div>
