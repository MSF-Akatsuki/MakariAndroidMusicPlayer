package com.msfakatsuki.musicplayer.ui.list

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.session.MediaControllerCompat
import android.util.Log
import android.view.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.graphics.drawable.toIcon
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.navGraphViewModels
import com.msfakatsuki.musicplayer.MusicApplication
import com.msfakatsuki.musicplayer.R
import com.msfakatsuki.musicplayer.databinding.FragmentDbsongListBinding
import com.msfakatsuki.musicplayer.util.ContextMenuRecyclerView
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
            if (navViewModel.isFiltered && itemList != null && navViewModel.displayState==MusicFilterViewModel.DISPLAY_ARTIST_LIST) {
                lifecycleScope.launch(Dispatchers.IO) {
                    val application = requireActivity().application as MusicApplication
                    val strList = Array<String>(itemList.size) { itemList.get(it).toString() }
                    val list = application.database.musicDao().getMusicByArtistsList(strList)
                    this@DbSongListFragment.adapter.submitList(list)
                }
            }
        }

        navViewModel.selectedAlbumList.observe(viewLifecycleOwner) { itemList ->
            Log.i("wthtah",navViewModel.isFiltered.toString())
            if (navViewModel.isFiltered && itemList!= null
                && ( navViewModel.displayState==MusicFilterViewModel.DISPLAY_ALBUM_LIST
                || navViewModel.displayState==MusicFilterViewModel.DISPLAY_ALBUM_LIST_BY_ARTISTS)) {
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
                navViewModel.currentSongList = adapter.currentList
                findNavController().navigate(R.id.action_musicSelectFragment_to_dbSongCheckFragment)
                true
            }
            R.id.action_add_to_playlist -> {
                adapter.addAllToPlaylist()
                true
            }
            R.id.action_scan_directory -> {
                getContent.launch(null)
                true
            }
            R.id.action_clear_filter -> {
                navViewModel.isFiltered = false
                navViewModel.displayState = MusicFilterViewModel.DO_NOTHING
                listViewModel.songList.value.let { itemList ->
                    if (navViewModel.displayState==MusicFilterViewModel.DO_NOTHING)
                        itemList?.let { this@DbSongListFragment.adapter.submitList(it) }
                }
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onResume() {
        super.onResume()
        navViewModel.selectedArtistList.value.let { itemList ->
            if (navViewModel.isFiltered && itemList != null && navViewModel.displayState==MusicFilterViewModel.DISPLAY_ARTIST_LIST) {
                lifecycleScope.launch(Dispatchers.IO) {
                    val application = requireActivity().application as MusicApplication
                    val strList = Array<String>(itemList.size) { itemList.get(it).toString() }
                    val list = application.database.musicDao().getMusicByArtistsList(strList)
                    this@DbSongListFragment.adapter.submitList(list)
                }
            }
        }

        navViewModel.selectedAlbumList.value.let { itemList ->
            Log.i("wthtah",navViewModel.isFiltered.toString())
            if (navViewModel.isFiltered && itemList!= null
                && ( navViewModel.displayState==MusicFilterViewModel.DISPLAY_ALBUM_LIST
                        || navViewModel.displayState==MusicFilterViewModel.DISPLAY_ALBUM_LIST_BY_ARTISTS)) {
                lifecycleScope.launch(Dispatchers.IO) {
                    val application = requireActivity().application as MusicApplication
                    val strList = Array<String>(itemList.size) { itemList.get(it).toString() }
                    val list = application.database.musicDao().getMusicByAlbumsList(strList)
                    this@DbSongListFragment.adapter.submitList(list)
                }
            }
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
            Log.i("dbSRVAdap",it.localMediaUri?:"NULL")
            Log.i("dbSRVAdap",Uri.parse(it.localMediaUri).toIcon().uri.toString())
            MediaControllerCompat.getMediaController(requireActivity())?.addQueueItem(
                MediaDescriptionCompat.Builder().run {
                    setMediaUri(Uri.parse(it.localMediaUri))
                    it.localIconUri?.let {icon-> setIconUri(Uri.parse(icon))}?:setIconUri(null)
                    setTitle(it.title)
                    setExtras(extra)
                    setMediaId(it.id.toString())
                    build()
                }
            )
        }
    }

    fun modifyByAdapterPosition(aPosition:Int) {
        val vh = binding.list.findViewHolderForAdapterPosition(aPosition) as DbSongRecyclerViewAdapter.ViewHolder?
        val bindedSongItem = vh?.bindedSongItem

        val dialog = bindedSongItem?.let { DbModifySingleDialogFragment.newInstance(it) }
        dialog?.show(childFragmentManager,"DbModiftSDialog")

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