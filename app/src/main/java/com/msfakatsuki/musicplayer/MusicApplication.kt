package com.msfakatsuki.musicplayer

import android.app.Application
import com.msfakatsuki.musicplayer.database.music.MusicRoomDatabase
import com.msfakatsuki.musicplayer.database.music.MusicRoomRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob

class MusicApplication : Application() {

    val applicationScope = CoroutineScope(SupervisorJob())

    val database by lazy { MusicRoomDatabase.getDatabase(this,applicationScope) }
    val repository by lazy { MusicRoomRepository(database.musicDao()) }
}