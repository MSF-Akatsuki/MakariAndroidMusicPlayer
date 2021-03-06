package com.msfakatsuki.musicplayer

import android.Manifest
import android.content.ComponentName
import android.content.pm.PackageManager
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
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.PackageManagerCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.*
import com.msfakatsuki.musicplayer.databinding.MusicPlayUiActivityBinding
import com.msfakatsuki.musicplayer.ui.play.MusicPlayUIFragment
import com.msfakatsuki.musicplayer.ui.play.MusicPlayUIViewModel
import com.msfakatsuki.musicplayer.ui.play.PlayerViewPagerFragment
import com.msfakatsuki.musicplayer.ui.play.SongItemFragment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MusicPlayUIActivity : AppCompatActivity() {


    val viewModel : MusicPlayUIViewModel by viewModels()

    private var mediaBrowser: MediaBrowserCompat
        get() = viewModel.mediaBrowser
        set(value) {viewModel.mediaBrowser=value}

    private var _binding:MusicPlayUiActivityBinding?=null
    private val binding:MusicPlayUiActivityBinding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

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

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ) {
            ActivityCompat.requestPermissions(this,arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),1)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        grantResults.forEach {
            if (it!=PackageManager.PERMISSION_GRANTED) {
                finish()
                return@forEach
            }
        }
    }

    var OnBackPressedCallback: (() -> Boolean)?=null

    override fun onBackPressed() {
        OnBackPressedCallback?.let {
            if(!it()) super.onBackPressed()
        }?: super.onBackPressed()
    }

    override fun onSupportNavigateUp(): Boolean {
        _binding?.let { it->
            val appBarConfiguration = AppBarConfiguration(setOf(R.id.musicSelectFragment,R.id.musicFilterFragment),binding.container)
            return findNavController(R.id.nav_music_play).navigateUp(appBarConfiguration = appBarConfiguration) || super.onSupportNavigateUp()
        }?: return super.onSupportNavigateUp()

    }

    /*
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        Log.i("Activ","onOptionsItemSelected")
        return item.onNavDestinationSelected(findNavController(R.id.nav_music_play)) || super.onOptionsItemSelected(item)
    }
     */

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

                _binding = MusicPlayUiActivityBinding.inflate(layoutInflater)
                setContentView(binding.root)

                val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_music_play) as NavHostFragment
                val localNavController = navHostFragment.navController
                val appBarConfiguration = AppBarConfiguration(setOf(R.id.musicSelectFragment,R.id.musicFilterFragment),binding.container)

                setupActionBarWithNavController(localNavController,appBarConfiguration)

                binding.navView.setupWithNavController(localNavController)
            }

            viewModel.isServiceConnected.value = true
            mediaBrowser.subscribe(
                mediaBrowser.root,
                Bundle().apply {
                    putBoolean(MusicPlaybackService.HINT_IS_PLAYER,true)
                },
                subscriptionCallback)
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
            viewModel.songDescription.value = metadata
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
            if (children.size>0) {
                val description = children[0].description
                val uri = description.mediaUri!!

                val title: String = (description.title ?: "") as String
                val album = description.extras?.getString("album", "")
                val artist = description.extras?.getString("artist", "")
                val mediaUri = description.mediaUri?.toString()
                val iconUri = description.iconUri?.toString()



                viewModel.songDescription.value = MediaMetadataCompat.Builder().run {
                        putString(MediaMetadataCompat.METADATA_KEY_ALBUM, album)
                        putString(MediaMetadataCompat.METADATA_KEY_ARTIST, artist)
                        putString(MediaMetadataCompat.METADATA_KEY_TITLE, title)
                        putString(MediaMetadataCompat.METADATA_KEY_MEDIA_URI,mediaUri)
                        putString(MediaMetadataCompat.METADATA_KEY_DISPLAY_ICON_URI,iconUri)
                        build()
                    }

            }
            else
                viewModel.songDescription.value = null
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