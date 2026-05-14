package dev.benedek.sael.data.model
import android.graphics.Bitmap
import android.net.Uri

data class Track(
    val id: Long,
    val title: String?,
    val artist: String?,
    val album: String?,
    val duration: Long,
    val trackNumber: Int,
    val year: Int,
    val path: String?,
    val mimeType: String?,
    val bitrate: Int,
    val contentUri: Uri,
    val artworkUri: Uri,
    val lrcPath: String?
)