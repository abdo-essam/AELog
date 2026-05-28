package com.ae.log.sample

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.outlined.List
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.outlined.Analytics
import androidx.compose.material.icons.outlined.BugReport
import androidx.compose.material.icons.outlined.Speed
import androidx.compose.material.icons.outlined.Wifi
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import com.ae.log.sample.ui.features.analytics.AnalyticsScreen
import com.ae.log.sample.ui.features.crashes.CrashScreen
import com.ae.log.sample.ui.features.log.LogScreen
import com.ae.log.sample.ui.features.network.NetworkScreen
import com.ae.log.sample.ui.features.perf.PerfScreen
import com.ae.log.sample.ui.theme.SampleTheme
import com.ae.log.AELog
import com.ae.log.logs.LogPlugin
import com.ae.log.ui.AELogOverlay

@Composable
fun App() {
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }

    AELogOverlay()

    SampleTheme {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            bottomBar = {
                SampleNavBar(
                    selectedTab = selectedTab,
                    onTabSelected = { selectedTab = it },
                )
            },
        ) { innerPadding ->
            Box(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
                when (selectedTab) {
                    0 -> LogScreen()
                    1 -> NetworkScreen()
                    2 -> AnalyticsScreen()
                    3 -> CrashScreen()
                    4 -> PerfScreen()
                }
            }
        }
    }
}

private data class NavItem(
    val label: String,
    val filledIcon: ImageVector,
    val outlinedIcon: ImageVector,
)

@Composable
private fun SampleNavBar(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
) {
    val items =
        listOf(
            NavItem("Logs", Icons.AutoMirrored.Filled.List, Icons.AutoMirrored.Outlined.List),
            NavItem("Network", Icons.Filled.Wifi, Icons.Outlined.Wifi),
            NavItem("Analytics", Icons.Filled.Analytics, Icons.Outlined.Analytics),
            NavItem("Crashes", Icons.Filled.BugReport, Icons.Outlined.BugReport),
            NavItem("Perf", Icons.Filled.Speed, Icons.Outlined.Speed),
        )

    NavigationBar {
        items.forEachIndexed { index, item ->
            NavigationBarItem(
                selected = selectedTab == index,
                onClick = { onTabSelected(index) },
                label = { Text(item.label) },
                icon = {
                    Icon(
                        imageVector = if (selectedTab == index) item.filledIcon else item.outlinedIcon,
                        contentDescription = item.label,
                    )
                },
            )
        }
    }
}
