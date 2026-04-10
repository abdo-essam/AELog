package com.ae.devlens.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.ae.devlens.core.UIPlugin
import com.ae.devlens.ui.theme.DevLensSpacing

/**
 * Tabbed container that renders UI plugins as tabs.
 *
 * Each [UIPlugin] gets its own tab with an icon, name, and optional badge.
 * The active plugin's [UIPlugin.HeaderContent], [UIPlugin.HeaderActions],
 * and [UIPlugin.Content] are rendered below the tab row.
 */
@Composable
internal fun DevLensContent(
    plugins: List<UIPlugin>,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (plugins.isEmpty()) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = androidx.compose.ui.Alignment.Center
        ) {
            Text(
                text = "No plugins installed",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        return
    }

    var selectedIndex by remember { mutableIntStateOf(0) }
    val selectedPlugin = plugins.getOrElse(selectedIndex) { plugins.first() }

    Column(modifier = modifier.fillMaxSize()) {
        // Header row with title and close
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = DevLensSpacing.x5, vertical = DevLensSpacing.x3),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
        ) {
            Text(
                text = "AEDevLens",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )

            // Active plugin's header actions
            Row {
                selectedPlugin.HeaderActions()
            }
        }

        // Tab row (only show if multiple plugins)
        if (plugins.size > 1) {
            PrimaryScrollableTabRow(
                selectedTabIndex = selectedIndex,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary,
                edgePadding = DevLensSpacing.x4
            ) {
                plugins.forEachIndexed { index, plugin ->
                    val badgeCount by plugin.badgeCount.collectAsState()

                    Tab(
                        selected = index == selectedIndex,
                        onClick = { selectedIndex = index },
                        text = { Text(plugin.name, style = MaterialTheme.typography.labelMedium) },
                        icon = {
                            if (badgeCount != null && badgeCount!! > 0) {
                                BadgedBox(
                                    badge = {
                                        Badge {
                                            Text(
                                                text = if (badgeCount!! > 99) "99+" else badgeCount.toString(),
                                                style = MaterialTheme.typography.labelSmall
                                            )
                                        }
                                    }
                                ) {
                                    Icon(
                                        imageVector = plugin.icon,
                                        contentDescription = plugin.name
                                    )
                                }
                            } else {
                                Icon(
                                    imageVector = plugin.icon,
                                    contentDescription = plugin.name
                                )
                            }
                        }
                    )
                }
            }
        }

        // Active plugin's header content (flexible slot)
        selectedPlugin.HeaderContent()

        // Active plugin's main content (with error boundary)
        SafePluginContent(
            plugin = selectedPlugin,
            modifier = Modifier.weight(1f)
        )
    }
}
