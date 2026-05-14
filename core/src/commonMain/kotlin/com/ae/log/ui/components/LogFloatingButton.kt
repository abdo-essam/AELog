package com.ae.log.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import com.ae.log.ui.theme.LogSpacing

/** Floating action button to open AELog. */
@Composable
internal fun LogFloatingButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier =
            modifier
                .size(LogSpacing.x12)
                .shadow(LogSpacing.x2, CircleShape)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClickLabel = "Open AELogs panel",
                ) { onClick() },
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = Icons.Default.BugReport,
            contentDescription = "Open AELog",
            tint = MaterialTheme.colorScheme.onPrimary,
        )
    }
}
