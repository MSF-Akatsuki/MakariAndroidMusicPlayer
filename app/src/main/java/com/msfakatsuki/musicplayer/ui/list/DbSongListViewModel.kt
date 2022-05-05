package com.msfakatsuki.musicplayer.ui.list

import androidx.lifecycle.*
import com.msfakatsuki.musicplayer.database.music.MusicRoomRepository
import com.msfakatsuki.musicplayer.database.music.RoomMusicItem

class DbSongListViewModel(private val repository: MusicRoomRepository) : ViewModel() {
    // TODO: Implement the ViewModel
    val songList : LiveData<List<RoomMusicItem>> = repository.listMusic.asLiveData()

}

class DbSongListViewModelFactory(private val repository: MusicRoomRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DbSongListViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return DbSongListViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
