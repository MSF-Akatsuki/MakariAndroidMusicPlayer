package com.msfakatsuki.musicplayer.ui.play

import android.media.MediaMetadataRetriever
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.content.res.AppCompatResources.getDrawable
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.NavController
import com.bumptech.glide.Glide
import com.msfakatsuki.musicplayer.MusicPlaybackService
import com.msfakatsuki.musicplayer.R
import com.msfakatsuki.musicplayer.databinding.FragmentBottomPlayerBinding

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [BottomPlayerFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class BottomPlayerFragment : Fragment() {

    private lateinit var binding : FragmentBottomPlayerBinding
    private val viewModel: MusicPlayUIViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentBottomPlayerBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val serviceConnectionObserver = Observer<Boolean> {
            if (it) {
                registerTransportControls()
            } else {
                unregisterTransportControls()
            }
        }
        val mediaController = MediaControllerCompat.getMediaController(requireActivity())
        mediaController.registerCallback(controllerCallbacks)


        viewModel.isServiceConnected.observe(viewLifecycleOwner, serviceConnectionObserver)

        viewModel.mediaBrowser.subscribe(
            viewModel.mediaBrowser.root,
            Bundle().apply {
                putBoolean(MusicPlaybackService.HINT_IS_PLAYER,true)
            },subscriptionCallback
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Log.println(Log.INFO,"mpuiF","onDestroyView")
        val mediaController = MediaControllerCompat.getMediaController(requireActivity())
        mediaController.unregisterCallback(controllerCallbacks)
        viewModel.mediaBrowser.unsubscribe(viewModel.mediaBrowser.root)
    }

    private fun unregisterTransportControls() {
        binding.btnPlay.setOnClickListener(null)
        binding.btnPause.setOnClickListener(null)
        binding.btnSkipNext.setOnClickListener(null)
    }

    fun registerTransportControls() {
        val mediaController = MediaControllerCompat.getMediaController(requireActivity())
        binding.btnPlay.setOnClickListener {
            mediaController.transportControls.play()
        }

        binding.btnPause.setOnClickListener {
            mediaController.transportControls.pause()
        }
        binding.btnSkipNext.setOnClickListener {
            mediaController.transportControls.skipToNext()
        }
    }

    val mmr = MediaMetadataRetriever()

    val subscriptionCallback  = object : MediaBrowserCompat.SubscriptionCallback() {

        override fun onChildrenLoaded(
            parentId: String,
            children: MutableList<MediaBrowserCompat.MediaItem>,
            options: Bundle
        ) {
            super.onChildrenLoaded(parentId, children, options)
            try {
                val description = children[0].description
                binding.tvPlayUiAlbum.text = description.extras?.getString("album")
                binding.tvPlayUiArtist.text = description.extras?.getString("artist")
                binding.tvPlayUiTitle.text = description.title
                description.iconUri?.let {
                    Glide.with(requireContext()).load(description.iconUri).into(binding.ivMediaIcon)
                }?: kotlin.run {
                    mmr.setDataSource(requireContext(),description.mediaUri)
                    val pic = mmr.embeddedPicture?:ByteArray(0)
                    if (pic.isNotEmpty()) {
                        Glide.with(requireContext()).load(pic).into(binding.ivMediaIcon)
                    } else {
                        Glide.with(requireContext()).load(getDrawable(requireContext(),R.drawable.uniform_noise)).into(binding.ivMediaIcon)
                    }
                }

            }catch (e:Exception) {
                Log.e("mpuiFrag",e.message?:"NO MESSAGE")
            }
        }
    }


    private var controllerCallbacks = object  : MediaControllerCompat.Callback() {
        override fun onMetadataChanged(metadata: MediaMetadataCompat?) {
            Log.i("oMDcgd","onMetadataChanged ${metadata.toString()}")
            metadata?.let{
                viewModel.duration = it.getLong(MediaMetadataCompat.METADATA_KEY_DURATION) as Long
                binding.progressBar.duration = viewModel.duration
                binding.progressBar.invalidate()

                try {
                    val description = it.description
                    binding.tvPlayUiAlbum.text = it.getString(MediaMetadataCompat.METADATA_KEY_ALBUM)
                    binding.tvPlayUiArtist.text = it.getString(MediaMetadataCompat.METADATA_KEY_ARTIST)
                    binding.tvPlayUiTitle.text = description.title
                    description.iconUri?.let {
                        Glide.with(requireContext()).load(description.iconUri).into(binding.ivMediaIcon)
                    }?: kotlin.run {
                        mmr.setDataSource(requireContext(),description.mediaUri)
                        val pic = mmr.embeddedPicture?:ByteArray(0)
                        if (pic.isNotEmpty()) {
                            Glide.with(requireContext()).load(pic).into(binding.ivMediaIcon)
                        } else {
                            Glide.with(requireContext()).load(getDrawable(requireContext(),R.drawable.uniform_noise)).into(binding.ivMediaIcon)
                        }
                    }

                }catch (e:Exception) {
                    Log.e("mpuiFrag",e.message?:"NO MESSAGE")
                }

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

    companion object {

        @JvmStatic
        fun newInstance() =
            BottomPlayerFragment().apply {
                arguments = Bundle().apply {
                }
            }
    }
}