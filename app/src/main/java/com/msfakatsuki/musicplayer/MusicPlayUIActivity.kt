package com.msfakatsuki.musicplayer

import android.content.ComponentName
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.browse.MediaBrowser
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.activity.viewModels
import androidx.navigation.fragment.NavHostFragment
import com.msfakatsuki.musicplayer.databinding.MusicPlayUiActivityBinding
import com.msfakatsuki.musicplayer.ui.play.MusicPlayUIFragment
import com.msfakatsuki.musicplayer.ui.play.MusicPlayUIViewModel

class MusicPlayUIActivity : AppCompatActivity() {

    private lateinit var mediaBrowser: MediaBrowserCompat
    val viewModel : MusicPlayUIViewModel by viewModels()

    private lateinit var binding:MusicPlayUiActivityBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = MusicPlayUiActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mediaBrowser = MediaBrowserCompat(
            this,
            ComponentName(this, MusicPlaybackService::class.java),
            connectionCallbacks,
            null
        )
        /*
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.container, MusicPlayUIFragment.newInstance())
                .commitNow()
        }*/
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_music_play) as NavHostFragment
        // val navController = navHostFragment.navController

        mediaBrowser.connect()

        //mediaBrowser.subscribe(mediaBrowser.root,subscriptionCallback)
    }

    override fun onStart() {

        super.onStart()
    }

    override fun onResume() {
        super.onResume()
        volumeControlStream = AudioManager.STREAM_MUSIC
    }

    override fun onStop() {

        super.onStop()
        MediaControllerCompat.getMediaController(this)?.unregisterCallback(controllerCallbacks)
    }

    override fun onDestroy() {
        mediaBrowser.disconnect()
        super.onDestroy()
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

            viewModel.isServiceConnected.value = true
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
        val mediaController = MediaControllerCompat.getMediaController(this)
        // TODO("Not yet implemented")

        mediaController.registerCallback(controllerCallbacks)
    }

    private var controllerCallbacks = object  : MediaControllerCompat.Callback() {
        override fun onMetadataChanged(metadata: MediaMetadataCompat?) {
            super.onMetadataChanged(metadata)
        }

        override fun onPlaybackStateChanged(state: PlaybackStateCompat?) {
            super.onPlaybackStateChanged(state)
        }
    }

    private var subscriptionCallback = object : MediaBrowserCompat.SubscriptionCallback() {
        override fun onChildrenLoaded(
            parentId: String,
            children: MutableList<MediaBrowserCompat.MediaItem>
        ) {
            super.onChildrenLoaded(parentId, children)
        }

        override fun onChildrenLoaded(
            parentId: String,
            children: MutableList<MediaBrowserCompat.MediaItem>,
            options: Bundle
        ) {
            super.onChildrenLoaded(parentId, children, options)
        }
    }
}