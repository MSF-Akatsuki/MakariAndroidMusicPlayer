package com.msfakatsuki.musicplayer

import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import androidx.media.MediaBrowserServiceCompat
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import com.msfakatsuki.musicplayer.util.DetailedMediaPlayer
import kotlinx.coroutines.*
import java.lang.Runnable
import kotlin.random.Random
import kotlin.random.nextInt


class MusicPlaybackService : MediaBrowserServiceCompat() {

    companion object{
        private val LOG_TAG = "makari_mbs"


        val PLAYBACK_SEQUENCIAL = 0
        val PLAYBACK_SEQUENCIAL_LOOP = 1
        val PLAYBACK_SHUFFLED = 2
        val PLAYBACK_SINGLE_LOOP = 3

        val STATE_NEED_PREPARE = 0
        val STATE_PREPARING = 1
        val STATE_PREPARED = 2

        val HINT_IS_PLAYER = "hint_is_player"
        val HINT_IS_PLAYLIST = "hint_is_playlist"
    }

    private val coroutineScope = CoroutineScope(Job() + Dispatchers.IO)

    private val MAK_MUSIC_EMPTY_ROOT_ID = "EMPTY"
    private val MAK_MUSIC_ROOT_ID = "ROOT"
    private val MAK_MUSIC_PLAYER_ROOT_ID = "PLAYER"
    private val MAK_MUSIC_PLAYLIST_ROOT_ID = "PLAYLIST"

    private var mMediaPlayer: DetailedMediaPlayer?=null
    private var mpStatePrepare: Int = STATE_NEED_PREPARE

    private var mediaSession: MediaSessionCompat?=null
    private lateinit var stateBuilder: PlaybackStateCompat.Builder

    private val lcg = LinearRandomGenerator()
    private val mSongPlayList = arrayListOf<MediaSessionCompat.QueueItem>()
    private val mMediaIdSet = mutableSetOf<Long>()
    private val playlistSize get() = mSongPlayList.size

    private var songId : Int=0

    private var playBackType:Int = PLAYBACK_SEQUENCIAL

    private var handler : Handler?=null

    private var playingMetadata:MediaMetadataCompat?=null


    override fun onCreate() {
        super.onCreate()
        Log.println(Log.INFO,"mpbService","onCreate")
        mediaSession = MediaSessionCompat(baseContext, LOG_TAG).apply {

            setFlags(MediaSessionCompat.FLAG_HANDLES_QUEUE_COMMANDS)

            stateBuilder = PlaybackStateCompat.Builder()
                .setActions(
                    PlaybackStateCompat.ACTION_PLAY or
                    PlaybackStateCompat.ACTION_PLAY_PAUSE or
                    PlaybackStateCompat.ACTION_PAUSE or
                    PlaybackStateCompat.ACTION_STOP or
                    PlaybackStateCompat.ACTION_SEEK_TO or
                    PlaybackStateCompat.ACTION_SKIP_TO_NEXT or
                    PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS
                )
            setPlaybackState(stateBuilder.build())

            setCallback(callback)

            setSessionToken(sessionToken)
        }

        handler = Handler(Looper.getMainLooper())
        handler?.postDelayed(iTicker,20)
    }


    private val iTicker : Runnable = object : Runnable{
        override fun run() {
            handler?.removeCallbacksAndMessages(this)

            mMediaPlayer?.let { mMediaPlayer ->
                val currentPosition = mMediaPlayer.currentPosition
                if (mMediaPlayer.isPlaying) {
                    mediaSession?.setPlaybackState(
                        stateBuilder.setState(
                            PlaybackStateCompat.STATE_PLAYING, currentPosition.toLong(), 1.0F
                        ).build()
                    )
                } else  {
                    /*
                    mediaSession?.setPlaybackState(
                        stateBuilder.setState(
                            PlaybackStateCompat.STATE_PAUSED, currentPosition.toLong(), 1.0F
                        ).build()
                    )*/
                }
            }

            handler?.postDelayed(this, 100)
        }
    }


