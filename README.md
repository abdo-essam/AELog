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
| 🧩 **Plugin System** | Extend with custom debug panels through modular dependencies |
| 📱 **Adaptive Layout** | Bottom sheet on phones, dialog on tablets |
| 🔌 **Zero Release Overhead**| Disable with a single flag — no runtime cost |
| 🍎 **Multiplatform** | Android, iOS, Desktop (JVM), Web (WASM) |

## 📦 Installation

AEDevLens is fully modularized. Include only the plugins you need to keep your app light!

### Kotlin Multiplatform

```kotlin
// build.gradle.kts (shared module)
kotlin {
    sourceSets {
        commonMain.dependencies {
            // Core UI + Logging plugin (Required)
            implementation("io.github.abdo-essam:devlens:1.0.0")
            
            // Optional Plugins
            implementation("io.github.abdo-essam:devlens-network:1.0.0")
            implementation("io.github.abdo-essam:devlens-analytics:1.0.0")
        }
    }
}
```

### Version Catalog

```toml
[versions]
devlens = "1.0.0"

[libraries]
devlens-core      = { module = "io.github.abdo-essam:devlens", version.ref = "devlens" }
devlens-network   = { module = "io.github.abdo-essam:devlens-network", version.ref = "devlens" }
devlens-analytics = { module = "io.github.abdo-essam:devlens-analytics", version.ref = "devlens" }
```

## 🚀 Quick Start

### 1. Initialize & Install Plugins

Best called early in your platform-specific entry points (e.g. `Application.onCreate` for Android, or main `ViewController` for iOS):

```kotlin
DevLensSetup.init(
    plugins = listOf(
        LogsPlugin(),      // Default built-in logs
        NetworkPlugin(),   // Network inspector
        AnalyticsPlugin()  // Analytics tracker
    )
)
```

### 2. Wrap Your App with UI Provider

```kotlin
@Composable
fun App(debugMode: Boolean) {
    AEDevLensProvider(
        inspector = AEDevLens.default,
        enabled = debugMode, // ← disables UI overhead in release builds
        uiConfig = DevLensUiConfig(
            showFloatingButton = true, // Enables the 'bug' overlay button
            enableLongPress = true,    // Show panel on 3-finger long press
        )
    ) {
        MaterialTheme {
            YourAppContent()
        }
    }
}
```

### 3. Log Data to Plugins

Use the global static APIs corresponding to your installed plugins:

```kotlin
// 1. Logs API
DevLens.i("HomeScreen", "App launched!")
DevLens.e("Database", "Failed to clear cache", exception)

// 2. Network API
NetworkApi.logRequest(method = "GET", url = "https://api.example.com/users", headers = mapOf("Auth" to "Bearer 123"))
NetworkApi.logResponse(url = "https://api.example.com/users", statusCode = 200, responseBody = "{ \"count\": 2 }")

// 3. Analytics API
AnalyticsApi.logEvent("item_added_to_cart", properties = mapOf("id" to "123", "price" to "29.99"))
```

### 4. Open DevLens

Three ways to open the inspector:
1. Tap the floating **bug button** (bottom-right corner)
2. Long-press with multiple fingers anywhere on screen (if enabled)
3. Programmatically: `LocalAEDevLensController.current.show()`

## 🧩 Modularity & Available Plugins

| Module / Plugin | Class | Description |
|--------|------|-------------|
| `:devlens` | `LogsPlugin` | Log viewer with severity filters (ALL / VERBOSE / DEBUG / INFO / WARN / ERROR) |
| `:devlens-network` | `NetworkPlugin` | HTTP inspector with method badges, status filtering (2xx / 4xx / 5xx) and full body view |
| `:devlens-analytics` | `AnalyticsPlugin` | Analytics tracker separating Screens / Events with expandable properties |

## 🔨 Custom Plugins

Create your own debug panel (e.g., a Database Inspector or Feature Flags toggler) in 3 steps:

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
DevLensSetup.init(plugins = listOf(LogsPlugin(), FeatureFlagsPlugin()))
```

📖 See the [Custom Plugins Guide](https://abdo-essam.github.io/AEDevLens/custom-plugins) for the full API reference.

## 🔗 Logging Integrations

AEDevLens works seamlessly with your existing logging infrastructures (like Kermit or Napier). Just forward your logs to the APIs.

```kotlin
class DevLensKermitWriter : LogWriter() {
    override fun log(severity: Severity, message: String, tag: String, throwable: Throwable?) {
        DevLens.log(
            severity = severity.toDevLensLogSeverity(),
            tag = tag,
            message = buildString {
                append(message)
                throwable?.let { append("\n${it.stackTraceToString()}") }
            }
        )
    }
}
```

📖 See the [Logging Integrations Guide](https://abdo-essam.github.io/AEDevLens/integrations) for more examples.

## 🏗️ Architecture

The SDK follows an encapsulated `Model-Store-API-UI` pattern, making plugins 100% reactive, modular, and thread-safe.

```text
┌─────────────────────────────────────────────────┐
│              AEDevLensProvider                   │  Compose wrapper
│  ┌───────────────────────────────────────────┐  │
│  │            AEDevLens (Core)               │  │  Plugin engine
│  │  ┌─────────┐ ┌─────────┐ ┌────────────┐  │  │
│  │  │  Logs   │ │ Network │ │ Analytics  │  │  │  Plugins
│  │  │ Plugin  │ │ Plugin  │ │  Plugin    │  │  │
│  │  └────┬────┘ └────┬────┘ └─────┬──────┘  │  │
│  │       │           │            │          │  │
│  │  ┌────┴────┐ ┌────┴────┐ ┌────┴──────┐  │  │
│  │  │LogStore │ │NetStore │ │AnalyticsStore│  │ Data layer
│  │  └─────────┘ └─────────┘ └───────────┘  │  │
│  └───────────────────────────────────────────┘  │
└─────────────────────────────────────────────────┘
```

## 📋 Requirements

| Platform | Minimum Version |
|----------|----------------|
| Android | API 24 (Android 7.0) |
| iOS | 15.0 |
| Kotlin | 2.1.10+ |
| Compose Multiplatform | 1.7.3+ |

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
Copyright 2026 Abdo Essam

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
