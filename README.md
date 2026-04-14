<p align="center">
  <img src="docs/assets/logo.png" width="600" alt="AEDevLens Banner" />
</p>

<h1 align="center">AEDevLens</h1>

<p align="center">
  <strong>Extensible on-device dev tools for Kotlin Multiplatform</strong>
  <br />
  A mini Flipper for KMP — inspect logs, network, and more with a beautiful Compose UI.
</p>

<p align="center">
  <a href="https://central.sonatype.com/artifact/io.github.abdo-essam/devlens">
    <img src="https://img.shields.io/maven-central/v/io.github.abdo-essam/devlens?style=flat-square&color=BF3547" alt="Maven Central" />
  </a>
  <a href="https://github.com/abdo-essam/AEDevLens/actions/workflows/ci.yml">
    <img src="https://img.shields.io/github/actions/workflow/status/abdo-essam/AEDevLens/ci.yml?branch=main&style=flat-square" alt="CI" />
  </a>
  <a href="https://codecov.io/gh/abdo-essam/AEDevLens">
    <img src="https://img.shields.io/codecov/c/github/abdo-essam/AEDevLens?style=flat-square&color=00B894" alt="Code Coverage" />
  </a>
  <a href="https://kotlin.github.io/binary-compatibility-validator/">
    <img src="https://img.shields.io/badge/API-stable-blue?style=flat-square" alt="API Stability" />
  </a>
  <a href="LICENSE">
    <img src="https://img.shields.io/github/license/abdo-essam/AEDevLens?style=flat-square" alt="License" />
  </a>
  <a href="https://kotlinlang.org">
    <img src="https://img.shields.io/badge/Kotlin-2.3.0-7F52FF?style=flat-square&logo=kotlin" alt="Kotlin" />
  </a>
</p>

<p align="center">
  <a href="#-features">Features</a> •
  <a href="#-installation">Installation</a> •
  <a href="#-quick-start">Quick Start</a> •
  <a href="#-plugins">Plugins</a> •
  <a href="#-custom-plugins">Custom Plugins</a> •
  <a href="https://abdo-essam.github.io/AEDevLens/">Documentation</a>
</p>

---

<p align="center">
  <img src="docs/assets/demo-light.gif" width="280" alt="Light Mode Demo" />
  &nbsp;&nbsp;&nbsp;
  <img src="docs/assets/demo-dark.gif" width="280" alt="Dark Mode Demo" />
</p>

## ✨ Features

| Feature | Description |
|---------|-------------|
| 🔍 **Log Inspector** | Search, filter, and copy logs with syntax-highlighted JSON |
| 🌐 **Network Viewer** | HTTP request/response inspection with method badges |
| 📊 **Analytics Tracker** | Monitor analytics events in real-time |
| 🎨 **Beautiful UI** | Material3 design with light/dark mode support |
| 🧩 **Plugin System** | Extend with custom debug panels |
| 📱 **Adaptive Layout** | Bottom sheet on phones, dialog on tablets |
| 🔌 **Zero Release Overhead**| Disable with a single flag — no runtime cost |
| 🍎 **Multiplatform** | Android, iOS, Desktop (JVM) |

## 📦 Installation

### Kotlin Multiplatform

```kotlin
// build.gradle.kts (shared module)
kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation("io.github.abdo-essam:devlens:1.0.0")
        }
    }
}
```

### Android Only

```kotlin
// build.gradle.kts (app module)
dependencies {
    debugImplementation("io.github.abdo-essam:devlens-android:1.0.0")
}
```

### Version Catalog

```toml
[versions]
devlens = "1.0.0"

[libraries]
devlens = { module = "io.github.abdo-essam:devlens", version.ref = "devlens" }
```

## 🚀 Quick Start

### 1. Wrap Your App

```kotlin
@Composable
fun App() {
    AEDevLensProvider(
        enabled = BuildConfig.DEBUG  // ← zero overhead in release
    ) {
        MaterialTheme {
            YourAppContent()
        }
    }
}
```

### 2. Log Messages

```kotlin
val inspector = AEDevLens.default

// Simple logging
inspector.log(LogLevel.INFO, "HomeScreen", "User opened home screen")
inspector.log(LogLevel.ERROR, "API", "Failed to fetch user: 401 Unauthorized")

// Network logging (auto-detected)
inspector.log(LogLevel.DEBUG, "HTTP", "--> GET https://api.example.com/users")
inspector.log(LogLevel.DEBUG, "HTTP", "<-- 200 OK https://api.example.com/users")

// Analytics events
inspector.log(LogLevel.INFO, "Analytics", "screen_view: HomeScreen")
```

