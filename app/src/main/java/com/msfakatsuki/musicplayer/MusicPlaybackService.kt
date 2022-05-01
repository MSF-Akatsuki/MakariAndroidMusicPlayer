package com.msfakatsuki.musicplayer

import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.MediaPlayer
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
    }

    private val MAK_MUSIC_EMPTY_ROOT_ID = "EMPTY"
    private val MAK_MUSIC_PLAYLIST_ROOT_ID = "ROOT"

    private var mMediaPlayer: MediaPlayer?=null
    private var mpStatePrepare: Int = STATE_NEED_PREPARE

    private var mediaSession: MediaSessionCompat?=null
    private lateinit var stateBuilder: PlaybackStateCompat.Builder

    private val lcg = LinearRandomGenerator()
    private val mSongPlayList = arrayListOf<MediaSessionCompat.QueueItem>()
    private val playlistSize get() = mSongPlayList.size
    private var songId : Int = -1
    private var mediaId : Long = 0
    private var playBackType:Int = PLAYBACK_SEQUENCIAL

    private var handler : Handler?=null

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
                    PlaybackStateCompat.ACTION_SKIP_TO_NEXT
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
                } else {
                    mediaSession?.setPlaybackState(
                        stateBuilder.setState(
                            PlaybackStateCompat.STATE_PAUSED, currentPosition.toLong(), 1.0F
                        ).build()
                    )
                }
            }

            handler?.postDelayed(this, 800)
        }
    }


    override fun onDestroy() {
        Log.println(Log.INFO,"mpbService","onDestroy")
        handler?.removeCallbacksAndMessages(null)
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
        if (clientPackageName == "com.msfakatsuki.musicplayer")
            return BrowserRoot(MAK_MUSIC_PLAYLIST_ROOT_ID,null)
        else
            return BrowserRoot(MAK_MUSIC_EMPTY_ROOT_ID,null)
    }

    override fun onLoadChildren(
        parentId: String,
        result: Result<MutableList<MediaBrowserCompat.MediaItem>>
    ) {

        Log.println(Log.INFO,"mpbService","onLoadChildren")

        if (parentId==MAK_MUSIC_PLAYLIST_ROOT_ID)
            result.sendResult(MediaBrowserCompat.MediaItem.fromMediaItemList(mSongPlayList))
        else
            result.sendResult(null)
        return
    }
    private lateinit var afChangeListener : AudioManager.OnAudioFocusChangeListener
    private lateinit var audioFocusRequest : AudioFocusRequest
    private val callback = object: MediaSessionCompat.Callback() {
        override fun onPlay() {

            Log.println(Log.INFO,"mec_callback","on_play")

            val am = baseContext.getSystemService(Context.AUDIO_SERVICE) as AudioManager

            audioFocusRequest=AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN).run {
                //setOnAudioFocusChangeListener(afChangeListener)
                setAudioAttributes(AudioAttributes.Builder().run{
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
                            Log.println(Log.INFO,"mpbService","Set data source of mediaplayer")
                            mMediaPlayer!!.setDataSource(baseContext, it)
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

            description?:return

            if (playBackType== PLAYBACK_SHUFFLED) {
                songId %= playlistSize
            }

            mSongPlayList.add(MediaSessionCompat.QueueItem(description,mediaId++) )
            mediaSession?.setQueue(mSongPlayList)
            // mediaSession?.setPlaybackState(stateBuilder.setState(0,13, 1.0F).build())
            Log.println(Log.INFO,"mpbService","Nothing. playListSize is ${playlistSize}  ${mSongPlayList.size}")

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

        override fun onAddQueueItem(description: MediaDescriptionCompat?, index: Int) {
            super.onAddQueueItem(description, index)
        }

        override fun onRemoveQueueItem(description: MediaDescriptionCompat?) {
            super.onRemoveQueueItem(description)
        }

    }

    fun createMediaPlayer():MediaPlayer {
        val mediaPlayer = MediaPlayer()
        mediaPlayer.setOnPreparedListener {
            mpStatePrepare = STATE_PREPARED
            mediaSession?.setMetadata(
                MediaMetadataCompat.Builder().run {
                    putLong(MediaMetadataCompat.METADATA_KEY_DURATION, mediaPlayer.duration.toLong())
                    build()
                }
            )
            it.start()
        }
        mediaPlayer.setOnCompletionListener {
            skipToNext()
        }
        return mediaPlayer
    }


    private fun skipToNext(isForced: Boolean=false) = mMediaPlayer?.let { it->


        // Only when it is doing a playback sequencial queue is nextSate STATE_PAUSED
        var nextState:Int = PlaybackStateCompat.STATE_PLAYING

        if (isForced || playBackType != PLAYBACK_SINGLE_LOOP) {
            mpStatePrepare = STATE_NEED_PREPARE
            it.reset()
        }

        if (playlistSize > 0){
            if (!isForced && playBackType == PLAYBACK_SEQUENCIAL) {
                if (songId + 1 < playlistSize) {
                    /*Todo: Figure out how to avoid !! cast*/
                    it.setDataSource(this,mSongPlayList[songId + 1].description.mediaUri!!)
                    it.prepareAsync()
                    songId += 1
                } else {
                    it.setDataSource(this,mSongPlayList[0].description.mediaUri!!)
                    nextState = PlaybackStateCompat.STATE_PAUSED
                    songId = 0
                }
            } else if ((isForced && playBackType == PLAYBACK_SEQUENCIAL) || playBackType == PLAYBACK_SEQUENCIAL_LOOP) {
                if (songId + 1 < playlistSize) {
                    it.setDataSource(this,mSongPlayList[songId + 1].description.mediaUri!!)
                    it.prepareAsync()
                    songId += 1
                } else {
                    it.setDataSource(this,mSongPlayList[0].description.mediaUri!!)
                    it.prepareAsync()
                    songId = 0
                }
            } else if (playBackType == PLAYBACK_SHUFFLED) {
                songId = lcg.next(songId)
                it.setDataSource(this,mSongPlayList[songId%playlistSize].description.mediaUri!!)
                it.prepareAsync()
            } else if (!isForced && playBackType == PLAYBACK_SINGLE_LOOP) {
                it.start()
            }
        }



    }

    data class MakSongData(
        public val path: String,
        public val title: String?,
        public val artist: String?,
        public val album: String?
    )

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