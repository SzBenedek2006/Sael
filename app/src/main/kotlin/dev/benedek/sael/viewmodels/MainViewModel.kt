package dev.benedek.sael.viewmodels

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import dev.benedek.sael.data.model.Track

class MainViewModel : ViewModel() {
    public var track: Track? = null

    fun playTrack(uri: Uri) {
        Log.i(this.toString(),"Playing this Uri: $uri")
    }


}