package com.msfakatsuki.musicplayer.ui.list

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import androidx.navigation.navGraphViewModels
import com.msfakatsuki.musicplayer.MusicApplication
import com.msfakatsuki.musicplayer.R
import com.msfakatsuki.musicplayer.databinding.FragmentMusicSearchBinding
import com.msfakatsuki.musicplayer.databinding.FragmentMusicSearchlistBinding
import com.msfakatsuki.musicplayer.ui.play.placeholder.PlaceholderContent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * A fragment representing a list of Items.
 */
class MusicSearchFragment : Fragment() {

    private var columnCount = 1
    private lateinit var binding: FragmentMusicSearchlistBinding

    private val factory: MusicFilterViewModelFactory = MusicFilterViewModelFactory()
    private val navViewModel: MusicFilterViewModel by navGraphViewModels(R.id.nav_db_list) {factory}
    private val adapter = MusicSearchRecyclerViewAdapter()

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

        binding = FragmentMusicSearchlistBinding.inflate(inflater,container,false)

        val view = binding.list
        with(view) {
            layoutManager = when {
                columnCount <= 1 -> LinearLayoutManager(context)
                else -> GridLayoutManager(context, columnCount)
            }
            adapter = this@MusicSearchFragment.adapter
        }



        return binding.root
    }

    fun changeStateTo(){}

    companion object {

        // TODO: Customize parameter argument names
        const val ARG_COLUMN_COUNT = "column-count"

        // TODO: Customize parameter initialization
        @JvmStatic
        fun newInstance(columnCount: Int) =
            MusicSearchFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_COLUMN_COUNT, columnCount)
                }
            }
    }
}