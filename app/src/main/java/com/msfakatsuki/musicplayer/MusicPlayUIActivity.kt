package com.msfakatsuki.musicplayer

import android.content.ComponentName
import android.media.AudioManager
import android.media.MediaPlayer
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.PlaybackStateCompat
import com.msfakatsuki.musicplayer.ui.play.MusicPlayUIFragment

class MusicPlayUIActivity : AppCompatActivity() {

    private lateinit var mediaBrowser: MediaBrowserCompat

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.music_play_ui_activity)

        mediaBrowser = MediaBrowserCompat(
            this,
            ComponentName(this, MusicPlaybackService::class.java),
            connectionCallbacks,
            null
        )

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.container, MusicPlayUIFragment.newInstance())
                .commitNow()
        }

    }

    override fun onStart() {
        super.onStart()
        mediaBrowser.connect()
    }

    override fun onResume() {
        super.onResume()
        volumeControlStream = AudioManager.STREAM_MUSIC
    }

    override fun onStop() {
        super.onStop()
        MediaControllerCompat.getMediaController(this)?.unregisterCallback(controllerCallbacks)
        mediaBrowser.disconnect()
    }

    private val connectionCallbacks =object : MediaBrowserCompat.ConnectionCallback() {

        override fun onConnected() {
            mediaBrowser.sessionToken.also { token ->
                val mediaController = MediaControllerCompat(
                    this@MusicPlayUIActivity,
                    token
                )

                MediaControllerCompat.setMediaController(this@MusicPlayUIActivity,mediaController)
            }

            buildTransportControls()

            super.onConnected()
        }

        override fun onConnectionSuspended() {
            super.onConnectionSuspended()
        }

        override fun onConnectionFailed() {
            super.onConnectionFailed()
        }

    }

    private fun buildTransportControls() {
        TODO("Not yet implemented")
    }

    private var controllerCallbacks = object  : MediaControllerCompat.Callback() {
        override fun onMetadataChanged(metadata: MediaMetadataCompat?) {
            super.onMetadataChanged(metadata)
        }

        override fun onPlaybackStateChanged(state: PlaybackStateCompat?) {
            super.onPlaybackStateChanged(state)
        }
    }

}