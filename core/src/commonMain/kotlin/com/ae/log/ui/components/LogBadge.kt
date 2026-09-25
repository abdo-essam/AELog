package com.ae.log.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ae.log.ui.theme.LogDimens
import com.ae.log.ui.theme.LogSpacing
import com.ae.log.ui.theme.LogTheme

/**
 * Standard badge used for tags, operation types, severity indicators, and HTTP methods.
 *
 * @param text Badge label text (typically uppercase).
 * @param containerColor Background color of the badge.
 * @param contentColor Text color inside the badge.
 */
@Composable
public fun LogBadge(
    text: String,
    containerColor: Color = LogTheme.colors.surfaceVariant,
    contentColor: Color = LogTheme.colors.onSurfaceVariant,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier =
            modifier
                .background(containerColor, RoundedCornerShape(LogDimens.badgeCornerRadius))
                .padding(horizontal = LogSpacing.x1_5, vertical = 2.dp),
    ) {
        Text(
            text = text,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = contentColor,
            letterSpacing = 0.5.sp,
            maxLines = 1,
        )
    }
}
