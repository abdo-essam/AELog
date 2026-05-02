# Plugins Overview

AELog uses a plugin architecture. Each plugin adds a tab to the AELog overlay.

## Built-in Plugins

| Plugin | Type | Description |
|--------|------|-------------|
| `LogPlugin` | `UIPlugin` | Real-time log viewer with level filtering |
| `NetworkPlugin` | `UIPlugin` | HTTP traffic inspector with detailed request/response views |
| `AnalyticsPlugin` | `UIPlugin` | Tracks events and screen views with custom payload properties |

## Plugin Types

### UIPlugin
Renders a tab with header and content areas. Ideal for inspecting data visually.

### DataPlugin
Background data collector with no UI. Feeds data to a `UIPlugin`.

## Installing Plugins

```kotlin
AELogSetup.init(
    plugins = listOf(
        LogPlugin(),
        NetworkPlugin(),
        AnalyticsPlugin(),
        MyCustomPlugin()
    )
)
```

## Creating Custom Plugins

See the [Custom Plugins](custom-plugins.md) guide for a full walkthrough.
