package com.ae.log.ui.layout

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.ae.log.plugin.UIPlugin
import com.ae.log.ui.LogController
import com.ae.log.ui.theme.LogSpacing
import com.ae.log.ui.theme.LogTheme

/**
 * Tabbed container that renders UI plugins as tabs.
 *
 * Each [UIPlugin] gets its own tab with an icon, name, and optional badge count.
 * The active plugin's [UIPlugin.Content] is rendered below the tab row.
 * Plugins are responsible for their own internal layout (e.g. search bars, sticky headers).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun LogContent(
    plugins: List<UIPlugin>,
    onDismiss: () -> Unit,
    controller: LogController,
    modifier: Modifier = Modifier,
) {
    if (plugins.isEmpty()) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "No plugins installed",
                style = LogTheme.typography.bodyLarge,
                color = LogTheme.colors.onSurfaceVariant,
            )
        }
        return
    }

    val activeTabIndex by controller.activeTabIndex.collectAsState()

    val safeIndex = activeTabIndex.coerceIn(0, plugins.lastIndex.coerceAtLeast(0))
    val selectedPlugin = plugins.getOrElse(safeIndex) { plugins.first() }

    Column(modifier = modifier.fillMaxSize()) {
        if (plugins.size > 1) {
            PrimaryScrollableTabRow(
                selectedTabIndex = safeIndex,
                containerColor = LogTheme.colors.surface,
                contentColor = LogTheme.colors.primary,
                edgePadding = LogSpacing.x4,
            ) {
                plugins.forEachIndexed { index, plugin ->
                    val badgeCount by plugin.badgeCount.collectAsState()
                    val count = badgeCount
                    Tab(
                        selected = index == safeIndex,
                        onClick = { controller.selectTab(index) },
                        text = {
                            Text(plugin.name, style = LogTheme.typography.labelMedium)
                        },
                        icon = {
                            if (count > 0) {
                                BadgedBox(badge = {
                                    Badge {
                                        Text(
                                            text = if (count > 99) "99+" else count.toString(),
                                            style = LogTheme.typography.labelSmall,
                                        )
                                    }
                                }) {
                                    plugin.icon()
                                }
                            } else {
                                plugin.icon()
                            }
                        },
                    )
                }
            }
        }

        // Active plugin content
        PluginContent(
            plugin = selectedPlugin,
            modifier = Modifier.weight(1f),
        )
    }
}
