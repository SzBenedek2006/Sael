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
    val embeddedCover: ByteArray?,
    val embeddedLyrics: String?,
    val lrcPath: String?
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Track

        if (id != other.id) return false
        if (duration != other.duration) return false
        if (trackNumber != other.trackNumber) return false
        if (year != other.year) return false
        if (bitrate != other.bitrate) return false
        if (title != other.title) return false
        if (artist != other.artist) return false
        if (album != other.album) return false
        if (path != other.path) return false
        if (mimeType != other.mimeType) return false
        if (contentUri != other.contentUri) return false
        if (artworkUri != other.artworkUri) return false
        if (!embeddedCover.contentEquals(other.embeddedCover)) return false
        if (embeddedLyrics != other.embeddedLyrics) return false
        if (lrcPath != other.lrcPath) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + duration.hashCode()
        result = 31 * result + trackNumber
        result = 31 * result + year
        result = 31 * result + bitrate
        result = 31 * result + (title?.hashCode() ?: 0)
        result = 31 * result + (artist?.hashCode() ?: 0)
        result = 31 * result + (album?.hashCode() ?: 0)
        result = 31 * result + (path?.hashCode() ?: 0)
        result = 31 * result + (mimeType?.hashCode() ?: 0)
        result = 31 * result + contentUri.hashCode()
        result = 31 * result + artworkUri.hashCode()
        result = 31 * result + (embeddedCover?.contentHashCode() ?: 0)
        result = 31 * result + (embeddedLyrics?.hashCode() ?: 0)
        result = 31 * result + (lrcPath?.hashCode() ?: 0)
        return result
    }
}
