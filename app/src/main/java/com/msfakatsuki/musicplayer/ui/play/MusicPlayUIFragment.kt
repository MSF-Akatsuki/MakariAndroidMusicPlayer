package com.msfakatsuki.musicplayer.ui.play

import android.content.ComponentName
import android.media.MediaMetadataRetriever
import android.media.session.MediaController
import android.net.Uri
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.provider.MediaStore
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.msfakatsuki.musicplayer.MusicApplication
import com.msfakatsuki.musicplayer.MusicPlayUIActivity
import com.msfakatsuki.musicplayer.MusicPlaybackService
import com.msfakatsuki.musicplayer.R
import com.msfakatsuki.musicplayer.database.music.RoomMusicItem
import com.msfakatsuki.musicplayer.databinding.MusicPlayUiFragmentBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MusicPlayUIFragment : Fragment() {

    companion object {
        fun newInstance() = MusicPlayUIFragment()
    }

    private val viewModel: MusicPlayUIViewModel by activityViewModels()
    private var _binding: MusicPlayUiFragmentBinding?=null
    protected val binding get() = _binding!!

    private lateinit var mediaBrowser: MediaBrowserCompat


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Log.println(Log.INFO,"mpuiFrag","OnCreateView")

        _binding = MusicPlayUiFragmentBinding.inflate(inflater,container,false)
        binding.progressBar.duration = viewModel.duration
        binding.progressBar.currentPosition = viewModel.currentPosition

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.println(Log.INFO,"mpuiFrag","OnViewCreated")
        val serviceConnectionObserver = Observer<Boolean> {
            if (it) {
                registerTransportControls()
            } else {
                unregisterTransportControls()
            }
        }

        val mediaController = MediaControllerCompat.getMediaController(requireActivity())
        mediaController.registerCallback(controllerCallbacks)

        binding.btnTransfer.setOnClickListener {
            findNavController().navigate(R.id.action_musicPlayUIFragment_to_songItemFragment2)
        }
        viewModel.isServiceConnected.observe(viewLifecycleOwner, serviceConnectionObserver)


    }

    override fun onDestroyView() {
        super.onDestroyView()
        Log.println(Log.INFO,"mpuiF","onDestroyView")
        val mediaController = MediaControllerCompat.getMediaController(requireActivity())
        mediaController.unregisterCallback(controllerCallbacks)
    }

    private fun unregisterTransportControls() {
        binding.btnPlay.setOnClickListener(null)
        binding.btnStop.setOnClickListener(null)
        binding.btnPause.setOnClickListener(null)
        binding.btnChooseSong.setOnClickListener(null)
    }

    fun registerTransportControls() {
        val mediaController = MediaControllerCompat.getMediaController(requireActivity())
        binding.btnPlay.setOnClickListener {
             mediaController.transportControls.play()
        }
        binding.btnStop.setOnClickListener {
            mediaController.transportControls.stop()
        }
        binding.btnPause.setOnClickListener {
            mediaController.transportControls.pause()
        }
        binding.btnChooseSong.setOnClickListener {
            getContent.launch("audio/*")
        }
    }


    val mmr = MediaMetadataRetriever()

    val getContent = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri?->
        val mediaController = MediaControllerCompat.getMediaController(requireActivity())
        //mediaController.registerCallback((requireActivity() as MusicPlayUIActivity).controllerCallbacks)

        mmr.setDataSource(requireContext(),uri)

        val title = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE)
        val album = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM)
        val artist = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST)


        Log.println(Log.INFO,"get_content",uri.toString())
        val mdc:MediaDescriptionCompat = MediaDescriptionCompat.Builder().run {
            setMediaUri(uri)
            setTitle(title)
            build()
        }
        mediaController.addQueueItem(mdc)

        uri?.let {
            val item : RoomMusicItem?
            item = RoomMusicItem(
                id = 0, title = title?:"TITLE NULL",artist=artist?:"",album = album?:"", sha256 = "",localPath=it.path?:"", remoteLink = ""
            )
            val app = requireActivity().application as MusicApplication
            Log.println(Log.INFO,"mpuif","adding item")
            activity?.lifecycleScope?.launch(Dispatchers.IO) {
                app.repository.insert(item)
            }
        }
    }


    private var controllerCallbacks = object  : MediaControllerCompat.Callback() {
        override fun onMetadataChanged(metadata: MediaMetadataCompat?) {
            metadata?.let{
                viewModel.duration = it.getLong(MediaMetadataCompat.METADATA_KEY_DURATION) as Long
                binding.progressBar.duration = viewModel.duration
                binding.progressBar.invalidate()
            }
            super.onMetadataChanged(metadata)

        }

        override fun onPlaybackStateChanged(state: PlaybackStateCompat?) {
            state?.let {
                viewModel.currentPosition = it.position
                binding.progressBar.currentPosition = it.position
                binding.progressBar.invalidate()
            }
            Log.println(Log.DEBUG,"mpuiF","onPlaybackStateChanged")
            super.onPlaybackStateChanged(state)
        }

    }

}