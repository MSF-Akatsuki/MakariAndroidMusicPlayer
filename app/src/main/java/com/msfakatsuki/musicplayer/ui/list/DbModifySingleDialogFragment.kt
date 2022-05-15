package com.msfakatsuki.musicplayer.ui.list

import android.app.Activity
import android.content.DialogInterface
import android.net.Uri
import android.os.Bundle
import android.os.Parcelable
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
import com.msfakatsuki.musicplayer.databinding.FragmentDbModifySingleBinding
import com.msfakatsuki.musicplayer.databinding.FragmentDbProcessedDialogBinding
import com.msfakatsuki.musicplayer.ui.play.MusicPlayUIViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*
import kotlin.collections.ArrayList

// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER

private const val ARG_PARCELABLE = "dbprocessedDiag.arg.db.parcelable"
/**
 * A simple [Fragment] subclass.
 * Use the [DbProcessedDialogFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class DbModifySingleDialogFragment :  DialogFragment() {

    private lateinit var binding : FragmentDbModifySingleBinding

    private var iconUri : Uri?=null

    private var parcelable: RoomMusicItem?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            parcelable = it.getParcelable<RoomMusicItem>(ARG_PARCELABLE)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentDbModifySingleBinding.inflate(inflater,container,false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {


        fun checkString(str:String) = if(str=="") null else str

        super.onViewCreated(view, savedInstanceState)

        binding.etTitleDbp.text?.append(parcelable?.title)
        binding.etArtistDbp.text?.append(parcelable?.artist)
        binding.etAlbumDbp.text?.append(parcelable?.album)

        binding.btnConfirmDbp.setOnClickListener {
            val title = binding.etTitleDbp.text.toString()
            val artist = binding.etArtistDbp.text.toString()
            val album = binding.etAlbumDbp.text.toString()
            val activity = requireActivity()
            activity.lifecycleScope.launch(Dispatchers.IO) {
                modifyDatabaseSingle(
                    activity,
                    title,
                    artist,
                    album,
                    iconUri?.toString()
                )
            }
            dismiss()
        }

        binding.etIconPath.setOnClickListener {
            getContent.launch("image/*")
        }


    }

    @WorkerThread
    private fun modifyDatabaseSingle(activity:Activity,pTitle:String, pArtist : String, pAlbum: String, pIconUri: String?) {
        parcelable?.let { item->
            val newItem = RoomMusicItem(
                id = item.id,
                title = pTitle,
                artist = pArtist,
                album = pAlbum,
                sha256 = item.sha256,
                localMediaUri = item.localMediaUri,
                localIconUri = pIconUri,
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
        fun newInstance(dbParcelable: RoomMusicItem) =
            DbModifySingleDialogFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(ARG_PARCELABLE,dbParcelable)
                }
            }
    }
}