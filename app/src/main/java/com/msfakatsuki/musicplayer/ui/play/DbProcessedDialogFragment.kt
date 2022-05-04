package com.msfakatsuki.musicplayer.ui.play

import android.app.Activity
import android.content.Context
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.FrameLayout
import androidx.annotation.WorkerThread
import androidx.documentfile.provider.DocumentFile
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.msfakatsuki.musicplayer.MusicApplication
import com.msfakatsuki.musicplayer.database.music.RoomMusicItem
import com.msfakatsuki.musicplayer.databinding.FragmentDbProcessedDialogBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_DOCTREE_PATH = "dbprocessedDiag.arg.doctree.path"

/**
 * A simple [Fragment] subclass.
 * Use the [DbProcessedDialogFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class DbProcessedDialogFragment :  DialogFragment() {
    // TODO: Rename and change types of parameters



    private lateinit var binding : FragmentDbProcessedDialogBinding
    private val viewModel: MusicPlayUIViewModel by activityViewModels()

    private var docTree : DocumentFile?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            try {
                docTree = DocumentFile.fromTreeUri(requireActivity().application,Uri.parse(it.getString(ARG_DOCTREE_PATH)))
            } catch (e:Exception) {
                Log.w("dbpdFrag","Dialog created with docTree null")
                docTree = null
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentDbProcessedDialogBinding.inflate(inflater,container,false)
        return binding.root
    }




    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        fun checkString(str:String) = if(str=="") null else str

        super.onViewCreated(view, savedInstanceState)
        binding.btnConfirmDbp.setOnClickListener {
            val artist = binding.etArtistDbp.text.toString()
            val album = binding.etAlbumDbp.text.toString()
            docTree?.let { docTree->
                requireActivity().lifecycleScope.launch(Dispatchers.IO) {
                    searchAudioInPath(requireActivity(),docTree, checkString(artist),checkString(album))
                }
            }
            dismiss()
        }

    }

    override fun onResume() {
        super.onResume()
        val window: Window? = dialog?.window
        window?.setLayout(FrameLayout.LayoutParams.MATCH_PARENT,FrameLayout.LayoutParams.WRAP_CONTENT)
    }

    @WorkerThread
    fun searchAudioInPath(activity:Activity, docTree : DocumentFile, pArtist : String?, pAlbum: String?) {

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
                val mediaUri = MediaStore.getMediaUri(activity,item.uri)
                if (mediaUri?.let { contentResolver.getType(it)?.startsWith("audio", ignoreCase = true) } ==true) {
                    mmr.setDataSource(activity,mediaUri)


                    val path = mediaUri.path

                    Log.i("fbpdFrag",path?:"NONE")

                    val cutS = path?.lastIndexOf('/')
                    val cutD = path?.lastIndexOf('.')
                    val filename : String = if (cutS!=null && cutD!=null && cutS!=-1 && cutD!=-1 )
                        path.substring(cutS+1,cutD)
                    else if(cutS!=null && cutS!=-1)
                        path.substring(cutS+1)
                    else "NULL"

                    val metaTitle = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE)
                    val metaAlbum = pAlbum?:mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM)
                    val metaArtist = pArtist?:mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST)

                    val title = metaTitle?:item.name?:"NULL"
                    val album = metaAlbum?:"NULL"
                    val artist = metaArtist?:"NULL"

                    val hasTitle = metaTitle!=null
                    val hasAlbum = metaAlbum!=null
                    val hasArtist = metaArtist!=null

                    var resultHex : String?=null
                    /*
                    contentResolver.openInputStream(mediaUri)?.use { stream->
                        val bytes = stream.readBytes()
                        val md = MessageDigest.getInstance("SHA-256")
                        val digest = md.digest(bytes)
                        resultHex = digest.fold("") { str, it -> str + "%02x".format(it) }
                    }

                     */

                    mediaUri.toString().let { path ->
                        val roomMusicItem = RoomMusicItem(0,title,artist,album,resultHex?:"",path,"")
                        Log.i("?sdvaew",roomMusicItem.localPath)
                        (activity.application as MusicApplication).repository.insert(item = roomMusicItem)
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
         * @return A new instance of fragment DbProcessedDialogFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(docTreePath: String) =
            DbProcessedDialogFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_DOCTREE_PATH, docTreePath)
                }
            }
    }
}