package com.msfakatsuki.musicplayer.ui.list

import android.content.res.Resources
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.navGraphViewModels
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.msfakatsuki.musicplayer.MusicApplication
import com.msfakatsuki.musicplayer.R
import com.msfakatsuki.musicplayer.database.music.RoomArtistItem
import com.msfakatsuki.musicplayer.databinding.FragmentMusicFilterBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [MusicFilterFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class MusicFilterFragment : Fragment() {

    private lateinit var binding : FragmentMusicFilterBinding

    private val factory: MusicFilterViewModelFactory = MusicFilterViewModelFactory()
    private val navViewModel: MusicFilterViewModel by navGraphViewModels(R.id.nav_db_list) {factory}

    private val adapter = MusicSearchRecyclerViewAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        navViewModel.reset()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentMusicFilterBinding.inflate(inflater,container,false)

        val view = binding.filterIncluded.list
        with(view) {
            adapter = this@MusicFilterFragment.adapter
        }
        val application = requireActivity().application as MusicApplication

        lifecycleScope.launch(Dispatchers.IO) {
            val artistList  = application.database.musicDao().getAllArtist()
            adapter.submitList(artistList)
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.filterConfirm.setOnClickListener {
            SyncList()
            findNavController().navigateUp()
        }


        binding.filterSwitch.setOnClickListener {
            SyncList()
            val application = requireActivity().application as MusicApplication
            when(navViewModel.displayState){
                MusicFilterViewModel.DISPLAY_ARTIST_LIST -> {
                    binding.filterSwitch.text = "Cancel"

                    if (navViewModel.selectedArtistList.value!=null){
                        val list= adapter.getSelectedList()
                        navViewModel.displayState = MusicFilterViewModel.DISPLAY_ALBUM_LIST_BY_ARTISTS

                        lifecycleScope.launch(Dispatchers.IO) {
                            val strList = Array<String>(list.size) { list.get(it).toString() }
                            val albumList  = application.database.musicDao().getAlbumByArtistsList(strList)
                            adapter.submitList(albumList)
                        }
                    } else {
                        navViewModel.displayState = MusicFilterViewModel.DISPLAY_ALBUM_LIST
                        lifecycleScope.launch(Dispatchers.IO) {
                            val albumList  = application.database.musicDao().getAllAlbum()
                            adapter.submitList(albumList)
                        }
                    }
                }
                MusicFilterViewModel.DISPLAY_ALBUM_LIST -> {
                    navViewModel.displayState = MusicFilterViewModel.DO_NOTHING
                    findNavController().navigateUp()
                }
                MusicFilterViewModel.DISPLAY_ALBUM_LIST_BY_ARTISTS -> {
                    navViewModel.displayState = MusicFilterViewModel.DO_NOTHING
                    findNavController().navigateUp()
                }
            }
        }
    }

    fun SyncList(){
        val list = adapter.getSelectedList()
        when(navViewModel.displayState) {
            MusicFilterViewModel.DISPLAY_ARTIST_LIST -> {
                navViewModel.selectedArtistList.value=if (list.size>0) list else null
            }
            MusicFilterViewModel.DISPLAY_ALBUM_LIST -> {
                navViewModel.selectedAlbumList.value=if (list.size>0) list else null
            }
            MusicFilterViewModel.DISPLAY_ALBUM_LIST_BY_ARTISTS -> {
                navViewModel.selectedAlbumList.value=if (list.size>0) list else null
            }
        }
    }


    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment MusicFilterFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            MusicFilterFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}