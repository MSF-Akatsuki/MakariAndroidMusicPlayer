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

data class RoomSizeItem(val size:Int)
data class RoomArtistItem(val artist: String)
data class RoomAlbumItem(val album: String)

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

    @Query("SELECT * FROM MusicMetaData WHERE artist LIKE :reg")
    fun getMusicByArtistRegex(reg : String):List<RoomMusicItem>

    @Query("SELECT * FROM MusicMetaData WHERE artist IN (:listArtists)")
    fun getMusicByArtistsList(listArtists : Array<String>):List<RoomMusicItem>

    @Query("SELECT * FROM MusicMetaData WHERE album IN (:listAlbums)")
    fun getMusicByAlbumsList(listAlbums : Array<String>):List<RoomMusicItem>

    @Query("SELECT COUNT(*) as size FROM MusicMetaData WHERE localPath == :location")
    fun checkListSizeOfLocalPath(location: String):RoomSizeItem

    @Query("SELECT distinct artist FROM MusicMetaData")
    fun getAllArtist(): Flow<List<RoomArtistItem>>

    @Query("SELECT distinct album FROM MusicMetaData")
    fun getAllAlbum(): Flow<List<RoomAlbumItem>>

    @Query("SELECT distinct album FROM MusicMetaData where artist IN (:listArtists)")
    fun getAlbumByArtistsList(listArtists : Array<String>): List<RoomAlbumItem>
}