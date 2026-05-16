# Configuration

AELog is designed to be "zero-config" by default, but provides hooks to customize both data collection and UI behavior.

## Core Configuration (Data)

Pass a `LogConfig` object to `AELog.init()` to control background behavior:

```kotlin
AELog.init(
    LogPlugin(),
    config = LogConfig(
        maxLogEntries = 1000,
        // Custom dispatcher for background processing
        dispatcher = Dispatchers.Default,
        // Catch and report internal SDK errors
        errorHandler = { throwable -> FirebaseCrashlytics.getInstance().recordException(throwable) }
    )
)
```

## UI Configuration

Configure the overlay behavior via `LogProvider`:

```kotlin
LogProvider(
    enabled = BuildConfig.DEBUG,
    uiConfig = UiConfig(
        showFloatingButton = true,
        enableLongPress = true,
        floatingButtonOffset = 100.dp,
        overlayMode = OverlayMode.BottomSheet
    )
) {
    MyApp()
}
```

> [!TIP]
> You no longer need to pass `instance = AELog.default`. `LogProvider` picks up the initialized shared instance automatically.
