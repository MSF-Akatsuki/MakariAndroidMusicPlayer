package com.msfakatsuki.musicplayer.ui.play

import android.net.Uri
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.session.MediaSessionCompat
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow

class MusicPlayUIFragmentViewModel : ViewModel() {
    var iconUri: Uri?=null
    var albumText: String?=null
    var artistText: String?=null
}
