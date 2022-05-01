package com.msfakatsuki.musicplayer.database.music

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import kotlinx.coroutines.CoroutineScope

@Database(entities = arrayOf(RoomMusicItem::class),version=1,exportSchema = false)
public abstract class MusicRoomDatabase : RoomDatabase() {

    abstract fun musicDao(): RoomMusicDao

    companion object {

        @Volatile
        private var INSTANCE : MusicRoomDatabase?=null

        fun getDatabase(context: Context, scope: CoroutineScope): MusicRoomDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    MusicRoomDatabase::class.java,
                    "music_room_db"
                ).build()
                INSTANCE = instance
                instance
            }
        }

    }
}