### 3. Open DevLens

Three ways to open the inspector:
1. Tap the floating bug button (bottom-right corner)
2. Long-press anywhere on screen
3. Programmatically: `LocalAEDevLensController.current.show()`

## 🔧 Configuration

```kotlin
val inspector = AEDevLens.create(
    AEDevLensConfig(
        maxLogEntries = 1000,            // Default: 500
        showFloatingButton = true,        // Default: true
        floatingButtonAlignment = Alignment.BottomEnd,
        enableLongPress = true,           // Default: true
        colorScheme = yourCustomScheme,   // Default: built-in theme
    )
)

AEDevLensProvider(inspector = inspector) {
    YourApp()
}
```

## 🧩 Plugins

### Built-in Plugins
| Plugin | Type | Description |
|--------|------|-------------|
| LogsPlugin | UI | Log viewer with search, filter, copy |

### Coming Soon
| Plugin | Type | Description |
|--------|------|-------------|
| NetworkPlugin | UI | Ktor/OkHttp request inspector |
| PreferencesPlugin | UI | SharedPreferences / DataStore viewer |
| CrashPlugin | Data | Crash reporting and history |

## 🔨 Custom Plugins

Create your own debug panel in 3 steps:

```kotlin
class FeatureFlagsPlugin : UIPlugin {
    override val id = "feature_flags"
    override val name = "Flags"
    override val icon = Icons.Default.Flag

    private val _badgeCount = MutableStateFlow<Int?>(null)
    override val badgeCount: StateFlow<Int?> = _badgeCount

    override fun onAttach(inspector: AEDevLens) {
        // Initialize your plugin
    }

    @Composable
    override fun Content(modifier: Modifier) {
        // Your Compose UI here
        LazyColumn(modifier = modifier) {
            items(flags) { flag ->
                FlagRow(flag)
            }
        }
    }
}

// Install it
val inspector = AEDevLens.default
inspector.install(FeatureFlagsPlugin())
```

📖 See the [Custom Plugins Guide](https://abdo-essam.github.io/AEDevLens/custom-plugins) for the full API reference.

## 🔗 Kermit Integration

Bridge your existing Kermit logs into AEDevLens:

```kotlin
// In your DI / initialization
val inspector = AEDevLens.default

Logger.addLogWriter(AEDevLensLogWriter(inspector))
```

```kotlin
class AEDevLensLogWriter(
    private val inspector: AEDevLens
) : LogWriter() {
    override fun log(severity: Severity, message: String, tag: String, throwable: Throwable?) {
        inspector.log(
            level = severity.toDevLensLevel(),
            tag = tag,
            message = buildString {
                append(message)
                throwable?.let { append("\n${it.stackTraceToString()}") }
            }
        )
    }
}
```

## 🏗️ Architecture

```text
┌─────────────────────────────────────────────────┐
│              AEDevLensProvider                   │  Compose wrapper
│  ┌───────────────────────────────────────────┐  │
│  │            AEDevLens (Core)               │  │  Plugin engine
│  │  ┌─────────┐ ┌─────────┐ ┌────────────┐  │  │
│  │  │  Logs   │ │ Network │ │  Custom    │  │  │  Plugins
│  │  │ Plugin  │ │ Plugin  │ │  Plugin    │  │  │
│  │  └────┬────┘ └────┬────┘ └─────┬──────┘  │  │
│  │       │           │            │          │  │
│  │  ┌────┴────┐ ┌────┴────┐ ┌────┴──────┐  │  │
│  │  │LogStore │ │NetStore │ │ YourStore │  │  │  Data layer
│  │  └─────────┘ └─────────┘ └───────────┘  │  │
│  └───────────────────────────────────────────┘  │
└─────────────────────────────────────────────────┘
```

## 📋 Requirements

| Platform | Minimum Version |
|----------|----------------|
| Android | API 24 (Android 7.0) |
| iOS | 15.0 |
| Kotlin | 2.3.0 |
| Compose Multiplatform | 1.9.3 |

## 🤝 Contributing

Contributions are welcome! Please read the [Contributing Guide](CONTRIBUTING.md) first.

```bash
git clone https://github.com/abdo-essam/AEDevLens.git
cd AEDevLens
./gradlew build
./gradlew allTests
```

## 📄 License

```text
Copyright 2025 Abdo Essam

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0
```

## 💖 Acknowledgements

- Jetpack Compose — UI toolkit
- Kotlin Multiplatform — Cross-platform
- Flipper — Inspiration for the plugin architecture
- Chucker — Inspiration for network inspection
