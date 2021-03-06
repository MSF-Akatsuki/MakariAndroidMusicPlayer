package com.msfakatsuki.musicplayer.util

import android.content.Context
import android.graphics.Bitmap
import android.media.MediaMetadata
import android.media.MediaMetadataRetriever
import android.media.MediaPlayer
import android.net.Uri
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.MediaMetadataCompat
import android.util.Log
import androidx.appcompat.content.res.AppCompatResources.getDrawable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.textInputServiceFactory
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.bumptech.glide.Glide
import com.msfakatsuki.musicplayer.R
import kotlinx.coroutines.*

class DetailedMediaPlayer(
    val coroutineScope: CoroutineScope,
    val onMetadataLoadedListener:OnMetadataLoadedListener?
) : MediaPlayer() {

    companion object{
        val STATE_NEED_PREPARE = 0
        val STATE_PREPARING = 1
        val STATE_PREPARED = 2
    }

    var statePrepare: Int = STATE_NEED_PREPARE
    var isMetadataLoaded = false

    var meta: MediaMetadataCompat?=null
    private val mmr = MediaMetadataRetriever()

    override fun reset() {
        statePrepare = STATE_NEED_PREPARE
        super.reset()
    }

    fun setDataSourceByDescription(context: Context, description: MediaDescriptionCompat) {
        val uri = description.mediaUri!!

        val title: String = (description.title ?: "") as String
        val album = description.extras?.getString("album", "")
        val artist = description.extras?.getString("artist", "")
        val mediaUri = description.mediaUri?.toString()
        val iconUri = description.iconUri?.toString()

        coroutineScope.launch(Dispatchers.IO){

            meta = MediaMetadataCompat.Builder().run {
                putString(MediaMetadataCompat.METADATA_KEY_ALBUM, album)
                putString(MediaMetadataCompat.METADATA_KEY_ARTIST, artist)
                putString(MediaMetadataCompat.METADATA_KEY_TITLE, title)
                putString(MediaMetadataCompat.METADATA_KEY_MEDIA_URI,mediaUri)
                putString(MediaMetadataCompat.METADATA_KEY_DISPLAY_ICON_URI,iconUri)
                build()
            }
            onMetadataLoadedListener?.onMetadataLoaded(this@DetailedMediaPlayer)
        }


        super.setDataSource(context, uri)
    }

    override fun prepareAsync() {
        statePrepare = STATE_PREPARING
        super.prepareAsync()
    }

    override fun setOnPreparedListener(listener: OnPreparedListener?) {
        super.setOnPreparedListener{
            statePrepare = STATE_PREPARED
            listener?.onPrepared(this)
        }
    }

    fun clearUpScope() {
        coroutineScope.cancel()
    }

    abstract class OnMetadataLoadedListener {
        open fun onMetadataLoaded(mp: DetailedMediaPlayer) {
            mp.isMetadataLoaded = true
        }
    }


}