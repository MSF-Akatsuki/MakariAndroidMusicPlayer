package com.msfakatsuki.musicplayer.ui.play

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class MusicPlayUIViewModel : ViewModel() {
    // TODO: Implement the ViewModel
    var isServiceConnected: MutableLiveData<Boolean> = MutableLiveData<Boolean>(false)
}
