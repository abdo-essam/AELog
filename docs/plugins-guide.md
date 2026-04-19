# Plugins Overview

AEDevLens uses a plugin architecture. Each plugin adds a tab to the DevLens overlay.

## Built-in Plugins

| Plugin | Type | Description |
|--------|------|-------------|
| `LogsPlugin` | `UIPlugin` | Real-time log viewer with level filtering |
| `NetworkPlugin` | `UIPlugin` | HTTP traffic inspector with detailed request/response views |
| `AnalyticsPlugin` | `UIPlugin` | Tracks events and screen views with custom payload properties |

## Plugin Types

### UIPlugin
Renders a tab with header and content areas. Ideal for inspecting data visually.

### DataPlugin
Background data collector with no UI. Feeds data to a `UIPlugin`.

## Installing Plugins

```kotlin
DevLensSetup.init(
    plugins = listOf(
        LogsPlugin(),
        NetworkPlugin(),
        AnalyticsPlugin(),
        MyCustomPlugin()
    )
)
```

## Creating Custom Plugins

See the [Custom Plugins](custom-plugins.md) guide for a full walkthrough.
