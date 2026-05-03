# Plugins Overview

AELog uses a **plugin architecture**. Each plugin adds a dedicated tab to the AELog overlay panel. The library itself is just a host — plugins are what make it useful.

## How Plugins Work

When you call `AELog.init(...)`, you pass a list of plugins. The library:

1. **Installs** each plugin and calls its `onAttach()` lifecycle hook
2. **Renders** `UIPlugin` instances as tabs in the overlay panel
3. **Manages** the lifecycle — calling `onOpen()` / `onClose()` as the panel opens and closes
4. **Provides** each plugin with access to a coroutine scope and sibling plugins via `PluginContext`

## Built-in Plugins

| Plugin | Module | Type | Description |
|--------|--------|------|-------------|
| `LogPlugin` | `:log` | `UIPlugin` | Real-time log viewer with level filtering (VERBOSE / DEBUG / INFO / WARN / ERROR) |
| `NetworkPlugin` | `:log-network` | `UIPlugin` | HTTP traffic inspector with method badges, status filtering, and full body view |
| `AnalyticsPlugin` | `:log-analytics` | `UIPlugin` | Tracks analytics events and screen views with expandable custom properties |

## Plugin Types

### UIPlugin
Renders a **tab with header and content areas** inside the AELog panel. Ideal for visually inspecting data (logs, network calls, feature flags, database records, etc.).

### DataPlugin
A **headless background collector** with no UI. It feeds data to a `UIPlugin` or runs standalone. Use it for crash collectors, performance samplers, or anything that needs to observe data without a dedicated panel.

## Installing Plugins

```kotlin
// In your app entry point (Application.onCreate on Android, etc.)
AELog.init(
    LogPlugin(),
    NetworkPlugin(),
    AnalyticsPlugin(),
    MyCustomPlugin()   // ← your own plugin
)
```

## Plugin Lifecycle

```text
install() → onAttach(context)
                  ↓
           ┌→ onOpen()  ←┐
           │      ↓       │   (user opens/closes AELog panel)
           └─ onClose() ──┘
                  ↓
             onDetach()  ← uninstall()
```

Each hook has a clear responsibility:

| Hook | When Called | Use For |
|------|-------------|---------|
| `onAttach(context)` | Once, on install | Store `PluginContext`, start background work |
| `onOpen()` | Every time panel opens | Resume UI updates, refresh data |
| `onClose()` | Every time panel closes | Pause expensive operations |
| `onClear()` | User taps "Clear All" | Reset stored data |
| `onDetach()` | On uninstall | Cancel coroutines, release resources |

## Creating Custom Plugins

See the [Custom Plugins](custom-plugins.md) guide for a full walkthrough including code examples, lifecycle details, and best practices.
