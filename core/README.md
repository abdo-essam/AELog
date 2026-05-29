# AELog Core Module

Welcome to the **AELog Core Module**! This is the engine room of the AELog SDK—a strictly feature-agnostic, lightweight, and highly modular Kotlin Multiplatform (KMP) foundation. 

This guide is designed to help onboarding developers quickly understand the architectural principles, directory layout, plugin creation system, and internal communication patterns of AELog.

---

## 1. Directory & Package Structure

The core module is organized into five platform source sets matching standard KMP layout rules, under the base package `com.ae.log`:

```
core/
├── src/
│   ├── commonMain/kotlin/com/ae/log/
│   │   ├── AELog.kt         # The main facade and LogInspector controller
│   │   ├── AELogLifecycle.kt # Library-wide lifecycle manager
│   │   ├── config/          # SDK global configurations (LogConfig)
│   │   ├── event/           # Central EventBus and standard event payloads
│   │   ├── plugin/          # Plugin contracts and managers
│   │   ├── storage/         # High-level memory & file persistence layers
│   │   ├── ui/              # Compose Multiplatform core layout & overlays
│   │   └── utils/           # Time, ID, and low-level FileOperations platform utilities
│   ├── androidMain/         # Android specific integrations (LogViewerActivity)
│   ├── iosMain/             # iOS file system and native integrations
│   ├── jvmMain/             # JVM file system integrations
│   └── commonTest/          # Multiplatform unit & integration tests
```

---

## 2. The Core Plugin Architecture

AELog is designed so that all actual logger features (standard text logs, network interceptors, crash capture, etc.) are implemented as decoupled **Plugins**. The core does not know about these features; it merely coordinates them.

There are two ways to write plugins:
1. **Headless Plugin**: Implement the base `Plugin` interface directly to collect or process data in the background (e.g. crash collection, network interceptors).
2. **UI Plugin**: Implement the `UIPlugin` interface to add a visual panel (tab) to the Compose overlay.

### 2.1. Plugin Lifecycle Hooks

Every plugin can hook into the following lifecycle hooks:

| Lifecycle Method | Description |
|:---|:---|
| `onAttach(context: PluginContext)` | Invoked when the plugin is registered. The plugin receives a local `CoroutineScope` and access to the shared `EventBus` and `LogConfig`. |
| `onDetach()` | Called when the plugin is uninstalled; its local coroutine scope is cancelled automatically. |
| `onClear()` | Triggered when `AELog.clearAll()` is executed. The plugin must clear its stored records/caches. |
| `onMigrateFrom(oldPlugin: Plugin)` | Triggered during `AELog.configure` hot-swaps, allowing you to transfer accumulated state from the previous instance of this plugin. |

> [!TIP]
> **Reactive Lifecycles**: Stale, imperative lifecycles like `onStart`, `onOpen`, and `onClose` are removed to favor a reactive approach. If your plugin needs to respond to overlay or app state changes, simply observe them from the `EventBus` in `onAttach`:
> ```kotlin
> context.collectEvents<PanelOpenedEvent> { /* Overlay opened */ }
> context.collectEvents<PanelClosedEvent> { /* Overlay closed */ }
> ```

---

## 3. Creating a Custom Plugin

### 3.1. Headless Plugin Example

Here is how you create a simple data collection plugin:

```kotlin
import com.ae.log.plugin.Plugin
import com.ae.log.plugin.PluginContext

class CustomDataPlugin : Plugin {
    // override val id: String = "custom-data-tracker" // Optional: Defaults to qualified class name
    override val name: String = "Custom Tracker"

    private var pluginContext: PluginContext? = null

    override fun onAttach(context: PluginContext) {
        this.pluginContext = context
        
        // Start listening to background events or perform setup
        println("Plugin attached with scope: ${context.scope}")
    }

    override fun onClear() {
        // Clear internal data caches here
    }
}
```

### 3.2. Visual UI Plugin Example

Here is how you create a plugin that renders a visual panel inside the overlay. UI plugins now own their entire layout inside `Content` (e.g. sticky headers, search bars, list content):

```kotlin
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.ae.log.plugin.UIPlugin
import com.ae.log.plugin.PluginContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class CustomUiPlugin : UIPlugin {
    // override val icon: @Composable () -> Unit = { Icon(Icons.Default.Star, null) } // Optional: Defaults to a generic plug icon!
    // override val id: String = "custom-ui-panel" // Optional: Defaults to qualified class name
    override val name: String = "Custom Panel"
    // override val badgeCount — omit entirely when you don't need a counter on the tab

    override fun onAttach(context: PluginContext) {
        // Perform initialization
    }

    @Composable
    override fun Content(modifier: Modifier) {
        Column(modifier = modifier.fillMaxSize()) {
            // Renders your sticky search bars or custom action headers directly in Content!
            Text("This is my custom panel header actions / controls")
            Text("This is my custom panel main list content!")
        }
    }
}
```

---

## 4. Internal Decoupled Communication (`EventBus`)

To pass events safely between decoupled plugins without cross-dependencies, AELog utilizes a reactive `EventBus`.

### Subscribing to Events
Inside a plugin, you can subscribe to specific events through the `PluginContext`'s helper method:

```kotlin
override fun onAttach(context: PluginContext) {
    context.collectEvents<PanelOpenedEvent> { event ->
        println("The AELog panel was opened on screen!")
    }
}
```

---

## 5. Extensible Persistence Layer

If your plugin needs to persist records across application restarts, AELog core provides a thread-safe, file-backed storage utility: `PersistentPluginStorage`.

```kotlin
import com.ae.log.storage.PersistentPluginStorage
import kotlinx.serialization.serializer

// Initialize a persistent storage for custom events
val storage = PersistentPluginStorage(
    directoryPath = "/absolute/path/to/cache",
    serializer = serializer<MyCustomEvent>()
)

// Add an item (writes asynchronously to disk as a JSON side-effect)
storage.add(MyCustomEvent("data"))

// Observe reactive flow
val stateFlow = storage.dataFlow
```

### Mocking File I/O for Pure Unit Tests
AELog abstracts low-level, platform-level file systems under the `FileOperations` interface, situated in the `com.ae.log.utils` package. During testing, you can inject a mock file system to write lightning-fast unit tests in `commonTest` without ever hitting the actual disk:

```kotlin
import com.ae.log.utils.FileOperations

val mockFileOps = object : FileOperations {
    val files = mutableMapOf<String, String>()
    override fun ensureDirectoryExists() {}
    override fun writeFile(content: String) { files["data.json"] = content }
    override fun readAllFiles(): List<String> = files.values.toList()
    override fun deleteAllFiles() { files.clear() }
}

val testStorage = PersistentPluginStorage(
    directoryPath = "/test",
    serializer = serializer<String>(),
    fileOps = mockFileOps // Pure in-memory unit tests!
)
```

