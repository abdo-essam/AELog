package com.ae.log.network.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * Centralized color palette for HTTP methods and status codes.
 * Ensures visual consistency across Log and Network plugins.
 */
public object NetworkColors {
    public fun getMethodColor(method: String?): Color =
        when (method?.uppercase()) {
            "GET" -> Color(0xFF2196F3) // Blue
            "POST" -> Color(0xFF4CAF50) // Green
            "PUT" -> Color(0xFFFF9800) // Orange
            "PATCH" -> Color(0xFF9C27B0) // Purple
            "DELETE" -> Color(0xFFE53935) // Red
            "HEAD" -> Color(0xFF607D8B) // Blue Grey
            "OPTIONS" -> Color(0xFF795548) // Brown
            else -> Color(0xFF9E9E9E) // Grey
        }

    public fun getStatusCodeColor(statusCode: Int?): Color =
        when (statusCode) {
            in 100..199 -> Color(0xFF2196F3) // Information (Blue)
            in 200..299 -> Color(0xFF4CAF50) // Success (Green)
            in 300..399 -> Color(0xFF9C27B0) // Redirection (Purple)
            in 400..499 -> Color(0xFFE53935) // Client Error (Red)
            in 500..599 -> Color(0xFFFF5252) // Server Error (Light Red)
            else -> Color(0xFFFFC107) // Unknown (Amber)
        }
}
