package com.msfakatsuki.musicplayer.ui.play

import android.app.Activity
import android.net.Uri
import android.os.Bundle
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.session.MediaControllerCompat
import android.util.Log
import android.view.*
import android.widget.AdapterView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.fragment.app.activityViewModels
import com.msfakatsuki.musicplayer.MusicApplication
import com.msfakatsuki.musicplayer.R
import com.msfakatsuki.musicplayer.databinding.FragmentDbsongListBinding
import com.msfakatsuki.musicplayer.util.view.ContextMenuRecyclerView

/**
 * A fragment representing a list of Items.
 */
class DbSongListFragment : Fragment() {

    private val LOG_TAG = "dblFrag"

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

        registerForContextMenu(binding.list)

        return view
    }

    override fun onCreateContextMenu(
        menu: ContextMenu,
        v: View,
        menuInfo: ContextMenu.ContextMenuInfo?
    ) {
        Log.i(LOG_TAG,"onCreateContextMenu")
        super.onCreateContextMenu(menu, v, menuInfo)
        val inflater: MenuInflater = requireActivity().menuInflater
        inflater.inflate(R.menu.menu_dblist_item_on_hold,menu)
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        Log.i(LOG_TAG,"onContextItemSelected")
        val info = item.menuInfo as ContextMenuRecyclerView.RecyclerViewContextMenuInfo
        return when (item.itemId) {
            R.id.add_to_playlist_mitem -> {
                addToPlaylistByAdapterPosition(info.position)
                true
            }
            R.id.modify_mitem -> {
                modifyByAdapterPosition(info.position)
                true
            }
            else -> super.onContextItemSelected(item)
        }
    }

    fun addToPlaylistByAdapterPosition(aPosition: Int) {
        val vh = binding.list.findViewHolderForAdapterPosition(aPosition) as DbSongRecyclerViewAdapter.ViewHolder?
        val bindedSongItem = vh?.bindedSongItem

        bindedSongItem?.let {
            val extra = Bundle()
            extra.putString("artist",it.artist)
            extra.putString("album",it.album)
            extra.putLong("id",it.id.toLong())
            Log.i("dbSRVAdap",it.localPath)
            MediaControllerCompat.getMediaController(requireActivity())?.addQueueItem(
                MediaDescriptionCompat.Builder().run {
                    setMediaUri(Uri.parse(it.localPath))
                    setTitle(it.title)
                    setExtras(extra)
                    build()
                }
            )
        }
    }

    fun modifyByAdapterPosition(aPosition:Int) {
        val vh = binding.list.findViewHolderForAdapterPosition(aPosition) as DbSongRecyclerViewAdapter.ViewHolder?
        val bindedSongItem = vh?.bindedSongItem

        Log.i("sacawc","Modify")
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