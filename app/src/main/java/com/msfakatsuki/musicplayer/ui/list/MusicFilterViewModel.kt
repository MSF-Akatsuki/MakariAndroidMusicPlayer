package com.msfakatsuki.musicplayer.ui.list

import android.util.Log
import androidx.lifecycle.*
import com.msfakatsuki.musicplayer.database.music.MusicRoomRepository
import com.msfakatsuki.musicplayer.database.music.RoomMusicItem
import com.msfakatsuki.musicplayer.database.music.SingleStringObject

class MusicFilterViewModel() : ViewModel() {
    val selectedArtistList:MutableLiveData<List<SingleStringObject>> by lazy {
        MutableLiveData<List<SingleStringObject>>()
    }
    val selectedAlbumList:MutableLiveData<List<SingleStringObject>> by lazy{
        MutableLiveData<List<SingleStringObject>>()
    }

    var isFiltered = false

    companion object{
        val DO_NOTHING = 0
        val DISPLAY_ARTIST_LIST = 1
        val DISPLAY_ALBUM_LIST = 2
        val DISPLAY_ALBUM_LIST_BY_ARTISTS = 3
    }

    var displayState : Int = DO_NOTHING

    init {
        Log.i("MFviewModel","isCreated")
    }

    fun reset(){
        selectedAlbumList.value = null
        selectedArtistList.value = null
        displayState=DISPLAY_ARTIST_LIST
        isFiltered = false
    }

}

class MusicFilterViewModelFactory() : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MusicFilterViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MusicFilterViewModel() as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
