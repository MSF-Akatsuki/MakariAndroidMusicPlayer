package com.msfakatsuki.musicplayer.ui.play

import android.annotation.SuppressLint
import android.content.ContentUris
import android.content.Intent
import android.database.Cursor
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.session.MediaControllerCompat
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.annotation.WorkerThread
import androidx.core.net.toFile
import androidx.core.net.toUri
import androidx.documentfile.provider.DocumentFile
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.msfakatsuki.musicplayer.MusicApplication
import com.msfakatsuki.musicplayer.R
import com.msfakatsuki.musicplayer.database.music.RoomMusicItem
import com.msfakatsuki.musicplayer.databinding.FragmentMusicSelectBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.security.MessageDigest
import java.util.*

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [MusicSelectFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class MusicSelectFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private lateinit var binding : FragmentMusicSelectBinding
    private val viewModel: MusicPlayUIViewModel by activityViewModels()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentMusicSelectBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.fileReadProcessNumber.observe(viewLifecycleOwner) {
            Log.println(Log.INFO,"mslctFrag","Observe vlco change:${it}")
        }

        binding.btnSwitchPlay.setOnClickListener {
            findNavController().navigate(R.id.action_musicSelectFragment_to_musicPlayUIFragment)
        }
        binding.btnScanSongs.setOnClickListener {
            getContent.launch(null)
        }
    }

    val getContent = registerForActivityResult(ActivityResultContracts.OpenDocumentTree()) { treeUri: Uri?->

        val docTree = DocumentFile.fromTreeUri(requireActivity().application, treeUri!!)
        val docUriTree = docTree?.uri
        docUriTree?.path?.let {
            Log.println(Log.INFO,"mslctFrag","get path :${it}")
        }
        docTree?.let {

            lifecycleScope.launch(Dispatchers.IO) {
                searchAudioInPath(it)
            }

        }

    }

    @WorkerThread
    fun searchAudioInPath(docTree : DocumentFile) {

        viewModel.fileReadProcessNumber.postValue(viewModel.fileReadProcessNumber.value?:0 + 1)
        viewModel.fileDbProcessFlag.postValue(true)

        val queue : Queue<DocumentFile> = LinkedList<DocumentFile>()
        queue.add(docTree)
        val mmr = MediaMetadataRetriever()
        val contentResolver = requireActivity().contentResolver
        while (queue.isNotEmpty()) {
            val item = queue.poll() ?: continue
            if (item.isDirectory) {
                item.listFiles().forEach {
                    queue.add(it)
                }
            } else if (item.isFile) {
                val mediaUri = MediaStore.getMediaUri(requireActivity(),item.uri)
                if (mediaUri?.let { contentResolver.getType(it)?.startsWith("audio", ignoreCase = true) } ==true) {
                    mmr.setDataSource(requireActivity(),mediaUri)

                    val title = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE)?:"NULL"
                    val album = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM)?:"NULL"
                    val artist = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST)?:"NULL"


                    var resultHex : String?=null
                    /*
                    contentResolver.openInputStream(mediaUri)?.use { stream->
                        val bytes = stream.readBytes()
                        val md = MessageDigest.getInstance("SHA-256")
                        val digest = md.digest(bytes)
                        resultHex = digest.fold("") { str, it -> str + "%02x".format(it) }
                    }

                     */

                    mediaUri.path?.let { path ->
                        val roomMusicItem = RoomMusicItem(0,title,artist,album,resultHex?:"",path,"")
                        (requireActivity().application as MusicApplication).repository.insert(item = roomMusicItem)
                    }
                }
            }

        }
        viewModel.fileReadProcessNumber.postValue(viewModel.fileReadProcessNumber.value?:1 - 1)

    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment MusicSelectFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            MusicSelectFragment().apply {

            }
    }



}