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
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import androidx.activity.viewModels
import androidx.navigation.fragment.NavHostFragment
import com.msfakatsuki.musicplayer.databinding.MusicPlayUiActivityBinding
import com.msfakatsuki.musicplayer.ui.play.MusicPlayUIFragment
import com.msfakatsuki.musicplayer.ui.play.MusicPlayUIViewModel
import com.msfakatsuki.musicplayer.ui.play.SongItemFragment

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
    }

    override fun onDestroy() {
        Log.println(Log.INFO,"mpuia","onDestroy" +
                "")
        mediaBrowser.disconnect()
        MediaControllerCompat.getMediaController(this)?.unregisterCallback(controllerCallbacks)
        super.onDestroy()
    }

    private val connectionCallbacks =object : MediaBrowserCompat.ConnectionCallback() {

        override fun onConnected() {
            mediaBrowser.sessionToken.also { token ->
                val mediaController = MediaControllerCompat(
                    this@MusicPlayUIActivity,
                    token
                )
                mediaController.registerCallback(controllerCallbacks)
                MediaControllerCompat.setMediaController(this@MusicPlayUIActivity,mediaController)
            }

            viewModel.isServiceConnected.value = true
            mediaBrowser.subscribe(mediaBrowser.root,subscriptionCallback)
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

    }



    private var controllerCallbacks = object  : MediaControllerCompat.Callback() {
        override fun onMetadataChanged(metadata: MediaMetadataCompat?) {
            super.onMetadataChanged(metadata)

        }

        override fun onPlaybackStateChanged(state: PlaybackStateCompat?) {
            Log.println(Log.INFO,"mpuia","onPlaybackStateChanged")
            super.onPlaybackStateChanged(state)
        }

        override fun onQueueChanged(queue: MutableList<MediaSessionCompat.QueueItem>?) {
            Log.println(Log.INFO,"mpuia","onQueueChanged")
            val playlist = supportFragmentManager.findFragmentById(R.id.playlist)
            viewModel.SongPlayingList=queue
            playlist?.let { it ->
                (it as SongItemFragment).bindList(viewModel.SongPlayingList)
            }
            super.onQueueChanged(queue)
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

    fun onPlaylistChanged() {
        mediaBrowser.subscribe(mediaBrowser.root,subscriptionCallback)

    }
}