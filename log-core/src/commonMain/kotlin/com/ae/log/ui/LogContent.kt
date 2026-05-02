package com.ae.log.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.ae.log.core.UIPlugin
import com.ae.log.ui.theme.LogSpacing

/**
 * Tabbed container that renders UI plugins as tabs.
 *
 * Each [UIPlugin] gets its own tab with an icon, name, and optional badge count.
 * The active plugin's [UIPlugin.HeaderContent], [UIPlugin.HeaderActions],
 * and [UIPlugin.Content] are rendered below the tab row.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun LogContent(
    plugins: List<UIPlugin>,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    if (plugins.isEmpty()) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "No plugins installed",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        return
    }

    var selectedIndex by remember(plugins.size) { mutableIntStateOf(0) }
    val safeIndex = selectedIndex.coerceIn(0, plugins.lastIndex.coerceAtLeast(0))
    val selectedPlugin = plugins.getOrElse(safeIndex) { plugins.first() }

    Column(modifier = modifier.fillMaxSize()) {
        // Header — title + active plugin's action buttons
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = LogSpacing.x5, vertical = LogSpacing.x3),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "AELog",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Row { selectedPlugin.HeaderActions() }
        }

        // Tab row (only shown when there are multiple plugins)
        if (plugins.size > 1) {
            PrimaryScrollableTabRow(
                selectedTabIndex = safeIndex,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary,
                edgePadding = LogSpacing.x4,
            ) {
                plugins.forEachIndexed { index, plugin ->
                    val badgeCount by plugin.badgeCount.collectAsState()
                    val count = badgeCount
                    Tab(
                        selected = index == safeIndex,
                        onClick = { },
                        text = {
                            Text(plugin.name, style = MaterialTheme.typography.labelMedium)
                        },
                        icon = {
                            if (count > 0) {
                                BadgedBox(badge = {
                                    Badge {
                                        Text(
                                            text = if (count > 99) "99+" else count.toString(),
                                            style = MaterialTheme.typography.labelSmall,
                                        )
                                    }
                                }) {
                                    Icon(plugin.icon, contentDescription = plugin.name)
                                }
                            } else {
                                Icon(plugin.icon, contentDescription = plugin.name)
                            }
                        },
                    )
                }
            }
        }

        // Active plugin slots
        selectedPlugin.HeaderContent()

        PluginContent(
            plugin = selectedPlugin,
            modifier = Modifier.weight(1f),
        )
    }
}
