package com.ae.log.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import com.ae.log.ui.theme.LogSpacing
import com.ae.log.ui.theme.LogTheme

/**
 * Standard section header used to partition screens into logical groups.
 *
 * @param title Prominent group title.
 * @param subtitle Optional description text explaining the section.
 * @param action Optional trailing action composable.
 */
@Composable
public fun LogSectionHeader(
    title: String,
    subtitle: String? = null,
    action: (@Composable () -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f, fill = false)) {
            Text(
                text = title,
                style = LogTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = LogTheme.colors.onSurface,
            )
            if (!subtitle.isNullOrBlank()) {
                Spacer(Modifier.height(LogSpacing.x0_5))
                Text(
                    text = subtitle,
                    style = LogTheme.typography.bodySmall,
                    color = LogTheme.colors.onSurfaceVariant,
                )
            }
        }

        if (action != null) {
            action()
        }
    }
}
