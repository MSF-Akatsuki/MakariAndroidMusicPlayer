package com.msfakatsuki.musicplayer.ui.list

import android.content.Context
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.navGraphViewModels
import com.msfakatsuki.musicplayer.MusicApplication
import com.msfakatsuki.musicplayer.R
import com.msfakatsuki.musicplayer.database.music.RoomMusicItem
import com.msfakatsuki.musicplayer.databinding.FragmentDbSongCheckListBinding

/**
 * A fragment representing a list of Items.
 */
class DbSongCheckFragment : Fragment() {

    private var columnCount = 1

    private lateinit var binding : FragmentDbSongCheckListBinding

    private val factory: MusicFilterViewModelFactory = MusicFilterViewModelFactory()
    private val navViewModel: MusicFilterViewModel by navGraphViewModels(R.id.nav_db_list) {factory}


    private val listViewModel: DbSongListViewModel by activityViewModels {
        DbSongListViewModelFactory((activity?.application as MusicApplication).repository)
    }

    val adapter = DbSongCheckboxRecyclerViewAdapter()

    private var isUsed = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        isUsed = true
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.menu_db_check_list,menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when(item.itemId) {
            R.id.action_add_selected_to_playlist -> {
                adapter.addSelectedToPlaylist()
                true
            }
            R.id.action_modify_selected -> {
                isUsed = true
                val list = adapter.getSelectedList()
                val dialog = DbModifyDialogFragment.newInstance(list as ArrayList<RoomMusicItem>)

                dialog.onDismissCallback = object : DbModifyDialogFragment.OnDismissListener {
                    override fun onDismiss() {
                        this@DbSongCheckFragment.findNavController().navigateUp()
                    }
                }

                dialog.show(childFragmentManager,"DbModifyDialog")
                true
            }
            else -> super.onOptionsItemSelected(item)
        }

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setHasOptionsMenu(true)
        binding = FragmentDbSongCheckListBinding.inflate(inflater,container,false)
        val view = binding.list

        with(view) {
            layoutManager = when {
                columnCount <= 1 -> LinearLayoutManager(context)
                else -> GridLayoutManager(context, columnCount)
            }
            adapter = this@DbSongCheckFragment.adapter
        }
        this@DbSongCheckFragment.adapter.submitList(
            navViewModel.currentSongList?:listViewModel.songList.value
        )
        return view
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (isUsed) {
            findNavController().navigateUp()
        }
    }

    companion object {

        @JvmStatic
        fun newInstance(columnCount: Int) =
            DbSongCheckFragment().apply {
            }
    }
}