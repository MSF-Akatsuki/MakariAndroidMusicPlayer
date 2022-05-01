package com.msfakatsuki.musicplayer.ui.play

import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.session.MediaSessionCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.Flow
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

class MusicPlayUIViewModel : ViewModel() {
    // TODO: Implement the ViewModel
    var isServiceConnected: MutableLiveData<Boolean> = MutableLiveData<Boolean>(false)

    val SongPlayingList : MutableLiveData<List<MediaSessionCompat.QueueItem>> by lazy {
        MutableLiveData<List<MediaSessionCompat.QueueItem>>()
    }

}
