# 🤖 JARVIS - AI Personal Assistant

[![Build Status](https://github.com/yourusername/JARVIS/actions/workflows/android-build.yml/badge.svg)](https://github.com/yourusername/JARVIS/actions/workflows/android-build.yml)

**JARVIS** is a futuristic, production-ready Android AI assistant built with Kotlin. It features voice interaction, AI-powered chat, wake word detection, floating overlay, call handling, and support for multiple AI providers including OpenAI, Gemini, and AgentRouter.

---

## ✨ Features

### 🎤 Dual Interaction System
- **Global Assistant Mode** - Wake word trigger, floating overlay, hands-free voice control
- **In-App Chat Mode** - Modern chat UI with message history, markdown rendering, provider switching

### 🤖 Multi-Provider AI System
- **OpenAI** - GPT-3.5, GPT-4, and custom models
- **Gemini** - Google's Gemini 1.5 Flash and Pro
- **AgentRouter** - Router-based AI orchestration
- **Local LLM** - Import and run GGUF/GGML models locally

### 🗣️ Voice Features
- Speech-to-text recognition
- Text-to-speech responses
- Continuous listening mode
- Customizable wake word ("Hey JARVIS", "Hello JARVIS", custom)
- Voice command parsing for device control

### 💬 Floating Overlay
- Draggable assistant bubble
- Quick access to voice, chat, and controls
- Customizable opacity and size
- Background service with persistent notification

### 📞 Call Handling
- Incoming call detection
- Caller announcement via TTS
- Answer/end calls via voice commands
- Speaker mode toggle
- Accessibility service integration

### 📱 Device Control
- Open apps and settings via voice
- Flashlight control (on/off/toggle)
- Battery status monitoring
- Notification reading
- Web search and music playback
- Weather updates via Open-Meteo API

### 🎨 UI/UX
- Futuristic cinematic design
- Dark theme with neon blue accents
- Glassmorphism card effects
- Smooth animations and transitions
- Material 3 components

### 🔒 Security
- Encrypted shared preferences for API keys
- Runtime permission management
- Secure storage with Android Keystore

---

## 🏗️ Architecture

```
JARVIS/
├── app/
│   └── src/main/
│       ├── java/com/jarvis/assistant/
│       │   ├── ai/              # AI provider system
│       │   │   ├── openai/      # OpenAI integration
│       │   │   ├── gemini/      # Gemini integration
│       │   │   ├── agentrouter/ # AgentRouter integration
│       │   │   └── locallm/     # Local LLM manager
│       │   ├── data/
│       │   │   ├── local/       # Room database, DAOs, entities
│       │   │   ├── remote/      # Retrofit APIs, interceptors
│       │   │   └── repository/  # Data repositories
│       │   ├── di/              # Hilt dependency injection
│       │   ├── service/         # Background services
│       │   ├── ui/              # UI layer
│       │   │   ├── home/        # Home dashboard
│       │   │   ├── chat/        # Chat screen
│       │   │   ├── voice/       # Voice activity
│       │   │   ├── settings/    # Settings screens
│       │   │   └── ...          # Other screens
│       │   └── utils/           # Utilities
│       └── res/                 # Resources, layouts, drawables
├── .devcontainer/               # GitHub Codespaces setup
├── .github/workflows/           # CI/CD pipeline
├── gradle/wrapper/              # Gradle wrapper
├── build.gradle.kts             # Root build config
├── settings.gradle.kts          # Project settings
└── gradle.properties            # Gradle properties
```

### Tech Stack
| Component | Technology |
|-----------|-----------|
| Language | Kotlin 1.9.24 |
| Architecture | MVVM + Repository Pattern |
| UI | XML Layouts + ViewBinding |
| DI | Hilt 2.51.1 |
| Database | Room 2.6.1 |
| Network | Retrofit 2.11 + OkHttp 4.12 |
| Async | Coroutines + Flow |
| Security | EncryptedSharedPreferences |
| CI/CD | GitHub Actions |

---

## 🚀 Quick Start

### Prerequisites
- Android Studio Hedgehog (2023.1.1+) or later
- JDK 17
- Android SDK 34
- Gradle 8.7

### Build & Run

```bash
# Clone the repository
git clone https://github.com/yourusername/JARVIS.git
cd JARVIS

# Build debug APK
./gradlew assembleDebug

# Install on emulator/device
./gradlew installDebug

# Build release APK
./gradlew assembleRelease
```

### GitHub Codespaces

1. Open the repository in Codespaces
2. The devcontainer automatically installs Java 17, Android SDK, and all dependencies
3. Run `./gradlew assembleDebug` to build the APK

```bash
# After Codespaces starts
./gradlew assembleDebug
```

### Configure API Keys

1. Open the app and go to **Settings → AI Providers**
2. Select your preferred provider (OpenAI, Gemini, or AgentRouter)
3. Enter your API key
4. (Optional) Customize the model

> **⚠️ No API keys are hardcoded.** All keys are stored securely using EncryptedSharedPreferences.

---

## 📱 App Screens

| Screen | Description |
|--------|-------------|
| 🏠 **Home Dashboard** | Greeting, quick actions, feature cards |
| 💬 **Chat** | Modern chat UI with AI responses |
| 🎤 **Voice** | Full-screen voice interaction |
| ⚙️ **Settings** | Configuration hub |
| 🤖 **AI Providers** | OpenAI, Gemini, AgentRouter config |
| 🗣️ **Wake Word** | Wake word selection and management |
| 📞 **Voice Settings** | TTS, call handling options |
| 💎 **Overlay** | Floating bubble customization |
| 🌤️ **Weather** | Weather service configuration |
| 📦 **Local Models** | GGUF/GGML model importer |
| 🔐 **Permissions** | Runtime permission management |
| ℹ️ **About** | App information and credits |

---

## 📦 Build Configuration

### Gradle
```kotlin
- Gradle 8.7
- Android Gradle Plugin 8.5.0
- Kotlin 1.9.24
- Compile SDK: 34
- Target SDK: 34
- Min SDK: 26
- Java: 17
```

### Dependencies
```kotlin
- Jetpack (Core, AppCompat, Lifecycle, Navigation)
- Material 3 (Material Design)
- Hilt (Dependency Injection)
- Room (Local Database)
- Retrofit + OkHttp (Networking)
- Gson (JSON Parsing)
- Coroutines + Flow (Async)
- DataStore (Preferences)
- Security Crypto (Encrypted Storage)
- Markwon (Markdown Rendering)
- Lottie (Animations)
- Glide (Image Loading)
```

---

## 🌐 AI Providers

### OpenAI
- Base URL: `https://api.openai.com/`
- Models: `gpt-3.5-turbo`, `gpt-4`, `gpt-4-turbo`, custom
- Features: Chat completions, streaming support ready

### Gemini
- Base URL: `https://generativelanguage.googleapis.com/`
- Models: `gemini-1.5-flash`, `gemini-1.5-pro`, custom
- Features: Content generation

### AgentRouter
- Base URL: `https://agentrouter.org/`
- Features: Router-based AI orchestration
- Custom: Configurable endpoints and models

### Local LLM
- Supports GGUF and GGML model formats
- Import models via file picker
- Model management with size and quantization info
- Inference architecture ready for llama.cpp integration

---

## 🔧 Services

### Active Services
| Service | Purpose | Type |
|---------|---------|------|
| `WakeWordService` | Wake word detection | Foreground (Microphone) |
| `FloatingOverlayService` | Floating assistant bubble | Foreground |
| `BackgroundService` | Persistent background process | Foreground |
| `VoiceCommandService` | Execute device commands | Background |
| `CallHandlingService` | Accessibility call control | Accessibility |
| `NotificationListener` | Read app notifications | Notification Listener |

### Receivers
| Receiver | Purpose |
|----------|---------|
| `BootReceiver` | Auto-start services on device boot |
| `NotificationActionReceiver` | Handle notification actions |

---

## 📁 Project Structure - File Reference

```
JARVIS/
├── build.gradle.kts                    # Root build configuration
├── settings.gradle.kts                 # Project modules
├── gradle.properties                   # Gradle JVM settings
├── gradlew / gradlew.bat               # Gradle wrapper scripts
├── local.properties.example            # SDK path example
│
├── .devcontainer/
│   ├── devcontainer.json               # Codespaces config
│   ├── Dockerfile                      # Android SDK Docker image
│   └── setup.sh                        # Post-create setup
│
├── .github/workflows/
│   └── android-build.yml               # CI/CD pipeline
│
├── app/
│   ├── build.gradle.kts                # App module build
│   ├── proguard-rules.pro              # ProGuard rules
│   └── src/main/
│       ├── AndroidManifest.xml         # App manifest
│       ├── assets/                     # App assets
│       ├── res/                        # Resources
│       └── java/com/jarvis/assistant/  # Source code
│           ├── JarvisApplication.kt    # Application class
│           ├── di/                     # DI modules
│           ├── data/                   # Data layer
│           ├── ai/                     # AI system
│           ├── service/                # Services
│           ├── ui/                     # UI layer
│           └── utils/                  # Utilities
```

---

## 🛡️ License

```
MIT License

Copyright (c) 2024 ANISH SAINI

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files...
```

## 👨‍💻 Developer

**ANISH SAINI**
- Email: anishsaini939@gmail.com

---

## 🤝 Contributing

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

---

## 🙏 Acknowledgments

- **OpenAI** for their powerful language models
- **Google** for Gemini and the Android ecosystem
- **AgentRouter** for AI routing infrastructure
- **Open-Meteo** for free weather API
- **JetBrains** for Kotlin
- All open-source libraries used in this project

---

<p align="center">
  <b>Built with ❤️ using Kotlin & Android</b><br>
  <i>JARVIS - Your Personal AI Assistant</i>
</p>
