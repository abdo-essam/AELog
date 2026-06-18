package com.ae.log.network.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import com.ae.log.ui.theme.LogSpacing
import com.ae.log.ui.theme.LogTheme

/** A single label + monospaced value row (URL, Status, Duration). */
@Composable
internal fun NetworkDetailSection(
    label: String,
    value: String,
) {
    Column(modifier = Modifier.padding(bottom = LogSpacing.x2)) {
        Text(
            text = label,
            style = LogTheme.typography.labelSmall,
            color = LogTheme.colors.primary,
        )
        Text(
            text = value,
            style = LogTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
            color = LogTheme.colors.onSurface,
        )
    }
}
