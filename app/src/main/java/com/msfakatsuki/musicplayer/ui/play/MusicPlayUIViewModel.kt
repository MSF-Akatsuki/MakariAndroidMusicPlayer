package com.msfakatsuki.musicplayer.ui.play

import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow

class MusicPlayUIViewModel : ViewModel() {
    // TODO: Implement the ViewModel
    var isServiceConnected: MutableLiveData<Boolean> = MutableLiveData<Boolean>(false)

    val SongPlayingList : MutableLiveData<List<MediaSessionCompat.QueueItem>> by lazy {
        MutableLiveData<List<MediaSessionCompat.QueueItem>>()
    }

    val songDescription : MutableLiveData<MediaMetadataCompat> by lazy {
        MutableLiveData<MediaMetadataCompat>()
    }

    var duration : Long = 0
    var currentPosition : Long = 0

    val fileReadProcessNumber by lazy { MutableLiveData<Int>(0) }
    val fileDbProcessFlag by lazy { MutableLiveData<Boolean>(false) }

    lateinit var mediaBrowser: MediaBrowserCompat
}
