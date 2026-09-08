package com.shobhankarthish.pocket.shelf

import java.util.Locale

object ByteSizeFormatter {
    fun format(bytes: Long): String {
        val value = bytes.coerceAtLeast(0)
        return when {
            value < 1024 -> "$value B"
            value < 1024L * 1024 -> formatUnit(value / 1024.0, "KB")
            else -> formatUnit(value / (1024.0 * 1024.0), "MB")
        }
    }

    private fun formatUnit(amount: Double, unit: String): String {
        val pattern = if (amount < 10) "%.1f %s" else "%.0f %s"
        return String.format(Locale.US, pattern, amount, unit)
    }
}
