package com.msfakatsuki.musicplayer.ui.play

import android.content.ComponentName
import android.media.session.MediaController
import android.net.Uri
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.msfakatsuki.musicplayer.MusicPlaybackService
import com.msfakatsuki.musicplayer.R
import com.msfakatsuki.musicplayer.databinding.MusicPlayUiFragmentBinding

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

        binding.btnTransfer.setOnClickListener {
            findNavController().navigate(R.id.action_musicPlayUIFragment_to_songItemFragment2)
        }

        viewModel.isServiceConnected.observe(viewLifecycleOwner, serviceConnectionObserver)

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


    val getContent = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri?->
        val mediaController = MediaControllerCompat.getMediaController(requireActivity())
        Log.println(Log.INFO,"get_content",uri.toString())
        val mdc:MediaDescriptionCompat = MediaDescriptionCompat.Builder().run {
            setMediaUri(uri)
            build()
        }
        mediaController.addQueueItem(mdc)
    }

}