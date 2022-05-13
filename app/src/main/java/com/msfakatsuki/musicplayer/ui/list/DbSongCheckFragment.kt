package com.msfakatsuki.musicplayer.ui.list

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.fragment.app.activityViewModels
import com.msfakatsuki.musicplayer.MusicApplication
import com.msfakatsuki.musicplayer.R
import com.msfakatsuki.musicplayer.databinding.FragmentDbSongCheckListBinding

/**
 * A fragment representing a list of Items.
 */
class DbSongCheckFragment : Fragment() {

    private var columnCount = 1

    private lateinit var binding : FragmentDbSongCheckListBinding

    private val listViewModel: DbSongListViewModel by activityViewModels {
        DbSongListViewModelFactory((activity?.application as MusicApplication).repository)
    }

    val adapter = DbSongCheckboxRecyclerViewAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            columnCount = it.getInt(ARG_COLUMN_COUNT)
        }
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
        this@DbSongCheckFragment.adapter.submitList(listViewModel.songList.value)
        return view
    }

    companion object {

        // TODO: Customize parameter argument names
        const val ARG_COLUMN_COUNT = "column-count"

        // TODO: Customize parameter initialization
        @JvmStatic
        fun newInstance(columnCount: Int) =
            DbSongCheckFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_COLUMN_COUNT, columnCount)
                }
            }
    }
}