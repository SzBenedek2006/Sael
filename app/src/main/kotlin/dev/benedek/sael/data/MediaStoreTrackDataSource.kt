package dev.benedek.sael.data

import android.annotation.SuppressLint
import android.content.ContentUris
import android.content.Context
import android.os.Build
import android.provider.MediaStore
import dev.benedek.sael.data.model.Track
import java.io.File
import androidx.core.net.toUri

class   MediaStoreTrackDataSource(
    private val context: Context
) {
    @SuppressLint("Range")
    fun loadTracks(): List<Track> {
        val result = mutableListOf<Track>()

        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.ALBUM_ID,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.TRACK,
            MediaStore.Audio.Media.YEAR,
            MediaStore.Audio.Media.DATA,
            MediaStore.Audio.Media.MIME_TYPE,
            MediaStore.Audio.Media.BITRATE
        )

        val selection = """
            ${MediaStore.Audio.Media.IS_MUSIC} != 0
            AND ${MediaStore.Audio.Media.DURATION} > 30000
        """.trimIndent()

        val sortOrder = "${MediaStore.Audio.Media.TITLE} COLLATE NOCASE ASC"

        context.contentResolver.query(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            projection,
            selection,
            null,
            sortOrder
        )?.use { cursor ->
            while (cursor.moveToNext()) {
                val id = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media._ID))
                val albumId = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID))
                val path = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA))

                val contentUri = ContentUris.withAppendedId(
                    MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                    id
                )

                val artworkUri = ContentUris.withAppendedId(
                    "content://media/external/audio/albumart".toUri(),
                    albumId
                )



                val lrcPath = path?.substringBeforeLast('.')?.plus(".lrc")
                val trackNumber = cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media.TRACK))

                result += Track(
                    id = id,
                    title = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE)),
                    artist = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST)),
                    album = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM)),
                    duration = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.DURATION)),
                    trackNumber = trackNumber,
                    year = cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media.YEAR)),
                    path = path,
                    mimeType = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.MIME_TYPE)),
                    bitrate = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R)
                        cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media.BITRATE))
                    else 0,
                    contentUri = contentUri,
                    artworkUri = artworkUri,
                    lrcPath = if (lrcPath != null && File(lrcPath).exists()) lrcPath else null
                )
            }
        }

        return result
    }
}