    override fun onDestroy() {
        Log.println(Log.INFO,"mpbService","onDestroy")
        handler?.removeCallbacksAndMessages(null)
        coroutineScope.cancel()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? {
        startService(Intent(baseContext,MediaBrowserServiceCompat::class.java))
        Log.println(Log.INFO,"mpbService","onBind")
        return super.onBind(intent)
    }

    override fun onGetRoot(
        clientPackageName: String,
        clientUid: Int,
        rootHints: Bundle?
    ): BrowserRoot? {
        Log.println(Log.INFO,"mpbService","onGetRoot")
        if (clientPackageName == "com.msfakatsuki.musicplayer") {
            return if (rootHints?.getBoolean(HINT_IS_PLAYER) == true)
                BrowserRoot(MAK_MUSIC_PLAYER_ROOT_ID,null)
            else if (rootHints?.getBoolean(HINT_IS_PLAYLIST) == true)
                BrowserRoot(MAK_MUSIC_PLAYLIST_ROOT_ID,null)
            else
                BrowserRoot(MAK_MUSIC_ROOT_ID,null)
        }
        else
            return BrowserRoot(MAK_MUSIC_EMPTY_ROOT_ID,null)
    }

    override fun onLoadChildren(
        parentId: String,
        result: Result<MutableList<MediaBrowserCompat.MediaItem>>,
        options: Bundle
    ) {
        when(parentId) {
            MAK_MUSIC_ROOT_ID -> {
                if (options.getBoolean(HINT_IS_PLAYER)){
                    result.sendResult(
                        if( playlistSize > 0 )
                            mutableListOf(MediaBrowserCompat.MediaItem(mSongPlayList[songId % playlistSize].description, MediaBrowserCompat.MediaItem.FLAG_PLAYABLE))
                        else
                            null
                    )
                }
                else if (options.getBoolean(HINT_IS_PLAYLIST))
                    result.sendResult(MutableList<MediaBrowserCompat.MediaItem>(
                        playlistSize
                    ) { index ->
                        MediaBrowserCompat.MediaItem(
                            mSongPlayList[index].description,
                            MediaBrowserCompat.MediaItem.FLAG_PLAYABLE
                        )
                    })
                else super.onLoadChildren(parentId, result, options)
            }
            else -> super.onLoadChildren(parentId, result, options)
        }

    }

    override fun onLoadChildren(
        parentId: String,
        result: Result<MutableList<MediaBrowserCompat.MediaItem>>
    ) {

        Log.println(Log.INFO,"mpbService","onLoadChildren")

        when (parentId) {
            MAK_MUSIC_ROOT_ID->result.sendResult(null)
            else->result.sendResult(null)
        }
        return
    }

    private lateinit var audioFocusRequest : AudioFocusRequest
    private val callback = object: MediaSessionCompat.Callback() {
        override fun onPlay() {

            Log.println(Log.INFO,"mec_callback","on_play")

            if(playlistSize<=0) return

            val am = baseContext.getSystemService(Context.AUDIO_SERVICE) as AudioManager

            audioFocusRequest=AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN).run {
                setOnAudioFocusChangeListener(afChangeListener)
                setAudioAttributes(AudioAttributes.Builder().run{
                    setUsage(AudioAttributes.USAGE_MEDIA)
                    setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    build()
                })
                build()
            }
            val result = am.requestAudioFocus(audioFocusRequest)
            if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                startService(Intent(baseContext,MediaBrowserServiceCompat::class.java))
                mediaSession?.isActive = true
                mMediaPlayer?.let{ mMediaPlayer ->
                    if (mpStatePrepare == STATE_PREPARED) {
                        mMediaPlayer.start()
                    } else if(mpStatePrepare == STATE_NEED_PREPARE) {
                        mMediaPlayer.prepareAsync()
                    }
                }?: kotlin.run {
                    mMediaPlayer = createMediaPlayer()
                    if (playlistSize > 0 ) {
                        mSongPlayList[0].description.mediaUri?.let {
                            Log.println(Log.INFO,"mpbService","Set data source of mediaplayer${it.toString()}")
                            mMediaPlayer!!.setDataSourceByDescription(baseContext, mSongPlayList[0].description)
                            songId = 0
                            mMediaPlayer!!.prepareAsync()
                        }
                    } else {
                        Log.println(Log.INFO,"mpbService","Nothing. playListSize is ${playlistSize}  ${mSongPlayList.size}")
                    }
                }
            }

            super.onPlay()
        }


