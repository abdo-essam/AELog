package com.ae.log.sample

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import com.ae.log.AELog
import com.ae.log.LogProvider
import com.ae.log.UiConfig
import com.ae.log.sample.ui.features.analytics.AnalyticsScreen
import com.ae.log.sample.ui.features.log.LogScreen
import com.ae.log.sample.ui.features.network.NetworkScreen
import com.ae.log.sample.ui.theme.SampleTheme

@Composable
fun App(debugMode: Boolean = true) {
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }

    SampleTheme {
        LogProvider(
            uiConfig =
                UiConfig(
                    showFloatingButton = true,
                ),
            enabled = debugMode,
        ) {
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
                    }
                }
            }
        }
    }
}

@Composable
private fun SampleNavBar(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
) {
    val items =
        listOf(
            "Logs" to (Icons.Filled.List to Icons.Outlined.List),
            "Network" to (Icons.Filled.Wifi to Icons.Outlined.Wifi),
            "Analytics" to (Icons.Filled.Analytics to Icons.Outlined.Analytics),
        )

    NavigationBar {
        items.forEachIndexed { index, (label, icons) ->
            NavigationBarItem(
                selected = selectedTab == index,
                onClick = { onTabSelected(index) },
                label = { Text(label) },
                icon = {
                    Icon(
                        imageVector = if (selectedTab == index) icons.first else icons.second,
                        contentDescription = label,
                    )
                },
            )
        }
    }
}
