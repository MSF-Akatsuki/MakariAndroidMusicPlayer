package com.msfakatsuki.musicplayer

import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.session.MediaSession
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.provider.MediaStore
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.media.MediaBrowserServiceCompat
import java.net.URI

class MusicPlaybackService : MediaBrowserServiceCompat() {

    companion object{
        private val LOG_TAG = "makari_mbs"


        val PLAYBACK_SEQUENCIAL = 0
        val PLAYBACK_SEQUENCIAL_LOOP = 1
        val PLAYBACK_SHUFFLED = 2
        val PLAYBACK_SINGLE_LOOP = 3

    }

    private var mMediaPlayer: MediaPlayer?=null
    private var mediaSession: MediaSessionCompat?=null
    private lateinit var stateBuilder: PlaybackStateCompat.Builder

    private val mSongPlayList = arrayListOf<MakSongData?>()
    private val shuffledIdList = arrayListOf<Int>()
    private val playlistSize get() = mSongPlayList.size
    private var songId = -1
    private var playBackType:Int = 0

    override fun onCreate() {
        super.onCreate()

        mediaSession = MediaSessionCompat(baseContext, LOG_TAG).apply {

            stateBuilder = PlaybackStateCompat.Builder()
                .setActions(PlaybackStateCompat.ACTION_PLAY or PlaybackStateCompat.ACTION_PLAY_PAUSE)
            setPlaybackState(stateBuilder.build())

            setCallback(callback)

            setSessionToken(sessionToken)
        }

    }

    override fun onGetRoot(
        clientPackageName: String,
        clientUid: Int,
        rootHints: Bundle?
    ): BrowserRoot? {
        return BrowserRoot(MAK_MUSIC_ROOT_ID,null)
        TODO("Logic For control")
    }

    override fun onLoadChildren(
        parentId: String,
        result: Result<MutableList<MediaBrowserCompat.MediaItem>>
    ) {



    }
    private lateinit var afChangeListener : AudioManager.OnAudioFocusChangeListener
    private lateinit var audioFocusRequest : AudioFocusRequest
    private val callback = object: MediaSessionCompat.Callback() {
        override fun onPlay() {
            val am = baseContext.getSystemService(Context.AUDIO_SERVICE) as AudioManager

            audioFocusRequest=AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN).run {
                setOnAudioFocusChangeListener(afChangeListener)
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

                mMediaPlayer?: kotlin.run {
                    mMediaPlayer = createMediaPlayer()
                }



            }

            super.onPlay()
        }

        override fun onPause() {
            mMediaPlayer?.pause()
            super.onPause()
        }

        override fun onStop() {
            mMediaPlayer?.stop()
            super.onStop()
        }
    }

    fun createMediaPlayer():MediaPlayer {
        val mediaPlayer = MediaPlayer()
        mediaPlayer.setOnPreparedListener {
            it.start()
        }
        mediaPlayer.setOnCompletionListener {


            if (playBackType != PLAYBACK_SINGLE_LOOP) it.reset()

            if (playBackType == PLAYBACK_SEQUENCIAL) {
                if (songId + 1 < playlistSize) {
                    it.setDataSource(mSongPlayList[songId + 1]?.path)
                    it.prepareAsync()
                    songId += 1
                } else {
                    it.setDataSource(mSongPlayList[0]?.path)
                    songId = 0
                }
            } else if (playBackType == PLAYBACK_SEQUENCIAL_LOOP) {
                if (songId + 1 < playlistSize) {
                    it.setDataSource(mSongPlayList[songId + 1]?.path)
                    it.prepareAsync()
                    songId += 1
                } else {
                    it.setDataSource(mSongPlayList[0]?.path)
                    it.prepareAsync()
                    songId = 0
                }
            } else if (playBackType == PLAYBACK_SHUFFLED) {
                if (songId + 1 < playlistSize) {
                    it.setDataSource(mSongPlayList[shuffledIdList[songId + 1]]?.path)
                    it.prepareAsync()
                    songId += 1
                } else {
                    it.setDataSource(mSongPlayList[shuffledIdList[0]]?.path)
                    it.prepareAsync()
                    songId = 0
                }
            } else if (playBackType == PLAYBACK_SINGLE_LOOP) {
                it.start()
            }
        }

        return mediaPlayer
    }

    class MakSongData(
        public val path: String,
        public val title: String?,
        public val artist: String?,
        public val album: String?
    )
}

