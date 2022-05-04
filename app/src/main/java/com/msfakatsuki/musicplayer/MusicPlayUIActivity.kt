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
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
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
        mediaBrowser.connect()

        viewModel.fileReadProcessNumber.observe(this) {
            if (viewModel.fileDbProcessFlag.value==true) {
                
                viewModel.fileDbProcessFlag.value = false
            }
        }

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_music_play) as NavHostFragment
        val localNavController = navHostFragment.navController
        val appBarConfiguration = AppBarConfiguration(localNavController.graph)

        setupActionBarWithNavController(localNavController,appBarConfiguration)

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
            Log.i("MPS_CON","CONNECTED")
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
            super.onPlaybackStateChanged(state)
        }

        override fun onQueueChanged(queue: MutableList<MediaSessionCompat.QueueItem>?) {
            Log.println(Log.INFO,"mpuia","onQueueChanged")
            val playlist = supportFragmentManager.findFragmentById(R.id.playlist)
            viewModel.SongPlayingList.value=queue

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

}