package com.msfakatsuki.musicplayer

import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.MediaPlayer
import androidx.media.MediaBrowserServiceCompat
import android.os.Bundle
import android.os.IBinder
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaDescriptionCompat
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

    private var mMediaPlayer: MediaPlayer?=null
    private var mpStatePrepare: Int = STATE_NEED_PREPARE

    private var mediaSession: MediaSessionCompat?=null
    private lateinit var stateBuilder: PlaybackStateCompat.Builder

    private val mSongPlayList = arrayListOf<MediaDescriptionCompat>()
    private val shuffledIdList = arrayListOf<Int>()
    private val playlistSize get() = mSongPlayList.size
    private var songId = -1
    private var playBackType:Int = PLAYBACK_SEQUENCIAL

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

    }

    override fun onDestroy() {
        Log.println(Log.INFO,"mpbService","onDestroy")
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
        return BrowserRoot(MAK_MUSIC_EMPTY_ROOT_ID,null)
    }

    override fun onLoadChildren(
        parentId: String,
        result: Result<MutableList<MediaBrowserCompat.MediaItem>>
    ) {
        Log.println(Log.INFO,"mpbService","onLoadChildren")
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
                    if (playlistSize > 0 && mSongPlayList[0].mediaUri != null) {
                        mSongPlayList[0].mediaUri?.let {
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
            description?:run{
                Log.println(Log.WARN,"mpbService", "MediaUri is null")
                return
            }
            mSongPlayList.add(description)
            shuffledIdList.add(playlistSize-1)
            Log.println(Log.INFO,"mpbService","Nothing. playListSize is ${playlistSize}  ${mSongPlayList.size}")
            if (playBackType== PLAYBACK_SHUFFLED) {
                val realSongId = shuffledIdList[songId]
                shuffledIdList.shuffle()
                songId = shuffledIdList.indexOf(realSongId)
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
            it.start()
        }
        mediaPlayer.setOnCompletionListener {
            skipToNext()
        }
        return mediaPlayer
    }


    private fun skipToNext(isForced: Boolean=false) = mMediaPlayer?.let { it->

        if (isForced || playBackType != PLAYBACK_SINGLE_LOOP) {
            mpStatePrepare = STATE_NEED_PREPARE
            it.reset()
        }

        if (playlistSize > 0){
            if (!isForced && playBackType == PLAYBACK_SEQUENCIAL) {
                if (songId + 1 < playlistSize) {
                    /*Todo: Figure out how to avoid !! cast*/
                    it.setDataSource(this,mSongPlayList[songId + 1].mediaUri!!)
                    it.prepareAsync()
                    songId += 1
                } else {
                    it.setDataSource(this,mSongPlayList[0].mediaUri!!)
                    songId = 0
                }
            } else if ((isForced && playBackType == PLAYBACK_SEQUENCIAL) || playBackType == PLAYBACK_SEQUENCIAL_LOOP) {
                if (songId + 1 < playlistSize) {
                    it.setDataSource(this,mSongPlayList[songId + 1].mediaUri!!)
                    it.prepareAsync()
                    songId += 1
                } else {
                    it.setDataSource(this,mSongPlayList[0].mediaUri!!)
                    it.prepareAsync()
                    songId = 0
                }
            } else if (playBackType == PLAYBACK_SHUFFLED) {
                if (songId + 1 < playlistSize) {
                    it.setDataSource(this,mSongPlayList[shuffledIdList[songId + 1]].mediaUri!!)
                    it.prepareAsync()
                    songId += 1
                } else {
                    it.setDataSource(this,mSongPlayList[shuffledIdList[0]].mediaUri!!)
                    it.prepareAsync()
                    songId = 0
                }
            } else if (!isForced && playBackType == PLAYBACK_SINGLE_LOOP) {
                it.start()
            }
        }
    }

    class MakSongData(
        public val path: String,
        public val title: String?,
        public val artist: String?,
        public val album: String?
    )
}