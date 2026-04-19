# Configuration

Configure AEDevLens structure via `DevLensSetup.init()` and behaviour via `DevLensUiConfig`.

## Options

| Option | Type | Default | Description |
|--------|------|---------|-------------|
| `enabled` | `Boolean` | `false` | Enable or disable the overlay entirely (passed into `AEDevLensProvider`) |
| `showFloatingButton` | `Boolean` | `true` | Show floating bug button overlay |
| `enableLongPress` | `Boolean` | `true` | Show panel on 3-finger long press |

## Example

```kotlin
// Data config
DevLensSetup.init(
    config = DevLensConfig(maxLogEntries = 1000)
)

// UI config
AEDevLensProvider(
    inspector = AEDevLens.default,
    enabled = BuildConfig.DEBUG,
    uiConfig = DevLensUiConfig(
        showFloatingButton = true,
        enableLongPress = true
    )
) {
    MyApp()
}
```
