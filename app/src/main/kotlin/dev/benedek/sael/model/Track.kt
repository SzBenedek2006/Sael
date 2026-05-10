package dev.benedek.sael.model
import android.graphics.Bitmap

data class Track(
    val title: String,
    val artist: String,
    val artwork: Bitmap?,
    val duration: Long
)
