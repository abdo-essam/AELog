# Configuration

Configure AELog structure via `AELogSetup.init()` and behaviour via `AELogUiConfig`.

## Options

| Option | Type | Default | Description |
|--------|------|---------|-------------|
| `enabled` | `Boolean` | `false` | Enable or disable the overlay entirely (passed into `AELogProvider`) |
| `showFloatingButton` | `Boolean` | `true` | Show floating bug button overlay |
| `enableLongPress` | `Boolean` | `true` | Show panel on 3-finger long press |

## Example

```kotlin
// Data config
AELogSetup.init(
    config = AELogConfig(maxLogEntries = 1000)
)

// UI config
AELogProvider(
    inspector = AELog.default,
    enabled = BuildConfig.DEBUG,
    uiConfig = AELogUiConfig(
        showFloatingButton = true,
        enableLongPress = true
    )
) {
    MyApp()
}
```
