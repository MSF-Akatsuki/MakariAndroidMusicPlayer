package com.msfakatsuki.musicplayer.ui.list

import android.net.Uri
import android.os.Bundle
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.session.MediaControllerCompat
import android.util.Log
import android.view.*
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.navGraphViewModels
import com.msfakatsuki.musicplayer.MusicApplication
import com.msfakatsuki.musicplayer.R
import com.msfakatsuki.musicplayer.databinding.FragmentDbsongListBinding
import com.msfakatsuki.musicplayer.util.view.ContextMenuRecyclerView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * A fragment representing a list of Items.
 */
class DbSongListFragment : Fragment() {

    private val LOG_TAG = "dblFrag"

    private var columnCount = 1
    private lateinit var binding : FragmentDbsongListBinding
    private val adapter =  DbSongRecyclerViewAdapter()

    private val factory: MusicFilterViewModelFactory = MusicFilterViewModelFactory()
    private val navViewModel: MusicFilterViewModel by navGraphViewModels(R.id.nav_db_list) {factory}

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
        setHasOptionsMenu(true)
        binding = FragmentDbsongListBinding.inflate(inflater, container, false)
        val view = binding.root


        with(view) {
            layoutManager = when {
                columnCount <= 1 -> LinearLayoutManager(context)
                else -> GridLayoutManager(context, columnCount)
            }
            adapter = this@DbSongListFragment.adapter
        }

        listViewModel.songList.observe(viewLifecycleOwner) { itemList ->
            if (navViewModel.displayState==MusicFilterViewModel.DO_NOTHING)
                itemList?.let { this@DbSongListFragment.adapter.submitList(it) }
        }

        navViewModel.selectedArtistList.observe(viewLifecycleOwner) { itemList ->
            if (itemList != null && navViewModel.displayState==MusicFilterViewModel.DISPLAY_ARTIST_LIST) {
                lifecycleScope.launch(Dispatchers.IO) {
                    val application = requireActivity().application as MusicApplication
                    val strList = Array<String>(itemList.size) { itemList.get(it).toString() }
                    val list = application.database.musicDao().getMusicByArtistsList(strList)
                    this@DbSongListFragment.adapter.submitList(list)
                }
            }
        }

        navViewModel.selectedAlbumList.observe(viewLifecycleOwner) { itemList ->
            if (itemList!= null
                && navViewModel.displayState==MusicFilterViewModel.DISPLAY_ALBUM_LIST
                || navViewModel.displayState==MusicFilterViewModel.DISPLAY_ALBUM_LIST_BY_ARTISTS) {
                lifecycleScope.launch(Dispatchers.IO) {
                    val application = requireActivity().application as MusicApplication
                    val strList = Array<String>(itemList.size) { itemList.get(it).toString() }
                    val list = application.database.musicDao().getMusicByAlbumsList(strList)
                    this@DbSongListFragment.adapter.submitList(list)
                }
            }
        }


        registerForContextMenu(binding.list)
        return view
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.menu_appbar,menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when(item.itemId) {
            R.id.action_switch -> {
                adapter.checkAll()
                true
            }
            R.id.action_add_to_playlist -> {
                adapter.addAllToPlaylist()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
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