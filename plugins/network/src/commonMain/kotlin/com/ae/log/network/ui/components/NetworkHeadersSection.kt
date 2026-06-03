package com.ae.log.network.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import com.ae.log.ui.theme.LogTheme

/** Scrollable key=value header grid with copy-selection support. */
@Composable
internal fun NetworkHeadersSection(
    label: String,
    headers: Map<String, String>,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            style = LogTheme.typography.labelSmall,
            color = LogTheme.colors.primary,
        )
        SelectionContainer {
            Column(modifier = Modifier.padding(top = 4.dp)) {
                headers.forEach { (key, value) ->
                    Row(modifier = Modifier.padding(bottom = 2.dp)) {
                        Text(
                            text = "$key:",
                            style = LogTheme.typography.bodySmall,
                            color = LogTheme.colors.onSurfaceVariant,
                            modifier = Modifier.padding(end = 4.dp),
                        )
                        Text(
                            text = value,
                            style =
                                LogTheme.typography.bodySmall.copy(
                                    fontFamily = FontFamily.Monospace,
                                ),
                            color = LogTheme.colors.onSurface,
                        )
                    }
                }
            }
        }
    }
}