        override fun onPause() {
            Log.println(Log.INFO,"mec_callback","on_pause")
            mMediaPlayer?.pause()
            stopForeground(false)
            super.onPause()
        }

        override fun onStop() {
            Log.println(Log.INFO,"mec_callback","on_Stop")
            mpStatePrepare = STATE_NEED_PREPARE
            mMediaPlayer?.stop()
            stopSelf()
            stopForeground(false)
            super.onStop()
        }


        override fun onAddQueueItem(description: MediaDescriptionCompat?) {
            Log.println(Log.INFO,"mpbService","onAddQueueItem")

            val id = description?.extras?.getLong("id")
            id?:return

            if (mMediaIdSet.contains(id))
                return
            else
                mMediaIdSet.add(id)

            if (playBackType== PLAYBACK_SHUFFLED) {
                songId %= playlistSize
            }

            mSongPlayList.add(MediaSessionCompat.QueueItem(description, id) )
            mediaSession?.setQueue(mSongPlayList)
            if (playlistSize == 1) {
                mMediaPlayer?:run{ mMediaPlayer = createMediaPlayer() }
                mMediaPlayer?.setDataSourceByDescription(baseContext,mSongPlayList[0].description)
                songId = 0
            }

            if(playBackType == PLAYBACK_SHUFFLED) {
                songId += Random.nextInt(16384) * playlistSize
            }

            super.onAddQueueItem(description)
        }

        override fun onSeekTo(pos: Long) {
            super.onSeekTo(pos)
            if (mpStatePrepare == STATE_PREPARED) {
                mMediaPlayer?.seekTo(pos.toInt())
            }
        }

        override fun onSkipToNext() {
            super.onSkipToNext()
            skipToNext(true)
        }

        override fun onSkipToPrevious() {
            super.onSkipToPrevious()
            skipToPrevious(true)
        }

        override fun onAddQueueItem(description: MediaDescriptionCompat?, index: Int) {
            super.onAddQueueItem(description, index)
        }

        override fun onRemoveQueueItem(description: MediaDescriptionCompat?) {
            super.onRemoveQueueItem(description)
        }

        override fun onSetRepeatMode(repeatMode: Int) {
            super.onSetRepeatMode(repeatMode)
            if (playBackType != repeatMode) {
                playBackType = repeatMode
                songId %= playlistSize
                if (repeatMode == PLAYBACK_SHUFFLED){
                    songId += Random.nextInt(16384) * playlistSize
                }
            }
        }

