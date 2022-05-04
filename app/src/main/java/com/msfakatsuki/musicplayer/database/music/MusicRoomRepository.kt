package com.msfakatsuki.musicplayer.database.music

import androidx.annotation.WorkerThread
import kotlinx.coroutines.flow.Flow

class MusicRoomRepository(private val musicDao: RoomMusicDao) {

    val listMusic : Flow<List<RoomMusicItem>> = musicDao.getAllMusic()

    @WorkerThread
    fun insert(item : RoomMusicItem) {
        val x = musicDao.checkListSizeOfLocalPath(item.localPath)
        if (x.size==0)
            musicDao.insert(item)
    }

    @WorkerThread
    fun delete(item : RoomMusicItem) {
        musicDao.delete(item)
    }

    @WorkerThread
    fun update(item : RoomMusicItem) {
        musicDao.update(item)
    }



}