package dev.benedek.sael.utils

import java.util.Locale

fun formatDuration(durationMs: Long): String {
    if (durationMs < 0) return "0:00"
    val totalSeconds = durationMs / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format(Locale.US, "%d:%02d", minutes, seconds)
}