        override fun onCustomAction(action: String?, extras: Bundle?) {
            if (action == "CA_REMOVE_ALL_FROM_QUEUE") {
                mMediaPlayer=null
                mSongPlayList.removeIf { true }
                mMediaIdSet.removeIf { true }
                mediaSession?.setQueue(mSongPlayList)
            }
            super.onCustomAction(action, extras)
        }

    }



    private var afChangeListener =object: AudioManager.OnAudioFocusChangeListener {
        override fun onAudioFocusChange(focusChange: Int) {
            when(focusChange) {
                AudioManager.AUDIOFOCUS_LOSS -> {
                    mediaSession?.controller?.transportControls?.pause()
                }
                AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> {
                    mediaSession?.controller?.transportControls?.pause()
                }
                AudioManager.AUDIOFOCUS_GAIN -> {
                    mediaSession?.controller?.transportControls?.play()
                }
            }
        }
    }

    fun createMediaPlayer():DetailedMediaPlayer {
        val mediaPlayer = DetailedMediaPlayer(coroutineScope,onPlayerMetaLoadedListener)
        mediaPlayer.setOnPreparedListener {
            val mp = it as DetailedMediaPlayer
            mpStatePrepare = STATE_PREPARED
            if(mp.isMetadataLoaded) updateMetadata(mp)
            it.start()
        }
        mediaPlayer.setOnCompletionListener {
            skipToNext()
        }
        return mediaPlayer
    }

    private fun skipToPrevious(isForced: Boolean=true)= mMediaPlayer?.let { it->
        if (isForced) {
            var nextState:Int = PlaybackStateCompat.STATE_PLAYING
            nextState = STATE_NEED_PREPARE
            it.reset()
            if (playlistSize > 0){
                if((isForced && playBackType == PLAYBACK_SEQUENCIAL) || playBackType == PLAYBACK_SEQUENCIAL_LOOP) {
                        it.setDataSourceByDescription(this,mSongPlayList[(songId - 1 + playlistSize)%playlistSize].description)
                        it.prepareAsync()
                        songId = (songId - 1 + playlistSize)%playlistSize
                } else if (playBackType == PLAYBACK_SHUFFLED) {
                    songId = lcg.prior(songId)
                    it.setDataSourceByDescription(this,mSongPlayList[songId%playlistSize].description)
                    it.prepareAsync()
                }
            }
            mpStatePrepare = nextState
        }
    }

    private fun skipToNext(isForced: Boolean=false) = mMediaPlayer?.let { it->


        // Only when it is doing a playback sequencial queue is nextSate STATE_PAUSED
        var nextState:Int = PlaybackStateCompat.STATE_PLAYING

        if (isForced || playBackType != PLAYBACK_SINGLE_LOOP) {
            nextState = STATE_NEED_PREPARE
            it.reset()
        }

        if (playlistSize > 0){
            if (!isForced && playBackType == PLAYBACK_SEQUENCIAL) {
                if (songId + 1 < playlistSize) {
                    /*Todo: Figure out how to avoid !! cast*/
                    it.setDataSourceByDescription(this,mSongPlayList[songId + 1].description)
                    it.prepareAsync()
                    songId += 1
                } else {
                    it.setDataSourceByDescription(this,mSongPlayList[0].description)
                    nextState = PlaybackStateCompat.STATE_PAUSED
                    songId = 0
                }
            } else if ((isForced && playBackType == PLAYBACK_SEQUENCIAL) || playBackType == PLAYBACK_SEQUENCIAL_LOOP) {
                if (songId + 1 < playlistSize) {
                    it.setDataSourceByDescription(this,mSongPlayList[songId + 1].description)
                    it.prepareAsync()
                    songId += 1
                } else {
                    it.setDataSourceByDescription(this,mSongPlayList[0].description)
                    it.prepareAsync()
                    songId = 0
                }
            } else if (playBackType == PLAYBACK_SHUFFLED) {
                songId = lcg.next(songId)
                it.setDataSourceByDescription(this,mSongPlayList[songId%playlistSize].description)
                it.prepareAsync()
            } else if (!isForced && playBackType == PLAYBACK_SINGLE_LOOP) {
                it.start()
            }
        }
        mpStatePrepare = nextState
    }


    val onPlayerMetaLoadedListener = object : DetailedMediaPlayer.OnMetadataLoadedListener() {
        override fun onMetadataLoaded(mp: DetailedMediaPlayer) {
            super.onMetadataLoaded(mp)
            if (mp.statePrepare == DetailedMediaPlayer.STATE_PREPARED) {
                updateMetadata(mp)
            } else {
                mp.let { player->
                    val builder = player.meta?.let{ meta-> MediaMetadataCompat.Builder(meta) }
                    builder?.let { it->
                        it.putLong(MediaMetadataCompat.METADATA_KEY_DURATION,0)
                        mediaSession?.setMetadata(it.build())
                    }
                }
            }
        }
    }

    private fun updateMetadata(mp: DetailedMediaPlayer) {
        mp.let { player->
            val builder = player.meta?.let{ meta-> MediaMetadataCompat.Builder(meta) }
            builder?.let { it->
                it.putLong(MediaMetadataCompat.METADATA_KEY_DURATION,player.duration.toLong())
                mediaSession?.setMetadata(it.build())
            }
        }
    }

    class LinearRandomGenerator(
        private val modulo:Long=2147483647,
        private val a : Long = 16807,
        private val aRev : Long = 1407677000,
        private val c : Long = 0
    ) {
        
        fun next(v : Int) = ((a*v+c)%modulo).toInt()
        fun prior(v : Int)=((v-c)*aRev%modulo).toInt()
    }
}
