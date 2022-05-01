package com.msfakatsuki.musicplayer.database.music

import android.net.Uri
import androidx.room.*
import kotlinx.coroutines.flow.Flow


@Entity(tableName = "MusicMetaData")
data class RoomMusicItem(
    @PrimaryKey(autoGenerate = true) val id: Int,
    val title: String,
    val artist: String,
    val album: String,
    val sha256: String,
    val localPath: String,
    val remoteLink: String
)

@Dao
interface RoomMusicDao {

    @Insert
    fun insert(vararg item: RoomMusicItem)

    @Delete
    fun delete(vararg item: RoomMusicItem):Int

    @Update
    fun update(vararg item: RoomMusicItem)

    @Query("SELECT * FROM MusicMetaData")
    fun getAllMusic(): Flow<List<RoomMusicItem>>

    @Query("SELECT * FROM MusicMetaData WHERE title LIKE :reg")
    fun getMusicByTitleRegex(reg : String):List<RoomMusicItem>

    @Query("SELECT * FROM MusicMetaData WHERE artist IN (:listArtists)")
    fun getMusicByArtistsList(listArtists : Array<String>):List<RoomMusicItem>



    @Query("SELECT * FROM MusicMetaData WHERE album IN (:listAlbums)")
    fun getMusicByAlbumsList(listAlbums : Array<String>):List<RoomMusicItem>

}