package com.ae.log.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.ae.log.ui.theme.LogDimens
import com.ae.log.ui.theme.LogSpacing
import com.ae.log.ui.theme.LogTheme

/**
 * Standard key-value attribute row used in detail inspectors across plugins.
 *
 * @param key Attribute key/name displayed on the left.
 * @param value Attribute value string displayed on the right.
 * @param keyWidth Fixed width for the key label (defaults to 110.dp).
 * @param onCopy Optional click handler to copy value.
 * @param isDividerVisible Whether to draw a divider beneath the row.
 */
@Composable
public fun LogKeyValueItem(
    key: String,
    value: String?,
    keyWidth: Dp = 110.dp,
    onCopy: (() -> Unit)? = null,
    isDividerVisible: Boolean = true,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .then(
                    if (onCopy != null) {
                        Modifier.clickable { onCopy() }
                    } else {
                        Modifier
                    },
                )
                .padding(horizontal = LogSpacing.x4, vertical = LogSpacing.x2_5),
            verticalAlignment = Alignment.Top,
        ) {
            Text(
                text = key,
                style = LogTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = LogTheme.colors.onSurfaceVariant,
                modifier = Modifier.width(keyWidth),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )

            Spacer(Modifier.width(LogSpacing.x2))

            Text(
                text = value ?: "null",
                style = LogTheme.typography.bodySmall,
                fontFamily = FontFamily.Monospace,
                fontStyle = if (value == null) FontStyle.Italic else FontStyle.Normal,
                color = if (value == null) {
                    LogTheme.colors.error.copy(alpha = 0.7f)
                } else {
                    LogTheme.colors.onSurface
                },
                modifier = Modifier.weight(1f),
            )
        }

        if (isDividerVisible) {
            HorizontalDivider(
                modifier = Modifier.padding(horizontal = LogSpacing.x4),
                color = LogTheme.colors.outlineVariant,
                thickness = LogDimens.listDividerThickness,
            )
        }
    }
}
