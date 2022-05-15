package com.msfakatsuki.musicplayer.ui.list

import android.app.Activity
import android.content.DialogInterface
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.FrameLayout
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.WorkerThread
import androidx.documentfile.provider.DocumentFile
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.msfakatsuki.musicplayer.MusicApplication
import com.msfakatsuki.musicplayer.database.music.RoomMusicItem
import com.msfakatsuki.musicplayer.databinding.FragmentDbProcessedDialogBinding
import com.msfakatsuki.musicplayer.ui.play.MusicPlayUIViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*
import kotlin.collections.ArrayList

// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_DOCTREE_PATH = "dbprocessedDiag.arg.doctree.path"
private const val ARG_DB_IDLIST = "dbprocessedDiag.arg.db.idlist"
private const val ARG_PARCEL_LIST = "dbprocessedDiag.arg.db.parcellist"
/**
 * A simple [Fragment] subclass.
 * Use the [DbProcessedDialogFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class DbModifyDialogFragment :  DialogFragment() {

    private lateinit var binding : FragmentDbProcessedDialogBinding

    private var iconUri : Uri?=null

    private var parcelableList: ArrayList<RoomMusicItem>?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            parcelableList = it.getParcelableArrayList<RoomMusicItem>(ARG_PARCEL_LIST)
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
            requireActivity().lifecycleScope.launch(Dispatchers.IO) {
                modifyDatabase(
                    requireActivity(),
                    checkString(artist),
                    checkString(album)
                )
            }
            dismiss()
        }


        binding.etIconPath.setOnClickListener {
            getContent.launch("image/*")
        }


    }

    @WorkerThread
    private fun modifyDatabase(activity:Activity, pArtist : String?, pAlbum: String?) {
        parcelableList?.forEach { item->
            val newItem = RoomMusicItem(
                id = item.id,
                title = item.title,
                artist = pArtist?:item.artist,
                album = pAlbum?:item.album,
                sha256 = item.sha256,
                localMediaUri = item.localMediaUri,
                localIconUri = iconUri?.toString(),
                remoteLink = item.remoteLink
            )
            val application = activity.application as MusicApplication
            application.database.musicDao().update(newItem)
        }
    }

    override fun onResume() {
        super.onResume()
        val window: Window? = dialog?.window
        window?.setLayout(FrameLayout.LayoutParams.MATCH_PARENT,FrameLayout.LayoutParams.WRAP_CONTENT)
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        onDismissCallback?.onDismiss()
    }

    val getContent = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri?->
        uri?:return@registerForActivityResult
        iconUri = uri
        binding.etIconPath.text?.clear()
        binding.etIconPath.text?.append(uri.path)
    }

    public var onDismissCallback : OnDismissListener?=null

    interface OnDismissListener{
        abstract fun onDismiss()
    }

    companion object {

        @JvmStatic
        fun newInstance(dbIdList: ArrayList<RoomMusicItem>) =
            DbModifyDialogFragment().apply {
                arguments = Bundle().apply {
                    putParcelableArrayList(ARG_PARCEL_LIST,dbIdList)
                }
            }
    }
}