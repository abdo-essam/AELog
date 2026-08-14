package com.ae.log.ui.layout

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.SettingsBrightness
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.ae.log.ui.LocalLogController
import com.ae.log.ui.theme.LogSpacing
import com.ae.log.ui.theme.LogTheme
import com.ae.log.ui.theme.LogThemeMode

@Composable
internal fun SettingsContent(
    modifier: Modifier = Modifier,
) {
    val controller = LocalLogController.current
    val themeMode by controller.themeMode.collectAsState()

    Column(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(LogSpacing.x4)
        ) {
            item {
                Text(
                    text = "Appearance",
                    style = LogTheme.typography.labelSmall,
                    color = LogTheme.colors.primary,
                    modifier = Modifier.padding(bottom = LogSpacing.x2)
                )
            }

            item {
                ThemeOption(
                    title = "System",
                    icon = Icons.Default.SettingsBrightness,
                    selected = themeMode == LogThemeMode.SYSTEM,
                    onClick = { controller.setThemeMode(LogThemeMode.SYSTEM) }
                )
            }

            item {
                ThemeOption(
                    title = "Light",
                    icon = Icons.Default.LightMode,
                    selected = themeMode == LogThemeMode.LIGHT,
                    onClick = { controller.setThemeMode(LogThemeMode.LIGHT) }
                )
            }

            item {
                ThemeOption(
                    title = "Dark",
                    icon = Icons.Default.DarkMode,
                    selected = themeMode == LogThemeMode.DARK,
                    onClick = { controller.setThemeMode(LogThemeMode.DARK) }
                )
            }
        }
    }
}

@Composable
private fun ThemeOption(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        color = Color.Transparent
    ) {
        Row(
            modifier = Modifier
                .padding(vertical = LogSpacing.x3),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(LogSpacing.x3)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (selected) LogTheme.colors.primary else LogTheme.colors.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = title,
                style = LogTheme.typography.bodyMedium,
                color = if (selected) LogTheme.colors.primary else LogTheme.colors.onSurface,
                modifier = Modifier.weight(1f)
            )
            RadioButton(
                selected = selected,
                onClick = null,
                colors = RadioButtonDefaults.colors(
                    selectedColor = LogTheme.colors.primary
                )
            )
        }
    }
}
