package com.msfakatsuki.musicplayer.ui.list

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.msfakatsuki.musicplayer.R
import com.msfakatsuki.musicplayer.databinding.FragmentMusicSelectBinding
import com.msfakatsuki.musicplayer.ui.play.MusicPlayUIViewModel

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

        /*
        binding.btnSwitchPlay.setOnClickListener {
            findNavController().navigate(R.id.action_musicSelectFragment_to_playerViewPagerFragment)
        }
        binding.btnScanSongs.setOnClickListener {
            getContent.launch(null)
        } */
        binding.bottomPlayerFragment.setOnClickListener {
            findNavController().navigate(R.id.action_musicSelectFragment_to_playerViewPagerFragment)
        }
    }

    val getContent = registerForActivityResult(ActivityResultContracts.OpenDocumentTree()) { treeUri: Uri?->
        treeUri?.let { treeUri
            val contentResolver = requireActivity().contentResolver
            val takeFlags: Int = Intent.FLAG_GRANT_READ_URI_PERMISSION or
                    Intent.FLAG_GRANT_WRITE_URI_PERMISSION
// Check for the freshest data.
            contentResolver.takePersistableUriPermission(treeUri, takeFlags)
            DbProcessedDialogFragment.newInstance(treeUri.toString()).show(
                childFragmentManager,"dbfixDialog"
            )
        }
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