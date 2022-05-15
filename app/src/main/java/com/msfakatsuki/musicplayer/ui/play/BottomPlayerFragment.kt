package com.msfakatsuki.musicplayer.ui.play

import android.content.Context
import android.media.MediaMetadataRetriever
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
import androidx.appcompat.content.res.AppCompatResources.getDrawable
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
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

        Glide.with(requireActivity()).load(getDrawable(requireActivity(),R.drawable.uniform_noise)).into(binding.ivMediaIcon)

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

        viewModel.songDescription.observe(viewLifecycleOwner){ metadata ->
            metadata?.let {
                onMetadataChanged(metadata)
            }
        }

    }

    override fun onStart() {
        super.onStart()
        viewModel.mediaBrowser.subscribe(
            viewModel.mediaBrowser.root,
            Bundle().apply {
                putBoolean(MusicPlaybackService.HINT_IS_PLAYER,true)
            },subscriptionBPCallback
        )
    }

    override fun onStop() {
        super.onStop()
        viewModel.mediaBrowser.unsubscribe(
            viewModel.mediaBrowser.root)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
    }

    override fun onDetach() {
        super.onDetach()
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

    fun shortenString(str: String?, ub: Int) : String? {
        return if (str!=null) {
            if (str.length>ub)
                str.subSequence(0,ub).padEnd(3,'.').toString()
            else
                str
        } else null
    }

    val mmr = MediaMetadataRetriever()

    private val subscriptionBPCallback  = object : MediaBrowserCompat.SubscriptionCallback() {

        override fun onChildrenLoaded(
            parentId: String,
            children: MutableList<MediaBrowserCompat.MediaItem>,
            options: Bundle
        ) {
            super.onChildrenLoaded(parentId, children, options)

        }

        override fun onChildrenLoaded(
            parentId: String,
            children: MutableList<MediaBrowserCompat.MediaItem>
        ) {
            super.onChildrenLoaded(parentId, children)
        }

        override fun onError(parentId: String) {
            super.onError(parentId)
            Log.e("bpFrag","error")
        }

        override fun onError(parentId: String, options: Bundle) {
            super.onError(parentId, options)
            Log.e("bpFrag","error")
        }
    }

    fun observeOnDescriptionChange(description:MediaDescriptionCompat) {
        try {
            binding.tvPlayUiAlbum.text = shortenString( description.extras?.getString("album"), 7 )
            binding.tvPlayUiArtist.text = shortenString( description.extras?.getString("artist"), 7)
            binding.tvPlayUiTitle.text = shortenString( description.title.toString(), 10)
            description.iconUri?.let {
                Glide.with(requireActivity()).asBitmap().load(description.iconUri).into(binding.ivMediaIcon)
            }?: kotlin.run {
                mmr.setDataSource(requireActivity(),description.mediaUri)
                val pic = mmr.embeddedPicture?:ByteArray(0)
                if (pic.isNotEmpty()) {
                    Glide.with(requireActivity()).asBitmap().load(pic).into(binding.ivMediaIcon)
                } else {
                    Glide.with(requireActivity()).load(getDrawable(requireActivity(),R.drawable.uniform_noise)).into(binding.ivMediaIcon)
                }
            }

        }catch (e:Exception) {
            Log.e("mpuiFrag",e.message?:"NO MESSAGE")
        }
    }

    fun onMetadataChanged(metadata: MediaMetadataCompat?) {
        Log.i("oMDcgd","onMetadataChanged ${metadata.toString()}")
        metadata?.let{
            viewModel.duration = it.getLong(MediaMetadataCompat.METADATA_KEY_DURATION) as Long
            binding.progressBar.duration = viewModel.duration
            binding.progressBar.invalidate()

            try {
                val description = it.description
                binding.tvPlayUiAlbum.text = shortenString( it.getString(MediaMetadataCompat.METADATA_KEY_ALBUM), 7)
                binding.tvPlayUiArtist.text = shortenString( it.getString(MediaMetadataCompat.METADATA_KEY_ARTIST), 7)
                binding.tvPlayUiTitle.text = shortenString(description.title.toString(), 10)
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