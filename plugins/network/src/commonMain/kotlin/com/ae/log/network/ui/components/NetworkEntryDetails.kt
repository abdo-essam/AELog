package com.ae.log.network.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import com.ae.log.network.model.NetworkEntry
import com.ae.log.network.utils.extractQueryParams
import com.ae.log.network.utils.prettyPrintJson
import com.ae.log.ui.components.ExpandedDetails
import com.ae.log.ui.theme.LogSpacing

@Composable
internal fun NetworkEntryDetails(
    entry: NetworkEntry,
    onCopy: () -> Unit,
) {
    val bgColor =
        when {
            entry.isError -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
            entry.isSuccess -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.15f)
            else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        }

    val clipboard = LocalClipboardManager.current

    ExpandedDetails(bgColor = bgColor, onCopy = onCopy) {
        Column(modifier = Modifier.padding(LogSpacing.x3)) {
            // ── Overview ─────────────────────────────────────────────────
            NetworkDetailSection("URL", entry.url)
            entry.statusCode?.let { NetworkDetailSection("Status", it.toString()) }
            entry.durationMs?.let { NetworkDetailSection("Duration", "${it}ms") }

            // ── Request ───────────────────────────────────────────────────
            val queryParams = entry.url.extractQueryParams()
            val hasCustomHeaders = entry.requestHeaders.isNotEmpty()
            val hasQueryParams = queryParams.isNotEmpty()
            val hasRequestBody = !entry.requestBody.isNullOrBlank()

            if (hasQueryParams) {
                Spacer(Modifier.height(LogSpacing.x3))
                NetworkHeadersSection("Query Parameters", queryParams)
            }
            if (hasCustomHeaders) {
                Spacer(Modifier.height(LogSpacing.x3))
                NetworkHeadersSection("Request Headers", entry.requestHeaders)
            }
            if (hasRequestBody) {
                Spacer(Modifier.height(LogSpacing.x3))
                NetworkBodySection(
                    label = "Request Body",
                    body = entry.requestBody!!.prettyPrintJson(),
                    onCopy = { clipboard.setText(AnnotatedString(entry.requestBody!!)) },
                )
            }

            // ── Pending indicator ─────────────────────────────────────────
            if (entry.isPending) {
                Spacer(Modifier.height(LogSpacing.x3))
                NetworkPendingIndicator()
            }

            // ── Error (connection failure only) ───────────────────────────
            entry.error?.let { error ->
                Spacer(Modifier.height(LogSpacing.x3))
                Box(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(LogSpacing.x2))
                            .background(MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f))
                            .padding(LogSpacing.x2),
                ) {
                    SelectionContainer {
                        Text(
                            text = error,
                            style =
                                MaterialTheme.typography.bodySmall.copy(
                                    fontFamily = FontFamily.Monospace,
                                ),
                            color = MaterialTheme.colorScheme.onErrorContainer,
                        )
                    }
                }
            }

            // ── Response ──────────────────────────────────────────────────
            val hasResponseHeaders = entry.responseHeaders.isNotEmpty()
            val hasResponseBody = !entry.responseBody.isNullOrBlank()

            if (hasResponseHeaders) {
                Spacer(Modifier.height(LogSpacing.x3))
                NetworkHeadersSection("Response Headers", entry.responseHeaders)
            }
            if (hasResponseBody) {
                Spacer(Modifier.height(LogSpacing.x3))
                NetworkBodySection(
                    label = "Response Body",
                    body = entry.responseBody!!.prettyPrintJson(),
                    onCopy = { clipboard.setText(AnnotatedString(entry.responseBody!!)) },
                )
            }
        }
    }
}
