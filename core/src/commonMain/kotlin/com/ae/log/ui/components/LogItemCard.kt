package com.ae.log.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.ae.log.ui.theme.LogDimens
import com.ae.log.ui.theme.LogSpacing
import com.ae.log.ui.theme.LogTheme

/**
 * Standard item card with a leading icon container, title, subtitle, and chevron.
 *
 * @param title Prominent item title.
 * @param subtitle Optional secondary descriptor.
 * @param meta Optional tertiary metadata line.
 * @param icon Leading icon composable.
 * @param iconContainerColor Background color of the rounded icon box.
 * @param onClick Click handler. If non-null, card is clickable and shows a chevron by default.
 * @param trailing Optional trailing composable to replace or augment the chevron.
 */
@Composable
public fun LogItemCard(
    title: String,
    subtitle: String? = null,
    meta: String? = null,
    icon: @Composable () -> Unit,
    iconContainerColor: Color = LogTheme.colors.primaryContainer.copy(alpha = 0.5f),
    onClick: (() -> Unit)? = null,
    trailing: (@Composable () -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(LogDimens.cardCornerRadius))
            .then(
                if (onClick != null) {
                    Modifier.clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onClick,
                    )
                } else {
                    Modifier
                },
            ),
        shape = RoundedCornerShape(LogDimens.cardCornerRadius),
        colors = CardDefaults.cardColors(containerColor = LogTheme.colors.surface),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = LogSpacing.x4, vertical = LogSpacing.x3),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .background(iconContainerColor, RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center,
            ) {
                icon()
            }

            Spacer(Modifier.width(LogSpacing.x3))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = LogTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = LogTheme.colors.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                if (!subtitle.isNullOrBlank()) {
                    Spacer(Modifier.height(LogSpacing.x0_5))
                    Text(
                        text = subtitle,
                        style = LogTheme.typography.bodySmall,
                        color = LogTheme.colors.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                if (!meta.isNullOrBlank()) {
                    Spacer(Modifier.height(LogSpacing.x0_5))
                    Text(
                        text = meta,
                        style = LogTheme.typography.labelSmall,
                        color = LogTheme.colors.onSurfaceVariant.copy(alpha = 0.8f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }

            if (trailing != null) {
                trailing()
            } else if (onClick != null) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    tint = LogTheme.colors.onSurfaceVariant,
                    modifier = Modifier.size(LogSpacing.x5),
                )
            }
        }
    }
}
