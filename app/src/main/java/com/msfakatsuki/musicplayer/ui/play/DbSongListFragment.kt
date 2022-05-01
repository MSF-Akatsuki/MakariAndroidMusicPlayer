package com.msfakatsuki.musicplayer.ui.play

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import com.msfakatsuki.musicplayer.MusicApplication
import com.msfakatsuki.musicplayer.databinding.FragmentDbsongListBinding

/**
 * A fragment representing a list of Items.
 */
class DbSongListFragment : Fragment() {

    private var columnCount = 1
    private lateinit var binding : FragmentDbsongListBinding
    private val adapter =  DbSongRecyclerViewAdapter()

    private val listViewModel: DbSongListViewModel by activityViewModels {
        DbSongListViewModelFactory((activity?.application as MusicApplication).repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            columnCount = it.getInt(ARG_COLUMN_COUNT)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentDbsongListBinding.inflate(inflater, container, false)
        val view = binding.root
        // Set the adapter
        with(view) {
            layoutManager = when {
                columnCount <= 1 -> LinearLayoutManager(context)
                else -> GridLayoutManager(context, columnCount)
            }
            adapter = this@DbSongListFragment.adapter

            listViewModel.songList.observe(viewLifecycleOwner) { itemList ->
                itemList.let { this@DbSongListFragment.adapter.submitList(it) }
            }
        }
        return view
    }

    companion object {

        // TODO: Customize parameter argument names
        const val ARG_COLUMN_COUNT = "column-count"

        // TODO: Customize parameter initialization
        @JvmStatic
        fun newInstance(columnCount: Int) =
            DbSongListFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_COLUMN_COUNT, columnCount)
                }
            }
    }
}