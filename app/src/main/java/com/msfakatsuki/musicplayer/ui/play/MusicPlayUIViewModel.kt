package com.msfakatsuki.musicplayer.ui.play

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

    var duration : Long = 0
    var currentPosition : Long = 0

    val fileReadProcessNumber by lazy { MutableLiveData<Int>(0) }
    val fileDbProcessFlag by lazy { MutableLiveData<Boolean>(false) }
}
