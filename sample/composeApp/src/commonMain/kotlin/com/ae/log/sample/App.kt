package com.ae.log.sample

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.outlined.Analytics
import androidx.compose.material.icons.outlined.List
import androidx.compose.material.icons.outlined.WifiFind
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import com.ae.log.AELogProvider
import com.ae.log.AELogUiConfig
import com.ae.log.sample.ui.analytics.AnalyticsScreen
import com.ae.log.sample.ui.logs.LogScreen
import com.ae.log.sample.ui.network.NetworkScreen
import com.ae.log.sample.ui.theme.SampleTheme

// ── Navigation model ──────────────────────────────────────────────────────────

private data class NavTab(
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
)

private val TABS =
    listOf(
        NavTab("Logs", Icons.Filled.List, Icons.Outlined.List),
        NavTab("Network", Icons.Filled.Wifi, Icons.Outlined.WifiFind),
        NavTab("Analytics", Icons.Filled.Analytics, Icons.Outlined.Analytics),
    )

// ── Root composable ───────────────────────────────────────────────────────────

@Composable
fun App(debugMode: Boolean = false) {
    SampleTheme {
        AELogProvider(
            uiConfig =
                AELogUiConfig(
                    showFloatingButton = true,
                    enableLongPress = true,
                ),
            enabled = debugMode,
        ) {
            var selectedTab by rememberSaveable { mutableIntStateOf(0) }

            Scaffold(
                modifier = Modifier.fillMaxSize(),
                bottomBar = {
                    SampleNavBar(
                        selectedTab = selectedTab,
                        onTabSelected = { selectedTab = it },
                    )
                },
            ) { innerPadding ->
                when (selectedTab) {
                    0 -> LogScreen()
                    1 -> NetworkScreen()
                    2 -> AnalyticsScreen()
                }
                // Apply inner padding so content isn't hidden behind nav bar
                androidx.compose.foundation.layout.Box(
                    modifier = Modifier.padding(innerPadding),
                )
            }
        }
    }
}

// ── Bottom Navigation Bar ─────────────────────────────────────────────────────

@Composable
private fun SampleNavBar(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
) {
    NavigationBar {
        TABS.forEachIndexed { index, tab ->
            NavigationBarItem(
                selected = selectedTab == index,
                onClick = { onTabSelected(index) },
                icon = {
                    Icon(
                        imageVector = if (selectedTab == index) tab.selectedIcon else tab.unselectedIcon,
                        contentDescription = tab.label,
                    )
                },
                label = { Text(tab.label) },
            )
        }
    }
}
