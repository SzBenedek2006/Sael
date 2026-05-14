package dev.benedek.sael.viewmodels

import android.content.Context
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import dev.benedek.sael.data.MediaStoreTrackDataSource
import dev.benedek.sael.data.model.Track
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MainViewModel : ViewModel() {
    public var track by mutableStateOf<Track?>(null)
    private val _tracks =
        MutableStateFlow<List<Track>>(emptyList())

    val tracks = _tracks.asStateFlow()
    var mediaController: MediaController? = null
        set(value) {
            field = value
            value?.addListener(object : Player.Listener {
                override fun onPlayWhenReadyChanged(playWhenReady: Boolean, reason: Int) {
                    isPlaying = playWhenReady
                }
            })
        }


    var isPlaying by mutableStateOf(false)
    var currentPositionMs by mutableLongStateOf(0L)

    fun scanTracks(context: Context) {

        viewModelScope.launch(Dispatchers.IO) {

            Log.i("MainViewModel", "Scanning tracks...")

            val dataSource =
                MediaStoreTrackDataSource(context)

            _tracks.value =
                dataSource.loadTracks()

            Log.i(
                "MainViewModel",
                "Found ${_tracks.value.size} tracks"
            )
        }
    }

    fun resume() {
        mediaController?.play()
    }

    fun pause() {
        mediaController?.pause()
    }

    fun stop() {
        mediaController?.stop()
        mediaController?.clearMediaItems()
    }


    fun playTrack(track: Track) {
        Log.i(this.toString(),"Playing this track: $track")
        val controller = mediaController ?: run {
            Log.d(this.toString(), "mediaController was null")
            return
        }
        val mediaItem = MediaItem.fromUri(track.contentUri)

        controller.setMediaItem(mediaItem)
        controller.prepare()
        controller.play()

        this.track = track


        startPollingPosition()
    }

    fun seekTo(positionMs: Long) {
        mediaController?.seekTo(positionMs)
        currentPositionMs = positionMs
    }
    private fun startPollingPosition() {
        viewModelScope.launch(Dispatchers.Main) {
            while (true) {
                mediaController?.let {
                    // Only update if playing, or if we just loaded a track
                    if (it.isPlaying) {
                        currentPositionMs = it.currentPosition
                    }
                }
                delay(1000L / 30L) // ~30fps
            }
        }
    }


}