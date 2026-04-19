# Creating Custom Plugins

AEDevLens is designed to be extended. You can create two types of plugins:

## Plugin Types

| Type | Interface | Has UI | Use Case |
|------|-----------|--------|----------|
| **UI Plugin** | `UIPlugin` | ✅ Tab + Content | Log viewer, network inspector, DB browser |
| **Data Plugin** | `DataPlugin` | ❌ Headless | Crash collector, performance sampler |

## Creating a UI Plugin

### Step 1: Implement UIPlugin

```kotlin
class FeatureFlagsPlugin : UIPlugin {
    override val id = "feature_flags"
    override val name = "Flags"
    override val icon = Icons.Default.Flag
    
    // Badge shown on tab (null = no badge)
    private val _badgeCount = MutableStateFlow<Int?>(null)
    override val badgeCount: StateFlow<Int?> = _badgeCount
    
    private var inspector: AEDevLens? = null
    
    // Called once when installed
    override fun onAttach(inspector: AEDevLens) {
        this.inspector = inspector
        _badgeCount.value = getFlags().size
    }
    
    // Called when DevLens panel opens
    override fun onOpen() {
        refreshFlags()
    }
    
    // Called when DevLens panel closes
    override fun onClose() {
        // Pause expensive operations
    }
    
    // Called when user taps "Clear All"
    override fun onClear() {
        resetFlags()
        _badgeCount.value = 0
    }
    
    // Called when plugin is uninstalled
    override fun onDetach() {
        inspector = null
    }
    
    // Main content rendered in the DevLens panel
    @Composable
    override fun Content(modifier: Modifier) {
        val flags = remember { getFlags() }
        
        LazyColumn(modifier = modifier.fillMaxSize()) {
            items(flags) { flag ->
                FlagRow(
                    name = flag.name,
                    enabled = flag.enabled,
                    onToggle = { toggleFlag(flag.id) }
                )
            }
        }
    }
    
    // Optional: Controls above the main content
    @Composable
    override fun HeaderContent() {
        Text(
            text = "Toggle feature flags for testing",
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(horizontal = 20.dp)
        )
    }
    
    // Optional: Action buttons in the header
    @Composable
    override fun HeaderActions() {
        IconButton(onClick = { resetAllFlags() }) {
            Icon(Icons.Default.Refresh, "Reset all")
        }
    }
}
```

### Step 2: Install the Plugin

```kotlin
DevLensSetup.init(plugins = listOf(LogsPlugin(), FeatureFlagsPlugin()))
```

### Step 3: Done!
Your plugin now appears as a tab in the DevLens panel.

## Creating a Data Plugin

```kotlin
class PerformancePlugin : DataPlugin {
    override val id = "performance"
    override val name = "Performance"
    
    private val _metrics = MutableStateFlow<List<Metric>>(emptyList())
    val metrics: StateFlow<List<Metric>> = _metrics.asStateFlow()
    
    override fun onAttach(inspector: AEDevLens) {
        startCollecting()
    }
    
    override fun onDetach() {
        stopCollecting()
    }
    
    fun recordMetric(name: String, durationMs: Long) {
        _metrics.update { it + Metric(name, durationMs) }
    }
}

// Usage
val perfPlugin = inspector.getPlugin<PerformancePlugin>()
perfPlugin?.recordMetric("api_call", 250L)
```

## Plugin Lifecycle

```text
install() → onAttach()
                ↓
         ┌→ onOpen()  ←┐
         │      ↓       │   (user opens/closes DevLens)
         └─ onClose() ──┘
                ↓
           onDetach()  ← uninstall()
```

!!! warning "Important"
    - `onAttach` is called exactly once
    - `onOpen`/`onClose` may be called many times
    - Always null-check resources in `Content()` — it may render before `onAttach`
    - Handle your own errors inside `Content()` — Compose has no try-catch for composables

## Best Practices
- **Keep plugins focused** — one responsibility per plugin
- **Use StateFlow** — all reactive data should use `StateFlow`, not `mutableStateOf`
- **Minimize main-thread work** — heavy computation goes in `Dispatchers.Default`
- **Clean up in onDetach** — cancel coroutines, close resources
- **Test independently** — plugins should be testable without the UI
