# Quick Start

Add AELog to your project and initialize it in just a few lines.

## 1. Initialize

Call this early in your app lifecycle (e.g., `Application.onCreate` on Android or `Main` on iOS):

```kotlin
AELog.init(
    LogPlugin(),
    NetworkPlugin(),
    AnalyticsPlugin(),
    CrashPlugin()
)
```

## 2. Wrap your App

Wrap your root Composable with `LogProvider`. This enables the floating debug button and the overlay panel.

```kotlin
// In your App composable (commonMain)
LogProvider(
    enabled = true // Tie this to your build variant (e.g., debug=true, release=false)
) {
    // Your app content
    MyApp()
}
```

## 3. Start Logging

Use the discoverable static APIs. AELog handles everything else behind the scenes.

```kotlin
// 1. Logs (with automatic tag derivation from the calling class)
AELog.log.i("App launched!")
AELog.log.d("Auth", "Token refreshed") // Or specify an explicit tag

// 2. Network (handled automatically via interceptors)
// val client = HttpClient { install(KtorInterceptor) }

// 3. Analytics
AELog.analytics.logEvent("button_tap")

// 4. Crashes (manually record non-fatal exceptions)
try {
    performDangerousTask()
} catch (t: Throwable) {
    AELog.crashes.recordNonFatal(t)
}
```

## Show / Hide the Overlay

The overlay can be triggered by:
- **Floating Button** — enabled by default
- **Long-Press Gesture** — enabled by default
- **Programmatically** via `LocalLogController.current.show()` / `hide()`
